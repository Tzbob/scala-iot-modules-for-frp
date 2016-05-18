package be.kuleuven.LMS_extensions

import scala.lms.common.{CGenEffect, EffectExp, Base}
import scala.reflect.SourceContext

trait Modules extends Base {

  def declare_module(modName: String): Rep[Unit]
  def systemInits(): Rep[Unit]
  def moduleDeploy(modNames: List[String]): Rep[Unit]
  def connectionDeploy(modOut: String, outfun: Rep[_], modIn: String, infun: Rep[_]): Rep[Unit]
  def registerButton(id: Int, cb: Rep[(Int)=>Unit]): Rep[Unit]
  def eventLoop(buttonHandler: Boolean, timerHandler: Boolean): Rep[Unit]
  def headers(): Rep[Unit]
  def enableInterrupts(): Rep[Unit]
  def disableInterrupts(): Rep[Unit]
  def sizeof(s: Rep[_])(implicit pos: SourceContext, tIn:Typ[Int]): Rep[Int]

  def printIntToLCD(i: Rep[Int]): Rep[Unit]

}

trait ModulesExp extends Modules with ExpressionsExt with EffectExp {

  case class ModuleDecl(name: String) extends Def[Unit]
  case class GlobalInit() extends Def[Unit]
  case class ModuleDeploy(modNames: List[String]) extends Def[Unit]
  case class ConnectionDeploy(modOut: String, outfun: Exp[_], modIn: String, infun: Exp[_]) extends Def[Unit]
  case class RegisterButton(id:Int, cb: Rep[(Int)=>Unit]) extends Def[Unit]
  case class EventLoop(buttonHandler: Boolean, timerHandler: Boolean) extends Def[Unit]
  case class Headers() extends Def[Unit]
  case class PrintIntLCD(i: Rep[Int]) extends Def[Unit]
  case class DisInts() extends Def[Unit]
  case class EnInts() extends Def[Unit]
  case class Sizeof(s:Exp[_]) extends Def[Int]

  override def printIntToLCD(i: Rep[Int]): Exp[Unit] = {
    reflectEffect(new PrintIntLCD(i))
    Const()
  }

  override def declare_module(n: String): Exp[Unit] = {
    reflectEffect(new ModuleDecl(n))
    Const()
  }

  override def systemInits(): Exp[Unit] = {
    reflectEffect(new GlobalInit())
    Const()
  }

  override def moduleDeploy(modNames: List[String]): Exp[Unit] = {
    reflectEffect(new ModuleDeploy(modNames))
    Const()
  }

  override def connectionDeploy(modOut: String, outfun: Exp[_], modIn: String, infun: Exp[_]): Exp[Unit] = {
    reflectEffect(new ConnectionDeploy(modOut, outfun, modIn, infun))
    Const()
  }

  override def registerButton(id: Int, cb: Exp[(Int)=>Unit]): Exp[Unit] = {
    reflectEffect(new RegisterButton(id,cb))
    Const()
  }

  override def eventLoop(buttonHandler: Boolean, timerHandler: Boolean): Exp[Unit] = {
    reflectEffect(new EventLoop(buttonHandler,timerHandler))
    Const()
  }

  override def headers(): Exp[Unit] = {
    reflectEffect(new Headers())
    Const()
  }

  override def enableInterrupts(): Exp[Unit] = {
    reflectEffect(new EnInts())
    Const()
  }

  override def disableInterrupts(): Exp[Unit] = {
    reflectEffect(new DisInts())
    Const()
  }

  override def sizeof(s: Exp[_])(implicit pos: SourceContext, tIn: Typ[Int]): Exp[Int] = {
    reflectEffect(Sizeof(s))
  }
}


trait CGenModules extends CGenEffect with SMCLikeCodeGen {
  val IR: ModulesExp
  import IR._

  override def emitNode(sym: Sym[Any], rhs: Def[Any]) = rhs match {
    case ModuleDecl(n) => // no modules in standard C
    case GlobalInit() => // nothing
    case ModuleDeploy(l) => // nothing
    case ConnectionDeploy(a,b,c,d) => // nothing
    case RegisterButton(_,_) => //nothing
    case EventLoop(bH, tH) => // nothing
    case Headers() =>
      stream.println(
        "#include <stdio.h>\n" +
        "#include <stdlib.h>\n" +
        "#include <string.h>\n" +
        "#include <stdint.h>\n" +
        "#include <math.h>\n" +
        "#include <stdbool.h>\n"
      )
    case PrintIntLCD(i) => stream.println("printf(\"%d \", " + quote(i) + ");")
    case EnInts() => // nothing
    case DisInts() => // nothing
    case Sizeof(s) => stream.println("size_t " + quote(sym) +  " = sizeof(" + quote(s) + ");")
    case _ => super.emitNode(sym, rhs)
  }
}
trait SMCGenModules extends CGenEffect with SMCLikeCodeGen {
  val IR: ModulesExp
  import IR._

  override def emitNode(sym: Sym[Any], rhs: Def[Any]) = rhs match {
    case ModuleDecl(n) => stream.println("DECLARE_SM(" + n + ", 0x1234);")
    case GlobalInit() => stream.println(
      "  //INIT FUNCTION\n" +
        "  WDTCTL = WDTHOLD | WDTPW;\n" +
        "  uart_init();\n" +
        "  pmodcls_init();\n" +
        "  pmodcls_set_wrap_mode(PmodClsWrapAt16);\n" +
        "  buttons_init();\n" +
        "  asm(\"eint\");"
    )
    case ModuleDeploy(l) => {
      val strB = new StringBuilder
      strB.append("  //DEPLOY FUNCTION\n")
      for( n <- l ) {
        strB.append("  sancus_enable(&" + n + ");\n")
        strB.append("  sm_register_existing(&" + n +");\n")
      }
      stream.println(strB.toString())
    }
    case ConnectionDeploy(modOut, outfun, modIn, infun) =>
      stream.println("  REACTIVE_CONNECT(" + modOut + ", " + quote(outfun) + ", " + modIn + ", " + quote(infun) + ");")
    case EventLoop(bH,tH) => {
      val strB = new StringBuilder
      strB.append("while(1) {\n")
      if(bH){
        strB.append("  buttons_handle_events();\n")
      }
      if(tH){
        strB.append("  timer_handler();\n")
      }
      strB.append("}")
      stream.println(strB.toString())
    }

    case RegisterButton(id, cb) =>
      stream.println("buttons_register_callback(Button" + id + "," +  quote(cb) + ");")
    case Headers() =>
      stream.println(
        "#include <sancus/sm_support.h>\n\n" +
          "#include <sancus_support/uart.h>\n" +
          "#include <sancus_support/pmodcls.h>\n" +
          "#include <sancus_support/sm_control.h>\n\n" +
          "#include <msp430.h>\n\n" +
          "#include \"reactive.h\"\n" +
          "#include \"buttons.h\"\n" +
          "#include \"string.h\"\n" +
          "#include <stdbool.h>\n\n" +
          "static int lcd_printf(const char* fmt, ...)\n" +
          "{\n  " +
          "va_list va;\n  " +
          "va_start(va, fmt);\n  " +
          "int result = vuprintf(pmodcls_putchar, fmt, va);\n  " +
          "va_end(va);\n  return result;\n" +
          "}\n\n" +
          "static void __attribute__((noinline)) lcd_printf_int(const char* fmt, int i)\n" +
          "{\n  " +
          "lcd_printf(fmt, i);\n" +
          "}\n\n" +
          "static void __attribute__((noinline)) printf_int(const char* fmt, int i)\n" +
          "{\n" +
          "  printf(fmt, i);\n" +
          "}\n"
      )
    case PrintIntLCD(i) => stream.println("lcd_printf_int(\"%d \", " + quote(i) + ");")
    case EnInts() => stream.println("asm(\"eint\");\n")
    case DisInts() => stream.println("asm(\"dint\");\n")
    case Sizeof(s) => stream.println("size_t " + quote(sym) +  " = sizeof(" + quote(s) + ");")
    case _ => super.emitNode(sym, rhs)
  }
}
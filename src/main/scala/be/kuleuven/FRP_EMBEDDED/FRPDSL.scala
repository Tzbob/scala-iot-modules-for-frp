package be.kuleuven.FRP_EMBEDDED

import scala.lms.common._
import java.lang.{Integer => JInteger}

trait FRPDSL
    extends ScalaOpsPkg with TupledFunctions with UncheckedOps with LiftPrimitives with LiftString with LiftVariables with LiftBoolean
    with EventOps with BehaviorOps {

  def printEvent[A](e: Event[A]): String

  def generator[A](e: Event[A]): Unit

  // keep track of top level functions
  // TODO: can be removed eventually, user won't need to define them
  def toplevel[A:Typ,B:Typ](name: String)(f: Rep[A] => Rep[B]): Rep[A] => Rep[B]
}

trait FRPDSLImpl extends FRPDSL with EventOpsImpl with BehaviorOpsImpl {

  override def printEvent[A](e: Event[A]) = {
    def printParents[B](p: Event[B]): String = {
      val x = printEvent(p)
      x.mkString(",")
    }

    e match {
      case i @ InputEvent(_) => "InputEvent@"+ JInteger.toHexString(System.identityHashCode(i))
      case c @ ConstantEvent(_,_) => "ConstantEvent@"+ JInteger.toHexString(System.identityHashCode(c)) + "(" + printParents(c.parent) + ")"
      case m @ MapEvent(_,_) => "MapEvent@" + JInteger.toHexString(System.identityHashCode(m)) + "(" + printParents(m.parent) + ")"
      case f @ FilterEvent(_,_) => "FilterEvent@" + JInteger.toHexString(System.identityHashCode(f)) + "(" + printParents(f.parent) + ")"
      case m @ MergeEvent(_,_) => "MergeEvent@" + JInteger.toHexString(System.identityHashCode(m)) +
                                      "(" + printParents(m.parentLeft) + "," + printParents(m.parentRight) + ")"
      case _ => "other"
    }
  }

  def printEventTree(): Unit = {
    System.out.println(nodeMap)
  }

  //NOTE: One propagation considers only 1 active input!
  override def generator[X](e: Event[X]): Unit = {
    /*def generate[C,D](ev: EventNode[C,D], s:String): Rep[C] => Rep[D] = {
      toplevel(s+ev.id)(ev.updateFunc)(ev.typIn,ev.typOut)
    }*/

    def myComposeFunction[A,B,C,D]
        (first:Rep[B] => Rep[A], second:Rep[C] => Rep[B]): Rep[C] => Rep[A]  = {
      x:Rep[C] => val y = second(x);first(y)
    }

    def myComposeUnit[A,B,C,D]
        (first:Rep[B] => Rep[A], second:Rep[Unit]=>Rep[B]): Rep[Unit] => Rep[A] = {
      x:Rep[Unit] => val y = second(); first(y)
    }

    def myComposeCombinedFunction[A,B]
        (first:Rep[A]=>Rep[B], second:Rep[A]=>Rep[B], combo:(Rep[B],Rep[B])=>Rep[B]): Rep[A]=>Rep[B] = {
      x:Rep[A] => val y = first(x); val z = second(x); combo(y,z)
    }

    def generateCombRec[B,MergeIn,InputOut](e:Event[B], f:Rep[B]=>Rep[MergeIn], target: EventID) : Rep[InputOut]=>Rep[MergeIn] = {
      e match {
        case en @ MergeEvent(_,_) =>
          def isDisjointFor(target: EventID, left: Set[EventID], right: Set[EventID]): Boolean = {
            var b: Boolean = true
            for(l <- left if l==target) {
              if(right.contains(l)) b = false
            }
            b
          }
          if(isDisjointFor(target, en.inputIDsLeft, en.inputIDsRight)){
            if(en.inputIDsLeft.contains(target)) generateCombRec(en.parentLeft, f, target)
            else generateCombRec(en.parentRight, f, target)
          }
          else {
            System.out.println("MergeEvent(ID:" + en.id + ") ID=" + target + ": Non-Disjoint")

            val inputNode: Event[_] =
              nodeMap.get(target) match {
                case Some(e) => e
                case None => throw new IllegalStateException("Node does not exist.")
              }

            //build left function until target input
            val idfun: Rep[en.In]=>Rep[en.In] = (x:Rep[en.In])=>x
            val y: Rep[inputNode.Out]=>Rep[en.In] = generateCombRec(en.parentLeft, idfun, target)
            //build right function until target input
            val z: Rep[inputNode.Out]=>Rep[en.In] = generateCombRec(en.parentRight, idfun, target)
            val mergeFun: (Rep[en.In],Rep[en.In])=>Rep[en.Out] = toplevel2("mergefun"+en.id)(en.mergeFun)(en.typIn,en.typOut)
            val g: Rep[inputNode.Out]=>Rep[en.Out] = myComposeCombinedFunction(y,z,mergeFun)
            val x : Rep[inputNode.Out]=>Rep[MergeIn] = myComposeFunction(f,g)
            x.asInstanceOf[Rep[InputOut]=>Rep[MergeIn]] //TODO: fix typecast (because of InputOut type -> totally useless)
          }

        case en @ ConstantEvent(_,_) =>
          val g: (Rep[en.In] => Rep[en.Out]) = toplevel("constantfun"+en.id)(en.constFun)(en.typIn,en.typOut)
          val x: (Rep[en.In] => Rep[MergeIn]) = myComposeFunction(f,g)
          generateCombRec(en.parent, x, target)

        case en @ FilterEvent(_,_) =>
          val filterfun: (Rep[en.In] => Rep[Boolean]) = toplevel("filterfun"+en.id)(en.filterFun)(en.typIn,typ[Boolean])
          val g: Rep[en.In]=>Rep[en.Out] = { x =>
            if (!filterfun(x)) unchecked[Unit]("return")
            x
          }
          val x: Rep[en.In] => Rep[MergeIn] = myComposeFunction(f,g)
          generateCombRec(en.parent, x, target)

        case en @ MapEvent(_,_) =>
          val g: (Rep[en.In] => Rep[en.Out]) = toplevel("mapfun"+en.id)(en.mapFun)(en.typIn,en.typOut)
          val x: (Rep[en.In] => Rep[MergeIn]) = myComposeFunction(f,g)
          generateCombRec(en.parent, x, target)

        case en @ InputEvent(_) =>
          f.asInstanceOf[Rep[InputOut]=>Rep[MergeIn]]

        case _ => throw new IllegalStateException("Unsupported Event type")
      }
    }

    // TODO: notion: all explicit types can be ommitted since all Any -> no actual type checking. Fix?
    def generateLinRec[B](e:Event[B], f:Rep[B]=>Rep[Unit], target: EventID): Unit = {
      e match {
        case en @ MergeEvent(_,_) =>
          def isDisjointFor(target: EventID, left: Set[EventID], right: Set[EventID]): Boolean = {
            var b: Boolean = true
            for(l <- left if l==target) {
              if(right.contains(l)) b = false
            }
            b
          }
          if(isDisjointFor(target, en.inputIDsLeft, en.inputIDsRight)){
            System.out.println("MergeEvent(ID:" + en.id + ") ID=" + target + ": Disjoint")
            if(en.inputIDsLeft.contains(target)) generateLinRec(en.parentLeft, f, target)
            else generateLinRec(en.parentRight, f, target)
          } else {
            System.out.println("MergeEvent(ID:" + en.id + ") ID=" + target + ": Non-Disjoint")

            val inputNode: Event[_] =
              nodeMap.get(target) match {
                case Some(e) => e
                case None => throw new IllegalStateException("Node does not exist.")
              }

            val idfun: Rep[en.In]=>Rep[en.In] = (x:Rep[en.In])=>x
            //build left function until target input
            val y: Rep[inputNode.Out]=>Rep[en.In] = generateCombRec(en.parentLeft, idfun, target)
            //build right function until target input
            val z: Rep[inputNode.Out]=>Rep[en.In] = generateCombRec(en.parentRight, idfun, target)

            val mergeFun: (Rep[en.In],Rep[en.In])=>Rep[en.Out] = toplevel2("mergefun"+en.id)(en.mergeFun)(en.typIn,en.typOut)
            val g: Rep[inputNode.Out]=>Rep[en.Out] = myComposeCombinedFunction(y,z,mergeFun)
            val x : Rep[inputNode.Out]=>Rep[Unit] = myComposeFunction(f,g)
            generateLinRec[inputNode.Out](inputNode.asInstanceOf[Event[inputNode.Out]], x, target) //TODO: fix cast
          }


        case en @ ConstantEvent(_,_) =>
          val g: (Rep[en.In] => Rep[en.Out]) = toplevel("constantfun"+en.id)(en.constFun)(en.typIn,en.typOut)
          val x: (Rep[en.In] => Rep[Unit]) = myComposeFunction(f,g)
          generateLinRec[en.In](en.parent, x, target)

        case en @ FilterEvent(_,_) =>
          val filterfun: (Rep[en.In] => Rep[Boolean]) = toplevel("filterfun"+en.id)(en.filterFun)(en.typIn,typ[Boolean])
          val g: Rep[en.In]=>Rep[en.Out] = { x =>
            if (!filterfun(x)) unchecked[Unit]("return")
            x
          }
          val x: Rep[en.In] => Rep[Unit] = myComposeFunction(f,g)
          generateLinRec[en.In](en.parent, x, target)

        case en @ MapEvent(_,_) =>
          val g: (Rep[en.In] => Rep[en.Out]) = toplevel("mapfun"+en.id)(en.mapFun)(en.typIn,en.typOut)
          val x: (Rep[en.In] => Rep[Unit]) = myComposeFunction(f,g)
          generateLinRec[en.In](en.parent, x, target)

        case en @ InputEvent(_) =>
          val g: (Rep[Unit] => Rep[en.Out]) = toplevel("inputfun"+en.id)(en.inputFun)(en.typIn,en.typOut)
          val x: (Rep[en.In] => Rep[Unit]) = myComposeUnit(f,g)   //f.compose(g)
          toplevel("top"+en.id)(x)(en.typIn,typ[Unit])

        case _ => throw new IllegalStateException("Unsupported Event type")
      }
    }

    val voidretfun = {x:Rep[X] => unitToRepUnit( () )}
    for(inputID <- e.inputEventIDs)
    generateLinRec(e,voidretfun, inputID)

  }


  case class TopLevel[A,B](name: String, mA: Typ[A], mB:Typ[B], f: Rep[A] => Rep[B])
  val rec = new scala.collection.mutable.HashMap[String,TopLevel[_,_]]
  override def toplevel[A:Typ,B:Typ](name: String)(f: Rep[A] => Rep[B]): Rep[A] => Rep[B] = {
    val g = (x: Rep[A]) => unchecked[B](name,"(",x,")")
    rec.getOrElseUpdate(name, TopLevel(name, typ[A], typ[B], f))
    g
  }

  case class TopLevel2[A,B](name: String, mA: Typ[A], mB:Typ[B], f: (Rep[A],Rep[A]) => Rep[B])
  val rec2 = new scala.collection.mutable.HashMap[String,TopLevel2[_,_]]
  def toplevel2[A:Typ,B:Typ](name: String)(f: (Rep[A],Rep[A])=>Rep[B]): (Rep[A],Rep[A])=>Rep[B] = {
    val g = (x: Rep[A],y: Rep[A]) => unchecked[B](name,"(",x,",",y, ")")
    rec2.getOrElseUpdate(name, TopLevel2(name, typ[A], typ[B], f))
    g
  }
}
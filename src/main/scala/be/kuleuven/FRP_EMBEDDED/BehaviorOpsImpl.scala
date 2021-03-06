package be.kuleuven.FRP_EMBEDDED

import be.kuleuven.LMS_extensions.ScalaOpsPkgExpExt

trait BehaviorOpsImpl extends BehaviorOps_Impl with ScalaOpsPkgExpExt {
  eventImpl: EventOpsImpl =>

  def getBehaviorNodeList: List[Behavior[_]] = {
    val listbuilder = scala.collection.mutable.ListBuffer.empty[Behavior[_]]
    getNodeMap.values.foreach( n =>
      getOptionBehavior(n) match {
        case Some(e) => listbuilder += e
        case None => // do not add it
      }
    )
    listbuilder.toList
  }

  def getOptionBehavior[X](n: Node[X]): Option[Behavior[X]] = {
    n match {
      case bn @ ConcreteConstantBehavior(_) => Some(bn)
      case bn @ ConcreteMap2Behavior(_,_) => Some(bn)
      case bn @ ConcreteFoldpBehavior(_,_,_) => Some(bn)
      case bn @ ConcreteFoldp2Behavior(_,_,_,_,_,_) => Some(bn)
      case bn @ ConcreteStartsWithBehavior(_,_) => Some(bn)
      case _ => None
    }
  }

  implicit def behaviorToBehaviorImpl[X](b: Behavior[X]): BehaviorImpl[X] = {
    b match {
      case bn @ ConcreteConstantBehavior(_) => bn
      case bn @ ConcreteMap2Behavior(_,_) => bn
      case bn @ ConcreteFoldpBehavior(_,_,_) => bn
      case bn @ ConcreteFoldp2Behavior(_,_,_,_,_,_) => bn
      case bn @ ConcreteStartsWithBehavior(_,_) => bn
      case _ => throw new Exception("Event without Implementation")
    }
  }

  override def constantB[A:Typ](c: Rep[A])(implicit n: ModuleName): Behavior[A] = new ConcreteConstantBehavior[A](c)
  override def printLCD(l: List[(Behavior[Int], String, Int, Int)])(implicit n: ModuleName): Behavior[Nothing] = {
    ConcretePrintLCDBehavior(l)
  }

  case class ConcreteConstantBehavior[A](init: Rep[A])(implicit tA: Typ[A], mn: ModuleName)
    extends ConstantBehavior[A](init) with BehaviorImpl[A] {

    lazy val value = vardeclmod_new[A](mn.str)
    override def getValue = value
    lazy val valueInit = var_assign[A](value, init)
    override def getInitializer() = valueInit

    lazy val behaviorfun: Rep[(Unit)=>Unit] = {
      namedfun0 (mn.str) { () =>
        var_assign(fired, unit(true))
        unitToRepUnit(())
      }
    }

    override def generateNode(): Unit = {
      fired
      value
    }
    override def produceFunction() = behaviorfun
    override def useFunction() = behaviorfun( () )
  }

  case class ConcreteMap2Behavior[A:Typ,B:Typ,C](parents: (Behavior[A],Behavior[B]), f: (Rep[A],Rep[B])=>Rep[C])(implicit tC: Typ[C], mn: ModuleName)
    extends Map2Behavior[A,B,C](parents, f) with BehaviorImpl[C] {

    lazy val parentleftvalue: Var[A] = parentLeft.getValue
    lazy val parentleftfired: Var[Boolean] = parentLeft.getFired
    lazy val parentrightvalue: Var[B] = parentRight.getValue
    lazy val parentrightfired: Var[Boolean] = parentRight.getFired
    lazy val value = vardeclmod_new[C](mn.str)
    override def getValue = value
    lazy val valueInit = var_assign[C](value, f(parentleftvalue, parentrightvalue))
    override def getInitializer() = valueInit
    lazy val behaviorfun: Rep[(Unit)=>Unit] = {
      namedfun0 (mn.str) { () =>
        if(parentleftfired || parentrightfired) {
          var_assign[C](value, f(parentleftvalue, parentrightvalue))
          var_assign(fired, unit(true))
        }
        else{
          var_assign(fired, unit(false))
        }
        unitToRepUnit(())
      }
    }
    override def produceFunction() = behaviorfun
    override def useFunction() = behaviorfun( () )
    override def generateNode(): Unit = {
      fired
      value
      behaviorfun
    }

  }

  case class ConcreteFoldpBehavior[A,B](parent: Event[A], f: (Rep[A],Rep[B])=>Rep[B], init: Rep[B])(implicit tA: Typ[A], tB: Typ[B], mn: ModuleName)
    extends FoldpBehavior[A,B](parent,f,init) with BehaviorImpl[B] {

    lazy val value = vardeclmod_new[B](mn.str)
    override def getValue = value
    lazy val valueInit = var_assign[B](value, init)
    override def getInitializer() = valueInit
    lazy val parentvalue: Rep[A] = readVar(parent.getValue)
    lazy val parentfired: Rep[Boolean] = readVar(parent.getFired)
    lazy val behaviorfun: Rep[(Unit)=>Unit] = {
      namedfun0 (mn.str) { () =>
        if(parentfired) {
          var_assign(fired, unit(true))
          var_assign[B](value, f(parentvalue,readVar(value)))
        }
        else{
          var_assign(fired, unit(false))
        }
        unitToRepUnit( () )
      }
    }
    override def produceFunction() = behaviorfun
    override def useFunction() = behaviorfun( () )
    override def generateNode(): Unit = {
      fired
      value
      behaviorfun
    }

  }

  case class ConcreteFoldp2Behavior[A,B,C]
  (parentLeft: Event[A], parentRight: Event[B],
   f1:((Rep[A],Rep[C]) => Rep[C]),f2:((Rep[B],Rep[C]) => Rep[C]), f3:((Rep[A],Rep[B],Rep[C]) => Rep[C]),
   init: Rep[C] ) (implicit tA: Typ[A], tB: Typ[B], tC: Typ[C], mn: ModuleName)
    extends Foldp2Behavior[A,B,C](parentLeft, parentRight, f1,f2,f3,init) with BehaviorImpl[C] {

    lazy val parentleftvalue: Var[A] = parentLeft.getValue
    lazy val parentleftfired: Rep[Boolean] = readVar(parentLeft.getFired)
    lazy val parentrightvalue: Var[B] = parentRight.getValue
    lazy val parentrightfired: Rep[Boolean] = readVar(parentRight.getFired)
    lazy val value = vardeclmod_new[C](mn.str)
    override def getValue = value
    lazy val valueInit = var_assign[C](value, init)
    override def getInitializer() = valueInit
    lazy val behaviorfun: Rep[(Unit)=>Unit] = {
      namedfun0 (mn.str) { () =>
        if(parentleftfired && parentrightfired) {
          var_assign[C](value, f3(parentleftvalue, parentrightvalue, readVar(value)))
        }
        else if(parentleftfired) {
          var_assign(fired, unit(true))
          var_assign[C](value, f1(parentleftvalue, readVar(value)))
        }
        else if(parentrightfired){
          var_assign(fired, unit(true))
          var_assign[C](value, f2(parentrightvalue, readVar(value)))
        }
        else{
          var_assign(fired, unit(false))
        }
        unitToRepUnit( () )
      }
    }

    override def produceFunction() = behaviorfun
    override def useFunction() = behaviorfun( () )
    override def generateNode(): Unit = {
      fired
      value
      behaviorfun
    }

  }

  case class ConcreteStartsWithBehavior[A](parent: Event[A], init: Rep[A])(implicit tA: Typ[A], mn: ModuleName)
    extends StartsWithBehavior(parent, init) with BehaviorImpl[A] {

    lazy val value = vardeclmod_new[A](mn.str)
    override def getValue = value
    lazy val valueInit = var_assign[A](value, init)
    override def getInitializer() = valueInit
    lazy val parentvalue: Rep[A] = readVar(parent.getValue)
    lazy val parentfired: Rep[Boolean] = readVar(parent.getFired)
    lazy val behaviorfun: Rep[(Unit)=>Unit] = {
      namedfun0 (mn.str) { () =>
        if(parentfired) {
          var_assign(fired, unit(true))
          var_assign[A](value, parentvalue)
        }
        else {
          var_assign(fired, unit(false))
        }
        unitToRepUnit( () )
      }
    }

    override def produceFunction() = behaviorfun
    override def useFunction() = behaviorfun( () )
    override def generateNode(): Unit = {
      fired
      value
      behaviorfun
    }
  }

  case class ConcretePrintLCDBehavior(l: List[(Behavior[Int], String, Int, Int)])(implicit mn: ModuleName)
    extends PrintLCDBehavior(l) with BehaviorImpl[Nothing] {

    override def getValue = throw new UnsupportedOperationException("No value.")
    override def getInitializer() = useFunction()

    override def produceFunction() = behaviorfun
    override def useFunction() = behaviorfun( () )
    override def generateNode(): Unit = {
      behaviorfun
    }
  }

  trait BehaviorImpl[A] extends Behavior[A] with NodeImpl[A] {

    override def snapshot[B:Typ](e: Event[B])(implicit n: ModuleName): Event[A] = {
      implicit val tOut = typOut
      ConcreteSnapshotEvent(this, e)
    }
    override def changes()(implicit n: ModuleName): Event[A] = ConcreteChangesEvent(this)(typOut,n)
    override def map2[B:Typ, C:Typ](b: Behavior[B], f: (Rep[A], Rep[B]) => Rep[C])(implicit n: ModuleName): Behavior[C] = {
      implicit val tOut: Typ[A] = typOut
      ConcreteMap2Behavior((this, b), f)
    }

    addNodeToNodemap(id,this)
    addBehaviorID(id)

    lazy val fired = vardeclmod_new[Boolean](moduleName.str)
    def getFired = fired

    override val childNodeIDs = scala.collection.mutable.HashSet[NodeID]()
    override def addChild(id: NodeID): Unit = {
      childNodeIDs.add(id)
    }
  }
}

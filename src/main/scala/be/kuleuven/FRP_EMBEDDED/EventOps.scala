package be.kuleuven.FRP_EMBEDDED

import scala.collection.immutable.HashSet

trait EventOps extends ScalaOpsPkgExt {
  behavior: BehaviorOps =>

  type EventID = Int

  trait Event[A] {
    type In
    type Out = A

    //TODO: make private[FRP_EMBEDDED]
    val typIn: Typ[In]
    val typOut: Typ[Out]
    val id: EventID
    val inputEventIDs: Set[EventID]
    val ancestorEventIDs: List[EventID]
    val childEventIDs: scala.collection.mutable.Set[EventID]
    def addChild(id: EventID): Unit //TODO: can be removed? Depends on FRPDSLImpl

    // Public part
    def constant[B:Typ] (c: Rep[B]): Event[B]
    def map[B:Typ] (f: Rep[A] => Rep[B]): Event[B]
    def filter (f: Rep[A] => Rep[Boolean]): Event[A]
    def merge (e: Event[A], f:(Rep[A],Rep[A])=>Rep[A]): Event[A]

    def startsWith(i: Rep[A]): Behavior[A]
    def foldp[B]( fun:((A,B) => B), init:B): Behavior[B]
  }

  def TimerEvent(i: Rep[Int])/*(implicit tI:Typ[Int])*/: Event[Int]

}

trait EventOpsImpl extends EventOps {
  behaviorImpl: BehaviorOpsImpl =>

  val nodeMap: scala.collection.mutable.Map[EventID,Event[_]] = scala.collection.mutable.HashMap()

  override def TimerEvent(i: Rep[Int]) = InputEvent[Int](i)  // only conceptual

  abstract class EventNode[A,B:Typ] extends EventImpl[B] {
    override val id = EventNode.nextid
    nodeMap += ((id, this))
    override type In = A
    override val childEventIDs = scala.collection.mutable.HashSet[EventID]()
    def addChild(id: EventID): Unit = {
      childEventIDs.add(id)
    }
    lazy val fired = vardecl_new[Boolean]()
    lazy val value = vardecl_new[B]()
    // NOT GENERAL ANYMORE
    //val updateFunc: Rep[In] => Rep[Out]
    //val parentEvents: List[Event[In]]

    // val rank // Use for topological order --> glitch prevention

    /* NOT NEEDED:
     * pulse needs childEvents, but we don't need them since function-composition at staging time bottom-up
     *
     * val childEvents: List[EventNode[B,A]] = _ // immutable so build from bottom to top! NEEDED?
     * def pulse(x: A): Unit
     */
  }
  object EventNode {
    private var id: Int = 0
    private def nextid = {id += 1;id}
  }

  case class InputEvent[A] (i: Rep[A]) (implicit tA:Typ[A]) extends EventNode[Unit,A] {
    val inputFun: () => Rep[Out] = () => i
    override val typIn: Typ[In] = typ[Unit]
    override val typOut: Typ[Out] = tA
    override val inputEventIDs: Set[EventID] = HashSet(this.id)
    override val ancestorEventIDs: List[EventID] = Nil

    System.err.println("Create InputEvent(ID:" + id + "): " + inputEventIDs + ": " + ancestorEventIDs)
  }
  case class ConstantEvent[A,B](parent: Event[A], c : Rep[B])(implicit tB:Typ[B]) extends EventNode[A,B] {
    val constFun: Rep[In]=>Rep[Out] = _ => c
    override val typIn: Typ[In] = parent.typOut
    override val typOut: Typ[Out] = tB
    override val inputEventIDs: Set[EventID] = parent.inputEventIDs
    override val ancestorEventIDs: List[EventID] = parent.id::parent.ancestorEventIDs

    System.err.println("Create ConstantEvent(ID:" + id + "): " + inputEventIDs + ": " + ancestorEventIDs)
  }
  case class MapEvent[A,B](parent: Event[A], f: Rep[A] => Rep[B])(implicit tB:Typ[B]) extends EventNode[A,B] {
    val mapFun: Rep[In]=>Rep[Out] = f
    override val typIn: Typ[In] = parent.typOut
    override val typOut: Typ[Out] = tB
    override val inputEventIDs: Set[EventID] = parent.inputEventIDs
    override val ancestorEventIDs: List[EventID] = parent.id::parent.ancestorEventIDs

    System.err.println("Create MapEvent(ID:" + id + "): " + inputEventIDs + ": " + ancestorEventIDs)
  }
  case class FilterEvent[A](parent: Event[A], f: Rep[A] => Rep[Boolean])(implicit tA:Typ[A]) extends EventNode[A,A] {
    val filterFun: Rep[In]=>Rep[Boolean] = f
    override val typIn: Typ[In] = parent.typOut
    override val typOut: Typ[Out] = typIn
    override val inputEventIDs: Set[EventID] = parent.inputEventIDs
    override val ancestorEventIDs: List[EventID] = parent.id::parent.ancestorEventIDs

    System.err.println("Create FilterEvent(ID:" + id + "): " + inputEventIDs + ": " + ancestorEventIDs)
  }
  case class MergeEvent[A](parents: (Event[A],Event[A]), f: (Rep[A],Rep[A])=>Rep[A] )(implicit tA:Typ[A]) extends EventNode[A,A] {
    val mergeFun: (Rep[In],Rep[In])=>Rep[Out] = f
    //val parentEvents: List[Event[In]] = parents._1::parents._2::Nil
    val parentLeft: Event[In] = parents._1
    val parentRight: Event[In] = parents._2
    override val typIn: Typ[In] = parentLeft.typOut //TODO: fix if different typed Events can be merged
    override val typOut: Typ[Out] = typIn
    val inputIDsLeft: Set[EventID] = parentLeft.inputEventIDs
    val inputIDsRight: Set[EventID] = parentRight.inputEventIDs
    override val inputEventIDs: Set[EventID] = inputIDsLeft ++ inputIDsRight
    override val ancestorEventIDs: List[EventID] = (parents._1.id::parents._1.ancestorEventIDs)++(parents._2.id::parents._2.ancestorEventIDs)
    val leftAncestors = parents._1.id::parents._1.ancestorEventIDs
    val rightAncestors = parents._2.id::parents._2.ancestorEventIDs

    System.err.println("Create MergeEvent(ID:" + id + "): " + inputEventIDs + ". Left: " + inputIDsLeft + ", Right: " + inputIDsRight + ": " + ancestorEventIDs)
  }

  trait EventImpl[A] extends Event[A] {
    override def constant[B:Typ](c: Rep[B]): Event[B] = ConstantEvent[A,B](this, c)
    override def map[B:Typ](f: Rep[A] => Rep[B]): Event[B] = MapEvent[A,B](this, f)
    override def filter(f: Rep[A] => Rep[Boolean]): Event[A] = FilterEvent[A](this, f)(typOut)
    override def merge(e: Event[A], f: (Rep[A],Rep[A])=>Rep[A]) = MergeEvent[A]( (this, e), f)(typOut)

    override def startsWith(i: Rep[A]): Behavior[A] = ??? // TODO: implement
    override def foldp[B](fun: (A, B) => B, init: B): Behavior[B] = ??? // TODO: implement
  }
}

package be.kuleuven.FRP_EMBEDDED

trait FRPDSL extends EventOps with BehaviorOps {

  abstract class Module[A] {
    val name: ModuleName
    val output: OutputEvent[A]
  }

}

trait FRPDSL_Impl extends FRPDSL with EventOps_Impl with BehaviorOps_Impl {

  def printGraph(): Unit = {
      val inputevents = getInputEventNodes
      System.err.println("InputEvents:")
      inputevents.foreach(System.err.println )

      //get all end nodes
      val leafNodes = getOutputNodes.values.toList
      System.err.println("LeafNodes:")
      leafNodes.foreach(System.err.println )
    }

  def buildFRPGraph(): Unit = {
    getNodeMap.foreach{ case (_, n) => n.buildGraphTopDown() }
    printGraph()
  }

  def buildProgram(modList: List[Module[_]]): () => Rep[Unit] = {
    () => {
      headers()
      val (listmap, inits) = (for (module <- modList) yield {
        generateModule(module)
      }).unzip
      val inputToToplevel: Map[NodeID, Rep[((Ptr[Byte], Int)) => Unit]] = listmap reduce (_ ++ _)

      generateExtras(modList, inputToToplevel, inits)
    }
  }

  def generateModule(module: Module[_]): (Map[NodeID,Rep[((Ptr[Byte],Int))=>Unit]], Rep[(Unit)=>Unit]) = {
    // generate per level
    System.err.println("max level : " + getMaxLevel)
    for (i <- 0 to getMaxLevel) {
      val nodes = getNodesOnLevel(getNodeMap.values.toList, i)
      val modnodes = nodes.filter(n => n.moduleName == module.name)
      modnodes.foreach { node => node.generateNode() }
    }

    // function to reset all event fired values
    val behaviorsInModule = getBehaviorNodes.values.filter( node => node.moduleName == module.name)

    val initialised = vardeclmod_new[Int](module.name.toString())
    val initModule: Rep[(Unit) => Unit] = entryfun0(module.name.toString(), "initModule") { () =>
      if (readVar(initialised) == 0) {
        for (i <- 0 to getMaxLevel) {
          getNodesOnLevel(behaviorsInModule.toList, i)
            .foreach(_.getInitializer())
        }
        var_assign(initialised, 1)
      }
      else{
        // Nothing outside if check since this would be executable from outside of module (!)
      }
      // I mean it, nothing here!
      unitToRepUnit(())
    }

    val initNodes: Rep[(Unit)=>Unit] = namedfun0(module.name.toString()) { () =>
      generateGlobalFRPInits(module)
      unitToRepUnit( () )
    }

    (generateTopFunctions(module, initModule, initNodes), initModule)
  }

  def generateGlobalFRPInits(module: Module[_]): Unit

  def generateTopFunctions(module: Module[_], initModule: Rep[(Unit) => Unit], initNodes: Rep[(Unit) => Unit])
    : Map[NodeID,Rep[((Ptr[Byte],Int))=>Unit]] = {

    val inputToTopMap: scala.collection.mutable.Map[NodeID,Rep[((Ptr[Byte],Int))=>Unit]] = scala.collection.mutable.HashMap()

    //get all input events
    val inputs = getInputEventNodes
    val modinputs = inputs.filter(n => n.moduleName == module.name)

    // generate top functions
    for( ie <- modinputs) {
      System.err.println("Generate dependencies of inputnode " + ie.id)
      val toplevelfun = generateTopFunction(ie, initModule, initNodes, module)
      inputToTopMap += ((ie.id, toplevelfun))
    }
    System.err.println("End of generateModule")

    inputToTopMap.toMap
  }

  def generateTopFunction[X](input: InputEvent[X], initModule: => Rep[(Unit)=>Unit], initNodes: Rep[(Unit)=>Unit], m: Module[_])
    : Rep[((Ptr[Byte],Int))=>Unit] = {

    System.err.println("top" + input.id)

    val descendantIDs = getDecendantNodeIDs(input).filter(id => id != input.id)
    val descendantNodes = getNodesWithIDs(descendantIDs)

    // get topological ordering
    val listbuilder = scala.collection.mutable.ListBuffer.empty[Node[_]]
    for( i <- 0 to getMaxLevel)
      listbuilder ++= getNodesOnLevel(descendantNodes.values.toList,i)
    val nodesTO = listbuilder.toList
    nodesTO.foreach(x => System.err.println(x.id))

    m.output match {
      case coe @ AOutputEvent(_) => System.err.println("Output for: " + coe.parent.id)
      case _ => System.err.println("No outputs for this module")
    }
    //val behaviorsInModule = getBehaviorNodes.values.filter( node => node.moduleName == input.moduleName)

    val top: Rep[((Ptr[Byte],Int))=>Unit] = inputfun(input.moduleName.str, "top"+input.id) { (data: Rep[Ptr[Byte]], len: Rep[Int]) =>
      //disableInterrupts()

      initModule() // can be removed if main is always generated!
      initNodes()

      resetSymMap()
      input.useInput(data, len)

      nodesTO.foreach( x => { x.useFunction() } ) // apply the functions in this context

      m.output match {
        case coe @ AOutputEvent(_) =>
          if (coe.inputNodeIDs.contains(input.id) ) coe.useOutput()
        case _ => // we didn't had an output, it was None
      }

      //enableInterrupts()
      unitToRepUnit( () )
    }
    doApplyDecl(top)
    top
  }

  def generateExtras(modlist: List[Module[_]], inputToToplevel: Map[NodeID, Rep[((Ptr[Byte], Int)) => Unit]], initModules: List[Rep[(Unit)=>Unit]])
    : Unit = {

    for( mod <- modlist) {
      declare_module(mod.name.str)
    }

    val buttonList: Map[Int, Rep[(Int)=>Unit]] = generateButtonFunctions(inputToToplevel)

    val init: Rep[(Unit)=>Unit] = staticfun0 { () =>
      systemInits()
      unitToRepUnit( () )
    }

    val modNames = modlist.map{ m => m.name.toString()}
    //deployFun(modNames, getOutInList)
    val deploy: Rep[(Unit) => Unit] = staticfun0 { () =>
      moduleDeploy(modNames)
      for( (oe,ie) <- getOutInList) {
        (oe,ie) match {
          case (out @ AOutputEvent(_), in @ InputEvent(_)) => {
            val resultInput = inputToToplevel.getOrElse(in.id,throw new Exception("Error inputToTopLevel"))
            connectionDeploy(oe.mn.str, out.outfun , ie.moduleName.str, resultInput)
          }
          case _  => throw new Exception("Output Input problem in deploy function")
        }
      }

      unitToRepUnit( () )
    }

    val otimercb = getTimersRegister.get(systemTimer)

    val timercb = otimercb match {
      case Some(tcb) => {
        val resultInput = inputToToplevel.getOrElse(tcb.id, throw new Exception("Error inputToTopLevel"))
        val timercb = timercallback { () =>
          val timerVal: Rep[Int] = var_new(5) // dummy actually
          val in: Rep[Byte] = rep_asinstanceof(timerVal, typ[Int], typ[Byte])
          val inptr = ptr_new(in)
          resultInput(inptr,sizeof(in))
          unitToRepUnit( () )
        }
        doApplyDecl(timercb)
        Some(timercb)
      }
      case None => None
    }

    val main = mainfun { () =>
      init( () )
      unchecked[Unit]("puts(\"main started\")")
      deploy( () )

      for( (id,_) <- getButtonsRegister) {
        val resultButton = buttonList.getOrElse(id, throw new Exception("Error with button register"))
        registerButton(id, resultButton)
      }

      unchecked[Unit]("\n// modules inits")
      for(moduleInit <- initModules) moduleInit( () )

      eventLoop(getButtonsRegister.keys.toList.nonEmpty, timercb.isDefined)

      unit(0)
    }
    doApplyDecl(main)
  }

  def generateButtonFunctions(inputToToplevel: Map[NodeID, Rep[((Ptr[Byte], Int)) => Unit]]): Map[Int,Rep[(Int)=>Unit]] = {
    val buttonFunctions = for( (bId,(input, updown)) <- getButtonsRegister) yield {

      val f = staticfun { (pressed: Rep[Int]) =>
        val in: Rep[Byte] = rep_asinstanceof(pressed, typ[Int], typ[Byte])
        val insize = sizeof(in)
        val inptr = ptr_new(in)
        val resultInput = inputToToplevel.getOrElse(input.id, throw new Exception("Error in inputToTopLevel"))

        if(updown) {
          if(pressed == 1) { resultInput(inptr, insize) }
          else if(pressed == 0) { resultInput(inptr, insize) }
          else { }
        }
        else {
          if(pressed == 1) { resultInput(inptr, insize) }
          else { }
        }

        unitToRepUnit( () )
      }
      bId -> f
    }

    buttonFunctions.toMap
  }
}

trait FRPDSLImpl extends FRPDSL_Impl with EventOpsImpl with BehaviorOpsImpl {

  override def generateGlobalFRPInits(module: Module[_]): Unit = {
    getEventNodeList
      .filter(e => e.moduleName == module.name)
      .foreach(e => var_assign(e.getFired, false))

    getBehaviorNodeList
      .filter(e => e.moduleName == module.name)
      .foreach(e => var_assign(e.getFired, false))
  }

}

trait FRPDSLOptImpl extends FRPDSL_Impl with EventOpsOptImpl with BehaviorOpsOptImpl {

  override def generateGlobalFRPInits(module: Module[_]): Unit = {
    // nothing to do since not global anymore
  }

}

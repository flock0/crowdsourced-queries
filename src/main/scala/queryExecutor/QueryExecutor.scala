package queryExecutor

import parser.Parser
import tree.Tree._
import crowdsourced.mturk._
import scala.collection.mutable.ListBuffer
import java.text.SimpleDateFormat
import java.util.Calendar
import scala.util.Random
import scala.collection.JavaConverters._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{ Success, Failure }
import play.api.libs.json._

/**
 * This class represents and can execute a complete query in our query language.
 * Construct an object by submitting an ID and the string to parse.
 * Call execute() in order to run the request and to start recursive interaction with Amazon Mechanical Turk
 * @authors Joachim Hugonot, Francois Farquet, Kristof Szabo
 */
class QueryExecutor(val queryID: Int, val queryString: String) {

  val listTaskStatus = ListBuffer[TaskStatus]()
  val listResult = ListBuffer[String]()
	
  private val NOT_STARTED = "Not started"
  private val PROCESSING = "Processing"
  private val FINISHED = "Finished"
  
  private var futureResults: List[Future[List[Assignment]]] = Nil
  private var queryTree: Q = null
  
  val DEFAULT_ELEMENTS_SELECT = 4
  val MAX_ELEMENTS_PER_WORKER = 2
  
  /**
   * Use parser to return the full tree of the parsed request
   */
  private def parse(query: String): Q = Parser.parseQuery(query).get
  
  /**
   * Construct the hierarchy of all requests and the chaining of tasks based on the parsed tree
   */
  private def startingPoint(node: Q): List[Future[List[Assignment]]] = node match {
      // TODO we need to pass a limit to taskSelect. The dataset could be very small or huge...
      // TODO maybe get this from the request using the LIMIT keyword. Or ask a worker for number of elements in the web page
      case Select(nl, fields) => taskSelect(nl, fields)
      case Join(left, right, on) => taskJoin(left, right, on) //recursiveTraversal(left); recursiveTraversal(right);
      case Where(selectTree, where) => taskWhere(selectTree, where)
      case OrderBy(query, List(ascendingOrDescending)) => taskOrderBy(query,ascendingOrDescending) //TODO
      case Group(query,by)=>taskGroupBy(query,by)
      case _ => List[Future[List[Assignment]]]() //TODO
    }

  /**
   * Start in background the exection of the request
   * Call waitAndPrintResults() if you want to block until this is done
   */
  def execute(): Unit = {
    this.queryTree = parse(queryString) // TODO handle parsing errors in order to avoid a crash
    println("Starting execution of the query : \"" + this.queryTree + "\"")
    this.futureResults = startingPoint(queryTree)
  }
  
  def waitAndPrintResults(): Unit = {
    if (this.queryTree == null) {
      println("[Error] Query hasn't been executed yet. Call execute() first.")
    } else {
      queryResultToString(this.queryTree, this.futureResults)
      this.futureResults.map(Await.result(_, Duration.Inf))
      Thread sleep 5000 // in order to be sure that the buffer has been filled
      println("Results :")
      getResults.foreach(r => println("\t"+r))
      printListTaskStatus
      println("Total duration : " + getDurationString)
    }
  }

  def queryResultToString(query: Q, res: List[Future[List[Assignment]]]): Unit = {
    res.map(x => {
    	x onSuccess{
    	  case assign => {
          listResult ++= extractNodeAnswers(query, assign)
    	  }
    	}
      }
    )
  }
  
  /********************************* TASKS CREATIONS ********************************/
  
  /**
   * Creation of FROM task
   */
  def taskNaturalLanguage(s: String, fields: List[P]): List[Future[List[Assignment]]] = {
    println("Task natural language")
    val taskID = generateUniqueID()
    val status = new TaskStatus(taskID, "FROM")
    listTaskStatus += status
    printListTaskStatus
 
    
    val tasks: List[AMTTask] = TasksGenerator.naturalLanguageTasksGenerator(s, fields)
    tasks.foreach(_.exec)
    status.addTasks(tasks)
    val assignments = List(Future{tasks.flatMap(_.waitResults)}) 
    printListTaskStatus

    assignments
  }
  
  /**
   * Creation of SELECT Task
   */
  def taskSelect(from: Q, fields: List[P]): List[Future[List[Assignment]]] = {
    println("Task select started")

    val taskID = generateUniqueID()
    val status = new TaskStatus(taskID, "SELECT")
    listTaskStatus += status
    
    printListTaskStatus

    val NLAssignments: List[Future[List[Assignment]]] = from match { case NaturalLanguage(nl) => taskNaturalLanguage(nl, fields) }
    
    val fAssignments = NLAssignments.map(x => {
        val p = promise[List[Assignment]]()
        val f = p.future
        x onSuccess {
          case nl => {
            val tasks = TasksGenerator.selectTasksGenerator(extractNodeAnswers(from, nl).head, from.toString, fields, MAX_ELEMENTS_PER_WORKER, DEFAULT_ELEMENTS_SELECT)
            tasks.foreach(_.exec)
            status.addTasks(tasks)
            p success tasks.flatMap(_.waitResults)
          }
        }
        f
    })

    printListTaskStatus

    fAssignments
  }
  
  /**
   * Creation of WHERE task
   */
  def taskWhere(select: SelectTree, where: Condition): List[Future[List[Assignment]]] = {
    println("Task where started")
    val taskID = generateUniqueID()
    
    val status = new TaskStatus(taskID, "WHERE")
    listTaskStatus += status
    
    printListTaskStatus
    
    val assignments = select match {case Select(nl, fields) => taskSelect(nl, fields)}
    val fAssignments = assignments.map(x => {
      val p = promise[List[Assignment]]()
      val f = p.future 
      x onSuccess {
      case a => {
        val tasks = TasksGenerator.whereTasksGenerator(extractNodeAnswers(select, a), where)
        tasks.foreach(_.exec)
        status.addTasks(tasks)
        p success tasks.flatMap(_.waitResults)
      }
      }
      f
      })/*
    val tasks = whereTasksGenerator(extractSelectAnswers(assignments), where)
    tasks.foreach(_.exec) // submit all tasks (workers can then work in parallel)
    val assignements = tasks.flatMap(_.waitResults)
   
    println("Final results " + extractWhereAnswers(assignements))
    assignements*/
    fAssignments
    //printListTaskStatus
    //TODO We need to pass the status object to the AMT task in order to obtain the number of finished hits at any point.
    //TODO We need to retrieve the number of tuples not eliminated by WHERE clause.
  }
  
  /**
   * Creation of JOIN task
   */
  def taskJoin(left: Q, right: Q, on: String): List[Future[List[Assignment]]] = {
 
    println("Task join started")
    val taskID = generateUniqueID()
    val status = new TaskStatus(taskID, "JOIN")
    listTaskStatus += status
    
    printListTaskStatus
    
    val a = Future { executeNode(left) }
    val b = Future { executeNode(right) }
   
    val resultsLeft = Await.result(a, Duration.Inf) //Future[List[Assignment]]
    val resultsRight = Await.result(b, Duration.Inf)
    val resLeft = resultsLeft.flatMap(Await.result(_, Duration.Inf)) //List[Assignment]
    val resRight = resultsRight.flatMap(Await.result(_, Duration.Inf))
    /*val fAssignments = resultsRight.map(x => {
      val p = promise[List[Assignment]]()
      val f = p.future //Future[List[Assignment]]
      x onSuccess { //onSuccess of Future[List[Assignment]]
      case r => {
        val tasks = joinTasksGenerator(extractNodeAnswers(left, resLeft), extractNodeAnswers(right, r))
        tasks.foreach(_.exec)
        p success tasks.flatMap(_.waitResults)
      }
      }
      f
      })*/ //Does not work yet !
//      fAssignments
    val tasks = TasksGenerator.joinTasksGenerator(extractNodeAnswers(left, resLeft), extractNodeAnswers(right, resRight))
    status.addTasks(tasks)
    tasks.foreach(_.exec) // submit all tasks (workers can then work in parallel)

    val assignments: List[Future[List[Assignment]]] = tasks.map(x => Future{x.waitResults})
    
//    println("Final results " + extractJoinAnswers(assignments))

    assignments
  }
  
  /**
   * Creation of GROUPBY task
   */
  def taskGroupBy(q: Q, by: String): List[Future[List[Assignment]]] = {
    println("Task GROUPBY")
    val taskID = generateUniqueID()
    val status = new TaskStatus(taskID, "GROUPBY")
    listTaskStatus += status
    
    printListTaskStatus
    
    val toGroupBy = executeNode(q)
    val fAssignments = toGroupBy.map(x => {
      val p = promise[List[Assignment]]()
      val f = p.future 
      x onSuccess { 
      case a => {
        val tasks = TasksGenerator.groupByTasksGenerator(extractNodeAnswers(q, a), by)
        tasks.foreach(_.exec)
        status.addTasks(tasks)
        p success tasks.flatMap(_.waitResults)
      }
      }
      f
      })
    val finishedToGroupBy = toGroupBy.flatMap(x => Await.result(x, Duration.Inf))
    val tuples = extractNodeAnswers(q, finishedToGroupBy)
    //Future{println(printGroupByRes(tuples, fAssignments).groupBy(x=>x._2))}
    fAssignments
  }
  
  /**
   * Creation of ORDERBY task
   */
  def taskOrderBy(q: Q3, order: O): List[Future[List[Assignment]]] = {
    println("Task order by")
    
    val taskID = generateUniqueID()
    val status = new TaskStatus(taskID, "ORDER BY")
    listTaskStatus += status
    printListTaskStatus
    
    val toOrder = executeNode(q)
    val finishedToOrder = toOrder.flatMap(x => Await.result(x, Duration.Inf))
    val tuples = extractNodeAnswers(q, finishedToOrder)
    val tasks = TasksGenerator.orderByTasksGenerator(tuples, order)
    tasks.foreach(_.exec())
    status.addTasks(tasks)
    val assignments = Future{tasks.flatMap(_.waitResults())}::List()
    
//    println("Final results " + extractOrderByAnswers(assignments))
    assignments
  }
  
  /******************************* HELPERS, GETTERS AND PRINTS **********************************/
  
  /**
   * Takes a string in format "(_, _)" and converts it to a tuple
   */
  def stringToTuple(s: String): Tuple2[String, String] = {
    val tup = s.split(",")
    (tup(0).tail, tup(1).take(tup(1).length-1))
  }
  
  /**
   * Special way for printing GROUPBY results
   */
  def printGroupByRes(tuples: List[String], fAssignments: List[Future[List[Assignment]]]) = {
     var results = List[(String,String)]() // TODO remove vars...
     val assignments = fAssignments.flatMap(x => Await.result(x, Duration.Inf))
    tuples.zip(assignments).map(x =>{
       val answersMap = x._2.getAnswers().asScala.toMap
        answersMap.foreach{case(key, value) => { 
          results = results ::: List((x._1, value.toString))}
        }})
  
    results
  }
  
  /**
   * Tells if the query is finished or not
   */
  def isQueryFinished(): Boolean = getListTaskStatus().foldLeft(true)((res, ts) => if(ts.getCurrentStatus != FINISHED) false else res)
  
  /**
   * Tells if the query has started yet or not
   */
  def hasQueryStarted(): Boolean = getListTaskStatus().foldLeft(false)((res, ts) => if(ts.getCurrentStatus != NOT_STARTED) true else res)
  def getStatus(): String = {
    if (isQueryFinished()) FINISHED
    else if (hasQueryStarted()) PROCESSING
    else NOT_STARTED
  }
  
  
  
  /**
   * Returns the list of all TaskStatus related to this query
   */
  def getListTaskStatus(): List[TaskStatus] = this.listTaskStatus.toList
  
  /**
   * Returns the final results if the request is finished or the partial results if it is still running
   */
  def getResults(): List[String] = this.listResult.toList
  
  /**
   * Print the status of all tasks related to this query
   */
  def printListTaskStatus() = {
    println("Task status summary : ")
    getListTaskStatus().foreach(println)
    println(getJSON.toString) // TODO print JSONs for testing only
  }
  
  /**
   * Returns the timestamp stating when the query has started or -1 if it has not
   */
  def getStartTime(): Long = {
    val starts = getListTaskStatus.map(_.getStartTime).filter(_ > 0)
    if (starts.length > 0) starts.min
    else -1
  }
  
  /**
   * Returns the timestamp stating when the query has ended or -1 if it has not
   */
  def getEndTime(): Long = {
    val haveAllFinished: Boolean = getListTaskStatus.foldLeft(true)((b, ts) => if (ts.getEndTime <= 0) false else b)
    if (haveAllFinished) getListTaskStatus.map(_.getEndTime).max
    else -1
  }
  
  /**
   * Returns a nice string of the duration of the query
   */
  def getDurationString: String = {
    if (getStartTime < 0) "Task hasn't started yet"
    else if (getEndTime < 0) "Task is still running"
    else {
      val duration_sec = (getEndTime - getStartTime)/1000 
      val days: Long = duration_sec / 86400
      val hours: Long = (duration_sec - days * 86400) / 3600
      val minutes: Long = (duration_sec - days * 86400 - hours * 3600) / 60
      val seconds: Long = duration_sec - days * 86400 - hours * 3600 - minutes * 60
      val s = new StringBuilder()
      if (days > 0) s ++= days+"d "
      if (hours > 0) s ++= hours+"h "
      if (minutes > 0) s ++= minutes +"m "
      s ++= seconds +"s "
      s.toString
    }
  }
  
  /**
   * Returns the JSON of the query
   */
  def getJSON(): JsValue = JsObject(Seq(
      "query_id" -> JsNumber(this.queryID),
      "query_status" -> JsString(getStatus()),
      "query_results_number" -> JsNumber(getResults().length),
      "start_time" -> JsNumber(getStartTime()),
      "end_time" -> JsNumber(getEndTime()),
      "list_of_tasks" -> JsArray(getListTaskStatus.map(_.getJSON).toSeq),
      "detailed_query_results" -> JsArray(getResults().map(JsString(_)).toSeq)
      ))
      
  /**
   * Creates a unique ID which is the full date followed by random numbers
   */
  def generateUniqueID(): String = new SimpleDateFormat("y-M-d-H-m-s").format(Calendar.getInstance().getTime()).toString + "--" + new Random().nextInt(100000)
      
     /******************** EXTRACT FUNCTIONS THAT WE SHOULD DELETE *******************/
  /***** We have to write a more general function to replace the copy/pastes ******/
  /********************* from here until the end of the file **********************/

    /**
   * Helper function when nodes have left and right parts
   */
  private def executeNode(node: Q): List[Future[List[Assignment]]] = {
    node match {
    case Select(nl, fields) => taskSelect(nl, fields)
    case Join(left, right, on) => taskJoin(left, right, on)
    case Where(selectTree, where) => taskWhere(selectTree, where)
    case _ => ???
    }
  }
    
  private def extractNodeAnswers(node: Q, assignments: List[Assignment]): List[String] = {
    node match {
    case Join(_, _, _) | Where(_,_) => assignments.flatMap(_.getAnswers().asScala.toMap.filter(_._2.toString.endsWith("yes")).map(ans => ans._2.toString.substring(0, ans._2.toString.length-8)))
    case Group(_,_) => assignments.flatMap(_.getAnswers().asScala.toMap).map(s => stringToTuple(s._2.toString)).groupBy(_._2).map(x=> x.toString).toList
    case _ => assignments.flatMap(_.getAnswers().asScala.toMap).flatMap(_._2.toString.stripMargin.split("[\r\n\r\n]").toList)
    }
  }
}
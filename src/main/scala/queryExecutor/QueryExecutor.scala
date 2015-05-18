package queryExecutor

import parser.QueryParser
import tree.QueryTree._
import crowdsourced.mturk.task._
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
  
  private val PARALLELIZED = true
  private val JOIN_PAIRWISE = false
  private val RETRIEVE_PRIMARY_KEY_FIRST = false
    
  private var futureResults: List[Future[List[Assignment]]] = Nil
  private var queryTree: RootNode = null
  private var aborted: Boolean = false
  
  val DEFAULT_ELEMENTS_SELECT = 44
  val MAX_ELEMENTS_PER_WORKER = 5
  
  /**
   * Use parser to return the full tree of the parsed request
   */
  def parse(query: String): Either[RootNode, String] = try {
    Left(QueryParser.parseQuery(query))
  } catch {
    case e: Exception => {
	Right(e.getMessage)
	}
  }
  
  /**
   * Construct the hierarchy of all requests and the chaining of tasks based on the parsed tree
   */
  private def startingPoint(node: RootNode, limit: Int = DEFAULT_ELEMENTS_SELECT): List[Future[List[Assignment]]] = node match {
      case Select(nl, fields) => taskSelect(nl, fields, limit)
      case Join(left, right, on) => taskJoin(left, right, on) //recursiveTraversal(left); recursiveTraversal(right);
      case Where(selectTree, where) => taskWhere(selectTree, where, limit)
      case OrderBy(query, List(ascendingOrDescending)) => taskOrderBy(query,ascendingOrDescending, limit) //TODO
      case Group(query,by)=>taskGroupBy(query,by)
      case Limit(query, limit) => limit match {
        case IntL(i) => startingPoint(query, i)
        case _ => startingPoint(query)
      }
      case _ => List[Future[List[Assignment]]]() //TODO
    }

  /**
   * Start in background the exection of the request
   * Call waitAndPrintResults() if you want to block until this is done
   */
  def execute(queryTree: RootNode): Boolean = {
    if (queryTree != null) {
        this.queryTree = queryTree
    }
    
    if (this.queryTree == null) { // if the parsing failed we return false
      println("[Error] Parsing of the query failed.")
      false
    } else {
      println("Starting execution of the query : \"" + this.queryTree + "\"")
      this.futureResults = startingPoint(queryTree)
      queryResultToString(this.queryTree, this.futureResults)
      true
    }
  }
  
  /**
   * Blocking function until request has reached the end of execution
   */
  def waitAndPrintResults(): Unit = {
    if (this.queryTree == null) {
      println("[Error] There is no query running.")
    } else {
      this.futureResults.map(Await.result(_, Duration.Inf))
      Thread sleep 5000 // in order to be sure that the buffer has been filled
      println("Results :")
      getResults.foreach(r => println("\t"+r))
      printListTaskStatus
      println("Total duration : " + getDurationString)
    }
  }
  
  /**
   * Prevents the creation of new HITs
   */
  def abort(): Unit = {
    println("Aborting query "+this.queryID+".")
    this.aborted = true
  }
  
  /**
   * Returns the unique number of this query
   */
  def getQueryID: Int = this.queryID

  /**
   * Add results to the list of partial results as soon as one is received
   */
  def queryResultToString(query: RootNode, res: List[Future[List[Assignment]]]): Unit = {
    res.map(x => {
    	x onSuccess{
    	  case assign => {
          this.synchronized { listResult ++= extractNodeAnswers(query, assign) }
    	  }
    	}
      }
    )
  }
  
  /********************************* TASKS CREATIONS ********************************/
  
  /**
   * Creation of FROM task
   */
  def taskNaturalLanguage(s: String, fields: List[Operation]): List[Future[List[Assignment]]] = {
    println("Task natural language")
    
    val taskID = generateUniqueID()
    val status = new TaskStatus(taskID, "FROM")
    listTaskStatus += status
    printListTaskStatus
 
     val tasks = RETRIEVE_PRIMARY_KEY_FIRST match {
      case false => TasksGenerator.naturalLanguageTasksGenerator(s, fields)
      case true => TasksGenerator.naturalLanguagePrimaryKeyTasksGenerator(s, fields)
    }
    tasks.foreach(_.exec)
    status.addTasks(tasks)
    val assignments = List(Future{tasks.flatMap(_.waitResults)}) 
    printListTaskStatus
	if(!PARALLELIZED)
	      assignments.map(x => Await.ready(x, Duration.Inf))
    assignments
  }
  
  /**
   * Creation of SELECT Task
   */
  def taskSelect(from: RootNode, fields: List[Operation], limit: Int = DEFAULT_ELEMENTS_SELECT): List[Future[List[Assignment]]] = {
    println("Task select started")

    val taskID = generateUniqueID()
    val status = new TaskStatus(taskID, "SELECT")
    listTaskStatus += status
    
    printListTaskStatus

    val NLAssignments: List[Future[List[Assignment]]] = from match { case NaturalLanguage(nl) => taskNaturalLanguage(nl, fields) }

    val nl = Await.result(NLAssignments.head, Duration.Inf)
    
    println(extractNodeAnswers(from, nl).mkString(", "))
    val tasks = RETRIEVE_PRIMARY_KEY_FIRST match {
      case false => TasksGenerator.selectTasksGenerator(extractNodeAnswers(from, nl).head, from.toString, fields, MAX_ELEMENTS_PER_WORKER, limit)
      case true => TasksGenerator.selectPrimaryKeyTasksGenerator(extractNodeAnswers(from, nl), from.toString, fields, MAX_ELEMENTS_PER_WORKER, limit)
    }
    tasks.foreach(_.exec)
    status.addTasks(tasks)
    val fAssignments = tasks.map(x => Future{x.waitResults()})
    printListTaskStatus
	if(!PARALLELIZED)
	      fAssignments.map(x => Await.ready(x, Duration.Inf))
    fAssignments
  }
  
  /**
   * Creation of WHERE task
   */
  def taskWhere(select: SelectTree, where: Condition, limit: Int = DEFAULT_ELEMENTS_SELECT): List[Future[List[Assignment]]] = {
    println("Task where started")
    val taskID = generateUniqueID()
    
    val status = new TaskStatus(taskID, "WHERE")
    listTaskStatus += status
    
    printListTaskStatus
    
    val assignments = select match {case Select(nl, fields) => taskSelect(nl, fields, limit)}
    if(!PARALLELIZED)
      assignments.map(x => Await.ready(x, Duration.Inf))
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
      f})
    fAssignments
    //TODO We need to retrieve the number of tuples not eliminated by WHERE clause.
  }
  
  /**
   * Creation of JOIN task
   */
  def taskJoin(left: RootNode, right: RootNode, on: String): List[Future[List[Assignment]]] = {
 
    println("Task join started")
    val taskID = generateUniqueID()
    val status = new TaskStatus(taskID, "JOIN")
    listTaskStatus += status
    
    printListTaskStatus
    
    val a = Future { executeNode(left) }
    if(!PARALLELIZED)
      Await.ready(a, Duration.Inf)
    val b = Future { executeNode(right) }
    if(!PARALLELIZED)
      Await.ready(b, Duration.Inf)
    val resultsLeft = Await.result(a, Duration.Inf) 
    val resultsRight = Await.result(b, Duration.Inf)
    val resLeft = resultsLeft.flatMap(Await.result(_, Duration.Inf))
    val resRight = resultsRight.flatMap(Await.result(_, Duration.Inf))
    val tasks = JOIN_PAIRWISE match {
      case false => TasksGenerator.joinTasksGenerator(extractNodeAnswers(left, resLeft), extractNodeAnswers(right, resRight))
      case true => TasksGenerator.pairwiseJoinTasksGenerator(extractNodeAnswers(left, resLeft), extractNodeAnswers(right, resRight), on)
    }
    tasks.foreach(_.exec)
    status.addTasks(tasks)

    val assignments: List[Future[List[Assignment]]] = tasks.map(x => Future{x.waitResults})
    
  if(!PARALLELIZED)
		assignments.map(x => Await.ready(x, Duration.Inf))
    assignments
  }
  
  /**
   * Creation of GROUPBY task
   */
  def taskGroupBy(q: RootNode, by: String, limit: Int = DEFAULT_ELEMENTS_SELECT): List[Future[List[Assignment]]] = {
    println("Task GROUPBY")
    val taskID = generateUniqueID()
    val status = new TaskStatus(taskID, "GROUPBY")
    listTaskStatus += status
    
    printListTaskStatus
    
    val toGroupBy = executeNode(q, limit)
    if(!PARALLELIZED)
      toGroupBy.map(x => Await.ready(x, Duration.Inf))
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
      f})
    val finishedToGroupBy = toGroupBy.flatMap(x => Await.result(x, Duration.Inf))
    val tuples = extractNodeAnswers(q, finishedToGroupBy)
    fAssignments
  }
  
  /**
   * Creation of ORDERBY task
   */
  def taskOrderBy(q: Prio3Node, order: Ordering, limit: Int = DEFAULT_ELEMENTS_SELECT): List[Future[List[Assignment]]] = {
    println("Task order by")
    
    val taskID = generateUniqueID()
    val status = new TaskStatus(taskID, "ORDER BY")
    listTaskStatus += status
    printListTaskStatus
    
    val toOrder = executeNode(q, limit)
    val finishedToOrder = toOrder.flatMap(x => Await.result(x, Duration.Inf))
    val tuples = extractNodeAnswers(q, finishedToOrder)
    val tasks = TasksGenerator.orderByTasksGenerator(tuples, order)
    tasks.foreach(_.exec())
    status.addTasks(tasks)
    val assignments = Future{tasks.flatMap(_.waitResults())}::List()
    
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
   * Helper function when nodes have left and right parts
   */
  private def executeNode(node: RootNode, limit: Int = DEFAULT_ELEMENTS_SELECT): List[Future[List[Assignment]]] = {
    node match {
    case Select(nl, fields) => taskSelect(nl, fields, limit)
    case Join(left, right, on) => taskJoin(left, right, on)
    case Where(selectTree, where) => taskWhere(selectTree, where, limit)
    case Limit(query, limit) => limit match {
        case IntL(i) => startingPoint(query, i)
        case _ => startingPoint(query)
      }
    case _ => ???
    }
  }
  
  /**
   * Converts Assignments received by workers to nice String results depending on node type
   */
  private def extractNodeAnswers(node: RootNode, assignments: List[Assignment]): List[String] = {
    node match {
    case Join(_, _, _) | Where(_,_) => assignments.flatMap(_.getAnswers().asScala.toMap.filter(_._2.toString.endsWith("yes")).map(ans => ans._2.toString.substring(0, ans._2.toString.length-8)))
    case Group(_,_) => assignments.flatMap(_.getAnswers().asScala.toMap).map(s => stringToTuple(s._2.toString)).groupBy(_._2).map(x=> x.toString).toList
    case _ => assignments.flatMap(_.getAnswers().asScala.toMap).flatMap(_._2.toString.stripMargin.split("[\r\n\r\n]").toList)
    }
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
    if (haveAllFinished && getListTaskStatus.size > 0) getListTaskStatus.map(_.getEndTime).max
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
      "query_string" -> JsString(this.queryString),
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
  
  /**
   * Print the status of all tasks related to this query
   */
  def printListTaskStatus() = {
    println("Task status summary : ")
    getListTaskStatus().foreach(println)
    println(getJSON.toString) // TODO we print JSONs for testing only
  }
}

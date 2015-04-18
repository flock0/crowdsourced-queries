package queryExecutor
import parser.Parser
import tree.Tree._
import crowdsourced.mturk._
import scala.collection.mutable.LinkedList
import java.text.SimpleDateFormat
import java.util.Calendar
import scala.util.Random
import scala.collection.JavaConverters._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{ Success, Failure }

class QueryExecutor() {

  var listTaskStatus = List[TaskStatus]()

  val DEFAULT_ELEMENTS_SELECT = 4
  val MAX_ELEMENTS_PER_WORKER = 2

  def generateUniqueID(): String = new SimpleDateFormat("y-M-d-H-m-s").format(Calendar.getInstance().getTime()).toString + "--" + new Random().nextInt(10000)

  def parse(query: String): Q = {
    return Parser.parseQuery(query).get
  }

  def execute(query: Q) = {
    println("Starting execution of the query : \"" + query + "\"")
    def startingPoint(node: Q): List[Future[List[Assignment]]] = node match {
      // TODO we need to pass a limit to taskSelect. The dataset could be very small or huge...
      // TODO maybe get this from the request using the LIMIT keyword. Or ask a worker for number of elements in the web page
      case Select(nl, fields) => taskSelect(nl, fields, DEFAULT_ELEMENTS_SELECT)
      case Join(left, right, on) => taskJoin(left, right, on) //recursiveTraversal(left); recursiveTraversal(right);
      case Where(selectTree, where) => taskWhere(selectTree, where)
      case OrderBy(query, ascendingOrDescending, field) => taskOrderBy(query,ascendingOrDescending, field) //TODO
      case GroupBy(query,by)=>taskGroupBy(query,by)
      case _ => List[Future[List[Assignment]]]() //TODO

    }
    startingPoint(query)
  }

  def taskWhere(select: SelectTree, where: Condition): List[Future[List[Assignment]]] = {
    println("Task where started")
    var results = List[String]()
    val idStatus = generateUniqueID()
    val status = new TaskStatus(idStatus)
    status.setOperator("WHERE")
    status.setNumberHits(DEFAULT_ELEMENTS_SELECT)
    listTaskStatus = listTaskStatus ::: List(status)
    printListTaskStatus
    val assignments = select match {case Select(nl, fields) => taskSelect(nl, fields, DEFAULT_ELEMENTS_SELECT)}
    val fAssignments = assignments.map(x => {
      val p = promise[List[Assignment]]()
      val f = p.future //Future[List[Assignment]]
      x onSuccess { //onSuccess of Future[List[Assignment]]
      case a => {
        val tasks = whereTasksGenerator(extractSelectAnswers(a), where)
        tasks.foreach(_.exec)
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
  
  
  def taskSelect(from: Q, fields: List[P], limit: Int): List[Future[List[Assignment]]] = {
    println("Task select started")
    for (i <- List.range(1, limit, MAX_ELEMENTS_PER_WORKER)) {
      println(List.range(1, limit, MAX_ELEMENTS_PER_WORKER) + " " + i + " " + Math.min(i + MAX_ELEMENTS_PER_WORKER - 1, limit))
    }
    val idStatus = generateUniqueID()
    val status = new TaskStatus(idStatus)
    status.setOperator("Select")
    status.setNumberHits(List.range(1, limit, MAX_ELEMENTS_PER_WORKER).size)
    listTaskStatus = listTaskStatus ::: List(status)
    printListTaskStatus

    val NLAssignments: List[Assignment] = from match { case NaturalLanguage(nl) => taskNaturalLanguage(nl, fields) }
    
    val tasks: List[AMTTask] = selectTasksGenerator(extractNaturalLanguageAnswers(NLAssignments), from.toString, fields, limit)
    tasks.foreach(_.exec) // submit all tasks (workers can then work in parallel)

    val assignments: List[Future[List[Assignment]]] = tasks.map(x => Future{x.waitResults}) // we wait for all results from all workers

    status.setCurrentStatus("Finished")
    //TODO We need to pass the status object to the AMT task in order to obtain the number of finished hits at any point.
    printListTaskStatus

    assignments
  }
  
  def executeNode(node: Q): List[Future[List[Assignment]]] = {
    node match {
		case Select(nl, fields) => taskSelect(nl, fields, DEFAULT_ELEMENTS_SELECT)
		case Join(left, right, on) => taskJoin(left, right, on)
		case Where(selectTree, where) => taskWhere(selectTree, where)
		case _ => ???
    }
  }
    
  def extractNodeAnswers(node: Q, assignments: List[Assignment]): List[String] = {
    node match {
    case Select(nl, fields) => extractSelectAnswers(assignments)
    case Join(left, right, on) => extractJoinAnswers(assignments)
    case Where(selectTree, where) => extractWhereAnswers(assignments)
    case _ => ???
    }
  }
  def printGroupByRes(tuples: List[String], fAssignments: List[Future[List[Assignment]]]) = {
     var results = List[(String,String)]()
     val assignments = fAssignments.flatMap(x => Await.result(x, Duration.Inf))
    tuples.zip(assignments).map(x =>{
       val answersMap = x._2.getAnswers().asScala.toMap
        answersMap.foreach{case(key, value) => { 
          results = results ::: List((x._1,value.toString))}
        }})
  
    results
  }
  def taskGroupBy(q: Q, by: String) = {
    println("Task GROUPBY")
    val toGroupBy = executeNode(q)
//    val finishedToGroupBy = toGroupBy.flatMap(x => Await.result(x, Duration.Inf))
//    val tuples = extractNodeAnswers(q, finishedToGroupBy)
//    val tasks = groupByTasksGenerator(tuples,by)
//    tasks.foreach(_.exec)
//    val assignments = tasks.map(x => Future{x.waitResults})
    val fAssignments = toGroupBy.map(x => {
      val p = promise[List[Assignment]]()
      val f = p.future //Future[List[Assignment]]
      x onSuccess { //onSuccess of Future[List[Assignment]]
      case a => {
        val tasks = groupByTasksGenerator(extractNodeAnswers(q, a), by)
        tasks.foreach(_.exec)
        p success tasks.flatMap(_.waitResults)
      }
      }
      f
      })
    //println(extractGroupByAnswers(tuples,assignments))
    val finishedToGroupBy = toGroupBy.flatMap(x => Await.result(x, Duration.Inf))
    val tuples = extractNodeAnswers(q, finishedToGroupBy)
    Future{println(printGroupByRes(tuples, fAssignments).groupBy(x=>x._2))}
    fAssignments
  }

  def taskJoin(left: Q, right: Q, on: String) = {
    val a = Future { executeNode(left) }
    val b = Future { executeNode(right) }
    println("Task join")
    
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
    val tasks = joinTasksGenerator(extractNodeAnswers(left, resLeft), extractNodeAnswers(right, resRight))
    tasks.foreach(_.exec) // submit all tasks (workers can then work in parallel)

    val assignments: List[Future[List[Assignment]]] = tasks.map(x => Future{x.waitResults})
    
//    println("Final results " + extractJoinAnswers(assignments))

    assignments
  }
  
  def ascOrDesc(order: O): String = order match {
      case ASC() => "ascending"
      case DESC() => "descending"
    
  }
  
  def taskOrderBy(q: Q3, order: O, by: String): List[Future[List[Assignment]]] = {
    val toOrder = executeNode(q)
    val finishedToOrder = toOrder.flatMap(x => Await.result(x, Duration.Inf))
    val tuples = extractNodeAnswers(q, finishedToOrder)
    val questionTitle = "Sort a list of " + tuples.size +" elements."
    val questionDescription = "Please sort the following list : [ " + tuples.mkString(", ") + " ]  on [ " + by + " ] attribute by [ " + ascOrDesc(order) + " ] order, please put only one element per line."
    val keywords = List("URL retrieval", "Fast")
    val expireTime = 60 * 30 // 30 minutes
    val numAssignments = 1
    val rewardUSD = 0.01 toFloat
    val question: Question = new StringQuestion(generateUniqueID(), questionTitle, questionDescription)
    val hit = new HIT(questionTitle, questionDescription, List(question).asJava, expireTime, numAssignments, rewardUSD, 3600, keywords.asJava)
    val task = new AMTTask(hit)
    val assignments = Future{task.execBlocking()}::List()
    
//    println("Final results " + extractOrderByAnswers(assignments))
    assignments
  }
  def taskNaturalLanguage(s: String, fields: List[P]): List[Assignment] = {
    println("Task natural language")

    val idStatus = generateUniqueID()
    val status = new TaskStatus(idStatus)
    status.setOperator("FROM")
    status.setNumberHits(1)
    listTaskStatus = listTaskStatus ::: List(status)
    printListTaskStatus

    val questionTitle = "Find URL containing required information"
    val questionDescription = "What is the most relevant website to find [" + s + "] ?\nNote that we are interested by : " + fields.mkString(", ")
    val keywords = List("URL retrieval", "Fast")
    val expireTime = 60 * 30 // 30 minutes
    val numAssignments = 1
    val rewardUSD = 0.01 toFloat

    val question: Question = new URLQuestion(generateUniqueID(), questionTitle, questionDescription)
    val hit = new HIT(questionTitle, questionDescription, List(question).asJava, expireTime, numAssignments, rewardUSD, 3600, keywords.asJava)
    val task = new AMTTask(hit)
    val assignments = task.execBlocking()

    status.setCurrentStatus("finished")
    status.setTaskResultNumber(1) //TODO to change if we choose that workers should retrieve more URLs
    printListTaskStatus
    //TODO We need to pass the status object to the AMT task in order to obtain the number of finished hits at any point.

    assignments
  }

  /**
   * Extract the URL from a Natural language task.
   */
  def extractNaturalLanguageAnswers(assignments: List[Assignment]): String = {
    val firstNLAssignment:Assignment = assignments.head
    val (uniqueID, answer) = firstNLAssignment.getAnswers().asScala.head // retrieving first answer of first assignment
    answer.toString
  }
  
  /**
   * Extract the list of tuples from a Select task.
   */
  def extractSelectAnswers(assignments: List[Assignment]): List[String] = {
    var results = List[String]()
    assignments.foreach(ass => {
        println("Assignment result :")
        val answersMap = ass.getAnswers().asScala.toMap
        
        println(answersMap)
        answersMap.foreach{case(key, value) => { 
            results = results ::: value.toString.stripMargin.split("[\n\r]").toList //Take care of multilines answer
         }}
       
      })
     results
  }
  def extractOrderByAnswers(assignments: List[Assignment]): List[String] = {
    var results = List[String]()
    assignments.foreach(ass => {
        println("Assignment result :")
        val answersMap = ass.getAnswers().asScala.toMap
        
        println(answersMap)
        answersMap.foreach{case(key, value) => { 
            results = results ::: value.toString.stripMargin.split("[\n\r]").toList //Take care of multilines answer
         }}
       
      })
     results
  }
  /**
   * Extract the list of tuple satisfying the where clause.
   */
  def extractWhereAnswers(assignments: List[Assignment]): List[String] = {
    var results = List[String]()
    assignments.foreach{ass => {
        val answersMap = ass.getAnswers().asScala.toMap
        answersMap.foreach{case(key, value) => { 
          val res = value.toString.split(",")//// TODO proper way
          if (res(1) == "yes") {results = results ::: List(res(0))}
        }}}}
    results
  }
  
  def extractJoinAnswers(assignments: List[Assignment]): List[String] = {
    var results = List[String]()
    assignments.foreach{ass => {
        val answersMap = ass.getAnswers().asScala.toMap
        answersMap.foreach{case(key, value) => { 
          val res = value.toString.split(",")//// TODO proper way
          if (res(1) == "yes") {results = results ::: List(res(0))}
        }}}}
    results
  }
  
  def extractGroupByAnswers(tuples: List[String], assignments: List[Assignment]): List[(String,String)] = {
    var results = List[(String,String)]()
    tuples.zip(assignments).map(x =>{
       val answersMap = x._2.getAnswers().asScala.toMap
        answersMap.foreach{case(key, value) => { 
          results = results ::: List((x._1,value.toString))}
        }})
  
    results
  }
  
  
  /**
   * Helper function to create a list of AMTTask to split the data retrieval jobs between several workers
   */
  def selectTasksGenerator(url: String, nl: String, fields: List[P], limit: Int): List[AMTTask] = {

    // tuples of (start, end) for each worker
    val tuples = for (i <- List.range(1, limit + 1, MAX_ELEMENTS_PER_WORKER)) yield (i, Math.min(i + MAX_ELEMENTS_PER_WORKER - 1, limit))

    val tasks = tuples.map { tuple =>
      val (start: Int, end: Int) = tuple
      val fieldsString = fields.mkString(", ")
      val questionTitle = "Data extraction from URL"
      val questionDescription = s"""On this website, retrieve the following information ($fieldsString) about $nl
                              Select only items in the range $start to $end (both included)
                              URL : $url
                              Please provide one element per line."""
      val question: Question = new StringQuestion(generateUniqueID(), questionTitle, questionDescription)
      val questionList = List(question)
      val numWorkers = 1
      val rewardUSD = 0.01 toFloat
      val expireTime = 60 * 60 // 60 minutes
      val keywords = List("data extraction", "URL", "easy")
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, expireTime, numWorkers, rewardUSD, 3600, keywords.asJava)

      new AMTTask(hit)
    }

    tasks
  }

  /**
   * Helper function to create a list of AMTTask to split the data retrieval jobs between several workers
   */
  def whereTasksGenerator(answers: List[String], where: Condition): List[AMTTask] = {
    println(answers)
    val tasks = answers.map(ans => {
      val questionTitle = "Evaluate if a claim makes sense"
      val questionDescription = "Is [" + ans + "] coherent/true for the following predicate : " + where + " ?"
      val optionYes = new MultipleChoiceOption(ans + ",yes", "yes")
      val optionNo = new MultipleChoiceOption(ans + ",no", "no")
      val listOptions = List(optionYes, optionNo)
      val question: Question = new MultipleChoiceQuestion(generateUniqueID(), questionTitle, questionDescription, listOptions.asJava)
      val questionList = List(question)
      val numWorkers = 1
      val rewardUSD = 0.01 toFloat
      val expireTime = 60 * 60 // 60 minutes
      val keywords = List("Claim evaluation", "Fast", "easy")
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, expireTime, numWorkers, rewardUSD, 3600, keywords.asJava)

      new AMTTask(hit)
    })
    tasks
  }

  def joinTasksGenerator(R: List[String], S: List[String]): List[AMTTask] = {
    val tasks = R.map(r => {
      val questionTitle = "Is the following element part of a list"
      val questionDescription = "Is [" + r + "] present in the following list : " + S.mkString(", ") + " ?"
      val optionYes = new MultipleChoiceOption(r + ",yes", "yes")
      val optionNo = new MultipleChoiceOption(r + ",no", "no")
      val listOptions = List(optionYes, optionNo)
      val question: Question = new MultipleChoiceQuestion(generateUniqueID(), questionTitle, questionDescription, listOptions.asJava)
      val questionList = List(question)
      val numWorkers = 1
      val rewardUSD = 0.01 toFloat
      val expireTime = 60 * 60 // 60 minutes
      val keywords = List("Claim evaluation", "Fast", "easy")
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, expireTime, numWorkers, rewardUSD, 3600, keywords.asJava)

      new AMTTask(hit)
    })
    tasks
  }
  
  def groupByTasksGenerator(tuples: List[String], by: String): List[AMTTask] = {
    val tasks = tuples.map(tuple=> {
      val questionTitle = "Simple question"
      val questionDescription = "For the following element [ " + tuple + " ], what is its [ " + by + " ] ?" 
      val question: Question = new StringQuestion(generateUniqueID(), questionTitle, questionDescription)
      val questionList = List(question)
      val numWorkers = 1
      val rewardUSD = 0.01 toFloat
      val expireTime = 60 * 60 // 60 minutes
      val keywords = List("simple question", "question", "easy")
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, expireTime, numWorkers, rewardUSD, 3600, keywords.asJava)
      new AMTTask(hit)
    })
    tasks
  }

  def getListTaskStatus() = this.listTaskStatus

  def printListTaskStatus() = {
    println("Task status summary : ")
    this.listTaskStatus.foreach(status => {
      println("    Task " + status.getStatusId)
      println("        Current Status : " + status.getCurrentStatus)
      println("        Operator : " + status.getOperator)
      println("        Number of hits : " + status.getNumberHits)
      println("        Number of finished hits " + status.getFinishedNumberHits)
      println("        Number of results : " + status.getTaskResultNumber)
    })
  }

}

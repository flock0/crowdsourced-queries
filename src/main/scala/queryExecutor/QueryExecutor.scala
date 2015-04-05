package queryExecutor
import parser.Parser
import tree.Tree._
import crowdsourced.mturk._
import scala.collection.mutable.LinkedList
import java.text.SimpleDateFormat
import java.util.Calendar
import scala.util.Random
import scala.collection.JavaConverters._

class QueryExecutor() {
  
  val DEFAULT_ELEMENTS_SELECT = 3
  val MAX_ELEMENTS_PER_WORKER = 2
  
  def generateUniqueID():String = new SimpleDateFormat("y-M-d-H-m-s").format(Calendar.getInstance().getTime()).toString +"--"+ new Random().nextInt(10000) 
  
  def parse(query: String): Q = {
    return Parser.parseQuery(query).get
  }

  def execute(query: Q) = {

    def startingPoint(node: Q): Unit = node match{
        // TODO we need to pass a limit to taskSelect. The dataset could be very small or huge...
        // TODO maybe get this from the request using the LIMIT keyword. Or ask a worker for number of elements in the web page
        case Select(nl, fields) => taskSelect(nl, fields, DEFAULT_ELEMENTS_SELECT)
        case Join(left,right,on) => taskJoin(left, right, on)//recursiveTraversal(left); recursiveTraversal(right);
        case Where(selectTree, where) => taskWhere(selectTree, where)
        case OrderBy(query, ascendingOrDescending, field) => taskOrderBy(query, ascendingOrDescending,field)
        case _ => println("Problem");

    }
      println("Starting execution of the query : \"" + query + "\"")
      startingPoint(query)
      //println(t)
  }

  def taskWhere(select: SelectTree, where: Condition) = {
    println("Task where started")
    val assignments = select match {case Select(nl, fields) => taskSelect(nl, fields, DEFAULT_ELEMENTS_SELECT) }
    println("Task where not implemented.")
    println("Displaying results of Select instead.\n")
    
        
    assignments.foreach(ass => {
      println("Assignment result :")
      
      val answersMap = ass.getAnswers(
      println(answersMap)
      /*
      val questionTitle = "Evaluate if a claim makes sense"
      val questionDescription = "Is " + tuple + " coherent/true for the following predicate : " + where + " ?"
      val optionYes = new MultipleChoiceOption("yes","yes")
      val optionNo = new MultipleChoiceOption("no","no")
      val listOptions = List(optionYes,optionNo)
      val question: Question = new MultipleChoiceQuestion(generateUniqueID(), questionTitle, questionDescription, listOptions.asJava) 
      val questionList = List(question)
      val numWorkers = 1
      val rewardUSD = 0.02 toFloat
      val expireTime = 60 * 60 // 60 minutes
      val keywords = List("Claim evaluation", "Fast", "easy")
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, expireTime, numWorkers, rewardUSD, 3600, keywords.asJava) 
      
      val task = new AMTTask(hit).exec*/
    })
  }
  
  def taskSelect(from: Q, fields: List[P], limit: Int): List[Assignment] = {
    println("Task select started")
    val NLAssignments: List[Assignment] = from match {case NaturalLanguage(nl) => taskNaturalLanguage(nl, fields) }   
    println("NL task has finished")
    val firstNLAssignment:Assignment = NLAssignments.head
    val (uniqueID, answer) = firstNLAssignment.getAnswers().asScala.head // retrieving first answer of first assignment
    val url = answer.toString
    
    val tasks: List[AMTTask] = selectTasksGenerator(url, from.toString, fields, limit)
    tasks.foreach(_.exec) // submit all tasks (workers can then work in parallel)
    
    Thread sleep 30 * 1000 // wait 30 seconds (useless as we wait just below, but it's fun to wait)
    val assignments: List[Assignment] = tasks.flatMap(_.waitResults) // we wait for all results from all workers
    
    assignments
  }
  
  def taskJoin(left: Q, right: Q, on: String) = {
    val a = left match  {
      case Select(nl,fields)=> taskSelect(nl, fields, DEFAULT_ELEMENTS_SELECT)
      case Where(select,where) => taskWhere(select, where)
      //case NaturalLanguage(s) => taskNaturalLanguage(s)
    }
    
    val b = right match  {
      case Select(nl,fields) => taskSelect(nl, fields, DEFAULT_ELEMENTS_SELECT)
      case Where(select,where) => taskWhere(select, where)
      //case NaturalLanguage(s) => taskNaturalLanguage(s)
    }
    println("Task join")
    
  }
  def taskOrderBy(q: Q3, order: O, by: String) = {
    val a = q match  {
      case Select(nl,fields) => taskSelect(nl, fields, DEFAULT_ELEMENTS_SELECT)
      case Where(select,where) => taskWhere(select, where)
    }
     println("Task order by")
  }
  
  def taskNaturalLanguage(s: String, fields: List[P]): List[Assignment] = {
    
    println("Task natural language")
    
    val questionTitle = "Find URL containing required information"
    val questionDescription = "What is the most relevant website to find ["+s+"] ?\nNote that we are interested by : "+fields.mkString(", ")
    val keywords = List("URL retrieval","Fast")
    val expireTime = 30 * 60 // 30 minutes
    val numAssignments = 1
    val rewardUSD = 0.02 toFloat
    
    val question: Question = new URLQuestion(generateUniqueID(), questionTitle, questionDescription)
    val hit = new HIT(questionTitle, questionDescription, List(question).asJava, expireTime, numAssignments, rewardUSD, 3600, keywords.asJava) 
    
    println("     Asking worker : "+questionDescription)
    
    val task = new AMTTask(hit)
    
    task.execBlocking()
  }
  
  /**
   * Helper function to create a list of AMTTask to split the data retrieval jobs between several workers
   */
  def selectTasksGenerator(url: String, nl: String, fields:List[P], limit: Int):List[AMTTask] = {
    
    // tuples of (start, end) for each worker
    val tuples = for (i <- List.range(1, limit, MAX_ELEMENTS_PER_WORKER)) yield (i, Math.min(i + MAX_ELEMENTS_PER_WORKER-1, limit))
    
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
      val rewardUSD = 0.02 toFloat
      val expireTime = 60 * 60 // 60 minutes
      val keywords = List("data extraction", "URL", "easy")
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, expireTime, numWorkers, rewardUSD, 3600, keywords.asJava) 
      
      new AMTTask(hit)
    }
    
    tasks
  }
  
}

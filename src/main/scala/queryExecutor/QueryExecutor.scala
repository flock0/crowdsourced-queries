package queryExecutor
import parser.Parser
import tree.Tree._
import crowdsourced.mturk.HIT
import crowdsourced.mturk.Question
import crowdsourced.mturk.StringQuestion
import crowdsourced.mturk.URLQuestion
import java.text.SimpleDateFormat
import java.util.Calendar
import scala.collection.JavaConverters._

class QueryExecutor() {

  def parse(query: String): Q = {
    return Parser.parseQuery(query).get
  }

  def execute(query: Q) = {

    def startingPoint(node: Q): Unit = node match{

        case Select(nl, fields) => taskSelect(nl,fields)
        case Join(left,right,on) => taskJoin(left,right,on)//recursiveTraversal(left); recursiveTraversal(right);
        case Where(selectTree, where) => taskWhere(selectTree, where)
        case OrderBy(query, ascendingOrDescending, field) => taskOrderBy(query,ascendingOrDescending,field)
        case _ => println("Problem");

    }
      println("Starting execution of the query : \"" + query + "\"")
      startingPoint(query)
      //println(t)
  }

  def taskWhere(select: SelectTree, where: Condition) = {
    val a = select match {case Select(nl, fields) => taskSelect(nl,fields)}
    println("Task where")
    
  }
  def taskSelect(from: Q, fields: List[P]) = {
    
    val NLTask: AMTTask = from match {case NaturalLanguage(s) => taskNaturalLanguage(s,fields)}   
    println("Task select")
    
    NLTask.onFinish(() => {
      println("NL task has finished.")
      
      val timeID = new SimpleDateFormat("y-M-d-H-m-s").format(Calendar.getInstance().getTime())
      val questionTitle = "On this website, retrieve all distinct elements with 
          the following information (" +fields.mkString(", ")\n URL : " + NLTask.getAnswer().toString() + " "
      val questionDescription = "Please provide one elemen per line"
      val question: Question = new StringQuestion(timeID,"Extract information from website", questionTitle)
      val questionList = List(question)
      val numWorkers = 1
      val rewardUSD = 0.01 toFloat
      val keywords = List("Extract information from URL")
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, 31536000, numWorkers, rewardUSD, 3600, keywords.asJava) 
      
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, 31536000, numWorkers, rewardUSD, 3600, keywords.asJava) 
    
      println("    Asking worker : "+questionTitle)
    
      val task = new AMTTask(hit)
      task.exec()
    
      task
    }
  }
  
  def taskJoin(left: Q, right: Q, on: String) = {
    val a = left match  {
      case Select(nl,fields)=> taskSelect(nl, fields)
      case Where(select,where) => taskWhere(select, where)
      //case NaturalLanguage(s) => taskNaturalLanguage(s)
    }
    
    val b = right match  {
      case Select(nl,fields) => taskSelect(nl, fields)
      case Where(select,where) => taskWhere(select, where)
      //case NaturalLanguage(s) => taskNaturalLanguage(s)
    }
    println("Task join")
    
  }
  def taskOrderBy(q: Q3, order: O, by: String) = {
    val a = q match  {
      case Select(nl,fields) => taskSelect(nl, fields)
      case Where(select,where) => taskWhere(select, where)
      
    }
     println("Task order by")
  }
  
  def taskNaturalLanguage(s: String, fields: List[P]) = {
    
    println("Task natural language")
    
    val timeID = new SimpleDateFormat("y-M-d-H-m-s").format(Calendar.getInstance().getTime())
    val questionTitle = "What is the most relevant website to find ["+s+"] ?\nWe are interested by : "+fields.mkString(", ")
    val questionDescription = "Select URL from which other workers can extract required information"
    val question: Question = new URLQuestion(timeID,"Find the most relevant website",questionTitle)
    val questionList = List(question)
    val numWorkers = 1
    val rewardUSD = 0.01 toFloat
    val keywords = List("URL retrieval","Fast")
    val hit = new HIT(questionTitle, questionDescription, questionList.asJava, 31536000, numWorkers, rewardUSD, 3600, keywords.asJava) 
    
    println("    Asking worker : "+questionTitle)
    
    val task = new AMTTask(hit)
    task.exec()
    
    task
  }
}

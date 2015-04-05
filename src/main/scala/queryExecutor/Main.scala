package queryExecutor
import crowdsourced.mturk._
import java.text.SimpleDateFormat
import java.util.Calendar
import scala.collection.JavaConverters._

object Main extends App {
  
 // val query = "SELECT (full name, date of birth) FROM [Presidents of the USA] WHERE [political party is democrat] ORDER BY date of birth"
  //val query = "SELECT (movies) FROM [Movies with Angelina Jolie] JOIN SELECT (movies) FROM [Movies with Brad Pitt] ON movies"
  
  val query = "SELECT (full name, date of birth) FROM [Presidents of USA] WHERE [political party is democrat]"
  
  val queryExec = new QueryExecutor
  val parsedQuery = queryExec.parse(query)
  val executedQuery = queryExec.execute(parsedQuery)
  
  
  /*
  val s = "Presidents of the USA"
  val fields = List("full name")
  // manual creation of a HIT
  val timeID = new SimpleDateFormat("y-M-d-H-m-s").format(Calendar.getInstance().getTime())
  val questionTitle = "Find URL containing required information"
  val questionDescription = "What is the most relevant website to find ["+s+"] ?\nNote that we are interested by : "+fields.mkString(", ")
  val keywords = List("URL retrieval","Fast")
  val expireTime = 30 * 60 // 30 minutes
  val numAssignments = 1
  val rewardUSD = 0.01 toFloat
  
  val question: Question = new StringQuestion(timeID, questionTitle, questionDescription)
  val hit = new HIT(questionTitle, questionDescription, List(question).asJava, expireTime, numAssignments, rewardUSD, 3600, keywords.asJava) 
  
  println("     Asking worker : "+questionDescription)
  
  val assignments:List[Assignment] = new AMTTask(hit).execBlocking()
  
  //AMTCommunicator.checkBalance(74.74 toFloat)
  
  assignments.foreach(ass => {
      println("Assignment result :")
      val answersMap: Map[String, Answer] = ass.getAnswers().asScala.toMap
      answersMap.foreach { case (key, value) => println(key+" => "+value) }
    })
    
 */
  
}
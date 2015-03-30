package queryExecutor
import crowdsourced.mturk._
import java.text.SimpleDateFormat
import java.util.Calendar
import scala.collection.JavaConverters._

object Main extends App {
  
  //val query = "SELECT (full name) FROM [Presidents of the USA] WHERE [political party is democrat] ORDER BY age of death"
  //val query = "SELECT (movies) FROM [Movies with Angelina Jolie] JOIN SELECT (movies) FROM [Movies with Brad Pitt] ON movies"
  //val queryExec = new QueryExecutor
  //val parsedQuery = queryExec.parse(query)
  //val executedQuery = queryExec.execute(parsedQuery)
  
  
  // manual creation of a HIT
  val timeID = new SimpleDateFormat("y-M-d-H-m-s").format(Calendar.getInstance().getTime())
  println(timeID)
  //val questionTitle = "What is the most relevant website to find the complete list of USA presidents"
  val questionTitle = "Find URL containing required information"
  val questionDescription = "What is the most relevant website to find this information : Presidents of the USA ?"
  val question: Question = new URLQuestion(timeID, questionTitle, questionDescription)
  val questionList = List(question)
  val numWorkers = 1
  val rewardUSD = 0.02 toFloat
  val keywords = List("URL retrieval","Fast", "URL", "extraction")
  val hit = new HIT(questionTitle, questionDescription, questionList.asJava, 31536000, numWorkers, rewardUSD, 3600, keywords.asJava) 
  
  //AMTCommunicator.checkBalance(74.74 toFloat)
  val task = new AMTTask(hit)
  val assignments : List[Assignment] = task.execBlocking()
  
  assignments.foreach(ass => {
      println("Assignment result :")
      val answersMap: Map[String, Answer] = ass.getAnswers().asScala.toMap
      answersMap.foreach { case (key, value) => println(key+" => "+value) }
    })
  
}
package queryExecutor
import crowdsourced.mturk._
import java.text.SimpleDateFormat
import java.util.Calendar
import scala.collection.JavaConverters._
object Main extends App {
  
  val query = "SELECT (full name) FROM [Presidents of the USA] WHERE [political party is democrat] ORDER BY age of death"
  //val query = "SELECT (movies) FROM [Movies with Angelina Jolie] JOIN SELECT (movies) FROM [Movies with Brad Pitt] ON movies"
  val queryExec = new QueryExecutor
  val parsedQuery = queryExec.parse(query)
  val executedQuery = queryExec.execute(parsedQuery)
  
  val timeID = new SimpleDateFormat("y-M-d-H-m-s").format(Calendar.getInstance().getTime())
  println(timeID)
  val questionTitle = "What is the most relevant website to find the complete list of USA presidents"
  val questionDescription = "Select URL from which other workers can extract required information"
  val question: Question = new URLQuestion(timeID,"Find the most relevant website",questionTitle)
  val questionList = List(question)
  val numWorkers = 3
  val rewardUSD = 0.005 toFloat
  val keywords = List("URL retrieval","Fast")
  val HIT = new HIT(questionTitle, questionDescription, questionList.asJava, 31536000, numWorkers, rewardUSD, 3600, keywords.asJava) 
  //val amtCommunicator = new AMTCommunicator
}
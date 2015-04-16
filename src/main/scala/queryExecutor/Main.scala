package queryExecutor
import crowdsourced.mturk._
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Arrays
import scala.collection.JavaConverters._

object Main extends App {
  
 // val query = "SELECT (full name, date of birth) FROM [Presidents of the USA] WHERE [political party is democrat] ORDER BY date of birth"
  //val query = "SELECT (movies) FROM [Movies with Angelina Jolie] JOIN SELECT (movies) FROM [Movies with Brad Pitt] ON movies"
  
  val query = "SELECT (full name) FROM [Presidents of USA] WHERE [political party is democrat]"
  
  val queryExec = new QueryExecutor
  val parsedQuery = queryExec.parse(query)
  //val executedQuery = queryExec.execute(parsedQuery)
  val nodeCount = parsedQuery.countNodes
    
  val questionTitle = "Data extraction from URL"
      val questionDescription = "Description"
      val question: Question = new StringQuestion("dummy_id", questionTitle, questionDescription)
      val questionList = List(question)
      val numWorkers = 1
      val rewardUSD = 0.02 toFloat
      val expireTime = 60 * 60 // 60 minutes
      val keywords = List("data extraction", "URL", "easy")
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, expireTime, numWorkers, rewardUSD, 3600, keywords.asJava) 
  val task =new AMTTask(hit)
  val assignments  = task.execBlocking()
  assignments.foreach(ass => {
        println("Assignment result :")
        val answersMap = ass.getAnswers().asScala.toMap
        answersMap.foreach{case(key, value)=>{
          val s = value.toString.stripMargin.split("[\n\r]").toList
          println(s.size)
          s.foreach(chaar => println(chaar+" "+chaar.size))
          
        }    }  
  })
    /*
    val questionDescription = "Is [ abc ] coherent/true for the following predicate : [ def ] ?"
            val optionYes = new MultipleChoiceOption("yes","yes")
            val optionNo = new MultipleChoiceOption("no","no")
            val listOptions = List(optionYes,optionNo)
            val question: Question = new MultipleChoiceQuestion("Dummy_id", "Claim evaluation", questionDescription, listOptions.asJava) 
            val questionList = List(question)
            val numWorkers = 1
            val rewardUSD = 0.02 toFloat
            val expireTime = 60 * 60 // 60 minutes
            val keywords = List("Claim evaluation", "Fast", "easy")
            val hit = new HIT("Claim evaluation", questionDescription, questionList.asJava, expireTime, numWorkers, rewardUSD, 3600, keywords.asJava) 
            val amtTask = new AMTTask(hit)
            val task = amtTask.exec
            println("----")
            println(task.getHIT().getHITId())
            println("----")*/
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
package queryExecutor
import tree.Tree._
import crowdsourced.mturk.Answer
import crowdsourced.mturk.AnswerCallback
import crowdsourced.mturk.Assignment
import scala.collection.JavaConverters._

class HITAnswersHandler extends AnswerCallback {
  
  def newAssignmentsReceived(newAssignments: java.util.List[Assignment]): Unit = {
    val assignments: List[Assignment] = newAssignments.asScala.toList
    
    // print results in console
    assignments.foreach(ass => {
      println("Assignment result :")
      val answersMap: Map[String, Answer] = ass.getAnswers().asScala.toMap
      answersMap.foreach { case (key, value) => println(key+" => "+value) }
    })
    
  }
  
  def jobFinished(): Unit = {
     println("HIT finished successfully.")
  }
  
  def errorOccured(): Unit = {
    println("Error occurred while handling answers from HIT.")
  }
}

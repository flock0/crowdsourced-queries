package queryExecutor

import tree.Tree._
import crowdsourced.mturk.AMTCommunicator
import crowdsourced.mturk.HIT
import crowdsourced.mturk.PendingJob
import crowdsourced.mturk.Answer
import crowdsourced.mturk.AnswerCallback
import crowdsourced.mturk.Assignment
import scala.collection.JavaConverters._

/**
 * Represents an AMT task and implements the AnswerCallback interface.
 * When calling exec(), the HIT is sent to AMT.
 * The onFinish method takes a function in argument that is executed when HIT is finished.
 * @author Francois Farquet, Joachim Hugonot
 */
class AMTTask(val hit: HIT) extends AnswerCallback  {
  
  // Attributes definition
  private var assignments: List[Assignment] = null
  private var finished: Boolean = false
  private var toExec : () => Unit = null
  
  /**
   * Sends the request to AMT and set itself as the callback object
   * This function is non-blocking
   */
  def exec(): PendingJob = AMTCommunicator.sendHIT(hit, this) // TODO modify it to the blocking sendHIT()
  
  /**
   * Sends the request to AMT and set itself as the callback object
   * This function is blocking
   */
  def execBlocking(): List[Assignment] = {
    val pj : PendingJob = AMTCommunicator.sendHIT(hit, this)
    while(pj.getAssignments.size <= 0) {
      Thread sleep 5000
    }
    pj.getAssignments.asScala.toList
  }
  
  /**
   * Returns true if the HIT has finished
   */
  def isFinished(): Boolean = this.finished
  
  /**
   * Executes the given function when AMT HIT has finished.
   */
  def onFinish(f : () => Unit) = { toExec = f }
  
  /**
   * Callback method executed automatically when AMT returns results.
   */
  override def newAssignmentsReceived(newAssignments: java.util.List[Assignment]): Unit = {
    println("New assignments received from AMT.")
    
    val assignList: List[Assignment] = newAssignments.asScala.toList
    this.assignments = assignList
    
    // print results in console
    assignments.foreach(ass => {
      println("Assignment result :")
      val answersMap: Map[String, Answer] = ass.getAnswers().asScala.toMap
      answersMap.foreach { case (key, value) => println(key+" => "+value) }
    })
  }
  
  /**
   * Callback method automatically executed when AMT has finished processing the HIT.
   */
  override def jobFinished(): Unit = {
    this.finished = true
     println("HIT finished successfully.")
  }
  
 /**
   * Callback method automatically executed when AMT returns an error.
   */
  override def errorOccured(): Unit = {
    println("Error occurred while handling answers from HIT.")
  }
}
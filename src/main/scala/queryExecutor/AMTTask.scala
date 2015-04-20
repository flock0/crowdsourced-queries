package queryExecutor

import tree.Tree._
import crowdsourced.mturk.AMTCommunicator
import crowdsourced.mturk.HIT
import crowdsourced.mturk.PendingJob
import crowdsourced.mturk.Answer
import crowdsourced.mturk.AnswerCallback
import crowdsourced.mturk.Assignment
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

/**
 * Represents an AMT task and implements the AnswerCallback interface.
 * When calling exec(), the HIT is sent to AMT.
 * The onFinish method takes a function in argument that is executed when HIT is finished.
 * @authors Francois Farquet, Joachim Hugonot
 */
class AMTTask(val hit: HIT) extends AnswerCallback  {
  
  private var finished: Boolean = false
  private var started: Boolean = false
  private val assignments: ListBuffer[Assignment] = ListBuffer()
  private val WAIT_TIME_RESULTS: Int = 1000 // wait time between checks if results arrived
  
  AMTCommunicator.loadCredentials("./credentials.txt")
  
  /**
   * Sends the request to AMT and set itself as the callback object
   * This function is non-blocking
   */
  def exec(): PendingJob = {
    this.started = true
    AMTCommunicator.sendHIT(hit, this)
  }
  
  /**
   * Will block until we have the complete list of result
   */
  def waitResults(): List[Assignment] = {
    while(!isFinished()) Thread sleep WAIT_TIME_RESULTS
    getAssignments()
  }
  
  /**
   * Sends the request to AMT and set itself as the callback object
   * This function is blocking
   */
  def execBlocking(): List[Assignment] = {
    exec()
    waitResults()
  }
  
  /**
   * Returns the list of assignments already received
   */
  def getAssignments(): List[Assignment] = this.assignments.toList
  
  /**
   * Returns true if the HIT has finished
   */
  def isFinished(): Boolean = this.finished
  
  /**
   * Returns true if the HIT has been submitted to AMT
   */
  def hasStarted(): Boolean = this.started
  /**
   * Callback method executed automatically when AMT returns results.
   */
  override def newAssignmentsReceived(newAssignments: java.util.List[Assignment]): Unit = {
    println("New assignments received from AMT.")
    
    val assignList: List[Assignment] = newAssignments.asScala.toList
    this.assignments ++= assignList
    
    // print results in console
    assignments.foreach(ass => {
      println("Assignment result :")
      val answersMap: Map[String, Answer] = ass.getAnswers().asScala.toMap
      answersMap.foreach { case (key, value) => println(key+" => "+value) }
    })
    
    this.setFinished // TODO we assume, for now, that one result means end of task (later, AMT team will call jobFinished and we can delete this line)
  }
  
  private def setFinished() = this.finished = true
  
  /**
   * Callback method automatically executed when AMT has finished processing the HIT.
   */
  override def jobFinished(): Unit = {
    println("HIT finished successfully (callback called).")
    this.setFinished
  }
  
 /**
   * Callback method automatically executed when AMT returns an error.
   */
  override def errorOccured(): Unit = {
    println("Error occurred while handling answers from HIT.")
  }
}
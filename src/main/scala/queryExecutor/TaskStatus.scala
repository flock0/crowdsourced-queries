package queryExecutor

import scala.collection.mutable.ListBuffer
import play.api.libs.json._

/**
 * TaskStatus encapsulates statistics related to a specific query task (one or more hits).
 * @authors Francois Farquet, Joachim Hugonot
 */
class TaskStatus(val statusID: String, val operator: String) {
  
  private val NOT_STARTED = "Not started"
  private val PROCESSING = "Processing"
  private val FINISHED = "Finished"
  
  private var currentStatus: String = NOT_STARTED
  private var startTime: Long = -1
  private var endTime: Long = -1
  private val taskListBuffer: ListBuffer[AMTTask] = ListBuffer()
  
  /**
   * Stores an AMTTask in order to keep track of its activity and update status accordingly
   */
  def addTask(task: AMTTask): Unit = {
    this.synchronized { this.taskListBuffer += task }
    this.checkStatus // check all tasks in order to update status and other variables
  }
  
  /**
   * Generalization of addTask for multiple tasks at once
   */
  def addTasks(taskList: List[AMTTask]): Unit = {
    this.synchronized { this.taskListBuffer ++= taskList }
    this.checkStatus // check all tasks in order to update status and other variables
  }
  
  /**
   * Returns the status of the task in text format (ex: "Not started", "Processing", Processing"Finished")
   */
  def getCurrentStatus: String = {
    checkStatus
    this.synchronized { this.currentStatus }
  }
  
  /**
   * Returns the operator of the query task (ex: SELECT, WHERE, ORDER BY, ...) that was given in the constructor
   */
  def getOperator: String = this.operator
  
  /**
   * Returns the unique ID of the query task that was given in the constructor
   */
  def getStatusID: String = this.statusID
  
  /**
   * Returns the start time as a timestamp or -1 if it has not started yet
   */
  def getStartTime: Long = {
    checkStatus // be sure that information are up-to-date
    this.synchronized { this.startTime }
  }
  
  /**
   * Returns the end time as a timestamp or -1 if it has not ended yet
   */
  def getEndTime: Long = {
    checkStatus // be sure that information are up-to-date
    this.synchronized { this.endTime }
  }
  
  /**
   * Returns a nice string of the duration of the query task
   */
  def getDurationString: String = {
    if (getStartTime < 0) "Task hasn't started yet"
    else if (getEndTime < 0) "Task is still running"
    else {
      val duration_sec = this.synchronized { (this.endTime - this.startTime)/1000 }
      val days: Long = duration_sec / 86400
      val hours: Long = (duration_sec - days * 86400) / 3600
      val minutes: Long = (duration_sec - days * 86400 - hours * 3600) / 60
      val seconds: Long = duration_sec - days * 86400 - hours * 3600 - minutes * 60
      val s = new StringBuilder()
      if (days > 0) s ++= days+"d "
      if (hours > 0) s ++= hours+"h "
      if (minutes > 0) s ++= minutes +"m "
      s ++= seconds +"s "
      s.toString
    }
  }
  
  /**
   * returns the JSON object containing all information about the current task
   */
  def getJSON(): JsValue = JsObject(Seq(
      "task_id" -> JsString(this.statusID),
      "task_status" -> JsString(getCurrentStatus),
      "task_operator" -> JsString(this.operator),
      "number_of_hits" -> JsNumber(getNumberHits),
      "finished_hits" -> JsNumber(getNumberFinishedHits),
      "task_results_number" -> JsNumber(getTaskAssignmentNumber)
      ))
  
  /**
   * Returns true if the task is finished, i.e. all hits are done.
   */
  def areHitsDone: Boolean = getNumberHits == getNumberFinishedHits
  
  /**
   * Returns the list of all HITs related to this query task
   */
  def getTaskList = this.synchronized { this.taskListBuffer.toList }
  
  /**
   * Returns the number of HITs related to this query task
   */
  def getNumberHits = getTaskList.length
  
  /**
   * Returns the number of completed HITs
   */
  def getNumberFinishedHits = getTaskList.filter(_.isFinished()).length
  
  /**
   * Returns the number of assignments already recevied
   */
  def getTaskAssignmentNumber: Int = getTaskList.foldLeft(0)((ctr, t) => t.getAssignments.length + ctr)
  
  /**
   * Checks if at least one of the AMTTask has started (so that the HIT is visible to workers)
   */
  private def isATaskRunning: Boolean = getTaskList.filter(_.hasStarted).length > 0
  
  /**
   * Check if tasks are running or finished in order to update the status
   */
  private def checkStatus: Unit = {
    if (isATaskRunning) taskHasStarted
    if (getNumberHits > 0 && areHitsDone) taskCompleted
  }
  
  /**
   * Assumes task has started and updates status accordingly
   */
  private def taskHasStarted = {
    this.synchronized {
      this.currentStatus = PROCESSING
      if (this.startTime <= 0) this.startTime = System.currentTimeMillis
    }
  }
  
  /**
   * Assumes task is finished and updates endTime and status accordingly
   */
  private def taskCompleted = {
    this.synchronized {
      this.currentStatus = FINISHED
      if (this.endTime <= 0) this.endTime = System.currentTimeMillis
    }
  }
  
  /**
   * Returns nice formatted string description of the task and its content
   */
  override def toString: String = s"""\tTask $getStatusID
  \t\tCurrent Status : $getCurrentStatus
  \t\tOperator : $getOperator
  \t\tStart time : $getStartTime
  \t\tEnd time : $getEndTime
  \t\tDuration : $getDurationString
  \t\tNumber of hits : $getNumberHits
  \t\tNumber of finished hits : $getNumberFinishedHits
  \t\tNumber of results received : $getTaskAssignmentNumber
  """
}

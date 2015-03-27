package queryExecutor

import crowdsourced.mturk.AMTCommunicator
import crowdsourced.mturk.HIT

class AMTTask(val hit: HIT) {
  
  private val answersHandler = new HITAnswersHandler(this)
  var toExec : () => Unit = null
  
  private var finished: Boolean = false
  
  def exec(): Unit = AMTCommunicator.sendHIT(hit, answersHandler)
  def getAnswersHandler = this.answersHandler
  def finish(): Unit = {
    this.finished = true
    
    if (toExec != null) toExec()
  }
  def isFinished(): Boolean = this.finished
  def onFinish(f : () => Unit) = { toExec = f }
}
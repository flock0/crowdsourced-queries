package queryExecutor


class TaskStatus(id: Int) {

  
  private var currentStatus: String = null
  private var operator: String = null
  private var numberHits: String = null
  private var finishedNumberHits: String = null
  private var taskResultNumber: String = null
  private var statusID: Int = id
  
 def getCurrentStatus = this.currentStatus
 def getOperator = this.operator
 def getNumberHits = this.numberHits
 def getFinishedNumberHits = this finishedNumberHits
 def getTaskResultNumber = this.taskResultNumber
 def getStatusId = this.statusID
 

 def SetCurrentStatus(value: String): Unit = this.currentStatus = value
 def setOperator(value: String): Unit = this.operator = value
 def setNumberHits(value: String): Unit = this.numberHits = value
 def setFinishedNumberHits(value: String): Unit = this.finishedNumberHits = value
 def setTaskResultNumber(value: String): Unit = this.taskResultNumber = value
 
 
}

package queryExecutor


class TaskStatus(id: String) {

  
  private var currentStatus: String = "under process"
  private var operator: String = null
  private var numberHits: Int = -1
  private var finishedNumberHits: Int = 0
  private var taskResultNumber: Int = 0
  private var statusID: String = id
  
 def getCurrentStatus = this.currentStatus
 def getOperator = this.operator
 def getNumberHits = this.numberHits
 def getFinishedNumberHits = this finishedNumberHits
 def getTaskResultNumber = this.taskResultNumber
 def getStatusId = this.statusID
 

 def setCurrentStatus(value: String): Unit = this.currentStatus = value
 def setOperator(value: String): Unit = this.operator = value
 def setNumberHits(value: Int): Unit = this.numberHits = value
 def setFinishedNumberHits(value: Int): Unit = this.finishedNumberHits = value
 def setTaskResultNumber(value: Int): Unit = this.taskResultNumber = value
 
 
}

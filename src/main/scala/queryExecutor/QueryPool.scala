package queryExecutor

import crowdsourced.http.QueryInterface
import scala.collection.mutable.ListBuffer
import play.api.libs.json._
import scala.concurrent._
import ExecutionContext.Implicits.global

class QueryPool() extends QueryInterface {
  println("QueryPool started...")

  val executors = ListBuffer[QueryExecutor]()

  def queriesInfo(): String = {
    val json = getJSON.toString
    println("Returning queries status to interface.")
    println("JSON content : "+json)
    json
  }

  def newQuery(query: String): String = {
    println("Query received : "+query)

    val queryID = this.executors.length
    val queryExec = new QueryExecutor(queryID, query)
    val success = queryExec.parse(query)
    
    queryExec.parse(query) match {
        case Left(qt) => 
          executors += queryExec
          Future(queryExec.execute(qt))
          JsObject(Seq(
            "success" -> JsBoolean(true),
            "queryId" -> JsString(queryID.toString))).toString
        case Right(e) => 
          println("Parsing failed: " + e)
          JsObject(Seq(
            "success" -> JsBoolean(false),
            "message" -> JsString("Parsing of query failed:\n" + e))).toString
    }
  }
  
  /**
   * Returns the list of all queryExecutors tasks.
   */
  def getQueryExecutors: List[QueryExecutor] = executors.toList
  
  /**
   * Called by interface when clicked on abort button. It will prevent the query from creating new HITs.
   */
  def abortQuery(queryId: String): String = {
    val queriesToAbort = getQueryExecutors.filter(_.getQueryID == queryId.toInt)
    if (queriesToAbort.length > 0) {
     
      // aborting query
      queriesToAbort.foreach{ t =>
        t.abort()
        executors -= t
      }
      
      
      JsObject(Seq(
      "success" -> JsBoolean(true),
      "message" -> JsString("Query aborted."))).toString
    } else {
    JsObject(Seq(
      "success" -> JsBoolean(false),
      "message" -> JsString("Query already aborted !"))).toString
    }
  }
  
  /**
   * Returns the full JSON of the state of all queries and tasks.
   */
  def getJSON: JsValue = JsObject(Seq(
      "list_of_queries" -> JsArray(getQueryExecutors.map(_.getJSON).toSeq)
      ))
}

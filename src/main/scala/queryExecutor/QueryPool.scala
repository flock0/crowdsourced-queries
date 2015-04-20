package queryExecutor

import crowdsourced.http.QueryInterface
import scala.collection.mutable.ListBuffer
import play.api.libs.json._

class QueryPool() extends QueryInterface {
  
  val executors = ListBuffer[QueryExecutor]()
  
  def queriesInfo(): String = getJSON.toString

  def newQuery(query: String): String = {
    
    val queryID = this.executors.length
    val queryExec = new QueryExecutor(queryID)
    executors += queryExec
    val parsedQuery = queryExec.parse(query)
    val executedQuery = queryExec.execute(parsedQuery)
    
    queryID.toString // returning the id as a string
  }
  
  def getQueryExecutors: List[QueryExecutor] = executors.toList

  //TODO Not urgent, for this query, we should abort all running HIT.
  def abortQuery(queryId: String): Unit = {
    println("ABORT QUERY NOT IMPLEMENTED YET.")
  }
  
  def getJSON: JsValue = JsObject(Seq(
      "list of queries" -> JsArray(getQueryExecutors.map(_.getJSON).toSeq)
      ))
}

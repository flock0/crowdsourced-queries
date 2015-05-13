package queryExecutor

import crowdsourced.http.QueryInterface
import scala.collection.mutable.ListBuffer
import play.api.libs.json._

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
    executors += queryExec
    val success = queryExec.execute()
    if (success) {
      JsObject(Seq(
        "success" -> JsBoolean(true),
        "queryId" -> JsString(queryID.toString))).toString
    } else {
      JsObject(Seq(
        "success" -> JsBoolean(false),
        "message" -> JsString("Parsing of query failed."))).toString
    }
  }

  def getQueryExecutors: List[QueryExecutor] = executors.toList

  //TODO Not urgent, for this query, we should abort all running HITs.
  def abortQuery(queryId: String): String = {
    println("ABORT QUERY NOT IMPLEMENTED YET.")
    JsObject(Seq(
      "success" -> JsBoolean(false),
      "message" -> JsString("Abort operation not implemented"))).toString
  }

  def getJSON: JsValue = JsObject(Seq(
      "list_of_queries" -> JsArray(getQueryExecutors.map(_.getJSON).toSeq)
      ))
}

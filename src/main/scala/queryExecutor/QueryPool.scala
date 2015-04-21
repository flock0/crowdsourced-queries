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

  //TODO Not urgent, for this query, we should abort all running HITs.
  def abortQuery(queryId: String): String = {
    println("ABORT QUERY NOT IMPLEMENTED YET.")
    "{ status = \"error\", message: \"Not implemented\"}"
  }

  def getJSON: JsValue = JsObject(Seq(
      "list_of_queries" -> JsArray(getQueryExecutors.map(_.getJSON).toSeq)
      ))
}

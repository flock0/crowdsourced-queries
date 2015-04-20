package queryExecutor

import scala.collection.mutable.ListBuffer

class QueryPool() {
  
  val executorsList = ListBuffer[QueryExecutor]()
  
  def queriesInfo(): String = "" //TODO Return JSON

  def newQuery(query: String): String = {
    
    val queryExec = new QueryExecutor(1) //TODO Handle ID creation
    executorsList += queryExec
    val parsedQuery = queryExec.parse(query)
    val executedQuery = queryExec.execute(parsedQuery)
    
    "" //TODO Return results
  }

  def abortQuery(queryId: String): Unit = {}//TODO Not urgent, for this query, we should abort all running HIT.
  
}

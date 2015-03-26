package queryExecutor

object Main extends App {
  
  //val query = "SELECT (full name) FROM [Presidents of the USA] WHERE [political party is democrat] ORDER BY age of death"
  val query = "SELECT (movies) FROM [Movies with Angelina Jolie] JOIN SELECT (movies) FROM [Movies with Brad Pitt] ON movies"
  val queryExec = new QueryExecutor
  val parsedQuery = queryExec.parse(query)
  val executedQuery = queryExec.execute(parsedQuery)
}
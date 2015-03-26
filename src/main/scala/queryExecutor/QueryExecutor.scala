package queryExecutor
import parser.Parser

class QueryExecutor() {
  
  def parse(query: String) = {
    val parsedQuery = Parser.parseQuery(query)
    println(parsedQuery)
  }
}


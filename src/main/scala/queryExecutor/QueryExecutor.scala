package queryExecutor
import parser.Parser
import tree.Tree._
class QueryExecutor() {

	def parse(query: String) = {
		val parsedQuery = Parser.parseQuery(query)

				parsedQuery.get match {

				
        case Select(nl, fields) => println(nl+" , "+fields)
        
        //case Join(left,right,on) => println("Join found")
        //case Where(selectTree, where) => println("where")
        case _ => println("shit")
		}

	}
}


package queryExecutor
import parser.Parser
import tree.Tree._

class QueryExecutor() {

	def parse(query: String): Q = {
		return Parser.parseQuery(query).get
	}

  def execute(query: Q) = {

    def recursiveTraversal(node: Q): Unit = node match{

        case Select(nl, fields) => recursiveTraversal(nl)
        case Join(left,right,on) => recursiveTraversal(left); recursiveTraversal(right);
        case Where(selectTree, where) => recursiveTraversal(selectTree)
        case OrderBy(query, ascendingOrDescending, field) => recursiveTraversal(query)
        case NaturalLanguage(s) => println("Node found");
        case _ => println("Problem");

    }
      println("Starting execution of the query : \"" + query + "\"")
      recursiveTraversal(query)
  }

  def taskWhere(select: SelectTree, where: Condition) = ???
  def taskSelect(from: Q, elem: List[P]) = ???
  def taskJoin(left: Q, right: Q, on: String) = ???
  def taskOrderBy(q: Q3, order: O, by: String) = ???

}

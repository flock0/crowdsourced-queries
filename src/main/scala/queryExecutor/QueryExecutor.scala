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
        case Join(left,right,on) => ;taskJoin(left,right,on)//recursiveTraversal(left); recursiveTraversal(right);
        case Where(selectTree, where) => recursiveTraversal(selectTree)
        case OrderBy(query, ascendingOrDescending, field) => recursiveTraversal(query)
        case NaturalLanguage(s) => println("Node found");
        case _ => println("Problem");

    }
      println("Starting execution of the query : \"" + query + "\"")
      recursiveTraversal(query)
      //println(t)
  }

  def taskWhere(select: SelectTree, where: Condition) = {
    val a = select match {case Select(nl, fields) => taskSelect(nl,fields)}
    println("Task where")
    
  }
  def taskSelect(from: Q, elem: List[P]) = {
    val a = from match {case NaturalLanguage(s) => taskNaturalLanguage(s)}   
    println("Task select")
  }
  def taskJoin(left: Q, right: Q, on: String) = {
    val a = left match  {
      case Select(nl,fields)=> taskSelect(nl, fields)
      case Where(select,where) => taskWhere(select, where)
      case NaturalLanguage(s) => taskNaturalLanguage(s)
    }
    
    val b = left match  {
      case Select(nl,fields) => taskSelect(nl, fields)
      case Where(select,where) => taskWhere(select, where)
      case NaturalLanguage(s) => taskNaturalLanguage(s)
    }
    println("Task join")
  }
  def taskOrderBy(q: Q3, order: O, by: String) = {
    val a = q match  {
      case Select(nl,fields) => taskSelect(nl, fields)
      case Where(select,where) => taskWhere(select, where)
      case NaturalLanguage(s) => taskNaturalLanguage(s)
    }
     println("Task order by")
  }
  
  def taskNaturalLanguage(s: String) = {
    println("Task natural language")
  }
}

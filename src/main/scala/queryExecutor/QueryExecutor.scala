package queryExecutor
import parser.Parser
import tree.Tree._

class QueryExecutor() {

	def parse(query: String): Q = {
		return Parser.parseQuery(query).get
	}

  def execute(query: Q) = {

    def startingPoint(node: Q): Unit = node match{

        case Select(nl, fields) => taskSelect(nl,fields)
        case Join(left,right,on) => taskJoin(left,right,on)//recursiveTraversal(left); recursiveTraversal(right);
        case Where(selectTree, where) => taskWhere(selectTree, where)
        case OrderBy(query, ascendingOrDescending, field) => taskOrderBy(query,ascendingOrDescending,field)
        case _ => println("Problem");

    }
      println("Starting execution of the query : \"" + query + "\"")
      startingPoint(query)
      //println(t)
  }

  def taskWhere(select: SelectTree, where: Condition) = {
    val a = select match {case Select(nl, fields) => taskSelect(nl,fields)}
    println("Task where")
    
  }
  def taskSelect(from: Q, fields: List[P]) = {
    val a = from match {case NaturalLanguage(s) => taskNaturalLanguage(s,fields)}   
    println("Task select")
  }
  def taskJoin(left: Q, right: Q, on: String) = {
    val a = left match  {
      case Select(nl,fields)=> taskSelect(nl, fields)
      case Where(select,where) => taskWhere(select, where)
      //case NaturalLanguage(s) => taskNaturalLanguage(s)
    }
    
    val b = right match  {
      case Select(nl,fields) => taskSelect(nl, fields)
      case Where(select,where) => taskWhere(select, where)
      //case NaturalLanguage(s) => taskNaturalLanguage(s)
    }
    println("Task join")
    
  }
  def taskOrderBy(q: Q3, order: O, by: String) = {
    val a = q match  {
      case Select(nl,fields) => taskSelect(nl, fields)
      case Where(select,where) => taskWhere(select, where)
      
    }
     println("Task order by")
  }
  
  def taskNaturalLanguage(s: String, fields: List[P]) = {
    println("Task natural language")
    println("    Send HIT with information "+s+","+fields)
    println("    Wait response")
    println("    Notify")
    
  }
}

package parser
import tree.QueryTree._
import scala.util.parsing.combinator.RegexParsers
//SELECT (movies) FROM « Movies with Angelina Jolie » JOIN SELECT (movies) FROM « Movies with Brad Pitt » ON movies
//
//SELECT (full name, NUMERIC age of death) FROM « Presidents of the USA » WHERE « political party is democrat » ORDER BY age of death

/**
 * Parser parses the input queries and returns a QueryTree
 * @authors Vincent Zellweger, Xinyi Guo, Kristof Szabo
 */
object QueryParser extends RegexParsers with java.io.Serializable{
  
  def baseParser: Parser[RootNode] = (
    parseRootNode ^^ {case root => root}
  )

  def parseRootNode: Parser[RootNode] = (
    "(" ~ parseRootNode ~ ") JOIN (" ~ parseRootNode ~ ") ON " ~ parseE ^^ {case _ ~ left ~ _ ~ right ~ _ ~ on => Join(left, right, on)}
    | "(" ~ parseRootNode ~ ") IN (" ~ parseRootNode ~ ")" ^^ {case _ ~ left ~ _ ~ right ~ _ => In(left, right)}
    | "(" ~ parseRootNode ~ ") NOT IN (" ~ parseRootNode ~ ")" ^^ {case _ ~ left ~ _ ~ right ~ _ => NotIn(left, right)}
    | "(" ~ parseRootNode ~ ") INTERSECT (" ~ parseRootNode ~ ")" ^^ {case _ ~ left ~ _ ~ right ~ _ => Intersect(left, right)}
    | "(" ~ parseRootNode ~ ") UNION (" ~ parseRootNode ~ ")" ^^ {case _ ~ left ~ _ ~ right ~ _ => Union(left, right)}
    | parsePrio1Node ^^ {case q1 => q1}
  )

  def parsePrio1Node: Parser[Prio1Node] = (
    "("~parsePrio2Node ~ ") LIMIT" ~ parseNumber^^ {case _~q2 ~ _ ~ lim => Limit(q2, lim)}
    | parsePrio2Node ^^ {case q2 => q2}
  )

  def parsePrio2Node: Parser[Prio2Node] = (
    "("~parsePrio3Node~") ORDER BY"~parseOrder ^^ {case _~q~_~o => OrderBy(q, o)}
    | parsePrio3Node ^^ {case q3 => q3}
  )
  
  def parseOrder: Parser[List[Ordering]] = (
    parseBaseOrder ~ opt(","~parseOrder) ^^ {
      case p1 ~ Some(_~p2) => p1 :: p2
      case p ~ None => List(p)
    }
  )
  
  def parseBaseOrder: Parser[Ordering] = (
    str~"Desc" ^^ {case e~_ => OrdDesc(e)}
    | str~opt("Asc") ^^ {case e ~ _ => OrdAsc(e)}
  )

  def parsePrio3Node: Parser[Prio3Node] = (
    "("~parsePrio4Node ~ ") GROUP BY" ~ parseE ^^ {case _~q4~_~elem => Group(q4, elem)}
    | parsePrio4Node ^^{case q4 => q4}
  )

  def parsePrio4Node: Parser[Prio4Node] = (
    "(" ~parseSelectTree ~ ") WHERE" ~ parseCondition ^^ {case _~select~_~c => Where(select, c)}
    | parseSelectTree ^^{case q5 => q5}
  )
  
  def parseSelectTree: Parser[SelectTree] = (
    "SELECT (" ~ parseP ~ ") FROM " ~ parseNl ^^ {case _~p~_~e => Select(e, p)}
    | "SELECT (" ~ parseP ~ ") FROM (" ~ parseRootNode~")" ^^ {case _~p~_~e~_ => Select(e, p)}
    | parseNl ^^ {case nl5 => nl5}
  )
  
  def parseP: Parser[List[Operation]] = (
    parseComplexOperation ~ opt(","~parseP) ^^ {
      case p1 ~ Some(_~p2) => p1 :: p2
      case p ~ None => List(p)
    }
  )
  
  def parseComplexOperation: Parser[Operation] = (
    "SUM(" ~ parseBaseOperation ~ ")" ^^ {case _~p~_ => Sum(p)}
    | "DISTINCT(" ~ parseBaseOperation ~ ")" ^^ {case _~p~_ => Distinct(p)}
    | "COUNT(" ~ parseComplexOperation ~ ")" ^^ {case _~p~_ => Count(p)}
    | parseBaseOperation ^^ {case p => p}
  )

  def parseBaseOperation: Parser[BaseOperation] = (
    "NUMERIC" ~ parseE ^^ {case _~p => ElementNum(p)}
    | parseE ^^ {case p => ElementStr(p)}
  )

  def parseE: Parser[String] = (
    elem ^^ {case e => e}
  )

  def parseNl: Parser[NaturalLanguage] = (
    "["~nl~"]" ^^ {case _~nl~_ => NaturalLanguage(nl)}
  )

  def parseCondition: Parser[Condition] = (
    parseE ~ "<" ~ parseNumber ^^ {case e ~_~ i => LessThan(e, i)}
    | parseE ~ ">" ~ parseNumber ^^ {case e ~_~ i => GreaterThan(e, i)}
    | parseE ~ "<=" ~ parseNumber ^^ {case e ~_~ i => LessThanOrEqual(e, i)}
    | parseE ~ ">=" ~ parseNumber ^^ {case e ~_~ i => GreaterThanOrEqual(e, i)}
    | parseE ~ "=" ~ parseLeaf ^^ {case e ~_~ t => Equals(e, t)}
    | parseE ~ "IN" ~ parseGrouping ^^ {case e ~_~ g => ConditionIn(e, g)}
    | parseE ~ "NOT IN" ~ parseGrouping ^^ {case e ~_~ g => ConditionNotIn(e, g)}
    | "(" ~ parseCondition ~ ") AND (" ~ parseCondition ~ ")" ^^ {case _~left ~_~ right~_ => And(left, right)}
    | "(" ~ parseCondition ~ ") OR (" ~ parseCondition ~ ")" ^^ {case _~left ~_~ right~_  => Or(left, right)}
    | parseNl ^^ {case nl4 => nl4}
  )

  def parseLeaf: Parser[Leaf] = (
    parseNumber ^^ {case i => i}
   | parseBooleanNode ^^ {case b => b}
   | parseStringLiteral ^^ {case s => s}
   | "(" ~ parseRootNode ~ ")" ^^ {case _~q~_ => q}
  )
  
  def parseBooleanNode: Parser[BooleanNode] = {
    "True" ^^ {case _ => True()}
    "False" ^^ {case _ => False()}
  }
  
  def parseStringLiteral: Parser[StringLiteral] = (
    "\"" ~ str ~ "\"" ^^ {case _~str~_ => StrL(str)}
    | parseNl ^^ {case nl3 => nl3}
  )
  
  def parseNumber: Parser[Number] = (
    int ^^ {case i => IntL(i.toInt)}
    | parseNl ^^ {case nl2 => nl2}
  )

  def parseGrouping: Parser[Grouping] = (
    "("~parseBaseG~")" ^^ {case _~l~_ => CondGroup(l)}
    | parseNl ^^ {case nl1 => nl1}
    | parseRootNode ^^ {case q => q}
  )
  
  def parseBaseG: Parser[List[Leaf]] = (
    parseLeaf ~ opt(","~parseBaseG) ^^ {
      case p1 ~ Some(_~p2) => p1 :: p2
      case p ~ None => List(p)
    }
  )
  
  val elem: Parser[String] = "[A-Za-z0-9_ ]+".r
  val int: Parser[String] = "[0-9]+".r
  val nl: Parser[String] = "[a-zA-Z0-9- ]+".r
  val str: Parser[String] = "[A-Za-z0-9_]+".r
  
  def parseQuery(query: String): RootNode = {
		parse(baseParser, ""+query+"") match {
      case Success(root, _) => root
      case e => {
		println(e.toString)
		throw new RuntimeException(e.toString)
	}
    }
/**
		parse(parseRootNode, query);*/
 
}
    
}

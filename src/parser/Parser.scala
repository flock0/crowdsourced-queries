package parser
import tree.Tree._
import scala.util.parsing.combinator.RegexParsers
//SELECT (movies) FROM « Movies with Angelina Jolie » JOIN SELECT (movies) FROM « Movies with Brad Pitt » ON movies
//
//SELECT (full name, NUMERIC age of death) FROM « Presidents of the USA » WHERE « political party is democrat » ORDER BY age of death
object Parser extends RegexParsers with java.io.Serializable{
  
  def parseQ: Parser[Q] = (
    "(" ~ parseQ ~ ") JOIN (" ~ parseQ ~ ") ON " ~ parseE ^^ {case _ ~ left ~ _ ~ right ~ _ ~ on => Join(left, right, on)}
    | "(" ~ parseQ ~ ") IN (" ~ parseQ ~ ")" ^^ {case _ ~ left ~ _ ~ right ~ _ => In(left, right)}
    | "(" ~ parseQ ~ ") NOT IN (" ~ parseQ ~ ")" ^^ {case _ ~ left ~ _ ~ right ~ _ => NotIn(left, right)}
    | "(" ~ parseQ ~ ") INTERSECT (" ~ parseQ ~ ")" ^^ {case _ ~ left ~ _ ~ right ~ _ => Intersect(left, right)}
    | "(" ~ parseQ ~ ") UNION (" ~ parseQ ~ ")" ^^ {case _ ~ left ~ _ ~ right ~ _ => Union(left, right)}
    | parseQ1 ^^ {case q1 => q1}
    | failure("Error on parsing")
  )

  def parseQ1: Parser[Q1] = (
    "("~parseQ2 ~ ") LIMIT" ~ parseI^^ {case _~q2 ~ _ ~ lim => Limit(q2, lim)}
    | parseQ2 ^^ {case q2 => q2}
    | failure("Error on parsing")
  )

  def parseQ2: Parser[Q2] = (
    "("~parseQ3~") ORDER BY"~parseOrder ^^ {case _~q~_~o => OrderBy(q, o)}
    | parseQ3 ^^ {case q3 => q3}
    | failure("Error on parsing")
  )
  
  def parseOrder: Parser[List[O]] = (
    parseBaseOrder ~ opt(","~parseOrder) ^^ {
      case p1 ~ Some(_~p2) => p1 :: p2
      case p ~ None => List(p)
    }
    | failure("Error on parsing")
  )
  
  def parseBaseOrder: Parser[O] = (
    str~"Desc" ^^ {case e~_ => OrdDesc(e)}
    | str~opt("Asc") ^^ {case e ~ _ => OrdAsc(e)}
    | failure("Error on parsing")
  )

  def parseQ3: Parser[Q3] = (
    "("~parseQ4 ~ ") GROUP BY" ~ parseE ^^ {case _~q4~_~elem => Group(q4, elem)}
    | parseQ4 ^^{case q4 => q4}
    | failure("Error on parsing")
  )

  def parseQ4: Parser[Q4] = (
    "(" ~parseSelectTree ~ ") WHERE" ~ parseC ^^ {case _~select~_~c => Where(select, c)}
    | parseSelectTree ^^{case q5 => q5}
    | failure("Error on parsing")
  )
  
  def parseSelectTree: Parser[SelectTree] = (
    "SELECT (" ~ parseP ~ ") FROM " ~ parseNl ^^ {case _~p~_~e => Select(e, p)}
    | "SELECT (" ~ parseP ~ ") FROM (" ~ parseQ~")" ^^ {case _~p~_~e~_ => Select(e, p)}
    | parseNl ^^ {case nl5 => nl5}
    | failure("Error on parsing")
  )
  
  def parseP: Parser[List[P]] = (
    parseComplexP ~ opt(","~parseP) ^^ {
      case p1 ~ Some(_~p2) => p1 :: p2
      case p ~ None => List(p)
    }
    | failure("Error on parsing")
  )
  
  def parseComplexP: Parser[P] = (
    "SUM(" ~ parseBaseP ~ ")" ^^ {case _~p~_ => Sum(p)}
    | "DISTINCT(" ~ parseBaseP ~ ")" ^^ {case _~p~_ => Distinct(p)}
    | "COUNT(" ~ parseComplexP ~ ")" ^^ {case _~p~_ => Count(p)}
    | parseBaseP ^^ {case p => p}
    | failure("Error on parsing")
  )

  def parseBaseP: Parser[BaseP] = (
    "NUMERIC" ~ parseE ^^ {case _~p => ElementNum(p)}
    | parseE ^^ {case p => ElementStr(p)}
    | failure("Error on parsing")
  )

  def parseE: Parser[String] = (
    elem ^^ {case e => e}
    | failure("Error on parsing")
  )

  def parseNl: Parser[NaturalLanguage] = (
    "["~nl~"]" ^^ {case _~nl~_ => NaturalLanguage(nl)}
    | failure("Error on parsing")
  )

  def parseC: Parser[Condition] = (
    parseE ~ "<" ~ parseI ^^ {case e ~_~ i => LessThan(e, i)}
    | parseE ~ ">" ~ parseI ^^ {case e ~_~ i => GreaterThan(e, i)}
    | parseE ~ "<=" ~ parseI ^^ {case e ~_~ i => LessThanOrEqual(e, i)}
    | parseE ~ ">=" ~ parseI ^^ {case e ~_~ i => GreaterThanOrEqual(e, i)}
    | parseE ~ "=" ~ parseT ^^ {case e ~_~ t => Equals(e, t)}
    | parseE ~ "IN" ~ parseG ^^ {case e ~_~ g => ConditionIn(e, g)}
    | parseE ~ "NOT IN" ~ parseG ^^ {case e ~_~ g => ConditionNotIn(e, g)}
    | "(" ~ parseC ~ ") AND (" ~ parseC ~ ")" ^^ {case _~left ~_~ right~_ => And(left, right)}
    | "(" ~ parseC ~ ") OR (" ~ parseC ~ ")" ^^ {case _~left ~_~ right~_  => Or(left, right)}
    | parseNl ^^ {case nl4 => nl4}
    | failure("Error on parsing")
  )

  def parseT: Parser[T] = (
     parseI ^^ {case i => i}
    | parseB ^^ {case b => b}
    | parseS ^^ {case s => s}
    | parseQ ^^ {case q => q}
    | failure("Error on parsing")
  )
  
  def parseB: Parser[B] = {
    "True" ^^ {case _ => True()}
    "False" ^^ {case _ => False()}
  }
  
  def parseS: Parser[S] = (
    "\"" ~ str ~ "\"" ^^ {case _~str~_ => StrL(str)}
    | parseNl ^^ {case nl3 => nl3}
    | failure("Error on parsing")
  )
  
  def parseI: Parser[I] = (
    int ^^ {case i => IntL(i.toInt)}
    | parseNl ^^ {case nl2 => nl2}
    | failure("Error on parsing")
  )

  def parseG: Parser[G] = (
    "("~parseBaseG~")" ^^ {case _~l~_ => CondGroup(l)}
    | parseNl ^^ {case nl1 => nl1}
    | parseQ ^^ {case q => q}
    | failure("Error on parsing")
  )
  
  def parseBaseG: Parser[List[T]] = (
    parseT ~ opt(","~parseBaseG) ^^ {
      case p1 ~ Some(_~p2) => p1 :: p2
      case p ~ None => List(p)
    }
    | failure("Error on parsing")
  )
  
  val elem: Parser[String] = "[A-Za-z0-9_ ]+".r
  val int: Parser[String] = "[0-9]+".r
  val nl: Parser[String] = "[a-zA-Z0-9 ]+".r
  val str: Parser[String] = "[A-Za-z0-9_]+".r
  
  def parseQuery(query: String): ParseResult[Q] = parse(parseQ, query) 
    
}
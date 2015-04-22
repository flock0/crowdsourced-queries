package parser
import tree.Tree._
import scala.util.parsing.combinator.RegexParsers

object Parser extends RegexParsers with java.io.Serializable{
  
	def parseQ: Parser[Q] = (
	  parseQ1 ~ "JOIN" ~ parseQ1 ~ "ON" ~ parseE ^^ {case left ~ _ ~ right ~ _ ~ on => Join(left, right, on)}
	  | parseQ1 ^^ {case q1 => q1}
    | failure("Error")
	)
	
	def parseQ1: Parser[Q1] = (
	  parseQ2 ^^ {case q2 => q2}
    | failure("Error")
	)
	
	def parseQ2: Parser[Q2] = (
	  parseQ3~"ORDER BY"~parseE~"DESC" ^^ {case q~_~e~o => OrderBy(q, DESC(), e)}
	  | parseQ3~"ORDER BY"~parseE~opt("ASC") ^^ {case q~_~e~_ => OrderBy(q, ASC(), e)}
	  | parseQ3 ^^ {case q3 => q3}
    | failure("Error")
	)
	
	def parseQ3: Parser[Q3] = (
	  parseSelectTree ~ "WHERE" ~ parseC ^^ {case select~_~c => Where(select, c)}
	  | parseSelectTree ^^{case q4 => q4}
    | failure("Error")
	)
	
	def parseSelectTree: Parser[SelectTree] = (
	  "SELECT (" ~ parseP ~ ") FROM" ~ parseNl ^^ {case _~p~_~e => Select(e, p)}
    | failure("Error")
	  //| parseNL ^^ (case nl => nl)
	)
	
	def parseP: Parser[List[P]] = (
	  parseBaseP ~ opt(","~parseP) ^^ {
	    case p1 ~ Some(_~p2) => p1 :: p2
	    case p ~ None => List(p)
	  }
	)
	
	def parseBaseP: Parser[P] = (
	  "NUMERIC" ~ parseE ^^ {case _~p => ElementNum(p)}
	  | parseE ^^ {case p => ElementStr(p)}
	)
	
	def parseE: Parser[String] = (
	  elem ^^ {case e => e}
	)
	
	def parseNl: Parser[NaturalLanguage] = (
	  "["~nl~"]" ^^ {case _~nl~_ => NaturalLanguage(nl)}
	)
	
	def parseC: Parser[Condition] = (
	  parseE ~ "<" ~ parseI ^^ {case e ~_~ i => LessThan(e, i)}
	  | parseE ~ ">" ~ parseI ^^ {case e ~_~ i => GreaterThan(e, i)}
	  | parseE ~ "<=" ~ parseI ^^ {case e ~_~ i => LessThanOrEqual(e, i)}
	  | parseE ~ ">=" ~ parseI ^^ {case e ~_~ i => GreaterThanOrEqual(e, i)}
	  | parseNl ^^ {case nl => nl}
	)
	
	def parseI: Parser[I] = (
	  int ^^ {case i => IntL(i.toInt)}
	  | parseNl ^^ {case nl => nl}
	)
	
	val elem: Parser[String] = "[A-Za-z0-9_ ]+".r
	val int: Parser[String] = "[0-9]+".r
	val nl: Parser[String] = "[a-zA-Z0-9 ]+".r
	
//	def main(args: Array[String]) {
//	  val testStr = "SELECT (full name, NUMERIC age of death) FROM [Presidents of the USA] WHERE [political party is democrat] ORDER BY age of death";
//	  
//	  println(parse(parseQ, testStr).getOrElse(ErrorString()))
//	}
}
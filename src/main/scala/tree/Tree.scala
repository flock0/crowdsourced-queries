package tree

object Tree{
  
  trait Q extends G with java.io.Serializable{
    def countNodes: Int
  }
  
  case class In(left: Q, right: Q) extends Q{
    def countNodes = 1 + left.countNodes + right.countNodes
  }
  case class NotIn(left: Q, right: Q) extends Q{
    def countNodes = 1 + left.countNodes + right.countNodes
  }
  case class Join(left: Q, right: Q, on: String) extends Q {
    override def toString() = left + " JOIN " + right + " ON " + on
    def countNodes = 1 + left.countNodes + right.countNodes
  }
  case class GroupBy(q: Q, by: String) extends Q {
    def countNodes = 1 + q.countNodes
  }
  
  case class ErrorString() extends Q {
    override def toString() = "Error on parsing"
    def countNodes = 0
  }
  
  trait Q1 extends Q
  
  case class Limit(q: Q2, limit: Int) extends Q1 {
    def countNodes = 1 + q.countNodes
  }
  
  trait Q2 extends Q1
  
  case class OrderBy(q: Q3, order: O, by: String) extends Q2 {
    override def toString() = q +" ORDER BY " + by+ " " + order
    def countNodes = 1 + q.countNodes
  }
  
  trait Q3 extends Q2
  
  case class Where(select: SelectTree, where: Condition) extends Q3 {
    override def toString() = select + " WHERE " + where
    def countNodes = 1 + select.countNodes
  }
  
  trait SelectTree extends Q3
  
  case class Select(from: Q, elem: List[P]) extends SelectTree {
    def computeList(e: List[P]): String = e match {
      case e :: Nil => e + ""
      case h :: t => h + ", " + computeList(t)
    }
    def countNodes = 1 + from.countNodes
    override def toString() = "SELECT (" + computeList(elem) + ") FROM " + from
  }
  
  
  trait Condition
  
  case class CIn(elem: String, in: G) extends Condition
  case class Equals(elem: String, t: T) extends Condition
  case class LessThan(elem: String, t: I) extends Condition {
    override def toString() = elem + " < " + t
  }
  case class GreaterThan(elem: String, t: I) extends Condition {
    override def toString() = elem + " > " + t
  }
  case class LessThanOrEqual(elem: String, t: I) extends Condition {
    override def toString() = elem + " <= " + t
  }
  case class GreaterThanOrEqual(elem: String, t: I) extends Condition {
    override def toString() = elem + " >= " + t
  }
  case class And(left: Condition, right: Condition) extends Condition
  case class Or(left: Condition, right: Condition) extends Condition
  
  trait P
//  case class Sum(nl: NaturalLanguage) extends P
  case class Distinct(s: String) extends P
  case class ElementStr(s: String) extends P {
    override def toString() = s
  }
  case class ElementNum(s: String) extends P {
    override def toString() = "NUMERIC " + s
  }
  
  trait O
  case class ASC() extends O {
    override def toString() = "ASC"
  }
  case class DESC() extends O {
    override def toString() = "DESC"
  }
  
  trait T
  case class Bool(b: Boolean) extends T
  case class Str(s: String) extends T
  
  trait G
  case class Strings(s: String*) extends G
  case class Ints(i: Int*) extends G
  
  
  case class NaturalLanguage(s: String) extends Condition with SelectTree with G with I {
    override def toString() = "[" + s + "]"
  }
  
  trait I extends T
  case class IntL(i: Int) extends I {
    override def toString() = i + ""
  }
}
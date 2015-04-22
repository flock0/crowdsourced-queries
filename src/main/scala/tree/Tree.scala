package tree

object Tree{
  
  trait Q extends G with T with java.io.Serializable
  
  case class In(left: Q, right: Q) extends Q {
    override def toString() = "(" + left + ") IN (" + right + ")"
  }
  case class NotIn(left: Q, right: Q) extends Q {
    override def toString() = "(" + left + ") IN (" + right + ")"
  }
  case class Join(left: Q, right: Q, on: String) extends Q {
    override def toString() = "(" + left + ") JOIN (" + right + ") ON " + on
  }
  
  case class Intersect(left: Q, right: Q) extends Q {
    override def toString() = "(" + left + ") INTERSECT (" + right + ")"
  }
  case class Union(left: Q, right: Q) extends Q {
    override def toString() = "(" + left + ") UNION (" + right + ")"
  }
  
  case class ErrorString() extends Q {
    override def toString() = "Error on parsing"
  }
  
  trait Q1 extends Q
  
  case class Limit(q: Q2, limit: I) extends Q1 {
    override def toString() = q + " LIMIT " + limit
  }
  
  trait Q2 extends Q1
  
  case class OrderBy(q: Q3, order: List[O]) extends Q2 {
  def recOrder(ord: List[O]): String = ord match {
    case Nil => ""
    case h :: Nil => h.toString
    case h :: tail => h.toString + ", " + recOrder(tail)
  }
  
    override def toString() = q +" ORDER BY " + recOrder(order)
  }
  
  trait Q3 extends Q2
  
  case class Group(q: Q4, by: String) extends Q3 {
    override def toString() = q + " GROUP BY " + by
  }
  
  trait Q4 extends Q3
  
  case class Where(select: SelectTree, where: Condition) extends Q4 {
    override def toString() = select + " WHERE " + where
  }
  
  trait SelectTree extends Q4
  
  case class Select(from: Q, elem: List[P]) extends SelectTree {
    def computeList(e: List[P]): String = e match {
      case Nil => ""
      case e :: Nil => e + ""
      case h :: t => h + ", " + computeList(t)
    }
    
    override def toString() = "SELECT (" + computeList(elem) + ") FROM " + from
  }
  
  
  trait Condition
  
  case class ConditionIn(elem: String, group: G) extends Condition {
    override def toString() = elem + " IN " + group
  }
  case class ConditionNotIn(elem: String, group: G) extends Condition {
    override def toString() = elem + " NOT IN " + group
  }
  
  case class Equals(elem: String, t: T) extends Condition {
    override def toString() = elem + " = " + t
  }
  case class LessThan(elem: String, i: I) extends Condition {
    override def toString() = elem + " < " + i
  }
  case class GreaterThan(elem: String, i: I) extends Condition {
    override def toString() = elem + " > " + i
  }
  case class LessThanOrEqual(elem: String, i: I) extends Condition {
    override def toString() = elem + " <= " + i
  }
  case class GreaterThanOrEqual(elem: String, i: I) extends Condition {
    override def toString() = elem + " >= " + i
  }
  case class And(left: Condition, right: Condition) extends Condition {
    override def toString() = "(" + left + ") AND (" + right + ")"
  }
  case class Or(left: Condition, right: Condition) extends Condition {
    override def toString() = "(" + left + ") OR (" + right + ")"
  }
  
  
  
  trait P
//  case class Sum(nl: NaturalLanguage) extends P
  case class Sum(elem: P) extends P {
    override def toString() = "SUM(" + elem + ")"
  }
  case class Distinct(elem: P) extends P {
    override def toString() = "DISTINCT(" + elem + ")"
  }
  case class Count(elem: P) extends P {
    override def toString() = "COUNT(" + elem + ")"
  }
  
  trait BaseP extends P
  case class ElementNum(s: String) extends BaseP {
    override def toString() = "NUMERIC " + s
  }
  case class ElementStr(s: String) extends BaseP {
    override def toString() = s
  }
  
  
  trait O
  case class OrdAsc(elem: String) extends O {
    override def toString() = elem + " Asc"
  }
  case class OrdDesc(elem: String) extends O {
    override def toString() = elem + " Desc"
  }
  
  trait T
  
  trait G
  case class CondGroup(g: List[T]) extends G {
  def group(e: List[T]): String = e match {
    case Nil => ""
    case h :: Nil => h.toString
    case h :: tail => h + ", " + group(tail)
  }
  
  override def toString() = "(" + group(g) + ")"
  }
  
  
  case class NaturalLanguage(s: String) extends Condition with SelectTree with G with S with I {
    override def toString() = "[" + s + "]"
  }
  
  trait I extends T
  case class IntL(i: Int) extends I {
    override def toString() = i + ""
  }
  
  trait B extends T
  case class True() extends B {
  override def toString() = "TRUE";
  }
  case class False() extends B {
  override def toString() = "FALSE";
  }
  
  trait S extends T
  case class StrL(s: String) extends S {
  override def toString() = "\"" + s + "\"";
  }
}
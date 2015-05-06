package tree
/**
 * Used to represent the query after the parsing
 * 
 */
object QueryTree{
  /**
   * Subclasses of this trait are potential root nodes for a tree. Classes extending directly this trait have
   * higher priority while parsing.
   * @authors Vincent Zellweger, Xinyi Guo, Kristof Szabo
   */
  trait RootNode extends Grouping with Leaf with java.io.Serializable
  
  case class In(left: RootNode, right: RootNode) extends RootNode {
    override def toString() = "(" + left + ") IN (" + right + ")"
  }
  case class NotIn(left: RootNode, right: RootNode) extends RootNode {
    override def toString() = "(" + left + ") IN (" + right + ")"
  }
  case class Join(left: RootNode, right: RootNode, on: String) extends RootNode {
    override def toString() = "(" + left + ") JOIN (" + right + ") ON " + on
  }
  
  case class Intersect(left: RootNode, right: RootNode) extends RootNode {
    override def toString() = "(" + left + ") INTERSECT (" + right + ")"
  }
  case class Union(left: RootNode, right: RootNode) extends RootNode {
    override def toString() = "(" + left + ") UNION (" + right + ")"
  }
  
  case class ErrorString() extends RootNode {
    override def toString() = "Error on parsing"
  }
  /**
   * Prio1Node, Prio2Node, ... define priorities of nodes for parsing
   */
  trait Prio1Node extends RootNode
  
  case class Limit(q: Prio2Node, limit: Number) extends Prio1Node {
    override def toString() = q + " LIMIT " + limit
  }
  
  trait Prio2Node extends Prio1Node
  
  case class OrderBy(q: Prio3Node, order: List[Ordering]) extends Prio2Node {
  def recOrder(ord: List[Ordering]): String = ord match {
    case Nil => ""
    case h :: Nil => h.toString
    case h :: tail => h.toString + ", " + recOrder(tail)
  }
  
    override def toString() = q +" ORDER BY " + recOrder(order)
  }
  
  trait Prio3Node extends Prio2Node
  
  case class Group(q: Prio4Node, by: String) extends Prio3Node {
    override def toString() = q + " GROUP BY " + by
  }
  
  trait Prio4Node extends Prio3Node
  
  case class Where(select: SelectTree, where: Condition) extends Prio4Node {
    override def toString() = select + " WHERE " + where
  }
  
  trait SelectTree extends Prio4Node
  
  case class Select(from: RootNode, elem: List[Operation]) extends SelectTree {
    def computeList(e: List[Operation]): String = e match {
      case Nil => ""
      case e :: Nil => e + ""
      case h :: t => h + ", " + computeList(t)
    }
    
    override def toString() = "SELECT (" + computeList(elem) + ") FROM " + from
  }
  
  trait Condition
  
  case class ConditionIn(elem: String, group: Grouping) extends Condition {
    override def toString() = elem + " IN " + group
  }
  case class ConditionNotIn(elem: String, group: Grouping) extends Condition {
    override def toString() = elem + " NOT IN " + group
  }
  
  case class Equals(elem: String, t: Leaf) extends Condition {
    override def toString() = elem + " = " + t
  }
  case class LessThan(elem: String, i: Number) extends Condition {
    override def toString() = elem + " < " + i
  }
  case class GreaterThan(elem: String, i: Number) extends Condition {
    override def toString() = elem + " > " + i
  }
  case class LessThanOrEqual(elem: String, i: Number) extends Condition {
    override def toString() = elem + " <= " + i
  }
  case class GreaterThanOrEqual(elem: String, i: Number) extends Condition {
    override def toString() = elem + " >= " + i
  }
  case class And(left: Condition, right: Condition) extends Condition {
    override def toString() = "(" + left + ") AND (" + right + ")"
  }
  case class Or(left: Condition, right: Condition) extends Condition {
    override def toString() = "(" + left + ") OR (" + right + ")"
  }
  
  trait Operation
//  case class Sum(nl: NaturalLanguage) extends Operation
  case class Sum(elem: Operation) extends Operation {
    override def toString() = "SUM(" + elem + ")"
  }
  case class Distinct(elem: Operation) extends Operation {
    override def toString() = "DISTINCT(" + elem + ")"
  }
  case class Count(elem: Operation) extends Operation {
    override def toString() = "COUNT(" + elem + ")"
  }
  
  trait BaseOperation extends Operation
  case class ElementNum(s: String) extends BaseOperation {
    override def toString() = "NUMERIC " + s
  }
  case class ElementStr(s: String) extends BaseOperation {
    override def toString() = s
  }
    
  trait Ordering
  case class OrdAsc(elem: String) extends Ordering {
    override def toString() = elem + " Asc"
  }
  case class OrdDesc(elem: String) extends Ordering {
    override def toString() = elem + " Desc"
  }
  /**
   * Trait representing generally a leaf node (not allways true)
   */
  trait Leaf
  
  trait Grouping
  case class CondGroup(g: List[Leaf]) extends Grouping {
  def group(e: List[Leaf]): String = e match {
    case Nil => ""
    case h :: Nil => h.toString
    case h :: tail => h + ", " + group(tail)
  }
  
  override def toString() = "(" + group(g) + ")"
  }
  
  case class NaturalLanguage(s: String) extends Condition with SelectTree with Grouping with StringLiteral with Number {
    override def toString() = "[" + s + "]"
  }
  
  trait Number extends Leaf
  case class IntL(i: Int) extends Number {
    override def toString() = i + ""
  }
  
  trait BooleanNode extends Leaf
  case class True() extends BooleanNode {
	override def toString() = "TRUE";
  }
  
  case class False() extends BooleanNode {
	override def toString() = "FALSE";
  }
  
  trait StringLiteral extends Leaf
  case class StrL(s: String) extends StringLiteral {
	override def toString() = "\"" + s + "\"";
  }
}
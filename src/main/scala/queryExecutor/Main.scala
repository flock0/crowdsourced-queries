package queryExecutor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Arrays
import scala.collection.JavaConverters._
import parser.QueryParser._
import tree.QueryTree._

object Main extends App {
  
  // chose the query among the list below
  val choice = 7
  
  val query = choice match {
    case 1 => "(SELECT (full_name, birth_date, birth_location, number_children) FROM [Presidents of USA]) ORDER BY name ASC"
    case 2 => "(SELECT (first_name) FROM [president of United States]) GROUP BY political party"
    case 3 => "(SELECT (movies) FROM [Movies with Angelina Jolie]) JOIN (SELECT (movies) FROM [Movies with Brad Pitt]) ON movies"
    case 4 => "(SELECT (full_name, birth_date, birth_location, number_children) FROM [Presidents of USA]) WHERE [political party is democrat]"
    case 5 => "(SELECT (full_name, birth_date, birth_location, number_children) FROM [Presidents of USA]) LIMIT 2"
    case 6 => "((SELECT (full_name, birth_date, birth_location, number_children) FROM [Presidents of USA]) WHERE [political party is democrat]) LIMIT 3"
    case 7 => "(SELECT (sofa_offer_url, sofa_price, approximate_driving_distance_in_miles_from_628_Waverley_street) FROM [Offers for second-hand sofas in the Bay area]) LIMIT 50 WHERE [the sofa is red]"
    case _ => ""
  }
  
  // creates the query
  val queryExec = new QueryExecutor(0, query)
  
  // parses and executes the query by crowdsourcing
  val executedQuery = queryExec.execute(null)
  
  // blocks until all results arrive and print results
  queryExec.waitAndPrintResults()
  
}

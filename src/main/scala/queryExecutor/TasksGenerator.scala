package queryExecutor

import java.util.Calendar
import scala.util.Random
import scala.collection.JavaConverters._
import scala.concurrent._
import ExecutionContext.Implicits.global
import crowdsourced.mturk.question._
import crowdsourced.mturk.task._
import tree.QueryTree._
import java.text.SimpleDateFormat
import scala.concurrent.duration.Duration

object TasksGenerator {
  
  /**
   * Constant definitions for HIT creations
   */
  val REWARD_PER_HIT = 0.01
  //val HIT_LIFETIME = 60 * 60 * 24 * 4 // 4 Days
  val HIT_LIFETIME = 60 * 60 // 1 Hour
  val MAJORITY_VOTE = 1 //TODO Implement majority votes for WHERE and JOIN tasks.
  val REWARD_SORT = 0.05 //Sort is longer so we should pay more
  val ASSIGNMENT_LIFETIME = 600
  
/********************************** FUNCTIONS TO GENERATE AMTTASKS ********************************/
   
  /**
   * AMTTask generator for FROM statement
   */
  def naturalLanguageTasksGenerator(s: String, fields: List[Operation]): List[AMTTask] =  {
    
    val taskID = generateUniqueID()
    val questionTitle = "Find website on the internet"
    val questionDescription = "Search for website with desired information" 
    val questionText = "What is the most relevant website to find [" + s + "] ?\nNote that we are interested in : " + fields.mkString(", ")
    val keywords = List("search", "find", "URL", "website", "list", "easy", "google", "single")
    val numAssignments = 1
    val question: Question = new URLQuestion(taskID, questionTitle, questionText)
    val hit = new HIT(questionTitle, questionDescription, List(question).asJava, HIT_LIFETIME, numAssignments, REWARD_PER_HIT toFloat, HIT_LIFETIME, keywords.asJava)
    
    List(new AMTTask(hit))
  }
  
  /**
   * AMTTask generator for FROM statement where we retrieve directly the primary key for a more structured approach.
   */
  def naturalLanguagePrimaryKeyTasksGenerator(s: String, fields: List[Operation]): List[AMTTask] =  {
     
    val taskID = generateUniqueID()
    val questionTitle = "Create a list"
    val questionDescription = "Create a list from data already provided." 
    val questionText = "For each element of the following set  [ " + s + " ], please retrieve its [ " + fields.head + " ].\nPlease provide only one " + fields.head + " per line."
    val keywords = List("copy", "list", "simple", "retrieve", "easy")
    val numAssignments = 1
    val question: Question = new StringQuestion(taskID, questionTitle, questionText, "", 0)
    val hit = new HIT(questionTitle, questionDescription, List(question).asJava, HIT_LIFETIME, numAssignments, REWARD_PER_HIT toFloat, HIT_LIFETIME, keywords.asJava)
    
    List(new AMTTask(hit))
  }
  
  /**
   * AMTTask generator for SELECT statement
   */
  def selectTasksGenerator(url: String, nl: String, fields: List[Operation], elementPerWorker: Int, limit: Int): List[AMTTask] = {

    // tuples of (start, end) for each worker
    val tuples = for (i <- List.range(1, limit + 1, elementPerWorker)) yield (i, Math.min(i + elementPerWorker - 1, limit))

    val tasks = tuples.map { tuple =>
      val (start: Int, end: Int) = tuple
      val fieldsString = fields.mkString(", ")
      val taskID = generateUniqueID()
      val questionTitle = "Find information on a website"
      val questionDescription = "Find information that we want from a website" 
      val questionText = s"""On this website, retrieve the following information ($fieldsString) about $nl
                              Select only items in the range $start to $end (both included)
                              URL : $url
                              Please provide one element per line."""
      val question: Question = new StringQuestion(taskID, questionTitle, questionText, "", 0)
      val questionList = List(question)
      val numWorkers = 1
      val keywords = List("extract", "website", "url", "data", "easy", "fast", "find", "retrieve", "information")
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, HIT_LIFETIME, numWorkers, REWARD_PER_HIT toFloat, HIT_LIFETIME, keywords.asJava)
      new AMTTask(hit)
    }

    tasks
  }
  
  /**
   * AMTTask generator for SELECT statement where we ask the user to inform "non-primary" keys.
   */
  def selectPrimaryKeyTasksGenerator(primaryKeys: List[String], nl: String, fields: List[Operation], elementPerWorker: Int, limit: Int): List[AMTTask] = {
    val string = fields.tail.mkString("_TO_GIVE, ")
    val tuples = for (i <- List.range(1, limit + 1, elementPerWorker)) yield (i, Math.min(i + elementPerWorker - 1, limit))

    val tasks = tuples.map { tuple =>
      val (start: Int, end: Int) = tuple
      val fieldsString = fields.mkString(", ")
      val taskID = generateUniqueID()
      val questionTitle = "Information finding"
      val questionDescription = "Question description" 
      val questionText = s"""For the following elements (there is exactly one element per line), please retrieve necessary information.
                              For instance the following element : (Barack Obama, birth_date__TO_GIVE, birth_location_TO_GIVE, current_age_TO_GIVE, wife_name_TO_GIVE)
                              Should be changed in (Barack Obama, August 4 1961, Honolulu Hawaii, 53 years, Michelle Obama)."""
      val question: Question = new StringQuestion(taskID, questionTitle, questionText, "("+primaryKeys.slice(start-1,end).mkString(", "+string+") \n(")+", "+string+")", 0)
      val questionList = List(question)
      val numWorkers = 1
      val keywords = List("information finding", "fast", "easy")
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, HIT_LIFETIME, numWorkers, REWARD_PER_HIT toFloat, HIT_LIFETIME, keywords.asJava)
      new AMTTask(hit)
    }
    
    tasks
  }

  /**
   * AMTTask generator for WHERE statement
   */
  def whereTasksGenerator(answers: List[String], where: Condition): List[AMTTask] = {
   
    val tasks = answers.map(ans => {
      val taskID = generateUniqueID()
      val questionTitle = "Answer a simple Yes/No question"
      val questionDescription = "Answer a Yes/No question using common sense or an internet search" 
      val questionText = "\"" + ans + "\": " + where + ". Is this true?"
      val optionYes = new MultipleChoiceOption(ans + ",yes", "yes")
      val optionNo = new MultipleChoiceOption(ans + ",no", "no")
      val listOptions = List(optionYes, optionNo)
      val question: Question = new MultipleChoiceQuestion(taskID, questionTitle, questionText, listOptions.asJava)
      val questionList = List(question)
      val numWorkers = 1
      val keywords = List("claim evaluation", "fast", "easy", "question", "yes", "no", "true", "false", "quick", "answer", "simple")
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, HIT_LIFETIME, MAJORITY_VOTE, REWARD_PER_HIT toFloat, HIT_LIFETIME, keywords.asJava)

      new AMTTask(hit)
    })
    
    tasks
  }

  /**
   * AMTTask generator for JOIN statement
   */
  def joinTasksGenerator(R: List[String], S: List[String]): List[AMTTask] = {
    
    val tasks = R.map(r => {
      val taskID = generateUniqueID()
      val questionTitle = "Is the following element part of a list"
      val questionDescription = "Question description" 
      val questionText = "Is [" + r + "] present in the following list : " + S.mkString(", ") + " ?"
      val optionYes = new MultipleChoiceOption(r + ",yes", "yes")
      val optionNo = new MultipleChoiceOption(r + ",no", "no")
      val listOptions = List(optionYes, optionNo)
      val question: Question = new MultipleChoiceQuestion(taskID, questionTitle, questionText, listOptions.asJava)
      val questionList = List(question)
      val numWorkers = 1
      val keywords = List("Claim evaluation", "Fast", "easy")
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, HIT_LIFETIME, MAJORITY_VOTE, REWARD_PER_HIT toFloat, HIT_LIFETIME, keywords.asJava)
      new AMTTask(hit)
    })
    
    tasks
  }
  
  /**
   * AMTTask generator for JOIN statement with pairwise comparaison
   */
  def pairwiseJoinTasksGenerator(R: List[String], S: List[String], predicate: String): List[AMTTask] = {
    val pairwiseTuples = for (r <- R; s <- S) yield (r, s)
    val tasks = pairwiseTuples.map(pair => {
      val taskID = generateUniqueID()
      val questionTitle = "Is the following claim true?"
      val questionDescription = "Question description" 
      val questionText = "Are [" + pair._1 + "] and ["+ pair._2 +"] satisfying the following predicate : [" + predicate +"] ?"
      val optionYes = new MultipleChoiceOption(pair + ",yes", "yes")
      val optionNo = new MultipleChoiceOption(pair + ",no", "no")
      val listOptions = List(optionYes, optionNo)
      val question: Question = new MultipleChoiceQuestion(taskID, questionTitle, questionText, listOptions.asJava)
      val questionList = List(question)
      val numWorkers = 1
      val keywords = List("Claim evaluation", "Fast", "easy")
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, HIT_LIFETIME, MAJORITY_VOTE, REWARD_PER_HIT toFloat, HIT_LIFETIME, keywords.asJava)
      new AMTTask(hit)
    })
    
    tasks
  }
  
  /**
   * AMTTask generator for GROUPBY statement
   */
  def groupByTasksGenerator(tuples: List[String], by: String): List[AMTTask] = {
    
    val tasks = tuples.map(tuple=> {
      val taskID = generateUniqueID()
      val questionTitle = "Simple question"
      val questionDescription = "Question description" 
      val questionText = "For the following element [ " + tuple + " ], what is its [ " + by + " ] ? Please put your answer after the coma and before the right parenthesis." 
      val question: Question = new StringQuestion(taskID, questionTitle, questionText, "("+tuple+",)", 1)
      val questionList = List(question)
      val numWorkers = 1
      val keywords = List("simple question", "question", "easy")
      val hit = new HIT(questionTitle, questionDescription, questionList.asJava, HIT_LIFETIME, numWorkers, REWARD_PER_HIT toFloat, ASSIGNMENT_LIFETIME, keywords.asJava)
      new AMTTask(hit)
    })
    
    tasks
  }
  
  /**
   * AMTTask generator for ORDERBY statement
   */
  def orderByTasksGenerator(tuples: List[String], order: Ordering): List[AMTTask] = {
    
    val taskID = generateUniqueID()
    val questionTitle = "Sort a list of " + tuples.size +" elements."
    val questionDescription = "Question description" 
    val questionText = "Please sort the following list : [ " + tuples.mkString(", ") + " ]  on [ " + returnString(order) + " ] attribute by [ " + ascOrDesc(order) + " ] order, please put only one element per line."
    val keywords = List("URL retrieval", "Fast")
    val numAssignments = 1
    val question: Question = new StringQuestion(taskID, questionTitle, questionText, "", 0)
    val hit = new HIT(questionTitle, questionDescription, List(question).asJava, HIT_LIFETIME, numAssignments, REWARD_SORT toFloat, HIT_LIFETIME, keywords.asJava)
    
    List(new AMTTask(hit))
  }
  
  /**
   * Creates a unique ID which is the full date followed by random numbers
   */
  def generateUniqueID(): String = new SimpleDateFormat("y-M-d-H-m-s").format(Calendar.getInstance().getTime()).toString + "--" + new Random().nextInt(100000)
  
  /**
   * Returns the string corresponding to ASC and DESC in order to formulate the question for the workers in an understandable manner
   */
  def ascOrDesc(order: Ordering): String = order match {
      case OrdAsc(_) => "ascending"
      case OrdDesc(_) => "descending"
  }
  
  /**
   * Returns the string on which we will sort
   */
  def returnString(order: Ordering): String = order match {
      case OrdAsc(string) => string
      case OrdDesc(string) => string
  }
 
  
}
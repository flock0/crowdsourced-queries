package crowdsourced.mturk;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains a complete assignment. This is an answer to all the questions of the corresponding HIT.
 * @author Florian Chlan
 *
 */
public class Assignment {
  
  /**
   * The answers to the questions.
   * Key: The unique identifier of the question.
   * Value: The answer to that particular question.
   */
  private Map<String, Answer> answers = new HashMap<String, Answer>();
  
  /**
   * Returns a map of answers to the questions.
   * Key: The unique identifier of the question.
   * Value: The answer to that particular question.
   * @return A map of answers to the questions.
   */
  public Map<String, Answer> getAnswers() {
    return answers;
  }
}

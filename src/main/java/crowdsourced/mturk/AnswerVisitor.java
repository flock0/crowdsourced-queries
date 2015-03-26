package crowdsourced.mturk;

import java.net.URL;

/**
 * A visitor for the answer objects.
 * @author Florian Chlan
 */
public interface AnswerVisitor {

  public void visit(String answer);
  
  public void visit(int answer);
  
  public void visit(URL answer);
  
  public void visit(MultipleChoiceOption answer);
  
  public void visit(boolean answer);
}

package crowdsourced.mturk;

import java.net.URL;

/**
 * A visitor for the answer objects
 */
public interface AnswerVisitor {
	public void visit(String answer);
	public void visit(int answer);
	public void visit(URL answer);
}

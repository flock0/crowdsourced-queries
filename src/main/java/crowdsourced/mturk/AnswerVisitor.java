package crowdsourced.mturk;

/**
 * A visitor for the answer objects
 */
public interface AnswerVisitor {
	public void visit(String answer);
	public void visit(int answer);
}

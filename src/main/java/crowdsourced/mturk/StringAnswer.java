package crowdsourced.mturk;

/**
 * An answer containing a string value.
 */
public class StringAnswer extends Answer {
	
	protected String answer;
	
	protected StringAnswer(AnswerType type, Question question) {
		super(type, question);
	}
	
	/**
	 * Creates a new answer object.
	 * @param question The question that this answer is for.
	 * @param answer The answer to the question.
	 */
	public StringAnswer(Question question, String answer) {
		this(AnswerType.String, question);
		this.answer = answer;
	}

	@Override
	public void getAnswer(AnswerVisitor ex) {
		ex.visit(answer);
	}
}

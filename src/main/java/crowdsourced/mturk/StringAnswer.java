package crowdsourced.mturk;

/**
 * An answer containing a string value.
 * @author Florian Chlan
 */
public class StringAnswer extends Answer {

	private String answer;

	protected StringAnswer(AnswerType type, Question question) {
		super(type, question);
	}

	/**
	 * Creates a new answer object.
	 * @param question The question that this answer is for.
	 * @param text The answer to the question.
	 */
	public StringAnswer(Question question, String text) {
		this(AnswerType.String, question);
		this.answer = text;
	}

	@Override
	public void getAnswer(AnswerVisitor ex) {
		ex.visit(answer);
	}

}

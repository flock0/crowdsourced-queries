package crowdsourced.mturk;

/**
 * An answer containing a numerical (integer) value.
 */
public class NumericAnswer extends Answer {

	protected int answer;
	
	protected NumericAnswer(AnswerType type, Question question) {
		super(type, question);
	}

	/**
	 * Creates a new answer object.
	 * @param question The question that this answer is for.
	 * @param answer The answer to the question.
	 */
	public NumericAnswer(Question question, int answer) {
		this(AnswerType.Numeric, question);
		this.answer = answer;
	}

	
	@Override
	public void getAnswer(AnswerVisitor ex) {
		ex.visit(answer);
	}

}

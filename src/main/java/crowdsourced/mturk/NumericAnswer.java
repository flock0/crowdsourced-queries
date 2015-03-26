package crowdsourced.mturk;

/**
 * An answer containing a numerical (integer) value.
 */
public class NumericAnswer extends Answer {

	private int answer;

	protected NumericAnswer(AnswerType type, Question question) {
		super(type, question);
	}

	/**
	 * Creates a new answer object.
	 * @param question The question that this answer is for.
	 * @param answer The answer to the question.
	 */
	public NumericAnswer(Question q, int num) {
		this(AnswerType.Numeric, q);
		this.answer = num;
	}

	@Override
	public void getAnswer(AnswerVisitor ex) {
		ex.visit(answer);
	}

}

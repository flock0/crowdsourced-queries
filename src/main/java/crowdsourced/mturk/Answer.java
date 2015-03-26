package crowdsourced.mturk;

/**
 * An answer to a HIT.
 */
public abstract class Answer {
	/**
	 * The type of the answer
	 */
	private AnswerType type;

	/**
	 * The question that this Answer belongs to.
	 */
	private Question question;

	/**
	 * Gets the answer value.
	 * @param ex The visitor implementation.
	 */
	public abstract void getAnswer(AnswerVisitor ex);

	protected Answer(AnswerType t, Question q) {
		this.type = t;
		this.question = q;
	}

	public AnswerType getType() {
		return type;
	}

	public Question getQuestion() {
		return question;
	}
}

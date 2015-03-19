package crowdsourced.mturk;

/**
 * An answer to a HIT.
 */
public abstract class Answer {
	/**
	 * The type of the answer
	 */
	protected AnswerType type;
	
	/**
	 * The question that this Answer belongs to.
	 */
	protected Question question;

	/**
	 * Gets the answer value.
	 * @param ex The visitor implementation.
	 */
	public abstract void getAnswer(AnswerVisitor ex);
	
	protected Answer(AnswerType type, Question question) {
		this.type = type;
		this.question = question;
	}
	
	public AnswerType getType() {
		return type;
	}
	
	public Question getQuestion() {
		return question;
	}
}

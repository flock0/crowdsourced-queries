package crowdsourced.mturk.answer;

import crowdsourced.mturk.question.Question;

/**
 * An answer to a HIT.
 * @author Florian Chlan
 */
public abstract class Answer {

    /**
	 * The question that this Answer belongs to.
	 */
	private Question question;

	/**
	 * Gets the answer value.
	 * @param ex The visitor implementation.
	 */
	public abstract void getAnswer(AnswerVisitor ex);

	protected Answer(Question q) {
		this.question = q;
	}

	public Question getQuestion() {
		return question;
	}
}

/**
 *
 */
package crowdsourced.mturk;

/**
 * An answer for a multiple-choice question containing the chosen option.
 */
public class MultipleChoiceAnswer extends Answer {

	private MultipleChoiceOption answer;

	protected MultipleChoiceAnswer(AnswerType type, Question question) {
		super(type, question);
	}

	/**
	 * Creates a new answer object.
	 * @param question The question that this answer is for.
	 * @param answer The answer to the question.
	 */
	public MultipleChoiceAnswer(Question question, MultipleChoiceOption choice) {
		this(AnswerType.MultipleChoice, question);
		this.answer = choice;
	}

	/* (non-Javadoc)
	 * @see crowdsourced.mturk.Answer#getAnswer(crowdsourced.mturk.AnswerVisitor)
	 */
	@Override
	public void getAnswer(AnswerVisitor ex) {
		ex.visit(answer);

	}

}

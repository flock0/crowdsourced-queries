package crowdsourced.mturk;

import java.net.URL;

public class URLAnswer extends Answer {

	private URL answer;

	protected URLAnswer(AnswerType type, Question question) {
		super(type, question);
	}

	/**
	 * Creates a new answer object.
	 * @param question The question that this answer is for.
	 * @param answer The answer to the question.
	 */
	public URLAnswer(Question question, URL answer) {
		this(AnswerType.URL, question);
		this.answer = answer;
	}

	@Override
	public void getAnswer(AnswerVisitor ex) {
		ex.visit(answer);

	}

}

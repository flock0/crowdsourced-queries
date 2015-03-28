package crowdsourced.mturk;

import java.net.URL;

/**
 * An answer containing an URL.
 * @author Florian Chlan
 */
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
	public URLAnswer(Question q, URL url) {
		this(AnswerType.URL, q);
		this.answer = url;
	}

	@Override
	public void getAnswer(AnswerVisitor ex) {
		ex.visit(answer);
	}

}

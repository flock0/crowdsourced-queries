package crowdsourced.mturk;

import javax.xml.bind.Element;

public class NumericQuestion extends Question {

	protected boolean hasRangeCheck = false;
	protected int minValue;
	protected int maxValue;
	
	/**
	 * Creates a question that requires a numeric answer.
	 * @param questionIdentifier An identifier for the question. This identifier is used to associate the Worker's answers with the question in the answer data.
	 * @param displayName A name for the question, displayed as a prominent heading.
	 * @param questionContent The text of the question.
	 */
	public NumericQuestion(String questionIdentifier, String displayName,
			String questionContent) {
		super(questionIdentifier, displayName, questionContent);
	}
	
	/**
	 * Creates a question that requires a numeric answer with a range check.
	 * @param minValue The minimum value of the answer. Must be smaller than maxValue.
	 * @param maxValue The minimum value of the answer. Must be greater than minValue.
	 * @param questionIdentifier An identifier for the question. This identifier is used to associate the Worker's answers with the question in the answer data.
	 * @param displayName A name for the question, displayed as a prominent heading.
	 * @param questionContent The text of the question.
	 */
	public NumericQuestion(int minValue, int maxValue, String questionIdentifier, String displayName,
			String questionContent) {
		super(questionIdentifier, displayName, questionContent);
		this.minValue = minValue;
		this.maxValue = maxValue;
		hasRangeCheck = true;
	}
	
	@Override
	public Element generateXML() {
		//TODO: Create an appropriate XML-Element.
		throw new UnsupportedOperationException("Not yet implemented");  
	}

}

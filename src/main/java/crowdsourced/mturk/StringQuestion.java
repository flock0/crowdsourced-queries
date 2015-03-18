package crowdsourced.mturk;

import javax.xml.bind.Element;

/**
 * A question that uses a simple text field for the answer.
 */
public class StringQuestion extends Question {
	
	/**
	 * Creates a question that uses a simple text field for the answer.
	 * @param questionIdentifier An identifier for the question. This identifier is used to associate the Worker's answers with the question in the answer data.
	 * @param displayName A name for the question, displayed as a prominent heading.
	 * @param questionContent The text of the question.
	 */
	public StringQuestion(String questionIdentifier, String displayName,
			String questionContent) {
		super(questionIdentifier, displayName, questionContent);
	}
	
	@Override
	public Element generateXML() {
		//TODO: Create an appropriate XML-Element.
		throw new UnsupportedOperationException("Not yet implemented");  
	}

}

package crowdsourced.mturk;

import javax.xml.bind.Element;

/**
 * A question that is asked to the workers in a HIT.
 * @see http://docs.aws.amazon.com/AWSMechTurk/latest/AWSMturkAPI/ApiReference_QuestionFormDataStructureArticle.html
 */
public abstract class Question {
	
	/**
	 * An identifier for the question. This identifier is used to associate the Worker's answers with the question in the answer data.
	 */
	protected String questionIdentifier;
	
	/**
	 * A name for the question, displayed as a prominent heading.
	 */
	protected String displayName;
	
	/**
	 * The text of the question.
	 */
	protected String questionContent;
	
	/**
	 * Creates a question.
	 * @param questionIdentifier An identifier for the question. This identifier is used to associate the Worker's answers with the question in the answer data.
	 * @param displayName A name for the question, displayed as a prominent heading.
	 * @param questionContent The text of the question.
	 */
	protected Question(String questionIdentifier, String displayName,
			String questionContent) {
		this.questionIdentifier = questionIdentifier;
		this.displayName = displayName;
		this.questionContent = questionContent;
	}


	/**
	 * Creates an XML element for the question that can be used in the request to AMT.
	 * @return A <Question>-XML-Element with all the properties set accordingly.
	 */
	public abstract Element generateXML();
}

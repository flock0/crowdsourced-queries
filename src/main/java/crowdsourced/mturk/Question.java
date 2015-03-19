package crowdsourced.mturk;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A question that is asked to the workers in a HIT.
 * @see http://docs.aws.amazon.com/AWSMechTurk/latest/AWSMturkAPI/ApiReference_QuestionFormDataStructureArticle.html
 */
public abstract class Question {

	/**
	 * An identifier for the question.
	 * This identifier is used to associate the Worker's answers with the
	 * question in the answer data.
	 */
	private String questionIdentifier;

	/**
	 * A name for the question, displayed as a prominent heading.
	 */
	private String displayName;

	/**
	 * The text of the question.
	 */
	private String questionContent;

	/**
	 * Creates a question.
	 * @param indentifier An identifier for the question. This identifier
	 *   is used to associate the Worker's answers with the question in the
	 *   answer data.
	 * @param name A name for the question, displayed as a prominent
	 *   heading.
	 * @param content The text of the question.
	 */
	protected Question(String identifier, String name,
					String content) {
		this.questionIdentifier = identifier;
		this.displayName = name;
		this.questionContent = content;
	}


	/**
	 * Creates an XML element for the question that can be used in the request to AMT.
	 * @param doc The document to which the returned element will later be
	 *   added. (The caller is responsible for the addition of the element to
	 *   the document.)
	 * @return A <Question>-XML-Element with all the properties set accordingly.
	 */
	public Element asXMLElement(Document doc) {
		Element question = doc.createElement("Question");

		Element identifier = doc.createElement("QuestionIdentifier");
		identifier.appendChild(doc.createTextNode(this.questionIdentifier));

		Element name = doc.createElement("DisplayName");
		name.appendChild(doc.createTextNode(this.displayName));

		Element required = doc.createElement("IsRequired");
		required.appendChild(doc.createTextNode("true"));

		Element content = doc.createElement("QuestionContent");
		content.appendChild(doc.createTextNode("Text"));

		question.appendChild(identifier);
		question.appendChild(name);
		question.appendChild(required);
		question.appendChild(content);
		return question;
	}
}

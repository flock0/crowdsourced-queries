package crowdsourced.mturk.question;

import java.io.IOException;

import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import crowdsourced.mturk.answer.Answer;

/**
 * A question that is asked to the workers in a HIT.
 * @see http://docs.aws.amazon.com/AWSMechTurk/latest/AWSMturkAPI/ApiReference_QuestionFormDataStructureArticle.html
 * @author Florian Chlan
 */
public abstract class Question {

	/**
	 * An identifier for the question.
	 * This identifier is used to associate the Worker's answers with the
	 * question in the answer data.
	 */
	private String identifier;

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
	 * @param _identifier An identifier for the question. This identifier
	 *   is used to associate the Worker's answers with the question in the
	 *   answer data.
	 * @param _name A name for the question, displayed as a prominent
	 *   heading.
	 * @param _content The text of the question.
	 */
	protected Question(String _identifier, String _name, String _content) {
		this.identifier = _identifier;
		this.displayName = _name;
		this.questionContent = _content;
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

		Element questIdentifier = doc.createElement("QuestionIdentifier");
		questIdentifier.appendChild(doc.createTextNode(this.identifier));

		Element name = doc.createElement("DisplayName");
		name.appendChild(doc.createTextNode(this.displayName));

		Element required = doc.createElement("IsRequired");
		required.appendChild(doc.createTextNode("true"));

		Element content = doc.createElement("QuestionContent");
		Element subContent = doc.createElement("Text");
		subContent.appendChild(doc.createTextNode(this.questionContent));
		content.appendChild(subContent);

		question.appendChild(questIdentifier);
		question.appendChild(name);
		question.appendChild(required);
		question.appendChild(content);
		return question;
	}


    /**
     * Parses an XML-<Answer>-Node and extracts the value from it
     *
     * @param item
     *            An XML-<Answer>-node from AMT.
     * @param xPath
     *            An xPath object to use to traverse the XML-node.
     * @return An answer object with the value extracted.
     * @throws IOException
     *             when the parsing of the <Answer>-node fails.
     */
	public abstract Answer parseXMLAnswer(Node item, XPath xPath) throws IOException;

	public String getIdentifier() {
		return identifier;
	}
}

package crowdsourced.mturk;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A question that accepts a single URL.
 * @author Florian Chlan, Florian Vessaz
 */
public class URLQuestion extends StringQuestion {

	private static final String JAVASCRIPT_URL_REGEX = "@(https?|ftp)://(-\\.)?([^\\s/?\\.#-]+\\.?)+(/[^\\s]*)?$@iS";

	/**
	 * Creates a question that expect an URL as answer.
	 * @param identifier an identifier for the question. this
	 *   identifier is used to associate the Worker's answers with the question
	 *   in the answer data.
	 * @param name A name for the question, displayed as a prominent heading.
	 * @param content The text of the question.
	 */
	public URLQuestion(String identifier, String name, String content) {
		super(identifier, name, content);
		setDefaultText("https://");
		setSuggestedLineCount(1);
	}

	@Override
	public Element asXMLElement(Document doc) {
		Element question = super.asXMLElement(doc);

		Element constraints;
		NodeList nl = question.getElementsByTagName("Constraints");
		if (nl.getLength() > 0) {
			constraints = (Element) nl.item(0);
		} else {
			//Not needed anymore. Will be deleted in the future.
			NodeList nlUp = question.getElementsByTagName("FreeTextAnswer");
			Element freeText = (Element) nlUp.item(0);
			constraints = doc.createElement("Constraints");
			freeText.appendChild(constraints);
		}

		Element format = doc.createElement("AnswerFormatRegex");
		constraints.appendChild(format);
		format.setAttribute("regex", JAVASCRIPT_URL_REGEX);
		format.setAttribute("errorText", "A well formated URL (including protocol) is expected");

		return question;
	}

}

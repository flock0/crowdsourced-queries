package crowdsourced.mturk;

import java.io.IOException;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A question that accepts a single URL.
 *
 * @author Florian Chlan, Florian Vessaz
 */
public class URLQuestion extends StringQuestion {

    private static final String JAVASCRIPT_URL_REGEX =
        "((http|ftp|https):\\/\\/)[\\w-]+(\\.[\\w-]+)+([\\w.,@?^=%&amp;:\\/~+#-]*[\\w@?^=%&amp;\\/~+#-])?";

    /**
     * Creates a question that expect an URL as answer.
     *
     * @param identifier
     *            an identifier for the question. this identifier is used to
     *            associate the Worker's answers with the question in the answer
     *            data.
     * @param name
     *            A name for the question, displayed as a prominent heading.
     * @param content
     *            The text of the question.
     */
    public URLQuestion(String identifier, String name, String content) {
        super(identifier, name, content, "http://", 1);
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
        format.setAttribute("errorText",
                "A well formated URL (including protocol) is expected");

        return question;
    }

    @Override
    public Answer parseXMLAnswer(Node item, XPath xPath) throws IOException {
        try {
            String answer = xPath.compile("./FreeText").evaluate(item);
            return new URLAnswer(this, new URL(answer));
        } catch (XPathExpressionException e) {
            throw new IOException("Couldn't parse <Answer> to URLQuestion: ", e);
        }
    }
}

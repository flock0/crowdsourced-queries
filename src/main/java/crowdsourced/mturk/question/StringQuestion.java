package crowdsourced.mturk.question;

import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import crowdsourced.mturk.answer.Answer;
import crowdsourced.mturk.answer.StringAnswer;

/**
 * A question that uses a simple text field for the answer.
 *
 * @author Florian Chlan, Florian Vessaz
 */
public class StringQuestion extends Question {

    private int answerMinLength = 0;
    private int answerMaxLength = 0;
    private int suggestedLineCount = 0;
    private String defaultText = null;

    /**
     * Creates a question that uses a simple text field for the answer.
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
    public StringQuestion(String identifier, String name, String content, String _defaultText, int numberLines) {
        super(identifier, name, content);
        setDefaultText(_defaultText);
        if (numberLines > 0) {
            setSuggestedLineCount(numberLines);
        }
    }

    /**
     * Set the length constraint of the answer.
     *
     * @param min
     *            The minimum number of characters allowed.
     * @param max
     *            The maximum number of characters allowed.
     */
    public void setAnswerLength(int min, int max) {
        this.answerMinLength = min;
        this.answerMaxLength = max;
    }

    /**
     * Set the suggested number of line that the turk should provide.
     *
     * @param lines
     *            The suggested number of lines of the answer.
     */
    public void setSuggestedLineCount(int lines) {
        this.suggestedLineCount = lines;
    }

    /**
     * Set the default text of this answer. The default text can be used to
     * suggest the wanted format of the answer. For instance you may set it to
     * "8:00 - 20:00" to suggest a time range format.
     *
     * @param text
     *            The suggested answer text.
     */
    public void setDefaultText(String text) {
        this.defaultText = text;
    }

    @Override
    public Element asXMLElement(Document doc) {
        Element question = super.asXMLElement(doc);

        Element answerSpec = doc.createElement("AnswerSpecification");
        question.appendChild(answerSpec);
        Element freeText = doc.createElement("FreeTextAnswer");
        answerSpec.appendChild(freeText);
        Element constraints = doc.createElement("Constraints");
        freeText.appendChild(constraints);
        if (answerMinLength > 0 || answerMaxLength > 0) {
            Element length = doc.createElement("Length");
            constraints.appendChild(length);
            if (answerMinLength > 0) {
                length.setAttribute("minLength", Integer.toString(answerMinLength));
            }
            if (answerMaxLength > 0) {
                length.setAttribute("maxLength", Integer.toString(answerMaxLength));
            }
        }

        if (defaultText != null) {
            Element text = doc.createElement("DefaultText");
            text.appendChild(doc.createTextNode(defaultText));
            freeText.appendChild(text);
        }

        if (suggestedLineCount > 0) {
            Element lines = doc.createElement("NumberOfLinesSuggestion");
            lines.appendChild(doc.createTextNode(Integer.toString(suggestedLineCount)));
            freeText.appendChild(lines);
        }

        return question;
    }

    @Override
    public Answer parseXMLAnswer(Node item, XPath xPath) throws IOException {
        try {
            String answer = xPath.compile("./FreeText").evaluate(item);
            return new StringAnswer(this, answer);
        } catch (XPathExpressionException e) {
            throw new IOException("Couldn't parse <Answer> to StringQuestion: ", e);
        }
    }

}

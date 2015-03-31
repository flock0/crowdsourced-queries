package crowdsourced.mturk;

import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A question that accepts a numerical value for the answer.
 *
 * @author Florian Chlan, Florian Vessaz
 */
public class NumericQuestion extends Question {

    private boolean hasRangeCheck = false;
    private int minValue = Integer.MIN_VALUE;
    private int maxValue = Integer.MAX_VALUE;

    /**
     * Creates a question that requires a numeric answer.
     *
     * @param questionIdentifier
     *            An identifier for the question. This identifier is used to
     *            associate the Worker's answers with the question in the answer
     *            data.
     * @param displayName
     *            A name for the question, displayed as a prominent heading.
     * @param questionContent
     *            The text of the question.
     */
    public NumericQuestion(String questionIdentifier, String displayName,
            String questionContent) {
        super(questionIdentifier, displayName, questionContent);
    }

    /**
     * Creates a question that requires a numeric answer with a range check.
     *
     * @param min
     *            The minimum value of the answer.
     * @param max
     *            The maximum value of the answer.
     * @param identifier
     *            An identifier for the question. This identifier is used to
     *            associate the Worker's answers with the question in the answer
     *            data.
     * @param name
     *            A name for the question, displayed as a prominent heading.
     * @param content
     *            The text of the question.
     */
    public NumericQuestion(int min, int max, String identifier, String name,
            String content) {
        super(identifier, name, content);
        this.minValue = min;
        this.maxValue = max;
        hasRangeCheck = true;
    }

    @Override
    public Element asXMLElement(Document doc) {
        Element question = super.asXMLElement(doc);

        Element answerSpec = doc.createElement("AnswerSpecification");
        question.appendChild(answerSpec);
        Element freeText = doc.createElement("FreeTextAnswer");
        answerSpec.appendChild(freeText);
        Element lines = doc.createElement("NumberOfLinesSuggestion");
        lines.appendChild(doc.createTextNode("1"));
        freeText.appendChild(lines);
        Element constraints = doc.createElement("Constraints");
        freeText.appendChild(constraints);
        Element numeric = doc.createElement("IsNumeric");
        constraints.appendChild(numeric);

        if (hasRangeCheck) {
            numeric.setAttribute("minValue", Integer.toString(minValue));
            numeric.setAttribute("maxValue", Integer.toString(maxValue));
        }

        return question;
    }

    @Override
    public Answer parseXMLAnswer(Node item, XPath xPath) throws IOException {
        try {
            String answerString = xPath.compile("./FreeText").evaluate(item);
            int answer = Integer.parseInt(answerString);
            return new NumericAnswer(this, answer);
        } catch (XPathExpressionException e) {
            throw new IOException("Couldn't parse <Answer> to NumericQuestion: ", e);
        }
    }

}

package crowdsourced.mturk;

import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A True/False question.
 *
 * @author Florian Chlan
 */
public class BooleanChoiceQuestion extends Question {

    /**
     * Creates a True/False question.
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
    public BooleanChoiceQuestion(String identifier, String name, String content) {
        super(identifier, name, content);
    }

    @Override
    public Element asXMLElement(Document doc) {
        Element question = super.asXMLElement(doc);

        Element answerSpec = doc.createElement("AnswerSpecification");
        question.appendChild(answerSpec);

        Element selectionAnswer = doc.createElement("SelectionAnswer");
        answerSpec.appendChild(selectionAnswer);

        Element styleSugg = doc.createElement("StyleSuggestion");
        styleSugg.appendChild(doc.createTextNode("radiobutton"));
        selectionAnswer.appendChild(styleSugg);

        Element selections = doc.createElement("Selections");
        selectionAnswer.appendChild(selections);
        selections.appendChild(getSelectionElement("True", doc));
        selections.appendChild(getSelectionElement("False", doc));

        return question;
    }

    private Node getSelectionElement(String string, Document doc) {
        Element selection = doc.createElement("Selection");

        Element id = doc.createElement("SelectionIdentifier");
        id.appendChild(doc.createTextNode(string));

        Element text = doc.createElement("Text");
        text.appendChild(doc.createTextNode(string));

        selection.appendChild(id);
        selection.appendChild(text);

        return selection;
    }

    @Override
    public Answer parseXMLAnswer(Node item, XPath xPath) throws IOException {
        try {
            String selectionIdentifier = xPath.compile("./SelectionIdentifier").evaluate(item);
            return new BooleanChoiceAnswer(this, selectionIdentifier.equals("True"));
        } catch (XPathExpressionException e) {
            throw new IOException(
                    "Couldn't parse <Answer> to BooleanChoiceQuestion: ", e);
        }
    }

}

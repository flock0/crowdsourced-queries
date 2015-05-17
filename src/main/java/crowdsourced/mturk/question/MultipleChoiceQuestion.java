package crowdsourced.mturk.question;

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import crowdsourced.mturk.answer.Answer;
import crowdsourced.mturk.answer.MultipleChoiceAnswer;

/**
 * A question that is presented as multiple-choice to the worker.
 *
 * @author Florian Chlan
 */
public class MultipleChoiceQuestion extends Question {

    private List<MultipleChoiceOption> answerOptions;

    /**
     * Creates a multiple-choice question.
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
    protected MultipleChoiceQuestion(String identifier, String name,
            String content) {
        super(identifier, name, content);
    }

    /**
     * Creates a multiple-choice question.
     *
     * @param identifier
     *            an identifier for the question. this identifier is used to
     *            associate the Worker's answers with the question in the answer
     *            data.
     * @param name
     *            A name for the question, displayed as a prominent heading.
     * @param content
     *            The text of the question.
     * @param options
     *            A list of possible answers that are presented to the worker.
     *            Must have at least two elements.
     */
    public MultipleChoiceQuestion(String identifier, String name,
            String content, List<MultipleChoiceOption> options) {
        this(identifier, name, content);
        setAnswerOptions(options);
    }

    /**
     * Set the possible answer that are presented to the worker.
     *
     * @param answerOptions
     *            A list of possible answers that are presented to the worker.
     *            Must have at least to elements.
     */
    private void setAnswerOptions(List<MultipleChoiceOption> _answerOptions) {
        this.answerOptions = _answerOptions;
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
        for (MultipleChoiceOption opt : answerOptions) {
            selections.appendChild(opt.asXMLElement(doc));
        }

        return question;
    }

    @Override
    public Answer parseXMLAnswer(Node item, XPath xPath) throws IOException {
        try {
            String selectionIdentifier = xPath.compile("./SelectionIdentifier").evaluate(item);
            for (MultipleChoiceOption mco : answerOptions) {
                if (mco.getIdentifier().equals(selectionIdentifier)) {
                    return new MultipleChoiceAnswer(this, mco);
                }
            }
            throw new IOException("Selection from the MultipleChoiceQuestion could not be mapped to a valid answer.");
        } catch (XPathExpressionException e) {
            throw new IOException("Couldn't parse <Answer> to MultipleChoiceQuestion: ", e);
        }
    }

}

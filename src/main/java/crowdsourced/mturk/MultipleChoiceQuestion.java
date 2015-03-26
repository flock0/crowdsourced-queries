package crowdsourced.mturk;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A question that is presented as multiple-choice to the worker.
 */
public class MultipleChoiceQuestion extends Question {

	private List<MultipleChoiceOption> answerOptions;
	/**
	 * Creates a multiple-choice question.
	 * @param identifier an identifier for the question. this
	 *   identifier is used to associate the Worker's answers with the question
	 *   in the answer data.
	 * @param name A name for the question, displayed as a prominent heading.
	 * @param content The text of the question.
	 */
	protected MultipleChoiceQuestion(String identifier, String name, String content) {
		super(identifier, name, content);
	}

	/**
	 * Creates a multiple-choice question.
	 * @param identifier an identifier for the question. this
	 *   identifier is used to associate the Worker's answers with the question
	 *   in the answer data.
	 * @param name A name for the question, displayed as a prominent heading.
	 * @param content The text of the question.
	 * @param options A list of possible answers that are presented to the worker. Must have at least to elements.
	 */
	public MultipleChoiceQuestion(String identifier, String name, String content, List<MultipleChoiceOption> options) {
		this(identifier, name, content);
		setAnswerOptions(options);
	}

	/**
	 * Set the possible answer that are presented to the worker.
	 * @param answerOptions A list of possible answers that are presented to the worker. Must have at least to elements.
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


}

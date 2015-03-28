package crowdsourced.mturk;

//TODO Is this really needed? We have the visitor pattern, which should render a type-enum obsolete
/**
 * Indicates what kind of type an answer has.
 * @author Florian Chlan
 *
 */
public enum AnswerType {
	String,
	Numeric,
	URL,
	MultipleChoice,
	Boolean
}

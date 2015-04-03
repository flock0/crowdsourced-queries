package crowdsourced.mturk;

/**
 * An answer containing a boolean value.
 *
 * @author Florian Chlan
 */
public class BooleanChoiceAnswer extends Answer {

    private boolean answer;

    protected BooleanChoiceAnswer(Question question) {
        super(question);
    }

    /**
     * Creates a new answer object.
     *
     * @param question
     *            The question that this answer is for.
     * @param answer
     *            The answer to the question.
     */
    public BooleanChoiceAnswer(Question question, boolean choice) {
        this(question);
        this.answer = choice;
    }

    @Override
    public void getAnswer(AnswerVisitor ex) {
        ex.visit(answer);
    }

    public String toString() {
        return "" + answer;
    }

}

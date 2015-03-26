package crowdsourced.mturk;

/**
 * An answer containing a boolean value.
 */
public class BooleanChoiceAnswer extends Answer {

    private boolean answer;

    protected BooleanChoiceAnswer(AnswerType type, Question question) {
        super(type, question);
        // TODO Auto-generated constructor stub
    }

    /**
     * Creates a new answer object.
     * @param question The question that this answer is for.
     * @param answer The answer to the question.
     */
    public BooleanChoiceAnswer(Question question, boolean choice) {
        this(AnswerType.Boolean, question);
        this.answer = choice;
    }

    @Override
    public void getAnswer(AnswerVisitor ex) {
        ex.visit(answer);

    }

}

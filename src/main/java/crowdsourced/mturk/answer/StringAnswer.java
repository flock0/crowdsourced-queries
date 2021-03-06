package crowdsourced.mturk.answer;

import crowdsourced.mturk.question.Question;

/**
 * An answer containing a string value.
 *
 * @author Florian Chlan
 */
public class StringAnswer extends Answer {

    private String answer;

    protected StringAnswer(Question question) {
        super(question);
    }

    /**
     * Creates a new answer object.
     *
     * @param question
     *            The question that this answer is for.
     * @param text
     *            The answer to the question.
     */
    public StringAnswer(Question question, String text) {
        this(question);
        this.answer = text;
    }

    @Override
    public void getAnswer(AnswerVisitor ex) {
        ex.visit(answer);
    }

    public String toString() {
        if (answer == null) {
            return "";
        } else {
            return answer;
        }
    }

}

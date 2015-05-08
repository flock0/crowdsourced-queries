package crowdsourced.mturk.answer;

import crowdsourced.mturk.question.Question;

/**
 * An answer containing a numerical (integer) value.
 *
 * @author Florian Chlan
 */
public class NumericAnswer extends Answer {

    private int answer;

    protected NumericAnswer(Question question) {
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
    public NumericAnswer(Question q, int num) {
        this(q);
        this.answer = num;
    }

    @Override
    public void getAnswer(AnswerVisitor ex) {
        ex.visit(answer);
    }

    public String toString() {
        return "" + answer;
    }

}

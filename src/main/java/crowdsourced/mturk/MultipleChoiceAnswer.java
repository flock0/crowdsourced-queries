/**
 *
 */
package crowdsourced.mturk;

/**
 * An answer for a multiple-choice question containing the chosen option.
 *
 * @author Florian Chlan
 */
public class MultipleChoiceAnswer extends Answer {

    private MultipleChoiceOption answer;

    protected MultipleChoiceAnswer(Question question) {
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
    public MultipleChoiceAnswer(Question question, MultipleChoiceOption choice) {
        this(question);
        this.answer = choice;
    }

    @Override
    public void getAnswer(AnswerVisitor ex) {
        ex.visit(answer);
    }
    
    public String toString(){
    	return answer.getIdentifier() + "," +answer.getText();
    }

}

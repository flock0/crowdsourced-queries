package crowdsourced.mturk.answer;

import java.net.URL;

import crowdsourced.mturk.question.MultipleChoiceOption;

/**
 * A visitor for the answer objects.
 *
 * @author Florian Chlan
 */
public interface AnswerVisitor {

    void visit(String answer);

    void visit(int answer);

    void visit(URL answer);

    void visit(MultipleChoiceOption answer);

    void visit(boolean answer);
}

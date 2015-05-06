package crowdsourced.test.mturk;

import java.net.URL;

import crowdsourced.mturk.AnswerVisitor;
import crowdsourced.mturk.MultipleChoiceOption;

public class ManualTestVisitor implements AnswerVisitor {

    @Override
    public void visit(String answer) {
        System.out.println("Visited String: " + answer);
    }

    @Override
    public void visit(int answer) {
        System.out.println("Visited int: " + answer);

    }

    @Override
    public void visit(URL answer) {
        System.out.println("Visited URL: " + answer.toString());

    }

    @Override
    public void visit(MultipleChoiceOption answer) {
        System.out.println("Visited MultipleChoice: " + answer.getText());

    }

    @Override
    public void visit(boolean answer) {
        System.out.println("Visited boolean: " + answer);

    }

}

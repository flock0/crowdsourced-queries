package crowdsourced.test.mturk;

import java.util.List;

import crowdsourced.mturk.answer.Answer;
import crowdsourced.mturk.answer.AnswerCallback;
import crowdsourced.mturk.answer.AnswerVisitor;
import crowdsourced.mturk.task.Assignment;

public class ManualTestCallback implements AnswerCallback {

    @Override
    public void newAssignmentsReceived(List<Assignment> newAssignments) {
        System.out.println("We have received new assignments:");
        AnswerVisitor vis = new ManualTestVisitor();
        for (Assignment a : newAssignments) {
            System.out.println("AssignmentID: " + a.getAssignmentID());
            for (Answer ans : a.getAnswers().values()) {
                ans.getAnswer(vis);
            }
        }
    }

    @Override
    public void jobFinished() {
        // TODO Auto-generated method stub
    }

    @Override
    public void errorOccured() {
        // TODO Auto-generated method stub
    }

  }

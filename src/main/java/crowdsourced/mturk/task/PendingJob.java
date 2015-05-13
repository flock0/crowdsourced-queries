package crowdsourced.mturk.task;

import java.util.Set;

/**
 * Holds information over a pending job/HIT, its status and received assignments so far.
 *
 * @author Florian Chlan
 */
public class PendingJob {

    private AMTTask task;

    PendingJob(AMTTask _task) {
        this.task = _task;
    }
    public void abort() {
        task.finishTask();
    }

    public HIT getHIT() {
        return task.getHIT();
    }

    public Set<Assignment> getAssignments() {
        return task.getAssignments();
    }

}

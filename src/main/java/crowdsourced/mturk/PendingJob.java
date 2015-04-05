package crowdsourced.mturk;

import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds information over a pending job/HIT, its status and received assignments so far.
 *
 * @author Florian Chlan
 */
public class PendingJob {

    private AMTTask task;

    public PendingJob(AMTTask task) {
        this.task = task;
    }
    public void abort() {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }

    public HIT getHIT() {
        return task.getHIT();
    }

    public Set<Assignment> getAssignments() {
        return task.getAssignments();
    }

}

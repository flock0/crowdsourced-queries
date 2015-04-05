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

    private final HIT hit;
    private Timer answerTimer;
    private Timer finishedTimer;
    /**
     * The assignments that have been received so far for the HIT.
     */
    private final Set<Assignment> assignments;

    public PendingJob(HIT _hit, AnswerCallback _callback) {
        this.hit = _hit;
        assignments = Collections.newSetFromMap(new ConcurrentHashMap<Assignment, Boolean>());
        answerTimer = new Timer();
        answerTimer.schedule(new PollAnswersTask(hit, _callback, assignments),
                PollAnswersTask.POLLING_INITIAL_DELAY_MILLISECONDS, PollAnswersTask.POLLING_RATE_MILLISECONDS);
        finishedTimer = new Timer();
        finishedTimer.schedule(new PollFinishedTask(), hit.getLifetimeInSeconds() * 1000, PollFinishedTask.POLLING_FINISHED_RATE_MILLISECONDS);
    }

    public void abort() {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }

    public HIT getHIT() {
        return hit;
    }

    public Set<Assignment> getAssignments() {
        return assignments;
    }

}

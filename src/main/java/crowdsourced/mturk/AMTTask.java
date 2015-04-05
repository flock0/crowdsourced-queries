package crowdsourced.mturk;

import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds information over a pending job/HIT, its status and received assignments so far. This object is mainly used internally.
 * 
 * @author Florian Chlan
 */
public class AMTTask {

    private final HIT hit;
    private PollAnswersTimerTask pollingTask;
    private Timer pollingTimer;
    private Timer finishedTimer;
    private AnswerCallback callback;
    
    /**
     * The assignments that have been received so far for the HIT.
     */
    private final Set<Assignment> assignments;

    public AMTTask(HIT _hit, AnswerCallback _callback) {
        this.hit = _hit;
        assignments = Collections.newSetFromMap(new ConcurrentHashMap<Assignment, Boolean>());
        pollingTimer = new Timer();
        pollingTask = new PollAnswersTimerTask(this);
        pollingTimer.schedule(pollingTask,
                PollAnswersTimerTask.POLLING_INITIAL_DELAY_MILLISECONDS, PollAnswersTimerTask.POLLING_RATE_MILLISECONDS);
    }

    public HIT getHIT() {
        return hit;
    }

    public AnswerCallback getCallback() {
        return callback;
    }

    public Set<Assignment> getAssignments() {
        return assignments;
    }

    public void finishJob() {
        pollingTask.cancelAfterNextRun();
    }

    public PendingJob getJob() {
        return new PendingJob(this); 
    }
}

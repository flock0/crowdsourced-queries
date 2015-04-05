package crowdsourced.mturk;

import java.io.IOException;
import java.security.SignatureException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds information over a pending job/HIT, its status and received assignments so far.
 * This object is mainly used internally.
 *
 * @author Florian Chlan
 */
public class AMTTask {

    /**
     * Used in the Timer to poll for new assignments.
     */
    private PollAnswersTimerTask pollingTask;
    /**
     * Used to poll for new assignments.
     */
    private Timer pollingTimer;
    /**
     * The assignments that have been received so far for the HIT.
     */
    private final Set<Assignment> assignments;
    private final HIT hit;
    private AnswerCallback callback;

    AMTTask(HIT _hit, AnswerCallback _callback) {
        this.hit = _hit;
        this.callback = _callback;
        assignments = Collections.newSetFromMap(new ConcurrentHashMap<Assignment, Boolean>());
    }

    /**
     * Starts the for polling for new assignments.
     */
    void startPolling() {
        pollingTimer = new Timer();
        pollingTask = new PollAnswersTimerTask(this);
        pollingTimer.schedule(pollingTask,
                PollAnswersTimerTask.POLLING_INITIAL_DELAY_MILLISECONDS,
                PollAnswersTimerTask.POLLING_RATE_MILLISECONDS);
    }

    /**
     * Indicates that this task should be finished after the next retrieval of assignments.
     */
    void finishTask() {
        pollingTask.cancelAfterNextRun();
        AMTCommunicator.finishTask(this);
    }

    /**
     * Disposes this task from AMT.
     */
    void disposeTask() {
        Map<String, String> param = new HashMap<String, String>();
        param.put("Operation", "DisposeHIT");
        param.put("HITId", hit.getHITId());

        try {
            AMTCommunicator.sendGet(param);
        } catch (SignatureException | IOException e) {
            /* Nothing we can do about it here. Just ignore */
        }
        System.out.println(String.format("HIT %s disposed", hit.getHITId()));
    }

    PendingJob getJob() {
        return new PendingJob(this);
    }

    HIT getHIT() {
        return hit;
    }

    AnswerCallback getCallback() {
        return callback;
    }

    Set<Assignment> getAssignments() {
        return assignments;
    }
}

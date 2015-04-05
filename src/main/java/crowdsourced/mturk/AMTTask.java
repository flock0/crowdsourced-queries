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
        this.callback = _callback;
        assignments = Collections.newSetFromMap(new ConcurrentHashMap<Assignment, Boolean>());
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
        AMTCommunicator.finishJob(this);
    }

    public void disposeJob() {
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

    public PendingJob getJob() {
        return new PendingJob(this); 
    }

    public void startPolling() {
        pollingTimer = new Timer();
        pollingTask = new PollAnswersTimerTask(this);
        pollingTimer.schedule(pollingTask,
                PollAnswersTimerTask.POLLING_INITIAL_DELAY_MILLISECONDS, PollAnswersTimerTask.POLLING_RATE_MILLISECONDS);
    }
}

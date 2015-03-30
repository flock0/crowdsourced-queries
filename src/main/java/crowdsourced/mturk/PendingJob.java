package crowdsourced.mturk;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds information over a pending job/HIT, its status and received assignments so far.
 *
 * @author Florian Chlan
 */
public class PendingJob {

    private final HIT hit;

    /**
     * The assignments that have been received so far for the HIT.
     */
    private final Set<Assignment> assignments;

    public PendingJob(HIT _hit) {
        this.hit = _hit;
        assignments = Collections.newSetFromMap(new ConcurrentHashMap<Assignment, Boolean>());
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

package crowdsourced.mturk;

import java.util.List;

/**
 * Contains methods to notify about status update concerning a HIT.
 *
 * @author Florian Chlan
 */
public interface AnswerCallback {

    /**
     * Invoked when new assignments have been received.
     *
     * @param newAssignments
     *            A list of new complete assignments.
     */
    void newAssignmentsReceived(List<Assignment> newAssignments);

    /**
     * Invoked when the job is finished an no further answer can be expected for
     * this HIT.
     */
    void jobFinished();

    /**
     * Invoked when an error occured and the HIT can no longer be processed.
     */
    void errorOccured();
}

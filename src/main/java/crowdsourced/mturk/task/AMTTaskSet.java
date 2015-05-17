package crowdsourced.mturk.task;

import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all active tasks.
 * Also takes care of polling for finished tasks.
 *
 * @author Florian Chlan
 *
 */
public class AMTTaskSet {

    /**
     * A set of all tasks currently active.
     */
    private Set<AMTTask> activeTasks = Collections.newSetFromMap(new ConcurrentHashMap<AMTTask, Boolean>());
    /**
     * Used for polling for finished tasks.
     */
    private Timer reviewableTimer = null;

    /**
     * Adds a task to the set of active tasks.
     * If the collection was previously empty, start the Timer.
     *
     * @param task The task to add to the set.
     */
    public synchronized void add(AMTTask task) {
        activeTasks.add(task);
        if (activeTasks.size() == 1) {
            reviewableTimer = new Timer();
            reviewableTimer.schedule(new PollFinishedTimerTask(this), 0,
                    PollFinishedTimerTask.POLLING_FINISHED_RATE_MILLISECONDS);
        }
    }

    /**
     * Removes a task from the set of active tasks.
     * If the collection is now empty, stop the timer.
     * @param task The task to remove from the set.
     */
    public synchronized void finish(AMTTask task) {
        activeTasks.remove(task);
        if (activeTasks.isEmpty()) {
            reviewableTimer.cancel();
            reviewableTimer = null;
        }
    }

    /**
     * Returns the set of active tasks.
     * @return The set of active tasks.
     */
    public Set<AMTTask> getActiveTasks() {
        return activeTasks;
    }
}

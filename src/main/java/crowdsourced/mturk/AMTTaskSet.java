package crowdsourced.mturk;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class AMTTaskSet {
    
    private Set<AMTTask> activeTasks = Collections.newSetFromMap(new HashMap<AMTTask, Boolean>());
    private Timer reviewableTimer = null;
    
    public synchronized void add(AMTTask task) {
        activeTasks.add(task);
        if(activeTasks.size() == 1) {
            reviewableTimer = new Timer();
            reviewableTimer.schedule(new PollFinishedTimerTask(this), 30, PollFinishedTimerTask.POLLING_FINISHED_RATE_MILLISECONDS);
        }
    }
    
    public synchronized void finish(AMTTask task) {
        activeTasks.remove(task);
        if(activeTasks.isEmpty()) {
            reviewableTimer.cancel();
            reviewableTimer = null;
        }
    }

    public Set<AMTTask> getActiveTasks() {
        return activeTasks;
    }
}

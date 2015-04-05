package crowdsourced.mturk;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class AMTActiveTasks {
    
    private Set<AMTTask> activeTasks = new HashSet<>();
    private Timer reviewableTimer;
    
    public synchronized void add(AMTTask task) {
        activeTasks.add(task);
        if(activeTasks.size() == 1) {
            reviewableTimer = new Timer();
            reviewableTimer.schedule(new PollFinishedTimerTask(this), PollFinishedTimerTask.POLLING_FINISHED_RATE_MILLISECONDS);
        }
    }
    
    public synchronized void finish(AMTTask task) {
        activeTasks.remove(task);
        if(activeTasks.isEmpty()) {
            reviewableTimer.cancel();
        }
    }
}

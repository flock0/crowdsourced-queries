package crowdsourced.mturk;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Handles the checking of whether all accepted assignments for the HIT are submitted.
 *
 * @author Florian Chlan
 */
public class PollFinishedTimerTask extends TimerTask {

    public static final long POLLING_FINISHED_RATE_MILLISECONDS = 120 * 1000; // At what rate should we poll whether the HIT is finished? This is only relevant after the lifetime of the HIT is over.
    private AMTActiveTasks amtActiveTasks;
    private DocumentBuilder docBuilder;
    
    public PollFinishedTimerTask(AMTActiveTasks amtActiveTasks) {
         this.amtActiveTasks = amtActiveTasks;
         DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
         try {
             docBuilder = docFactory.newDocumentBuilder();
         } catch (ParserConfigurationException e) {
             throw new RuntimeException(
                     "Could not initialize document builder: " + e.toString());
         }
    }
    @Override
    public void run() {
        boolean lastResponseWasValid;
        Set<String> reviewableHITs = new HashSet<>();
        do {
            String response = getReviewableHITs();
            Document doc = docBuilder.parse(new InputSource(new StringReader(response)));
            lastResponseWasValid = responseIsValid(doc);

            if (lastResponseWasValid) {

                Set<String> hits = extractReviewableHITs(doc);
                reviewableHITs.addAll(hits);
                
            }
        } while (lastResponseWasValid && moreReviewableHITsAvailable);
        
        for (AMTTask t : amtActiveTasks) {
            if (t.isReviewable(reviewableHITs)) {
                stopPolling();
                getLastAssignments(); // Use same code as PollAnswersTimerTask
                callJobFinished();
            }
        }
    }
    private String getReviewableHITs() {
        Map<String, String> param = new HashMap<String, String>();
        param.put("Operation", "GetReviewableHITs");
        param.put("HITId", task.getHIT().getHITId());
        param.put("AssignmentStatus", "Submitted");
        param.put("PageSize", Integer.toString(PAGE_SIZE));

        return AMTCommunicator.sendGet(param);
    }

}

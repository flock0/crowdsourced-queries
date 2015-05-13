package crowdsourced.mturk.task;

import java.io.IOException;
import java.io.StringReader;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Handles the checking of whether all accepted assignments for the HIT are submitted.
 *
 * @author Florian Chlan
 */
public class PollFinishedTimerTask extends TimerTask {

    /**
     * The default number of HITs to fetch from one poll.
     */
    public static final int PAGE_SIZE = 100;
    /**
     * At what rate should we poll whether the HITs are finished?
     */
    public static final long POLLING_FINISHED_RATE_MILLISECONDS = 5 * 1000;
    /**
     * The task set to work on.
     */
    private AMTTaskSet taskSet;
    /**
     * The total number of pages of reviewable HITs for the most recent retrieval operation.
     */
    private int totalNumberOfPages;
    private DocumentBuilder docBuilder;

    public PollFinishedTimerTask(AMTTaskSet _taskSet) {
        this.taskSet = _taskSet;
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
        try {
            boolean lastResponseWasValid;
            int currentPageNumber = 1;
            /* Retrieve all pages of reviewable HITs one-by-one */
            Set<String> reviewableHITs = new HashSet<>();
            do {
                String response = getReviewableHITs(currentPageNumber);
                Document doc = docBuilder.parse(new InputSource(new StringReader(response)));
                lastResponseWasValid = responseIsValid(doc);

                if (lastResponseWasValid) {

                    Set<String> hits = extractReviewableHITs(doc);
                    reviewableHITs.addAll(hits);
                    currentPageNumber++;
                }
            } while (lastResponseWasValid && currentPageNumber <= totalNumberOfPages);

            /* Check if any of the active tasks is reviewable. This means, that they are finished. */
            for (AMTTask t : taskSet.getActiveTasks()) {
                if (reviewableHITs.contains(t.getHIT().getHITId())) {
                    t.finishTask();
                }
            }
        } catch (IOException | SAXException | XPathExpressionException | SignatureException ex) {
            System.out.println("Polling for reviewable HITs has failed: " + ex.getMessage());
        }
    }

    /**
     * Retrieves one page of reviewable HITs.
     * @param pageNumber The page number to retrieve.
     * @return The answer from AMT.
     */
    private String getReviewableHITs(int pageNumber) throws SignatureException, IOException {
        Map<String, String> param = new HashMap<String, String>();
        param.put("Operation", "GetReviewableHITs");
        param.put("Status", "Reviewable");
        param.put("PageSize", Integer.toString(PAGE_SIZE));
        param.put("PageNumber", Integer.toString(pageNumber));

        return AMTCommunicator.sendGet(param);
    }

    /**
     * Returns whether the response received from AMT is valid.
     * @param doc The reponse from AMT that will be evaluated.
     * @return True if the response is valid, false otherwise.
     */
    private boolean responseIsValid(Document doc) {

        XPath xPath = XPathFactory.newInstance().newXPath();

        try {
            String isValid = xPath.compile(
                    "/GetReviewableHITsResponse/GetReviewableHITsResult/Request/IsValid")
                    .evaluate(doc);
            return isValid.equals("True");
        } catch (XPathExpressionException e) {
            return false;
        }
    }

    /**
     * Extracts the individual HITIds from the document.
     * @param doc THE XML-document received by AMT.
     * @return A set of individual HITIds that are reviewable.
     */
    private Set<String> extractReviewableHITs(Document doc) throws XPathExpressionException {
        XPathFactory xPathFac = XPathFactory.newInstance();
        XPath xPath = xPathFac.newXPath();

        /* If there are more HITs than can fit on this page, fetch more after this round */
        String numResultsString = xPath
                .compile("/GetReviewableHITsResponse/GetReviewableHITsResult/NumResults")
                .evaluate(doc);
        String totalNumResultsString = xPath
                .compile("/GetReviewableHITsResponse/GetReviewableHITsResult/TotalNumResults")
                .evaluate(doc);
        int numResults = Integer.parseInt(numResultsString);
        int totalNumResults = Integer.parseInt(totalNumResultsString);
        totalNumberOfPages = (totalNumResults + PAGE_SIZE / 2 + 1) / PAGE_SIZE;

        Set<String> result = new HashSet<>();
        if (numResults > 0) {
            NodeList hits = (NodeList) xPath.compile(
                    "/GetReviewableHITsResponse/GetReviewableHITsResult/HIT")
                    .evaluate(doc, XPathConstants.NODESET);

            /* Go through all the reviewable HITs received */
            for (int i = 0; i < numResults; i++) {
                Node n = hits.item(i);
                result.add(xPath.compile("./HITId").evaluate(n));
            }
        }

        return result;
    }
}

package crowdsourced.mturk;

import java.io.IOException;
import java.io.StringReader;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * Handles the polling for and extraction of new answers.
 *
 * @author Florian Chlan
 */
public class PollAnswersTimerTask extends TimerTask {

    /**
     * The default number of assignments to fetch from one poll.
     */
    public static final int PAGE_SIZE = 100;
    public static final long POLLING_INITIAL_DELAY_MILLISECONDS = 3 * 1000; // How long should we wait after creating the HIT before we start polling?
    public static final long POLLING_RATE_MILLISECONDS = 5 * 1000; // At what rate should we poll for new answers?
    
    private DocumentBuilder docBuilder;
    private AMTTask task;
    private boolean moreAssignmentsAvailable;
    private boolean cancellationRequested = false;

    /**
     * Creates a new TimerTask that takes care of polling for new answers.
     *
     * @param _task
     *          The task that we want to poll for.
     */
    public PollAnswersTimerTask(AMTTask _task) {
        this.task = _task;
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
            do {
                String response = getAssignmentsForHIT();
                Document doc = docBuilder.parse(new InputSource(new StringReader(response)));
                lastResponseWasValid = responseIsValid(doc);

                if (lastResponseWasValid) {

                    List<Assignment> newAssignments = extractAssignments(doc);

                    if (newAssignments.size() > 0) {
                        List<Assignment> filteredAssignments = filterExisting(newAssignments);

                        addToSet(filteredAssignments);

                        task.getCallback().newAssignmentsReceived(filteredAssignments);
                        approveAssignments(newAssignments);
                        
                    }
                }
            } while (lastResponseWasValid && moreAssignmentsAvailable);

            if (receivedEnoughAssignments() || cancellationRequested) {
                this.cancel();
                task.getCallback().jobFinished();
                task.disposeJob();
            }
        } catch (IOException | SAXException | XPathExpressionException | SignatureException ex) {
            System.out.println(String.format("Polling of HIT %s failed: %s",
                    task.getHIT().getHITId(), ex.getMessage()));
        }
    }

    private boolean receivedEnoughAssignments() {
        return task.getAssignments().size() == task.getHIT().getMaxAssignments();
    }

    /**
     * Gets the submitted assignments of this task.getHIT().
     * @return The answer from AMT.
     * @throws SignatureException
     * @throws IOException
     */
    private String getAssignmentsForHIT() throws SignatureException, IOException {

        Map<String, String> param = new HashMap<String, String>();
        param.put("Operation", "GetAssignmentsForHIT");
        param.put("HITId", task.getHIT().getHITId());
        param.put("AssignmentStatus", "Submitted");
        param.put("PageSize", Integer.toString(PAGE_SIZE));

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
                    "/GetAssignmentsForHITResponse/GetAssignmentsForHITResult/Request/IsValid")
                    .evaluate(doc);
            return isValid.equals("True");
        } catch (XPathExpressionException e) {
            return false;
        }
    }

    /**
     * Extracts the individual assignments from the response.
     * @param doc The response from AMT.
     * @return A list of all newly submitted assignments for this task.getHIT().
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     */
    private List<Assignment> extractAssignments(Document doc)
            throws XPathExpressionException, IOException, SAXException {

        XPathFactory xPathFac = XPathFactory.newInstance();
        XPath xPath = xPathFac.newXPath();

        /* If there are more assignments than can fit on this page, fetch more after this round */
        String numResultsString = xPath
                .compile("/GetAssignmentsForHITResponse/GetAssignmentsForHITResult/NumResults")
                .evaluate(doc);
        String totalNumResultsString = xPath
                .compile("/GetAssignmentsForHITResponse/GetAssignmentsForHITResult/TotalNumResults")
                .evaluate(doc);
        int numResults = Integer.parseInt(numResultsString);
        moreAssignmentsAvailable = numResults < Integer.parseInt(totalNumResultsString);

        List<Assignment> results = new ArrayList<Assignment>();
        if (numResults > 0) {
            NodeList assignments = (NodeList) xPath.compile(
                    "/GetAssignmentsForHITResponse/GetAssignmentsForHITResult/Assignment")
                    .evaluate(doc, XPathConstants.NODESET);

            /* Go through all the assignments received */
            for (int i = 0; i < numResults; i++) {
                Assignment a = new Assignment(task.getHIT());
                Node n = assignments.item(i);

                a.setAssignmentID(xPath.compile("./AssignmentId").evaluate(n));

                //String unescapedAnswerXML = StringEscapeUtils.unescapeXml(xPath.compile("./Answer").evaluate(n));
                String unescapedAnswerXML = xPath.compile("./Answer").evaluate(n);
                Document answerXML = docBuilder.parse(new InputSource(new StringReader(unescapedAnswerXML)));
                NodeList answers = (NodeList) xPath.compile(
                        "./QuestionFormAnswers/Answer").evaluate(answerXML,
                                XPathConstants.NODESET);
                /* Go through all answers in this particular assignment */
                for (int j = 0; j < answers.getLength(); j++) {
                    String questionIdentifier = xPath.compile("./QuestionIdentifier").evaluate(answers.item(j));

                    if (!task.getHIT().getQuestionsMap().containsKey(questionIdentifier)) {
                        throw new IOException(
                                "This assignment contains an answer for which no matching question can be found");
                    }
                    Question q = task.getHIT().getQuestionsMap().get(questionIdentifier);

                    /* Parse the answer and add it to the assignment */
                    Answer answ = q.parseXMLAnswer(answers.item(j), xPath);
                    a.getAnswers().put(answ.getQuestion().getIdentifier(), answ);
                }

                results.add(a);
            }
        }

        return results;
    }

    /**
     * Filters out assignments that have been received by AMT but are not new to us.
     * @param newAssignments All assignments received by AMT during the last poll.
     * @return A list of only those assigments that are new to us.
     */
    private List<Assignment> filterExisting(List<Assignment> newAssignments) {
        List<Assignment> result = new ArrayList<Assignment>();
        for (Assignment a : newAssignments) {
            if (!task.getAssignments().contains(a)) {
                result.add(a);
            }
        }

        return result;
    }

    private void addToSet(List<Assignment> newAssignments) {
        task.getAssignments().addAll(newAssignments);
    }

    /**
     * Approves the passed assigments.
     * @param newAssignments The assigments that should be approved.
     */
    private void approveAssignments(List<Assignment> newAssignments) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("Operation", "ApproveAssignment");
        for (Assignment a : newAssignments) {
            param.put("AssignmentId", a.getAssignmentID());
            try {
                AMTCommunicator.sendGet(param);
            } catch (IOException | SignatureException e) {
                /* Nothing we can do about it. Just skip it this assignment for now */
            }
        }
    }

    public void cancelAfterNextRun() {
        cancellationRequested = true;
    }

}

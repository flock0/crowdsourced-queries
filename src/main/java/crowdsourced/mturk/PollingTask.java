package crowdsourced.mturk;

import java.io.IOException;
import java.io.StringReader;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Handles the polling for new answers.
 *
 * @author Florian Chlan
 */
public class PollingTask extends TimerTask {

    /**
     * The default number of assignments to fetch from one poll.
     */
    private static final int PAGE_SIZE = 100;

    private DocumentBuilder docBuilder;
    private final AnswerCallback callback;
    private final PendingJob job;
    private boolean moreAssignmentsAvailable;

    /**
     * Creates a new PollingTask
     *
     * @param jobArg
     *            The job object of the HIT.
     * @param callbackArg
     *            The object where new answers will be sent to.
     */
    public PollingTask(PendingJob jobArg, AnswerCallback callbackArg) {
        this.job = jobArg;
        this.callback = callbackArg;

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

                        callback.newAssignmentsReceived(filteredAssignments);
                        approveAssignments(newAssignments);
                    }
                }
            } while (lastResponseWasValid && moreAssignmentsAvailable);

        } catch (IOException | SAXException | XPathExpressionException | SignatureException ex) {
            System.out.println(String.format("Polling of HIT %s failed: %s",
                    job.getHIT().getHITId(), ex.getMessage()));
        }
    }

    private String getAssignmentsForHIT() throws SignatureException, IOException {

        Map<String, String> param = new HashMap<String, String>();
        param.put("Operation", "GetAssignmentsForHIT");
        param.put("HITId", job.getHIT().getHITId());
        param.put("AssignmentStatus", "Submitted");
        param.put("PageSize", Integer.toString(PAGE_SIZE));

        return AMTCommunicator.sendGet(param);
    }

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
                Assignment a = new Assignment(job.getHIT());
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

                    if (!job.getHIT().getQuestionsMap().containsKey(questionIdentifier)) {
                        throw new IOException(
                                "This assignment contains an answer for which no matching question can be found");
                    }
                    Question q = job.getHIT().getQuestionsMap().get(questionIdentifier);

                    /* Parse the answer and add it to the assignment */
                    Answer answ = q.parseXMLAnswer(answers.item(j), xPath);
                    a.getAnswers().put(answ.getQuestion().getIdentifier(), answ);
                }

                results.add(a);
            }
        }

        return results;
    }

    private List<Assignment> filterExisting(List<Assignment> newAssignments) {
        List<Assignment> result = new ArrayList<Assignment>();
        for (Assignment a : newAssignments) {
            if (!job.getAssignments().contains(a)) {
                result.add(a);
            }
        }

        return result;
    }

    private void addToSet(List<Assignment> newAssignments) {
        job.getAssignments().addAll(newAssignments);
    }

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

}

package crowdsourced.mturk;

import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * A Human-Intelligence-Task for Amazon Mechanical Turk
 *
 * @see http://docs.aws.amazon.com/AWSMechTurk/latest/AWSMturkAPI/
 *      ApiReference_CreateHITOperation.html
 */
public class HIT {

	/**
	 * The number of seconds after which a submitted assignment is approved
	 * automatically. Default set to two days.
	 */
	private static final int AUTO_APPROVAL_DELAY_IN_SECONDS = 60 * 60 * 24 * 2;

	/**
	 * A short and descriptive title of the HIT. Must not be longer than 128
	 * characters.
	 */
	private String title;

	/**
	 * A detailed description of the kind of task the HIT contains. Must not be
	 * longer than 2000 characters.
	 */
	private String description;

	/**
	 * The questions that will be posed to the worker.
	 */
	private List<Question> questions;

	/**
	 * An amount of time, in seconds, after which the HIT is no longer available
	 * for users to accept. After the lifetime of the HIT elapses, the HIT no
	 * longer appears in HIT searches, even if not all of the assignments for
	 * the HIT have been accepted. Must be >= 30 (30 seconds) and <= 31536000
	 * (365 days).
	 */
	private int lifetimeInSeconds;

	/**
	 * The number of times the HIT can be accepted and completed before the HIT
	 * becomes unavailable. Must be positive.
	 */
	private int maxAssignments;

	/**
	 * The amount of money we will pay to the worker. Must be positive.
	 */
	private float rewardInUSD;

	/**
	 * The amount of time that a Worker has to complete the HIT after accepting
	 * it. Must be >= 30 (30 seconds) and <= 31536000 (365 days).
	 */
	private int assignmentDurationInSeconds;

	/**
	 * One or more words or phrases that describe the HIT. This is used in
	 * searches to find HITs.
	 */
	private List<String> keywords;

	/**
	 * Creates a new HIT.
	 *
	 * @param operationName
	 *            The name of the operation.
	 * @param title
	 *            A short and descriptive title of the HIT. Must not be longer
	 *            than 128 characters.
	 * @param description
	 *            A detailed description of the kind of task the HIT contains.
	 *            Must not be longer than 2000 characters.
	 * @param questions
	 *            The questions that will be posed to the worker.
	 * @param lifetimeInSeconds
	 *            An amount of time, in seconds, after which the HIT is no
	 *            longer available for users to accept. Must be >= 30 (30
	 *            seconds) and <= 31536000 (365 days).
	 * @param maxAssignments
	 *            The number of times the HIT can be accepted and completed
	 *            before the HIT becomes unavailable. Must be positive.
	 * @param reward
	 *            The amount of time that a Worker has to complete the HIT after
	 *            accepting it. Must be >= 30 (30 seconds) and <= 31536000 (365
	 *            days).
	 * @param assignmentDurationInSeconds
	 *            The amount of time that a Worker has to complete the HIT after
	 *            accepting it. Must be >= 30 (30 seconds) and <= 31536000 (365
	 *            days).
	 * @param keywords
	 *            One or more words or phrases that describe the HIT. This is
	 *            used in searches to find HITs.
	 */
	public HIT(String titleArg,
					String descriptionArg, List<Question> questionsArg,
					int lifetimeInSecondsArg, int maxAssignmentsArg, float rewardInUSDArg,
					int assignmentDurationInSecondsArg, List<String> keywordsArg) {
		this.questions = questionsArg;
		this.lifetimeInSeconds = lifetimeInSecondsArg;
		this.maxAssignments = maxAssignmentsArg;
		this.title = titleArg;
		this.description = descriptionArg;
		this.rewardInUSD = rewardInUSDArg;
		this.assignmentDurationInSeconds = assignmentDurationInSecondsArg;
		this.keywords = keywordsArg;
	}

	/**
	 * Return a XML document corresponding to the QuestionForm for this HIT.
	 * @return The QuestionForm representation of this HIT.
	 */
	public Document asXMLDocument() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Could not initialize document builder: "
					+ e.toString());
		}
		Document doc = docBuilder.newDocument();

		Element questionForm = doc.createElement("QuestionForm");
		questionForm.setAttribute("xmlns", "http://mechanicalturk.amazonaws.com/AWSMechanicalTurk/2014-08-15/AWSMechanicalTurkRequester.wsdl");

		for (Question q: this.questions) {
			Element question = q.asXMLElement(doc);
			doc.appendChild(question);
		}

		return doc;
	}

	/**
	 * @return the autoApprovalDelayInSeconds
	 */
	public static int getAutoApprovalDelayInSeconds() {
		return AUTO_APPROVAL_DELAY_IN_SECONDS;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the questions
	 */
	public List<Question> getQuestions() {
		return questions;
	}

	/**
	 * @return the lifetimeInSeconds
	 */
	public int getLifetimeInSeconds() {
		return lifetimeInSeconds;
	}

	/**
	 * @return the maxAssignments
	 */
	public int getMaxAssignments() {
		return maxAssignments;
	}

	/**
	 * @return the rewardInUSD
	 */
	public float getRewardInUSD() {
		return rewardInUSD;
	}

	/**
	 * @return the assignmentDurationInSeconds
	 */
	public int getAssignmentDurationInSeconds() {
		return assignmentDurationInSeconds;
	}

	/**
	 * @return the keywords
	 */
	public List<String> getKeywords() {
		return keywords;
	}


}

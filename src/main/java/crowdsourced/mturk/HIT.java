package crowdsourced.mturk;

import java.util.List;

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
	public HIT(String title,
			String description, List<Question> questions,
			int lifetimeInSeconds, int maxAssignments, float rewardInUSD,
			int assignmentDurationInSeconds, List<String> keywords) {
		this.questions = questions;
		this.lifetimeInSeconds = lifetimeInSeconds;
		this.maxAssignments = maxAssignments;
		this.title = title;
		this.description = description;
		this.rewardInUSD = rewardInUSD;
		this.assignmentDurationInSeconds = assignmentDurationInSeconds;
		this.keywords = keywords;
	}

}

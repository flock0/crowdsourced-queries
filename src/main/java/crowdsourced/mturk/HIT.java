package crowdsourced.mturk;

import java.util.List;

/**
 * A Human-Intelligence-Task for Amazon Mechanical Turk
 * @see http://docs.aws.amazon.com/AWSMechTurk/latest/AWSMturkAPI/ApiReference_CreateHITOperation.html
 */
public class HIT {

	private String operationName;
	private List<Question> questions;
	private int lifetimeInSeconds;
	private int maxAssignments;
	private String title;
	private String description;
	private float reward;
	private int assignmentDurationInSeconds;
	private List<String> keywords;
	private String requesterAnnotation;
	
	protected HIT(String operationName, List<Question> questions,
			int lifetimeInSeconds, int maxAssignments, String title,
			String description, float reward, int assignmentDurationInSeconds,
			List<String> keywords, String requesterAnnotation) {
		super();
		this.operationName = operationName;
		this.questions = questions;
		this.lifetimeInSeconds = lifetimeInSeconds;
		this.maxAssignments = maxAssignments;
		this.title = title;
		this.description = description;
		this.reward = reward;
		this.assignmentDurationInSeconds = assignmentDurationInSeconds;
		this.keywords = keywords;
		this.requesterAnnotation = requesterAnnotation;
	}
	
	
}

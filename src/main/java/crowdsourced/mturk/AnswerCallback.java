package crowdsourced.mturk;

import java.util.List;

/**
 * Contains methods to indicate 
 * @author Florian Chlan
 *
 */
public interface AnswerCallback {
	
	/**
	 * Invoked when new answers have been received.
	 * @param newAnswers A list of new answers.
	 */
	public void answersReceived(List<Answer> newAnswers);
	
	/**
	 * Invoked when the job is finished an no further answer can be expected for this HIT. 
	 */
	public void jobFinished();
	
	/**
	 * Invoked when an error occured and the HIT can no longer be processed.
	 */
	public void errorOccured();
}

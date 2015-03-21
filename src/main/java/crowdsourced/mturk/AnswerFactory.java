package crowdsourced.mturk;
import java.util.List;

import javax.xml.bind.Element;

public class AnswerFactory {

	 public static List<Answer> extractAnswers(List<Element> xmlElement) {
		//TODO: Not sure whether we should pass the <Assignment>-elements or the <Answer>-elements
		// (see http://docs.aws.amazon.com/AWSMechTurk/latest/AWSMturkAPI/ApiReference_GetAssignmentsForHITOperation.html)
		throw new UnsupportedOperationException("Not yet implemented");
	}
}

package crowdsourced.mturk;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A question that accepts a single URL.
 */
public class URLQuestion extends StringQuestion {

	private static final String JAVASCRIPT_URL_REGEX = "@(https?|ftp)://(-\\.)?([^\\s/?\\.#-]+\\.?)+(/[^\\s]*)?$@iS";
	protected URLQuestion(String identifier, String name, String content) {
		super(identifier, name, content);
		setDefaultText("https://");
		setSuggestedLineCount(1);
	}
	@Override
	public Element asXMLElement(Document doc) {
		/*
		 * TODO:
		 * Either insert the Regex-Constraint into the result of the super.asXMLElement call
		 * or duplicate the code from the base class and make the variables in StringQuestion protected.
		 * Not sure which way is the best.
		 */
		throw new UnsupportedOperationException("Not yet implemented");
	}

}

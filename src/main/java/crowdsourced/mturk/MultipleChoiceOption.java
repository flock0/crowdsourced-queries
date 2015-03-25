package crowdsourced.mturk;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A single option for a multiple-choice question.
 */
public class MultipleChoiceOption {
	private String identifier;
	private String text;
	
	/**
	 * Create a new multiple-choice option object
	 * @param identifier The unique id of the option.
	 * @param text The text that will be displayed to the worker.
	 */
	public MultipleChoiceOption(String identifier, String text) {
        super();
		this.identifier = identifier;
		this.text = text;
	}
	
	/**
     * Creates an XML element for the option that can be used in the request to AMT.
     * @param doc The document to which the returned element will later be
     *   added. (The caller is responsible for the addition of the element to
     *   the document.)
     * @return A <Question>-XML-Element with all the properties set accordingly.
     */
	public Element asXMLElement(Document doc) {
	    Element selection = doc.createElement("Selection");
	    
	    Element id = doc.createElement("SelectionIdentifier");
        id.appendChild(doc.createTextNode(this.identifier));
	    
        Element text = doc.createElement("Text");
	    text.appendChild(doc.createTextNode(this.text));
	    
	    selection.appendChild(id);
	    selection.appendChild(text);
	    
        return selection;
    }
	
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	
}

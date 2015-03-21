package crowdsourced.mturk;

import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * The class dedicated to direct communications with AMT
 * @author simon
 *
 */
public class AMTCommunicator {

	/**
	 * Checks if the current account has a balance superior or equal to the passed amount
	 * @param amount the minimum amount to have on the account
	 * @return true if balance >= amount, false elsewhere.
	 */
	protected boolean checkBalance(float amount) {
		return false;
	}

	/**
	 * Converts an XML Document to a string
	 * @param document the XML Document to convert
	 * @return a string corresponding to the serialization of the XML document
	 */
	private static String convertXMLToString(Document document)    {
	    DOMImplementationLS domImplementation = (DOMImplementationLS) document.getImplementation();
	    LSSerializer lsSerializer = domImplementation.createLSSerializer();
	    return lsSerializer.writeToString(document);
	}

}

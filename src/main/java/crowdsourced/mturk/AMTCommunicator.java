package crowdsourced.mturk;

import net.iharder.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.Timestamp;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * The class dedicated to direct communications with AMT
 * @author simon
 *
 */
public class AMTCommunicator {


	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

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
	private String convertXMLToString(Document document)    {
	    DOMImplementationLS domImplementation = (DOMImplementationLS) document.getImplementation();
	    LSSerializer lsSerializer = domImplementation.createLSSerializer();
	    return lsSerializer.writeToString(document);
	}

    /**
     * Computes RFC 2104-compliant HMAC signature.
     *
     * @param data
     *     The data to be signed.
     * @param key
     *     The signing key.
     * @return
     *     The Base64-encoded RFC 2104-compliant HMAC signature.
     * @throws
     *     java.security.SignatureException when signature generation fails
     */
    private String calculateSignature(String data, String key)
        throws java.security.SignatureException {
        String result;
        try {
            // get an hmac_sha1 key from the raw key bytes
        	SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);

            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());

            // base64-encode the hmac
            result = encodeBase64(rawHmac);
        } catch (GeneralSecurityException e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
        return result;
    }

    /**
     * Performs base64-encoding of input bytes.
     *
     * @param rawData
     *      Array of bytes to be encoded.
     * @return
     *      The base64-encoded string representation of rawData.
     */
    private String encodeBase64(byte[] rawData) {
        return Base64.encodeBytes(rawData);
    }

    /*private String getTimestamp() {
    	java.util.Date date = new java.util.Date();

    	//We get a string with format yyyy-mm-dd hh:mm:ss.fffffffff format
   	 	//String time = new Timestamp(date.getTime()).toString();

   	 	//We are expecting
    }*/

}

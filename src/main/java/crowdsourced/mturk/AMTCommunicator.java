package crowdsourced.mturk;

import net.iharder.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * The class dedicated to direct communications with AMT
 * @author Simon Rodriguez
 *
 */
public class AMTCommunicator {

	//DO NOT STORE THE CREDENTIALS WHEN PUSHING
	private static final String ACCESS_KEY_ID = "";
	private static final String ACCESS_KEY_SECRET_ID = "";
	//DO NOT STORE THE CREDENTIALS WHEN PUSHING

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	private static final String USER_AGENT = "Mozilla/5.0";
	private static final String AMT_URL = "https://mechanicalturk.amazonaws.com";
	// can use "https://mechanicalturk.sandbox.amazonaws.com"
	private static final String AMT_REQUEST_BASE_URL = AMT_URL
					+ "/?Service=AWSMechanicalTurkRequester"
					+ "&AWSAccessKeyId=" + ACCESS_KEY_ID
					+ "&Version=2014-08-15";

	public static void main(String[] args) {

	}

	/**
	 * Sends a REST GET request using the default base URL and with the parameters appended.
	 * @param parameters A map of parameters with the key being the
	 *  descriptor of the parameter and the value being the value.
	 * @return The response from the GET request.
	 * @throws IOException
	 * @throws SignatureException
	 */
	public static String sendGet(Map<String, String> parameters) throws IOException, SignatureException {
		String operation = null;
		String service = "AWSMechanicalTurkRequester";
		String timestamp = getTimestamp();
		StringBuffer url = new StringBuffer();
		url.append(AMT_REQUEST_BASE_URL);
		for (String key : parameters.keySet()) {
			url.append(String.format("&%s=%s", key, encodeUrl(parameters.get(key))));
			if (key.equals("Operation")) {
				operation = parameters.get(key);
			}
		}
		if (operation == null) {
			throw new IOException("Request does not contain a Operation-prameter");
		}

		url.append("&Timestamp=");
		url.append(timestamp);
		url.append("&Signature=");
		url.append(encodeUrl(calculateSignature(service + operation + timestamp, ACCESS_KEY_SECRET_ID)));

		return sendGet(url.toString());
	}

	/**
	 * Sends a REST GET request with the passed URL.
	 * @param url the destination URL
	 * @return The response from the GET request.
	 * @throws IOException
	 */
	private static String sendGet(String url) throws IOException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}


	/**
	 * Checks if the current account has a balance superior or equal to the passed amount
	 * @param amount the minimum amount to have on the account
	 * @return true if balance >= amount, false elsewhere.
	 */
	protected static boolean checkBalance(float amount) {
		try {
			String service = "AWSMechanicalTurkRequester";
			String operation = "GetAccountBalance";
			String timestamp = getTimestamp();
			String ur = AMT_URL + "/?Service=" + service
							+ "&AWSAccessKeyId=" + ACCESS_KEY_ID
							+ "&Version=2014-08-15"
							+ "&Operation=" + encodeUrl(operation)
							+ "&Signature="
							+ encodeUrl(calculateSignature(service + operation + timestamp, ACCESS_KEY_SECRET_ID))
							+ "&Timestamp=" + encodeUrl(timestamp);
			System.out.println(service + operation + timestamp);
			System.out.println(sendGet(ur));

		} catch  (IOException e) {
			System.out.println("The GET request couldn't be sent.");
		} catch (SignatureException e) {
			System.out.println("The signature couldn't be generated properly.");
		}
		return false;
	}


	/**
	 * Sends the passed HIT to
	 * @param hit the HIT to send
	 * @param callback The object where new answers will be sent to.
	 */
	protected static void sendHIT(HIT hit, AnswerCallback callback) {
		String serial = convertXMLToString(hit.asXMLDocument());
		//Need securisation
		serial = serial.substring(serial.indexOf("\n") + 1);
		System.out.println(serial);
		/*No need to XML escape the string when using REST,
		see [http://docs.aws.amazon.com/AWSMechTurk/latest/AWSMturkAPI/ApiReference_XMLParameterValuesArticle.html]*/
		try {
			String urlEncodedQuestion = encodeUrl(serial);
			String service = "AWSMechanicalTurkRequester";
			String operation = "CreateHIT";
			String timestamp = getTimestamp();
			//The following need to be URL encoded (twice? ie with %20 for spaces)
			String title = encodeUrl(hit.getTitle());
			String description = encodeUrl(hit.getDescription());
			String rewardAmount = encodeUrl(Float.toString(hit.getRewardInUSD()));
			String durationInSeconds = encodeUrl(Integer.toString(hit.getAssignmentDurationInSeconds()));
			String lifetimeInSeconds = encodeUrl(Integer.toString(hit.getLifetimeInSeconds()));
			String tempKeywords = Arrays.toString(hit.getKeywords().toArray());
			String keywords = "default";
			if (tempKeywords.length() > 2) {
				keywords = encodeUrl(tempKeywords.substring(1, tempKeywords.length() - 1));
			}

			String url = AMT_URL + "/?Service=" + service
							+ "&AWSAccessKeyId=" + encodeUrl(ACCESS_KEY_ID)
							+ "&Version=2014-08-15"
							+ "&Operation=" + encodeUrl(operation)
							+ "&Signature="
							+ encodeUrl(calculateSignature(service + operation + timestamp, ACCESS_KEY_SECRET_ID))
							+ "&Timestamp=" + encodeUrl(timestamp)
							+ "&Title=" + title
							+ "&Description=" + description
							+ "&Reward.1.Amount=" + rewardAmount
							+ "&Reward.1.CurrencyCode=USD"
							+ "&Question=" + urlEncodedQuestion
							+ "&AssignmentDurationInSeconds=" + durationInSeconds
							+ "&LifetimeInSeconds=" + lifetimeInSeconds
							+ "&Keywords=" + keywords;

			String response = sendGet(url);
			System.out.println(response);
			int start = response.indexOf("<HITId>") + "<HITId>".length();
			int stop = response.indexOf("</HITId>");
			String hitId = response.substring(start, stop);
			System.out.println("HIT ID: " + hitId);
			hit.setHITId(hitId);
			if (callback != null) {
				callback.jobFinished();
			}

		} catch  (IOException e) {
			System.out.println("The GET request couldn't be sent.");
			if (callback != null) {
				callback.errorOccured();
			}
		} catch (SignatureException e) {
			System.out.println("The signature couldn't be generated properly.");
			if (callback != null) {
				callback.errorOccured();
			}
		}
	}


	/**
	 * Generates the timestamp corresponding to the current time "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
	 * @return a string with format "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
	 */
	private static String getTimestamp() {
		Date currentTime = new Date();
		SimpleDateFormat gmtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	    gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return gmtFormat.format(currentTime.getTime());
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
    private static String calculateSignature(String data, String key)
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
	 * Converts a XML Document to a string
	 * @param document the XML Document to convert
	 * @return a string corresponding to the serialization of the XML document
	 */
	private static String convertXMLToString(Document document)    {
	    DOMImplementationLS domImplementation = (DOMImplementationLS) document.getImplementation();
	    LSSerializer lsSerializer = domImplementation.createLSSerializer();
	    return lsSerializer.writeToString(document);
	}


	/**
	 * Parses a String to generate a XML document
	 * @param xml the string to parse
	 * @return a XML document if the string was parsed successfully
	 * @throws Exception
	 */
	public static Document loadXMLFromString(String xml) throws Exception {
		/*DOMImplementationRegistry registry;
		try {
			registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
			LSParser parser = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, "http://www.w3.org/TR/REC-xml");
			LSInput input = impl.createLSInput();
			input.setEncoding("UTF-8");
			input.setStringData(xml);
		    return parser.parse(input);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return null;
	}

    /**
     * Performs base64-encoding of input bytes.
     *
     * @param rawData
     *      Array of bytes to be encoded.
     * @return
     *      The base64-encoded string representation of rawData.
     */
    private static String encodeBase64(byte[] rawData) {
        return Base64.encodeBytes(rawData);
    }



    /**
     * Encodes a string to the URL standard format
     * @param question the string to encode
     * @return an URL encoded string
     * @throws UnsupportedEncodingException
     */
    private static String encodeUrl(String question) throws UnsupportedEncodingException {
		return  URLEncoder.encode(question, "UTF-8");
	}


}

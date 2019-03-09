package il.co.pelephone.musix.comm;

import java.util.HashMap;

public interface IMXCommCallback {

	
	/*
	 * After all the response date is received and parsed, 
	 * the class should call the caller’s
	* responseReceived method and pass the XML response
	* */
	void responseReceived(String XMLResponse);
	
	/*
	 * In case an error occurred when sending request 
	 * or receiving the response the class will
	 * notify the caller of the error
	 * */
	void responseFailed(MXCommError error);
	
	
	/*
	 * When images are received in the response 
	 * the class will send them to the caller 
	 * using this method.
	 * */
	void imagesReceived(HashMap images);
	
	
}

package il.co.pelephone.musix.comm;


import org.apache.http.HttpResponse;

public class MXCallbackWrapper implements Runnable {
 
	private MXHttpResponseListener callbackActivity;
	private HttpResponse response;
 
	public MXCallbackWrapper(MXHttpResponseListener callbackActivity) {
		this.callbackActivity = callbackActivity;
	}
 
	public void run() {
		callbackActivity.onResponseReceived(response);
	}
 
	public void setResponse(HttpResponse response) {
		this.response = response;
	} 
}
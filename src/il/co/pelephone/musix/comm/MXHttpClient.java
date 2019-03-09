package il.co.pelephone.musix.comm;

import android.os.Handler;

import org.apache.http.HttpRequest;
 
public class MXHttpClient {
 
	public static void sendRequest(final HttpRequest request, MXHttpResponseListener callback) {
		(new MXAsynchronousSender(request, new Handler(),
				new MXCallbackWrapper(callback))).start();
	}
 
}
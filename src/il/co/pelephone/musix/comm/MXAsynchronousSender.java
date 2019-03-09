package il.co.pelephone.musix.comm;


import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
 
import android.os.Handler;
import android.util.Log;
 
public class MXAsynchronousSender extends Thread {
	private static final  String LOGTAG="MXAsynchronousSender.";
	private final DefaultHttpClient httpClient =
		new DefaultHttpClient();
 
	private HttpRequest  request;
	private Handler handler;
	private MXCallbackWrapper wrapper;
 
	protected MXAsynchronousSender(HttpRequest request,
			Handler handler, MXCallbackWrapper wrapper) {
		this.request = request;
		this.handler = handler;
		this.wrapper = wrapper;
	}
 
	public void run() {
		boolean isError=false;
		try {
			final HttpResponse response;
			synchronized (httpClient) {
				response = getClient().execute((HttpUriRequest) request);
			}
			// process response
			wrapper.setResponse(response);
			handler.post(wrapper);
		} catch (ClientProtocolException e) {
			isError=true;
			Log.e(LOGTAG, "ClientProtocolException" , e);
		} catch (IOException e) {
			isError=true;
			Log.e(LOGTAG, "IOException" , e);
		}  catch (Exception e) {
			isError=true;
			Log.e(LOGTAG, "exception" , e);
		}
		
		if (isError)
		{
			wrapper.setResponse(null);
			handler.post(wrapper);			
		}
	}
 
	private HttpClient getClient() {
		return httpClient;
	}
 
}
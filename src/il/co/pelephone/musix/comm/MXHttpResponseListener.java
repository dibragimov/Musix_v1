package il.co.pelephone.musix.comm;

import org.apache.http.HttpResponse;

public interface MXHttpResponseListener {
	public void onResponseReceived(HttpResponse response);
}

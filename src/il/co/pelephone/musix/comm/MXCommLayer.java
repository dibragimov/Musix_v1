package il.co.pelephone.musix.comm;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import android.util.Log;


/*
 * The response data received contains more than just the XML itself.
 *  Following is the response format:
 * [data-item][data-item][data-item]000
 * 
 * Each data item in the response will be in the following format:
 * <data name length><data name><data length><data>
 * 
 * Example of a response:
 * <binary 3>XML<binary 1000><XML content>000
 * 
 * */
public class MXCommLayer implements MXHttpResponseListener{	
	private static final String LOGTAG = "MXCommLayer";
	
	public static final String DEV_MSISDN ="BALORA";
	public static final String DEV_IMSI ="BALORA";
	public static final String RESPONSE_DATA_ENDING = "000";
	public static final int DATA_NAME_LENGTH_SIZE = 3;
	public static final int DATA_LENGTH_SIZE = 8;
	public static final int MAX_REQUESTS=50;
	public static final int READ_BUFFER_SIZE=128;
	
	public static final String TRIPLAY_URL_DEV = "http://dev1.tmusic.triplay-inc.com";
	public static final String TRIPLAY_URL_TEST = "http://10.171.9.40";
	public static final String TRIPLAY_URL_PROD = "http://gw.musix.co.il";

		
	public static final String HTTP_REQUEST__URI_SYNC = "/api/mobileapi/syncService";
	public static final String HTTP_REQUEST__URI_STORE = "/api/mobileapi/storeService";
	public static final String POST_XML_DATA_PARAMETER_NAME = "data";
	
	private static String url;		
	private static  ArrayList <IMXCommCallback> RequestQueue;
	
	private byte env;
	
	private  MXCommLayer()	 {
		
		this.env=MXEnv.getEnv();
		setUrl();
		RequestQueue=new ArrayList <IMXCommCallback>();
	};

	
	/*Singleton*/
	private static class MXCommLayerHolder {
	    private static final MXCommLayer INSTANCE = new MXCommLayer();
	  }

	  public static MXCommLayer getInstance() {
		  Log.d(LOGTAG, "getInstance");
	    return MXCommLayerHolder.INSTANCE;
	  }

      public Object clone() throws CloneNotSupportedException {
          throw new CloneNotSupportedException();
        }
	
      
      
      
      
      
      
	//Send the request to Musix’s server. If there are any pending requests cancel them first.
	public void SendRequest(final MXXMLRequest mx_request, IMXCommCallback mx_callback)
	{
		String url = this.url;
		if(mx_request instanceof MXStoreRequest){
			url = getStoreServiceUrl();
			Log.d(LOGTAG, "Request is type of MXStoreRequest: "+mx_request.getClass().getName());
		}
		Log.d(LOGTAG, "SendRequest to "+url);
		if(RequestQueue.size()>=MAX_REQUESTS)
		{
			Log.e(LOGTAG, "Too many basic connections");
			mx_callback.responseFailed(MXCommError.MxCommErrorNetwork);
			return;
		}

		
		String dataString=buildHttpRequestData(mx_request);
		if (dataString==null || dataString.length()<=0)
		{
			Log.e(LOGTAG, "dataString is empty");
			mx_callback.responseFailed(MXCommError.MxCommErrorInvalidData);
			return;
		}
		
		try
		{  			
			HttpRequest request= new HttpPost(url);  
			HttpPost httpPost = new HttpPost(url);  
			HttpParams params = new BasicHttpParams();             
			//params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
			//params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
			//httpPost.setParams(params);
			
			httpPost.setHeader("CONTENT-TYPE", "application/x-www-form-urlencoded; charset=utf-8");
			
			
			//add headers
			if (getEnv()==MXEnv.ENV_DEV)	{
				httpPost.setHeader("MSISDN", DEV_MSISDN);
				httpPost.setHeader("IMSI", DEV_IMSI);  
			}
			else
				httpPost.setHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 1.5; iw-il; GT-I5700");

			// Add your data  
			//List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();  
		//	nameValuePairs.add(new BasicNameValuePair( POST_XML_DATA_PARAMETER_NAME, dataString));
			//httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  

			StringEntity se=new StringEntity(POST_XML_DATA_PARAMETER_NAME+"="+ dataString , "UTF-8");
			httpPost.setEntity(se);  

			String str=httpPost.toString();
			Log.d(LOGTAG, "httpPost"+str);

			RequestQueue.add(mx_callback);
			MXHttpClient.sendRequest(httpPost, this);
		
		} catch (IOException e) {  
			Log.e(LOGTAG, "IOException: ", e);
			e.printStackTrace(); 
			mx_callback.responseFailed(MXCommError.MxCommErrorNetwork);
		}  
		catch (Exception e) 
		{  	         
			Log.e(LOGTAG, "Exception: ", e);
			e.printStackTrace(); 
			mx_callback.responseFailed(MXCommError.MxCommErrorNetwork);
		}  	
		Log.d(LOGTAG, "after SendRequest");
	}
	
	



	public String buildHttpRequestData(final MXXMLRequest mx_request)
	{
		String XML=null;
		String POSTText = null; 
		
		try {
			XML=mx_request.getXML();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
				
		if (XML!=null && XML.length()>0)
		{
			try {
				POSTText =URLEncoder.encode(XML, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return null;
			} 
		}       		
      
		
		return POSTText;
	}
	
	
	
	
	public void onResponseReceived(HttpResponse response)
	{		
		Log.d(LOGTAG, "onResponseReceived");
		
		IMXCommCallback caller=RequestQueue.get(0);
		
		if (response==null)	{			
			caller.responseFailed(MXCommError.MxCommErrorNetwork);
		}
		else	{ /* If the status code is 200 ok  */
			int status=response.getStatusLine().getStatusCode();
	        if(status == 200)  
	        {
	        	Log.d(LOGTAG, "onResponseReceived status == 200");
	        	ParseResponse(response , caller);	          
	         } 
	         else   { 
	        	  Log.e(LOGTAG, "Http return status code:"+ status+" "+response.getStatusLine().getReasonPhrase());
	        	  caller.responseFailed(MXCommError.MxCommErrorNetwork);
	         } 					
		}
		
		RequestQueue.remove(0);		
	}
	
	
	
	
	
	public boolean ParseResponse(HttpResponse response ,IMXCommCallback caller)
	{
		boolean bRes=true;
		InputStream inContent=null;
		Log.d(LOGTAG, "ParseResponse");
		
		
		  /* Read  the data  */  
    	try{
    		HttpEntity entity = response.getEntity();
    		inContent = entity.getContent(); 
    		
        	String XMLResponse=parseForXmlData(inContent);
        	
        	if(XMLResponse!=null && XMLResponse.length()>0){
        		Log.d(LOGTAG, "XMLResponse"+XMLResponse);
        		caller.responseReceived(XMLResponse);	           			            
        	}
        	else
        	{
        		Log.e(LOGTAG, "empty XMLResponse");		        		
        		caller.responseFailed(MXCommError.MXCommErrorInvalidXML);
        		bRes=false;
        //	
        	bRes=parseForImageData(inContent);
        	}
        	
        	
    	} catch (IOException e) {  
    		Log.e(LOGTAG, "IOException: ", e);
    		e.printStackTrace(); 
    		bRes=false;
    		caller.responseFailed(MXCommError.MxCommErrorNetwork);    		
    	}  
    	catch (Exception e) 
    	{  	         
    		Log.e(LOGTAG, "Exception: ", e);
    		e.printStackTrace(); 
    		bRes=false;
    		caller.responseFailed(MXCommError.MxCommErrorNetwork);
    	} 
    	 finally {
 			try {
 				inContent.close();
 			} catch (IOException e) {
 	    		Log.e(LOGTAG, "IOException: ", e);
 	    	}			
 		}
		
		return bRes;
	}
	
	
	
	
	/*
	 * first read xml , then images
	 * */
	private String parseForXmlData(InputStream is)
	{	
	   int offset=0;
	   String dataname=null;
	   
	   Log.d(LOGTAG, "parseForXmlData");
	   
	   if ((dataname=getData(is , 0 , DATA_NAME_LENGTH_SIZE))==null) {
		   return null;		   
	   }
	   
	   String xml=null;	
	   
	   if ((xml=getData(is , offset , DATA_LENGTH_SIZE))==null)  {
		   return null;		   
	   }
		
		return xml;
	}	

	
	

	public String getData(InputStream is , int offset , int headerLength)
	{
		String data=null;
		int bytes=0;
	
		Log.d(LOGTAG, "getData offset="+offset+",headerlen="+headerLength);
		
		try {
			byte[] dataLengthBuf = new byte[headerLength];	
			is.skip(offset);
			
			if ((bytes=readBytes(is ,dataLengthBuf ,0 , headerLength))!=headerLength){	
				Log.e(LOGTAG, "getXmlData headerLength failed ");	   		    
			}
	
			String dataLengthStr = new String(dataLengthBuf);			
			int dataLenInt = Integer.parseInt(dataLengthStr);
			Log.d(LOGTAG, "after read dataLength  len="+dataLenInt);

			if (dataLenInt>0)
			{
				byte[] dataBuf = new byte[dataLenInt];
				
				Log.d(LOGTAG, "before read " + dataLenInt + "Bytes");
			//	in.skip(offset+headerLength);
				
				 bytes=readBytes(is ,dataBuf , 0 , dataLenInt );
	
				if (bytes==dataLenInt) {
					data=new String(dataBuf , "UTF-8");
					Log.d(LOGTAG, "after read data:"+data);

				}		
			}
	
		} catch (IOException e) {
    		Log.e(LOGTAG, "IOException: ", e);
    		return null;

		}
		Log.d(LOGTAG, "before return from getData");

		return data;
	}

	

	/*
	 * first read xml , then images
	 * */
	private boolean parseForImageData(InputStream is)
	{	
	/*   int offset=0;
	   String dataname=null;
	   
	   ListArray <>
	   
	   while(true){
	   if ((dataname=getImageData(is , 0 , DATA_NAME_LENGTH_SIZE))==null) {
		   return null;		   
	   }
	   else
		   offset= dataname.length()+DATA_NAME_LENGTH_SIZE;	   
	   
	   String xml=null;	
	   
	   if ((xml=getData(is , offset , DATA_LENGTH_SIZE))==null)  {
		   return null;		   
	   }
	   else
		   offset+= xml.length()+DATA_LENGTH_SIZE;	   
	   
	   }
		*/
		return true;
	}	
	
	
	
	
	
	private int readBytes (InputStream in, byte [] buf, int offset, int len)
		throws IOException
	{
		int readLen = 0;
		while (readLen <len)
		{
			try {
				int offSize = in.read (buf, offset + readLen, len - readLen);
				if (offSize == 0 || offSize == -1) {
					break;
				}
				readLen = readLen + offSize;
			} catch (IOException e){
				break;
			}
		}
		return readLen;
	}



	public void setUrl()
	{
		if ( env == MXEnv.ENV_DEV )
		{
			url=TRIPLAY_URL_DEV+HTTP_REQUEST__URI_SYNC;
		}
		else if ( env == MXEnv.ENV_TEST )
		{
			url=TRIPLAY_URL_TEST+HTTP_REQUEST__URI_SYNC;			
		}
		else
			url=TRIPLAY_URL_PROD+HTTP_REQUEST__URI_SYNC;		
	}
	
	private String getStoreServiceUrl(){
		if ( env == MXEnv.ENV_DEV )
		{
			return TRIPLAY_URL_DEV+HTTP_REQUEST__URI_STORE;
		}
		else if ( env == MXEnv.ENV_TEST )
		{
			return TRIPLAY_URL_TEST+HTTP_REQUEST__URI_STORE;			
		}
		else
			return TRIPLAY_URL_PROD+HTTP_REQUEST__URI_STORE;
	}

	public void setEnv(byte env) {
		this.env = env;
	}

	public byte getEnv() {
		return env;
		
	}
	
}
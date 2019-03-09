package il.co.pelephone.musix.comm;

import il.co.pelephone.musix.comm.IMXLoginCallback.LoginFailureType;
import il.co.pelephone.musix.comm.IMXLoginCallback.LoginSuccessType;
import il.co.pelephone.musix.comm.MXSettings.MXAppMode;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;


public class MXLogin implements IMXCommCallback {
	
	public static final String LOGTAG="MXLogin";
	
	/*constant definitions*/
	public static final String LOGIN_STATUS_SUCCESS = "SUCCESS";
	public static final String LOGIN_STATUS_SUCCESS_DISPLAY_TERMS = "SUCCESS_DISPLAY_TERMS";
	public static final String LOGIN_STATUS_FAILED_DUE_ROAMING = "FAILED_DUE_ROAMING";
	public static final String LOGIN_STATUS_USER_IS_BLOCKED = "USER_IS_BLOCKED";
	public static final String LOGIN_STATUS_USER_IS_PREPAID = "USER_IS_PREPAID";
	public static final String LOGIN_STATUS_REGISTER_TO_WEB_ONLY = "REGISTER_TO_WEB_ONLY";
	public static final String LOGIN_STATUS_REGISTER_TO_APPLICATION_FIRST = "REGISTER_TO_APPLICATION_FIRST";
	public static final String LOGIN_STATUS_WRONG_ACCESS_POINT = "WRONG_ACCESS_POINT";
		
	private static final String RESPONSE_TAG = "response";
	private static final String LOGIN_RESPONSE_TAG = "loginResonse";
	private static final String STATUS_TAG = "status";	
	private static final String UPGRADE_URL_TAGE = "upgradeURL";
	private static final String SYNC_TAG = "sync";
	
	private static final String LAST_LOGIN_ATTR = "milliseconds";
	private static final String 	ENCRIPTION_KEY_ATTR = "encryptionKey";
	private static final String 	IS_ENCRIPTED_ATTR = "isEncrypted";
	private static final String 	MANDATORY_ATTR = "mandatory";

			
	public static final int GRACE_PERIOD=14;
	
		
	/*private members*/	
	private boolean isRegisterAccepted;
	private boolean isEncripted;
	private String upgradeURL;	
	private String timestamp;
	private String encriptionKey;
	private String mStatus;
	private String XMLResponse;	
	private MXLoginRequest mRequest;	
	private IMXLoginCallback mCaller;	
	private MXCommLayer mSender;	
	private boolean responseSent;			
	private boolean isMandatoryUpd;
	
	/*Initializes the object and sets the callback object.*/
	public MXLogin (IMXLoginCallback caller)
	{
		Log.d(LOGTAG, "in constructor");		
		this.mCaller=caller;
		isRegisterAccepted=false;
		upgradeURL=null;
		timestamp="";
		encriptionKey="";
		isEncripted=false;
		mStatus="";
		XMLResponse=null;
		responseSent=false;
		mRequest =new MXLoginRequest();
		isMandatoryUpd=false;
	}
	
	
	public MXLogin (IMXLoginCallback caller ,boolean isRegisterAccepted)
	{
		Log.d(LOGTAG, "in constructor");
		
		this.mCaller=caller;
		this.isRegisterAccepted=isRegisterAccepted;
		upgradeURL=null;
		timestamp="";
		encriptionKey=null;
		isEncripted=false;
		mStatus="";
		XMLResponse=null;
		responseSent=false;
		mRequest =new MXLoginRequest(isRegisterAccepted);
		isMandatoryUpd=false;
	}
	
	public void setRegisterAccepted(boolean flag)
	{
		this.isRegisterAccepted=flag;
		mRequest.setRegisterAccepted(flag);		
	}
	
	/*Starts the login process.*/
	public void login()
	{	
		Log.d(LOGTAG, "SendRequest start");
		try{
			Log.d(LOGTAG , mRequest.getXML());
		}
		catch(Exception e)
		{
			
		}
		mSender=MXCommLayer.getInstance(); //do it here for saving connection resources
		mSender.SendRequest(mRequest, this);	
		Log.d(LOGTAG, "after SendRequest");
	}
	
	public void login(String lastTimestamp)
	{	
		Log.d(LOGTAG, "SendRequest start");
		mRequest.setTimestamp(lastTimestamp);
		try{
			Log.d(LOGTAG , mRequest.getXML());
		}
		catch(Exception e)
		{
			
		}
		mSender=MXCommLayer.getInstance(); //do it here for saving connection resources
		mSender.SendRequest(mRequest, this);	
		Log.d(LOGTAG, "after SendRequest");
	}
	
	/*
	 * 	Check if the difference between the last response time and current time is smaller than
	*   the defined grace period (currently 14 days). If it is return YES if it’s not return NO.
	 */
	public boolean isInGracePeriod(String lastSavedTimestamp)
	{
		long now=System.currentTimeMillis();
		if (lastSavedTimestamp==null || lastSavedTimestamp.length()<=0 )
			return false;
		
		long numLastSavedTimestamp=Long.parseLong(lastSavedTimestamp);

		if(((now - numLastSavedTimestamp) /1000/60/60/24 ) > GRACE_PERIOD)
			return false;
		else
			return true;
	}
	
	
	
	//CommLayer response
	@Override
	public void responseReceived(String XMLResponseStr) {
		
		Log.d(LOGTAG, "responseReceived");
		
		this.XMLResponse=XMLResponseStr;
		
		try{	
			
			//Log.d(LOGTAG , "before parseLoginResponse");
			parseLoginResponse(XMLResponse);
			//Log.d(LOGTAG , "after parseLoginResponse mStatus="+mStatus);		
			
			if (upgradeURL!=null && upgradeURL.length()>0)	{
				if(isMandatoryUpd)
					sendResponseFailed(LoginFailureType.LoginFailureTypeMandatoryUpgrade);
				else
					sendResponseSuccess(LoginSuccessType.LoginSuccessTypeSuccessUpgrade);

					
			}
			else	if (mStatus==null)
			{
				sendResponseFailed(LoginFailureType.LoginFailureTypeErrorInXML);
			}
			else	{		
				if(mStatus.equals(LOGIN_STATUS_SUCCESS) ||  mStatus.equals(LOGIN_STATUS_SUCCESS_DISPLAY_TERMS))			
				{		
					if(mCaller.changeEncryptionKey(encriptionKey))	{ //ask delegate
						sendResponseFailed(LoginFailureType.LoginFailureTypeDifferentEncryptionKey);
					}
					else {
						sendResponseSuccess(LoginSuccessType.LoginSuccessTypeSuccessful);
					}					
				}
				if(!responseSent)
					sendResponse();
			}
		}
		
		catch (Exception e) {			
			Log.e(LOGTAG , "Exception in responseReceived."+e);
			sendResponseFailed(LoginFailureType.LoginFailureTypeErrorInXML);
		}
	}
	
	
	

	@Override
	public void responseFailed(MXCommError error) {
		Log.d(LOGTAG, "responseFailed");
		
		if(error.equals(MXCommError.MxCommErrorNetwork))
			sendResponseFailed(LoginFailureType.LoginFailureTypeNetworkError);
		else
			sendResponseFailed(LoginFailureType.LoginFailureTypeErrorInXML);

	}

	
	/* not implemented here*/
	@Override
	public void imagesReceived(HashMap images) {

		Log.d(LOGTAG, "imagesReceived");
		
	}
	

	public void sendResponseSuccess(LoginSuccessType t)
	{
		Log.d(LOGTAG, "sendResponseSuccess: " + t);
		responseSent=true;
		mCaller.loginSuccess(t);
	}
	

	public void sendResponseFailed(LoginFailureType t)
	{
		Log.d(LOGTAG, "sendResponseFailed: " + t);
		responseSent=true;
		mCaller.loginFailed(t);
	}
	
	
	
	public void sendResponse()
	{		
		Log.d(LOGTAG, "sendResponse");
		responseSent=true;
		if(mStatus.equals(LOGIN_STATUS_SUCCESS)) 
			mCaller.loginSuccess(LoginSuccessType.LoginSuccessTypeSuccessful);
		else if(mStatus.equals(LOGIN_STATUS_SUCCESS_DISPLAY_TERMS)) 
			mCaller.loginSuccess(LoginSuccessType.LoginSuccessTypeAcceptTerms);
		else if(mStatus.equals(LOGIN_STATUS_FAILED_DUE_ROAMING)) 
			mCaller.loginSuccess(LoginSuccessType.LoginSuccessTypeOffline);
		else if(mStatus.equals(LOGIN_STATUS_USER_IS_BLOCKED))
			mCaller.loginFailed(LoginFailureType.LoginFailureTypeUserBlocked);
		else if(mStatus.equals(LOGIN_STATUS_USER_IS_PREPAID))
			mCaller.loginFailed(LoginFailureType.LoginFailureTypeUserPrepaid);
		else if(mStatus.equals(LOGIN_STATUS_REGISTER_TO_WEB_ONLY))
			mCaller.loginFailed(LoginFailureType.LoginFailureTypeRegisterToWebOnly);
		else if(mStatus.equals(LOGIN_STATUS_REGISTER_TO_APPLICATION_FIRST))
			mCaller.loginFailed(LoginFailureType.LoginFailureTypeRegisterToAppFirst);
		else if(mStatus.equals(LOGIN_STATUS_WRONG_ACCESS_POINT))
			mCaller.loginFailed(LoginFailureType.LoginFailureTypeNetworkError);
	}
	
	
	

	
	
	
	public void parseLoginResponse(String XMLResponse) throws SAXException
	{	
		
		Log.d(LOGTAG, "parseLoginResponse");
		Log.d(LOGTAG, "XML len="+XMLResponse.length());

		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			
			parser.setInput( new StringReader ( XMLResponse) );
			int eventType = parser.getEventType();

			// auto-detect the encoding from the stream
			boolean done = false;
			while (eventType != XmlPullParser.END_DOCUMENT && !done){
				String name = null;
				switch (eventType){
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase(RESPONSE_TAG)){      	//getAttributes
						int attrcount;
						if((attrcount=parser.getAttributeCount())>0)     {        		    		
							for (int i=0 ; i<attrcount ;i++ )		{
								String attrName = parser.getAttributeName(i);
								String attrValue = parser.getAttributeValue(i);
								if(attrName.equalsIgnoreCase(LAST_LOGIN_ATTR))
									timestamp=attrValue;
								else if(attrName.equalsIgnoreCase(ENCRIPTION_KEY_ATTR))
									encriptionKey=attrValue;
								else if(attrName.equalsIgnoreCase(IS_ENCRIPTED_ATTR))
									isEncripted=(attrValue.equalsIgnoreCase("1"))?true:false;
							}		    			
						} 
					} else if (name.equalsIgnoreCase(STATUS_TAG))	{
						mStatus=parser.nextText(); 
					}
					else if (name.equalsIgnoreCase(UPGRADE_URL_TAGE))	{
						int attrcount;
						if((attrcount=parser.getAttributeCount())>0)     {        		    		
							for (int i=0 ; i<attrcount ;i++ )		{
								String attrName = parser.getAttributeName(i);
								String attrValue = parser.getAttributeValue(i);
								if(attrName.equalsIgnoreCase(MANDATORY_ATTR))
									isMandatoryUpd=(attrValue.equalsIgnoreCase("1")) ?true :false;					
							}		    			
						} 
						upgradeURL=parser.nextText();
						
					}                        
					break;
				case XmlPullParser.END_TAG:
					name = parser.getName();
					if((name.equalsIgnoreCase(LOGIN_RESPONSE_TAG)) ){
						done=true;
						Log.d(LOGTAG, "END_TAG LOGIN_RESPONSE_TAG received");

					}
					
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			Log.e(LOGTAG , "Exception in parseLoginResponse."+e);
			throw new RuntimeException(e);
		}
	 }
	
	

	public String getUpgradeURL()
	{
		return upgradeURL;
	}
	
		
	public void setUpgradeURL(String upgradeURL)
	{
		this.upgradeURL=upgradeURL;
	}
	
	
	
	public void setMXLoginRequest(MXLoginRequest request)
	{
		this.mRequest=request;		
	}
	
	
	
	public MXLoginRequest getMXRequest()
	{		
		return mRequest;
	}
	
	
	public void addPlayedMedia(String mediaID, int playCount)
	{		
		this.mRequest.addPlayedMedia(mediaID, playCount);
	}


	public String getRequestAsString()
	{
		String xml=null;
		
		try{
			xml=this.mRequest.getXML();
		}
		catch (IOException e )
		{
			xml="IOException "+e.getMessage().toString();
		}
		
		
		return xml;
	}
	



	public String getResponseTimestamp()
	{
		return timestamp;		
	}
	
	public String getResponseEncriptionKey()
	{
		return encriptionKey;		
	}
	
		
	public String getResponseAsString()
	{
		return this.XMLResponse;
	}
}

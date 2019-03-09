package il.co.pelephone.musix.utility;

import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import il.co.pelephone.musix.UI.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.XmlResourceParser;
import android.util.Log;

public class MXMessages{
	private static final String LOGTAG = "MXMessages";


	public static final String MSG_DEFAULT="MSG_DEFAULT";
	public static final String MSG_DIFFERENT_SIMCARD="MSG_DIFFERENT_SIMCARD";
	public static final String MSG_REGISTRATION_FAILED="MSG_REGISTRATION_FAILED";
	public static final String MSG_REGISTRATION_SUCCESS="MSG_REGISTRATION_SUCCESS";
	public static final String MSG_ACCEPT_TERMS="MSG_ACCEPT_TERMS";
	public static final String MSG_FIRST_TIME="MSG_FIRST_TIME";
	public static final String MSG_USER_BLOCKED="MSG_USER_BLOCKED";
	public static final String MSG_FIRST_TIME_REGISTERED="MSG_FIRST_TIME_REGISTERED";
	public static final String MSG_COPYRIGHT_DELETION="MSG_COPYRIGHT_DELETION";
	public static final String MSG_SONGS_ADDED_TO_MYMUSIC="MSG_SONGS_ADDED_TO_MYMUSIC";
	public static final String MSG_BEFORE_DELETE="MSG_BEFORE_DELETE";
	public static final String MSG_SONG_ADDED_TO_PLAYLIST="MSG_SONG_ADDED_TO_PLAYLIST";
	public static final String INSUFFICIENT_SPACE="MSG_INSUFFICIENT_SPACE";
	public static final String MSG_JOIN="MSG_JOIN";
	public static final String MSG_UPGRADE_NONMANDATORY="MSG_UPGRADE_NONMANDATORY";
	public static final String MSG_UPGRADE_MANDATORY="MSG_UPGRADE_MANDATORY";
	public static final String MSG_UNREGISTERED_ROAMING="MSG_UNREGISTERED_ROAMING";
	public static final String MSG_ROAMING="MSG_ROAMING";
	public static final String MSG_GRACE_EXPIRED_NO_3GCONNECTION="MSG_GRACE_EXPIRED_NO_3GCONNECTION";
	public static final String MSG_NO_3GCONNECTION="MSG_NO_3GCONNECTION";
	public static final String MSG_UNREGISTERED_NO_3GCONNECTION="MSG_UNREGISTERED_NO_3GCONNECTION";
	public static final String MSG_WIFI_CONNECTION="MSG_WIFI_CONNECTION";
	public static final String MSG_TEST_BUY_ALBUMS="MSG_TEST_BUY_ALBUMS";
	public static final String MSG_TEST_BUY_ALBUMS_ERROR="MSG_TEST_BUY_ALBUMS_ERROR";
	public static final String MSG_DELETE_SONG_FAILED="MSG_DELETE_SONG_FAILED";
	public static final String MSG_ADD_SONG_FAILED="MSG_ADD_SONG_FAILED";
	public static final String MSG_ERROR_DOWNLOADING="MSG_ERROR_DOWNLOADING";
	public static final String MSG_SEARCH_NO_ITEMS_FOUNDS="MSG_SEARCH_NO_ITEMS_FOUNDS";
	public static final String MSG_SEARCH_QUERY_TOO_SHORT="MSG_SEARCH_QUERY_TOO_SHORT";
	public static final String MSG_WIFI_CONNECTION_CATALOG="MSG_WIFI_CONNECTION_CATALOG";
	public static final String MSG_WIFI_CONNECTION_DOWNLOAD="MSG_WIFI_CONNECTION_DOWNLOAD";
	public static final String MSG_WIFI_CONNECTION_GRACE_EXPIRED="MSG_WIFI_CONNECTION_GRACE_EXPIRED";
	public static final String MSG_NO_SDCARD="MSG_NO_SDCARD";

	
	public enum MGButtonCode {
		MGButtonCodeOK,
		MGButtonCodeCancel,
		MGButtonCodeDismiss,
		MGButtonCodeNext
	}

	public enum MGLanguage {
		MGLanguageEnglish,
		MGLanguageHebrew
	}


	//private static MXMessagesCallback mCaller;
	private MGLanguage mLang;

	private MXMessages()
	{
		mLang=MGLanguage.MGLanguageEnglish;
	}



	/*Singleton*/
	private static class MXMessagesHolder {
		private static final MXMessages INSTANCE = new MXMessages();
	}

	//getSharedMessages
	public static MXMessages getInstance() {
		return MXMessagesHolder.INSTANCE;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}




	public  String getMessageForCode(Activity a, String  messageCode)
	{
		boolean done=false;
		String message=null;
		XmlResourceParser parser=null;

		if(this.mLang==MGLanguage.MGLanguageEnglish)
			parser = a.getResources().getXml(R.xml.messages_e);
		else
			parser = a.getResources().getXml(R.xml.messages_he);


		try {
			int eventType = parser.getEventType();

			
			String name=null;
			while (eventType != XmlPullParser.END_DOCUMENT && !done)
			{
				if (parser.getEventType() == XmlResourceParser.START_TAG)
				{
					name = parser.getName();
					if (name.equalsIgnoreCase("Message")) 
					{

						if(parser.getIdAttribute().equalsIgnoreCase(messageCode))
						{
							done=true;
							message=parser.getAttributeValue(null, "text");

						}
					}
				}

				eventType = parser.next();
			}
		}
		catch (XmlPullParserException e) {
			throw new RuntimeException("Cannot parse XML");
		}
		catch (IOException e) {
			throw new RuntimeException("Cannot parse XML");
		}
		finally {
			parser.close();
		}

		return message;

	}

	/*
	 * Displays a UIAlertView for the specified message code. 
	 * Displays button in the UIAlertView according to the buttons provided in the property list.
	 * The function should not return until the user dismisses the alert. 
	 * The function should return the button code pressed.
	 */
	/*   public MGButtonCode  displayModalMessageForCode (String messageCode)
      {

    	  return null;
      }
	 */


	/*
	 * Creates a UIAlertView with the specified message and buttons. 
	 * The class should function as the delegate for the UIAlertView
	 *  and then forward the result to the specified MXMessagesCallback using 
	 *  the buttonClicked(forMessageCode) method specified in MXMessagesCallback.
	 */

	public void displayMessageForCode(Activity a ,final String messageCode ,final MXMessagesCallback caller)
	{
		String title = "Alert!";
		String text="This is an alert!!!";
		boolean found=false;
		AlertDialog alertDialog=null;
		HashMap <String , String> buttons = null;

		boolean done=false;
		XmlResourceParser parser=null;

		Log.d(LOGTAG, "displayMessageForCode "+messageCode);


		if(this.mLang.equals(MGLanguage.MGLanguageEnglish))
			parser = a.getResources().getXml(R.xml.messages_e);
		else
			parser = a.getResources().getXml(R.xml.messages_he);

		try {
			int eventType = parser.getEventType();
			String name=null;
			Log.d(LOGTAG, "displayMessageForCode try");
			while (eventType != XmlPullParser.END_DOCUMENT && !done)
			{
				Log.d(LOGTAG, "displayMessageForCode while");
				if (parser.getEventType() == XmlResourceParser.START_TAG)
				{
					name = parser.getName();
					if (name.equalsIgnoreCase("Message")) 
					{

						if(parser.getIdAttribute().equalsIgnoreCase(messageCode))
						{
							found=true;
							title=parser.getAttributeValue(null, "title");
							text=parser.getAttributeValue(null, "text");
							buttons=new HashMap();
						}
					}
					else if (found && name.equalsIgnoreCase("button"))
					{
						String buttonCode=parser.getAttributeValue(null, "code");
						String buttonName=parser.getAttributeValue(null, "name");
						buttons.put(buttonCode, buttonName);
					}
				}
				else if (parser.getEventType() == XmlResourceParser.END_TAG)
				{
					name = parser.getName();
					if (name.equalsIgnoreCase("Message") && found) 
					{
						Log.d(LOGTAG, "displayMessageForCode beforeAlertDialog");
						alertDialog= new AlertDialog.Builder(a).setTitle(title).setMessage(text).create();

						int i=2;
						int btnType=AlertDialog.BUTTON_POSITIVE;
						for (final String s: buttons.keySet())
						{	
							++i;
							btnType=getButtonType(s);

							Log.d(LOGTAG, "add button "+s);

							alertDialog.setButton( btnType, buttons.get(s), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									caller.buttonClicked(MGButtonCode.valueOf(s), messageCode);
									return;
								} });
							
							if(i>3)
								break;
						}

						done=true;
						if(alertDialog !=null){
							Log.d(LOGTAG, "show alertDialog ");
							alertDialog.show();
						}

					}// of if 
    					  
				} // of end tag
				
				eventType = parser.next();
			} // of while
		}//of try
		catch (XmlPullParserException e) {
			throw new RuntimeException("Cannot parse XML");
		}
		catch (IOException e) {
			throw new RuntimeException("Cannot parse XML");
		}
		finally {
			parser.close();
		}
	}

	
	
	
	
	public  int getButtonType(String s)
	{
		int type=AlertDialog.BUTTON_POSITIVE;

		if (s.equalsIgnoreCase(MGButtonCode.MGButtonCodeCancel.name())) 
			type=AlertDialog.BUTTON_NEGATIVE;
		else if (s.equalsIgnoreCase(MGButtonCode.MGButtonCodeDismiss.name())) 
			type=AlertDialog.BUTTON_NEGATIVE;
		else if (s.equalsIgnoreCase(MGButtonCode.MGButtonCodeNext.name())) 
			type=AlertDialog.BUTTON_NEUTRAL;


		return type;

	}



	public void setLanguage(MGLanguage lang)
	{
		this.mLang=lang;
	}


	/*
	 * If the message code exist, update the title and text. 
	 * If it doesn’t add it to the plist.
	 *  In both cases save the plist once it’s updated.
	 */
	/*
      public void setText(String text , String title , String messageCode)
      {


      }*/

}

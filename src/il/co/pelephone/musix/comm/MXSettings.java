package il.co.pelephone.musix.comm;

import android.content.Context;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MXSettings { //extends PreferenceActivity {

	public static final String PREF_FILE_NAME = "MXSettings";
	
	public static enum MXAppMode {
		MXAppModeNormal,
		MXAppModeOffline,
		MXAppModeDisabled
	}
	
	public static final String PREF_KEY_TIMESTAMP =  "lastResponseTimestamp";
	public static final String PREF_KEY_ENCRIPTION =  "encryptionKey";
	public static final String PREF_KEY_VERSION =  "clientVersion";
	public static final String PREF_KEY_USERID =  "userID";
	public static final String PREF_KEY_APPMODE =  "appMode";
	public static final String PREF_KEY_TOS =  "tos";
	
	public static final String DFLT_TIMESTAMP =  "";
	public static final String DFLT_ENCRIPTION =  "";
	public static final String DFLT_VERSION =  "1.0";	
	public static final String DFLT_USERID =  null;	
	public static final MXAppMode DFLT_APPMODE =  MXAppMode.MXAppModeNormal;
	public static final String DFLT_TOS =  null;	

	
	private static String lastResponseTimestamp;
	private static String encryptionKey;//	Default value is an empty string
	private static String clientVersion;//	Default value 1.0
	private static String userID;//	Default value nil
	private static MXAppMode appMode;//	Not Persistent. Default value MXAppModeNormal.
	
	private static String tos;//	Default value nil
	private SharedPreferences mPreferences; 
		
	public MXSettings (Context context)
	{		
		mPreferences =   PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public MXSettings()
	{
		
	}
	 
	 /* @Override
	 protected void onCreate(Bundle state){         
	       super.onCreate(state);
	       this.Load();
	 }
	
	 
	 @Override
	    protected void onStop(){
	       super.onStop();
	 }
	 */
	 
	 
	public boolean Save()
	{		
		
		SharedPreferences.Editor editor=mPreferences.edit();
		
		if(lastResponseTimestamp != null)
			editor.putString(PREF_KEY_TIMESTAMP , lastResponseTimestamp);	
		
		if(encryptionKey != null)
			editor.putString(PREF_KEY_ENCRIPTION , encryptionKey);
		
		if(clientVersion != null)
			editor.putString(PREF_KEY_VERSION , clientVersion);
		
		if(userID != null)
			editor.putString(PREF_KEY_USERID , userID);
		
		if(appMode != null)
			editor.putString(PREF_KEY_APPMODE , appMode.toString());
         
		if(tos != null)
			editor.putString(PREF_KEY_TOS , tos);
		
        editor.commit();
		return true;
	}
	
	
	public boolean Load()
	{	
		//SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
					
		lastResponseTimestamp=mPreferences.getString(PREF_KEY_TIMESTAMP, DFLT_TIMESTAMP);
		encryptionKey=mPreferences.getString(PREF_KEY_ENCRIPTION, DFLT_ENCRIPTION);
		clientVersion=mPreferences.getString(PREF_KEY_VERSION, DFLT_VERSION);
		userID=mPreferences.getString(PREF_KEY_USERID, DFLT_USERID);		
		appMode= MXAppMode.valueOf(mPreferences.getString(PREF_KEY_APPMODE, DFLT_APPMODE.toString())); 
		tos=mPreferences.getString(PREF_KEY_TOS, DFLT_TOS);		
			
		return true;
	}	
	
	/*
	private MXAppMode appMode;//	Not Persistent. Default value MXAppModeNormal.
	
	 private SharedPreferences mPreferences; */
	
	public void setLastResponseTimestamp(String s)	{		
		MXSettings.lastResponseTimestamp=s;
	}
	
	public String getLastResponseTimestamp(){
		return lastResponseTimestamp;
	}
	
	public void setEncryptionKey(String s)	{		
		MXSettings.encryptionKey=s;
	}
	
	public String getEncryptionKey(){
		return encryptionKey;
	}
	
	public void setClientVersion(String s)	{		
		MXSettings.clientVersion=s;
	}
	
	public String getClientVersion(){
		return clientVersion;
	}
	
	public void setUserID(String s)	{		
		MXSettings.userID=s;
	}
	
	public String getUserID(){
		return userID;
	}
	
	public void setAppMode(MXAppMode m)
	{
		MXSettings.appMode=m;
	}
	
	public MXAppMode getAppMode()
	{
		return appMode;
	}
	
	public void setTOS(String s)	{		
		MXSettings.tos=s;
	}
	
	public String getTOS(){
		return tos;
	}
	
}

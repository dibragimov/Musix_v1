package il.co.pelephone.musix.UI;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import il.co.pelephone.musix.UI.utility.Constants;
import il.co.pelephone.musix.comm.IMXLoginCallback;
import il.co.pelephone.musix.comm.IMXSyncCallback;
import il.co.pelephone.musix.comm.MXLogin;
import il.co.pelephone.musix.comm.MXSettings;
import il.co.pelephone.musix.comm.MXSync;
import il.co.pelephone.musix.comm.MXSettings.MXAppMode;

import il.co.pelephone.musix.utility.MXMessages;
import il.co.pelephone.musix.utility.MXMessagesCallback;
import il.co.pelephone.musix.utility.MXMessages.MGButtonCode;
import il.co.pelephone.musix.utility.MXMessages.MGLanguage;

public class MXUILoginScreen extends Activity implements IMXLoginCallback , IMXSyncCallback ,MXMessagesCallback{
	private final String LOGTAG="MXUILoginScreen";

//	private static final int SHOWSPLASH = 1;
//	private static final int STOPSPLASH = 2;
	private static final int LOGINSUCCESS = 3;
	private static final int LOGINFAILURE = 4;
	private static final int SYNCCOMPLETE = 5;
	private static final int SYNCFAILED = 6;
	private static final int SYNCCOPYRIGHTDELETED = 7;
	private static final int SDCARDMESSAGE_NOSD = 8;
	private static final int SDCARDMESSAGE_NOSPACE = 9;
	
//	private AnimationDrawable anim;

	private MXSettings mSettings;
	private MXLogin login;
	private MXSync sync;
	
//	private String ResponseString;
	
	private PopupWindow popupSync;
	private View syncView;
	private ProgressBar syncProgress;
	private int oneMB = 1024*1024;
		
	IntentFilter syncIntentFilter = new IntentFilter(Constants.Strings.SYNC);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSettings= new MXSettings(this);
		mSettings.Load();
		
		setContentView(R.layout.loader);
		
		
	}

	
	private boolean checkSDCard() {
		File f = new File("/sdcard");
		StatFs fs = new StatFs("/sdcard");
		if(!f.exists() || fs.getAvailableBlocks()==0){
			Message msgSDCard = new Message();
			msgSDCard.what = SDCARDMESSAGE_NOSD;
			handler.sendMessageDelayed(msgSDCard, 100);
			return true;
		}
		else if( ((fs.getAvailableBlocks()*fs.getBlockSize())/oneMB) < 100){
			Message msgSDCard = new Message();
			msgSDCard.what = SDCARDMESSAGE_NOSPACE;
			handler.sendMessageDelayed(msgSDCard, 100);
			return true;
		}
		
		Log.d("sdcard length", "sdcard length: "+((fs.getAvailableBlocks()*fs.getBlockSize())/oneMB));
		return false;
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(isUsingWiFi())
		{
//			if(anim.isRunning())
//				anim.stop();

			showMessageAllert(MXMessages.MSG_WIFI_CONNECTION);
			
		}
		else if(checkSDCard()){
//			if(anim.isRunning())
//				anim.stop();
			Log.d("MXUILoginScreen", "sdcard problem");
		}
		else{
			
			
			login=new MXLogin (this);
			login.login(mSettings.getLastResponseTimestamp());
			Log.d("MXUILoginScreen", "login started. ");
			
		}

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		registerReceiver (syncUpdateReceiver, syncIntentFilter);
	}

	private Handler handler = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
		
			case LOGINSUCCESS:
				break;
				
			case LOGINFAILURE:
				break;
				
			case SYNCCOMPLETE:
				Log.d(LOGTAG, "in syncCompleted");
				
				mSettings.setEncryptionKey(sync.getResponseEncriptionKey());
				mSettings.setLastResponseTimestamp(sync.getResponseTimestamp());
				mSettings.setClientVersion(sync.getResponseClientVersion());
				mSettings.setAppMode(MXAppMode.MXAppModeNormal);		
				mSettings.Save();
				
				dismissSyncPanel();
				
				Intent intent = new Intent(MXUILoginScreen.this, MXUIMusixMainScreen.class); 
				startActivity(intent);
				finish();
				
				break;
				
			case SYNCFAILED:
				showMessageAllert(MXMessages.MSG_DEFAULT);
				break;
				
			case SYNCCOPYRIGHTDELETED:
				break;
			case SDCARDMESSAGE_NOSPACE:
				showMessageAllert(MXMessages.INSUFFICIENT_SPACE);
				break;
			case SDCARDMESSAGE_NOSD:
				showMessageAllert(MXMessages.MSG_NO_SDCARD);////need to change
				break;
				
			default:
				break;
			}
			return false;
		}

	});

	@Override
	public void loginSuccess(LoginSuccessType type) {

		Log.d(LOGTAG, "in loginSuccess type="+type);
		if (type.equals(LoginSuccessType.LoginSuccessTypeSuccessful))	{
			mSettings.setEncryptionKey(login.getResponseEncriptionKey());
			mSettings.setLastResponseTimestamp(login.getResponseTimestamp());
			mSettings.Save();
			
			try{
				sync();			
			}
			catch (Exception e)	{
				Log.e(LOGTAG, "in loginSuccess Exception "+e);
				finish();			
			}
		}
		else if (type.equals(LoginSuccessType.LoginSuccessTypeOffline))	{
			if(	login.isInGracePeriod(mSettings.getLastResponseTimestamp())==true )	{
				showMessageAllert(MXMessages.MSG_NO_3GCONNECTION);
			}
			else	{
				showMessageAllert(MXMessages.MSG_GRACE_EXPIRED_NO_3GCONNECTION);				
			}
		}		
		else if (type.equals(LoginSuccessType.LoginSuccessTypeAcceptTerms))
		{
			mSettings.setLastResponseTimestamp(login.getResponseTimestamp());
			mSettings.Save();
			showMessageAllert(MXMessages.MSG_JOIN);
		}
		else if (type.equals(LoginSuccessType.LoginSuccessTypeSuccessUpgrade))
		{
			mSettings.setEncryptionKey(login.getResponseEncriptionKey());
			mSettings.setLastResponseTimestamp(login.getResponseTimestamp());
			mSettings.Save();
			
			showMessageAllert(MXMessages.MSG_UPGRADE_NONMANDATORY);
		}
	}



	@Override
	public void loginFailed(LoginFailureType reason) {
		
		Log.d(LOGTAG, "in loginFailed  type=" +reason);

//		if(anim != null && anim.isRunning())
//			anim.stop();
		
		if (reason.equals(LoginFailureType.LoginFailureTypeUserBlocked))	{
			showMessageAllert(MXMessages.MSG_USER_BLOCKED);
		}
		else if (reason.equals(LoginFailureType.LoginFailureTypeUserPrepaid))	{
			showMessageAllert(MXMessages.MSG_USER_BLOCKED);

		}else if (reason.equals(LoginFailureType.LoginFailureTypeRegisterToWebOnly))	{
			showMessageAllert(MXMessages.MSG_USER_BLOCKED);
			
		}else if (reason.equals(LoginFailureType.LoginFailureTypeRegisterToAppFirst))	{
			
			//TODO:  ???		
			showMessageAllert(MXMessages.MSG_JOIN);
			
		}else if (reason.equals(LoginFailureType.LoginFailureTypeErrorInXML))	{
			showMessageAllert(MXMessages.MSG_DEFAULT);

		}else if (reason.equals(LoginFailureType.LoginFailureTypeUnknown))	{
			showMessageAllert(MXMessages.MSG_DEFAULT);

		}else if (reason.equals(LoginFailureType.LoginFailureTypeNetworkError))	{
			showMessageAllert(MXMessages.MSG_NO_3GCONNECTION);////MSG_DEFAULT

		}else if (reason.equals(LoginFailureType.LoginFailureTypeDifferentEncryptionKey))	{
			showMessageAllert(MXMessages.MSG_DIFFERENT_SIMCARD);
			
		}else if (reason.equals(LoginFailureType.LoginFailureTypeMandatoryUpgrade))	{
			showMessageAllert(MXMessages.MSG_UPGRADE_MANDATORY);
			
		}
	}
	
	
	
	

	public void sync() throws IOException
	{
		showSyncPanel();

		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try{
					ContentResolver localContentProvider = getContentResolver();

					String 	ResponseString=login.getResponseAsString();    	

					//sync=new MXSync(this ,localContentProvider, this);
					sync=new MXSync(MXUILoginScreen.this ,localContentProvider, mSettings);
					sync.setContext(MXUILoginScreen.this);

					Log.d(LOGTAG, "before sync " +ResponseString);
					sync.sync(ResponseString);
					Log.d(LOGTAG, "after sync");
					ResponseString+=sync.getResponseAsString();   
				}
				catch (Exception e){
					Log.e(LOGTAG, "exception "+e);
					throw new RuntimeException();

				}
			}
		};
		Thread thr = new Thread(r);
		thr.start();

	}

	@Override
	public void syncCompleted(int newSongCount) {
		Log.d(LOGTAG, "sync completed");
		Message msgSyncComplete = new Message();
		msgSyncComplete.what = SYNCCOMPLETE;
		handler.sendMessageDelayed(msgSyncComplete, 1);
	}

	
	
	@Override
	public void syncFailed(MXSyncError error) {
		// TODO Auto-generated method stub
		Log.d(LOGTAG, "in syncFailed error="+ error);
		Message msgSyncFailed = new Message();
		msgSyncFailed.what = SYNCFAILED;
		handler.sendMessageDelayed(msgSyncFailed, 1);
		
	}
	
	
	

	@Override
	public void syncSongsCopyrightDeleted(String[] songNames) {
		// TODO Auto-generated method stub
		Log.d(LOGTAG, "in syncSongsCopyrightDeleted");
		//showMessageAllert(MXMessages.MSG_COPYRIGHT_DELETION);

	}




	@Override
	public void buttonClicked(MGButtonCode buttonCode, String messageCode) {
		//Log.d(LOGTAG, "in buttonClicked buttonCode="+buttonCode.name() + " messageCode="+messageCode);

		if (messageCode.equals(MXMessages.MSG_NO_3GCONNECTION))
		{
			//TODO: set offline mode
			mSettings.setAppMode(MXAppMode.MXAppModeOffline);
			mSettings.Save();
			//showmain screen
			Intent intent = new Intent(MXUILoginScreen.this, MXUIMusixMainScreen.class); 
			startActivity(intent);
			finish();
		}
			
		else  if (messageCode.equals(MXMessages.MSG_JOIN))
		{
			if (buttonCode.equals(MGButtonCode.MGButtonCodeOK))	{
				findViewById(R.id.prgrsBrLoader).setVisibility(View.VISIBLE); ////show animation
				login=new MXLogin (this , true);
				login.login(mSettings.getLastResponseTimestamp());				
			}
			else	{
				//TODO: show terms and then login 
				
				finish();					
			}			
		}
		else if (messageCode.equals(MXMessages.MSG_UPGRADE_NONMANDATORY))
		{
			try{
				sync();			
			}
			catch (Exception e)	{
				Log.e(LOGTAG, "in loginSuccess Exception "+e);
				finish();			
			}		
		}
		else if ( messageCode.equals(MXMessages.MSG_DIFFERENT_SIMCARD))
		{
			if (buttonCode.equals(MGButtonCode.MGButtonCodeOK))	{
				mSettings.setEncryptionKey(login.getResponseEncriptionKey());
				mSettings.setLastResponseTimestamp(login.getResponseTimestamp());
				mSettings.Save();
				
				try{
					sync();			
				}
				catch (Exception e)	{
					Log.e(LOGTAG, "in loginSuccess Exception "+e);
					finish();			
				}
			}
			else {
				finish();					
			}		
		}	
		else if (messageCode.equals(MXMessages.MSG_COPYRIGHT_DELETION))
		{
					
		}	
		else if(messageCode.equals(MXMessages.INSUFFICIENT_SPACE))
		{
			login=new MXLogin (this);
			login.login(mSettings.getLastResponseTimestamp());
			Log.d("MXUILoginScreen", "login started. ");
		}
		else if(messageCode.equals(MXMessages.MSG_NO_SDCARD))////needs to change 
		{
			login=new MXLogin (this);
			login.login(mSettings.getLastResponseTimestamp());
			Log.d("MXUILoginScreen", "login started. ");
		}
		else
		{
			finish();			
		}
	}


	public void showMessageAllert(String err)
	{
		//Log.d(LOGTAG, "MXMessages.getInstance");
		((ProgressBar)findViewById(R.id.prgrsBrLoader)).setIndeterminate(false);
		findViewById(R.id.prgrsBrLoader).setVisibility(View.GONE); ////hide animation
		
		MXMessages message=MXMessages.getInstance();	    
		message.setLanguage(MGLanguage.MGLanguageHebrew);				    
		message.displayMessageForCode(this , err , this );			
	}


	public boolean isUsingWiFi()
	{

		try{
			ConnectivityManager conMan = 
				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

			if(conMan!=null){
				NetworkInfo ni = conMan.getActiveNetworkInfo();
				if (ni!=null   && ( ni.getType()== conMan.TYPE_WIFI) ){
					Log.e(LOGTAG, "in isUsingWiFi return true");
					return true;
				}
			}
		}
		catch(Exception e)
		{
			Log.e(LOGTAG, "in isUsingWiFi exception "+e);
			return true;
		}

		return false;
	}
	
	
	//return false if do not need to change
	@Override
	public boolean changeEncryptionKey(String newEncriptionKey) {

		if (newEncriptionKey==null) // do not need a change
			return false;		
		
		// Fixed By NGSoft
//		if(( mSettings.getEncryptionKey()==null) 
//			|| 	(!mSettings.getEncryptionKey().equals(newEncriptionKey))	)	
		if ((mSettings.getEncryptionKey() != null && mSettings.getEncryptionKey().length() > 0) &&
			(!mSettings.getEncryptionKey().equals(newEncriptionKey))	)	
		// Fixed By NGSoft
		{ 
			Log.d(LOGTAG, "need to change EncryptionKey");
			return true;
		}
		
		return false;
	}
	
	private BroadcastReceiver syncUpdateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int msg = intent.getIntExtra(Constants.Strings.MSG, -1);
			if(!popupSync.isShowing() && syncProgress != null)
				return;
			switch (msg) {
				case Constants.Integers.ALLARTISTPROCESSED:
					syncProgress.setProgress(syncProgress.getProgress()+5);
					break;

				case Constants.Integers.ALLALBUMSPROCESSED:
					syncProgress.setProgress(syncProgress.getProgress()+5);
					break;
				
				case Constants.Integers.ALLGENRESPROCESSED:
					syncProgress.setProgress(syncProgress.getProgress()+5);
					break;
				
				case Constants.Integers.ALLSONGSPROCESSED:
					syncProgress.setProgress(syncProgress.getProgress()+80);
					break;
					
				case Constants.Integers.ONESONGPROCESSED:
					syncProgress.setProgress(syncProgress.getProgress()+1);
					break;
					
				case Constants.Integers.ALLPLAYLISTSPROCESSED:
					syncProgress.setProgress(syncProgress.getProgress()+5);
					break;
				
				default:
					break;
			}
			Log.d(LOGTAG, "broadcast message:"+msg);
		}
		
	};
	
	private void showSyncPanel(){
		if(syncView == null){
			LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			syncView = inflater.inflate(R.layout.initial_sync, null);
			syncProgress = (ProgressBar)syncView.findViewById(R.id.prgrsBrSync);
		}
		if(popupSync == null){
			WindowManager mWinMgr = (WindowManager)getSystemService(WINDOW_SERVICE); 
			int displayWidth = mWinMgr.getDefaultDisplay().getWidth();
			popupSync = new PopupWindow(syncView, displayWidth, 250);
		}
		
		popupSync.showAsDropDown(findViewById(R.id.linearSyncAnchor));//AtLocation(getWindow().getDecorView(), Gravity.NO_GRAVITY, 0, 0);
		
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		dismissSyncPanel();
		unregisterReceiver(syncUpdateReceiver);
	}

	private void dismissSyncPanel(){
		if(popupSync != null && popupSync.isShowing())
			popupSync.dismiss();
	}
}

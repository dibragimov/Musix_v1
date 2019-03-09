package il.co.pelephone.musix.UI.MediaPlayer;
//package com.pocketjourney.media;
//Code taken from below URL. 
//http://blog.pocketjourney.com/2008/04/04/tutorial-custom-media-streaming-for-androids-mediaplayer/
//Good looking out!

import il.co.pelephone.musix.UI.R;
import il.co.pelephone.musix.UI.utility.Constants;
import il.co.pelephone.musix.UI.utility.MusixPlayList;
import il.co.pelephone.musix.comm.MXCommLayer;
import il.co.pelephone.musix.comm.MXEnv;
import il.co.pelephone.musix.comm.MXSettings;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

/**
 * This class provides a pseudo-streaming function
 * by downloading the content incrementally, deciphering it, & playing as soon as 
 * it get enough audio in the temporary storage.
 */ 
public class StreamingMediaPlayer extends Service {

	final static public String AUDIO_MPEG =  "audio/mpeg";
    private int INTIAL_KB_BUFFER ;
    final private int BIT = 8 ;
    final private int SECONDS = 10 ; ////
	
	private File downloadingMediaFile ; 
	private final String DOWNFILE = "downloadingMediaFile";

	private int totalKbRead  ;
	private Context context;
	private int tempFileCounter ;
	private int playedcounter ;
	//TODO should convert to Queue object instead of Vector
	private Vector<MediaPlayer> mediaplayers ;
	
	private boolean processHasStarted ; 
	
	private int songID;
	private double songPlayedTime;
	private int songPlayExpectedTime;

	private BufferedInputStream stream  ;
    
    Thread preparringthread ;
    Thread downloadingThread;
    
    private String TAG = "StreamingMediaPlayer";
    private boolean downloadCompleted = false;
    
    private MusixPlayList playlistSongIDs;
    
    private Decrypter decrypter;
    private String url;
    
    private int appMode = 0;
    
    private Timer timerSongProgressUpdater;
    
  //Setup all the variables
	private  void setupVars(){
		totalKbRead = 0;
		tempFileCounter = 0;
		playedcounter = 0;
		//TODO should convert to Queue object instead of Vector
		mediaplayers = new Vector<MediaPlayer>();
		processHasStarted = false; 
		stream = null;
		
		downloadCompleted = false;
	    
	    playlistSongIDs = null;
	    preparringthread = null;
	    
	    songID=0;
	    songPlayedTime=0;
	}
    
    // listen for calls
	// http://www.androidsoftwaredeveloper.com/2009/04/20/how-to-detect-call-state/ 
	  final PhoneStateListener myPhoneListener = new PhoneStateListener() {
		  public void onCallStateChanged(int state, String incomingNumber) {
			  String TAG = "PhoneStateListener";
			  
			  switch (state) {
			  	case TelephonyManager.CALL_STATE_RINGING:
			  		Log.d(TAG, "Someone's calling. Let us stop the service");
			  		sendMessage(Constants.Integers.PAUSE);
			  		////stop();
			  		pause();
				  	break;
			  	case TelephonyManager.CALL_STATE_OFFHOOK:
				  	break;
				case TelephonyManager.CALL_STATE_IDLE:
					//startMediaPlayer();////here we may want to start the service again. need to ask
				  	break;
				default:
				  	Log.d(TAG, "Unknown phone state = " + state);
			  }
		  }
	  };
	
    
    //This object will allow other processes to interact with our service
    private final IStreamingMediaPlayer.Stub ourBinder = new IStreamingMediaPlayer.Stub(){
        String TAG = "IStreamingMediaPlayer.Stub";
        
        //Start playing audio
    	public void startAudio(){
    		Log.d(TAG, "starting Audio" );
    		
    		Runnable r = new Runnable() {   
    			public void run() {
    				//here we need to either start a new media player or continue playing the old one
    				startMediaPlayer();
    				////onStart (startingIntent, 0);
    			}   
    		};   
    		new Thread(r).start(); 
    		
    	}
    	
		@Override
		public int getPlayedTime() throws RemoteException {
			if(mediaplayers.size() > 1 && mediaplayers.get(0)!= null && mediaplayers.get(0).isPlaying())
				return (int)songPlayedTime+(mediaplayers.get(0).getCurrentPosition()/1000);
			return (int)songPlayedTime;
		}

		@Override
		public String getSongID() throws RemoteException {
			return playlistSongIDs.getCurrent();
		}

		@Override
		public boolean isPlaying() throws RemoteException {
			if(mediaplayers != null && mediaplayers.size() > 0){
				return mediaplayers.get(0).isPlaying();
			}
			return isPlayingAudio();
		}

		@Override
		public void pauseAudio() throws RemoteException {
			pause();
		}

		@Override
		public void stop() throws RemoteException {
			Log.d(TAG, "stopping service" );
    		stopService();
		}

		@Override
		public void playNext() throws RemoteException {
			Runnable r = new Runnable() {   
    			public void run() {
    				playNextSong();
    			}   
    		};   
    		new Thread(r).start();
		}

		@Override
		public void playPrevious() throws RemoteException {
			Runnable r = new Runnable() {   
    			public void run() {
    				playPreviousSong();
    			}   
    		};   
    		new Thread(r).start(); 
		}

		@Override
		public void setRepeatPlayback(boolean repeating) throws RemoteException {
			if(playlistSongIDs != null)
				playlistSongIDs.setRepeating(repeating);
		}

		@Override
		public void setShufflePlaylist(boolean shuffling) throws RemoteException {
			if(playlistSongIDs != null)
				playlistSongIDs.setShuffled(shuffling);
		}
    	
    };
	
    /* called only once when the service is instantiated.
     * starts listening for incoming calls - we need to stop the app if somebody is calling
     * (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override 
    public void onCreate() {
    	  super.onCreate();
    	  
    	  Log.d(TAG, " onCreate: Setting up phone listener");
    	  TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
    	  tm.listen(myPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

    	  byte env = MXEnv.getEnv();
  		  if ( env == MXEnv.ENV_DEV )
  		  {
  			  url=MXCommLayer.TRIPLAY_URL_DEV+Constants.Strings.MEDIA_DOWNLOAD_URL_PART;
  		  }
  		  else if ( env == MXEnv.ENV_TEST )
  		  {
  			  url=MXCommLayer.TRIPLAY_URL_TEST+Constants.Strings.MEDIA_DOWNLOAD_URL_PART;			
  		  }
  		  else
  			  url=MXCommLayer.TRIPLAY_URL_PROD+Constants.Strings.MEDIA_DOWNLOAD_URL_PART;
  		
  		  ////if service is playling music - send updates to UI every 2 secs 
  		  if(timerSongProgressUpdater == null){
  			  timerSongProgressUpdater = new Timer();
  			  timerSongProgressUpdater.schedule(new TimerTask() {
				
  				  @Override
  				  public void run() {
  					
  					  if( mediaplayers != null && mediaplayers.size() > 0 && mediaplayers.get(0).isPlaying() ){
  						  sendSeconds((int)songPlayedTime+(mediaplayers.get(0).getCurrentPosition()/1000));
//  						  Log.d(TAG, "seconds sent");
  					  }
//  					Log.d(TAG, "task running");
  				  }
  		  }, 3000, 1000); //// 3000 - 3 sec delay to start the update, 1000 - 1 sec - update every 1 sec
		}
    }
    
    /*
     * called every time the app calls startService(Intent) - need to start the new song probably
     * (non-Javadoc)
     * @see android.app.Service#onStart(android.content.Intent, int)
     */
    @Override
    public void onStart (Intent intent, int startId){
    	super.onStart(intent, startId);
//    	sendMessage(Constants.Integers.SPIN);
    	cleanup();//// stops all media players and deletes all temp files 
    	
    	Log.d(TAG, " onStart: Setup Vars");
    	////nullify all variables
  	    setupVars();
  	    
    	Log.d(TAG, "Intent: " + intent.getStringExtra(Constants.Strings.URL));
    	
    	//audiourl =  intent.getStringExtra(Constants.Strings.URL);
    	songPlayExpectedTime = intent.getIntExtra(Constants.Strings.SONGPLAYEXPECTEDTIME, 0);
    	
    	//// add all mediaIDs to an array to play them
    	final String catalogMediaID = intent.getStringExtra(Constants.Strings.CATALOGMEDIAID);
    	Log.d(TAG, "currentCatalogMediaID: "+catalogMediaID);
    	String[] playlistIDs = intent.getStringArrayExtra(Constants.Strings.PLAYLIST_SONGS);
    	playlistSongIDs = new MusixPlayList();
//    	playlistSongIDs.add(catalogMediaID);
    	if(playlistIDs != null)
    		for (String songid : playlistIDs) {
//    			if(!catalogMediaID.equals(songid)){ /// put in a queue if not the starting ID
    				playlistSongIDs.add(songid);
//    			}
    		}
    	playlistSongIDs.setCurrent(catalogMediaID);
    	
    	appMode = intent.getIntExtra(Constants.Strings.MODE, Constants.Integers.MXAppModeNormal);
    	
    	context = this;
    	Runnable r = new Runnable() {
			
			@Override
			public void run() {
				playMedia(catalogMediaID);
			}
		};
		new Thread(r).start();
    }
    
    @Override 
    public void onDestroy() {
    	Log.d(TAG, "stop() Remove Phone listener");
      	TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
      	tm.listen(myPhoneListener, PhoneStateListener.LISTEN_NONE);
      	
    	super.onDestroy();
    	Log.d(TAG, " onDestroy");
    }
    
    @Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	@Override
    public IBinder onBind (Intent intent){
    	context = this;
    	
    	return ourBinder;
    }
   
	
    /**  
     * Progressively down load the media to a temporary location and update the MediaPlayer 
     * as new content becomes available.
     */  
    public void startStreaming(final String mediaUrl) throws IOException {
    	int bitrate = 192;//56
    	
    	totalKbRead = 0;
		tempFileCounter = 0;
		playedcounter = -1;
		processHasStarted = true;
		downloadCompleted = false;
		songPlayedTime = 0;
	    
		//sendMessage( Constants.Integers.SPIN );
        
        //// code to get Encryption key from Shared Preferences
		MXSettings mSettings = new MXSettings();
    	//SharedPreferences prefs = this.getSharedPreferences(MXSettings.PREF_FILE_NAME, Context.MODE_PRIVATE); //PreferenceManager.getDefaultSharedPreferences(context);
  	  //  String encKey = prefs.getString(MXSettings.PREF_KEY_ENCRIPTION, "0342");//// right now does not work
		String encKey = mSettings.getEncryptionKey();
  	    Log.i(TAG, "encryption Key: "+encKey+", "+context.getPackageName()+". "+context.toString());
  	    decrypter = new Decrypter();
  	    decrypter.init(encKey);
  	    //// enf of code to get Encryption key from Shared Preferences
		
		Log.d(TAG, "Setup incremental stream");
        	//Lets Start Streaming by downloading parts of the stream and playing it in pieces
        	//Set up buffer size
        	//Assume XX kbps * XX seconds / 8 bits per byte
        INTIAL_KB_BUFFER =  bitrate * SECONDS / BIT; 
        	 
    		Runnable r = new Runnable() {   
    	        public void run() {   
    	            try {   
    	        		downloadAudioIncrement(mediaUrl);
    	            } catch (Exception e) {
    	            	Log.e(TAG, "Unable to initialize the MediaPlayer for Audio Url = " + mediaUrl, e);
    	            	sendMessage( Constants.Integers.TROUBLEWITHAUDIO);
    	            	//// nullify streams
    	            	return;
    	            }   
    	        }   
    	    };   
    	    downloadingThread = new Thread(r);
        	downloadingThread.start(); 
    }
    
    /**  
     * Download the url stream to a temporary location and then call the setDataSource  
     * for that local file
     */  
    public void downloadAudioIncrement(String mediaUrl) throws IOException {
    	int bufsizeForDownload = 8 * 1024;
    	int bufsizeForFile = 8 * 8 * 1024;
    	
    	HttpPost httpPost = new HttpPost(mediaUrl);
		
		httpPost.setHeader("CONTENT-TYPE", "application/x-www-form-urlencoded; charset=utf-8");
		//add headers
		if (MXEnv.getEnv() == MXEnv.ENV_DEV)
		{
			httpPost.setHeader("MSISDN", "BALORA");
			httpPost.setHeader("IMSI", "BALORA");
		}
		else
		{
			httpPost.setHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 1.5; iw-il; GT-I5700");
		}
		
		
		HttpResponse resp = new DefaultHttpClient().execute(httpPost);
    	
        stream = new BufferedInputStream ( resp.getEntity().getContent(), bufsizeForDownload );

        if (stream == null) {
        	Log.e(TAG, " downloadAudioIncrement(). Unable to create InputStream for mediaUrl: " + mediaUrl);
        	sendMessage( Constants.Integers.TROUBLEWITHAUDIO);
        	//// can't read bytes from stream - finish down loading thread
        	//// exit download thread
        	return;
        }
        
        downloadingMediaFile = new File(context.getCacheDir(),DOWNFILE + tempFileCounter); ////naming files with counter
     	downloadingMediaFile.deleteOnExit();
        
		Log.d(TAG, " downloadAudioIncrement(). File name: " + downloadingMediaFile.getAbsolutePath());
		BufferedOutputStream bout = new BufferedOutputStream ( new FileOutputStream(downloadingMediaFile), bufsizeForFile );   
		
        byte buf[] = new byte[bufsizeForDownload];
        int totalBytesRead = 0, incrementalBytesRead = 0, numread = 0;

        do {
        	if (bout == null) {
        		tempFileCounter++;
        		Log.d(TAG, "FileOutputStream is null, Create new one: " + DOWNFILE + tempFileCounter);
        		downloadingMediaFile = new File(context.getCacheDir(),DOWNFILE + tempFileCounter);
        		downloadingMediaFile.deleteOnExit();
        		bout = new BufferedOutputStream ( new FileOutputStream(downloadingMediaFile) , bufsizeForFile );	
        	}

        	try {
        		//Log.v(TAG, "read stream");
        		numread = stream.read(buf);
        	} catch (IOException e){
        		Log.e(TAG, e.toString());
        		e.printStackTrace();
        		Log.e(TAG, " downloadAudioIncrement(). Bad read. Let's quit.");
        		sendMessage( Constants.Integers.TROUBLEWITHAUDIO);
        		stream.close();
            	stream=null; ////this will exit the loop and downloading thread
        		
        	} catch (NullPointerException e) {
        		//Let's get out of here
        		Log.d(TAG, " downloadAudioIncrement(). "+e.getMessage());
        		e.printStackTrace();
        		break;
        	}
        	
            if (numread < 0) {  
            	//We got something weird. Let's get out of here.
            	Log.e(TAG, "End of stream. We got some number less than 0: " + numread + " Let's quit." );
            	
            	downloadCompleted  = true;
            	if(totalBytesRead>0)//// something was downloaded before the end reached
            		bout.flush();
            	bout.close();
            	stream.close();
            	stream=null;
            	////start for debugging when file size is small for prod server
            	if(totalBytesRead==0 ){
            		bout.flush();
            		StringBuilder contents = new StringBuilder();
            		BufferedReader br = new BufferedReader(new FileReader(downloadingMediaFile));
            		try {
            	        String line = null; //not declared within while loop
            	        
            	        while (( line = br.readLine()) != null){
            	          contents.append(line);
            	          contents.append(System.getProperty("line.separator"));
            	        }
            	    }
            	    finally {
            	        br.close();
            	    }
            	    Log.d(TAG, contents.toString());
            	}
            	else////end for debugging when file size is small for prod server - need to be removed for release
            		if(totalBytesRead==0)
            		downloadingMediaFile.delete();/// if the file was created and nothing is read
            	else{////downloaded the whole file
            		bout.flush();
                	bout.close();
            		setupPlayer(downloadingMediaFile);
            		totalBytesRead = 0;
            	}
            	/// from stream - delete the file
                break;   
            	
            } else if ( numread >= 1 ) {
            	bout.write(buf, 0, numread);

            	totalBytesRead += numread;
            	incrementalBytesRead += numread;
            	totalKbRead = totalBytesRead/1000;
            }
            
            /*if (totalKbRead >= INTIAL_KB_BUFFER) {
            	//Log.v(TAG, "Reached Buffer amount we want: " + "totalKbRead: " + totalKbRead + " INTIAL_KB_BUFFER: " + INTIAL_KB_BUFFER);
            	if(incrementalBytesRead % 2==0 && tempFileCounter==0)
            		bout.write(stream.read());
            	else if(tempFileCounter > 0 && !(totalBytesRead % 2==0))
            		bout.write(stream.read());
            	bout.flush();
            	bout.close();
            	Log.v(TAG, "Finished writing to file: "+downloadingMediaFile.getName());
            	bout = null;
            	
            	////now - create a player for the chunk
            	setupPlayer(downloadingMediaFile);
            	totalBytesRead = 0;

            }*/
            
        } while (stream != null);  //// check to make sure we nullify stream in the loop
        Log.d(TAG, "Done with streaming");

    }  
    
    /**
     * Set Up player(s)
     */  
    private void  setupPlayer(File partofaudio) {
    	final File f = partofaudio;
    	
    	Runnable r = new Runnable() {
	        public void run() {
	        	
	        	MediaPlayer mp = new MediaPlayer();
	        	try {
	        		
	        		MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener () {
	        			public void onCompletion(MediaPlayer mpl){
	        				
	        				if (mediaplayers.size() <= 1 && !downloadCompleted){
		        		    	Log.d(TAG, "waiting for another mediaplayer");//// waiting for end of download
		        		    	sendMessage( Constants.Integers.SPIN); //// let the UI show loader
		        		    }
	        				
	        				sendMessage( Constants.Integers.STOP);//// maybe we need to send a message about how long it was played
    						processHasStarted = false;
    						
    						mpl.release();
        					mediaplayers.remove(mpl); //// removing the previous chunk
        					Log.d(TAG, "after the removal the media player's size is: "+mediaplayers.size());
        					//mp=null;
        					playedcounter++;
	        				removeFile();
    						
	        				/*final MediaPlayer mp = mpl;
	        				Runnable r = new Runnable() {
								
								@Override
								public void run() {
									Log.d(TAG, "setupPlayer() MediaPlayer.OnCompletionListener "+"Current size of mediaplayer list: " + mediaplayers.size());
			        				Log.d(TAG, "setupPlayer() MediaPlayer.OnCompletionListener "+mp.toString()+" is finished playing");
			        				
			        				songPlayedTime += ( ((double)mp.getDuration())/1000 );////this many seconds the file has played
			        				
				        		    long timeInMilli = Calendar.getInstance().getTime().getTime();
				        		    long timeToQuit = (1000 * 120) + timeInMilli; ////add 30 seconds - this many seconds we are waiting 
				        		    ////for the first download (or this is pause between plays)
				        		    if (mediaplayers.size() <= 1 && !downloadCompleted){
				        		    	Log.d(TAG, "waiting for another mediaplayer");//// waiting for end of download
				        		    	sendMessage( Constants.Integers.SPIN); //// let the UI show loader
				        		    }
			        				while (mediaplayers.size() <= 1){
			        					if(downloadCompleted){//// everything has been played out - exit the method
			        						sendMessage( Constants.Integers.STOP);//// maybe we need to send a message about how long it was played
			        						processHasStarted = false;
			        						break;
			        					}
			        					if ( timeInMilli > timeToQuit ) {
			        						//time to get out of here
			        						Log.e(TAG, "Timeout occured waiting for another media player");
		    			        			sendMessage( Constants.Integers.TROUBLEWITHAUDIO);
		    			        			processHasStarted = false;
		    			        			stopService();//// need to think
			        					}
			        					try {
											Thread.sleep(1000);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
			        					timeInMilli = Calendar.getInstance().getTime().getTime();
			        					
			        					if(!processHasStarted)////exits the thread if process to be stopped
			        						break;
		    			        	}
			        				
			        				if(mediaplayers.size()>1){
			        					MediaPlayer mp2 = mediaplayers.get(1);//// this is the next
			        				//// chunk of music that needs to be played
			        					try {
											mp2.prepare();
										} catch (IllegalStateException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
				        				mp2.start();
				        				sendMessage(Constants.Integers.STOPSPIN);
				        				Log.d(TAG, "setupPlayer() MediaPlayer.OnCompletionListener. Starting another player.");//// play the next downloaded chunk
			        				}
			        				
			        				mp.release();
		        					mediaplayers.remove(mp); //// removing the previous chunk
		        					Log.d(TAG, "after the removal the media player's size is: "+mediaplayers.size());
		        					//mp=null;
		        					playedcounter++;
			        				removeFile();
								}
							};
	        				new Thread(r).start();*/
	        			}
	        		};
	        		
	        		File tempF = decrypter.decryptToFile(f);
	        		
	        		FileInputStream ins = new FileInputStream( tempF );
	            	mp.setDataSource(ins.getFD());
	        		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        		
	        		mp.setOnCompletionListener(listener);
	        		
//	        		if (mediaplayers.size() <= 1){
//	        			Log.d(TAG, "setupPlayer() Prepare synchronously.");
//	        			mp.prepare();
//	        		} else {
////	        			//This will save us a few more seconds
//	        			Log.d(TAG, "setupPlayer() Prepare Asynchronously.");
//	        			mp.prepareAsync();
//	        		}
	        		
	        		mediaplayers.add(mp);
	        		
	        		////runs only first time
	        		if ( mediaplayers.size() == 1 && (! mediaplayers.get(0).isPlaying()) ){
		        		Log.d(TAG, "setupPlayer() Start Media Player ");
		        		////sendMessage( Constants.Integers.STOPSPIN );//we must have been waiting ////startmediaPlayer() has its own sendMessage
		        		startMediaPlayer();
		        	}
	        		
	        	} catch ( FileNotFoundException e ) {
	        		Log.e(TAG, e.toString());
	        		Log.e(TAG, "Can't find file. Android must have deleted it on a clean up :-(" );
	        	} catch  (IllegalStateException	e) {
	        		Log.e(TAG, e.toString());
	        		sendMessage( Constants.Integers.TROUBLEWITHAUDIO);
	        		return;
	        	} catch  (IOException	e) {
	        		Log.e(TAG, e.toString());
	        		e.printStackTrace();
	        		sendMessage( Constants.Integers.TROUBLEWITHAUDIO);
	        		return;
	        	} catch  (Exception	e) {
	        		Log.e(TAG, e.toString());
	        		e.printStackTrace();
	        		sendMessage( Constants.Integers.TROUBLEWITHAUDIO);
	        		return;
	        	}
 	        }
	    };

	    preparringthread = new Thread(r);
	    preparringthread.start();
	    
    }
   
    // Removed file from cache
    // Also Copy the file to the bigger File
    private void removeFile (){
    	File temp = new File(context.getCacheDir(),DOWNFILE + playedcounter);
    	File tempDecrypted = new File(context.getCacheDir(),DOWNFILE + playedcounter+Constants.Strings.DECRYPTED_PART);
    	
    	//// copy the downloaded part to the complete file
    	File song = new File(context.getCacheDir(), playlistSongIDs.getCurrent());
    	Log.d(TAG, "song file: "+song.getAbsolutePath());
    	
    	FileInputStream fis = null;
    	FileOutputStream fos = null;
    	
    	try{
    		fis = new FileInputStream(temp);
    		fos = new FileOutputStream(song, true);//// we need to gather all files into one
    		byte[]  buff = new byte[8192];
	        int numChars;
	        while ( (numChars = fis.read(  buff, 0, buff.length ) ) != -1) {
	        	fos.write( buff, 0, numChars );
  		    }
    		fos.flush();

    	}
    	catch(FileNotFoundException fnfe){
    		Log.e(TAG, fnfe.getMessage());
    		
    	}
    	catch(IOException ioe){
    		ioe.printStackTrace();
    		Log.e(TAG, ioe.getMessage());
    	}
    	finally{
    		try{
        		if(fis != null)
        			fis.close();
        		if(fos!= null)
        			fos.close();
    		}
    		catch(IOException ex){
    			ex.printStackTrace();
    			Log.e(TAG, ex.getMessage());
    		}
    	}
    	//// end of copy the downloaded part to the complete file
    	Log.d(TAG, "Media player size: "+mediaplayers.size());
    	if(((int)songPlayedTime == songPlayExpectedTime || downloadCompleted) && song.exists() && mediaplayers.size() < 1){
    		////download completed. this song is fully downloaded
    		Log.i(TAG, "removeFile() DOWNLOAD completed - ready to move it to the SDCard");
    		File newCatalogFile = new File(new File(Constants.Strings.MUSIX_PATH), playlistSongIDs.getCurrent()+Constants.Strings.DOT+Constants.Strings.ENCRYPTED_SONG_EXTENSION);
    		copyFiles(song, newCatalogFile);
    		song.delete();
    		playNextSong();
    	}
    	
    	Log.d(TAG, temp.getAbsolutePath());
    	temp.delete();
    	tempDecrypted.delete();
    	
    }
    
    
    
    //Start first audio clip
    private void startMediaPlayer() {
    	if(mediaplayers == null || mediaplayers.size() < 1){
    		Log.d(TAG, "startMediaPlayer() size is 0. return");
    		return;
    	}
    	
    	//Grab out first media player
    	MediaPlayer mp = mediaplayers.get(0);
    	try {
			mp.prepare();
		} catch (IllegalStateException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
    	Log.d(TAG, "startMediaPlayer() Start Player");
    	if(!mp.isPlaying())
    		mp.start(); 
    	
    	sendMessage(Constants.Integers.STOPSPIN);
    }
    
    private void pause(){
    	if(mediaplayers.size()>0){
    		MediaPlayer mp = mediaplayers.get(0);
        	Log.d(TAG,"Pause Player");
        	mp.pause();
    	}
    }
    
    private void removeAllTempFiles(){
    	if(!processHasStarted){
    		File dir =context.getCacheDir();
    		
    		for (File file : dir.listFiles(/*new FilenameFilter() {
    			
    			@Override
    			public boolean accept(File dir, String filename) {
    				if(filename.contains(DOWNFILE)){
    					return true;
    				}
    				return false;
    			}
    		}*/)) {
    			file.delete();
    		}
    	}
    	
    }
    
    //Stop
    public void stopService(){
    	
    	Log.d(TAG,"stop() Entering the method");
    	
    	cleanup();
    	
    	stopSelf();//// service 
    }
    
    private void cleanup() {
    	
    	if (mediaplayers != null){
			if (! mediaplayers.isEmpty() ){
    			final MediaPlayer mp = mediaplayers.get(0);
    			if (mp.isPlaying()){
    				mp.stop();  
    			}
    			for (MediaPlayer player : mediaplayers) {
					player.release();
				}
    			while(mediaplayers.size()>0){
    				mediaplayers.remove(0);
    			}
    		}
		}
    	processHasStarted = false;
      	if ( preparringthread != null) {
      		preparringthread.interrupt();
      	}
      	removeAllTempFiles();
	}

    //Is the streamer playing audio?
    public boolean isPlayingAudio() {
    	return processHasStarted ;
    }
    
    private void playLocalFile(File f){
    	//sendMessage(Constants.Integers.SPIN);
    	Log.d(TAG, "playing local file: "+f.getAbsolutePath());
    	
		MXSettings mSettings = new MXSettings();
    	//SharedPreferences prefs = this.getSharedPreferences(MXSettings.PREF_FILE_NAME, Context.MODE_PRIVATE); //PreferenceManager.getDefaultSharedPreferences(context);
  	  //  String encKey = prefs.getString(MXSettings.PREF_KEY_ENCRIPTION, "0342");//// right now does not work
		String encKey = mSettings.getEncryptionKey();
  	    Log.i(TAG, "encryption Key: "+encKey+", "+context.getPackageName()+". "+context.toString());
  	    decrypter = new Decrypter();
  	    decrypter.init(encKey);
  	    //// enf of code to get Encryption key from Shared Preferences
    	
    	MediaPlayer mp = new MediaPlayer();
    	try {
    		final File tempDecrypted = new File(context.getCacheDir(), f.getName() + Constants.Strings.DECRYPTED_PART);
    		Log.d(TAG, "temp decryptedFile: "+tempDecrypted.getAbsolutePath());
    		boolean createdFile = tempDecrypted.createNewFile();
    		Log.d(TAG, "temp decryptedFile created: "+createdFile);
    		MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener () {

				@Override
				public void onCompletion(MediaPlayer mp) {
					Log.d(TAG, "play next song by completion listener. ");
					mediaplayers.remove(mp);
					mp.release();
					mp=null;
					tempDecrypted.delete();
					playNextSong();
					
				}
    			
    		};
    		
    		File tempF = decrypter.decryptToFile(f, tempDecrypted);
    		
    		FileInputStream ins = new FileInputStream( tempF );
        	mp.setDataSource(ins.getFD());
        	ins.close();
    		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
    		
    		mp.setOnCompletionListener(listener);
    		
    		Log.d(TAG, "playLocalFile() Prepare synchronously.");
    		mp.prepare();
    		
    		
    		mediaplayers.add(mp);
    		
    		mp.start();
    		sendMessage(Constants.Integers.STOPSPIN);
    		//startMediaPlayer();
        	
    	}
    	catch(Exception e){
    		
    	}
    }
    
    private void playNextSong(){
    	if(playlistSongIDs.hasNext()){
//    		sendMessage(Constants.Integers.SPIN);
    		cleanup();
    		playMedia(playlistSongIDs.getNext());
    	}
    	else{
    		sendMessage(Constants.Integers.NO_NEXT_SONG);
    	}
    }
    
    private void playPreviousSong(){
    	if(playlistSongIDs.hasPrevious()){
//    		sendMessage(Constants.Integers.SPIN);
    		cleanup();
    		playMedia(playlistSongIDs.getPrevious());
    	}
    	else{
    		sendMessage(Constants.Integers.NO_PREV_SONG);
    	}
    }
    
    private boolean checkFileOnSDCard(String mediaID){
    	File cardDir = new File(Constants.Strings.MUSIX_PATH);
    	if(!cardDir.exists())
    		cardDir.mkdirs();
    	
    	File mediaIDFile = new File(cardDir, mediaID + Constants.Strings.DOT + Constants.Strings.ENCRYPTED_SONG_EXTENSION);
    	return mediaIDFile.exists();
    }
    
    private void playMedia(final String mediaID){
    	songPlayedTime = 0;
//    	Runnable runnable = new Runnable() {   
//     		public void run() {   
     			sendMessage(Constants.Integers.SPIN);
     			sendMessage(mediaID, playlistSongIDs.hasNext(), playlistSongIDs.hasPrevious());//// tell UI which song will be played
//    	    }   
//    	};   
//    	Thread thr = new Thread(runnable);
//    	thr.start();
    	
    	boolean fileExists = checkFileOnSDCard(mediaID);
    	if(fileExists){
    		File f = new File(new File(Constants.Strings.MUSIX_PATH), mediaID + Constants.Strings.DOT + Constants.Strings.ENCRYPTED_SONG_EXTENSION);
    		Log.d(TAG, "File exists: "+f.getAbsolutePath());
    		playLocalFile(f);
    	}
    	else{
    		if(appMode!=Constants.Integers.MXAppModeNormal){
    			sendMessage(Constants.Integers.TROUBLEWITHAUDIO);////may need to be changed
    			return;
    		}
    		final String audiourl = String.format(url, new Object[] {mediaID} );
//    		final String audiourl = String.format("http://62.209.149.66/%s.mp3", new Object[] {mediaID} );
//    		downloadingMediaFile = new File(context.getCacheDir(),DOWNFILE + tempFileCounter); ////naming files with counter
//         	downloadingMediaFile.deleteOnExit();

         	Runnable r = new Runnable() {   
         		public void run() {   
         			try {
         					startStreaming( audiourl );
    				} catch (IOException e) {
    					e.printStackTrace();
    					Log.d(TAG, e.toString() );
    				} 
        	    }   
        	};   
        	Thread t = new Thread(r);
        	t.start();
    	}
    }
    
    //Send Message to Activity that is controlling the service
    private void sendMessage(int m){
    	String TAG = "sendMessage";
    	Intent i = new Intent(Constants.Strings.tPLAY);

    	i.putExtra(Constants.Strings.MSG, m);
    	Log.d(TAG, "Broadcasting intent: "+m);
    	context.sendBroadcast (i) ;
    }
    
    //Send Message to Activity that is controlling the service on how many seconds the player has played
    private void sendSeconds(int m){
    	
    	Intent i = new Intent(Constants.Strings.SONGPLAYEDTIME);

    	i.putExtra(Constants.Strings.MSG, m);
//    	Log.d(TAG, "Broadcasting intent(number of seconds): "+m);
    	context.sendBroadcast (i) ;
    }
    
    private void sendMessage(String currentSongName, boolean hasNext, boolean hasPrevious){
    	String TAG = "sendMessage";
    	Intent i = new Intent(Constants.Strings.tPLAY);

    	i.putExtra(Constants.Strings.MSG, Constants.Integers.CURRENT_SONG);
    	i.putExtra(Constants.Strings.CURRENT_SONG, currentSongName);
    	i.putExtra(Constants.Strings.CURRENT_SONG_HAS_NEXT, hasNext);
    	i.putExtra(Constants.Strings.CURRENT_SONG_HAS_PREVIOUS, hasPrevious);
    	Log.d(TAG, "Broadcasting intent: "+Constants.Integers.CURRENT_SONG);
    	context.sendBroadcast (i) ;
    }
    
    private boolean copyFiles(File copyFrom, File copyTo){
    	boolean successful = false;
    	FileInputStream fis = null;
    	FileOutputStream fos = null;
    	
    	try{
    		fis = new FileInputStream(copyFrom);
    		fos = new FileOutputStream(copyTo);
    		byte[]  buff = new byte[8192];
	        int numChars;
	        while ( (numChars = fis.read(  buff, 0, buff.length ) ) != -1) {
	        	fos.write( buff, 0, numChars );
  		    }
    		fos.flush();

    		successful = true;
    	}
    	catch(FileNotFoundException fnfe){
    		Log.e(TAG, fnfe.getMessage());
    		successful = false;
    	}
    	catch(IOException ioe){
    		ioe.printStackTrace();
    		Log.e(TAG, ioe.getMessage());
    		successful = false;
    	}
    	finally{
    		try{
        		if(fis != null)
        			fis.close();
        		if(fos!= null)
        			fos.close();
    		}
    		catch(IOException ex){
    			Log.e(TAG, ex.getMessage());
    			ex.printStackTrace();
    			successful = false;
    		}
    	}
    	return successful;
    }
    
}


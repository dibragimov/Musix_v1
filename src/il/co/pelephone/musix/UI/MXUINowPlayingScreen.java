package il.co.pelephone.musix.UI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import il.co.pelephone.musix.UI.MXUIMusixMainScreen.Tabs;
import il.co.pelephone.musix.UI.MediaPlayer.IStreamingMediaPlayer;
import il.co.pelephone.musix.UI.MediaPlayer.StreamingMediaPlayer;
import il.co.pelephone.musix.UI.utility.Constants;
import il.co.pelephone.musix.UI.utility.MXUINowPlayingPopupAdapter;
import il.co.pelephone.musix.UI.utility.MXUIUtilityMyMusicSongAdapter;
import il.co.pelephone.musix.UI.utility.Stopwatch;
import il.co.pelephone.musix.comm.IMXImageCallback;
import il.co.pelephone.musix.comm.MXCoverImagesManager;
import il.co.pelephone.musix.comm.MXSettings;
import il.co.pelephone.musix.comm.MXCoverImagesManager.ImageSize;
import il.co.pelephone.musix.comm.MXSettings.MXAppMode;
import il.co.pelephone.musix.data.MXDatabaseHelper;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata;
import il.co.pelephone.musix.data.Song;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.SongsTableMetaData;
import il.co.pelephone.musix.utility.MXMessages;
import il.co.pelephone.musix.utility.MXMessagesCallback;
import il.co.pelephone.musix.utility.MXMessages.MGButtonCode;
import il.co.pelephone.musix.utility.MXMessages.MGLanguage;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MXUINowPlayingScreen extends Activity implements ServiceConnection, MXMessagesCallback, IMXImageCallback {

	final String TAG = "MXUINowPlayingScreen";
	private IStreamingMediaPlayer playerService;
	IntentFilter playerIntentFilter = new IntentFilter(Constants.Strings.tPLAY);
	IntentFilter myMusicIntentFilter = new IntentFilter(Constants.Strings.INTENT_PLAYLIST_SONGS);
	IntentFilter songPlayedTimeIntentFilter = new IntentFilter(Constants.Strings.SONGPLAYEDTIME);
	private PopupWindow popupPlaylist;
	private ArrayList<Song> playlistSongsCollection; 
	private Song startSong;
	private ImageButton popupBtn;
	private ImageButton playBtn;
	
	String currentSongID = null;
	String currentSongAlbumID = null;
	private TextView txtPlayerHeader;
	private ProgressBar songProgress;
	
	private MXSettings mSettings; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.nowplaying);
		
		initializeAllButtons();
		
		txtPlayerHeader = (TextView)findViewById(R.id.txtPlayerHeader);
		
		songProgress = (ProgressBar)findViewById(R.id.prgrsBarPlayedTimePlayer);
		
		mSettings = new MXSettings(this);
		mSettings.Load();
		
	}

	private void initializeAllButtons() {
		ImageButton btnNext = (ImageButton)findViewById(R.id.btnPlayNext);
		btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playNext();
				closePlaylistPopupIfOpen();
			}
		});
		btnNext.setEnabled(false);
		
		ImageButton btnPrev = (ImageButton)findViewById(R.id.btnPlayPrevious);
		btnPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playPrevious();
				closePlaylistPopupIfOpen();
			}
		});
		btnPrev.setEnabled(false);
		
		playBtn = (ImageButton)findViewById(R.id.btnPlayerPlay);
		playBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageButton b = (ImageButton)v;
				b.setSelected(!b.isSelected());
				////changed places to have it react faster
				if(!b.isSelected())
					pausePlaying();
				else
					startPlaying();
				
				closePlaylistPopupIfOpen();
			}
		});
		playBtn.setEnabled(false);
		
		ImageButton repeatBtn = (ImageButton)findViewById(R.id.btnPlayerRefresh);
		repeatBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageButton b = (ImageButton)v;
				b.setSelected(!b.isSelected());
				////changed places to have it react faster
				if(playerService != null)
					try {
						playerService.setRepeatPlayback(b.isSelected());
					} catch (RemoteException e) {
						// TODO: probably need to show a message to a user
						e.printStackTrace();
						b.setSelected(!b.isSelected());//// return back and probably need to show message
					}
				closePlaylistPopupIfOpen();
			}
		});
		repeatBtn.setEnabled(false);
		
		ImageButton shuffleBtn = (ImageButton)findViewById(R.id.btnPlayerShuffle);
		shuffleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageButton b = (ImageButton)v;
				b.setSelected(!b.isSelected());
				////changed places to have it react faster
				if(playerService != null)
					try {
						playerService.setShufflePlaylist(b.isSelected());
					} catch (RemoteException e) {
						// TODO: probably need to show a message to a user
						e.printStackTrace();
						b.setSelected(!b.isSelected());//// return back and probably need to show message
					}
				closePlaylistPopupIfOpen();
			}
		});
		shuffleBtn.setEnabled(false);
		
		popupBtn = (ImageButton)findViewById(R.id.btnPlayerPlaylistPopup);
		popupBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "popup button clicked");
				ImageButton btn = (ImageButton)v;
				btn.setSelected(!btn.isSelected());
				Log.d(TAG, "popup button name: "+btn.getId());
				if(btn.isSelected()){
					Log.d(TAG, "popup button selected");
					showPopup();
				}
				else{
					Log.d(TAG, "popup button not selected");
					if(popupPlaylist != null){
						popupPlaylist.dismiss();
						Log.d(TAG, "popup dismissed");
					}
				}
			}
		});
		popupBtn.setEnabled(false);
		
		ImageButton btnBack = (ImageButton)findViewById(R.id.btnPlayerBackToPrevScreen);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((MXUIMusixMainScreen)getParent()).setCurrentTab(Tabs.MyMusic);
				closePlaylistPopupIfOpen();
			}
		});
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		registerReceiver (playerReceiver, playerIntentFilter);
		registerReceiver(songReceiver, myMusicIntentFilter);
		registerReceiver(songPlayedTimeReceiver, songPlayedTimeIntentFilter);
		Intent in = new Intent(IStreamingMediaPlayer.class.getName());//NowPlayingScreen.this, StreamingMediaPlayer.class);
		Log.d(TAG, "binding Player Service "+IStreamingMediaPlayer.class.getName());
		getParent().bindService (in, this , Context.BIND_AUTO_CREATE);
		Log.d(TAG, "onStart() called");
	}

	@Override
	protected void onStop() {
		if(playerReceiver != null)
			unregisterReceiver(playerReceiver);
		if(songReceiver != null)
			unregisterReceiver(songReceiver);
		if(songPlayedTimeReceiver != null)
			unregisterReceiver(songPlayedTimeReceiver);
		getParent().unbindService(this);
		
		if(popupPlaylist != null && popupPlaylist.isShowing())
			popupPlaylist.dismiss();
		
		super.onStop();
		Log.d(TAG, "onStop() called");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy() called");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart() called");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume() called");
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "Player Service instantiated");
		playerService = IStreamingMediaPlayer.Stub.asInterface(service);
		//// if service is running - make play button - pause button
		try {
			if(playerService.isPlaying()){
				Log.d(TAG, "::::::::::::::::::: service is playing");
				String catalogMediaID = playerService.getSongID();
				int elapsedSecs = playerService.getPlayedTime();
				Log.d(TAG, " elapsedSecs: "+elapsedSecs);
				
				boolean hasNext = true;
				boolean hasPrev = true;
				updateButtonsOnMessageArrival(catalogMediaID, hasNext, hasPrev);
				currentSongID = catalogMediaID;
				
				currentSongAlbumID = getAlbumID(catalogMediaID);
				if(currentSongAlbumID != null){
					MXCoverImagesManager.getInstance().getImagesForAlbums(MXUINowPlayingScreen.this, new String[]{currentSongAlbumID}, ImageSize.SIZE_110);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {

	}

	private void startPlaying(){
		try {
    		Log.d(TAG, "Is service playing audio? " + ((playerService == null)?"":playerService.isPlaying()) );
    		
    		playerService.startAudio();
    	}catch (RemoteException e) {
    		Log.e(TAG, "ServiceConnection.onServiceConnected", e);
        }
	}
	
	private void pausePlaying(){
		try {
			Log.d(TAG, "pausing service: ");
			playerService.pauseAudio();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
    	Log.d(TAG, "Done pausing audio");
	}
	
	private void playNext(){
		try{
			Log.d(TAG, "playNext() service: ");
			playerService.playNext();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void playPrevious(){
		try{
			Log.d(TAG, "playPrevious() service: ");
			playerService.playPrevious();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void showPopup(){
		if(popupPlaylist != null){
			View viewToShow = popupPlaylist.getContentView();
			ListView playlistSongs = (ListView)viewToShow.findViewById(R.id.lstSongsCurrentPlaylist);
			TextView txtAlbumName = (TextView)viewToShow.findViewById(R.id.txtPlayerHeader1);
			txtAlbumName.setText(txtPlayerHeader.getText());
			if(playlistSongsCollection != null && startSong != null){
				MXUINowPlayingPopupAdapter adapter = (MXUINowPlayingPopupAdapter)playlistSongs.getAdapter();
				adapter.setCurrentSong(currentSongID);
				adapter.notifyDataSetChanged();
				//playlistSongs.setAdapter(adapter);
				popupPlaylist.showAtLocation(getParent().getWindow().getDecorView(), Gravity.NO_GRAVITY, 0, 0);
			}
			Log.d(TAG, "playlist popup not null. shown.");
			return;
		}
		else{
			LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			View viewToShow = inflater.inflate(R.layout.popuplaylist, null);
			ListView playlistSongs = (ListView)viewToShow.findViewById(R.id.lstSongsCurrentPlaylist);
			TextView txtAlbumName = (TextView)viewToShow.findViewById(R.id.txtPlayerHeader1);
			txtAlbumName.setText(txtPlayerHeader.getText());
			if(playlistSongsCollection != null && startSong != null){
				playlistSongs.setAdapter(MXUINowPlayingPopupAdapter.Instance(this, playlistSongsCollection, startSong));
				playlistSongs.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
						Song s = (Song)MXUINowPlayingPopupAdapter.Instance(MXUINowPlayingScreen.this, playlistSongsCollection, startSong).getItem(position);
						Intent playlistIntent = new Intent(Constants.Strings.INTENT_PLAYLIST_SONGS);
						playlistIntent.putParcelableArrayListExtra(Constants.Strings.PLAYLIST_SONGS, playlistSongsCollection);
						playlistIntent.putExtra(Constants.Strings.SELECTED_SONG, s);
						sendBroadcast(playlistIntent);
						popupPlaylist.dismiss();
						popupBtn.setSelected(false);
						Log.d(TAG, "popup song clicked: "+s.catalogMediaID);
						Log.d(TAG, "popup song clicked name: "+s.name);
					}
				});
			}
			((ImageButton)viewToShow.findViewById(R.id.btnPlayerPlaylistPopup1)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					popupPlaylist.dismiss();
					popupBtn.setSelected(false);
					}
			});
			((ImageButton)viewToShow.findViewById(R.id.btnPlayerBackToPrevScreen1)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					popupPlaylist.dismiss();
					popupBtn.setSelected(false);
					((MXUIMusixMainScreen)getParent()).setCurrentTab(Tabs.MyMusic);
				}
			});
			popupPlaylist = new PopupWindow(viewToShow, 320, 225+45);
//			popupPlaylist.setFocusable(true);
			popupPlaylist.setTouchable(true);
			popupPlaylist.showAtLocation(getParent().getWindow().getDecorView(), Gravity.NO_GRAVITY, 0, 0);//.showAsDropDown(findViewById(R.id.panelPlayerHeader));
			Log.d(TAG, "playlist popup shown. was null before.");
		}
	}
	
	private void closePlaylistPopupIfOpen(){
		if (popupPlaylist != null && popupPlaylist.isShowing()){
			popupPlaylist.dismiss();
			popupBtn.setSelected(false);
		}
		
	}
	
	
	
	private void updateButtonsOnMessageArrival(String currentSong, boolean hasNext, boolean hasPrev){
		ImageButton btnNext = (ImageButton)findViewById(R.id.btnPlayNext);
		btnNext.setEnabled(hasNext);
		ImageButton btnPrev = (ImageButton)findViewById(R.id.btnPlayPrevious);
		btnPrev.setEnabled(hasPrev);
		Song s = getCurrentSong(currentSong);
		if(s != null){
			((TextView)findViewById(R.id.txtPlayerSongName)).setText(s.name);
			((TextView)findViewById(R.id.txtPlayerAlbumName)).setText(s.ArtistName);
			((TextView)findViewById(R.id.txtPlayerDuration)).setText(convertToTime(s.duration));
			((TextView)findViewById(R.id.txtPlayerDuration)).setTag(""+s.duration);
		}
		playBtn.setSelected(true);
		playBtn.setEnabled(true);
	}
	
	private void enableAllPlayButtons(){
		playBtn.setClickable(true);
		playBtn.setEnabled(true);
		popupBtn.setClickable(true);
		popupBtn.setEnabled(true);
		ImageButton btnNext = (ImageButton)findViewById(R.id.btnPlayNext);
		btnNext.setClickable(true);
		btnNext.setEnabled(true);
		ImageButton btnPrev = (ImageButton)findViewById(R.id.btnPlayPrevious);
		btnPrev.setClickable(true);
		btnPrev.setEnabled(true);
		ImageButton repeatBtn = (ImageButton)findViewById(R.id.btnPlayerRefresh);
		repeatBtn.setClickable(true);
		repeatBtn.setEnabled(true);
		ImageButton shuffleBtn = (ImageButton)findViewById(R.id.btnPlayerShuffle);
		shuffleBtn.setClickable(true);
		shuffleBtn.setEnabled(true);
	}
	
	private void disableAllPlayButtons(){
//		playBtn.setEnabled(true);
		popupBtn.setClickable(false);
		ImageButton btnNext = (ImageButton)findViewById(R.id.btnPlayNext);
		btnNext.setClickable(false);
		
		ImageButton btnPrev = (ImageButton)findViewById(R.id.btnPlayPrevious);
		btnPrev.setClickable(false);
		
		ImageButton repeatBtn = (ImageButton)findViewById(R.id.btnPlayerRefresh);
		repeatBtn.setClickable(false);
		
		ImageButton shuffleBtn = (ImageButton)findViewById(R.id.btnPlayerShuffle);
		shuffleBtn.setClickable(false);
	}
	
	private Song getCurrentSong(String currentCatalogMediaID){
		Song song = null;
		if(playlistSongsCollection != null){
			for (
				Song s : playlistSongsCollection) {
				if(s.catalogMediaID.equals(currentCatalogMediaID)){
					song = s;
					break;
				}
			}
		}
		else
			song = getCurrentSongByIDFromDB(currentCatalogMediaID);
		return song;
	}
	
	private BroadcastReceiver playerReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int msg = intent.getIntExtra(Constants.Strings.MSG, -1);
			switch (msg) {
				case Constants.Integers.SPIN:
					Log.i(TAG, "spinning");
					startSplash();
					break;

				case Constants.Integers.STOPSPIN:
					Log.i(TAG, "end spinning");
					stopSplash();
					break;
				
				case Constants.Integers.TROUBLEWITHAUDIO:
					Log.i(TAG, "trouble with audio");
					stopSplash();
					showMessageAlert(MXMessages.MSG_ERROR_DOWNLOADING);
					break;
				
				case Constants.Integers.STOP:
					Log.i(TAG, "stop");
					playBtn.setSelected(false);
					break;
				case Constants.Integers.CURRENT_SONG:
					Log.i(TAG, "current song");
					
					String catalogMediaID = intent.getStringExtra(Constants.Strings.CURRENT_SONG);
					boolean hasNext = intent.getBooleanExtra(Constants.Strings.CURRENT_SONG_HAS_NEXT, false);
					boolean hasPrev = intent.getBooleanExtra(Constants.Strings.CURRENT_SONG_HAS_PREVIOUS, false);
					//stopSplash();
					updateButtonsOnMessageArrival(catalogMediaID, hasNext, hasPrev);
					currentSongID = catalogMediaID;
					
					currentSongAlbumID = getAlbumID(catalogMediaID);
					if(currentSongAlbumID != null){
						MXCoverImagesManager.getInstance().getImagesForAlbums(MXUINowPlayingScreen.this, new String[]{currentSongAlbumID}, ImageSize.SIZE_110);
					}
					
					break;
				
				default:
					break;
			}
		}
		
	};
	
	private BroadcastReceiver songReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			songProgress.setProgress(0);////start all over
			((TextView)findViewById(R.id.txtPlayerPlayedTime)).setText(convertToTime(0));////start all over
			startSong = (Song)intent.getParcelableExtra(Constants.Strings.SELECTED_SONG) ;
			ArrayList<Parcelable> playlistSongsParcelable =(ArrayList<Parcelable>)intent.getParcelableArrayListExtra(Constants.Strings.PLAYLIST_SONGS);
			playlistSongsCollection = new ArrayList<Song>();
			String[] songs = new String[playlistSongsParcelable.size()];
			int i=0;
			for (Parcelable parcelable : playlistSongsParcelable) {
				Song s = (Song)parcelable;
				playlistSongsCollection.add(s);
				songs[i] = s.catalogMediaID;
				i++;
			}
			try {
	    		Log.d(TAG, "Is service playing audio? " + ((playerService == null)?"":playerService.isPlaying()) );
	    		
	    		final Intent playerIntent = new Intent(MXUINowPlayingScreen.this, StreamingMediaPlayer.class);
	    		playerIntent.putExtra(Constants.Strings.CATALOGMEDIAID, startSong.catalogMediaID );
	    		playerIntent.putExtra(Constants.Strings.PLAYLIST_SONGS, songs);
	    		if(mSettings.getAppMode()==MXAppMode.MXAppModeNormal)
	    			playerIntent.putExtra(Constants.Strings.MODE, Constants.Integers.MXAppModeNormal);
	    		if(mSettings.getAppMode()==MXAppMode.MXAppModeOffline)
	    			playerIntent.putExtra(Constants.Strings.MODE, Constants.Integers.MXAppModeOffline);
	    		if(mSettings.getAppMode()==MXAppMode.MXAppModeDisabled)
	    			playerIntent.putExtra(Constants.Strings.MODE, Constants.Integers.MXAppModeDisabled);
	    		Log.d(TAG, "startService(i)");
	    		
	    		Runnable r = new Runnable() {
					
					@Override
					public void run() {
						startService(playerIntent) ;//startSplash();
					}
				};
				new Thread(r).start();
				
	    		 
	    		if(popupPlaylist != null && popupPlaylist.isShowing()){
	    			popupPlaylist.dismiss();
	    			showPopup();
	    		}
	    		enableAllPlayButtons();
	    		
	    	}catch (RemoteException e) {
	    		Log.e(TAG, "songReceiver.onReceive() ", e);
	        }
		}
		
	};
	
	private BroadcastReceiver songPlayedTimeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int msg = intent.getIntExtra(Constants.Strings.MSG, -1);
			int duration = Integer.parseInt(((TextView)findViewById(R.id.txtPlayerDuration)).getTag().toString());
			songProgress.setProgress((msg*100/duration));
//			Log.d(TAG, "progress set");
			((TextView)findViewById(R.id.txtPlayerPlayedTime)).setText(convertToTime(msg));
		}
		
	};
	
	private void startSplash(){
		findViewById(R.id.prgrsBarPlayer).setVisibility(View.VISIBLE);
		playBtn.setVisibility(View.GONE);
		disableAllPlayButtons();
	     
	}
	
	private void stopSplash(){
		findViewById(R.id.prgrsBarPlayer).setVisibility(View.GONE);
		playBtn.setVisibility(View.VISIBLE);
		enableAllPlayButtons();
	}

	
	private String convertToTime(int duration){
    	int minutes = (int)(duration / 60);
    	int seconds = duration - minutes * 60;
    	String response = ""+minutes+":"+seconds;
    	if(seconds < 10){
    		response = ""+minutes+":0"+seconds;
    	}
    	return response;
    }

	public void showMessageAlert(String err)
	{
		//Log.d(LOGTAG, "MXMessages.getInstance");
		MXMessages message=MXMessages.getInstance();	    
		message.setLanguage(MGLanguage.MGLanguageHebrew);				    
		message.displayMessageForCode(this , err , this );		
		playBtn.setSelected(false);
		playBtn.setEnabled(false);
	}
	
	@Override
	public void buttonClicked(MGButtonCode buttonCode, String messageCode) {
		if (messageCode.equals(MXMessages.MSG_ERROR_DOWNLOADING))
		{
			////do nothing right now
		}
	}

	@Override
	public void imagesReceived(HashMap<String, Bitmap> images, boolean isPlaylistImages) {
		Log.d(TAG, "images received for playback");
		if(images == null || images.size() == 0 || currentSongID == null){
			Log.d(TAG, "smth wrong");
			return;
		}
		final Bitmap img = images.get(currentSongAlbumID);
		if(img!=null){
			final ImageView coverImg = (ImageView)findViewById(R.id.imgPlayerAlbumCover);
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					coverImg.setImageBitmap(img);
					Log.d(TAG, "coverImg.setImage()");
				}
			};
			coverImg.post(r);
		}
	}
	
	private String getAlbumID(String catalogMediaID){
		String albumID = null;
		String albumName = null;
		String myPath = MXDatabaseHelper.DB_PATH+MXDatabaseHelper.DB_NAME;
		SQLiteDatabase db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		Cursor c = db.rawQuery("select catalogAlbumID, albumName from tblAlbums left join tblSongs on tblSongs.albumID=tblAlbums._id where tblSongs.catalogMediaID = ?", new String[]{catalogMediaID});
		if(c!= null && c.getCount()>0){
			c.moveToFirst();
			albumID = c.getString(0);
			albumName = c.getString(1);
			txtPlayerHeader.setText(albumName);
		}
		c.close();
		db.close();
		c=null;
		Log.d(TAG, "getAlbumID finished");
		return albumID;
	}
	
	private Song getCurrentSongByIDFromDB(String catalogMediaID){
		Cursor curSongs;
		Song s = null;
		Log.d(TAG, "getCurrentSongByIDFromDB: catalogMediaID: "+catalogMediaID);
		curSongs = this.managedQuery(Uri.withAppendedPath(SongsTableMetaData.CONTENT_URI_CATALOGMEDIA, catalogMediaID), null, null, null, SongsTableMetaData.DEFAULT_SORT_ORDER);
		if(curSongs.getCount()>0){
			curSongs.moveToFirst();
			s = new Song(curSongs.getInt(0), curSongs.getString(1), curSongs.getString(2), curSongs.getString(3), curSongs.getString(4), curSongs.getString(5), curSongs.getInt(6), curSongs.getString(7));
			Log.d(TAG, "song retrieved from DB: "+s.name);
		}
		curSongs.close();
		curSongs = null;
		return s;
	}
}

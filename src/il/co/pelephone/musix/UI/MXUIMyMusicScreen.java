package il.co.pelephone.musix.UI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import il.co.pelephone.musix.UI.MXUIMusixMainScreen.Tabs;
import il.co.pelephone.musix.UI.utility.Constants;
import il.co.pelephone.musix.UI.utility.MXUIUtilityMyMusicAdapter;
import il.co.pelephone.musix.UI.utility.MXUIUtilityMyMusicAlbumAdapter;
import il.co.pelephone.musix.UI.utility.MXUIUtilityMyMusicAlbumAdapterGrid;
import il.co.pelephone.musix.UI.utility.MXUIUtilityMyMusicAllAdapters;
import il.co.pelephone.musix.UI.utility.MXUIUtilityMyMusicArtistAdapter;
import il.co.pelephone.musix.UI.utility.MXUIUtilityMyMusicBaseAdapter;
import il.co.pelephone.musix.UI.utility.MXUIUtilityMyMusicGenresAdapter;
import il.co.pelephone.musix.UI.utility.MXUIUtilityMyMusicPlaylistAdapter;
import il.co.pelephone.musix.UI.utility.MXUIUtilityMyMusicPlaylistAdapterGrid;
import il.co.pelephone.musix.UI.utility.MXUIUtilityMyMusicSongAdapter;
import il.co.pelephone.musix.UI.utility.ScreenInfo;
import il.co.pelephone.musix.comm.IMXImageCallback;
import il.co.pelephone.musix.comm.IMXSyncCallback;
import il.co.pelephone.musix.comm.MXSettings;
import il.co.pelephone.musix.comm.MXSync;
import il.co.pelephone.musix.comm.MXSyncRequest;
import il.co.pelephone.musix.data.Album;
import il.co.pelephone.musix.data.Artist;
import il.co.pelephone.musix.data.Genre;
import il.co.pelephone.musix.data.Playlist;
import il.co.pelephone.musix.data.Song;
import il.co.pelephone.musix.utility.MXMessages;
import il.co.pelephone.musix.utility.MXMessagesCallback;
import il.co.pelephone.musix.utility.MXMessages.MGButtonCode;
import il.co.pelephone.musix.utility.MXMessages.MGLanguage;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MXUIMyMusicScreen extends ListActivity implements IMXSyncCallback, MXMessagesCallback, IMXImageCallback {

	public static final int MYMUSICMAINSCREEN = -1;
	public static final int MYMUSICALLSONGSSCREEN = 0;
	public static final int MYMUSICSONGLISTSCREEN = 5;
	public static final int MYMUSICPLAYLISTSCREEN = 1;
	public static final int MYMUSICGENRESCREEN = 2;
	public static final int MYMUSICARTISTSCREEN = 3;
	public static final int MYMUSICALBUMSCREEN = 4;
	
	public static final int STOPSPLASH = 1;
	public static final int SHOWSPLASH = 2;
	public static final int UPDATELIST = 100;
	
	private int currentScreen;
	private  GridView grid;
	private ImageButton btnSwitchToGrid;
	private ImageButton btnSwitchToList; 
	private MXUIUtilityMyMusicAllAdapters allAdapters;
	private PopupWindow popupAbout;
	private PopupWindow popupSplash;
	private AnimationDrawable animSplash;
	private Stack<ScreenInfo> screens;
	private Button btnGoBack;
	private String TAG="MXUIMyMusicScreen";
	
	private MXSettings mSettings;
	
	private static final  String DFLT_VERSION="1.1";
	private boolean isRegisterAccepted=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mymusic);
		grid = (GridView)findViewById(R.id.grid);		
		
		initializeAllButtons();
		
		mSettings = new MXSettings(this);
		mSettings.Load();
		
		screens = new Stack<ScreenInfo>();
		
		allAdapters = MXUIUtilityMyMusicAllAdapters.getInstance(this);
		setListAdapter(allAdapters.getGeneralMyMusicAdapter());
		currentScreen = MYMUSICMAINSCREEN;
		screens.push(new ScreenInfo(getResources().getString(R.string.mymusic_title_mymusic), MYMUSICMAINSCREEN, -1));
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.d("MXUIMyMusicScreen", "onPause");
	//// if popup displaying - stop it
		if(popupSplash != null && popupSplash.isShowing())
			popupSplash.dismiss();
		if(popupAbout != null && popupAbout.isShowing())
			((Button)popupAbout.getContentView().findViewById(R.id.btnDismissPopup)).performClick();
			//popupAbout.dismiss();
		
		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getWindowToken(), 0);
		
		super.onPause();
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
//		Log.d("MXUIMyMusicScreen", "row selected "+ getParent().toString());
		if(getListAdapter() instanceof MXUIUtilityMyMusicAdapter){ 
			switch (position) {
			case MYMUSICALLSONGSSCREEN://All Music
				allAdapters.getSongAdapter().setDefaultSortOrder(MXUIUtilityMyMusicSongAdapter.TIMESTAMPSORTORDER);
				switchToSongs();
				screens.push(new ScreenInfo(getResources().getString(R.string.mymusic_title_allmusic), MYMUSICALLSONGSSCREEN, ((ScreenInfo)screens.peek()).getNumber()));
				break;
				
			case MYMUSICPLAYLISTSCREEN://Playlist
				switchToPlaylist();
				screens.push(new ScreenInfo(getResources().getString(R.string.mymusic_title_playlists), MYMUSICPLAYLISTSCREEN, ((ScreenInfo)screens.peek()).getNumber()));
				break;
			case MYMUSICGENRESCREEN://Genres
				switchToGenre();
				screens.push(new ScreenInfo(getResources().getString(R.string.mymusic_title_genres), MYMUSICGENRESCREEN, ((ScreenInfo)screens.peek()).getNumber()));
				break;
			case MYMUSICALBUMSCREEN://Albums
				switchToAlbum();
				screens.push(new ScreenInfo(getResources().getString(R.string.mymusic_title_albums), MYMUSICALBUMSCREEN, ((ScreenInfo)screens.peek()).getNumber()));
				break;
			case MYMUSICARTISTSCREEN:
				switchToArtist();
				screens.push(new ScreenInfo(getResources().getString(R.string.mymusic_title_artists), MYMUSICARTISTSCREEN, ((ScreenInfo)screens.peek()).getNumber()));
				break;
			case MYMUSICSONGLISTSCREEN:
				allAdapters.getSongAdapter().setDefaultSortOrder(MXUIUtilityMyMusicSongAdapter.DEFAULTSORTORDER);
				switchToSongs();
				screens.push(new ScreenInfo(getResources().getString(R.string.mymusic_title_songs), MYMUSICSONGLISTSCREEN, ((ScreenInfo)screens.peek()).getNumber()));
				break;
			default:
				break;
			}
		}
		else if (getListAdapter() instanceof MXUIUtilityMyMusicGenresAdapter){
			int genreID = ((Genre)((MXUIUtilityMyMusicGenresAdapter)getListAdapter()).getItem(position)).ID;
			switchToArtist( genreID);
			screens.push(new ScreenInfo(getResources().getString(R.string.mymusic_title_artists), MYMUSICARTISTSCREEN, ((ScreenInfo)screens.peek()).getNumber(), genreID));
		}
		else if (getListAdapter() instanceof MXUIUtilityMyMusicPlaylistAdapter){
			switchToSongs(-1, ((Playlist)((MXUIUtilityMyMusicPlaylistAdapter)getListAdapter()).getItem(position)).ID);
			screens.push(new ScreenInfo(getResources().getString(R.string.mymusic_title_songs), MYMUSICSONGLISTSCREEN, ((ScreenInfo)screens.peek()).getNumber()));
		}
		else if (getListAdapter() instanceof MXUIUtilityMyMusicAlbumAdapter){
			switchToSongs(((Album)((MXUIUtilityMyMusicAlbumAdapter)getListAdapter()).getItem(position)).ID, -1);
			screens.push(new ScreenInfo(getResources().getString(R.string.mymusic_title_songs), MYMUSICSONGLISTSCREEN, ((ScreenInfo)screens.peek()).getNumber()));
		}
		else if (getListAdapter() instanceof MXUIUtilityMyMusicArtistAdapter){
			int artistID = ((Artist)((MXUIUtilityMyMusicArtistAdapter)getListAdapter()).getItem(position)).ID;
			int genreID = ((Artist)((MXUIUtilityMyMusicArtistAdapter)getListAdapter()).getItem(position)).GenreID;
			switchToAlbum(artistID);
//			Log.d("MXUIMyMusicScreen", "artisdID: "+artistID);
			screens.push(new ScreenInfo(getResources().getString(R.string.mymusic_title_albums), MYMUSICALBUMSCREEN, ((ScreenInfo)screens.peek()).getNumber(), genreID));
		}
		else if (getListAdapter() instanceof MXUIUtilityMyMusicSongAdapter){
			Intent playlistIntent = new Intent(Constants.Strings.INTENT_PLAYLIST_SONGS);
			playlistIntent.putParcelableArrayListExtra(Constants.Strings.PLAYLIST_SONGS, ((MXUIUtilityMyMusicSongAdapter)getListAdapter()).getCurrentSongsList());
			Song currentSong = (Song)((MXUIUtilityMyMusicSongAdapter)getListAdapter()).getCurrentSongsList().get(position);
			playlistIntent.putExtra(Constants.Strings.SELECTED_SONG, currentSong);
//			Log.d(TAG, "song parent: "+getParent().toString());
			((MXUIMusixMainScreen)getParent()).setCurrentTab(Tabs.NowPlaying);
			sendBroadcast(playlistIntent);
//			Log.d(TAG, "current song: "+currentSong.name);
		}
		
//		Log.d("MXUIMyMusicScreen", "unknown adapter");
//		stopSplash();
	}
	
	private void switchToGenre(){
		findViewById(R.id.panelMyMusicMain).setVisibility(View.GONE);
		findViewById(R.id.panelMyMusicAdditional).setVisibility(View.VISIBLE);
		findViewById(R.id.panelMyMusicSubPanel).setVisibility(View.GONE);
		findViewById(R.id.panelMyMusicEditSubPanel).setVisibility(View.GONE);
		((TextView)findViewById(R.id.txtMyMusicAdditionalCaption)).setText(getResources().getString(R.string.mymusic_title_genres));
		setListAdapter(allAdapters.getGenresAdapter());
		currentScreen = MYMUSICGENRESCREEN;
//		Log.d("MXUIMyMusicScreen", "switched to genre");
	}
	private void switchToPlaylist(){
		findViewById(R.id.panelMyMusicMain).setVisibility(View.GONE);
		findViewById(R.id.panelMyMusicAdditional).setVisibility(View.VISIBLE);
		findViewById(R.id.panelMyMusicSubPanel).setVisibility(View.VISIBLE);
		findViewById(R.id.panelMyMusicEditSubPanel).setVisibility(View.GONE);
		((TextView)findViewById(R.id.txtMyMusicAdditionalCaption)).setText(getResources().getString(R.string.mymusic_title_playlists));
		setListAdapter(allAdapters.getPlaylistAdapter(this));
		currentScreen = MYMUSICPLAYLISTSCREEN;
//		Log.d("MXUIMyMusicScreen", "switched to playlist");
	}
	
	private void switchToAlbum(){
		switchToAlbum(-1);
	}
	
	private void switchToAlbum(int artistID){
		findViewById(R.id.panelMyMusicMain).setVisibility(View.GONE);
		findViewById(R.id.panelMyMusicAdditional).setVisibility(View.VISIBLE);
		findViewById(R.id.panelMyMusicSubPanel).setVisibility(View.VISIBLE);
		findViewById(R.id.panelMyMusicEditSubPanel).setVisibility(View.GONE);
		((TextView)findViewById(R.id.txtMyMusicAdditionalCaption)).setText(getResources().getString(R.string.mymusic_title_albums));
		setListAdapter(allAdapters.getAlbumAdapter(this).setCurrentArtist(artistID));
		currentScreen = MYMUSICALBUMSCREEN;
//		Log.d("MXUIMyMusicScreen", "switched to album");
	}
	
	private void switchToAlbumStale(){
		findViewById(R.id.panelMyMusicMain).setVisibility(View.GONE);
		findViewById(R.id.panelMyMusicAdditional).setVisibility(View.VISIBLE);
		findViewById(R.id.panelMyMusicSubPanel).setVisibility(View.VISIBLE);
		findViewById(R.id.panelMyMusicEditSubPanel).setVisibility(View.GONE);
		((TextView)findViewById(R.id.txtMyMusicAdditionalCaption)).setText(getResources().getString(R.string.mymusic_title_albums));
		setListAdapter(allAdapters.getAlbumAdapter(this));
		currentScreen = MYMUSICALBUMSCREEN;
//		Log.d("MXUIMyMusicScreen", "switched to album");
	}
	
	private void switchToArtist(){
		switchToArtist(-1);
	}
	private void switchToArtist(int genreID){
		findViewById(R.id.panelMyMusicMain).setVisibility(View.GONE);
		findViewById(R.id.panelMyMusicAdditional).setVisibility(View.VISIBLE);
		findViewById(R.id.panelMyMusicSubPanel).setVisibility(View.GONE);
		findViewById(R.id.panelMyMusicEditSubPanel).setVisibility(View.GONE);
		((TextView)findViewById(R.id.txtMyMusicAdditionalCaption)).setText(getResources().getString(R.string.mymusic_title_artists));
		setListAdapter(allAdapters.getArtistAdapter().setCurrentGenre(genreID));
		currentScreen = MYMUSICARTISTSCREEN;
//		Log.d("MXUIMyMusicScreen", "switched to artist");
	}
	
	private void switchToArtistStale(){
		findViewById(R.id.panelMyMusicMain).setVisibility(View.GONE);
		findViewById(R.id.panelMyMusicAdditional).setVisibility(View.VISIBLE);
		findViewById(R.id.panelMyMusicSubPanel).setVisibility(View.GONE);
		findViewById(R.id.panelMyMusicEditSubPanel).setVisibility(View.GONE);
		((TextView)findViewById(R.id.txtMyMusicAdditionalCaption)).setText(getResources().getString(R.string.mymusic_title_artists));
		setListAdapter(allAdapters.getArtistAdapter());
		currentScreen = MYMUSICARTISTSCREEN;
//		Log.d("MXUIMyMusicScreen", "switched to artist");
	}
	
	private void switchToSongs(){
		switchToSongs(-1, -1);
	}
	private void switchToSongs(int albumID, int playlistID){
		findViewById(R.id.panelMyMusicMain).setVisibility(View.GONE);
		findViewById(R.id.panelMyMusicAdditional).setVisibility(View.VISIBLE);
		findViewById(R.id.panelMyMusicSubPanel).setVisibility(View.GONE);
		findViewById(R.id.panelMyMusicEditSubPanel).setVisibility(View.VISIBLE);
		((TextView)findViewById(R.id.txtMyMusicAdditionalCaption)).setText(getResources().getString(R.string.mymusic_title_songs));
		if(albumID >=0){
			setListAdapter(allAdapters.getSongAdapter().setCurrentAlbum(albumID));
			Log.i("MXUIMyMusicScreen", "albumID: "+albumID);
		}
		else if (playlistID>=0){
			setListAdapter(allAdapters.getSongAdapter().setCurrentPlaylist(playlistID));
			Log.i("MXUIMyMusicScreen", "playlistID: "+playlistID);
		}
		else{
			allAdapters.getSongAdapter().initialize(allAdapters.getSongAdapter().getDefaultSortOrder());////check for true later
			setListAdapter(allAdapters.getSongAdapter());
			Log.i("MXUIMyMusicScreen", "NO id: "+albumID+" "+playlistID);
		}
		currentScreen = MYMUSICSONGLISTSCREEN;
//		Log.d("MXUIMyMusicScreen", "switched to song");
	}
	
	private void switchToMyMusicMain(){
		findViewById(R.id.panelMyMusicMain).setVisibility(View.VISIBLE);
		findViewById(R.id.panelMyMusicAdditional).setVisibility(View.GONE);
		findViewById(R.id.panelMyMusicSubPanel).setVisibility(View.GONE);
		findViewById(R.id.grid).setVisibility(View.GONE);
		getListView().setVisibility(View.VISIBLE);
		setListAdapter(allAdapters.getGeneralMyMusicAdapter());
		currentScreen = MYMUSICMAINSCREEN;
//		Log.d("MXUIMyMusicScreen", "switched to mymusic main");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//need to return to the previous screen
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(currentScreen == MYMUSICMAINSCREEN){ 
				return false;
			}
			
			goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void goBack(){
		////exit edit mode
		exitSongEditMode();
		findViewById(R.id.panelMyMusicEditSubPanel).setVisibility(View.GONE);
		exitSearch();
		///end of exit edit mode
		
		//// if popup displaying - stop it
		if(popupSplash != null && popupSplash.isShowing())
			popupSplash.dismiss();
		
		ScreenInfo prevScreen = screens.pop();
		if(prevScreen.getPreviousScreenNumber() == MYMUSICALBUMSCREEN){
			// Fixed By NGSoft
//			switchToAlbumStale();
			switchToAlbum();
			// Fixed By NGSoft
			if(btnSwitchToGrid.isClickable()){
				grid.setVisibility(View.GONE);
				MXUIMyMusicScreen.this.getListView().setVisibility(View.VISIBLE);
			}
			else if(btnSwitchToList.isClickable()){
				grid.setVisibility(View.VISIBLE);
				MXUIMyMusicScreen.this.getListView().setVisibility(View.GONE);
			}
		}
		else if(prevScreen.getPreviousScreenNumber() == MYMUSICARTISTSCREEN){
			switchToArtistStale();////need to think about number
		}
		else if(prevScreen.getPreviousScreenNumber() == MYMUSICPLAYLISTSCREEN){
			switchToPlaylist();
			if(btnSwitchToGrid.isClickable()){
				grid.setVisibility(View.GONE);
				MXUIMyMusicScreen.this.getListView().setVisibility(View.VISIBLE);
			}
			else if(btnSwitchToList.isClickable()){
				grid.setVisibility(View.VISIBLE);
				MXUIMyMusicScreen.this.getListView().setVisibility(View.GONE);
			}
		}
		else if(prevScreen.getPreviousScreenNumber() == MYMUSICGENRESCREEN){
			switchToGenre();
		}
		else
			switchToMyMusicMain();
	}
	
	private void initializeAllButtons() {
		
		////TermsOfService button
		final Button btnAbout = (Button)findViewById(R.id.btnAbout);
		btnAbout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
//				Log.i("MXUIMyMusicScreen","About click "+v.toString()+" "+((Button)v).getText());
				
				if(((Button)v).getText().equals(MXUIMyMusicScreen.this.getResources().getString(R.string.mymusicabout))){
					LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
					View viewToShow = inflater.inflate(R.layout.tos, null);
					
					// Fixed By NGSoft
					TextView textView = (TextView) viewToShow.findViewById(R.id.txtTermsOfService);
					textView.setText(mSettings.getTOS());
					// Fixed By NGSoft
					
					////closing popup with PopupClose button
					((Button)viewToShow.findViewById(R.id.btnDismissPopup)).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View innerV) {
							if(popupAbout != null && popupAbout.isShowing()){
								popupAbout.dismiss();
								popupAbout = null;
								(btnAbout).setText(R.string.mymusicabout);
							}
						}
					});
					
					popupAbout = new PopupWindow(viewToShow, MXUIMyMusicScreen.this.findViewById(R.id.panelMyMusicMain).getWidth(), 250);
//					Log.i("MXUIMyMusicScreen","About click "+v.toString()+" "+MXUIMyMusicScreen.this.findViewById(R.id.panelMyMusicMain).getWidth()+viewToShow.getHeight());
					popupAbout.showAsDropDown(MXUIMyMusicScreen.this.findViewById(R.id.panelMyMusicMain));
					((Button)v).setText(R.string.mymusicaboutclose);
				}
				else{
					popupAbout.dismiss();
					popupAbout = null;
					((Button)v).setText(R.string.mymusicabout);
				}
			}
		});
		
		////edit mode for songs (starting)
		((Button)findViewById(R.id.btnMyMusicSubBar)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getListAdapter() instanceof MXUIUtilityMyMusicSongAdapter){
					enterSongEditMode();
				}				
			}			
		});
		
		////edit mode for songs (finishing)
		((Button)findViewById(R.id.btnMyMusicDone)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exitSongEditMode();
			}
		});
		
		
		//// buttons to switch between list and grid (table) mode
		btnSwitchToGrid = (ImageButton)findViewById(R.id.btnMyMusicShowAsGrid);
		btnSwitchToList = (ImageButton)findViewById(R.id.btnMyMusicShowAsList);
		
		btnSwitchToGrid.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				Log.i("MXUIMyMusicScreen","clicked to switch to grid"+v.toString());
				if(grid.getVisibility()==View.GONE){
					v.setBackgroundResource(R.drawable.my_music_grid_icon_selected);
					
					if(getListAdapter() instanceof MXUIUtilityMyMusicAlbumAdapter){
						MXUIUtilityMyMusicAlbumAdapterGrid adapter = new MXUIUtilityMyMusicAlbumAdapterGrid(MXUIMyMusicScreen.this);
						adapter.initialize(((MXUIUtilityMyMusicAlbumAdapter)getListAdapter()).getCurrentAlbums());
						grid.setAdapter(adapter);
						grid.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
								grid.setVisibility(View.GONE);
								MXUIMyMusicScreen.this.getListView().setVisibility(View.VISIBLE);
								switchToSongs(((Album)(parent.getItemAtPosition(position))).ID, -1);
								screens.push(new ScreenInfo(getResources().getString(R.string.mymusic_title_songs), MYMUSICSONGLISTSCREEN, ((ScreenInfo)screens.peek()).getNumber()));
							}
						});
					}
					else if(getListAdapter() instanceof MXUIUtilityMyMusicPlaylistAdapter){
						MXUIUtilityMyMusicPlaylistAdapterGrid adapter = new MXUIUtilityMyMusicPlaylistAdapterGrid(MXUIMyMusicScreen.this);
						adapter.initialize(((MXUIUtilityMyMusicPlaylistAdapter)getListAdapter()).getCurrentPlaylists());
						grid.setAdapter(adapter);
						grid.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
								switchToSongs(-1, ((Playlist)(parent.getItemAtPosition(position))).ID);
								screens.push(new ScreenInfo(getResources().getString(R.string.mymusic_title_songs), MYMUSICSONGLISTSCREEN, ((ScreenInfo)screens.peek()).getNumber()));
							}
						});
					}
					
					grid.setVisibility(View.VISIBLE);
					MXUIMyMusicScreen.this.getListView().setVisibility(View.GONE);
					v.setClickable(false);
					btnSwitchToList.setClickable(true);
					btnSwitchToList.setBackgroundResource(R.drawable.my_music_list_icon_up);
//					Log.i("MXUIMyMusicScreen","clicked - now grid");
				}
//				else
//					Log.i("MXUIMyMusicScreen","clicked - no comparison");
			}
		});
		
		btnSwitchToList.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i("MXUIMyMusicScreen","clicked to switch to list"+v.toString());
				if(grid.getVisibility()==View.VISIBLE){
					v.setBackgroundResource(R.drawable.my_music_list_icon_selected);
					grid.setVisibility(View.GONE);
					MXUIMyMusicScreen.this.getListView().setVisibility(View.VISIBLE);
					v.setClickable(false);
					btnSwitchToGrid.setClickable(true);
					btnSwitchToGrid.setBackgroundResource(R.drawable.my_music_grid_icon_up);
//					Log.i("MXUIMyMusicScreen","clicked - now list");
				}
//				else
//					Log.i("MXUIMyMusicScreen","clicked - no comparison");
			}
		});
		
		////this is iphone style button "BACK" on the screen
		btnGoBack = (Button)findViewById(R.id.btnMyMusicGenresBack);
		btnGoBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				goBack();
			}
		});
		
		//// addAllToPlaylist  button
		((Button)findViewById(R.id.btnMyMusicAddToPlaylist2goAll)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MXUIUtilityMyMusicSongAdapter adapter=allAdapters.getSongAdapter();
				ArrayList<Song> songs = adapter.getCurrentSongsList();
				int count = 0;
				for (Song song : songs) {
					if(!adapter.isInUserPlaylist(song.ID)){
						adapter.addSongToUserPlaylist(song);
						count++;
					}
				}
				
				new AlertDialog.Builder(MXUIMyMusicScreen.this.getParent()) 
			    .setTitle(MXUIMyMusicScreen.this.getResources().getString(R.string.mymusic_alert_title_songsadded)) 
			    .setMessage(MXUIMyMusicScreen.this.getResources().getString(R.string.mymusic_alert_message_songsadded)+count) 
			    .setPositiveButton(MXUIMyMusicScreen.this.getResources().getString(R.string.mymusic_ok), new DialogInterface.OnClickListener() { 

					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					} 
			     })
			     .show();
				setListAdapter(adapter);
				
			}
		});
		
		////delete all from playlist
		((Button)findViewById(R.id.btnMyMusicDeleteAll)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(MXUIMyMusicScreen.this.getParent()) 
			    .setTitle(MXUIMyMusicScreen.this.getResources().getString(R.string.mymusic_alert_title_deletesong)) 
			    .setMessage(MXUIMyMusicScreen.this.getResources().getString(R.string.mymusic_alert_message_deletesong)) 
			    .setPositiveButton(MXUIMyMusicScreen.this.getResources().getString(R.string.mymusic_yes), new DialogInterface.OnClickListener() { 
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						MXSyncRequest mx_request = new MXSyncRequest(DFLT_VERSION, isRegisterAccepted);
						MXSettings _settings = new MXSettings(MXUIMyMusicScreen.this);
						_settings.Load();
						
						MXUIUtilityMyMusicSongAdapter adapter=allAdapters.getSongAdapter();
						ArrayList<Song> songs = adapter.getCurrentSongsList();
						int count = 0;
						for (Song song : songs) {
							mx_request.addDeletedMedia(song.catalogMediaID);
							//adapter.deleteSong(song.ID);
							count++; //// not showing it right now but maybe in the future
						}
						mx_request.setTimestamp(_settings.getLastResponseTimestamp());
						MXSync sync = new MXSync(MXUIMyMusicScreen.this, getContentResolver(), mSettings);
						sync.sync(mx_request);
						
					}
			    })
			    .setNegativeButton(MXUIMyMusicScreen.this.getResources().getString(R.string.mymusic_no), new DialogInterface.OnClickListener() { 
				      
			    	@Override
					public void onClick(DialogInterface dialog, int which) {
						
					} 
				}) 
				.show();
			}
		});
		
		////hide/show search panel
		((ImageButton)findViewById(R.id.btnSearchAdditional)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Log.d("MXUIMyMusicScreen","clicked search");
				if(!(getListAdapter() instanceof MXUIUtilityMyMusicAdapter)){
//					Log.d("MXUIMyMusicScreen","clicked search - not main adapter");
					if(findViewById(R.id.panelMyMusicSearch).getVisibility()==View.VISIBLE)
						findViewById(R.id.panelMyMusicSearch).setVisibility(View.GONE);
					else
						findViewById(R.id.panelMyMusicSearch).setVisibility(View.VISIBLE);
				}
			}
		});
		
		
		////search text box
		((EditText)findViewById(R.id.txtMyMusicSearch)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Log.i("MXUIMyMusicScreen","search");
//				if(count > 2){
//					if((getListAdapter() instanceof MXUIUtilityMyMusicSongAdapter)){
//						allAdapters.getSongAdapter().setFilter(s.toString());
//					}
//					setListAdapter(allAdapters.getSongAdapter());
//				}
				
				((MXUIUtilityMyMusicBaseAdapter)getListAdapter()).setFilter(s.toString());
				setListAdapter(getListAdapter());
			}
			////not needed
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				
			}
			////not needed
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
	}

	private void exitSongEditMode() {
		if (getListAdapter() instanceof MXUIUtilityMyMusicSongAdapter){
			allAdapters.getSongAdapter().isInEditMode = false;
//			findViewById(R.id.panelMyMusicSubPanel).setVisibility(View.VISIBLE);
			findViewById(R.id.panelEditButton).setVisibility(View.VISIBLE);
			findViewById(R.id.btnSearchAdditional).setVisibility(View.VISIBLE);
			findViewById(R.id.btnMyMusicDone).setVisibility(View.GONE);
			setListAdapter(allAdapters.getSongAdapter());
		}
	}
	
	private void enterSongEditMode() {
		allAdapters.getSongAdapter().isInEditMode = true;
		//findViewById(R.id.panelMyMusicSubPanel).setVisibility(View.GONE);
		findViewById(R.id.panelEditButton).setVisibility(View.GONE);
		findViewById(R.id.btnSearchAdditional).setVisibility(View.GONE);
		findViewById(R.id.btnMyMusicDone).setVisibility(View.VISIBLE);
		setListAdapter(allAdapters.getSongAdapter());
		if(allAdapters.getSongAdapter().getCount()>0){
			findViewById(R.id.btnMyMusicAddToPlaylist2goAll).setVisibility(View.VISIBLE);
			findViewById(R.id.btnMyMusicDeleteAll).setVisibility(View.VISIBLE);
		}
		else{
			findViewById(R.id.btnMyMusicAddToPlaylist2goAll).setVisibility(View.GONE);
			findViewById(R.id.btnMyMusicDeleteAll).setVisibility(View.GONE);
		}
	}
	
	private void exitSearch(){
		findViewById(R.id.panelMyMusicSearch).setVisibility(View.GONE);
		((EditText)findViewById(R.id.txtMyMusicSearch)).setText(null);
		allAdapters.getSongAdapter().setFilter(null);
	}
	
	/*private void startSplash(){
		LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		View viewToShow = inflater.inflate(R.layout.splashscreen, null);
		WindowManager mWinMgr = (WindowManager)getSystemService(WINDOW_SERVICE); 
		int displayWidth = mWinMgr.getDefaultDisplay().getWidth();
		int displayHeight = mWinMgr.getDefaultDisplay().getHeight();
		Log.i("MXUIMyMusicScreen", "width: "+displayWidth+" height: "+displayHeight);
		ImageView splash =(ImageView)viewToShow.findViewById(R.id.imgSplash);
		
		popupSplash = new PopupWindow(viewToShow, displayWidth, displayHeight);
		
	    splash.setBackgroundResource(R.drawable.splash_animation);
		animSplash = (AnimationDrawable)splash.getBackground();
		popupSplash.showAtLocation(getParent().getWindow().getDecorView(), Gravity.NO_GRAVITY, 0, 0);
		
		Message msg = new Message();
	    msg.what = SHOWSPLASH;
	    handler.sendMessageDelayed(msg, 100);
	     
	}
	
	private void stopSplash(){

		Message msgStopAnimation = new Message();
        msgStopAnimation.what = STOPSPLASH;
        handler.sendMessageDelayed(msgStopAnimation, 1500);
	}
	
	private Handler handler = new Handler(new Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case SHOWSPLASH:
				animSplash.start();
				return true;
				
			case STOPSPLASH:
				animSplash.stop();
				popupSplash.dismiss();
				return true;
				
			default:
				break;
			}
			return false;
		}
		
	});*/


	@Override
	protected void onResume() {
		super.onResume();
		allAdapters.getGeneralMyMusicAdapter().initialize();
		if(getListAdapter() instanceof MXUIUtilityMyMusicAdapter){
			setListAdapter(allAdapters.getGeneralMyMusicAdapter());
			Log.d("MXUIMyMusicScreen", "onResume, list set");
//			Log.d("aaa", getParent().getClass().getName());
		}
		Log.d("MXUIMyMusicScreen", "onResume");
	}

	@Override
	protected void onStart() {
		super.onStart();
		
//		Log.d("MXUIMyMusicScreen", "onStart");
	}

	@Override
	protected void onStop() {
		super.onStop();
		//allAdapters.releaseResources();
//		Log.d("MXUIMyMusicScreen", "onStop");
	}

	@Override
	public void syncCompleted(int newSongCount) {
		MXUIUtilityMyMusicSongAdapter adapter=allAdapters.getSongAdapter();
//		ArrayList<Song> songs = adapter.getCurrentSongsList();
//		for (Song song : songs) {
//			adapter.deleteSong(song.ID);
//		}
		allAdapters.nullify();
		allAdapters = MXUIUtilityMyMusicAllAdapters.getInstance(this);
		
		if (getListAdapter() instanceof MXUIUtilityMyMusicSongAdapter){
			setListAdapter(allAdapters.getSongAdapter());
			if(findViewById(R.id.btnMyMusicDone).getVisibility()==View.VISIBLE){
				((Button)findViewById(R.id.btnMyMusicDone)).performClick();
				((Button)findViewById(R.id.btnMyMusicSubBar)).performClick();
			}
			
		}
		//btnGoBack.performClick();
		
		
	}

	@Override
	public void syncFailed(MXSyncError error) {
		MXMessages message=MXMessages.getInstance();	    
		message.setLanguage(MGLanguage.MGLanguageHebrew);				    
		message.displayMessageForCode(this , MXMessages.MSG_DELETE_SONG_FAILED, this );
	}

	@Override
	public void syncSongsCopyrightDeleted(String[] songNames) {
		
	}

	@Override
	public void buttonClicked(MGButtonCode buttonCode, String messageCode) {
		
	}
	
	@Override
	public void imagesReceived(HashMap<String, Bitmap> images, boolean isPlaylistImages) {
		Log.d(TAG, "images received, size: "+images.size());
		if(!isPlaylistImages){
			Message msgUpdateList = new Message();
	        msgUpdateList.what = UPDATELIST;
	        handler.sendMessageDelayed(msgUpdateList, 100);
		}
	}
	
	private Handler handler = new Handler(new Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			if(msg.what!=UPDATELIST){
				Log.d(TAG, "not updatelist message, return false.");
				return false;
			}
			if(getListAdapter() instanceof MXUIUtilityMyMusicAlbumAdapter){
				Log.d(TAG, "album adapter");
				setListAdapter(allAdapters.getAlbumAdapter(MXUIMyMusicScreen.this));
			}
			Log.d(TAG, "images received, not playlist");
			return true;
		}
		
	});
	
}

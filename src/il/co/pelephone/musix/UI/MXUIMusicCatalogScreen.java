package il.co.pelephone.musix.UI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import il.co.pelephone.musix.UI.utility.IMXUIDownloadEntity;
import il.co.pelephone.musix.UI.utility.MXUIMusicCatalogAllGenresAdapter;
import il.co.pelephone.musix.UI.utility.MXUIMusicCatalogFeaturedAdapter;
import il.co.pelephone.musix.UI.utility.MXUIMusicCatalogMusicEntityPromotionAdapter;
import il.co.pelephone.musix.UI.utility.MXUIMusicCatalogSongFeaturedAdapter;
import il.co.pelephone.musix.UI.utility.ScreenInfo;
import il.co.pelephone.musix.comm.IMXImageCallback;
import il.co.pelephone.musix.comm.IMXSyncCallback;
import il.co.pelephone.musix.comm.MXCoverImagesManager;
import il.co.pelephone.musix.comm.MXSettings;
import il.co.pelephone.musix.comm.MXStore;
import il.co.pelephone.musix.comm.MXStoreSearchRequest;
import il.co.pelephone.musix.comm.MXSync;
import il.co.pelephone.musix.comm.MXSyncRequest;
import il.co.pelephone.musix.comm.MXCoverImagesManager.ImageSize;
import il.co.pelephone.musix.data.MusicEntity;
import il.co.pelephone.musix.utility.MXMessages;
import il.co.pelephone.musix.utility.MXMessagesCallback;
import il.co.pelephone.musix.utility.MXMessages.MGButtonCode;
import il.co.pelephone.musix.utility.MXMessages.MGLanguage;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MXUIMusicCatalogScreen extends Activity implements IMXUIMusicCatalogCallback, MXMessagesCallback, IMXSyncCallback, IMXImageCallback, IMXUIDownloadEntity{

	public static final int STOPSPLASH = 1;
	public static final int SHOWSPLASH = 2;
	
	public static final int MUSIC_FEATURED_SCREEN = 0;
	public static final int MUSIC_GENRES_SCREEN = 1;
	public static final int MUSIC_SEARCH_SCREEN = 2;
	public static final int MUSIC_GENRES_FEATUED_SCREEN = 3;
	public static final int MUSIC_GENRES_PLAYLIST_SCREEN = 4;
	public static final int MUSIC_GENRES_ALBUM_SCREEN = 5;
	public static final int MUSIC_GENRES_SONG_SCREEN = 6;
	public static final int MUSIC_SEARCH_SCREEN_MEDIAS = 7;
	public static final int MUSIC_SEARCH_SCREEN_ALBUMS = 8;
	public static final int MUSIC_SEARCH_SCREEN_ARTISTS = 9;
	
	private static final  String DFLT_VERSION="1.1";
	private boolean isRegisterAccepted=false;
	
	private Stack<ScreenInfo> screens;
	
	private PopupWindow popupSplash;
	private AnimationDrawable animSplash;
	
	private Gallery playlistsGallery;
	private Gallery albumsGallery;
	private Gallery songsGallery;
	
	private ListView genresList;
	
	private Button btnFeatured;
	private Button btnGenres;
	private Button btnSearch;
	
	private Button btnDownloadFeaturedAll;
	
	private MXStore store;
	
	private String TAG="MXUIMusicCatalogScreen";
	
	private ListView listFeatured;
	private ListView listSearched;
	private MXUIMusicCatalogFeaturedAdapter featuredAdapter;
	private MXUIMusicCatalogFeaturedAdapter featuredPlaylistAdapter;
	private MXUIMusicCatalogFeaturedAdapter featuredAlbumAdapter;
	private MXUIMusicCatalogFeaturedAdapter featuredSongAdapter;
	private MXUIMusicCatalogFeaturedAdapter searchedArtistAdapter;
	
	private MXSettings mSettings;
	private MXSync sync;
	
	private int currentScreen;
	private int numberOfDownloadableSongs;
	
	private SearchCriteria currentSearchCriteria;
	
	private MXStoreSearchRequest mx_SearchRequest;
	
	private ArrayList<String> downloadedSongsId;
	
	private View listFooterView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.musiccatalog);
		Log.i("MXUIMusicCatalogScreen", "onCreate");
		screens = new Stack<ScreenInfo>();
		currentScreen = MUSIC_FEATURED_SCREEN;
		startSplash();
		//stopSplash();
		
		mSettings = new MXSettings(this);
		mSettings.Load();
		
		downloadedSongsId = new ArrayList<String>();
		
		initializeDownloadButtons();
		
		listFeatured = (ListView)findViewById(R.id.listCatalogFeatured);
		listSearched = (ListView)findViewById(R.id.listCatalogSearchResults);
		
		btnFeatured = (Button)findViewById(R.id.btnCatalogMainFeatured);
		btnFeatured.setSelected(true);
		btnGenres = (Button)findViewById(R.id.btnCatalogMainGenres);
		btnSearch = (Button)findViewById(R.id.btnCatalogMainSearch);
		
		btnFeatured.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(btnFeatured.isSelected() && findViewById(R.id.panelCombinedFeaturedGenres).getVisibility()==View.VISIBLE)
					//// visibility in IF is needed for making sure the button is clicked by user, not by program
					return; //do nothing - you are already selected
				else{
					btnGenres.setSelected(false);
					btnSearch.setSelected(false);
					btnFeatured.setSelected(true);
					
					showFeaturedPanel();
				}
			}
		});
		
		btnGenres.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(btnGenres.isSelected() && findViewById(R.id.panelCombinedFeaturedGenres).getVisibility()==View.VISIBLE)
					return; //do nothing - you are already selected
				else{
					btnFeatured.setSelected(false);
					btnSearch.setSelected(false);
					btnGenres.setSelected(true);
					
					showGenresPanel();
					
					((Button)findViewById(R.id.btnCatalogBack)).setVisibility(View.INVISIBLE);
				}
			}
		});

		btnSearch.setOnClickListener(new OnClickListener() {
	
			@Override
			public void onClick(View v) {
				
				showSearchPanel();
				
			}
		});
		
		initializeSearchCriteriaButtons();
		initializeSearchButton();
		initializeBackButton();
		
		store = MXStore.getInstance(mSettings);
		store.setDelegate(this);
		
		store.getMainPromotions();
		
		sync = new MXSync(this, getContentResolver(), mSettings);
		
		screens.push(new ScreenInfo(getResources().getString(R.string.catalog_title_downloadstring), MUSIC_FEATURED_SCREEN, -1));
		if(screens.size()>1)
			((Button)findViewById(R.id.btnCatalogBack)).setVisibility(View.VISIBLE);
		Log.d(TAG, "current stack size: "+screens.size());
		
//		LayoutInflater  inflater = getLayoutInflater();
//		listFooterView = inflater.inflate(R.layout.musiccatalogsearchbutton, null);
//		listFeatured.addFooterView(listFooterView);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i("MXUIMusicCatalogScreen", "on Resume");
		
		
	}

	@Override
	protected void onPause() {
		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getWindowToken(), 0);
		
		super.onPause();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.i("MXUIMusicCatalogScreen", "on Start");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.i("MXUIMusicCatalogScreen", "on Stop");
	}
	
	private void startSplash(){
		LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		View viewToShow = inflater.inflate(R.layout.splashscreen, null);
//		viewToShow.setBackgroundResource(R.drawable.splash_background);
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
        handler.sendMessageDelayed(msgStopAnimation, 100);
	}
	
	private Handler handler = new Handler(new Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case SHOWSPLASH:
				animSplash.start();
				return true;
				
			case STOPSPLASH:
				if(popupSplash.isShowing()){
					animSplash.stop();
					popupSplash.dismiss();
				}
				return true;
				
			default:
				break;
			}
			return false;
		}
		
	});

	@Override
	public void requestFailedInternalError() {		
		stopSplash();
		
		MXMessages message=MXMessages.getInstance();	    
		message.setLanguage(MGLanguage.MGLanguageHebrew);				    
		message.displayMessageForCode(this , MXMessages.MSG_DEFAULT, this );
	}

	@Override
	public void requestFailedNetworkError() {
		stopSplash();
		
		MXMessages message=MXMessages.getInstance();	    
		message.setLanguage(MGLanguage.MGLanguageHebrew);				    
		message.displayMessageForCode(this , MXMessages.MSG_ERROR_DOWNLOADING , this );
	}

	@Override
	public void responseReceived(MusicCatalogResponseType type) {
		// TODO remove animation, refresh the screen
		switch (type) {
		case ResponseTypeGeneralPromotion:
			playlistsGallery = (Gallery)findViewById(R.id.galleryPlaylists);
			playlistsGallery.setAdapter(new MXUIMusicCatalogMusicEntityPromotionAdapter(this, store.promotedPlaylists));
			if(store.promotedPlaylists != null){
				String[] playlistIDs = new String[store.promotedPlaylists.size()];
				for(int j=0; j<store.promotedPlaylists.size(); j++){
					playlistIDs[j]=store.promotedPlaylists.get(j).getId();
				}
				MXCoverImagesManager.getInstance().getImagesForPlaylists(this, playlistIDs, ImageSize.SIZE_60);
			}
			playlistsGallery.setSpacing(5);
			playlistsGallery.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					MXUIMusicCatalogMusicEntityPromotionAdapter adapter = (MXUIMusicCatalogMusicEntityPromotionAdapter)parent.getAdapter();
					MusicEntity playlist = (MusicEntity)adapter.getItem(position);
					String playlistID = playlist.getId();
					startSplash();
					listFeatured.setAdapter(null);
					showGenresFeaturedPanel();
					Log.d(TAG, "current playlist: "+playlistID);
					((TextView)findViewById(R.id.txt_catalog_caption)).setText(playlist.getName());
					Log.d(TAG, "back to featured genres album/playlist/medias");
					store.getMediasForPlaylist(playlistID);
					btnDownloadFeaturedAll.setText(getResources().getString(R.string.catalog_btn_download_playlist));
					btnDownloadFeaturedAll.setTag(playlistID);
				}
			});
			
			albumsGallery = (Gallery)findViewById(R.id.galleryAlbums);
			albumsGallery.setAdapter(new MXUIMusicCatalogMusicEntityPromotionAdapter(this, store.promotedAlbums));
			albumsGallery.setSpacing(5);
			albumsGallery.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					MXUIMusicCatalogMusicEntityPromotionAdapter adapter = (MXUIMusicCatalogMusicEntityPromotionAdapter)parent.getAdapter();
					MusicEntity album = (MusicEntity)adapter.getItem(position);
					String albumID = album.getId();
					startSplash();
					listFeatured.setAdapter(null);
					showGenresFeaturedPanel();
					Log.d(TAG, "current album: "+albumID);
					((TextView)findViewById(R.id.txt_catalog_caption)).setText(album.getName());
					store.getMediasForAlbum(albumID);
					btnDownloadFeaturedAll.setText(getResources().getString(R.string.catalog_btn_download_album));
					btnDownloadFeaturedAll.setTag(albumID);
				}
			});

			songsGallery = (Gallery)findViewById(R.id.gallerySongs);
			songsGallery.setAdapter(new MXUIMusicCatalogMusicEntityPromotionAdapter(this, store.promotedSongs));
			if(store.promotedSongs != null){
				String[] albumIDs = new String[store.promotedSongs.size()];
				for(int j=0; j<store.promotedSongs.size(); j++){
					albumIDs[j]=store.promotedSongs.get(j).getAlbumId();
				}
				MXCoverImagesManager.getInstance().getImagesForAlbums(this, albumIDs, ImageSize.SIZE_60);
			}
			songsGallery.setSpacing(5);
			songsGallery.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					MXUIMusicCatalogMusicEntityPromotionAdapter adapter = (MXUIMusicCatalogMusicEntityPromotionAdapter)parent.getAdapter();
					MusicEntity song = (MusicEntity)adapter.getItem(position);
					String songID = song.getId();
					
					MXSettings _settings = new MXSettings(parent.getContext());
					_settings.Load();
					MXSyncRequest mx_request = new MXSyncRequest(DFLT_VERSION, isRegisterAccepted);
					mx_request.addMedia(songID);
					mx_request.setTimestamp(_settings.getLastResponseTimestamp());
					sync.sync(mx_request);
					Log.d(TAG, "the id of the media to be purchased: "+songID);
					startSplash();
				}
			});
			
			if(store.promotedAlbums != null){
				String[] albumIDs = new String[store.promotedAlbums.size()];
				for(int j=0; j<store.promotedAlbums.size(); j++){
					albumIDs[j]=store.promotedAlbums.get(j).getId();
				}
				MXCoverImagesManager.getInstance().getImagesForAlbums(this, albumIDs, ImageSize.SIZE_60);
			}
			
			break;
			
		case ResponseTypeAllGenres:
			genresList = (ListView)findViewById(R.id.listCatalogGenres);
			genresList.setAdapter(new MXUIMusicCatalogAllGenresAdapter(this, store.searchedGenres));
			genresList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					MXUIMusicCatalogAllGenresAdapter adapter = (MXUIMusicCatalogAllGenresAdapter)parent.getAdapter();
					MusicEntity genre = (MusicEntity)adapter.getItem(position);
					switchFromAllGenresToFeatured(genre.getId());
					
				}
			});
			Log.d(TAG, "genres populated");
			break;

		case ResponseTypePlaylistPromotion:
//			listFeatured.removeFooterView(listFooterView);
			
			int prevScreen = (screens.size() == 0)?-1:screens.peek().getNumber();
			screens.push(new ScreenInfo(getResources().getString(R.string.catalog_title_featured_playlist), MUSIC_GENRES_PLAYLIST_SCREEN, prevScreen));
			if(screens.size()>1)
				((Button)findViewById(R.id.btnCatalogBack)).setVisibility(View.VISIBLE);
			Log.d(TAG, "Featured playlist screen for genres: current stack size: "+screens.size());
			currentScreen = MUSIC_GENRES_PLAYLIST_SCREEN;
			
//			btnDownloadFeaturedAll.setText(R.string.catalog_btn_download_playlist);
//			findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.VISIBLE);
			featuredPlaylistAdapter = new MXUIMusicCatalogFeaturedAdapter(store.promotedPlaylists, this); 
			featuredPlaylistAdapter.setDownloadCallback(this);
			if(store.promotedPlaylists != null){
				String[] playlistIDs = new String[store.promotedPlaylists.size()];
				for(int j=0; j<store.promotedPlaylists.size(); j++){
					playlistIDs[j]=store.promotedPlaylists.get(j).getId();
				}
				MXCoverImagesManager.getInstance().getImagesForPlaylists(this, playlistIDs, ImageSize.SIZE_60);
			}
			listFeatured.setAdapter(featuredPlaylistAdapter);
			listFeatured.setOnItemClickListener(featuredPlaylistListener);
			break;
			
		case ResponseTypeAlbumPromotion:
//			listFeatured.removeFooterView(listFooterView);
			
			int prevScreen1 = (screens.size() == 0)?-1:screens.peek().getNumber();
			screens.push(new ScreenInfo(getResources().getString(R.string.catalog_title_featured_album), MUSIC_GENRES_ALBUM_SCREEN, prevScreen1));
			if(screens.size()>1)
				((Button)findViewById(R.id.btnCatalogBack)).setVisibility(View.VISIBLE);
			Log.d(TAG, "Featured playlist screen for genres: current stack size: "+screens.size());
			currentScreen = MUSIC_GENRES_ALBUM_SCREEN;
			
//			btnDownloadFeaturedAll.setText(R.string.catalog_btn_download_album);
//			findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.VISIBLE);
			featuredAlbumAdapter = new MXUIMusicCatalogFeaturedAdapter(store.promotedAlbums, this);
			featuredAlbumAdapter.setDownloadCallback(this);
			featuredAlbumAdapter.setSearchAlbum(false);
			if(store.promotedAlbums != null){
				String[] albumIDs = new String[store.promotedAlbums.size()];
				for(int j=0; j<store.promotedAlbums.size(); j++){
					albumIDs[j]=store.promotedAlbums.get(j).getId();
				}
				MXCoverImagesManager.getInstance().getImagesForAlbums(this, albumIDs, ImageSize.SIZE_60);
			}
			listFeatured.setAdapter(featuredAlbumAdapter);
			listFeatured.setOnItemClickListener(featuredAlbumListener);
			break;
			
		case ResponseTypeMediaPromotion:
//			listFeatured.removeFooterView(listFooterView);
			
			int prevScreen2 = (screens.size() == 0)?-1:screens.peek().getNumber();
			screens.push(new ScreenInfo(getResources().getString(R.string.catalog_title_featured_song), MUSIC_GENRES_SONG_SCREEN, prevScreen2));
			if(screens.size()>1)
				((Button)findViewById(R.id.btnCatalogBack)).setVisibility(View.VISIBLE);
			Log.d(TAG, "Featured playlist screen for genres: current stack size: "+screens.size());
			currentScreen = MUSIC_GENRES_SONG_SCREEN;
			
			if(btnDownloadFeaturedAll.getText().toString().equalsIgnoreCase(getResources().getString(R.string.catalog_btn_download_songs))){
				findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.GONE);
				findViewById(R.id.panelCatalogFeaturedDownloadTwoButtons).setVisibility(View.VISIBLE);
			}
			else{
				findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.VISIBLE);
				findViewById(R.id.panelCatalogFeaturedDownloadTwoButtons).setVisibility(View.GONE);
			}
			featuredSongAdapter = new MXUIMusicCatalogSongFeaturedAdapter(store.promotedSongs, this);
			((MXUIMusicCatalogSongFeaturedAdapter)featuredSongAdapter).setCheckedSongs(downloadedSongsId);
			listFeatured.setAdapter(featuredSongAdapter);
			//// temporarily
			listFeatured.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					//// select/deselect the song
					((MusicEntity)featuredSongAdapter.getItem(position)).setSelected(!((MusicEntity)featuredSongAdapter.getItem(position)).isSelected());
					listFeatured.setAdapter(featuredSongAdapter);
					if(((MXUIMusicCatalogSongFeaturedAdapter)featuredSongAdapter).hasSelectedSongs()){
						findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.GONE);
						findViewById(R.id.panelCatalogFeaturedDownloadTwoButtons).setVisibility(View.VISIBLE);
					}
					else{
						findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.VISIBLE);
						findViewById(R.id.panelCatalogFeaturedDownloadTwoButtons).setVisibility(View.GONE);
					}
					
					Log.d(TAG, "the id of the media to be selected: "+((MusicEntity)parent.getAdapter().getItem(position)).getId());
//					startSplash();
				}
			});
			break;
			
		case ResponseTypeSearchedAlbums:
			Log.d(TAG, "albums Found");
//			listFeatured.addFooterView(listFooterView);
//			listSearched.setAdapter(new MXUIMusicCatalogFeaturedAdapter(store.promotedAlbums, this));
			findViewById(R.id.panelMusicCatalogSearchResultNumber).setVisibility(View.VISIBLE);
			if(store.getTotalItemsInSearch() > 20 && store.promotedAlbums.size() == 20)
				findViewById(R.id.btnMusicCatalogGetNext20).setVisibility(View.VISIBLE);
			else
				findViewById(R.id.btnMusicCatalogGetNext20).setVisibility(View.GONE);
			
			showGenresFeaturedPanel();
			
			if(!(mx_SearchRequest != null && mx_SearchRequest.getPageNumber() > 1)){
				int prevScreen4 = (screens.size() == 0)?-1:screens.peek().getNumber();
				screens.push(new ScreenInfo(getResources().getString(R.string.catalog_title_featured_album), MUSIC_SEARCH_SCREEN_ALBUMS, prevScreen4));
				if(screens.size()>1)
					((Button)findViewById(R.id.btnCatalogBack)).setVisibility(View.VISIBLE);
				Log.d(TAG, "Searched album screen: current stack size: "+screens.size());
				currentScreen = MUSIC_SEARCH_SCREEN_ALBUMS;
			}
			
			
			((TextView)findViewById(R.id.txtMusicCatalogSearchResultsNumber)).setText(getResources().getString(R.string.catalog_found)+ " " +store.getTotalItemsInSearch()+ " " +getResources().getString(R.string.catalog_items));
			
//			btnDownloadFeaturedAll.setText(R.string.catalog_btn_download_album);
//			findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.VISIBLE);
			featuredAlbumAdapter = new MXUIMusicCatalogFeaturedAdapter(store.promotedAlbums, this);
			featuredAlbumAdapter.setDownloadCallback(this);
			featuredAlbumAdapter.setSearchAlbum(true);
			if(store.promotedAlbums != null){
				String[] albumIDs = new String[store.promotedAlbums.size()];
				for(int j=0; j<store.promotedAlbums.size(); j++){
					albumIDs[j]=store.promotedAlbums.get(j).getId();
				}
				MXCoverImagesManager.getInstance().getImagesForAlbums(this, albumIDs, ImageSize.SIZE_60);
			}
			listFeatured.setAdapter(featuredAlbumAdapter);
			listFeatured.setOnItemClickListener(featuredAlbumListener);
			if(featuredAlbumAdapter.getCount()==0){
				((Button)findViewById(R.id.btnCatalogBack)).performClick();
				MXMessages message=MXMessages.getInstance();	    
				message.setLanguage(MGLanguage.MGLanguageHebrew);				    
				message.displayMessageForCode(this , MXMessages.MSG_SEARCH_NO_ITEMS_FOUNDS, this );
			}
			break;
		case ResponseTypeSearchedArtists:
			Log.d(TAG, "aartists Found");
//			listFeatured.addFooterView(listFooterView);
//			listSearched.setAdapter(new MXUIMusicCatalogFeaturedAdapter(store.searchedArtists, this));
			findViewById(R.id.panelMusicCatalogSearchResultNumber).setVisibility(View.VISIBLE);
			if(store.getTotalItemsInSearch() > 20 && store.searchedArtists.size() == 20)
				findViewById(R.id.btnMusicCatalogGetNext20).setVisibility(View.VISIBLE);
			else
				findViewById(R.id.btnMusicCatalogGetNext20).setVisibility(View.GONE);
			
			showGenresFeaturedPanel();
			if(!(mx_SearchRequest != null && mx_SearchRequest.getPageNumber() > 1)){
				int prevScreen5 = (screens.size() == 0)?-1:screens.peek().getNumber();
				screens.push(new ScreenInfo(getResources().getString(R.string.catalog_title_featured_album), MUSIC_SEARCH_SCREEN_ARTISTS, prevScreen5));
				if(screens.size()>1)
					((Button)findViewById(R.id.btnCatalogBack)).setVisibility(View.VISIBLE);
				Log.d(TAG, "Searched artist screen: current stack size: "+screens.size());
				currentScreen = MUSIC_SEARCH_SCREEN_ARTISTS;
			}
			
			
			((TextView)findViewById(R.id.txtMusicCatalogSearchResultsNumber)).setText(getResources().getString(R.string.catalog_found)+  " " +store.getTotalItemsInSearch()+ " " +getResources().getString(R.string.catalog_items));
			
			searchedArtistAdapter = new MXUIMusicCatalogFeaturedAdapter(store.searchedArtists, this);
			searchedArtistAdapter.setDownloadCallback(this);
			
			listFeatured.setAdapter(searchedArtistAdapter);
			listFeatured.setOnItemClickListener(searchedArtistsListener);
			
			if(searchedArtistAdapter.getCount()==0){
				((Button)findViewById(R.id.btnCatalogBack)).performClick();
				MXMessages message=MXMessages.getInstance();	    
				message.setLanguage(MGLanguage.MGLanguageHebrew);				    
				message.displayMessageForCode(this , MXMessages.MSG_SEARCH_NO_ITEMS_FOUNDS, this );
			}
			
			break;
		case ResponseTypeSearchedMedias:
			Log.d(TAG, "songs Found");
			findViewById(R.id.panelMusicCatalogSearchResultNumber).setVisibility(View.VISIBLE);
			if(store.getTotalItemsInSearch() > 20 && store.promotedSongs.size() == 20)
				findViewById(R.id.btnMusicCatalogGetNext20).setVisibility(View.VISIBLE);
			else
				findViewById(R.id.btnMusicCatalogGetNext20).setVisibility(View.GONE);
			
//			listFeatured.addFooterView(listFooterView);
			
			showGenresFeaturedPanel();
			//listSearched.setAdapter(new MXUIMusicCatalogFeaturedAdapter(store.promotedSongs, this));
			if(!(mx_SearchRequest != null && mx_SearchRequest.getPageNumber() > 1)){
				int prevScreen3 = (screens.size() == 0)?-1:screens.peek().getNumber();
				screens.push(new ScreenInfo(getResources().getString(R.string.catalog_title_featured_song), MUSIC_SEARCH_SCREEN_MEDIAS, prevScreen3));
				if(screens.size()>1)
					((Button)findViewById(R.id.btnCatalogBack)).setVisibility(View.VISIBLE);
				Log.d(TAG, "Searched screen for medias: current stack size: "+screens.size());
				currentScreen = MUSIC_SEARCH_SCREEN_MEDIAS;
			}
			
			
			((TextView)findViewById(R.id.txtMusicCatalogSearchResultsNumber)).setText(getResources().getString(R.string.catalog_found)+ " " + store.getTotalItemsInSearch()+ " " +getResources().getString(R.string.catalog_items));
			
			findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.GONE);
			findViewById(R.id.panelCatalogFeaturedDownloadTwoButtons).setVisibility(View.GONE);
			
			featuredSongAdapter = new MXUIMusicCatalogSongFeaturedAdapter(store.promotedSongs, this);
			((MXUIMusicCatalogSongFeaturedAdapter)featuredSongAdapter).setCheckedSongs(downloadedSongsId);
			listFeatured.setAdapter(featuredSongAdapter);
			//// temporarily
			listFeatured.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					((MusicEntity)featuredSongAdapter.getItem(position)).setSelected(!((MusicEntity)featuredSongAdapter.getItem(position)).isSelected());
					listFeatured.setAdapter(featuredSongAdapter);
					if(((MXUIMusicCatalogSongFeaturedAdapter)featuredSongAdapter).hasSelectedSongs()){
						findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.GONE);
						findViewById(R.id.panelCatalogFeaturedDownloadTwoButtons).setVisibility(View.VISIBLE);
					}
					else{
						findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.GONE);
						findViewById(R.id.panelCatalogFeaturedDownloadTwoButtons).setVisibility(View.GONE);
					}

					Log.d(TAG, "the id of the media to be selected: "+((MusicEntity)parent.getAdapter().getItem(position)).getId());
//					startSplash();
				}
			});
			
			if(featuredSongAdapter.getCount()==0){
				((Button)findViewById(R.id.btnCatalogBack)).performClick();
				MXMessages message=MXMessages.getInstance();	    
				message.setLanguage(MGLanguage.MGLanguageHebrew);				    
				message.displayMessageForCode(this , MXMessages.MSG_SEARCH_NO_ITEMS_FOUNDS, this );
			}
			
			break;
			
		default:
			break;
		}
		stopSplash();
	}

	@Override
	public void buttonClicked(MGButtonCode buttonCode, String messageCode) {
		////do nothing here - button was clicked when message appeared
		if(featuredSongAdapter != null)
			featuredSongAdapter.notifyDataSetChanged();
	}
	
	private void switchFromAllGenresToFeatured(String genreID){
		findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.GONE);
		findViewById(R.id.panelCatalogFeaturedDownloadTwoButtons).setVisibility(View.GONE);
		
		if(featuredAdapter == null){
			ArrayList<MusicEntity> list = new ArrayList<MusicEntity>(3);
			MusicEntity entity = new MusicEntity(genreID, getResources().getString(R.string.catalog_featured_playlists));
			entity.setImageDrawable(getResources().getDrawable(R.drawable.plalist_katalog));
			list.add(entity);
			MusicEntity entity1 = new MusicEntity(genreID, getResources().getString(R.string.catalog_featured_albums));
			entity1.setImageDrawable(getResources().getDrawable(R.drawable.albums_katalog)); 
			list.add(entity1);
			MusicEntity entity2 = new MusicEntity(genreID, getResources().getString(R.string.catalog_featured_songs));
			entity2.setImageDrawable(getResources().getDrawable(R.drawable.songs_katalog));
			list.add(entity2);
			
			featuredAdapter = new MXUIMusicCatalogFeaturedAdapter(list, this);
			listFeatured.setAdapter(featuredAdapter);
			listFeatured.setOnItemClickListener(featuredListener);
		}
		
		else {////if(!(listFeatured.getAdapter() instanceof MXUIMusicCatalogFeaturedAdapter))
			MusicEntity ent = null;
			if(featuredAdapter.getItem(0) != null)
				ent = (MusicEntity)featuredAdapter.getItem(0);
			if( ent != null && !(ent.getId().equalsIgnoreCase(genreID)) ){
				ArrayList<MusicEntity> list = new ArrayList<MusicEntity>(3);
				MusicEntity entity = new MusicEntity(genreID, getResources().getString(R.string.catalog_featured_playlists));
				entity.setImageDrawable(getResources().getDrawable(R.drawable.my_music_playlist_icon));
				list.add(entity);
				MusicEntity entity1 = new MusicEntity(genreID, getResources().getString(R.string.catalog_featured_albums));
				entity1.setImageDrawable(getResources().getDrawable(R.drawable.my_music_album_icon));
				list.add(entity1);
				MusicEntity entity2 = new MusicEntity(genreID, getResources().getString(R.string.catalog_featured_songs));
				entity2.setImageDrawable(getResources().getDrawable(R.drawable.my_music_songs_icon));
				list.add(entity2);
				
				featuredAdapter = new MXUIMusicCatalogFeaturedAdapter(list, this);
			}
			listFeatured.setAdapter(featuredAdapter);
			listFeatured.setOnItemClickListener(featuredListener);
		}
		
		int prevScreen = (screens.size() == 0)?-1:screens.peek().getNumber();
		screens.push(new ScreenInfo(genreID, MUSIC_GENRES_FEATUED_SCREEN, prevScreen));
		if(screens.size()>1)
			((Button)findViewById(R.id.btnCatalogBack)).setVisibility(View.VISIBLE);
		Log.d(TAG, "Featured screen: current stack size: "+screens.size());
		currentScreen = MUSIC_GENRES_FEATUED_SCREEN;
		
		findViewById(R.id.panelCatalogFeatured).setVisibility(View.VISIBLE);
		findViewById(R.id.panelCatalogSearch).setVisibility(View.GONE);
		findViewById(R.id.panelCombinedFeaturedGenres).setVisibility(View.GONE);
		
		
	}
	
	private void switchToFeaturedPlaylists(String genreID) {
		startSplash();
		store.getPlaylistPromotionsForGenre(genreID);
		Log.d(TAG, "current genre: "+genreID);
	}
	
	private void switchToFeaturedAlbums(String genreID) {
		startSplash();
		store.getAlbumPromotionsForGenre(genreID);
		Log.d(TAG, "current genre: "+genreID);
	}
	
	private void switchToFeaturedSongs(String genreID) {
		startSplash();
		btnDownloadFeaturedAll.setText(getResources().getString(R.string.catalog_btn_download_songs));
		store.getMediaPromotionsForGenre(genreID);
		Log.d(TAG, "current genre: "+genreID);
	}
	
	private void showSearchPanel(){
		findViewById(R.id.panelCatalogSearch).setVisibility(View.VISIBLE);
		findViewById(R.id.panelCatalogFeatured).setVisibility(View.GONE);
		findViewById(R.id.panelCombinedFeaturedGenres).setVisibility(View.GONE);
		
		screens.push(new ScreenInfo(getResources().getString(R.string.catalog_title_search), MUSIC_SEARCH_SCREEN, currentScreen));
		if(screens.size()>1)
			((Button)findViewById(R.id.btnCatalogBack)).setVisibility(View.VISIBLE);
		Log.d(TAG, "current stack size: "+screens.size());
		currentScreen = MUSIC_SEARCH_SCREEN;
	}
	
	private void showGenresFeaturedPanel(){
		findViewById(R.id.panelCatalogSearch).setVisibility(View.GONE);
		findViewById(R.id.panelCatalogFeatured).setVisibility(View.VISIBLE);
		findViewById(R.id.panelCombinedFeaturedGenres).setVisibility(View.GONE);
		findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.GONE);
		findViewById(R.id.panelCatalogFeaturedDownloadTwoButtons).setVisibility(View.GONE);
	}
	
	private void showGenresPanel(){
		findViewById(R.id.panelCatalogSearch).setVisibility(View.GONE);
		findViewById(R.id.panelCatalogFeatured).setVisibility(View.GONE);
		findViewById(R.id.panelCombinedFeaturedGenres).setVisibility(View.VISIBLE);
		//findViewById(R.id.panelCatalogGenres).setVisibility(View.VISIBLE);
		findViewById(R.id.panelCatalogFeatured).setVisibility(View.GONE);
		
		((LinearLayout)findViewById(R.id.panelCategoryDownloadGalleries)).setVisibility(View.GONE);
		findViewById(R.id.panelCatalogGenres).setVisibility(View.VISIBLE);
		
		int prevScreen = (screens.size() == 0)?-1:screens.peek().getNumber();
		if(prevScreen != MUSIC_GENRES_SCREEN){
			screens.push(new ScreenInfo(getResources().getString(R.string.catalog_title_genres), MUSIC_GENRES_SCREEN, prevScreen));
			if(screens.size()>1)
				((Button)findViewById(R.id.btnCatalogBack)).setVisibility(View.VISIBLE);
		}
		Log.d(TAG, "Genres button pressed. current stack size: "+screens.size());
		currentScreen = MUSIC_GENRES_SCREEN;
		
		if(genresList == null){/////not initialized yet - no data 
			store.getGenres();
			startSplash();
		}
	}
	
	private void showFeaturedPanel(){
		findViewById(R.id.panelCatalogSearch).setVisibility(View.GONE);
		findViewById(R.id.panelCatalogFeatured).setVisibility(View.GONE);
		findViewById(R.id.panelCombinedFeaturedGenres).setVisibility(View.VISIBLE);
		findViewById(R.id.panelCatalogGenres).setVisibility(View.GONE);
		findViewById(R.id.panelCategoryDownloadGalleries).setVisibility(View.VISIBLE);
		
		screens.clear();//// when clicking featured - starts everything all over again (in screens)
		screens.push(new ScreenInfo(getResources().getString(R.string.catalog_title_downloadstring), MUSIC_FEATURED_SCREEN, -1));
		if(screens.size()>1)
			((Button)findViewById(R.id.btnCatalogBack)).setVisibility(View.VISIBLE);
		Log.d(TAG, "Featured Button pressed. current stack size: "+screens.size());
		
		currentScreen = MUSIC_FEATURED_SCREEN;
	}

	
	private void initializeSearchCriteriaButtons(){
		final Button btnArtistCriteria = (Button)findViewById(R.id.btnCatalogSearchArtists);
		final Button btnSongCriteria =(Button)findViewById(R.id.btnCatalogSearchSongs);
		final Button btnAlbumCriteria =(Button)findViewById(R.id.btnCatalogSearchAlbums);
		
		(btnArtistCriteria).setSelected(true);///// when initialized - this is selected
		currentSearchCriteria = SearchCriteria.Artist;
		
		(btnArtistCriteria).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!v.isSelected()){
					v.setSelected(!v.isSelected());
					btnAlbumCriteria.setSelected(!v.isSelected());
					btnSongCriteria.setSelected(!v.isSelected());
					if(v.isSelected()){
						currentSearchCriteria = SearchCriteria.Artist;
					}
				}
				
			}
		});
		
		
		(btnAlbumCriteria).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!v.isSelected()){
					v.setSelected(!v.isSelected());
					btnArtistCriteria.setSelected(!v.isSelected());
					btnSongCriteria.setSelected(!v.isSelected());
					if(v.isSelected()){
						currentSearchCriteria = SearchCriteria.Album;
					}
				}
			}
		});
		
		
		(btnSongCriteria).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!v.isSelected()){
					v.setSelected(!v.isSelected());
					btnArtistCriteria.setSelected(!v.isSelected());
					btnAlbumCriteria.setSelected(!v.isSelected());
					if(v.isSelected()){
						currentSearchCriteria = SearchCriteria.Song;
					}
				}
			}
		});
	}
	
	private void initializeSearchButton() {
		final EditText txtSearch = (EditText)findViewById(R.id.txtCatalogSearch);
		Button searchButton = (Button)findViewById(R.id.btnCatalogSearch);
		searchButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				String searchText = txtSearch.getText().toString();
				if(searchText == null || searchText.length()==0)
					return;////no text - no search
				switch (currentSearchCriteria) {
					case Album :
						mx_SearchRequest = store.searchAlbumByName(searchText);
						listFeatured.setAdapter(null);
						break;
					case Artist :
						mx_SearchRequest = store.searchArtistName(searchText);
						break;
					case Song :
						mx_SearchRequest = store.searchMediaByName(searchText);
						listFeatured.setAdapter(null);
						break;
					default:
						break;
				}
				Log.d(TAG, "search started. criteria: "+currentSearchCriteria+" text: "+searchText);
				startSplash();
				InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); 
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
		});
		
		((Button)findViewById(R.id.btnMusicCatalogGetNext20)).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mx_SearchRequest = store.searchAgain(mx_SearchRequest, true);
				startSplash();
			}
		});
	}
	
	private void initializeBackButton(){
		((Button)findViewById(R.id.btnCatalogBack)).setVisibility(View.INVISIBLE);
		
		((Button)findViewById(R.id.btnCatalogBack)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(screens.size()>1){
					
//					if((currentScreen==MUSIC_SEARCH_SCREEN_ALBUMS ||
//							currentScreen==MUSIC_SEARCH_SCREEN_MEDIAS ||
//							currentScreen==MUSIC_SEARCH_SCREEN_ARTISTS) 
//							&& mx_SearchRequest != null && mx_SearchRequest.getPageNumber() > 1){
//						screens.pop();
//						screens.pop();
//						mx_SearchRequest = store.searchAgain(mx_SearchRequest, false);
//						startSplash();
//						return;
//					}
					
					findViewById(R.id.panelMusicCatalogSearchResultNumber).setVisibility(View.GONE);
					
					ScreenInfo curScreen = screens.pop();
					////specifically for search
					if(curScreen.getNumber()==MUSIC_SEARCH_SCREEN_ALBUMS ||
							curScreen.getNumber()==MUSIC_SEARCH_SCREEN_MEDIAS ||
							curScreen.getNumber()==MUSIC_SEARCH_SCREEN_ARTISTS){
						findViewById(R.id.panelCatalogSearch).setVisibility(View.VISIBLE);
						findViewById(R.id.panelCatalogFeatured).setVisibility(View.GONE);
						findViewById(R.id.panelCombinedFeaturedGenres).setVisibility(View.GONE);
						Log.d(TAG, "current stack size: "+screens.size());
						currentScreen = MUSIC_SEARCH_SCREEN;
						//((TextView)findViewById(R.id.txt_catalog_caption)).setText(getResources().getString(R.string.catalog_title_search));
						return;
					}
					
					if(screens.size()<2)
						((Button)findViewById(R.id.btnCatalogBack)).setVisibility(View.INVISIBLE);
					Log.d(TAG, "current stack size main: "+screens.size());
					if(curScreen.getPreviousScreenNumber()==MUSIC_SEARCH_SCREEN){
//						showSearchPanel();
						Log.d(TAG, "featured screen showing after search");
						btnFeatured.performClick();
					}
					else if(curScreen.getPreviousScreenNumber()==MUSIC_GENRES_SCREEN){
						Log.d(TAG, "genres screen showing");
						//((TextView)findViewById(R.id.txt_catalog_caption)).setText(getResources().getString(R.string.catalog_title_genres));
						btnGenres.performClick();
					}
					else if(curScreen.getPreviousScreenNumber()==MUSIC_GENRES_FEATUED_SCREEN){
						showGenresFeaturedPanel();
						listFeatured.setAdapter(featuredAdapter);
						listFeatured.setOnItemClickListener(featuredListener);
						
						Log.d(TAG, "MUSIC_GENRES_FEATUED_SCREEN");
					}
					else if(curScreen.getPreviousScreenNumber()==MUSIC_FEATURED_SCREEN){
						Log.d(TAG, "featured screen showing");
//						showFeaturedPanel();
						((TextView)findViewById(R.id.txt_catalog_caption)).setText(getResources().getString(R.string.catalog_title_downloadstring));
						btnFeatured.performClick();
					}
					else if(curScreen.getPreviousScreenNumber()==MUSIC_GENRES_PLAYLIST_SCREEN){
						//TODO listFeatured - promotedPlaylists  - remove panels
						findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.GONE);
						findViewById(R.id.panelCatalogFeaturedDownloadTwoButtons).setVisibility(View.GONE);
						listFeatured.setAdapter(featuredPlaylistAdapter);
						listFeatured.setOnItemClickListener(featuredPlaylistListener);
						((TextView)findViewById(R.id.txt_catalog_caption)).setText(getResources().getString(R.string.catalog_title_downloadstring));
						Log.d(TAG, "back to featured genres album/playlist/medias");
					}
					else if(curScreen.getPreviousScreenNumber()==MUSIC_GENRES_ALBUM_SCREEN){
						//TODO listFeatured - promotedAlbums
						findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.GONE);
						findViewById(R.id.panelCatalogFeaturedDownloadTwoButtons).setVisibility(View.GONE);
						listFeatured.setAdapter(featuredAlbumAdapter);
						listFeatured.setOnItemClickListener(featuredAlbumListener);
						((TextView)findViewById(R.id.txt_catalog_caption)).setText(getResources().getString(R.string.catalog_title_downloadstring));
						Log.d(TAG, "back to featured genres album/playlist/medias");
					}
					else if(curScreen.getPreviousScreenNumber()==MUSIC_SEARCH_SCREEN_ALBUMS ){
						
						findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.GONE);
						findViewById(R.id.panelCatalogFeaturedDownloadTwoButtons).setVisibility(View.GONE);
						findViewById(R.id.panelMusicCatalogSearchResultNumber).setVisibility(View.VISIBLE);
						listFeatured.setAdapter(featuredAlbumAdapter);
						listFeatured.setOnItemClickListener(featuredAlbumListener);
//						Log.d(TAG, "current stack size: "+screens.size());
						((TextView)findViewById(R.id.txt_catalog_caption)).setText(getResources().getString(R.string.catalog_title_downloadstring));
						Log.d(TAG, "back to search album/playlist/medias");
						return;
					}
					
				}
			}
		});
	}

	private void initializeDownloadButtons() {
		btnDownloadFeaturedAll = (Button)findViewById(R.id.btnCatalogDownloadFeaturedAll);
		btnDownloadFeaturedAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String str = btnDownloadFeaturedAll.getText().toString();
				if(str.equalsIgnoreCase(getResources().getString(R.string.catalog_btn_download_album))){
					//// album needs to be purchased
					String albumID = btnDownloadFeaturedAll.getTag().toString();
					numberOfDownloadableSongs = 0;
					MXSettings _settings = new MXSettings(MXUIMusicCatalogScreen.this);
					_settings.Load();
					MXSyncRequest mx_request = new MXSyncRequest(DFLT_VERSION, isRegisterAccepted);
					mx_request.addAlbum(albumID);
					mx_request.setTimestamp(_settings.getLastResponseTimestamp());
					sync.sync(mx_request);
					startSplash();
					synchronized (featuredSongAdapter) {
						for(int i=0; i<featuredSongAdapter.getCount(); i++){
							MusicEntity song = (MusicEntity)featuredSongAdapter.getItem(i);
							String songID = song.getId();
							downloadedSongsId.add(songID);
						}
						((MXUIMusicCatalogSongFeaturedAdapter)featuredSongAdapter).setCheckedSongs(downloadedSongsId);
					}
				}
				else if(str.equalsIgnoreCase(getResources().getString(R.string.catalog_btn_download_playlist))){
				//// playlist needs to be purchased
					String playlistID = btnDownloadFeaturedAll.getTag().toString();
					numberOfDownloadableSongs = 0;
					MXSettings _settings = new MXSettings(MXUIMusicCatalogScreen.this);
					_settings.Load();
					MXSyncRequest mx_request = new MXSyncRequest(DFLT_VERSION, isRegisterAccepted);
					mx_request.addPlaylist(playlistID);
					mx_request.setTimestamp(_settings.getLastResponseTimestamp());
					sync.sync(mx_request);
					startSplash();
					synchronized (featuredSongAdapter) {
						for(int i=0; i<featuredSongAdapter.getCount(); i++){
							MusicEntity song = (MusicEntity)featuredSongAdapter.getItem(i);
							String songID = song.getId();
							downloadedSongsId.add(songID);
						}
						((MXUIMusicCatalogSongFeaturedAdapter)featuredSongAdapter).setCheckedSongs(downloadedSongsId);
					}
				}
			}
		});
		
		Button deselectAllButton = (Button)findViewById(R.id.btnCatalogDownloadFeaturedDeselect);
		deselectAllButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for(int i=0; i<featuredSongAdapter.getCount(); i++){
					((MusicEntity)featuredSongAdapter.getItem(i)).setSelected(false);
				}
				listFeatured.setAdapter(featuredSongAdapter);
				////display one button - or no button
				if(currentScreen == MUSIC_SEARCH_SCREEN_MEDIAS){
					findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.GONE);
					findViewById(R.id.panelCatalogFeaturedDownloadTwoButtons).setVisibility(View.GONE);
				}
				else {
					findViewById(R.id.panelCatalogFeaturedDownloadOneButton).setVisibility(View.VISIBLE);
					findViewById(R.id.panelCatalogFeaturedDownloadTwoButtons).setVisibility(View.GONE);
				}

				
			}
		});
		
		Button downloadSelectedSongsButton = (Button)findViewById(R.id.btnCatalogDownloadFeaturedSelectedSongs);
		downloadSelectedSongsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				numberOfDownloadableSongs = 0;
				for(int i=0; i<featuredSongAdapter.getCount(); i++){
					MusicEntity song = (MusicEntity)featuredSongAdapter.getItem(i);
					if(song.isSelected()){
						numberOfDownloadableSongs++;
						String songID = song.getId();
						downloadedSongsId.add(songID);
						MXSettings _settings = new MXSettings(MXUIMusicCatalogScreen.this);
						_settings.Load();
						MXSyncRequest mx_request = new MXSyncRequest(DFLT_VERSION, isRegisterAccepted);
						mx_request.addMedia(songID);
						mx_request.setTimestamp(_settings.getLastResponseTimestamp());
						sync.sync(mx_request);
						Log.d(TAG, "the id of the media to be purchased: "+songID);
					}
				}
				((MXUIMusicCatalogSongFeaturedAdapter)featuredSongAdapter).setCheckedSongs(downloadedSongsId);
				Log.d(TAG, "number of purchased songs: "+numberOfDownloadableSongs);
				if(numberOfDownloadableSongs > 0)
					startSplash();
			}
		});
	}
	
	OnItemClickListener featuredListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			MXUIMusicCatalogFeaturedAdapter adapter = (MXUIMusicCatalogFeaturedAdapter)parent.getAdapter();
			MusicEntity genre = (MusicEntity)adapter.getItem(position);
			String genreID = genre.getId();
			switch (position) {
			case 0:
				switchToFeaturedPlaylists(genreID);
				break;
			case 1:
				switchToFeaturedAlbums(genreID);
				break;
			case 2:
				switchToFeaturedSongs(genreID);
				break;

			default:
				break;
			}
		}
	};
	
	OnItemClickListener featuredAlbumListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			MXUIMusicCatalogFeaturedAdapter adapter = (MXUIMusicCatalogFeaturedAdapter)parent.getAdapter();
			MusicEntity album = (MusicEntity)adapter.getItem(position);
			String albumID = album.getId();
			startSplash();
			listFeatured.setAdapter(null);
			showGenresFeaturedPanel();
			
			findViewById(R.id.panelMusicCatalogSearchResultNumber).setVisibility(View.GONE);
			//findViewById(R.id.btnMusicCatalogGetNext20).setVisibility(View.GONE);
			
			Log.d(TAG, "current album: "+albumID);
			((TextView)findViewById(R.id.txt_catalog_caption)).setText(album.getName());////set name
			
			btnDownloadFeaturedAll.setText(getResources().getString(R.string.catalog_btn_download_album));
			btnDownloadFeaturedAll.setTag(albumID);
			
			store.getMediasForAlbum(albumID);
		}
	};
	
	OnItemClickListener featuredPlaylistListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			MXUIMusicCatalogFeaturedAdapter adapter = (MXUIMusicCatalogFeaturedAdapter)parent.getAdapter();
			MusicEntity playlist = (MusicEntity)adapter.getItem(position);
			String playlistID = playlist.getId();
			startSplash();
			listFeatured.setAdapter(null);
			showGenresFeaturedPanel();////setName
			((TextView)findViewById(R.id.txt_catalog_caption)).setText(playlist.getName());
			Log.d(TAG, "current playlist: "+playlistID);
			
			
			btnDownloadFeaturedAll.setText(getResources().getString(R.string.catalog_btn_download_playlist));
			btnDownloadFeaturedAll.setTag(playlistID);
			
			store.getMediasForPlaylist(playlistID);
		}
	};
	
	OnItemClickListener searchedArtistsListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			MXUIMusicCatalogFeaturedAdapter adapter = (MXUIMusicCatalogFeaturedAdapter)parent.getAdapter();
			MusicEntity artist = (MusicEntity)adapter.getItem(position);
			String artistID = artist.getId();
			startSplash();
			listFeatured.setAdapter(null);
			showGenresFeaturedPanel();
			Log.d(TAG, "current artist: "+artistID+", type:"+artist.getType().toString());
			
			store.getAlbumsByArtist(artistID);
		}
	};
	
	public enum SearchCriteria{
		Artist,
		Album,
		Song
	}

	@Override
	public void syncCompleted(int newSongCount) {
		if(numberOfDownloadableSongs > 0)
			numberOfDownloadableSongs--;
		Log.d(TAG, "current numberOfDownloadableSongs: "+ newSongCount);
		if(numberOfDownloadableSongs == 0){
			stopSplash();
			MXMessages message=MXMessages.getInstance();	    
			message.setLanguage(MGLanguage.MGLanguageHebrew);				    
			message.displayMessageForCode(this , MXMessages.MSG_SONGS_ADDED_TO_MYMUSIC, this );
		}
		featuredSongAdapter.notifyDataSetChanged();
//		listFeatured.setAdapter(null);
//		listFeatured.setAdapter(/*listFeatured.getAdapter());/*/featuredSongAdapter);
		Log.d(TAG, "listFeatured updated");
//		listFeatured.invalidate();
	}

	@Override
	public void syncFailed(MXSyncError error) {
		if(numberOfDownloadableSongs > 0)
			numberOfDownloadableSongs--;
		Log.d(TAG, "current numberOfDownloadableSongs: "+ numberOfDownloadableSongs);
		stopSplash();
		MXMessages message=MXMessages.getInstance();	    
		message.setLanguage(MGLanguage.MGLanguageHebrew);				    
		message.displayMessageForCode(this , MXMessages.MSG_ADD_SONG_FAILED, this );
	}

	@Override
	public void syncSongsCopyrightDeleted(String[] songNames) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void imagesReceived(HashMap<String, Bitmap> images, boolean isPlaylistImages) {
		if(images==null){
			Log.d(TAG, "images is null ");
			return;
		}
		Log.d(TAG, "images received, size: "+images.size());
		if(isPlaylistImages){
			if(featuredPlaylistAdapter != null){
				for(int i=0; i<featuredPlaylistAdapter.getCount(); i++){
					MusicEntity entity = (MusicEntity)featuredPlaylistAdapter.getItem(i);
					Bitmap img = images.get(entity.getId());
					if(img != null){
						Drawable d = new BitmapDrawable(img);
						entity.setImageDrawable(d);
					}
				}
			}
			for(int i=0; i<playlistsGallery.getAdapter().getCount(); i++){
				MusicEntity entity = (MusicEntity)playlistsGallery.getAdapter().getItem(i);
				Bitmap img = images.get(entity.getId());
				if(img != null){
					Drawable d = new BitmapDrawable(img);
					entity.setImageDrawable(d);
				}
			}
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					playlistsGallery.setAdapter(playlistsGallery.getAdapter());
				}
			};
			playlistsGallery.post(r);
		}
		else{
			if(featuredAlbumAdapter != null){
				for(int i=0; i<featuredAlbumAdapter.getCount(); i++){
					MusicEntity entity = (MusicEntity)featuredAlbumAdapter.getItem(i);
					Bitmap img = images.get(entity.getId());
					if(img != null){
						Drawable d = new BitmapDrawable(img);
						entity.setImageDrawable(d);
					}
				}
			}
			for(int i=0; i<albumsGallery.getAdapter().getCount(); i++){
				MusicEntity entity = (MusicEntity)albumsGallery.getAdapter().getItem(i);
				Bitmap img = images.get(entity.getId());
				if(img != null){
					Drawable d = new BitmapDrawable(img);
					entity.setImageDrawable(d);
				}
			}
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					albumsGallery.setAdapter(albumsGallery.getAdapter());
				}
			};
			albumsGallery.post(r);
			for(int i=0; i<songsGallery.getAdapter().getCount(); i++){
				MusicEntity entity = (MusicEntity)songsGallery.getAdapter().getItem(i);
				Bitmap img = images.get(entity.getAlbumId());
				if(img != null){
					Drawable d = new BitmapDrawable(img);
					entity.setImageDrawable(d);
				}
			}
			Runnable r1 = new Runnable() {
				
				@Override
				public void run() {
					songsGallery.setAdapter(songsGallery.getAdapter());
				}
			};
			songsGallery.post(r1);
		}
		
		Runnable r1 = new Runnable() {
			
			@Override
			public void run() {
				listFeatured.setAdapter(listFeatured.getAdapter());
			}
		};
		listFeatured.post(r1);
	}

	@Override
	public void addAlbum(String albumID) {
		
			//// album needs to be purchased
			numberOfDownloadableSongs = 0;
			MXSettings _settings = new MXSettings(MXUIMusicCatalogScreen.this);
			_settings.Load();
			MXSyncRequest mx_request = new MXSyncRequest(DFLT_VERSION, isRegisterAccepted);
			mx_request.addAlbum(albumID);
			mx_request.setTimestamp(_settings.getLastResponseTimestamp());
			sync.sync(mx_request);
			startSplash();

		
	}

	@Override
	public void addPlaylist(String playlistID) {
		
		//// playlist needs to be purchased
			numberOfDownloadableSongs = 0;
			MXSettings _settings = new MXSettings(MXUIMusicCatalogScreen.this);
			_settings.Load();
			MXSyncRequest mx_request = new MXSyncRequest(DFLT_VERSION, isRegisterAccepted);
			mx_request.addPlaylist(playlistID);
			mx_request.setTimestamp(_settings.getLastResponseTimestamp());
			sync.sync(mx_request);
			startSplash();

		
	}
}

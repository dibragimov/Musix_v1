package il.co.pelephone.musix.UI.utility;

import il.co.pelephone.musix.comm.IMXImageCallback;
import android.app.Activity;

public class MXUIUtilityMyMusicAllAdapters {
	
	private static MXUIUtilityMyMusicAllAdapters _instance;
		
	private Activity context;
//	private IMXImageCallback imageCallBack;
	
	private MXUIUtilityMyMusicAlbumAdapter albumAdapter;
//	private MXUIUtilityMyMusicAlbumAdapterGrid albumAdapterGrid;
	private MXUIUtilityMyMusicArtistAdapter artistAdapter;
	private MXUIUtilityMyMusicGenresAdapter genreAdapter;
	private MXUIUtilityMyMusicPlaylistAdapter playlistAdapter;
	private MXUIUtilityMyMusicSongAdapter songAdapter;
	private MXUIUtilityMyMusicAdapter generalMyMusicAdapter;
	
	public static MXUIUtilityMyMusicAllAdapters getInstance(Activity context){
		if (_instance == null)
			_instance = new MXUIUtilityMyMusicAllAdapters(context);
		return _instance;
	}
	
	private MXUIUtilityMyMusicAllAdapters(Activity context){
		this.context = context;
	}
	
	public synchronized MXUIUtilityMyMusicAlbumAdapter getAlbumAdapter(IMXImageCallback callback) {
		if(albumAdapter == null)
			albumAdapter = new MXUIUtilityMyMusicAlbumAdapter(context, callback);
		return albumAdapter;
	}
	
//	public synchronized MXUIUtilityMyMusicAlbumAdapterGrid getAlbumAdapterGrid(IMXImageCallback callback) {
//		if(albumAdapterGrid == null)
//			albumAdapterGrid = new MXUIUtilityMyMusicAlbumAdapterGrid(context, callback);
//		return albumAdapterGrid;
//	}
	
	public MXUIUtilityMyMusicArtistAdapter getArtistAdapter() {
		if(artistAdapter == null)
			artistAdapter = new MXUIUtilityMyMusicArtistAdapter(context);
		return artistAdapter;
	}
	
	public MXUIUtilityMyMusicGenresAdapter getGenresAdapter() {
		if(genreAdapter == null)
			genreAdapter = new MXUIUtilityMyMusicGenresAdapter(context);
		return genreAdapter;
	}
	
	public MXUIUtilityMyMusicPlaylistAdapter getPlaylistAdapter(IMXImageCallback callback) {
		if(playlistAdapter == null)
			playlistAdapter = new MXUIUtilityMyMusicPlaylistAdapter(context, callback);
		return playlistAdapter;
	}
	
	public MXUIUtilityMyMusicSongAdapter getSongAdapter() {
		if(songAdapter == null)
			songAdapter = new MXUIUtilityMyMusicSongAdapter(context);
		return songAdapter;
	}
	
	public MXUIUtilityMyMusicAdapter getGeneralMyMusicAdapter() {
		if(generalMyMusicAdapter == null)
			generalMyMusicAdapter = new MXUIUtilityMyMusicAdapter(context);
		return generalMyMusicAdapter;
	}
	
	public void nullify(){
		_instance = null;
	}
	
//	public void releaseResources(){
//		albumAdapter = null;
//		artistAdapter = null;
//		genreAdapter = null;
//		playlistAdapter = null;
//		songAdapter = null;
//		generalMyMusicAdapter = null;
//		_instance = null;
//	}
}

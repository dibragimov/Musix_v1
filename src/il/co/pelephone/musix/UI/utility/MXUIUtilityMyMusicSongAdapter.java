package il.co.pelephone.musix.UI.utility;

import il.co.pelephone.musix.UI.MXUIMyMusicScreen;
import il.co.pelephone.musix.UI.R;
import il.co.pelephone.musix.comm.IMXCommCallback;
import il.co.pelephone.musix.comm.IMXSyncCallback;
import il.co.pelephone.musix.comm.MXSettings;
import il.co.pelephone.musix.comm.MXSync;
import il.co.pelephone.musix.comm.MXSyncRequest;
import il.co.pelephone.musix.data.Song;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.AlbumSongsTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.PlaylistSongsTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.PlaylistTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.SongsTableMetaData;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class MXUIUtilityMyMusicSongAdapter extends MXUIUtilityMyMusicBaseAdapter {

	Activity context;
	Cursor curSongs;
	ArrayList<Song> songs;
	ArrayList<Song> filteredSongs;
	public Boolean isInEditMode;
	private int userPlaylistID = -1;
	private String filter = "";
	private boolean isDefaultSortOrder = true;;
	
	public final static int DEFAULTSORTORDER = 1;
	public final static int TIMESTAMPSORTORDER = 2;
	
	private static final  String DFLT_VERSION="1.1";
	private boolean isRegisterAccepted=false;
	
	public MXUIUtilityMyMusicSongAdapter(Activity context) {
		super();
		this.context = context;
		isInEditMode = false;
		initialize(isDefaultSortOrder);
	}
	
	public void initialize(boolean isDefaultSortOrder){
		songs = new ArrayList<Song>();
		if(isDefaultSortOrder)
			curSongs = context.managedQuery(SongsTableMetaData.CONTENT_URI, null, null, null, SongsTableMetaData.DEFAULT_SORT_ORDER);
		else
			curSongs = context.managedQuery(SongsTableMetaData.CONTENT_URI, null, null, null, SongsTableMetaData.TIMESTAMP_SORT_ORDER);
		
		context.startManagingCursor(curSongs);
		for (int i = 0; i < curSongs.getCount(); i++) {
			curSongs.moveToPosition(i);
			
			Song s = new Song(curSongs.getInt(0), curSongs.getString(1), curSongs.getString(2), curSongs.getString(3), curSongs.getString(4), curSongs.getString(5), curSongs.getInt(6), curSongs.getString(7));
			songs.add(s);
		}
		curSongs.close();
	}
	
	public void setDefaultSortOrder(int sortOrder){
		if(sortOrder==TIMESTAMPSORTORDER)
			this.isDefaultSortOrder = false;
		else
			this.isDefaultSortOrder = true;
	}
	
	public boolean getDefaultSortOrder(){
		return isDefaultSortOrder;
	}
	
	public MXUIUtilityMyMusicSongAdapter setCurrentPlaylist(int playlistID){
		if(playlistID < 0) 
			initialize(isDefaultSortOrder);
		else{
			songs = new ArrayList<Song>();
			curSongs = context.managedQuery(Uri.withAppendedPath(PlaylistSongsTableMetaData.CONTENT_URI, ""+playlistID), null, null, null, PlaylistSongsTableMetaData.DEFAULT_SORT_ORDER);
			context.startManagingCursor(curSongs);
			for (int i = 0; i < curSongs.getCount(); i++) {
				curSongs.moveToPosition(i);
				Song s = new Song(curSongs.getInt(0), curSongs.getString(1), curSongs.getString(2), "", curSongs.getString(6), "", curSongs.getInt(5), curSongs.getString(4));
				songs.add(s);
			}
			curSongs.close();
		}
		return this;
	}
	
	public MXUIUtilityMyMusicSongAdapter setCurrentAlbum(int albumID){
		if(albumID < 0) 
			initialize(isDefaultSortOrder);
		else{
			songs = new ArrayList<Song>();
			curSongs = context.managedQuery(Uri.withAppendedPath(AlbumSongsTableMetaData.CONTENT_URI, ""+albumID), null, null, null, AlbumSongsTableMetaData.DEFAULT_SORT_ORDER);
			context.startManagingCursor(curSongs);
			for (int i = 0; i < curSongs.getCount(); i++) {
				curSongs.moveToPosition(i);
				Song s = new Song(curSongs.getInt(2), curSongs.getString(3), curSongs.getString(4), curSongs.getString(1), curSongs.getString(8), "", curSongs.getInt(7), curSongs.getString(6));
				songs.add(s);
			}
			curSongs.close();
		}
		return this;
	}

	@Override
	public int getCount() {
		if(filteredSongs != null)
			return filteredSongs.size();
		return songs.size();
	}

	@Override
	public Object getItem(int position) {
		if(filteredSongs != null)
			return filteredSongs.get(position);
		return songs.get(position);
	}

	@Override
	public long getItemId(int position) {
		if(filteredSongs != null)
			return filteredSongs.get(position).ID;
		return songs.get(position).ID;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		
		if(!isInEditMode){
			if(convertView== null){
				LayoutInflater inflater = context.getLayoutInflater();
				view = inflater.inflate(R.layout.rowmymusicsong, null);
			}
			else
				view = convertView;
			Song song = (filteredSongs == null) ? songs.get(position):filteredSongs.get(position);
			
			TextView txtAlbumTitle = (TextView)view.findViewById(R.id.txtMyMusicSongRowTitle);
			txtAlbumTitle.setText(song.name);//catalogMediaID);
			
			return view;
		}
		else{
			if(convertView== null){
				LayoutInflater inflater = context.getLayoutInflater();
				view = inflater.inflate(R.layout.rowmymusicsongediting, null);
			}
			else
				view = convertView;
			((Button)view.findViewById(R.id.btnMyMusicAddToPlaylist)).setEnabled(true);
			
			final Song song = (filteredSongs == null) ? songs.get(position):filteredSongs.get(position);
			final int songID = song.ID;
			TextView txtAlbumTitle = (TextView)view.findViewById(R.id.txtMyMusicEditSongTitle);
			txtAlbumTitle.setText(song.name);//catalogMediaID);
			
			((Button)view.findViewById(R.id.btnMyMusicDeleteSong)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					showDialog(song.ID); 
					Log.i("MXUIUtilityMyMusicSongAdapter","the song will be deleted: "+songID);
				}

				
			});
			
			if(!isInUserPlaylist(songID))
				((Button)view.findViewById(R.id.btnMyMusicAddToPlaylist)).setOnClickListener(new OnClickListener() {
				
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Log.i("MXUIUtilityMyMusicSongAdapter","the song will be added to playlist: "+songID);
						addSongToUserPlaylist(song);
					}
				});
			else
				((Button)view.findViewById(R.id.btnMyMusicAddToPlaylist)).setEnabled(false);
			
			return view;
		}
		
	}
	
	private void showDialog(final int songID) {
		new AlertDialog.Builder(context.getParent())
		.setTitle(context.getResources().getString(R.string.mymusic_alert_title_deletesong)) 
	    .setMessage(context.getResources().getString(R.string.mymusic_alert_message_deletesong)) 
	    .setPositiveButton(context.getResources().getString(R.string.mymusic_yes), new DialogInterface.OnClickListener() { 
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Song deletingSong = null;
//				deleteSong(songID);
				//// remove from adapter as well
				for (Song song : songs) {
					if(song.ID == songID)
						deletingSong = song;
				}
				if(deletingSong != null){
//					songs.remove(deletingSong);
				////end of remove from adapter as well
				//// refresh the screen
				
					MXSyncRequest mx_request = new MXSyncRequest(DFLT_VERSION, isRegisterAccepted);
					MXSettings _settings = new MXSettings(context);
					_settings.Load();
					mx_request.addDeletedMedia(deletingSong.catalogMediaID);
						
					mx_request.setTimestamp(_settings.getLastResponseTimestamp());
					MXSync sync = new MXSync((IMXSyncCallback)context, context.getContentResolver(), _settings);
					sync.sync(mx_request);
				}
				((ListActivity)context).setListAdapter(MXUIUtilityMyMusicSongAdapter.this);
				
			} 
	     }) 
	    .setNegativeButton(context.getResources().getString(R.string.mymusic_no), null)
	     .show();
	     
	     
	     /*AlertDialog dialog = new AlertDialog.Builder(context.getParent())
	     .create();
		
		 LayoutInflater inflater = context.getLayoutInflater();
		 View dialodView = inflater.inflate(R.layout.mxuidialog, null);
		 ((TextView)dialodView.findViewById(R.id.txtDialogTitle)).setText(context.getResources().getString(R.string.mymusic_alert_title_deletesong));
		 ((TextView)dialodView.findViewById(R.id.txtDialogMessage)).setText(context.getResources().getString(R.string.mymusic_alert_message_deletesong));
		 dialog.setView(dialodView);
		 dialog.show();*/
	}
	
	public void addSongToUserPlaylist(Song song){
		createUserPlaylist();//// creates if it does not exist
		if(isInUserPlaylist(song.ID))////if it is in user playlist - no need to add it again
			return;
		ContentResolver cr = context.getContentResolver();
		Cursor cursorPL = cr.query(Uri.withAppendedPath(PlaylistTableMetaData.CONTENT_URI_WITH_USERID, context.getResources().getString(R.string.mymusic_playlist2go)), null, null, null, PlaylistTableMetaData.DEFAULT_SORT_ORDER);
		if(cursorPL.getCount()>0){
			cursorPL.moveToFirst();
			int playlistID = cursorPL.getInt(0);
			int songCount =  cursorPL.getInt(3);
			
			ContentValues values = new ContentValues();
			values.put(PlaylistSongsTableMetaData.PLAYLISTSONG_PLAYLISTID, playlistID);
			values.put(PlaylistSongsTableMetaData.PLAYLISTSONG_SONGID, song.ID);
			values.put(PlaylistSongsTableMetaData.PLAYLISTSONG_TRACKNUMBER, songCount+1);
			cr.insert(PlaylistSongsTableMetaData.CONTENT_URI, values);
		}
		
		cursorPL.close();
		Log.i("MXUIUtilityMyMusicSongAdapter","song or playlist created "+Uri.withAppendedPath(PlaylistTableMetaData.CONTENT_URI_WITH_USERID, context.getResources().getString(R.string.mymusic_playlist2go)).toString());
		((IMXSyncCallback)context).syncCompleted(0);
	}
	
	private int createUserPlaylist(){
		ContentResolver cr = context.getContentResolver();
		
		Cursor cursorPL = context.managedQuery(Uri.withAppendedPath(PlaylistTableMetaData.CONTENT_URI_WITH_USERID, context.getResources().getString(R.string.mymusic_playlist2go)), null, null, null, PlaylistTableMetaData.DEFAULT_SORT_ORDER);
		context.startManagingCursor(cursorPL);
		int counts = cursorPL.getCount() ;
		
		if( counts < 1){
			
			ContentValues values = new ContentValues();
			values.put(PlaylistTableMetaData.PLAYLIST_USERPLAYLISTID, context.getResources().getString(R.string.mymusic_playlist2go));
			values.put(PlaylistTableMetaData.PLAYLIST_NAME, context.getResources().getString(R.string.mymusic_playlist2go));
			Uri plUri = cr.insert(PlaylistTableMetaData.CONTENT_URI, values);
			userPlaylistID = Integer.parseInt(plUri.getLastPathSegment());
			Log.i("MXUIUtilityMyMusicSongAdapter","playlist2go id: "+plUri.getLastPathSegment());
		}
		else{
			cursorPL.moveToFirst();
			userPlaylistID = cursorPL.getInt(0);
		}
		cursorPL.close();
		return userPlaylistID;
	}
	
	////may need optimization - storing all playlist songs (or just IDs) in array, getting it only once (one read operation from a database), and then checking from local array 
	public boolean isInUserPlaylist(int songID){
		
		if(userPlaylistID <0)
			createUserPlaylist();
		
		if(userPlaylistID > 0){	
			Cursor cursorPL = context.managedQuery(Uri.withAppendedPath(PlaylistSongsTableMetaData.CONTENT_URI, ""+userPlaylistID), null, null, null, PlaylistSongsTableMetaData.DEFAULT_SORT_ORDER);
			context.startManagingCursor(cursorPL);
			for (int i = 0; i < cursorPL.getCount(); i++) {
				cursorPL.moveToPosition(i);
				if(cursorPL.getInt(0)==songID){ //// song ID is the first column
					cursorPL.close();
					return true;
				}
			}
			cursorPL.close();
			return false;
		}
		return false;
	}
	
	public void deleteSong (int songID){
		ContentResolver cr = context.getContentResolver();
		int count = cr.delete(SongsTableMetaData.CONTENT_URI, SongsTableMetaData._ID+"=?", 
				new String[]{""+songID});
		
		Log.i("MXUIUtilityMyMusicSongAdapter","song deleted "+count);
	}
	
	public ArrayList<Song> getCurrentSongsList(){
		return songs;
	}
	
	public void setFilter(String startStr){
		filter = startStr;
		filterSongs();
	}
	
	private void filterSongs(){
		if(filter == null || filter.length() == 0){
			filteredSongs = null;
			return;
		}
		filteredSongs = new ArrayList<Song>();
		for (Song song : songs) {
			if(song.name.toLowerCase().contains(filter.toLowerCase()))
				filteredSongs.add(song);
		}
	}

}

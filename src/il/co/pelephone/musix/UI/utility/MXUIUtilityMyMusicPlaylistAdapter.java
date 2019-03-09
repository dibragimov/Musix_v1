package il.co.pelephone.musix.UI.utility;

import java.util.ArrayList;
import java.util.HashMap;

import il.co.pelephone.musix.UI.R;
import il.co.pelephone.musix.comm.IMXImageCallback;
import il.co.pelephone.musix.comm.MXCoverImagesManager;
import il.co.pelephone.musix.comm.MXCoverImagesManager.ImageSize;
import il.co.pelephone.musix.data.Album;
import il.co.pelephone.musix.data.Playlist;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.PlaylistTableMetaData;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore.Audio.Playlists;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MXUIUtilityMyMusicPlaylistAdapter extends MXUIUtilityMyMusicBaseAdapter implements IMXImageCallback{

	private Activity context;
	ArrayList<Playlist> playlists;
	ArrayList<Playlist> filteredPlaylists;
	private String filter = "";
	private IMXImageCallback callback;
	private String TAG = "MXUIUtilityMyMusicPlaylistAdapter";
	
	public MXUIUtilityMyMusicPlaylistAdapter(Activity context, IMXImageCallback callback) {
		super();
		this.context = context;
		this.callback = callback;
		initialize();
	}
	
	public void initialize(){
		Cursor curPlaylist = context.managedQuery(PlaylistTableMetaData.CONTENT_URI, null, null, null, PlaylistTableMetaData.DEFAULT_SORT_ORDER);
		context.startManagingCursor(curPlaylist);
		playlists = new ArrayList<Playlist>();
		for (int i = 0; i < curPlaylist.getCount(); i++) {
			curPlaylist.moveToPosition(i);
			Playlist p = new Playlist(curPlaylist.getInt(0), curPlaylist.getString(1), curPlaylist.getString(2), curPlaylist.getString(3), curPlaylist.getInt(4));
			playlists.add(p);
		}
		curPlaylist.close();
		getImages();
		Log.i("MXUIUtilityMyMusicPlaylistAdapter", "Playlist adapter initialized");
	}

	private void getImages() {//// here we need to take the fact that userPlaylistID may be in playlists array - no need to get image
		int countCatalogPlaylistIDNull = 0; 
		for(int j=0; j<playlists.size(); j++){
			if(playlists.get(j).catalogPlaylistID == null)
				countCatalogPlaylistIDNull++;
		}
		
		String[] playlistIDs = new String[playlists.size()-countCatalogPlaylistIDNull];
		countCatalogPlaylistIDNull = 0; 
		for(int j=0; j<playlists.size(); j++){
			if(playlists.get(j).catalogPlaylistID != null)
				playlistIDs[j-countCatalogPlaylistIDNull]=playlists.get(j).catalogPlaylistID;
			else
				countCatalogPlaylistIDNull++;
		}
		MXCoverImagesManager.getInstance().getImagesForPlaylists(this, playlistIDs, ImageSize.SIZE_60);
	}
	
	@Override
	public int getCount() {
		if(filteredPlaylists != null)
			return filteredPlaylists.size();
		return playlists.size();
	}

	@Override
	public Object getItem(int position) {
		if(filteredPlaylists != null)
			return filteredPlaylists.get(position);
		return playlists.get(position);
	}

	@Override
	public long getItemId(int position) {
		if(filteredPlaylists != null)
			return filteredPlaylists.get(position).ID;
		return playlists.get(position).ID;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if(convertView == null){
			LayoutInflater inflater = context.getLayoutInflater();
			view = inflater.inflate(R.layout.rowmymusicplaylist, null);
		}
		else{
			view = convertView;
		}
		TextView txtPlaylist = (TextView)view.findViewById(R.id.txtMyMusicPlaylistName);
		Playlist p = (filteredPlaylists == null) ? playlists.get(position):filteredPlaylists.get(position);
		String name = p.name;//// catalogPlaylistID is 3rd column
		if(name == null || name.length() ==0)//// either catalogPlaylistID or userPlaylistID is not null
			name = p.userPlaylistID;//// userPlaylistID is 3rd column
		txtPlaylist.setText(name);
		
		Drawable icon = p.getImageDrawable();
		if(icon != null)
			((ImageView)view.findViewById(R.id.imgMyMusicPlaylistCover)).setImageDrawable(icon);
		else
			((ImageView)view.findViewById(R.id.imgMyMusicPlaylistCover)).setImageBitmap(null);
		return view;
	}
	
	public void setFilter(String startStr){
		filter = startStr;
		filterPlaylists();
	}
	
	private void filterPlaylists(){
		if(filter == null || filter.length() == 0){
			filteredPlaylists = null;
			return;
		}
		filteredPlaylists = new ArrayList<Playlist>();
		for (Playlist playlist : playlists) {
			if((playlist.name != null && playlist.name.toLowerCase().contains(filter.toLowerCase())) || (playlist.userPlaylistID != null && playlist.userPlaylistID.toLowerCase().contains(filter.toLowerCase())))
				filteredPlaylists.add(playlist);
		}
	}
	
	public ArrayList<Playlist> getCurrentPlaylists(){
		if(filteredPlaylists != null)
			return filteredPlaylists;
		else
			return playlists;
	}

	@Override
	public void imagesReceived(HashMap<String, Bitmap> images, boolean isPlaylistImages) {
		if(images==null){
			Log.d(TAG, "images is null ");
			return;
		}
		Log.d(TAG, "images received, size: "+images.size());
		if(isPlaylistImages){
			//ListAdapter adapter = 
			
			for (String key : images.keySet()) {
				Log.d(TAG, "key in images set: "+key);
			}
			
			
			for(int i=0; i<playlists.size(); i++){
				Playlist entity = playlists.get(i);
				Bitmap img = images.get(entity.catalogPlaylistID);
				Log.d(TAG, "catalogAlbumID: " + entity.catalogPlaylistID);
				if(img != null){
					Log.d(TAG, "img not null");
					Drawable d = new BitmapDrawable(img);
					entity.setImageDrawable(d);
				}
			}
			Log.d(TAG, "images received, playlist");
		
		}
		callback.imagesReceived(images, isPlaylistImages);
	}
}

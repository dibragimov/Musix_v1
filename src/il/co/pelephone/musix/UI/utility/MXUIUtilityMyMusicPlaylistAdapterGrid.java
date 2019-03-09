package il.co.pelephone.musix.UI.utility;

import il.co.pelephone.musix.UI.R;
import il.co.pelephone.musix.data.Playlist;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.PlaylistTableMetaData;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MXUIUtilityMyMusicPlaylistAdapterGrid extends MXUIUtilityMyMusicBaseAdapter {

	private Activity context;
	ArrayList<Playlist> playlists;
	ArrayList<Playlist> filteredPlaylists;
	private String filter = "";
	public MXUIUtilityMyMusicPlaylistAdapterGrid(Activity context) {
		super();
		this.context = context;
//		initialize();
	}
	
	public void initialize(ArrayList<Playlist> playlists){
		this.playlists = playlists;
		Log.i("MXUIUtilityMyMusicPlaylistAdapter", "Playlist adapter initialized");
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
			view = inflater.inflate(R.layout.cellmusiccatalogpromotion, null);
		}
		else{
			view = convertView;
		}
		TextView txtPlaylist = (TextView)view.findViewById(R.id.txtCatalogPromotionTitle);
		Playlist p = (filteredPlaylists == null) ? playlists.get(position):filteredPlaylists.get(position);
		String name = p.catalogPlaylistID;//// catalogPlaylistID is 3rd column
		if(name == null || name.length() ==0)//// either catalogPlaylistID or userPlaylistID is not null
			name = p.userPlaylistID;//// userPlaylistID is 3rd column
		txtPlaylist.setText(name);
		Drawable icon = playlists.get(position).getImageDrawable();
		if(icon != null)
			((ImageView)view.findViewById(R.id.imgCatalogPromotionIcon)).setImageDrawable(icon);
		else
			((ImageView)view.findViewById(R.id.imgCatalogPromotionIcon)).setImageBitmap(null);
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
			if((playlist.catalogPlaylistID != null && playlist.catalogPlaylistID.toLowerCase().contains(filter.toLowerCase())) || (playlist.userPlaylistID != null && playlist.userPlaylistID.toLowerCase().contains(filter.toLowerCase())))
				filteredPlaylists.add(playlist);
		}
	}

}

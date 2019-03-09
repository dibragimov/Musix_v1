package il.co.pelephone.musix.UI.utility;

import java.util.ArrayList;

import il.co.pelephone.musix.UI.R;
import il.co.pelephone.musix.data.Artist;
import il.co.pelephone.musix.data.Playlist;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.ArtistsForGenreTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.ArtistsTableMetaData;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MXUIUtilityMyMusicArtistAdapter extends MXUIUtilityMyMusicBaseAdapter{

	private Activity context;
	ArrayList<Artist> artists;
	ArrayList<Artist> filteredArtists;
	private String filter = "";
	private boolean isListLayout;
	
	public MXUIUtilityMyMusicArtistAdapter(Activity context) {
		super();
		this.context = context;
		isListLayout = true;
		initialize();
	}

	public void initialize() {
		Cursor cursor = context.managedQuery(ArtistsTableMetaData.CONTENT_URI, null, null, null, ArtistsTableMetaData.DEFAULT_SORT_ORDER);
		context.startManagingCursor(cursor);
		artists = new ArrayList<Artist>();
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			Artist a = new Artist(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getString(6));
			artists.add(a);
		}
		cursor.close();
	}
	
	public MXUIUtilityMyMusicArtistAdapter setCurrentGenre(int genreID){
		if(genreID < 0) 
			initialize();
		else{
			Cursor cursor = context.managedQuery(Uri.withAppendedPath(ArtistsForGenreTableMetaData.CONTENT_URI, ""+genreID), null, null, null, null);
			context.startManagingCursor(cursor);
			artists = new ArrayList<Artist>();
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				Artist a = new Artist(cursor.getInt(0), cursor.getString(1), "", genreID, cursor.getInt(2), cursor.getInt(3), cursor.getString(4));
				artists.add(a);
			}
			cursor.close();
		}
		return this;
	}

	@Override
	public int getCount() {
		if(filteredArtists != null)
			return filteredArtists.size();
		return artists.size();
	}

	@Override
	public Object getItem(int position) {
		if(filteredArtists != null)
			return filteredArtists.get(position);
		return artists.get(position);
	}

	@Override
	public long getItemId(int position) {
		if(filteredArtists != null)
			return filteredArtists.get(position).ID;
		return artists.get(position).ID;
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 * uses normal album layout to display Artist Name as Title and song count as subtitle
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if(convertView== null){
			LayoutInflater inflater = context.getLayoutInflater();
			view = inflater.inflate(R.layout.rowmymusicalbum, null);
		}
		else
			view = convertView;
		
		Artist artist = (filteredArtists == null) ? artists.get(position):filteredArtists.get(position);
		
		TextView txtAlbumTitle = (TextView)view.findViewById(R.id.txtMyMusicAlbumTitle);
		txtAlbumTitle.setText(artist.catalogArtistName);
		if(isListLayout){
			TextView txtArtistName = (TextView)view.findViewById(R.id.txtMyMusicAlbumArtistName);
			txtArtistName.setText(artist.SongCount + " " +context.getResources().getString(R.string.mymusic_subtitle_songs));
		}
		
		return view;
	}
	
	public void setFilter(String startStr){
		filter = startStr;
		filterArtists();
	}
	
	private void filterArtists(){
		if(filter == null || filter.length() == 0){
			filteredArtists = null;
			return;
		}
		filteredArtists = new ArrayList<Artist>();
		for (Artist artist : artists) {
			if(artist.catalogArtistName != null && artist.catalogArtistName.toLowerCase().contains(filter.toLowerCase()))
				filteredArtists.add(artist);
		}
	}
	
	public void setListLayout(boolean isListLayout){
		this.isListLayout = isListLayout;
	}

}

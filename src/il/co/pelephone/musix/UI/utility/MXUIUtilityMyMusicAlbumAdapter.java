package il.co.pelephone.musix.UI.utility;

import java.util.ArrayList;
import java.util.HashMap;

import il.co.pelephone.musix.UI.R;
import il.co.pelephone.musix.comm.IMXImageCallback;
import il.co.pelephone.musix.comm.MXCoverImagesManager;
import il.co.pelephone.musix.comm.MXCoverImagesManager.ImageSize;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.AlbumsForArtistTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.AlbumsTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.ArtistsForGenreTableMetaData;
import il.co.pelephone.musix.data.Album;
import il.co.pelephone.musix.data.Artist;
import il.co.pelephone.musix.data.Playlist;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MXUIUtilityMyMusicAlbumAdapter extends MXUIUtilityMyMusicBaseAdapter implements IMXImageCallback{

	private Activity context;
	private IMXImageCallback callback;
	private ArrayList<Album> albums;
	private ArrayList<Album> filteredAlbums;
	private String filter = "";
	private String TAG = "MXUIUtilityMyMusicAlbumAdapter";
	private int length;
	public MXUIUtilityMyMusicAlbumAdapter(Activity context, IMXImageCallback callback) {
		super();
		this.context = context;
		this.callback = callback;
		initialize();
	}
	
	public void initialize(){
		Cursor curAlbum = context.managedQuery(AlbumsTableMetaData.CONTENT_URI, null, null, null, AlbumsTableMetaData.DEFAULT_SORT_ORDER);
		context.startManagingCursor(curAlbum);
		length = curAlbum.getCount();
		albums = new ArrayList<Album>();//// 6 columns:_ID, catalogAlbumID, Name, ArtistName, GenreName, SongCount
		////reading data from cursor - filling array with data
		
		for (int i = 0; i < length; i++) {
			curAlbum.moveToPosition(i);
			Album al = new Album(curAlbum.getInt(0), curAlbum.getString(1), curAlbum.getString(2), curAlbum.getString(3), curAlbum.getString(4), curAlbum.getInt(5));
			albums.add(al);
		}
		curAlbum.close();
		Log.i("MXUIUtilityMyMusicAlbumAdapter", "Album adapter initialized");
		getImages();
	}
	
	public MXUIUtilityMyMusicAlbumAdapter setCurrentArtist(int artistID){
		if(artistID < 0) 
			initialize();
		else{
			Cursor cursor = context.managedQuery(Uri.withAppendedPath(AlbumsForArtistTableMetaData.CONTENT_URI, ""+artistID), null, null, null, null);
			context.startManagingCursor(cursor);
			albums = new ArrayList<Album>();
			Log.d("MXUIUtilityMyMusicAlbumAdapter", "number of albums for artists: "+cursor.getCount());
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				Album a = new Album(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(3));
				albums.add(a);
			}
			cursor.close();
		}
		getImages();
		return this;
	}

	private void getImages() {
		String[] albumIDs = new String[albums.size()];
		for(int j=0; j<albums.size(); j++){
			albumIDs[j]=albums.get(j).catalogAlbumID;
		}
		MXCoverImagesManager.getInstance().getImagesForAlbums(this, albumIDs, ImageSize.SIZE_60);
	}

	@Override
	public int getCount() {
		if(filteredAlbums != null)
			return filteredAlbums.size();
		return albums.size();
	}

	@Override
	public Object getItem(int position) {
		if(filteredAlbums != null)
			return filteredAlbums.get(position);
		return albums.get(position);
	}

	@Override
	public long getItemId(int position) {
		if(filteredAlbums != null)
			return filteredAlbums.get(position).ID;
		return albums.get(position).ID;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if(convertView == null){
			LayoutInflater inflater = context.getLayoutInflater();
			view = inflater.inflate(R.layout.rowmymusicalbum, null);
		}
		else
			view = convertView;
		
		Album album = (filteredAlbums == null) ? albums.get(position):filteredAlbums.get(position);
		
		TextView txtAlbumTitle = (TextView)view.findViewById(R.id.txtMyMusicAlbumTitle);
		txtAlbumTitle.setText(album.Name);//// AlbumTitle is 3rd column
		TextView txtArtistName = (TextView)view.findViewById(R.id.txtMyMusicAlbumArtistName);
		txtArtistName.setText(album.ArtistName);//// ArtistName is 4th column
		
		Drawable icon = albums.get(position).getImageDrawable();
		if(icon != null)
			((ImageView)view.findViewById(R.id.imgMyMusicAlbumCover)).setImageDrawable(icon);
		else
			((ImageView)view.findViewById(R.id.imgMyMusicAlbumCover)).setImageBitmap(null);
//		Log.d("MXUIUtilityMyMusicAlbumAdapter", "icon for album is null: "+(icon == null));
		
		return view;
	}
	
	public void setFilter(String startStr){
		filter = startStr;
		filterAlbums();
	}
	
	private void filterAlbums(){
		if(filter == null || filter.length() == 0){
			filteredAlbums = null;
			return;
		}
		filteredAlbums = new ArrayList<Album>();
		for (Album album : albums) {
			if(album.Name != null && album.Name.toLowerCase().contains(filter.toLowerCase()))
				filteredAlbums.add(album);
		}
	}
	
	public ArrayList<Album> getCurrentAlbums(){
		if(filteredAlbums != null)
			return filteredAlbums;
		else
			return albums;
	}

	@Override
	public void imagesReceived(HashMap<String, Bitmap> images, boolean isPlaylistImages) {
		
			if(images==null){
				Log.d(TAG, "images is null ");
				return;
			}
			Log.d(TAG, "images received, size: "+images.size());
			if(!isPlaylistImages){
				//ListAdapter adapter = 
				
//				for (String key : images.keySet()) {
//					Log.d(TAG, "key in images set: "+key);
//				}
				
				
				for(int i=0; i<albums.size(); i++){
					Album entity = albums.get(i);
					Bitmap img = images.get(entity.catalogAlbumID);
//					Log.d(TAG, "catalogAlbumID: " + entity.catalogAlbumID);
					if(img != null){
//						Log.d(TAG, "img not null");
						Drawable d = new BitmapDrawable(img);
						entity.setImageDrawable(d);
					}
				}
				Log.d(TAG, "images received, not playlist");
			
		}
		callback.imagesReceived(images, isPlaylistImages);
	}
}

package il.co.pelephone.musix.UI.utility;

import java.util.ArrayList;
import java.util.HashMap;

import il.co.pelephone.musix.UI.R;
import il.co.pelephone.musix.comm.IMXImageCallback;
import il.co.pelephone.musix.comm.MXCoverImagesManager;
import il.co.pelephone.musix.comm.MXCoverImagesManager.ImageSize;
import il.co.pelephone.musix.data.Album;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.AlbumsForArtistTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.AlbumsTableMetaData;
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
import android.widget.ImageView;
import android.widget.TextView;

public class MXUIUtilityMyMusicAlbumAdapterGrid extends	MXUIUtilityMyMusicBaseAdapter /*implements IMXImageCallback*/{

	private Activity context;
//	private IMXImageCallback callback;
	private ArrayList<Album> albums;
	private ArrayList<Album> filteredAlbums;
	private String filter = "";
	private String TAG = "MXUIUtilityMyMusicAlbumAdapterGrid";
//	private int length;
	public MXUIUtilityMyMusicAlbumAdapterGrid(Activity context/*, IMXImageCallback callback*/) {
		super();
		this.context = context;
//		this.callback = callback;
//		initialize();
	}
	
	public void initialize(ArrayList<Album> albums){
		this.albums = albums;
		Log.i("MXUIUtilityMyMusicAlbumAdapter", "Album adapter initialized");
	}
	
	/*public MXUIUtilityMyMusicAlbumAdapterGrid setCurrentArtist(int artistID){
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
	}*/

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
			view = inflater.inflate(R.layout.cellmusiccatalogpromotion, null);
		}
		else
			view = convertView;
		
		Album album = (filteredAlbums == null) ? albums.get(position):filteredAlbums.get(position);
		
		TextView txtAlbumTitle = (TextView)view.findViewById(R.id.txtCatalogPromotionTitle);
		txtAlbumTitle.setText(album.Name);//// AlbumTitle is 3rd column
//		TextView txtArtistName = (TextView)view.findViewById(R.id.txtMyMusicAlbumArtistName);
//		txtArtistName.setText(album.ArtistName);//// ArtistName is 4th column
		
		Drawable icon = albums.get(position).getImageDrawable();
		if(icon != null)
			((ImageView)view.findViewById(R.id.imgCatalogPromotionIcon)).setImageDrawable(icon);
		else
			((ImageView)view.findViewById(R.id.imgCatalogPromotionIcon)).setImageBitmap(null);
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
			if(album.catalogAlbumID != null && album.catalogAlbumID.toLowerCase().contains(filter.toLowerCase()))
				filteredAlbums.add(album);
		}
	}
/*
	@Override
	public void imagesReceived(HashMap<String, Bitmap> images, boolean isPlaylistImages) {
		
			if(images==null){
				Log.d(TAG, "images is null ");
				return;
			}
			Log.d(TAG, "images received, size: "+images.size());
			if(!isPlaylistImages){
				//ListAdapter adapter = 
				
				for (String key : images.keySet()) {
					Log.d(TAG, "key in images set: "+key);
				}
				
				
				for(int i=0; i<albums.size(); i++){
					Album entity = albums.get(i);
					Bitmap img = images.get(entity.catalogAlbumID);
					Log.d(TAG, "catalogAlbumID: " + entity.catalogAlbumID);
					if(img != null){
						Log.d(TAG, "img not null");
						Drawable d = new BitmapDrawable(img);
						entity.setImageDrawable(d);
					}
				}
				Log.d(TAG, "images received, not playlist");
			
		}
		callback.imagesReceived(images, isPlaylistImages);
	}*/

}

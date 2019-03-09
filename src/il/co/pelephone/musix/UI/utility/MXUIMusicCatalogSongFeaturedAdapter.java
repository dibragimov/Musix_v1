package il.co.pelephone.musix.UI.utility;

import il.co.pelephone.musix.UI.R;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata;
import il.co.pelephone.musix.data.MusicEntity;
import il.co.pelephone.musix.data.Song;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.SongsTableMetaData;
import il.co.pelephone.musix.data.MusicEntity.EntityType;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MXUIMusicCatalogSongFeaturedAdapter extends
		MXUIMusicCatalogFeaturedAdapter {

//	private Song[] songsInDatabase;
//	Cursor curSongs;
	String TAG = "MXUIMusicCatalogSongFeaturedAdapter";
	private ArrayList<String> checkedSongs;
	
	public MXUIMusicCatalogSongFeaturedAdapter(
			ArrayList<MusicEntity> collection, Context ctx) {
		super(collection, ctx);
		
//		curSongs = ctx.getContentResolver().query(MXLocalContentProviderMetadata.SongsTableMetaData.CONTENT_URI, null, null, null, SongsTableMetaData.TIMESTAMP_SORT_ORDER);
//		if(curSongs!=null && curSongs.getCount() > 0)
//		{
//			songsInDatabase = new Song[curSongs.getCount()];
//			for (int i = 0; i < curSongs.getCount(); i++) {
//				curSongs.moveToPosition(i);
//				Song s = new Song(curSongs.getInt(0), curSongs.getString(1), curSongs.getString(2), curSongs.getString(3), curSongs.getString(4), curSongs.getString(5), curSongs.getInt(6), curSongs.getString(7));
//				songsInDatabase[i]=s;
//			}
//			
//			Log.d(TAG, "songs in array(database): "+songsInDatabase.length);
//		}
//		curSongs.close();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view ;
		if(convertView == null){
			LayoutInflater inflater = LayoutInflater.from(ctx);
			view = inflater.inflate(R.layout.rowmusiccatalogfeatured, null);
		}
		else
			view = convertView;
		
		TextView txt = (TextView)view.findViewById(R.id.txtMusicCatalogFeaturedName);
		txt.setText(collection.get(position).getName());
		
//		Drawable drawable = collection.get(position).getImageDrawable();
		ImageView img = (ImageView)view.findViewById(R.id.imgMusicCatalogFeaturedIcon);
		
		if(isInDatabase(collection.get(position).getId())){
			img.setImageResource(R.drawable.music_catalog_already_checked);
		}
		else{
			img.setImageResource(R.drawable.music_catalog_empty_check_box);
		}
		if(collection.get(position).isSelected() && !isInDatabase(collection.get(position).getId()))
			img.setImageResource(R.drawable.music_catalog_selected_check_box);
		
		if(collection.get(position).getType()==EntityType.Song){
			view.findViewById(R.id.imgMusicCatalogFeaturedArrow).setVisibility(View.INVISIBLE);
		}
		
		
		return view;
	}
	
	public void setCheckedSongs(ArrayList<String> checkedSongs) {
		this.checkedSongs = checkedSongs;
		Log.d(TAG, "number of checked songs: "+checkedSongs.size());
	}

	public boolean hasSelectedSongs(){
		for (MusicEntity entity : collection) {
			if(entity.isSelected())
				return true;
		}
		return false;
	}
	
	private boolean isInDatabase(String catalogMediaID){
		if(this.checkedSongs==null){
			Log.d(TAG, "song NOT checked - "+catalogMediaID);
			return false;
		}
		for (String songID : this.checkedSongs) {
			if(songID.equalsIgnoreCase(catalogMediaID)){
				Log.d(TAG, "song checked - "+catalogMediaID);
				return true;
			}
		}
		return false;
	}

}

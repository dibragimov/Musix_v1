package il.co.pelephone.musix.UI.utility;

import il.co.pelephone.musix.UI.R;
import il.co.pelephone.musix.data.MusicEntity;
import il.co.pelephone.musix.data.MusicEntity.EntityType;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MXUIMusicCatalogFeaturedAdapter extends BaseAdapter {

	protected ArrayList<MusicEntity> collection;
	protected Context ctx;
	private IMXUIDownloadEntity downloadCallback;
	private boolean isSearchAlbum;
	
	public boolean isSearchAlbum() {
		return isSearchAlbum;
	}

	public void setSearchAlbum(boolean isSearchAlbum) {
		this.isSearchAlbum = isSearchAlbum;
	}

	public IMXUIDownloadEntity getDownloadCallback() {
		return downloadCallback;
	}

	public void setDownloadCallback(IMXUIDownloadEntity downloadCallback) {
		this.downloadCallback = downloadCallback;
	}

	public MXUIMusicCatalogFeaturedAdapter(ArrayList<MusicEntity> collection,
			Context ctx) {
		super();
		if(collection == null) collection = new ArrayList<MusicEntity>();////to prevent NPE
		this.collection = collection;
		this.ctx = ctx;
	}

	@Override
	public int getCount() {
		return collection.size();
	}

	@Override
	public Object getItem(int position) {
		return collection.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
//		return null;
		View view ;
		if(convertView == null){
			LayoutInflater inflater = LayoutInflater.from(ctx);
			view = inflater.inflate(R.layout.rowmusiccatalogfeatured, null);
		}
		else
			view = convertView;
		
		if(isSearchAlbum){
			TextView txtSubname = (TextView)view.findViewById(R.id.txtMusicCatalogFeaturedSubname);
			txtSubname.setVisibility(View.VISIBLE);
			txtSubname.setText(collection.get(position).getArtistName());
		}
		else
			view.findViewById(R.id.txtMusicCatalogFeaturedSubname).setVisibility(View.GONE);
		
		TextView txt = (TextView)view.findViewById(R.id.txtMusicCatalogFeaturedName);
		txt.setText(collection.get(position).getName());
		
		Drawable drawable = collection.get(position).getImageDrawable();
		ImageView img = (ImageView)view.findViewById(R.id.imgMusicCatalogFeaturedIcon);
		if(drawable != null ){
			
			img.setBackgroundDrawable(drawable);
			if (collection.get(position).getType()==EntityType.Album || collection.get(position).getType()==EntityType.Playlist){
				img.setImageResource(R.drawable.dowload_overlay);
				img.setTag(collection.get(position));
				img.setOnClickListener(new OnClickListener() {
				
					@Override
					public void onClick(View v) {
						MusicEntity entity = (MusicEntity)v.getTag();
						if(entity.getType()==EntityType.Album)
							if(downloadCallback!=null) downloadCallback.addAlbum(entity.getId());
						else if(entity.getType()==EntityType.Playlist)
							if(downloadCallback!=null) downloadCallback.addPlaylist(entity.getId());
					}
				});
			}
		}
		else
			img.setImageBitmap(null);
		
		return view;
	}

}

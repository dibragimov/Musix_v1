package il.co.pelephone.musix.UI.utility;

import il.co.pelephone.musix.UI.R;
import il.co.pelephone.musix.data.MusicEntity;
import il.co.pelephone.musix.data.Playlist;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MXUIMusicCatalogMusicEntityPromotionAdapter extends BaseAdapter {

	private Context ctx;
	private ArrayList<MusicEntity> entityLists;
	
	public MXUIMusicCatalogMusicEntityPromotionAdapter(Context ctx, ArrayList<MusicEntity> playlists) {
		super();
		this.ctx = ctx;
		this.entityLists = (playlists!= null)?playlists:new ArrayList<MusicEntity>();
	}

	@Override
	public int getCount() {
		return entityLists.size();//0;
	}

	@Override
	public Object getItem(int position) {
		return entityLists.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//return null;
		View v;
		if(convertView == null){
			LayoutInflater inflater = LayoutInflater.from(ctx);
			v = inflater.inflate(R.layout.cellmusiccatalogpromotion, null);
		}
		else
			v = convertView;
		
		((TextView)v.findViewById(R.id.txtCatalogPromotionTitle)).setText(entityLists.get(position).getName());
		Drawable icon = entityLists.get(position).getImageDrawable();
		if(icon != null)
			((ImageView)v.findViewById(R.id.imgCatalogPromotionIcon)).setBackgroundDrawable(icon);
		else
			((ImageView)v.findViewById(R.id.imgCatalogPromotionIcon)).setBackgroundResource(R.drawable.icon);
		return v;
	}

}

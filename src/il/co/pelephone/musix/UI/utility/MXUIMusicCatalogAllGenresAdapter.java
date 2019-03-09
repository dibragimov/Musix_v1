package il.co.pelephone.musix.UI.utility;

import il.co.pelephone.musix.UI.R;
import il.co.pelephone.musix.data.MusicEntity;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MXUIMusicCatalogAllGenresAdapter extends BaseAdapter {

	private Context ctx;
	private ArrayList<MusicEntity> genres;
	
	public MXUIMusicCatalogAllGenresAdapter(Context ctx, ArrayList<MusicEntity> genres) {
		super();
		this.ctx = ctx;
		this.genres = genres;
	}

	@Override
	public int getCount() {
		return genres.size();
	}

	@Override
	public Object getItem(int position) {
		return genres.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//return null;
		View genreView;
		if(convertView == null){
			LayoutInflater inflater = LayoutInflater.from(ctx);
			genreView = inflater.inflate(R.layout.rowmymusicgenre, null);
		}
		else
			genreView = convertView;
		
		((TextView)genreView.findViewById(R.id.txtMyMusicGenreRowTitle)).setText(genres.get(position).getName());
		return genreView;
	}

}

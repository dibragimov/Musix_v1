package il.co.pelephone.musix.UI.utility;

import java.util.ArrayList;

import il.co.pelephone.musix.UI.R;
import il.co.pelephone.musix.data.Genre;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.GenresTableMetaData;
import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MXUIUtilityMyMusicGenresAdapter extends MXUIUtilityMyMusicBaseAdapter {

	private Activity context;
	ArrayList<Genre> genres;
	ArrayList<Genre> filteredGenres;
	private String filter = "";
	
	public MXUIUtilityMyMusicGenresAdapter(Activity context) {
		super();
		this.context = context;
		initialize();
	}


	private void initialize() {
		Cursor curGenre = context.managedQuery(GenresTableMetaData.CONTENT_URI, null, null, null, GenresTableMetaData.DEFAULT_SORT_ORDER);
		context.startManagingCursor(curGenre);
		genres = new ArrayList<Genre>();
		for (int i = 0; i < curGenre.getCount(); i++) {
			curGenre.moveToPosition(i);
			Genre g = new Genre(curGenre.getInt(0), curGenre.getString(1), curGenre.getInt(2), curGenre.getInt(3), curGenre.getString(4)); 
			genres.add(g);
		}
		curGenre.close();
		Log.i("MXUIUtilityMyMusicGenresAdapter", "Genres adapter initialized");
	}
	

	@Override
	public int getCount() {
		if(filteredGenres != null)
			return filteredGenres.size();
		return genres.size();
	}

	@Override
	public Object getItem(int position) {
		if(filteredGenres != null)
			return filteredGenres.get(position);
		return genres.get(position);
	}

	@Override
	public long getItemId(int position) {
		if(filteredGenres != null)
			return filteredGenres.get(position).ID;
		return genres.get(position).ID;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View myMusicGenreItem;
		if(convertView== null){
			LayoutInflater inflater = context.getLayoutInflater();
			myMusicGenreItem = inflater.inflate(R.layout.rowmymusicgenre, null);
		}
		else
			myMusicGenreItem = convertView;
		TextView title = (TextView)myMusicGenreItem.findViewById(R.id.txtMyMusicGenreRowTitle);
		Genre g = (filteredGenres == null) ? genres.get(position):filteredGenres.get(position);
		title.setText(g.name);// genre name
		return myMusicGenreItem;
	}
	
	public void setFilter(String startStr){
		filter = startStr;
		filterGenres();
	}
	
	private void filterGenres(){
		if(filter == null || filter.length() == 0){
			filteredGenres = null;
			return;
		}
		filteredGenres = new ArrayList<Genre>();
		for (Genre genre : genres) {
			if(genre.name != null && genre.name.toLowerCase().contains(filter.toLowerCase()))
				filteredGenres.add(genre);
		}
	}

}

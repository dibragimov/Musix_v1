package il.co.pelephone.musix.UI.utility;

import il.co.pelephone.musix.UI.R;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.AlbumsTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.ArtistsTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.GenresTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.PlaylistTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.SongsTableMetaData;
import android.app.Activity;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MXUIUtilityMyMusicAdapter extends MXUIUtilityMyMusicBaseAdapter{//BaseAdapter {

	private final int ALLMUSICCATEGORY = 0;
	private final int PLAYLISTCATEGORY = 1;
	private final int GENRECATEGORY = 2;
	private final int ARTISTCATEGORY = 3;
	private final int ALBUMCATEGORY = 4;
	private final int SONGCATEGORY = 5;
	private Activity context; 
	//private SQLiteDatabase dbMusix;
	
	int counts[] = new int[6];
	
	public MXUIUtilityMyMusicAdapter(Activity context) {
		super();
		this.context = context;
		initialize();
	}

	public void initialize() {
		Cursor curAllMus = context.managedQuery(SongsTableMetaData.CONTENT_URI, null, null, null, SongsTableMetaData.TIMESTAMP_SORT_ORDER);
		context.startManagingCursor(curAllMus);
		counts[ALLMUSICCATEGORY] = curAllMus!= null ? curAllMus.getCount() : 0;
//		curAllMus.close();
		Cursor curPlaylist = context.managedQuery(PlaylistTableMetaData.CONTENT_URI, null, null, null, PlaylistTableMetaData.DEFAULT_SORT_ORDER);
		context.startManagingCursor(curPlaylist);
		counts[PLAYLISTCATEGORY] = curPlaylist != null ? curPlaylist.getCount() : 0;
//		curPlaylist.close();
		Cursor curGenre = context.managedQuery(GenresTableMetaData.CONTENT_URI, null, null, null, GenresTableMetaData.DEFAULT_SORT_ORDER);
		context.startManagingCursor(curGenre);
		counts[GENRECATEGORY] = curGenre != null ? curGenre.getCount() : 0;
//		curGenre.close();
		Cursor curArtist = context.managedQuery(ArtistsTableMetaData.CONTENT_URI, null, null, null, ArtistsTableMetaData.DEFAULT_SORT_ORDER);
		context.startManagingCursor(curArtist);
		counts[ARTISTCATEGORY] = curArtist != null ? curArtist.getCount():0;
//		curArtist.close();
		Cursor curAlbums = context.managedQuery(AlbumsTableMetaData.CONTENT_URI, null, null, null, AlbumsTableMetaData.DEFAULT_SORT_ORDER);
		context.startManagingCursor(curAlbums);
		counts[ALBUMCATEGORY] = curAlbums != null ? curAlbums.getCount():0;
//		curAlbums.close();
		
		counts[SONGCATEGORY] = counts[ALLMUSICCATEGORY];
		
	}

	@Override
	public int getCount() {
		return 6; //// totally 6 items
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View oldView, ViewGroup root) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = context.getLayoutInflater();
		View myMusicListItem = inflater.inflate(R.layout.rowmymusic, null);
		//myMusicListItem.setMinimumHeight(49);
		ImageView icon = (ImageView)myMusicListItem.findViewById(R.id.imgMyMusicRowCategory);
		icon.setImageResource(pics[position]);
		TextView title = (TextView)myMusicListItem.findViewById(R.id.txtMyMusicRowTitle);
		TextView subtitle = (TextView)myMusicListItem.findViewById(R.id.txtMyMusicRowSubTitle);
		switch (position) {
			case ALLMUSICCATEGORY:
				title.setText(context.getResources().getString(R.string.mymusic_title_allmusic));
				subtitle.setText(counts[ALLMUSICCATEGORY] + " " +context.getResources().getString(R.string.mymusic_subtitle_allmusic));
				break;
			case PLAYLISTCATEGORY:
				title.setText(context.getResources().getString(R.string.mymusic_title_playlists));
				subtitle.setText(counts[PLAYLISTCATEGORY] + " " + context.getResources().getString(R.string.mymusic_subtitle_playlists));
				break;
			case GENRECATEGORY:
				title.setText(context.getResources().getString(R.string.mymusic_title_genres));
				subtitle.setText(counts[GENRECATEGORY] + " " + context.getResources().getString(R.string.mymusic_subtitle_genres));
				break;
			case ARTISTCATEGORY:
				title.setText(context.getResources().getString(R.string.mymusic_title_artists));
				subtitle.setText(counts[ARTISTCATEGORY] + " " + context.getResources().getString(R.string.mymusic_subtitle_artists));
				break;
			case ALBUMCATEGORY:
				title.setText(context.getResources().getString(R.string.mymusic_title_albums));
				
				subtitle.setText(counts[ALBUMCATEGORY] + " " + context.getResources().getString(R.string.mymusic_subtitle_albums));
				break;
			case SONGCATEGORY:
				title.setText(context.getResources().getString(R.string.mymusic_title_songs));
				subtitle.setText(counts[SONGCATEGORY] + " " +context.getResources().getString(R.string.mymusic_subtitle_songs));
				break;
			default:
				break;
		}
		return myMusicListItem;
	}
	
	
	
	Integer[] pics = new Integer[]{R.drawable.my_music_allmusic_icon, R.drawable.my_music_playlist_icon, 
			R.drawable.my_music_genre_icon, R.drawable.my_music_artist_icon, R.drawable.my_music_album_icon, 
			R.drawable.my_music_songs_icon };

	@Override
	public void setFilter(String filterStr) {
		// not needed
		return;
	}

}

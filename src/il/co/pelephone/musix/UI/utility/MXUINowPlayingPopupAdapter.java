package il.co.pelephone.musix.UI.utility;

import java.util.ArrayList;

import il.co.pelephone.musix.UI.R;
import il.co.pelephone.musix.data.Song;
import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MXUINowPlayingPopupAdapter extends BaseAdapter {

	private ArrayList<Song> songs;
	private Song currentSong;
	private static MXUINowPlayingPopupAdapter _instance;
	Context ctx;
	private MXUINowPlayingPopupAdapter(Context context, ArrayList<Song> songs, Song currentSong) {
		ctx = context;
		this.currentSong = currentSong;
		this.songs = songs;
	}

	@Override
	public int getCount() {
		return songs.size();//0;
	}

	@Override
	public Object getItem(int position) {
		return songs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return songs.get(position).ID;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		View v;
		if(convertView == null){
			LayoutInflater inflater = LayoutInflater.from(ctx);
			v = inflater.inflate(R.layout.rowplayerpopupplaylist, null);
		}
		else
			v = convertView;
		
		((TextView)v.findViewById(R.id.txtPlayerPlaylistSongDuration)).setText(convertToTime(songs.get(position).duration));
		((TextView)v.findViewById(R.id.txtPlayerPlaylistSongName)).setText(songs.get(position).name);
		((TextView)v.findViewById(R.id.txtPlayerPopupPlaylistNumber)).setText(""+(position+1));
		if(songs.get(position).catalogMediaID.equals(currentSong.catalogMediaID)){
			((ImageView)v.findViewById(R.id.imgPlayerPopupPlaylistNowPlaying)).setVisibility(View.VISIBLE);
			v.setSelected(true);
		}
		else{
			((ImageView)v.findViewById(R.id.imgPlayerPopupPlaylistNowPlaying)).setVisibility(View.GONE);
			v.setSelected(false);
		}
		return v;
	}
	
	public void setCurrentSong(Song s){
		this.currentSong = s;
	}
	
	public void setCurrentSong(String songID){
		for (Song s : songs) {
			if(s.catalogMediaID.equalsIgnoreCase(songID))
				this.currentSong = s;
		}
	}
	
	public void setPlaylistSongs(ArrayList<Song> songs){
		this.songs = songs;
	}

	public static MXUINowPlayingPopupAdapter Instance(Context context, ArrayList<Song> songs, Song currentSong){
		if (_instance == null)
			_instance = new MXUINowPlayingPopupAdapter(context, songs, currentSong);
		
		return _instance;
	}
	
	private String convertToTime(int duration){
    	int minutes = (int)(duration / 60);
    	int seconds = duration - minutes * 60;
    	String response = ""+minutes+":"+seconds;
    	return response;
    }
	
}

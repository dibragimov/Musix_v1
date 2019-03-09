package il.co.pelephone.musix.data;

import android.graphics.drawable.Drawable;

public class Playlist {
	public int ID;
	public String name;
	public String userPlaylistID;
	public String catalogPlaylistID;
	public int SongCount;
	private Drawable imageDrawable;
	
	public Drawable getImageDrawable() {
		return imageDrawable;
	}

	public void setImageDrawable(Drawable imageDrawable) {
		this.imageDrawable = imageDrawable;
	}

	public Playlist(int iD, String name, String userPlaylistID, String catalogPlaylistID,
			int songCount) {
		super();
		ID = iD;
		this.name = name;
		this.userPlaylistID = userPlaylistID;
		this.catalogPlaylistID = catalogPlaylistID;
		SongCount = songCount;
	}
	
	////specifically for UI (Music Catalog)
	
	public Playlist(String catalogPlaylistID, String displayName){
		this.catalogPlaylistID = catalogPlaylistID;
		this.name = displayName;
	}
	
	////end of specifically for UI (Music Catalog)
}

package il.co.pelephone.musix.data;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class Album {
	public int ID;
	public String catalogAlbumID;
	public String Name;
	public String ArtistName;
	public String GenreName;
	public int SongCount;
	private Drawable imageDrawable;
	
	public Album(int iD, String catalogAlbumID, String name, String artistName,
			String genreName, int songCount) {
		super();
		ID = iD;
		this.catalogAlbumID = catalogAlbumID;
		Name = name;
		ArtistName = artistName;
		GenreName = genreName;
		SongCount = songCount;
	}
	
	////specifically for UI (Music Catalog)
	
	public Album(String catalogAlbumID, String displayName){
		this.catalogAlbumID = catalogAlbumID;
		this.Name = displayName;
	}
	
	////end of specifically for UI (Music Catalog)
	
	public Drawable getImageDrawable() {
		return imageDrawable;
	}

	public void setImageDrawable(Drawable imageDrawable) {
		this.imageDrawable = imageDrawable;
//		Log.d("Album", "image set for Album");
	}
}


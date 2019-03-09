package il.co.pelephone.musix.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable{
	public int ID;
	public String catalogMediaID;
	public String userMediaID;
	public String AlbumName;
	public String ArtistName;
	public String GenreName;
	public int duration;
	public String name;
	public Song(int iD, String catalogMediaID, String userMediaID,
			String albumName, String artistName, String genreName, int duration) {
		super();
		ID = iD;
		this.catalogMediaID = catalogMediaID;
		this.userMediaID = userMediaID;
		AlbumName = albumName;
		ArtistName = artistName;
		GenreName = genreName;
		this.duration = duration;
	}
	
	public Song(int iD, String catalogMediaID, String userMediaID,
			String albumName, String artistName, String genreName, int duration, String name) {
		super();
		ID = iD;
		this.catalogMediaID = catalogMediaID;
		this.userMediaID = userMediaID;
		AlbumName = albumName;
		ArtistName = artistName;
		GenreName = genreName;
		this.duration = duration;
		this.name = name;
	}
	
	////specifically for UI (Music Catalog)
	
	public Song(String catalogMediaID, String albumName, String artistName, 
			String genreName, int duration, String name){
		this.catalogMediaID = catalogMediaID;
		AlbumName = albumName;
		ArtistName = artistName;
		GenreName = genreName;
		this.duration = duration;
		this.name = name;
	}
	
	////end of specifically for UI (Music Catalog)

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(ID);
		dest.writeString(catalogMediaID);
		dest.writeString(userMediaID);
		dest.writeString(AlbumName);
		dest.writeString(ArtistName);
		dest.writeString(GenreName);
		dest.writeInt(duration);
		dest.writeString(name);
	}
	
	public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
		public Song createFromParcel(Parcel in) {
			return new Song(in);
			}
		
		public Song[] newArray(int size) {
			return new Song[size];
			}
	};
		
	private Song(Parcel in) {
		ID = in.readInt();
		catalogMediaID = in.readString();
		userMediaID = in.readString();
		AlbumName = in.readString();
		ArtistName = in.readString();
		GenreName = in.readString();
		duration = in.readInt();
		name = in.readString();
	}
}

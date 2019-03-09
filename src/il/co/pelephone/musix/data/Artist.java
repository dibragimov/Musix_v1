package il.co.pelephone.musix.data;

public class Artist {
	public int ID;
	public String catalogArtistID;
	public String GenreName;
	public int GenreID;
	public int AlbumCount;
	public int SongCount;
	public String catalogArtistName;
	public Artist(int iD, String catalogArtistID, String genreName,
			int genreID, int albumCount, int songCount) {
		super();
		ID = iD;
		this.catalogArtistID = catalogArtistID;
		GenreName = genreName;
		GenreID = genreID;
		AlbumCount = albumCount;
		SongCount = songCount;
	}
	
	public Artist(int iD, String catalogArtistID, String genreName,
			int genreID, int albumCount, int songCount, String name) {
		super();
		ID = iD;
		this.catalogArtistID = catalogArtistID;
		GenreName = genreName;
		GenreID = genreID;
		AlbumCount = albumCount;
		SongCount = songCount;
		this.catalogArtistName = name;
	}
	
	////specifically for UI (Music Catalog)
	
	public String DisplayName; 
	public Artist(String catalogArtistID, String displayName){
		this.catalogArtistID = catalogArtistID;
		this.DisplayName = displayName;
	}
	
	////end of specifically for UI (Music Catalog)
}

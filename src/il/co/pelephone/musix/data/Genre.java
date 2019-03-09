package il.co.pelephone.musix.data;

public class Genre {
	public int ID;
	public String catalogGenreID;
	public int ArtistCount;
	public int SongCount;
	public String name;
	public Genre(int iD, String catalogGenreID, int artistCount, int songCount,
			String name) {
		super();
		ID = iD;
		this.catalogGenreID = catalogGenreID;
		ArtistCount = artistCount;
		SongCount = songCount;
		this.name = name;
	}
	
	////specifically for UI (Music Catalog)
	
	public Genre(String catalogGenreID, String displayName){
		this.catalogGenreID = catalogGenreID;
		this.name = displayName;
	}
	
	////end of specifically for UI (Music Catalog)
}

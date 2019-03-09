package il.co.pelephone.musix.data;

import android.graphics.drawable.Drawable;

public class MusicEntity {

	private String id;
	private String name;
	private EntityType type;
	private String artistName;
	private Drawable imageDrawable; 
	private String albumName;
	private String length;
	private String genreName;
	private boolean isSelected;
	private String albumId;

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public MusicEntity(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	public MusicEntity(String id, String name, EntityType type) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public EntityType getType() {
		return type;
	}

	public void setType(EntityType type) {
		this.type = type;
	}

	public Drawable getImageDrawable() {
		return imageDrawable;
	}

	public void setImageDrawable(Drawable imageDrawable) {
		this.imageDrawable = imageDrawable;
	}
	
	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getGenreName() {
		return genreName;
	}

	public void setGenreName(String genreName) {
		this.genreName = genreName;
	}


	public MusicEntity(String id, String name,
			String artistName, String albumName, String length, String genreName, EntityType type) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.artistName = artistName;
		this.albumName = albumName;
		this.length = length;
		this.genreName = genreName;
	}


	public enum EntityType{
		Song,
		Album,
		Artist,
		Playlist,
		Genre
	}
}

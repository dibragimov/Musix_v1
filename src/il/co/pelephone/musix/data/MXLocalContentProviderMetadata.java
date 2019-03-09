package il.co.pelephone.musix.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MXLocalContentProviderMetadata {
	public static final String AUTHORITY = "il.co.pelephone.musix.local";
	public static final String DATABASE_NAME = "musix.db";
	public static final int DATABASE_VERSION = 1;
	public static final String SONGS_TABLE_NAME = "tblSongs";
	public static final String ARTISTS_TABLE_NAME = "tblArtists";
	public static final String ALBUMS_TABLE_NAME = "tblAlbums";
	public static final String GENRES_TABLE_NAME = "tblGenres";
	public static final String PLAYLISTS_TABLE_NAME = "tblPlaylists";
	public static final String PLAYLISTSONGS_TABLE_NAME = "tblPlaylistsSongs";
	public static final String ALBUMSSONGS_TABLE_NAME = "tblAlbumsSongs";
	
	private MXLocalContentProviderMetadata() {}
	
	public static final class SongsTableMetaData implements BaseColumns
	{
	    private SongsTableMetaData() {}
		public static final String TABLE_NAME = SONGS_TABLE_NAME;
		//uri and MIME type definitions
		public static final Uri CONTENT_URI =
		Uri.parse("content://" + AUTHORITY + "/songs");
		public static final Uri CONTENT_URI_CATALOGMEDIA =
			Uri.parse("content://" + AUTHORITY + "/songs/catalogmedia");
		public static final String CONTENT_TYPE =
		"vnd.il.co.pelephone.musix.cursor.dir/vnd.il.co.pelephone.musix.song";
		public static final String CONTENT_ITEM_TYPE =
		"vnd.il.co.pelephone.musix.cursor.item/vnd.il.co.pelephone.musix.song";
		
		//Additional Columns start here.
		//string type
		public static final String SONG_NAME = "name";
		//real type  - should be converted to long
		public static final String SONG_ADDED_TIMESTAMP = "addedTimestamp";
		//int type
		public static final String SONG_ALBUMID = "albumID";
		//int
		public static final String SONG_ARTISTID = "artistID";
		//string
		public static final String SONG_CATALOGMEDIAID = "catalogMediaID";
		//string
		public static final String SONG_USERMEDIAID = "userMediaID";
		//int
		public static final String SONG_DURATION = "duration";
		//int
		public static final String SONG_PLAYCOUNT = "playCount";
		//int
		public static final String SONG_GENREID = "genreID";
		
		public static final String DEFAULT_SORT_ORDER = TABLE_NAME+"."+SONG_NAME + " ASC";
		public static final String TIMESTAMP_SORT_ORDER = SONG_ADDED_TIMESTAMP + " DESC";
	}
	
	public static final class ArtistsTableMetaData implements BaseColumns
	{
	    private ArtistsTableMetaData() {}
		public static final String TABLE_NAME = ARTISTS_TABLE_NAME;
		//uri and MIME type definitions
		public static final Uri CONTENT_URI =
		Uri.parse("content://" + AUTHORITY + "/artists");
		public static final String CONTENT_TYPE =
		"vnd.il.co.pelephone.musix.cursor.dir/vnd.il.co.pelephone.musix.artist";
		public static final String CONTENT_ITEM_TYPE =
		"vnd.il.co.pelephone.musix.cursor.item/vnd.il.co.pelephone.musix.artist";
		
		//Additional Columns start here.
		//string type
		public static final String ARTIST_CATALOGARTISTID = "catalogArtistID";
		//int type
		public static final String ARTIST_GENREID = "genreID";
		public static final String ARTIST_NAME = "catalogArtistName";
		
		public static final String DEFAULT_SORT_ORDER = ARTIST_CATALOGARTISTID + " ASC";
	}
	
	public static final class ArtistsForGenreTableMetaData implements BaseColumns
	{
	    private ArtistsForGenreTableMetaData() {}
//		public static final String TABLE_NAME = ARTISTS_TABLE_NAME;
		//uri and MIME type definitions
		public static final Uri CONTENT_URI =
		Uri.parse("content://" + AUTHORITY + "/artistsforgenre");
		public static final String CONTENT_TYPE =
		"vnd.il.co.pelephone.musix.cursor.dir/vnd.il.co.pelephone.musix.artist";
		public static final String CONTENT_ITEM_TYPE =
		"vnd.il.co.pelephone.musix.cursor.item/vnd.il.co.pelephone.musix.artist";
	}
	
	public static final class AlbumsTableMetaData implements BaseColumns
	{
	    private AlbumsTableMetaData() {}
		public static final String TABLE_NAME = ALBUMS_TABLE_NAME;
		//uri and MIME type definitions
		public static final Uri CONTENT_URI =
		Uri.parse("content://" + AUTHORITY + "/albums");
		public static final String CONTENT_TYPE =
		"vnd.il.co.pelephone.musix.cursor.dir/vnd.il.co.pelephone.musix.album";
		public static final String CONTENT_ITEM_TYPE =
		"vnd.il.co.pelephone.musix.cursor.item/vnd.il.co.pelephone.musix.album";
		
		//Additional Columns start here.
		//string type
		public static final String ALBUM_ALBUMNAME = "albumName";
		//int type
		public static final String ALBUM_ARTISTID = "artistID";
		//int type
		public static final String ALBUM_GENREID = "genreID";
		//string type
		public static final String ALBUM_CATALOGALBUMID = "catalogAlbumID";
		
		public static final String DEFAULT_SORT_ORDER = ALBUM_ALBUMNAME+" ASC";
		public static final String ARTIST_SORT_ORDER = "tblArtists.catalogArtistID ASC";
	}
	
	public static final class AlbumsForArtistTableMetaData implements BaseColumns
	{
	    private AlbumsForArtistTableMetaData() {}
//		public static final String TABLE_NAME = ARTISTS_TABLE_NAME;
		//uri and MIME type definitions
		public static final Uri CONTENT_URI =
		Uri.parse("content://" + AUTHORITY + "/albumsforartist");
		public static final String CONTENT_TYPE =
		"vnd.il.co.pelephone.musix.cursor.dir/vnd.il.co.pelephone.musix.album";
		public static final String CONTENT_ITEM_TYPE =
		"vnd.il.co.pelephone.musix.cursor.item/vnd.il.co.pelephone.musix.album";
	}
	
	public static final class GenresTableMetaData implements BaseColumns
	{
	    private GenresTableMetaData() {}
		public static final String TABLE_NAME = GENRES_TABLE_NAME;
		//uri and MIME type definitions
		public static final Uri CONTENT_URI =
		Uri.parse("content://" + AUTHORITY + "/genres");
		public static final String CONTENT_TYPE =
		"vnd.il.co.pelephone.musix.cursor.dir/vnd.il.co.pelephone.musix.genre";
		public static final String CONTENT_ITEM_TYPE =
		"vnd.il.co.pelephone.musix.cursor.item/vnd.il.co.pelephone.musix.genre";
		public static final String DEFAULT_SORT_ORDER = "name ASC";
		//Additional Columns start here.
		//string type
		public static final String GENRE_CATALOGGENREID = "catalogGenreID";
		//int type
		public static final String GENRE_NAME = "name";
		//int type
		public static final String GENRE_ORDINAL = "ordinal";
	}
	
	public static final class PlaylistTableMetaData implements BaseColumns
	{
	    private PlaylistTableMetaData() {}
		public static final String TABLE_NAME = PLAYLISTS_TABLE_NAME;
		//uri and MIME type definitions
		public static final Uri CONTENT_URI =
		Uri.parse("content://" + AUTHORITY + "/playlists");
		public static final Uri CONTENT_URI_WITH_USERID =
		Uri.parse("content://" + AUTHORITY + "/playlists/userplaylist");
		public static final Uri CONTENT_URI_WITH_CATALOGID =
		Uri.parse("content://" + AUTHORITY + "/playlists/catalogplaylist");
		public static final String CONTENT_TYPE =
		"vnd.il.co.pelephone.musix.cursor.dir/vnd.il.co.pelephone.musix.playlist";
		public static final String CONTENT_ITEM_TYPE =
		"vnd.il.co.pelephone.musix.cursor.item/vnd.il.co.pelephone.musix.playlist";
		
		//Additional Columns start here.
		//string type
		public static final String PLAYLIST_CATALOGPLAYLISTID = "catalogPlaylistID";
		//string type
		public static final String PLAYLIST_USERPLAYLISTID = "userPlaylistID";
		//int type
		public static final String PLAYLIST_NAME = "name";
		
		public static final String DEFAULT_SORT_ORDER = PLAYLIST_NAME + " ASC";
		public static final String CATALOG_SORT_ORDER = "IFNULL(" + PLAYLIST_CATALOGPLAYLISTID + ", 'zzzzzzzzz') ASC";
	}
	
	// the query includes more columns than specified here
	public static final class AlbumSongsTableMetaData implements BaseColumns{
		private AlbumSongsTableMetaData() {}
		public static final String TABLE_NAME = ALBUMSSONGS_TABLE_NAME;
		//uri and MIME type definitions
		public static final Uri CONTENT_URI =
		Uri.parse("content://" + AUTHORITY + "/albumsongs");
		public static final String CONTENT_TYPE =
		"vnd.il.co.pelephone.musix.cursor.dir/vnd.il.co.pelephone.musix.albumsong";
		public static final String CONTENT_ITEM_TYPE =
		"vnd.il.co.pelephone.musix.cursor.item/vnd.il.co.pelephone.musix.albumsong";
		
		//Additional Columns start here.
		//int type
		public static final String ALBUMSONG_TRACKNUMBER = "trackNumber";
		//int type
		public static final String ALBUMSONG_SONGID = "songID";
		//int type
		public static final String ALBUMSONG_ALBUMID = "albumID";
		
		public static final String DEFAULT_SORT_ORDER = ALBUMSONG_TRACKNUMBER + " ASC";
	}
	
	// the query includes more columns than specified here 
	public static final class PlaylistSongsTableMetaData implements BaseColumns{
		private PlaylistSongsTableMetaData() {}
		public static final String TABLE_NAME = PLAYLISTSONGS_TABLE_NAME;
		//uri and MIME type definitions
		public static final Uri CONTENT_URI =
		Uri.parse("content://" + AUTHORITY + "/playlistsongs");
		public static final String CONTENT_TYPE =
		"vnd.il.co.pelephone.musix.cursor.dir/vnd.il.co.pelephone.musix.playlistsong";
		public static final String CONTENT_ITEM_TYPE =
		"vnd.il.co.pelephone.musix.cursor.item/vnd.il.co.pelephone.musix.playlistsong";
		
		//Additional Columns start here.
		//int type
		public static final String PLAYLISTSONG_TRACKNUMBER = "trackNumber";
		//int type
		public static final String PLAYLISTSONG_SONGID = "songID";
		//int type
		public static final String PLAYLISTSONG_PLAYLISTID = "playlistID";
		
		public static final String DEFAULT_SORT_ORDER = PLAYLISTSONG_TRACKNUMBER + " ASC";
	}
	
}

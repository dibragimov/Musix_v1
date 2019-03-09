package il.co.pelephone.musix.data;

import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.AlbumSongsTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.AlbumsTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.ArtistsTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.GenresTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.PlaylistSongsTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.PlaylistTableMetaData;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata.SongsTableMetaData;

import java.io.IOException;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.text.Html.TagHandler;
import android.util.Log;

public class MusixLocalContentProvider extends ContentProvider {

	//Create a Projection Map for Columns
	//Projection maps are similar to "as" construct in an sql
	//statement whereby you can rename the
	//columns.
	private static String ORDERBY_STRING = " order by ";
	private static HashMap<String, String> sMusixSongsProjectionMap;
	static
	{
	sMusixSongsProjectionMap = new HashMap<String, String>();
	
	sMusixSongsProjectionMap.put(SongsTableMetaData._ID, SongsTableMetaData._ID);
	sMusixSongsProjectionMap.put(SongsTableMetaData.SONG_ADDED_TIMESTAMP
			, SongsTableMetaData.SONG_ADDED_TIMESTAMP);
	sMusixSongsProjectionMap.put(SongsTableMetaData.SONG_ALBUMID
			, SongsTableMetaData.SONG_ALBUMID);
	sMusixSongsProjectionMap.put(SongsTableMetaData.SONG_ARTISTID
			, SongsTableMetaData.SONG_ARTISTID);
	sMusixSongsProjectionMap.put(SongsTableMetaData.SONG_CATALOGMEDIAID
			, SongsTableMetaData.SONG_CATALOGMEDIAID);
	sMusixSongsProjectionMap.put(SongsTableMetaData.SONG_DURATION
			, SongsTableMetaData.SONG_DURATION);
	sMusixSongsProjectionMap.put(SongsTableMetaData.SONG_GENREID
			, SongsTableMetaData.SONG_GENREID);
	sMusixSongsProjectionMap.put(SongsTableMetaData.SONG_NAME
			, SongsTableMetaData.SONG_NAME);
	sMusixSongsProjectionMap.put(SongsTableMetaData.SONG_PLAYCOUNT
			, SongsTableMetaData.SONG_PLAYCOUNT);
	sMusixSongsProjectionMap.put(SongsTableMetaData.SONG_USERMEDIAID
			, SongsTableMetaData.SONG_USERMEDIAID);
	}
	
	private static HashMap<String, String> sMusixGenresProjectionMap;
	static{
		sMusixGenresProjectionMap = new HashMap<String, String>();
		
		sMusixGenresProjectionMap.put(GenresTableMetaData._ID, GenresTableMetaData._ID);
		sMusixGenresProjectionMap.put(GenresTableMetaData.GENRE_NAME, GenresTableMetaData.GENRE_NAME);
		sMusixGenresProjectionMap.put(GenresTableMetaData.GENRE_CATALOGGENREID, GenresTableMetaData.GENRE_CATALOGGENREID);
	}
	
	private static HashMap<String, String> sMusixArtistsProjectionMap;
	static{
		sMusixArtistsProjectionMap = new HashMap<String, String>();
		
		sMusixArtistsProjectionMap.put(ArtistsTableMetaData._ID, ArtistsTableMetaData._ID);
		sMusixArtistsProjectionMap.put(ArtistsTableMetaData.ARTIST_CATALOGARTISTID, ArtistsTableMetaData.ARTIST_CATALOGARTISTID);
		sMusixArtistsProjectionMap.put(ArtistsTableMetaData.ARTIST_GENREID, ArtistsTableMetaData.ARTIST_GENREID);
	}
	
	private static HashMap<String, String> sMusixAlbumsProjectionMap;
	static{
		sMusixAlbumsProjectionMap = new HashMap<String, String>();
		
		sMusixAlbumsProjectionMap.put(AlbumsTableMetaData._ID, ArtistsTableMetaData._ID);
		sMusixAlbumsProjectionMap.put(AlbumsTableMetaData.ALBUM_ALBUMNAME, AlbumsTableMetaData.ALBUM_ALBUMNAME);
		sMusixAlbumsProjectionMap.put(AlbumsTableMetaData.ALBUM_GENREID, AlbumsTableMetaData.ALBUM_GENREID);
		sMusixAlbumsProjectionMap.put(AlbumsTableMetaData.ALBUM_ARTISTID, AlbumsTableMetaData.ALBUM_ARTISTID);
		sMusixAlbumsProjectionMap.put(AlbumsTableMetaData.ALBUM_CATALOGALBUMID, AlbumsTableMetaData.ALBUM_CATALOGALBUMID);
	}
	
	
	private static HashMap<String, String> sMusixAlbumSongsProjectionMap;
	static{
		sMusixAlbumsProjectionMap = new HashMap<String, String>();
		
		sMusixAlbumsProjectionMap.put(AlbumSongsTableMetaData._ID, AlbumSongsTableMetaData._ID);
		sMusixAlbumsProjectionMap.put(AlbumSongsTableMetaData.ALBUMSONG_ALBUMID, AlbumSongsTableMetaData.ALBUMSONG_ALBUMID);
		sMusixAlbumsProjectionMap.put(AlbumSongsTableMetaData.ALBUMSONG_SONGID, AlbumSongsTableMetaData.ALBUMSONG_SONGID);
		sMusixAlbumsProjectionMap.put(AlbumSongsTableMetaData.ALBUMSONG_TRACKNUMBER, AlbumSongsTableMetaData.ALBUMSONG_TRACKNUMBER);
	}
	
	
	private static HashMap<String, String> sMusixPlaylistsProjectionMap;
	static{
		sMusixPlaylistsProjectionMap = new HashMap<String, String>();
		
		sMusixPlaylistsProjectionMap.put(PlaylistTableMetaData._ID, ArtistsTableMetaData._ID);
		sMusixPlaylistsProjectionMap.put(PlaylistTableMetaData.PLAYLIST_NAME, PlaylistTableMetaData.PLAYLIST_NAME);
		sMusixPlaylistsProjectionMap.put(PlaylistTableMetaData.PLAYLIST_USERPLAYLISTID, PlaylistTableMetaData.PLAYLIST_USERPLAYLISTID);
		sMusixPlaylistsProjectionMap.put(PlaylistTableMetaData.PLAYLIST_CATALOGPLAYLISTID, PlaylistTableMetaData.PLAYLIST_CATALOGPLAYLISTID);
	}
	
	private static HashMap<String, String> sMusixPlaylistSongsProjectionMap;
	static{
		sMusixPlaylistSongsProjectionMap = new HashMap<String, String>();
		
		sMusixPlaylistSongsProjectionMap.put(PlaylistSongsTableMetaData._ID, ArtistsTableMetaData._ID);
		sMusixPlaylistSongsProjectionMap.put(PlaylistSongsTableMetaData.PLAYLISTSONG_PLAYLISTID, PlaylistSongsTableMetaData.PLAYLISTSONG_PLAYLISTID);
		sMusixPlaylistSongsProjectionMap.put(PlaylistSongsTableMetaData.PLAYLISTSONG_SONGID, PlaylistSongsTableMetaData.PLAYLISTSONG_SONGID);
		sMusixPlaylistSongsProjectionMap.put(PlaylistSongsTableMetaData.PLAYLISTSONG_TRACKNUMBER, PlaylistSongsTableMetaData.PLAYLISTSONG_TRACKNUMBER);
	}
	
	//Provide a mechanism to identify all the incoming uri patterns.
	private static final UriMatcher sUriMatcher;
	private static final int SONGS_COLLECTION_URI_INDICATOR = 1;
	private static final int SINGLE_SONG_URI_INDICATOR = 2;
	private static final int SINGLE_SONG_BYUSERMEDIA_URI_INDICATOR = 3;
	private static final int SINGLE_SONG_BYCATALOGMEDIA_URI_INDICATOR = 4;
	
	private static final int GENRES_COLLECTION_URI_INDICATOR = 11;
	private static final int SINGLE_GENRE_URI_INDICATOR = 12;
	
	private static final int  ARTISTS_COLLECTION_URI_INDICATOR = 21;
	private static final int  SINGLE_ARTIST_URI_INDICATOR = 22;
	
	private static final int  ALBUMS_COLLECTION_URI_INDICATOR = 31;
	private static final int  SINGLE_ALBUM_URI_INDICATOR = 32;
	
	private static final int  PLAYLISTS_COLLECTION_URI_INDICATOR = 41;
	private static final int  SINGLE_PLAYLIST_URI_INDICATOR = 42;
	private static final int  SINGLE_PLAYLIST_BYCATALOGPLAYLISTID_URI_INDICATOR = 43;
	private static final int  SINGLE_PLAYLIST_BYUSERPLAYLISTID_URI_INDICATOR = 44;
	
	private static final int  PLAYLISTSONGS_COLLECTION_URI_INDICATOR = 51;
	private static final int  SINGLE_PLAYLISTSONG_URI_INDICATOR = 52;
	
	private static final int  ALBUMSONGS_COLLECTION_URI_INDICATOR = 61;
	private static final int  SINGLE_ALBUMSONG_URI_INDICATOR = 62;
	
	private static final int  ARTISTSFORGENRE_URI_INDICATOR = 71;
	private static final int  ARTISTSFORGENRE_URI_INDICATOR_CATALOG = 72;
	
	private static final int  ALBUMSFORARTIST_URI_INDICATOR = 81;
	
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "songs", SONGS_COLLECTION_URI_INDICATOR);
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "songs/#",	SINGLE_SONG_URI_INDICATOR);
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "songs/usermedia/*",	SINGLE_SONG_BYUSERMEDIA_URI_INDICATOR);
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "songs/catalogmedia/*",	SINGLE_SONG_BYCATALOGMEDIA_URI_INDICATOR);
		
		
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "genres", GENRES_COLLECTION_URI_INDICATOR);
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "genres/#",	SINGLE_GENRE_URI_INDICATOR);
		
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "artists", ARTISTS_COLLECTION_URI_INDICATOR);
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "artists/#",	SINGLE_ARTIST_URI_INDICATOR);
		
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "albums", ALBUMS_COLLECTION_URI_INDICATOR);
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "albums/#",	SINGLE_ALBUM_URI_INDICATOR);
		
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "playlists", PLAYLISTS_COLLECTION_URI_INDICATOR);
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "playlists/#",	SINGLE_PLAYLIST_URI_INDICATOR);
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "playlists/catalogplaylist/*",	SINGLE_PLAYLIST_BYCATALOGPLAYLISTID_URI_INDICATOR);
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "playlists/userplaylist/*",	SINGLE_PLAYLIST_BYUSERPLAYLISTID_URI_INDICATOR);
		
		
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "playlistsongs", PLAYLISTSONGS_COLLECTION_URI_INDICATOR);
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "playlistsongs/#",	SINGLE_PLAYLISTSONG_URI_INDICATOR);
		
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "albumsongs", ALBUMSONGS_COLLECTION_URI_INDICATOR);
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "albumsongs/#",	SINGLE_ALBUMSONG_URI_INDICATOR);
		
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "artistsforgenre/#",	ARTISTSFORGENRE_URI_INDICATOR);
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "artistsforgenre/cataloggenre/*",	ARTISTSFORGENRE_URI_INDICATOR_CATALOG);
		
		sUriMatcher.addURI(MXLocalContentProviderMetadata.AUTHORITY, "albumsforartist/#",	ALBUMSFORARTIST_URI_INDICATOR);
	}
	// Deal with OnCreate call back
	private MXDatabaseHelper mOpenHelper;
	
	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
			case GENRES_COLLECTION_URI_INDICATOR:
				return GenresTableMetaData.CONTENT_TYPE;
			case SINGLE_GENRE_URI_INDICATOR:
				return GenresTableMetaData.CONTENT_ITEM_TYPE;
			case SONGS_COLLECTION_URI_INDICATOR:
				return SongsTableMetaData.CONTENT_TYPE;
			case SINGLE_SONG_BYUSERMEDIA_URI_INDICATOR:
				return SongsTableMetaData.CONTENT_ITEM_TYPE;
			case SINGLE_SONG_BYCATALOGMEDIA_URI_INDICATOR:
				return SongsTableMetaData.CONTENT_ITEM_TYPE;
			case SINGLE_SONG_URI_INDICATOR:
				return SongsTableMetaData.CONTENT_ITEM_TYPE;
			case ALBUMS_COLLECTION_URI_INDICATOR:
				return AlbumsTableMetaData.CONTENT_TYPE;
			case SINGLE_ALBUM_URI_INDICATOR:
				return AlbumsTableMetaData.CONTENT_ITEM_TYPE;
			case ARTISTS_COLLECTION_URI_INDICATOR:
				return ArtistsTableMetaData.CONTENT_TYPE;
			case SINGLE_ARTIST_URI_INDICATOR:
				return ArtistsTableMetaData.CONTENT_ITEM_TYPE;
			case PLAYLISTS_COLLECTION_URI_INDICATOR:
				return PlaylistTableMetaData.CONTENT_TYPE;
			case SINGLE_PLAYLIST_URI_INDICATOR:
				return PlaylistTableMetaData.CONTENT_ITEM_TYPE;
			case ALBUMSONGS_COLLECTION_URI_INDICATOR:
				return AlbumSongsTableMetaData.CONTENT_TYPE;
			case SINGLE_ALBUMSONG_URI_INDICATOR:
				return AlbumSongsTableMetaData.CONTENT_ITEM_TYPE;
			case PLAYLISTSONGS_COLLECTION_URI_INDICATOR:
				return PlaylistSongsTableMetaData.CONTENT_TYPE;
			case SINGLE_PLAYLISTSONG_URI_INDICATOR:
				return PlaylistSongsTableMetaData.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
	
	@Override
	public int delete(Uri uri, String whereClause, String[] whereArgs) {
		//SQLiteDatabase db = mOpenHelper.createDataBase();
		SQLiteDatabase db =	mOpenHelper.getWritableDatabase();

		int affectedRows = 0;
		switch (sUriMatcher.match(uri))
		{		
			case PLAYLISTS_COLLECTION_URI_INDICATOR:
				Cursor c = db.query(PlaylistTableMetaData.TABLE_NAME, new String[]{PlaylistTableMetaData._ID}, whereClause, whereArgs, null, null, null);
				if(c.getCount() > 0){
					c.moveToFirst();
					int ID = c.getInt(0);////id of the playlist exists
					Log.i("MusixLocalContentProvider", "Playlist #"+ID+" will be deleted");
					affectedRows = db.delete(PlaylistTableMetaData.TABLE_NAME, whereClause, whereArgs);
//					if(affectedRows > 0){
						int songs = db.delete(PlaylistSongsTableMetaData.TABLE_NAME, PlaylistSongsTableMetaData.PLAYLISTSONG_PLAYLISTID+"=?", new String[] {""+ID});
						Log.i("MusixLocalContentProvider", ""+songs+" songs were be deleted");
//					}
				}
				break;
			case PLAYLISTSONGS_COLLECTION_URI_INDICATOR:
				affectedRows = db.delete(PlaylistSongsTableMetaData.TABLE_NAME, whereClause, whereArgs);
				break;
			case SONGS_COLLECTION_URI_INDICATOR:
				Cursor songsC = db.query(SongsTableMetaData.TABLE_NAME, new String[]{SongsTableMetaData._ID, SongsTableMetaData.SONG_ALBUMID, SongsTableMetaData.SONG_ARTISTID, SongsTableMetaData.SONG_GENREID}, whereClause, whereArgs, null, null, null);
				if(songsC.getCount() > 0){
					songsC.moveToFirst();
					int ID = songsC.getInt(0);////id of the song exists
					int albumID = songsC.getInt(1);
					int artistID = songsC.getInt(2);
					int genreID = songsC.getInt(3);
					Log.i("MusixLocalContentProvider", ""+albumID+" "+artistID+" "+genreID);
					int playlistID = 0;
					
					Log.i("MusixLocalContentProvider", "Song #"+ID+" will be deleted");
					affectedRows = db.delete(SongsTableMetaData.TABLE_NAME, whereClause, whereArgs);
					
					int deletedAlbumSongs = db.delete(AlbumSongsTableMetaData.TABLE_NAME, AlbumSongsTableMetaData.ALBUMSONG_SONGID+"=?", new String[]{""+ID});
					
					Cursor curPl_ID = db.rawQuery("select playlistID from tblPlaylistsSongs where songID=?", new String[]{""+ID});
					if(curPl_ID.getCount() > 0){
						curPl_ID.moveToFirst();
						playlistID = curPl_ID.getInt(0);
					}
					curPl_ID.close();
					
					int deletedPlaylistSongs = db.delete(PlaylistSongsTableMetaData.TABLE_NAME, PlaylistSongsTableMetaData.PLAYLISTSONG_SONGID+"=?", new String[]{""+ID});
					Log.i("MusixLocalContentProvider", ""+deletedAlbumSongs+" songs were be deleted from AlbumSongs "+deletedPlaylistSongs+" songs were be deleted from PlaylistSongs");
					
					//// check if playlists, Albums, Artists, Genre empty
					//// if empty - delete entries
					//playlist
					Cursor curPl = db.rawQuery("select count(songID) as SongCount from tblPlaylistsSongs where playlistID=?", new String[]{""+playlistID});
					curPl.moveToFirst();
					int songsNumPl = curPl.getInt(0);
					Log.i("MusixLocalContentProvider", "SongsNumInPlaylist"+songsNumPl);
					if(songsNumPl ==0){
						db.delete(PlaylistTableMetaData.TABLE_NAME, PlaylistTableMetaData._ID+"=?", new String[]{""+playlistID});
						Log.i("MusixLocalContentProvider", "Deleted playlist");
					}
					//albums
					Cursor curAl = db.rawQuery("select count(songID) as SongCount from tblAlbumsSongs where albumID=?", new String[]{""+albumID});
					curAl.moveToFirst();
					int songsNumAl = curAl.getInt(0);
					Log.i("MusixLocalContentProvider", "SongsNumInAlbum"+songsNumAl);
					if(songsNumAl ==0){
						db.delete(AlbumsTableMetaData.TABLE_NAME, AlbumsTableMetaData._ID+"=?", new String[]{""+albumID});
						Log.i("MusixLocalContentProvider", "Deleted album");
					}
					//artists
					Cursor curAr = db.rawQuery("select count(_id) as SongCount from tblSongs where artistID=?", new String[]{""+artistID});
					curAr.moveToFirst();
					int songsNumAr = curAr.getInt(0);
					Log.i("MusixLocalContentProvider", "SongsNumForArtist"+songsNumAr);
					if(songsNumAr ==0){
						db.delete(ArtistsTableMetaData.TABLE_NAME, ArtistsTableMetaData._ID+"=?", new String[]{""+artistID});
						Log.i("MusixLocalContentProvider", "Deleted artist");
					}
					//genre
					Cursor curGen = db.rawQuery("select count(_id) as SongCount from tblSongs where genreID=?", new String[]{""+genreID});
					curGen.moveToFirst();
					int songsNumGen = curGen.getInt(0);
					Log.i("MusixLocalContentProvider", "SongsNumForGenre"+songsNumGen);
					if(songsNumGen ==0){
						db.delete(GenresTableMetaData.TABLE_NAME, GenresTableMetaData._ID+"=?", new String[]{""+genreID});
						Log.i("MusixLocalContentProvider", "Deleted genre");
					}
				}
				break;
			default:
				throw new IllegalArgumentException("Delete not supported. URI: "+uri.toString());
		}
		
		///close the DB
		db.close();
		
		return affectedRows;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.i("MusixLocalContentProvider", "insert uri="+uri);

		//Songs collection
		if(sUriMatcher.match(uri) == SONGS_COLLECTION_URI_INDICATOR) {			
			//validate input fields
			// Make sure that the fields are all set
//			if (values.containsKey(SongsTableMetaData.SONG_ADDED_TIMESTAMP) == false) {
//				throw new IllegalArgumentException("Failed to insert row because Added Timestamp is needed " + uri);
//			}
			if (values.containsKey(SongsTableMetaData.SONG_CATALOGMEDIAID) == false) {
				throw new IllegalArgumentException("Failed to insert row because Catalog Album ID is needed " + uri);
			}
		//	if (values.containsKey(SongsTableMetaData.SONG_USERMEDIAID) == false) {
			//	throw new IllegalArgumentException("Failed to insert row because User Media ID is needed " + uri);
			//}
			if (values.containsKey(ArtistsTableMetaData.ARTIST_CATALOGARTISTID) == false) {
				throw new IllegalArgumentException("Failed to insert row because Artist ID is needed " + uri);
			}
			if (values.containsKey(AlbumsTableMetaData.ALBUM_CATALOGALBUMID) == false) {
				throw new IllegalArgumentException("Failed to insert row because Catalog Media ID is needed " + uri);
			}
			if (values.containsKey(GenresTableMetaData.GENRE_CATALOGGENREID) == false) {
				throw new IllegalArgumentException("Failed to insert row because Genre ID is needed " + uri);
			}
			if (values.containsKey(SongsTableMetaData.SONG_DURATION) == false) {
				throw new IllegalArgumentException("Failed to insert row because Duration is needed " + uri);
			}
			if (values.containsKey(SongsTableMetaData.SONG_NAME) == false) {
				throw new IllegalArgumentException("Failed to insert row because Name is needed " + uri);
			}
			
			if (values.containsKey(AlbumSongsTableMetaData.ALBUMSONG_TRACKNUMBER) == false) {
				throw new IllegalArgumentException("Failed to insert row because TrackNumber is needed " + uri);
			}
			
			//SQLiteDatabase db = mOpenHelper.createDataBase();
			SQLiteDatabase db =	mOpenHelper.getWritableDatabase();

			
			Cursor artistIdCur = db.rawQuery("select _id from "+ArtistsTableMetaData.TABLE_NAME+" where "+ArtistsTableMetaData.ARTIST_CATALOGARTISTID+"=?", new String[]{values.get(ArtistsTableMetaData.ARTIST_CATALOGARTISTID).toString()});
			artistIdCur.moveToFirst();
			if(artistIdCur.getCount()>0){
				values.put(SongsTableMetaData.SONG_ARTISTID, artistIdCur.getInt(0));
				values.remove(ArtistsTableMetaData.ARTIST_CATALOGARTISTID);
				artistIdCur.close();
			}
			else{
				db.close();
				throw new IllegalArgumentException("Failed to insert row because Catalog Artist ID is not in a database " + uri);
			}
			
			Cursor albumIdCur = db.rawQuery("select _id from "+AlbumsTableMetaData.TABLE_NAME+" where "+AlbumsTableMetaData.ALBUM_CATALOGALBUMID+"=?", new String[]{values.get(AlbumsTableMetaData.ALBUM_CATALOGALBUMID).toString()});
			albumIdCur.moveToFirst();
			if(albumIdCur.getCount()>0){
				values.put(SongsTableMetaData.SONG_ALBUMID, albumIdCur.getInt(0));
				values.remove(AlbumsTableMetaData.ALBUM_CATALOGALBUMID);
				albumIdCur.close();
			}
			else{
				albumIdCur.close();
				db.close();
				throw new IllegalArgumentException("Failed to insert row because Catalog Album ID is not in a database " + uri);
			}
			
			Cursor genreIdCur = db.rawQuery("select _id from "+GenresTableMetaData.TABLE_NAME+" where "+GenresTableMetaData.GENRE_CATALOGGENREID+"=?", new String[]{values.get(GenresTableMetaData.GENRE_CATALOGGENREID).toString()});
			genreIdCur.moveToFirst();
			if(genreIdCur.getCount()>0){
				values.put(SongsTableMetaData.SONG_GENREID, genreIdCur.getInt(0));
				values.remove(GenresTableMetaData.GENRE_CATALOGGENREID);
				genreIdCur.close();
			}
			else{
				db.close();
				throw new IllegalArgumentException("Failed to insert row because Catalog Genre ID is not in a database " + uri);
			}
			
			ContentValues sa_values = new ContentValues();
			sa_values.put(AlbumSongsTableMetaData.ALBUMSONG_ALBUMID, values.getAsString(AlbumSongsTableMetaData.ALBUMSONG_ALBUMID));
			sa_values.put(AlbumSongsTableMetaData.ALBUMSONG_TRACKNUMBER, values.getAsString(AlbumSongsTableMetaData.ALBUMSONG_TRACKNUMBER));
			values.remove(AlbumSongsTableMetaData.ALBUMSONG_TRACKNUMBER);
			
			
			//table  INSERT INTO tblAlbumsSongs(artistID, duration, catalogMediaID, albumID, name, genreID) VALUES(?, ?, ?, ?, ?, ?);

			
			long rowId = db.insert(SongsTableMetaData.TABLE_NAME, SongsTableMetaData.SONG_PLAYCOUNT, values);
			if (rowId > 0) {
				Uri insertedSongUri = ContentUris.withAppendedId(SongsTableMetaData.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(insertedSongUri, null);

				sa_values.put(AlbumSongsTableMetaData.ALBUMSONG_SONGID,   rowId);

				if (db.insert(AlbumSongsTableMetaData.TABLE_NAME, AlbumSongsTableMetaData.ALBUMSONG_TRACKNUMBER, sa_values)>0)
				{
					db.close();	
					return insertedSongUri;
				}
			}
			db.close();
			throw new IllegalArgumentException("Failed to insert row into " + uri);
			// TODO: replace IllegalArgumentException with SQLException 
		}
		
		//// Albums collection
		else if(sUriMatcher.match(uri) == ALBUMS_COLLECTION_URI_INDICATOR) {
			//validate input fields
			// Make sure that the fields are all set
			if (values.containsKey(AlbumsTableMetaData.ALBUM_CATALOGALBUMID) == false) {
				throw new IllegalArgumentException("Failed to insert row because Catalog Album ID is needed " + uri);
			}
			if (values.containsKey(AlbumsTableMetaData.ALBUM_ALBUMNAME) == false) {
				throw new IllegalArgumentException("Failed to insert row because Artist ID is needed " + uri);
			}
			/*
			 *  Remove the genreID column from the actual table, remove the genreID when inserting a new album,
			 *  Ron Srebro, May 20, 2010
			 */
			/*if (values.containsKey(GenresTableMetaData.GENRE_CATALOGGENREID) == false) {
				throw new IllegalArgumentException("Failed to insert row because Genre ID is needed " + uri);
			}*/
			
			/*
			 * In albums the artistID should be optional
			 * Ron Srebro, May 20, 2010
			 */
			/*
			if (values.containsKey(ArtistsTableMetaData.ARTIST_CATALOGARTISTID) == false) {
				throw new IllegalArgumentException("Failed to insert row because Artist ID is needed " + uri);
			}*/
			
			//SQLiteDatabase db = mOpenHelper.createDataBase();
			SQLiteDatabase db =	mOpenHelper.getWritableDatabase();

			
			if (values.containsKey(ArtistsTableMetaData.ARTIST_CATALOGARTISTID) == true){
				String result = String.format("select _id from "+ArtistsTableMetaData.TABLE_NAME+" where "+ArtistsTableMetaData.ARTIST_CATALOGARTISTID+"='%s'", new String[]{values.get(ArtistsTableMetaData.ARTIST_CATALOGARTISTID).toString()});
				Log.i("MusixLocalContentProvider", "Album: ArtistID selection - "+result);
				Cursor artistIdCur = db.rawQuery("select _id from "+ArtistsTableMetaData.TABLE_NAME+" where "+ArtistsTableMetaData.ARTIST_CATALOGARTISTID+"=?", new String[]{values.get(ArtistsTableMetaData.ARTIST_CATALOGARTISTID).toString()});
				artistIdCur.moveToFirst();
				if(artistIdCur.getCount()>0){
					values.put(AlbumsTableMetaData.ALBUM_ARTISTID, artistIdCur.getInt(0));
					values.remove(ArtistsTableMetaData.ARTIST_CATALOGARTISTID);
					artistIdCur.close();
				}
				else{
					db.close();
					throw new IllegalArgumentException("Failed to insert row because Catalog Artist ID is not in a database " + uri);
				}
			}
			
			/*
			 * Currently the Albums table contains a genreID, however it seems I made a mistake 
			 * and there's no direct connection between an album and a genre. 
			 * The connection between them is done using the artist (Album as an artist, artist has a genre). 
			 * Remove the genreID column from the actual table, remove the genreID when inserting a new album, 
			 * when return a select query still return the genreID and name but do so by joining the Artists table 
			 * to the genres table, instead of directly joining Genres table to the Albums table.
			 * Ron Srebro, May 20, 2010
			 */
			/*
			String result1 = String.format("select _id from "+GenresTableMetaData.TABLE_NAME+" where "+GenresTableMetaData.GENRE_CATALOGGENREID+"='%s'", new String[]{values.get(GenresTableMetaData.GENRE_CATALOGGENREID).toString()});
			Log.i("MusixLocalContentProvider", "Album: GenreID selection - "+result1);
			Cursor genreIdCur = db.rawQuery("select _id from "+GenresTableMetaData.TABLE_NAME+" where "+GenresTableMetaData.GENRE_CATALOGGENREID+"=?", new String[]{values.get(GenresTableMetaData.GENRE_CATALOGGENREID).toString()});
			genreIdCur.moveToFirst();
			if(genreIdCur.getCount()>0){
				values.put(AlbumsTableMetaData.ALBUM_GENREID, genreIdCur.getInt(0));
				values.remove(GenresTableMetaData.GENRE_CATALOGGENREID);
				genreIdCur.close();
			}
			else{
				db.close();
				throw new IllegalArgumentException("Failed to insert row because Catalog Genre ID is not in a database " + uri);
			}*/
			
			long rowId = db.insert(AlbumsTableMetaData.TABLE_NAME, AlbumsTableMetaData.ALBUM_ARTISTID, values);
			if (rowId > 0) {
				Uri insertedAlbumUri = ContentUris.withAppendedId(AlbumsTableMetaData.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(insertedAlbumUri, null);
				db.close();
				return insertedAlbumUri;
			}
			db.close();
			throw new IllegalArgumentException("Failed to insert row into " + uri);
			// TODO: replace IllegalArgumentException with SQLException 
		}
		
		//Playlist
		else if(sUriMatcher.match(uri) == PLAYLISTS_COLLECTION_URI_INDICATOR) {			
			//validate input fields
			// Make sure that the fields are all set
			if (values.containsKey(PlaylistTableMetaData.PLAYLIST_CATALOGPLAYLISTID) == false && values.containsKey(PlaylistTableMetaData.PLAYLIST_USERPLAYLISTID) == false) {
				throw new IllegalArgumentException("Failed to insert row because Catalog Playlist ID or User Playlist ID is needed " + uri);
			}
			if (values.containsKey(PlaylistTableMetaData.PLAYLIST_NAME) == false) {
				throw new IllegalArgumentException("Failed to insert row because Playlist Name is needed " + uri);
			}
			
			//SQLiteDatabase db = mOpenHelper.createDataBase();
			SQLiteDatabase db =	mOpenHelper.getWritableDatabase();

			
			long rowId = db.insert(PlaylistTableMetaData.TABLE_NAME, PlaylistTableMetaData.PLAYLIST_NAME, values);
			if (rowId > 0) {
				Uri insertedPlaylistUri = ContentUris.withAppendedId(PlaylistTableMetaData.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(insertedPlaylistUri, null);
				db.close();
				return insertedPlaylistUri;
			}
			db.close();
			throw new IllegalArgumentException("Failed to insert row into " + uri);
			// TODO: replace IllegalArgumentException with SQLException 
		}
		
		////Genres collection
		else if (sUriMatcher.match(uri) == GENRES_COLLECTION_URI_INDICATOR) {
			
			//validate input fields
			// Make sure that the fields are all set
			if (values.containsKey(GenresTableMetaData.GENRE_NAME) == false) {
				throw new IllegalArgumentException("Failed to insert row because Genre Name is needed " + uri);
			}
			if (values.containsKey(GenresTableMetaData.GENRE_CATALOGGENREID) == false) {
				throw new IllegalArgumentException("Failed to insert row because Catalog Genre ID is needed " + uri);
			}
			
			//SQLiteDatabase db = mOpenHelper.createDataBase();
			SQLiteDatabase db =	mOpenHelper.getWritableDatabase();

			long rowId = db.insert(GenresTableMetaData.TABLE_NAME, GenresTableMetaData.GENRE_CATALOGGENREID, values);
			if (rowId > 0) {
				Uri insertedGenreUri = ContentUris.withAppendedId(GenresTableMetaData.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(insertedGenreUri, null);
				db.close();
				return insertedGenreUri;
			}
			db.close();
			throw new IllegalArgumentException("Failed to insert row into " + uri);
			// TODO: replace IllegalArgumentException with SQLException 
		}
		
		//// Artists
		else if (sUriMatcher.match(uri) == ARTISTS_COLLECTION_URI_INDICATOR) {
			
			//validate input fields
			// Make sure that the fields are all set
			if (values.containsKey(ArtistsTableMetaData.ARTIST_CATALOGARTISTID) == false) {
				throw new IllegalArgumentException("Failed to insert row because Catalog Artist ID is needed " + uri);
			}
			/* In artists the genereID should be optional on insert. And there should be an option to update records 
			 * in the table in order to update the genreID for an artist
			 * (Ron Srebro, May 20, 2010)
			 * */
			/*if (values.containsKey(GenresTableMetaData.GENRE_CATALOGGENREID) == false) {
				throw new IllegalArgumentException("Failed to insert row because Catalog Genre ID is needed " + uri);
			}*/
			
		//	SQLiteDatabase db = mOpenHelper.createDataBase();
			SQLiteDatabase db =	mOpenHelper.getWritableDatabase();

			if (values.containsKey(GenresTableMetaData.GENRE_CATALOGGENREID) == true){//// if catalogGenreID is present - find genreID (int)
				String result = String.format("select _id from "+GenresTableMetaData.TABLE_NAME+" where "+GenresTableMetaData.GENRE_CATALOGGENREID+"='%s'", new String[]{values.get(GenresTableMetaData.GENRE_CATALOGGENREID).toString()});
				Log.i("MusixLocalContentProvider", "GenreID selection: "+result);
				Cursor genreIdCur = db.rawQuery("select _id from "+GenresTableMetaData.TABLE_NAME+" where "+GenresTableMetaData.GENRE_CATALOGGENREID+"=?", new String[]{values.get(GenresTableMetaData.GENRE_CATALOGGENREID).toString()});
				genreIdCur.moveToFirst();
				if(genreIdCur.getCount()>0){
					values.put(ArtistsTableMetaData.ARTIST_GENREID, genreIdCur.getInt(0));
					values.remove(GenresTableMetaData.GENRE_CATALOGGENREID); //// this needs to be removed because such column does not exist
					genreIdCur.close();
				}
				else{
					db.close();
					throw new IllegalArgumentException("Failed to insert row because Catalog Genre ID is not in a database " + uri);
				}
			}
			
			long rowId = db.insert(ArtistsTableMetaData.TABLE_NAME, ArtistsTableMetaData.ARTIST_CATALOGARTISTID, values);
			if (rowId > 0) {
				Uri insertedArtistUri = ContentUris.withAppendedId(ArtistsTableMetaData.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(insertedArtistUri, null);
				db.close();
				return insertedArtistUri;
			}
			db.close();
			throw new IllegalArgumentException("Failed to insert row into " + uri);
			// TODO: replace IllegalArgumentException with SQLException 
		}
		
		//// playlist songs
		else if(sUriMatcher.match(uri) == PLAYLISTSONGS_COLLECTION_URI_INDICATOR) {			
			//validate input fields
			// Make sure that the fields are all set
			if (values.containsKey(PlaylistSongsTableMetaData.PLAYLISTSONG_PLAYLISTID) == false) {
				throw new IllegalArgumentException("Failed to insert row because User Playlist ID is needed " + uri);
			}
			if (values.containsKey(PlaylistSongsTableMetaData.PLAYLISTSONG_SONGID) == false) {
				throw new IllegalArgumentException("Failed to insert row because Playlist Song ID is needed " + uri);
			}
			if (values.containsKey(PlaylistSongsTableMetaData.PLAYLISTSONG_TRACKNUMBER) == false) {
				throw new IllegalArgumentException("Failed to insert row because Track Number is needed " + uri);
			}
			
			//SQLiteDatabase db = mOpenHelper.createDataBase();
			SQLiteDatabase db =	mOpenHelper.getWritableDatabase();

			
			long rowId = db.insert(PlaylistSongsTableMetaData.TABLE_NAME, PlaylistSongsTableMetaData.PLAYLISTSONG_TRACKNUMBER, values);
			if (rowId > 0) {
				Uri insertedPlaylistSongUri = ContentUris.withAppendedId(PlaylistSongsTableMetaData.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(insertedPlaylistSongUri, null);
				db.close();
				return insertedPlaylistSongUri;
			}
			db.close();
			throw new IllegalArgumentException("Failed to insert row into " + uri);
			// TODO: replace IllegalArgumentException with SQLException 
		}
		
		//// Album songs
		else if(sUriMatcher.match(uri) == ALBUMSONGS_COLLECTION_URI_INDICATOR) {			
			//validate input fields
			// Make sure that the fields are all set
			if (values.containsKey(AlbumSongsTableMetaData.ALBUMSONG_ALBUMID) == false) {
				throw new IllegalArgumentException("Failed to insert row because Album ID is needed " + uri);
			}
			if (values.containsKey(AlbumSongsTableMetaData.ALBUMSONG_SONGID) == false) {
				throw new IllegalArgumentException("Failed to insert row because Album Song ID is needed " + uri);
			}
			if (values.containsKey(AlbumSongsTableMetaData.ALBUMSONG_TRACKNUMBER) == false) {
				throw new IllegalArgumentException("Failed to insert row because Track Number is needed " + uri);
			}
			
			//SQLiteDatabase db = mOpenHelper.createDataBase();
			SQLiteDatabase db =	mOpenHelper.getWritableDatabase();

			
			//long rowId = db.insert(AlbumSongsTableMetaData.TABLE_NAME, AlbumSongsTableMetaData.ALBUMSONG_TRACKNUMBER, values);
			long rowId = db.insert(AlbumSongsTableMetaData.TABLE_NAME, null,  values);
			if (rowId > 0) {
				Uri insertedAlbumSongUri = ContentUris.withAppendedId(AlbumSongsTableMetaData.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(insertedAlbumSongUri, null);
				db.close();
				return insertedAlbumSongUri;
			}
			db.close();
			throw new IllegalArgumentException("Failed to insert row into " + uri);
			// TODO: replace IllegalArgumentException with SQLException 
		}
		
		////URI does not match any pattern for this ContentProvider
		throw new IllegalArgumentException("Unknown URI " + uri);
	}


	@Override
	public boolean onCreate() {
		//mOpenHelper = new MXDatabaseHelper(getContext());
		//return true;
		
        mOpenHelper = new MXDatabaseHelper(getContext());
        if (!mOpenHelper.isDataBaseExist())
        {
                try
                {
                        mOpenHelper.copyDataBase();
                }
                catch (IOException e)
                {
                        e.printStackTrace();
               
                }
        }
        return true;
	}
	
	/**
	 * This method queries the database for all tables
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		// TODO delete Log.i() for production
		Log.i("MusixLocalContentProvider", "selection: "+selection+", uri: "+uri.toString());
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		// If no sort order is specified use the default
		String orderBy;
		
		// Get the database and run the query
		//SQLiteDatabase db =	mOpenHelper.createDataBase();
		SQLiteDatabase db =	mOpenHelper.getReadableDatabase();

		Cursor c;
		
		switch (sUriMatcher.match(uri))
		{		
			// songs select statement
			////select tblSongs._id, tblSongs.catalogMediaID, tblSongs.userMediaID, tblAlbums.albumName as AlbumName, tblArtists.catalogArtistID as ArtistName, tblGenres.name as GenreName, tblSongs.duration
			////from tblSongs inner join tblAlbums inner join tblArtists inner join tblGenres on tblSongs.albumID=tblAlbums._id and tblSongs.artistID=tblArtists._id and tblSongs.genreID=tblGenres._id
			case SONGS_COLLECTION_URI_INDICATOR:
				////probably this is normal query
				if(selectionArgs != null && selectionArgs.length > 0){
					qb.setTables(SongsTableMetaData.TABLE_NAME);
					if(projection != null && projection.length < 1)
						qb.setProjectionMap(sMusixSongsProjectionMap);
					if (TextUtils.isEmpty(sortOrder)) {
						orderBy = SongsTableMetaData.DEFAULT_SORT_ORDER;
					}
					else {
						orderBy = sortOrder;
					}
					c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
				}
				else{
					String songSelectionStatement= "select tblSongs._id, tblSongs.catalogMediaID, tblSongs.userMediaID, tblAlbums.albumName as AlbumName, tblArtists.catalogArtistName as ArtistName, tblGenres.name as GenreName, tblSongs.duration, tblSongs.name from tblSongs inner join tblAlbums inner join tblArtists inner join tblGenres on tblSongs.albumID=tblAlbums._id and tblSongs.artistID=tblArtists._id and tblSongs.genreID=tblGenres._id";
					if(sortOrder.equals(SongsTableMetaData.TIMESTAMP_SORT_ORDER))
						songSelectionStatement = songSelectionStatement + ORDERBY_STRING + SongsTableMetaData.TIMESTAMP_SORT_ORDER;
					else
						songSelectionStatement = songSelectionStatement + ORDERBY_STRING + SongsTableMetaData.DEFAULT_SORT_ORDER;
					c = db.rawQuery(songSelectionStatement, null);
					Log.d("MusixLocalContentProvider","songs selected");
				}
				
				break;			
			case SINGLE_SONG_URI_INDICATOR:
				if(selectionArgs != null && selectionArgs.length > 0){
					qb.setTables(SongsTableMetaData.TABLE_NAME);
					if(projection != null && projection.length < 1)
						qb.setProjectionMap(sMusixSongsProjectionMap);
					qb.appendWhere(SongsTableMetaData._ID + "="
							+ uri.getPathSegments().get(1));
					if (TextUtils.isEmpty(sortOrder)) {
						orderBy = SongsTableMetaData.DEFAULT_SORT_ORDER;
					}
					else {
						orderBy = sortOrder;
					}
					c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
				}
				else{
					c = db.rawQuery("select tblSongs._id, tblSongs.catalogMediaID, tblSongs.userMediaID, tblAlbums.albumName as AlbumName, tblArtists.catalogArtistName as ArtistName, tblGenres.name as GenreName, tblSongs.duration, tblSongs.name from tblSongs inner join tblAlbums inner join tblArtists inner join tblGenres on tblSongs.albumID=tblAlbums._id and tblSongs.artistID=tblArtists._id and tblSongs.genreID=tblGenres._id where tblSongs._id=?", new String[]{uri.getPathSegments().get(1)});
				}
				break;
			case SINGLE_SONG_BYCATALOGMEDIA_URI_INDICATOR:
				
				c = db.rawQuery("select tblSongs._id, tblSongs.catalogMediaID, tblSongs.userMediaID, tblAlbums.albumName as AlbumName, tblArtists.catalogArtistName as ArtistName, tblGenres.name as GenreName, tblSongs.duration, tblSongs.name from tblSongs inner join tblAlbums inner join tblArtists inner join tblGenres on tblSongs.albumID=tblAlbums._id and tblSongs.artistID=tblArtists._id and tblSongs.genreID=tblGenres._id where tblSongs.catalogMediaID=?", new String[]{uri.getPathSegments().get(2)});
				
				break;
			case SINGLE_SONG_BYUSERMEDIA_URI_INDICATOR:
				
				c = db.rawQuery("select tblSongs._id, tblSongs.catalogMediaID, tblSongs.userMediaID, tblAlbums.albumName as AlbumName, tblArtists.catalogArtistName as ArtistName, tblGenres.name as GenreName, tblSongs.duration, tblSongs.name from tblSongs inner join tblAlbums inner join tblArtists inner join tblGenres on tblSongs.albumID=tblAlbums._id and tblSongs.artistID=tblArtists._id and tblSongs.genreID=tblGenres._id where tblSongs.userMediaID=?", new String[]{uri.getPathSegments().get(2)});
				
				break;
				
				// AlbumSongs select statement
//				select tblAlbums._id, tblAlbums.catalogAlbumID, tblSongs._id as songID, tblSongs.catalogMediaID, tblSongs.userMediaID, tblAlbumsSongs.trackNumber, tblSongs.name, tblSongs.duration 
//				from tblSongs inner join tblAlbums on tblSongs.albumID=tblAlbums._id inner join tblAlbumsSongs on tblSongs._id=tblAlbumsSongs.songID

				//rostik
			case ALBUMSONGS_COLLECTION_URI_INDICATOR:
				if(selectionArgs != null && selectionArgs.length > 0){
					qb.setTables(AlbumSongsTableMetaData.TABLE_NAME);
					if(projection != null && projection.length < 1)
						qb.setProjectionMap(sMusixAlbumSongsProjectionMap);
					if (TextUtils.isEmpty(sortOrder)) {
						orderBy = PlaylistSongsTableMetaData.DEFAULT_SORT_ORDER;
					}
					else {
						orderBy = sortOrder;
					}
					c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
				}
				else{
					c=null;

				}
				break;
				
			case SINGLE_ALBUMSONG_URI_INDICATOR:
				if(selectionArgs != null && selectionArgs.length > 0){
					qb.setTables(AlbumSongsTableMetaData.TABLE_NAME);
					qb.appendWhere(AlbumsTableMetaData._ID + "="
							+ uri.getPathSegments().get(1));
					if (TextUtils.isEmpty(sortOrder)) {
						orderBy = AlbumSongsTableMetaData.DEFAULT_SORT_ORDER;
					}
					else {
						orderBy = sortOrder;
					}
					c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
				}
				else{
					String albumSongSelectionStatement= "select tblAlbums._id, tblAlbums.catalogAlbumID, tblSongs._id as songID, tblSongs.catalogMediaID, tblSongs.userMediaID, tblAlbumsSongs.trackNumber, tblSongs.name, tblSongs.duration,tblArtists.catalogArtistName as ArtistName from tblSongs inner join tblAlbums on tblSongs.albumID=tblAlbums._id inner join tblAlbumsSongs on tblSongs._id=tblAlbumsSongs.songID inner join tblArtists on tblSongs.artistID = tblArtists._id where tblAlbums._id=?";
					albumSongSelectionStatement = albumSongSelectionStatement + ORDERBY_STRING + AlbumSongsTableMetaData.DEFAULT_SORT_ORDER;
					c = db.rawQuery(albumSongSelectionStatement, new String[]{uri.getPathSegments().get(1)});
				}
				break;
				
			////playlistsongs
//				select tblSongs._id, tblSongs.catalogMediaID, tblSongs.userMediaID, tblPlaylistsSongs.trackNumber, tblSongs.name, tblSongs.duration, tblArtists.catalogArtistID as artistName
//				from tblPlaylistsSongs inner join tblSongs on tblPlaylistsSongs.songID=tblSongs._id inner join tblArtists on tblSongs.artistID=tblArtists._id
//				where tblPlaylistsSongs.playlistID = 1
//				order by trackNumber	
				
				//Added by rostik
			case PLAYLISTSONGS_COLLECTION_URI_INDICATOR :
				if(selectionArgs != null && selectionArgs.length > 0){
					qb.setTables(PlaylistSongsTableMetaData.TABLE_NAME);
					if(projection != null && projection.length < 1)
						qb.setProjectionMap(sMusixPlaylistSongsProjectionMap);
					if (TextUtils.isEmpty(sortOrder)) {
						orderBy = PlaylistSongsTableMetaData.DEFAULT_SORT_ORDER;
					}
					else {
						orderBy = sortOrder;
					}
					c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
				}
				else{
					c=null;

				}
				break;
			case SINGLE_PLAYLISTSONG_URI_INDICATOR:
				if(selectionArgs != null && selectionArgs.length > 0){
					qb.setTables(PlaylistSongsTableMetaData.TABLE_NAME);
					qb.appendWhere(PlaylistSongsTableMetaData._ID + "="
							+ uri.getPathSegments().get(1));
					if (TextUtils.isEmpty(sortOrder)) {
						orderBy = PlaylistSongsTableMetaData.DEFAULT_SORT_ORDER;
					}
					else {
						orderBy = sortOrder;
					}
					c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
				}
				else{
					c = db.rawQuery("select tblSongs._id, tblSongs.catalogMediaID, tblSongs.userMediaID, tblPlaylistsSongs.trackNumber, tblSongs.name, tblSongs.duration, tblArtists.catalogArtistName as artistName from tblPlaylistsSongs inner join tblSongs on tblPlaylistsSongs.songID=tblSongs._id inner join tblArtists on tblSongs.artistID=tblArtists._id where tblPlaylistsSongs.playlistID = ? order by trackNumber", new String[]{uri.getPathSegments().get(1)});
				}
				break;
				
				//Albums
//				select tblAlbums._id, tblAlbums.catalogAlbumID, tblAlbums.albumName as Name, tblArtists.catalogArtistID as ArtistName, tblGenres.name as GenreName, count(tblAlbumsSongs.songID) as SongCount
//				from tblAlbums inner join tblArtists on tblAlbums.adtistID=tblArtists._id inner join tblGenres on tblAlbums.genreID=tblGenres._id left join tblAlbumsSongs on tblAlbums._id=tblAlbumsSongs.albumID
//				group by tblAlbums.catalogAlbumID, tblAlbums.albumName, tblArtists.catalogArtistID, tblGenres.name, tblAlbums._id
				
			//// new Albums (Ron Srebro, May 20, 2010)
//				select tblAlbums._id, tblAlbums.catalogAlbumID, tblAlbums.albumName as Name, tblArtists.catalogArtistID as ArtistName, tblGenres.name as GenreName, count(tblAlbumsSongs.songID) as SongCount
//				from tblAlbums inner join tblArtists on tblAlbums.artistID=tblArtists._id left join tblGenres on tblArtists.genreID=tblGenres._id left join tblAlbumsSongs on tblAlbums._id=tblAlbumsSongs.albumID
//				group by tblAlbums.catalogAlbumID, tblAlbums.albumName, tblArtists.catalogArtistID, tblGenres.name, tblAlbums._id
				////inner join was substituted by left join - no albums when artistID is not set error 
			case ALBUMS_COLLECTION_URI_INDICATOR:
				if(selectionArgs != null && selectionArgs.length > 0){
					qb.setTables(AlbumsTableMetaData.TABLE_NAME);
					if(projection != null && projection.length < 1)
						qb.setProjectionMap(sMusixAlbumsProjectionMap);
					if (TextUtils.isEmpty(sortOrder)) {
						//orderBy = AlbumSongsTableMetaData.DEFAULT_SORT_ORDER;
						orderBy =AlbumsTableMetaData.DEFAULT_SORT_ORDER;
					}
					else {
						orderBy = sortOrder;
					}
					c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
				}
				else{
					String albumsSelectionStatement= "select tblAlbums._id, tblAlbums.catalogAlbumID, tblAlbums.albumName as Name, tblArtists.catalogArtistName as ArtistName, tblGenres.name as GenreName, count(tblAlbumsSongs.songID) as SongCount from tblAlbums inner join tblArtists on tblAlbums.artistID=tblArtists.catalogArtistID left join tblGenres on tblArtists.genreID=tblGenres._id left join tblAlbumsSongs on tblAlbums._id=tblAlbumsSongs.albumID group by tblAlbums.catalogAlbumID, tblAlbums.albumName, tblArtists.catalogArtistID, tblGenres.name, tblAlbums._id";
					if(sortOrder.equals(AlbumsTableMetaData.DEFAULT_SORT_ORDER))
						albumsSelectionStatement = albumsSelectionStatement + ORDERBY_STRING + AlbumsTableMetaData.DEFAULT_SORT_ORDER;
					else
						albumsSelectionStatement = albumsSelectionStatement + ORDERBY_STRING + AlbumsTableMetaData.ARTIST_SORT_ORDER;
					c = db.rawQuery(albumsSelectionStatement, null);
				}
				break;
				
			case SINGLE_ALBUM_URI_INDICATOR:
				if(selectionArgs != null && selectionArgs.length > 0){
					qb.setTables(AlbumsTableMetaData.TABLE_NAME);
					if(projection != null && projection.length < 1)
						qb.setProjectionMap(sMusixAlbumsProjectionMap);
					qb.appendWhere(AlbumsTableMetaData._ID + "="
							+ uri.getPathSegments().get(1));
					if (TextUtils.isEmpty(sortOrder)) {
						orderBy = AlbumSongsTableMetaData.DEFAULT_SORT_ORDER;
					}
					else {
						orderBy = sortOrder;
					}
					c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
				}
				else{
					String res = String.format("select tblAlbums._id, tblAlbums.catalogAlbumID, tblAlbums.albumName as Name, tblArtists.catalogArtistName as ArtistName, tblGenres.name as GenreName, count(tblAlbumsSongs.songID) as SongCount from tblAlbums left join tblArtists on tblAlbums.artistID=tblArtists._id left join tblGenres on tblArtists.genreID=tblGenres._id left join tblAlbumsSongs on tblAlbums._id=tblAlbumsSongs.albumID where tblAlbums._id='%s' group by tblAlbums.catalogAlbumID, tblAlbums.albumName, tblArtists.catalogArtistID, tblGenres.name, tblAlbums._id", new String[]{uri.getPathSegments().get(1)});
					Log.i("MusixLocalCP", res);
					c = db.rawQuery("select tblAlbums._id, tblAlbums.catalogAlbumID, tblAlbums.albumName as Name, tblArtists.catalogArtistName as ArtistName, tblGenres.name as GenreName, count(tblAlbumsSongs.songID) as SongCount from tblAlbums left join tblArtists on tblAlbums.artistID=tblArtists._id left join tblGenres on tblArtists.genreID=tblGenres._id left join tblAlbumsSongs on tblAlbums._id=tblAlbumsSongs.albumID where tblAlbums._id=? group by tblAlbums.catalogAlbumID, tblAlbums.albumName, tblArtists.catalogArtistID, tblGenres.name, tblAlbums._id", new String[]{uri.getPathSegments().get(1)});
					Log.i("MusixLocalCP", "quesry executed, length "+c.getCount());
				}
				break;
			
			//playlists
//			select tblPlaylists._id, tblPlaylists.userPlaylistID, tblPlaylists.catalogPlaylistID, count(tblPlaylistsSongs.songID) as SongCount 
//			from tblPlaylists inner join tblPlaylistsSongs on tblPlaylists._id=tblPlaylistsSongs.playlistID 
//			group by tblPlaylists.userPlaylistID, tblPlaylists.catalogPlaylistID, tblPlaylists._id
				
			////new (added name field
//				select tblPlaylists._id, tblPlaylists.name, tblPlaylists.userPlaylistID, tblPlaylists.catalogPlaylistID, count(tblPlaylistsSongs.songID) as SongCount 
//				from tblPlaylists inner join tblPlaylistsSongs on tblPlaylists._id=tblPlaylistsSongs.playlistID 
//				group by tblPlaylists.userPlaylistID, tblPlaylists.catalogPlaylistID, tblPlaylists._id
			case PLAYLISTS_COLLECTION_URI_INDICATOR:
				if(selectionArgs != null && selectionArgs.length > 0){
					qb.setTables(PlaylistTableMetaData.TABLE_NAME);
					if(projection != null && projection.length < 1)
						qb.setProjectionMap(sMusixPlaylistsProjectionMap);
					if (TextUtils.isEmpty(sortOrder)) {
						orderBy = PlaylistTableMetaData.DEFAULT_SORT_ORDER;
					}
					else {
						orderBy = sortOrder;
					}
					c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
				}
				else{
					String playlistsSelectionStatement= "select tblPlaylists._id, tblPlaylists.name, tblPlaylists.userPlaylistID, tblPlaylists.catalogPlaylistID, count(tblPlaylistsSongs.songID) as SongCount from tblPlaylists inner join tblPlaylistsSongs on tblPlaylists._id=tblPlaylistsSongs.playlistID group by tblPlaylists.userPlaylistID, tblPlaylists.catalogPlaylistID, tblPlaylists._id";
					if(sortOrder.equals(PlaylistTableMetaData.DEFAULT_SORT_ORDER))
						playlistsSelectionStatement = playlistsSelectionStatement + ORDERBY_STRING + PlaylistTableMetaData.DEFAULT_SORT_ORDER;
					else
						playlistsSelectionStatement = playlistsSelectionStatement + ORDERBY_STRING + PlaylistTableMetaData.CATALOG_SORT_ORDER;
					c = db.rawQuery(playlistsSelectionStatement, null);
				}
				break;
				
			case SINGLE_PLAYLIST_URI_INDICATOR:
				if(selectionArgs != null && selectionArgs.length > 0){
					qb.setTables(PlaylistTableMetaData.TABLE_NAME);
					if(projection != null && projection.length < 1)
						qb.setProjectionMap(sMusixPlaylistsProjectionMap);
					qb.appendWhere(PlaylistTableMetaData._ID + "="
							+ uri.getPathSegments().get(1));
					if (TextUtils.isEmpty(sortOrder)) {
						orderBy = PlaylistTableMetaData.DEFAULT_SORT_ORDER;
					}
					else {
						orderBy = sortOrder;
					}
					c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
				}
				else{
					c = db.rawQuery("select tblPlaylists._id, tblPlaylists.name, tblPlaylists.userPlaylistID, tblPlaylists.catalogPlaylistID, count(tblPlaylistsSongs.songID) as SongCount from tblPlaylists inner join tblPlaylistsSongs on tblPlaylists._id=tblPlaylistsSongs.playlistID where tblPlaylists._id=? group by tblPlaylists.userPlaylistID, tblPlaylists.catalogPlaylistID, tblPlaylists._id", new String[]{uri.getPathSegments().get(1)});
				}
				break;
			case SINGLE_PLAYLIST_BYCATALOGPLAYLISTID_URI_INDICATOR:
				c = db.rawQuery("select tblPlaylists._id, tblPlaylists.name, tblPlaylists.userPlaylistID, tblPlaylists.catalogPlaylistID, count(tblPlaylistsSongs.songID) as SongCount from tblPlaylists inner join tblPlaylistsSongs on tblPlaylists._id=tblPlaylistsSongs.playlistID where tblPlaylists.catalogPlaylistID=? group by tblPlaylists.userPlaylistID, tblPlaylists.catalogPlaylistID, tblPlaylists._id", new String[]{uri.getPathSegments().get(2)});
				break;
			case SINGLE_PLAYLIST_BYUSERPLAYLISTID_URI_INDICATOR:
				c = db.rawQuery("select tblPlaylists._id, tblPlaylists.name, tblPlaylists.userPlaylistID, tblPlaylists.catalogPlaylistID, count(tblPlaylistsSongs.songID) as SongCount from tblPlaylists left join tblPlaylistsSongs on tblPlaylists._id=tblPlaylistsSongs.playlistID where tblPlaylists.userPlaylistID=? group by tblPlaylists.userPlaylistID, tblPlaylists.catalogPlaylistID, tblPlaylists._id", new String[]{uri.getPathSegments().get(2)});
				break;
			
			//Genres
//				select tblGenres._id, catalogGenreID, count(distinct artistID) as artistCount, count(tblSongs._id) as songCount, tblGenres.name 
//				from tblGenres left join tblSongs on tblGenres._ID=tblSongs.genreID group by catalogGenreID, tblGenres.name, tblGenres._id 
			case GENRES_COLLECTION_URI_INDICATOR:
				if(selectionArgs != null && selectionArgs.length > 0){
					qb.setTables(GenresTableMetaData.TABLE_NAME);
					if(projection != null && projection.length < 1)
						qb.setProjectionMap(sMusixGenresProjectionMap);
					if (TextUtils.isEmpty(sortOrder)) {
						orderBy = GenresTableMetaData.DEFAULT_SORT_ORDER;
					}
					else {
						orderBy = sortOrder;
					}
					c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
				}
				else
					c = db.rawQuery("select tblGenres._id, catalogGenreID, count(distinct artistID) as artistCount, count(tblSongs._id) as songCount, tblGenres.name from tblGenres left join tblSongs on tblGenres._ID=tblSongs.genreID group by catalogGenreID, tblGenres.name, tblGenres._id", null);
				
				break;
				
			case SINGLE_GENRE_URI_INDICATOR:
				if(selectionArgs != null && selectionArgs.length > 0){
					if (TextUtils.isEmpty(sortOrder)) {
						orderBy = GenresTableMetaData.DEFAULT_SORT_ORDER;
					}
					else {
						orderBy = sortOrder;
					}
					qb.setTables(GenresTableMetaData.TABLE_NAME);
					if(projection != null && projection.length < 1)
						qb.setProjectionMap(sMusixGenresProjectionMap);
					qb.appendWhere(GenresTableMetaData._ID + "="
							+ uri.getPathSegments().get(1));
				
					c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
				}
				else{
					c = db.rawQuery("select tblGenres._id, catalogGenreID, count(distinct artistID) as artistCount, count(tblSongs._id) as songCount, tblGenres.name from tblGenres left join tblSongs on tblGenres._ID=tblSongs.genreID where tblGenres._id=?", new String[]{uri.getPathSegments().get(1)});
				}
				break;
			
				//artists
//				select tblArtists._id, tblArtists.catalogArtistID, tblGenres.name as GenreName, tblGenres.catalogGenreID as GenreID, count(distinct tblAlbums.albumName) as AlbumCount, count(tblSongs.name) as SongCount
//				from tblArtists inner join tblGenres on tblArtists.genreID=tblGenres._id inner join tblAlbums on tblArtists._id=tblAlbums.artistID left join tblSongs on tblSongs.artistID=tblArtists._id
//				group by  tblArtists.catalogArtistID, tblGenres.name, tblGenres.catalogGenreID, tblArtists._id
				//// _ID instead of catalogGenreID
				//// left join with Albums and genres - because albums may not have artists 
			case ARTISTS_COLLECTION_URI_INDICATOR:
				if(selectionArgs != null && selectionArgs.length > 0){
					qb.setTables(ArtistsTableMetaData.TABLE_NAME);
					if(projection != null && projection.length < 1)
						qb.setProjectionMap(sMusixArtistsProjectionMap);
					if (TextUtils.isEmpty(sortOrder)) {
						orderBy = ArtistsTableMetaData.DEFAULT_SORT_ORDER;//GenresTableMetaData.DEFAULT_SORT_ORDER;
					}
					else {
						orderBy = sortOrder;
					}
					c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
				}
				else
					c = db.rawQuery("select tblArtists._id, tblArtists.catalogArtistID, tblGenres.name as GenreName, tblGenres._ID as GenreID, count(distinct tblAlbums.albumName) as AlbumCount, count(tblSongs.name) as SongCount, tblArtists.catalogArtistName from tblArtists left join tblGenres on tblArtists.genreID=tblGenres._id left join tblAlbums on tblArtists._id=tblAlbums.artistID left join tblSongs on tblSongs.artistID=tblArtists._id group by  tblArtists.catalogArtistID, tblGenres.name, tblGenres.catalogGenreID, tblArtists._id", null);
				
				break;
				
			case SINGLE_ARTIST_URI_INDICATOR:
				if(selectionArgs != null && selectionArgs.length > 0){
					qb.setTables(ArtistsTableMetaData.TABLE_NAME);
					if(projection != null && projection.length < 1)
						qb.setProjectionMap(sMusixArtistsProjectionMap);
					qb.appendWhere(ArtistsTableMetaData._ID + "="
							+ uri.getPathSegments().get(1));
					if (TextUtils.isEmpty(sortOrder)) {
						orderBy = GenresTableMetaData.DEFAULT_SORT_ORDER;
					}
					else {
						orderBy = sortOrder;
					}
					c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
				}
				else{
					String artistSelectionStatement= "select tblArtists._id, tblArtists.catalogArtistID, tblGenres.name as GenreName, tblGenres._ID as GenreID, count(distinct tblAlbums.albumName) as AlbumCount, count(tblSongs.name) as SongCount, tblArtists.catalogArtistName from tblArtists left join tblGenres on tblArtists.genreID=tblGenres._id left join tblAlbums on tblArtists.catalogArtistID=tblAlbums.artistID left join tblSongs on tblSongs.artistID=tblArtists._id where tblArtists._id=? group by  tblArtists.catalogArtistID, tblGenres.name, tblGenres.catalogGenreID, tblArtists._id";
					artistSelectionStatement = artistSelectionStatement + ORDERBY_STRING + ArtistsTableMetaData.DEFAULT_SORT_ORDER;
					c = db.rawQuery(artistSelectionStatement, new String[]{uri.getPathSegments().get(1)});
				}
				break;
				
			//artistsforgenre
//			select tblArtists._id, tblArtists.catalogArtistID as ArtistName, count(distinct tblAlbums.albumName) as AlbumCount, count(tblSongs.name) as SongCount
//			from tblArtists inner join  tblAlbums on tblArtists._id=tblAlbums.artistID inner join tblGenres on tblArtists.genreID=tblGenres._id inner join tblSongs on tblArtists._id=tblSongs.artistID
//			where tblGenres.catalogGenreID="popgenre"
//			--where tblGenres._id=2
//			group by tblArtists._id,  ArtistName, tblArtists._id
//			order by ArtistName
				////left joins instead of inner joins for genre and albums
			case ARTISTSFORGENRE_URI_INDICATOR:
				c = db.rawQuery("select tblArtists._id, tblArtists.catalogArtistID as ArtistID, count(distinct tblAlbums.albumName) as AlbumCount, count(tblSongs.name) as SongCount, tblArtists.catalogArtistName as ArtistName from tblArtists left join  tblAlbums on tblArtists._id=tblAlbums.artistID left join tblGenres on tblArtists.genreID=tblGenres._id inner join tblSongs on tblArtists._id=tblSongs.artistID where tblGenres._id=? group by tblArtists._id,  ArtistName, tblArtists._id order by ArtistName", new String[]{uri.getPathSegments().get(1)});
				break;
				
			case ARTISTSFORGENRE_URI_INDICATOR_CATALOG:
				Log.i("MusixLocalContentProvider", "foung: "+ARTISTSFORGENRE_URI_INDICATOR_CATALOG+ uri.getPathSegments().get(2));
				c = db.rawQuery("select tblArtists._id, tblArtists.catalogArtistID as ArtistID, count(distinct tblAlbums.albumName) as AlbumCount, count(tblSongs.name) as SongCount, tblArtists.catalogArtistName as ArtistName from tblArtists left join  tblAlbums on tblArtists._id=tblAlbums.artistID left join tblGenres on tblArtists.genreID=tblGenres._id inner join tblSongs on tblArtists._id=tblSongs.artistID where tblGenres.catalogGenreID=? group by tblArtists._id,  ArtistName, tblArtists._id order by ArtistName", new String[]{uri.getPathSegments().get(2)});
				break;
				
			//// select tblAlbums._id, tblAlbums.catalogAlbumID, tblAlbums.albumName as Name, tblArtists.catalogArtistID as ArtistName, tblGenres.name as GenreName, count(tblAlbumsSongs.songID) as SongCount from tblAlbums inner join tblArtists on tblAlbums.artistID=tblArtists._id left join tblGenres on tblArtists.genreID=tblGenres._id left join tblAlbumsSongs on tblAlbums._id=tblAlbumsSongs.albumID where tblAlbums.artistID=? group by tblAlbums.catalogAlbumID, tblAlbums.albumName, tblArtists.catalogArtistID, tblGenres.name, tblAlbums._id
			case ALBUMSFORARTIST_URI_INDICATOR:
				Log.i("MusixLocalContentProvider", "albums for artist");
				c = db.rawQuery("select tblAlbums._id, tblAlbums.catalogAlbumID, tblAlbums.albumName as Name, tblArtists.catalogArtistName as ArtistName, tblGenres.name as GenreName, count(tblAlbumsSongs.songID) as SongCount from tblAlbums inner join tblArtists on tblAlbums.artistID=tblArtists.catalogArtistID left join tblGenres on tblArtists.genreID=tblGenres._id left join tblAlbumsSongs on tblAlbums._id=tblAlbumsSongs.albumID where tblArtists._id=? group by tblAlbums.catalogAlbumID, tblAlbums.albumName, tblArtists.catalogArtistID, tblGenres.name, tblAlbums._id", new String[]{ uri.getPathSegments().get(1) });
				break;
				
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		
		// Tell the cursor what uri to watch,
		// so it knows when its source data changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		//SQLiteDatabase db = mOpenHelper.();
		SQLiteDatabase db =	mOpenHelper.getWritableDatabase();

		int affectedRows = 0;
		switch (sUriMatcher.match(uri))
		{		
			case SONGS_COLLECTION_URI_INDICATOR:
				affectedRows = db.update(SongsTableMetaData.TABLE_NAME, values, where, whereArgs);
				break;
			case ARTISTS_COLLECTION_URI_INDICATOR:
				//// if there is catalogGenreID - replace it with normal genreID (int)
				if (values.containsKey(GenresTableMetaData.GENRE_CATALOGGENREID) == true){//// if catalogGenreID is present - find genreID (int)
					String result = String.format("select _id from "+GenresTableMetaData.TABLE_NAME+" where "+GenresTableMetaData.GENRE_CATALOGGENREID+"='%s'", new String[]{values.get(GenresTableMetaData.GENRE_CATALOGGENREID).toString()});
					Log.i("MusixLocalContentProvider", "GenreID selection: "+result);
					Cursor genreIdCur = db.rawQuery("select _id from "+GenresTableMetaData.TABLE_NAME+" where "+GenresTableMetaData.GENRE_CATALOGGENREID+"=?", new String[]{values.get(GenresTableMetaData.GENRE_CATALOGGENREID).toString()});
					genreIdCur.moveToFirst();
					if(genreIdCur.getCount()>0){
						values.put(ArtistsTableMetaData.ARTIST_GENREID, genreIdCur.getInt(0));
						values.remove(GenresTableMetaData.GENRE_CATALOGGENREID); //// this needs to be removed because such column does not exist
						genreIdCur.close();
					}
					else{
						db.close();
						throw new IllegalArgumentException("Failed to insert row because Catalog Genre ID is not in a database " + uri);
					}
				}
				affectedRows = db.update(ArtistsTableMetaData.TABLE_NAME, values, where, whereArgs);
				break;
			default:
				throw new IllegalArgumentException("Update not supported. URI: " + uri);
		}
		
		///close the DB
		db.close();
		
		return affectedRows;
	}

}

package il.co.pelephone.musix.comm;

import il.co.pelephone.musix.UI.IMXUIMusicCatalogCallback;
import il.co.pelephone.musix.UI.IMXUIMusicCatalogCallback.MusicCatalogResponseType;
import il.co.pelephone.musix.UI.utility.Constants;
import il.co.pelephone.musix.comm.MXSettings.MXAppMode;
import il.co.pelephone.musix.comm.MXStoreSearchRequest.SDStoreSearchType;
import il.co.pelephone.musix.data.MusicEntity;
import il.co.pelephone.musix.data.MusicEntity.EntityType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class MXStore implements IMXCommCallback{

	private String TAG = "MXStore";
	private static MXStore _instance;
	private MXCommLayer mSender;
	private int numberOfPendingRequests;////this will increase every time the request is sent
	////to a server and decrease every time the response is received. this way when the number
	////is zero we know that all responses came back and we may update the UI.  
	private IMXUIMusicCatalogCallback callback;
	private IMXUIMusicCatalogCallback.MusicCatalogResponseType currentCall;
	
	public ArrayList<MusicEntity> promotedPlaylists;
	public ArrayList<MusicEntity> promotedAlbums;
	public ArrayList<MusicEntity> promotedSongs;
	public ArrayList<MusicEntity> searchedArtists;
	public ArrayList<MusicEntity> searchedGenres;
	
	private static final String albumsTag = "albums";
	private static final String albumTag = "album";
	private static final String playlistsTag = "playlists";
	private static final String playlistTag = "playlist";
	private static final String genresTag = "genres";
	private static final String genreTag = "genre";
	private static final String artistsTag = "artists";
	private static final String artistTag = "artist";
	private static final String mediasTag = "medias";
	private static final String mediaTag = "media";
	private static final String searchNumbersTag = "totalItemsInSearch";
	
	private static final String artistsTagStart="<artists>";
	private static final String artistsTagEnd="</artists>";
	private static final String albumsTagStart="<albums>";
	private static final String albumsTagEnd="</albums>";
	private static final String mediasTagStart="<medias>";
	private static final String mediasTagEnd="</medias>";
	private static final String genresTagStart="<genres>";
	private static final String genresTagEnd="</genres>";
	private static final String playlistsTagStart="<playlists>";
	private static final String playlistsTagEnd="</playlists>";
	private static final String totalItemsTagStart="<totalItemsInSearch>";
	private static final String totalItemsTagEnd="</totalItemsInSearch>";
	
	private int artistsNodeEndIndex=-1;
	private int artistsNodeStartIndex=-1;	
	private int albumsNodeEndIndex=-1;
	private int albumsNodeStartIndex=-1;		
	private int mediasNodeEndIndex=-1;
	private int mediasNodeStartIndex=-1;	
	private int genresNodeEndIndex=-1;
	private int genresNodeStartIndex=-1;
	private int playlistsNodeEndIndex=-1;
	private int playlistsNodeStartIndex=-1;
	private int totalItemsSearchNodeEndIndex=-1;
	private int totalItemsSearchNodeStartIndex=-1;
	
	private static final String idAttr = "id";
	private static final String nameAttr = "name";
	private static final String lengthAttr = "length";
	private static final String artistNameAttr = "artistName";
	private static final String genreNameAttr = "genreName";
	private static final String albumNameAttr = "albumName";
	private static final String albumIdAttr = "albumId";
	
	private MXSettings mSettings;
	
	private XmlPullParser parser;
	private boolean palylistGeneralPromotionReceived = false;
	private boolean albumGeneralPromotionReceived = false;
	private boolean mediaGeneralPromotionReceived = false;
	
	private int totalItemsInSearch = 0;
	
	public int getTotalItemsInSearch() {
		return totalItemsInSearch;
	}

	private MXStore(MXSettings mSettings){
		numberOfPendingRequests = 0;
		mSender=MXCommLayer.getInstance();
		this.mSettings = mSettings;
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			parser = factory.newPullParser();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static MXStore getInstance(MXSettings mSettings){
		if(_instance == null)
			_instance = new MXStore(mSettings);
		
		return _instance;
	}
	
	public void getMediaPromotionsForGenre(String genreID){
		currentCall = MusicCatalogResponseType.ResponseTypeMediaPromotion;
		getMediaPromotionsForGenreInternal(genreID);
//		Log.d(TAG, "media promotion for genre: "+genreID);
	}

	private void getMediaPromotionsForGenreInternal(String genreID) {
		MXGetMediaPromotionsRequest mx_request = new MXGetMediaPromotionsRequest(genreID);
		sendRequest(mx_request);
	}
	
	public void getAlbumPromotionsForGenre(String genreID){
		currentCall = MusicCatalogResponseType.ResponseTypeAlbumPromotion;
		getAlbumPromotionsForGenreInternal(genreID);
//		Log.d(TAG, "album promotion for genre: "+genreID);
	}

	private void getAlbumPromotionsForGenreInternal(String genreID) {
		MXGetAlbumPromotionsRequest mx_request = new MXGetAlbumPromotionsRequest(genreID);
		sendRequest(mx_request);
	}
	
	public void getPlaylistPromotionsForGenre (String genreID){
		currentCall = MusicCatalogResponseType.ResponseTypePlaylistPromotion;
		getPlaylistPromotionsForGenreInternal(genreID);
//		Log.d(TAG, "playlits promotion for genre: "+genreID);
	}

	private void getPlaylistPromotionsForGenreInternal(String genreID) {
		MXGetPlaylistPromotionsRequest mx_request = new MXGetPlaylistPromotionsRequest(genreID);
		sendRequest(mx_request);
	}
	
	public void getPromotionsForGenre(String genreID){
		getPlaylistPromotionsForGenre(genreID);
		getAlbumPromotionsForGenre(genreID);
		getMediaPromotionsForGenre(genreID);
	}
	
	public void getMainPromotions(){
		palylistGeneralPromotionReceived = false;
		albumGeneralPromotionReceived = false;
		mediaGeneralPromotionReceived = false;
		
		getPlaylistPromotionsForGenreInternal(null);
		palylistGeneralPromotionReceived = true;
		
		currentCall = MusicCatalogResponseType.ResponseTypeGeneralPromotion;
	}
	
	public void getMediasForAlbum(String albumID){
		currentCall = MusicCatalogResponseType.ResponseTypeMediaPromotion;
		MXGetAlbumMediasRequest mx_request = new MXGetAlbumMediasRequest(albumID);
		sendRequest(mx_request);
	}
	
	public void getMediasForPlaylist(String playlistID){
		currentCall = MusicCatalogResponseType.ResponseTypeMediaPromotion;
		MXGetPlaylistMediasRequest mx_request = new MXGetPlaylistMediasRequest(playlistID);
		sendRequest(mx_request);
	}
	
	public void getGenres(){
		MXGetGenresRequest mx_request = new MXGetGenresRequest();
		currentCall = MusicCatalogResponseType.ResponseTypeAllGenres;
		sendRequest(mx_request);
	}
	
	public void getAlbumsByArtist(String artistID){
		MXGetAlbumsForArtistRequest mx_request = new MXGetAlbumsForArtistRequest(artistID);
		currentCall = MusicCatalogResponseType.ResponseTypeSearchedAlbums;
		sendRequest(mx_request);
	}
	
	public void setDelegate(IMXUIMusicCatalogCallback callback){
		this.callback = callback;
	}
	
	public MXStoreSearchRequest searchArtistName(String searchText){
		currentCall = MusicCatalogResponseType.ResponseTypeSearchedArtists;
		MXStoreSearchRequest mx_request = new MXStoreSearchRequest(searchText, SDStoreSearchType.SDStoreSearchTypeArtists);
		sendRequest(mx_request);
		searchedArtists = new ArrayList<MusicEntity>();
		return mx_request;
	}
	
	public MXStoreSearchRequest searchAlbumByName(String searchText){
		currentCall = MusicCatalogResponseType.ResponseTypeSearchedAlbums;
		MXStoreSearchRequest mx_request = new MXStoreSearchRequest(searchText, SDStoreSearchType.SDStoreSearchTypeAlbums);
		sendRequest(mx_request);
		promotedAlbums = new ArrayList<MusicEntity>();
		return mx_request;
	}

	public MXStoreSearchRequest searchMediaByName(String searchText){
		currentCall = MusicCatalogResponseType.ResponseTypeSearchedMedias;
		MXStoreSearchRequest mx_request = new MXStoreSearchRequest(searchText, SDStoreSearchType.SDStoreSearchTypeSongs);
		sendRequest(mx_request);
		promotedSongs = new ArrayList<MusicEntity>();
		return mx_request;
	}
	
	public MXStoreSearchRequest searchAgain(MXStoreSearchRequest mx_request, boolean forwardSearch){
		if(forwardSearch)
			mx_request.setPageNumber(mx_request.getPageNumber()+1);
		else
			mx_request.setPageNumber(mx_request.getPageNumber()-1);
		switch(mx_request.getSearchType()){
			case SDStoreSearchTypeArtists:
				currentCall = MusicCatalogResponseType.ResponseTypeSearchedArtists;
				searchedArtists = new ArrayList<MusicEntity>();
				break;
			case SDStoreSearchTypeAlbums:
				currentCall = MusicCatalogResponseType.ResponseTypeSearchedAlbums;
				promotedAlbums = new ArrayList<MusicEntity>();
				break;
			case SDStoreSearchTypeSongs:
				currentCall = MusicCatalogResponseType.ResponseTypeSearchedMedias;
				promotedSongs = new ArrayList<MusicEntity>();
				break;
		}
		sendRequest(mx_request);
		return mx_request;
	}

	private void sendRequest(MXStoreRequest mx_request) {
		if(mSettings.getAppMode()==MXAppMode.MXAppModeOffline){
			responseFailed(MXCommError.MxCommErrorNetwork);
			return;
		}
		mSender.SendRequest(mx_request, this);
		numberOfPendingRequests++;
	}

	@Override
	public void imagesReceived(HashMap images) {
		// TODO Auto-generated method stub
		Log.d(TAG, "imageReceived() called. ");
	}

	@Override
	public void responseFailed(MXCommError error) {
		Log.d(TAG, "responseFailed");
		numberOfPendingRequests--;
		
		if(error.equals(MXCommError.MxCommErrorNetwork)){
			////network error - notify user
			if(callback != null){
				callback.requestFailedNetworkError();
			}
		}
		else{
			////xml error - notify user of internal mistake
			if(callback != null){
				callback.requestFailedInternalError();
			}
		}
	}

	@Override
	public void responseReceived(String XMLResponse) {
		Log.d(TAG, "responseReceived");
		numberOfPendingRequests--;
		try{	

			parseResponse(XMLResponse);
			//send callback
			////notify UI of completion
			if(numberOfPendingRequests < 1){
				if(currentCall==MusicCatalogResponseType.ResponseTypeGeneralPromotion && !albumGeneralPromotionReceived){
					getAlbumPromotionsForGenreInternal(null);
					albumGeneralPromotionReceived = true;
					return;
				}
				if(currentCall==MusicCatalogResponseType.ResponseTypeGeneralPromotion && !mediaGeneralPromotionReceived){
					getMediaPromotionsForGenreInternal(null);
					mediaGeneralPromotionReceived = true;
					return;
				}
				Log.d(TAG, "lets notify the UI");
				if(callback == null) Log.d(TAG, "callback is null");
				if(currentCall == null) Log.d(TAG, "currentCall is null");
				callback.responseReceived(currentCall);
			}
		}

		catch (Exception e) {			
			Log.e(TAG, "responseReceived Exception "+e);
			e.printStackTrace();
			///notify UI of error
		}
	}
	
	private void parseResponse(String XMLResponse){
		Log.d(TAG, "parseResponse. numberofPendingRequests:  "+numberOfPendingRequests);
		writeToFile(XMLResponse);
		
		try{
			buildTagsIndex(XMLResponse);
		}catch (Exception e) {			
			Log.e(TAG, "buildTagsIndex failed");
			throw new RuntimeException(e);
		}

//		Log.d(TAG, "buildTagsIndex ok");
		
		try{
			if (artistsNodeStartIndex>0 && artistsNodeEndIndex>artistsNodeStartIndex)
				parseArtistsNode(XMLResponse.substring(artistsNodeStartIndex, artistsNodeEndIndex));

			if (albumsNodeStartIndex>0 && albumsNodeEndIndex>albumsNodeStartIndex)
				parseAlbumsNode(XMLResponse.substring(albumsNodeStartIndex, albumsNodeEndIndex+albumsTagEnd.length()));
			else if(currentCall.equals(MusicCatalogResponseType.ResponseTypeAlbumPromotion)){
				promotedAlbums = new ArrayList<MusicEntity>();
			}

			if (genresNodeStartIndex>0 && genresNodeEndIndex>genresNodeStartIndex)
				parseGenresNode(XMLResponse.substring(genresNodeStartIndex, genresNodeEndIndex));

			if (mediasNodeStartIndex>0 && mediasNodeEndIndex>mediasNodeStartIndex)
				parseMediasNode(XMLResponse.substring(mediasNodeStartIndex, mediasNodeEndIndex));
			else if(currentCall.equals(MusicCatalogResponseType.ResponseTypeMediaPromotion)){
				promotedSongs = new ArrayList<MusicEntity>();
			}
			 
			if (playlistsNodeStartIndex>0 && playlistsNodeEndIndex>playlistsNodeStartIndex)
				parsePlaylistsNode(XMLResponse.substring(playlistsNodeStartIndex, playlistsNodeEndIndex));
			else if(currentCall.equals(MusicCatalogResponseType.ResponseTypePlaylistPromotion)){
				promotedPlaylists = new ArrayList<MusicEntity>();
			}

			if (totalItemsSearchNodeStartIndex>0 && totalItemsSearchNodeEndIndex>totalItemsSearchNodeStartIndex)
				parseTotalSearchNode(XMLResponse.substring(totalItemsSearchNodeStartIndex, totalItemsSearchNodeEndIndex+totalItemsTagEnd.length()));

		}catch (Exception e) {			
			Log.e(TAG, "parse XMLResponse failed");
			throw new RuntimeException(e);
		}
	}
	
	public void buildTagsIndex(String xml) 
	{
//		int end=xml.length();
		try{
			albumsNodeEndIndex=xml.lastIndexOf(albumsTagEnd);
			if(albumsNodeEndIndex>0){
				albumsNodeStartIndex=xml.lastIndexOf(albumsTagStart);	
			}
			
			mediasNodeEndIndex=xml.lastIndexOf(mediasTagEnd);
			if(mediasNodeEndIndex>0){
				mediasNodeStartIndex=xml.lastIndexOf(mediasTagStart);	
			}
			
			genresNodeEndIndex=xml.lastIndexOf(genresTagEnd);
			if(genresNodeEndIndex>0){
				genresNodeStartIndex=xml.lastIndexOf(genresTagStart);
			}

			playlistsNodeEndIndex=xml.lastIndexOf(playlistsTagEnd);
			if(playlistsNodeEndIndex>0){
				playlistsNodeStartIndex=xml.lastIndexOf(playlistsTagStart);
			}

			artistsNodeEndIndex=xml.lastIndexOf(artistsTagEnd);
			if(artistsNodeEndIndex>0){
				artistsNodeStartIndex=xml.lastIndexOf(artistsTagStart);
			}
			
			totalItemsSearchNodeEndIndex=xml.lastIndexOf(totalItemsTagEnd);
			if(totalItemsSearchNodeEndIndex>0){
				totalItemsSearchNodeStartIndex=xml.lastIndexOf(totalItemsTagStart);
			}

		}catch (Exception e) {			
			Log.e(TAG, "Exception: ", e);
			throw  new IllegalArgumentException("Unable to buildTagsIndex" );
		}
	}
	
	/*
	 * <artist id="Z45343" name="Metallica" />
	 */
	public void parseArtistsNode(String xmlString) 
	{
		String artistId=null;
		String artistName=null;
		searchedArtists = new ArrayList<MusicEntity>();//// renew the list of artists

		Log.d(TAG, "parseArtistsNode");
		Log.d(TAG, xmlString);
		try {
			parser.setInput( new StringReader ( xmlString) );
			int eventType = parser.getEventType();
			boolean done = false;

			while (eventType != XmlPullParser.END_DOCUMENT && !done)
			{
				String name = null;
				switch (eventType){
					case XmlPullParser.START_DOCUMENT:
						break;
					case XmlPullParser.START_TAG:
						name = parser.getName();
						if (name.equalsIgnoreCase(artistTag)){  
							int attrcount;
							if((attrcount=parser.getAttributeCount())>0)     {        		    		
								for (int i=0 ; i<attrcount ;i++ )		{
									String attrName = parser.getAttributeName(i);
									String attrValue = parser.getAttributeValue(i);
									if(attrName.equalsIgnoreCase(idAttr))
										artistId=attrValue;
									else  if(attrName.equalsIgnoreCase(nameAttr))
										artistName=attrValue;
								}		    			
							} 
						}							

						break;
					case XmlPullParser.END_TAG:

						name = parser.getName();				 
						//	Log.d(LOGTAG, "END_TAG "+name);

						if((name.equalsIgnoreCase(artistTag)) )
						{
							////create new artist - put into ArrayList
							if (artistId!=null && artistName!=null){
								Log.d(TAG, "artist id and name not null");
								MusicEntity artist = new MusicEntity(artistId, artistName, EntityType.Artist);
								searchedArtists.add(artist);
							}
							else{
								Log.d(TAG, "either artist id or name is null. severe error.");
							}
						}
						else if((name.equalsIgnoreCase(artistsTag)) )
							done=true; //// finishing processing of artists
						break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			Log.d(TAG, "some kind of Exception "+e);

			throw new RuntimeException(e);
		}
	}
	
	/*
	 * <album id="Z12345" name="And justice for all" artistName="Metallica" />
	 * The artist name will be only if there is one artist for the album. Otherwise display "Collection"
	 */
	public void parseAlbumsNode(String xmlString)
	{
		String albumId=null;
		String albumName=null;
		String albumArtistName=null;
		promotedAlbums = new ArrayList<MusicEntity>();

		Log.d(TAG, "parseAlbumsNode");
		Log.d(TAG, xmlString);


		try {
			parser.setInput( new StringReader ( xmlString) );
			int eventType = parser.getEventType();
			boolean done = false;

			while (eventType != XmlPullParser.END_DOCUMENT && !done)
			{
//				Log.d(TAG, "eventType: "+eventType);
				String name = null;
				switch (eventType){
					case XmlPullParser.START_DOCUMENT:
						break;
					case XmlPullParser.START_TAG:
						name = parser.getName();
//						Log.d(TAG, "START_TAG "+name);
	
						if (name.equalsIgnoreCase(albumTag)){  
							int attrcount;
							if((attrcount=parser.getAttributeCount())>0)     {        		    		
								for (int i=0 ; i<attrcount ;i++ )		{
									String attrName = parser.getAttributeName(i);
									String attrValue = parser.getAttributeValue(i);
									if(attrName.equalsIgnoreCase(idAttr))
										albumId=attrValue;
									else  if(attrName.equalsIgnoreCase(nameAttr))
										albumName=attrValue;
									else  if(attrName.equalsIgnoreCase(artistNameAttr))
										albumArtistName=attrValue;
								}		    			
							}
						}
						break;
					case XmlPullParser.END_TAG:
						name = parser.getName();
						if((name.equalsIgnoreCase(albumTag)) )
						{
						////create new album - put into ArrayList
							if (albumId!=null && albumName!=null){
//								Log.d(TAG, "album id and album name is not null.");
								MusicEntity album = new MusicEntity(albumId, albumName, EntityType.Album);
								if (albumArtistName!=null){
									Log.d(TAG, "artist id not null, name is null - display \"Collection\" ");
									album.setArtistName(albumArtistName); 
								}
								promotedAlbums.add(album);
							}
							else{
								Log.d(TAG, "either album id or album name is null. severe error.");
							}
//							Log.d(TAG, "album tag identified: "+albumName);
						}
						else if((name.equalsIgnoreCase(albumsTag))){
							done=true  ;
//							Log.d(TAG, "done="+done);
						}
//						else{
//							Log.d(TAG, "tag name="+name);
//						}
						break;
				}
				eventType = parser.next();
			}
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * <genre id="Z54545" name="blahblahblah" />
	 */
	public void parseGenresNode(String xmlString) 
	{
		String genreID=null;
		String genreName=null;
		searchedGenres = new ArrayList<MusicEntity>();
		
		Log.d(TAG, "parseGenresNode");
		Log.d(TAG, xmlString);
		
		try {
			parser.setInput( new StringReader ( xmlString) );
			int eventType = parser.getEventType();
			boolean done = false;

			while (eventType != XmlPullParser.END_DOCUMENT && !done){
				String name = null;
				switch (eventType){
					case XmlPullParser.START_DOCUMENT:
						break;
					case XmlPullParser.START_TAG:
						name = parser.getName();
	
						if (name.equalsIgnoreCase(genreTag)){  
							int attrcount;
							if((attrcount=parser.getAttributeCount())>0)     {        		    		
								for (int i=0 ; i<attrcount ;i++ )		{
									String attrName = parser.getAttributeName(i);
									String attrValue = parser.getAttributeValue(i);
									if(attrName.equalsIgnoreCase(idAttr))
										genreID=attrValue;
									else  if(attrName.equalsIgnoreCase(nameAttr))
										genreName=attrValue;
								}		 
							}
						}
						break;
					case XmlPullParser.END_TAG:
						name = parser.getName();
						if((name.equalsIgnoreCase(genreTag)) )
						{
						////create new genre - put into ArrayList
							if (genreID!=null && genreName!=null){
//								Log.d(TAG, "genre id and name not null");
								MusicEntity genre = new MusicEntity(genreID, genreName, EntityType.Genre);
								searchedGenres.add(genre);
							}
							else{
								Log.d(TAG, "either genre id or name is null. severe error.");
							}
						}
	
						else if((name.equalsIgnoreCase(genresTag)) )
							done=true  ;
	
						break;
				}////end switch
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
//		Log.d(TAG, "genres parsed. size: "+searchedGenres.size());
	}
	
	/*
	 * <media id="Z44533" name="blahblahblah" artistName="blahblahblah" albumName="blahblahblah" albumId="285"
	 * length="195" genreName="blahblahblah" />
	 */
	public void parseMediasNode(String xmlString) throws SAXException
	{
		String mediaId=null;
		String mediaName=null;
		String artistName=null;
		String genreName=null;
		String albumName=null;
		String albumId=null;
		String length=null;
		
		promotedSongs = new ArrayList<MusicEntity>();
		
		Log.d(TAG, "parseMediasNode");
		Log.d(TAG, xmlString);
		
		try {
			parser.setInput( new StringReader ( xmlString) );
			int eventType = parser.getEventType();
			boolean done = false;

			while (eventType != XmlPullParser.END_DOCUMENT && !done){
				String name = null;
				switch (eventType){
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();
					//		Log.d(LOGTAG, "START_TAG "+name);

					if (name.equalsIgnoreCase(mediaTag)){  
						int attrcount;
						if((attrcount=parser.getAttributeCount())>0)     {        		    		
							for (int i=0 ; i<attrcount ;i++ )		{
								String attrName = parser.getAttributeName(i);
								String attrValue = parser.getAttributeValue(i);
								if(attrName.equalsIgnoreCase(idAttr))
									mediaId=attrValue;
								else  if(attrName.equalsIgnoreCase(nameAttr))
									mediaName=attrValue;
								else  if(attrName.equalsIgnoreCase(artistNameAttr))
									artistName=attrValue;
								else  if(attrName.equalsIgnoreCase(genreNameAttr))
									genreName=attrValue;
								else  if(attrName.equalsIgnoreCase(albumNameAttr))
									albumName=attrValue;
								else  if(attrName.equalsIgnoreCase(albumIdAttr))
									albumId=attrValue;
								else  if(attrName.equalsIgnoreCase(lengthAttr))
									length=attrValue;
							}		    			
						}
					}					

					break;
				case XmlPullParser.END_TAG:
					name = parser.getName();
					//	Log.d(LOGTAG, "END_TAG "+name);

					if((name.equalsIgnoreCase(mediaTag)) )
					{
						//// put the new song into array list
						if (mediaId != null && mediaName != null && albumName != null && artistName != null 
								&& genreName != null && length != null){
//							Log.d(TAG, "Song parameters are all present and not null");
							MusicEntity song = new MusicEntity(mediaId, mediaName, artistName, albumName, length, genreName, EntityType.Song);
							if(albumId != null && albumId.length()>0)
								song.setAlbumId(albumId);
							promotedSongs.add(song);
						}
						else{
							Log.d(TAG, "Song: one of the parameters is null. severe error.");
						}
					}
					if((name.equalsIgnoreCase(mediasTag)) )
						done=true  ;

					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * <playlist id="Z44544" name="And justice for all" />
	 */
	public void parsePlaylistsNode(String xmlString)
	{
		String playlistId=null;
		String playlistName=null;
		promotedPlaylists = new ArrayList<MusicEntity>();
		
		Log.d(TAG, "parsePlaylistsNode");
		Log.d(TAG, xmlString);
		
		try {
			parser.setInput( new StringReader ( xmlString) );
			int eventType = parser.getEventType();
			boolean done = false;

			while (eventType != XmlPullParser.END_DOCUMENT && done==false){

				String name = null;
				switch (eventType){
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();
					//	Log.d(LOGTAG, "START_TAG "+name);

					if (name.equalsIgnoreCase(playlistTag))
					{  
						int attrcount;
						if((attrcount=parser.getAttributeCount())>0)  {        		    		
							for (int i=0 ; i<attrcount ;i++ )		{
								String attrName = parser.getAttributeName(i);
								String attrValue = parser.getAttributeValue(i);

								if(attrName.equalsIgnoreCase(idAttr))
									playlistId=attrValue;		
								else  if(attrName.equalsIgnoreCase(nameAttr))
									playlistName=attrValue;									
							}
						}
					}
					break;

				case XmlPullParser.END_TAG:
					name = parser.getName();
					if((name.equalsIgnoreCase(playlistTag)) )  {
						////create playlist and put it into array list
						if (playlistId!=null && playlistName!=null){
//							Log.d(TAG, "playlist id and name not null");
							MusicEntity pl = new MusicEntity(playlistId, playlistName, EntityType.Playlist);
							promotedPlaylists.add(pl);
						}
						else{
							Log.d(TAG, "either playlist id or name is null. severe error.");
						}
					}
					else if((name.equalsIgnoreCase(playlistsTag)) )
						done=true  ;
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * <totalItemsInSearch>19</totalItemsInSearch> 
	 */
	public void parseTotalSearchNode(String xmlString) 
	{
		totalItemsInSearch = 0;

		Log.d(TAG, "parseTotalSearchNode");
		Log.d(TAG, xmlString);
		try {
			parser.setInput( new StringReader ( xmlString) );
			int eventType = parser.getEventType();
			boolean done = false;

			while (eventType != XmlPullParser.END_DOCUMENT && !done)
			{
				String name = null;
				switch (eventType){
					case XmlPullParser.START_DOCUMENT:
						break;
					case XmlPullParser.START_TAG:
						name = parser.getName();
//						if (name.equalsIgnoreCase(searchNumbersTag)){  
//							Log.d(TAG, "TEXT "+parser.getText());
//							totalItemsInSearch = Integer.parseInt(parser.getText());
//						}							

						break;
					
					case XmlPullParser.TEXT:
						Log.d(TAG, "TEXT "+parser.getText());
						totalItemsInSearch = Integer.parseInt(parser.getText());
						break;
					case XmlPullParser.END_TAG:

						name = parser.getName();				 
							Log.d(TAG, "END_TAG "+name);

//						if((name.equalsIgnoreCase(searchNumbersTag)) )
//						{
							////create new artist - put into ArrayList
//							if (artistId!=null && artistName!=null){
//								Log.d(TAG, "artist id and name not null");
//								MusicEntity artist = new MusicEntity(artistId, artistName, EntityType.Artist);
//								
//							}
//							else{
//								Log.d(TAG, "either artist id or name is null. severe error.");
//							}
							
//						}
						
						done=true; //// finishing processing of artists
						break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			Log.d(TAG, "some kind of Exception "+e);

			throw new RuntimeException(e);
		}
	}
	
	private void writeToFile(String XMLResponse){
		java.io.File f = new java.io.File(new java.io.File(Constants.Strings.MUSIX_PATH), "XML" + Constants.Strings.DOT + "xml");
		java.io.FileOutputStream fos = null;
		byte[] buffer = XMLResponse.getBytes();
		try {
			fos = new java.io.FileOutputStream(f, true);
			fos.write(buffer);
			fos.flush();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
    		try{
        		if(fos!= null)
        			fos.close();
    		}
    		catch(IOException ex){
    			Log.e(TAG, ex.getMessage());
    		}
    	}
		
	}
}

package il.co.pelephone.musix.comm;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import android.content.Context;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import il.co.pelephone.musix.UI.utility.Constants;
import il.co.pelephone.musix.comm.IMXSyncCallback.MXSyncError;
import il.co.pelephone.musix.comm.MXSettings.MXAppMode;
import il.co.pelephone.musix.data.MXLocalContentProviderMetadata;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class MXSync  implements IMXCommCallback {
	public static final String LOGTAG="MXSync";

	private static final  String DFLT_VERSION="1.1";

	private static final String responseTag = "response";
//	private static final String loginResponseTag = "loginResponse";
//	private static final String purchaseResponseTag = "purchaseResponse";	
//	private static final String syncTag = "sync";

	private static final String ConditionMsgTextTag = "ConditionMsgText";
	private static final String lineTag = "line";
	private static final String  joinMsgTextTag = "JoinMsgText";
	private static final String mediasTag = "medias";
	private static final String userMediaTag = "userMedia";

	private static final String albumsTag = "albums";
	private static final String albumTag = "album";
	private static final String playlistsTag = "playlists";
	private static final String userPlaylistTag = "userPlaylist";
	private static final String genresTag = "genres";
	private static final String genreTag = "genre";
	private static final String artistsTag = "artists";
	private static final String artistTag = "artist";

//	private static final String copyrightDeletionTag = "copyrightDeletion";
//	private static final String errorTag = "error";
//	private static final String clientDataResponseTag = "clientDataResponse";

	private static final String millisecondsAttr = "milliseconds";
//	private static final String 	encryptionKeyAttr = "encryptionKey";
//	private static final String 	isEncryptedAttr = "isEncrypted";

	private static final String idAttr = "id";
	private static final String nameAttr = "name";
	private static final String externalIdAttr = "externalId";
	private static final String mediaTypeAttr = "mediaType";
	private static final String artistIDAttr = "artistID";
	private static final String genreIDAttr = "genreID";
	private static final String albumIDAttr = "albumID";
	private static final String lengthAttr = "length";
	private static final String isSyncedAttr = "isSynced";
	private static final String deletedAttr = "deleted";
	private static final String sequenceAttr = "sequence";
	private static final String trackNumberAttr = "trackNumber";



	private static final String copyrightDeletionTagStart="<copyrightDeletion>";
	private static final  String copyrightDeletionTagEnd="</copyrightDeletion>";
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
	private static final String ConditionMsgTextTagStart="<ConditionMsgText>";
	private static final String ConditionMsgTextTagEnd="</ConditionMsgText>";

	private int copyrightDeletionNodeEndIndex=-1;
	private int copyrightDeletionNodeStartIndex=-1;	
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
	private int ConditionMsgTextNodeEndIndex=-1;
	private int ConditionMsgTextNodeStartIndex=-1;

	private String clientVersion;	
	private boolean isRegisterAccepted;
	private boolean isEncripted;
	private String upgradeURL;	
	private String timestamp;
	private String encriptionKey;
	private String mStatus;
	private String XMLResponse;

	private MXSyncRequest mRequest;	
	private IMXSyncCallback mCaller;	
	private MXCommLayer mSender;
	private XmlPullParser parser;
	private ArrayList <String> deleteQueue;
	private ArrayList <String> deleteList;
	private ArrayList <String> syncSongCopyrightDeletedList;
	private  ContentResolver localContentProvider;
	private MXSettings mSettings;

	private int newSongCount;

	private boolean responseSent;
	private boolean inMediasNode;
	private boolean inPlaylistsNode;
	private boolean inAlbumsNode;
	private boolean inCopyrightDeletionNode;

	private Cursor cur;
	
	private Context ctx;
	private boolean initialSync;

	//private Activity a;
	/*
	 * 	Constructor for initializing the class and sets the callback object.
	 */
	//public MXSync (IMXSyncCallback caller ,ContentResolver cp , Activity a) 
	public MXSync (IMXSyncCallback caller ,ContentResolver cp ,MXSettings mSettings ) 
	{
		Log.d(LOGTAG, "in constructor");

		clientVersion=DFLT_VERSION;
		this.mCaller=caller;
		isRegisterAccepted=false;
		upgradeURL=null;
		timestamp="";
		encriptionKey=null;
		isEncripted=false;
		mStatus="";
		XMLResponse=null;
		responseSent=false;
		//mRequest =new MXSyncRequest(clientVersion , isRegisterAccepted);
		//localContentProvider=new MusixLocalContentProvider();
		localContentProvider= cp;

		this.mSettings = mSettings;

		deleteList = new ArrayList<String>();
		syncSongCopyrightDeletedList= new ArrayList<String>();;	 
		inMediasNode=false;
		inPlaylistsNode=false;
		inAlbumsNode=false;
		inCopyrightDeletionNode=false;
		newSongCount=0;
		Cursor cur=null;
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			parser = factory.newPullParser();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}


		//		this.a=a;
		//a.startManagingCursor(cur);
	}

	/*
	 * 	Sends the specified request and parses the response to the provided request.
	 */
	public void sync(MXSyncRequest request)
	{
		initialSync = false;
		////check if in online mode
		if(mSettings.getAppMode()==MXAppMode.MXAppModeOffline){
			mCaller.syncFailed(MXSyncError.MXSyncErrorNetwork);
			return;
		}
		
//		Log.d(LOGTAG, "SendRequest start");
		mSender=MXCommLayer.getInstance(); //do it here for saving connection resources
		this.mRequest=request;
		mSender.SendRequest(mRequest, this);	
//		Log.d(LOGTAG, "after SendRequest");
	}

	public void sync()
	{
		
//		Log.d(LOGTAG, "SendRequest (this)start");
		mSender=MXCommLayer.getInstance(); //do it here for saving connection resources
		if(mRequest==null)
			mRequest =new MXSyncRequest(clientVersion , isRegisterAccepted);


		try{
			String dString=mRequest.getXML();
//			Log.d(LOGTAG, dString);
		}
		catch (Exception e)
		{
			Log.d(LOGTAG, "mRequest ");

		}



		//mSender.SendRequest(mRequest, this);	
//		Log.d(LOGTAG, "after SendRequest");
	}

	/*
	 * 	Used to handle the first sync response that was received by the MXLogin class. 
	 * When this method is called the class only needs to parse the provided response and does not initiate a sync request.
	 */
	public void sync(String XMLResponseStr)
	{
		Log.d(LOGTAG, "sync goto responseReceived");
		initialSync = true;
		responseReceived( XMLResponseStr) ;

	}





	@Override
	public void imagesReceived(HashMap images) {
	
	}

	@Override
	public void responseFailed(MXCommError error) {
	
		Log.d(LOGTAG, "responseFailed");

		if(error.equals(MXCommError.MxCommErrorNetwork))
			mCaller.syncFailed(MXSyncError.MXSyncErrorNetwork);
		else
			mCaller.syncFailed(MXSyncError.MXSyncErrorInvalidXML);
	}





	//CommLayer response

	@Override
	public void responseReceived(String XMLResponseStr) {
	
		//HandleResponse( XMLResponse);
		Log.d(LOGTAG, "responseReceived");
		Log.d(LOGTAG, XMLResponseStr);

		this.XMLResponse=XMLResponseStr;

		try{	

			parseSyncResponse(XMLResponse);
			//send callback
			mCaller.syncCompleted(newSongCount);
			//update response time
		}

		catch (Exception e) {			
			Log.e(LOGTAG, "responseReceived Exception "+e);
			mCaller.syncFailed(MXSyncError.MXSyncErrorInvalidXML);
		}
	}




	public void parseSyncResponse(String XMLResponse) 
	{	
		Log.d(LOGTAG, "parseSyncResponse");

		try{
			buildTagsIndex(XMLResponse);
		}catch (Exception e) {			
			Log.e(LOGTAG, "buildTagsIndex failed");
			throw new RuntimeException(e);
		}

//		Log.d(LOGTAG, "buildTagsIndex ok");

		try{

			if (ConditionMsgTextNodeStartIndex>0 && ConditionMsgTextNodeEndIndex>ConditionMsgTextNodeStartIndex) {
				parseConditionMsgTextNode(XMLResponse.substring(ConditionMsgTextNodeStartIndex, ConditionMsgTextNodeEndIndex));
			}
			
			if (artistsNodeStartIndex>0 && artistsNodeEndIndex>artistsNodeStartIndex)
				parseArtistsNode(XMLResponse.substring(artistsNodeStartIndex, artistsNodeEndIndex));
			if(initialSync)
				sendMessage(Constants.Integers.ALLARTISTPROCESSED);

			if (albumsNodeStartIndex>0 && artistsNodeEndIndex>albumsNodeStartIndex)
				parseAlbumsNode(XMLResponse.substring(albumsNodeStartIndex, albumsNodeEndIndex));
			if(initialSync)
				sendMessage(Constants.Integers.ALLALBUMSPROCESSED);
			
			if (genresNodeStartIndex>0 && genresNodeEndIndex>genresNodeStartIndex)
				parseGenresNode(XMLResponse.substring(genresNodeStartIndex, genresNodeEndIndex));
			if(initialSync)
				sendMessage(Constants.Integers.ALLGENRESPROCESSED);
			
			if (mediasNodeStartIndex>0 && mediasNodeEndIndex>mediasNodeStartIndex)
				parseMediasNode(XMLResponse.substring(mediasNodeStartIndex, mediasNodeEndIndex));
			if(initialSync)
				sendMessage(Constants.Integers.ALLSONGSPROCESSED); 
			
			if (playlistsNodeStartIndex>0 && playlistsNodeEndIndex>playlistsNodeStartIndex)
				parsePlaylistsNode(XMLResponse.substring(playlistsNodeStartIndex, playlistsNodeEndIndex));
			if(initialSync)
				sendMessage(Constants.Integers.ALLPLAYLISTSPROCESSED);

			deleteMedias();

			mCaller.syncSongsCopyrightDeleted((String [])syncSongCopyrightDeletedList.toArray(new String [syncSongCopyrightDeletedList.size()]));


			fetchResponseTimeStamp(XMLResponse);

		}catch (Exception e) {			
			Log.e(LOGTAG, "parse XMLResponse failed");
			throw new RuntimeException(e);
		}

	}



	void fetchResponseTimeStamp(String XMLResponse)
	{
//		Log.d(LOGTAG, "fetchResponseTimeStamp");

		try {

			parser.setInput( new StringReader ( XMLResponse) );
			int eventType = parser.getEventType();

			// auto-detect the encoding from the stream
			boolean done = false;
			while (eventType != XmlPullParser.END_DOCUMENT && !done){
				String name = null;
				switch (eventType){
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase(responseTag)){      	//getAttributes
						int attrcount;
						if((attrcount=parser.getAttributeCount())>0)     {        		    		
							for (int i=0 ; i<attrcount ;i++ )		{
								String attrName = parser.getAttributeName(i);
								String attrValue = parser.getAttributeValue(i);
								if(attrName.equalsIgnoreCase(millisecondsAttr));
								timestamp=attrValue;
								done=true;
							}		    			
						} 
					}                   
					break;
				case XmlPullParser.END_TAG:
					name = parser.getName();
					if((name.equalsIgnoreCase(responseTag)) )
						done=true;
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}


	public void buildTagsIndex(String xml) 
	{
		int end=xml.length();
		try{
			copyrightDeletionNodeEndIndex=xml.lastIndexOf(copyrightDeletionTagEnd);
			if(copyrightDeletionNodeEndIndex>0){
				copyrightDeletionNodeStartIndex=xml.lastIndexOf(copyrightDeletionTagStart, copyrightDeletionNodeEndIndex);	
				end=copyrightDeletionNodeStartIndex;
			}


			artistsNodeEndIndex=xml.lastIndexOf(artistsTagEnd, end);
			if(artistsNodeEndIndex>0){
				artistsNodeStartIndex=xml.lastIndexOf(artistsTagStart, artistsNodeEndIndex);
				end=artistsNodeStartIndex;
				Log.d(LOGTAG, "artistsNodeEndIndex: " + artistsNodeEndIndex +
							  ", artistsNodeStartIndex: " + artistsNodeStartIndex);
			}



			genresNodeEndIndex=xml.lastIndexOf(genresTagEnd, end);
			if(genresNodeEndIndex>0){
				genresNodeStartIndex=xml.lastIndexOf(genresTagStart, genresNodeEndIndex);
				end=genresNodeStartIndex;
				Log.d(LOGTAG, "genresNodeEndIndex: " + genresNodeEndIndex +
						  ", genresNodeStartIndex: " + genresNodeStartIndex);
			}


			albumsNodeEndIndex=xml.lastIndexOf(albumsTagEnd, end);
			if(albumsNodeEndIndex>0){
				albumsNodeStartIndex=xml.lastIndexOf(albumsTagStart, albumsNodeEndIndex);	
				end=albumsNodeStartIndex;
			}



			playlistsNodeEndIndex=xml.lastIndexOf(playlistsTagEnd, end);
			if(playlistsNodeEndIndex>0){
				playlistsNodeStartIndex=xml.lastIndexOf(playlistsTagStart, playlistsNodeEndIndex);
				end=playlistsNodeStartIndex;
			}


			mediasNodeEndIndex=xml.lastIndexOf(mediasTagEnd, end);
			if(mediasNodeEndIndex>0){
				mediasNodeStartIndex=xml.lastIndexOf(mediasTagStart, mediasNodeEndIndex);	
				end=mediasNodeStartIndex;
			}				

			ConditionMsgTextNodeEndIndex=xml.lastIndexOf(ConditionMsgTextTagEnd, end);
			if(ConditionMsgTextNodeEndIndex>0){
				ConditionMsgTextNodeStartIndex=xml.lastIndexOf(ConditionMsgTextTagStart, ConditionMsgTextNodeEndIndex);	
				end=ConditionMsgTextNodeStartIndex;
			}				

		}catch (Exception e) {			
			Log.e(LOGTAG, "IOException: ", e);
			throw  new IllegalArgumentException("Unable to buildTagsIndex" );
		}
	}



	//<artist id="Z45355" name="Prince">
	public void parseArtistsNode(String xmlString) 
	{
		String artistId=null;
		String artistName=null;

		Log.d(LOGTAG, "parseArtistsNode");
		Log.d(LOGTAG, xmlString);

		try {
			parser.setInput( new StringReader ( xmlString) );
			int eventType = parser.getEventType();
			boolean done = false;

			while (eventType != XmlPullParser.END_DOCUMENT && !done)
			{
				//Log.d(LOGTAG, "eventType "+eventType);

				String name = null;
				switch (eventType){
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();

						Log.d(LOGTAG, "START_TAG "+name);


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
						Log.d(LOGTAG, "END_TAG "+name);

					if((name.equalsIgnoreCase(artistTag)) )
					{
						//check if exists in contentProvider. if not exist - insert
						if (artistId!=null && artistName!=null){
//							Log.d(LOGTAG, "before query artist "+artistId);
							//String[] selectionArgs
							String selectionArgs[] = new String[] { artistId };
							cur = localContentProvider.query(
									MXLocalContentProviderMetadata.ArtistsTableMetaData.CONTENT_URI, null, 
									MXLocalContentProviderMetadata.ArtistsTableMetaData.ARTIST_CATALOGARTISTID+"=?", selectionArgs, null);

//							Log.d(LOGTAG, "after query artist "+artistId);


							if( cur.getCount()<=0){
								Log.d(LOGTAG, "insert artist "+artistId);

								ContentValues values = new ContentValues();
								values.put( MXLocalContentProviderMetadata.ArtistsTableMetaData.ARTIST_CATALOGARTISTID    , artistId);
								values.put( MXLocalContentProviderMetadata.ArtistsTableMetaData.ARTIST_NAME    , artistName);

//								Log.d(LOGTAG, "insert artist "+artistId);
								Uri rUri=localContentProvider.insert(MXLocalContentProviderMetadata.ArtistsTableMetaData.CONTENT_URI, values);
								//Log.d(LOGTAG, "after insert artist "+artistId );
								Log.d(LOGTAG, "after insert artist respUri= "+rUri );


							}
							else {
								Log.d(LOGTAG, " artist "+artistId + " "+artistName +" already exists");
							}
							if (cur!=null)
								cur.close();
						}
					else	if((name.equalsIgnoreCase(artistsTag)) )
						done=true  ;

					break;
					}
					
				}
				eventType = parser.next();
			}
		}
		catch (Exception e) {
			Log.d(LOGTAG, "insert artist Exception "+e);

			throw new RuntimeException(e);
		}
		finally
		{
			if (cur!=null)
				cur.close();
		}
	}




	//<album id="Z45674" name="Moon" artistID="Z3443">
	//<medias><userMedia id="2" /><medias>
	public void parseAlbumsNode(String xmlString) throws SAXException
	{
		String albumId=null;
		String albumName=null;
		String albumArtistId=null;
		String dbAlbumId=null;
		Log.d(LOGTAG, "parseAlbumsNode");
//		Log.d(LOGTAG, "parseAlbumsNode: " + xmlString);


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
					//	Log.d(LOGTAG, "START_TAG "+name);

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
								else  if(attrName.equalsIgnoreCase(artistIDAttr))
									albumArtistId=attrValue;
							}		    			
						} 
					}							

					break;
				case XmlPullParser.END_TAG:
					name = parser.getName();
					//	Log.d(LOGTAG, "END_TAG "+name);

					if((name.equalsIgnoreCase(albumTag)) )
					{
						//check if albumId exists in contentProvider. if not exist - insert
						if (albumId!=null && albumName!=null){

							String selectionArgs[] = new String[] { albumId };
							cur = localContentProvider.query(
									MXLocalContentProviderMetadata.AlbumsTableMetaData.CONTENT_URI, null, 
									MXLocalContentProviderMetadata.AlbumsTableMetaData.ALBUM_CATALOGALBUMID+"= ? ", selectionArgs, null);

							if(cur==null || cur.getCount()<=0)
							{
								ContentValues values = new ContentValues();
								values.put( MXLocalContentProviderMetadata.AlbumsTableMetaData.ALBUM_CATALOGALBUMID    , albumId);
								values.put( MXLocalContentProviderMetadata.AlbumsTableMetaData.ALBUM_ALBUMNAME    , albumName);
								if(albumArtistId!=null)
									values.put( MXLocalContentProviderMetadata.AlbumsTableMetaData.ALBUM_ARTISTID    , albumArtistId);
								Log.d(LOGTAG, "before insert albumId="+albumId + " albumName=" +albumName + " albumArtistId="+albumArtistId);
								Uri rUri=localContentProvider.insert(MXLocalContentProviderMetadata.AlbumsTableMetaData.CONTENT_URI, values);
								dbAlbumId=rUri.getLastPathSegment();
//								Log.d(LOGTAG, "after insert album _id=" +dbAlbumId + " rUri="+rUri);

							}
//							else
//								Log.d(LOGTAG, " albumId "+albumId + " "+albumName +" already exists");
							
							if (cur!=null)
								cur.close();
						}
					}
					if((name.equalsIgnoreCase(albumsTag)) )
						done=true  ;

					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally
		{
			if (cur!=null)
				cur.close();
		}
	}








	/*
	 * <genre id="Z75543" name="blyablya">
<artists><artist id="Z24343" /><artist id="Z45677" /></artists> </genre>
	 */
	public void parseGenresNode(String xmlString) throws SAXException
	{
		String genreID=null;
		String genreName=null;
		String artistID=null;
		String dbArtistId=null;
		String dbGenreId=null;


		Log.d(LOGTAG, "parseGenresNode");
//		Log.d(LOGTAG, "parseGenresNode "+xmlString);

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
					//	Log.d(LOGTAG, "START_TAG "+name);

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

						String selectionArgs[] = new String[] { genreID };
						String projection [] = new String[]{MXLocalContentProviderMetadata.GenresTableMetaData._ID}; //dbGenreId
						cur = localContentProvider.query(
								MXLocalContentProviderMetadata.GenresTableMetaData.CONTENT_URI, projection, 
								MXLocalContentProviderMetadata.GenresTableMetaData.GENRE_CATALOGGENREID+"= ? ", selectionArgs, null);

						if(cur==null || cur.getCount()<=0)
						{
							ContentValues values = new ContentValues();
							values.put( MXLocalContentProviderMetadata.GenresTableMetaData.GENRE_CATALOGGENREID    , genreID);
							values.put( MXLocalContentProviderMetadata.GenresTableMetaData.GENRE_NAME    , genreName);			
//							Log.d(LOGTAG, "before insert genreID "+genreID);
							Uri rUri=localContentProvider.insert(MXLocalContentProviderMetadata.GenresTableMetaData.CONTENT_URI, values);

							if(rUri!=null)
							{
								dbGenreId=rUri.getLastPathSegment();
//								Log.d(LOGTAG, "after insert genreID "+genreID);
							}
							else
							{
								Log.d(LOGTAG, "FAIL insert genreID "+genreID);
								throw new RuntimeException("FAIL insert genreID");
							}

						}
						else
						{
							Log.d(LOGTAG, " genreID "+genreID + " "+genreName +" already exists");
							if (cur.moveToFirst()) 
								dbGenreId=cur.getString(cur.getColumnIndex(MXLocalContentProviderMetadata.GenresTableMetaData._ID));
							if(dbGenreId==null) {
								Log.d(LOGTAG, "got null Genre _ID from CP");
								throw new RuntimeException("got null Genre _ID from CP");
							}
						}
						if (cur!=null)
							cur.close();
					}
					else if (name.equalsIgnoreCase(artistTag)){  
						int attrcount1;
						if((attrcount1=parser.getAttributeCount())>0)     {        		    		
							for (int i=0 ; i<attrcount1 ;i++ )		{
								String attrName = parser.getAttributeName(i);
								String attrValue = parser.getAttributeValue(i);
								if(attrName.equalsIgnoreCase(idAttr))
									artistID=attrValue;
							}		    			
						} 
					}							

					break;
				case XmlPullParser.END_TAG:
					name = parser.getName();
					//Log.d(LOGTAG, "END_TAG "+name);

					if((name.equalsIgnoreCase(artistTag)) )
					{	//update  genre of artist for this genre
						boolean toUpdate=false;
						String columns[] = new String[] { artistID };

						String projection[] = new String[] { MXLocalContentProviderMetadata.ArtistsTableMetaData._ID , 
								MXLocalContentProviderMetadata.ArtistsTableMetaData.ARTIST_GENREID};

//						Log.d(LOGTAG, "check artist " +artistID + " ,genreID "+genreID);

						cur = localContentProvider.query(
								MXLocalContentProviderMetadata.ArtistsTableMetaData.CONTENT_URI, null ,
								MXLocalContentProviderMetadata.ArtistsTableMetaData.ARTIST_CATALOGARTISTID+"= ?", columns, null);

						if(cur==null || cur.getCount()<=0 )
							toUpdate=false;									
						else  if (cur.moveToFirst()) {
							String  tempGenre = cur.getString(cur.getColumnIndex(MXLocalContentProviderMetadata.ArtistsTableMetaData.ARTIST_GENREID));
							dbArtistId = cur.getString(cur.getColumnIndex(MXLocalContentProviderMetadata.ArtistsTableMetaData._ID));

							if ( ( tempGenre==null || !tempGenre.equalsIgnoreCase(dbGenreId)) && dbArtistId!=null )
								toUpdate=true;
						}

						if (cur!=null)
							cur.close();

						if (toUpdate==true	)
						{
							ContentValues values = new ContentValues();
							values.put( MXLocalContentProviderMetadata.ArtistsTableMetaData.ARTIST_GENREID  , dbGenreId);
//							Log.d(LOGTAG, "update dbArtistId=" +dbArtistId+" ,dbGenreId="+dbGenreId + " ,artistID="+artistID);
							int ires=localContentProvider.update(MXLocalContentProviderMetadata.ArtistsTableMetaData.CONTENT_URI , 
									values, MXLocalContentProviderMetadata.ArtistsTableMetaData._ID+"= ?" , new String[] { dbArtistId });

							if (ires<1)
							{
								Log.e(LOGTAG, "FAILED update dbGenreId=" + dbGenreId + " genreID="+genreID + " for dbArtistId"+dbArtistId);
								throw new RuntimeException();
							}

//							else
//								Log.d(LOGTAG, "after update dbGenreId "+dbGenreId + " for dbArtistId"+dbArtistId);

						}

					}

					else if((name.equalsIgnoreCase(genresTag)) )
						done=true  ;

					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally
		{
			if (cur!=null)
				cur.close();
		}
	}



	/*
	 * <userMedia id="1" mediaType="AUDIO" isSynced="1" name="Nothing else matters"
artistID="Z1764" genreID="Z65744" length="156" deleted="0" albumID="Z4343" />
	 */

	public void parseMediasNode(String xmlString) throws SAXException
	{
		String mediaId=null;
		String mediaName=null;
		String artistID=null;
		String genreID=null;
		String albumID=null;
		String mediaType=null;
		String length=null;
		String trackNumber=null;

		boolean toDelete=false;

		Log.d(LOGTAG, "parseMediasNode");
//		Log.d(LOGTAG, "parseMediasNode: "+xmlString);

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

					if (name.equalsIgnoreCase(userMediaTag)){  
						int attrcount;
						if((attrcount=parser.getAttributeCount())>0)     {        		    		
							for (int i=0 ; i<attrcount ;i++ )		{
								String attrName = parser.getAttributeName(i);
								String attrValue = parser.getAttributeValue(i);
								if(attrName.equalsIgnoreCase(idAttr))
									mediaId=attrValue;
								else  if(attrName.equalsIgnoreCase(nameAttr))
									mediaName=attrValue;
								else  if(attrName.equalsIgnoreCase(artistIDAttr))
									artistID=attrValue;
								else  if(attrName.equalsIgnoreCase(genreIDAttr))
									genreID=attrValue;
								else  if(attrName.equalsIgnoreCase(albumIDAttr))
									albumID=attrValue;
								else  if(attrName.equalsIgnoreCase(mediaTypeAttr))
									mediaType=attrValue;
								else  if(attrName.equalsIgnoreCase(lengthAttr))
									length=attrValue;
								else  if(attrName.equalsIgnoreCase(trackNumberAttr))
									trackNumber=attrValue;

								////this needs to be rethought
//								else  if(attrName.equalsIgnoreCase(isSyncedAttr))
//									toDelete = (attrValue=="0") ?true : false;
								else  if(attrName.equalsIgnoreCase(deletedAttr)){
									toDelete =( attrValue.equals("1") )? true : false;
//									Log.d(LOGTAG, "toDelete: "+toDelete);
								}

							}		    			
						} 
					}							

					break;
				case XmlPullParser.END_TAG:
					name = parser.getName();
					//	Log.d(LOGTAG, "END_TAG "+name);

					if((name.equalsIgnoreCase(userMediaTag)) )
					{

						if(toDelete/*==true*/)		{ 
							deleteList.add(mediaId);
//							Log.d(LOGTAG, "user media: added to deleteList: "+mediaId);
						}
						else		 {
							insertSong( mediaId, mediaName, artistID, genreID,	 albumID,	 mediaType,	 length ,trackNumber);
							//insertAlbumSong(mediaId , albumID , trackNumber);
						}

						toDelete=false;
					}
					if((name.equalsIgnoreCase(mediasTag)) )
						done=true  ;
					
					if(initialSync)
						sendMessage(Constants.Integers.ONESONGPROCESSED);

					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally
		{
			if (cur!=null)
				cur.close();
		}
	}

	//<album id="Z45674" name="Moon" artistID="Z3443">
	//<medias><userMedia id="2" /><medias>
	public void parseConditionMsgTextNode(String xmlString) throws SAXException
	{
		String termsOfService="";
		Log.d(LOGTAG, "parseConditionMsgTextNode");
//		Log.d(LOGTAG, "parseConditionMsgTextNode: " + xmlString);


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
					//	Log.d(LOGTAG, "START_TAG "+name);

					if (name.equalsIgnoreCase(ConditionMsgTextTag)){
						termsOfService = "";
					} else if((name.equalsIgnoreCase(lineTag)) ) {
						String line = parser.nextText();
						termsOfService += line + "\n";
					}
					break;
				case XmlPullParser.END_TAG:
					name = parser.getName();
					//	Log.d(LOGTAG, "END_TAG "+name);

					if((name.equalsIgnoreCase(ConditionMsgTextTag)) ) {
						done=true;
						mSettings.setTOS(termsOfService);
					}
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally
		{
			if (cur!=null)
				cur.close();
		}
	}



	void insertSong(String mediaId,String mediaName,String artistID,String genreID,	String albumID,	String mediaType,	String length , String trackNumber)
	{
		try{
			String selectionArgs[] = new String[] { mediaId };

			cur = localContentProvider.query(
					MXLocalContentProviderMetadata.SongsTableMetaData.CONTENT_URI, null, 
					MXLocalContentProviderMetadata.SongsTableMetaData.SONG_CATALOGMEDIAID+"= ? " , selectionArgs, null);

			if(cur==null || cur.getCount()<=0)
			{

//				Log.d(LOGTAG, "before insert mediaId "+mediaId + " mediaName=" + mediaName + " artistID="+artistID + " albumID =" +albumID+
//						" mediaType="+mediaType +  " length="+length  + " trackNumber="+trackNumber );

				ContentValues values = new ContentValues();

				if(mediaId!=null)
					values.put( MXLocalContentProviderMetadata.SongsTableMetaData.SONG_CATALOGMEDIAID    , mediaId);

				if(mediaName!=null)
					values.put( MXLocalContentProviderMetadata.SongsTableMetaData.SONG_NAME    , mediaName);

				if(artistID!=null)
					values.put( MXLocalContentProviderMetadata.ArtistsTableMetaData.ARTIST_CATALOGARTISTID    , artistID);

				if(genreID!=null)
					values.put( MXLocalContentProviderMetadata.GenresTableMetaData.GENRE_CATALOGGENREID    , genreID);

				if(albumID!=null)
					values.put( MXLocalContentProviderMetadata.AlbumsTableMetaData.ALBUM_CATALOGALBUMID    , albumID);

				//	if(mediaType!=null)
				//	values.put( MXLocalContentProviderMetadata.SongsTableMetaData.CONTENT_TYPE    , mediaType);
				values.put( MXLocalContentProviderMetadata.SongsTableMetaData.SONG_ADDED_TIMESTAMP, System.currentTimeMillis());

				if(length!=null)
					values.put( MXLocalContentProviderMetadata.SongsTableMetaData.SONG_DURATION    , length);

				if (trackNumber!=null)
					values.put( MXLocalContentProviderMetadata.AlbumSongsTableMetaData.ALBUMSONG_TRACKNUMBER   , trackNumber);


				//Log.d(LOGTAG, "before insert mediaId "+mediaId );

				if(localContentProvider.insert(MXLocalContentProviderMetadata.SongsTableMetaData.CONTENT_URI, values)==null)
				{
					Log.e(LOGTAG, "after insert mediaId="+mediaId+ "failed" );

				}
//				else
//					Log.d(LOGTAG, "after insert mediaId="+mediaId);


				newSongCount++;
			}
			else
			{
				Log.d(LOGTAG, " mediaId="+mediaId+ " name=" + mediaName+ " already exists" );

			}

			if (cur!=null)
				cur.close();
		}
		catch (Exception e) {
			Log.e(LOGTAG ,"Failed insertMedia " +mediaId + " error:" +e);
			throw new RuntimeException(e);
		}
		finally
		{
			if (cur!=null)
				cur.close();
		}

	}




	/*
	public void insertAlbumSong(String mediaId , String albumID , String trackNumber)
	{

		Log.d(LOGTAG, "before insertAlbumSong="+mediaId + " albumID=" + albumID + " trackNumber="+trackNumber );


		try{
			String selectionArgs[] = new String[] { mediaId  ,albumID ,trackNumber};

			 cur = localContentProvider.query(
					MXLocalContentProviderMetadata.AlbumSongsTableMetaData.CONTENT_URI, null, 
					MXLocalContentProviderMetadata.AlbumSongsTableMetaData.ALBUMSONG_ALBUMID+"= ? and "+
					MXLocalContentProviderMetadata.AlbumSongsTableMetaData.ALBUMSONG_SONGID+"= ? and " 	+
					MXLocalContentProviderMetadata.AlbumSongsTableMetaData.ALBUMSONG_TRACKNUMBER+"= ?" ,
					selectionArgs, null);         

			if(cur==null || cur.getCount()<=0)
			{			

				if (cur!=null)
					cur.close();

				cur = localContentProvider.query(
						MXLocalContentProviderMetadata.AlbumsTableMetaData.CONTENT_URI, 
						new String[] { MXLocalContentProviderMetadata.AlbumsTableMetaData._ID}, 
						MXLocalContentProviderMetadata.AlbumsTableMetaData.ALBUM_CATALOGALBUMID+"= ? " ,
						new String[] { albumID}	, null);

				String dbAlbumId=cur.getString(cur.getColumnIndex(MXLocalContentProviderMetadata.AlbumsTableMetaData._ID));

				ContentValues values = new ContentValues();

				if(albumID!=null)
					values.put( MXLocalContentProviderMetadata.AlbumSongsTableMetaData.ALBUMSONG_ALBUMID    , dbAlbumId);

				if(mediaId!=null)
					values.put( MXLocalContentProviderMetadata.AlbumSongsTableMetaData.ALBUMSONG_SONGID    , mediaId);


				if(trackNumber!=null)
					values.put( MXLocalContentProviderMetadata.AlbumSongsTableMetaData.ALBUMSONG_TRACKNUMBER    , trackNumber);

				//Log.d(LOGTAG, "before insert mediaId "+mediaId );

				if(localContentProvider.insert(MXLocalContentProviderMetadata.AlbumSongsTableMetaData.CONTENT_URI, values)==null)
				{
					Log.e(LOGTAG, "after insertAlbumSong ="+mediaId+ "failed" );

				}
				else
					Log.d(LOGTAG, "after insertAlbumSong ="+mediaId);

			}
			else
			{
				Log.d(LOGTAG, " insertAlbumSong:  mediaId="+mediaId +" already exists" );

			}

			if (cur!=null)
				cur.close();
		}
		catch (Exception e) {
			Log.e(LOGTAG ,"Failed insertMedia " +mediaId + " error:" +e);
			throw new RuntimeException(e);
		}
		finally
		{
			if (cur!=null)
				cur.close();
		}

	}



	 */


	public void deleteMedias()
	{


		String projection[] = new String[] { MXLocalContentProviderMetadata.SongsTableMetaData._ID };
		String selection = MXLocalContentProviderMetadata.SongsTableMetaData.SONG_CATALOGMEDIAID+"= ? " ;


//		Log.d(LOGTAG, "deleteMedias: projection[0]: "+projection[0]+"selection: "+selection);

		try{
//			Log.d(LOGTAG, "deleteMedias: list length: "+deleteList.size());
			for(String mediaId : deleteList)
			{
//				Log.d(LOGTAG, "deleteMedias: start of for loop ");
				String selectionArgs[] = new String[] { mediaId };

				cur = localContentProvider.query(
						MXLocalContentProviderMetadata.SongsTableMetaData.CONTENT_URI, projection, selection , selectionArgs, null);	

//				Log.d(LOGTAG, "deleteMedias: cursor initialized ");
				if(cur!=null && cur.getCount()>0) {		

					cur.moveToFirst();
					String dbMediaId = cur.getString(cur.getColumnIndex(MXLocalContentProviderMetadata.SongsTableMetaData._ID));
					String delArgs[] = new String[] { dbMediaId };
//					Log.d(LOGTAG, "before delete dbMediaId=" + dbMediaId + " mediaId="+mediaId);
					localContentProvider.delete(MXLocalContentProviderMetadata.SongsTableMetaData.CONTENT_URI , selection , selectionArgs);
//					Log.d(LOGTAG, "after delete mediaId="+mediaId);

				}
				if (cur!=null)
					cur.close();
			}
		}catch(Exception e){
			Log.e(LOGTAG , "Failed deleteMedias  "+e);
		}
		finally
		{
//			Log.d(LOGTAG, "end of delete media");
			if (cur!=null)
				cur.close();
		}
	}





	public void parsePlaylistsNode(String xmlString) throws SAXException
	{
		String userId=null;
		String catalogId=null;
		String playlistName=null;
		String mediaId=null;
		String trackNumber=null;
		boolean toDelete=false;
		String dbPlaylistsId=null;

		Log.d(LOGTAG, "parsePlaylistsNode");
		Log.d(LOGTAG, "parsePlaylistsNode" + xmlString);


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

					if (name.equalsIgnoreCase(userPlaylistTag))
					{  
						int attrcount;
						if((attrcount=parser.getAttributeCount())>0)  {        		    		
							for (int i=0 ; i<attrcount ;i++ )		{
								String attrName = parser.getAttributeName(i);
								String attrValue = parser.getAttributeValue(i);

								if(attrName.equalsIgnoreCase(idAttr))
									userId=attrValue;
								else  if(attrName.equalsIgnoreCase(deletedAttr))  
									toDelete=(attrValue.equals("1"))?true:false;
								else  if(attrName.equalsIgnoreCase(externalIdAttr))
									catalogId=attrValue;		
								else  if(attrName.equalsIgnoreCase(nameAttr))
									playlistName=attrValue;									
							}		    			
						} 

						String selectionArgs[] = new String[] { catalogId  };
						String projection[] = new String[] { MXLocalContentProviderMetadata.PlaylistTableMetaData._ID  };

						cur = localContentProvider.query(MXLocalContentProviderMetadata.PlaylistTableMetaData.CONTENT_URI, null,
								MXLocalContentProviderMetadata.PlaylistTableMetaData.PLAYLIST_CATALOGPLAYLISTID + "= ? ", 
								selectionArgs, null);

						if(toDelete == true )   {


							if(cur!=null && cur.getCount()>0){
								dbPlaylistsId = cur.getString(cur.getColumnIndex(MXLocalContentProviderMetadata.PlaylistTableMetaData._ID));
								String delArgs[] = new String[] { dbPlaylistsId  };

								localContentProvider.delete(MXLocalContentProviderMetadata.PlaylistTableMetaData.CONTENT_URI ,
										MXLocalContentProviderMetadata.PlaylistTableMetaData._ID + "= ? ", delArgs);
							}

						}
						else
						{
							if(cur==null || cur.getCount()<=0)
							{

								ContentValues values = new ContentValues();
								values.put( MXLocalContentProviderMetadata.PlaylistTableMetaData.PLAYLIST_CATALOGPLAYLISTID    , catalogId);
								values.put( MXLocalContentProviderMetadata.PlaylistTableMetaData.PLAYLIST_USERPLAYLISTID   , userId);
								values.put( MXLocalContentProviderMetadata.PlaylistTableMetaData.PLAYLIST_NAME    , playlistName);
//								Log.d(LOGTAG, "before insert Playlist catalogId="+catalogId +" userId="+userId+ " playlistName"+playlistName);

								Uri rUri=localContentProvider.insert(MXLocalContentProviderMetadata.PlaylistTableMetaData.CONTENT_URI, values);

								if(rUri!=null)
								{
//									Log.d(LOGTAG, "after insert Playlist rUri="+rUri );
									dbPlaylistsId = rUri.getLastPathSegment();
//									Log.d(LOGTAG, "after insert Playlist dbPlaylistsId="+dbPlaylistsId );

								}
								else
								{
									Log.e(LOGTAG, "FAILED inserting Playlist" );
									throw new RuntimeException("FAILED inserting Playlist");									
								}
							}

						}

						if (cur!=null)
							cur.close();
					}
					else 	if (name.equalsIgnoreCase(userMediaTag))
					{
						int attrcount;
						if((attrcount=parser.getAttributeCount())>0)  {        		    		
							for (int i=0 ; i<attrcount ;i++ )		{
								String attrName = parser.getAttributeName(i);
								String attrValue = parser.getAttributeValue(i);

								if(attrName.equalsIgnoreCase(idAttr))
									mediaId=attrValue;
								else 	if(attrName.equalsIgnoreCase(sequenceAttr))
									trackNumber=attrValue;
							}
						}
					}

					break;

				case XmlPullParser.END_TAG:
					name = parser.getName();
					//	Log.d(LOGTAG, "END_TAG "+name);
					//dbPlaylistsId
					if((name.equalsIgnoreCase(userMediaTag)) )  {

						if (dbPlaylistsId==null)
						{
							cur = localContentProvider.query(MXLocalContentProviderMetadata.PlaylistTableMetaData.CONTENT_URI, 
									new String[] { MXLocalContentProviderMetadata.PlaylistTableMetaData._ID  },
									MXLocalContentProviderMetadata.PlaylistTableMetaData.PLAYLIST_CATALOGPLAYLISTID + "= ? ", 
									new String[] { catalogId  }, null);
							
							
							if(cur.moveToFirst())		{
								dbPlaylistsId = cur.getString(cur.getColumnIndex(MXLocalContentProviderMetadata.PlaylistTableMetaData._ID));
							}
							else   {
//								Log.d(LOGTAG, "PlaylistsId not found in Playlists_table" );
								throw new RuntimeException("PlaylistsId not found in Playlists_table");
							}

						}
						
						if (cur!=null)
							cur.close();
						
						/************/
						String dbMediaId=null;

						cur = localContentProvider.query(
								MXLocalContentProviderMetadata.SongsTableMetaData.CONTENT_URI, 
								new String[] { MXLocalContentProviderMetadata.SongsTableMetaData._ID}, 
								MXLocalContentProviderMetadata.SongsTableMetaData.SONG_CATALOGMEDIAID +"= ? " ,
								new String[] { mediaId}	, null);

						if(cur.moveToFirst())		{
							dbMediaId=cur.getString(cur.getColumnIndex(MXLocalContentProviderMetadata.SongsTableMetaData._ID));
						}
						else   {
//							Log.d(LOGTAG, "Song not found in songs_table" );
							throw new RuntimeException("Song not found in songs_table");
						}

						if (cur!=null)
							cur.close();


						String selection = MXLocalContentProviderMetadata.PlaylistSongsTableMetaData.PLAYLISTSONG_PLAYLISTID
						+"= ? " + " and "+MXLocalContentProviderMetadata.PlaylistSongsTableMetaData.PLAYLISTSONG_SONGID+"= ? ";

						String selectionArgs[] = new String[] { catalogId , dbMediaId };

						cur = localContentProvider.query(MXLocalContentProviderMetadata.PlaylistSongsTableMetaData.CONTENT_URI, null, 
								selection, selectionArgs, null);

						if(cur==null || cur.getCount()<=0)
						{
							ContentValues values = new ContentValues();
							values.put( MXLocalContentProviderMetadata.PlaylistSongsTableMetaData.PLAYLISTSONG_PLAYLISTID    , dbPlaylistsId);
							values.put( MXLocalContentProviderMetadata.PlaylistSongsTableMetaData.PLAYLISTSONG_SONGID   , dbMediaId);
							values.put( MXLocalContentProviderMetadata.PlaylistSongsTableMetaData.PLAYLISTSONG_TRACKNUMBER    , trackNumber);
//							Log.d(LOGTAG, "insert PlaylistUserMedia dbPlaylistsId="+dbPlaylistsId +" dbMediaId="+dbMediaId+ " trackNumber"+trackNumber);

							Uri rUri=localContentProvider.insert(MXLocalContentProviderMetadata.PlaylistSongsTableMetaData.CONTENT_URI,
									values);
//							Log.d(LOGTAG, "after insert PlaylistUserMedia rUri="+rUri );
						}

						if (cur!=null)
							cur.close();
					}

					else if((name.equalsIgnoreCase(playlistsTag)) )
						done=true  ;
					toDelete=false;
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally
		{
			if (cur!=null)
				cur.close();
		}
	}





	public String getRequestAsString()
	{
		String xml=null;

		try{
			xml=this.mRequest.getXML();
		}
		catch (IOException e )
		{
			xml="IOException "+e.getMessage().toString();
		}


		return xml;
	}
	
	


	public String getResponseTimestamp()
	{
		return timestamp;		
	}
	
	public String getResponseEncriptionKey()
	{
		return encriptionKey;		
	}
	
	
	public String getResponseClientVersion()
	{
		return clientVersion;		
	}
	

	public String getResponseAsString()
	{
		return this.XMLResponse;
	}
	
	public void setContext(Context ctx){
		this.ctx = ctx;
	}

	private void sendMessage(int m){
		if(ctx == null){
    		Log.d(LOGTAG, "send message error, context == null: "+m);
    		return;
    	}
    		
    	String TAG = "sendMessage";
    	Intent i = new Intent(Constants.Strings.SYNC);

    	i.putExtra(Constants.Strings.MSG, m);
//    	Log.d(TAG, "Broadcasting intent: "+m);
    	ctx.sendBroadcast (i) ;
    }

}

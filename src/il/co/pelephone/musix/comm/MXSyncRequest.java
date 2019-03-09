package il.co.pelephone.musix.comm;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;


public class MXSyncRequest extends MXXMLRequest{

	private String clientVersion;
	private String timestamp;
	private boolean isRegisterAccepted;

	private ArrayList <String> purchasedMedias; //id
	private ArrayList <String> purchasedAlbums; //id
	private ArrayList <String> purchasedPlaylists; //id
	private ArrayList <String> deletedMedia; //id
	
	private HashMap <String,String> playedMedia; //id:count
	private HashMap <String,String> syncMedia; //<id>:<add/remove flag>
	
	
	public MXSyncRequest(String clientVersion , boolean isRegisterAccepted)
	{
		super();
		this.clientVersion=clientVersion;
		this.timestamp="000000";
		this.isRegisterAccepted=isRegisterAccepted;	
		
		this.purchasedMedias=new ArrayList <String>();
		this.purchasedAlbums=new ArrayList <String>();
		this.purchasedPlaylists=new ArrayList <String>();		
		this.deletedMedia=new ArrayList <String>();
		
		this.playedMedia=new HashMap <String,String>();		
		this.syncMedia=new HashMap <String,String>();

	}
	
	

	//Indicates the current client version.
	public String getClientVersion()	{
		return this.clientVersion;
	}
	
	
	public void setClientVersion(String clientVersion)	{
		this.clientVersion=clientVersion;		
	}
	
	
	//Indicates the current client version.
	public String getTimestamp()	{
		return this.timestamp;
	}
	
	
	public void setTimestamp(String timestamp)	{
		this.timestamp=timestamp;		
	}
	
	

	
	//Indicates whether or not the user accepted the TOC for the app.
	//Should be empty by
	//default and included in the XML only if it’s value is 1.
	public boolean isRegisterAccepted()	{
		return this.isRegisterAccepted;		
	}
		
	
	//Adds a media that was purchased by the user using the app to the sync request
	public void addMedia (String mediaID)	{
		this.purchasedMedias.add(mediaID);
	}
	
	//Adds an album that was purchased by the user using the app to the sync request
	public void addAlbum (String albumId)	{
		this.purchasedAlbums.add(albumId);		
	}
	
	
	//Adds a playlist that was purchased by the user using the app to the sync request
	public void addPlaylist (String playlistID)	{
		this.purchasedPlaylists.add(playlistID);
		
	} 
	
	
	//Adds a record of media deleted by the user using the app since the last sync.
	public void addDeletedMedia (String mediaID)	{
		this.deletedMedia.add(mediaID);			
	}
	
	
	
	//Adds a statistics record for a media that was played and how many times it was played
	//by the user since the last sync.
	public void addPlayedMedia (String mediaID, int playCount)	{
		this.playedMedia.put(mediaID, String.valueOf(playCount));
		
	}
	
	
	public void addSyncMedia (String mediaID, int statusFlag)	{
		this.syncMedia.put(mediaID, String.valueOf(statusFlag));
	}
		
	

	@Override
	public String getXML() throws IOException
	{			
		HashMap <String , String> att=new HashMap <String , String>();
		att.put("milliseconds", this.timestamp);
		MXXMLNode rootNode=new MXXMLNode("request" , null ,att );		
		super.setRootNode(rootNode);		
		
	/*****************************************
	 * Login will not be sent in SyncRequest
	 * ***************************************/
	 /*****************************************
	  *  att.clear();		
		if (isRegisterAccepted)	{				
			att.put("isRegisterAccepted", "1");
		}		
		att.put("version" , this.clientVersion);		
		MXXMLNode node=new MXXMLNode("login" , null ,att );
		root.addChild(node);
		******************************************/
		
		
		if ( this.purchasedMedias.size()>0  || 
				this.purchasedAlbums.size()>0	||
				this.purchasedPlaylists.size()>0
		)
		{	
			root.addChild(addPurchaseNode());
		}			
		
		if (this.playedMedia.size()>0)
		{	
			root.addChild(mapToNode( "plays" , "usermedia" ,"id" , "count" , playedMedia));
		}
		
		
		if (this.deletedMedia.size()>0)
		{	
			root.addChild(listToNode( "deletes" , "usermedia" ,"id" , deletedMedia));
		}
				
		if (this.syncMedia.size()>0)
		{	
			root.addChild(mapToNode( "syncMedia" , "usermedia" ,"id" , "sync" , syncMedia));
		}
				
		return super.getXML();
	}


		
	public MXXMLNode addPurchaseNode()
	{
		MXXMLNode purchaseNode=new MXXMLNode("purchase" , null ,null);			
		
		if (purchasedMedias.size()>0){
			purchaseNode.addChild(listToNode( "medias" , "media" , "id " , purchasedMedias ) );			
		}
		
		if (purchasedAlbums.size()>0){
			purchaseNode.addChild(listToNode( "albums" , "album" ,"id " , purchasedAlbums ) );
		}
		
		if (purchasedPlaylists.size()>0){
			purchaseNode.addChild(listToNode( "playlists" , "playlist" ,"id " , purchasedPlaylists ) );
		}
		
		return purchaseNode;
	}
		
	

	
	public MXXMLNode listToNode(String nodeName , String childNodeName , 
								String valueName , ArrayList<String> srcList)
	{		
		MXXMLNode node=null;
		
		if (srcList.size()>0)
		{
			node=new MXXMLNode(nodeName ,null ,null );
			HashMap <String , String> att=new HashMap <String , String>();
	
			for (String s: srcList)
			{	
				att.clear();
				att.put(valueName , s);				
				MXXMLNode childNode=new MXXMLNode(childNodeName ,null ,att );
				node.addChild(childNode);			
			}	
		}
		
		return node;
	}
	
		
	

	
	public MXXMLNode mapToNode(String nodeName , String childNodeName ,
			String keyName ,String valueName ,	HashMap<String ,String> srcMap)
	{		
		MXXMLNode node=null;
		
		if (srcMap.size()>0)
		{
			node=new MXXMLNode(nodeName , null ,null);
			HashMap <String , String> att=new HashMap <String , String>();
		
			for (String s: srcMap.keySet())
			{	
				att.clear();
				att.put(keyName , s);
				att.put( valueName , srcMap.get(s));
				MXXMLNode childNode=new MXXMLNode(childNodeName ,null ,att );
				node.addChild(childNode);			
			}	
		}	
	
		return node;
	}	
	
	
}

package il.co.pelephone.musix.comm;

import java.io.IOException;
import java.util.HashMap;

import android.util.Log;

/*
 * <?xml version="1.0" encoding="UTF-8"?>
<request milliseconds="1234567">
<login isRegisterAccepted="1" version="1.5.7" />
<plays>
<usermedia id="5" count="5" />
MUSIX IPHONE APP REQUIREMENTS & DESIGN
20
<usermedia id="7" count="1" />
</plays>
</request>
 * */
public class MXLoginRequest extends MXXMLRequest{
	public static final String LOGTAG="MXLoginRequest";
	
	final private String DFLT_VERSION="1.1";
	final private String DFLT_TIMESTAMP="";
	
	private String clientVersion;
	private String timestamp;
	private boolean isRegisterAccepted;
	private HashMap <String,String> playedMedia; //id:count
		
	public MXLoginRequest(String clientVersion , boolean isRegisterAccepted)
	{	
		super();
		
		Log.d(LOGTAG, "in constructor 1");
		this.clientVersion=clientVersion;
		this.timestamp=DFLT_TIMESTAMP;
		this.isRegisterAccepted=isRegisterAccepted;	
		this.playedMedia=new HashMap <String,String>();
		this.playedMedia.clear();
	}

	public MXLoginRequest(boolean isRegisterAccepted)
	{
		super();
		Log.d(LOGTAG, "in constructor 2");
		//this.clientVersion=clientVersion;
		this.clientVersion=DFLT_VERSION;
		this.timestamp=DFLT_TIMESTAMP;
		this.isRegisterAccepted=isRegisterAccepted;	
		this.playedMedia=new HashMap <String,String>();
		this.playedMedia.clear();
	}
	
	public MXLoginRequest()
	{
		super();
		Log.d(LOGTAG, "in constructor 3");
		//this.clientVersion=clientVersion;
		this.clientVersion=DFLT_VERSION;
		this.timestamp=DFLT_TIMESTAMP;
		this.isRegisterAccepted=false;	
		this.playedMedia=new HashMap <String,String>();
		this.playedMedia.clear();
	}
	
	//Indicates the current client version.
	public String getClientVersion()
	{
		return this.clientVersion;
	}
	
	
	public void setClientVersion(String clientVersion)
	{
		this.clientVersion=clientVersion;		
	}
	
	
	//Indicates the current client version.
	public String getTimestamp()
	{
		return this.timestamp;
	}
	
	
	public void setTimestamp(String timestamp)
	{
		this.timestamp=timestamp;		
	}
	
	

	
	//Indicates whether or not the user accepted the TOC for the app.
	//Should be empty by
	//default and included in the XML only if it’s value is 1.
	public boolean isRegisterAccepted()
	{
		return this.isRegisterAccepted;		
	}
		
	
	public void setRegisterAccepted(boolean flag)
	{
		this.isRegisterAccepted=flag;
		
	}
	
	//Adds a statistics record for a media that was played 
	//and how many times it was played
	//by the user since the last sync.
	public void addPlayedMedia(String mediaID, int playCount)
	{		
		this.playedMedia.put(mediaID, String.valueOf(playCount));
	}
	
	
	@Override
	public String getXML() throws IOException
	{			
		HashMap <String , String> att=new HashMap <String , String>();
		att.put("milliseconds", this.timestamp);
		MXXMLNode rootNode=new MXXMLNode("request" , null ,att );		
		super.setRootNode(rootNode);		
		
		att.clear();		
		if (isRegisterAccepted)	{				
			att.put("isRegisterAccepted", "1");
		}		
		att.put("version" , this.clientVersion);		
		MXXMLNode node=new MXXMLNode("login" , null ,att );
		root.addChild(node);
		
		if (this.playedMedia.size()>0)
		{	
			root.addChild(addPlayedMediaNode());
		}
		
		return super.getXML();
	}


	public MXXMLNode addPlayedMediaNode()
	{
		MXXMLNode playNode=new MXXMLNode("plays" , null ,null);
		HashMap <String , String> att=new HashMap <String , String>();
		
		if (playedMedia.size()>0)
		{
			for (String s: playedMedia.keySet())
			{	
				att.clear();
				att.put("id" , s);
				att.put( "count" , playedMedia.get(s));
				MXXMLNode playedMediaNode=new MXXMLNode("usermedia" ,null ,att );
				playNode.addChild(playedMediaNode);			
			}
			return playNode;
		}
		else
			return null;
	}

	
	
	
}

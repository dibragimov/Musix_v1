package il.co.pelephone.musix.comm;


public class MXGetPlaylistMediasRequest extends MXStoreRequest{

	private String playlistID;
	
	public  MXGetPlaylistMediasRequest(String playlistID )
	{
		super();
		this.playlistID=playlistID;
		functionName=new String("getPlaylistMedias");
		parameterName=new String("playlistID");		
		parameterValue=playlistID;
		supportsPaging = false;	
	}
	

	public String getGenreID()	{
		return playlistID;
	}
	
	public void setGenreID(String genreID)	{
		this.playlistID=genreID;		
	}
	
	
}

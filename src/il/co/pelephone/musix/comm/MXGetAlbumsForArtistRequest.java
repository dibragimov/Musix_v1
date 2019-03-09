package il.co.pelephone.musix.comm;

public class MXGetAlbumsForArtistRequest extends MXStoreRequest {

	
	private String artistID;
	
	public  MXGetAlbumsForArtistRequest(String artistID)
	{
		super();
		this.artistID= artistID;
		functionName=new String("getAlbumsByArtist");		
		parameterName=new String("artistID");		
		parameterValue=artistID;
		supportsPaging = false;		
	}	
	
	

	public String getArtistID()	{
		return artistID;
	}
	
	public void setArtistID(String artistID)	{
		this.artistID=artistID;		
	}
	
}

package il.co.pelephone.musix.comm;

public class MXGetAlbumMediasRequest extends MXStoreRequest{
	
	private String albumID;
	
	public  MXGetAlbumMediasRequest(String albumID)
	{
		super();
		this.albumID=albumID;
		functionName=new String("getAlbumMedias");
		parameterName=new String("albumID");		
		parameterValue=albumID;
		supportsPaging = true;
		pageNumber = 1;
		resultsPerPage = -1;		
	}
	
	public String getAlbumID()	{
		return albumID;
	}
	
	public void setAlbumID(String albumID)	{
		this.albumID=albumID;		
	}
}

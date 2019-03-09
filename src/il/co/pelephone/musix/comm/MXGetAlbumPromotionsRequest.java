package il.co.pelephone.musix.comm;

public class MXGetAlbumPromotionsRequest extends MXStoreRequest{

	private String genreID;
	
	public  MXGetAlbumPromotionsRequest(String genreID)
	{
		super();
		this.genreID=genreID;
		functionName=new String("getAlbumPromotions");
		parameterName=new String("genreID");		
		parameterValue=genreID;
		supportsPaging = false;		
	}
	
	public String getGenreID()	{
		return genreID;
	}
	
	public void setGenreID(String genreID)	{
		this.genreID=genreID;		
	}
}

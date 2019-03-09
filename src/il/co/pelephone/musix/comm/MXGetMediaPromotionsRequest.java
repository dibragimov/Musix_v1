package il.co.pelephone.musix.comm;


public class MXGetMediaPromotionsRequest extends MXStoreRequest{
	
	private String genreID;
	
	public  MXGetMediaPromotionsRequest(String genreID)
	{
		super();
		this.genreID=genreID;
		functionName=new String("getMediaPromotions");
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

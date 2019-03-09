package il.co.pelephone.musix.comm;

import java.util.Calendar;


public class MXGetPlaylistPromotionsRequest extends MXStoreRequest{ 

	private String genreID;

	
	public  MXGetPlaylistPromotionsRequest(String genreID)
	{
		super();
		this.genreID=genreID;
		functionName=new String("getPlaylistPromotions");
		parameterName=new String("genreID");		
		parameterValue=genreID;
		supportsPaging = false;		
		setTimestamp(""+Calendar.getInstance().getTimeInMillis());
	}	
	

	public String getGenreID()	{
		return genreID;
	}
	
	public void setGenreID(String genreID)	{
		this.genreID=genreID;		
	}
	
}

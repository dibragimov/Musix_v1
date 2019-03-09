package il.co.pelephone.musix.comm;


public class MXGetGenresRequest extends MXStoreRequest {
	
	public  MXGetGenresRequest()
	{
		super();
		functionName=new String("listGenres");		
		supportsPaging = true;
		pageNumber = 1;
		resultsPerPage = -1;	
	}	
}

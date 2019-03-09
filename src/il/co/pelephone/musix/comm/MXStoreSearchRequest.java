package il.co.pelephone.musix.comm;

public class MXStoreSearchRequest extends MXStoreRequest {
		
	public enum SDStoreSearchType { 
		SDStoreSearchTypeArtists,  //searchArtistName
		SDStoreSearchTypeAlbums,  //searchAlbumByName
		SDStoreSearchTypeSongs   //searchMediaByName
		};
		
	private String searchCriteria;
	private SDStoreSearchType searchType;

	public  MXStoreSearchRequest(String searchCriteria ,SDStoreSearchType searchType )
	{
		super();
		
		this.searchCriteria=searchCriteria;
		this.searchType=searchType;
			
		parameterName=new String("searchText");		
		parameterValue=searchCriteria;
		supportsPaging = true;
		pageNumber = 1;
		resultsPerPage = 20;
		
		switch (searchType)
		{
		case SDStoreSearchTypeArtists:
			super.functionName="searchArtistName";
			break;
		case SDStoreSearchTypeAlbums:			
			super.functionName="searchAlbumByName";
			break;
		case SDStoreSearchTypeSongs:
			super.functionName="searchMediaByName";
			break;		
		}
	}	
	
	
	
	public String getSearchCriteria(){
		return searchCriteria;
	}
	
	
	public void setSearchCriteria(String searchCriteria){
		this.searchCriteria=searchCriteria;		
	}
	
	
	public SDStoreSearchType getSearchType(){
		return searchType;		
	}
	
	
	public void setSearchCriteria(SDStoreSearchType searchType)	{
		this.searchType=searchType;		
	}
	
}

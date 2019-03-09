package il.co.pelephone.musix.comm;

import java.io.IOException;
import java.util.HashMap;


public abstract class MXStoreRequest extends MXXMLRequest{

	private String timestamp;
	protected String functionName;	
	protected boolean supportsPaging;
	protected int pageNumber;
	protected int resultsPerPage;	
	protected String parameterName;
	protected String parameterValue;
			
	public MXStoreRequest()
	{
		super();
		timestamp=null;
		functionName=null;
		supportsPaging=false;
		pageNumber=0;
		resultsPerPage=0;
		parameterName=null;
		parameterValue=null;
	}
	
	@Override
	public String getXML() throws IOException
	{
		MXXMLNode rootNode=null;
		HashMap <String , String> att=new HashMap <String , String>();
		
		if (this.timestamp!=null){			
			att.put("milliseconds", this.timestamp);			
		}
		
		rootNode=new MXXMLNode("request" , null ,att );		
		super.setRootNode(rootNode);	
		
		MXXMLNode params = new MXXMLNode("parameters",null,null);
		
		root.addChild(new MXXMLNode("name" , functionName ,null ));
		
		 att=new HashMap <String , String>();
		 att.put("width", "60");
		 att.put("height","60");
		 
		MXXMLNode cover = new MXXMLNode("cover",null,att);
		root.addChild(cover);
		
		
		if (supportsPaging)
		{
			att.clear();
			att.put("pageNumber", String.valueOf(pageNumber));
			att.put("pageSize", String.valueOf(resultsPerPage) );
			root.addChild(new MXXMLNode("paging" , null ,att ));
		}
		
		if(parameterName!=null && parameterName.length()>0)
		{
			params.addChild(new MXXMLNode(parameterName , parameterValue ,null ));			
		}		
		
		root.addChild(params);
		return super.getXML(); 
	}
		

	
	public String getTimestamp(){
		return timestamp;		
	}
	
	public void setTimestamp(String timestamp){
		this.timestamp=timestamp;
	}
	
	public String getFunctionName(){
		return timestamp;		
	}
	
	public void setFunctionName(String functionName){
		this.functionName=functionName;
	}
	
	public String setParameterName(){
		return parameterName;		
	}
	
	public void setParameterName(String parameterName){
		this.parameterName=parameterName;
	}
	
	public String getParameterValue(){
		return parameterValue;		
	}
	
	public void setParameterValue(String parameterValue){
		this.parameterValue=parameterValue;
	}
	
		
	public void setPageNumber(int pageNumber )	{
		this.pageNumber=pageNumber;
	}
	
	public int getPageNumber()	{
		return pageNumber;
	}
	
	public void setPesultsPerPage(int resultsPerPage )	{
		this.resultsPerPage=resultsPerPage;
	}
	
	public int getResultsPerPage()	{
		return resultsPerPage;
	}
	
	public boolean isSupportsPaging()	{		
		return supportsPaging;
	}
	
	public void setPagingSupport(boolean flag)	{
		this.supportsPaging=flag;
	}	
	
}

package il.co.pelephone.musix.comm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//The class provides built-in methods to create XML files.
public abstract class MXXMLRequest {
	
	MXXMLNode root;
	
	public final  String XML_HEADER="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

	public MXXMLRequest()
	{
		this.root= new MXXMLNode(null , null , null);		
	}
	
	//Sets the root node for the request
	protected void setRootNode (MXXMLNode rootNode)
	{	
		
		this.root=rootNode.copy();
	}
	
	//Adds a child node to the root node
	protected void addNode (MXXMLNode childNode)
	{		
		this.root.addChild(childNode);
	}
	
	
		
	public  String getXML() throws IOException {

		  StringBuilder result = new StringBuilder(XML_HEADER);
			  
		  if (root.getName().length()>0)
			  result.append(nodeToString(root));
		  
		  return result.toString();
	}
		  
	
	
	
	public String nodeToString(MXXMLNode node)
	{
		StringBuilder strBuf=new StringBuilder("<") ;

		strBuf.append(node.getName());
		
		if (node.getAttributesSize()>0)
		{				
			HashMap <String , String> attributes=node.getAttributes();
			
			for (String s: attributes.keySet()) {
				strBuf.append(" ");
				strBuf.append(s);
				strBuf.append("=\"");
				strBuf.append(attributes.get(s));	 
				strBuf.append("\"");
			}
		}
		  
		
		if(!node.hasChildren() && node.getValue().length()==0){
			strBuf.append(" />\n");
		}
		else
		{
			strBuf.append(">");
			
			if(node.hasChildren())
			{
				ArrayList <MXXMLNode> children=node.getChildren();
				for (MXXMLNode c: children) 
				{
					strBuf.append(nodeToString(c));
				}	
			}
			
			if(node.getValue().length()>0)
			{
			//	strBuf.append("\");
				strBuf.append(node.getValue());
			//	strBuf.append("\"\n");
			}
			
			strBuf.append("</");
			strBuf.append(node.getName());
			strBuf.append(">\n");			
		}
		
		return strBuf.toString();
	}		
}

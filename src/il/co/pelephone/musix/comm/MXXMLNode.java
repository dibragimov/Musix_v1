package il.co.pelephone.musix.comm;

import java.util.ArrayList;
import java.util.HashMap;

//Represents a single node in an XML
public class MXXMLNode {

	private String name;
	private String value;
	private HashMap <String , String> attributes;	
	private ArrayList <MXXMLNode> children;
	
	
	
	protected MXXMLNode (String name, String value, HashMap <String , String> attributes)
	{		
		if(name!=null)
			this.name=name.trim();
		else
			this.name=new String("");
		if(value!=null)
			this.value=value.trim();
		else
			this.value=new String("");
		
		this.attributes=new HashMap <String , String>();	
		this.attributes.clear();
		if(attributes!=null && attributes.size()>0)
			this.attributes.putAll(attributes);		
		this.children=new ArrayList <MXXMLNode>();
		this.children.clear();
	}
	
	protected MXXMLNode (MXXMLNode node)
	{		
		this.name=node.getName();
		this.value=node.getValue();
		this.attributes=node.getAttributes();
		this.children=node.getChildren();
	}
	
	public MXXMLNode copy()
	{
		MXXMLNode copy = new MXXMLNode(name , value , attributes );
		copy.children = this.getChildren();
		return copy;
	}
	
	
	public void addChild (MXXMLNode childNode)	{
		this.children.add(childNode);			
	}	
	
	public String getName()	{
		return this.name;		
	}
	
	public void SetName(String name)	{
		this.name=name;		
	}
	
	
	
	public String getValue()	{
		return this.value;		
	}
	
	public void SetValue(String value)	{
		this.value=value;		
	}
	
	
	public void addAttributes(HashMap <String , String> attributes)	{
		this.attributes.putAll(attributes);		
	}
	
	
	public int getAttributesSize()	{
		return this.attributes.size();
	}
	
	
	public HashMap <String , String> getAttributes()	{		
		HashMap <String , String> copy=new HashMap<String , String>(this.attributes);
		return copy;
	}
	
	public boolean hasChildren()	{		
		return (this.children.isEmpty())?false:true;
	}
	
	
	public ArrayList <MXXMLNode> getChildren()	{
		ArrayList <MXXMLNode>copy =new ArrayList<MXXMLNode>(this.children);
		return copy;
	}
}

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


public class Entity implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3676124656636334156L;
	private Set<String> name; //The first element is preferably the KB header version of the name
	private String id = ""; //NIL if not in kb
	private String type = "";
	Entity(){
		name = new HashSet<String>(4);
		id = "NIL";
		type = "";
	}
	
	public String getId(){
		return id;
	}
	
	public Set<String> getAltNames(){
		return name;
	}
	
	public String getType(){
		return type;
	}

	public boolean isInstanceOf(String n){
		return name.contains(n.trim());
	}
	
	public void addAltName(String n){
		name.add(n.trim());
	}
	
	public void setId(String i){
		id = i.trim();
	}
	
	public void setType(String t){
		type = t;
	}
}

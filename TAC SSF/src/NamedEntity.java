import java.util.*;

public class NamedEntity {
	String entityid;
	String entity;
	String type;
	int beg;
	int end;
	HashMap<String, Double> resolutions;
	
	public NamedEntity(String id, String entity, String type, int beg, int end, HashMap<String, Double> resolutions){
		this.entityid = id;
		this.entity = entity;
		this.type = type;
		this.beg = beg;
		this.end = end;
		this.resolutions = resolutions;
	}
}

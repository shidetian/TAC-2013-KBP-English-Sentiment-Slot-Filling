import java.util.*;

public class NamedEntity {
	String entityid;
	String entity;
	int beg;
	int end;
	HashMap<String, Double> resolutions;
	
	public NamedEntity(String id, String entity, int beg, int end, HashMap<String, Double> resolutions){
		this.entityid = id;
		this.entity = entity;
		this.beg = beg;
		this.end = end;
		this.resolutions = resolutions;
	}
}

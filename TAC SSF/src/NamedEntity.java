public class NamedEntity {
	public String entityid;
	public String entity;
	public int beg;
	public int end;
	
	public NamedEntity(String id, String entity, int beg, int end){
		this.entityid = id;
		this.entity = entity;
		this.beg = beg;
		this.end = end;
	}
}

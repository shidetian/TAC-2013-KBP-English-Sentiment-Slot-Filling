
public class Query {
	String qId;
	String entity;
	String docId;
	String entityOffsets;
	String entityType;
	String nodeId;
	Sentiment sent;
	public Query(String qId, String ent, String docId, String entOffset, String entType, String nodeId, Sentiment s){
		this.qId = qId;
		entity = ent;
		this.docId = docId;
		entityOffsets = entOffset;
		entityType = entType;
		this.nodeId = nodeId;
		sent = s;
	}
}

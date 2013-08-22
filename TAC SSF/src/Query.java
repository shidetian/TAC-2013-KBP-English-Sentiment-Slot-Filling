import java.util.ArrayList;


public class Query {
	String qId;
	String entity;
	String docId;
	String entityOffsets;
	String entityType;
	String nodeId;
	Sentiment sent;
	
	//If this field is null, that means that no lookups have been performed, and coref, etc system will be run on first call to getAltNames()
	ArrayList<String> altNames;
	public Query(String qId, String ent, String docId, String entOffset, String entType, String nodeId, Sentiment s){
		this.qId = qId;
		entity = ent;
		this.docId = docId;
		entityOffsets = entOffset;
		entityType = entType;
		this.nodeId = nodeId;
		sent = s;
		
		altNames = null;
	}
	
	//altNames is "lazy", it will not be initialized until first call
	public ArrayList<String> getAltNames(boolean corefs){
		if (altNames == null){
			altNames = new ArrayList<String>();
			if (nodeId.charAt(0)=='E'){
				//TODO lookup in KB
				//altNames.addAll(KBImporter.kb.get(nodeId).getAltNames());
			}
			
			if (corefs){
				//TODO lookup in corefs
			}
		}
		return altNames;
	}
}
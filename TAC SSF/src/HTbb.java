import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

public class HTbb {
	public ArrayList<String> opinWords;
	public String sentence;
	public String holderSpan;
	public String targetSpan;
	public String[] entities;
	public String printing;
	public HashMap<String, String> results;
	public int sentencenBegin;
	public int sentenceEnd;
	
	
	private HTParser p;
	private Hashtable<String,Integer> holderCandidatesLength;
	private Hashtable<String,Integer> targetCandidatesLength;
	private Hashtable<String,Integer> holderCandidatesTimes;
	private Hashtable<String,Integer> targetCandidatesTimes;
	private Hashtable<String,Float> holderHash;
	private Hashtable<String,Float> targetHash;

	public HTbb(){
		
		this.p = new HTParser();
		this.holderCandidatesLength = new Hashtable<String,Integer>();
		this.targetCandidatesLength = new Hashtable<String,Integer>();
		this.holderCandidatesTimes = new Hashtable<String,Integer>();
		this.targetCandidatesTimes = new Hashtable<String,Integer>();
		this.holderHash = new Hashtable<String,Float>();
		this.targetHash = new Hashtable<String,Float>();
		
		
	}
	
	public HashMap<String, String> process(String s, ArrayList<String> opinWords, HashSet<String> entities, int sentenceBegin, int sentenceEnd){
		HashMap<String, String> results = new HashMap<String, String>();
		this.opinWords = opinWords;
		this.sentence = s;
		this.entities = (String[]) entities.toArray();
		this.sentencenBegin = sentenceBegin;
		this.sentenceEnd = sentenceEnd;
		
		p.getDependencyString(sentence);
		extractHT();
		rank();

		String opinionOffsets = Integer.toString(sentencenBegin) + "-" + Integer.toString(sentenceEnd);
		String holderOffsets = Integer.toString(sentence.indexOf(holderSpan)+sentencenBegin)+"-"+Integer.toString(sentence.indexOf(holderSpan)+sentencenBegin+holderSpan.length());
		String targetOffsets = Integer.toString(sentence.indexOf(targetSpan)+sentencenBegin)+"-"+Integer.toString(sentence.indexOf(targetSpan)+sentencenBegin+targetSpan.length());
		
		printing = sentence + "\t" + opinionOffsets + "\t" + holderSpan + "\t" + holderOffsets + "\t"+ targetSpan + targetOffsets;
		results.put("0_"+String.valueOf(s.length()-1), printing);
		
		return results;
		
	}
	
	private void extractHT(){
		for (int indexOpinWord=0;indexOpinWord<opinWords.size();indexOpinWord++){
			for (int indexEntity=0;indexEntity<entities.length;indexEntity++){
				String trace = p.getTheRelationBetween(opinWords.get(indexOpinWord), entities[indexEntity]);
				Boolean holderFlag = traceJudgeHolder(trace);
				Boolean targetFlag = traceJudgeTarget(trace);
				
				if (holderFlag && !targetFlag){
					if (holderCandidatesLength.contains(entities[indexEntity])){
						int value = holderCandidatesLength.get(entities[indexEntity]) + trace.split("-").length-1;
						holderCandidatesLength.put(entities[indexEntity],value);
						int times = holderCandidatesTimes.get(entities[indexEntity]) + 1;
						holderCandidatesTimes.put(entities[indexEntity], times);
					}
					else{
						holderCandidatesLength.put(entities[indexEntity],trace.split("-").length-1);
						holderCandidatesTimes.put(entities[indexEntity], 1);
					}
				}
				else if (!holderFlag && targetFlag){
					if (targetCandidatesLength.contains(entities[indexEntity])){
						int value = targetCandidatesLength.get(entities[indexEntity]) + trace.split("-").length-1;
						targetCandidatesLength.put(entities[indexEntity],value);
						int times = targetCandidatesTimes.get(entities[indexEntity]) + 1;
						targetCandidatesTimes.put(entities[indexEntity], times);
					}
					else{
						targetCandidatesLength.put(entities[indexEntity],trace.split("-").length-1);
						targetCandidatesTimes.put(entities[indexEntity], 1);
					}
				}
			}
		}
		return;
	}
	
	private Boolean traceJudgeHolder(String trace){
		if (!trace.contains("subj"))
			return false;
		
		String[] relations = trace.split("-");
		String lastRelation = relations[relations.length-1];
		if (lastRelation.contains("nn") || lastRelation.contains("subj") || lastRelation.contains("poss") ){
			return true;
		}
		
		
		return false;
	}
	
	private Boolean traceJudgeTarget(String trace){
		if (!trace.contains("obj"))
			return false;
		
		String[] relations = trace.split("-");
		String lastRelation = relations[relations.length-1];
		if (lastRelation.contains("nn") || lastRelation.contains("subjpass") || lastRelation.contains("obj") ){
			return true;
		}
		return false;
	}
	
	private void rank(){
		for (String holder: holderCandidatesLength.keySet()){
			if (holder == null)
				continue;
			holderHash.put(holder, holderCandidatesLength.get(holder)/(float)holderCandidatesTimes.get(holder) );
		}

		//Sort
		ArrayList<Map.Entry<String, Float>> l = new ArrayList<Entry<String, Float>>(holderHash.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<String, Float>>(){

			public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}});
		
		//output
		holderSpan = l.get(0).getKey();
		
		/////////////////////////////////////////////////

		for (String target: targetCandidatesLength.keySet()){
			if (target == null)
				continue;
			targetHash.put(target, targetCandidatesLength.get(target)/(float)targetCandidatesTimes.get(target) );
		}

		//Sort
		l = new ArrayList<Entry<String, Float>>(targetHash.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<String, Float>>(){

			public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}});
		
		//output
		targetSpan = l.get(0).getKey();

	}

}

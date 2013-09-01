import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

public class HTLastOzan {
	public String sentence;
	public ArrayList<String> opinWords;
	public String[] entities;
	public int sentencenBegin;
	public int sentenceEnd;
	
	public String[] holder;
	public String[] target;
	public String[] holderOffset; 
	public String[] targetOffset; 
	
	public String printing;
	public HashMap<String, String> results;
	
	private HTParser p;

	public HTLastOzan(){
	}
	
	public HashMap<String, String> process(String s, HTParser p, ArrayList<String> opinWords, HashSet<String> entities, int sentenceBegin, int sentenceEnd){
		this.results = new HashMap<String, String>();
		
		this.sentence = s;
		this.p = p;
		this.opinWords = opinWords;
		this.entities = entities.toArray(new String[entities.size()]);
		this.sentencenBegin = sentenceBegin;
		this.sentenceEnd = sentenceEnd;
		this.holder = new String[opinWords.size()];
		this.target = new String[opinWords.size()];
		this.holderOffset = new String[opinWords.size()];
		this.targetOffset = new String[opinWords.size()];
		
		extractingHT();
		
		for (int i=0;i<holder.length;i++){
			String opinword = opinWords.get(i);
			String opinSpan = Integer.toString(sentence.indexOf(opinword)+sentencenBegin)+"-"+Integer.toString(sentence.indexOf(opinword)+sentencenBegin+opinword.length());
			
			this.results.put(opinSpan, opinword+"\t"+opinSpan+"\t"+holder[i]+"\t"+holderOffset[i]+"\t"+target[i]+"\t"+targetOffset[i]);
		}
		
		
		return this.results;
		
	}
	
	
	public void extractingHT(){
		for (int indexOpinWord=0;indexOpinWord<opinWords.size();indexOpinWord++){
			String opinionOffsets = Integer.toString(sentence.indexOf(opinWords.get(indexOpinWord))+sentencenBegin)+"-"+Integer.toString(sentence.indexOf(opinWords.get(indexOpinWord))+sentencenBegin+opinWords.get(indexOpinWord).length());
			
			for (int indexEntity=0;indexEntity<entities.length;indexEntity++){
				String trace = p.getTheRelationBetween(opinWords.get(indexOpinWord), entities[indexEntity]);
				Boolean holderFlag = traceJudgeHolder(trace);
				Boolean targetFlag = traceJudgeTarget(trace);
				
				if (holderFlag && !targetFlag){
					holder[indexOpinWord] = entities[indexEntity];
					String holderOffsets = Integer.toString(sentence.indexOf(entities[indexEntity])+sentencenBegin)+"-"+Integer.toString(sentence.indexOf(entities[indexEntity])+sentencenBegin+entities[indexEntity].length());
					holderOffset[indexOpinWord] = holderOffsets;
				}
				else if (!holderFlag && targetFlag){
					target[indexOpinWord] = entities[indexEntity];
					String targetOffsets = Integer.toString(sentence.indexOf(entities[indexEntity])+sentencenBegin)+"-"+Integer.toString(sentence.indexOf(entities[indexEntity])+sentencenBegin+entities[indexEntity].length());
					targetOffset[indexOpinWord] = targetOffsets;
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
		if (!(trace.contains("obj")) && !(trace.contains("amod")))
			return false;
		
		String[] relations = trace.split("-");
		String lastRelation = relations[relations.length-1];
		if (lastRelation.contains("nn") || lastRelation.contains("subjpass") || lastRelation.contains("obj") || lastRelation.contains("amod") || lastRelation.contains("poss") ){
			return true;
		}
		return false;
	}
}

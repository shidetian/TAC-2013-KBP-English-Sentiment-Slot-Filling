package Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

//import HTParser.Parser;

//import opinUnit.SentimentUnit;
//import opinWords.lookUpOpinLeixcon;
//import opinWords.lookUpOpinLeixcon.OpinWord;


public class HTaa {
	
	public String[] opinWords;
	public String sentence;
	public String holderSpan;
	public String targetSpan;
	private HTParser p;
	private String[] holderDependency = {"subj","comp"}; // this should have an order
	private String[] targetDependency = {"amod","obj","subj"};
	private String[] holderCandidates;
	private String[] targetCandidates;
	private Hashtable<String,Integer> holderHash;
	private Hashtable<String,Integer> targetHash;

    public HTaa(String[] opinWords, String s){
		this.opinWords = opinWords;
		this.sentence = s;
		this.p = new HTParser();
		
		this.holderCandidates = new String[opinWords.length];
		this.targetCandidates = new String[opinWords.length];
		this.holderHash = new Hashtable<String,Integer>();
		this.targetHash = new Hashtable<String,Integer>();
		
		p.getDependencyString(sentence);
		extractHolder();
		extractTarget();
		rank();
    }
    
    private void extractHolder(){
    		for (int indexOpinWord=0;indexOpinWord<opinWords.length;indexOpinWord++){
    			String candidate = null;
    			int indexDependcy =0;
    			while (candidate == null){
    				candidate = p.getTheOtherWord(opinWords[indexOpinWord],holderDependency[indexDependcy],"gov");
    				indexDependcy++;
    			}
    			holderCandidates[indexOpinWord] = candidate;
    		}
    		return;
    }
    
    private void extractTarget(){
		for (int indexOpinWord=0;indexOpinWord<opinWords.length;indexOpinWord++){
			String candidate = null;
			int indexDependcy =0;
			while (candidate == null){
				candidate = p.getTheOtherWord(opinWords[indexOpinWord],targetDependency[indexDependcy],"gov");
				indexDependcy++;
			}
			targetCandidates[indexOpinWord] = candidate;
		}
		return;
    }
    
    private void rank(){
    		for (String holder: holderCandidates){
    			if (holderHash.containsKey(holder)){
    				int value = holderHash.get(holder);
    				holderHash.put(holder, value+1);
    			}
    			else
    				holderHash.put(holder, 1);
    		}
    		
    		//Transfer as List and sort it
 	    ArrayList<Map.Entry<String, Integer>> l = new ArrayList<Entry<String, Integer>>(holderHash.entrySet());
 	    Collections.sort(l, new Comparator<Map.Entry<String, Integer>>(){

 	    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
 	    		return o2.getValue().compareTo(o1.getValue());
 	    	}});
 	    
 	   holderSpan = l.get(0).getKey();
 	   
	   for (String target: targetCandidates){
		   if (targetHash.containsKey(target)){
			   int value = targetHash.get(target);
			   targetHash.put(target, value+1);
		   }
		   else
			   targetHash.put(target, 1);
	   }
	   
	 //Transfer as List and sort it
	    l = new ArrayList<Entry<String, Integer>>(targetHash.entrySet());
	    Collections.sort(l, new Comparator<Map.Entry<String, Integer>>(){

	    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
	    		return o2.getValue().compareTo(o1.getValue());
	    	}});
	    
	   targetSpan = l.get(0).getKey();
		
    }
}

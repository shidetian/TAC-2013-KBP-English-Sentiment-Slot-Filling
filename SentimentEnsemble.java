// ensemble (voting) different sentiment untis from different sentiment analysis system

// For any two sentiment units sharing the same holder and target, 
// the output will be only one

import java.util.ArrayList;

public class SentimentEnsemble{
	
	public ArrayList<SentimentUnit> sentimentList;
	public ArrayList<SentimentUnit> sentimentListEnsembled;
	
	public SentimentEnsemble(ArrayList<SentimentUnit> sentimentList){
		this.sentimentList = sentimentList;
		this.sentimentListEnsembled = new ArrayList<SentimentUnit>();
	}
	
	// add one more sentiment unit
	public void addSentimentUnit(SentimentUnit su){
		this.sentimentList.add(su);
		return;
	}
	
	// add a list of sentiment unit
	public void addSentimentUnitList(ArrayList<SentimentUnit> suList){
		this.sentimentList.addAll(suList);
		return;
	}
	
	public void ensemble(){
		/*
		Functions will be done to the sentimentList
		and will add the voting (ensemble) results 
		to the sentimentListEnsembled
		*/
		return;
	}
}

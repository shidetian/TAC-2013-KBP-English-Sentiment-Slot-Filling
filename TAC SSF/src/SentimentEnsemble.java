// ensemble (voting) different sentiment untis from different sentiment analysis system

// For any two sentiment units sharing the same holder and target, 
// the output will be only one

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class SentimentEnsemble{
	
	public ArrayList<SentimentUnit> sentimentList;
	public ArrayList<SentimentUnit> sentimentListEnsembled;
	
	public SentimentEnsemble(String cornellFile, String pittFile){
		this.sentimentList = new ArrayList<SentimentUnit>();
		this.sentimentListEnsembled = new ArrayList<SentimentUnit>();
		
		addSentimentFile(cornellFile);
		addSentimentFile(pittFile);
	}
	
	// add a list of sentiment unit
	public void addSentimentFile(String filename){
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
		String line;
            	while ((line=br.readLine())!= null){
            		String[] a = line.split("\t");
            		this.sentimentList.add(new SentimentUnit(a[0],a[1],a[2],a[3],a[4],a[5],Double.parseDouble(a[6])));
            	}
            	br.close();
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

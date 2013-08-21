// ensemble (voting) different sentiment untis from different sentiment analysis system

// For any two sentiment units sharing the same holder and target, 
// the output will be only one

import java.util.ArrayList;

public class SentimentEnsemble{
	
	public ArrayList<SentimentUnit> sentimentList;
	public ArrayList<SentimentUnit> sentimentListEnsembled;
	
	public SentimentEnsemble(){
		this.sentimentList = new ArrayList<Response>();
		this.sentimentListEnsembled = new ArrayList<Response>();
	}
	
	// add a list of sentiment unit
	public void addSentimentFile(String filename){
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
            	while ((String line=br.readLines())!= null){
            		String[] a = line.split("\t");
            		this.sentimentList.add(new Response(a[0],a[1],a[2],a[3],a[4],a[5],(double) a[6]));
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

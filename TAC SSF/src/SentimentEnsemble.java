package opinUnit;

// ensemble (voting) different sentiment untis from different sentiment analysis system

// For any two sentiment units sharing the same holder and target, 
// the output will be only one

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SentimentEnsemble{
	
	public ArrayList<SentimentUnit> sentimentList;
	public ArrayList<SentimentUnit> sentimentListEnsembled;
	
	public SentimentEnsemble(String cornellFile, String pittFile) throws IOException{
		this.sentimentList = new ArrayList<SentimentUnit>();
		this.sentimentListEnsembled = new ArrayList<SentimentUnit>();
		
		addSentimentFile("cornell_output.txt");
		addSentimentFile("pitt_output.txt");
	}
	
	// add a list of sentiment unit
	public void addSentimentFile(String filename) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
		String line;
            	while ((line=br.readLine())!= null){
            		String[] a = line.split("\t");
            		this.sentimentList.add(new SentimentUnit(a[0],a[1],a[2],a[3],a[4],a[5],
            				a[6], a[7], a[8], a[9], Double.parseDouble(a[10])));
            	}
            	br.close();
	}
	
	public void ensemble(){
		for(int i=0;i<sentimentList.size();i++){
			for (int j=i+1;j<sentimentList.size();j++){
				if (sameEntity(sentimentList.get(i).holderSpan, sentimentList.get(j).targetSpan) &&
				sameEntity(sentimentList.get(i).holderSpan, sentimentList.get(j).targetSpan) ){
					if (sentimentList.get(i).confidenceScore > sentimentList.get(j).confidenceScore)
						sentimentListEnsembled.add(sentimentList.get(i));
					else if (sentimentList.get(i).confidenceScore < sentimentList.get(j).confidenceScore)
						sentimentListEnsembled.add(sentimentList.get(j));
					else{
						if (sentimentList.get(i).sentenceSpan.length() > sentimentList.get(j).sentenceSpan.length())
							sentimentListEnsembled.add(sentimentList.get(i));
						else if (sentimentList.get(i).sentenceSpan.length() < sentimentList.get(j).sentenceSpan.length())
							sentimentListEnsembled.add(sentimentList.get(j));
						else
							sentimentListEnsembled.add(sentimentList.get(i));
					} //else
				} // sameEntity Judge
			} // inner loop
		} //out loop
		return;
	}
	
	private Boolean sameEntity(String span1, String span2){
		// how to introduce the knowledge base and oreference chain together...?
		return false;
	}
}

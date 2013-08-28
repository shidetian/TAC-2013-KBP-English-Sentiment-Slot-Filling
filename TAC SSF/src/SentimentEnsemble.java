// ensemble (voting) different sentiment untis from different sentiment analysis system

// For any two sentiment units sharing the same holder and target, 
// the output will be only one

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import NE.NEReader;
import NE.NamedEntity;

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
	
	public void ensemble() throws SAXException, IOException, ParserConfigurationException{
		Hashtable<Integer, Integer> checked = new Hashtable<Integer, Integer>();
		
		for(int i=0;i<sentimentList.size();i++){
			if (checked.containsKey(i))
				continue;
			
			for (int j=i+1;j<sentimentList.size();j++){
				if (checked.containsKey(j))
					continue;
				
				if (sameHT(sentimentList.get(i), sentimentList.get(j))){
					
					checked.put(j, i);
					sentimentList.get(i).holderOffsets += ","+sentimentList.get(j).holderOffsets;
					sentimentList.get(i).targetOffsets += ","+sentimentList.get(j).targetOffsets;
					sentimentListEnsembled.add(sentimentList.get(i));
					
					/*
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
					} //else   */
				} // sameEntity Judge
			} // inner loop
			checked.put(i, i);
		} //out loop
		return;
	}
	
	private Boolean sameHT(SentimentUnit su1, SentimentUnit su2) throws SAXException, IOException, ParserConfigurationException{
		// how to introduce the knowledge base and coreference chain together...?
		Boolean holderFlag = false;
		Boolean targetFlag = false;
		
		int[] holderOffsets1 = {Integer.parseInt(su1.holderOffsets.split("-")[0]), Integer.parseInt(su1.holderOffsets.split("-")[1])};
		int[] holderOffsets2 = {Integer.parseInt(su2.holderOffsets.split("-")[0]), Integer.parseInt(su2.holderOffsets.split("-")[1])};
		int[] targetOffsets1 = {Integer.parseInt(su1.targetOffsets.split("-")[0]), Integer.parseInt(su1.targetOffsets.split("-")[1])};
		int[] targetOffsets2 = {Integer.parseInt(su2.targetOffsets.split("-")[0]), Integer.parseInt(su2.targetOffsets.split("-")[1])};
		
		NEReader NE1 = new NEReader(null);
		NE1.parseNEs(su1.docID);
		List<NamedEntity> holder1 = NE1.getNEs(holderOffsets1[0], holderOffsets1[1]);
		List<NamedEntity> target1 = NE1.getNEs(targetOffsets1[0], targetOffsets1[1]);
		
		NEReader NE2 = new NEReader(null);
		NE2.parseNEs(su2.docID);
		List<NamedEntity> holder2 = NE2.getNEs(holderOffsets2[0], holderOffsets2[1]);
		List<NamedEntity> target2 = NE2.getNEs(targetOffsets2[0], targetOffsets2[1]);
		
		for (NamedEntity ne1:holder1){
			for (NamedEntity ne2:holder2){
				if (ne1.entityid.equals(ne2.entityid)){
					holderFlag = true;
				}
			}
		}
		
		for (NamedEntity ne1:target1){
			for (NamedEntity ne2:target2){
				if (ne1.entityid.equals(ne2.entityid)){
					targetFlag = true;
				}
			}
		}
		
		if (holderFlag && targetFlag)
			return true;
		
		
		
		return false;
	}
}

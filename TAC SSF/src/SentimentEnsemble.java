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


public class SentimentEnsemble{
	
	private String path = "/home/carmen/KBP-annotations";
	
	public ArrayList<SentimentUnit> sentimentList;
	public ArrayList<SentimentUnit> sentimentListEnsembled;
	
	private int fuzzyEqualsLength = 3;
	
	public SentimentEnsemble(String cornellFile, String pittFile) throws IOException{
		this.sentimentList = new ArrayList<SentimentUnit>();
		this.sentimentListEnsembled = new ArrayList<SentimentUnit>();
		
		addSentimentFile(cornellFile);
		addSentimentFile(pittFile);
	}
	
	public SentimentEnsemble() throws IOException{
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
            		if (a[9].toLowerCase().equals("positive") || a[9].toLowerCase().equals("pos"))
            			a[9] = "pos";
            		else if (a[9].toLowerCase().equals("negative") || a[9].toLowerCase().equals("neg"))
            			a[9] = "neg";
            		else
            			continue;
            		
            		this.sentimentList.add(new SentimentUnit(a[0],a[1],a[2],a[3],a[4],a[5],
            				a[6], a[7], a[8], a[9], Double.parseDouble(a[10])));
            	}
            	br.close();
	}
	
	public void ensemble() throws SAXException, IOException, ParserConfigurationException{
		ArrayList<Integer> stored = new ArrayList<Integer>();
		Hashtable<Integer, Integer> itsLarger = new Hashtable<Integer, Integer>();
		
		for(int i=0;i<sentimentList.size();i++){
			if (stored.contains(i))
				continue;
			else
				stored.add(i);
			
			for (int j=i+1;j<sentimentList.size();j++){
				if (stored.contains(j))
					continue;
				
				SentimentUnit sui = sentimentList.get(i);
				if (itsLarger.containsKey(i))
					sui = sentimentList.get(itsLarger.get(i));
				SentimentUnit suj = sentimentList.get(j);
				if (itsLarger.containsKey(j))
					suj = sentimentList.get(itsLarger.get(j));
				
				if (!sui.polarity.equals(suj.polarity))
					continue;
				
				else if (sameExact(sui, suj)){
					stored.add(j);
					
					if (compareFirstLargerThanSecond(sui,suj)){
						itsLarger.put(j, i);
						// should put sui and remove suj
						if (sentimentListEnsembled.contains(sui) && !sentimentListEnsembled.contains(suj))
							continue;
						if (sentimentListEnsembled.contains(sui) && sentimentListEnsembled.contains(suj))
							sentimentListEnsembled.remove(suj);
						else if (!sentimentListEnsembled.contains(sui) && !sentimentListEnsembled.contains(suj))
							sentimentListEnsembled.add(sui);
						else if (!sentimentListEnsembled.contains(sui) && sentimentListEnsembled.contains(suj))
							sentimentListEnsembled.set(sentimentListEnsembled.indexOf(suj), sui);
					} // sui >= suj
					else{
						itsLarger.put(i, j);
						// should put suj and remove sui
						if (sentimentListEnsembled.contains(suj) && !sentimentListEnsembled.contains(sui))
							continue;
						if (sentimentListEnsembled.contains(suj) && sentimentListEnsembled.contains(sui))
							sentimentListEnsembled.remove(sui);
						else if (!sentimentListEnsembled.contains(suj) && !sentimentListEnsembled.contains(sui))
							sentimentListEnsembled.add(suj);
						else if (!sentimentListEnsembled.contains(suj) && sentimentListEnsembled.contains(sui))
							sentimentListEnsembled.set(sentimentListEnsembled.indexOf(sui), suj);
					} // sui < suj
				} // sameExact
				
				else if (sameHT(sui, suj)){
					stored.add(j);
					if (compareFirstLargerThanSecond(sui,suj)){
						itsLarger.put(j, i);
						// attach suj to sui
						sui.holderOffsets += ","+suj.holderOffsets;
						sui.targetOffsets += ","+suj.targetOffsets;
						
						if (sentimentListEnsembled.contains(sui) && !sentimentListEnsembled.contains(suj))
							sentimentListEnsembled.set(sentimentListEnsembled.indexOf(sui), sui);
						if (sentimentListEnsembled.contains(sui) && sentimentListEnsembled.contains(suj)){
							sentimentListEnsembled.set(sentimentListEnsembled.indexOf(sui), sui);
							sentimentListEnsembled.remove(suj);
						}
						else if (!sentimentListEnsembled.contains(sui) && !sentimentListEnsembled.contains(suj))
							sentimentListEnsembled.add(sui);
						else if (!sentimentListEnsembled.contains(sui) && sentimentListEnsembled.contains(suj))
							sentimentListEnsembled.set(sentimentListEnsembled.indexOf(suj), sui);
					} // sui >= suj
					else{
						itsLarger.put(i, j);
						// attach sui to suj
						suj.holderOffsets += ","+sui.holderOffsets;
						suj.targetOffsets += ","+sui.targetOffsets;
						
						if (sentimentListEnsembled.contains(suj) && !sentimentListEnsembled.contains(sui))
							sentimentListEnsembled.set(sentimentListEnsembled.indexOf(suj), suj);
						if (sentimentListEnsembled.contains(suj) && sentimentListEnsembled.contains(sui)){
							sentimentListEnsembled.set(sentimentListEnsembled.indexOf(suj), suj);
							sentimentListEnsembled.remove(sui);
						}
						else if (!sentimentListEnsembled.contains(suj) && !sentimentListEnsembled.contains(sui))
							sentimentListEnsembled.add(suj);
						else if (!sentimentListEnsembled.contains(suj) && sentimentListEnsembled.contains(sui))
							sentimentListEnsembled.set(sentimentListEnsembled.indexOf(sui), suj);
					} // sui < suj
				} // sameHT
			} // inner loop
		} //out loop
		return;
	}
	
	private Boolean compareFirstLargerThanSecond(SentimentUnit su1, SentimentUnit su2){
		if (su1.confidenceScore > su2.confidenceScore){
			return true;
		}
		else if (su1.confidenceScore == su2.confidenceScore){
			if (!(su1.sentenceSpan.length() > su2.sentenceSpan.length())){
				return true;
			}
		}
		
		return false;
	}
	
	private Boolean fuzzyEquals(String offsetString1, String offsetString2){
		int[] offsets1 = {Integer.parseInt(offsetString1.split("-")[0]), Integer.parseInt(offsetString1.split("-")[1])};
		int[] offsets2 = {Integer.parseInt(offsetString2.split("-")[0]), Integer.parseInt(offsetString2.split("-")[1])};
		
		if ( (offsets2[0] < offsets1[0]+fuzzyEqualsLength) && (offsets2[0] > offsets1[0]-fuzzyEqualsLength) &&
				(offsets2[1] < offsets1[1]+fuzzyEqualsLength) && (offsets2[1] > offsets1[1]-fuzzyEqualsLength) && 
				(offsets1[0] < offsets2[0]+fuzzyEqualsLength) && (offsets1[0] > offsets2[0]-fuzzyEqualsLength) &&
				(offsets1[1] < offsets2[1]+fuzzyEqualsLength) && (offsets1[1] > offsets2[1]-fuzzyEqualsLength) )
			return true;
		
		
		
		return false;
	}
	
	private Boolean sameExact(SentimentUnit su1, SentimentUnit su2){
		if (!(su1.polarity.equals(su2.polarity)))
			return false;
		if (su1.docID.equals(su2.docID) && 
				fuzzyEquals(su1.holderOffsets.split(",")[0],su2.holderOffsets.split(",")[0]) &&
				fuzzyEquals(su1.targetOffsets.split(",")[0],su2.targetOffsets.split(",")[0]) &&
				fuzzyEquals(su1.opinOffsets,su2.opinOffsets) )
			return true;
			
		return false;
	}
	
	private Boolean sameHT(SentimentUnit su1, SentimentUnit su2) throws SAXException, IOException, ParserConfigurationException{
		
		// how to introduce the knowledge base and coreference chain together...?
		Boolean holderFlag = false;
		Boolean targetFlag = false;
		
		int[] holderOffsets1 = {Integer.parseInt(su1.holderOffsets.split(",")[0].split("-")[0]), Integer.parseInt(su1.holderOffsets.split(",")[0].split("-")[1])};
		int[] holderOffsets2 = {Integer.parseInt(su2.holderOffsets.split(",")[0].split("-")[0]), Integer.parseInt(su2.holderOffsets.split(",")[0].split("-")[1])};
		int[] targetOffsets1 = {Integer.parseInt(su1.targetOffsets.split(",")[0].split("-")[0]), Integer.parseInt(su1.targetOffsets.split(",")[0].split("-")[1])};
		int[] targetOffsets2 = {Integer.parseInt(su2.targetOffsets.split(",")[0].split("-")[0]), Integer.parseInt(su2.targetOffsets.split(",")[0].split("-")[1])};
		
		NEReader NE1 = new NEReader(path);
		NE1.parseNEs(su1.docID);
		List<NamedEntity> holder1 = NE1.getNEs(holderOffsets1[0], holderOffsets1[1]);
		List<NamedEntity> target1 = NE1.getNEs(targetOffsets1[0], targetOffsets1[1]);
		
		NEReader NE2 = new NEReader(path);
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

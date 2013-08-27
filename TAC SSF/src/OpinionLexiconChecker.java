import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpinionLexiconChecker {
	public String sentence;
	public ArrayList<OpinWord> opList;
	public HashSet<String> polterms;
	public String polarity;
	
	
	private String gfLexiconFileName = "goodFor_manual.csv";
	private String bfLexiconFileName = "badFor_manual.csv";
	private String DSESELexiconFileName = "subjclueslen1-CL06sub.tff";
	private String GILexiconFileName = "GeneralInquiry.csv";
	private ArrayList<OpinWord> lexicon;
	private Hashtable<String, Integer> lexiconHash;
	
	public OpinionLexiconChecker() throws IOException{
		polarity = "neutral";
		lexicon = new ArrayList<OpinWord>();
		lexiconHash = new Hashtable<String, Integer>();
		opList = new ArrayList<OpinWord>();
		polterms = new HashSet<String>();
		gfbfRead();
		DSESERead();
		GIRead();
	}

	public HashMap<String, String> runOpinionWordChecker(String line) throws IOException{
		HashMap<String, String> results = new HashMap<String, String>();
		sentence = line;
		ArrayList<OpinWord> opList = lookUp(sentence);
		int positiveNum = 0;
		int negativeNum = 0;
		for (OpinWord ow:opList){
			if (ow.polarity.contains("pos"))
				positiveNum++;
			else if (ow.polarity.contains("neg"))
				negativeNum++;
			
		}
		if (positiveNum > negativeNum){
			results.put("0_"+String.valueOf(line.length()-1), "positive");
			polarity = "positive";
		}
		
		else if (positiveNum < negativeNum){
			results.put("0_"+String.valueOf(line.length()-1), "negative");
			polarity = "negative";
		}
		
		return results;
	}
	
	private ArrayList<OpinWord> lookUp(String line) throws IOException{
		opList = new ArrayList<OpinWord>();
		
		String[] wordsList = line.split(" ");
		for (int i=0;i<wordsList.length;i++){
			String word = wordsList[i];
			
			// lower case
			word = word.toLowerCase();
			// remove puntuation
			word = word.replaceAll("(\\w+)\\p{Punct}(\\s|$)", "$1$2"); 
			
			if (lexiconHash.containsKey(word)){
				opList.add(lexicon.get(lexiconHash.get(word)));
				polterms.add(word);
			}
			
			
		}
		
		return opList;
	}
	
	private void gfbfRead() throws IOException{
		File gfLexiconF = new File(gfLexiconFileName);
		FileReader gfLexiconFR = new FileReader(gfLexiconF);
		BufferedReader gfLexiconBR = new BufferedReader(gfLexiconFR);
		String line = "";
		while ( (line=gfLexiconBR.readLine())!= null ){
			Pattern gfPattern = Pattern.compile("^(.*?)\t(.*?)\t(.*?)$");
			Matcher gfMatcher = gfPattern.matcher(line);
			if (gfMatcher.find()){
				if (gfMatcher.group(2).contentEquals("v")){
					OpinWord op = new OpinWord();
					op.word = gfMatcher.group(1);
					op.lexicon = "gfbf";
					op.polarity = "goodfor";
					op.info = "v";
					
					lexicon.add(op);
					lexiconHash.put(op.word, lexicon.size()-1);
				}
			}
		}
		
		File bfLexiconF = new File(bfLexiconFileName);
		FileReader bfLexiconFR = new FileReader(bfLexiconF);
		BufferedReader bfLexiconBR = new BufferedReader(bfLexiconFR);
		line = "";
		while ( (line=bfLexiconBR.readLine())!= null ){
			Pattern bfPattern = Pattern.compile("^(.*?)\t(.*?)\t(.*?)$");
			Matcher bfMatcher = bfPattern.matcher(line);
			if (bfMatcher.find()){
				//if (bfMatcher.group(2).contentEquals("v")){
				OpinWord op = new OpinWord();
				op.word = bfMatcher.group(1);
				op.lexicon = "gfbf";
				op.polarity = "badfor";
				op.info = "v";
				
				lexicon.add(op);
				lexiconHash.put(op.word, lexicon.size()-1);
				//}
			}
		}
		
		
		return;
	}
	
	private void DSESERead() throws IOException{
		File DSESELexiconF = new File(DSESELexiconFileName);
		FileReader DSESELexiconFR = new FileReader(DSESELexiconF);
		BufferedReader DSESELexiconBR = new BufferedReader(DSESELexiconFR);
		String line = "";
		while ( (line=DSESELexiconBR.readLine())!= null ){
			Pattern DSESEPattern = Pattern.compile("word1=(.*?) pos1=(.*?) stemmed1=(.*?) priorpolarity=(.*?) ");
			Matcher DSESEMatcher = DSESEPattern.matcher(line);
			if (DSESEMatcher.find()){
				if (!DSESEMatcher.group(4).contentEquals("neutral")){
					OpinWord op = new OpinWord();
					op.word = DSESEMatcher.group(1);
					op.lexicon = "DSESE";
					op.polarity = DSESEMatcher.group(4);
					//op.info = DSESEMatcher.group(5).toUpperCase();
					
					lexicon.add(op);
					lexiconHash.put(op.word, lexicon.size()-1);
				}
			}
		}
		
		return;
	}
	
	private void GIRead() throws IOException{
		File GILexiconF = new File(GILexiconFileName);
		FileReader GILexiconFR = new FileReader(GILexiconF);
		BufferedReader GILexiconBR = new BufferedReader(GILexiconFR);
		String line = "";
		while ( (line=GILexiconBR.readLine())!= null ){
			String[] tmp = line.split("\t");
			if ( (tmp[2].equals("Positv") && tmp[3].equals("Negativ")) || (!tmp[2].equals("Positiv") && !tmp[3].equals("Negativ")) )
				continue;
			
			OpinWord op = new OpinWord();
			Pattern signPattern = Pattern.compile("#[0-9]+");
			Matcher signMatcher = signPattern.matcher(tmp[0]);
			if (signMatcher.find())
				tmp[0]=tmp[0].replace(signMatcher.group(), "");
			op.word = tmp[0].toLowerCase();
			op.lexicon = "GI";
			if (tmp[2].equals("Positiv"))
				op.polarity = "positive";
			else if (tmp[3].equals("Negativ"))
				op.polarity = "negative";
		}
		
		return;
	}
	
	
	
	public class OpinWord{
		public String word;
		public String polarity;
		public String lexicon;
		public String info;
		
		
		public OpinWord(){
			word = "";
			polarity = "";
			lexicon = "";
			info = "";
			
		}
	}

}

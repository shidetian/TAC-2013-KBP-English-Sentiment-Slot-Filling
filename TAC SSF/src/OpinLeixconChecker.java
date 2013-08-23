package opinWords;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpinLeixconChecker {
	
	public OpinLeixconChecker(String option) throws IOException{
		lexicon = new ArrayList<OpinWord>();
		lexiconHash = new Hashtable<String, Integer>();
		
		if (option.contains("gfbf"))
			gfbfRead();
		else if (option.contains("DSESE"))
			DSESERead();
		else if (option.contains("GI"))
			GIRead();
		else{
			gfbfRead();
			DSESERead();
			GIRead();
		}
	}
	
	private String gfLeixconFileName = "goodFor_manual.csv";
	private String bfLeixconFileName = "badFor_manual.csv";
	private String DSESELexiconFileName = "subjclueslen1-CL06sub.tff";
	private String GILexiconFileName = "GeneralInquiry.csv";
	//private String subjLexiconFileName = "";
	private ArrayList<OpinWord> lexicon;
	private Hashtable<String, Integer> lexiconHash;
	
	public ArrayList<OpinWord> lookUp(String line) throws IOException{
		ArrayList<OpinWord> opList = new ArrayList<OpinWord>();
		
		String[] wordsList = line.split(" ");
		for (int i=0;i<wordsList.length;i++){
			String word = wordsList[i];
			
			if (lexiconHash.containsKey(word))
				opList.add(lexicon.get(lexiconHash.get(word)));
			
		}
		
		return opList;
	}
	
	private void gfbfRead() throws IOException{
		File gfLeixconF = new File(gfLeixconFileName);
		FileReader gfLeixconFR = new FileReader(gfLeixconF);
		BufferedReader gfLeixconBR = new BufferedReader(gfLeixconFR);
		String line = "";
		while ( (line=gfLeixconBR.readLine())!= null ){
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
		
		File bfLeixconF = new File(bfLeixconFileName);
		FileReader bfLeixconFR = new FileReader(bfLeixconF);
		BufferedReader bfLeixconBR = new BufferedReader(bfLeixconFR);
		line = "";
		while ( (line=bfLeixconBR.readLine())!= null ){
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
		File DSESELeixconF = new File(DSESELexiconFileName);
		FileReader DSESELeixconFR = new FileReader(DSESELeixconF);
		BufferedReader DSESELeixconBR = new BufferedReader(DSESELeixconFR);
		String line = "";
		while ( (line=DSESELeixconBR.readLine())!= null ){
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
                File GILeixconF = new File(GILexiconFileName);
                FileReader GILeixconFR = new FileReader(GILeixconF);
                BufferedReader GILeixconBR = new BufferedReader(GILeixconFR);
                String line = "";
                while ( (line=GILeixconBR.readLine())!= null ){
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

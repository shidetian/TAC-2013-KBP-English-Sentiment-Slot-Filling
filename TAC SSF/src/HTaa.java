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

        public ArrayList<String> opinWords;
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

        public HTaa(ArrayList<String> opinWords, String s){
                this.opinWords = opinWords;
                sentence = s;
                p = new HTParser();

                holderCandidates = new String[opinWords.size()];
                targetCandidates = new String[opinWords.size()];
                holderHash = new Hashtable<String,Integer>();
                holderHash.put("", -1);
                targetHash = new Hashtable<String,Integer>();
                targetHash.put("", -1);

                p.getDependencyString(sentence);

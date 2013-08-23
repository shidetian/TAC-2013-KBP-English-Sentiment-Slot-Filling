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
                                extractHolder();
                extractTarget();
                System.out.println(holderCandidates.length);
                System.out.println(targetCandidates.length);
                System.out.println(holderHash.size());
                System.out.println(holderHash.containsKey("bb"));
                rank();
        }

        private void extractHolder(){
                for (int indexOpinWord=0;indexOpinWord<opinWords.size();indexOpinWord++){
                        String candidate = null;
                        int indexDependency =0;
                        System.out.println(opinWords.size());
                        System.out.println(opinWords.get(indexOpinWord));
                        System.out.println(holderDependency[indexDependency]);
                        while (candidate == null && indexDependency<holderDependency.length){
                                candidate = p.getTheOtherWord(opinWords.get(indexOpinWord),holderDependency[indexDependency],"dep");
                                indexDependency++;
                        }
                        holderCandidates[indexOpinWord] = candidate;
                }
                return;
        }

        private void extractTarget(){
                for (int indexOpinWord=0;indexOpinWord<opinWords.size();indexOpinWord++){
                        String candidate = null;
                        int indexDependency =0;
                        while (candidate == null && indexDependency<targetDependency.length){
                                candidate = p.getTheOtherWord(opinWords.get(indexOpinWord),targetDependency[indexDependency],"gov");
                                indexDependency++;
                        }
                        targetCandidates[indexOpinWord] = candidate;
                }
                return;
        }

        private void rank(){
                for (String holder: holderCandidates){
                        System.out.println("~~~"+holder);
                        if (holder == null)
                                continue;
                        if (holderHash.containsKey(holder)){
                                System.out.println("@##");
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
                        if (target == null)
                                continue;
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



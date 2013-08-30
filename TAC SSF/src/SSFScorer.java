import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class SSFScorer {
	public static ArrayList<Response> responseReader(String file) throws IOException{
		ArrayList<Response> out = new ArrayList<Response>();
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line = "";
		while ((line=in.readLine())!=null){
			String[] parts = line.split("\t");
			out.add(new Response(parts[0], Sentiment.fromString(parts[1]), parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], Double.parseDouble(parts[8])));
		}
		return out;
	}
	
	//Argument 1: key
	//Argument 2: input
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ArrayList<Response> correct = responseReader(args[0]);
		HashMap<String, HashSet<String>> scoreCard = new HashMap<String, HashSet<String>>();
		HashMap<String, Integer> numEntries = new HashMap<String, Integer>();
		for (Response s: correct){
			if (!scoreCard.containsKey(s.qId)){
				scoreCard.put(s.qId, new HashSet<String>());
			}
			scoreCard.get(s.qId).add(s.entity);
		}
		
		for (String s : scoreCard.keySet()){
			numEntries.put(s, scoreCard.get(s).size());
		}
		
		ArrayList<Response> answers = responseReader(args[1]);
		for (Response r: answers){
			if (scoreCard.get(r.qId).contains(r.entity)){
				scoreCard.get(r.qId).remove(r.entity);
			}
		}
		int total = 0;
		int numCorrect = 0;
		//Calculate scores
		for (String s: scoreCard.keySet()){
			total+=numEntries.get(s);
			int temp = numEntries.get(s) - scoreCard.get(s).size();
			numCorrect+=temp;
			System.out.println(s+" : "+ temp / (double)numEntries.get(s));
		}
		System.out.println("================");
		System.out.println(numCorrect+" out of "+total+" or "+ (double) numCorrect/(double)total);
	}

}

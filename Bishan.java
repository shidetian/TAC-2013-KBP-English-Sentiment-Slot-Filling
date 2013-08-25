	import java.io.*;
	import java.util.*;
	import org.apache.solr.client.solrj.SolrServerException;

	import edu.stanford.nlp.trees.Tree;
	import edu.stanford.nlp.trees.TreePrint;

class Bishan {

  	public static void main(String[] args) {
  	}
  	public Bishan(){}
  
  	public ArrayList<SentimentUnit> run(QueryBundle qb) 
    		throws InterruptedException,IOException,SolrServerException,ClassNotFoundException {

		ArrayList<SentimentUnit> sentimentUnits = new ArrayList<SentimentUnit>();

      		for(String docid : qb.docIds) {
			String rawText = SolrInterface.getRawDocument(docid);
			Object[] processed = Preprocessor.Tokenize(rawText);
			String offsets = (String) processed[0];
			FileWriter fw = new FileWriter("/home/ozan/pipeline/offsets.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(offsets);
			bw.close();
			fw = new FileWriter("/home/ozan/pipeline/input.txt");
			bw = new BufferedWriter(fw);
			bw.write(rawText);
			bw.close();
			//System.out.println(offsets);
			String tokens = (String) processed[1];
			//System.out.println(tokens);
			ArrayList<Tree> trees = (ArrayList<Tree>) Preprocessor.fromBase64((byte[]) processed[2]);

			//ProcessedDocument pd = SolrInterface.getProcessedDocument(docid);
        		//BufferedWriter bw = new BufferedWriter(new FileWriter("input.txt.stp"));
			PrintWriter pw = new PrintWriter("/home/ozan/pipeline/stanford-parser/data/input.txt.stp");
			for (Tree t : trees) {
				// write the required files to the required places
				TreePrint printer = new TreePrint("wordsAndTags,penn,typedDependencies");
				printer.printTree(t, pw);
			}
			pw.close();
        
        
			// Execute Bishan's pipeline
			ProcessBuilder builder = new ProcessBuilder("/bin/bash", "dseilp.sh");
			builder.directory(new File("/home/ozan/pipeline"));
			Process p = builder.start();
			InputStream is = p.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;

        		System.out.println("Output:");
           
			while ((line = br.readLine()) != null) {
			  System.out.println(line);
			}
      			// read output files and create a list of SentimentUnits
			br = new BufferedReader(new FileReader("/home/ozan/pipeline/outputlines.txt"));
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t");
				sentimentUnits.add(new SentimentUnit(docid,parts[0],
				parts[1],parts[2],parts[3],parts[4],parts[5],parts[6],
				parts[7],"",0.5));
			}
 
	
 			break; 
		}    
		return sentimentUnits; 
	}
}

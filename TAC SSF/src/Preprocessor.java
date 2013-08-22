import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.util.Base64;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;


public class Preprocessor {
	static LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
	static TokenizerFactory<CoreLabel> ptbtf = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible");
	
	public static String toBase64(Object o) throws IOException{
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(bao);
		os.writeObject(o);
		byte[] temp = bao.toByteArray();
		return Base64.byteArrayToBase64(temp, 0, temp.length);
	}
	
	public static Object fromBase64(String s) throws IOException, ClassNotFoundException{
		byte[] temp = Base64.base64ToByteArray(s);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(temp));
		return ois.readObject();
	}
	
	public static String[] Tokenize(String doc) throws IOException{
		//try{
		//for (String arg : s) {
		//PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new FileReader(f), new CoreLabelTokenFactory(), "invertible");
		//InputStreamReader in = new InputStreamReader(new GZIPInputStream(new FileInputStream(f)), "UTF-8");
		DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(doc));
		
		//TokenizerFactory<CoreLabel> ptbtf2 = ptbtf.newPTBTokenizerFactory(false, true);
		dp.setTokenizerFactory(ptbtf);

		//FileWriter offsetFile = new FileWriter(f.getAbsolutePath()+".offset");
		//FileWriter tokenFile = new FileWriter(f.getAbsolutePath()+".tokenized");
		StringBuffer offsets = new StringBuffer();
		StringBuffer tokens = new StringBuffer();
		//BufferedWriter offsetOut = new BufferedWriter(offsetFile);
		//BufferedWriter tokenOut = new BufferedWriter(tokenFile);
		ArrayList<Tree> treeObjs = new ArrayList<Tree>();
		for (List<HasWord> sentence : dp) {
			/*for (CoreLabel label : sentence) {
	        System.out.println(label);
	        System.out.println(label.get(CharacterOffsetBeginAnnotation.class));
	      }*/
			boolean start = true;
			for (HasWord w : sentence) {
				//CoreLabel label = ptbt.next();
				CoreLabel label = (CoreLabel) w;
				//System.out.println((label+"\t"+label.get(CharacterOffsetBeginAnnotation.class)+":"+label.get(CharacterOffsetEndAnnotation.class)) );
				if (start) {
					//tokenOut.write(""+label);
					tokens.append(""+label);
					start = false;
				} else {
					//tokenOut.write(" "+label);
					tokens.append(" "+label);
				}
				offsets.append(label.get(CharacterOffsetBeginAnnotation.class)+":"+label.get(CharacterOffsetEndAnnotation.class)+"\n");
			}
			//System.out.println();
			/*tokenOut.write("\n");
			offsetOut.write("\n");
			tokenOut.flush();
			offsetOut.flush();*/
			tokens.append("\n");
			offsets.append("\n");
			Tree output = lp.apply(sentence);
			//output.pennPrint();
			//TreePrint printer = new TreePrint("wordsAndTags,penn,typedDependencies");
			//printer.printTree(output);
			
			treeObjs.add(output);
		}
		//offsetOut.close();
		//tokenOut.close();
		//}
		/*
	      for (CoreLabel label; ptbt.hasNext(); ) {
	      	label = ptbt.next();
	        //System.out.println(("\""+label+"\" @@ "+ "\""+label.get(OriginalTextAnnotation.class)+"\""+"||\""+label.get(BeforeAnnotation.class)+"\":\""+label.get(AfterAnnotation.class)+"\""+"||"+label.get(CharacterOffsetBeginAnnotation.class)+"-"+label.get(CharacterOffsetEndAnnotation.class)).replaceAll("\n", "/n").replaceAll("\r","/r"));
	        System.out.println((label+"\t"+label.get(CharacterOffsetBeginAnnotation.class)+":"+label.get(CharacterOffsetEndAnnotation.class)) );
	        //System.out.println("===");
	      }*/
		//} catch (IOException e) {
		//}
		return new String[]{offsets.toString(),tokens.toString(),toBase64(treeObjs)};
	}
	
	public static void main(String[] args) throws IOException{
		String[] temp = Tokenize("aaa d wE the people. had a drink. It was fun.");
		for(String s: temp){
			System.out.println(s);
		}
	}
}

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class OutputWriter {
	//TODO make sure output in UTF-8
	//TODO allow multiple justifications?
	//TODO sanitize output (replace tabs or others w/ space)
	private ArrayList<Response> outList;
	
	public OutputWriter(){
		outList = new ArrayList<Response>();
		
	}
	
	public void addResponse(Response r){
		outList.add(r);
	}
	
	//Writes the responses to the specified file, with the most confident responses first if inOrder
	public void write(String filename, boolean inOrder) throws IOException{
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(filename)));
		Response[] temp = new Response[outList.size()];
		temp = (Response[]) outList.toArray(temp);
		if (inOrder){
			Arrays.sort(temp);
		}
		for (Response r : temp){
			out.write(r.qId);
			out.write("\t");
			out.write(r.sent.toString());
			out.write("\t");
			out.write(r.teamId);
			out.write("\t");
			out.write(r.justId);
			out.write("\t");
			if (r.justId.equals("NIL")){
				out.write("\n");
				continue;
			}
			out.write(r.entity);
			out.write("\t");
			out.write(r.fillerEntityOffsets);
			out.write("\t");
			out.write(r.queryEntityOffsets);
			out.write("\t");
			out.write(r.justificationOffsets);
			out.write("\t");
			out.write(r.confidence + "");
			out.write("\n");
		}
		out.close();
	}
	//This main method is just for example
	public static void main(String[] args) throws IOException{
		OutputWriter writer = new OutputWriter();
		//This is from the first sample response
		writer.addResponse(new Response("SSF_ENG_001", Sentiment.neg_from, "CORNELL", "bolt-eng-DF-170-181109-8867106", "whitehall", "5111-5119", "5287-5289", "5283-5401", 0.9));
		
		writer.write("output", true);
	}
}

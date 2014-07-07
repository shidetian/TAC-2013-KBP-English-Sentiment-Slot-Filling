import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

//This class splits a corpus file into individual documents
public class DocumentSplitter {
	//Pattern endTag = Pattern.compile("</[Dd][Oo][Cc]>");
	BufferedReader file;
	DocumentSplitter(InputStream file) throws UnsupportedEncodingException{
		this.file = new BufferedReader(new InputStreamReader(file, "UTF-8"));
	}
	
	//Returns the next document or null
	public String getNext() throws IOException{
		StringBuffer out = new StringBuffer();
		String current = "";
		int letter;
		while ((letter = file.read())!=-1){
			current+=(char)letter;
			if (letter=='\n'){
				out.append(current);
				if (current.toLowerCase().contains("</doc>")){
					return out.toString();
				}
				current = "";
			}
		}
		return null;
	}

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		DocumentSplitter ds = new DocumentSplitter(new GZIPInputStream(new FileInputStream(args[0])));
		String current;
		while ((current=ds.getNext())!=null){
			System.out.println(current+"\n=======");
		}
	}

}

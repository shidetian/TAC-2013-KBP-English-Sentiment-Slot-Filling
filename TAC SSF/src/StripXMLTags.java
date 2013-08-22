import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.StringUtils;


public class StripXMLTags {
	
	
	public static StringBuffer fix(String s){
		Pattern p = Pattern.compile("<QUOTE[^>]+>|& ");
		Matcher m = p.matcher(s);
		StringBuffer fixed= new StringBuffer();
		while (m.find()) {
			String spaces= StringUtils.repeat(" ", m.end()-m.start());
			/*String temp = m.group();
			//System.out.println(temp+"+++");
			if (temp!="")
				temp = "<QUOTE"+ temp.substring(6, temp.length()-2).replaceAll("\"", " ") + "/>".replace("QUOTE", "quote");
			if (temp.length()!=spaces.length())
				System.out.println("bad");*/
			m.appendReplacement(fixed, spaces);
		}
		m.appendTail(fixed);
		return fixed;
	}
	//Method to make the non-compliant QUOTE tag in web compliant by self closing the quote tag
		public static void fixFileCorpus(File file) throws Exception {
			String raw = ShowTextForOffsets.readRaw(file);
			
			StringBuffer fixed = fix(raw);
			int temp = 0;
			if ((temp = verify(raw, fixed))==-1){
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(new File(file.getAbsolutePath()+"_fixed")))));
				for (int i = 0; i< fixed.length(); i++){
					out.write(fixed.charAt(i));
				}
				out.close();
			}else{
				System.out.println("Test failed at "+ temp +". Offsets do not match. Contact Detian.");
			}
		}

	public static StringBuffer strip(String s){
		//Source: http://stackoverflow.com/questions/1334676/use-regexp-to-replace-xml-tags-with-whitespaces-in-the-length-of-the-tags
		Pattern p = Pattern.compile("<[^>]+>");
		Matcher m = p.matcher(s);
		StringBuffer stripped= new StringBuffer();
		while (m.find()) {
			String spaces= StringUtils.repeat(" ", m.end()-m.start());
			m.appendReplacement(stripped, spaces);
		}
		m.appendTail(stripped);
		return stripped;
	}

	//Replace all xml tags in a document with spaces
	public static void stripFileCorpus(File file) throws Exception {
		String raw = ShowTextForOffsets.readRaw(file);
		
		StringBuffer stripped = strip(raw);

		if (verify(raw, stripped)==-1){
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(new File(file.getAbsolutePath()+"_stripped")))));
			for (int i = 0; i< stripped.length(); i++){
				out.write(stripped.charAt(i));
			}
			out.close();
		}else{
			System.out.println("Test failed. Offsets do not match. Contact Detian.");
		}
	}

	//Check to see if a==b for all indexes of b that is not a space
	//-1 if same or the first index diff
	public static int verify(String a, StringBuffer b){
		if (a.length()!=b.length()){
			return 0;
		}

		for (int i =0; i<a.length(); i++){
			if (a.charAt(i)==b.charAt(i) || b.charAt(i)==' ' || b.charAt(i)=='/'){
				continue;
			}else{
				System.out.println(a.charAt(i)+" vs "+b.charAt(i));
				return i;
			}
		}
		return -1;
	}

	public static void main(String[] args) throws Exception {
		File input = new File(args[0]);
		File[] inputs;
		if (input.isDirectory()){
			inputs = input.listFiles();
		}else{
			inputs = new File[1];
			inputs[0] = input;
		}
		for (File f : inputs){
			if (!f.isDirectory()){
				fixFileCorpus(f);
				System.out.println(f.getName()+ " has been stripped.");
			}
		}
	}

}

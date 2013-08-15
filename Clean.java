import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Clean {
	
	public void cleanWeb(String fileName, String outputName) throws IOException{
		//String fileName = "";
		
		File docF = new File(fileName);
		FileReader docFR = new FileReader(docF);
		BufferedReader docBR = new BufferedReader(docFR);
		String line = "";
		
		File outputF = new File(outputName);
		FileWriter outputFW = new FileWriter(outputF);
		BufferedWriter outputBW = new BufferedWriter(outputFW);
		
		Boolean postBeginFlag = false;
		Boolean postEndFlag = false;
		
		while ((line = docBR.readLine())!= null){
			String outputLine = line;
			
			Pattern postBeginPattern = Pattern.compile("<POST>");
			Matcher postBeginMatcher = postBeginPattern.matcher(line);
			if (postBeginMatcher.find()){
				postBeginFlag = true;
				postEndFlag = false;
			}
			
			
			Pattern postEndPattern = Pattern.compile("</POST>");
			Matcher postEndMatcher = postEndPattern.matcher(line);
			if (postEndMatcher.find()){
				postEndFlag = true;
				postBeginFlag = false;
			}
			
			//only processing the content of a post
			if (postBeginFlag & !postEndFlag){
				
				//URL
				Pattern httpPattern = Pattern.compile("https?://[^ ]+");
				Matcher httpMatcher = httpPattern.matcher(line);
				while (httpMatcher.find()){
					//System.out.println(httpMatcher.group());
					String spaceSpan = "";
					for (int i=0;i<httpMatcher.group().length();i++)
						spaceSpan += " ";
					outputLine = outputLine.replace(httpMatcher.group(), spaceSpan);
					System.out.println(line+"...");
					System.out.println(outputLine+"...");
				}
				
				//quoted message line, <QUOTE PREVIOUSPOST=\"
				Pattern quotePattern = Pattern.compile("^<QUOTE PREVIOUSPOST=\"");
				Matcher quoteMatcher = quotePattern.matcher(line);
				while (quoteMatcher.find()){
					String spaceSpan = "";
					for (int i=0;i<quoteMatcher.group().length();i++)
						spaceSpan += " ";
					outputLine = outputLine.replace(quoteMatcher.group(), spaceSpan);
					System.out.println(line+"...");
					System.out.println(outputLine+"...");
				}
				
				//original message line, -----Original Message-----.
				Pattern originalPattern = Pattern.compile("(\\-)+Original Message(\\-)");
				Matcher originalMatcher = originalPattern.matcher(line);
				while (originalMatcher.find()){
					String spaceSpan = "";
					for (int i=0;i<originalMatcher.group().length();i++)
						spaceSpan += " ";
					outputLine = outputLine.replace(originalMatcher.group(), spaceSpan);
					System.out.println(line+"...");
					System.out.println(outputLine+"...");
				}
				
				//information of original message, such as
				//From: Louise Horio
				//Sent: Monday, February 25, 2008 8:23 PM
				//To: Louise Reda Horio
				Pattern infoPattern = Pattern.compile("^((From:)|(Sent:)|(To:)).*");
				Matcher infoMatcher = infoPattern.matcher(line);
				while (infoMatcher.find()){
					String spaceSpan = "";
					for (int i=0;i<infoMatcher.group().length();i++)
						spaceSpan += " ";
					outputLine = outputLine.replace(infoMatcher.group(), spaceSpan);
					System.out.println(line+"...");
					System.out.println(outputLine+"...");
				}
				
				//sentences only containing characters, such as **********
				Pattern charPattern = Pattern.compile("^[^a-zA-Z0-9]+$");
				Matcher charMatcher = charPattern.matcher(line);
				while (charMatcher.find()){
					String spaceSpan = "";
					for (int i=0;i<charMatcher.group().length();i++)
						spaceSpan += " ";
					outputLine = outputLine.replace(charMatcher.group(), spaceSpan);
					System.out.println(line+"...");
					System.out.println(outputLine+"...");
				}
				
				//html language symbol, such as &lt;
				Pattern htmlPattern = Pattern.compile("&[a-z0-9A-Z;]+");
				Matcher htmlMatcher = htmlPattern.matcher(line);
				while (htmlMatcher.find()){
					String spaceSpan = "";
					for (int i=0;i<htmlMatcher.group().length();i++)
						spaceSpan += " ";
					outputLine = outputLine.replace(htmlMatcher.group(), spaceSpan);
					System.out.println(line+"...");
					System.out.println(outputLine+"...");
				}
				
				//file, such as Funzug.org_3.jpg
				Pattern fileNamePattern = Pattern.compile("[a-zA-Z0-9\\._]+\\.[a-z]+");
				Matcher fileNameMatcher = fileNamePattern.matcher(line);
				while (fileNameMatcher.find()){
					String spaceSpan = "";
					for (int i=0;i<fileNameMatcher.group().length();i++)
						spaceSpan += " ";
					outputLine = outputLine.replace(fileNameMatcher.group(), spaceSpan);
					System.out.println(line+"...");
					System.out.println(outputLine+"...");
				}
				
				
			}
		
		outputBW.write(outputLine);
		outputBW.newLine();
		
		}
		
		docBR.close();
		docFR.close();
		outputBW.close();
		outputFW.close();
	}
}

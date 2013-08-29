import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class NEReader {

	/**
	 * @param args
	 */
	static String path;
	static HashMap<Integer, NamedEntity> NEset;
	
	public NEReader(String path){
		this.path = path;
		this.NEset = new HashMap<Integer, NamedEntity>();
	}
	
	public static List<NamedEntity> getNEs(int beg, int end){
		List<NamedEntity> nes = new ArrayList<NamedEntity>();
		
		Set<Integer> keyset = NEset.keySet();
		Iterator<Integer> iter = keyset.iterator();
		while(iter.hasNext()){
			int idx = iter.next();
			NamedEntity temp = NEset.get(idx);
			//System.out.println(idx + " : " + temp.entity + " , " + temp.entityid + " , " + temp.beg);
			
			if((idx >= beg) && (idx <= end)){
				//System.out.println("check! : " + idx + " , " + temp.entity);
				nes.add(NEset.get(idx));
			}
		}
		
		
		return nes;
	}
	
	public static void parseNEs(String docID) throws SAXException, IOException, ParserConfigurationException{
		String filepath = "";
		if(docID.startsWith("bolt-eng-DF")){
			String[] toks = docID.split(".p");
			filepath = path.concat("/discussion_forums/").concat(toks[0]).concat(".sgm.apf.new");
			//System.out.println("File: " + filepath);
		}
		else
			filepath = path.concat("/newswire/").concat(docID).concat(".sgm.apf.new");
		
		String XMLpath = filepath.concat(".xml");
		//System.out.println("XML: " + XMLpath);
		
		File temp = new File(XMLpath);
		if(!temp.isFile()){
			createXMLFile(filepath, XMLpath);
		}
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(XMLpath));
		doc.normalizeDocument();
		
		NEset = new HashMap<Integer, NamedEntity>();
		
		//System.out.println(doc.getDocumentElement().getNodeName());
		if (!doc.getDocumentElement().getNodeName().equals("source_file")){
			System.err.println("Root element not kbpsentslotfill. Wrong file?");
			return ;
		}
		NodeList entities = doc.getElementsByTagName("entity");
		//System.out.println(entities.getLength());
		
		for(int i=0; i<entities.getLength(); i++){
			Element entity = (Element) entities.item(i);
			String entityid = entity.getAttribute("ID");
			//System.out.println(entityid);
			NodeList mentions = entity.getElementsByTagName("entity_mention");
			//System.out.println("mention: " + mentions.getLength());
			NodeList resolutions = entity.getElementsByTagName("entity_resolution");
			//System.out.println("Resolution: " + resolutions.getLength());
			
			if(mentions.getLength() != resolutions.getLength()){
				System.out.println("File Error");
				return;
			}
			
			for(int j=0; j<mentions.getLength(); j++){
				Element mention = (Element) mentions.item(j);
				NodeList extents = mention.getElementsByTagName("extent");
				HashMap<String, Double> ERs = new HashMap<String, Double>();
				
				Element entity_resolution = (Element) resolutions.item(j);
				NodeList resolutionList = entity_resolution.getElementsByTagName("resolution");
				//System.out.println("# Resolution: " + resolutionList.getLength());
				
				for(int k=0; k<resolutionList.getLength(); k++){
					Element resolution = (Element)resolutionList.item(k);
					ERs.put(resolution.getTextContent(), Double.parseDouble(resolution.getAttribute("PROBABILITY")));
					//System.out.println(resolution.getTextContent() + " , " + Double.parseDouble(resolution.getAttribute("PROBABILITY")));
				}
				
				for(int k=0; k<extents.getLength(); k++){
					Element extent = (Element)extents.item(k);
					Element info = (Element)extent.getElementsByTagName("charseq").item(0);
					int beg = Integer.parseInt(info.getAttribute("START"));
					int end = Integer.parseInt(info.getAttribute("END"));
					String ne = info.getTextContent();
					
					NEset.put(beg, new NamedEntity(entityid, ne, beg, end, ERs));
				}
			}
		}

	}
	
	public static void createXMLFile(String filepath, String XMLpath) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(filepath));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(XMLpath), "UTF-8"));
		String line;
		
		while((line=reader.readLine()) != null){
			if(line.contains("<resolution PROBABILITY")){
				int beg = line.indexOf("=");
				int end = line.indexOf(">");
				writer.write(line.substring(0, beg+1));
				writer.write("\"");
				writer.write(line.substring(beg+1, end));
				writer.write("\"");
				writer.write(line.substring(end));
				writer.newLine();
			}
			else if(line.startsWith("<!DOCTYPE")){
				
			}
			else{
				writer.write(line);
				writer.newLine();
			}
		}
		
		reader.close();
		writer.close();
	}

	public static void main(String[] args) throws SAXException {
		// TODO Auto-generated method stub

		try {
			NEReader ner = new NEReader("temp");
			ner.parseNEs("bolt-eng-DF-170-181103-8881817");
			List<NamedEntity> results = ner.getNEs(0, 10000);

			for(NamedEntity result : results){
				System.out.println(result.entityid + " , " + result.entity + " , " + result.beg);
				System.out.println("  " + result.resolutions);
			}
			//System.out.println("# queries: " + qs.size());
		} catch (IOException e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			
		}
	}
}

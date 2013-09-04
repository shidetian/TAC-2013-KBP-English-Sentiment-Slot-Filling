import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class NEReader {

	/**
	 * @param args
	 */

	static HashMap<Integer, NamedEntity> NEset;
	
	public NEReader(){
		this.NEset = new HashMap<Integer, NamedEntity>();
	}
	
	public static NamedEntity getInformativeNE(String entityid){
		int max = -1;
		NamedEntity informative = null;
		
		Set<Integer> keyset = NEset.keySet();
		Iterator<Integer> iter = keyset.iterator();
		while(iter.hasNext()){
			int idx = iter.next();
			NamedEntity temp = NEset.get(idx);
			
			if(temp.entityid.compareTo(entityid) != 0)
				continue;
			
			if(temp.entity.length() > max){
				max = temp.entity.length();
				informative = temp;
			}
		}
		
		return informative;
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
	
	
	public static boolean getNEAnnotations(String docid) throws SolrServerException, SAXException, IOException, ParserConfigurationException{
		String anno = SolrInterface.getRawAnnotation(docid);
		
		if(anno == null)
			return false;
		
		anno = anno.replaceAll("PROBABILITY=", "PROBABILITY=\"");
		anno = anno.replaceAll(">E0", "\">E0");
		
		//System.out.println(anno);
		ByteArrayInputStream stream = new ByteArrayInputStream(anno.getBytes("UTF-8"));
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
		doc.normalizeDocument();
		
		NEset = new HashMap<Integer, NamedEntity>();
		
		NodeList entities = doc.getElementsByTagName("entity");
		//System.out.println(entities.getLength());
		
		for(int i=0; i<entities.getLength(); i++){
			Element entity = (Element) entities.item(i);
			String entityid = entity.getAttribute("ID");
			String type = entity.getAttribute("TYPE");
			//System.out.println(entityid);
			NodeList mentions = entity.getElementsByTagName("entity_mention");
			//System.out.println("mention: " + mentions.getLength());
			NodeList resolutions = entity.getElementsByTagName("entity_resolution");
			
			HashMap<String, Double> ERs = new HashMap<String, Double>();
			
			Element entity_resolution = (Element) resolutions.item(0);
			NodeList resolutionList = entity_resolution.getElementsByTagName("resolution");
				
			for(int j=0; j<resolutionList.getLength(); j++){
				Element resolution = (Element)resolutionList.item(j);
				ERs.put(resolution.getTextContent(), Double.parseDouble(resolution.getAttribute("PROBABILITY")));
				//System.out.println(resolution.getTextContent() + " , " + Double.parseDouble(resolution.getAttribute("PROBABILITY")));
			}
			
			for(int j=0; j<mentions.getLength(); j++){
				Element mention = (Element) mentions.item(j);
				NodeList extents = mention.getElementsByTagName("extent");
				
				for(int k=0; k<extents.getLength(); k++){
					Element extent = (Element)extents.item(k);
					Element info = (Element)extent.getElementsByTagName("charseq").item(0);
					int beg = Integer.parseInt(info.getAttribute("START"));
					int end = Integer.parseInt(info.getAttribute("END"));
					String ne = info.getTextContent();
					//System.out.println(ne + " , " + beg + " , " + end);
					NEset.put(beg, new NamedEntity(entityid, ne, type, beg, end, ERs));
				}
			}
		}

		return true;
	}

}

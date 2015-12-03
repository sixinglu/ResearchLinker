/***********************************************
 * Author: Sixing Lu
 * Creation Date: 10/28/2015
 * Function: read html and create docs
 ***********************************************/
package com.mkyong.seo;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.io.File;

public class readXML{
	
	final static int k = 100;
	MLtrainData traininput = new MLtrainData();
	
	/***************************************************************
     * this function parse XML collection
     * @param XML file, zooms after parse, map of author&affiliation
     * @return void
	 **************************************************************/
	 public void  readcitation(String filedir, ArrayList<ArrayList<String>> papers, HashMap<String,String> author_affiliation, String keywordsfile, String mintime, String maxtime, String option) {
	    try {
	    	// memory for training, comment out when run the tool
	    	ArrayList<String> coded =new ArrayList<String>();
	    	ArrayList<String> summaries =new ArrayList<String>();
	    	HashMap<String,Integer> wordTable = new HashMap<String,Integer>();
	    	//long mintime = Long.MAX_VALUE, maxtime = Long.MIN_VALUE;
//	    	mintime="2018";
//	    	maxtime="0";
	
	    	File filename = new File(filedir);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(filename);
					
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
		
			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
					
			NodeList nList = doc.getElementsByTagName("entry");
					
			//System.out.println("----------------------------");
		
			for (int temp = 0; temp < nList.getLength(); temp++) {
				ArrayList<String> onepaper =new ArrayList<String>();
				Node nNode = nList.item(temp);
						
				//System.out.println("\nCurrent Element :" + nNode.getNodeName());
						
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		
					Element eElement = (Element) nNode;	
					// add title zoom
					//System.out.println("title : " + eElement.getElementsByTagName("title").item(0).getTextContent());
					String title =eElement.getElementsByTagName("title").item(0).getTextContent();
					title = title.replaceAll("\n","");
					title = title.trim().replaceAll(" +", " ");
					onepaper.add(title);	
		
									
					// add author zoom
					String author =new String();
					String Affiliation = new String();
					Integer authorNO = eElement.getElementsByTagName("author").getLength();
					ArrayList<String> author_pair =new ArrayList<String>();
					
					for(int i=0; i<authorNO; i++){
						Element subauthor = (Element)eElement.getElementsByTagName("author").item(i);
						String name = subauthor.getElementsByTagName("name").item(0).getTextContent();  // get <name>
						String afflication =" ";
						if(subauthor.getElementsByTagName("arxiv:affiliation").getLength()!=0){   // get <affiliation>
							afflication = subauthor.getElementsByTagName("arxiv:affiliation").item(0).getTextContent();
						}
						else{
							afflication = "empty";
						}

						//System.out.println("author : " + name);
						//System.out.println("affiliation: " + afflication);
					    author = author + "," + name;    // name zoom
					    Affiliation = Affiliation + "," + afflication;  // affiliation zoom
					    author_pair.add(name+"*"+afflication+"*");  // for training ,can be comment out
						
					    // create a hash between author and affiliation
					    if(author_affiliation.get(name)==null){
					    	author_affiliation.put(name,afflication); 
					    }
					    else{
					    	author_affiliation.replace(name, afflication); //update to new affiliation
					    }
					    
					}
					onepaper.add(author);
					onepaper.add(Affiliation);
								
					// add time zoom
					//System.out.println("published time : " + eElement.getElementsByTagName("published").item(0).getTextContent());
					String time = eElement.getElementsByTagName("published").item(0).getTextContent();
					onepaper.add(time);
					if(time.compareTo(mintime)<0){
						mintime = time;
					}
					if(time.compareTo(maxtime)>0){
						maxtime = time;
					}
					// add summary zoom
					//System.out.println("summary : " + eElement.getElementsByTagName("summary").item(0).getTextContent());
					String summary = eElement.getElementsByTagName("summary").item(0).getTextContent();
					onepaper.add(summary);
					papers.add(onepaper);
					
					
		///// for training phase, comment out when running the tool /////
					if(option.equals("-g")){
						for(int i=0;i<authorNO; i++ ){
							StringBuffer codeline = new StringBuffer();
							codeline.append(author_pair.get(i));
							codeline.append(title+"*");
							codeline.append(time);
							coded.add(codeline.toString());
	//						coded.add(author_pair.get(i)+" \'"+title+"\' "+time);
							summaries.add(summary);
						}
						traininput.cal_freq(summary,wordTable);
					}
		////////////////////////////////////////////////////////////////
					
				}						
			}
			
		    ///// for training phase, comment out when running the tool /////
			if(option.equals("-g")){
				traininput.GatherInfo(coded, summaries, wordTable, mintime, maxtime, keywordsfile);
			}
	        ////////////////////////////////////////////////////////////////
			
			//return papers;
		} 
	    catch (Exception e) {
			e.printStackTrace();
			//return null;
	    }
    }
}
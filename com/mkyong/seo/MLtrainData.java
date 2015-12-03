/***********************************************
 * Author: Sixing Lu
 * Creation Date: 11/16/2015
 * Function: output training data for Machine Learning
 ***********************************************/

package com.mkyong.seo;
import java.lang.*;
import java.math.BigInteger;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MLtrainData{
	
	private Integer intervalNO = 4;
	
	/*******************************************************
	 * calculate frequency of all words
	 * used for machine learning input
	 * @param summary string, existing word-freq table
     * @return update word-freq table
	 ******************************************************/
	 public void cal_freq(String summary,HashMap<String,Integer> wordTable){
		List<String> words= new ArrayList<String>();
		words =Arrays.asList(summary.split("\\s*(,|\\s)\\s*"));
		for(int i=0; i<words.size(); i++){
			if(words.get(i)==""){
				continue;
			}
			if(wordTable.containsKey(words.get(i))){
				Integer freq = wordTable.get(words.get(i)) + 1;
				wordTable.replace(words.get(i), freq);
			}
			else{
				wordTable.put(words.get(i), 1);
			}
		}
	 }

	/*******************************************************
	 * sort words based on frequency
	 * used for machine learning input
	 * @param  word-freq table
	 * @return void
	 ******************************************************/
	 public Map<String, Integer> sortWord(HashMap<String,Integer> wordTable){
		 Map<String, Integer> testMap = new HashMap<String, Integer>(wordTable);
		 testMap = MapUtil.sortByValue( testMap );
		 return testMap;
	 }	 
	
	/*******************************************************
	 * calculate frequency of keyword in one summary 
	 * used for machine learning input
     * @param summary string, keyword list
     * @return encoded keywork vector
	 ******************************************************/
	 public ArrayList<Integer> encodeSummary(String summary, ArrayList<String> keywords){
		ArrayList<Integer> code = new ArrayList<Integer>();
		HashMap<String,Integer> localTable = new HashMap<String,Integer>();
		cal_freq(summary,localTable);
		for(String entry: keywords){
			if(localTable.containsKey(entry)){
				code.add(localTable.get(entry));
			}
			else{
				code.add(0);
			}
			
		}
		return code;
	}

	/*******************************************************
	 * run at training pharse 
	 * used for machine learning input
     * @param encoding ArrayList<String>, Summary ArrayList<String>,
     *        ArrayList<String>keywords, HashMap<String,Integer> wordTable
     * @return encoded keywork vector
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 ******************************************************/
	 public void GatherInfo(ArrayList<String> coded, ArrayList<String> summaries, HashMap<String,Integer> wordTable, String mintime, String maxtime, String keywordsfile) throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter analyzeFreq = new PrintWriter("wordfreq.txt", "UTF-8");
		//PrintWriter parselog = new PrintWriter("parselog.txt", "UTF-8");
		ArrayList<String> parselog = new ArrayList<String>();
		Map<String,Integer> sorted =sortWord(wordTable);
		int i=0;
		
		//////////// print freq of words to tell which are keys /////////
		 
		for(Map.Entry<String,Integer> entry: sorted.entrySet() ){
//			if(i>=k){
//				break;
//			}
			double dft=0;
			analyzeFreq.print(entry.getKey());
			analyzeFreq.print(" ");
//			analyzeFreq.print(entry.getValue());
//			analyzeFreq.print(" ");
			
			for(String summery:summaries ){
				if(summery.contains(entry.getKey())){
					dft++;
				}
			}
			analyzeFreq.print(entry.getValue()*Math.log10(summaries.size()/dft));
			
			analyzeFreq.print("\n");
			i++;
		}	
		analyzeFreq.close();
		
		ArrayList<String> keywords = new ArrayList<String>();
		if(!keywordsfile.equals("")){
			BufferedReader words = null;
			try {
				String Currentdoc;
				words = new BufferedReader(new FileReader(keywordsfile));
				while ((Currentdoc = words.readLine()) != null) {
					keywords.add(Currentdoc.toLowerCase());		
				}
			} 
			catch (IOException e) {
				e.printStackTrace();
			} 
			finally {
				try {
					if (words != null)words.close();
				} 
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		
//		keywords.add("interaction");
//		keywords.add("quantum");
//		keywords.add("scattering");
//		keywords.add("spin");
//		keywords.add("magnetic");
//		keywords.add("electron-phonon");
//		keywords.add("density");
//		keywords.add("coupling");
//		keywords.add("transport");
//		keywords.add("Coulomb");
//		keywords.add("Fermi");
//		keywords.add("gas");
		
		//parselog.println("name affiliation paper time abstract");
		//////////// write to log ///////////
		ArrayList<ArrayList<Integer>> allfreq = new  ArrayList<ArrayList<Integer>>();
		for(i=0;i<coded.size(); i++ ){
			//StringBuffer temp = new StringBuffer();
			ArrayList<Integer> code = encodeSummary(summaries.get(i), keywords);
			allfreq.add(code);
			//temp.append(coded.get(i)+" \'");
			//temp.append(code);
			//temp.append("\'");
			//parselog.add(temp.toString());
		}
		//parselog.close();
		
		////// re-parse training input //////
		WriteTrainingLog(coded, allfreq, mintime, maxtime);		
	}

	/*******************************************************
	 * run at training pharse 
	 * used for machine learning input, publish time separated
	 * @param parselog
	 * @return a file contain ML input data
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 ******************************************************/
	 public void WriteTrainingLog(ArrayList<String> coded, ArrayList<ArrayList<Integer>> allfreq, String mintime, String maxtime) throws FileNotFoundException, UnsupportedEncodingException{
		 PrintWriter MLinput = new PrintWriter("MLinput.txt", "UTF-8");
		 Map<String, ArrayList<Integer> > AffliPerAuthor = new HashMap<String,ArrayList<Integer> >();
		 Map<String, ArrayList<ArrayList<Integer>>> MLdatalineInterval = new HashMap<String, ArrayList<ArrayList<Integer>>>();
		 HashMap<String,Integer> affiCode = new HashMap<String,Integer>();
		 //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ");
		 
		 try{
		 Integer MAXtime = Integer.parseInt(maxtime.substring(0,4)); 
		 Integer MINtime=  Integer.parseInt(mintime.substring(0,4));
		 Integer span = (MAXtime - MINtime)/intervalNO;   // real intervalNO will +1, because use integer
		 
		 int count =0;
		 for(String line:coded){
			 List<String> info= new ArrayList<String>();
			 info =Arrays.asList(line.split("\\*"));
			 String author="", affiliation="", paper="";
			 Integer time=0;
			 int i=0,j=0;
			 for(String empty: info){
				 empty.replaceAll("\\s+", " ");
				 if( !(empty.equals(" ") || empty.isEmpty()) ){
					 // seperate the line
					 switch (j){
						 case 0: author = info.get(i); break;
						 case 1: affiliation = info.get(i); break;
						 case 2: paper = info.get(i); break;
						 case 3: time = Integer.parseInt(info.get(i).substring(0,4)); break;
						 default: break;
					 }
					 j++;
				 }
				 i++;
			   }
			   ArrayList<Integer> current = allfreq.get(count);
			   
//			   if(author.equals("R. Laiho")){
//				   System.out.println("debug");
//			   }
			   // re-organize keywords freq
			   int span_loc = (time-MINtime)/span;
			   if(MLdatalineInterval.containsKey(author)){  // already has this author
				  // ArrayList<ArrayList<Integer>> test = MLdatalineInterval.get(author);
				    ArrayList<Integer> ori = MLdatalineInterval.get(author).get(span_loc);
				    for(int g=0; g<ori.size();g++){
						ori.set( g, ori.get(g) + current.get(g) );
				    }
				    MLdatalineInterval.get(author).set(span_loc, ori);
				}
				else{
					 ArrayList<ArrayList<Integer>> Intervals = new ArrayList<ArrayList<Integer>>(); // four intervals
					 for(int k=0; k<intervalNO+1;k++){  // initilization
						 ArrayList<Integer> zeros = new ArrayList<Integer>();
						 for(int p=0;p<allfreq.get(0).size();p++){
							 zeros.add(0);
						 }
						 Intervals.add(zeros);		 
					 }
					//ArrayList<ArrayList<Integer>> iniIntervals = new ArrayList<ArrayList<Integer>>(Intervals);
					//iniIntervals.set(span_loc, current);
					//MLdatalineInterval.put(author, iniIntervals);
					Intervals.set(span_loc, current);
				    MLdatalineInterval.put(author, Intervals);
				}
			   
			   // re-organize affilications
			   Integer affencode = -1;
			   if(!affiliation.equals("empty")){
				   affencode = encode_affiliation(affiCode, affiliation);
			   }
			   if(AffliPerAuthor.containsKey(author)){ // already has this author
				   if(!AffliPerAuthor.get(author).contains(affencode)){
					   AffliPerAuthor.get(author).add(affencode);  // add affiliation
				   }
			   }
			   else{
				   ArrayList<Integer> startAff = new  ArrayList<Integer>();
				   startAff.add(affencode);
				   AffliPerAuthor.put(author, startAff);
			   }		 
			 count++;
		 }
		 
		 // write into the file
		 MLinput.println("author affiliation time1 time2 time3 time4 time5");
		 Iterator it = AffliPerAuthor.entrySet().iterator();
		 BigInteger authors_code = BigInteger.valueOf(0);
		 while (it.hasNext()) {
	         Map.Entry pair = (Map.Entry)it.next();
		     //MLinput.print("["+pair.getKey()+"] ");
	         MLinput.print(authors_code+" ");
		     MLinput.print(pair.getValue()+" ");
		     MLinput.println(MLdatalineInterval.get(pair.getKey()));
		     
		     authors_code = authors_code.add(BigInteger.valueOf(1));
		 }
		 
		 MLinput.close();
		 }
		 catch(Exception e){
				System.out.println(e.getMessage());
			}
	 }
	 
	 
    /*******************************************************
     * this function encode affiliation string to int
     * used for machine learning input
     * @param all affiliations in string, and encode target
     * @return code in the form of integer
	 ******************************************************/
	 private Integer encode_affiliation(HashMap<String,Integer> affiliation_encoding, String affil){
		 if(affiliation_encoding.isEmpty()){
			 affiliation_encoding.put(affil, 0);
			 return 0;
		 }
		 if(affiliation_encoding.containsKey(affil)){
			 return affiliation_encoding.get(affil);
		 }
		 else{
			 Integer code= Collections.max(affiliation_encoding.values()) +1; 
			 affiliation_encoding.put(affil, code);
			 return code;
		 }

	 }
}
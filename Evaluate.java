/***********************************************
 * Author: Sixing Lu
 * Creation Date: 11/25/2015
 * Function: create testfile and evaluate the tool
 ***********************************************/

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;

import com.mkyong.seo.MapUtil;

public class Evaluate{
	Integer depth;
	Integer width;
	Integer testQueryNUM;
	
	
	public void setDepth(Integer dep){
		depth = dep;
	}
	public void setWidth(Integer wid){
		width = wid;
	}
	public void settestQueryNUM(Integer NUM){
		testQueryNUM = NUM;
	}
	
	/*******************************************************
	* calculate accuracy of basic search
	* @param documents, combination of authors
	* @return accuracy of basic search
	******************************************************/
	public double evaluate_basic_accuracy(ArrayList<ArrayList<String>> papers, ArrayList<ArrayList<String>> author_pairs) throws IOException, IndexOutOfBoundsException, ParseException{
		double accuracy = 0.0;	
		Integer setdepth =depth;
		Integer setwidth =width; 
		
		try{
			BasicSearch basic_search = new BasicSearch(setdepth,setwidth,papers);
			BruteForce accur_search = new BruteForce(setdepth,setwidth,papers);
		
			for(int i=0; i<testQueryNUM; i++){  // generate randomNum 100 times
				int randomNum = 0 + (int)(Math.random()*author_pairs.size()); 
				String querystr1 = author_pairs.get(randomNum).get(0);
				String querystr2 = author_pairs.get(randomNum).get(1);
				LinkedList<String> basicPath= basic_search.basicSearch(querystr1,querystr2);
				LinkedList<String> bruteForce = accur_search.bruteforceSearch(querystr1,querystr2);
				String basic="", brute="";
				if(basicPath!=null){
					basic = basicPath.toString();
				}
				if(bruteForce!=null){
					brute = bruteForce.toString();
				}
				if(basic.equals(brute)){
					accuracy++;
				}
				
				querystr1 = null;
				querystr2 = null;
				basicPath = null;
				bruteForce = null;				
			}
			
			basic_search = null;
			accur_search = null;
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
		return accuracy/testQueryNUM;
	}
	
	/*******************************************************
	* calculate execution time of basic search
	* @param documents, combination of authors
	* @return execution time of basic search
	******************************************************/
	public double evaluate_basic_performance(ArrayList<ArrayList<String>> papers, ArrayList<ArrayList<String>> author_pairs) throws IOException{
		double performance = 0.0;
		Integer setdepth =depth;
		Integer setwidth =width; 	
		try{
			BasicSearch basic_search = new BasicSearch(setdepth,setwidth,papers);
			long start = System.nanoTime();
			for(int i=0; i<testQueryNUM; i++){  // generate randomNum 100 times
				int randomNum = 0 + (int)(Math.random()*author_pairs.size()); 
				String querystr1 = author_pairs.get(randomNum).get(0);
				String querystr2 = author_pairs.get(randomNum).get(1);
				basic_search.basicSearch(querystr1,querystr2);
				
				querystr1 = null;
				querystr2 = null;
			}	
			long end = System.nanoTime();
			performance = (end-start)/testQueryNUM;
			
			basic_search = null;
			
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
		return performance;
	}
	
	/*******************************************************
	* compare accuracy between basic search and ML search
	* @param documents, combination of authors, keywords
	* @return ML accuracy 
	******************************************************/
	public double evaluate_MLcompare_accuracy(ArrayList<ArrayList<String>> papers, ArrayList<ArrayList<String>> author_pairs, String mintime, String maxtime, String keywordsfile, String coeffFile ) throws IOException, IndexOutOfBoundsException, ParseException{
		//double accuracy_basic = 0.0;	
		double accuracy_ML = 0.0;
		Integer setdepth =depth;
		Integer setwidth =width; 
		
		try{
			//BasicSearch basic_search = new BasicSearch(setdepth,setwidth,papers);
			BruteForce accur_search = new BruteForce(setdepth,setwidth,papers);
			MLsearch mlsearch = new MLsearch(depth,width,papers);
		
			for(int i=0; i<testQueryNUM; i++){  // generate randomNum 100 times
				int randomNum = 0 + (int)(Math.random()*author_pairs.size()); 
				String querystr1 = author_pairs.get(randomNum).get(0);
				String querystr2 = author_pairs.get(randomNum).get(1);
				//LinkedList<String> basicPath= basic_search.basicSearch(querystr1,querystr2);
				LinkedList<String> bruteForce = accur_search.bruteforceSearch(querystr1,querystr2);
				LinkedList<String> MLPath = mlsearch.machinelearningSearch(querystr1,querystr2, mintime, maxtime, keywordsfile, coeffFile);
				String basic = "", brute="", ML="";
//				if(basicPath!=null){
//					basic = basicPath.toString();
//				}
				if(bruteForce!=null){
					brute = bruteForce.toString();
				}
				if(MLPath!=null){
					ML = MLPath.toString();
				}
				
//				if(basic.equals(brute)){
//					accuracy_basic++;
//				}
				if(ML.equals(brute)){
					accuracy_ML++;
				}
				
				//basicPath = null;
				bruteForce = null;
				MLPath = null;
				querystr1 = null;
				querystr2 = null;
			}
			
			//basic_search = null;
			accur_search = null;
			
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
		return (accuracy_ML)/testQueryNUM;
	}
	
	/*******************************************************
	* compare spped between basic search and ML search
	* @param documents, combination of authors, keywords
	* @return ML performance
	******************************************************/
	public double evaluate_MLcompare_speedup(ArrayList<ArrayList<String>> papers, ArrayList<ArrayList<String>> author_pairs, String mintime, String maxtime, String keywordsfile, String coeffFile ) throws IOException, IndexOutOfBoundsException, ParseException{
//		double total_basic = 0.0;	
		double total_ML = 0.0;
		Integer setdepth =depth;
		Integer setwidth =width; 
//		long start_basic =0;
		long start_ML =0;
		try{
			BasicSearch basic_search = new BasicSearch(setdepth,setwidth,papers);
			MLsearch mlsearch = new MLsearch(depth,width,papers);
		
			for(int i=0; i<testQueryNUM; i++){  // generate randomNum 100 times
				int randomNum = 0 + (int)(Math.random()*author_pairs.size());
				String querystr1 = author_pairs.get(randomNum).get(0);
				String querystr2 = author_pairs.get(randomNum).get(1);
				
//				start_basic = System.nanoTime();
//				LinkedList<String> basicPath= basic_search.basicSearch(querystr1,querystr2);
//				total_basic = total_basic + (System.nanoTime()-start_basic);
				
				start_ML = System.nanoTime();
				LinkedList<String> MLPath = mlsearch.machinelearningSearch(querystr1,querystr2, mintime, maxtime, keywordsfile,coeffFile );
				total_ML = total_ML + (System.nanoTime()-start_ML);
			}	
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
		return total_ML/testQueryNUM;
	}
	
	/*******************************************************
	* calculate all query combination's correct path
	* the output file is used for machine learning
	* it takes very long to execute
	* @param documents, combination of authors
	* @return file all correct path
	******************************************************/
	public void correctanswer(ArrayList<ArrayList<String>> papers, ArrayList<ArrayList<String>> author_pairs) throws IOException, ParseException{
		PrintWriter allCombination = new PrintWriter("allCombination.txt", "UTF-8");
		BruteForce accur_search = new BruteForce(depth,width,papers);
		
		int count = author_pairs.size()-1;
		for(; count>0; count--){
			String querystr1 = author_pairs.get(count).get(0);
			String querystr2 = author_pairs.get(count).get(1);

		    LinkedList<String> connectPath = accur_search.bruteforceSearch(querystr1,querystr2);
			allCombination.println("-- "+querystr1+" -- "+querystr2+" --");
			if(connectPath!=null){
				allCombination.println(connectPath);
			}
			
			if(count%1000==0){
				System.out.println(count);
			}	
		}
		allCombination.close();
		
	}
}

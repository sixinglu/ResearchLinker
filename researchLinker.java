/***********************************************
 * Author: Sixing Lu
 * Creation Date: 10/28/2015
 * Function: main function
 ***********************************************/

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
//import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

//import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.document.Field;
//import org.apache.lucene.document.StringField;
//import org.apache.lucene.document.TextField;
//import org.apache.lucene.index.DirectoryReader;
//import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.index.IndexWriter;
//import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.paukov.combinatorics.combination.simple.createTest;

import com.mkyong.seo.MapUtil;
//import org.apache.lucene.queryparser.classic.QueryParser;
//import org.apache.lucene.search.BooleanClause;
//import org.apache.lucene.search.BooleanQuery;
//import org.apache.lucene.search.IndexSearcher;
//import org.apache.lucene.search.Query;
//import org.apache.lucene.search.ScoreDoc;
//import org.apache.lucene.search.TopScoreDocCollector;
//import org.apache.lucene.search.similarities.BM25Similarity;
//import org.apache.lucene.store.Directory;
//import org.apache.lucene.store.RAMDirectory;
//import org.apache.lucene.util.Version;
import com.mkyong.seo.readXML;


public class researchLinker 
{
	public static void main(String[] args) throws IOException, ParseException
	{
		try
		{
			// read collection from XML file
			readXML reading = new readXML();  
			ArrayList<ArrayList<String>> papers = new ArrayList<ArrayList<String>>();
	    	HashMap<String,String> author_affiliation = new HashMap<String,String>();
	    	String mintime ="2018";  // the latest year 
	    	String maxtime = "0";
	    	if(args.length>=5){
	    		// if enable -g, will generate files for ML training
	    		reading.readcitation(args[1], papers,author_affiliation,args[2],mintime, maxtime, args[0]); 
	    	}
	    	else{
	    		System.out.println("USAGE: researchLinker -b 'workfile' 'keywords' 'authorA' 'authorB' ");
	    		return;
	    	}
			
			//	build a query
			String querystr1="",querystr2="";
			querystr1 = args[3];
			querystr2 = args[4];
			
			
			// basic search
			if(args[0].equals("-b")){
				Integer depth =5;
				Integer width =10; // search how many paper for this author
				BasicSearch basic_search = new BasicSearch(depth,width,papers);
				LinkedList<String> strongestPath= basic_search.basicSearch(querystr1,querystr2);
				System.out.println(strongestPath);
				return;
			}
		
			// create correct answer for ML
			if(args[0].equals("-t")){
				createTest testInput = new createTest();
				ArrayList<ArrayList<String>> author_pairs = testInput.create_authorPair(author_affiliation);
				Evaluate evaObj = new Evaluate();
				evaObj.setDepth(5);
				evaObj.setWidth(10);
				evaObj.correctanswer(papers, author_pairs);
				return;
			}
			
			// ML search
			if(args[0].equals("-m")){
				Integer depth =5;
				Integer width =10;
				MLsearch mlsearch = new MLsearch(depth,width,papers);
				LinkedList<String> strongestPath = mlsearch.machinelearningSearch(querystr1,querystr2, mintime, maxtime, args[2], args[5]);
				System.out.println(strongestPath);
				return;
			}
			
			// evaluation, it takes very very long, and a lot of memory with multiple threads
			if(args[0].equals("-e")){
				int QueryNUM = 100;
				createTest testInput = new createTest();
				ArrayList<ArrayList<String>> author_pairs = testInput.create_authorPair(author_affiliation);
							
				// evaluate width
				for(int i=2; i<=10; i=i+2){
					int depth = 5; 
					MultiThreadEvaluate R = new MultiThreadEvaluate("thread_ew"+(i/2),depth,i,QueryNUM, author_pairs,papers,args[2],args[5]);
					R.start();
				}
				
				// evaluate depth
				for(int i=1; i<=5; i++){
					int width = 10;
					MultiThreadEvaluate R = new MultiThreadEvaluate("thread_ed"+i,i,width,QueryNUM, author_pairs,papers,args[2],args[5]);
					R.start();
				}	
				
				// test individual, beacuse multiple threads cost memory
//				Evaluate evaObj = new Evaluate();
//				int depth = 4;
//				int width = 10;
//				evaObj.setDepth(depth);
//				evaObj.setWidth(width);
//				evaObj.settestQueryNUM(QueryNUM);
				
//				// debug
//				BruteForce accur_search = new BruteForce(depth,width,papers);
//				System.out.print(accur_search.bruteforceSearch(querystr1,querystr2));
				
//				double accuarcy = evaObj.evaluate_basic_accuracy(papers, author_pairs);
//				System.out.print("width "+width+" dpth "+depth+" -- ");
//				System.out.println("accuracy: "+accuarcy);
//				
//				double performance = evaObj.evaluate_basic_performance(papers, author_pairs);
//				System.out.print("width "+width+" dpth "+depth+" -- ");
//				System.out.println("performance: "+performance);
				
//				double speedup = evaObj.evaluate_MLcompare_speedup(papers, author_pairs, "2018", "0",  args[2], args[5] );
//				System.out.print("width "+width+" dpth "+depth+" -- ");
//				System.out.println("speedup from ML: "+speedup);
				
//				double ml_accur = evaObj.evaluate_MLcompare_accuracy(papers, author_pairs, "2018", "0",  args[2], args[5] ) ;
//				System.out.print("width "+width+" dpth "+depth+" -- ");
//				System.out.println("accuracy for ML: "+ml_accur);
				
				
				author_pairs = null;
			}		 
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	
}
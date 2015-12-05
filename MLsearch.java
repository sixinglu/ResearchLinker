/***********************************************
 * Author: Sixing Lu
 * Creation Date: 11/24/2015
 * Function: find connection between authors
 *           use Machine Learning output
 ***********************************************/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import com.mkyong.seo.MapUtil;

public class MLsearch{
	
	Integer depth, width;
	StandardAnalyzer analyzer;
	Directory index;
	IndexWriterConfig config;
	IndexWriter w;
	IndexReader reader;
	IndexSearcher searcher;
	QueryParser qp;
	ArrayList<ArrayList<String>> papers;
	List<String> visited;  // get rid of kind of loop
	Integer shortest; // only print shortest path so far
	LinkedList<String> shortestPath;
	
	private Integer intervalNO = 4;

	
	MLsearch(Integer d, Integer wid, ArrayList<ArrayList<String>> papers) throws IOException{
		depth = d;  // how deep to search
		width = wid;  // how many paper per person to search
		
        //  The analyzer for tokenizing text, indexing and searching
		analyzer = new StandardAnalyzer();  // default stop words
				
		//	create the index
		index = new RAMDirectory();
		config = new IndexWriterConfig(analyzer);
		w = new IndexWriter(index, config);
				
		// add documents
		for(int i=0; i<papers.size(); i++){
			addDoc(w, papers.get(i).get(0),papers.get(i).get(1),papers.get(i).get(2),papers.get(i).get(3),papers.get(i).get(4));
		}
		w.close();
		
		reader = DirectoryReader.open(index);
	    searcher = new IndexSearcher(reader);   
	    qp = new QueryParser("author", analyzer);
	    
	    visited = new LinkedList<String>();
	    shortest = Integer.MAX_VALUE;
	    shortestPath = new LinkedList<String>();
	}
	
	private class Node{	
		String name;
		LinkedList<ScoreDoc> paper;
		
		Node(String n, LinkedList<ScoreDoc> p){
			name = n;
			paper =p;
		}
	}
	
	/*******************************************************
	 * use ML output to search relationship between researchers 
     * @param  String querystr1,String querystr2
     * @return void
	 * @throws IOException 
	 * @throws ParseException 
	 ******************************************************/
	public LinkedList<String> machinelearningSearch(String querystr1,String querystr2, String mintime, String maxtime, String keywordsfile) throws IOException, ParseException{
		
		try{
			
			// prepare
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
			
			LinkedList<LinkedList<String>> connectPath = new LinkedList<LinkedList<String>>();
			TopScoreDocCollector collector = TopScoreDocCollector.create(width);
		    Query q = qp.parse(querystr1);
		    searcher.search(q, collector); // Search
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    LinkedList<ScoreDoc> newhits = new LinkedList<ScoreDoc>();
		    
		    // for machine learning training, must get rid of the paper does not contain exactly the same spelling withe the query
		    for(int i=0;i<hits.length;i++) 
			{
		       int docId = hits[i].doc;
		       
		    // can comment out this part of code when evaluate the brute force itself.
//		       Document d = searcher.doc(docId);
//		       if(d.get("author").contains(querystr1)){
		    	   newhits.add(hits[i]);
//		       }
		    } 
		    Node root = new Node(querystr1,newhits);
		    
		    FindClosest( root,  querystr2,  mintime,  maxtime,connectPath, keywords);
		    
			// rank results
			if(connectPath.size()!=0){
				HashMap<LinkedList<String>, Integer> hset = new HashMap<LinkedList<String>, Integer>();
				for(LinkedList<String> path:connectPath ){
					if(hset.containsKey(path)){
						hset.put(path, hset.get(path)+1);       
					}
					else{
						hset.put(path,1);
					}
				}
				Map<LinkedList<String>, Integer> result = new HashMap<LinkedList<String>, Integer>(hset);
				result = MapUtil.sortByValue( result );
				
				hset.remove(Collections.singleton(null));
				hset = null;
				newhits.removeAll(Collections.singleton(null));
				newhits = null;
		 		connectPath.removeAll(Collections.singleton(null));
		 		connectPath = null;
		 			 		
				// strongest freq
				Iterator it = result.entrySet().iterator();
				if(it.hasNext()){
					Map.Entry first = (Map.Entry)it.next();
					
					result.clear();
					result = null;
				
					return (LinkedList<String>) first.getKey();
				}
				result.clear();
				result = null;
			}
			keywords = null;
			connectPath.removeAll(Collections.singleton(null));
			connectPath = null;
		    
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
		
		return null;
	}
	
	/*******************************************************
	 * apply ML trained formula, create graph at the same time 
	 * Fastest, may trade off accuracy if train is not accurate
     * @param  Node root, String querystr2
     * @return void
	 * @throws IOException 
	 * @throws ParseException 
	 ******************************************************/
	private void FindClosest(Node root, String querystr2, String mintime, String maxtime,LinkedList<LinkedList<String>> connectPath,ArrayList<String> keywords) throws IndexOutOfBoundsException, IOException, ParseException{
		Queue<LinkedList<Node>> queue = new LinkedList< LinkedList<Node> >();
		LinkedList<Node> start_path = new LinkedList<Node>();
		start_path.add(root);
		queue.add(start_path);
		//System.out.println(root.name+"--"+querystr2);
		Integer shortSize = depth;
		LinkedList<String> neighborAuthors = new LinkedList<String>();
		
		try{
		while(!queue.isEmpty()){
			LinkedList<Node> path = queue.poll();
			if(path.size() - this.depth > 0.0){
				return;
			}
			Node lastnode = path.getLast();
			for(int i=0; i<lastnode.paper.size(); i++){
				String cooper = searcher.doc(lastnode.paper.get(i).doc).get("author");
				//int thispaper = lastnode.paper.get(i).doc;
		    	if(cooper.contains(querystr2)){  
		    		// print out result
		    		LinkedList<String> singlePath = new LinkedList<String>();
				    for(int g=0; g<path.size(); g++ ){
//					   		System.out.print(path.get(g).name);
//					    	System.out.print(" -- ");
				    	singlePath.add(path.get(g).name);
				    }
//					    System.out.println(querystr2);
				    singlePath.add(querystr2);
				    if(singlePath.size()<shortSize){
				    	connectPath.add(singlePath);
				    	shortSize = singlePath.size();
				    }
				    else{
				    	singlePath = null;
				    	return;
				    }
				    singlePath = null;
		    		//return;
		    	}
		    	else{
		    		List<String> cooperators = new ArrayList<String>();
			    	cooperators = Arrays.asList(cooper.split(","));  // one paper have multiple authors
			    	for(int j=0; j<cooperators.size(); j++){
			    		if(cooperators.get(j).isEmpty()){   // if not split well
			    			continue;
			    		}
			    		else if(visited.contains(cooperators.get(j))){  // ignore visited author
			    			continue;
			    		}
					    if(isContain(path,cooperators.get(j))){
					    	continue;
					    }
					    
					    neighborAuthors.add(cooperators.get(j));  // add all neighbor into candidate	    		
			    	} 
			    	cooperators.removeAll(Collections.singleton(null));
			    	cooperators = null;
		    	}
			}
			
			Node next_target = MLselectAuthor(neighborAuthors, mintime,  maxtime, keywords);
			if(next_target!=null){
				// add the next target search node into Q
				LinkedList<Node> new_path = new LinkedList<Node>(path);
				new_path.add(next_target);
	    		queue.add(new_path);
	    		neighborAuthors.clear();
			}
				
		}
		
		queue.removeAll(Collections.singleton(null));
		queue = null;
		start_path.removeAll(Collections.singleton(null));
		start_path = null;
		
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			System.out.println(root.name);
			System.out.println(querystr2);
		}
	}
	
	/*******************************************************
	 * select next author closest to destination
     * @param  neighborAuthors, mintime, maxtime, keywords
     * @return selected node
	 * @throws IOException 
	 * @throws ParseException 
	 ******************************************************/
	private Node MLselectAuthor(LinkedList<String> neighborAuthors,String mintime, String maxtime, ArrayList<String> keywords) throws ParseException, IOException{
		
		// ML model, decide which node push into Q
		int min_distance = Integer.MAX_VALUE;
	    Node next_target =null;
	    for(String name:neighborAuthors){
			Query q = qp.parse(name);
    		TopScoreDocCollector collector = TopScoreDocCollector.create(width);
    		searcher.search(q, collector); // Search
    		ScoreDoc[] hits = collector.topDocs().scoreDocs;
    		LinkedList<ScoreDoc> newhits = new LinkedList<ScoreDoc>();
    		ArrayList<String> summary = new ArrayList<String>();
    		ArrayList<String> time  = new ArrayList<String>();
    		for(int k=0; k<hits.length; k++){  
    			int docId = hits[k].doc;
    			Document d = searcher.doc(docId);
    			summary.add(d.get("summary"));
    			time.add(d.get("published"));
    			
    			// for machine learning training, only add doc with author spelling exactly with query
    			//if(d.get("author").contains(name)){  
    			     newhits.add(hits[k]);
    			     
    			//}    					
    		}
    		if(newhits.size()>0){
    			
    			ArrayList<ArrayList<Integer>> MLparameters = encodewithTime(summary, time, mintime, maxtime, keywords);
    			// calculate ML output
    			int result =0;
    			
    			
    			
    			
    			
    			
    			
    			
    			
    			
    			
    			
    			if(result<min_distance){
    				min_distance = result;
    				Node newnode = new Node(name,newhits);
    				next_target = newnode;
    			}
    		}
			
		}
	    
	    return next_target;
	}
	
	private boolean isContain(LinkedList<Node> path, String author){
		int i=0; 
		for(i=0; i<path.size(); i++){
			if(path.get(i).name.equals(author)){
				break;
			}
		}
		if(i>=path.size()){
			return false;
		}
		return true;
	}
	
	/*******************************************************
	 * calculate parameters feed into ML model
     * @param  summary, mintime, maxtime, keywords
     * @return encoded keywords freq vector
	 * @throws IOException 
	 * @throws ParseException 
	 ******************************************************/
	private ArrayList<ArrayList<Integer>> encodewithTime(ArrayList<String> summary, ArrayList<String> time, String mintime, String maxtime, ArrayList<String> keywords){
		Integer MAXtime = Integer.parseInt(maxtime.substring(0,4)); 
		Integer MINtime=  Integer.parseInt(mintime.substring(0,4));
		Integer span = (MAXtime - MINtime)/intervalNO;
		ArrayList<ArrayList<Integer>> Intervals = new ArrayList<ArrayList<Integer>>(); // four intervals
		
		// initilization
		for(int k=0; k<intervalNO+1;k++){  
			 ArrayList<Integer> zeros = new ArrayList<Integer>();
			 for(int p=0;p<keywords.size();p++){
				 zeros.add(0);
			 }
			 Intervals.add(zeros);		 
		}
		
		for(int i=0; i<summary.size();i++){
			Integer int_time = Integer.parseInt(time.get(i).substring(0,4));
			int span_loc = (int_time-MINtime)/span;
			ArrayList<Integer> encoded_key = encodeSummary(summary.get(i),keywords);
			for(int g=0; g<Intervals.get(span_loc).size();g++){
				Intervals.get(span_loc).set( g, Intervals.get(span_loc).get(g) + encoded_key.get(g) );
			}
		}
		
		return Intervals;
		
	}
	
	/*******************************************************
	 * calculate frequency of keyword in one summary 
     * @param summary string, keyword list
     * @return encoded keywork vector
	 ******************************************************/
	 private ArrayList<Integer> encodeSummary(String summary, ArrayList<String> keywords){
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
	* calculate frequency of all words
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
	
	private static void addDoc(IndexWriter w, String title, String author, String afffiliation, String publishtime, String summary) throws IOException 
	{
		  Document doc = new Document();
		  // A text field will be tokenized
		  doc.add(new TextField("author", author, Field.Store.YES));
		  doc.add(new TextField("afffiliation", afffiliation, Field.Store.YES));
		  doc.add(new TextField("publishtime", publishtime, Field.Store.YES));
		  doc.add(new TextField("summary", summary, Field.Store.YES));
		  
		  // We use a string field for docID because we don\'t want it tokenized
		  doc.add(new StringField("title", title, Field.Store.YES));
		  w.addDocument(doc);
	}
	
}
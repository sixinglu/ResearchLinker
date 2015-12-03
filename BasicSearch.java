/***********************************************
 * Author: Sixing Lu
 * Creation Date: 11/11/2015
 * Function: find connection between authors
 *           brute-force
 ***********************************************/

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

public class BasicSearch{
	
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
	
	BasicSearch(Integer d, Integer wid, ArrayList<ArrayList<String>> papers) throws IOException{
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
	 * bruteforce to search relationship between researchers 
     * @param  analyzer, query, search depth
     * @return connection
	 * @throws IOException 
	 * @throws ParseException 
	 ******************************************************/
	public LinkedList<String> basicSearch(String querystr1,String querystr2) throws IndexOutOfBoundsException,IOException, ParseException{
		
		LinkedList<LinkedList<String>> connectPath = new LinkedList<LinkedList<String>>();
		
	try{
		TopScoreDocCollector collector = TopScoreDocCollector.create(width);
	    Query q = qp.parse(querystr1);
	    searcher.search(q, collector); // Search
	    ScoreDoc[] hits = collector.topDocs().scoreDocs;
	    LinkedList<ScoreDoc> newhits = new LinkedList<ScoreDoc>();
	    
	    // doc return inaccurate will lead to inaccuracy in result
	    for(int i=0;i<hits.length;i++) 
		{     
	        newhits.add(hits[i]);
	    } 
	    //Node root = new Node(querystr1,hits);
	    Node root = new Node(querystr1,newhits);
	    
	    
	    // BFS, faster to search
	    BFS(root,querystr2,connectPath);
	    
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
	 				
	 		// strongest freq
	 		Iterator it = result.entrySet().iterator();
	 		Map.Entry first = (Map.Entry)it.next();
	 		return (LinkedList<String>) first.getKey();
	 				
	 	}
	    	
		}
	catch(Exception e){
			System.out.println(e.getMessage());
			System.out.print(querystr1+" ");
			System.out.println(querystr2);
		}
	    return null;
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
	 * Branch First Search, create graph at the same time 
	 * Faster than DFS, storage memory for all paths
     * @param  Node root, String querystr2
     * @return void
	 * @throws IOException 
	 * @throws ParseException 
	 ******************************************************/
	private void BFS(Node root, String querystr2,LinkedList<LinkedList<String>> connectPath) throws IndexOutOfBoundsException, IOException, ParseException{
		Queue<LinkedList<Node>> queue = new LinkedList< LinkedList<Node> >();
		LinkedList<Node> start_path = new LinkedList<Node>();
		start_path.add(root);
		queue.add(start_path);
		//System.out.println(root.name+"--"+querystr2);
		Integer shortSize = depth;
		
		try{
		while(!queue.isEmpty()){
			LinkedList<Node> path = queue.poll();
			if(path.size() - this.depth > 0.0){
				return;
			}
			Node lastnode = path.getLast();
			for(int i=0; i<lastnode.paper.size(); i++){
				String cooper = searcher.doc(lastnode.paper.get(i).doc).get("author");
				int thispaper = lastnode.paper.get(i).doc;
		    	if(cooper.contains(querystr2)){  
		    		// print out result
		    		LinkedList<String> singlePath = new LinkedList<String>();
				    for(int g=0; g<path.size(); g++ ){
				    	singlePath.add(path.get(g).name);
				    }
				    singlePath.add(querystr2);
				    if(singlePath.size()<shortSize){
				    	connectPath.add(singlePath);
				    	shortSize = singlePath.size();
				    }
				    else{
				    	return;
				    }
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
			    		Query q = qp.parse(cooperators.get(j));
			    		TopScoreDocCollector collector = TopScoreDocCollector.create(width);
			    		searcher.search(q, collector); // Search
			    		ScoreDoc[] hits = collector.topDocs().scoreDocs;
			    		LinkedList<ScoreDoc> newhits = new LinkedList<ScoreDoc>();
			    		for(int k=0; k<hits.length; k++){  
			    			int docId = hits[k].doc;
			    			
			    			if(docId != thispaper){ // get rid of this doc, in case pingpang
			    			     //Document d = searcher.doc(docId);
			    			     //System.out.println( d.get("author"));
			    			     //if(d.get("author").contains(cooperators.get(j))){  // for machine learning training, only add doc with author spelling exactly with query
			    			    	  newhits.add(hits[k]);
			    			     //}    				
			    			}
			    		}
			    		if(newhits.size()>0){
			    			LinkedList<Node> new_path = new LinkedList<Node>(path);
				    		Node newnode = new Node(cooperators.get(j),newhits);
				    		new_path.add(newnode);
				    		queue.add(new_path);
			    		}
			    	} 	
			    	
		    	}
			}
		}
		}
		catch(Exception e){
			System.out.println(e.getMessage());
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
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.queryparser.classic.ParseException;

class MultiThreadEvaluate implements Runnable {
	   private Thread t;
	   private String threadName;
	   private int depth;
	   private int width;
	   private Integer testQueryNUM;
	   private ArrayList<ArrayList<String>> author_pairs;
	   ArrayList<ArrayList<String>> papers;
	   String keywordsfile;
	   String coeffFile;
	   
	   MultiThreadEvaluate(String name, int d, int w, int testNUM, ArrayList<ArrayList<String>> pairs, ArrayList<ArrayList<String>> paper, String keywordsf, String coeffF){
		   threadName = name;
		   depth = d;
		   width = w;
		   testQueryNUM = testNUM;
		   author_pairs = pairs;
		   papers = paper;
		   keywordsfile = keywordsf;
		   coeffFile = coeffF;
	       System.out.println("Creating " +  threadName );
	   }
	   
	@Override
	public void run() {
		
		try {
			Evaluate evaObj = new Evaluate();
			evaObj.setDepth(depth);
			evaObj.setWidth(width);
			evaObj.settestQueryNUM(testQueryNUM);
			
			double accuarcy = evaObj.evaluate_basic_accuracy(papers, author_pairs);
			System.out.print("width "+width+" dpth "+depth+" -- ");
			System.out.println("accuracy for basic: "+accuarcy);
			
			double performance = evaObj.evaluate_basic_performance(papers, author_pairs);
			System.out.print("width "+width+" dpth "+depth+" -- ");
			System.out.println("performance for basic: "+performance);
			
			double ml_performance = evaObj.evaluate_MLcompare_speedup(papers, author_pairs, "2018", "0", keywordsfile, coeffFile );
			System.out.print("width "+width+" dpth "+depth+" -- ");
			System.out.println("performance for ML: "+ml_performance);
			
			double ml_accur = evaObj.evaluate_MLcompare_accuracy(papers, author_pairs, "2018", "0",  keywordsfile, coeffFile ) ;
			System.out.print("width "+width+" dpth "+depth+" -- ");
			System.out.println("accuracy for ML: "+ml_accur);
		}
		catch (IndexOutOfBoundsException e) {
	         System.out.println("Thread " +  threadName + " interrupted.");
	     } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Thread " +  threadName + " exiting.");
	}
	
	public void start ()
	{
	      System.out.println("Starting " +  threadName );
	      if (t == null)
	      {
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	}
}
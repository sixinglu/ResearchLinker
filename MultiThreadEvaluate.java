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
	   
	   MultiThreadEvaluate(String name, int d, int w, int testNUM, ArrayList<ArrayList<String>> pairs, ArrayList<ArrayList<String>> paper){
		   threadName = name;
		   depth = d;
		   width = w;
		   testQueryNUM = testNUM;
		   author_pairs = pairs;
		   papers = paper;
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
			System.out.println("accuracy: "+accuarcy);
			
			double performance = evaObj.evaluate_basic_performance(papers, author_pairs);
			System.out.print("width "+width+" dpth "+depth+" -- ");
			System.out.println("performance: "+performance);
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
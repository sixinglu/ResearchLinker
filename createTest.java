/***********************************************
 * Author: Sixing Lu
 * Creation Date: 11/25/2015
 * Function: create testfile
 ***********************************************/
package org.paukov.combinatorics.combination.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

public class createTest{

   	public ArrayList<ArrayList<String>> create_authorPair(HashMap<String,String> author_affiliation){
   		ArrayList<ArrayList<String>> combines = new ArrayList<ArrayList<String>>();
   		
		//String[] pool =  author_affiliation.keySet().toArray(new String[author_affiliation.keySet().size()]);
		ICombinatoricsVector<String> originalVector = Factory.createVector(author_affiliation.keySet());
		Generator<String> gen = Factory.createSimpleCombinationGenerator(originalVector, 2);
		Iterator<ICombinatoricsVector<String>> itr = gen.iterator();// create iterator
		
		while (itr.hasNext()) {
			ICombinatoricsVector<String> combination = itr.next();
			ArrayList<String> pair = new ArrayList<String>();
			pair.add(combination.getValue(0));
			pair.add(combination.getValue(1));
			combines.add(pair);
			//System.out.println(combination);
		}
		
		return combines;
	}
}
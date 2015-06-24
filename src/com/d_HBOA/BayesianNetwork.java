package com.d_HBOA;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeSet;

import com.z_PEA.Individual;
import com.z_PEA.Problem;
import com.z_PEA.SelectedSet;



@SuppressWarnings("unchecked")   	 																			// Supress warnings for creating an Array of ArrayList.  
class BayesianNetwork{           	 																			// Check: B. Eckel, "Thinking in Java", 4th ed., MindView Inc., 2006, [pp. 759-761]
	
	private IBayesianMetric bayesianMetric;
	public int offspringSize; 																					// NOTE: The BN is responsible for sampling new individuals.
	private int maxVertexDegree;																				// Maximum number of parents per vertex.
	
	private DecisionGraph[]         decisionGraphs = new DecisionGraph[Problem.n];
	private LinkedHashSet<Integer>[]    parentList = (LinkedHashSet<Integer>[])new LinkedHashSet[Problem.n];	// Use this to generate the topological ordering and the CP Tables.
	private LinkedHashSet<Integer>[] adjacencyList = (LinkedHashSet<Integer>[])new LinkedHashSet[Problem.n];	// Use this to insure an acyclic BN.
	private TreeSet<Integer>[]           splitList = (TreeSet<Integer>[])new TreeSet[Problem.n]; 	  			// Use this to choose only the correct splits.   																										
	private TreeSet<Integer>             oldScores;													  			// Use this to mark for removal obsolete scores in the scoreList. 
	private LinkedList<Score>            scoreList;												   	  			// NOTE: 'scoreList' is ordered in decreasing value of scores.
																								  				// 			 Use ScoreList.insertScore(..) and ScoreList.removeScores(..) 
																								  				// 			 static methods for optimal performance.
	private int[] color;				 // NOTE: color[] is initialized with all zeros, by default.
	private ArrayList<Integer> topSort;  // Use topological sort to sample the Bayesian Network.
	
	public BayesianNetwork(IBayesianMetric bayesianMetric, int offspringSize, int maxVertexDegree){
		this.bayesianMetric = bayesianMetric;
		this.offspringSize = offspringSize;
		this.maxVertexDegree = maxVertexDegree;
	}
	
	public DecisionGraph[] getDecisionGraph(){return decisionGraphs;}
	public DecisionGraph getDecisionGraph(int i){return decisionGraphs[i];}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// This generator performs a greedy search to generate a bayesian model based on decision graphs,
	// best fitted for the selected set. The model includes a topological sort of the final BN, used for sampling.
	// The greedy algorithm is described in Martin Pelikan, 'Hierarchical Bayesian Optimization Algorithm', pag. 116
	//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void generateModel(SelectedSet selectedSet){
		this.generateDecisionGraphs(selectedSet);		
		this.topologicalSort();      
	}
	
	private void generateDecisionGraphs(SelectedSet selectedSet){
		this.initializeBN(selectedSet);			 										// Refresh the BN. Compute and store in decreasing order the first score gains, corresponding to adding a first edge to the empty BN.
		while(scoreList.size() > 0){		 			 								// Search for the best split and store all the necessary information to effectively perform it.	
			Score bestScore = scoreList.getFirst();		 								// This is the best split. NOTE: getFirst() returns but does not remove the score from scoreList.
			int i = bestScore.getI(); 					 								// DecisionGraph i.
			int j = bestScore.getJ(); 					 								// Leaf j.
			int k = bestScore.getK();					 								// Split Variable k. We are adding the edge (Xk)-->(Xi) to the BN. 				
			
			if(parentList[i].size() >= maxVertexDegree && !(parentList[i].contains(k)))	// Do NOT perform the split. Xi has reached the maximum number of parents. 
				scoreList.remove();														// Remove the first score in scoreList.
			else{																		// Perform the best split. 
				updateBN(i, j, k);							 							// Update the BN and remove non-valid and obsolete scores from ScoreList.
				updateDecisionGraphs(selectedSet, i, j, k); 							// Update DecisionGraphs[i].
				updateScoreList(selectedSet, i, j, k);									// Compute new scores and update scoreList and scoreOrders accordingly.
			}

			for(int s = 0; s < scoreList.size(); s++){	  								// Update the score orders on every leaf.
				Score score = scoreList.get(s);			  								// This will be used to ensure proper removal.
				int Xi = score.getI(); 					  								// Decision Graph i.
				int Lj = score.getJ(); 					  								// Leaf j.
				int Sk = score.getK(); 					  								// Split Variable k.
				decisionGraphs[Xi].getLeaf(Lj).setScoreOrder(Sk,s);
			}
		}
	} // END: generateDecisionGraphs(...)
	
	private void initializeBN(SelectedSet selectedSet){
		int NS = selectedSet.getN();
		scoreList = new LinkedList<Score>();	
		Individual[] individuals = selectedSet.getIndividuals();
		for(int i = 0; i < Problem.n; i++){
			// Refresh the Bayesian Network structure
			parentList[i] = new LinkedHashSet<Integer>();    
			adjacencyList[i] = new LinkedHashSet<Integer>();   
			splitList[i] = new TreeSet<Integer>();        			 
			for(int n = 0; n < Problem.n; n++)
				if(n != i)
					splitList[i].add(n);
			
			int mOne = selectedSet.getUniFrequencies(i);								// Construct the initial single leaf decision graphs.
			int mZero = NS - mOne;
			Leaf newLeaf = new Leaf(0,	-1, mZero, mOne);	 							// There is only a single leaf per variable, at depth 0, with side -1.
			decisionGraphs[i] = new DecisionGraph(newLeaf);								// Initially each graph as a single leaf and there are n-1 possible splits.
			if(mZero > 0 && mOne > 0){													// If mZero = 0 or mOne = 0 there is no need to try any split with this leaf.
				for(int j = 0; j < NS; j++){
					char alleleI = individuals[j].getAllele(i);							// Value of Xi in individual j.
					for(int s: splitList[i]){
						char alleleS = individuals[j].getAllele(s);						// Value of Xs in individual j.
						if(alleleI == '0'){
							if(alleleS == '0')
								newLeaf.addPossibleSplitFrequency(0, s); 				// m00[s]++;
							else
								newLeaf.addPossibleSplitFrequency(1, s); 				// m01[s]++;
						}
						else{
							if(alleleS == '0')
								newLeaf.addPossibleSplitFrequency(2, s); 				// m10[s]++;
							else
								newLeaf.addPossibleSplitFrequency(3, s); 				// m11[s]++;
						}
					}
				}
				for(int s: splitList[i]){
					int m00 = newLeaf.getPossibleSplitFrequency(0,s);
					int m01 = newLeaf.getPossibleSplitFrequency(1,s);
					int m10 = newLeaf.getPossibleSplitFrequency(2,s);
					int m11 = newLeaf.getPossibleSplitFrequency(3,s);
					double scoreGain = bayesianMetric.computeScoreGain(mZero, mOne, m00, m01, m10, m11);
					if(scoreGain > 0){
						Score currentScore = new Score(i, 0, s, m00, m01, m10, m11, scoreGain);
						ScoreList.insertScore(scoreList, currentScore);
					}
				}
			}
		}
		for(int s = 0; s < scoreList.size(); s++){										// Initialize the score orders on each leaf. This will be used to ensure proper removal. 	
			Score score = scoreList.get(s);			
			int i = score.getI(); 														// Decision Graph i.
			int j = score.getJ(); 														// Leaf j.
			int k = score.getK(); 														// Split Variable k.
			decisionGraphs[i].getLeaf(j).setScoreOrder(k,s);
		}
	} // END: initializeBN(...)
	
	private void updateBN(int i, int j, int k){
		oldScores = new TreeSet<Integer>();												// Sorted collection of score orders that are to be removed from ScoreList.
																						// NOTE: Use ScoreList.removeAllScores(..) to ensure proper removal!						 
		parentList[i].add(k);					 										// Xk is a parent of Xi.
		adjacencyList[k].add(i);				 										// Xi is a child of Xk.
		Leaf leafJ = decisionGraphs[i].getLeaf(j);
		for(int split: splitList[i]){													// Mark the old scores corresponding to all splits (now obsolete) in Leaf j of DecisionGraph i.
																						// NOTE: splitList[i] was not updated yet!
			int scoreOrder = leafJ.getScoreOrder(split);								// NOTE: TreeSet guarantees NO duplicates! 
			if(scoreOrder >= 0)															// NOTE: This is only necessary because scoreOrders are initialized with -1.
				oldScores.add(scoreOrder);	 											// NOTE: The first score, corresponding to the best split, is also marked for removal.
		}													
		TreeSet<Integer> descendants = new TreeSet<Integer>();
		TreeSet<Integer> ascendants = new TreeSet<Integer>();
		getDescendants(descendants, i);						 							// NOTE: The list of descendants includes variable Xi itself. 
		getAscendants(ascendants, k);						 							// NOTE: The list of ascendants includes variable Xk itself.
		for(int asc: ascendants)							 							// No child of the child of the child of ... of Xi can be a parent of Xk.
			for(int desc: descendants){						 							// No child of the child of the child of ... of Xi can be a parent of the parent of the parent of ... of Xk.
				splitList[asc].remove(desc);				 							// Xdesc can no longer be a parent of Xasc. Remove splits to avoid cycles.
				addOldScores(oldScores, asc, desc);			 							// Remove all splits of Xdesc for all leafs in Xasc.
			}
	}
		
	
	private void updateDecisionGraphs(SelectedSet selectedSet, int i , int j, int k){	// Effectively perform the best split. Consider insert split directly in generateDecisionGraphs(...)
		
		decisionGraphs[i].splitLeaf(j, k);												// Effectively perform the best split.
		
		ArrayList<Leaf> leafsI = decisionGraphs[i].getLeafs();							// Increment the leaf number for all scores acting on the j-tale of decisionGraphs[i].leafs
		for(int l = j+2; l < leafsI.size(); l++){ 										// We've already performed a split in leaf j. The j-tale starts at leaf j+2.
			Leaf leafIL = leafsI.get(l);
			for(int s = 0; s < Problem.n; s++){
				int scoreOrder = leafIL.getScoreOrder(s);
				if(scoreOrder >= 0){
					Score updatedScore = scoreList.get(scoreOrder);
					updatedScore.setJ(l);			 									// Update the current position of this score in the leafs list.
					scoreList.set(scoreOrder, updatedScore);
				}
			}
		}
	}
	
	
	private void updateScoreList(SelectedSet selectedSet, int i, int j, int k){			// CAUTION: Do not change the loop order!!
		Iterator<Integer> iterator = oldScores.descendingIterator(); 					// Remove all obsolete scores. // NOTE: Use descendingIterator() to ensure proper removal.
		while(iterator.hasNext())
			scoreList.remove((int)iterator.next()); 			     					// NOTE: Do NOT remove cast to int.
		// Reset all scoreOrders to -1
		for(int a = 0; a < Problem.n; a++){
			ArrayList<Leaf> leafs = decisionGraphs[a].getLeafs();
			for(Leaf leaf: leafs)
				for(int b = 0; b < Problem.n; b++)
					leaf.setScoreOrder(b,-1);					 
		}
		
		computeNewLeafScores(selectedSet, i, j);	 									// Function computeNewLeafScores() changes scoreList thus rendering all scoreOrders obsolete!!
	}																					// Compute scores for new leaf0 and leaf1, and add them to scoreList.
	
	
	private void computeNewLeafScores(SelectedSet selectedSet, int i, int j){			// Compute scores for new leaf0 and leaf1, and add them to scoreList.
		int NS = selectedSet.getN();
		for(int a = 0; a < NS; a++){
			Individual individual = selectedSet.getIndividual(a);
			IGraph iterator = decisionGraphs[i].getGraph();
			while(!(iterator instanceof Leaf)){											// Iterator is a variable because we are still traversing the DG.
				int x = ((Variable)iterator).getVariable();
				char alleleX = individual.getAllele(x);
				if(alleleX == '0')
					iterator = ((Variable)iterator).getZero();
				else
					iterator = ((Variable)iterator).getOne();
			}
			ArrayList<Leaf> leafsI = decisionGraphs[i].getLeafs();
			int itrPosition = leafsI.indexOf((Leaf)iterator);
			if(itrPosition == j || itrPosition == j+1){									// We've reached one of the new leafs.
				int mZero = ((Leaf)iterator).getMZero();
				int mOne = ((Leaf)iterator).getMOne();
				if(mZero > 0 && mOne > 0){		   										// It's still "interesting" to split.
					char alleleI = individual.getAllele(i);								// Value of Xi in individual a.
					for(int split: splitList[i]){
						char alleleS = individual.getAllele(split); 					// Value of Xsplit in individual a.
						if(alleleI == '0'){
							if(alleleS == '0')
								((Leaf)iterator).addPossibleSplitFrequency(0,split); 	// m00[split]++;
							else
								((Leaf)iterator).addPossibleSplitFrequency(1,split); 	// m01[split]++;  
						}
						else{
							if(alleleS == '0')
								((Leaf)iterator).addPossibleSplitFrequency(2,split); 	// m10[split]++;
							else
								((Leaf)iterator).addPossibleSplitFrequency(3,split); 	// m11[split]++;						
						}
					}
				}
			}
		}
		for(int a = 0; a <= 1; a++){
			Leaf newLeaf = decisionGraphs[i].getLeaf(j+a); 								// The new two new leafs are at positions 'j' and 'j+1'. 
 			int mZero = newLeaf.getMZero();
			int mOne = newLeaf.getMOne();
			if(mZero > 0 && mOne > 0)
				for(int split: splitList[i]){
					int m00 = newLeaf.getPossibleSplitFrequency(0,split);
					int m01 = newLeaf.getPossibleSplitFrequency(1,split);
					int m10 = newLeaf.getPossibleSplitFrequency(2,split);
					int m11 = newLeaf.getPossibleSplitFrequency(3,split);
					double scoreGain = bayesianMetric.computeScoreGain(mZero, mOne, m00, m01, m10, m11);
					if(scoreGain > 0){
						Score currentScore = new Score(i, j+a, split, m00, m01, m10, m11, scoreGain);
						ScoreList.insertScore(scoreList, currentScore);
					}
				}
		}
	} // END: computeNewLeafScores(...)
	
	private void addOldScores(TreeSet<Integer> oldScores, int i, int k){
		ArrayList<Leaf> bestLeafs = decisionGraphs[i].getLeafs();
		for(Leaf leaf: bestLeafs){
			int scoreOrder = leaf.getScoreOrder(k);
			if(scoreOrder >= 0)
				oldScores.add(scoreOrder);												// Mark for removal all 'k' scores, leaf wise, for variable Xi.
		}
	}
	
	private void getDescendants(TreeSet<Integer> descendants, int i){
		boolean notVisited = descendants.add(i);
		if(notVisited)
			for(int desc: adjacencyList[i])
				getDescendants(descendants, desc);		
	}
	
	private void getAscendants(TreeSet<Integer> ascendants, int k){
		boolean notVisited = ascendants.add(k);
		if(notVisited)
			for(int asc: parentList[k])
				getAscendants(ascendants, asc);
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// The topological sort of the BayesianNetwork is stored in the array 'topSort'.
	// Since our BN has a single connected component the topologicalSort() starts with X0, 
	// the first variable, and uses  'WHITE = 0'    'GRAY = 1'. 
	//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void topologicalSort(){
		color = new int[Problem.n];							// NOTE: color[] is initialized with all zeros, by default.
		topSort = new ArrayList<Integer>();
		for(int Xi = 0; Xi < Problem.n; Xi++)
			if(color[Xi] == 0)  
				DFSVisit(Xi);
	}
	
	private void DFSVisit(int Xi){
		color[Xi] = 1;      								// Xi is now GRAY.
		for(int child: adjacencyList[Xi])
			if(color[child] == 0)        
				DFSVisit(child);
		topSort.add(Xi);   									// NOTE: Topological sort is in ASCENDENT order of finishing time.
	}
	
	public Individual[] sampleNewIndividuals(){
		Individual[] newIndividuals = new Individual[offspringSize];
		for(int i = 0; i < offspringSize; i++)
			newIndividuals[i] = this.generateInstance();
		return newIndividuals;
	}	
	
	private Individual generateInstance(){
		char[] indiv = new char[Problem.n];
		for(int i = Problem.n-1; i >= 0; i--)  				// NOTE: Topological sort is in ASCENDENT order of finishing time.
			decisionGraphs[topSort.get(i)].generateAllele(indiv, topSort.get(i));
		return new Individual(indiv);
	}
	
	public String toString(){
		String str = "Xi    pList          aList             splitList\n";
		String pList = "";
		String aList = "";
		String sList = "";
		for(int i = 0; i < Problem.n; i++){
			for(int p: parentList[i])
				pList += p + "|";
			for(int a: adjacencyList[i])
				aList += a + "|";
			for(int s: splitList[i])
				sList += s + "|";
			str += i + "    " + pList + "              " + aList + "                       " + sList + "\n";
			pList = "";
			aList = "";
			sList = "";			
		}
		return str;
	}	
}// End of BayesianNetwork.	


	

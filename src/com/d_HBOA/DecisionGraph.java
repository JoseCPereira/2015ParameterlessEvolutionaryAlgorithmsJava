package com.d_HBOA;

import java.util.ArrayList;

import com.z_PEA.PARAMETERLESS;
import com.z_PEA.Problem;


interface IGraph{
	public void setParent(Variable parent, int side);	
}

class Variable implements IGraph{
	private int variable;
	private IGraph zero, one;
	
	
	public Variable(int x){variable = x;}  								// Default constructor
	public Variable(int x, IGraph zero, IGraph one){
		variable = x;
		if(zero instanceof Leaf)          								// A variable is responsible for setParent()  
			zero.setParent(this,0);       								// to both of its leaves.
		if(one instanceof Leaf)
			one.setParent(this,1);
		this.zero = zero;
		this.one = one;
	}
	
	public int getVariable(){return variable;}
	public IGraph getZero(){return zero;}
	public IGraph getOne(){return one;}
	public void setZero(IGraph zero){this.zero = zero;}
	public void setOne(IGraph one){this.one = one;}
	public void setParent(Variable parent, int side){} 					// No need to store a variable's parent.
		
	public String toString(){return "(X" + variable + " ("+ zero + ")" + " (" + one +"))";}
}

class Leaf implements IGraph{
	private int depth, 
				side,
				mZero, mOne;
	private Variable parent;
	private int[][] possibleSplitFrequencies = new int[4][Problem.n];	// NOTE: 0 -> m00; 1 -> m01; 2 -> m10; 3 -> m11
	private int[] scoreOrders = new int [Problem.n]; 				  	// NOTE: scoreOrders is initialized with all zeros.
												 				  	  	//	 	 Is it necessary to initialize with all (-1)'s ?							 
	public Leaf(){}  // Default constructor
	public Leaf(int depth, int side, int mZero, int mOne){	 
		this.depth = depth;									
		this.side = side;
		this.mZero = mZero;
		this.mOne = mOne;
		for(int i = 0; i < Problem.n; i++)
			scoreOrders[i] = -1;
		// parent = null;  												   NOTE: The Variable constructor is responsible to set the leaf's parent.
		//						  												  The initial leaves will have a null parent by default.
	}

	public int getDepth(){return depth;}
	public int getSide(){return side;}
	public int getMZero(){return mZero;}
	public int getMOne(){return mOne;}
	public int[][] getPossibleSplitFrequencies(){return possibleSplitFrequencies;}
	public int getPossibleSplitFrequency(int row, int split){return possibleSplitFrequencies[row][split];}
	public int[] getScoreOrders(){return scoreOrders;}
	public int getScoreOrder(int k){return scoreOrders[k];}
	public Variable getParent(){return parent;}

	public void addPossibleSplitFrequency(int x, int y){possibleSplitFrequencies[x][y]++;}
	public void setPossibleSplitFrequency(int x, int y, int freq){possibleSplitFrequencies[x][y] = freq;}
	public void setScoreOrder(int k, int s){scoreOrders[k] = s;}
	public void setParent(Variable parent, int side){
		this.parent = parent;
		this.side = side;
	}
			
	public String toString(){
		String str = "";
		for(int i = 0; i < Problem.n; i++)
			str += scoreOrders[i] + ",";								// Print the scoreOrders array.
		str = str.substring(0, str.length()-1);  						// Remove the last comma.
		return "[d = " + depth + "; s = " + side + "; m0 = " + mZero + 
				"; m1= " + mOne + "]" + "; scoreOrders=[" + str + "]";
	}
}


class DecisionGraph{
	private IGraph graph;
	private ArrayList<Leaf> leafs = new ArrayList<Leaf>();
	
	public DecisionGraph(){}          									// Empty constructor.
	public DecisionGraph(Leaf leaf){  									// A DecisionGraph always starts with a single 
		graph = leaf;                 									// leaf and it evolves with each splitLeaf().
		leafs.add(leaf);
	}
	public IGraph getGraph(){return graph;}
	public ArrayList<Leaf> getLeafs(){return leafs;}
	public Leaf getLeaf(int j){return leafs.get(j);}
	
	public void splitLeaf(int j, int split){
		Leaf oldLeaf = leafs.get(j);
		int depth = oldLeaf.getDepth() + 1;
		int m00 = oldLeaf.getPossibleSplitFrequency(0, split);
		int m01 = oldLeaf.getPossibleSplitFrequency(1, split);
		int m10 = oldLeaf.getPossibleSplitFrequency(2, split);
		int m11 = oldLeaf.getPossibleSplitFrequency(3, split);
		Leaf leaf0 = new Leaf(depth, 0, m00, m10);
		Leaf leaf1 = new Leaf(depth, 1, m01, m11);
		IGraph newSplit = new Variable(split,leaf0,leaf1);				// NOTE: The Variable constructor is responsible to set the leaf's parent.
		if(oldLeaf.getParent() == null) 						
			graph = newSplit;
		else if(oldLeaf.getSide() == 0)
			oldLeaf.getParent().setZero(newSplit);
			else
				oldLeaf.getParent().setOne(newSplit);
		leafs.set(j, leaf0);    										// Replace old leaf with leaf zero.
		leafs.add(j+1,leaf1);   										// Leaf one is added next to leaf zero. Is this necessary? Try leafs.add(leaf1). 
																		// NOTE: How to ensure the correct leaf order? Check BN.updateScores(...)
	}	
	
	public void generateAllele(char[] indiv, int Xi){
		double prob = getCondProb(indiv);
		indiv[Xi] = (PARAMETERLESS.random.nextDouble() <= prob) ? '1' : '0';
	}
	
	private double getCondProb(char[] indiv){
		IGraph iterator = graph;
		while(iterator instanceof Variable){
			int x = ((Variable)iterator).getVariable();
			if(indiv[x] == '0')
				iterator = ((Variable)iterator).getZero();
			else
				iterator = ((Variable)iterator).getOne();
		}
		int m0 = ((Leaf)iterator).getMZero();
		int m1 = ((Leaf)iterator).getMOne();
		return ((double)m1)/((double)(m0 + m1));
	}

	public String toString(){return graph + "\n";}
}








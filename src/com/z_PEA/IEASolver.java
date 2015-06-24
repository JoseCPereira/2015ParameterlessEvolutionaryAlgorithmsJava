package com.z_PEA;

//DESIGN PATTERN STRATEGY
public abstract class IEASolver{
	
	private boolean dummy = false;
	protected int currentGeneration = 0;			// Current generation for this solver. This is updated by each nextGeneration() call.
	
	public void setDummy(boolean dum){dummy = dum;}
	public boolean getDummy(){return dummy;}
	
	public void incrementCurrentGeneration(){currentGeneration++;}
	public int getCurrentGeneration(){return currentGeneration;}
	
	public abstract int        getN();
	public abstract Population getCurrentPopulation();
	public abstract double     getAvgFitness();
	public abstract int        getFitnessCalls();
	
	public abstract boolean    nextGeneration();	// This function computes the next 'currentPopulation'.  
													// It is also responsible for incrementing the value of 
													// 'currentGeneration' and updating the value of 'avgFitness'.	
}
















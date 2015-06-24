package com.c_ECGA;

import com.z_PEA.Individual;
import com.z_PEA.IEASolver;
import com.z_PEA.Population;
import com.z_PEA.RandomPopulation;
import com.z_PEA.SelectedSet;
import com.z_PEA.Stopper;



public class ECGASolver extends IEASolver{	
	public  int N;	  				 		// Population size.
	private Population currentPopulation;
	private int currentGeneration = 0;		// Current generation for this solver. This is updated by each nextGeneration() call.
	private int fitnessCalls = 0;				// Number of fitness calls for this solver. This must be updated for all RandomPopulation() and replace() calls.
	private double avgFitness;				// Average fitness of the current population for this Solver. This is updated by each nextGeneration() call.
	
	private  Selection selection;		 	// NOTE: Use Parameter.initializeSelector() to generate the chosen selector type.
	private  MPModel mPModel;				// NOTE: Use initializeMPModel() to initialize the chosen bayesian network generator.
	private  IReplacement replacement; 		// NOTE: Use Parameter.initializeReplacement() to generate the chosen replacement type.
	
	
	public ECGASolver(String paramFile, int currentN){
		this.N = currentN;
		currentPopulation = new RandomPopulation(N);	// Initialize random population. Fitness and statistics are automatically computed.
		fitnessCalls      = this.N;						// NOTE: RandomPopulation() computes the fitness of all N individuals in the initial population.
		
		Parameter.initializeParameters(paramFile);  	// Initialize and validate hBOA parameters
		selection   = Parameter.initializeSelection(N);
		mPModel     = Parameter.initializeMPModel(N);
		replacement = Parameter.initializeReplacement();					
	}
	
	public int getN(){return N;}
	public Population getCurrentPopulation(){return currentPopulation;}
	public int getCurrentGeneration(){return currentGeneration;}
	public int getFitnessCalls(){return fitnessCalls;}
	public double getAvgFitness(){return avgFitness;}
		
	public boolean nextGeneration(){
		currentGeneration++;
		SelectedSet selectedSet = selection.select(currentPopulation);		 	 // 1. SELECTION.
		mPModel.generateModel(selectedSet);		 								 // 2. GENERATE MARGINAL PRODUCT MODEL.
		Individual[] newIndividuals = mPModel.sampleNewIndividuals(selectedSet); // 3. SAMPLING.		
		replacement.replace(currentPopulation, newIndividuals);					 // 4. REPLACEMENT. NOTE: replace() computes the fitness only of the newIndividuals. 
																				 //	                NOTE: This function is also responsible for updating the  
		fitnessCalls += newIndividuals.length;									 //						  information about the best individual. 			
		currentPopulation.computeUnivariateFrequencies();
		avgFitness = currentPopulation.computeAvgFitness();					
		
		return Stopper.criteria(currentGeneration, currentPopulation);
	}
}









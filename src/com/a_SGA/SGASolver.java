package com.a_SGA;

import com.z_PEA.Individual;
import com.z_PEA.IEASolver;
import com.z_PEA.Population;
import com.z_PEA.RandomPopulation;
import com.z_PEA.SelectedSet;
import com.z_PEA.Stopper;



public class SGASolver extends IEASolver{		
	public int           N;
	private Population   currentPopulation;
	
	private int          fitnessCalls;					// Number of fitness calls for this solver. This must be updated for all RandomPopulation() and replace() calls.
	private double       avgFitness;					// Average fitness of the current population for this Solver. This is updated by each nextGeneration() call.
	
	private Selection    selection;		 				// NOTE: Use Parameter.initializeSelector() to generate the chosen selector type.
	private Crossover    crossover;						// NOTE: Use Parameter.initializeCrossover() to generate the chosen crossover type.
	private Mutation     mutation;						// NOTE: Use Parameter.initializeMutation() to generate the chosen mutation type.
	private IReplacement replacement; 					// NOTE: Use Parameter.initializeReplacement() to generate the chosen replacement type.
	
	
	public SGASolver(String paramFile, int currentN){
		this.N = currentN;
		currentPopulation = new RandomPopulation(N);	// Initial random population. Fitness and statistics are automatically computed.
		fitnessCalls      = this.N;						// NOTE: RandomPopulation() computes the fitness of all N individuals in the initial population.
		
		Parameter.initializeParameters(paramFile);  	// Initialize and validate SGA parameters
		selection   = Parameter.initializeSelection(N);
		crossover   = Parameter.initializeCrossover();
		mutation    = Parameter.initializeMutation();
		replacement = Parameter.initializeReplacement();
	}
	
	public int                        getN(){return N;}
	public Population getCurrentPopulation(){return currentPopulation;}
	public int             getFitnessCalls(){return fitnessCalls;}
	public double            getAvgFitness(){return avgFitness;}
	
	public boolean nextGeneration(){
		currentGeneration++;
		SelectedSet     selectedSet = selection.select(currentPopulation);		// 1. SELECTION.
		Individual[] newIndividuals = crossover.cross(selectedSet);				// 2. CROSSOVER.
		mutation.mutate(newIndividuals);										// 3. MUTATION.	 
		replacement.replace(currentPopulation, newIndividuals);					// 4. REPLACEMENT. NOTE: replace() computes the fitness only of the newIndividuals. 
																				//	               NOTE: This function is also responsible for updating the  
		fitnessCalls += newIndividuals.length;									//						 information about the best individual. 
		currentPopulation.computeUnivariateFrequencies();						
		avgFitness = currentPopulation.computeAvgFitness();						// NOTE: Every nextGeneration() must compute the average fitness of its current Population!
																				//		 No need to update information about the best individual. 
		return Stopper.criteria(currentGeneration, currentPopulation);			//		 Replacement is responsible for that.
	}
}








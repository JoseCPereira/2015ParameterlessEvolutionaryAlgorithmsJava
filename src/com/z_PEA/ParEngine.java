package com.z_PEA;

import java.util.ArrayList;


public class ParEngine{	
	public static int                  nextSolver;			// Number of generations to perform before next Solver. NOTE: This is initialized by ParParmeter.java
	public static int                  N0;					// Initial population size to be used by the EDA. 	    NOTE: This is initialized by ParParmeter.java		
	public static Problem              problem;				// Problem to be solved.
	public static IEAlgorithm          EAlgorithm;			// Chosen EA to perform the parameterless strategy. Design Pattern Strategy. 
	public static BestSoFar            bestSoFar;			// Object where the best results are stored. It is also responsible for printing those results in the bestFileName.txt file.
	public static ArrayList<IEASolver> IEASolvers;		
	public static int                  iteration ;			// Current iteration (linear counter), within a run;
	public static long                 fitCalls;			// Number of fitness calls within a run. This is incremented by Problem.computeFitness(...);
															// NOTE: This must be initialized by each parameterize() call!
	
	public ParEngine(String parParamFile){
		ParParameter.initializeParameters(parParamFile); 	// Initialize and validate Parameterless parameters.
		problem    = ParParameter.initializeProblem();		// Design Pattern Strategy.
		EAlgorithm = ParParameter.initializeEAlgorithm();	// Design Pattern Strategy.		
		ParPress.initializePress();
	}															
															// Perform parameterless strategy with the chosen EAlgorithm.
	public int RUN(int nRun){								// CHECK: Pelikan et al., 'Parameter-less Hierarchical Bayesian Optimization Algorithm'.
		fitCalls = 0;										// Initialize the number of fitness calls for this run.
		iteration = 0;
		Stopper.setSuccess(false);							// If we're still looking for Ones, we haven't reached success yet.
		IEASolvers = new ArrayList<IEASolver>();			// Initialize the list of active Solvers;
		IEASolvers.add(EAlgorithm.newIEASolver(N0));		// Initialize the first Solver.
		bestSoFar = new BestSoFar();						// NOTE: Do not change the initialization order!
		
		if(nextSolver == 0)
			return STANDALONE(nRun);
		else{
			PEA pea = new PEA();
			return pea.run(nRun);
		}
	}
		
	private int STANDALONE(int nRun){
		IEASolver currentSolver = IEASolvers.get(0);				// The Standalone algorithm is the only active algorithm.
		do{
			iteration++;
			boolean stopped = currentSolver.nextGeneration();
			bestSoFar.updateBest(currentSolver, 0);					// Update information about best individuals. NOTE: This information is automatically written in the bestFileName.txt.
			ParPress.printCurrentSolverInfo(currentSolver, 0);
			if(stopped){											// The Standalone algorithm has stopped. Update data and proceed to next run.
				ParPress.printRunFinalStats(currentSolver, N0);
				return Stopper.foundOptimum()? 1 : 0;
			}
		}while(true);
	}
	
	protected class PEA{
		private int solverPosition;
		private int highestN;
		private int lastSolver;
		
		private PEA(){}																// Default constructor.
	
		private int run(int nRun){
			solverPosition = 0;														// Initialize the current Solver position.				
			highestN = N0;															// Highest current population size.
			lastSolver = 0;															// Initialize the position of the last active Solver;
			do{
				IEASolver currentSolver = IEASolvers.get(solverPosition);
				if(currentSolver.getDummy()){										// This is a DUMMY solver.
					if(solverPosition == 0){
						deleteSolvers(0);
						if(IEASolvers.isEmpty())									// If there are no active Solvers we must generate the next one. 
							addNextSolver();
					}
					else{															// The Dummy Solver is in the mid section. Keep it active.
						currentSolver.currentGeneration++;							// Perform a Dummy generation.						
						ParPress.printCurrentSolverInfo(currentSolver, solverPosition);
						iteration++;
						solverPosition++;
					}
				}
				else{																// This is NOT a dummy solver. Perform PEA.
					iteration++;
					boolean stopped = currentSolver.nextGeneration();				// Perform the next generation for the current Solver. Class Stopper is responsible for computing the value of 'stopped' in each iteration.				
					bestSoFar.updateBest(currentSolver, solverPosition);			// Update information about best individuals. NOTE: This information is automatically written in the bestFileName.txt.			
					ParPress.printCurrentSolverInfo(currentSolver, solverPosition);
				
					if(Stopper.foundOptimum()){										// Optimum found. Return 1 (= success).
						ParPress.printRunFinalStats(currentSolver, highestN);
						return 1;					
					}
					if(stopped){													// The current Solver has stopped without finding the optimum solution.
						stopSolvers(solverPosition, solverPosition);	
						if(IEASolvers.isEmpty())									// If there are no active Solvers we must generate the next one. 
							addNextSolver();
						continue;													// Current Solver was stopped. Just perform the next iteration.
					}
					if(solverPosition > 0){											// Check Fitness Invariant.
						boolean invariant = true;
						int i = 0;
						do{
							IEASolver previousSolver = IEASolvers.get(i);
							if(!previousSolver.getDummy())
								invariant = previousSolver.getAvgFitness() > currentSolver.getAvgFitness() || 	
											previousSolver.getFitnessCalls() < currentSolver.getFitnessCalls();
							if(!invariant){											// Current Solver has a better average fitness and performed fewer fitness calls.
								stopSolvers(i, solverPosition-1);					// Fitness Invariant does not hold for Solver 'i'. Stop Solver 'i' and all Solvers in between.
								break;												// Found the first Solver that does not comply with 'invariant'. 
							}														// Make all solvers from this position up to currentSolver (exclusive) Dummy.
							i++;
						}while(invariant && i < solverPosition);	
					}																// END: if(solverPosition > 0)
					if(currentSolver.getCurrentGeneration() % nextSolver == 0){		// It is time to perform one iteration with next Solver, using PEA.
						solverPosition++;	
						if(solverPosition > lastSolver)								// If there is no active next Solver we must generate the next one.
							addNextSolver();
					}
					else															// Keep using the first Solver.											
						solverPosition = 0;	 
				}																	// END: NOT a Dummy Solver.
			}while(true);
		}					 														// END: PEA(int nRun).								
		
		private void stopSolvers(int i, int j){
			if(i == 0)
				deleteSolvers(j);
			else
				for(int k = i; k <= j; k++)
					IEASolvers.get(k).setDummy(true);
		}
	
		private void deleteSolvers(int j){
			IEASolvers.subList(0, j+1).clear();										// Keep Fitness Invariant.
			lastSolver -= (j+1);													// Update the number of active Solvers.
			solverPosition = 0;														// Update the current Solver position.
		}				
	
		private void addNextSolver(){
			highestN *= 2;
			IEASolvers.add(EAlgorithm.newIEASolver(highestN));			
			lastSolver++;
		}
	} 
	// END: Private class PEA
			
}// END: ParEngine class.





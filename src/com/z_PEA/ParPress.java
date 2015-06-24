package com.z_PEA;

import java.io.BufferedWriter;
import java.io.FileWriter;


public class ParPress{
	
	private static String         testFileName;							// This file stores all the information that is also printed in the console during an entire run.
	private static FileWriter     fstreamTest;
	private static BufferedWriter testFileOut;
	
	private static String         testFileNameStats;					// This file stores only the statistics necessary to generate graphics.
	private static BufferedWriter testFileOutStats;
	
	
	public static void initializePress(){
		String str = ParEngine.EAlgorithm.getName() + "_m" + ParEngine.nextSolver + "_" +
					 Problem.problemName + "_n" + Problem.n + ".txt";
		if(ParEngine.nextSolver > 99){
			testFileName      = "STANDARD_"       + str;
			testFileNameStats = "STANDARD-STATS_" + str;
		}
		else{
			testFileName      = "PARAMETERLESS_"       + str;
			testFileNameStats = "PARAMETERLESS-STATS_" + str;
		}
		try{
			fstreamTest 	 = new FileWriter(testFileName);			// 'true' => Append to file.
			testFileOut 	 = new BufferedWriter(fstreamTest);
			
			fstreamTest      = new FileWriter(testFileNameStats);		// 'true' => Append to file.
			testFileOutStats = new BufferedWriter(fstreamTest);
		}catch(Exception e){System.err.println("ERROR: " + e.getMessage());}
	}
	
	public static void printString(String str){							// NOTE: Use this method to print simultaneous in 
		System.out.println(str);										//	 	 the console and in the testFileOut.
		try{	
			testFileOut.write("\n" + str);
		}catch(Exception e){System.err.println("ERROR: " + e.getMessage());} 
	}
	
	private static void printStats(String str){							// NOTE: Use this method to print in the STATS file.										//	 	 the console and in the testFileOut.
		try{	
			testFileOutStats.write(str + "\n");
		}catch(Exception e){System.err.println("ERROR: " + e.getMessage());} 
	}
	
	
	public static void printInitialInfo(){
		String str;
		if(ParEngine.nextSolver > 99)
			str = "############ - STANDALONE METHOD - ############ - STANDALONE METHOD - ############\n#" +
				  "\n#   PARAMETERLESS: " +
				  "\n#     ->    Number of Runs = " + PARAMETERLESS.parRuns      +
				  "\n#     ->   Population Size = " + ParEngine.N0;                
		else
			str = "############ - PARAMETERLESS METHOD - ############ - PARAMETERLESS METHOD - ############\n#" +
				  "\n#" +
				  "\n#   PARAMETERLESS: " +
				  "\n#     ->    Number of Runs = " + PARAMETERLESS.parRuns      +
				  "\n#     -> Initial Pop. Size = " + ParEngine.N0               +
				  "\n#     ->        nextSolver = " + ParEngine.nextSolver;
				  
		 str   += "\n#" +
				  "\n#   EALGORITHM: " + ParEngine.EAlgorithm                    +
				  "\n#" +
				  "\n#   PROBLEM:" +
				  "\n#     ->              Name = " + Problem.problemName        +
				  "\n#     ->       String size = " + Problem.n                  +
				  "\n#" +
				  "\n#   STOPPER:" + 
				  "\n#     ->    maxGenerations = " + Stopper.maxNGen            +
				  "\n#     ->       maxFitCalls = " + Stopper.maxFitCalls        +					 
				  "\n#     ->   allFitnessEqual = " + Stopper.allFitnessEqual    +
				  "\n#     ->           epsilon = " + Stopper.epsilon            +
				  "\n#     ->        maxOptimal = " + Stopper.maxOptimal         +
				  "\n#     ->      foundBestFit = " + Stopper.foundBestFit       +
				  "\n#     ->         foundOnes = " + Stopper.foundOnes          +
				  "\n#" +
				  "\n############################################################################\n";
		printString(str);		
		
		printStats("StringSize  TotalIteration  BestPopIteration  HighestPopSize  BestPopSize  TotalFitCalls  BestPopFitCalls  AvgFitness  BestFitness");
	}
	
	
	public static void printRunInitialInfo(int r){
		printString("\n##### START RUN " + (r+1) + "/" + PARAMETERLESS.parRuns + " #####"     + 
			 	      "##### START RUN " + (r+1) + "/" + PARAMETERLESS.parRuns + " #####"     +
			 	      "##### START RUN " + (r+1) + "/" + PARAMETERLESS.parRuns + " #####\n\n" +
			 	      "Iteration           Pop. Size           Generation           Avg. Fitness           BestCurrentFitness           BestFitnessSoFar");
	}
	
	
	public static void printCurrentSolverInfo( IEASolver currentSolver, int solverPosition){
		if(ParEngine.iteration%30 == 0)
			printString("\nIteration           Pop. Size           Generation           Avg. Fitness           BestCurrentFitness           BestFitnessSoFar");
		if(currentSolver.getDummy())
			printString(String.format("%5d %18d (%d) %16d %23s %24s %26.1f (%d)%n",
							ParEngine.iteration, 
							currentSolver.getN(), solverPosition,
							currentSolver.getCurrentGeneration(),
							"DUMMY", "DUMMY", 
							ParEngine.bestSoFar.getFitness(),
							ParEngine.bestSoFar.getPopulation().getN()
						)
				);
		else
			printString(String.format("%5d %18d (%d) %16d %23s %24s %26.1f (%d)%n",
							ParEngine.iteration, 
							currentSolver.getN(), solverPosition,
							currentSolver.getCurrentGeneration(),
							currentSolver.getAvgFitness(),
							currentSolver.getCurrentPopulation().getBestFit(),
							ParEngine.bestSoFar.getFitness(),
							ParEngine.bestSoFar.getPopulation().getN()
					)
				);	
	}
	
	public static void printRunFinalStats(IEASolver currentSolver, int highestN){
		Population population = currentSolver.getCurrentPopulation();
		printStats(String.format("%6d %13d %15d %16d %14d %16d %13d %15.2f %12.2f",
						Problem.n,
						ParEngine.iteration,
						currentSolver.getCurrentGeneration(),
						highestN,
						currentSolver.getN(),
						ParEngine.fitCalls,
						currentSolver.getFitnessCalls(),
						population.getAvgFit(), 
						population.getBestFit())
					);
	}

	
	public static void printRunFinalInfo(int r){
		String str = "\n############################################################################" +
	                 "\n#"                                                                            +
					 "\n#               Success: " + Stopper.foundOptimum()                           + 
					 "\n#  Current Success Rate: " + PARAMETERLESS.nSuccess + "/" + (r+1)             +					 
					 "\n#   Total Fitness Calls: " + ParEngine.fitCalls                               +
					 "\n#  Best Population Size: " + ParEngine.bestSoFar.getPopulation().getN()       +
					 "\n#          Best Fitness: " + ParEngine.bestSoFar.getFitness()                 +
					 "\n#"                                                                            +
					 "\n######## END RUN " + (r+1) + "/" + PARAMETERLESS.parRuns + " #####"           + 
					      "##### END RUN " + (r+1) + "/" + PARAMETERLESS.parRuns + " #####"           +
					      "##### END RUN " + (r+1) + "/" + PARAMETERLESS.parRuns + " ########\n\n\n";
		printString(str);
	}
	
	
	public static void printFinalInfo(){
		printString("\nSUCCESS RATE = " + PARAMETERLESS.nSuccess + "/" + PARAMETERLESS.parRuns + "\n");
		closeTestFiles();
	}
	
	private static void closeTestFiles(){		
		try{testFileOut.close();}
		catch(Exception e){System.err.println("ERROR: " + e.getMessage());}
		try{testFileOutStats.close();}
		catch(Exception e){System.err.println("ERROR: " + e.getMessage());}
	}
	
}














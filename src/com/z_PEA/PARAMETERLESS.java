package com.z_PEA;

import java.util.Random;


public class PARAMETERLESS{
	private static String parParamFile;						// Name of the main parameters file.
	public static long         parRuns; 					// Number of runs to perform with the same problem.
	public static int	      nSuccess = 0;	 		 		// Number of successful runs;
	public static Random        random = new Random();
	
	public static void main(String[] args){		
		//random.setSeed(654324);							// This will fix the sequence of seeds that will be used on each run of the EA.
												
		parParamFile = args[0];
		ParEngine parEngine = new ParEngine(parParamFile);	// Initialize the Parameterless Engine.
		ParPress.printInitialInfo();
		for(int r = 0; r < PARAMETERLESS.parRuns; r++){
			ParPress.printRunInitialInfo(r);
			nSuccess += parEngine.RUN(r);					// Perform one run of the parameterless problem.	
			ParPress.printRunFinalInfo(r);
		}
		ParPress.printFinalInfo();
	}		
}		
		
		






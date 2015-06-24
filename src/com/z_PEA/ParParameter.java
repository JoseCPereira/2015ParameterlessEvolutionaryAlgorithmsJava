package com.z_PEA;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Scanner;


class ParParameter{
	
	// NOTE!! optionNames must coincide exactly with the option names in the 'Parameters.txt' file.
	private static final String optionNames[] =     
	{"parRuns", "nextSolver", "problemType", "stringSize", "optimumValue", "sigmaK", "kay", "eAlg", "eaParamFile", 
	 "N0", "maxNGen", "maxFitCalls", "allFitnessEqual", "epsilon", "maxOptimal", "foundBestFit", "foundOnes"};
	
	// Problem type
	private static int    problemType;	// Problem type. Check 'BParameters.txt' for the option menu.
	private static int     stringSize;	// Size of each individual. 	NOTE: This parameter must be set accordingly to the problem type.
	private static float optimumValue;	// Best fitness. 				NOTE: This parameter must be set accordingly to the problem type.
	private static double      sigmaK;	// Standard deviation of noise. NOTE: Set sigmaK = 0 for non-NOISY problems.
	private static int            kay;	// Order of TRAP-K problems.	NOTE: This is only for the TRAP-K problems.
	// EA
	private static int           eAlg;	// Estimation Distribution Algorithm. Check 'BParameters.txt' for the option menu.
	private static String eaParamFile;	// Parameter file for the corresponding EA.
	
	public static int getProblemType(){return problemType;}		// NOTE: This is for testing only!!
	public static double getSigmaK(){return sigmaK;}			// 		 Check ParEngine.parametrize(...)
	
	
	// NOTE: Execute this initialization PRIOR to any other.
	public static void initializeParameters(String parameterFile){		
		try{
			// Open the file to be read
			FileInputStream fstream = new FileInputStream(parameterFile);
			// Create an object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader buff = new BufferedReader(new InputStreamReader(in));
			int nLine = 0; // Line number
			String line;
			while((line = buff.readLine())!= null){
				nLine++; 						// Increment line number
				if(line.length() > 0){		   	// Ignore empty lines
					if(line.charAt(0) != '#'){ 	// Ignore comments
						Scanner scanner = new Scanner(line);
						scanner.useDelimiter("=");
						// Get option name and value. If not valid, exit program!
						String optionName = scanner.next().trim();
						validateOptionName(line, optionName, nLine);
						String optionValue = scanner.next().trim(); 
						validateOptionValue(optionName, optionValue, nLine); 
					}
				}
			}
			in.close();
		}  // Catch open file  error.
		catch(Exception e){
			System.err.println("Error: " + e.getMessage());
		}
	}	
	
	private static void validateOptionName(String line, String option, int nLine){
		if(option.length() >= line.length())
			exitError("Line " + nLine + " --> Missing equal sign '='");
		if(!validateName(option))
			exitError("Line " + nLine + " --> INVALID OPTION NAME '" + option + "'");
	}
	
	private static boolean validateName(String name){
		for(int i = 0; i < optionNames.length; i++)
			if(name.equals((String)optionNames[i]))
				return true;
		return false;
	}
	
	// Usage of 'switch' with 'String' only legal for Java SE 7 or later!
	private static void validateOptionValue(String optionName,String optionValue, int nLine)
	throws NumberFormatException{
		if(optionName.equals("parRuns")){
			PARAMETERLESS.parRuns = Long.parseLong(optionValue);
			if(PARAMETERLESS.parRuns <= 0)
				exitError("Line " + nLine + " --> Number of runs to perform must be a POSITIVE integer.");
			return; // Option validated!!
		}
		if(optionName.equals("nextSolver")){
			ParEngine.nextSolver = Integer.parseInt(optionValue);
			if(ParEngine.nextSolver <= 1 && ParEngine.nextSolver != 0)
				exitError("Line " + nLine + " --> Number of generations until next Solver must be a positive integer " +
												 "greater than one (PEBS) or 0 (STANDALONE)");
			return; // Option validated!!
		}
		if(optionName.equals("N0")){
			ParEngine.N0 = Integer.parseInt(optionValue);
			if(ParEngine.N0 <= 0)
				exitError("Line " + nLine + " --> Population size must be a POSITIVE integer.");
			return; // Option validated!!
		}
		if(optionName.equals("eAlg")){
			eAlg = Integer.parseInt(optionValue); 			// NOTE: Add new option for each new problem
			if(!(eAlg == 0 || eAlg == 1 || eAlg == 2 || eAlg == 3)) 
				exitError("Line " + nLine + " --> INVALID EA. Please check 'ParParameters.txt' for the complete list of valid problems.");
			return; // Option validated!!
		}
		if(optionName.equals("eaParamFile")){
			eaParamFile = optionValue;			 			// No need to check. 'optionValue' already contains the name of the parameter file.
			return;
		}
		if(optionName.equals("problemType")){
			problemType = Integer.parseInt(optionValue);  	// NOTE: Add new option for each new problem
			if(!((problemType >= 0 && problemType <= 7) || (problemType >= 10 && problemType <= 17) || problemType == 21 || problemType == 22 || problemType == 30)) 
				exitError("Line " + nLine + " --> INVALID Problem Type. Please check 'BParameters.txt' for the complete list of valid problems.");
			return; // Option validated!!
		}		
		if(optionName.equals("kay")){						// NOTE: This is only for the TRAP-K problems.
			kay = Integer.parseInt(optionValue);
			if(kay <= 1)
				exitError("Line " + nLine + " --> Order of TRAP-K must be an integer greater than one.");
			return; // Option validated!!
		}
		// NOTE: Link string size to problem type!
		if(optionName.equals("stringSize")){
			stringSize = Integer.parseInt(optionValue);
			if(stringSize <= 0)
				exitError("Line " + nLine + " --> Individual string size must be a POSITIVE integer.");
			return; // Option validated!!
		}
		if(optionName.equals("optimumValue")){
			optimumValue = Float.parseFloat(optionValue);
			// optimumValue is problem dependent, there are no restrictions on possible values.
			return; // Option validated!!  
		}
		if(optionName.equals("sigmaK")){
			sigmaK = Double.parseDouble(optionValue);
			if(sigmaK < 0)
				exitError("Line " + nLine + " --> Noise standard deviation must be a NON-NEGATIVE Number.");
			return; // Option validated!!  
		}
		if(optionName.equals("maxNGen")){
			Stopper.maxNGen = Integer.parseInt(optionValue);
			if(Stopper.maxNGen <= 0)
				exitError("Line " + nLine + " --> Maximal number of generations must be a POSITIVE integer.");
			return; // Option validated!!
		}
		if(optionName.equals("maxFitCalls")){
			Stopper.maxFitCalls = Long.parseLong(optionValue);
			if(Stopper.maxFitCalls <= 0 && Stopper.maxFitCalls != -1)
				exitError("Line " + nLine + " --> Maximal number of fitness calls must be either '-1' or a POSITIVE integer.");
			return; // Option validated!!
		}
		if(optionName.equals("allFitnessEqual")){
			Stopper.allFitnessEqual = Integer.parseInt(optionValue);
			if(!(Stopper.allFitnessEqual == -1 || Stopper.allFitnessEqual == 1))
				exitError("Line " + nLine + " --> 'allFitnessEqual' must be either '-1' or '1'.");
			return; // Option validated!!
		}
		if(optionName.equals("epsilon")){
			Stopper.epsilon = Float.parseFloat(optionValue);
			if((Stopper.epsilon < 0 || Stopper.epsilon > 1) && Stopper.epsilon != -1)
				exitError("Line " + nLine + " --> Termination threshold for the univariate frequencies must be either '-1' or a number BETWEEN '0' and '1'.");
			return; // Option validated!!
		}
		if(optionName.equals("maxOptimal")){
			Stopper.maxOptimal = Float.parseFloat(optionValue);
			if((Stopper.maxOptimal < 0 || Stopper.maxOptimal > 1) && Stopper.maxOptimal != -1)
				exitError("Line " + nLine + " --> Optimal and non-optimal ind. threshold must be either '-1' or a number BETWEEN '0' and '1'.");
			return; // Option validated!!
		}
		if(optionName.equals("foundBestFit")){
			Stopper.foundBestFit = Integer.parseInt(optionValue);
			if(Stopper.foundBestFit != -1 && Stopper.foundBestFit != 1)
				exitError("Line " + nLine + " --> 	INVALID option for stopWhenFoundOptimum.");
			return; // Option validated!!
		}
		if(optionName.equals("foundOnes")){
			Stopper.foundOnes = Integer.parseInt(optionValue);
			if(Stopper.foundOnes != -1 && Stopper.foundOnes != 0 && Stopper.foundOnes != 1)
				exitError("Line " + nLine + " --> 	INVALID option for stopWhenFoundOnes.");
			return; // Option validated!!
		}
		if(true)
			exitError("Line" + nLine +
					  " --> If you are reading this message something is FUNDAMENTALLY WRONG with 'validateOptionValue(String, String, int)'.\n" + 
					  "You may contact the author at 'unidadeimaginaria@gmail.com'\n" +
					  "Sorry for the inconvenience!");
	}// END: validateOptionValue(...)
	
	
	// NOTE: When implementing a new problem one must add a corresponding new case.
	public static Problem initializeProblem(){	// Design Pattern Strategy
		switch(problemType){
			// ZERO Problems
			case 0: return new Problem(new ZeroMax(), stringSize, optimumValue, sigmaK);
			case 1: return new Problem(new ZeroQuadratic(), stringSize, optimumValue, sigmaK);
			case 2: return new Problem(new ZeroThreeDeceptive(), stringSize, optimumValue, sigmaK);
			case 3: return new Problem(new ZeroThreeDeceptiveBiPolar(), stringSize, optimumValue, sigmaK);
			case 4: return new Problem(new ZeroThreeDeceptiveOverlapping(), stringSize, optimumValue, sigmaK);
			case 5: return new Problem(new ZeroTrapK(kay), stringSize, optimumValue, sigmaK);
			case 6: return new Problem(new ZeroUniformSixBlocks(), stringSize, optimumValue, sigmaK);
			// ONE Problems
			case 10: return new Problem(new OneMax(), stringSize, optimumValue, sigmaK);
			case 11: return new Problem(new Quadratic(), stringSize, optimumValue, sigmaK);
			case 12: return new Problem(new ThreeDeceptive(), stringSize, optimumValue, sigmaK);
			case 13: return new Problem(new ThreeDeceptiveBiPolar(), stringSize, optimumValue, sigmaK);
			case 14: return new Problem(new ThreeDeceptiveOverlapping(), stringSize, optimumValue, sigmaK);
			case 15: return new Problem(new TrapK(kay), stringSize, optimumValue, sigmaK);
			case 16: return new Problem(new UniformSixBlocks(), stringSize, optimumValue, sigmaK);
			// OTHER Problems
			case 21: return new Problem(new HierarchicalTrapOne(), stringSize, optimumValue, sigmaK);
			case 22: return new Problem(new HierarchicalTrapTwo(), stringSize, optimumValue, sigmaK);
			case 30: return new Problem(new IsingSpin(), stringSize, optimumValue, sigmaK);
			default: exitError("If you are reading this message something is FUNDAMENTALLY WRONG with the validation of the 'problemType' value.\n" + 
					   		"You may contact the author at 'unidadeimaginaria@gmail.com'\n" +
					   		"Sorry for the inconvenience!");
			 		 // This line is never executed!
					 return new Problem(new OneMax(), stringSize, optimumValue, sigmaK);
		}
	}
	
	public static IEAlgorithm initializeEAlgorithm(){	// Design Pattern Strategy
		switch(eAlg){								
			case 0: return new SGA(eaParamFile);
			case 1: return new UMDA(eaParamFile);
			case 2: return new ECGA(eaParamFile);
			case 3: return new HBOA(eaParamFile);
			default: exitError("If you are reading this message something is FUNDAMENTALLY WRONG with the validation of the 'eAlg' value.\n" + 
			   				"You may contact the author at 'unidadeimaginaria@gmail.com'\n" +
			   				"Sorry for the inconvenience!");
	 				 // This line is never executed!
					 return new HBOA(eaParamFile);
		}
	}
	
	
	// Input error found!! Exit program!
	public static void exitError(String message){
		System.err.println(new Error(message));
		System.exit(1);
	}
	
}// End of class Parameter






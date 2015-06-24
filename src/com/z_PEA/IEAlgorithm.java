package com.z_PEA;

//DESIGN PATTERN STRATEGY
abstract class IEAlgorithm{
	
	protected static String paramFile;
	
	
	public abstract IEASolver newIEASolver(int currentN);
	
	public abstract String getName();
	public abstract String toString();
}






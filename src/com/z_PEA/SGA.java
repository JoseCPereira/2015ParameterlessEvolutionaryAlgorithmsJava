package com.z_PEA;

import com.a_SGA.SGASolver;


class SGA extends IEAlgorithm{
	
	public SGA(String parFile){IEAlgorithm.paramFile = parFile;}
	
	public String getName(){return "SGA";}
	
	// Design Pattern Strategy.
	public IEASolver newIEASolver(int currentN){
		return new SGASolver(paramFile, currentN);
	} 
	
	public String toString(){
		String indent = "     ";
		return " SGA:\n" + com.a_SGA.Parameter.writeParameters(indent);
	}
}

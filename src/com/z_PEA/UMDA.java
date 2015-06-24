package com.z_PEA;

import com.b_UMDA.UMDASolver;


class UMDA extends IEAlgorithm{
	
	public UMDA(String parFile){IEAlgorithm.paramFile = parFile;}
	
	public String getName(){return "UMDA";}
	
	public IEASolver newIEASolver(int currentN){return new UMDASolver(paramFile, currentN);} // Design Pattern Strategy.
	
	public String toString(){
		String indent = "     ";
		return " UMDA:\n" + com.b_UMDA.Parameter.writeParameters(indent);
	}
}



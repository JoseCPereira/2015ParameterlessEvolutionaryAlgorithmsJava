package com.z_PEA;

import com.d_HBOA.HBOASolver;


class HBOA extends IEAlgorithm{
	
	public HBOA(String parFile){IEAlgorithm.paramFile = parFile;}
	
	public String getName(){return "HBOA";}
	
	public IEASolver newIEASolver(int currentN){return new HBOASolver(paramFile, currentN);} // Design Pattern Strategy.
	
	public String toString(){
		String indent = "     ";
		return " HBOA:\n" + com.d_HBOA.Parameter.writeParameters(indent);
	}
}



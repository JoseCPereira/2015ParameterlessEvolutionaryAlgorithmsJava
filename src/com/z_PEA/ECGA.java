package com.z_PEA;

import com.c_ECGA.ECGASolver;


class ECGA extends IEAlgorithm{
	
	public ECGA(String parFile){IEAlgorithm.paramFile = parFile;}
	
	public String getName(){return "ECGA";}
	
	public IEASolver newIEASolver(int currentN){return new ECGASolver(paramFile, currentN);} // Design Pattern Strategy.
	
	public String toString(){
		String indent = "     ";
		return " ECGA:\n" + com.c_ECGA.Parameter.writeParameters(indent);
	}
}



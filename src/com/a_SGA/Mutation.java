package com.a_SGA;

import com.z_PEA.Individual;
import com.z_PEA.PARAMETERLESS;
import com.z_PEA.Problem;

class Mutation{
	private double pMutation;											// Probability of mutation;
	
	public Mutation(double pMutation){this.pMutation = pMutation;}
	
	public double getPMutation(){return pMutation;}
	
	public void mutate(Individual[] newIndividuals){
		if(pMutation > 0)												// Perform mutation only if pMutation > 0.
			for(int i = 0; i < newIndividuals.length; i++)
				for(int j = 0; j < Problem.n; j++)
					if(PARAMETERLESS.random.nextDouble() < pMutation){	// Perform mutation in every position with probability pMutation.
						char allele = newIndividuals[i].getAllele(j);
						if(allele == '0')
							newIndividuals[i].setAllele(j, '1');
						else
							newIndividuals[i].setAllele(j, '0');
					}	
	}
}
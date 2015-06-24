package com.c_ECGA;

import java.util.ArrayList;

import com.z_PEA.Individual;
import com.z_PEA.PARAMETERLESS;
import com.z_PEA.Problem;
import com.z_PEA.SelectedSet;


class MPModel{
	public int offspringSize;								// NOTE: The MPM is responsible for sampling new individuals.
	
	private ArrayList<Subset> subsets;  					// NOTE: Consider using Subset[] array instead.
	private Cache cache;
	
	
	public MPModel(int offSize){offspringSize = offSize;}	// initializeMPM(...) is responsible for initializing the MPM structure.	
	
	public ArrayList<Subset> getSubsets(){return subsets;}
	public Subset getSubset(int i){return subsets.get(i);}
	public int getNSets(){return subsets.size();}
	public Cache getCache(){return cache;}
	
	public void generateModel(SelectedSet selectedSet){
		initializeMPM(selectedSet);
		while(cache.getSize() > 0){						
			Subset newSet = cache.mergedSets.pollFirst();
			int id1 = cache.idA.pollFirst(),
				id2 = cache.idB.pollFirst();				// 1. Retrieve and delete from the cache all the information about the best subset.
			cache.compressions.removeFirst();
			int maxID = Math.max(id1, id2),					// 2. Update the MPM.
				minID = Math.min(id1,  id2);
			subsets.set(minID,  newSet);					// 	  2.1. Replace setA with the new merged set.
			subsets.remove(maxID);							//    2.2. Remove setB.
			cache.removeSubsets(minID, maxID);				// 3. Update Cache. Remove from the cache any subsets composed by setA or setB. 
															//    Also update all id's greater than maxID.
			for(int i = 0; i < subsets.size(); i++)			// 4. Compute the new subsets and insert them in the cache.
				if(i != minID){								// NOTE: 'minID' is now the ID for the new merged set in 'subsets'.
					Subset setI = subsets.get(i),
						   mergeSet = setI.merge(newSet, selectedSet);
					double compI = setI.getCC(),
						   compJ = newSet.getCC(),
						   compIJ = mergeSet.getCC(),
						   compression = compI + compJ - compIJ;
					if(compression > 0 && mergeSet.getNFrequencies() < selectedSet.getN())	// NOTE: Assert that the new merge set is not too big.
						cache.insertSubset(mergeSet, i, minID, compression);				// NOTE: Insertion in the cache is in descending order of compression.
				}
		}// END: Cache loop.
	}// END: generateModel(...)
	
	public void initializeMPM(SelectedSet selectedSet){
		subsets = new ArrayList<Subset>();					// 0. Initialize MPM structure.	
		for(int i = 0; i < Problem.n; i++)					// 1. Initial MPM is [0][1][2]...[Problem.n-1].
			subsets.add(new Subset(selectedSet, i));		// NOTE: The Subset constructor is responsible for computing the partial complexities of each set
		cache  = new Cache();								// 2. Initialize the Cache.
		int nSets = Problem.n;						 
		for(int i = 0; i < nSets-1; i++)					// Compute the new subsets and insert them in the cache. 
			for(int j = i+1; j < nSets; j++){				// NOTE: Insertion in the cache is in descending order of compression.
				Subset setI = subsets.get(i),
					   setJ = subsets.get(j),
					   mergeSet = setI.merge(setJ, selectedSet);
				double compI = setI.getCC(),
					   compJ = setJ.getCC(),
					   compIJ = mergeSet.getCC(),
					   compression = compI + compJ - compIJ;
				if(compression > 0)
					cache.insertSubset(mergeSet, i, j, compression);
			}
	}
	
	public Individual[] sampleNewIndividuals(SelectedSet selectedSet){
		Individual[] newIndividuals = new Individual[offspringSize];
		for(int i = 0; i < offspringSize; i++)
			newIndividuals[i] = new Individual();
		for(Subset subset: subsets){
			int [] xList = subset.getXList();
			for(int i = 0; i < offspringSize; i++){
				int pick = PARAMETERLESS.random.nextInt(offspringSize);
				Individual pickIndiv = selectedSet.getIndividual(pick);
				for(int l = 0; l < xList.length; l++){
					int locus = xList[l];
					char allele = pickIndiv.getAllele(locus);
					newIndividuals[i].setAllele(locus, allele);
				}
			}
		}
		return newIndividuals;
	}
		
	public String toString(){
		return "Subsets: " + subsets + "\nCache: " + cache; 
	}
}










	


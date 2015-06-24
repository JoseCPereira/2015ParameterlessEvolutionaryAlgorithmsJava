package com.c_ECGA;

import java.util.LinkedList;


class Cache{
	
	public LinkedList<Subset> mergedSets;									// NOTE: Try implementing subclass 'MergedSet' instead.
	public LinkedList<Integer> idA, idB;
	public LinkedList<Double> compressions;
	
	public Cache(){
		mergedSets = new LinkedList<Subset>();
		idA = new LinkedList<Integer>();
		idB = new LinkedList<Integer>();
		compressions = new LinkedList<Double>();
	}
	
	public int getSize(){return mergedSets.size();}
	public LinkedList<Double> getCompressions(){return compressions;}
	public double getCompression(int i){return compressions.get(i);}
	
	private void insert(int position, Subset mergedSet, int id1, int id2, double compression){
		mergedSets.add(position, mergedSet);								// We are out of the loop! Insert in position left.
		idA.add(position, id1);		
		idB.add(position, id2);
		compressions.add(position, compression);
	}
	
	public void insertSubset(Subset mergedSet, int id1, int id2, double compression){
		int cacheSize = mergedSets.size();
		if(cacheSize == 0 || compression >= compressions.getFirst()){
			this.insert(0, mergedSet, id1, id2, compression);				// Insert in first position.
			return;
		}
		if(cacheSize > 0 && compression <= compressions.getLast()){
			this.insert(cacheSize, mergedSet, id1, id2, compression);		// Insert in the last position.
			return;
		}
		int left = 0,
			right = cacheSize - 1;
		while(left < right){
			int midPoint = (left + right)/2;
			double compressionTemp = compressions.get(midPoint);
			if(compression < compressionTemp)								// Insert in the right half: [midPoint+1; right].
				left = midPoint + 1;
			else if(compression > compressionTemp)							// Insert in the left half: [left; midPoint].
				right = midPoint;
			else{														
				this.insert(midPoint+1, mergedSet, id1, id2, compression);	// Equal compressions! Insert here!
				return;
			}
		}
		this.insert(left, mergedSet, id1, id2, compression);				// We are out of the loop! Insert in position left.
	}
	
	public void removeSubsets(int minID, int maxID){
		int k = mergedSets.size() - 1;
		while(k >= 0){
			int idAK = idA.get(k),
				idBK = idB.get(k);
			
			if(idAK == minID || idAK == maxID || idBK == minID || idBK == maxID){
				mergedSets.remove(k);
				idA.remove(k);
				idB.remove(k);
				compressions.remove(k);
			}
			else{
				if(idAK > maxID)		 
					idA.set(k, idAK-1);	 									// Update list idA after maxID removal from subsets@MPM.
				if(idBK > maxID)
					idB.set(k, idBK-1);	 									// Update list idB after maxID removal from subsets@MPM.
			}
			k--; 				 		 
		}	
	}
	
	public String toString(){return "Sets: " + mergedSets + "\nidA: " + idA + "\nidB: " + idB;}
}









package com.d_HBOA;

import java.util.LinkedList;


class Score{
	private int i,j,k; 		// variable, leaf, split, respectively.  
	private int m00, m01,	// Check DecisionGraph.possibleSplitFrequencies.
				m10, m11;
	private double score;
	
	public Score(int i, int j, int k, int m00, int m01, int m10, int m11, double score){
		this.i = i;
		this.j = j;
		this.k = k;
		this.m00 = m00;
		this.m01 = m01;
		this.m10 = m10;
		this.m11 = m11;
		this.score = score;
	}
	
	public void setI(int i){this.i = i;}
	public void setJ(int j){this.j = j;}
	public void setK(int k){this.k = k;}
	public void setM00(int m00){this.m00 = m00;}
	public void setM01(int m01){this.m01 = m01;}
	public void setM10(int m10){this.m10 = m10;}
	public void setM11(int m11){this.m11 = m11;}
	public void setScore(double score){this.score = score;}
	
	public int getI(){return i;}
	public int getJ(){return j;}
	public int getK(){return k;}
	public int getM00(){return m00;}
	public int getM01(){return m01;}
	public int getM10(){return m10;}
	public int getM11(){return m11;}
	public double getScore(){return score;}
	public String toString(){
		return "(" + i + ", " + j + ", " + k + ") ==> " + score;
	}
	
}

/////////////////////////////////////////////////////////////////////////////////
//
// ScoreList is a (doubly) LinkedList that is always in descent order of scores.
// The class provides the insertScore() method to be used by the
// scoreList in the Bayesian Network class.
//
/////////////////////////////////////////////////////////////////////////////////

class ScoreList{
		
	public static void insertScore(LinkedList<Score> scoreList, Score score){
		double scoreValue = score.getScore();
		if(scoreList.size() == 0 || scoreValue >= scoreList.getFirst().getScore()){ 	// NOTE: Keep conditional expression in this order!
			scoreList.offerFirst(score);												// Insert in the first position
			return;
		}
		if(scoreList.size() > 0 && scoreValue <= scoreList.getLast().getScore()){
			scoreList.offerLast(score);													// Insert in the last position
			return;
		}
		int left = 0,
			right = scoreList.size() - 1;
		while(left < right){
			int midPoint = (left + right)/2;
			double scoreTemp = scoreList.get(midPoint).getScore();
			if(scoreValue < scoreTemp)
				left = midPoint + 1;
			else if(scoreValue > scoreTemp)
				right = midPoint;
			else{
				scoreList.add(midPoint+1, score);
				return;
			}
		}
		scoreList.add(left, score);
	}
	
}






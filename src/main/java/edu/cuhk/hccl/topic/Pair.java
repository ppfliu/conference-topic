package edu.cuhk.hccl.topic;

public class Pair implements Comparable<Pair>{
	
	private int index;
	private double value;

	public Pair(){
		this.index = 0;
		this.value = 0;
	}
	
	public Pair(int index, double value){
		this.index = index;
		this.value = value;
	}
	
	@Override
	public int compareTo(Pair other) {
		
		return -Double.compare(this.value, other.value);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	public static Pair[] buildPairs(double[] distribution) {
		
		int length = distribution.length;
		Pair[] pairs = new Pair[length];
		for (int i = 0; i < length; i++){
			Pair pair = new Pair(i, distribution[i]);
			pairs[i] = pair;
		}
		
		return pairs;
	}	
}

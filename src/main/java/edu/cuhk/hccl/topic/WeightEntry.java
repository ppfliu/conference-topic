package edu.cuhk.hccl.topic;

public class WeightEntry implements Comparable<WeightEntry>{
	
	public int index;
	public double weight;
	
	public WeightEntry(int index, double weight) {
		this.index = index;
		this.weight = weight;
	}

	public int compareTo(WeightEntry entry) {
		
		int result = 0;
		if(this.weight < entry.weight)
			result = 1;
		else if (this.weight == entry.weight)
			result = 0;
		else
			result = -1;
			
		return result;
	}
}
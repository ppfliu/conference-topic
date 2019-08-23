package edu.cuhk.hccl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class AppAnalyzeSimilarity {

	public static void main(String[] args) throws IOException {
		
		System.out.println("Processing begins...\n");
		
		// Load the similarity file into a 2-dimensional array
		String dataFile = args[0];
			
		final float SIMILARITY_CUTOFF = Float.parseFloat(args[1]);
		
		String countFileName = "matching-count.txt";
		if (args.length >= 3)
			countFileName = args[2];
		File countFile = new File(countFileName);
		if (!countFile.exists())
			countFile.createNewFile();
		
		List<String> lines = FileUtils.readLines(new File(dataFile));
		
		float[][] similarities = new float[lines.size()][];
		
		for (int i = 0; i < lines.size(); i++){
			String line = lines.get(i);
			String[] strArray = line.split("\t");
			similarities[i] = new float[strArray.length];
			
			for (int j = 0; j < strArray.length; j++){
				similarities[i][j] = Float.parseFloat(strArray[j]);
			}
		}
		
		// Initialize the Map trackCounts with 0 for each track from 1 to 12 (similarities.length)
		Map<Integer, Integer> trackCounts = new HashMap<Integer, Integer>();
		for (int idx = 1; idx <= similarities.length; idx++){
			trackCounts.put(idx, 0);
		}
		
		// Analyze the 2d array to find top track for each topic
		StringBuilder matchBuffer = new StringBuilder();
		for (int j = 0; j < similarities[0].length; j++){ // loop topics
			// Find max of each column
			int maxIdx = 0;
			float maxSim = -1.0f;
			
			// Store the second (previous) max-index and max-similarity
			int prevMaxIdx = maxIdx;
			float prevMaxSim = maxSim;
			
			// Loop tracks to find top track for each topic
			for (int i = 0; i < similarities.length; i++){ 
				if (maxSim < similarities[i][j]){
					
					prevMaxIdx = maxIdx;
					prevMaxSim = maxSim;
					
					maxIdx = i;
					maxSim = similarities[i][j];
				} else if (maxSim > similarities[i][j]){
					if (prevMaxSim < similarities[i][j]){
						prevMaxIdx = i;
						prevMaxSim = similarities[i][j];
					}
				}
			}
			
			if (maxSim < SIMILARITY_CUTOFF )
				continue;
			
			// Increment count for the track "maxIdx + 1"
			trackCounts.put(maxIdx + 1, trackCounts.get(maxIdx + 1) + 1);
			
			matchBuffer.append(maxIdx + 1);
			matchBuffer.append(" ");
			
			System.out.println(String.format("Topic %d matches track %d with f-measure %.3f.", j, maxIdx + 1, maxSim));
			System.out.println(String.format("Topic %d matches second track %d with f-measure %.3f.", j, prevMaxIdx + 1, prevMaxSim));
			
		}
		System.out.println("\n" + matchBuffer.toString() + "\n");
			
		// Print out matching count for each track
		StringBuilder countBuffer = new StringBuilder();
		for (int idx = 1; idx <= similarities.length; idx++){
			int count = trackCounts.get(idx);
			countBuffer.append(count + " ");
			System.out.println(String.format("Track %d has %d topic matches.", idx, count));
		}
		
		String countLine = countBuffer.toString() + "\n";
		System.out.println("\n" + countLine);
		FileUtils.write(countFile, countLine, true);
		
		System.out.println("Processing is finished!");
	}

}

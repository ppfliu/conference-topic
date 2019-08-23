package edu.cuhk.hccl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import cc.mallet.util.CollectionUtils;

public class AppComputeJaccardCoefficient {
	
	public static void main(String[] args) throws IOException {
		
		// Get parameters
		File topicWordsFile = new File(args[0]);
		File trackWordsFile = new File(args[1]);
		File fscoreFile = new File(args[2]);
		File verboseFile = new File(args[3]);
		
		// Read the topic csv file and ignore the header
		List<String> csvLines = FileUtils.readLines(topicWordsFile);
		csvLines.remove(0);
		
		// Read track words
		List<String> trackWords = FileUtils.readLines(trackWordsFile);
		
		// Get topic words and word-probability map
		Map<String, Double> weightMap = new HashMap<String, Double>();
		List<String> topicWords = new ArrayList<String>();
		
		for (String line : csvLines){
			String[] cols = line.split(",");
			String word = cols[0];
			double probability = Double.parseDouble(cols[2]);
			
			topicWords.add(word);
			weightMap.put(word, probability);
		}
		
		// Get matching words
		Collection<?> matches = CollectionUtils.intersection(topicWords, trackWords);
		Collection<?> union = CollectionUtils.union(topicWords, trackWords);
				
		List<String> verbose = new ArrayList<String>();
		double fscore = 0.0d;
		
		if (!matches.isEmpty()) {
			
			verbose.add("[INFO] Matching Words:");
			List<String> wordsList = new ArrayList<String>();
			for (Object obj : matches){
				String word = (String) obj;
				wordsList.add(word);
			}
			
			Collections.sort(wordsList);
			
			verbose.addAll(wordsList);
			
			verbose.add("[INFO] Jaccard Coefficient:");
			
			if (union.size() > 0)
				fscore = matches.size() * 1.0 / union.size();
			
			verbose.add(String.format("Jaccard score: %.3f", fscore));
		}
		
		// Append f-score to outFile
		FileUtils.write(fscoreFile, String.format("%.3f", fscore) + "\n", true);
		
		// Write verbose to verboseFile
		FileUtils.writeLines(verboseFile, verbose, false);
	}

}

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


public class AppComputeFMeasure {
	
	public static void main(String[] args) throws IOException {
		
		// Get parameters
		File topicWordsFile = new File(args[0]);
		File trackWordsFile = new File(args[1]);
		String mode = args[2];
		File fscoreFile = new File(args[3]);
		File verboseFile = new File(args[4]);
		
		int probColumn = 2;
		if (args.length == 6)
			probColumn = Integer.parseInt(args[5]);
		
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
			double probability = Double.parseDouble(cols[probColumn]);
			
			topicWords.add(word);
			weightMap.put(word, probability);
		}
		
		// Get matching words
		Collection<?> matches = CollectionUtils.intersection(topicWords, trackWords);
				
		double fscore = 0.0d;
		
		List<String> verbose = new ArrayList<String>();
		
		if (!matches.isEmpty()) {
			
			verbose.add("[INFO] Matching Words:");
			List<String> wordsList = new ArrayList<String>();
			for (Object obj : matches){
				String word = (String) obj;
				wordsList.add(word);
			}
			Collections.sort(wordsList);
			
			verbose.addAll(wordsList);
			
			// true means using the weighted version
			if (mode.equalsIgnoreCase("true")) { 
				verbose.add("[INFO] Weighted Measure:");
				fscore = computeWeightedPRF(trackWords, weightMap, matches, verbose);
			} else {
				verbose.add("[INFO] Normal Measure:");
				fscore = computePRF(trackWords, topicWords, matches, verbose);
			}
		}
		
		// Append f-score to outFile
		FileUtils.write(fscoreFile, String.format("%.3f", fscore) + "\n", true);
		
		// Write verbose to verboseFile
		FileUtils.writeLines(verboseFile, verbose, false);
	}

	private static double computePRF(List<String> trackWords, List<String> topicWords,
			Collection<?> matches, List<String> verbose) {
		double precision = matches.size() * 1.0 / trackWords.size();
		double recall = matches.size() * 1.0 / topicWords.size();
		double fscore = 2 * precision * recall / (precision + recall);
		
		verbose.add(String.format("Precision: %.3f", precision));
		verbose.add(String.format("Recall: %.3f", recall));
		verbose.add(String.format("F-score: %.3f", fscore));
		
		return fscore;
	}
	
	private static double computeWeightedPRF(List<String> trackWords, Map<String, Double> weightMap,
			Collection<?> matches, List<String> verbose) {
		double precision = matches.size() * 1.0 / trackWords.size();
		
		double matchProbSum = 0.0d;
		for (Object obj : matches){
			matchProbSum += weightMap.get(obj);
		}
		
		double totalProb = 0.0d;	
		for (String key : weightMap.keySet()){
			totalProb += weightMap.get(key);
		}
		
		double recall = matchProbSum / totalProb;
		
		double fscore = 2 * precision * recall / (precision + recall);
		
		verbose.add(String.format("Precision: %.3f", precision));
		verbose.add(String.format("Recall: %.3f", recall));
		verbose.add(String.format("F-score: %.3f", fscore));
		
		return fscore;
	}

}

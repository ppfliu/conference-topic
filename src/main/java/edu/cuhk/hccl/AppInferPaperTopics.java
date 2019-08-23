package edu.cuhk.hccl;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import edu.cuhk.hccl.topic.Pair;
import edu.cuhk.hccl.topic.TopicModel;

public class AppInferPaperTopics {

	public static void main(String[] args) throws Exception {

		File modelFile = new File(args[0]);
		File dataFile = new File(args[1]);
		int iterations = Integer.parseInt(args[2]);
		int bgTopic = Integer.parseInt(args[3]);
		int topTopics = Integer.parseInt(args[4]);
		
		// Build instances
		List<String> dataLines = FileUtils.readLines(dataFile);
		String[] data = new String[dataLines.size()];
		InstanceList instances = TopicModel.createInstances(dataLines.toArray(data));
		System.out.println("[INFO] The number of instances is: " + instances.size());
		
		// Infer topics
		ParallelTopicModel model = ParallelTopicModel.read(modelFile);
		TopicInferencer inferencer = model.getInferencer();
		
		// Count papers for each topic
		int[] counter = new int[model.numTopics];
		
		// Calculate the mean topic probability for each topic
		double[] topicProbabilities = new double[model.numTopics];
		
		for (int index = 0; index < instances.size(); index++){
			Instance instance = instances.get(index);
			
			double[] distribution = inferencer.getSampledDistribution(instance, iterations, 1, 5);
			for (int i = 0; i < distribution.length; i++){
				topicProbabilities[i] += distribution[i];
			}
			
			Pair[] pairs = Pair.buildPairs(distribution);
			Arrays.sort(pairs);
			
			// We only count the top topTopics topics for each paper and ignore background topic
			int tmpCounter = 0;
			for (int t = 0; t < model.numTopics; t++){
				Pair p = pairs[t];
				if (p.getIndex() == bgTopic)
					continue;
				else{
					counter[p.getIndex()] += 1;
					tmpCounter += 1;
					
					if (tmpCounter == topTopics)
						break;
				}
			}
		}
		
		// Print number of papers for each topic
		int totalPapers = 0;
		System.out.println("Topic \t Papers");
		System.out.println("==========");
		for (int i = 0; i < counter.length; i++){
			System.out.println( i  + "\t" + counter[i]);
			totalPapers += counter[i];
		}
		System.out.println("[INFO] Total papers are: " + totalPapers);
		
		// Print average topic probabilities for each topic
		Pair[] topicProbPair = Pair.buildPairs(topicProbabilities);
		Arrays.sort(topicProbPair);
		System.out.println("Topic \t Probability");
		System.out.println("==========");
		for (int i = 0; i < topicProbPair.length; i++) {
			Pair pair = topicProbPair[i];
			System.out.println(pair.getIndex() + "\t" + pair.getValue() / instances.size());
		}
	}
	
}


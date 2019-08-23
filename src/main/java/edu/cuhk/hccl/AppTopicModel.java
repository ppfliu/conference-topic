package edu.cuhk.hccl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import edu.cuhk.hccl.topic.TopicModel;

public class AppTopicModel {

	public static void main(String[] args) {

		try {
			String dataFile = args[0];
			int numTopics = Integer.parseInt(args[1]);
			int iterations = Integer.parseInt(args[2]);
			int numThreads = Integer.parseInt(args[3]);
			int wordsPerTopic = Integer.parseInt(args[4]);
			
			List<String> lines = FileUtils.readLines(new File(dataFile));
			
			TopicModel model = new TopicModel(numTopics, wordsPerTopic, iterations, numThreads);
			String[] data = new String[lines.size()];
			
			Map<String, List<String>> topicWords = model.train(lines.toArray(data));
			
			// Output a csv file for each topic
			String header = "text,size,probability,topic";
			List<String> csvLines = new ArrayList<String>();
			for (String key : topicWords.keySet()){
				csvLines.clear();
				csvLines.add(header);
				csvLines.addAll(topicWords.get(key));
				FileUtils.writeLines(new File("word-cloud/topic_words" + key + ".csv"), csvLines, false);
			}
			
			// Output all topics into a single csv file
			header = "word,count,probability,topic";
			csvLines.clear();
			csvLines.add(header);
			for (String key : topicWords.keySet()){
				csvLines.addAll(topicWords.get(key));
			}
			FileUtils.writeLines(new File("topic-csv/topic_words_" + numTopics + ".csv"), csvLines, false);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

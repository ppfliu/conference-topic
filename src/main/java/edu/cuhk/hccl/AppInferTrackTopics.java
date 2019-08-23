package edu.cuhk.hccl;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import edu.cuhk.hccl.topic.Pair;
import edu.cuhk.hccl.topic.TopicModel;

public class AppInferTrackTopics {

	public static void main(String[] args) throws Exception {

		File modelFile = new File(args[0]);
		File dataFolder = new File(args[1]);
		int iterations = Integer.parseInt(args[2]);
		int topTopics = Integer.parseInt(args[3]);
		
		// Build instances
		File[] files = dataFolder.listFiles();
		Arrays.sort(files, new Comparator<File>() {
			public int compare(File f1, File f2) {
				String f1Name = FilenameUtils.getBaseName(f1.getName());
				String f2Name = FilenameUtils.getBaseName(f2.getName());
				
				return Integer.valueOf(f1Name).compareTo(Integer.valueOf(f2Name));
			}
		});
		
		int length = files.length;
		String[] data = new String[length];
		for (int i = 0; i < length; i++) {
			String content = FileUtils.readFileToString(files[i]).replaceAll("\n", " ");
			data[i] = content;
		}
		InstanceList instances = TopicModel.createInstances(data);
		
		// Infer topics
		ParallelTopicModel model = ParallelTopicModel.read(modelFile);
		TopicInferencer inferencer = model.getInferencer();
		
		int size = instances.size();
		for (int index = 0; index < size; index++){
			Instance instance = instances.get(index);
			
			double[] distribution = inferencer.getSampledDistribution(instance, iterations, 1, 5);
			Pair[] pairs = Pair.buildPairs(distribution);
			Arrays.sort(pairs);
			
			System.out.println("==================");
			System.out.println("Track: " + files[index].getPath());
			for (int t = 0; t < topTopics; t++){
				Pair p = pairs[t];
				System.out.println( p.getIndex() + "\t" + p.getValue());
			}
		}
	}
}

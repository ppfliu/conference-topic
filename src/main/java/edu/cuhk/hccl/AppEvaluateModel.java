package edu.cuhk.hccl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.topics.MarginalProbEstimator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class AppEvaluateModel {

	private static ParallelTopicModel topicModel;
	
	private static InstanceList createInstances(String[] data) {
		Pipe instancePipe = new SerialPipes (new Pipe[] {
				new CharSequenceLowercase (),
				new CharSequence2TokenSequence (),
				new TokenSequenceRemoveStopwords (),
			    //new TokenSequenceNGrams (new int[]{1,2}),
				new TokenSequence2FeatureSequence(),
			});

		InstanceList instances = new InstanceList (instancePipe);
		instances.addThruPipe(new StringArrayIterator(data));
		return instances;
	}
	
	public static void main(String[] args) {

		try {
			String dataFile = args[0];
			int numTopics = Integer.parseInt(args[1]);
			int iterations = Integer.parseInt(args[2]);
			int numThreads = Integer.parseInt(args[3]);
			int wordsPerTopic = Integer.parseInt(args[4]);
			
			List<String> lines = FileUtils.readLines(new File(dataFile));
			
			String[] data = new String[lines.size()];
			InstanceList dataset = createInstances(lines.toArray(data));
			
			int totalWords = 0;
			
			for (Instance instance : dataset){
				FeatureSequence sequence = (FeatureSequence) instance.getData();
				totalWords += sequence.getLength();
			}
			
			System.out.println("Size of Vocabulary: " + dataset.getAlphabet().size());
			
			topicModel = new ParallelTopicModel(numTopics);
			topicModel.wordsPerTopic = wordsPerTopic;
			
	        topicModel.setNumIterations(iterations);
	        topicModel.setNumThreads(numThreads);
	       
			topicModel.addInstances(dataset);
			topicModel.estimate();
			
			MarginalProbEstimator evaluator = topicModel.getProbEstimator();
			double logLikelihood = evaluator.evaluateLeftToRight(dataset, 10, false, null);
			double perplexity = Math.exp(- logLikelihood / totalWords);
			
			System.out.println("Number of Topics: " + numTopics + "; Iterations: " + iterations);
			System.out.println(String.format("Log-likelihood: %.3f", logLikelihood));
			
			System.out.println("Total words: " + totalWords);
			System.out.println(String.format("Perplexity: %.3f", perplexity));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

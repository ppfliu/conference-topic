package edu.cuhk.hccl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequenceWithBigrams;
// import cc.mallet.pipe.TokenSequenceNGrams;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.topics.TopicalNGrams;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Randoms;


public class AppTopicalNGramsModel {

	public static void main(String[] args) {

		try {
			String dataFile = args[0];
			int numTopics = Integer.parseInt(args[1]);
			int iterations = Integer.parseInt(args[2]);
			int wordsPerTopic = Integer.parseInt(args[3]);
			String outputModelFilename = args[4];
			
			List<String> lines = FileUtils.readLines(new File(dataFile));
			InstanceList instances = createInstances(lines);

			TopicalNGrams ngrams = new TopicalNGrams(numTopics);
			ngrams.estimate(instances, iterations, 1000, 1000, outputModelFilename, new Randoms());
			
			ngrams.printTopWords(wordsPerTopic, true);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static InstanceList createInstances(List<String> strLines) {
		String[] data = new String[strLines.size()];
		strLines.toArray(data);
		Pipe instancePipe = new SerialPipes (new Pipe[] {
				new CharSequenceLowercase (),
				new CharSequence2TokenSequence (),
				new TokenSequenceRemoveStopwords (),
			    // new TokenSequenceNGrams (new int[]{1,2}),
				new TokenSequence2FeatureSequenceWithBigrams(),
			});

		InstanceList instances = new InstanceList (instancePipe);
		instances.addThruPipe(new StringArrayIterator(data));
		return instances;
	}

}

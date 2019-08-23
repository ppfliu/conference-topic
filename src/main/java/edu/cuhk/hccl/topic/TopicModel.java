package edu.cuhk.hccl.topic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
//import cc.mallet.pipe.TokenSequenceNGrams;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import edu.cuhk.hccl.nlp.PreProcessor;

public class TopicModel {

    private ParallelTopicModel topicModel;

	public TopicModel(int numTopics, int wordsPerTopic, int iterations, int numThreads) {

		topicModel = new ParallelTopicModel(numTopics);
		topicModel.wordsPerTopic = wordsPerTopic;

        topicModel.setNumIterations(iterations);
        topicModel.setNumThreads(numThreads);
	}

	public TopicModel(int numTopics, int wordsPerTopic, double alphaSum, double beta,
			int numIterations, int numThreads, int optimizeInterval) {

		topicModel = new ParallelTopicModel(numTopics, alphaSum, beta);
		topicModel.wordsPerTopic = wordsPerTopic;

		topicModel.setNumIterations(numIterations);
		topicModel.setNumThreads(numThreads);

		topicModel.setOptimizeInterval(optimizeInterval);
	}

    public  TopicModel (File topicModelFile){
    	try {
			topicModel = ParallelTopicModel.read(topicModelFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	public static InstanceList createInstances(String[] data) {
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

	public static String[] preprocess(List<String> lines) throws IOException{
		
		List<String> data = new ArrayList<String>();
		
		// Load stop words
		PreProcessor.loadStopWords("dataset/stopwords.txt");
		for (int i = 0; i < lines.size(); i++){
			String line = lines.get(i).replaceAll("\\W", " ");
			List<String> words = PreProcessor.tokenizeText(line, true);
			
			StringBuffer buffer = new StringBuffer();
			for (int j = 0 ; j < words.size() - 1; j++){
				String word = words.get(j);
				buffer.append(word);
				buffer.append(" ");
			}
			
			if (words.size() > 0){
				buffer.append(words.get(words.size() - 1));
				data.add(buffer.toString());
			}
		}
		
		String[] array = new String[data.size()];
		return data.toArray(array);
	}

    /**
     *  Infer topics for the provided sentence.
     *
     *  @param sentence
     *  @param numSamplings The total number of samplings per sentence
     *  @param thinning      The number of iterations between saved samples
     *  @param burnIn        The number of iterations before the first saved sample
     */
    public double[] getSampledDistribution(String sentence, int numSamplings, int thinning, int burnIn){

        TopicInferencer inferencer = topicModel.getInferencer();
        InstanceList instances = createInstances(new String[]{sentence});
        double[] distribution = inferencer.getSampledDistribution(
                instances.get(0), numSamplings, thinning, burnIn);

        return distribution;
    }

    public Map<String, List<String>> train(String[] data) throws IOException{

        InstanceList instances = createInstances(data);
        topicModel.addInstances(instances);
        topicModel.estimate();

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = topicModel.getSortedWords();
        Alphabet dataAlphabet = instances.getDataAlphabet();

        // Get top N words for each topic
        Map<String, List<String>> topicWords = new HashMap<String, List<String>>();

        // Store probability space for top N words
        List<String> probabilites = new ArrayList<String>();

        for (int topic = 0; topic < topicModel.numTopics; topic++) {
        	List<String> csvLines = new ArrayList<String>();

        	Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
        	int totalWordsInTopic = topicSortedWords.get(topic).size();

        	// count the total number of words frequency for each topic
        	int counterSummary = 0;
        	while (iterator.hasNext()) {
                IDSorter idCountPair = iterator.next();
                int counter = (int)idCountPair.getWeight();
                counterSummary += counter;
        	}

            // re-gain the iterator
        	int rank = 0;
        	double probabilitySpace = 0.0d;
            iterator = topicSortedWords.get(topic).iterator();
            while (iterator.hasNext() && rank < topicModel.wordsPerTopic) {
                IDSorter idCountPair = iterator.next();
                int counter = (int)idCountPair.getWeight();
                double probability = counter * 1.0 / counterSummary;
                String line = String.format("%s,%d,%.3f,%d",
                		dataAlphabet.lookupObject(idCountPair.getID()),
                		counter, probability, topic).toString();

                rank++;
                probabilitySpace += probability;

                csvLines.add(line);
            }
            topicWords.put(String.valueOf(topic), csvLines);

            probabilites.add(String.format("[INFO] Accumulated probabilities of top %d words for topic %d (%d words) is %.3f",
            		topicModel.wordsPerTopic, topic, totalWordsInTopic, probabilitySpace));
        }

        String probFileName = String.format("probability-%d-%d.txt", topicModel.numTopics, topicModel.wordsPerTopic);
        FileUtils.writeLines(new File(probFileName), probabilites, false);

        return topicWords;
    }
    
	public Map<String, Double> getTopic(int topic) throws IOException {
		Map<String, Double> topicWords = new HashMap<String, Double>();

		for (int type = 0; type < topicModel.numTypes; type++) {

			int[] topicCounts = topicModel.typeTopicCounts[type];

			double weight = topicModel.beta;

			int index = 0;
			while (index < topicCounts.length && topicCounts[index] > 0) {

				int currentTopic = topicCounts[index] & topicModel.topicMask;

				if (currentTopic == topic) {
					weight += topicCounts[index] >> topicModel.topicBits;
					break;
				}

				index++;
			}
			String word = (String) topicModel.alphabet.lookupObject(type);
			topicWords.put(word, weight);
		}
		return topicWords;
	}

    public ParallelTopicModel getTopicModel() {
        return topicModel;
    }

    public void save (File modelFile){
    	topicModel.write(modelFile);
    }

}

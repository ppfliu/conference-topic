package edu.cuhk.hccl;

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import edu.stanford.nlp.process.Morphology;

public class AppTrackWords {

	private static Set<String> stopWordsSet = new HashSet<String>();
	private static Morphology morphology = new Morphology();
	
	public static void main(String[] args) throws IOException {
		
		System.out.println("\nProcessing begins...");
		
		// Get parameters
		if(args.length < 3)
			System.exit(-1);
		
		String stopFileName = args[0];
		String trackFileName = args[1];
		String outFileName = args[2];
		
		// Load stop words
		loadStopWords(stopFileName);
		
		// Tokenize each track file
		File trackFile = new File(trackFileName);
		List<String> trackWords = FileUtils.readLines(trackFile);
		
		Set<String> keyWords = new HashSet<String>();
		for (String line : trackWords){
			
			String noPunct = line.replaceAll("[\\p{Punct}–]+", " ");
			
			List<String> tokens = tokenizeText(noPunct, true);
			
			for (String token : tokens){
				if (!keyWords.contains(token))
					keyWords.add(token);
			}
		}
		
		// Save key words
		File outFile = new File(outFileName);
		FileUtils.deleteQuietly(outFile);
		for (String word : keyWords){
			FileUtils.writeStringToFile(outFile, word + "\n", true);
		}
		
		System.out.println("[INFO] The key words are saved in " + outFileName);
		
		System.out.println("Processing is finished!");
	}

	private static void loadStopWords(String stopFile) throws IOException {
		File stopWordsFile = new File(stopFile);
		
		List<String> stopWordsList = FileUtils.readLines(stopWordsFile);
		for (String word : stopWordsList){
			stopWordsSet.add(word);
		}
	}
	
	public static List<String> tokenizeText(String text, boolean removeStopwords) {

		List<String> tokens = new ArrayList<String>();

		StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(text));
		tokenizer.lowerCaseMode(true);
		tokenizer.eolIsSignificant(false);

		try {
			while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
				switch (tokenizer.ttype) {
				case StreamTokenizer.TT_WORD:
					String token = morphology.stem(tokenizer.sval);
					if (!stopWordsSet.contains(token))
						tokens.add(token);
					
					break;
				default:
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tokens;
	}
}

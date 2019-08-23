package edu.cuhk.hccl.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import edu.cuhk.hccl.nlp.TextProcessor.Type;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.util.CoreMap;

public class PreProcessor {

	private static StanfordCoreNLP coreNLP = new TextProcessor(
			TextProcessor.getProperties(Type.Tagger)).getCoreNLP();
	
	private static Morphology morphology = new Morphology();
	
	private static Set<String> stopWordsSet = null;
	
	/*
	 * Choose only content words from a document
	 * 
	 * */
	public static String getContentWords(String document) {

		if (stopWordsSet == null){
			stopWordsSet = new HashSet<String>();
			try {
				File stopWordsFile = new File("data/stopwords.txt");
				List<String> stopWordsList = FileUtils.readLines(stopWordsFile);
				for (String word : stopWordsList){
					stopWordsSet.add(word);
				}
			} catch (IOException e) {
				System.out.println("[ERROR] A fatal error happened due to loading stop words!");
			}
		}
		
		Pattern pattern = Pattern.compile("[a-zA-Z]{2,}");
		BufferedReader reader = new BufferedReader(new StringReader(document));

		StringBuilder buffer = new StringBuilder();
		String line;

		try {
			while ((line = reader.readLine()) != null) {

				Matcher m = pattern.matcher(line);
				while (m.find()) {
					int start = m.start(0);
					int end = m.end(0);

					String word = line.substring(start, end).toLowerCase();
					if (!stopWordsSet.contains(word))
						buffer.append(morphology.stem(word) + " ");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return buffer.toString();
	}

	/*
	 * Get the lemmatized content words for a whole document
	 * 
	 * */
	public static String lemmatizeDocument(String document) {
		
		Annotation annotation = new Annotation(document);
        coreNLP.annotate(annotation);

        StringBuilder buffer = new StringBuilder();
        for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
        	String words = PreProcessor.lemmatizeSentence(sentence);
        	if (words !=null && words.length() > 0)
        		buffer.append(words);
        }

		return buffer.toString();
	}
	
	/*
	 * Get the lemmatized content words for an instance of CoreMap
	 * 
	 * */
	public static String lemmatizeSentence(CoreMap sentence) {
		Pattern pattern = Pattern.compile("^\\w+$");

		StringBuilder wordsBuilder = new StringBuilder();
		for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
			String lemma = token.get(LemmaAnnotation.class).trim();
			if (lemma != null) {

				// check if lemma is a word
				Matcher match = pattern.matcher(lemma);
				if (match.find())
					wordsBuilder.append(lemma).append(" ");
			}
		}

		return wordsBuilder.toString();
	}
	
	public static String tokenizeStream(StringReader reader) {

		StringBuilder buffer = new StringBuilder();

		StreamTokenizer tokenizer = new StreamTokenizer(reader);
		tokenizer.lowerCaseMode(true);
		tokenizer.eolIsSignificant(false);
		tokenizer.whitespaceChars('.', '.');

		try {
			while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
				switch (tokenizer.ttype) {
				case StreamTokenizer.TT_WORD:
					buffer.append(tokenizer.sval + " ");
					break;
				case StreamTokenizer.TT_NUMBER:
					buffer.append(tokenizer.nval + " ");
					break;
				case StreamTokenizer.TT_EOL:
					break;
				default:
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return buffer.toString();
	}
	
	public static void loadStopWords(String stopFile) throws IOException {
		File stopWordsFile = new File(stopFile);
		
		List<String> stopWordsList = FileUtils.readLines(stopWordsFile);
		
		if (stopWordsSet == null)
			stopWordsSet = new HashSet<String>();
			
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

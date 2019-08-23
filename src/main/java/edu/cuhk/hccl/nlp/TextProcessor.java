package edu.cuhk.hccl.nlp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class TextProcessor {
	
	public static final Set<String> NN_TAGS = new HashSet<String>(Arrays.asList(new String[] { "NN", "NNPS", "NNS" }));
	public static final Set<String> JJ_TAGS = new HashSet<String>(Arrays.asList(new String[] { "JJ", "JJR", "JJS" }));
	    
	public enum Type{Tagger, Parser, Ssplit};
	
	private StanfordCoreNLP coreNLP = null;
	
	protected TextProcessor(){
		
	}
	
	public TextProcessor(Properties props){
		this.coreNLP = new StanfordCoreNLP(props);
	}
	
	public static Properties getProperties(Type type){
		Properties props = new Properties();
		
		switch(type){
		case Tagger:
			props.put("annotators", "tokenize, ssplit, pos, lemma");
		    props.put("pos.model", "taggers/english-left3words-distsim.tagger");
			break;
		case Parser:
			props.setProperty("annotators", "tokenize, ssplit, parse");
			props.setProperty("parse.model", "parsers/englishPCFG.ser.gz");
			break;
		case Ssplit:
			props.setProperty("annotators", "tokenize, ssplit");
			break;
		default:
			props.put("annotators", "tokenize, ssplit, pos, lemma");
		    props.put("pos.model", "taggers/english-left3words-distsim.tagger");
			break;
		}
		
		return props;
	}

	public StanfordCoreNLP getCoreNLP() {
		return coreNLP;
	}
	
	public String extractTokens(String text, Set<String> tags) {
        Annotation document = new Annotation(text);
        coreNLP.annotate(document);
        
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        StringBuffer tokens = new StringBuffer();
        
        for (CoreMap sentence : sentences) {
            List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
            for (CoreLabel label : labels) {
                if (tags.contains(label.get(PartOfSpeechAnnotation.class))) {
                	tokens.append(label.lemma() + " ");
                }
            }
        }
        
        return tokens.toString();
    }
}

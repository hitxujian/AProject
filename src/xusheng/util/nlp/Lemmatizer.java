package xusheng.util.nlp;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

//Reference: http://nlp.stanford.edu/software/corenlp.shtml
public class Lemmatizer {

	public static StanfordCoreNLP pipeline = null;
	
	public static void initPipeline() {
		if (pipeline != null) return;
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma");
	    pipeline = new StanfordCoreNLP(props);
	}
	
	public static String lemmatize(String str) throws Exception {
		// initialize CoreNLP pipeline
		initPipeline();
		// create an empty Annotation just with the given text
	    Annotation document = new Annotation(str);
	    // run all Annotators on this text
	    pipeline.annotate(document);
	    
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    StringBuffer sb = new StringBuffer();
	    for(CoreMap sentence: sentences) {						//we assume there's only one sentence
	    	// traversing the words in the current sentence
	    	// a CoreLabel is a CoreMap with additional token-specific methods
	    	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	    		// this is the lemma label of the token
		        String lemma = token.get(LemmaAnnotation.class);
		        if (sb.length() > 0) sb.append(' ');
		        sb.append(lemma);
	    	}
	    }
	    return sb.toString();
	}
	
	public static void main(String[] args) throws Exception {
		String str = "did";
		System.out.println(lemmatize(str));
	}
}

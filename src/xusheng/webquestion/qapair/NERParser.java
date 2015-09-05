package xusheng.webquestion.qapair;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NERParser {
	
	private static NERParser instance=null;
	private static AbstractSequenceClassifier<CoreLabel> classifier=null;
	
	public static NERParser getInstance() throws ClassCastException, ClassNotFoundException, IOException {
		if(NERParser.instance == null){ 
			classifier= CRFClassifier.getClassifier("lib/classifiers/english.all.3class.caseless.distsim.crf.ser.gz");
			NERParser.instance=new NERParser(classifier);
		}
		return NERParser.instance;
	}
	
	public NERParser(AbstractSequenceClassifier<CoreLabel> classifier){
		NERParser.classifier=classifier;
    }  
	
	public ArrayList<String> getNamedEntity(String sentence){
		ArrayList<String> entities=new ArrayList<String>();
		List<Triple<String,Integer,Integer>> name2index = classifier.classifyToCharacterOffsets(sentence);
		for(Triple<String, Integer, Integer> name: name2index){
			entities.add(sentence.substring(name.second(), name.third()));
		}
		return entities;
	}
}

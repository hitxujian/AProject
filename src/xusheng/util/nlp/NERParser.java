package xusheng.util.nlp;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;
import fig.basic.LogInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xusheng on 12/22/15.
 * Named Entity Recognizer using stanford nlp
 */

public class NERParser {

    private static NERParser instance = null;
    private static AbstractSequenceClassifier<CoreLabel> classifier = null;

    public NERParser(AbstractSequenceClassifier<CoreLabel> classifier) {
        NERParser.classifier = classifier;
    }

    public static NERParser getInstance() throws ClassCastException,
            ClassNotFoundException, IOException {
        if (instance == null) {
            classifier = CRFClassifier.getClassifier("lib/classifiers/english" +
                    ".all.3class.caseless.distsim.crf.ser.gz");
            instance = new NERParser(classifier);
        }
        return instance;
    }

    public ArrayList<String> getNamedEntity(String sentence){
        ArrayList<String> entities=new ArrayList<>();
        List<Triple<String,Integer,Integer>> name2index = classifier.classifyToCharacterOffsets(sentence);
        for(Triple<String, Integer, Integer> name: name2index){
            entities.add(sentence.substring(name.second(), name.third()));
        }
        return entities;
    }

    public static void main(String[] args) throws Exception{
        NERParser instance = NERParser.getInstance();
        LogInfo.logs(instance.getNamedEntity(
                "who is barack obama and it's already 7:20 a.m.").toString());
    }

}

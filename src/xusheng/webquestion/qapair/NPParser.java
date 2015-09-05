package xusheng.webquestion.qapair;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class NPParser {
	
	private static NPParser instance=null;
	private static LexicalizedParser lp=null;
	private static TokenizerFactory<CoreLabel> tokenizerFactory=null;
	private static TregexPattern NPpattern = null;
	
	public static NPParser getInstance() {
		if(NPParser.instance == null){ 
			lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
			lp.setOptionFlags(new String[]{"-maxLength", "80", "-retainTmpSubcategories"});
			tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
			NPpattern = TregexPattern.compile("@NP !<< @NP");
			NPParser.instance=new NPParser(lp, tokenizerFactory, NPpattern);
		}
		return NPParser.instance;
	}
	
	public NPParser(LexicalizedParser lp, TokenizerFactory<CoreLabel> tokenizerFactory,
			TregexPattern NPpattern){
		NPParser.lp = lp;  
		NPParser.tokenizerFactory = tokenizerFactory;  
		NPParser.NPpattern = NPpattern;
    }  
	
	public ArrayList<String> getParsedNP(String sentence){
		ArrayList<String> NPs=new ArrayList<String>();
		List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
		Tree parse = lp.apply(rawWords);
		TregexMatcher matcher = NPpattern.matcher(parse);
		while (matcher.findNextMatchingNode()) {
			Tree match = matcher.getMatch();
			NPs.add(Sentence.listToString(match.yield()));
		}
		return NPs;
	}
	
	public String[] getTokenizedWords(String sentence){
		List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
		String[] words=new String[rawWords.size()];
		for(int i=0;i<rawWords.size();i++){
			words[i]=rawWords.get(i).toString();
		}
		return words;
	}

}

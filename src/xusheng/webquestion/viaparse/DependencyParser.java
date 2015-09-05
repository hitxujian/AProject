package xusheng.webquestion.viaparse;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.*;
import edu.stanford.nlp.process.*;
import edu.stanford.nlp.trees.*;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;

public class DependencyParser {

	LexicalizedParser lp;
	TokenizerFactory<CoreLabel> tokenizerFactory;
	
	static DependencyParser dpInstance = null;
	
	public static DependencyParser getInstance()
	{
		if (dpInstance == null)
			dpInstance = new DependencyParser();
		return dpInstance;
	}
	
	public DependencyParser()
	{
		lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		lp.setOptionFlags(new String[]{"-maxLength", "500", "-retainTmpSubcategories"});
		tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
	}
	
	public Collection<TypedDependency> stanfordParse(String text) {
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();

		List<CoreLabel> wordList = tokenizerFactory.getTokenizer(new StringReader(text)).tokenize();
		Tree tree = lp.apply(wordList);
		GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
		//Collection tdl = gs.typedDependenciesCCprocessed(true);
		Collection<TypedDependency> tdl = gs.typedDependencies();
		return tdl;
	}
	
	public DepTree parse(String text) {
		return parse(text, null);
	}
	
	public DepTree parse(String text, StringBuffer sb)
	{
		Collection<TypedDependency> tdl = stanfordParse(text);
		if (sb == null)
			System.out.println("#-Parse:\t" + tdl);
		else
			sb.append("#-Parse:\t" + tdl.toString() + "\n");
		
		DepTree depTree = DepTree.converToTree(tdl);
		return depTree;
	}
	
	public static void main(String[] args)
	{
		DependencyParser dp = DependencyParser.getInstance();
		
		String text = "what province is toronto in ( 7 letters )?";
		DepTree depTree = dp.parse(text);
		depTree.printTree();
	}
	
}

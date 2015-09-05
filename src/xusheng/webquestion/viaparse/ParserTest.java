package xusheng.webquestion.viaparse;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import kangqi.util.struct.Question;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParserTest {

	public static void main(String[] args) throws Exception {
		
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		lp.setOptionFlags(new String[]{"-maxLength", "500", "-retainTmpSubcategories"});
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		
//		String text = "what is my timezone in louisiana?";
//		List wordList = tokenizerFactory.getTokenizer(new StringReader(text)).tokenize();
//		Tree tree = lp.apply(wordList);
//		GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
//		//Collection tdl = gs.typedDependenciesCCprocessed(true);
//		Collection<TypedDependency> tdl = gs.typedDependencies();
//		for (TypedDependency td : tdl)
//			System.out.println(td.toString());
		
		ArrayList<Question> qList = Question.readStdQuestion(args[0]);
		for (Question q : qList) {
			String str = q.recaseToken;
			String[] spt = str.split(" ");
			
			List wordList = tokenizerFactory.getTokenizer(new StringReader(str)).tokenize();
			String[] spt2 = new String[wordList.size()];
			for (int i = 0; i < spt2.length; i++)
				spt2[i] = wordList.get(i).toString();
			
			boolean flag = true;
			if (spt.length != spt2.length) flag = false;
			for (int i = 0; i < spt.length; i++)
				if (!spt[i].equals(spt2[i])) {
					flag = false;
					break;
				}
			if (!flag) {
				System.out.println(str);
				System.out.println(Arrays.toString(spt));
				System.out.println(Arrays.toString(spt2));
				System.out.println("----------");
			}
		}
		
	}
	
}

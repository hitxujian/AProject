package xusheng.util.nlp;

/**
 * Created by Xusheng on 12/22/15.
 */

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
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

        String text = "I can almost always tell when movies use fake dinosaurs.";
        DepTree depTree = dp.parse(text);
        depTree.printTree();
    }

}

package xusheng.util.nlp;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

import java.util.List;
import java.util.Properties;

/**
 * Created by Xusheng on 8/1/2016.
 * Usage: Chinese Word Segmentation
 */
public class ChWordSegmentor {

    private static final String basedir = "/home/xusheng/AProject/lib/data";
    private static CRFClassifier<CoreLabel> segmenter = null;

    public static void initialize() throws Exception {
        if (segmenter != null) return;
        Properties props = new Properties();
        props.setProperty("sighanCorporaDict", basedir);
        // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
        // props.setProperty("normTableEncoding", "UTF-8");
        // below is needed because CTBSegDocumentIteratorFactory accesses it
        props.setProperty("serDictionary", basedir + "/dict-chris6.ser.gz");
        props.setProperty("inputEncoding", "UTF-8");
        props.setProperty("sighanPostProcessing", "true");

        segmenter = new CRFClassifier<>(props);
        segmenter.loadClassifierNoExceptions(basedir + "/ctb.gz", props);
    }

    public static List<String> segment(String str) throws Exception {
        return segmenter.segmentString(str);
    }

    public static void segmentFile(String testFile) throws Exception {
        segmenter.classifyAndWriteAnswers(testFile);
    }
}

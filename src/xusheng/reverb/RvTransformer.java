package xusheng.reverb;

/**
 * Created by angrymidiao on 4/24/15;
 *
 * Goal: 1. Lemmatize the predicate
 *       2. and change continuous or participle tense into present/past tense.
 */

import fig.basic.LogInfo;
import kangqi.util.LogHelper;
import kangqi.util.MultiThread;
import kangqi.util.grammar.Lemmatizer;
import kangqi.util.grammar.RichSyntactiParser;
import xusheng.webquestion.qapair.getNounPhrase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class RvTransformer implements Runnable {

	public static HashSet<String> modalSet = null;
	public static boolean verbose = false;

	public static void initModel() throws Exception {
		RichSyntactiParser.initModel(false);		//cased model
		Lemmatizer.initPipeline();
		modalSet = new HashSet<String>(Arrays.asList("can", "could", "will", "would", "may", "might", "must", "shall", "should"));
	}
	
	private void addWord(StringBuffer sb, String wd) {
		if (sb.length() > 0) sb.append(' ');
		sb.append(wd);
	}

	
	private int[] getSynt(String subj, String pred, String obj, 
			ArrayList<String> tokenList, ArrayList<String> posList, ArrayList<String> lemmaList) throws Exception {
		String sentence = subj + " " + pred + " " + obj;
		RichSyntactiParser.syntacticParse(sentence, tokenList, posList, lemmaList);
		int[] idx = getNounPhrase.getIndex(tokenList, pred);
		return idx;
	}
	
	//1. remove "not" right after modal verb, or be/do/have
	//2. remove MD
	//3. remove RB, RBR, RBS
	private String initialFiltering(String subj, String pred, String obj) throws Exception {
		if (pred.length() == 0) return pred;
		ArrayList<String> tokenList = new ArrayList<String>(), posList = new ArrayList<String>(), lemmaList = new ArrayList<String>();
		int[] idx = getSynt(subj, pred, obj, tokenList, posList, lemmaList);
		int predStart = idx[0], predEnd = idx[1];
		boolean[] rmArr = new boolean[tokenList.size()];
		for (int i = 0; i < tokenList.size(); i++) {
			String token = tokenList.get(i);
			if (token.equals("not") && i > 0) {		//remove "not" right after modal verb, or be/do/have
				String pLem = lemmaList.get(i - 1);
				if (pLem.equals("be") || pLem.equals("do") || pLem.equals("have") || modalSet.contains(pLem))
					rmArr[i] = true;
			}
			String tag = posList.get(i);
			if (modalSet.contains(token) || tag.startsWith("RB"))		//remove modal verb and adverbial
				rmArr[i] = true;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = predStart; i < predEnd; i++)
			if (!rmArr[i]) addWord(sb, tokenList.get(i));
		return sb.toString();
	}
	
	//have + VBN
	private String removeParticiple(String subj, String pred, String obj) throws Exception {
		if (pred.length() == 0) return pred;
		ArrayList<String> tokenList = new ArrayList<String>(), posList = new ArrayList<String>(), lemmaList = new ArrayList<String>();
		int[] idx = getSynt(subj, pred, obj, tokenList, posList, lemmaList);
		int predStart = idx[0], predEnd = idx[1];
		boolean flag = false;
		if (lemmaList.get(predStart).equals("have") && predStart + 1 < predEnd) {
			String nextTag = posList.get(predStart + 1);
			if (nextTag.equals("VBN")) 
				flag = true;
		}
		if (flag) {
			StringBuffer sb = new StringBuffer();
			addWord(sb, lemmaList.get(predStart + 1));
			for (int i = predStart + 2; i < predEnd; i++)
				addWord(sb, tokenList.get(i));
			return sb.toString();
		}
		return pred;
	}
	
	//be + VBG
	private String removeContinuous(String subj, String pred, String obj) throws Exception {
		if (pred.length() == 0) return pred;
		ArrayList<String> tokenList = new ArrayList<String>(), posList = new ArrayList<String>(), lemmaList = new ArrayList<String>();
		int[] idx = getSynt(subj, pred, obj, tokenList, posList, lemmaList);
		int predStart = idx[0], predEnd = idx[1];
		boolean flag = false;
		if (lemmaList.get(predStart).equals("be") && predStart + 1 < predEnd) {
			String nextTag = posList.get(predStart + 1);
			if (nextTag.equals("VBG")) 
				flag = true;
		}
		if (flag) {
			StringBuffer sb = new StringBuffer();
			addWord(sb, lemmaList.get(predStart + 1));
			for (int i = predStart + 2; i < predEnd; i++)
				addWord(sb, tokenList.get(i));
			return sb.toString();
		}
		return pred;
	}
	
	//remove part + remove cont + lemmatize
	private String getResult(String subj, String pred, String obj) throws Exception {
		if (pred.length() == 0) return pred;
		ArrayList<String> tokenList = new ArrayList<String>(), posList = new ArrayList<String>(), lemmaList = new ArrayList<String>();
		int[] idx = getSynt(subj, pred, obj, tokenList, posList, lemmaList);
		int predStart = idx[0], predEnd = idx[1];
		
		int realStart = -1;
		StringBuffer sb = new StringBuffer();
		if (predStart + 1 < predEnd && lemmaList.get(predStart).equals("be") 
				&& posList.get(predStart + 1).equals("VBG")) {		//be doing --> do
			realStart = predStart + 1;
		} else if (predStart + 2 < predEnd && lemmaList.get(predStart).equals("have") 
				&& tokenList.get(predStart + 1).equals("been") && posList.get(predStart + 2).equals("VBG")) {					//have been doing --> do
			realStart = predStart + 2;
		} else if (predStart + 1 < predEnd && lemmaList.get(predStart).equals("have") 
				&& posList.get(predStart + 1).equals("VBN")) {		//have done --> do
			realStart = predStart + 1;
		} else {
			realStart = predStart;
		}
		for (int i = realStart; i < predEnd; i++)
			addWord(sb, lemmaList.get(i));
		return sb.toString();
	}
	
	private String processCaseNew(String subj, String pred, String obj) throws Exception {
		String filteredPred = initialFiltering(subj, pred, obj);
		if (verbose) LogInfo.logs("Initial Filtered Pred: %s", filteredPred);
//		String noParticiplePred = removeParticiple(subj, filteredPred, obj);
//		String noContinuousPred = removeContinuous(subj, noParticiplePred, obj);
//		return Lemmatizer.lemmatize(noContinuousPred);
		return getResult(subj, filteredPred, obj);
	}
	
	public void run() {
		while (true) {
			try {
				int idx = getCurrentIndex();
				if (idx == -1) return;
				String task = taskList.get(idx);
				String[] spt = task.split("\t");
				String subj = spt[0], pred = spt[1], obj = spt[2];
				String cacheValue = getCache(pred);
				if (cacheValue == null) {
					//String norPred = getResult(subj, pred, obj);
					String norPred = processCaseNew(subj, pred, obj);
					setCache(pred, norPred);
					writeAnswer(idx, norPred);
				} else {
					writeAnswer(idx, cacheValue);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	//---------------------------------------------------------------------------//
	
	public static HashMap<String, String> cache = null;
	public static ArrayList<String> taskList = null;
	public static String[] retArr = null;
	public static int end = -1, curr = -1;
	
	public static synchronized int getCurrentIndex() {
		if (curr < end) {
			int ret = curr;
			curr++;
			LogHelper.showLine(curr, 1000);
			if (curr % 1000 == 0) LogInfo.logs("Current Idx: %d, Cache Size: %d [%s]", curr, cache.size(), new Date().toString());
			return ret;
		}
		return -1;
	}
	
	private static synchronized void setCache(String pred, String ret) {
		cache.put(pred, ret);
	}
	
	private static synchronized String getCache(String pred) {
		if (cache.containsKey(pred)) return cache.get(pred);
		return null;
	} 
	
	private static synchronized void writeAnswer(int idx, String ret) {
		retArr[idx] = ret;
	} 
	
	public static void multiThreadWorking(String rvFile, String outFile) throws Exception {
		LogInfo.begin_track("Loading Reverb Task ... ");
		String str = "";
		int scan = 0;
		taskList = new ArrayList<String>();
		cache = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(rvFile));
		while ((str = br.readLine()) != null) {
			scan++;
			LogHelper.showLine(scan, 300000);
			String[] spt = str.split("\t");
			taskList.add(spt[0] + "\t" + spt[1] + "\t" + spt[2]);
		}
		br.close();
		LogInfo.logs("Total %d Reverb read.", taskList.size());

		curr = 0; end = taskList.size();
		retArr = new String[end];
		
		int threads = 4;
		RvTransformer workThread = new RvTransformer();
		MultiThread multi = new MultiThread(threads, workThread);
		LogInfo.begin_track("Now %d threads running ... ", threads);
		multi.runMultiThread();
		LogInfo.end_track();
		
		LogInfo.logs("Now Write Result ... ");
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		for (int i = 0; i < retArr.length; i++) {
			if (retArr[i] == null)	bw.write("\n");
			else					bw.write(retArr[i] + "\n");
		}
		bw.close();
		
		LogInfo.logs("Done.");
	}
	
	public static void batchWork(String rvFile, String outFile) throws Exception {
		HashMap<String, String> cache = new HashMap<String, String>();
		//getNounPhrase.verbose = true;
		RvTransformer worker = new RvTransformer();
		verbose = true;
		LogInfo.begin_track("Reading ReVerb ... ");
		String str = "";
		int scan = 0;
		BufferedReader br = new BufferedReader(new FileReader(rvFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		while ((str = br.readLine()) != null) {
			scan++;
			LogHelper.showLine(scan, 100);
			String[] spt = str.split("\t");
			if (!cache.containsKey(spt[1])) {
				LogInfo.begin_track("Showing \"%s %s %s\" ... ", spt[0], spt[1], spt[2]);
				String outputPred = worker.processCaseNew(spt[0], spt[1], spt[2]);
				LogInfo.end_track();
				cache.put(spt[1], outputPred);
			}
			bw.write(cache.get(spt[1]) + "\n");
		}
		bw.close();
		br.close();
		LogInfo.end_track();
	}
	
	public static void main(String[] args) throws Exception {
		initModel();
//		batchWork(args[0], args[1]);
		multiThreadWorking(args[0], args[1]);
	}
}
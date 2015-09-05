package xusheng.reverb;

import kangqi.util.grammar.Lemmatizer;
import kangqi.util.grammar.RichSyntactiParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;


public class RelTranserST {

	private HashSet<String> modalSet = null;

	private static RelTranserST inst = null;

	public static RelTranserST getInstance() throws Exception {
		if (inst == null) inst = new RelTranserST();
		return inst;
	}

	RelTranserST() throws Exception {
		RichSyntactiParser.initModel(false);		//cased model
		Lemmatizer.initPipeline();
		modalSet = new HashSet<>(Arrays.asList("can", "could", "will", "would", "may", "might", "must", "shall", "should"));
	}
	
	private int[] getIndex(ArrayList<String> tokenList, String se) {
		int[] ret = new int[2];
		ret[0] = ret[1] = -1;
		int len = tokenList.size();
		String tmpSe = se.replace(" ", "");
		for (int i = 0; i < len; i++) {
			if (ret[0] != -1) break;
			int ed = i;
			StringBuffer sb = new StringBuffer(tokenList.get(i));
			while (true) {
				String x = sb.toString();
				if (tmpSe.equals(x)) {
					ret[0] = i;
					ret[1] = ed + 1;
					break;
				} else if (tmpSe.startsWith(x)) {
					ed++;
					sb.append(tokenList.get(ed));
				} else {
					break;
				}
			}
		}
		return ret;
	}
	
	private void addWord(StringBuffer sb, String wd) {
		if (sb.length() > 0) sb.append(' ');
		sb.append(wd);
	}

	private int[] getSynt(String subj, String pred, String obj, 
			ArrayList<String> tokenList, ArrayList<String> posList, ArrayList<String> lemmaList) throws Exception {
		String sentence = subj + " " + pred + " " + obj;
		RichSyntactiParser.syntacticParse(sentence, tokenList, posList, lemmaList);
//		System.out.println(tokenList.toString());
//		System.out.println(posList.toString());
//		System.out.println(lemmaList.toString());
		int[] idx = getIndex(tokenList, pred);
		return idx;
	}
	
	//1. remove "not" right after modal verb, or be/do/have
	//2. remove MD
	//3. remove RB, RBR, RBS
	private String initialFiltering(String subj, String pred, String obj) throws Exception {
		if (pred.length() == 0) return pred;
		ArrayList<String> tokenList = new ArrayList<>(), posList = new ArrayList<>(), lemmaList = new ArrayList<>();
		int[] idx = getSynt(subj, pred, obj, tokenList, posList, lemmaList);
		int predStart = idx[0], predEnd = idx[1];
//		System.out.println("Init. Filtering: start = " + predStart + ", end = " + predEnd);
		boolean[] rmArr = new boolean[tokenList.size()];
		for (int i = 0; i < tokenList.size(); i++) {
			if (tokenList.get(i).equals("not") && i > 0) {		//remove "not" right after modal verb, or be/do/have
				String pLem = lemmaList.get(i - 1);
				if (pLem.equals("be") || pLem.equals("do") || pLem.equals("have") || modalSet.contains(pLem))
					rmArr[i] = true;
			}
			String tag = posList.get(i);
			if (tag.equals("MD") || tag.startsWith("RB"))		//remove modal verb and adverbial
				rmArr[i] = true;
		}
//		for (int i = 0; i < tokenList.size(); i++) {
//			if (rmArr[i])	System.out.print("Y, ");
//			else			System.out.print("N, ");
//		}
//		System.out.println();
		StringBuffer sb = new StringBuffer();
		for (int i = predStart; i < predEnd; i++)
			if (rmArr[i] == false) addWord(sb, tokenList.get(i));
//		System.out.println("sb: " + sb.toString());
		return sb.toString();
	}
	
	//remove part + remove cont + lemmatize
	private String getResult(String subj, String pred, String obj) throws Exception {
		if (pred.length() == 0) return pred;
		ArrayList<String> tokenList = new ArrayList<>(), posList = new ArrayList<>(), lemmaList = new ArrayList<>();
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
	
	//Given subj, pred, obj, return the transformed relation
	public String processCaseNew(String subj, String pred, String obj) throws Exception {
		String filteredPred = initialFiltering(subj, pred, obj);
		return getResult(subj, filteredPred, obj);
	}

	
	public static void main(String[] args) throws Exception {
		RelTranserST dt = new RelTranserST();
		System.out.println("Load.");
		Scanner cin = new Scanner(System.in);
		while (cin.hasNext()) {
			String str = cin.nextLine();
			String tranStr = dt.processCaseNew("X", str, "Y");
			System.out.println("\"" + str + "\" --> \"" + tranStr + "\"");
		}
		cin.close();
	}
}

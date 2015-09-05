package xusheng.webquestion.relextract.viaparse;

//Author: Kangqi Luo
//Goal: Given a question, and starting entity position, return the relation pattern
//Some syntactic rules will be applied.

import edu.stanford.nlp.trees.*;
import fig.basic.LogInfo;
import kangqi.util.grammar.Lemmatizer;
import xusheng.webquestion.qapair.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class RelationPatternExtractor {

	public static boolean verbose = true;
	public static DependencyParser parser = null;
	
	public static HashSet<String> verbSet = null;
	public static HashSet<String> prepSet = null;
	
	public static String verbFile = "lib/wordlist/verb.list";
	public static String prepFile = "lib/wordlist/prep.list";
	
	public static void initParser() {
		parser = DependencyParser.getInstance();
		verbSet = loadWords(verbFile);
		prepSet = loadWords(prepFile);
		Lemmatizer.initPipeline();
	}
	
	public static HashSet<String> loadWords(String file) {
		HashSet<String> ret = new HashSet<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str = "";
			while ((str = br.readLine()) != null)
				ret.add(str);
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}
	
	
	//Tracing the path from ROOT word to the specified word
	public static void traceToLeaf(int pos, DepTree tree, StringBuffer sb) {
		sb.append(tree.edge).append("##").append(tree.index);
		if (tree.start == tree.end - 1 && tree.start == pos)
			return;
		for (int i = 0; i < tree.children.size(); i++) {
			DepTree child = tree.children.get(i);
			if (pos >= child.start && pos < child.end) {
				sb.append("##");
				traceToLeaf(pos, child, sb);
			}
		}
	}
	
	public static String getEdge(String[] path, int edgeCnt) {
		if (2 * edgeCnt >= path.length) return "NULL";
		return path[2 * edgeCnt];
	}
	
	public static int getWordIdx(String[] path, int edgeCnt) {
		if (2 * edgeCnt >= path.length) return -1;
		return Integer.parseInt(path[2 * edgeCnt + 1]);
	}
	
	//Judge the usage for some part in the dependency trace
	public static boolean serveAsSubj(String[] path, int edgeCnt) {
		String edge = getEdge(path, edgeCnt);
		return edge.equals("nsubj");
	}
	
	public static boolean serveAsSubjPass(String[] path, int edgeCnt) {
		String edge = getEdge(path, edgeCnt);
		return edge.equals("nsubjpass");
	}
	
	public static boolean serveAsDobj(String[] path, int edgeCnt) {
		String edge = getEdge(path, edgeCnt);
		return edge.equals("dobj");
	}
	
	public static boolean serveAsPobj(String[] path, int edgeCnt) {
		String edge = getEdge(path, edgeCnt);
		return edge.equals("pobj");
	}
	
	//dep is an imprecise dependency relation, we need to tolerant this label
	public static boolean serveAsDep(String[] path, int edgeCnt) {
		String edge = getEdge(path, edgeCnt);
		return edge.equals("dep");
	}
	
	public static boolean serveAsAdvmod(String[] path, int edgeCnt) {
		String edge = getEdge(path, edgeCnt);
		return edge.equals("advmod") || edge.equals("tmod");
	}
	
	static void addWord(ArrayList<String> relWordList, DepTree tree, int wordIdx) {
		relWordList.add(tree.wholeTextSpan.get(wordIdx));
	}
	
	static void addWord(ArrayList<String> relWordList, String wd) {
		relWordList.add(wd);
	}
	
	static boolean canAdd(int idx, int st, int ed) {
		return idx < st || idx >= ed;
	}
	
	static boolean isBe(String word) {
		return word.equals("is") || word.equals("are") || word.equals("am") || word.equals("be") || 
				word.equals("was") || word.equals("were"); 
	}
	
	//Kernal Function!
	//We analyze the two paths, fitting into some different style, and extract relation word
	//Return String: rel pattern \t direction
	public static String analyzeFromPath(String[] path1, String[] path2, DepTree tree, int st, int ed) {
		int direction = -1;			//0: answer occurred in left side; 1: right side
		ArrayList<String> relWordList = new ArrayList<String>();
		int l1 = path1.length / 2, l2 = path2.length / 2;
		int keyPos = l2 - 1;
		String headWord = tree.word;
		for (int i = 0; i < keyPos; i++) {
			String str = path2[2 * i];
			if (str.equals("nsubj") || str.equals("nsubjpass") || str.equals("dobj") 
					|| str.equals("dep") || str.equals("advmod") || str.equals("pobj")) {
				keyPos = i;
				break;
			}
		}
		if (serveAsSubj(path1, 1) && serveAsDobj(path2, keyPos)) {
			if (verbose) LogInfo.logs("Pattern 0: Who played X (in xx) ?");
			for (int i = 0; i < keyPos; i++) 
				if (canAdd(getWordIdx(path2, i), st, ed)) addWord(relWordList, tree, getWordIdx(path2, i));
			direction = 0;
		} else if (serveAsSubj(path1, 1) && serveAsPobj(path2, keyPos)) {
			if (verbose) LogInfo.logs("Pattern 1: Who played (xx) in X ?");
			for (int i = 0; i < keyPos; i++)
				if (canAdd(getWordIdx(path2, i), st, ed)) addWord(relWordList, tree, getWordIdx(path2, i));
			direction = 0;
		} else if (serveAsDobj(path1, 1) && serveAsSubj(path2, keyPos)) {
			//if (verbose) LogInfo.logs("Pattern 2: Who did X play (null | in | in xx) ?");
			for (int i = 0; i < keyPos; i++)
				if (canAdd(getWordIdx(path2, i), st, ed)) addWord(relWordList, tree, getWordIdx(path2, i));
			//TODO: Add prt, or add the prep having no pobj
			int addPrep = -1;			//-1: no action ; 0: won't add prep ; 1: prep added
			for (DepTree child : tree.children) {
				if (addPrep >= 0) break;			//We've added the extra prep.
//				if (verbose) LogInfo.logs("--> %s : %s", child.edge, child.word);
				if (child.edge.equals("prt")) {
					addWord(relWordList, tree, child.index);
					addPrep = 1;
					if (verbose) LogInfo.logs("Pattern 2-1: Who did X play in ?");			//prt
				} else if (child.edge.equals("prep")) {
					boolean hasPobj = false;
					for (DepTree subChild : child.children) {
						if (subChild.edge.equals("pobj")) {
							hasPobj = true;
							break;
						}
					}
					if (!hasPobj) {
						addWord(relWordList, tree, child.index);
						addPrep = 1;
						if (verbose) LogInfo.logs("Pattern 2-1: Who did X play in ?");		//prep, without pobj
					} else {
						addPrep = 0;
						if (verbose) LogInfo.logs("Pattern 2-2: Who did X play in xx ?");	//prep, with pobj
					}
				}
			}
			if (addPrep == -1)			//no prt, no prep
				if (verbose) LogInfo.logs("Pattern 2-0: Who did X play ?");
			direction = 1;
		} else if ((serveAsAdvmod(path1, 1) || serveAsSubj(path1, 1) || 
				(serveAsDep(path1, 1) && !isBe(headWord)))			//dep + "be" + nsubj --> what is the X of Y.
				&& serveAsSubj(path2, keyPos)) {
			//Situation 1: where did X come from ?   where is X from ?   where did X go to school ?
			//Situation 2: what city is X from ? (2 nusbj arcs in the sentence)
			for (int i = 0; i < keyPos; i++)
				if (canAdd(getWordIdx(path2, i), st, ed)) addWord(relWordList, tree, getWordIdx(path2, i));
			boolean addExtra = false;		//whether added prep/pobj structure in rel words, or not
			for (DepTree child : tree.children) {
				if (addExtra == true) break;
//				if (child.edge.equals("prep") || child.edge.equals("pobj")) {
//					if (child.edge.equals("prep"))
//						if (verbose) LogInfo.logs("Pattern 3-1: When did X play in xx ?");
//					else if (child.edge.equals("pobj"))
//						if (verbose) LogInfo.logs("Pattern 3-2: When did X play xx ?");
//					ArrayList<String> argWords = child.getTextSpan();
//					for (String wd : argWords) addWord(relWordList, wd);
//					addExtra = true;
//				}
				if (child.edge.equals("prep") || child.edge.equals("prt")) {
					addExtra = true;
					addWord(relWordList, tree, child.index);
					if (verbose) LogInfo.logs("Pattern 3-1: When did X go to school ? | Where did X come from ? | What city is X from ?");
				}
			}
			if (!addExtra) {
				if (verbose) LogInfo.logs("Pattern 3-0: When did X play ?");
				addWord(relWordList, "in");				//add the prep "in", in order to match advmod
			}
			direction = 1;
		} else if (serveAsSubjPass(path1, 1) && serveAsPobj(path2, keyPos)) {
			if (verbose) LogInfo.logs("Pattern 4: Who was played by X ?");
			addWord(relWordList, "is");				//added the passive word into rel word list
			for (int i = 0; i < keyPos; i++)
				if (canAdd(getWordIdx(path2, i), st, ed)) addWord(relWordList, tree, getWordIdx(path2, i));
			direction = 0;
		} else if ((serveAsAdvmod(path1, 1) || serveAsDep(path1, 1)) && serveAsSubjPass(path2, keyPos)) {
			if (verbose) LogInfo.logs("Pattern 5: When was X played ?");
			addWord(relWordList, "is");
			for (int i = 0; i < keyPos; i++)
				if (canAdd(getWordIdx(path2, i), st, ed)) addWord(relWordList, tree, getWordIdx(path2, i));
			boolean addExtra = false;
			for (DepTree child : tree.children) {
				if (addExtra) break;
				if (prepSet.contains(child.word)) {
					addExtra = true;
					addWord(relWordList, child.word);
				}
			}
			if (!addExtra) addWord(relWordList, "in");			//We manually add the prep in order to match the statement form
			direction = 1;
		} else if (l2 >= 4 && getEdge(path2, 1).equals("nsubj") && 
				getEdge(path2, 2).equals("prep") && getEdge(path2, 3).equals("pobj")) {
			if (verbose) LogInfo.logs("Pattern 6: What is the capital of X ?");
			addWord(relWordList, "is");
			for (int i = 1; i < l2 - 1; i++)
				if (canAdd(getWordIdx(path2, i), st, ed)) addWord(relWordList, tree, getWordIdx(path2, i));
			direction = 0;
		} else if (l2 >= 3 && getEdge(path2, 1).equals("nsubj") && getEdge(path2, 2).equals("poss")) {
			if (verbose) LogInfo.logs("Pattern 7: What is X 's capital ?");
			addWord(relWordList, "is");
			for (int i = 1; i < l2 - 1; i++)
				if (canAdd(getWordIdx(path2, i), st, ed)) addWord(relWordList, tree, getWordIdx(path2, i));
			addWord(relWordList, "of");				//turn "X's capital" to "capital of X"
			direction = 0;
		} else {
			//Unknown pattern, extract all word except [st, ed) and stopwords as rel
			//Use simple rules to judge the direction.
			//if found "do/did/does", dir = 1, otherwise dir = 0
			if (verbose) LogInfo.logs("Unexpected Pattern.");
			boolean hasDo = false;
			boolean hasBe = false;
			int pos = -1;
			//if the sentence has do/be, the word must lie in the top 5 words, and before the entity.
			int lmt = Math.min(st, Math.min(5, tree.wholeTextSpan.size()));
			for (int i = 0; i < lmt; i++) {
				String wd = tree.wholeTextSpan.get(i);
				if (wd.equals("do") || wd.equals("does") || wd.equals("did")) {
					hasDo = true;
					pos = i;
					break;
				} else if (wd.equals("is") || wd.equals("were") || wd.equals("was")
						|| wd.equals("am") || wd.equals("be") || wd.equals("'s")) {
					hasBe = true;
					pos = i;
					break;
				}
			}
			if (hasDo)	{
				if (verbose) LogInfo.logs("Here goes to simple rule --> 2,3");
				direction = 1;	//Patten 2,3: what did xx play ?
			} else if (hasBe) {
				if (prepSet.contains(tree.wholeTextSpan.get(st - 1))) {
					if (verbose) LogInfo.logs("Here goes to simple rule --> 6-0");
					direction = 0;			//Pattern 6: what is the capital [of] X ?
				} else {
					String lemmaWord = "";
					if (pos + 1 < tree.wholeTextSpan.size()) {
						try { lemmaWord = Lemmatizer.lemmatize(tree.wholeTextSpan.get(pos + 1)); }
						catch (Exception ex) {lemmaWord = tree.wholeTextSpan.get(pos + 1); }
					}
					if (verbSet.contains(lemmaWord)) {
						if (verbose) LogInfo.logs("Here goes to simple rule --> 4");
						direction = 0;			//Pattern 4: who was [played] by X ?
					} else {
						String lemmaWord2 = "";
						if (ed < tree.wholeTextSpan.size()) {
							try { lemmaWord2 = Lemmatizer.lemmatize(tree.wholeTextSpan.get(ed)); }
							catch (Exception ex) {lemmaWord2 = tree.wholeTextSpan.get(ed); }
						}
						if (verbSet.contains(lemmaWord2) || prepSet.contains(lemmaWord2)) {
							if (verbose) LogInfo.logs("Here goes to simple rule --> 5 or 3");
							direction = 1;		//Pattern 5: where was X [played] / [from] ?
						} else {
							String whWord = tree.wholeTextSpan.get(0).toLowerCase();
							if (whWord.equals("where") || whWord.equals("when")) {
								if (verbose) LogInfo.logs("Here goes to simple rule --> Where/When ...... X ... ?");
								direction = 1;
							}
							else {
								if (verbose) LogInfo.logs("Here goes to simple rule --> 6-1 (Or Unexpected)");
								direction = 0;		//pattern 6: what is X 's capital ?
							}
						}
					}
				}
			}
			else {
				String whWord = tree.wholeTextSpan.get(0).toLowerCase();
				if (whWord.equals("where") || whWord.equals("when")) {
					if (verbose) LogInfo.logs("Here goes to simple rule --> Where/When ...... X ... ?");
					direction = 1;
				}
				else {
					if (verbose) LogInfo.logs("Here goes to simple rule --> 0,1 (Or Unexpected)");
					direction = 0;		//Pattern 0,1: who played xxx ?
				}	
			}
			int start = 1;
//			if (pos != -1) start = pos;		//omit the type word like "what county"
			for (int i = start; i < tree.wholeTextSpan.size(); i++) {
				if (i >= st && i < ed) continue;
				String wd = tree.wholeTextSpan.get(i);
				if (wd.equals("do") || wd.equals("did") || wd.equals("does") || wd.equals("?")
						|| wd.equals("the") || wd.equals("'s")) continue;
				addWord(relWordList, wd);
			}
		}
		if (direction == -1)	if (verbose) LogInfo.logs("Direction Not Defined!");
		if (direction == 0)		if (verbose) LogInfo.logs("Rel: <?, %s, X>", relWordList.toString());
		if (direction == 1)		if (verbose) LogInfo.logs("Rel: <X, %s, ?>", relWordList.toString());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < relWordList.size(); i++) {
			if (sb.length() > 0) sb.append(" ");
			sb.append(relWordList.get(i));
		}
		sb.append("\t").append(direction);
		return sb.toString();
	}
	
	
	//Given a question, and a list of possible noun phrases, return a list of relation patterns
	//relatioin pattern = relation words \t direction
	public static ArrayList<String> parse(String q, ArrayList<NounPhrase> npList) {
		Collection<TypedDependency> tdl = parser.stanfordParse(q);
		DepTree tree = DepTree.converToTree(tdl);
		if (verbose) LogInfo.logs(tdl.toString());

		ArrayList<String> ret = new ArrayList<String>();
		for (int i = 0; i < npList.size(); i++) {
			NounPhrase np = npList.get(i);
			if (verbose) LogInfo.begin_track("NP: %s  Position: [%d, %d)", np.nounphrase, np.start, np.end);
			int st = np.start, ed = np.end;
			int pos = ed - 1;
			StringBuffer sb = new StringBuffer(), sb2 = new StringBuffer();
			
			traceToLeaf(0, tree, sb);
			traceToLeaf(pos, tree, sb2);
			
			if (verbose) LogInfo.logs("path 1: %s", sb.toString());
			if (verbose) LogInfo.logs("path 2: %s", sb2.toString());
			
			String[] spt1 = sb.toString().split("##");
			String[] spt2 = sb2.toString().split("##");
			
			String relPattern = analyzeFromPath(spt1, spt2, tree, st, ed);
			ret.add(relPattern);
			if (verbose) LogInfo.end_track();
			
		}
		return ret;
	}
	
	//Only consider one np
	public static ArrayList<String> parse(String q, NounPhrase np) {
		Collection<TypedDependency> tdl = parser.stanfordParse(q);
		DepTree tree = DepTree.converToTree(tdl);
		if (verbose) LogInfo.logs(tdl.toString());
		ArrayList<String> ret = new ArrayList<String>();
		if (verbose) LogInfo.begin_track("NP: %s  Position: [%d, %d)", np.nounphrase, np.start, np.end);
		int st = np.start, ed = np.end;
		int pos = ed - 1;
		StringBuffer sb = new StringBuffer(), sb2 = new StringBuffer();
		
		traceToLeaf(0, tree, sb);
		traceToLeaf(pos, tree, sb2);
		
		if (verbose) LogInfo.logs("path 1: %s", sb.toString());
		if (verbose) LogInfo.logs("path 2: %s", sb2.toString());
		
		String[] spt1 = sb.toString().split("##");
		String[] spt2 = sb2.toString().split("##");
		
		String relPattern = analyzeFromPath(spt1, spt2, tree, st, ed);
		ret.add(relPattern);
		if (verbose) LogInfo.end_track();
		return ret;
	}
	
	public static void main(String[] args) throws Exception {
		initParser();
		BufferedReader br = new BufferedReader(new FileReader("test.txt"));
		String str = "";
		while ((str = br.readLine()) != null) {
			if (str.startsWith("#")) continue;
			String[] spt = str.split("\t");
			String question = spt[0];
			int st = Integer.parseInt(spt[1]), ed = Integer.parseInt(spt[2]);
			if (verbose) LogInfo.begin_track("Question: %s, Position:[%d, %d) ... ", question, st, ed);
			NounPhrase np = new NounPhrase(0, "", st, ed);
			ArrayList<NounPhrase> npList = new ArrayList<NounPhrase>();
			npList.add(np);
			parse(question, npList);
			if (verbose) LogInfo.end_track();
		}
		br.close();
	}
}

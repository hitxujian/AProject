package xusheng.webquestion.viasyntactic;

//Author: Kangqi Luo
//Goal: Use lexical based method to extract relation pattern

import fig.basic.LogInfo;
import kangqi.util.PairII;
import kangqi.util.grammar.RichSyntactiParser;
import kangqi.util.struct.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class LexicalPatternExtractor {

	public static boolean verbose = false;
	
	public static HashSet<String> commonPrepSet = null;
	
	public static void initSyntModel() throws Exception {
		RichSyntactiParser.initModel();
		String[] commonPrepList = new String[] {"as", "at", "but", "by", "for", "in", "of", "off", "on", "to", "up", "via"};
		commonPrepSet = new HashSet<String>(Arrays.asList(commonPrepList));
	}
	
	public int start = 0;					//indicate the position that "wh/what X" ends
	public boolean serveAsSubj = false;	//whether wh part can be treated as a subj / obj
	public boolean serveAsAdv = false;		//whether wh part can be treated as an adverbial modifier
	public boolean passive = false;		//whether the voice of the question is passive or not.
	
	public boolean hasBe = false, hasDo = false;
	public boolean hasActionAfterBe = false;	//whether verb exists after be
	
	public boolean hasPreAction = false;		//found action verb BEFORE np
	public boolean hasPostAction = false;		//found action verb AFTER np
	
	public boolean hasOfNp = false;			//xxx of NP
	public boolean hasNpPoss = false;		//NP 's xxx
	public boolean hasByNp = false;			//xxxed by NP
	
	
	
	//Quesion = [WH] + [Aux] + [PrePhrase] + [NP] + [PostPhrase]
	public Question q = null;
	public ArrayList<String> tokenList = null, lemmaList = null, posList = null;
	public PairII headTypePos = null, npPos = null;
	public int len = 0;
	
	public String whWord = "";
	public boolean hasType = false;

	public int auxSearchStart = -1, auxIdx = -1;		//the search position of aux, and the real position of aux
	public int auxWordType = -1;		//0: no aux; 1: be 2: do 3: have  (have been is simplified)
	
	public int mainVerbSearchStart = -1;
	public int mainVerbIdx = -1;
	public String mainVerb = "", lemmaMainVerb = "", tagMainVerb = "";
	
	public PairII prePhrasePos = new PairII(-1, -1), prePhraseActionPos = new PairII(-1, -1);		//the position of pre-phrase,  the position of action in pre-phrase
	public String prePhraseLemmaAction = "";							//the lemma of pre-phrase action
	public int prePhraseActionTense = -1;			//0: no tense  1: present/past  2: continuous  3: participle
	public String prepBeforeNP = "", prepAfterPreAction = "";			//the prep. directly before NP, and the prep. directly after pre-phrase action
	public int prepBeforeNpPos = -1, prepAfterPreActionPos = -1;		//the position of these preps
	
	public LexicalPatternExtractor(Question curQ, ArrayList<String> tokList, 
			ArrayList<String> pList, ArrayList<String> lemList, PairII tPos, PairII nPos) {
		q = curQ;
		tokenList = tokList;
		posList = pList;
		lemmaList = lemList;
		len = tokenList.size();
		headTypePos = new PairII(tPos.v1, tPos.v2);
		npPos = new PairII(nPos.v1, nPos.v2);
		if (headTypePos.v1 != -1) hasType = true;
	}
	
	private boolean overlap(PairII span, int idx) {
		return idx >= span.v1 && idx < span.v2;
	}
	
	private boolean contained(PairII smallSpan, PairII largeSpan) {
		return smallSpan.v1 >= largeSpan.v1 && smallSpan.v2 <= largeSpan.v2;
	}
	
	//---------------------------------------------------------------//
	//Basic Information Extraction
	
	//extract wh- information
	private void extractWhInfo() {
		whWord = tokenList.get(0);
		auxSearchStart = 1;
		if (hasType) auxSearchStart = headTypePos.v2;				//aux word starts from the next word of type word
	}
	

	//be , do , has  ("has been" is already simplified)
	//Get aux word type, determine the start pos of pre phrase
	//Search starts from wh word , or type word (if have), ends from the start of npPos, since the aux word won't stay after the NP
	private void extractAuxWord() {
		if (verbose) LogInfo.logs("Aux Word Search Starts: %d", auxSearchStart);
		for (int i = auxSearchStart; i < npPos.v1; i++) {
			String lemmaWd = lemmaList.get(i);
			if (lemmaWd.equals("be"))			auxWordType = 1;
			else if (lemmaWd.equals("do"))		auxWordType = 2;
			else if (lemmaWd.equals("have"))	auxWordType = 3;
			if (auxWordType > 0) {
				auxIdx = i;
				mainVerbSearchStart = i + 1;
				break;
			}
		}
		if (auxWordType <= 0) {
			auxWordType = 0;
			auxIdx = -1;
			mainVerbSearchStart = auxSearchStart;
		}
		
		if (verbose) {
			String[] msk = new String[] {"NONE", "BE", "DO", "HAVE"};
			if (auxIdx != -1)	LogInfo.logs("Aux Word Position: %d --> [%s]", auxIdx, msk[auxWordType]);
			else				LogInfo.logs("Aux Word Not Found.");
		}
	}
	
	//Search for the main verb
	private void searchMainVerb() {
		if (verbose) LogInfo.logs("Main Verb Search Starts: %d", mainVerbSearchStart);
		for (int i = mainVerbSearchStart; i < len; i++) {
			if (overlap(npPos, i)) continue;			//skip the words in NP
			String tok = tokenList.get(i);
			String tag = posList.get(i);
			if (tag.startsWith("VB")) {					//tag: VB
				mainVerbIdx = i; break;
			} else if (tok.endsWith("ed")) {
				String nextTag = posList.get(i + 1);
				if (!nextTag.startsWith("NN")) {		//xxxed, and the next word is not NN
					mainVerbIdx = i; break;
				}
			}
		}
		if (mainVerbIdx != -1) {
			mainVerb = tokenList.get(mainVerbIdx);
			lemmaMainVerb = lemmaList.get(mainVerbIdx);
			tagMainVerb = posList.get(mainVerbIdx);
		}
		if (verbose) {
			if (mainVerbIdx >= 0)	LogInfo.logs("Main Verb Position: %d --> [%s]", mainVerbIdx, tokenList.get(mainVerbIdx));
			else					LogInfo.logs("Main Verb Not Found.");
		}
	}
	
	//------------------------------------------------------------//
	//Work For Extracting Information Before & After Main Verb
	
	PairII npBeforeVerbPos = new PairII(-1, -1);
	String npBeforeVerb = "";
	
	//show the position of NP when compared with main verb
	int npDirForVerb = -1;					//0: NP on left side;  1: NP on right side
	
	//use arraylist to show all the possible <[IN] [NP]> info
	ArrayList<PairII> npAfterVerbPosList = new ArrayList<PairII>();		//np word po		non-exist = [-1, -1)
	ArrayList<String> npAfterVerbList = new ArrayList<String>();			//np word			non-exist = ""
	ArrayList<String> prepAfterVerbList = new ArrayList<String>();		//prep word			non-exist = ""
	ArrayList<PairII> prepAfterVerbPosList = new ArrayList<PairII>();		//prep position		non-exist = [-1, -1)
	int whichOneIsNP = -1;											//which one is the marked NP
	
	//Set whether NP is in the left side or right side of the verb
	private void setNpDirectionWithVerb() {
		if (npPos.v1 > mainVerbIdx)			npDirForVerb = 1;
		else if (npPos.v2 <= mainVerbIdx)	npDirForVerb = 0;
		
		if (verbose) {
			if (npDirForVerb == 0) LogInfo.logs("NP is in the LEFT side of verb.");
			else if (npDirForVerb == 1) LogInfo.logs("NP is in the RIGHT side of verb.");
			else	LogInfo.logs("ERROR: Conflict between NP Position and Main Verb Position!");
		}
	}
	
	//If at least NN or VBG or PRP contained in the range, return true
	private boolean isValidNP(int start, int end) {
		for (int i = start; i < end; i++) {
			String tag = posList.get(i);
			if (tag.startsWith("NN") || tag.equals("VBG") || tag.equals("PRP")) return true;
		}
		return false;
	}
	
	//Find the NP before verb
	private void verbLeftHandSideAnalysis() {
		int searchEnd = mainVerbIdx, searchStart = mainVerbSearchStart;		//set the range before verb
		if (npPos.v2 <= mainVerbIdx) {	//NP before main verb, so the relation direction should be <NP, rel, ?>
			npBeforeVerbPos = new PairII(npPos.v1, npPos.v2);
		} else if (isValidNP(searchStart, searchEnd)) {			//Check whether NP exists in the search range
			npBeforeVerbPos = new PairII(searchStart, searchEnd);
		}
		if (npBeforeVerbPos.v1 != -1) {
			StringBuffer sb = new StringBuffer();
			for (int i = npBeforeVerbPos.v1; i < npBeforeVerbPos.v2; i++) {
				if (sb.length() > 0) sb.append(' ');
				sb.append(tokenList.get(i));
			}
			npBeforeVerb = sb.toString();
		}
		if (verbose) {
			if (npBeforeVerbPos.v1 != -1) {
				LogInfo.logs("NP-Before-Verb: [%d, %d) --> %s", npBeforeVerbPos.v1, npBeforeVerbPos.v2, npBeforeVerb);
				if (npPos.v1 > mainVerbIdx)				//have found NP, but not what we want, this is embarrassed.
					LogInfo.logs("Warning: Found NP is not the NP we want.");
			}
			else
				LogInfo.logs("NP-Before-Verb Not Found.");
		}
	}
	
	//Find the <[IN] [NP]> after verb
	//Find prop first, then use prop to split
	//Assume: if there are more than one NP after the verb, they MUST BE SPLIT by preps
	//We can skip the prep within the marked NP
	private void verbRightHandSideAnalysis() {
		int searchStart = mainVerbIdx + 1;		//search from start
		int searchEnd = len - 1;				//of course we'll omit the last question mark
		//Find all prep position
		ArrayList<PairII> tmpPrepPosList = new ArrayList<PairII>();
		for (int i = searchStart; i < searchEnd; i++) {
			if (overlap(npPos, i)) continue;		//omit the prep in the marked NP
			String tag = posList.get(i);
			String tok = tokenList.get(i);
			//normal preposition along with "to"
			//(though I don't know whether "to" will harm the result or not, but we can try first)
			if (!tok.equals("to") && (tag.equals("IN") || commonPrepSet.contains(tok)))		//a common prep.
				tmpPrepPosList.add(new PairII(i, i + 1));
			else if (tok.equals("to")) {
				String nextTag = posList.get(i + 1);
				//found infinitive (to see, to accomplish) and doesn't conflict with NP, treat a uniform prep word
				if (nextTag.startsWith("VB") && !overlap(npPos, i + 1))
					tmpPrepPosList.add(new PairII(i, i + 2));
				else
					tmpPrepPosList.add(new PairII(i, i + 1));
			}
		}
		tmpPrepPosList.add(new PairII(searchEnd, searchEnd));			//add searchEnd position, in order to make further partition better
		if (verbose) LogInfo.logs("RHS Prep List: %s", tmpPrepPosList.toString());
		
		//If we found x preps, it can split the range into x + 1 parts
		//NP | IN NP | IN NP | IN NP | $
		//The first part must have NP, otherwise, remove the first part
		int prepSize = tmpPrepPosList.size();
		if (searchStart < tmpPrepPosList.get(0).v1) {		//the first part has something
			PairII span = new PairII(searchStart, tmpPrepPosList.get(0).v1);
			boolean flag = false;
			if (contained(npPos, span)) {
				npAfterVerbPosList.add(npPos);			//if the span covers the NP, just use NP itself
				whichOneIsNP = npAfterVerbPosList.size() - 1;
				flag = true;
			}
			else if (isValidNP(span.v1, span.v2)) {
				npAfterVerbPosList.add(span);					//directly put a valid span
				flag = true;
			}
			if (flag) prepAfterVerbPosList.add(new PairII(-1, -1));
		}
		for (int i = 0; i < prepSize - 1; i++) {		//middle part
			PairII span = new PairII(tmpPrepPosList.get(i).v2, tmpPrepPosList.get(i + 1).v1);		//the beginning prep is not contained
			prepAfterVerbPosList.add(tmpPrepPosList.get(i));
			if (span.v2 - span.v1 == 0)		//No NP
				npAfterVerbPosList.add(new PairII(-1, -1));
			else if (contained(npPos, span)) {
				npAfterVerbPosList.add(npPos);
				whichOneIsNP = npAfterVerbPosList.size() - 1;
			}
			else if (isValidNP(span.v1, span.v2))
				npAfterVerbPosList.add(span);					//directly put a valid span
			else
				npAfterVerbPosList.add(new PairII(-1, -1));		//Find a invalid span
		}
		fillPrepNpInfo();			//fill the other two string lists
		
		if (verbose) {
			LogInfo.logs("WhichOneIsNP = %d", whichOneIsNP);
			for (int i = 0; i < npAfterVerbPosList.size(); i++) {
				String np = npAfterVerbList.get(i);
				String prep = prepAfterVerbList.get(i);
				PairII tmpPos = npAfterVerbPosList.get(i);
				LogInfo.logs("RHS #%d: [%s] [%s] [%d,%d)%s", i, prep, np, tmpPos.v1, tmpPos.v2, whichOneIsNP == i ? " <-- FOUND NP" : "");
			}
		}
	}
	
	//Fill the list of npAfterVerbList, prepAfterVerbIdxList
	private void fillPrepNpInfo() {
		for (int i = 0; i < npAfterVerbPosList.size(); i++) {
			PairII tmpPos = npAfterVerbPosList.get(i);
			PairII prepPos = prepAfterVerbPosList.get(i);
			//fill prep name
			if (prepPos.v1 == -1)	prepAfterVerbList.add("");
			else {
				StringBuffer sb = new StringBuffer();
				for (int j = prepPos.v1; j < prepPos.v2; j++) {
					if (sb.length() > 0) sb.append(' ');
					sb.append(tokenList.get(j));
				}
				prepAfterVerbList.add(sb.toString());
			}
			//fill np name
			if (tmpPos.v1 == -1)	npAfterVerbList.add("");
			else {
				StringBuffer sb = new StringBuffer();
				for (int j = tmpPos.v1; j < tmpPos.v2; j++) {
					if (sb.length() > 0) sb.append(' ');
					sb.append(tokenList.get(j));
				}
				npAfterVerbList.add(sb.toString());
			}
		}
	}
	
	//--------------------------------------------------------------------------//
	//Work For Extracting Relations Given LHS and RHS Information
	
	//Used when need to get an extra prep.
	private String getExtraPrep() {
		return "in";
	}
	
	private void outputRelation(ArrayList<String> possibleRelList, String coarseMethod, String fineMethod) {
		LogInfo.begin_track("Showing Relation From [%s] (%s) ... ", coarseMethod, fineMethod);
		for (String rel : possibleRelList) {
			String[] spt = rel.split("\t");
			int direction = Integer.parseInt(spt[1]);
			if (direction == 0) LogInfo.logs("<?, %s, X>", spt[0]);
			if (direction == 1) LogInfo.logs("<X, %s, ?>", spt[0]);
		}
		LogInfo.end_track();
	}
	
	//When forming relation, change continuous tense & participle tense into normal tense
	//so the tense is restricted into present/past tense, along with their passive voice form.
	private String tenseReduce(int auxWordType, String tagMainVerb) {
		if (auxWordType == 1 && tagMainVerb.equals("VBG"))		//is playing --> play
			return lemmaMainVerb;
		if (auxWordType == 3 && !tagMainVerb.equals("VBG"))		//has played --> play (usually the verb should be VBN, but here we allow any verb type except VBG)
			return lemmaMainVerb;
		if (auxWordType == 1) return "is " + mainVerb;			//output as usual
		if (auxWordType == 3) return "have " + mainVerb;
		return mainVerb;
	}
	
	//IMPORTANT FUNCTION: Generate Relation when NP is in the left hand side of verb
	private ArrayList<String> generateRelationWithVerb_LeftMode() {
		String coarseMethod = "", fineMethod = "";
		ArrayList<String> possibleRelList = new ArrayList<String>();
		int direction = 1;											//<X, rel, ?>
		coarseMethod = "NP in the left of Verb";
		int sz = prepAfterVerbPosList.size();
		int missingNpPos = -1;
		for (int i = 0; i < sz; i++) {
			if (npAfterVerbList.get(i).equals("")) {		//Found NP Missing
				missingNpPos = i;
				break;
			}
		}
		String tmpRel = "";
		if (missingNpPos == -1) {
			String topPrep = "";							//check the top preposition
			if (sz > 0) topPrep = prepAfterVerbList.get(0);
			//Rule 1: answer serves as obj, and there has room for a direct obj without extra preps. so the form is just "X play ?", ? is the direct obj.
			if ((whWord.equals("what") || whWord.equals("who") || whWord.equals("which")) && (sz == 0 || !topPrep.equals(""))) {
				//X play --> X play ?
				//X play [IN NP] [IN NP] --> X play ?
				fineMethod = "No Missing NP, No Need Extra";
				tmpRel = tenseReduce(auxWordType, tagMainVerb);
				possibleRelList.add(tmpRel + "\t" + direction);	
			} 
			//Rule 2: either answer serves as adv, or there doesn't provide a room for direct obj, then we need an extra prep.
			else {
				//X play (Y) [IN NP] [IN NP] --> X play (Y) [IN NP] [IN NP] prep_extra ?
				fineMethod = "No Missing NP, Need Extra";
				String extraPrep = getExtraPrep();
				StringBuffer sb = new StringBuffer();
				sb.append(tenseReduce(auxWordType, tagMainVerb));
				possibleRelList.add(sb.toString() + " " + extraPrep + "\t" + direction);
				for (int j = 0; j < sz; j++) {					//increase [IN NP] one by one
					String npName = npAfterVerbList.get(j);
					String prepName = prepAfterVerbList.get(j);
					if (!prepName.equals(""))							//append prep. if have
						sb.append(" ").append(prepName);
					sb.append(" ").append(npName);						//append np name
					possibleRelList.add(sb.toString() + " " + extraPrep + "\t" + direction);
				}
			}
		} else if (missingNpPos >= 0) {	//we found missing NP, occurring at arbitrary position, so: X play [IN NP] [IN NP] with Y
			fineMethod = "Found Missing NP";
			String prep = prepAfterVerbList.get(missingNpPos);
			StringBuffer sb = new StringBuffer();
			sb.append(tenseReduce(auxWordType, tagMainVerb));
			possibleRelList.add(sb.toString() + " " + prep + "\t" + direction);
			for (int j = 0; j < missingNpPos; j++) {				//increase [IN NP] one by one
				String npName = npAfterVerbList.get(j);
				String prepName = prepAfterVerbList.get(j);
				if (!prepName.equals(""))							//append prep. if have
					sb.append(" ").append(prepName);
				sb.append(" ").append(npName);						//append np name
				possibleRelList.add(sb.toString() + " " + prep + "\t" + direction);
			}
		}

		if (verbose) outputRelation(possibleRelList, coarseMethod, fineMethod);
		
		return possibleRelList;
	}
	
	//IMPORTANT FUNCTION: Generate Relation when NP is in the right hand side of verb
	private ArrayList<String> generateRelationWithVerb_RightMode() {
		String coarseMethod = "", fineMethod = "";
		ArrayList<String> possibleRelList = new ArrayList<String>();
		int direction = -1;
		coarseMethod = "NP in the right of Verb";
		
		//Rule 1: No NP found before verb, so ? serves as subj, just directly put
		if (npBeforeVerb.equals("")) {
			direction = 0;									//<?, rel, X>
			fineMethod = "No NP Found before Verb";
			StringBuffer sb = new StringBuffer();
			sb.append(tenseReduce(auxWordType, tagMainVerb));
			for (int j = 0; j <= whichOneIsNP; j++) {		//add all the info before NP
				String npName = npAfterVerbList.get(j);
				String prepName = prepAfterVerbList.get(j);
				if (!prepName.equals(""))							//append prep. if have
					sb.append(" ").append(prepName);
				if (j < whichOneIsNP)
					sb.append(" ").append(npName);					//append np name, except NP itself
				possibleRelList.add(sb.toString() + "\t" + direction);
			}
		}
		//Rule 2: Found NP (subj) before verb, currently we convert the predicate into passive tense in order to solve the case 
		else {
			direction = 0;												//<?, rel, X>
			fineMethod = "NP Found before Verb, Simple Handle by Passive Transformation";
			String tmpRel = "is " + lemmaMainVerb;						//passive form
			String prepName = prepAfterVerbList.get(whichOneIsNP);		//get the prep for the marked NP
			if (!prepName.equals("")) tmpRel += (" " + prepName);
			possibleRelList.add(tmpRel + "\t" + direction);
		}
		
		if (verbose) outputRelation(possibleRelList, coarseMethod, fineMethod);
		
		return possibleRelList;
	}
	
	//---------------------------------------------------------------------------//
	//Work For No-Verb Information Collection
	
	String attrName = "";				//lemmatized attr
	PairII attrPos = new PairII(-1, -1);
	String followPrep = "";			//the prep after NP
	int followPrepIdx = -1;
	
	//Merge string in [a, b)
	private String mergeString(ArrayList<String> source, PairII interval) {
		StringBuffer sb = new StringBuffer();
		for (int i = interval.v1; i < interval.v2; i++) {
			if (sb.length() > 0) sb.append(' ');
			sb.append(source.get(i));
		}
		return sb.toString();
	}
	
	//Find [the|a] xxx [IN] NP
	private void extractAttrBeforeNP() {
		int searchStart = mainVerbSearchStart, searchEnd = npPos.v1;
		if (auxWordType == 0 || auxWordType == 1) {		//we accept "be" or no aux
			int attrStart = searchStart, attrEnd = searchEnd - 1;
			String firstTok = tokenList.get(searchStart);
			if (firstTok.equals("the") || firstTok.equals("a"))			//the|a
				attrStart++;
			String lastTok = tokenList.get(attrEnd);					//a prep at the last position
			String lastTag = posList.get(attrEnd);
			if ((commonPrepSet.contains(lastTok) || lastTag.equals("IN")) && attrEnd > attrStart && isValidNP(attrStart, attrEnd))	{		//attr not empty
				attrPos = new PairII(attrStart, attrEnd);
				attrName = mergeString(lemmaList, attrPos);
			}
		}
		if (verbose) {
			if (attrPos.v1 != -1)	LogInfo.logs("Attr Before NP: %s --> %s", attrPos.toString(), attrName);
			else					LogInfo.logs("Attr Before NP Not Found.");
		}
	}
	
	//Find NP 's xxx
	private void extractAttrAfterNP() {
		int searchStart = npPos.v2, searchEnd = len - 1;
		String tok = tokenList.get(searchStart);
		if (tok.equals("'s")) {
			attrPos = new PairII(searchStart + 1, searchEnd);
			attrName = mergeString(lemmaList, attrPos);
		}
		if (verbose) {
			if (attrPos.v1 != -1)	LogInfo.logs("Attr After NP: %s --> %s", attrPos.toString(), attrName);
			else					LogInfo.logs("Attr After NP Not Found.");
		}
	}
	
	private void getFollowPrep() {
		for (int i = npPos.v2; i < len; i++) {
			String tok = tokenList.get(i);
			String tag = posList.get(i);
			if (commonPrepSet.contains(tok) || tag.equals("IN")) {
				followPrep = tok;
				followPrepIdx = i;
				break;
			}
		}
		if (verbose) {
			if (followPrepIdx != -1)	LogInfo.logs("Prep Follow NP: %d --> %s", followPrepIdx, followPrep);
			else						LogInfo.logs("Prep Follow NP Not Found.");
		}
	}
	
	//since many cases omit 's, we use this function to extract attr after NP.
	//Constraint: 
	//1. auxWord must be "is"
	//2. no attr extracted
	//3. no head type found
	//4. no follow prep found
	private void guessAttrAfterNP() {
		if (auxWordType != 1) return;
		if (!attrName.equals("")) return;
		if (headTypePos.v1 != -1) return;
		if (!followPrep.equals("")) return;
		int searchStart = npPos.v2, searchEnd = len - 1;
		if (searchEnd > searchStart && isValidNP(searchStart, searchEnd)) {
			attrPos = new PairII(searchStart, searchEnd);
			attrName = mergeString(lemmaList, attrPos);
		}
		if (verbose) {
			if (attrPos.v1 != -1)	LogInfo.logs("Attr Guessing After NP: %s --> %s", attrPos.toString(), attrName);
		}
	}
	
	private ArrayList<String> generateRelationWithAttr() {
		String coarseMethod = "Find Relation By Attr", fineMethod = "";
		ArrayList<String> possibleRelList = new ArrayList<String>();
		int direction = -1;
		if (!attrName.equals("") && (auxWordType == 0 || auxWordType == 1)) {
			direction = 0;				//<?, attr, X>
			String rel = "is the " + attrName + " of" + "\t" + direction;
			possibleRelList.add(rel);
			fineMethod = "Find Attr";
		} else if (!followPrep.equals("") && auxWordType == 1) {
			direction = 1;				//<X, is in, ?>
			String rel = "is " + followPrep + "\t" + direction;
			possibleRelList.add(rel);
			fineMethod = "Find Prep";
		}
		
		if (verbose)
			outputRelation(possibleRelList, coarseMethod, fineMethod);
		return possibleRelList;
	}
	
	
	public String updateTypeInfo(PairII outTypePos) {
		if (attrPos.v1 != -1) {
			outTypePos.v1 = attrPos.v1;
			outTypePos.v2 = attrPos.v2;
			return attrName;
		}
		return "";
	}
	
	//Determine:
	//1. whether has action in pre-phrase
	//2. the tense of action: present(past), continuous, past participle
	//3. Having VB + Prep or not
	private void prePhraseAnalysis(ArrayList<String> tokenList, ArrayList<String> posList, ArrayList<String> lemmaList) {
		int start = prePhrasePos.v1, end = prePhrasePos.v2;
		int actionStart = start;
		if (verbose) LogInfo.logs("Pre-Phrase Range: [%d, %d)", start, end);
		while (actionStart < end) {				//Skip beginning adverbs
			if (lemmaList.get(actionStart).startsWith("RB")) actionStart++;
			else break;
		}
		if (actionStart < end) {				//we have enough room to find a candidate action word 
			String tagAction = posList.get(actionStart);
			String tokAction = tokenList.get(actionStart);
			if (verbose) LogInfo.logs("Candidate Pre-Action: \"%s\" [%s]", tokAction, tagAction);
			if (tagAction.equals("VBG")) prePhraseActionTense = 2;		//continuous tense (we can tolerant the missing of "be")
			else if (tagAction.equals("VBZ") || tagAction.equals("VBD") 
					|| tagAction.equals("VBP") || (tokAction.endsWith("ed") && auxWordType == 0))
				prePhraseActionTense = 1;								//present / past, we rely on VBZ/VBP/VBD, and tolerant a JJ word ends with "ed" if no aux word found.
			else if ((auxWordType == 1 || auxWordType == 3) && (tagAction.equals("VBN") || tokAction.endsWith("ed")))
				prePhraseActionTense = 3;								//is xxed, has xxed
			else
				prePhraseActionTense = 0;
			
			if (prePhraseActionTense > 0) {
				prePhraseLemmaAction = lemmaList.get(actionStart);
				prePhraseActionPos = new PairII(actionStart, actionStart + 1);
			}
		}
		
		if (verbose) {
			String[] msk = new String[] {"None", "Present/Past", "Continuous", "Participle"};
			if (prePhraseActionTense > 0)
				LogInfo.logs("Pre-Action: [%d, %d) --> %s, Tense: %s", 
						prePhraseActionPos.v1, prePhraseActionPos.v2, prePhraseLemmaAction, msk[prePhraseActionTense]);
			else
				LogInfo.logs("Pre-Action Not Found.");
		}
	}
	
	public ArrayList<String> lexicalAnalysisNew() throws Exception {
		ArrayList<String> ret = new ArrayList<String>();
		if (verbose) LogInfo.begin_track("Lexical Analysis New ... ");
		extractWhInfo();
		if (whWord.startsWith("wh")) {
			extractAuxWord();
			searchMainVerb();
			if (mainVerbIdx != -1) {
				setNpDirectionWithVerb();
				verbLeftHandSideAnalysis();
				verbRightHandSideAnalysis();
				if (npDirForVerb == 0)	ret.addAll(generateRelationWithVerb_LeftMode());
				if (npDirForVerb == 1)	ret.addAll(generateRelationWithVerb_RightMode());
			} else {
				extractAttrBeforeNP();
				if (attrName.equals("")) extractAttrAfterNP();
				getFollowPrep();
				guessAttrAfterNP();
				if (auxWordType == 0 || auxWordType == 1) ret.addAll(generateRelationWithAttr());
			}
		}
		if (verbose) LogInfo.logs("Return %d Relations", ret.size());
		if (verbose) LogInfo.end_track();
		return ret;
	}
	
	
	public void lexicalAnalysis(String content, ArrayList<String> tokenList, 
			ArrayList<String> posList, ArrayList<String> lemmaList, PairII npPos, PairII typePos) throws Exception {
		start = 1;
		if (typePos.v1 != -1)
			start = typePos.v2;
		String whWord = tokenList.get(0);
		if (whWord.equals("who")) serveAsSubj = true;
		if (whWord.equals("where") || whWord.equals("when")) serveAsAdv = true;
		if (whWord.equals("what") || whWord.equals("which")) {
			serveAsSubj = true;
			if (start > 1) serveAsAdv = true;
			//what|which X, here we have a type word, if the type word is time/location related,
			//then people will treat the component as when/where, so that it can be an adverbial modifier
		}
		
		//Now check the word / tag at the start position
		//common speaking, there are 3 possibilities: [be] / [do] / an action
		String lemmaSt = lemmaList.get(start);
		String tagSt = posList.get(start);
		if (lemmaSt.equals("be")) hasBe = true;
		else if (lemmaSt.equals("do")) hasDo = true;
		else if (tagSt.startsWith("VB")) hasPreAction = true;

		String tagBeforeNP = posList.get(npPos.v1 - 1);
		String tagAfterNP = posList.get(npPos.v2);
		String lemmaBeforeNP = lemmaList.get(npPos.v1 - 1);
		String lemmaAfterNP = lemmaList.get(npPos.v2);

		if (tagBeforeNP.startsWith("VB") && 
				!lemmaBeforeNP.equals("do") && !lemmaBeforeNP.equals("be")) hasPreAction = true;
		if (tagAfterNP.startsWith("VB")) hasPostAction = true;
		
		if (lemmaBeforeNP.equals("of")) hasOfNp = true;
		else if (lemmaBeforeNP.equals("by") && npPos.v1 - 2 >= 0 && posList.get(npPos.v1 - 2).equals("VBD"))
			hasByNp = true;
		if (lemmaAfterNP.equals("'s")) hasNpPoss = true;
	}
	
	private String tf(String show, boolean x) {
		if (x == true) return "[" + show + "]";
		return show; 
	}
	
	public void outputIndicator() {
		LogInfo.begin_track("Indicators: ");
		LogInfo.logs("%s | %s", tf("Serve As Subj", serveAsSubj), tf("Serve As Adv", serveAsAdv));
		LogInfo.logs("%s | %s", tf("Has Be", hasBe), tf("Has Do", hasDo));
		LogInfo.logs("%s | %s", tf("Has Pre-Action", hasPreAction), tf("Has Post-Action", hasPostAction));
		LogInfo.logs("%s | %s | %s", tf("Has of NP", hasOfNp), tf("Has NP 's", hasNpPoss), tf("Has VBD by NP", hasByNp));
		LogInfo.end_track();
	}
}

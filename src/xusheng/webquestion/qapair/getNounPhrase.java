package xusheng.webquestion.qapair;

import fig.basic.LogInfo;
import kangqi.util.grammar.Tokenizer;
import kangqi.util.struct.Question;

import java.util.ArrayList;
import java.util.Calendar;

public class getNounPhrase {

	public static boolean verbose = false;

	public static void main(String[] args) throws Exception {
		Calendar c1=Calendar.getInstance();
		NERParser sp=NERParser.getInstance();
		NPParser sp2=NPParser.getInstance();
		ArrayList<Question> qList = Question.readStdQuestion("/home/kangqi/Webquestions/Standard/questions.test.detail.v1");

		for (Question q : qList) {
			LogInfo.logs(q.content);
			parseQuestionFile(sp, sp2, q);
		}


//		Scanner cin = new Scanner(System.in);
//		int cnt = 100;
//		while (cnt-- > 0) {
//			int x = Integer.parseInt(cin.nextLine());
//			parseQuestionFile(sp, sp2, qList.get(x - 3778));
//		}
//		parseQuestionFile(sp, sp2, Question.readStdQuestion
//				("/home/kangqi/Webquestions/Standard/questions.test.detail.v0").get(0));
//		int[] index=getIndex(sp2, "who plays ken barlow in coronation street", "who plays");
//		System.out.println(""+index[0]+"\t"+index[1]);
//		Calendar c2=Calendar.getInstance();
//		System.out.println("Time cost: "+(c2.getTimeInMillis()-c1.getTimeInMillis())/1000+"s");
	}

	/**
	 *
	 * @author zy
	 * @function parse the Question to NounPhrase Struct
	 * @param sp--a NERParser Instance
	 * @param sp2--a NPParser Instance
	 * @param q--a Question Struct Instance
	 * @return the list of NounPhrase Struct Instance
	 * @throws Exception
	 */
	public static ArrayList<NounPhrase> parseQuestionFile(NERParser sp, NPParser sp2, Question q) throws Exception{
		ArrayList<String> startEntityCandidates=new ArrayList<String>();

		startEntityCandidates=sp.getNamedEntity(q.content);
//		LogInfo.logs("Named Entity: %s", startEntityCandidates.toString());
//		
//		ArrayList<String> startEntityCandidates2=sp2.getParsedNP(q.content);
//		LogInfo.logs("Parsed NP Entity: %s", startEntityCandidates2.toString());

		if(startEntityCandidates.size()==0){
			startEntityCandidates=sp2.getParsedNP(q.content);
		}

		ArrayList<NounPhrase> nps=new ArrayList<NounPhrase>();
		ArrayList<String> tokenList = Tokenizer.tokenize(q.content);
		for(String se:startEntityCandidates){
//			int[] index=getIndex(sp2, q.content, se);
			int[] index = getIndex(tokenList, se);
			nps.add(new NounPhrase(q.idx, se, index[0], index[1]));
		}
		return nps;

	}

	/**
	 *
	 * @author zy
	 * @function get the start and end index of the se in q 
	 * @param sp2--NPParser Instance(use as a tokenizer)
	 * @param q--question content
	 * @param se--starting entity
	 * @return
	 */
	public static int[] getIndex(NPParser sp2, String q, String se) throws Exception {
		if (verbose) LogInfo.begin_track("Find Start & End for \"%s\" in \"%s\" ... ", se, q);
		int[] index=new int[2];
		int qindex=0;
		int eindex=0;
		boolean flag=false;
		String[] qwords=sp2.getTokenizedWords(q);
		String[] ewords=sp2.getTokenizedWords(se);

//		ArrayList<String> qTokList = Tokenizer.tokenize(q);
//		ArrayList<String> seTokList = Tokenizer.tokenize(se);
//		String[] qwords = new String[qTokList.size()];	qTokList.toArray(qwords);
//		String[] ewords = new String[seTokList.size()];	seTokList.toArray(ewords);

//		if (verbose) LogInfo.logs("Question: %s", qTokList.toString());
//		if (verbose) LogInfo.logs("Cand. NP: %s", seTokList.toString());

		for(; qindex<qwords.length; qindex++){
			if(qwords[qindex].equals(ewords[eindex])){
				flag=true;
			}
			if(flag && !qwords[qindex].equals(ewords[eindex++])){
				eindex=0;
				flag=false;
			}
			if(eindex==ewords.length){
				index[1]=qindex+1;
				index[0]=index[1]-ewords.length;
				break;
			}
		}
		if (verbose) LogInfo.end_track();
		return index;
	}

	public static int[] getIndex(ArrayList<String> tokenList, String se) {
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
		if (verbose) {
			LogInfo.begin_track("Get NP Pos ... ");
			LogInfo.logs("se: [%s], Token: %s", se, tokenList.toString());
			LogInfo.logs("Finding Pos for [%s] --> [%d, %d)", se, ret[0], ret[1]);
			LogInfo.end_track();
		}
		return ret;
	}
}

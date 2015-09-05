package xusheng.reverb;

import kangqi.util.grammar.Lemmatizer;
import kangqi.util.struct.MapIntHelper;

import java.io.*;
import java.util.*;

public class RvMatcher {
	
	public static int numOfCandidates = 10000;
	public static ArrayList<String> lines = new ArrayList<>();
	public static ArrayList<String> raw = new ArrayList<>();
	
	public static void main(String[] args) throws Exception
	{
		String[] ex_s = args[0].split(" ");
		//lem();
		preprocess();
		readRawReVerb();
		
		ArrayList<String> ex_l = new ArrayList<>(Arrays.asList(ex_s));
		ArrayList<Integer> ex_r = getRVp(ex_l);
		for (int i=0;i<ex_r.size();i++)
			System.out.printf(raw.get(ex_r.get(i)-1)+"\n");
		System.out.printf("Total: "+ex_r.size()+"\n");
	}
	
	public RvMatcher() throws IOException
	{
		preprocess();
	}
	
	public static void lem() throws Exception
	{
		List<String> wordList = new ArrayList<>();
		FileWriter fileWriter = new FileWriter("/home/freefish_6174/Lab/QA/relation_link/TypeInference/src/xusheng/lemmatized-reverb.txt");
		String rvFile = "/home/freefish_6174/Lab/QA/relation_link/linked-extractions.tsv";
		BufferedReader br=new BufferedReader(new FileReader(rvFile));
		String line = br.readLine();
		while (line!=null)
		{
			String[] words = line.split("\t");
			wordList = Arrays.asList(words);
			String tmp = Lemmatizer.lemmatize(wordList.get(1));
			System.out.println(tmp+"\n");
			fileWriter.write(tmp+"\n");
			line = br.readLine();
		}
		br.close();
		fileWriter.close();
		System.out.print("Xusheng: Reverb predicate lemmatize done\n");
	}
	
	public static void preprocess() throws IOException
	{
		String localFp = "C:/Users/xushengl/Documents/Eclipse/TypeInference/src/xusheng/lemmatized-reverb.txt";
		String workFp = "/home/kangqi/TypeInference/resources/ReVerb/lemmatized-reverb.txt";
		File localFile = new File(localFp);
		File workFile = new File(workFp);
		BufferedReader br = null;
		if (localFile.exists())
			br = new BufferedReader(new FileReader(localFile));
		else
			br = new BufferedReader(new FileReader(workFile));
		String line = br.readLine();
		while (line!=null)
		{
			lines.add(line);
			line = br.readLine();
		}
		br.close();
		System.out.print("Xusheng: Lemmatized Reverb Data read into memory done\n");
	}
	
	public static void readRawReVerb() throws Exception {
		String workFp = "/home/kangqi/TypeInference/resources/ReVerb/linked-extractions.tsv";
		File workFile = new File(workFp);
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(workFile));
		String line = br.readLine();
		while (line!=null)
		{
			raw.add(line);
			line = br.readLine();
		}
		br.close();
		System.out.print("Xusheng: Raw Reverb Data read into memory done\n");
	}
	
	public static void insert(int score,ArrayList<Integer> scores)
	{
		int tmp;
		scores.add(score);
		int i = scores.size()-2;
		while (i>=0 && scores.get(i) < score)
		{
			tmp = scores.get(i);
			scores.set(i+1,tmp);
			i--;
		}
		scores.set(i+1,score);
	}
	
	public static void adjust(int score,int index,ArrayList<Integer> scores,HashMap<Integer,ArrayList<Integer>> candidate)
	{
		int lowest = scores.get(scores.size()-1);
		ArrayList<Integer> tmp = candidate.get(lowest);
		//---------------------------------------------
		if (tmp.size()>1)
		{
			tmp.remove(tmp.size()-1);
			candidate.put(lowest,tmp);
		}
		else 
		{
			candidate.remove(lowest);
			scores.remove(scores.size()-1);
		}
		//---------------------------------------------
		if (scores.contains(score))
		{
			tmp = candidate.get(score);
			tmp.add(index);
		}
		else 
		{
			insert(score,scores);
			tmp = new ArrayList<Integer>();
			tmp.add(index);
			candidate.put(score,tmp);
		}
	}
	
	public static String getRVpString(ArrayList<String> relPattern) throws Exception {
		HashMap<String, Integer> scoreMp = new HashMap<>();
		HashMap<String, Integer> sizeMp = new HashMap<>();
		HashMap<String, Boolean> exactMp = new HashMap<>();
		
		String[] irlegal_s = {"the","of","be","do","a","an","to","for", "from"};
		
		int lenOfRel = 0;   // real length of target relation pattern
		int lenOfMatch; // real length of match part of pattern
		int count,score,canum = 0,lnum = 0;
		boolean exactMatch = false;
		List<String> irlegal = new ArrayList<>();
		irlegal = Arrays.asList(irlegal_s);
		ArrayList<Integer> resRvp = new ArrayList<>();
		List<String> relList = new ArrayList<>();
		ArrayList<Integer> scores = new ArrayList<>();
		HashMap<Integer,ArrayList<Integer>> candidate = new HashMap<>();
		
		//-----------------------------------------------------------------------------------------------
		
		for (int i=0;i<relPattern.size();i++)
			if (!irlegal.contains(relPattern.get(i)))
				lenOfRel ++;

		String line; 
		for (int k=0;k<lines.size();k++)
		{
			boolean localExactMatch = false;
			line = lines.get(k);
			lnum ++;
			String[] words = line.split(" ");
			relList = Arrays.asList(words);
	
			for (int i=0;i<relList.size();i++)
				relList.set(i, Lemmatizer.lemmatize(relList.get(i)));
			StringBuffer rvSb = new StringBuffer();
			for (int i = 0; i < relList.size(); i++) {
				if (rvSb.length() > 0) rvSb.append(' ');
				rvSb.append(relList.get(i));
			}

			lenOfMatch = 0;
			count = 0;
			for (int i=0;i<relPattern.size();i++)
			{
				if (relList.contains(relPattern.get(i)))
				{
					count ++;
					if (!irlegal.contains(relPattern.get(i)))
						lenOfMatch ++;
				}		
			}
			
			
			if (count == relPattern.size() && count == relList.size()) {
				exactMatch = true;
				localExactMatch = true;
			}
			
			
			int sub = Math.abs(relList.size()-count);
			
			if (lenOfRel>0)
				if (lenOfMatch>0 && (relPattern.size()-count)<2 && sub <=2)
					score = count - sub;
				else 
					score = 0;
			else 
				if ((relPattern.size()-count)<=2 && sub <= 2 )
					score = count - sub;
				else 
					score = 0;
			//--------------------------------------------------------------------
			
			if (lenOfMatch > 0 && score > 0)
			{
				String str = rvSb.toString();
				if (!scoreMp.containsKey(str)) scoreMp.put(str, score);
				if (!exactMp.containsKey(str)) exactMp.put(str, localExactMatch);
				new MapIntHelper<String>().addToMapInt(sizeMp, rvSb.toString(), 1);
			}
		}
		
		HashMap<String, Integer> filterSizeMp = new HashMap<>();
		for (Map.Entry<String, Boolean> kvp : exactMp.entrySet()) {
			if (kvp.getValue() == true) {
				filterSizeMp.put(kvp.getKey(), sizeMp.get(kvp.getKey()));
			}
		}
		if (filterSizeMp.size() == 0) return "";
		ArrayList<Map.Entry<String,Integer>> srtList = 
				new MapIntHelper<String>().sort(filterSizeMp);
		return srtList.get(0).getKey();
	}
	
	public static ArrayList<Integer> getRVp(ArrayList<String> relPattern) throws Exception
	{
		String[] irlegal_s = {"the","of","be","do","a","an","to","for", "from"};
		
		int lenOfRel = 0;   // real length of target relation pattern
		int lenOfMatch; // real length of match part of pattern
		int count,score,canum = 0,lnum = 0;
		boolean exactMatch = false;
		List<String> irlegal = new ArrayList<>();
		irlegal = Arrays.asList(irlegal_s);
		ArrayList<Integer> resRvp = new ArrayList<>();
		List<String> relList = new ArrayList<>();
		ArrayList<Integer> scores = new ArrayList<>();
		HashMap<Integer,ArrayList<Integer>> candidate = new HashMap<>();
		
		//-----------------------------------------------------------------------------------------------
		
		for (int i=0;i<relPattern.size();i++)
			if (!irlegal.contains(relPattern.get(i)))
				lenOfRel ++;

		String line; 
		for (int k=0;k<lines.size();k++)
		{
			line = lines.get(k);
			lnum ++;
			String[] words = line.split(" ");
			relList = Arrays.asList(words);
	
			for (int i=0;i<relList.size();i++)
			{
				if (relList.get(i).equals("is") || relList.get(i).equals("are"))
					relList.set(i,"be");
				if (relList.get(i).equals("did") || relList.get(i).equals("does"))
					relList.set(i,"do");
			}

			lenOfMatch = 0;
			count = 0;
			for (int i=0;i<relPattern.size();i++)
			{
				if (relList.contains(relPattern.get(i)))
				{
					count ++;
					if (!irlegal.contains(relPattern.get(i)))
						lenOfMatch ++;
				}		
			}
			
			
			if (count == relPattern.size() && count == relList.size())
				exactMatch = true;
			
			
			int sub = Math.abs(relList.size()-count);
			
			if (lenOfRel>0)
				if (lenOfMatch>0 && (relPattern.size()-count)<2 && sub <=2)
					score = count - sub;
				else 
					score = 0;
			else 
				if ((relPattern.size()-count)<=2 && sub <= 2 )
					score = count - sub;
				else 
					score = 0;
			//--------------------------------------------------------------------
			
			if (lenOfMatch > 0 && score > 0)
			{
				//System.out.printf(relList+" "+score+"\n");
				//System.out.printf(scores+"\n");
				//System.out.printf(candidate+"\n");
				if (canum <numOfCandidates)
				{
					if (candidate.containsKey(score))
						candidate.get(score).add(lnum);
					else
					{
						ArrayList<Integer> tmp = new ArrayList<>();
						tmp.add(lnum);
						candidate.put(score,tmp);
						if (scores.size() == 0)
							scores.add(score);
						else 
							insert(score,scores);
					}
					canum ++;
				}
				else
					if (scores.get(scores.size()-1) < score)
						adjust(score,lnum,scores,candidate);
							
			}
		}
		
		//System.out.printf(scores+"\n");
		if (exactMatch) {
			int s = scores.get(0);
			ArrayList<Integer> res = candidate.get(s);
			for (int j=0;j<res.size();j++)
				resRvp.add(res.get(j));
		}
		else {
			for (int i=0;i<scores.size();i++)
			{
				int s = scores.get(i);
				ArrayList<Integer> res = candidate.get(s);
				for (int j=0;j<res.size();j++)
					resRvp.add(res.get(j));
				//System.out.printf(res.size()+"\t"+res+"\n");
			}
		}
		return resRvp;
	}

}


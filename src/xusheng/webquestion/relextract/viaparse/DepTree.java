package xusheng.webquestion.relextract.viaparse;

import edu.stanford.nlp.trees.TypedDependency;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class DepTree {

	public ArrayList<String> wholeTextSpan;		//the text span of the ENTIRE sentence (use start/end to get sub part.)
	public ArrayList<DepTree> children;
	
	public int start, end, len;					//the [start, end), and the length of the text span		
	public int nodes;							//how many children
	
	public DepTree parent;	//null if the current node connects the ROOT
	public String edge;		//the edge from parent to the current node
	public String word;		//the word of the current node
	public int index;		//the index of the current word
	
	public DepTree()
	{
		children = new ArrayList<DepTree>();
		start = 100000; end = index = -1; len = 0; nodes = 0;
		parent = null; edge = word = ""; 
	}
	
	public static DepTree converToTree(Collection<TypedDependency> tdl)
	{
		ArrayList<DepTree> dePool = new ArrayList<DepTree>();
		
		Iterator<TypedDependency> iter = tdl.iterator();
		int maxlen = 0;
		while (iter.hasNext()) {						//get the maxlen of the depTree
			TypedDependency td = iter.next();
			int depIdx = td.dep().index() - 1;
			maxlen = Math.max(maxlen, depIdx);
		}
		String[] textSpan = new String[maxlen + 1]; 
		for (int i = 0; i <= maxlen; i++)
		{
			dePool.add(new DepTree());
			textSpan[i] = "";
		}
		
		int rootIdx = -1;
		iter = tdl.iterator();
		while (iter.hasNext())
		{
			TypedDependency td = iter.next();
			String depWord = td.dep().originalText().trim();
			int headIdx = td.gov().index() - 1;
			int depIdx = td.dep().index() - 1;
			DepTree cur = dePool.get(depIdx);	
			textSpan[depIdx] = depWord;
			
			if (headIdx == -1) { 
				cur.parent = null;
				rootIdx = depIdx; 
			} else { 
				cur.parent = dePool.get(headIdx);
				dePool.get(headIdx).children.add(cur);
				dePool.get(headIdx).nodes++;
			}
			cur.word = depWord; cur.edge = td.reln().toString(); cur.index = depIdx;
		}
		
		dePool.get(rootIdx).updateTreeLen();
		ArrayList<String> spanList = new  ArrayList<String>();
		for (String s : textSpan) spanList.add(s);			//convert string[] to arraylist
		
		for (DepTree cur : dePool)
			cur.wholeTextSpan = spanList;
		
		return dePool.get(rootIdx);
	}
	
	void updateTreeLen()
	{
		start = index; end = index + 1;
		for (DepTree child : children)
		{
			child.updateTreeLen();
			start = Math.min(child.start, start);
			end = Math.max(child.end, end);
		}
		len = end - start;
	}
	
	public ArrayList<String> getTextSpan()
	{
		ArrayList<String> ret = new ArrayList<String>();
		for (int i = start; i < end; i++)
			ret.add(wholeTextSpan.get(i));
		return ret;
	}
	
	public void printTree()
	{
//		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
		printTree(null);
	}
	
	public void printTree(BufferedWriter bw)
	{
		printTree("ROOT", -1, bw);
	}
	
	void printTree(String pWord, int pIdx, BufferedWriter bw)
	{
		try {
			if (bw == null)
				System.out.println(edge + "(" + pWord + "-" + pIdx + ", " + word + "-" + index + ")");
			else {
			bw.write(edge + "(" + pWord + "-" + pIdx + ", " + word + "-" + index + ")");
			bw.newLine();
			}
		} catch (Exception ex) { System.out.println(ex); }
		for (DepTree child : children)
			child.printTree(word, index, bw);
	}
}

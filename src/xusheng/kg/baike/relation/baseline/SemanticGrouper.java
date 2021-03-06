package xusheng.kg.baike.relation.baseline;

import fig.basic.LogInfo;
import xusheng.util.struct.MultiThread;

import java.io.*;
import java.util.*;

/**
 * Created by Xusheng on 7/13/2016.
 * Cluster cleaned-up relations in semantic level
 * by using embedding vectors.
 */

public class SemanticGrouper implements Runnable{

    public static String rootFp = "/home/xusheng/starry/baidubaike";
    public static String relFp = rootFp + "/infobox.text";


    public void run() {
        while (true) {
            try {
                int idx = getCurr();
                if (idx == -1) return;
                work4pas(idx);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static int curr = -1, end = -1;
    public static synchronized int getCurr() {
        if (curr <= end) {
            int ret = curr;
            curr ++;
            return ret;
        }
        return -1;
    }

    public static Map<Integer, StringBuffer> rel2BOW = new HashMap<>();
    public static void multiThreadWork(int st, int ed, int numOfThreads) throws Exception {
        readRelEpMap();
        readPassages(st, ed);
        constructIdx();
        curr = st; end = ed;
        LogInfo.logs("Begin to construct vector rep. of relations...");
        SemanticGrouper workThread = new SemanticGrouper();
        MultiThread multi = new MultiThread(numOfThreads, workThread);

        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.logs("Non-use relations: %d", numOfNull);
        LogInfo.end_track();

        LogInfo.begin_track("Start to generate relation embedding... Size: %d", rel2BOW.size());
        postProcessing();
        LogInfo.end_track();
    }

    public static int numOfNull = 0;
    // each thread works for one set of relations on all passages
    public static void work4rel(int idx) throws Exception {
        int ed = idx * 10000;
        int st = ed - 10000 + 1;
        if (ed > numOfRel) ed = numOfRel;
        LogInfo.logs("Working for relation %d to %d... [%s]", st, ed, new Date().toString());
        //------ construct first word TO subj & subj TO rel+obj index --------
        Map<String, List<String>> ch2str = new HashMap<>(),
                subj2robj = new HashMap<>();
        for (int i=st; i<=ed; i++) {
            if (relTasks[i] == null) {
                numOfNull ++;
                continue;
            }
            List<String> triples4OneRel = relTasks[i];
            for (String triple : triples4OneRel) {
                String[] spt = triple.split("\t");
                String subj = spt[0], robj = String.valueOf(i) + "\t" + spt[1];
                String ch = subj.substring(0,1);
                if (!ch2str.containsKey(ch))
                    ch2str.put(ch, new ArrayList<>());
                ch2str.get(ch).add(subj);
                if (!subj2robj.containsKey(subj))
                    subj2robj.put(subj, new ArrayList<>());
                subj2robj.get(subj).add(robj);
            }
            // release memory
            relTasks[i].clear();
        }
        LogInfo.logs("[%d, %d]: ch-str index done. Size: %d, %d. [%s]",
                st, ed, ch2str.size(), subj2robj.size(), new Date().toString());
        //------- scan one pass of all the passages ---------
        for (int i=1; i<300; i++) {
            StringBuffer passage = passages[i];
            for (int j = 0; j < passage.length(); j++) {
                if (ch2str.containsKey(String.valueOf(passage.charAt(j)))) {
                    List<String> candSubj = ch2str.get(String.valueOf(passage.charAt(j)));
                    for (String subj : candSubj) {
                        if (j + subj.length() <= passage.length() &&
                                passage.substring(j, j + subj.length()).equals(subj)) {
                            //LogInfo.logs(subj + " ||| " + passage.substring(j, j+subj.length()+lenOfwIn));
                            extendVector(passage.substring(j + subj.length(), j + subj.length() + lenOfwIn),
                                    subj2robj.get(subj));
                        }
                    }
                }
            }
            LogInfo.logs("[%d, %d]: %d/300 passages scanned. [%s]", st, ed, i, new Date().toString());
        }
        LogInfo.logs("[%d, %d]: Passage scanning finished. [%s]", st, ed, new Date().toString());
    }

    // each thread works for all relations on one passage
    public static void work4pas(int idx) throws Exception {
        LogInfo.logs("Working for passage %d... [%s]", idx, new Date().toString());
        //------- scan one pass of the passage ---------
        StringBuffer passage = passages[idx];
        int j = 0;
        while (j < passage.length()) {
            if (j % 10000 == 0)
                LogInfo.logs("[%d]: %d/%d scanned. [%s]", idx, j, passage.length(), new Date().toString());
            if (ch2str.containsKey(String.valueOf(passage.charAt(j)))) {
                List<String> candSubj = ch2str.get(String.valueOf(passage.charAt(j)));
                for (String subj : candSubj) {
                    if (j + subj.length() + lenOfwIn <= passage.length() &&
                            passage.substring(j, j + subj.length()).equals(subj)) {
                        //LogInfo.logs(subj + " ||| " + passage.substring(j, j+subj.length()+lenOfwIn));
                        if (extendVector(passage.substring(j + subj.length(), j + subj.length() + lenOfwIn),
                                subj2robj.get(subj))) {
                            j += (subj.length() + lenOfwIn)-1;
                            break;
                        }
                    }
                }
            }
            j++;
        }
        LogInfo.logs("[%d]: Passage scanning finished. [%s]", idx, new Date().toString());
    }

    public static Map<String, List<String>> ch2str = new HashMap<>(),
            subj2robj = new HashMap<>();
    public static void constructIdx() {
        //------ construct first word TO subj & subj TO rel+obj index --------
        int st = 1;
        int ed = numOfRel;
        for (int i=st; i<=ed; i++) {
            if (relTasks[i] == null) {
                numOfNull ++;
                continue;
            }
            List<String> triples4OneRel = relTasks[i];
            for (String triple : triples4OneRel) {
                String[] spt = triple.split("\t");
                String subj = spt[0], robj = String.valueOf(i) + "\t" + spt[1];
                String ch = subj.substring(0,1);
                if (!ch2str.containsKey(ch))
                    ch2str.put(ch, new ArrayList<>());
                ch2str.get(ch).add(subj);
                if (!subj2robj.containsKey(subj))
                    subj2robj.put(subj, new ArrayList<>());
                subj2robj.get(subj).add(robj);
            }
            relTasks[i].clear();
        }
        LogInfo.logs("[log] Ch-str index done. Size: %d, %d. [%s]",
                ch2str.size(), subj2robj.size(), new Date().toString());
    }

    // add wordsInBetween to the raw vector of a specific relation
    public static boolean extendVector(String text, List<String> target) {
        for (String str: target) {
            String[] spt = str.split("\t");
            int idx = Integer.parseInt(spt[0]);
            String obj = spt[1];
            if (text.contains(obj)) {
                int pos = text.indexOf(obj);
                modifyRel2BOW(idx, text.substring(0, pos));
                return true;
            }
        }
        return false;
    }

    public static synchronized void modifyRel2BOW(int idx, String str) {
        //LogInfo.logs("Extend vector for rel. %d", idx);
        if (!rel2BOW.containsKey(idx))
            rel2BOW.put(idx, new StringBuffer());
        rel2BOW.get(idx).append(str);
    }

    // write raw vectors into files, and calculate real embeddings
    public static void postProcessing() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(rootFp + "/raw_vectors.txt"));
        for (Map.Entry<Integer, StringBuffer> entry: rel2BOW.entrySet()) {
            bw.write(entry.getKey().toString() + "\t" + entry.getValue().toString() + "\n");
        }
        bw.close();
        LogInfo.logs("Raw vectors written. Size: %d", rel2BOW.size());
    }

    // -------- pre-processing --------

    public static int numOfRel = 0;
    public static String[] rels = null;
    public static void readRelation() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "edge_dict.tsv.v1"));
        String line;
        rels = new String[numOfRel];
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            rels[Integer.parseInt(spt[0])] = spt[1];
        }
        br.close();
    }

    public static List<String>[] relTasks = null;
    public static void readRelEpMap() throws IOException {
        // todo:
        // infobox.text.v1 should be cleaned up, both entities should contain
        // only chineses words without any marks!!! and relations should be replaced by index.
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "/infobox.text.v1"));
        String line; int cnt = 0;
        relTasks = new List[numOfRel+1];
        while ((line = br.readLine()) != null) {
            cnt ++;
            //LogUpgrader.showLine(cnt, 1000000);
            String[] spt = line.split("\t");
            if (spt[0].equals("年") || spt[0].equals("年月") || spt[0].equals("月日") ||
                    spt[0].equals("年月日") ) continue;
            int idx = Integer.parseInt(spt[1]);
            if (relTasks[idx] == null) relTasks[idx] = new ArrayList<>();
            relTasks[idx].add(spt[0] + "\t" + spt[2]);
        }
        br.close();
        LogInfo.logs("[log] Relation-Entity Pairs Map Loaded.");
    }

    public static StringBuffer[] passages = new StringBuffer[300];
    public static void readPassages(int st, int ed) throws IOException {
        for (int i=st; i<=ed; i++) {
            // todo: check 270.txt non-exsit
            if (i == 270) {
                passages[i] = new StringBuffer();
                continue;
            }
            //LogUpgrader.showLine(i, 50);
            passages[i] = new StringBuffer();
            String fp = rootFp + "/content/" + i + ".txt";
            BufferedReader br = new BufferedReader(new FileReader(fp));
            String line;
            while ((line = br.readLine()) != null)
                passages[i].append(line);
        }
        LogInfo.logs("[log] %d-%d passages loaded.", st, ed);
    }

    public static int lenOfwIn;
    public static void main(String[] args) throws Exception {
        numOfRel = Integer.parseInt(args[0]);
        lenOfwIn = Integer.parseInt(args[1]);
        multiThreadWork(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    }
}

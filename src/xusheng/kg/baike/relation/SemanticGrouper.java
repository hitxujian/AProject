package xusheng.kg.baike.relation;

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
                work(idx);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static int curr = -1, end = -1;
    public static synchronized int getCurr() {
        if (curr < end) {
            int ret = curr;
            curr ++;
            return ret;
        }
        return -1;
    }

    public static Map<Integer, StringBuffer> rel2BOW = new HashMap<>();
    public static void multiThreadWork() throws Exception {
        readRelEpMap();
        curr = 1; end = 200;
        LogInfo.logs("Begin to construct vector rep. of relations...");
        int numOfThreads = 8;
        SemanticGrouper workThread = new SemanticGrouper();
        MultiThread multi = new MultiThread(numOfThreads, workThread);
        LogInfo.begin_track("%d threads are running...", numOfThreads);
        multi.runMultiThread();
        LogInfo.end_track();
        LogInfo.begin_track("Start to generate relation embedding... Size: %d", rel2BOW.size());
        postProcessing();
        LogInfo.end_track();
    }

    public static void work(int idx) throws Exception {
        int ed = idx * 1000;
        int st = ed - 1000 + 1;
        LogInfo.logs("Working for relation %d to %d... [%s]", st, ed, new Date().toString());
        //------ construct first word TO subj & subj TO rel+obj index --------
        Map<String, List<String>> ch2str = new HashMap<>(),
                subj2robj = new HashMap<>();
        for (int i=st; i<=ed; i++) {
            List<String> triples4OneRel = relTasks[i];
            for (String triple : triples4OneRel) {
                String[] spt = triple.split("\t");
                String subj = spt[0], robj = String.valueOf(i) + "\t" + spt[1];
                String ch = subj.substring(0,1);
                if (!ch2str.containsKey(ch)) {
                    ch2str.put(ch, new ArrayList<>());
                    ch2str.get(ch).add(subj);
                }
                if (!subj2robj.containsKey(subj)) {
                    subj2robj.put(subj, new ArrayList<>());
                    subj2robj.get(subj).add(robj);
                }
            }
            // release memory
            relTasks[i].clear();
        }
        //------- scan one pass of all the passages ---------
        for (int i=1; i<300; i++) {
            String fp = rootFp + "/content/" + i + ".txt";
            BufferedReader br = new BufferedReader(new FileReader(fp));
            String line, passage = "";
            while ((line = br.readLine()) != null) {
                passage += line;
            }
            for (int j=0; j<passage.length(); j++) {
                if (ch2str.containsKey(passage.charAt(j))) {
                    List<String> candSubj = ch2str.get(passage.charAt(j));
                    for (String subj: candSubj) {
                        if (j + subj.length() <= passage.length() &&
                                passage.substring(j, j + subj.length()).equals(subj)) {
                            extendVector(passage.substring(j+subj.length(), j+subj.length()+lenOfwIn),
                                    subj2robj.get(subj));
                        }
                    }
                }
            }
        }
        LogInfo.logs("Relation %d to %d finished. %s", st, ed, new Date().toString());
    }

    // add wordsInBetween to the raw vector of a specific relation
    public static void extendVector(String text, List<String> target) {
        for (String str: target) {
            String[] spt = str.split("\t");
            int idx = Integer.parseInt(spt[0]);
            String obj = spt[1];
            if (text.contains(obj)) {
                int pos = text.indexOf(obj);
                if (!rel2BOW.containsKey(idx)) rel2BOW.put(idx, new StringBuffer());
                rel2BOW.get(idx).append(text.substring(0, pos));
            }
        }
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
        BufferedReader br = new BufferedReader(new FileReader(rootFp + "infobox.text.v1"));
        String line;
        relTasks = new List[numOfRel];
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            relTasks[Integer.parseInt(spt[1])].add(spt[0] + "\t" + spt[2]);
        }
        br.close();
        LogInfo.logs("Relation-Entity Pairs Map Loaded.");
    }


    public static int lenOfwIn;
    public static void main(String[] args) throws Exception {
        numOfRel = Integer.parseInt(args[0]);
        lenOfwIn = Integer.parseInt(args[1]);
        multiThreadWork();
    }
}

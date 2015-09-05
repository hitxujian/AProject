package xusheng.wikipedia.EP;

import fig.basic.LogInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by angrymidiao on 6/10/15.
 * Extract positive linking pairs from Wikipedia
 */

public class LinkingPairCreator {

    public static BufferedWriter bw;
    public static boolean verbose = true;

    public static boolean flag(char ch) {
        if (ch <= 'z' && ch >= 'a' || ch <='Z' && ch >= 'A' || ch == '#')
            return true;
        else return false;
    }

    public static String process(String str) {
        if (isOther(str)) return "###";
        int i = 0;
        while (i<str.length() && ! flag(str.charAt(i))) i++;
        int j = str.length()-1;
        while (j >=0 && ! flag(str.charAt(j))) j--;
        if (i<=j) return str.substring(i, j + 1);
        else return "";
    }

    public static String extractHref(String sentence) {
        String[] spt = sentence.split("</a>")[0].split("\">");
        return spt[spt.length-1];
    }

    public static String extractHrefTitle(String sentence) {
        String[] spt = sentence.split("\">")[0].split("href=\"");
        String tmp = spt[spt.length-1];
        String ret = tmp.replace("%20", " ");
        return ret;
    }

    public static void writeRet(ArrayList<String> sentence, MatchItem i1, MatchItem i2) throws  Exception{
        if (i1.start <= 1 && i2.end >= sentence.size()-1 || i2.start <= 1 && i1.end >= sentence.size()-1) {
            StringBuffer ret = new StringBuffer();
            if (verbose) LogInfo.logs(i1.toString() + "\n" + i2.toString());
            if (i1.end < i2.start) {
                ret.append(i1.name + "\t" + i1.title + "\t");
                for (int i = i1.end; i < i2.start; i++) ret.append(sentence.get(i) + " ");
                ret.append("\t" + i2.name + "\t" + i2.title + "\n");
                bw.write(ret.toString());
                bw.flush();
            } else if (i2.end < i1.start) {
                ret.append(i2.name + "\t" + i2.title + "\t");
                for (int i = i2.end; i < i1.start; i++) ret.append(sentence.get(i) + " ");
                ret.append("\t" + i1.name + "\t" + i1.title + "\n");
                bw.write(ret.toString());
                bw.flush();
            }
        }
    }

    public static boolean isOther(String s) {
        for (int i=0; i<s.length()-2; i++)
            if (s.charAt(i) == '#' && s.charAt(i+1) == '#' && s.charAt(i+2) == '#') return true;
        return false;
    }

    public static void workForSentence(String sentence, String title) throws Exception {
        String[] spt = sentence.split("\">");
        if (spt.length != 2) return;
        ArrayList<String> alias = AliasTitle.getAlias(title.toLowerCase());
        alias.add(title.toLowerCase());

        if (verbose) LogInfo.begin_track("Original sentence: %s", sentence);
        String theOther = extractHref(sentence);
        String theOtherTitle = extractHrefTitle(sentence);
        if (verbose) LogInfo.logs("the other title: %s", theOther);
        sentence = sentence.replaceAll("<a.*a>", "###");
        if (verbose) LogInfo.logs("replaced sentence: %s", sentence);

        HashSet<String> aliases = new HashSet<>();
        int maxWin = 0;
        for (String str : alias) {
            aliases.add(str);
            int tmp = str.split(" ").length;
            if (tmp > maxWin) maxWin = tmp;
        }

        MatchItem i1, i2 = null;
        spt = sentence.split(" ");
        ArrayList<String> nSentence = new ArrayList<>();
        for (int i=0; i<spt.length; i++) {
            String word = process(spt[i]);
            nSentence.add(word);
            if (word.equals("###")) {
                i2 = new MatchItem(i, i+1, theOther, theOtherTitle);
            }
        }

        if (i2 == null) return;
        if (verbose) LogInfo.logs("transformed sentence: %s", nSentence.toString());

        for (int win = maxWin-1; win > 0; win--) {
            for (int st=0; st<nSentence.size()-win+1; st++) {
                int ed = st + win;
                StringBuffer name = new StringBuffer();
                name.append(nSentence.get(st));
                for (int i=st+1; i<ed; i++)
                    name.append(" " + nSentence.get(i));
                if (aliases.contains(name.toString().toLowerCase())) {
                    i1 = new MatchItem(st, ed, name.toString(), title);
                    if (verbose) LogInfo.logs("#match: %s", i1.toString());
                    writeRet(nSentence, i1, i2);
                    if (verbose) LogInfo.end_track();
                    return;
                }
            }
        }
        if (verbose) LogInfo.end_track();
    }
    /*public static String subString(ArrayList<String> sentence, MatchItem mi1, MatchItem mi2) {
        StringBuffer ret = new StringBuffer();
        ret.append(mi1.name);
        ret.append("\t" + sentence.get(mi1.end));
        for (int i=mi1.end+1; i<mi2.start; i++) ret.append(" " + sentence.get(i));
        ret.append("\t" + mi2.name);
        return ret.toString();
    }

    public static void workForSentence(String sentence) throws Exception{
        //bw.write(sentence.toLowerCase() + "\n");
        if (sentence.split(" ").length < 3) return;

        if (verbose) LogInfo.begin_track("Orignial sentence: %s", sentence);
        String[] spt = sentence.split(" ");
        ArrayList<String> nSentence = new ArrayList<>();
        for (int i=0; i<spt.length; i++) {
            String word = process(spt[i]);
            nSentence.add(word);
        }
        if (verbose) LogInfo.logs("transformed sentence: %s", nSentence.toString());
        ArrayList<MatchItem> matches = new ArrayList<>();

        for (int win=1; win < 6; win++) {
            for (int st=0; st<nSentence.size()-win+1; st++) {
                int ed = st + win;
                StringBuffer name = new StringBuffer();
                name.append(nSentence.get(st));
                for (int i=st+1; i<ed; i++)
                    name.append(" " + nSentence.get(i));
                if (AliasEntity.getEntity(name.toString()) != null) {
                    MatchItem tmp = new MatchItem(st, ed, name.toString());
                    matches.add(tmp);
                    if (verbose) LogInfo.logs("#%d match: %s", matches.size(), tmp.toString());
                }
            }
        }

        if (matches.size() < 2) {
            LogInfo.end_track();
            return;
        }

        for (int i=0; i<matches.size(); i++) {
            for (int j=i+1; j<matches.size(); j++) {
                int dist = matches.get(i).start - matches.get(j).end;
                if (dist > 0 && dist < 6) {
                    String ret = subString(nSentence, matches.get(j), matches.get(i));
                    if (verbose) LogInfo.logs("@find linking pairs: %s", ret);
                    bw.write(ret + "\n");
                }
                dist = matches.get(j).start - matches.get(i).end;
                if (dist > 0 && dist < 6) {
                    String ret = subString(nSentence, matches.get(i), matches.get(j));
                    if (verbose) LogInfo.logs("@find linking pairs: %s", ret);
                    bw.write(ret + "\n");
                }
            }
        }
        LogInfo.end_track();
    }*/

    public static String extractTitle(String line) {
        String[] spt = line.split("title=\"");
        String tmp = spt[spt.length-1];
        return tmp.split("\"")[0];
    }

    public static int articles = 0;
    public static void work(String inFile) throws Exception {
        File file = new File(inFile);
        if (! file.exists()) return;

        LogInfo.logs(inFile);
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        String line = "", title = "";
        while ((line = br.readLine()) != null) {
            if (line.startsWith("<doc")) {
                //LogInfo.end_track();
                title = extractTitle(line);
                articles ++;
                if (verbose) LogInfo.logs("Doc: %s", title);
                continue;
            }
            String[] spt = line.split("\\.");
            for (int i = 0; i < spt.length; i++) {
                String[] sptt = spt[i].split(",");
                for (int j = 0; j < sptt.length; j++)
                    workForSentence(sptt[j], title);
            }
        }
        br.close();
        if (verbose) LogInfo.logs("One file finished!");
    }


    public static void main(String[] args) throws Exception {
        AliasTitle.initialize(args[0]);
        bw = new BufferedWriter(new FileWriter(args[2]));

        for (char Ch ='A'; Ch<='D'; Ch++) {
            for (char ch = 'A'; ch <= 'Z'; ch++) {
                for (int i = 0; i <= 9; i++) {
                    String inFile = String.format("%s/%s%s/wiki_0%d", args[1], Ch, ch, i);
                    try {
                        work(inFile);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                for (int i = 10; i <= 99; i++) {
                    String inFile = String.format("%s/%s%s/wiki_%d", args[1], Ch, ch, i);
                    try {
                        work(inFile);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        /*
        for (char ch='A'; ch<='Z'; ch++) {
            for (int i = 0; i <= 9; i++) {
                String file = String.format("%s/A%s/wiki_0%d", args[1], ch, i);
                LogInfo.logs(file);
                try {
                    work(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            for (int i = 10; i <= 99; i++) {
                String file = String.format("%s/A%s/wiki_%d", args[1], ch, i);
                LogInfo.logs(file);
                try {
                    work(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        for (char ch='A'; ch<='R'; ch++) {

            for (int i = 0; i <= 9; i++) {
                String file = String.format("%s/B%s/wiki_0%d", args[1], ch, i);
                LogInfo.logs(file);
                try {
                    work(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            for (int i = 10; i <= 99; i++) {
                String file = String.format("%s/B%s/wiki_%d", args[1], ch, i);
                LogInfo.logs(file);
                try {
                    work(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        */
        LogInfo.logs("%d passages in total", articles);
        bw.close();
        /*String str = "fafsfs<a href=\"badfsf\">fsdf</a>fsfsfsfs<a href=\"\">fsfsf</a>daddadadad";
        String rep = str.replaceAll("<a.*?a>", "###");
        System.out.println(rep + "   " + str);*/

    }
}

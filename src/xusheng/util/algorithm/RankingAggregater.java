package xusheng.util.algorithm;

/**
 * Input : n ranking lists (with or without weights)
 * Output: one ranking lists
 * Algorithm: Genetic Algorithm (mutate -> choose best group -> continue to mutate....)
 * @author Xusheng
 * @author freefish_6174@126.com
 * @version 2.0
 */
import java.util.*;

import fig.basic.LogInfo;
import xusheng.util.struct.MapHelper;

public class RankingAggregater {

    public static ArrayList<String> elements = new ArrayList<>();
    public static HashMap<String, Integer> pairs = new HashMap<>();

    public static boolean verbose = true;

    /**
     * weighted list
     * @param lists      ranking lists
     * @param weightList weights list
     */
    public static ArrayList<String> rankingAggregate(ArrayList<ArrayList<String>> lists, ArrayList<Double> weightList) {
        ArrayList<ArrayList<String>> tmp = new ArrayList<ArrayList<String>>();
        int len = lists.size();
        for (int i=0; i<len; i++) {
            int cnt = (int) Math.round(weightList.get(i)*10);
            for (int j=0; j<cnt; j++)
                tmp.add(lists.get(i));
        }
        ArrayList<String> ret = ranking(tmp);
        return ret;
    }

    /**
     * non-weighted list
     * @param lists      ranking lists
     */
    public static ArrayList<String> rankingAggregate(ArrayList<ArrayList<String>> lists) {
        ArrayList<String> ret = ranking(lists);
        return ret;
    }

    public static ArrayList<String> ranking(ArrayList<ArrayList<String>> lists) {
        getElements(lists);
        int len =elements.size();
        /* number of mutations each round */
        int numOfMutate = len*(len-1)/2 > 100? 100: len*(len-1)/2;
        //int numOfMutate = len*(len-1)/2;
        /* number of size for next mutation */
        int numOfGeneration = numOfMutate;
        getPairs(lists);
        ArrayList<ArrayList<String>> generation = new ArrayList<> ();
        HashMap<ArrayList<String>, Integer> pmap = new HashMap<>();
        ArrayList<ArrayList<String>> self = new ArrayList<>();

        if (verbose) System.out.print("the ancestor: \n  " + getScore(elements) + ": ");
        if (verbose) printList(elements);

        if (verbose) System.out.println("initial scores: ");
        generation.add(elements);

        for (int i=0; i<numOfMutate; i++) {
            ArrayList<String> tmp = mutate(elements);
            while (generation.contains(tmp))
                tmp = mutate(elements);
            generation.add(tmp);
            if (verbose) System.out.printf("  %d: ", getScore(tmp));
            if (verbose) printList(tmp);
        }
        if (verbose) System.out.printf("the size of generation: %d\n", generation.size());

        int flag = 0, round = 0, prev = 0;
        /* after 4 rounds, if the best score is not changed, then consider it a solution */
        while (flag < 4) {
            if (verbose) LogInfo.begin_track("round %d...", round);
            pmap.clear();
            for (ArrayList<String> list: generation) {
                pmap.put(list, getScore(list));
                self.clear();
                for (int i=0; i<numOfMutate; i++) {
                    ArrayList<String> tmp = mutate(list);
                    while (self.contains(tmp))
                        tmp = mutate(list);
                    self.add(tmp);
                }

                for (ArrayList<String> tmp : self)
                    if (!pmap.containsKey(tmp)) {
                        int score = getScore(tmp);
                        //System.out.printf("  %d: ", score);
                        //printList(tmp);
                        pmap.put(tmp, score);
                    }
            }

            ArrayList<Map.Entry<ArrayList<String>, Integer>> pool =
                    MapHelper.sort(pmap);
            if (verbose) LogInfo.logs("the size of pool: %d", pool.size());

            generation.clear();
            for(int i=0; i<numOfGeneration; i++) {
                Map.Entry<ArrayList<String>, Integer> tmp = pool.get(i);
                generation.add(tmp.getKey());
                if (verbose) System.out.print("  " + tmp.getValue());
            }
            if (verbose) System.out.println();
            if (verbose) LogInfo.end_track();
            round ++;
            if (pool.get(0).getValue() == prev) flag ++;
            else flag = 0;
            prev = pool.get(0).getValue();
        }
        return generation.get(0);
    }

    public static void getElements(ArrayList<ArrayList<String>> lists) {
        for (ArrayList<String> list: lists)
            for (String elem: list)
                if (!elements.contains(elem))
                    elements.add(elem);
    }

    public static void getPairs(ArrayList<ArrayList<String>> lists) {
        for (ArrayList<String> list: lists)
            for (int i=0; i<list.size()-1; i++)
                for(int j=i+1; j<list.size(); j++) {
                    String pair = list.get(i) + "\t" + list.get(j);
                    if (pairs.containsKey(pair)) {
                        int cnt = pairs.get(pair);
                        pairs.put(pair, cnt+1);
                    }
                    else
                        pairs.put(pair, 1);
                }
    }

    public static Integer getScore(ArrayList<String> list) {
        int score = 0;
        for (int i=0; i<list.size()-1; i++)
            for (int j=i+1; j<list.size(); j++) {
                String pair = list.get(i) + "\t" + list.get(j);
                if (pairs.containsKey(pair))
                    score += pairs.get(pair);
            }
        return score;
    }

    public static ArrayList<String> mutate(ArrayList<String> list) {
        ArrayList<String> ret = new ArrayList<>(list);
        Random rand = new Random();
        int len = list.size();
        int pos_1 = rand.nextInt(len), pos_2 = rand.nextInt(len);
        while(pos_1 == pos_2) pos_2 = rand.nextInt(len);
        //LogInfo.logs("%d %d", pos_1, pos_2);
        ret.set(pos_1, list.get(pos_2));
        ret.set(pos_2, list.get(pos_1));
        return ret;
    }

    public static void printList(ArrayList<String> tmp) {
        for (int j=0; j<tmp.size(); j++)
            System.out.print("  " + tmp.get(j));
        System.out.println();
    }

    public static void main(String[] args) {

        ArrayList<String> standard = new ArrayList<String>();
        Random rand = new Random();
        for (int i=0; i<100; i++) {
            int tmp = rand.nextInt(100);
            while (standard.contains(String.valueOf(tmp)))
                tmp = rand.nextInt(100);
            standard.add(String.valueOf(tmp));
        }

        ArrayList<ArrayList<String>> ex_l = new ArrayList<ArrayList<String>> ();

        ex_l.add(mutate(mutate(standard)));

        for (int i=0; i<50; i++) {
            int tmp = rand.nextInt(200);
            while (standard.contains(String.valueOf(tmp)))
                tmp = rand.nextInt(200);
            standard.add(String.valueOf(tmp));
        }

        ex_l.add(mutate(mutate(mutate(mutate(standard)))));


        for (int i=0; i<ex_l.get(0).size(); i++) {
            String id = ex_l.get(0).get(i);
            System.out.print(id + " ");
        }
        System.out.println();

        for (int i=0; i<ex_l.get(1).size(); i++) {
            String id = ex_l.get(1).get(i);
            System.out.print(id + " ");
        }
        System.out.println();

        ArrayList<String> ex_r = rankingAggregate(ex_l);
        for (int i=0; i<ex_r.size(); i++) {
            String id = ex_r.get(i);
            System.out.print(id + " ");
        }
        System.out.println();

        for (int i=0; i<standard.size(); i++) {
            String id = standard.get(i);
            System.out.print(id + " ");
        }
        System.out.println("\n" + getScore(standard));
    }

    /* Output:

     */
}

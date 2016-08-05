package xusheng.kg.baike.relation.model;

import xusheng.util.nlp.ChWordSegmentor;

/**
 * Created by Xusheng on 8/1/2016.
 * Perform Chinese Word Segmentation using Stanford
 */

public class WordSegmenter implements Runnable{

    public void run() {
        while (true) {
            try{
                int idx = getCurr();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static synchronized int getCurr() {
        return -1;
    }

    public static void initialize() throws Exception {
        ChWordSegmentor.initialize();
    }

    public static void work() throws Exception {
        initialize();

    }

    public static void main(String[] args) throws Exception {
        work();
    }
}

package xusheng.util.struct;

import java.util.ArrayList;

/**
 * Created by Xusheng on 2015/5/16.
 * Multi-thread class for easy use.
 */

public class MultiThread {
    ArrayList<Thread> threadList = null;

    public MultiThread(int size, Runnable workThread) {
        this.threadList = new ArrayList();

        for(Integer i = Integer.valueOf(0); i.intValue() < size; i = Integer.valueOf(i.intValue() + 1)) {
            this.threadList.add(new Thread(workThread, i.toString()));
        }

    }

    public MultiThread(int size, Runnable workThread, boolean isDebug) {
        if(isDebug) {
            size = 1;
        }

        this.threadList = new ArrayList();

        for(Integer i = Integer.valueOf(0); i.intValue() < size; i = Integer.valueOf(i.intValue() + 1)) {
            this.threadList.add(new Thread(workThread, i.toString()));
        }

    }

    public void runMultiThread() throws Exception {
        int size = this.threadList.size();

        int i;
        for(i = 0; i < size; ++i) {
            ((Thread)this.threadList.get(i)).start();
        }

        for(i = 0; i < size; ++i) {
            ((Thread)this.threadList.get(i)).join();
        }

    }
}
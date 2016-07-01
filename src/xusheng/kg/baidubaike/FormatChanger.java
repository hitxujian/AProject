package xusheng.kg.baidubaike;

import java.io.*;

/**
 * Created by Xusheng on 7/1/2016.
 * Each function aims to change some file format
 * Refer to the specific notes
 */
public class FormatChanger {

    public static void RecoverInfoTriple() throws IOException {
        BkEntityIdxReader.initializeFromIdx2Name();
        BufferedReader br = new BufferedReader(new FileReader("/home/xusheng/starry/baidubaike/infobox.triple"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("/home/xusheng/starry/baidubaike/infobox.triple.raw"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] spt = line.split("\t");
            String leftName = BkEntityIdxReader.getName(Integer.parseInt(spt[0]));
            String rightName = BkEntityIdxReader.getName(Integer.parseInt(spt[2]));
            if (leftName != null && rightName != null)
                bw.write(leftName + "\t" + spt[1] + "\t" + rightName + "\n");
            else if (leftName == null && rightName == null)
                bw.write(line + "\n");
            else if (leftName != null)
                bw.write(leftName + "\t" + spt[1] + "\t" + spt[2] + "\n");
            else
                bw.write(spt[0] + "\t" + spt[1] + "\t" + rightName + "\n");
        }
        br.close();
        bw.close();
    }

    public static void main(String[] args) throws IOException {

    }
}

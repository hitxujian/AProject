package xusheng.util.log;

import fig.basic.LogInfo;

public class LogUpgrader {
	
	public static void showLine(int lines, int mod) {
		if (0 == lines % mod) {
            if (lines >= 100000000) LogInfo.logs("Current: %d,0000,0000", lines / 10000000);
            else if (lines >= 10000) LogInfo.logs("Current: %d,0000", lines / 10000);
            else LogInfo.logs("Current: %d", lines);
        }

	}
}

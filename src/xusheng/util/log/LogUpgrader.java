package xusheng.util.log;

import fig.basic.LogInfo;

public class LogUpgrader {
	
	public static boolean showLine(int lines, int mod) {
		if (0 == lines % mod) {
            LogInfo.logs("[log] %d lines.", lines);
            return true;
        }
        return false;
	}
}

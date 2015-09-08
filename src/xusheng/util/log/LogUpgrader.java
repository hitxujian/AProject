package xusheng.util.log;

import fig.basic.LogInfo;

public class LogUpgrader {
	
	public static void showLine(int lines, int mod) {
		if (0 == lines % mod)
			LogInfo.logs("Current: %d", lines);
	}
}

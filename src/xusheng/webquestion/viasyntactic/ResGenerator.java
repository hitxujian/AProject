package xusheng.webquestion.viasyntactic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class ResGenerator {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		process(args[0], args[1]);
	}
	
	public static void process(String inFile, String outFile) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		String line = "";
		while ((line=br.readLine()) != null) {
			if (line.trim().startsWith("Ex")) {
				bw.write(line.trim() + "\n");
			}			
			else if (line.trim().startsWith("<")) {
				bw.write(line.trim() + "\n");
			}
		}
		
		br.close();
		bw.close();
	}

}

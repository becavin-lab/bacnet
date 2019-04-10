package bacnet.scripts.core;

import java.io.IOException;

import bacnet.Database;
import bacnet.utils.CMD;

public class Rscript {

	public static String PATH_WIN7 = Database.getInstance().getPath()
			+ "Software/bacnet.scripts/Rscripts/Normalization/";

	public static String REXEC = "\"C:/Program Files/R/R-3.0.2/bin/x64/Rscript.exe\"";

	public static void run(String fileName) throws IOException {
		System.out.println("Run R script");
		String os = System.getProperty("os.name");
		String Rscript = "Rscript";
		if (os.equals("Win7"))
			Rscript = REXEC;

		String rcommand = Rscript + " \"" + fileName + "\" --vanilla --verbose";
		System.out.println(rcommand);
		CMD.runProcess(rcommand, true);
	}

	/**
	 * Test if the R script directory has been given, and open a dialog to update it
	 * in case
	 * 
	 * @return fileName = path of the genome directory
	 */
	public static String getRscriptDirectory() {
		String fileName = PATH_WIN7;
		return fileName;
	}

}

package bacnet.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CMD {

	/**
	 * Run a process.
	 * 
	 * @param execProcess the shell line to execute
	 * @param print       whether or not display the output
	 * @return
	 * @throws IOException
	 */
	public static String runProcess(String execProcess, boolean print, String dir) throws IOException {
		@SuppressWarnings("unused")
		String os = System.getProperty("os.arch");
		// if(!os.equals("amd64")) execProcess = execProcess.replaceAll("\"","");
		if (print) {
			System.out.println(execProcess);
		}
		runProcess(execProcess.split(" "), print, dir);
		return "";
	}

	/**
	 * Run a process
	 * 
	 * @param execProcess the shell line to execute
	 * @param print       whether or not display the output
	 * @return
	 * @throws IOException
	 */
	public static String runProcess(String execProcess, boolean print) throws IOException {
		String os = System.getProperty("os.arch");
		if (!os.equals("amd64"))
			execProcess = execProcess.replaceAll("\"", "");
		runProcess(execProcess.split(" "), print);
		return "";
	}

	/**
	 * Run a process.
	 * 
	 * @param args  list of argument which will be concatenated
	 * @param print whether or not display the output
	 * @return
	 * @throws IOException
	 */
	public static String runProcess(String[] args, boolean print, String dir) throws IOException {
		try {
			ProcessBuilder pb = new ProcessBuilder(args);
			pb.directory(new File(dir));

			// Map<String, String> env = pb.environment();
			// for (Entry entry : env.entrySet()) {
			// System.out.println(entry.getKey() + " : " + entry.getValue());
			// }
			// env.put("MonArg", "Valeur");

			Process p = pb.start();
			DisplayFlux fluxSortie = new DisplayFlux(p.getInputStream());
			DisplayFlux fluxErreur = new DisplayFlux(p.getErrorStream());
			new Thread(fluxSortie).start();
			new Thread(fluxErreur).start();
			p.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 
	 * @param args
	 * @param print
	 * @return
	 * @throws IOException
	 */
	public static String runProcess(String[] args, boolean print) throws IOException {
		try {
			ProcessBuilder pb = new ProcessBuilder(args);
			// pb.directory(new File(dir));

			// Map<String, String> env = pb.environment();
			// for (Entry entry : env.entrySet()) {
			// System.out.println(entry.getKey() + " : " + entry.getValue());
			// }
			// env.put("MonArg", "Valeur");

			Process p = pb.start();
			DisplayFlux fluxSortie = new DisplayFlux(p.getInputStream());
			DisplayFlux fluxErreur = new DisplayFlux(p.getErrorStream());
			new Thread(fluxSortie).start();
			new Thread(fluxErreur).start();
			p.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "";
	}
}

class DisplayFlux implements Runnable {

	private final InputStream inputStream;

	DisplayFlux(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	private BufferedReader getBufferedReader(InputStream is) {
		return new BufferedReader(new InputStreamReader(is));
	}

	@Override
	public void run() {
		BufferedReader br = getBufferedReader(inputStream);
		String line = "";
		try {
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

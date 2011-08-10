import java.io.*;

import org.jaywalk.Console;


public class LuaShell extends Console {
	Process p;
	PrintWriter out;
//	BufferedReader in;
	Thread reader;
	
	LuaShell() {
		super("Lua Interpreter","");
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("python","-i");
		pb.redirectErrorStream();
		try {
			p = pb.start();
			out = new PrintWriter(p.getOutputStream());
			final BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			reader = new Thread() {
				public void run() {
					try {
						int line = in.read();
						while (line != 0) {
							display((char)line+"\n");
							line = in.read();
						}
					} catch (IOException e) {
						System.out.println("Should not happen");
					}
				}
			};
			reader.start();
		} catch (IOException e) {
			display("sorry, can't load Lua\n");
		}
	}
	
	public boolean eval(String line) {
		out.println(line);
		out.flush();
//		display(line.toUpperCase()+"\n");
		return true;
	}
	
	public static void main(String[] args) {
		LuaShell ls = new LuaShell();
		ls.setVisible(true);
	}

}

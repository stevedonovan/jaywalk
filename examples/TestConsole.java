

import java.io.*;
import java.util.*;

import org.jaywalk.Commandlet;
import org.jaywalk.Console;

public class TestConsole extends Commandlet {
	public double A = 0.0;
	public String name = "help";
	
	public String bonzo(String stuff) {
		return stuff.toUpperCase();
	}
	
	public void dump() {
		println("A "+A+" and name = " + name);
	}
	
	public void test(String... items) {
		println(items[0]);
	}
	
	private class FilePatFilter implements FilenameFilter {
		private String pat;
		
		FilePatFilter (String pat_) {
			pat = pat_;
		}
		
		public boolean accept(File dir, String name) {
			return name.matches(pat);
		}		
	}
	
	public void ls (String pat) {
		File[] files = new File(".").listFiles(new FilePatFilter(pat));
		for (File f : files)
			println(f.getName());
	}
	
	public String get (String prop) {
		return System.getProperty(prop);
	}
	
	public void set (String prop, String value) {
		System.setProperty(prop,value);
	}
	
	public void quit() {
		System.exit(0);
	}
	
	
	TestConsole() {
	}

	public static void main(String[] args) {
		TestConsole me = new TestConsole();
		Console c = new Console("testing console","? ",me);
		me.go(c);
		c.setVisible(true);
	}

}

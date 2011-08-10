
import java.lang.reflect.*;
import java.util.*;
import java.io.*;

import org.jaywalk.Commandlet;
import org.jaywalk.Help;

@Help("demonstrate basic functionality")
public class TestCommand extends Commandlet {
	
	@Help("value for one command")
	public int flag = 10;
	private int hidden = 0;
	
	// will be visible as a flag -fred
	@Help("property with code")
	public void setFred(double f) {
		println("fred: "+f);
	}
	
	@Help("prints out a string with flag value")
	public void one(String name) {
		println("called one with " + name + ": " + flag);
	}
	
	@Help("takes a double and an int")
	public void two(double factor, int n) {
		println("two is "+factor+" "+n);
	}
	
	@Help("takes a variable number of doubles")
	public Object sum(double... vals) {
		double sum = 0.0;
		for (double x : vals) {
			sum += x;
		}
		return "sum was " + sum;
	}
	
	@Help("adds two numbers; does not print")
	public double add(double x, double y) {
		return x + y;
	}
	
	/* if a method of this name is defined, then a no-command invocation will
	 * go through it..
	public void stdin (BufferedReader in) throws IOException {		
		for (int i = 0; i < flag; i++)
			outln(in.readLine());
	}
	*/
	
	@Help("read a file and trim each line")
	public void read(BufferedReader file) throws IOException {
		String line = file.readLine();
		while (line != null) {
			System.out.println(line.trim());
			line = file.readLine();
		}
	}	
	
	@Help("match first string against a regular expression")
	public boolean match(String s, String pat) {
		return s.matches(pat);
	}
	
	public static void main(String[] args) {
		new TestCommand().go(args);
	}


}

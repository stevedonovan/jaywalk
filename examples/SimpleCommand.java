
import java.io.*;

import org.jaywalk.Commandlet;
import org.jaywalk.Help;
import org.jaywalk.Param;

@Help("Demonstrating a simple command-line framework")
public class SimpleCommand extends Commandlet {
	@Help("scale factor")
	public double scale = 1.0;

	@Help("adds two numbers together and scales")
	public double add(double x, double y) {
		return (x+y)*scale;
	}
	
	@Help("writes out a file")
	public void write(PrintStream w) {
		for (int i = 0; i < 10; i++)
			w.println("hello " + i);
		w.close();
	}
	
	@Help("reads a file")
	public void read(BufferedReader r) throws IOException {
		String line;
		while ((line = r.readLine()) != null) {
			System.out.println(line.trim());
		}
	}
	
	@Help("multiples two numbers")
	public double mul(
	@Param("a first number") double a,
	@Param("b second number") double b) {
		return a*b;
	}
	
	public static void main(String[] args) {
		new SimpleCommand().go(args);
	}

}

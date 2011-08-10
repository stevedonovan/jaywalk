package org.jaywalk;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;


public class Commandlet implements Evaluator {
	protected MethodMap methods;
	protected FieldMap fields;
	protected List<Closeable> files;
	public boolean debug = false;

	public static Object[] A(Object...objects) {
		return objects;
	}
	
	public static Map<String,Object> M(Object... objects) {
		Map<String,Object> res = new HashMap<String,Object>();
		extendMap(res,objects);
		return res;
	}
	
	public static void extendMap(Map<String,Object> map, Object... objects) {
		for (int i = 0; i < objects.length; i += 2) {
			map.put((String)objects[i], objects[i+1]);
		}
	}
	
	private void checkArg(boolean cond, String message) {
		if (! cond) {
			throw new IllegalArgumentException(message);			
		}
	}
	

	// returns true if the flag used the following value, false otherwise.
	// Throws a wobbly if it fails.
	protected boolean handleFlag(String arg, String svalue) throws Exception {
		boolean res = true;
		Object value;
		Class<?> ft = fields.getType(arg);
		if (ft == null) { // no such field!
			xflags.put(arg, svalue);
			return true;			
		}
		if (ft.equals(boolean.class)) {
			value = true;
			res = false;
		} else {
			value = convertString(svalue,ft); 
		}
		fields.put(arg, value);
		return res;
	}
	
	Map<String, String> xflags;
	
	Displayer display = null;
	
	protected void go(Displayer display_) {
		display = display_;
	}
	
	protected void go(String[] args) {
		methods = new MethodMap(this);
		fields = new FieldMap(this);
		files = new ArrayList<Closeable>();
		xflags = new HashMap<String,String>();
		boolean isInteractive = false;
		try {
			// evaluate any flags and collect the arguments
			List<String> cargs = new ArrayList<String>();
			for (int i = 0; i < args.length; ++i) {
				String arg = args[i];
				if (arg.startsWith("-")) {
					arg = arg.substring(1);
					if (arg.startsWith("-")) // support -- for old GNU habit
						arg = arg.substring(1);
					if (arg.equals("help")) {
						showHelp();
						return;
					} else 
					if (arg.equals("i")) {
						isInteractive = true;
					} else {
						if (handleFlag(arg,args[i+1]))	++i; 						
					}
				} else {
					cargs.add(arg);
				}
			}
			if (isInteractive) {
				interactive();
				return;
			}
			Method method;
			if (cargs.size() == 0) { // no explicit command
				method = methods.get("stdin");
				if (method != null)	
					cargs.add("=stdin");
				else {
					showHelp();
					System.exit(0);
				}
			} else {				
				// the command is the first non-flag parameter
				String cmdName = cargs.get(0);
				method = methods.get(cmdName);
				checkArg(method != null, "command not found: "+cmdName);
				cargs.remove(0);
				String[] parmNames = MethodMap.getParameterNames(method,false);
				if (parmNames != null && xflags.size() > 0) {
					for (String name : parmNames) {
						String sval = xflags.get(name);
						checkArg(sval != null,"named argument not found: "+name);
						xflags.remove(name);
						cargs.add(sval);
					}
				}
			}
			checkArg(xflags.size() == 0, "unknown flags");
			String[] parms = cargs.toArray(new String[]{});
			
			// each parameter must be converted using the corresponding argument type
			// of the method
			Class<?>[] types = method.getParameterTypes();
			ArrayList<Object> values = new ArrayList<Object>();
			int nargs = types.length;
			if (! method.isVarArgs()) {
				checkArg(types.length == parms.length, "wrong number of args. Expecting: "+parms.length);
			} else {
				--nargs;
			}
			for (int i = 0; i < nargs; ++i) {
				values.add(convertString(parms[i],types[i]));
			}
			// last argument of vararg functions is special; we have to make up an array using the
			// base type of the type
			if (method.isVarArgs()) {
				Class<?> type = types[nargs].getComponentType();
				Object varargs = Array.newInstance(type, parms.length - nargs);
				for (int i = nargs, j = 0; i < parms.length; ++i,++j) {
					Object res = convertString(parms[i],type);
					Array.set(varargs,j,res);
				}				
				values.add(varargs);
			}
			Object[] vals = values.toArray();
			Object result = method.invoke(this, vals);
			// the method may return a value; show it.
			if (result != null) {
				displayResult(result);
			}
			if (files.size() > 0) {
				for (Closeable c : files) {
					c.close();
				}
			}
			files.clear();
		} catch(Exception e) {
			if (! debug) {
				println("error: "+e.getMessage(),true);
			} else {
				e.printStackTrace();
			}
		}
	}
	
	private void interactive() throws IOException {
		BufferedReader in = openTextFile("=stdin");
		showHelp();
		System.out.print("? ");
		String line = in.readLine();		
		while (line != null && ! line.equals("quit")) {
			eval(line);
			System.out.print("? ");
			line = in.readLine();
		}
	}
	
	protected void println(String out, boolean error) {
		if (display != null) {
			display.display(out+"\n");
			return;
		}
		if (error)
			System.err.println(out);
		else
			System.out.println(out);
	}
	
	protected void println(String out) {
		println(out,false);
	}

	private void displayResult(Object result) throws Exception {
		//  can customize output with stringifiers
		Method m = findStringifier(result.getClass());
		if (m != null) {
			String res = (String)m.invoke(this, result);
			println(res,false);
		} else {
			println(result.toString(),false);
		}
	}	

	private void showHelp() {
		String help = fields.classHelp();
		if (help != null) {
			println(help);
		}
		// OK, we don't require methods/fields to be specially tagged,
		// but methods with names like 'setField' are actually used as 
		// flag setters.
		try {
			println("\nFlags: ");
			for (String name : fields.keysWithHelpSet()) {
				println("-"+name+":\t"+fields.getHelp(name));				
			}
			println("\nCommands: ");
			for (String name : methods.keysWithHelpSet()) {
				println(name+":\t"+methods.getHelp(name));
			}			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}	
	}
	
	private BufferedReader openTextFile (String file) throws FileNotFoundException {
		if (file.equals("=stdin")) {
			return new BufferedReader (new InputStreamReader(System.in));
		} else {
			FileReader fr = new FileReader(file);
			closeLater(fr);
			return new BufferedReader(fr);
		}
	}
	
	public void closeLater(Closeable f) {
		files.add(f);
	}

	private Object convertString(String arg, Class<?> type)
			throws Exception
 {
		Object value;
		if (type.equals(double.class)) {
			value = Double.parseDouble(arg);					
		} else
		if (type.equals(int.class)) {
			value = Integer.parseInt(arg);
		} else
		if (type.equals(BufferedReader.class)) {
			value = openTextFile(arg);
		} else
		if (type.equals(PrintStream.class)) {
			PrintStream pw = new PrintStream(arg);
			closeLater(pw);
			value = pw;
		} else
		if (type.equals(String.class)) {
			value = arg; // it's just a string 
		} else { 
			Method converter = findConverter(type);
			checkArg(converter != null, "cannot find a converter for: "+type);
			value = converter.invoke(this, arg);
		}
		return value;
	}
	
	private Method findConverter(Class<?> type) {
		for (Method m : methods.values()) {
			if (m.getAnnotation(Converter.class) != null) {
				if (m.getReturnType().equals(type)) {			
					return m;
				}
			}
		}
		return null;
	}
	
	private Method findStringifier(Class<?> type) {
		for (Method m : methods.values()) {
			if (m.getAnnotation(Stringifier.class) != null) {
				Class<?>[] types = m.getParameterTypes();
				if ( types[0].isAssignableFrom(type)) {
					return m;
				}
			}
		}
		return null;
	}	


	// implementing Evaluator
	public boolean eval(String line) {
		go(line.split(" "));
		return true;
	}
}

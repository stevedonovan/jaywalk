import java.lang.reflect.*;
import java.util.*;

import org.jaywalk.TypeUtils;

public class TestMap {
	
	static <T> List<T> L(T...objects) {
		return Arrays.asList(objects);
	}
	
	static <T> T[] A(T...objects) {
		return objects;
	}	
	
	static void println(Object o) { System.out.println(o); }
	
	static <T> Method getMethod(T value, String fun, Object... args) throws Exception {
		Class<?>[] types = getTypes(args);
		return value.getClass().getMethod(fun, types);		
	}
	
	static <T> List<T> filter (List<T> ls, String pred, Object... args) throws Exception {
		List<T> out = new ArrayList<T>();
		if (ls.size() == 0) return out;
		Method m = getMethod(ls.get(0),pred,args);
		for (T val : ls)
			if ((Boolean)m.invoke(val,args))
				out.add(val);
		return out;		
	}
	
	static <T> void forall (List<T> ls, String fun, Object... args) throws Exception {
		if (ls.size() == 0) return;
		Method m = getMethod(ls.get(0),fun,args);
		for (T val : ls)
			m.invoke(val,args);		
	}
	
	
	static <T> List<T> map( List<T> ls, String fun, Object... args) throws Exception {
		List<T> out = new ArrayList<T>();
		if (ls.size() == 0) return out;
		Method m = getMethod(ls.get(0),fun,args);
		for (T val : ls)
			out.add((T)m.invoke(val,args));
		return out;
	}
	
	static <T> T[] map (T[] ls, String fun, Object... args) throws Exception {
		T[] out = (T[])Array.newInstance(ls.getClass().getComponentType(), ls.length);
		if (ls.length == 0) return out;
		Method m = getMethod(ls[0],fun,args);
		for (int i = 0; i < ls.length; ++i)
			out[i] = (T)m.invoke(ls[i],args);
		return out;
	}

	private static Class<?>[] getTypes(Object... args) {
		Class<?>[] types = new Class<?>[args.length];
		for (int i = 0; i < args.length; ++i) {
			Class<?> type = args[i].getClass();			
			types[i] = TypeUtils.convertPrimitiveType(type);
		}
		return types;
	}


	public static void main(String[] args) {
		List<String> ls = L("one","two","three");
		try {
			List<String> nls = map(ls,"substring",0,1);
			for (String a : nls) println(a);			
			nls = map(ls,"toUpperCase");
			for (String a : nls) println(a);
			
			String[] as = A("here","we","go");
			String[] nas = map(as,"substring",1);
			for (String a : nas) println(a);
			
			nls = filter(ls,"startsWith","t");
			for (String a : nls) println(a);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}


import java.util.*;

public class SimpleExamples {
	
	static Object[] A(Object... objects) {
		return objects;
	}
	
	static void error (String message) throws IllegalArgumentException {
		throw new IllegalArgumentException(message);
	}
	
	static Map M (Object... objects) {
		Map map = new HashMap();
		for (int i = 0; i < objects.length; i += 2) {
			map.put(objects[i], objects[i+1]);
		}
		return map;
	}
	
	private static void println(Object o) {
		System.out.println(o);
	}

	public static void main(String[] args) {
		try {
			Map<String,Integer> map = M("one",10,"two",20);
			for (String s : map.keySet()) {
				println(s + ": " + (map.get(s)+1));
			}
			Integer i = new Integer(10);
			Double x = new Double(2.3);
			println(Integer.class.isAssignableFrom(Number.class));
			println(Number.class.isAssignableFrom(i.getClass()));
		} catch(Exception e) {
			System.out.println(e.toString());
		}
		
	}

}

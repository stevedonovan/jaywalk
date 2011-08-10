package org.jaywalk;
import java.util.*;

import java.lang.reflect.*;

class Person {
	public String name = "Bonzo";
}

class Child {
	public String name = "Alonzo";
	public int[] achievements = new int[]{};
}

class Bonzo extends Person {
	public int age = 27;	
	public double weight = 60.2;
	public int[] scores = {100,200,300};
	public short average = 444;
	static public Child child = new Child();
}

public class FieldMap implements Map<String,Object> {
	
	static void println(Object s) {
		System.out.println(s);
	}

	public static void main(String[] args) {
		FieldMap map = new FieldMap(new Object() {
			public double x = 2.3;
			public String name = "bonzo";
			private String surname = "Dog";
			public void setSurname(String s) {
				surname = s;
			}
			public String getSurname() {
				return surname;
			}
		});
		map.put("surname","dildo");
		for (String name : map.keySet()) {
			println(name+" = "+map.get(name));
		}		
	}
	
	Object object;
	Class<?> klass;
	boolean inherited;
	Map<String,Object> fields = new LinkedHashMap<String,Object>();
	Map<String,String> help = new HashMap<String,String>();
	Map<String, Class<?>> types = new HashMap<String,Class<?>>();
	
	public FieldMap(Object o, boolean inherited) {
		object = o;
		this.inherited = inherited;
		klass = o.getClass();
		Field[] ff = klass.getFields();
		for (Field f : ff) {
			if (accept(f)) {
				String name = f.getName();
				fields.put(name,f);
				types.put(name, f.getType());
				associateHelp(f,name);
			}
		}
		Method[] mm = klass.getMethods();
		for (Method m : mm) {
			String name = m.getName();
			if (name.matches("get[A-Z][a-z]+") && accept(m)) {
				name = m2flag("get",name);
				fields.put(name, m);
				types.put(name, m.getParameterTypes()[0]);
				associateHelp(m, name);
			}
		}
	}

	private void associateHelp(AnnotatedElement m, String name) {
		Help value = m.getAnnotation(Help.class);
		if (value == null)
			return;
		String helpstr = value.value();
		if (helpstr != null)
			help.put(name,helpstr);
	}
	
	public FieldMap(Object o) {
		this(o,false);
	}
	
	public String classHelp() {
		Help value = getClass().getAnnotation(Help.class);
		if (value != null) {
			return value.value();
		}		
		return null;
	}
	
	private boolean accept(Member f) {
		return ! Modifier.isStatic(f.getModifiers()) && (inherited || f.getDeclaringClass() == klass);
	}
	
	public String getHelp(String name) {
		return help.get(name);
	}
	
	public Set<String> keysWithHelpSet() {
		return help.keySet();
	}
	
	public Class<?> getType(String name) {
		return types.get(name);
	}
	
	public boolean containsKey(Object key) {
		return fields.containsKey((String)key);
	}

	public boolean containsValue(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public Set entrySet() {
		return fields.entrySet();
	}

	public Object get(Object key) {
		if (! containsKey(key)) return null;		
		try {
			Object f = fields.get(key);
			if (f instanceof Field) {
				return ((Field)f).get(object);
			} else {
				return ((Method)f).invoke(object);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public boolean isEmpty() {
		return size() > 0;
	}

	public Set<String> keySet() {
		return fields.keySet();
	}

	public int size() {
		return fields.size();
	}

	public Collection<Object> values() {
		List<Object> vals = new ArrayList<Object>(fields.size());
		for (String key : keySet()) {
			vals.add(get(key));
		}
		return vals;
	}
		
	// the Map.put operation corresponds to setting the field; this may
	// result in a type error.
	
	
	private static void error(String message) {
		throw new RuntimeException(message);
	}

	public Object put(String key, Object obj) {
		Object m = fields.get(key);
		if (m == null) return null;
		try {		
			if (m instanceof Field) {
				((Field)m).set(object, obj);
			} else {
				key = titleCase("set",key);
				klass.getMethod(key, obj.getClass()).invoke(object, obj);
				return null;
			}
			return obj;
		} catch (Exception e) {
			System.err.println(e);
			error(e.getMessage());
			return null;
		}
	}

	public void putAll(Map map) {
		for (Object key : map.keySet()) {
			put((String)key,map.get(key));
		}		
	}
	
	private String titleCase (String prefix, String val) {
		return prefix+val.substring(0,1).toUpperCase()+val.substring(1);		
	}
	
	private String m2flag (String prefix, String val) {
		String name = val.replaceFirst(prefix, "");		
		return name.substring(0,1).toLowerCase()+name.substring(1);
	}
	
	
    // this is not a resizable map!
	
	public void clear() { 	}
	public Object remove(Object arg0) { return null; }
}

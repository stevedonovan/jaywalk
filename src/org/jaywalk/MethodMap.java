package org.jaywalk;
import java.util.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;


public class MethodMap extends LinkedHashMap<String, Method> {
	Map<String,String> help = new HashMap<String,String>();
	
	public MethodMap(Object o) {
		Class<?> klass = o.getClass();
		Method[] methods = klass.getMethods();
		for (Method m : methods) {
			String name = m.getName();
			if (! name.matches("get[A-Z][a-z]+")) {
				associateHelp(m,name);
				put(name,m);
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
	
	public String getHelp(String name) {
		return help.get(name);
	}
	
	public Set<String> keysWithHelpSet() {
		return help.keySet();
	}	
	
	
	public static String[] getParameterNames(Method method, boolean justNames) {
		Annotation[][] annots = method.getParameterAnnotations();
		if (annots.length == 0) // no parameters
			return null;
		ArrayList<String> res = new ArrayList<String>();
		for (Annotation[] aa : annots) {
			if (aa.length == 0)
				return null;
			if (aa[0] instanceof Param) {
				
				String param = ((Param)aa[0]).value();
				String[] parts = param.split(" ",2);
//				System.out.println("gotcha " + param + " " + parts[0]);
				res.add(parts[0]);
			}
		}
		// only if all parameters are annotated!
		return res.toArray(new String[0]);
	}	

}

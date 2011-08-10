package org.jaywalk;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class Json extends HashMap<String,Object> {
	public static String sIndent = " ";
	
	private static void doIndent(StringBuffer b, String indent) {
		b.deleteCharAt(b.length()-1);
		b.append('\n');
		b.append(indent);
	}		

	private static String json(Object value, String indent) {
		if (indent != null)
			indent = indent + sIndent;
		Class<?> klass = value.getClass();
		if (value instanceof Map) {
			return asText((Map)value,indent);
		} else
		if (value instanceof Object[]) {
			return json((Object[])value,indent);
		} else
		if (value instanceof String) {
			return "\""+value+"\"";
		} else
		if (value == null) {
			return "null";
		} else {			
			return "\""+value+"\"";
		}		
	}
	

	public static String asText (Map<String,Object> map, String indent) {
		StringBuffer b = new StringBuffer();
		String sindent = null;
		b.append("{"); 
		if (indent != null) {			
			b.append('\n');	
			sindent = sIndent;
		}
		for (String key : map.keySet()) {
			Object value = map.get(key);
			if (indent != null)
				b.append(indent);
			b.append("\""+key+"\""+":");
			b.append(json(value,sindent));
			b.append(",");
			if (indent != null) 
				b.append('\n');
		}
		if (map.size() > 0)
			b.deleteCharAt(b.length()-1);
		if (indent != null) 
			doIndent(b,indent);		
		b.append("}");
		return b.toString();
	}
	

	private static String json (Object[] arr, String indent) {
		StringBuffer b = new StringBuffer();
		String sindent = null;
		b.append("[");
		if (indent != null) {
			b.append('\n');
			sindent = sIndent;
		}		
		for (Object o : arr) {
			if (indent != null)
				b.append(indent);
			b.append(json(o,sindent));
			b.append(",");
			if (indent != null)
				b.append('\n');			
		}
		if (arr.length > 0)
			b.deleteCharAt(b.length()-1);
		if (indent != null) 
			doIndent(b,indent);
		b.append("]");
		return b.toString();
	}
	
	public static Map<String,Object> parse(String s) throws IOException  {
		StreamTokenizer st = new StreamTokenizer(new StringReader(s));		
		int tok = st.nextToken();
		if ((char)tok != '{') return null;
		return parseMap(st);		
	}


	private static Map<String, Object> parseMap(StreamTokenizer st) throws IOException {
		Map<String,Object> res = new HashMap<String,Object>();
		int tok;
		String key;
		Object value;		
		tok = st.nextToken();
		while (tok != '}') {				
			if (tok != '"' && tok != '\'') return null;
			key = st.sval;
			tok = st.nextToken();
			if ((char)tok != ':') return null;
			value = parseValue(st);
			res.put(key, value);			
			tok = st.nextToken();
			if (tok == '}')
				break;
			tok = st.nextToken();
		}
		return res;
	}


	private static Object parseValue(StreamTokenizer st) throws IOException {
		int tok = st.nextToken();
		switch(tok) {
		case StreamTokenizer.TT_NUMBER:
			return st.nval;
		case StreamTokenizer.TT_WORD: {
			String word = st.sval;
			if (word.equals("true"))
				return true;
			else if (word.equals("false"))
				return false;
			else if (word.equals("null"))
				return null;
		} break;
		case '"':
		case '\'':
			return st.sval;
		case '[':
			return parseArray(st);
		case '{':
			return parseMap(st);
		default:
			return null;
		}
		return null;
	}


	private static Object parseArray(StreamTokenizer st) throws IOException {
		int tok = 0;
		List<Object> res = new ArrayList<Object>();
		while (tok != StreamTokenizer.TT_EOF) {
			Object value = parseValue(st); 
			res.add(value);
			tok = st.nextToken();
			if (tok == ']')
				break;
			//tok = st.nextToken();
		}
		return res.toArray(new Object[0]);
	}
	
	static String teststr = "{'one':1, 'two':[1,'hello',{'cope':null,'ok':true} ]}";
	
	public static void main(String[] args) {
		try {
			Map<String,Object> res = parse(teststr);
			System.out.println(asText(res," "));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}

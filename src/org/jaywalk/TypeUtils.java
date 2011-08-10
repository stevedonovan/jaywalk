package org.jaywalk;

public class TypeUtils {
	
	public static boolean isNumber(Class<?> c) {
		return Number.class.isAssignableFrom(c);
	}

	
	public static Class<?> convertPrimitiveType(Class<?> type) {
		if (type.equals(Integer.class)) return Integer.TYPE;
		if (type.equals(Long.class)) return Long.TYPE;
		if (type.equals(Double.class)) return Double.TYPE;
		if (type.equals(Float.class)) return Float.TYPE;
		if (type.equals(Boolean.class)) return Boolean.TYPE;
		if (type.equals(Character.class)) return Character.TYPE;
		if (type.equals(Byte.class)) return Byte.TYPE;
		if (type.equals(Void.class)) return Void.TYPE;
		if (type.equals(Short.class)) return Short.TYPE;
		return null;
	}
	
	public static double maximumIntegerValue(Class<?> type) {
		if (isNumber(type))
			type = convertPrimitiveType(type);
		if (type.equals(int.class)) return Integer.MAX_VALUE;
		if (type.equals(long.class)) return Long.MAX_VALUE;
		if (type.equals(double.class)) return Double.MAX_VALUE;
		if (type.equals(float.class)) return Float.MAX_VALUE;
		if (type.equals(short.class)) return Short.MAX_VALUE;
		if (type.equals(byte.class)) return Byte.MAX_VALUE;
		return 0;
	}
	
	public static Class<?> boxedNumberType (Class<?> type) {
		if (isNumber(type)) return type; // boxed already!
		if (type.equals(int.class)) return Integer.class;
		if (type.equals(long.class)) return Long.class;
		if (type.equals(double.class)) return Double.class;
		if (type.equals(float.class)) return Float.class;
		if (type.equals(short.class)) return Short.class;
		if (type.equals(byte.class)) return Byte.class;
		return null;
	}
	
	public static Object boxNumber (Class<?> type, double value) {
		if (isNumber(type))
			type = convertPrimitiveType(type);
		if (type.equals(int.class)) return new Integer((int)value);
		if (type.equals(long.class)) return new Long((long)value);
		if (type.equals(double.class)) return new Double(value);
		if (type.equals(float.class)) return new Float(value);
		if (type.equals(short.class)) return new Short((short)value);
		if (type.equals(byte.class)) return new Byte((byte)value);
		return null;
	}
	
	private static void error(String message) {
		throw new RuntimeException(message);
	}
	
	public static Object relaxedNumberConvert(Class<?> fieldType, Object obj) {
		Class<?> dataType = obj.getClass();
		if (isNumber(dataType)) { // assigning a number to a field			
			if (fieldType.isAssignableFrom(dataType)) return obj;
			// need to work using boxed types
			Class<?> boxFieldType = boxedNumberType(fieldType);
			if (boxFieldType != null) { // the field was a number
				double val = ((Number)obj).doubleValue();
				if (dataType.equals(Double.class)) {
					if (! boxFieldType.equals(Float.class)) {
						if (Math.ceil(val) != val)
							error("cannot assign fractional number to an integer");
					}
				}
				// the value must fit in the target type
				
				double max = maximumIntegerValue(fieldType);
				if (val >  max) 
					error("number value too large for this type: " + fieldType.toString());
				obj =  boxNumber(fieldType, val);
				// this almost works, but the Integer etc costructors are picky about their arguments
				//obj = boxFieldType.getConstructor(fieldType).newInstance(val);
			}
		}		
		return obj;
	}


}

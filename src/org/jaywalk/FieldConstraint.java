package org.jaywalk;

public interface FieldConstraint {
	public Object validate(Object object) throws IllegalArgumentException;
}

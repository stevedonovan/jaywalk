package org.jaywalk;
import javax.swing.JComponent;

public interface FieldConverter {
	public JComponent getComponent();
	public Object read(JComponent c, Class<?> type);
	public void write(JComponent c, Object value);

}

package org.jaywalk;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

class StringConverter implements FieldConverter {
	public JComponent getComponent() {
		return new JTextField();
	}
	
	public Object read(JComponent c, Class<?> type) {
		return ((JTextField)c).getText();
	}
	
	public void write(JComponent c, Object value) {
		((JTextField)c).setText(value.toString());
	}
}

class NumberConverter extends StringConverter {
	
	public Object read(JComponent c, Class<?> type) {
		String value = (String)super.read(c,type);
		double num = Double.parseDouble(value);
		return TypeUtils.relaxedNumberConvert(type, num);
	}
}

class BoolConverter implements FieldConverter {
	public JComponent getComponent() { return new JCheckBox(); }
	
	public Object read(JComponent c, Class<?> type) {
		return ((JCheckBox)c).isSelected();
	}
	
	public void write(JComponent c, Object value) {
		((JCheckBox)c).setSelected((Boolean)value);
	}
}

// The above converters work on a per-type basis, but some converters are particular to
// each object, since they represent particular constraints or need to track unique 
// internal data.

class FileConverter implements FieldConverter, FieldConstraint, ActionListener {
	FileNameExtensionFilter filter = null;
	boolean infile;
	File file;
	JTextField textField;
	
	public FileConverter (boolean infile) {
		this.infile = infile;
	}
	
	public FileConverter (boolean infile, String maskname, String... extensions) {
		this(infile);
		filter = new FileNameExtensionFilter(maskname, extensions);
	}

	public Object validate(Object object) throws IllegalArgumentException {
		return object;
	}

	public JComponent getComponent() {
		JPanel panel = new JPanel();
		textField = new JTextField(10); 
		panel.add(textField);
		JButton btn = new JButton("...");
		btn.addActionListener(this);
		panel.add(btn);
		return panel;
	}
	
	protected JTextField getTextField(JComponent c) {
		return (JTextField)c.getComponent(0);
	}

	public Object read(JComponent c, Class<?> type) {
		String fname = getTextField(c).getText();
		if (type.equals(File.class))
			return new File(fname);
		else
			return fname;
	}

	public void write(JComponent c, Object value) {
		getTextField(c).setText(value.toString());
	}

	public void actionPerformed(ActionEvent arg0) {
		JFileChooser chooser = new JFileChooser();
		if (filter != null)
			chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		int ret;
		ret = chooser.showOpenDialog(null);
//		ret = chooser.showDialog(arg0, arg1)
		if (ret == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
			textField.setText(file.getName());
		}
	}
	
}

class ListConverter implements FieldConverter, FieldConstraint {
	Object[] list;
	boolean isEditable;
	
	public ListConverter(Object[] _list, boolean _isEditable) {
		list = _list; isEditable = _isEditable;
	}
	
	public JComponent getComponent() {
		JComboBox box = new JComboBox(list);
		if (isEditable)
			box.setEditable(true);
		return box;
	}
	
	public Object read(JComponent c, Class<?> type) {
		Object val = ((JComboBox)c).getSelectedItem();
//		if (val == null) val = ((JComboBox)c).getText();
		return val;
	}
	
	public void write(JComponent c, Object value) {
		((JComboBox)c).setSelectedItem(value);
	}

	public Object validate(Object object) throws IllegalArgumentException {
		return object;
	}
}


public abstract class FieldConverterFactory {
	public JComponent getComponent() { return null; }
	public Object read(JComponent c, Class<?> type) { return null; }
	public void write(JComponent c, Object value) {}
	
	static Map<Class<?>,FieldConverter> map = new HashMap<Class<?>,FieldConverter>();
	
	static {
		map.put(String.class, new StringConverter());
		map.put(Number.class, new NumberConverter());
		map.put(boolean.class, new BoolConverter());
		map.put(File.class, new FileConverter(true));
	}
	
	public static FieldConverter get(Class<?> type) {
		// special cases: first, all numbers get the same converter
		Class<?> btype = TypeUtils.boxedNumberType(type);
		if (btype != null || TypeUtils.isNumber(type)) {
			type = Number.class;
		} else if (type.isEnum()) {
			// second, enumerations are done as a ListConverter 
			Object[] ls = type.getEnumConstants();
			return newListConverter(ls, false);
		}
		return map.get(type);
	}	
	
	public static void put (Class<?> type, FieldConverter fc) {
		map.put(type, fc);
	}
	
	public static FieldConverter newListConverter(Object[] ls, boolean isEditable) {
		return new ListConverter(ls,isEditable);
	}

	
	public static FieldConstraint newFileConverter(String maskname,	String[] extensions) {
		return new FileConverter(true,maskname,extensions);
	}

}

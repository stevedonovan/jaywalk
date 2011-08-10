import java.awt.*;

import javax.swing.*;

import org.jaywalk.FieldConstraint;
import org.jaywalk.FieldConverter;
import org.jaywalk.FieldConverterFactory;
import org.jaywalk.FieldMap;
import org.jaywalk.LayoutFrame;

import java.io.*;
import java.util.*;
import java.util.List;

enum Options {
	FIRST,
	SECOND,
	THIRD
}

class Frodo {
	public String surname = "baggins";
	public String name = "frodo";
	public int age = 40;
	public String relatives = "bilbo";
	public Options options = Options.FIRST;
	public File stuff = new File("baggins.txt");
}

class Range implements FieldConstraint {
	double minval,maxval;
	
	public Range (double minval, double maxval) {
		this.minval = minval;
		this.maxval = maxval;
	}

	public Object validate(Object object) throws IllegalArgumentException {
		Number n = (Number)object;
		double val = n.doubleValue();
		if (val < minval || val > maxval)
			throw new IllegalArgumentException("value is out of range " + minval + " to " + maxval);
		return object;
	}
	
}

class Row {
	public Row(String label, String var, Object def, FieldConstraint fc) {
		this.label = label;
		this.var = var;
		this.constraint = fc;
		this.def = def;
	}
	String label;
	String var;
	Object def;
	FieldConstraint constraint;
	JComponent control;
	FieldConverter converter;
	Class<?> type;
}

public class AutoFieldTest extends LayoutFrame {
	JPanel labelPane = vertPane();
	JPanel fieldPane = vertPane();
	List<Row> rows;
	Map<String,Object> map;
	
	protected static JPanel vertPane() {
		GridLayout layout = new GridLayout(0,1);
		layout.setVgap(10);
		return new JPanel(layout);
	}

	public AutoFieldTest(String caption, Object obj, Row... rows) {
		super(caption);
		if (Map.class.isAssignableFrom(obj.getClass()))
			map = (Map)obj;
		else
			map = new FieldMap(obj);
		if (rows.length == 0) {
			ArrayList<Row> rnew = new ArrayList<Row>();
			for (String var : map.keySet()) {
				rnew.add(R(var,var));
			}
			this.rows = rnew;
		} else {
			this.rows = Arrays.asList(rows); 
		}
		createLayout();
	}

	private void createLayout() {		
		for (Row row : rows) {
			Object value = map.get(row.var);
			if (value == null)
				value = row.def;
			if (value == null)
				throw new IllegalArgumentException("field has null value and no default specified");
			row.type = value.getClass();
			FieldConverter conv;
			if (row.constraint != null && row.constraint instanceof FieldConverter)
				conv = (FieldConverter)row.constraint;
			else
				conv = FieldConverterFactory.get(row.type);
			JComponent c = conv.getComponent();
			conv.write(c, value);
			labelPane.add(new JLabel(row.label));
			fieldPane.add(c);
			row.control = c;
			row.converter = conv;			
		}
		labelPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
		Container pane = panel(border(10),
				CENTER,labelPane,
				RIGHT,fieldPane);
		panel(FORM,
			CENTER,pane,
			BOTTOM,button("click me","extractFields")
		);
	}
	
	public void extractFields() {
		Map<String,Object> values = new LinkedHashMap<String,Object>();
		for (Row row : rows) {
			Object value;
			try {
				value = row.converter.read(row.control, row.type);
				if (row.constraint != null)
					value = row.constraint.validate(value);
				values.put(row.var, value);
			} catch(Exception e) {
				System.err.println("var "+row.var + " " + e);
				return;
			}			
		}
		for (String name : values.keySet()) {
			System.out.println("value for " + name + " is " + values.get(name));
		}
		map.putAll(values);
	}	
	

	public static Row R(String description, String var, Object def, FieldConstraint fc) {
		return new Row(description,var,def,fc);
	}
	
	public static Row R(String description, String var) {
		return R(description,var,null,null);
	}
	
	public static Row R(String description, String var, FieldConstraint fc) {
		return R(description,var,null,fc);
	}

	
	public static Row R(String description, String var, Object def) {
		return R(description,var,def,null);
	}
	
	public static Row R(String description, String var, boolean editable, Object... values) {
		return R(description,var,null,(FieldConstraint)FieldConverterFactory.newListConverter(values, editable));
	}
	
	public static FieldConstraint infile(String maskname, String...extensions) {
		return FieldConverterFactory.newFileConverter(maskname,extensions);
	}
	
	public static Range range (double start, double finish) {
		return new Range(start,finish);
	}
	

	public static void main(String[] args) {
		Frodo obj = new Frodo();
	//	/*
		AutoFieldTest me = new AutoFieldTest("testing fields",obj,
			R("Age","age", range(5,160)),
			R("Name","name"),
			R("Surname","surname"),
			R("Relatives","relatives",true,"julia","kino"),
			R("Options","options"),
			R("Dossier","stuff",infile("class files","class"))
		);
		// */
//		AutoFieldTest me = new AutoFieldTest("testing",obj);
		me.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		me.pack();
		me.setVisible(true);
	}

}

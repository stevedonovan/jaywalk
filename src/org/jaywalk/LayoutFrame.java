package org.jaywalk;
import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;

public class LayoutFrame extends JFrame implements ActionListener, MouseListener {
	Map<String,Method> methodMap = new HashMap<String,Method>();
	
	public enum Layout {
		TOP(BorderLayout.NORTH),
		BOTTOM(BorderLayout.SOUTH),
		LEFT(BorderLayout.WEST),
		RIGHT(BorderLayout.EAST),
		CENTER(BorderLayout.CENTER),
		ACROSS("_ACROSS_"),
		DOWN("_DOWN_"),
		FORM("_FORM_"),
		BORDER("_BORDER_");
		
		public String value;
		
		private Layout(String val) {
			value = val;
		}
	}
	
	public LayoutFrame(String caption) {
		super(caption);
	}
	
	public static final Layout TOP = Layout.TOP;
	public static final Layout BOTTOM = Layout.BOTTOM;
	public static final Layout LEFT = Layout.LEFT;
	public static final Layout RIGHT = Layout.RIGHT;
	public static final Layout CENTER = Layout.CENTER;
	public static final Layout ACROSS = Layout.ACROSS;
	public static final Layout DOWN = Layout.DOWN;
	public static final Layout FORM = Layout.FORM;
	public static final Layout BORDER = Layout.BORDER;	

	public static JLabel label(String l) {
		return new JLabel(l);
	}
	
	public JButton button(String l, String handler) {
		JButton btn = new JButton(l);
		try {
			addHandler(l,handler,btn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return btn;
	}
	
	int hspace = 5, vspace = 5;
	
	public void setSpacing(int h, int v) {
		hspace = h;
		vspace = v;
	}
	
	/**
	 * @param items
	 * @return
	 */
	public Container panel(Object... items) {
		int i = 0;
		Container p;
		if (items[0] == FORM) {
			p = this.getContentPane();
			++i;
		} else {
			p = new JPanel();			
		}
		p.setLayout(new BorderLayout());
		for (; i < items.length; i += 2) {
			Layout layout = (Layout)items[i];
			Component comp = (Component)items[i+1];
			p.add(comp,layout.value);			
		}
		return p;
	}
	
	public Container panel(Border b, Object...items) {
		return makeBorder(b,panel(items));
	}
	
	public Border border(Object value) {
		Border b = null;
		if (value instanceof Integer) {
			int space = (Integer)value;
			b = BorderFactory.createEmptyBorder(space, space, space, space);
		} else
		if (value instanceof Color) {
			b = BorderFactory.createLineBorder((Color)value);
		} else
		if (value instanceof String) {
			b = BorderFactory.createTitledBorder((String)value);
		}
		return b;
	}
	
	public Border border(Object value1, Object value2) {
		return new CompoundBorder(border(value1),border(value2));
	}

	private Container makeBorder(Border b, Container p) {
		((JComponent)p).setBorder(b);
		return p;
	}
	
	public Container splitter(Layout layout, Component first, Component second) {
		boolean across = layout == ACROSS;
		return new JSplitPane(across ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT,first,second);
	}	
	
	public Container splitter(Border b, Layout layout, Component first, Component second) {		
		return makeBorder(b,splitter(layout,first,second));
	}

	public Container scroll(Component inner) {
		JScrollPane jp = new JScrollPane(inner);
		jp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		return jp;
	}
	 
	private static Component createRigidArea (boolean across, int space) {
		return Box.createRigidArea(new Dimension(across ? space : 0, across ? 0 : space));
	}
	
	public Container box (Layout layout, Object... items) {
		boolean across = layout == ACROSS;
		Container p = new JPanel();
		p.setLayout(new BoxLayout(p, across ? BoxLayout.LINE_AXIS : BoxLayout.PAGE_AXIS));
		for (int i = 0; i < items.length; ++i) {
			Object item = items[i];
			boolean comp = false;
			if (item instanceof Integer) { // specifying some space between items
				int space = (Integer)item;
				p.add(createRigidArea(across,space));
			} else
			if (item instanceof String) {
				if (item.equals("..")) {
					p.add(createGlue(across));
				}
			}  else {
				comp = true;
				p.add((Component)item);
			}
			if (comp && i < items.length-1) {
				Object next = items[i+1];
				if (! (next instanceof Integer) && ! next.equals("..") ) {
					p.add(createRigidArea(across, across ? hspace : vspace));
				}				
			}
		}
		return p;
	}
	
	public Container box (Border b, Layout layout, Object ...items) {
		return makeBorder(b,box(layout,items));
	}
	
	public Container grid (int nrow, Component... items) {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(nrow,1,hspace,vspace));
		for (Component pi : items) {
			p.add(pi);
		}
		return p;
	}
	
	public Container grid (Border b, int nrow, Component... items) {
		return makeBorder(b,grid(nrow,items));
	}
	
	private static Component createGlue(boolean across) {
		if (across) {
			return Box.createHorizontalGlue();
		} else {
			return Box.createVerticalGlue();
		}
	}

	public void actionPerformed(ActionEvent evt) {
		String label = evt.getActionCommand();
		//System.out.println(label);
		Method m = methodMap.get(label);
		if (m != null) {
			try {
				m.invoke(this);
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}
	}
	protected void showMessage(String string) {
		JOptionPane.showMessageDialog(this, string, "Message", JOptionPane.INFORMATION_MESSAGE);
	}

	public static Object[] L(Object... objs) {
		return objs;
	}
	
	public JMenuBar addMainMenu(Object... items) {
		JMenuBar bar = new JMenuBar();	
		try {
			addMenuItems(bar,items);
		} catch(Exception e) {
			e.printStackTrace();
		}
		setJMenuBar(bar);
		return bar;
	}
	
	void addMenuItems(Object item, Object[] items) throws Exception {
		Class<?> ci = item.getClass();
		for (int i = 0; i < items.length; i += 2) {
			Object child = createMenuItem((String)items[i],items[i+1]); 
			if (child.equals("-sep-")) {
				((JMenu)item).addSeparator();
			} else {			
				Method add = ci.getMethod("add", child.getClass());
				add.invoke(item, child);
			}
		}
	}
	
	Object createMenuItem(String label, Object action) throws Exception {
		Object item = null;
		if (label.equals("-")) {
			item = "-sep-";
		} else {
			if (action instanceof String) {
				KeyStroke keyStroke = null;
				if (label.endsWith(")")) {
					int idx = label.indexOf("(");
					String sc = label.substring(idx+1,label.length()-1);
					label = label.substring(0,idx-1);
					keyStroke = KeyStroke.getKeyStroke(sc);
				//System.out.println("shortcut '" + sc + "'");
				}
				JMenuItem mitem = new JMenuItem(label);
				if (methodMap.get(label) != null) {
					throw new Exception("this menu caption used twice: " + label);
				}				
				addHandler(label, action, mitem);
				if (keyStroke != null) {
					mitem.setAccelerator(keyStroke);
				}
				item = mitem;				
			} else 
			if (action instanceof Object[]) {
				item = new JMenu(label);
				addMenuItems(item, (Object[])action);
			}
		}
		return item;
	}

	private void addHandler(String label, Object action, Component mitem)
			throws Exception {
		mitem.getClass().getMethod("addActionListener", ActionListener.class).invoke(mitem,this);
		Method m = this.getClass().getMethod((String)action, null);
		methodMap.put(label, m);
	}
	
	private Map<Component,Method> mouseClicked = new HashMap<Component,Method>();
	
	public void onMouseClicked(Component c, String method) throws Exception {
		mouseClicked.put(c,getClass().getMethod(method,MouseEvent.class));
		c.addMouseListener(this);
	}


	public void mouseClicked(MouseEvent evt) {
		Component c = evt.getComponent();
		Method m = mouseClicked.get(c);
		if (m != null) {
			try { m.invoke(this, evt); }
			catch (Exception e) { }			
		}
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}
	
	private Map<Component,JPopupMenu> menuMap = new HashMap<Component,JPopupMenu>();
	
	public void addPopupMenu(Component c, Object...objects) {
		JPopupMenu popup = new JPopupMenu();
		try {
			addMenuItems(popup,objects);
		} catch (Exception e) {
			e.printStackTrace();
		}
		menuMap.put(c, popup);
		c.addMouseListener(this);
	}
	
	private void checkPopup(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			Component c = evt.getComponent();
			JPopupMenu popup = menuMap.get(c);
			if (popup != null)
				popup.show(c, evt.getX(),evt.getY());			
		}
	}	


	public void mousePressed(MouseEvent evt) {
		//System.out.println("pressed! " + evt.isPopupTrigger());
		checkPopup(evt);
	}



	public void mouseReleased(MouseEvent evt) {
		checkPopup(evt);
	}
	
}

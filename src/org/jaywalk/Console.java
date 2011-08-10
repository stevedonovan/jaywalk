package org.jaywalk;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.util.*;
import java.util.List;

import java.awt.event.*;
import java.awt.*;

public class Console extends JFrame implements KeyListener, Runnable,Displayer,Evaluator, ActionListener {
	JTextArea text = new JTextArea();
	
	public void display (String s) {
		text.append(s);
	}
	
	static void dump(String s) {
		System.out.println(s);
	}
	
	private int startP, endP, lineNo;
	
	private boolean getCurrentLineRange() {
		int lineNo = text.getLineCount()-1;
		try {
			startP = text.getLineStartOffset(lineNo);
			endP = text.getLineEndOffset(lineNo);
//			dump(startP+" "+endP);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	private void replaceRange(String line) {
		text.replaceRange(line, startP, endP);
	}
	
	private String getCurrentLine() {
		if (getCurrentLineRange()) {
			try {
				return text.getDocument().getText(startP, endP-startP);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	List<String> history = new ArrayList<String>();
	int history_idx = 0;
	
	private class HistoryKeyHandler extends AbstractAction {
		private String op;
		
		HistoryKeyHandler(String name) {
			op = name;
		}

		public void actionPerformed(ActionEvent evt) {
			if (op.equals("tab")) {
				String line = getCurrentLine();
				if (line.endsWith("arg")) {
					text.replaceRange("argument",endP-3,endP);
				}
				dump(line);
				return;
			}
			dump("here?");
			int delta = 0;
			if (op.equals("up")) {
				delta = -1;
			} else 
			if (op.equals("down")) {
				delta = 1;
			}			
			history_idx += delta;
			String line = history.get(history_idx);
			if (line == null) {
				history_idx -= delta;
				return;
			}
			getCurrentLineRange();
			replaceRange(line);			
		}		
	}
	
	void bindKey(JComponent c, int key, int mod, String name) {
		KeyStroke ks = KeyStroke.getKeyStroke(key,mod);
		c.getInputMap().put(ks, name);
		c.getActionMap().put(name, new HistoryKeyHandler(name));
	}
	
	String prompt;
	private Evaluator evaluator = null;
	
	public void setEvaluator(Evaluator e) {
		evaluator = e;
	}
	
	public Console(String caption, String prompt_, Evaluator eval) {
		super(caption);
		prompt = prompt_;
		evaluator = eval;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Font font = new Font(Font.DIALOG,Font.BOLD,12);
		text.setFont(font);		
		text.addKeyListener(this);
		bindKey(text,KeyEvent.VK_UP,0,"up");
		bindKey(text,KeyEvent.VK_DOWN,0,"down");
		bindKey(text,KeyEvent.VK_TAB,0,"tab");
//		text.setBackground(Color.gray);
//		text.setForeground(Color.blue);
		JScrollPane pane = new JScrollPane(text);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(pane);
		pack();
		display(prompt);
		text.setCaretPosition(text.getDocument().getLength());
		setSize(400,400);
	}
	
	public Console(String caption, String prompt) {
		this(caption,prompt,null);
	}

	
	private String line;

	public void keyPressed(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
			line = getCurrentLine();
			SwingUtilities.invokeLater(this);		
		}
	}
	
	//(from Runnable) this will be called when we are out of the key handler
	// and ready to generate output 
	public void run() {
		if (line != null) {
			history.add(line);
			history_idx = history.size();
			if (line.startsWith(prompt))
				line = line.replace(prompt, "");
			boolean res;
			if (evaluator != null)
				res = evaluator.eval(line);
			else
				res = eval(line);
			display(prompt);
		}
	}
	// (from ActionListener)
	public void actionPerformed(ActionEvent evt) {
		dump(evt.getActionCommand());
	}	

	public void keyReleased(KeyEvent arg0) {}

	public void keyTyped(KeyEvent arg0) {}
	
	public static void main(String[] args) {
		Console me = new Console("Console Test","> ",null);
		me.setVisible(true);
	}

	public boolean eval(String line) {
		display(line.toUpperCase()+"\n");
		return true;
	}


}

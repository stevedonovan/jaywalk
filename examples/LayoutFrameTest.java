import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.jaywalk.LayoutFrame;


public class LayoutFrameTest extends LayoutFrame {
	
	JTextArea area = new JTextArea();
	JTree tree = new JTree();
	
	public void open() {
		showMessage("open!");
	}
	
	public void new_() {
		showMessage("new!");
	}
	
	public void copy() {
		
	}
	
	public void paste() {
		
	}
	
	public void mouseEdit(MouseEvent evt) {
		area.append("clicked!\n");
	}

	
	public LayoutFrameTest(String string) throws Exception {
		super(string);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addMainMenu(
			"File", L(
				"Open (shift ctrl O)","open",
				"New","new_"
			),
			"Edit",L(
				"Copy","copy",
				"-","-",
				"Paste","paste"
			)
		);
		
		addPopupMenu(area,
			"Open Doc","open",
			"New Doc","new_"
		);
		
		onMouseClicked(area,"mouseEdit");
		
		/*
		panel(
			BORDER,16,
			TOP,vbox(
					hbox(BORDER,"Hello Dolly",button("hello","open"),label("hah"),"..",button("phew!","open")),
					grid(2,label("one"),button("one","open"),label("two"),button("two","open"))
			),
			CENTER,hsplitter(scroll(tree),scroll(area)),			
			BOTTOM,hbox(BORDER,Color.red,label("fine!"),label("ok"))
		);
		*/
		panel(border(16),FORM,
			CENTER,splitter(border("Results"),ACROSS,scroll(tree),scroll(area)),			
			BOTTOM,box(border(Color.red,3),ACROSS,label("fine!"),"..",label("ok"))
		);
	}


	public static void main(String[] args) {		
		try {
			LayoutFrameTest me = new LayoutFrameTest("Menu Demo");
			me.pack();
			me.setSize(400,400);
			me.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

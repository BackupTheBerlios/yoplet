package org.yoplet.graphic;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextOutputPanel extends JPanel implements Outputable {

	 // A Swing textarea for display of string info
	  JTextArea fTextArea = null;

	  public TextOutputPanel () {
	    // A BorderLayout would be more appropriate here but
	    // it isn't discussed until chapter 7.
	    setLayout (new GridLayout (1,1));

	    // Create an instance of JTextArea
	    fTextArea = new JTextArea ();
	    fTextArea.setEditable (false);

	    // Add to a scroll pane so that a long list of
	    // computations can be seen.
	    JScrollPane area_scroll_pane = new JScrollPane (fTextArea);
	    
	    add (area_scroll_pane);
	  }

	  /** Display a string + carriage return on the JTextArea. **/
	  public void println(String str) {
	    fTextArea.append(str + CR);
	    fTextArea.setCaretPosition(fTextArea.getDocument().getLength());
	  }

	  /** Dispaly a string on the JTextArea. **/
	  public void print(String str) {
	    fTextArea.append(str);
	    fTextArea.setCaretPosition(fTextArea.getDocument().getLength());
	  }
}

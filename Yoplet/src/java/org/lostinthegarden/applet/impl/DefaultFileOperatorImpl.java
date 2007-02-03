package org.lostinthegarden.applet.impl;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.TextArea;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JApplet;

import org.lostinthegarden.applet.FileOperator;
import org.lostinthegarden.applet.graphic.Outputable;
import org.lostinthegarden.applet.graphic.impl.TextOutputPanel;

public class DefaultFileOperatorImpl extends JApplet implements FileOperator {
	
	private StringBuffer buffer = null;
	private String 		url		= null;
	private File 		in 		= null;
	private File 		out  	= null;
	private boolean		debug	= false;
	private Outputable output	= null;
	
	// UI stuff
	private TextArea 	texta 	= null;
	
	public DefaultFileOperatorImpl() {
		super();
	}

	public void performRead() {
		// TODO Auto-generated method stub
		
	}

	public void performRequest() {
		HttpURLConnection cnx = null;
		try
		{
			//FileInputStream stream = new FileInputStream(this.in);
			URL url = new URL(this.url);
			cnx = (HttpURLConnection)url.openConnection();
			addItem("connexion prepared...");
			cnx.connect();
			addItem("request performed..." + cnx.getResponseMessage());
//			OutputStreamWriter osw =  new OutputStreamWriter(cnx.getOutputStream());
//			osw.flush();
//			osw.close();
			BufferedReader reader = new BufferedReader(new InputStreamReader(cnx.getInputStream()));
			String line = null;
			
			while ((line = reader.readLine()) != null)
			{
			        addItem(line);
			}
			reader.close();
			
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		finally
		{
			if (null != cnx)
			{
				cnx.disconnect();
			}
		}
	}

	public void performWrite() {
		// TODO Auto-generated method stub
		
	}

	public void setDestFile(String path) {
		if (null == out)
		{
			this.out = new File(path);
		}
	}

	public void setSourceFile(String path) {
		if (null == in)
		{
			this.in = new File(path);
		}
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public void init() {
		super.init();
		Container contentPane = getContentPane ();
	    // Create an instance of the panel subclass
		// with JTextArea to show output
		output = new TextOutputPanel ();
	    // Add the panel to the applet's pane
	    contentPane.add ((TextOutputPanel)output);
	    
		this.buffer = new StringBuffer();
		setSourceFile(getParameter(FileOperator.SOURCE_FILE));
		setDestFile(getParameter(FileOperator.DEST_FILE));
		setDebug(Boolean.getBoolean(getParameter(FileOperator.DEBUG)));
		setUrl(getParameter(FileOperator.URL));
		addItem("init..");
	}
	
	
	public void start() {
		super.start();
		addItem("start...");
		performRequest();		
	}
	

    private void addItem(String newWord) {
    	output.println(newWord);
    }    
}

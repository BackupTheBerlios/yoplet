package org.lostinthegarden.applet.impl;

import java.awt.Container;
import java.awt.TextArea;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JApplet;

import org.lostinthegarden.applet.FileOperator;
import org.lostinthegarden.applet.graphic.Outputable;
import org.lostinthegarden.applet.graphic.impl.TextOutputPanel;

public class DefaultFileOperatorImpl extends JApplet implements FileOperator {
	private String       action	= null;

	private File         readFile	= null;
	private File         writeFile = null;
	private File         watchFile = null;
	
	private String       url		= null;
    private boolean     debug		= false;
    private Outputable   output	= null;
    private String       content	= null;
    
    public DefaultFileOperatorImpl() {
        super();
    }

    private void assertNotNull(Object object, String comment) throws Exception {
    	if (null == object) {
    		throw new Exception(comment);
    	}
    }
    private File createParamFile(String paramPath) {
    	String path = getParameter(paramPath);
		return  path != null ? new File(path) : null;
    }
    
    public void performRequest() {
        HttpURLConnection cnx = null;
        try         {
            //FileInputStream stream = new FileInputStream(this.in);
            URL url = new URL(this.url);
            cnx = (HttpURLConnection)url.openConnection();
            this.trace("connexion prepared");
            cnx.connect();
            this.trace(cnx.getResponseMessage(), "request performed");
//            OutputStreamWriter osw =  new OutputStreamWriter(cnx.getOutputStream());
//            osw.flush();
//            osw.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(cnx.getInputStream()));
            String line = null;
            
            int i = 0;
            while ((line = reader.readLine()) != null) {
            i++;
            }
            reader.close();
            this.trace(i, "Lines Read");
            
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

    public void performRead() {
        this.trace("Reading");

        try {
        	this.assertNotNull(this.readFile, "Read local path undefined");
            this.trace(this.readFile.toString(), "From local path");
		} 
        catch (Exception e) {
            this.trace("Warning: " + e.getMessage());
            
		}
        finally {
        }
    }

    public void performWrite() {
        this.trace(this.content, "Writing");


        PrintWriter writer = null;
        try {
        	this.assertNotNull(this.writeFile, "Write local path undefined");
            this.trace(this.writeFile.toString(), "To local path");
            this.writeFile.createNewFile();
            writer = new PrintWriter(new BufferedWriter(new FileWriter(this.writeFile)));
			writer.print(this.content);

        } 
        catch (Exception e) {
            this.trace("Warning: " + e.getMessage());
        } 
        finally {
            if (null != writer) {
                writer.flush();
                writer.close();
            }
        }
    }

	public void performWatch() {
        this.trace("Watching");

        try {
        	this.assertNotNull(this.watchFile, "Watch local path undefined");
            this.trace(this.watchFile.toString(), "for local path");

        } 
        catch (Exception e) {
            this.trace("Warning: " + e.getMessage());
        } 
        finally {
        }
	}    
        
    public void init() {
        super.init();
        
        // UI initialisation
        Container contentPane = getContentPane ();
        this.output = new TextOutputPanel();
        contentPane.add ((TextOutputPanel) this.output);
        
        this.trace("Initiliazing applet");

        // Data initialisation
        this.action = getParameter(FileOperator.ACTION);

        this.readFile = createParamFile(FileOperator.READ_PATH);
        this.writeFile = createParamFile(FileOperator.WRITE_PATH);
        this.watchFile = createParamFile(FileOperator.WATCH_PATH);

        this.debug = new Boolean(getParameter(FileOperator.DEBUG)).booleanValue();
        this.url = getParameter(FileOperator.URL);
        this.content = getParameter(FileOperator.CONTENT);
    }
    
    
    public void start() {
        super.start();
        
        try {
            this.trace("Starting applet");
            this.trace(this.action, "Action type");
            
            if (this.action.equals(FileOperator.ACTION_READ)) {
    			this.performRead();
            } 
            
            if (this.action.equals(FileOperator.ACTION_WRITE)) {
                this.trace("Applet writing");
    			this.performWrite();
            }
            
            if (this.action.equals(FileOperator.ACTION_WATCH)) {
                this.trace("Applet watching");
    			this.performWatch();
            } 

            this.trace("Applet now sleeping");
		} 
        catch (Exception e) {
			// TODO: handle exception
		}

    }
    

    private void trace(String string) {
        if (this.debug) {
            output.println(">  " + string + "...");
        }
	}

	private void trace(int value, String key) {
        if (this.debug) {
            output.println(">>  " + key + " = " + value);
        }
    }

	private void trace(String value, String key) {
        if (this.debug) {
            output.println(">>  " + key + " :");
            output.println(value);
        }
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}

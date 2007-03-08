package org.yoplet;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;

import javax.swing.JApplet;

import org.yoplet.graphic.Outputable;
import org.yoplet.graphic.TextOutputPanel;

public class Yoplet extends JApplet implements FileOperator {
	
	public void update(Observable o, Object arg) {
	
	}

	private String       action	= null;

	private File         readFile	= null;
	private File         writeFile 	= null;
	private File         watchFile 	= null;
	
	private String       url		= null;
    private boolean     debug		= false;
    private Outputable   output		= null;
    private String      content		= null;

    // javascript handle props
    private boolean jReadCall		= false;
    private boolean jWriteCall		= false;
    private boolean jWatchCall		= false;    
    
    Watcher watcher = null;
    
    Thread javascriptListener = new Thread() {

	  public void run() {
	
	    while (true) {
	
	    	if (jReadCall) {
	    		jReadCall = false;
	    		readFile();
		    }
	    	else if (jWriteCall) {
	    		jWriteCall = false;
	    		writeFile();
	    	}
	    	else if (jWatchCall) {
	    		jWatchCall = false;
	    		watchFile();
	    	}
		    try {
		    	sleep(30);
		    }
		    catch (Throwable t) {
		    	t.printStackTrace();
		    }
	    }
	  }

    };
    
    public Yoplet() {
        super();
    }

    private void assertNotNull(Object object, String comment) throws Exception {
    	if (null == object) {
    		throw new Exception(comment);
    	}
    }
    
    private String[] readData(File in) throws Exception{
    	String line = null;
    	Collection lines = new ArrayList();
		BufferedReader reader =  new BufferedReader(new FileReader(this.watchFile));
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
    	return (String[])lines.toArray(new String[]{});
    }
    
    /**
     * File creation method
     * @param paramPath input path
     * @return corresponding File
     */
    private File createParamFile(String paramPath) {
    	String path = getParameter(paramPath);
		return  path != null ? new File(path) : null;
    }
    
    /**
     * Request perform method 
     */
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
    
    /**
     * Read Operation 
     */
    public void performRead() {
    	this.jReadCall = true;
    }
    
    private void readFile() {
        this.trace("Reading");
        try {
        	this.assertNotNull(this.readFile, "Read local path undefined");
            this.trace(this.readFile.getAbsolutePath(), "From local path");
            String[] data = readData(this.readFile);
		} 
        catch (Exception e) {
            this.trace("Warning: " + e.getMessage());
            
		}
        finally {
        }    	
    }

    /**
     * Write operation
     */
    public void performWrite() {
    	jWriteCall = true;
    }
    
    
    /**
     * 
     */
    private void writeFile() {
        this.trace(this.content, "Writing");

        PrintWriter writer = null;
        try {
        	this.assertNotNull(this.writeFile, "Write local path undefined");
            this.trace(this.writeFile.getAbsolutePath(), "To local path");
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
		this.jWatchCall = true;
	}
	
	private void watchFile(){
        this.trace("Watching");
        if (null == this.watcher) this.watcher = new Watcher(this.watchFile,this,2000);
        this.watcher.start();
        while (true) {
        	try
        	{
        		synchronized (this) {
        			wait();
        			break;
        		}
        	} 
        	catch (InterruptedException e) {
        		e.printStackTrace();
        	}
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
        
        this.watcher = new Watcher(this.watchFile,this,2000);
        
        if (null != this.javascriptListener) {
        		this.javascriptListener.start();
        }
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
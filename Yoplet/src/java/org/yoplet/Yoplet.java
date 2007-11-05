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
import java.util.Observable;

import javax.swing.JApplet;

import org.yoplet.graphic.Outputable;
import org.yoplet.graphic.TextOutputPanel;

public class Yoplet extends JApplet implements FileOperator {
    
    public void update(Observable o, Object arg) {
    
    }

    private String     action = null;

    private File       file = null;
    private File       flag = null;
    
    private String     content    = null;
    private String     lineSeparator = null;

    private String     url        = null;
    private boolean   debug    = false;
    private Outputable output    = null;

    // javascript handle props
    private boolean jReadCall  = false;
    private boolean jWriteCall = false;
    private boolean jWatchCall = false;    
 
    private Watcher  watcher = null;
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
                watcher = new Watcher(file,Yoplet.this,2000);
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
    
    private void readData(File in) throws Exception {
        String line = null;
        this.content = "";
        BufferedReader reader =  new BufferedReader(new FileReader(this.file));
        while ((line = reader.readLine()) != null) {
        	this.content += line + this.lineSeparator;
        }
    }
    
    /**
     * File creation method
     * @param paramPath input path
     * @return corresponding File
     */
    private File createParamFile(String paramPath) {
        String path = getParameter(paramPath);
        this.trace(path, "File for " + paramPath);
        return  path != null ? new File(path) : null;
    }
    
    /**
     * Request perform method 
     */
    public String performRequest() {
        HttpURLConnection cnx = null;
        try         {
            //FileInputStream stream = new FileInputStream(this.in);
            URL url = new URL(this.url);
            cnx = (HttpURLConnection)url.openConnection();
            this.trace("connexion prepared");
            cnx.connect();
            this.trace(cnx.getResponseMessage(), "request performed");
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
        
        return "OK";
    }
    
    /**
     * Read Operation 
     */
    public String performRead() {
        this.jReadCall = true;
        String res = "OK";
        return res;
    }
    
    private void readFile() {
        this.trace("Reading up");
        try {
            this.assertNotNull(this.file, "Read local path undefined");

            // Check for flag file if needed
            if (null != this.flag) {
                this.trace(this.flag.getAbsolutePath(), "Should check flag file");
                if (!this.flag.exists()) {
                	this.content = "";
                	this.trace("Sorry, no flag file");
                	return;
                }
            }
            
            this.trace(this.file.getAbsolutePath(), "From local path");
            this.readData(this.file);
            this.trace(this.content, "Content Read");

            // Remove both read and flag files if flag file is provided
            if (null != this.flag) {
                this.trace("Also removes the flag and read files");
                this.flag.delete();
                this.file.delete();
            }
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
    public String performWrite(String content) {
    	this.content = content;
        this.jWriteCall = true;
        return "OK";
    }
    
    
    /**
     * 
     */
    private void writeFile() {
        this.trace(this.content, "Writing down");

        PrintWriter writer = null;
        try {
        	// Write the file
            this.assertNotNull(this.file, "Write local path undefined");
            this.trace(this.file.getAbsolutePath(), "To local path");
            this.file.createNewFile();
            writer = new PrintWriter(new BufferedWriter(new FileWriter(this.file)));

            // Print lines
            if (null != this.lineSeparator) {
                String[] lines = this.content.split(this.lineSeparator);
                for (int i = 0; i < lines.length;i++) {
                    writer.println(lines[i]);
                }
            }
            else {
                writer.println(this.content);
            }
            
            // Create a flag file if necessary
            if (null != this.flag) {
                this.flag.createNewFile();
                this.trace(this.flag.getAbsolutePath(), "Also created the flag file");
            }
            
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
    
    
    public String performWatch() {
        this.jWatchCall = true;
        String res = "KO";
        while (true) {
            if (null != watcher && null != watcher.getWatcherReturn()) {
            	res = "OK";
            	break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }
    
    private void watchFile(){
        this.trace("Watching");
        System.out.println("Watcher initialized " + (null == this.watcher));
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
        Container contentPane = getContentPane();
        this.output = new TextOutputPanel();
        contentPane.add ((TextOutputPanel) this.output);


        // Data initialisation
        this.debug = new Boolean(getParameter(FileOperator.DEBUG)).booleanValue();
        
        contentPane.setVisible(debug);
        
        this.action = getParameter(FileOperator.ACTION);

        this.file = createParamFile(FileOperator.FILE_PATH);
        this.flag = createParamFile(FileOperator.FLAG_PATH);

        this.url = getParameter(FileOperator.URL);
        this.content = getParameter(FileOperator.CONTENT);
        this.lineSeparator = getParameter(FileOperator.LINE_SEPERATOR);
        
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
                this.performWrite(this.content);
            }
            
            if (this.action.equals(FileOperator.ACTION_WATCH)) {
                this.trace("Applet watching");
                this.performWatch();
            } 

            this.trace("Applet now sleeping");
        } 
        catch (Exception e) {
            this.trace("Error starting applet: " + e.getMessage());
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

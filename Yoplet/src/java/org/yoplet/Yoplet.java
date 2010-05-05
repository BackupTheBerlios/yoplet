package org.yoplet;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;

import javax.swing.JApplet;
import javax.swing.JFileChooser;

import netscape.javascript.JSObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.Representation;
import org.yoplet.graphic.Outputable;
import org.yoplet.graphic.TextOutputPanel;
import org.yoplet.json.JSONArray;
import org.yoplet.json.JSONException;
import org.yoplet.json.JSONObject;

public class Yoplet extends JApplet implements FileOperator {
	public final static String RETURN_OK = "OK";
	public final static String RETURN_KO = "KO";
    
    public void update(Observable o, Object arg) {}

    private String     action = null;

    private File       file = null;
    private File       flag = null;
    
    private String     content       = null;
    
    private String      uploadErrors    = null;
    private String	     pattern         = null;
    private String      rename          = null;
    private int         count           = 0;
    private int         uploadedCount   = 0;
    private boolean     deleteFlag      = false;
    
    private String     lineSeparator    = null;

    private String     url        = null;
    private String     getParams  = null;
    private String     postParams = null;
    private boolean    debug      = false;
    private Outputable output     = null;
    
    // File listing part
    private boolean recursive       = false;
    private String listPath         = null;
    
    //callback part
    private String callbackmethod   = "appletCallBack";

    // javascript handle props
    private boolean jListCall       = false;
    private boolean jReadCall       = false;
    private boolean jWriteCall      = false;
    private boolean jCountCall      = false;
    private boolean jUploadCall     = false;
    private boolean jChooseRoot     = false;
    
    private JFileChooser jfilechoose = null;
    
    // upload operation
    private Client _client          = null;
    private Set uploadqueue = new HashSet();
 
    Runnable javascriptListener = new Runnable() {
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
	            else if (jCountCall) {
	            	jCountCall = false;
	                countFile();
	            }
	            else if (jUploadCall) {
	            	jUploadCall = false;
	                uploadFiles();
	            } else  if (jListCall) {
	                jListCall = false;
	                listFiles();
	            } else if (jChooseRoot) {
	                chooseRoot();
	            }
	            try {
	                Thread.sleep(30);
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
	
    public void init() {
        super.init();
        
       
        // Data initialisation
        this.debug = Boolean.parseBoolean(getParameter(FileOperator.DEBUG));
        
        System.out.println("Debug option" + debug);
        
        if (this.debug) {
            // UI initialisation
            Container contentPane = getContentPane();
            this.output = new TextOutputPanel();
            contentPane.add ((TextOutputPanel) this.output);
            contentPane.setVisible(true);
        }
        
        this.action = getParameter(FileOperator.ACTION);

        this.file = createParamFile(FileOperator.FILE_PATH);
        this.flag = createParamFile(FileOperator.FLAG_PATH);
        
        this.pattern = getParameter(FileOperator.FILE_PATTERN);
        this.rename = getParameter(FileOperator.RENAME);
        this.deleteFlag = Boolean.parseBoolean(getParameter(FileOperator.DELETE_FLAG));

        this.url = getParameter(FileOperator.URL);
        this.getParams = getParameter(FileOperator.GET_PARAMS);
        this.postParams = getParameter(FileOperator.POST_PARAMS);
        this.content = getParameter(FileOperator.CONTENT);
        this.lineSeparator = getParameter(FileOperator.LINE_SEPERATOR);
        
        String cbmethod = getParameter("callbackmethod");
        
        if (null != cbmethod) {
            this.callbackmethod = cbmethod; 
        }
        
        if (null != this.javascriptListener) {
            this.javascriptListener.run();
        }
        
        Operation op = new Operation("init",null);
        callback(new String[]{new JSONObject(op).toString()});
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
        try {
            while ((line = reader.readLine()) != null) {
            	this.content += line + this.lineSeparator;
            }
        } 
        catch (Throwable t) {
        	throw new Exception(t);
        }
        finally {
        	if (null != reader) {
        		try {
        			reader.close();
        		}
        		catch (Throwable t) {
        			this.trace(t.getMessage());
        		}
        	}
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
     * Search File Method, triggered by js
     */
    private void listFiles() {
        File root = new File(this.listPath);
        if (root.exists() && root.isDirectory()) {
            // lets look for file
            Collection files = FileUtils.listFiles(root, null, this.recursive);
            String[] res = new String[files.size()];
            int i= 0;
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                File f = (File) iterator.next();
                res[i] = f.getAbsolutePath(); 
                i++;
            }
            trace("Done with file listing  " + files.toString());
            Operation op = new Operation("listfiles",res);
            callback(new String[]{new JSONObject(op).toString()});
        } 
        this.listPath = null;
    }
    
    private File[] filterFile() {
    	if(this.file.exists() && this.file.isDirectory()) {
	    	FileFilter fileFilter = new WildcardFileFilter(this.pattern);
	    	return this.file.listFiles(fileFilter);
    	}
    	this.trace(this.file.getPath(), "File doesn't exist or isn't a directory ");
    	return null;
    }
    
    private void uploadFile(File file, String fileName) throws MalformedURLException {
    	if (checkJava5()) {
    	    Client client = null;
    	    try {
    	    trace("Check Java version " + System.getProperty("java.version"));
    	    java.net.URL u = new java.net.URL(this.url);
    	    String p = u.getProtocol();
    	    Protocol protoc = Protocol.valueOf(p);
    	    
    	    client = new Client(protoc);
    	    client.start();
            Reference baseRef = new Reference(Protocol.HTTP,u.getHost(),u.getPort());
            Reference resource = new Reference(baseRef,u.getPath());
            resource.addQueryParameter("filename", fileName).addQueryParameter("originalname", file.getAbsolutePath());
            FileRepresentation f = new FileRepresentation(file,MediaType.IMAGE_PNG);
            Response response = client.post(resource, f);
            
            trace("Status",""+response.getStatus()+"  vs " +Status.SUCCESS_OK.getCode());
            Representation rep = response.getEntity();
            
            if (Status.SUCCESS_OK.equals(response.getStatus())) {
                callback(new Object[]{new JSONObject(new Operation("uploadok",new String[]{file.getAbsolutePath()})).toString()});
            } else {
                callback(new Object[]{new JSONObject(new Operation("uploadko",new String[]{file.getAbsolutePath()})).toString()});
            }

                client.stop();
            } catch(Exception e) {
                trace(e.getMessage());
            } finally {
                client = null;
            }
    	} else {
    	    trace("Could not perform upload");
    	}
    }
    
    /**
     * Read perform method 
     */
    public String performRead() {
        this.jReadCall = true;
        return Yoplet.RETURN_OK;
    }
    
    /**
     * Write perform method
     */
    public String performWrite(String content) {
    	this.content = content;
        this.jWriteCall = true;
        return Yoplet.RETURN_OK;
    }
    
    /**
     * Exist perform method 
     */
    public String performCount() {
    	this.jCountCall = true;
        return Yoplet.RETURN_OK;
    }
    
    /**
     * Upload perform method 
     */
    public String performUpload(String rename, String files) {
        if (!jUploadCall) {
            try {
            	this.rename = rename;
            	JSONArray jsfiles = new JSONArray(files);
            	this.uploadqueue.clear();
            	for (int i = 0; i < jsfiles.length(); i++) {
            	    String file = jsfiles.getString(i);
                    this.uploadqueue.add(file);
                }
            	this.jUploadCall = true;
                return Yoplet.RETURN_OK;
            } catch (JSONException jse) {
                trace(jse.getMessage());
                return Yoplet.RETURN_KO;
            }
        } else {
            return Yoplet.RETURN_KO;
        }
        
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
                this.file.delete();
                this.flag.delete();
            }
        } 
        catch (Exception e) {
            this.trace("Warning: " + e.getMessage());
        }
    }
    
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
        		try {
                    writer.flush();
                    writer.close();
        		}
        		catch (Exception t) {
        			this.trace(t.getMessage());
        		}
        	}
        }
    }
    
    private void chooseRoot() {
        if (null == this.jfilechoose) {
            this.jfilechoose = new  JFileChooser();
        }
        this.jfilechoose.setMultiSelectionEnabled(false);
        jfilechoose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int choice = jfilechoose.showDialog(this, "OK");
        if (choice == JFileChooser.APPROVE_OPTION) {
            File f = jfilechoose.getSelectedFile();
            Operation op = new Operation("choosefile",new String[]{f.getAbsolutePath()});
            this.callback(new String[]{new JSONObject(op).toString()});
        }
        this.jChooseRoot = false;
    }
    
    private void countFile() {
    	this.count = 0;
    	File[] files = this.filterFile();
    	if(files != null)
    		this.count = files.length;
    	this.trace(this.count, " files found to upload");
    }
    
    private void uploadFiles() {
    	int  j=0;
    	System.out.println("Dealing with "+ this.uploadqueue.size() + " elements to be uploaded");
    	for (Iterator iterator = this.uploadqueue.iterator(); iterator.hasNext();) {
            String path = (String) iterator.next();
            System.out.println("Handling " + path +  "upload");
            File file = null;
            boolean rename = (this.rename != null && this.rename.length() > 0); 
    		try {
    		    file = new File(path);
    			// Define name of the part for rename files on server side
    			String fileName = FilenameUtils.getBaseName(file.getName());

				fileName = rename ? this.rename + j : "upload";
				this.trace("Define part name to " + fileName);
				
    			// lets check file exists
				if (file.exists() && !file.isDirectory()) {
				    this.uploadFile(file, fileName);
				} else {
				    System.out.println("Cannot upload directory or non existing file -- skipping");
				}
				
    			trace("Upload ok for file " + fileName);
    			j++;
			} catch (Exception e) {
			    StringBuffer sb = new StringBuffer(50);
				sb.append(file.getName());
				sb.append(" isn't uploaded : ");
				sb.append(e.getMessage());
				sb.append("\r\n");
				trace(sb.toString());
			}
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
            
            if (this.action.equals(FileOperator.ACTION_COUNT)) {
                this.trace("Applet test existing");
                this.performCount();
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
            getAppletContext().showStatus(string);
        }
    }

    private void trace(int value, String key) {
        if (this.debug) {
            output.println(">>  " + key + " = " + value);
            getAppletContext().showStatus(key +" : " +value);
        }
    }

    /**
     * Trace method, available in debug mode
     * @param value
     * @param key
     */
    private void trace(String value, String key) {
        if (this.debug) {
            output.println(">>  " + key + " :");
            output.println(value);
            getAppletContext().showStatus(key +" : " +value);            
        }
    }
    
    /**
     * Callback mehod (with mayscript tag within applet declaration)
     * @param method
     * @param params
     * @return
     */
    private boolean callback(Object[] params) {
        try  {
            
            JSObject  jsobj = JSObject.getWindow(this);
            ((JSObject)jsobj).call(this.callbackmethod, params);
            
            return true;
        } catch (Exception e){ 
            trace(e.getMessage(),"Error");
            return false;
        }
    }
    
    private boolean checkJava5() {
        return System.getProperty("java.version").startsWith("1.5");
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getUploadErrors() {
		return uploadErrors;
	}

	public void setUploadErrors(String uploadErrors) {
		this.uploadErrors = uploadErrors;
	}

	public int getUploadedCount() {
		return this.uploadedCount;
	}

	public void setUploadedCount(int uploadedCount) {
		this.uploadedCount = uploadedCount;
	}
	
	public void chooseFolder() {
	   if (!jChooseRoot) {
	       this.jChooseRoot = true;
	   }
	}
	
	public void listFiles(String path, String recursive) {
	    if (!jListCall){
    	    if (this.listPath == null) {
    	        this.listPath = path;
    	        this.recursive = Boolean.parseBoolean(recursive);
    	    }
    	    this.jListCall = true;
	    } else {
	        trace("File listing currently running, wait a minute");
	    }
	}

}

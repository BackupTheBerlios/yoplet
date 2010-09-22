package org.yoplet;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import javax.swing.JApplet;
import javax.swing.JFileChooser;

import netscape.javascript.JSObject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.Representation;
import org.yoplet.graphic.Outputable;
import org.yoplet.graphic.TextOutputPanel;

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
    private boolean     recursive       = false;
    private String      listPath        = null;
    private String[]    filefilters     = new String[]{};
    private Collection  cookies         = new ArrayList();
    private String      cookiestring    = null;
    
    //callback part
    private String callbackmethod   = "appletCallBack";

    // javascript handle props
    private boolean jListCall       = false;
    private boolean jReadCall       = false;
    private boolean jWriteCall      = false;
    private boolean jCountCall      = false;
    private boolean jUploadCall     = false;
    private boolean jChooseRoot     = false;
    private boolean jDeleteCall     = false;
    
    private JFileChooser jfilechoose = null;
    
    // upload operation
    private Set uploadqueue = new HashSet();
    private Set deletequeue = new HashSet();
 
    Runnable javascriptListener = new Runnable() {
    	public void run() {
    	    
    	    trace("running");
    	    
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
	            } else if (jDeleteCall) {
	                jDeleteCall = false;
	                deleteFiles();
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
        this.content = getParameter(FileOperator.CONTENT);
        this.lineSeparator = getParameter(FileOperator.LINE_SEPERATOR);
        this.cookiestring = getParameter(FileOperator.COOKIES);
        
        String cbmethod = getParameter("callbackmethod");
        
        if (null != cbmethod) {
            this.callbackmethod = cbmethod; 
        }
        
        JSONObject res = new JSONObject();
        res.put("name", "init");
        callback(new String[]{res.toString()});
        trace("init end");
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
            Collection files = FileUtils.listFiles(root, this.filefilters, this.recursive);
            boolean checksum = files.size() <=10;
            Collection results = new ArrayList();
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                File f = (File) iterator.next();
                Map obj = new HashMap();
                results.add(obj);
                obj.put("path", f.getAbsolutePath());
                obj.put("size", new Long(f.length()));
                try {
                    obj.put("checksum", new Long(FileUtils.checksumCRC32(f)));
                } catch (IOException ioe) {
                    trace("exception during crc check");
                }
            }
            trace("Done with file listing  " + files.toString());
            Map res = new HashMap();
            res.put("files", results);
            JSONObject js= new JSONObject();
            js.put("name", "listfiles");
            js.put("result", res);
            callback(new String[]{js.toString()});
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
    
    /**
     * Upload Method
     * @param file
     * @param fileName
     * @throws MalformedURLException
     */
    private void uploadFile(File file, String fileName) throws MalformedURLException {
    	if (checkJava5()) {
    	    String md5 = DigestUtils.md5Hex(file.getAbsolutePath());
    	    Client client = null;
    	    try {
        	    java.net.URL u = new java.net.URL(this.url);
        	    String p = u.getProtocol();
        	    Protocol protoc = Protocol.valueOf(p);
        	    
        	    client = new Client(new Context(),protoc);
        	    trace("before cookies");
        	    
        	    for (Iterator iterator = this.cookies.iterator(); iterator.hasNext();) {
                    Cookie c = (Cookie) iterator.next();
                    trace("Cookie " + c.toString());
                    client.getContext().getParameters().add(c);
                    trace("Client enriched with cookie " +  c.toString());
                }
        	    
        	    trace("after cookies");
        	    
        	    client.start();
        	    Reference baseRef = new Reference(Protocol.HTTP,u.getHost(),(-1 != u.getPort())?u.getPort():80);
        	    trace("baseRef",baseRef.toString());
        	    Reference resource = new Reference(baseRef,u.getPath());
        	    trace("resource",resource.toString());
        	    resource.addQueryParameter("filename", fileName)
        	    .addQueryParameter("originalname", file.getAbsolutePath())
        	    .addQueryParameter("checksum", ""+FileUtils.checksumCRC32(file));
        	    FileRepresentation f = new FileRepresentation(file,MediaType.IMAGE_PNG);
        	    Response response = client.post(resource, f);
        	    trace("url",resource.toString());
        	    trace("Status",""+response.getStatus()+"  vs " +Status.SUCCESS_OK.getCode());
        	    Representation rep = response.getEntity();
                JSONObject jso = new JSONObject();
                jso.put("path", file.getAbsolutePath());
                jso.put("md5", md5);
                jso.put("checksum",new Long(FileUtils.checksumCRC32(file)));
                JSONObject res = new JSONObject();
                res.put("result",jso);
                
                if (Status.SUCCESS_OK.equals(response.getStatus())) {
                    res.put("name", "uploadok");
                    callback(new String[]{res.toString()});
                } else if (Status.REDIRECTION_PERMANENT.equals(response.getStatus())){
                	
                	Form form = (Form)response.getAttributes().get("org.restlet.http.headers");
                	String newloc = form.getFirstValue("Location").toString();
                	trace("Moved url => check Location in header : " + newloc );
                	
                } else {
                	trace("Response header ",response.getAttributes().toString());
                    res.put("name", "uploadko");
                    callback(new String[]{res.toString()});
                }

            } catch(Exception e) {
                trace("Ooops " + e.getMessage());
            } finally {
                if (null != client) {
                    try {
                        client.stop();
                        client = null;
                    } catch (Exception e) {
                        // nada
                    }
                }
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
     * Deletion method
     * @param files
     * @return
     */
    public String performDelete(String files) {
        if (!jDeleteCall) {
            JSONArray jsfiles =  (JSONArray)JSONValue.parse(files);
            this.deletequeue.clear();
            for (int i = 0; i < jsfiles.size(); i++) {
                this.deletequeue.add((String)jsfiles.get(i));
            }
            this.jDeleteCall = true;
            return Yoplet.RETURN_OK;
        } else {
            return Yoplet.RETURN_KO;
        }
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
            	this.rename = rename;
            	JSONArray jsfiles = (JSONArray)JSONValue.parse(files);
            	this.uploadqueue.clear();
            	for (int i = 0; i < jsfiles.size(); i++) {
            	    String file = (String)jsfiles.get(i);
                    this.uploadqueue.add(file);
                }
            	this.jUploadCall = true;
                return Yoplet.RETURN_OK;
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
        int choice = jfilechoose.showDialog(null, "OK");
        trace("Dialog Shown");
        if (choice == JFileChooser.APPROVE_OPTION) {
            File f = jfilechoose.getSelectedFile();
            Map result = new HashMap();
            result.put("path",f.getAbsolutePath());
            JSONObject op = new JSONObject();
            op.put("name","choosefile");
            op.put("result", result);
            this.callback(new String[]{op.toString()});
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
    
    /**
     * Delete file method
     */
    private void deleteFiles() {
        String path;
        String md5;
        for (Iterator iterator = this.deletequeue.iterator(); iterator.hasNext();) {
            
            try {
                path = (String) iterator.next();
                md5 = DigestUtils.md5Hex(path);
                JSONObject jso = new JSONObject();
                jso.put("path", path);
                jso.put("md5", md5);
                
                JSONObject op = new JSONObject();
                op.put("result", jso);
                
                if (FileUtils.deleteQuietly(new File(path))) {
                    op.put("name", "deleteok");
                } else {
                    op.put("name", "deleteko");
                }
                this.callback(new String[]{op.toString()});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    
    private void uploadFiles() {
    	int  j=0;
    	for (Iterator iterator = this.uploadqueue.iterator(); iterator.hasNext();) {
            String path = (String) iterator.next();
            this.trace("Handling " + path +  "upload");
            
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
				    trace("Cannot upload directory or non existing file -- skipping");
				}
    			j++;
			} catch (Exception e) {
			    e.printStackTrace();
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
        
        trace("before parent start");
        super.start();
        trace(" after parent start");
        
        try {
            
            this.trace("Starting applet");
            this.trace(this.action, "Action type");

            Collection cs = new ArrayList();
            
            if (null != cookiestring) {
                String[] cooks = cookiestring.split("\\s");
                    if (null != cooks) {
                    for (int i = 0; i < cooks.length; i++) {
                        trace("Adding cookie " + cooks[i]);
                        String c = getCookie(cooks[i]);
                        if (null !=c) {
                            cs.add(new Cookie(cooks[i], c));
                            trace("Cookie added " + cooks[i]);
                        } else {
                            trace("Cookie not added " + cooks[i]);
                        }
                    }
                    this.cookies = cs;
                }
            }
            
            trace("cookie init done");
            
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
            
            trace("before runnable");
            this.javascriptListener.run();
            trace("applet started");
            
            JSONObject res = new JSONObject();
            res.put("name", "start");
            callback(new String[]{res.toString()});
        } catch (Exception e) {
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
            
            trace("Callback done");
            return true;
        } catch (Exception e){ 
            trace(e.getMessage(),"Error");
            return false;
        }
    }
    
    private boolean checkJava5() {
        String version = System.getProperty("java.version");
        boolean res = false;
        trace("Version : "+ version.split("\\.")[1]);
        if (version.split("\\.").length >= 2) {
            res = (Integer.parseInt(version.split("\\.")[1]) >= 5);
        } else {
            res = false;
        }
        return res;
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
	
	
	public void setFileFilters(String filters) {
	    if (null != filters && filters.trim().length() != 0) {
	        this.filefilters = filters.split("\\s");
	    }
	    trace("extensions filter " + this.filefilters.toString());
	}
	
	
    public void stop() {
       super.stop();
       this.javascriptListener = null;
    }

    public void destroy() {
        super.destroy();
        this.javascriptListener = null;
    }
    
    
    private String getCookie(String name) {
        
        JSObject js = JSObject.getWindow(this);
        trace("window found " + js);
        JSObject document = (JSObject)js.getMember("document");
        trace("document found " + document);
        String myCookie = (String)document.getMember("cookie");
        trace("Cookie found " + myCookie);
        
        String res = null;
        
        if (null!= myCookie && myCookie.length() > 0) {
            String search = name + "=";
            
            if (myCookie.length() > 0) {
                
               int offset = myCookie.indexOf(search);
               if (offset != -1) {
                  offset += search.length();
                  int end = myCookie.indexOf(";", offset);
                  if (end == -1) {
                      end = myCookie.length();
                  }
                  return myCookie.substring(offset,end);
               } else {
                 trace("Cookie not found " + name);
               }
             }
        } else {
            trace("Cookie not found " + name); 
        }
        return res;
   }

}
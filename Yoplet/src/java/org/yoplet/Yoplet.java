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
import java.util.List;
import java.util.Observable;

import javax.swing.JApplet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.util.ParameterParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.yoplet.graphic.Outputable;
import org.yoplet.graphic.TextOutputPanel;

public class Yoplet extends JApplet implements FileOperator {
	public final static String RETURN_OK = "OK";
    
    public void update(Observable o, Object arg) {}

    private String     action = null;

    private File       file = null;
    private File       flag = null;
    
    private String     content       = null;
    
    private String     uploadErrors  = null;
    private String	   pattern		 = null;
    private String     rename        = null;
    private int		   count         = 0;
    private int		   uploadedCount = 0;
    private boolean    deleteFlag    = false;
    
    private String     lineSeparator = null;

    private String     url        = null;
    private String     getParams  = null;
    private String     postParams = null;
    private boolean    debug      = false;
    private Outputable output     = null;

    // javascript handle props
    private boolean jReadCall		= false;
    private boolean jWriteCall		= false;
    private boolean jCountCall		= false;
    private boolean jUploadCall		= false;
 
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
        
        // UI initialisation
        Container contentPane = getContentPane();
        this.output = new TextOutputPanel();
        contentPane.add ((TextOutputPanel) this.output);


        // Data initialisation
        this.debug = Boolean.parseBoolean(getParameter(FileOperator.DEBUG));
        
        contentPane.setVisible(debug);
        
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
        
        if (null != this.javascriptListener) {
            this.javascriptListener.run();
        }
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
    
    private File[] filterFile() {
    	if(this.file.exists() && this.file.isDirectory()) {
	    	FileFilter fileFilter = new WildcardFileFilter(this.pattern);
	    	return this.file.listFiles(fileFilter);
    	}
    	this.trace(this.file.getPath(), "File doesn't exist or isn't a directory ");
    	return null;
    }
    
    private void uploadFile(File file, String fileName, String url, String getParameters, String postParameters) throws HttpException, IOException {
    	String urlString = url;
    	if(getParameters != null && !"".equals(getParameters))
    		urlString += "?" + getParameters;
    	PostMethod post = new PostMethod(urlString);
        
        try {
        	// Make part's list
        	ParameterParser parser = new ParameterParser();
        	List postParamsList = parser.parse(postParameters, '&');
            Part[] parts = new Part[postParamsList.size() + 1];
            for(int i = 0; i < postParamsList.size(); i++) {
            	NameValuePair param = (NameValuePair) postParamsList.get(i);
            	parts[i] = new StringPart(param.getName(), param.getValue());
            };
            parts[parts.length - 1] = new FilePart(fileName, file);
            
            // Proceed HTTP request
            post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
            HttpClient httpclient = new HttpClient();
            int result = httpclient.executeMethod(post);
            this.trace("Response status code : " + result);
            
            // Write response on disk
            this.writeResponse(post.getResponseBodyAsString(), fileName);
		} finally {
            post.releaseConnection();
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
    public String performUpload(String rename) {
    	this.rename = rename;
    	this.jUploadCall = true;
        return Yoplet.RETURN_OK;
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
    
    private void countFile() {
    	this.count = 0;
    	File[] files = this.filterFile();
    	if(files != null)
    		this.count = files.length;
    	this.trace(this.count, " files found to upload");
    }
    
    private void uploadFiles() {
    	this.uploadedCount = this.count;
    	this.uploadErrors = "";
    	File[] files = this.filterFile();
    	if(files != null) {	    	
	    	StringBuffer sb = new StringBuffer(50);
	    	for(int i = 0; i < files.length; i++) {
	    		try {
	    			// Define name of the part for rename files on server side
	    			String fileName = FilenameUtils.getBaseName(files[i].getName());
	    			if(this.rename != null && !"".equals(this.rename)) {
	    				fileName = this.rename + i;
	    				this.trace("Define part name to " + fileName);
	    			}
	    			
	    			this.uploadFile(files[i], fileName, this.url, this.getParams, this.postParams);
	    			this.uploadedCount--;
	    			// Delete files if needed
	    			if(this.deleteFlag) {
	    				this.trace("Also removes the file uploaded");
	    				files[i].delete();
	    				
	    			}
				} catch (Exception e) {
					sb.append(files[i].getName());
					sb.append(" isn't uploaded : ");
					sb.append(e.getMessage());
					sb.append("\r\n");
				}
	    	}
	    	this.uploadErrors = sb.toString();
	    	if(this.uploadErrors.length() > 0) {
	    		this.trace("Upload errors :");
	    		this.trace(this.uploadErrors);
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
            
            if (this.action.equals(FileOperator.ACTION_UPLOAD)) {
                this.trace("Applet uploading");
                this.performUpload(this.rename);
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
    
    private void writeResponse(String response, String fileName) throws IOException {
    	if (this.debug) {
    		BufferedWriter writer = null;
    		try {
	    		writer = new BufferedWriter(new FileWriter(this.file.getPath() + File.separator + "response_" + fileName.replace('.', '_') + ".html"));
	    		writer.write(response);
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

}

package controllers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import models.Upload;
import play.Logger;
import play.mvc.Controller;

import com.google.appengine.api.datastore.Blob;

public class Application extends Controller {

    public static void index() {
        render();
    }

	public static void uploadTest(String originalname,String filename) {
	    
	    OutputStream out = new ByteArrayOutputStream();
	    int n = 0;
	    
	    try {
	        
    	    byte[] buff= new byte[1024];
    	                         
    	    while((n = request.body.read(buff)) > 0 ) {
    	        out.write(buff, 0, n);
    	    }
    	    
    	    out.flush();
    	    out.close();
    	    Upload up = new Upload();
    	    up.filename = filename;
    	    up.originalname = originalname;
    	    up.contenttype = request.contentType;
    	    up.file = new Blob(((ByteArrayOutputStream)out).toByteArray());
    	    up.insert();

			renderText(up.id);
    	    
	    } catch (IOException ioe) {
	       Logger.error(ioe.getMessage());
	       error();
	    }

	}
	
	
	public static void movedTest() {
		redirect("/test/upload", true);
	}
	
	
	public static void uploads() {
	    List uploads = Upload.all().order("-created").fetch();
	    render(uploads);
	}

}
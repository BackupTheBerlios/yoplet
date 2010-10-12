package org.yoplet;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UploadTest {
    
    String resourceUrl = "test/upload";
    String serverurl = "http://y0pl3t.appspot.com";
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    private File toBeuploaded;
    
    @Before
    public void setUp() throws IOException {
        toBeuploaded = folder.newFile("up");
        BufferedWriter out = new BufferedWriter(new FileWriter(toBeuploaded));
        out.write("this.is = A File\n");
        out.write("for.a = Test\n");
        out.close();
    }    
    
    @Test
    public void testJava5() {
        String[] versions = System.getProperty("java.version").split("\\.");
        assertTrue(versions.length >= 2);
        assertTrue(5 <= Integer.parseInt((versions[1])));
    }
    
    
    @Test
    public void testUpload() throws Exception {
    	File toupload = folder.newFile("Jojo");
    	PostMethod pm = new PostMethod(serverurl+"/"+resourceUrl);
    	Part[] parts = {
    			new StringPart("originalname", toupload.getAbsolutePath(),"UTF-8"),
    			new StringPart("filename", toupload.getName(),"UTF-8"),
    			new FilePart("file", toupload)
    	};
    	
    	pm.setRequestEntity(new MultipartRequestEntity(parts, pm.getParams()));
    	HttpClient cl = new HttpClient();
    	int status = cl.executeMethod(pm);
    	
    	assertTrue(HttpStatus.SC_OK == status);
    }


}

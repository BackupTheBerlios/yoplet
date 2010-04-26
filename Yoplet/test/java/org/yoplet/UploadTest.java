package org.yoplet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.rules.TemporaryFolder;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.FileRepresentation;

public class UploadTest {
    
    Client c;
    String resourceUrl = "test/upload";
    String serverurl = "y0pl3t.appspot.com";
    
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
    public void testBasicUpload() {
        c = new Client(Protocol.HTTP);
        FileRepresentation f = new FileRepresentation(toBeuploaded,MediaType.IMAGE_PNG);
        Reference baseRef = new Reference(Protocol.HTTP,serverurl);
        Reference resource = new Reference(baseRef,resourceUrl);
        Response response = c.post(resource, f);
        
        assertEquals(response.getStatus(),Status.SUCCESS_OK);
    }
    
    @Test
    public void testSecuredUpload() {
        c = new Client(Protocol.HTTPS);
        FileRepresentation f = new FileRepresentation(toBeuploaded,MediaType.IMAGE_PNG);
        Reference baseRef = new Reference(Protocol.HTTPS,serverurl);
        Reference resource = new Reference(baseRef,resourceUrl);
        Response response = c.post(resource, f);        
        assertEquals(response.getStatus(),Status.SUCCESS_OK); 
    }
    
    
    @Test
    public void testMixedUpload() {
        c = new Client(Arrays.asList(Protocol.HTTP,Protocol.HTTPS));
        FileRepresentation f = new FileRepresentation(toBeuploaded,MediaType.IMAGE_PNG);
        Response response = c.post("test/upload", f);
        assertEquals(response.getStatus(),Status.SUCCESS_OK);
    }

}

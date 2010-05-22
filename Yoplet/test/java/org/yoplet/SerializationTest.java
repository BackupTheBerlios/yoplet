package org.yoplet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.Test;

public class SerializationTest {
    
    @Test
    public void testBasicSerialization() {
        Map result = new HashMap();
        result.put("file", "file information");
        JSONObject jso = new JSONObject();
        jso.putAll(result);
        assertNotNull(jso.toString());
        assertTrue(jso.toString().length() > 0 );
    }

}

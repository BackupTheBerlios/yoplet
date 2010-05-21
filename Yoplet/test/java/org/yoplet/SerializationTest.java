package org.yoplet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.yoplet.json.JSONObject;

public class SerializationTest {
    
    @Test
    public void testBasicSerialization() {
        Map result = new HashMap();
        result.put("file", "file information");
        JSONObject jso = new JSONObject(result);
        assertNotNull(jso.toString());
        assertTrue(jso.toString().length() > 0 );
    }

}

package org.yoplet;

import org.junit.Test;
import static org.junit.Assert.*;

import org.yoplet.json.JSONObject;

public class SerializationTest {
    
    @Test
    public void testBasicSerialization() {
        Operation op = new Operation("test", new String[]{"toto","titi"});
        JSONObject jso = new JSONObject(op);
        assertNotNull(jso.toString());
        assertTrue(jso.toString().length() > 0 );
    }

}

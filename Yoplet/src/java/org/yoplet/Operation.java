package org.yoplet;

import java.util.ArrayList;
import java.util.Collection;

public class Operation {

    private String name;
    
    private Collection results = new ArrayList();
    
    public Operation(String name,String[] result) {
        this.name = name;
        for (int i = 0; i < result.length; i++) {
            this.results.add(new Result(result[i]));
        }
        
    }
    
    public String getName() {
        return this.name;
    }
    
    public Collection getResults() {
        return this.results;
    }
}

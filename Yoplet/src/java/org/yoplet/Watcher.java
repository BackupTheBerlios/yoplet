package org.yoplet;

import java.io.File;

public class Watcher extends Thread {

	private File path = null;
	private Object notified = null;
	private long sleep = 1000l;
	
	public Watcher(File path,Object notify,long sleepMillisec) {
		super();
		System.out.println("Watcher init with " + path.getAbsolutePath());
		this.path = path;
		this.notified = notify;
	}
	
	public void run() {
  	    while (true) {  	
 		    try 
 		    {
 		    	if (this.path.canRead())
 		    	{
 		    		synchronized (this.notified) {
 		    			this.notified.notify();
 		    			break;
 		    		}
 		    	}
  		    	Thread.sleep(this.sleep);
  		    }
  		    catch (Throwable t) {
  		    	t.printStackTrace();
  		    }
  	    }
	}

}

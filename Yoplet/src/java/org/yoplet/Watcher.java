package org.yoplet;

import java.io.File;

public class Watcher extends Thread {

	private File path = null;
	private Object notified = null;
	private long sleep = 1000l;
	private Object watcherReturn = null;
	
	public Watcher(File path,Object notify,long sleepMillisec) {
		super();
		this.path = path;
		this.notified = notify;
		this.sleep = sleepMillisec;
	}
	
	public void run() {
  	    while (true) {  	
 		    try 
 		    {
 		    	if (null!= this.path && this.path.canRead())
 		    	{
 		    		synchronized (this.notified) {
 		    			this.notified.notify();
 		    			this.watcherReturn = "OK";
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

	public Object getWatcherReturn() {
		return watcherReturn;
	}
}

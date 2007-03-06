package org.lostinthegarden.applet;

/**
 * @author Yoplet consortium
 * FileOperator behavior specification. 
 */

public interface FileOperator {
	public final static String ACTION = "action";
	public final static String ACTION_READ = "read";
	public final static String ACTION_WRITE = "write";
	public final static String ACTION_WATCH = "watch";

	public final static String READ_PATH  = "readPath";
	public final static String WRITE_PATH = "writePath";
	public final static String WATCH_PATH = "watchPath";
	
	public final static String DEBUG      = "debug";
	public final static String URL        = "url";
	public final static String CONTENT    = "content";
	
	/**
	 * FileOperator will read data from a target read path
	 */
	public void performRead();
	
	/**
	 * FileOperator will write data to a target write path
	 */
	public void performWrite();
	
	/**
	 * FileOperator will watch for the creation of watch path
	 */
	public void performWatch ();
	
	/**
	 * File Operator can invoke remote servers
	 * @param target url to invoke
	 */
	public void performRequest();

	/**
	 * Content getter
	 * @return
	 */
	public String getContent();
	
	/**
	 * Content setter
	 * @param content content parameter
	 */
	public void setContent(String content);
	

}

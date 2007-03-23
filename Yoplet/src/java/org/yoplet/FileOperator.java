package org.yoplet;

/**
 * @author Yoplet consortium
 * FileOperator behavior specification. 
 */

public interface FileOperator {
	public final static String ACTION = "action";
	public final static String ACTION_READ = "read";
	public final static String ACTION_WRITE = "write";
	public final static String ACTION_WATCH = "watch";
	public final static String ACTION_DELETE= "delete";
	
	public final static String FILE_PATH  = "filePath";
	public final static String FLAG_PATH  = "flagPath";
	public final static String LINE_SEPERATOR = "lineSeparator";
	
	public final static String DEBUG      = "debug";
	public final static String URL        = "url";
	public final static String CONTENT    = "content";
	
	/**
	 * FileOperator will read data from a target read path
	 */
	public String performRead();
	
	/**
	 * FileOperator will write data to a target write path
	 */
	public String performWrite(String content);
	
	/**
	 * FileOperator will watch for the creation of watch path
	 */
	public String performWatch ();
	
	/**
	 * File Operator can invoke remote servers
	 * @param target url to invoke
	 */
	public String performRequest();

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

package org.lostinthegarden.applet;

/**
 * @author 7uc0
 * FileOperator behavior specification. 
 */

public interface FileOperator {
	
	
	public final static String SOURCE_FILE 	= 	"SOURCEFILE";
	public final static String DEST_FILE 	= 	"DESTFILE";
	public final static String DEBUG 		=  	"DEBUG";
	public final static String URL			=	"URL";
	
	/**
	 * The FileOperator can read data
	 * from a source file
	 */
	public void performRead();
	
	/**
	 * It can also write to a destination
	 * File
	 */
	public void performWrite();
	
	/**
	 * File Operator can invoke remote servers
	 * @param target url to invoke
	 */
	public void performRequest();

	
	/**
	 * One can specify its source file
	 * @param path source file spec
	 */
	public void setSourceFile(String path);
	
	/**
	 * One can specify its destination file
	 * @param path
	 */
	public void setDestFile(String path);
	
	
	/**
	 * debug mode setter
	 */
	public void setDebug(boolean debug);
	
	/**
	 * url setter
	 * @param url
	 */
	public void setUrl(String url);
	

}

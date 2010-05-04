package org.yoplet;

/**
 * @author Yoplet consortium
 * FileOperator behavior specification. 
 */

public interface FileOperator {
	public final static String ACTION        = "action";
	public final static String ACTION_READ   = "read";
	public final static String ACTION_WRITE  = "write";
	public final static String ACTION_COUNT  = "count";
	public final static String ACTION_UPLOAD = "upload";
	
	public final static String FILE_PATTERN   = "filePattern";
	public final static String FILE_PATH      = "filePath";
	public final static String FLAG_PATH      = "flagPath";
	public final static String LINE_SEPERATOR = "lineSeparator";
	
	public final static String DELETE_FLAG = "deleteFlag";
	public final static String DEBUG       = "debug";
	public final static String URL         = "url";
	public final static String GET_PARAMS  = "getParams";
	public final static String POST_PARAMS = "postParams";
	public final static String CONTENT     = "content";
	public final static String RENAME      = "rename";
	
	/**
	 * FileOperator will read data from a target read path
	 */
	public String performRead();
	
	/**
	 * FileOperator will write data to a target write path
	 */
	public String performWrite(String content);
	
	/**
	 * FileOperator will check the number of files 
	 * in the directory with match the pattern
	 */
	public String performCount();
	
	/**
	 * File Operator can upload file on remote servers
	 */
	public String performUpload(String rename,String files);

	/**
	 * Content getter
	 * @return String
	 */
	public String getContent();
	
	/**
	 * Content setter
	 * @param content content parameter
	 */
	public void setContent(String content);
	
	/**
	 * Count getter
	 * @return int
	 */
	public int getCount();
	
	/**
	 * Count setter
	 * @param count count parameter
	 */
	public void setCount(int count);
	
	/**
	 * UploadedCount getter
	 * @return int
	 */
	public int getUploadedCount();
	
	/**
	 * UploadedCount setter
	 * @param uploadedCount uploadedCount parameter
	 */
	public void setUploadedCount(int uploadedCount);
	
	/**
	 * UploadErrors getter
	 * @return String
	 */
	public String getUploadErrors();
	
	/**
	 * UploadErrors setter
	 * @param uploadErrors uploadErrors parameter
	 */
	public void setUploadErrors(String uploadErrors);
}

package org.yoplet;

/**
 * @author Yoplet consortium
 * FileOperator behavior specification. 
 */

public interface FileOperator {
	String ACTION        = "action";
	String ACTION_READ   = "read";
	String ACTION_WRITE  = "write";
	String ACTION_COUNT  = "count";
	String ACTION_UPLOAD = "upload";
	
	String FILE_PATTERN   = "filePattern";
	String FILE_PATH      = "filePath";
	String FLAG_PATH      = "flagPath";
	String LINE_SEPERATOR = "lineSeparator";
	
	String DELETE_FLAG = "deleteFlag";
	String DEBUG       = "debug";
	String URL         = "url";
	String CONTENT     = "content";
	String RENAME      = "rename";
	
	String COOKIES     = "cookies";
	
	/**
	 * FileOperator will read data from a target read path
	 */
	String performRead();
	
	/**
	 * FileOperator will write data to a target write path
	 */
	String performWrite(String content);
	
	/**
	 * FileOperator will check the number of files 
	 * in the directory with match the pattern
	 */
	String performCount();
	
	/**
	 * File Operator can upload file on remote servers
	 */
	String performUpload(String rename,String files);

	/**
	 * Content getter
	 * @return String
	 */
	String getContent();
	
	/**
	 * Content setter
	 * @param content content parameter
	 */
	void setContent(String content);
	
	/**
	 * Count getter
	 * @return int
	 */
	int getCount();
	
	/**
	 * Count setter
	 * @param count count parameter
	 */
	void setCount(int count);
	
	/**
	 * UploadedCount getter
	 * @return int
	 */
	int getUploadedCount();
	
	/**
	 * UploadedCount setter
	 * @param uploadedCount uploadedCount parameter
	 */
	void setUploadedCount(int uploadedCount);
	
	/**
	 * UploadErrors getter
	 * @return String
	 */
	String getUploadErrors();
	
	/**
	 * UploadErrors setter
	 * @param uploadErrors uploadErrors parameter
	 */
	void setUploadErrors(String uploadErrors);
}

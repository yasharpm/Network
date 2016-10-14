package com.yashoid.network.request;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileRequest extends NetworkRequest<File> {

	private static final int BUFFER_SIZE = 512;
	
	private File mFile;
	
	public FileRequest(String url, File dest) throws IOException {
		super(url);
		
		mFile = dest;
	}

	@Override
	protected File parseResponse(InputStream stream) throws IOException {
		long totalSize = getConnection().getContentLength();
		long currentSize = 0;
		
		FileOutputStream outStream = new FileOutputStream(mFile);
		
		byte[] buffer = new byte[BUFFER_SIZE];
		int len = 0;
		
		while (len!=-1) {
			len = stream.read(buffer);
			
			if (len>0) {
				outStream.write(buffer, 0, len);
				
				currentSize += len;
				
				onDownloadProgressChanged(currentSize, totalSize);
			}
		}
		
		outStream.close();
		
		return mFile;
	}
	
	/**
	 * This method is called inside a thread.
	 */
	protected void onDownloadProgressChanged(long current, long total) {
		
	}

}

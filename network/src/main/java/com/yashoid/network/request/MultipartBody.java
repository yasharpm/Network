package com.yashoid.network.request;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map.Entry;

import com.yashoid.network.request.NetworkRequest.BodyLoader;

public class MultipartBody implements BodyLoader {

	private static final String BOUNDARY_WRAPPER = "===";
	
	private static final String CONTENT_TYPE = "multipart/form-data; boundary=";
	
	private static final String CHARSET = "UTF_8";
	
	private static final String STRING_PART_CONTENT_TYPE = "Content-Type: text/plain; charset=" + CHARSET + "\r\n";

	private HttpURLConnection mConnection;
	private OutputStream mOutputStream;
	
	private String mBoundary;
	
	private HashMap<String, Object> mParts;
	
	public MultipartBody(HttpURLConnection connection) {
		mConnection = connection;
		
		mBoundary = BOUNDARY_WRAPPER + System.currentTimeMillis() + BOUNDARY_WRAPPER;
		
		mParts = new HashMap<String, Object>(10);
		
		mConnection.setUseCaches(false);
		mConnection.setRequestProperty(NetworkRequest.CONTENT_TYPE, getContentType());
	}
	
	public MultipartBody(NetworkRequest<?> request) {
		this(request.getConnection());
	}
	
	private String getContentType() {
		return CONTENT_TYPE + mBoundary;
	}
	
	public void addPart(String name, String value) {
		mParts.put(name, value);
	}
	
	public void addPart(String name, File value) {
		mParts.put(name, value);
	}

	@Override
	public void write(OutputStream output) throws IOException {
		mOutputStream = output;
		
		for (Entry<String, Object> part: mParts.entrySet()) {
			String name = part.getKey();
			Object value = part.getValue();

			if (value instanceof String) {
				writeStringPart(name, (String) value);
			}
			else if (value instanceof File) {
				writeFilePart(name, (File) value);
			}
			
			mOutputStream.flush();
		}
		
		mOutputStream.write(("\r\n--" + mBoundary + "--\r\n").getBytes(CHARSET));
	}
	
	private void writeStringPart(String name, String value) throws IOException {
		writePartStart();
		
		mOutputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n").getBytes(CHARSET));
		mOutputStream.write(STRING_PART_CONTENT_TYPE.getBytes(CHARSET));
		mOutputStream.write(("\r\n" + value + "\r\n").getBytes(CHARSET));
	}
	
	private void writeFilePart(String name, File value) throws IOException {
		writePartStart();
		
		mOutputStream.write(
				("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + value.getName() + "\"\r\n")
						.getBytes(CHARSET)
				);
		mOutputStream.write(
				("Content-Type: " + URLConnection.guessContentTypeFromName(value.getName()) + "\r\n")
						.getBytes(CHARSET)
				);
		mOutputStream.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes(CHARSET));
		
		FileInputStream inputStream = new FileInputStream(value);
		byte[] buffer = new byte[512];
		int len = 0;
		
		while (len!=-1) {
			len = inputStream.read(buffer);
			
			if (len>0) {
				mOutputStream.write(buffer, 0, len);
			}
		}
		
		inputStream.close();
		
		mOutputStream.write("\r\n".getBytes(CHARSET));
	}
	
	private void writePartStart() throws IOException {
		mOutputStream.write(("--" + mBoundary + "\r\n").getBytes(CHARSET));
	}

	@Override
	public boolean isCancelable() {
		return false;
	}

	@Override
	public boolean cancel() {
		return false;
	}

}

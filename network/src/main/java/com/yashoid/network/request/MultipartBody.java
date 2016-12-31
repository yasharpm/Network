package com.yashoid.network.request;

import com.yashoid.network.request.NetworkRequest.BodyLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class MultipartBody implements BodyLoader {

	private static final String BOUNDARY_WRAPPER = "===";
	
	private static final String CONTENT_TYPE = "multipart/form-data; boundary=";
	
	private static final String CHARSET = "UTF-8";
	
	private static final String STRING_PART_CONTENT_TYPE = "Content-Type: text/plain; charset=" + CHARSET + "\r\n";

	private HttpURLConnection mConnection;
	private OutputStream mOutputStream;
	
	private String mBoundary;
	
	private HashMap<String, ArrayList<Object>> mParts;
	
	public MultipartBody(HttpURLConnection connection) {
		mConnection = connection;
		
		mBoundary = BOUNDARY_WRAPPER + System.currentTimeMillis() + BOUNDARY_WRAPPER;
		
		mParts = new HashMap<>(10);
		
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
		putPart(name, value);
	}
	
	public void addPart(String name, File value) {
		putPart(name, value);
	}

	public void addPart(String name, String fileName, InputStream value) {
		putPart(name, new InputStreamValue(fileName, value, 0));
	}

	public void addPart(String name, String fileName, InputStream value, long length) {
		putPart(name, new InputStreamValue(fileName, value, length));
	}

	private void putPart(String name, Object value) {
		ArrayList<Object> values = mParts.get(name);

		if (values == null) {
			values = new ArrayList<>(4);

			mParts.put(name, values);
		}

		values.add(value);
	}

	@Override
	public void write(OutputStream output) throws IOException {
		mOutputStream = output;
		
		for (Entry<String, ArrayList<Object>> part: mParts.entrySet()) {
			String name = part.getKey();
			ArrayList<Object> values = part.getValue();

			for (Object value: values) {
				onStartedWritingPart(name);

				if (value instanceof String) {
					writeStringPart(name, (String) value);
				}
				else if (value instanceof File) {
					writeFilePart(name, (File) value);
				}
				else if (value instanceof InputStreamValue) {
					writeInputStreamPart(name, (InputStreamValue) value);
				}

				mOutputStream.flush();

				onFinishedWritingPart(name);
			}
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
		writeInputStreamPart(name, value.getName(), new FileInputStream(value), value.length());
	}

	private void writeInputStreamPart(String name, InputStreamValue value) throws IOException {
		writeInputStreamPart(name, value.fileName, value.stream, value.length);
	}

	private void writeInputStreamPart(String name, String fileName, InputStream inputStream, long total) throws IOException {
		writePartStart();

		mOutputStream.write(
				("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n")
						.getBytes(CHARSET)
		);
		mOutputStream.write(
				("Content-Type: " + URLConnection.guessContentTypeFromName(fileName) + "\r\n")
						.getBytes(CHARSET)
		);
		mOutputStream.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes(CHARSET));

		byte[] buffer = new byte[1024];
		int len = 0;

		long progress = 0;

		while (len!=-1) {
			len = inputStream.read(buffer);

			if (len>0) {
				mOutputStream.write(buffer, 0, len);
				mOutputStream.flush();

				progress += len;

				onWritingPartProgress(name, progress, total);
			}
		}

		inputStream.close();

		mOutputStream.write("\r\n".getBytes(CHARSET));
	}
	
	private void writePartStart() throws IOException {
		mOutputStream.write(("--" + mBoundary + "\r\n").getBytes(CHARSET));
	}

	protected void onStartedWritingPart(String name) {

	}

	protected void onWritingPartProgress(String name, long progress, long total) {

	}

	protected void onFinishedWritingPart(String name) {

	}

	@Override
	public boolean isCancelable() {
		return false;
	}

	@Override
	public boolean cancel() {
		return false;
	}

	private static class InputStreamValue {

		private String fileName;
		private InputStream stream;
		private long length;

		protected InputStreamValue(String fileName, InputStream stream, long length) {
			this.fileName = fileName;
			this.stream = stream;
			this.length = length;
		}

	}

}

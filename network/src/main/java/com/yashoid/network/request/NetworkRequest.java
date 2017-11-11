package com.yashoid.network.request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public abstract class NetworkRequest<Return> {
	
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String ACCEPT = "Accept";
	
	private static final String CONTENT_TYPE_JSON = "application/json";
	
	private static final int NO_RESPONSE_CODE = -1;

	private URL mURL;
	private HttpURLConnection mConnection;

	Map<String, List<String>> mRequestProperties;
	private BodyLoader mBodyLoader = null;
	
	private boolean mIsExecuted = false;
	private boolean mIsCanceled = false;
	
	private int mResponseCode = NO_RESPONSE_CODE;
	
	public NetworkRequest(String url) throws IOException {
		mURL = new URL(url);
		mConnection = (HttpURLConnection) (mURL.openConnection());

		mConnection.setDoInput(true);
	}
	
	public void resetRequest() throws IOException {
		try {
			disconnect();
		} catch (Throwable t) { }
		
		String method = mConnection.getRequestMethod();
		
		mURL = new URL(mURL.toExternalForm());
		mConnection = (HttpURLConnection) (mURL.openConnection());
		
		mConnection.setDoInput(true);
		
		for (String headerName: mRequestProperties.keySet()) {
			List<String> headerValues = mRequestProperties.get(headerName);
			
			for (String headerValue: headerValues) {
				mConnection.addRequestProperty(headerName, headerValue);
			}
		}
		
		setMethod(method);
	}
	
	public HttpURLConnection getConnection() {
		return mConnection;
	}
	
	public boolean setMethod(String method) {
		try {
			mConnection.setRequestMethod(method);
		} catch (ProtocolException exception) {
			HttpURLConnection connection = HttpURLConnection.class.cast(mConnection);
			
			try {
				Field field = HttpURLConnection.class.getDeclaredField("method");
				
				boolean isAccessible = field.isAccessible();
				field.setAccessible(true);
				
				field.set(connection, method);
				
				field.setAccessible(isAccessible);
			} catch (NoSuchFieldException e) {
				return false;
			} catch (IllegalAccessException e) {
				return false;
			} catch (IllegalArgumentException e) {
				return false;
			}
		}
		
		return true;
	}
	
	public void setBody(String body) {
		mBodyLoader = new StringBodyLoader(body);
	}
	
	public void setBody(JSONObject body) {
		mBodyLoader = new StringBodyLoader(body.toString());
		getConnection().setRequestProperty(CONTENT_TYPE, CONTENT_TYPE_JSON);
	}
	
	public void setBody(JSONArray body) {
		mBodyLoader = new StringBodyLoader(body.toString());
		getConnection().setRequestProperty(CONTENT_TYPE, CONTENT_TYPE_JSON);
	}
	
	public void setBody(InputStream stream) {
		mBodyLoader = new InputStreamBodyLoader(stream);
	}
	
	public void setBody(File file) {
		mBodyLoader = new FileBodyLoader(file);
	}
	
	public void setBody(BodyLoader bodyLoader) {
		mBodyLoader = bodyLoader;
	}
	
	public Return execute() throws IOException {
		mRequestProperties = mConnection.getRequestProperties();
		
		IOException exception = null;
		
		try {
			if (mBodyLoader!=null) {
				mConnection.setDoOutput(true);
				mBodyLoader.write(mConnection.getOutputStream());
				
				if (mIsCanceled) {
					disconnect();
					
					return null;
				}
			}
			
			mIsExecuted = true;
			
			mResponseCode = mConnection.getResponseCode();
			
			InputStream stream = mConnection.getErrorStream();
			
			if (stream==null) {
				stream = mConnection.getInputStream();
			}
			
			return parseResponse(stream);
		} catch (IOException e) {
			exception = e;
		}
		
		if (exception!=null) {
			mConnection.disconnect();
		}
		
		throw exception;
	}
	
	/**
	 * Implementations must call {@code disconnect()} after they are done with the connection.
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected abstract Return parseResponse(InputStream stream) throws IOException;
	
	protected void disconnect() {
		mConnection.disconnect();
	}
	
	public boolean isCancelable() {
		return mBodyLoader==null?false:mBodyLoader.isCancelable();
	}
	
	public boolean cancel() {
		if (!isCancelable()) {
			return false;
		}
		
		if (mIsExecuted) {
			return false;
		}
		
		mIsCanceled = mBodyLoader.cancel();
		
		return mIsCanceled;
	}
	
	public boolean isCanceled() {
		return mIsCanceled;
	}
	
	public boolean isExecuted() {
		return mIsExecuted;
	}
	
	public int getResponseCode() {
		return mResponseCode;
	}
	
	protected static interface BodyLoader {
		
		public void write(OutputStream output) throws IOException;
		
		public boolean isCancelable();
		
		public boolean cancel();
		
	}
	
	private static class StringBodyLoader implements BodyLoader {
	
		private String mBody;
		
		protected StringBodyLoader(String body) {
			mBody = body;
		}
		
		@Override
		public void write(OutputStream output) throws IOException {
			output.write(mBody.getBytes());
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
	
	protected static class InputStreamBodyLoader implements BodyLoader {

		private InputStream mStream;
		
		private boolean mIsCanceled = false;
		private boolean mIsFinished = false;
		
		protected InputStreamBodyLoader(InputStream stream) {
			mStream = stream;
		}
		
		@Override
		public void write(OutputStream output) throws IOException {
			int len = 0;
			byte[] buffer = new byte[256];
			while (len!=-1 && !mIsCanceled) {
				len = mStream.read(buffer);
				
				if (len>0 && !mIsCanceled) {
					output.write(buffer, 0, len);
					output.flush();
				}
			}
			
			mIsFinished = true;
		}
		
		@Override
		public boolean isCancelable() {
			return true;
		}
		
		@Override
		public boolean cancel() {
			if (mIsFinished) {
				return mIsCanceled;
			}
			
			mIsCanceled = true;
			
			return true;
		}
		
	}
	
	private static class FileBodyLoader implements BodyLoader {
		
		private File mFile;
		
		private boolean mIsCanceled = false;
		private boolean mIsFinished = false;
		
		protected FileBodyLoader(File file) {
			mFile = file;
		}
		
		@Override
		public void write(OutputStream output) throws IOException {
			FileInputStream input = new FileInputStream(mFile);
			
			int len = 0;
			byte[] buffer = new byte[256];
			
			while (len!=-1 && !mIsCanceled) {
				len = input.read(buffer);
				
				if (len>0 && !mIsCanceled) {
					output.write(buffer, 0, len);
					output.flush();
				}
			}
			
			input.close();
			
			mIsFinished = true;
		}
		
		@Override
		public boolean isCancelable() {
			return true;
		}
		
		@Override
		public boolean cancel() {
			if (mIsFinished) {
				return mIsCanceled;
			}
			
			mIsCanceled = true;
			
			return true;
		}
		
	}
	
	public static String toString(InputStream input, String charsetName) throws IOException {
		ByteArrayOutputStream stringBuilder = new ByteArrayOutputStream();
		
		int len = 0;
		byte[] buffer = new byte[256];
		int readOffset = 0;
		
		while (len != -1) {
			len = input.read(buffer, readOffset, 256 - readOffset);
			
			if (len + readOffset >= 8) {
				int lengthToBeRead = ((len + readOffset) / 8) * 8;

				stringBuilder.write(buffer, 0, lengthToBeRead);

				readOffset = len - lengthToBeRead;
			}
		}
		
		return stringBuilder.toString(charsetName);
	}
	
}

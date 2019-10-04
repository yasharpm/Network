package com.yashoid.network.bodyloader;

import java.io.IOException;
import java.io.OutputStream;

public class StringBody implements BodyLoader {

	private static final String CONTENT_TYPE = "text/plain";

	private String mContentType;
	private String mBody;

	public StringBody(String body) {
		mContentType = CONTENT_TYPE;
		mBody = body;
	}

	public StringBody(String body, String contentType) {
		mContentType = contentType;
		mBody = body;
	}

	@Override
	public String getContentType() {
		return mContentType;
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
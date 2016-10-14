package com.yashoid.network.request;

import java.io.IOException;
import java.io.InputStream;

public class StringRequest extends NetworkRequest<String> {

	public StringRequest(String url) throws IOException {
		super(url);
	}

	@Override
	protected String parseResponse(InputStream stream) throws IOException {
		String response = toString(stream, "UTF-8");
		
		disconnect();
		
		return response;
	}

}

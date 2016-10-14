package com.yashoid.network.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.JsonReader;

public class JSONReaderRequest extends NetworkRequest<JsonReader> {

	public JSONReaderRequest(String url) throws IOException {
		super(url);
	}

	/**
	 * {@code JSONReaderRequest.disconnect()} must be called after your are done with the response.
	 */
	@Override
	protected JsonReader parseResponse(InputStream stream) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(stream));
		return reader;
	}
	
	@Override
	public void disconnect() {
		super.disconnect();
	}

}

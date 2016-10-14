package com.yashoid.network.request;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;

public class JSONArrayRequest extends NetworkRequest<JSONArray> {

	public JSONArrayRequest(String url) throws IOException {
		super(url);
	}

	@Override
	protected JSONArray parseResponse(InputStream stream) throws IOException {
		String sResponse = toString(stream, "UTF-8");
		
		try {
			return new JSONArray(sResponse);
		} catch (JSONException e) {
			throw new IOException("Server response is not in JSON format.", e);
		} finally {
			disconnect();
		}
	}

}

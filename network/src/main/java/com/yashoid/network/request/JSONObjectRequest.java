package com.yashoid.network.request;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONObjectRequest extends NetworkRequest<JSONObject> {
	
	public JSONObjectRequest(String url) throws IOException {
		super(url);
	}

	@Override
	protected JSONObject parseResponse(InputStream stream) throws IOException {
		String sResponse = toString(stream, "UTF-8");
		
		try {
			return new JSONObject(sResponse);
		} catch (JSONException e) {
			throw new IOException("Server response is not in JSON format.", e);
		} finally {
			disconnect();
		}
	}

}

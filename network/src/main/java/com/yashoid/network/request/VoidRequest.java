package com.yashoid.network.request;

import java.io.IOException;
import java.io.InputStream;

public class VoidRequest extends NetworkRequest<Void> {

	public VoidRequest(String url) throws IOException {
		super(url);
	}

	@Override
	protected Void parseResponse(InputStream stream) throws IOException {
		disconnect();
		return null;
	}

}

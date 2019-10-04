package com.yashoid.network.bodyloader;

import java.io.IOException;
import java.io.OutputStream;

public interface BodyLoader {

	String getContentType();

	void write(OutputStream output) throws IOException;

	boolean isCancelable();

	boolean cancel();
		
}
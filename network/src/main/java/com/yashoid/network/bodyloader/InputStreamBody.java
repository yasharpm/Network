package com.yashoid.network.bodyloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class InputStreamBody implements BodyLoader {

	private static final String CONTENT_TYPE = "application/octet-stream";

	private StreamOpener mStreamOpener;

	private boolean mIsCanceled = false;
	private boolean mIsFinished = false;

	public InputStreamBody(final InputStream stream) {
		mStreamOpener = new StreamOpener() {

			@Override
			public InputStream openStream() throws IOException {
				return stream;
			}

		};
	}

	public InputStreamBody(final File file) {
		mStreamOpener = new StreamOpener() {

			@Override
			public InputStream openStream() throws IOException {
				return new FileInputStream(file);
			}

		};
	}

	public InputStreamBody(final URL url) {
		mStreamOpener = new StreamOpener() {

			@Override
			public InputStream openStream() throws IOException {
				return url.openStream();
			}

		};
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}

	@Override
	public void write(OutputStream output) throws IOException {
		InputStream stream = mStreamOpener.openStream();

		int len = 0;
		byte[] buffer = new byte[256];

		while (len != -1 && !mIsCanceled) {
			len = stream.read(buffer);

			if (len > 0 && !mIsCanceled) {
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

	private interface StreamOpener {

		InputStream openStream() throws IOException;

	}

}
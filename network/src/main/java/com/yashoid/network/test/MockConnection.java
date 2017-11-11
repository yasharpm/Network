package com.yashoid.network.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by Yashar on 11/10/2017.
 */

public class MockConnection extends HttpURLConnection {

    private MockInternet mInternet;

    private URL mUrl;

    private long mTimeToGetConnected;
    private long mTimeToGetInput;

    private ByteArrayOutputStream mOutputStream = null;
    private ByteArrayInputStream mInputStream = null;

    protected MockConnection(URL url, long timeToGetConnected, long timeToGetInput, MockInternet internet) {
        super(url);

        mInternet = internet;

        mUrl = url;

        mTimeToGetConnected = timeToGetConnected;
        mTimeToGetInput = timeToGetInput;
    }

    @Override
    public void connect() throws IOException {
        if (connected) {
            return;
        }

        if (mTimeToGetConnected > getConnectTimeout()) {
            try {
                Thread.sleep(getConnectTimeout());
            } catch (InterruptedException e) {
                throw new IOException("Failed to test wait until throws timeout exception.", e);
            }

            throw new SocketTimeoutException("Test socket timed out.");
        }

        try {
            Thread.sleep(mTimeToGetConnected);
        } catch (InterruptedException e) {
            throw new IOException("Failed to test connect.", e);
        }

        connected = true;
    }

    @Override
    public void disconnect() {
        connected = false;
    }

    @Override
    public boolean usingProxy() {
        return false;
    }
    @Override
    public InputStream getInputStream() throws IOException {
        if (mInputStream != null) {
            return mInputStream;
        }

        if (mTimeToGetInput > getReadTimeout()) {
            try {
                Thread.sleep(getReadTimeout());
            } catch (InterruptedException e) {
                throw new IOException("Failed to test wait until throws timeout exception.", e);
            }

            throw new SocketTimeoutException("Test socket timed out.");
        }

        try {
            Thread.sleep(mTimeToGetInput);
        } catch (InterruptedException e) {
            throw new IOException("Failed to get test InputStream.", e);
        }

        String requestMethod = getRequestMethod();
        String body = mOutputStream == null ? null : mOutputStream.toString("UTF-8");

        MockResponse response = mInternet.call(requestMethod, mUrl.toExternalForm(), body);

        responseCode = response.responseCode;

        if (response.responseType == MockResponse.ResponseType.JSON) {
            mInputStream = new ByteArrayInputStream(response.responseBody.getBytes("UTF-8"));
        }
        else {
            mInputStream = new ByteArrayInputStream(response.responseBody.getBytes("US-ASCII"));
        }

        return mInputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (mOutputStream == null) {
            mOutputStream = new ByteArrayOutputStream();
        }

        return mOutputStream;
    }

}

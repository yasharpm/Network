package com.yashoid.network.test;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Created by Yashar on 11/12/2017.
 */

public class InternetForTest implements URLStreamHandlerFactory {

    private static InternetForTest mInstance = null;

    public static InternetForTest getInstance() {
        if (mInstance == null) {
            mInstance = new InternetForTest();
        }

        return mInstance;
    }

    public static void initialize() {
        URL.setURLStreamHandlerFactory(getInstance());
    }

    private long mTimeToConnect = 400;
    private long mTimeToGetInput = 200;

    private MockInternet mInternet = new MockInternet();

    public long getTimeToConnect() {
        return mTimeToConnect;
    }

    public void setTimeToConnect(long time) {
        mTimeToConnect = time;
    }

    public long getTimeToGetInput() {
        return mTimeToGetInput;
    }

    public void setTimeToGetInput(long time) {
        mTimeToGetInput = time;
    }

    public MockInternet getInternet() {
        return mInternet;
    }

    public void addServer(String baseUrl) {
        mInternet.addServer(baseUrl);
    }

    public void addGetHandler(String url, IRequestHandler handler) {
        mInternet.addGetHandler(url, handler);
    }

    public void addPostHandler(String url, IRequestHandler handler) {
        mInternet.addPostHandler(url, handler);
    }

    public void addDeleteHandler(String url, IRequestHandler handler) {
        mInternet.addDeleteHandler(url, handler);
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        return mUrlStreamHandler;
    }

    private URLStreamHandler mUrlStreamHandler = new URLStreamHandler() {

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return new MockConnection(u, mTimeToConnect, mTimeToGetInput, mInternet);
        }

    };

}

package com.yashoid.network.test;

import java.io.IOException;

/**
 * Created by Yashar on 11/12/2017.
 */

public abstract class RequestHandler {

    private String mMethod;
    private String mUrl;

    public RequestHandler(String method, String url) {
        mMethod = method;
        mUrl = url;
    }

    public String getMethod() {
        return mMethod;
    }

    public String getUrl() {
        return mUrl;
    }

    abstract protected void call(String url, String body, MockResponse response) throws IOException;

    protected boolean matches(String method, String url) {
        if (!mMethod.equals(method)) {
            return false;
        }

        return url.startsWith(mUrl);
    }

}

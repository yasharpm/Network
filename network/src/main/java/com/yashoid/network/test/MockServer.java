package com.yashoid.network.test;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Yashar on 11/12/2017.
 */

public class MockServer {

    private String mBaseUrl;

    private ArrayList<RequestHandler> mHandlers = new ArrayList<>(10);

    public MockServer(String baseUrl) {
        mBaseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return mBaseUrl;
    }

    public void addRequestHandler(RequestHandler handler) {
        mHandlers.add(handler);
    }

    public void addRequestHandler(String method, String url, final IRequestHandler handler) {
        addRequestHandler(new RequestHandler(method, url) {

            @Override
            protected void call(String url, String body, MockResponse response) throws IOException {
                handler.call(url, body, response);
            }

        });
    }

    public void addGetHandler(String url, IRequestHandler handler) {
        addRequestHandler("GET", url, handler);
    }

    public void addPostHandler(String url, IRequestHandler handler) {
        addRequestHandler("POST", url, handler);
    }

    public void addDeleteHandler(String url, IRequestHandler handler) {
        addRequestHandler("DELETE", url, handler);
    }

    protected MockResponse call(String method, String url, String body) throws IOException {
        RequestHandler handler = findHandler(method, url);

        if (handler == null) {
            throw new IOException("No mock handler found for url '" + mBaseUrl + url + "'.");
        }

        MockResponse response = new MockResponse();

        handler.call(url, body, response);

        return response;
    }

    public RequestHandler findHandler(String method, String url) {
        for (RequestHandler handler: mHandlers) {
            if (handler.matches(method, url)) {
                return handler;
            }
        }

        return null;
    }

}

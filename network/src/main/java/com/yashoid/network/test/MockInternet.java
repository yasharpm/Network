package com.yashoid.network.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Yashar on 11/12/2017.
 */

public class MockInternet {

    private ArrayList<MockServer> mServers = new ArrayList<>(2);

    public MockInternet() {

    }

    public void addServer(MockServer server) {
        mServers.add(server);
    }

    public void addServer(String baseUrl) {
        addServer(new MockServer(baseUrl));
    }

    public void addRequestHandler(String url, RequestHandler handler) {
        MockServer server = findServer(url);

        server.addRequestHandler(handler);
    }

    public void addRequestHandler(String method, String baseUrl, String url, IRequestHandler handler) {
        MockServer server = findServer(baseUrl);

        server.addRequestHandler(method, url, handler);
    }

    public void addRequestHandler(String method, String url, IRequestHandler handler) {
        MockServer server = findServer(url);

        url = url.substring(server.getBaseUrl().length());

        server.addRequestHandler(method, url, handler);
    }

    public void addGetHandler(String url, IRequestHandler handler) {
        addRequestHandler("GET", url, handler);
    }

    public void addPostHandler(String url, IRequestHandler handler) {
        addRequestHandler("POST", url, handler);
    }

    public void addPutHandler(String url, IRequestHandler handler) {
        addRequestHandler("PUT", url, handler);
    }

    public void addDeleteHandler(String url, IRequestHandler handler) {
        addRequestHandler("DELETE", url, handler);
    }

    protected MockResponse call(String method, String url, String body) throws IOException {
        MockServer server = findServer(url);

        if (server == null) {
            throw new IOException("Mock server not found for url '" + url + "'.");
        }

        url = url.substring(server.getBaseUrl().length());

        return server.call(method, url, body);
    }

    public MockServer findServer(String baseUrl) {
        for (MockServer server: mServers) {
            if (server.getBaseUrl().toLowerCase(Locale.US).startsWith(baseUrl.toLowerCase(Locale.US)) ||
                    baseUrl.toLowerCase(Locale.US).startsWith(server.getBaseUrl().toLowerCase(Locale.US))) {
                return server;
            }
        }

        return null;
    }

}

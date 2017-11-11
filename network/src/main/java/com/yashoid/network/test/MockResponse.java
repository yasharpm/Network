package com.yashoid.network.test;

/**
 * Created by Yashar on 11/12/2017.
 */

public class MockResponse {

    public enum ResponseType { JSON, BINARY };

    public ResponseType responseType;
    public int responseCode;
    public String responseBody;

}

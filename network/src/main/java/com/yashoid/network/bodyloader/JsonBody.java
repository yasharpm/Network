package com.yashoid.network.bodyloader;

public class JsonBody extends StringBody {

    private static final String CONTENT_TYPE = "application/json";

    public JsonBody(String body) {
        super(body, CONTENT_TYPE);
    }

}

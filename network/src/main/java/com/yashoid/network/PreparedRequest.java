package com.yashoid.network;

public interface PreparedRequest<T> {

    String getMethod();

    String getUrl();

    String[] getHeaderNames();

    String getHeader(String name);

    void setHeader(String name, String value);

    RequestResponse<T> call();

}

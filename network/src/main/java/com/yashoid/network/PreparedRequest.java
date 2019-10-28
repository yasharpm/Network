package com.yashoid.network;

public interface PreparedRequest<T> {

    String getMethod();

    String getUrl();

    String[] getHeaderNames();

    String getHeader(String name);

    void setHeader(String name, String value);

    void setConnectTimeout(int timeout);

    void setReadTimeout(int timeout);

    RequestResponse<T> call();

}

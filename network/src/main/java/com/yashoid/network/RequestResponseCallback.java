package com.yashoid.network;

public interface RequestResponseCallback<T> {

    void onRequestResponse(RequestResponse<T> response);

}

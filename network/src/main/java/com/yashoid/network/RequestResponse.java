package com.yashoid.network;

public class RequestResponse<T> {

    public static<T> RequestResponse<T> failedResponse(PreparedRequest request, int responseCode, Exception exception) {
        return new RequestResponse<>(request, false, responseCode, null, exception);
    }

    public static<T> RequestResponse<T> failedResponse(PreparedRequest request, int responseCode) {
        return new RequestResponse<>(request, false, responseCode, null, null);
    }

    public static<T> RequestResponse<T> successfulResponse(PreparedRequest request, int responseCode, T content) {
        return new RequestResponse<>(request, true, responseCode, content, null);
    }

    private PreparedRequest mRequest;

    private boolean mSuccessful;

    private int mResponseCode;

    private T mContent;

    private Exception mException;

    private RequestResponse(PreparedRequest request, boolean successful, int responseCode, T content,
                            Exception exception) {
        mRequest = request;
        mSuccessful = successful;
        mResponseCode = responseCode;
        mContent = content;
        mException = exception;
    }

    public PreparedRequest getRequest() {
        return mRequest;
    }

    public boolean isSuccessful() {
        return mSuccessful;
    }

    public int getResponseCode() {
        return mResponseCode;
    }

    public T getContent() {
        return mContent;
    }

    public Exception getException() {
        return mException;
    }

}

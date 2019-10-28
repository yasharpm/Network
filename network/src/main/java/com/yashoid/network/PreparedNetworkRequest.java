package com.yashoid.network;

import android.text.TextUtils;
import android.util.JsonReader;

import com.yashoid.network.bodyloader.BodyLoader;
import com.yashoid.yashson.Yashson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

class PreparedNetworkRequest<T> implements PreparedRequest {

    private static final String CONTENT_TYPE = "Content-Type";

    private Yashson mYashson;

    private Class<T> mReturnTypeClass;

    private String mMethod;
    private String mUrl;
    private BodyLoader mBody;
    private HashMap<String, String> mHeaders;

    private int mConnectTimeout;
    private int mReadTimeout;

    PreparedNetworkRequest(NetworkOperator operator, Class<T> returnTypeClass,
                                     String method, String url, BodyLoader body, String... headers) {
        mYashson = operator.getYashson();

        mReturnTypeClass = returnTypeClass;

        mMethod = method;
        mUrl = url;
        mBody = body;

        mHeaders = new HashMap<>(headers.length / 2);

        for (int i = 0; i < headers.length; i += 2) {
            mHeaders.put(headers[i], headers[i + 1]);
        }

        mConnectTimeout = operator.getReadTimeout();
        mReadTimeout = operator.getReadTimeout();
    }

    @Override
    public String getMethod() {
        return mMethod;
    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public String[] getHeaderNames() {
        return mHeaders.keySet().toArray(new String[0]);
    }

    @Override
    public String getHeader(String name) {
        return mHeaders.get(name);
    }

    @Override
    public void setHeader(String name, String value) {
        mHeaders.put(name, value);
    }

    @Override
    public void setConnectTimeout(int timeout) {
        mConnectTimeout = timeout;
    }

    @Override
    public void setReadTimeout(int timeout) {
        mReadTimeout = timeout;
    }

    @Override
    public RequestResponse<T> call() {
        URL url;

        int responseCode = 0;

        try {
            url = new URL(mUrl);
        } catch (MalformedURLException e) {
            return RequestResponse.failedResponse(this, responseCode, e);
        }

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(mConnectTimeout);
            connection.setReadTimeout(mReadTimeout);

            connection.setDoInput(true);

            if (!setMethod(connection, mMethod)) {
                return RequestResponse.failedResponse(this, responseCode, new Exception("Failed to set method '" + mMethod + "' on request."));
            }

            for (Map.Entry<String, String> header: mHeaders.entrySet()) {
                connection.addRequestProperty(header.getKey(), header.getValue());
            }

            if (mBody != null) {
                connection.addRequestProperty(CONTENT_TYPE, mBody.getContentType());

                connection.setDoOutput(true);

                mBody.write(connection.getOutputStream());
            }

            responseCode = connection.getResponseCode();

            InputStream input;

            try {
                input = connection.getInputStream();
            } catch (Throwable t) {
                input = connection.getErrorStream();
            }

            T content = null;

            if (input != null) {
                if (mReturnTypeClass.isAssignableFrom(InputStream.class)) {
                    content = mReturnTypeClass.cast(input);
                }
                else if (mReturnTypeClass.isAssignableFrom(String.class)) {
                    content = mReturnTypeClass.cast(toString(input, getCharset(connection.getContentType())));
                }
                else {
                    content = mYashson.parse(new JsonReader(new InputStreamReader(input)), mReturnTypeClass);
                }
            }

            if (content == null) {
                return RequestResponse.failedResponse(this, responseCode);
            }

            return RequestResponse.successfulResponse(this, responseCode, content);
        } catch (IOException e) {
            return RequestResponse.failedResponse(this, responseCode, e);
        } finally {
            if (connection != null && !mReturnTypeClass.isAssignableFrom(InputStream.class)) {
                try {
                    connection.disconnect();
                } catch (Throwable t) { }
            }
        }
    }

    private boolean setMethod(HttpURLConnection connection, String method) {
        try {
            connection.setRequestMethod(method);
        } catch (ProtocolException exception) {
            try {
                Field field = HttpURLConnection.class.getDeclaredField("method");

                boolean isAccessible = field.isAccessible();
                field.setAccessible(true);

                field.set(connection, method);

                field.setAccessible(isAccessible);
            } catch (NoSuchFieldException e) {
                return false;
            } catch (IllegalAccessException e) {
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        return true;
    }

    private static final String CHARSET_INTRO = "charset=";

    private static String getCharset(String contentType) {
        if (TextUtils.isEmpty(contentType)) {
            return "UTF-8";
        }

        String[] parts = contentType.split(";");

        for (String part: parts) {
            String trimmedPart = part.trim();

            int index = trimmedPart.indexOf(CHARSET_INTRO);

            if (index >= 0) {
                return trimmedPart.substring(CHARSET_INTRO.length());
            }
        }

        return "UTF-8";
    }

    private static String toString(InputStream input, String charsetName) throws IOException {
        ByteArrayOutputStream stringBuilder = new ByteArrayOutputStream();

        int len = 0;
        byte[] buffer = new byte[256];

        while (len != -1) {
            len = input.read(buffer);

            if (len > 0) {
                stringBuilder.write(buffer, 0, len);
            }
        }

        return stringBuilder.toString(charsetName);
    }

}

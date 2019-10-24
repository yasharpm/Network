package com.yashoid.network;

import com.yashoid.network.bodyloader.BodyLoader;
import com.yashoid.network.bodyloader.InputStreamBody;
import com.yashoid.network.bodyloader.JsonBody;
import com.yashoid.network.bodyloader.MultipartBody;
import com.yashoid.network.bodyloader.StringBody;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NetworkRequest<ReturnType> {

    private NetworkOperator mOperator;

    private Class<ReturnType> mReturnTypeClass;

    private String mMethod;
    private String mUrl;
    private Object mBody;
    private RequestHeaderWriter mHeaderWriter;
    private String[] mHeaders;

    protected NetworkRequest(NetworkOperator operator, Class<ReturnType> returnTypeClass,
                             String method, String url, Object body,
                             RequestHeaderWriter headerWriter, String... headers) {
        mOperator = operator;

        mReturnTypeClass = returnTypeClass;

        mMethod = method;
        mUrl = url;
        mBody = body;
        mHeaderWriter = headerWriter;
        mHeaders = headers;
    }

    /*
    JSON body: Is an object
    Multipart body: set of key and other body types
    call("version", 1, new Post(), "fdsfsfs", "ddfsfsd")
     */
    public PreparedRequest<ReturnType> prepare(Object... params) {
        HashMap<String, Object> keyValues = new HashMap<>(params.length / 2);

        Object body = mBody;

        int index = 0;

        while (index < params.length) {
            Object param = params[index];

            if (param instanceof String) {
                if (index + 1 < params.length) {
                    keyValues.put((String) param, params[++index]);
                }
                else if (body == null) {
                    body = param;
                }
                else {
                    throw new IllegalArgumentException("Request body is already specified.");
                }
            }
            else {
                body = param;
            }

            index++;
        }

        String url = prepareUrl(keyValues);

        BodyLoader preparedBody = null;

        if (hasRequestBody(mMethod)) {
            if (body == null) {
                preparedBody = createMultiPartBody(keyValues);
            }
            else if (body instanceof BodyLoader) {
                preparedBody = (BodyLoader) body;
            }
            else if (body instanceof String) {
                preparedBody = new StringBody((String) body);
            }
            else if (body instanceof JSONObject || body instanceof JSONArray) {
                preparedBody = new JsonBody(body.toString());
            }
            else if (body instanceof InputStream) {
                preparedBody = new InputStreamBody((InputStream) body);
            }
            else if (body instanceof File) {
                preparedBody = new InputStreamBody((File) body);
            }
            else if (body instanceof URL) {
                preparedBody = new InputStreamBody((URL) body);
            }
            else {
                preparedBody = new JsonBody(mOperator.getYashson().toJson(body));
            }
        }

        PreparedNetworkRequest<ReturnType> preparedRequest =
                new PreparedNetworkRequest<>(mOperator.getYashson(), mReturnTypeClass,
                        mMethod, url, preparedBody, mHeaders);

        if (mHeaderWriter != null) {
            mHeaderWriter.writeHeaders(preparedRequest);
        }

        return preparedRequest;
    }

    private String prepareUrl(HashMap<String, Object> keyValues) {
        String url = mUrl;

        List<String> keys = new ArrayList<>(keyValues.keySet());

        for (String key: keys) {
            String replacementPart = "{" + key + "}";

            int index = url.indexOf(replacementPart);

            if (index >= 0) {
                url = url.substring(0, index) + keyValues.remove(key) + url.substring(index + replacementPart.length());
            }
        }

        return url;
    }

    private MultipartBody createMultiPartBody(HashMap<String, Object> keyValues) {
        MultipartBody body = new MultipartBody();

        for (Map.Entry<String, Object> entry: keyValues.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof BodyLoader) {
                body.addPart(name, (BodyLoader) value);
            }
            else if (value instanceof String) {
                body.addPart(name, (String) value);
            }
            else if (value instanceof File) {
                body.addPart(name, (File) value);
            }
            else {
                throw new IllegalArgumentException("Multi part body entry must either be a BodyLoader, a String or a File.");
            }
        }

        return body;
    }

    private static boolean hasRequestBody(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

}

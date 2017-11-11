package com.yashoid.network.test;

import java.io.IOException;

/**
 * Created by Yashar on 11/12/2017.
 */

public interface IRequestHandler {

    void call(String url, String body, MockResponse response) throws IOException;

}

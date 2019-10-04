package com.yashoid.network.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yashoid.network.NetworkOperator;
import com.yashoid.network.NetworkRequest;
import com.yashoid.network.PreparedRequest;
import com.yashoid.network.Priorities;
import com.yashoid.network.RequestResponse;
import com.yashoid.network.RequestResponseCallback;
import com.yashoid.network.test.IRequestHandler;
import com.yashoid.network.test.InternetForTest;
import com.yashoid.network.test.MockResponse;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private NetworkOperator mNetworkOperator;

    private TextView mTextResults;

    private int mHighCounter = 0;
    private int mMediumCounter = 0;
    private int mLowCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupMock();

        mNetworkOperator = new NetworkOperator.Builder().build();

        mTextResults = findViewById(R.id.text_results);
    }

    public void getHighData(View v) {
        getData("High - " + (mHighCounter++), Priorities.HIGH);
    }

    public void getMediumData(View v) {
        getData("Medium - " + (mMediumCounter++), Priorities.MEDIUM);
    }

    public void getLowData(View v) {
        getData("Low - " + (mLowCounter++), Priorities.LOW);
    }

    private void getData(String name, String priority) {
        NetworkRequest<String> request = mNetworkOperator.getRequest(String.class, "GET", "https://www.google.com/" + name);

        PreparedRequest<String> preparedRequest = request.prepare();

        mNetworkOperator.runRequest(preparedRequest, priority, new RequestResponseCallback<String>() {

            @Override
            public void onRequestResponse(RequestResponse<String> response) {
                String result = mTextResults.getText().toString() + "\n" + response.getContent();

                mTextResults.setText(result);
            }

        });
    }

    private void setupMock() {
        InternetForTest internetForTest = InternetForTest.getInstance();

        internetForTest.addServer("https://www.google.com/");

        internetForTest.addGetHandler("https://www.google.com/", new IRequestHandler() {

            @Override
            public void call(String url, String body, MockResponse response) throws IOException {
                response.responseCode = 200;
                response.responseType = MockResponse.ResponseType.BINARY;
                response.responseBody = url;
            }

        });

        internetForTest.setTimeToConnect(2000);
        internetForTest.setTimeToGetInput(2000);

        InternetForTest.initialize();
    }

}

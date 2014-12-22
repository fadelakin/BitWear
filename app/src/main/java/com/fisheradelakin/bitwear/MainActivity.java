package com.fisheradelakin.bitwear;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    private TextView mTextView;
    private Button mButton;

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    public static final String URL = "https://api.bitcoinaverage.com/ticker/global/USD/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mButton = (Button) stub.findViewById(R.id.button);

                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // All the code for the button goes in here
                        if(isNetworkAvailable()) {
                            updatePrice();
                        }
                    }
                });
            }
        });
    }

    protected void updatePrice() {

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if(networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    protected JSONObject getJSON(String url) {

        // Make request
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        } catch(IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                sb.append(line + "n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        try {
            // try parsing the string to a JSON object
            jObj = new JSONObject(json);
        } catch(Exception e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return json string
        return jObj;
    }

    private class ParseJSON extends AsyncTask<String, String, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... args) {
            //JSONParser jParser = new JSONParser();
            // Getting JSON from URL
            JSONObject json = getJSON(URL);
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                // jokes = json.getJSONArray(TAG_JOKE);
                // getting json from url
                //JSONObject c = json.getJSONObject("value");
                // store json item
                //String joke = c.getString("joke");
                // set json data in textview
                //priceText = (TextView) findViewById(R.id.text);
                //priceText.setText();

                JSONObject p = json.getJSONObject("");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

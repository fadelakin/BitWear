package com.fisheradelakin.bitwear;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.*;

public class MainActivity extends Activity {

    public TextView mTextView;
    private Button mButton;

    public static final String URL = "https://api.bitcoinaverage.com/ticker/global/USD/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                mButton = (Button) stub.findViewById(R.id.button);
                mTextView = (TextView) stub.findViewById(R.id.text);

                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // All the code for the button goes in here
                        if(isNetworkAvailable()) {
                            updatePrice();
                        } else {
                            Toast.makeText(getApplicationContext(), "No network", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    protected void updatePrice() {
        new JSONParse().execute();
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

    private class JSONParse extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jParser = new JSONParser();
            // Getting JSON from URL
            JSONObject json = jParser.getJsonFromUrl(URL);
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                // jokes = json.getJSONArray(TAG_JOKE);
                // getting json from url
                JSONObject c = json.getJSONObject("");
                // store json item
                String price = c.getString("price");
                // set json data in textview
                mTextView.setText(price);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}

package com.fisheradelakin.bitwear;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONObject;

// TODO: Implement refresh button so if the user wants to make sure.
// TODO: Implement Android Wear version
// TODO: - The idea is that I should just be able to send the data to the watch
// TODO: Support other currencies using dropdown menu.

public class MainActivity extends ActionBarActivity {

    public static final String URL = "https://api.bitcoinaverage.com/ticker/global/USD/";
    TextView mPriceText;
    TextView mUpdated;
    SwipeRefreshLayout swipeLayout;
    Button mRefreshButton;
    GoogleApiClient mApiClient;

    private static final String START_ACTIVITY = "/start_activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // pull to refresh
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeLayout.setColorSchemeColors(Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isNetworkAvailable()) {
                    new JSONParse().execute();
                }

                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        swipeLayout.setRefreshing(false);
                    }
                }, 5000);
            }
        });

        if(isNetworkAvailable()) {
            JSONParse parse = new JSONParse();
            parse.execute();
        }

        // refresh button since pull to refresh is kinda wonky
        // it's kinda doing too much but whatevs. it's just a small project
        mRefreshButton = (Button) findViewById(R.id.refreshButton);
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JSONParse().execute();
            }
        });
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
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Getting price ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
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
            pDialog.dismiss();
            try {
                // store json item
                Log.i("JSON", json.toString());
                String price = json.getString("24h_avg");
                String date = json.getString("timestamp");
                Log.i("JSON", price);
                // set json data in text view
                mPriceText = (TextView) findViewById(R.id.priceTextView);
                mPriceText.setText("$" + price);

                mUpdated = (TextView) findViewById(R.id.lastUpdatedTextView);
                mUpdated.setText("Last Updated: " + date.replace("-", "").replace("0", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

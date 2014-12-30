package com.fisheradelakin.bitwear;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;

import org.json.JSONObject;

// TODO: Implement Android Wear version [IN PROGRESS]
// TODO: - The idea is that I should just be able to send the data to the watch

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final String URL = "https://api.bitcoinaverage.com/ticker/global/USD/";
    TextView mPriceText;
    TextView mUpdated;
    SwipeRefreshLayout swipeLayout;
    Button mRefreshButton;
    GoogleApiClient mApiClient;

    String price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

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

    @Override
    protected void onStart() {
        super.onStart();
        mApiClient.connect();
    }

    // Disconnect from the layer when the activity stops
    @Override
    protected void onStop() {
        if(null != mApiClient && mApiClient.isConnected()) {
            mApiClient.disconnect();
        }
        super.onStop();
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }

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
                price = json.getString("24h_avg");
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

    private class SendToDataLayerThread extends Thread {
        String path;
        String message;

        // Constructor to send message to the data layer
        SendToDataLayerThread(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
            for(Node node : nodes.getNodes()) {
                SendMessageResult result = Wearable.MessageApi.sendMessage(mApiClient, node.getId(), path, message.getBytes()).await();
                if(result.getStatus().isSuccess()) {
                    Log.v("myTag", "Message: {" + message + "} sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Log.v("myTag", "ERROR: failed to send message");
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        String message = ((TextView)findViewById(R.id.priceTextView)).getText().toString();
        // Requires a new thread to avoid blocking the UI
        new SendToDataLayerThread("/message_path", message).start();
    }
}

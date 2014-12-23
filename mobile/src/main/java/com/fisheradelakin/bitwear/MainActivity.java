package com.fisheradelakin.bitwear;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONObject;


public class MainActivity extends ActionBarActivity {

    public static final String URL = "https://api.bitcoinaverage.com/ticker/global/USD/";
    TextView mPriceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isNetworkAvailable()) {
            JSONParse parse = new JSONParse();
            parse.execute();
        }
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
                // jokes = json.getJSONArray(TAG_JOKE);
                // getting json from url
                //JSONObject c = json.getJSONObject(json);
                // store json item
                //JSONObject c = new JSONObject(json);
                String price = json.getString("24h_avg");
                // set json data in textview
                mPriceText = (TextView) findViewById(R.id.priceTextView);
                mPriceText.setText(price);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

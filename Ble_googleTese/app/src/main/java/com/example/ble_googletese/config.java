package com.example.ble_googletese;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

/**
 * DONOT EDIT ANY OTHER FILE
 * Edit only the block below, with instructions as in the setup
 */
public class config {


    SharedPreferences pref;
    SharedPreferences.Editor editor;

    /*-----------------------------------------------------------------------------------------------------------*/
    public String bdurl;
    public String port;
    public String location = "Google";// Specify the location you are in
    public String userType = "NON-ADMIN"; // SPECIFY IF USER IS AN ADMIN / NON-ADMIN
    public String email = choose(userType); // Enter UserType's Email
    public String password = "dummy"; // Leave this field as is
    public String client_id;
    public String client_secret;
    public String access_token = "";
    public String beacon_mac="C4:75:30:66:78:79";
    /*----------------------------------------------------------------------------------------------------------*/
    private String url;
    public String ip;

    public String getToken() throws ExecutionException, InterruptedException, JSONException {

        Context context = MainActivity.gettheContext();
        pref = context.getSharedPreferences("MyPref", 0);
        editor = pref.edit();

        editor.putString("user", email);
        editor.apply();

        bdurl = pref.getString("bdurl", "http://google-demo.andrew.cmu.edu");
        port = pref.getString("bdport", "82");
        ip = bdurl + ":" + port;

        client_id = pref.getString("client_id", "iTK3khxcNoep9E3i9zf1nzxYIVFmaxiQiSebr8oM");
        client_secret = pref.getString("client_secret", "a2Urb3iGSViurlSCeJFxF0uNmh1Il29ZAfkxntjHc6qVs4sHdX");

        url = ip + "/oauth/access_token/client_id="+client_id+"/client_secret="+client_secret;
        postAsync post = new postAsync();
        access_token  = post.execute().get();
        JSONObject accessJson = new JSONObject(access_token);
        access_token = accessJson.getString("access_token");
        Log.d("access",access_token);
        return access_token;
    }

    private class postAsync extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... params) {
            String jsonStr = "finished";
            ServiceHandler sh = new ServiceHandler();
            // Making a request to BD for data and getting response as a String
            try {
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonStr;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("Post", "Finished");
            return;
        }
    }

    String choose(String userType){
        if(userType.equals("ADMIN")){
            email = "admin@admin.com";
        }

        else {
            email = "non-admin@non-admin.com";
        }
        return email;
    }
}

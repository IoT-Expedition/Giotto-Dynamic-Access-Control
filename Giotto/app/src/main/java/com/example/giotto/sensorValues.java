package com.example.giotto;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by GK on 10/22/15.
 */
public class sensorValues {

    public static String url;
    private static String value = null;

    public static String values(String sens_uuid, String username) throws ExecutionException, InterruptedException, JSONException {
        postAsync process = new postAsync();
        url = "http://buildingdepot.andrew.cmu.edu:82/service/api/v2/data/id="+sens_uuid+"/email="+username+"/interval=200d/";
//        url = "http://buildingdepot.andrew.cmu.edu:82/service/api/v2/data/id=" + sens_uuid + "/interval=200d/";
        String value_fetch = process.execute().get();
        JSONObject val_json = new JSONObject(value_fetch);

//        Added to check if sensor value read success
        try {
            if (val_json.getString("response").equals("success")) {
                try {
                    JSONObject array = (JSONObject) val_json.getJSONObject("data").getJSONArray("series").get(0);
                    int getLastValue = array.getJSONArray("values").length();
                    JSONArray valz = (JSONArray) array.getJSONArray("values").get(getLastValue - 1);
                    value = valz.getString(2);
                    Log.d("Value",value);
                    return value;
                } catch (JSONException e) {
                    Log.d("HERE","EXCEPTION");
                    return "none";
                }
            } else
                return "none";
        } catch (JSONException e) {
            return "none";
        }
    }

    private static class postAsync extends AsyncTask<Void, Void, String> {
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
            return;
        }
    }


}

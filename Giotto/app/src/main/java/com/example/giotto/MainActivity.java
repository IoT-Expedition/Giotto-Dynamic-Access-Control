package com.example.giotto;

import android.app.Activity;
import android.content.Context;
import android.content.Entity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity{

    String check = "no";
    public static String username,url,userjson,userpwd,uuid;
    static config configuration = new config();
    String ip;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public static Context contextOfApplication;

    public static DefaultHttpClient httpClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contextOfApplication = getApplication();
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();

        ip = pref.getString("ip","http://bd-exp.andrew.cmu.edu:82");

        final EditText user_name = (EditText)  findViewById(R.id.user_name);
        user_name.setText(pref.getString("user","non-admin@non-admin.com"));

        final EditText user_password = (EditText) findViewById(R.id.user_password);
        user_password.setText("*****");
        user_password.setKeyListener(null);

        final Button setting = (Button) findViewById(R.id.setting);

        setting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor.putString("user", user_name.getText().toString());
                editor.apply();
                Intent settingsIntent = new Intent(getApplicationContext(), AppSettings.class);
                startActivity(settingsIntent);
            }

        });

        final Button enter = (Button) findViewById(R.id.credentials_button);

        enter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                editor.putString("user", user_name.getText().toString());

                try {
                    configuration.access_token = configuration.getToken();
                    editor.putString("access_token", configuration.access_token);
                    editor.apply();
                } catch (ExecutionException | JSONException | InterruptedException e) {
                    e.printStackTrace();
                }

                // Perform action on click
                Intent devices = new Intent(getApplicationContext(), Devices.class);
                username = user_name.getText().toString();
                username = username.replace(" ","");
                userpwd = user_password.getText().toString();
                //Make fields username and password compulsory
                if (username.equals("")) {
                    user_name.setError("Name is required!");
                    if (userpwd.equals("")) {
                        user_password.setError("Password is required!");
                    }
                } else if (userpwd.equals("")) {
                    user_password.setError("Password is required!");
                }

                if(!username.equals("") && !userpwd.equals("")){
                    try {
                      check = checkForCredentials();
                    } catch (ExecutionException | InterruptedException | JSONException e) {
                        e.printStackTrace();
                    }
                }

                devices.putExtra("username",username);
                devices.putExtra("uuid", uuid);
                Log.d("UUID", uuid);

                if(check.equals("yes")){
                    user_password.setText("");
                    user_name.setText("");
                    username = userpwd = "";
                    Toast.makeText(getApplicationContext(), "VALID CREDENTIALS",
                            Toast.LENGTH_SHORT).show();
                    startActivity(devices);
                }
                else{
                    user_password.setText("dummy");
                    userpwd = "dummy";
                    Toast.makeText(getApplicationContext(), "INVALID CREDENTIALS",
                            Toast.LENGTH_SHORT).show();}

            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    /*-----------------------------------Checks For User Credentials--------------------------------------------------*/

    private String checkForCredentials() throws ExecutionException, InterruptedException, JSONException,
            IllegalArgumentException{
        url = ip +":81" + "/api/search";
        postAsync post = new postAsync();
        userjson = post.execute().get();
        Log.d("user",String.valueOf(userjson));
        return checkUser();
    }

    private String checkUser() throws JSONException {
        String sens_type="dummy";

        if((userjson.charAt(0))!='{'){
            check = "no";
        }
        else {
            JSONObject jsonObj = new JSONObject(userjson);
            JSONArray sensor = jsonObj.getJSONArray("result");
            JSONObject getMeta = sensor.getJSONObject(0);
            JSONArray getTags = getMeta.getJSONArray("tags");

            sens_type = getTags.getJSONObject(0).getString("value");

//            Getting UUID of Location sensor
            uuid = getMeta.getString("name");
            Log.d("there", sens_type+uuid);

        }

        if(sens_type.equals(username)){
            check="yes";
        }
        else{check="no";}
        return check;
    }

    private class postAsync extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... params) {
            String jsonStr = "finished";
            // Making a request to BD for data and getting response as a String
            httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(url);

            postRequest.addHeader("Authorization", "Bearer "+configuration.access_token);
            postRequest.addHeader("content-type", "application/json");
            postRequest.addHeader("charset", "utf-8");
            String datatopost = "{\"data\":{\"Tags\":[\"user:"+username+"\"]}}";
            StringEntity input = null;
            try {
                input = new StringEntity(datatopost);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            input.setContentType("application/json");
            postRequest.setEntity(input);
            HttpResponse response = null;
            HttpEntity httpEntity;
            try {
                response = httpClient.execute(postRequest);
                httpEntity = response.getEntity();
                jsonStr = EntityUtils.toString(httpEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }

            httpClient.getConnectionManager().shutdown();
            return jsonStr;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("Post", "Finished");
            return;
        }
    }

    /*----------------------------------End Of Check For User Credentials--------------------------------------------------*/

    public static Context gettheContext(){
        return contextOfApplication;
    }

}
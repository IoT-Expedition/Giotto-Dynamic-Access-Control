package com.example.ble_googletese;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity{

    String check = "no";
    public static String username,url,userjson,userpwd,uuid;

    static config configuration = new config();
    String ip;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public static Context contextOfApplication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contextOfApplication = getApplication();
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();

        ip = pref.getString("ip","http://128.2.113.192:82");

        final EditText user_name = (EditText)  findViewById(R.id.user_name);
        user_name.setText(pref.getString("user","admin@admin.com"));

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

                try {
                    configuration.access_token = configuration.getToken();
                    editor.putString("access_token", configuration.access_token);
                    editor.apply();
                } catch (ExecutionException | JSONException | InterruptedException e) {
                    e.printStackTrace();
                }

                // Perform action on click
                Intent devices = new Intent(getApplicationContext(), Scan.class);
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
                    user_password.setText("");
                    user_name.setText("");
                    username = userpwd = "";
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
        url =  ip + "/api/sensor/list?filter=metadata&Name="+pref.getString("user","admin@admin.com");
        postAsync post = new postAsync();
        userjson = post.execute().get();
        Log.d("userhere",userjson);
        return checkUser();
    }

    private String checkUser() throws JSONException {
        String sens_type="dummy";

        if((userjson.charAt(0))!='{'){
            check = "no";
        }
        else {
            JSONObject jsonObj = new JSONObject(userjson);
            JSONArray sensor = jsonObj.getJSONArray("data");
            JSONObject getMeta = sensor.getJSONObject(0);
            sens_type = getMeta.getJSONObject("metadata").getString("Name");

//            Getting UUID of Location sensor
            uuid = getMeta.getString("name");

        }

        if(sens_type.equals(username)){
            check="yes";
        }
        else{check="no";}
        return check;
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
            Log.d("Post", "Finished");
            return;
        }
    }

    /*----------------------------------End Of Check For User Credentials--------------------------------------------------*/

    public static Context gettheContext(){
        return contextOfApplication;
    }

}

package com.example.ble_googletese;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity{

    String check = "no";
    public static String username,url,userjson,userpwd,uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView user_name = (TextView)  findViewById(R.id.username);
        final TextView user_password = (TextView) findViewById(R.id.pwd);
        final Button enter = (Button) findViewById(R.id.credentials_button);
        enter.setVisibility(View.GONE);

        username = user_name.getText().toString();
        userpwd = user_password.getText().toString();

//        enter.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // Perform action on click
//                Intent scan = new Intent(getApplicationContext(), Scan.class);
//
//                username = user_name.getText().toString();
//                userpwd = user_password.getText().toString();
//
//                try {
//                    check = checkForCredentials();
//                } catch (ExecutionException | InterruptedException | JSONException e) {
//                    e.printStackTrace();
//                }
//
//                scan.putExtra("username",username);
//                scan.putExtra("uuid", uuid);
//
//                if(check.equals("yes")){;
//                    Toast.makeText(getApplicationContext(), "VALID CREDENTIALS",
//                            Toast.LENGTH_SHORT).show();
////                    startActivity(scan);
//                }
//                else{
//                    Toast.makeText(getApplicationContext(), "INVALID CREDENTIALS",
//                            Toast.LENGTH_SHORT).show();}
//
//            }
//        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        Intent scan = new Intent(getApplicationContext(), Scan.class);
        try {
            check = checkForCredentials();
        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }

        scan.putExtra("username",username);
        scan.putExtra("uuid", uuid);
        startActivity(scan);
    }

    /*-----------------------------------Checks For User Credentials--------------------------------------------------*/

    private String checkForCredentials() throws ExecutionException, InterruptedException, JSONException,
            IllegalArgumentException{
        url = "http://buildingdepot.andrew.cmu.edu:82/service/api/v1/Name="+username+"/metadata";
        postAsync post = new postAsync();
        userjson = post.execute().get();
        return checkUser();
    }

    private String checkUser() throws JSONException {
        String sens_type="dummy";

        if((userjson.charAt(0))!='{'){
            check = "no";
        }
        else {
            JSONObject jsonObj = new JSONObject(userjson);
            JSONObject sensor = jsonObj.getJSONObject("data").getJSONObject("sensor_1");
            sens_type = sensor.getJSONObject("metadata").getString("Name");

//            Getting UUID of sensor
            uuid = sensor.getString("name");

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
}
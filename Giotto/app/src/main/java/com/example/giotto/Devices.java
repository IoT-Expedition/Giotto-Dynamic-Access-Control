package com.example.giotto;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Devices extends Activity implements AdapterView.OnItemSelectedListener {

    WemoConnect we = new WemoConnect();

    public static DefaultHttpClient httpClient;
    public static String datatopost, url;

    public boolean showProgress = false;
    public int secondclick;

    ListView listView;
    Spinner spinnertech;
    ToggleButton toggleButton;

    public static String availability = "yes/no", username, checkClickedRoom = "dummy",uuid;
    public static int i = 1, flag = 0, destroyFlag = 0, actuatorFlag = 0;

    ArrayList<String> sensors = new ArrayList<String>();
    ArrayAdapter<String> listadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        listView = (ListView) findViewById(R.id.listView);

        // Set the user name Entered
        Intent getuserIntent = getIntent();
        uuid = getuserIntent.getStringExtra("uuid");
        username = getuserIntent.getStringExtra("username");
        TextView user = (TextView) findViewById(R.id.user);
        user.setText(username);

        // Set Toggle Button
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setVisibility(View.GONE);

        // Spinner Drop down elements
        List<String> location = new ArrayList<String>();
        location.add("Atrium");
        location.add("Google");
        location.add("Random");

        secondclick = 1;

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, location);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinnertech = (Spinner) findViewById(R.id.spinner);
        spinnertech.setAdapter(adapter);
        spinnertech.setOnItemSelectedListener(this);

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.spinner) {
            // On selecting a spinner item
            secondclick++;
            if (secondclick > 3) {
                if (listadapter != null) {
                    listadapter.clear();
                    listadapter.notifyDataSetChanged();
                }
            }

            String item = parent.getItemAtPosition(position).toString();
            checkClickedRoom = item;
            if (item.equals("Google")) {
                showProgress = true;
                invalidateOptionsMenu();
                Log.d("Location: ", item);
                availability = "yes";
                try {
                    save();
                } catch (IOException | ExecutionException | InterruptedException | JSONException e) {
                    e.printStackTrace();
                }
            } else {
                destroyFlag = 1;
                availability = "no";
                try {
                    save();
                } catch (IOException | ExecutionException | InterruptedException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    else{
            Log.d("NOT","SPINNER");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d("Location: ", "None");
    }

    @Override
    protected void onResume() {
        toggleButton.setVisibility(View.GONE);
//        we.wemo(0);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        availability = "no";
        try {
            destroyFlag = 1;
            save();
        } catch (IOException | ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }
        we.wemo(0);
    }

    //    To get the list view for sensor data
    private void listcreate() {
        listadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, sensors);

        for(int i=0;i<=sensors.size()-1;i++){
            if(sensors.get(i).equals("You are not authenticated yet to view this List")){
                actuatorFlag = 1;
            }
        }
        if(actuatorFlag !=1){
            sensors.add("Actuator1");
            actuatorFlag = 0;
        }


//         Assign adapter to ListView
        listView.setAdapter(listadapter);
        makelistenToList();
        showProgress = false;
        invalidateOptionsMenu();
    }

    /*--------------------------------------------Save Location to BD-----------------------------------------------------*/


    public void save() throws IOException, ExecutionException, InterruptedException, JSONException {
        long unixTime = System.currentTimeMillis() / 1000L;
        datatopost = "{\"data\":[{\"value\":\"" + availability + "\",\"time\":" + unixTime + "}],\"value_type\":\"GK\"}";
//        if (username.equals("gokulk")) {
//            url = "http://giotto.wv.cc.cmu.edu:82/service/sensor/4ccb6c56-4766-4d17-9354-dae8ecab985b/gokulk@andrew.cmu.edu/timeseries";
//        } else {
//            url = "http://giotto.wv.cc.cmu.edu:82/service/sensor/d15948b2-195a-43dc-bc5f-6b57f5aba3a4/anind@cs.cmu.edu/timeseries";
//        }
//        url = "http://192.168.1.100:82/service/sensor/"+uuid+"/timeseries";
        url = "http://buildingdepot.andrew.cmu.edu:82/service/sensor/"+uuid+"/timeseries";
// Log.d("Availability",availability);
        post();
    }

    private void post() throws IOException, RuntimeException, ExecutionException, InterruptedException, JSONException {
        if(destroyFlag==1) flag = 0;
        //Async Task Starts here
        postAsync p = new postAsync();
        String t = String.valueOf(p.execute().get());
//        Log.d("After Execute", t);
        if (destroyFlag == 0) {
            sensorlist();
        }
        destroyFlag = 0;
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
            if (flag == 0) {
                Log.d("Post", "POSTING");
                httpClient = new DefaultHttpClient();
                HttpPost postRequest = new HttpPost(url);
                StringEntity input = null;
                try {
                    input = new StringEntity(datatopost);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                input.setContentType("application/json");
                postRequest.setEntity(input);
                HttpResponse response = null;
                try {
                    response = httpClient.execute(postRequest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                httpClient.getConnectionManager().shutdown();
            } else {
                flag = 0;
                // Making a request to BD for data and getting sensorList response as a String
                try {
                    jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    /*--------------------------------------End Of Save Location to BD-----------------------------------------------------*/

    /*------------------------------------------Fetch Sensor data from BD--------------------------------------------------*/

    private void sensorlist() throws ExecutionException, InterruptedException, JSONException {

        postAsync process = new postAsync();
        flag = 1;
        url = "http://buildingdepot.andrew.cmu.edu:82/service/api/v1/Floor=3/tags";
        String sensor_list = process.execute().get();

        // Find number of Objects in data
        JSONObject jsonObj = new JSONObject(sensor_list);
        int len = jsonObj.getJSONObject("data").length();

        for (int i = 1; i <= len; i++) {

            JSONObject sensor = jsonObj.getJSONObject("data").getJSONObject("sensor_" + i);
            String sens_uuid = sensor.getString("name");
            String sens_type = sensor.getJSONObject("metadata").getString("Type");

            String value_sensor = sensorValues.values(sens_uuid, username);

            // Put the sensors into the array list
            if(!(value_sensor.equals("none"))) {
                sensors.add(sens_type + "\n" + value_sensor);
            }
            else {
                sensors.add("You are not authenticated yet to view this List");
            }
//        }
//        if(actuatorFlag ==1){
//            sensors.add("Actuator1");
//            actuatorFlag = 0;
        }
        listcreate();
    }

    /*-----------------------------------End Of Fetch Sensor data from BD--------------------------------------------------*/

    /*----------------------------------Loading icon in Action Bar---------------------------------------------------------*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Menu optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        if (showProgress) {
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_progress);
        } else {
            menu.findItem(R.id.menu_refresh).setActionView(null);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
//                Toast.makeText(getApplicationContext(),username,Toast.LENGTH_LONG).show();
                try {
                    if(listadapter!=null){
                        listadapter.clear();
                        listadapter.notifyDataSetChanged();
                    }
                    if (checkClickedRoom.equals("Google")){
                        actuatorFlag = 0;
                        sensorlist();
                    }
                } catch (ExecutionException | JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }

  /*----------------------------------End Of Loading icon in Action Bar------------------------------------------------*/

    // Disable back button
    @Override
    public void onBackPressed() { }

    private void makelistenToList(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if(parent.getItemAtPosition(position).equals("Actuator1")){
                    // Actuate the module

//                    Log.d("Here",we.getStatefunction());
//                    if(toggleButton.isChecked()){
//                        toggleButton.setText("ON");
//                    }
//                    else {
//                        toggleButton.setText("OFF");
//                    }
//                    if(we.getStatefunction().equals("ON"))
//                        toggleButton.setText(toggleButton.getTextOn());
//                    else
//                        toggleButton.setText(toggleButton.getTextOff());

                    toggleButton.setVisibility(View.VISIBLE);
                    if (toggleButton.getText().equals("ON")) toggleButton.toggle();
                    toggleButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (toggleButton.getText().equals("ON")) {
                                we.wemo(1);
                                Toast.makeText(getApplicationContext(), "ON", Toast.LENGTH_SHORT).show();
                            } else {
                                we.wemo(0);
                                Toast.makeText(getApplicationContext(), "OFF", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else toggleButton.setVisibility(View.GONE);

            }
        });
    }
}


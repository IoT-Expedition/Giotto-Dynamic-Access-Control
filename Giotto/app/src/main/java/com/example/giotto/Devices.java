package com.example.giotto;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Devices extends Activity implements AdapterView.OnItemSelectedListener {

    WemoConnect we = new WemoConnect();
    config configuration = new config();
    String ip;
    SharedPreferences pref;


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

        getActionBar().setTitle("Control");

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        ip = pref.getString("ip", "https://<URL here>");

        listView = (ListView) findViewById(R.id.listView);

        // Set the user name Entered
        Intent getuserIntent = getIntent();
        uuid = getuserIntent.getStringExtra("uuid");
        username = getuserIntent.getStringExtra("username");

        // Set Toggle Button
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setVisibility(View.GONE);

        // Spinner Drop down elements
        List<String> location = new ArrayList<String>();
//        location.add("Atrium");
        location.add("Random");
        location.add(configuration.location);

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
            if (item.equals(configuration.location)) {
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
            sensors.add("Connect to a Wemo");
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
        datatopost = "[{\"sensor_id\":\""+uuid+"\",\"samples\":[{\"value\":\""+availability+"\",\"time\":"+unixTime+"}]}]";
        Log.d("idhere",datatopost);
        url = ip + "/api/sensor/timeseries";
        post();
    }

    private void post() throws IOException, RuntimeException, ExecutionException, InterruptedException, JSONException {
        if(destroyFlag==1) flag = 0;
        //Async Task Starts here
        postAsync p = new postAsync();
        Log.d("execute",uuid);
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
                Log.d("Post", "POSTINGS");

                String access_token = pref.getString("access_token", "access");

                httpClient = new DefaultHttpClient();
                HttpPost postRequest = new HttpPost(url);

                postRequest.addHeader("Authorization", "Bearer "+access_token);
                postRequest.addHeader("content-type", "application/json");
                postRequest.addHeader("charset", "utf-8");

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
        url = ip + "/api/sensor/list?filter=tags&Giotto_dac=Sensor%20Tag";
        String sensor_list = process.execute().get();

        // Find number of Objects in data
        JSONObject jsonObj = new JSONObject(sensor_list);
        JSONArray sensor = jsonObj.getJSONArray("data");
        int len = sensor.length();

        for (int i = 1; i <= len; i++) {

            JSONObject getMeta = sensor.getJSONObject(0);
            String sens_type = getMeta.getJSONObject("metadata").getString("Type");
            String sens_uuid = getMeta.getString("name");

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
                if(parent.getItemAtPosition(position).equals("Controller")){
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


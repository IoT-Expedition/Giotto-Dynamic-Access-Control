package com.example.ble_googletese;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.concurrent.ExecutionException;

public class Dummy extends Activity {

    private static final long SCAN_PERIOD = 5000;
    private static Handler mHandler;
    private static ArrayList<BluetoothDevice> mLeDeviceListAdapter;
    private static BluetoothAdapter mBluetoothAdapter;
    int flagForscan = 0;

    WemoConnect we = new WemoConnect();
    config configuration = new config();
    String ip;
    SharedPreferences pref;

    public static DefaultHttpClient httpClient;
    public static String datatopost, url;

    public static String availability = "yes/no", username;
    String uuid;
    public static int i = 1, flag = 0, destroyFlag = 0, actuatorFlag = 0;

    ArrayList<String> sensors = new ArrayList<String>();
    ArrayAdapter<String> listadapter;

    ListView listView;
    ToggleButton toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dumm);

        getActionBar().setTitle("Control");
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        ip = pref.getString("ip", "https://<URL here>");

        Intent getlocation = getIntent();
        uuid = getlocation.getStringExtra("uuid");
        username = getlocation.getStringExtra("username");

        listView = (ListView) findViewById(R.id.listView);

        // Set Toggle Button
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setVisibility(View.GONE);
    }

    /*--------------------------------------------Save Location to BD-----------------------------------------------------*/


    public void save() throws IOException, ExecutionException, InterruptedException, JSONException {
        long unixTime = System.currentTimeMillis() / 1000L;

//        datatopost = "[{\"sensor_id\":\""+uuid+"\",\"samples\":[{\"value\":\""+availability+"\",\"time\":"+unixTime+"}]}]";
        datatopost = "[{\"sensor_id\":\""+uuid+"\",\"samples\":[{\"value\":\""+availability+"\",\"time\":"+unixTime+"}]}]";
        Log.d("idhere",datatopost);
        url = ip + "/api/sensor/timeseries";
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

                pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                String access_token = pref.getString("access_token", null);

                httpClient = new DefaultHttpClient();
                HttpPost postRequest = new HttpPost(url);

                postRequest.addHeader("Authorization", "Bearer " + access_token);
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
                Log.d("Location","posted");
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

    @Override
    protected void onResume() {
        super.onResume();
        try {
            availability = "yes";
            save();
        } catch (IOException | InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
//        we.wemo(0);
        mHandler = new Handler();
        newAsync p = new newAsync();
        p.execute();

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
//        we.wemo(0);
        finish();
    }


    private void listcreate() {
        listadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, sensors);

//         Assign adapter to ListView
        listView.setAdapter(listadapter);
        makelistenToList();
    }


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
                actuatorFlag = 1;
            }
            else {
                sensors.add("You are not authenticated yet to view this List");
            }
        }
        if(actuatorFlag ==1){
            sensors.add("Connect to a Wemo");
            actuatorFlag = 0;
        }
        listcreate();
    }

    private void makelistenToList(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if (parent.getItemAtPosition(position).equals("Light")) {
//                     Actuate the module
//                    toggleButton.setText(we.getStatefunction());
//                    if(we.getStatefunction().equals("ON"))
//                        toggleButton.setTextOn("ON");
//                    else
//                        toggleButton.setTextOff("OFF");
//                    toggleButton.setVisibility(View.VISIBLE);
//                    if (toggleButton.getText().equals("ON")) toggleButton.toggle();
//                    toggleButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (toggleButton.getText().equals("ON")) {
//                                Toast.makeText(getApplicationContext(), "ON", Toast.LENGTH_SHORT).show();
//                                we.wemo(1);
//                            } else {
//                                Toast.makeText(getApplicationContext(), "OFF", Toast.LENGTH_SHORT).show();
//                                we.wemo(0);
//                            }
//                        }
//                    });
                    toggleButton.setVisibility(View.VISIBLE);/////////////REMOVE
                } else toggleButton.setVisibility(View.GONE);



            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Menu optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dumm, menu);
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
                    toggleButton.setText("OFF");
                    toggleButton.setVisibility(View.GONE);
                    sensorlist();
                } catch (ExecutionException | JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }


    @Override
    public void onBackPressed() {}


    @Override
    public void onStop() {
        super.onStop();
        onDestroy();
    }

    private class newAsync extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... params) {
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            mLeDeviceListAdapter = new ArrayList<BluetoothDevice>();
            if(!mLeDeviceListAdapter.isEmpty())
                mLeDeviceListAdapter.clear();
            scanLeDevice(true);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("HERE", "Finished");
            return;
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    repeatAsync();
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }


    }
    private void repeatAsync() {
        int length = mLeDeviceListAdapter.size();
        for (int i=0;i<length;i++) {
            if (mLeDeviceListAdapter.get(i).getAddress().equals("C4:75:30:66:78:79"))
                flagForscan=1;
        }
        if(flagForscan == 1){
            Log.d("HERE", String.valueOf(flagForscan));
            flagForscan = 0;
            newAsync newp = new newAsync();
            newp.execute();
        }
        else {
            Log.d("HERE", String.valueOf(flagForscan));
            Intent gotoMain = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(gotoMain);
        }
    }
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    String locresponse=null;
                    if(!mLeDeviceListAdapter.contains(device) || mLeDeviceListAdapter.isEmpty()) {
                        Log.d("hellomike", device.getName() + " " + device.getAddress());
                        mLeDeviceListAdapter.add(device);
                    }
                }
            };



}

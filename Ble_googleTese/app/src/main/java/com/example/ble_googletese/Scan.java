package com.example.ble_googletese;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class Scan extends Activity {

    config configuration = new config();
    String ip = configuration.ip;
    SharedPreferences pref;

    private ArrayList<BluetoothDevice> mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private static String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        getActionBar().setTitle(R.string.title_devices);

        mHandler = new Handler();
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
//            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
//            menu.findItem(R.id.menu_refresh).setActionView(
//                    R.layout.actionbar_intermediate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        // Initializes list view adapter.
        mLeDeviceListAdapter = new ArrayList<BluetoothDevice>();
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        if(!mScanning){
//            System.out.println(mLeDeviceListAdapter.size());

        }
        invalidateOptionsMenu();
    }


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String locresponse = null;
                            if (!mLeDeviceListAdapter.contains(device) || mLeDeviceListAdapter.isEmpty()) {
                                Log.d("hello", device.getName() + " " + device.getAddress());
                                mLeDeviceListAdapter.add(device);
//                                    System.out.println(device.getAddress()+device.getName());
//                                try {
//                                    locresponse = findLocation(device.getAddress());
//                                } catch (ExecutionException | InterruptedException | JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                Log.d("debug","here");
//                                if(!locresponse.equals("no")) {
//                                    Log.d("Location", locresponse);
//                                    Intent getFromMain = getIntent();
//                                    String uuid = getFromMain.getStringExtra("uuid");
//                                    String username = getFromMain.getStringExtra("username");
//
//                                    Intent dummy_Intent = new Intent(getApplicationContext(),Dummy.class);
//                                    dummy_Intent.putExtra("location",locresponse.trim());
//                                    dummy_Intent.putExtra("uuid",uuid);
//                                    dummy_Intent.putExtra("username",username);
//                                    startActivity(dummy_Intent);
//                                    finish();
//                                }

//                                C8:39:57:6B:A8:57
                                pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                                String blemac = pref.getString("bmac","C4:75:30:66:78:79");
                                if (device.getAddress().equals(blemac)) {
                                    locresponse = "Google";
                                    Intent getFromMain = getIntent();
                                    String uuid = getFromMain.getStringExtra("uuid");
                                    String username = getFromMain.getStringExtra("username");

                                    Intent dummy_Intent = new Intent(getApplicationContext(),Dummy.class);
                                    dummy_Intent.putExtra("location",locresponse.trim());
                                    dummy_Intent.putExtra("uuid",uuid);
                                    dummy_Intent.putExtra("username",username);
                                    startActivity(dummy_Intent);
                                    finish();
                                }

                            }
                        }
                    });
                }
            };

    private String findLocation(String mac)throws ExecutionException, InterruptedException, JSONException,
            IllegalArgumentException{
        url = "http://buildingdepot.andrew.cmu.edu:82/service/api/v1/MAC="+mac+"/metadata";
        postAsync post = new postAsync();
        String locrespBD = post.execute().get();
        return checkLocBD(locrespBD);
    }

    private String checkLocBD(String locrespBD) throws JSONException {
        String flaghere="no";
        if((locrespBD.charAt(0))!='{'){
            flaghere = "no";
        }
        else{
            JSONObject jsonObj = new JSONObject(locrespBD);
            JSONObject sensor = jsonObj.getJSONObject("data").getJSONObject("sensor_1");
            String location = sensor.getJSONObject("metadata").getString("Location");
            flaghere = location;
        }
        return flaghere;
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

    @Override
    public void onBackPressed() {}

    @Override
    public void onDestroy(){
        super.onDestroy();
        scanLeDevice(false);
        finish();
    }

    @Override
    public void onStop(){
        super.onStop();
        onDestroy();
    }
}
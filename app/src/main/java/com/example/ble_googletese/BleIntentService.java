package com.example.ble_googletese;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by GK on 11/12/15.
 */
public class BleIntentService extends IntentService {

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private Handler mHandler;
    private ArrayList<BluetoothDevice> mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    public static String url;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BleIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mHandler = new Handler();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mLeDeviceListAdapter = new ArrayList<BluetoothDevice>();
        scanLeDevice(true);

    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                            String locresponse=null;
                            if(!mLeDeviceListAdapter.contains(device) || mLeDeviceListAdapter.isEmpty()) {
                                Log.d("hello", device.getName() + " " + device.getAddress());
                                mLeDeviceListAdapter.add(device);
//                                    System.out.println(device.getAddress()+device.getName());
                                try {
                                    locresponse = findLocation(device.getAddress());
                                } catch (ExecutionException | InterruptedException | JSONException e) {
                                    e.printStackTrace();
                                }
                                if (!locresponse.equals("no")) {
                                    Log.d("Location", locresponse);
                                }

                            }
                }
            };

    private String findLocation(String mac)throws ExecutionException, InterruptedException, JSONException,
            IllegalArgumentException{
        url = "http://128.2.177.65:82/service/api/v1/MAC="+mac+"/metadata";
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

}

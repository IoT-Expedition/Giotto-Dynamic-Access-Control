package com.example.giotto;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;


public class WemoConnect {
    static String url, deviceURI;
    static StringEntity soapMessageSet,soapMessageGet,soap;
    int portNum[]={49152,49153,49154,49155};
    static int getSet = 0;
    static char getState;
    static int state;

    public void wemo(int stateTopush){
        deviceURI = "192.168.1.201";

//        makesoapGet();
//
//        makepostrequest();

        getSet = 1;


        // Pass the state needed to set device message
        makesoapSet(stateTopush);

        // Post to the device with parameters of set
        makepostrequest();
    }

    private void makepostrequest() {
        for(int i=0;i<=3;i++) {
            url = "http://" + deviceURI + ":" + portNum[i] + "/upnp/control/basicevent1";
            postAsync g = new postAsync();
            try {
                String resp = g.execute().get();
                if(!resp.equals("portRefused")){
                    getState = resp.charAt(216);
                    Log.d("host",resp);
//                    Log.d("host", getState);
                    System.out.println("character is " + getState);
                    break;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void makesoapGet() {
        try {
            soapMessageGet = new StringEntity("<?xml version=1.0 encoding=utf-8?><s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                    "s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                    "<s:Body><u:GetBinaryState xmlns:u=\"urn:Belkin:service:basicevent:1\">" +
                    "<BinaryState>1</BinaryState></u:GetBinaryState></s:Body></s:Envelope>");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void makesoapSet(int state) {
        try {
            soapMessageSet = new StringEntity("<?xml version=1.0 encoding=utf-8?><s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                    "s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                    "<s:Body><u:SetBinaryState xmlns:u=\"urn:Belkin:service:basicevent:1\">" +
                    "<BinaryState>"+state+"</BinaryState></u:SetBinaryState></s:Body></s:Envelope>");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static class postAsync extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... params) {
            String jsonStr = "portRefused";
            ServiceHandler sh = new ServiceHandler();
            // Making a request to BD for data and getting response as a String
            if(getSet ==1) {
                try {
                    jsonStr = sh.maketheServiceCall(url, ServiceHandler.POST, soapMessageSet,getSet);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException f) {
                    Log.d("error", "Set here");
                    f.printStackTrace();
                }
            }
            else {
                try {
                    jsonStr = sh.maketheServiceCall(url, ServiceHandler.POST, soapMessageGet,getSet);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException f) {
                    Log.d("error", "Get here");
                    f.printStackTrace();
                }
            }
            Log.d("json", jsonStr);
            return jsonStr;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("Post", "Finished");
            return;
        }
    }

    public String getStatefunction(){
        deviceURI = "192.168.1.201";
        makesoapGet();
        getSet = 0;
        makepostrequest();
        if(getState == '8')
            return "ON";
        else
            return "OFF";
    }

}

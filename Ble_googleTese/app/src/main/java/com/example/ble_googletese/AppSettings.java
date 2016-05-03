package com.example.ble_googletese;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.ble_googletese.config;

public class AppSettings extends Activity {

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    config configuration = new config();

    String ip;
    EditText bdurl, bdport, client_id, client_secret, bmac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();

        bdurl = (EditText) findViewById(R.id.bdurl);
        bdport = (EditText) findViewById(R.id.bdport);

        bdurl.setText(pref.getString("bdurl", "https://cmu.buildingdepot.org"));
        bdport.setText(pref.getString("bdport", "82"));

        client_id = (EditText) findViewById(R.id.client_id);
        client_secret = (EditText) findViewById(R.id.client_secret);

        client_id.setText(pref.getString("client_id", "WO5xB2VyEfsQQRz6hhxC8Mucq47x3rmUcwFIPDG9"));
        client_secret.setText(pref.getString("client_secret", "B3FMqZhXVzTiPrJq6h3Da3YBCwxrpaDPnR4vldZqVnhlPNl9MY"));

        bmac = (EditText) findViewById(R.id.bmac);

        bmac.setText(pref.getString("bmac", "C4:75:30:66:78:79"));

        final Button finished = (Button) findViewById(R.id.finished);

        finished.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                editor.putString("bdurl", bdurl.getText().toString());
                editor.putString("bdport", bdport.getText().toString());

                editor.putString("client_id", client_id.getText().toString());
                editor.putString("client_secret", client_secret.getText().toString());

                editor.putString("bmac", bmac.getText().toString());

                ip = bdurl.getText().toString().trim() + ":" + bdport.getText().toString().trim();

                editor.putString("ip", ip);
                editor.apply();

                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainIntent);
            }

        });

    }

}

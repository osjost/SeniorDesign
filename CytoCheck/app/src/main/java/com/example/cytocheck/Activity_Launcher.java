package com.example.cytocheck;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import api.HandlerResponse;
import api.*;

public class Activity_Launcher extends AppCompatActivity {
    String linkString;
    private Button connectHRButton;
    private Button connectTempButton;
    private Button connectSensorsButton;
    private Button userManualButton;
    String hrUpper = "";
    String hrLower = "";
    String tempUpper = "";
    String tempLower = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Intent intent = getIntent();
        linkString = intent.getStringExtra("linkString");
        String token = intent.getStringExtra("token");
        String userID = intent.getStringExtra("userID");

        api global = api.getInstance();
        global.sendGetRequestWithHandlerWithToken(linkString + "threshold/" + userID + "/1", token, new HandlerResponse() {
            @Override
            public void handleResponse(String response) {

                try {
                    JSONObject patientHRThreshold = new JSONObject(response);
                    hrUpper = patientHRThreshold.getString("upper");
                    hrLower = patientHRThreshold.getString("lower");
                } catch (JSONException e) {

                }
            }
        });


        global.sendGetRequestWithHandlerWithToken(linkString + "threshold/" + userID + "/2", token, new HandlerResponse() {
            @Override
            public void handleResponse(String response) {

                try {
                    JSONObject patientTempThreshold = new JSONObject(response);
                    tempUpper = patientTempThreshold.getString("upper");
                    tempLower = patientTempThreshold.getString("lower");
                } catch (JSONException e) {

                }
            }
        });

        // Text Initialization
        TextView devicePairing = (TextView) findViewById(R.id.tv_connectSensor);
        devicePairing.setText("Connect Biosensors");

        // Button Initialization
        connectSensorsButton = (Button) findViewById(R.id.connectButton);
        userManualButton = (Button) findViewById(R.id.manualButton);

        // Button Listener
        userManualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Display User Manual
                Intent intent = new Intent(Activity_Launcher.this, Activity_UserManual.class);
                intent.putExtra("linkString", linkString);
                intent.putExtra("token", token);
                intent.putExtra("userID", userID);
                startActivity(intent);
                finish();
            }
        });

        // Button Listener
        connectSensorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Begin Pairing Mode
                if (!isBluetoothEnabled()) {
                    Toast.makeText(Activity_Launcher.this, "Please turn on bluetooth", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(ActivityCompat.checkSelfPermission(Activity_Launcher.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Activity_Launcher.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 100);
                    return;
                }
                Intent intent = new Intent(Activity_Launcher.this, Activity_SensorConnecter.class);
                intent.putExtra("linkString", linkString);
                intent.putExtra("token", token);
                intent.putExtra("userID", userID);
                intent.putExtra("hrLower", hrLower);
                intent.putExtra("hrUpper", hrUpper);
                intent.putExtra("tempLower", tempLower);
                intent.putExtra("tempUpper", tempUpper);
                startActivity(intent);
                finish();
            }
        });
    }
    public boolean isBluetoothEnabled() {
        BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return myBluetoothAdapter.isEnabled();
    }
}
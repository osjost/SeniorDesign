package com.example.cytocheck;

/*
 * Copyright (c) Garmin Canada Inc. 2019
 * All rights reserved.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.DataState;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import api.*;

/**
 * Base class to connects to Heart Rate Plugin and display all the event data.
 */
public abstract class Activity_BiometricViewer extends Activity {
    // Needed to access the plug in API
    protected abstract void requestAccessToPcc();

    /* ANT+ needed members */
    AntPlusHeartRatePcc hrPcc = null;
    protected PccReleaseHandle<AntPlusHeartRatePcc> releaseHandle = null;
    TextView tv_status;             // Status of the device connected
    TextView tv_computedHeartRate;  // Actual read heart rate
    /* -------------------------------------------------------- */

    /* BLE needed members */
    protected BluetoothDevice btDevice;
    TextView tv_tempData;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    final String DEVICE = "Device";
    final String FAHRENHEIT = "°F";
    final String CELSIUS = "°C";
    final String DEVICENAME = "Device: ";
    private BluetoothGatt mBluetoothGatt;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mTemperatureCharacteristic;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private double mTemperature = 0.0;
    TextView tv_degreesUnit;

    /* Backend Link elements */
    private String linkString;
    private String userID;
    private String token;
    private String hrLower;
    private String hrUpper;
    private String tempLower;
    private String tempUpper;
    private int HRdataCount = 0;
    private double HRavgData = 0;
    private int HRdataSum = 0;

    /* -------------------------------------------------------- */

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(btDevice.getAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case BluetoothLeService.ACTION_GATT_CONNECTED: {
                    setConnecting(false);
                    displayTemperature();
                }
                break;
                case BluetoothLeService.ACTION_GATT_DISCONNECTED: {
                    clearUI();
                    setConnecting(true);
                    displayTemperature();
                }
                break;
                case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED: {
                    listGattServices(mBluetoothLeService.getSupportedGattServices());
                    setTemperatureNotification(true);
                }
                break;
                case BluetoothLeService.ACTION_TEMPERATURE_AVAILABLE: {
                    displayTemperature();
                }
                break;
                case BluetoothAdapter.ACTION_STATE_CHANGED: {
                    if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                        clearUI();
                        displayTemperature();
                    }
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleReset();
    }

    protected void setIntentStrings(String serverAddress, String userId, String atoken, String HRLower, String HRUpper, String TempLower, String TempUpper) {
        linkString = serverAddress;
        userID = userId;
        token = atoken;
        hrLower = HRLower;
        hrUpper = HRUpper;
        tempLower = TempLower;
        tempUpper = TempUpper;
    }

    private void initBLEConnection() {
        tv_tempData = findViewById(R.id.tv_tempData);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        setConnecting(true);

        displayTemperature();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    /**
     * Resets the PCC connection to request access again and clears any existing display data.
     */
    protected void handleReset() {
        //Release the old access if it exists
        if(releaseHandle != null) { releaseHandle.close(); }
        requestAccessToPcc();
    }

    // Copyright to Garmin. Used this function to set up the data display
    protected void showDataDisplay(String status) {
        setContentView(R.layout.activity_biometric_viewer);

        // Text view instantiation
        tv_status = (TextView)findViewById(R.id.textView_Status);
        tv_computedHeartRate = (TextView)findViewById(R.id.textView_ComputedHeartRate);

        // Text view initialization
        tv_status.setText(status);
        tv_computedHeartRate.setText("---");

        // Text view initialization
        tv_degreesUnit = (TextView)findViewById(R.id.tv_degreesF);
        //This can be changed to celsius. Future works can be to make a
        tv_degreesUnit.setText(FAHRENHEIT);

    }

    /**
     * All Copyrights Rese: Garmin Canada
     * Switches the active view to the data display and subscribes to all the data events
     */
    public void subscribeToHrEvents() {
        hrPcc.subscribeHeartRateDataEvent(new IHeartRateDataReceiver() {
            @Override
            public void onNewHeartRateData(final long estTimestamp, EnumSet<EventFlag> eventFlags, final int computedHeartRate, final long heartBeatCount, final BigDecimal heartBeatEventTime, final DataState dataState) {
                String tempHRText = "";

                /* This is where HR Data is received, computedHeartRate is the variable with the HR value. */
                //TODO SERVER REQUEST WITH DATA

                if (!DataState.ZERO_DETECTED.equals(dataState)) {
                    tempHRText = String.valueOf(computedHeartRate);

                    api global = api.getInstance();
                    HRdataCount += 1; //increment data count to say that our app has received one more data point
                    HRdataSum += computedHeartRate;
                    HRavgData = HRdataSum / HRdataCount;
                    if (HRdataCount >= 20) { //userDefined number of how many cycles to send data after
                        HRdataCount = 0;
                        HRdataSum = 0;
                        //Send post with average data
                        JSONObject sensorData = new JSONObject();
                        try {
                            sensorData.put("reading", String.format("%.5f", HRavgData));
                            sensorData.put("sensor_id", "1");
                            sensorData.put("user_id", userID);
                        } catch (JSONException e) {

                        }
                        String sendDataString = linkString + "readings";
                        global.sendPostRequestWithHandlerWithToken(sendDataString, sensorData, token, new HandlerResponse() {
                            @Override
                            public void handleResponse(String response) {

                            }

                        });
                    }
                } else {
                    tempHRText = "NaN";
                }

                final String textHeartRate = tempHRText + " BPM";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_computedHeartRate.setText(textHeartRate);
                    }
                });
            }
        });
    }

    /**
     * Credits: Garmin Canada
     * Handles the state of the devices ANT+ connection
     */
    protected IPluginAccessResultReceiver<AntPlusHeartRatePcc> base_IPluginAccessResultReceiver = new IPluginAccessResultReceiver<AntPlusHeartRatePcc>() {
                //Handle the result, connecting to events on success or reporting failure to user.
                @Override
                public void onResultReceived(AntPlusHeartRatePcc result, RequestAccessResult resultCode, DeviceState initialDeviceState) {
                    showDataDisplay("Connecting...");
                    switch(resultCode) {
                        case SUCCESS:
                            hrPcc = result;
                            tv_status.setText(result.getDeviceName() + ": " + initialDeviceState);
                            subscribeToHrEvents();
                            initBLEConnection();
                            break;
                        case ADAPTER_NOT_DETECTED:
                            Toast.makeText(Activity_BiometricViewer.this, "ANT Adapter Not Available. Built-in ANT hardware or external adapter required.", Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                        case DEPENDENCY_NOT_INSTALLED:
                            tv_status.setText("Error. Do Menu->Reset.");
                            AlertDialog.Builder adlgBldr = new AlertDialog.Builder(Activity_BiometricViewer.this);
                            adlgBldr.setTitle("Missing Dependency");
                            adlgBldr.setMessage("The required service\n\"" + AntPlusHeartRatePcc.getMissingDependencyName() + "\"\n was not found. You need to install the ANT+ Plugins service or you may need to update your existing version if you already have it. Do you want to launch the Play Store to get it?");
                            adlgBldr.setCancelable(true);
                            adlgBldr.setPositiveButton("Go to Store", new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent startStore = null;
                                    startStore = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=" + AntPlusHeartRatePcc.getMissingDependencyPackageName()));
                                    startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    Activity_BiometricViewer.this.startActivity(startStore);
                                }
                            });
                            adlgBldr.setNegativeButton("Cancel", new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            final AlertDialog waitDialog = adlgBldr.create();
                            waitDialog.show();
                            break;
                        case USER_CANCELLED:
                            tv_status.setText("Cancelled. Do Menu->Reset.");
                            break;
                        case UNRECOGNIZED:
                            Toast.makeText(Activity_BiometricViewer.this,
                                    "Failed: UNRECOGNIZED. PluginLib Upgrade Required?",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                        default:
                            Toast.makeText(Activity_BiometricViewer.this, "Unrecognized result: " + resultCode, Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                    }
                }
            };

    //Receives state changes and shows it on the status display line
    protected  IDeviceStateChangeReceiver base_IDeviceStateChangeReceiver = new IDeviceStateChangeReceiver() {
                @Override
                public void onDeviceStateChange(final DeviceState newDeviceState) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() { tv_status.setText(hrPcc.getDeviceName() + ": " + newDeviceState); }
                    });
                }
            };

    @Override
    protected void onDestroy() {
        if(releaseHandle != null) {
            releaseHandle.close();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setBluetoothDevice(BluetoothDevice btDevice) {
        this.btDevice = btDevice;
    }

    public BluetoothDevice getBluetoothDevice() { return this.btDevice; }

    private void displayTemperature() {
        mTemperature = AppPreferences.getLastCbtValue(this);

        /* This is where Temperature data is received, mTemperature is the variable with the temperature value. */
        // TODO SEND IT

        if (mTemperature != 0) {
            String value;
            String unit;

            value = String.format("%.2f", TemperatureReading.celsiusToFahrenheit(mTemperature));
            unit = FAHRENHEIT;

            tv_tempData.setText(value);
            tv_degreesUnit.setText(unit);

            api global = api.getInstance();
            JSONObject sensorData = new JSONObject();
            try {
                sensorData.put("reading", String.format("%.5f",TemperatureReading.celsiusToFahrenheit(mTemperature)));
                sensorData.put("sensor_id", "2");
                sensorData.put("user_id", userID);
            }
            catch (JSONException e) {

            }
            String sendDataString = linkString + "readings";
            global.sendPostRequestWithHandlerWithToken(sendDataString,sensorData,token, new HandlerResponse() {
                @Override
                public void handleResponse(String response) {

                }

            });

        } else {
            tv_tempData.setText("NaN");
        }
    }

    private void setTemperatureNotification(boolean enable) {
        if (mTemperatureCharacteristic != null) {
            mBluetoothLeService.setCharacteristicNotification(mTemperatureCharacteristic, enable);
        } else {
            Log.e("bad news", "mTemperatureCharacteristic is null");
        }
    }

    private void setConnecting(boolean enabled) {
        if (enabled) {
            //keep screen during "connecting..." (it's annoying if the user cannot check whether the connection attempt was successful
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void listGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, CoreGATTAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                String mGattAttribute = CoreGATTAttributes.lookup(uuid, unknownCharaString);
                Log.d("gattAttribute", "gattAttribute: " + mGattAttribute);
                // extract interesting characteristics (Battery level and Body Temperature)
                if (mGattAttribute.equals("Temperature Measurement")) {
                    final int charaProp = gattCharacteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mTemperatureCharacteristic = gattCharacteristic;
                    }
                }

                currentCharaData.put(LIST_NAME, mGattAttribute);
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    private void clearUI() {
        tv_tempData.setText("No Data");
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_TEMPERATURE_AVAILABLE);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        return intentFilter;
    }
}
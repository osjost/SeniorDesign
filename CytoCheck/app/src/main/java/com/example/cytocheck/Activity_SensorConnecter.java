package com.example.cytocheck;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.AsyncScanController;

import java.util.ArrayList;
import java.util.Set;

// Define the class Activity_SensorConnecter, extending from Activity_BiometricViewer
public class Activity_SensorConnecter extends Activity_BiometricViewer {

    // Flags and positions to manage device selection
    private boolean bleDeviceFlag = false;
    private int blePos = -1;
    private boolean antDeviceFlag = false;
    private int antPos = -1;

    // UI elements and adapters for Bluetooth and ANT+ devices
    private Button continueButton;
    TextView mTextView_Status;
    ArrayList<AsyncScanController.AsyncScanResultDeviceInfo> mScannedDeviceInfo;
    ArrayAdapter<String> ScannedDevicesAdapter;
    AsyncScanController<AntPlusHeartRatePcc> HRController;

    // Bluetooth-related variables
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mConnectedDeviceList;
    public ListView connectedBTDevices;
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the UI layout
        initDisplay();
        super.onCreate(savedInstanceState);

        // Retrieve data passed via Intent
        Intent intent = getIntent();
        String linkString = intent.getStringExtra("linkString");
        String token = intent.getStringExtra("token");
        String userID = intent.getStringExtra("userID");
        String hrLower = intent.getStringExtra("hrLower");
        String hrUpper = intent.getStringExtra("hrUpper");
        String tempLower = intent.getStringExtra("tempLower");
        String tempUpper = intent.getStringExtra("tempUpper");

        // Set the received data into the activity
        setIntentStrings(linkString, userID, token, hrLower, hrUpper, tempLower, tempUpper);

        // Parse threshold values
        final double mtempUpper = Double.parseDouble(tempUpper);
        final double mtempLower = Double.parseDouble(tempLower);
        final int mhrUpper = Integer.parseInt(hrUpper);
        final int mhrLower = Integer.parseInt(hrLower);

        // Set the thresholds for biometric values
        setThresholds(mhrUpper, mhrLower, mtempUpper, mtempLower);
    }

    // Method to initialize the display elements
    private void initDisplay() {
        setContentView(R.layout.activity_sensor_connecter);

        // Initialize continue button and its onClick listener
        continueButton = (Button) findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canContinue()) {
                    // Proceed to connect to selected BLE device
                    requestConnectToResult(mScannedDeviceInfo.get(antPos));
                }
            }
        });

        // Check Bluetooth settings and display connected devices
        checkBTSettings();
        connectedBTDevices = (ListView) findViewById(R.id.lv_ble);
        displayConnectedDevices();

        // Initialize ANT+ device scanning
        mScannedDeviceInfo = new ArrayList<AsyncScanController.AsyncScanResultDeviceInfo>();
        ScannedDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        ListView listView_Devices = (ListView) findViewById(R.id.lv_ant);
        listView_Devices.setAdapter(ScannedDevicesAdapter);
        listView_Devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                // Handle selection of ANT+ device
                antDeviceFlag = true;
                antPos = pos;
                if (!bleDeviceFlag) {
                    // Prompt user to select a BLE device if not already selected
                    Toast.makeText(Activity_SensorConnecter.this, "Please select a BLE biosensor.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to request connection to an ANT+ device
    protected void requestConnectToResult(final AsyncScanController.AsyncScanResultDeviceInfo asyncScanResultDeviceInfo) {
        runOnUiThread(new Runnable() {
            public void run() {
                // Initiate device access request for ANT+ heart rate monitor
                releaseHandle = HRController.requestDeviceAccess(asyncScanResultDeviceInfo,
                        new AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc>() {
                            @Override
                            public void onResultReceived(AntPlusHeartRatePcc result, RequestAccessResult resultCode, DeviceState initialDeviceState) {
                                if (resultCode != RequestAccessResult.SEARCH_TIMEOUT) {
                                    // Handle connection result and device state
                                    AccessResultReceiver.onResultReceived(result, resultCode, initialDeviceState);
                                    HRController = null;
                                } else {
                                    // Notify user of connection failure
                                    Toast.makeText(Activity_SensorConnecter.this, "Could not connect to ANT+ device", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, DeviceStateChangeReceiver);
            }
        });
    }

    // Method to request access to ANT+ device scanning
    @Override
    protected void requestAccessToController() {
        initDisplay();
        HRController = AntPlusHeartRatePcc.requestAsyncScanController(this, 0,
                new AsyncScanController.IAsyncScanResultReceiver() {
                    @Override
                    public void onSearchStopped(RequestAccessResult reasonStopped) {
                        // Handle search stop event
                        AccessResultReceiver.onResultReceived(null, reasonStopped, DeviceState.DEAD);
                    }

                    @Override
                    public void onSearchResult(final AsyncScanController.AsyncScanResultDeviceInfo deviceFound) {
                        // Process scan results and display devices
                        for (AsyncScanController.AsyncScanResultDeviceInfo i : mScannedDeviceInfo) {
                            if (i.getAntDeviceNumber() == deviceFound.getAntDeviceNumber()) {
                                return; // Ignore already connected devices
                            }
                        }
                        // Add new device to the list if not already connected
                        if (!deviceFound.isAlreadyConnected()) {
                            mScannedDeviceInfo.add(deviceFound);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ScannedDevicesAdapter.add(deviceFound.getDeviceDisplayName());
                                    ScannedDevicesAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                });
    }

    // Method to handle cleanup on activity reset
    @Override
    protected void handleReset() {
        if (HRController != null) {
            HRController.closeScanController();
            HRController = null;
        }
        super.handleReset();
    }

    // Method to handle cleanup on activity destruction
    @Override
    protected void onDestroy() {
        if (HRController != null) {
            HRController.closeScanController();
            HRController = null;
        }
        super.onDestroy();
    }

    // Method to check and manage Bluetooth settings
    private void checkBTSettings() {
        // Check if BLE is supported on the device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Get Bluetooth adapter and enable if not already enabled
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Check if Bluetooth is supported on the device
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Method to verify if all required devices are selected to continue
    private boolean canContinue() {
        if (antDeviceFlag && bleDeviceFlag) {
            // Proceed if both ANT+ and BLE devices are selected
            return true;
        } else if (!antDeviceFlag) {
            // Prompt user to select an ANT+ device
            Toast.makeText(Activity_SensorConnecter.this, "Please select a ANT+ sensor.", Toast.LENGTH_SHORT).show();
        } else if (!bleDeviceFlag) {
            // Prompt user to select a BLE device
            Toast.makeText(Activity_SensorConnecter.this, "Please select a BLE sensor.", Toast.LENGTH_SHORT).show();
        } else {
            // Prompt user to select a device from both categories
            Toast.makeText(Activity_SensorConnecter.this, "Please select a sensor from each.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    // Method to display already connected Bluetooth devices
    private void displayConnectedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        mConnectedDeviceList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        connectedBTDevices.setAdapter(mConnectedDeviceList);

        // Add paired devices to the list
        for (BluetoothDevice pairedDevice : pairedDevices) {
            mConnectedDeviceList.add(pairedDevice.getName());
            mConnectedDeviceList.notifyDataSetChanged();
        }

        // Handle selection of a connected Bluetooth device
        connectedBTDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice[] btdeviceslist = new BluetoothDevice[pairedDevices.size()];
                pairedDevices.toArray(btdeviceslist);

                // Mark BLE device as selected
                bleDeviceFlag = true;
                blePos = i;
                setBluetoothDevice(btdeviceslist[i]);
                if (!antDeviceFlag) {
                    // Prompt user to select an ANT+ device if not already selected
                    Toast.makeText(Activity_SensorConnecter.this, "Please select an ANT+ biosensor.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
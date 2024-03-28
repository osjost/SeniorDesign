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

public class Activity_SensorConnecter extends Activity_BiometricViewer {

    private boolean bleDeviceFlag = false;
    private int blePos = -1;
    private boolean antDeviceFlag = false;
    private int antPos = -1;

    private Button continueButton;



    /* ANT+ Stuff */
    TextView mTextView_Status;
    ArrayList<AsyncScanController.AsyncScanResultDeviceInfo> mScannedDeviceInfos;
    ArrayAdapter<String> adapter_devNameList;
    AsyncScanController<AntPlusHeartRatePcc> hrScanCtrl;
    /* End of ANT+ Stuff */

    /* BLE Stuff */
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mConnectedDeviceList;
    public ListView connectedBTDevices;
    private static final int REQUEST_ENABLE_BT = 1;
    /* End of ANT+ Stuff */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initDisplay();
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String linkString = intent.getStringExtra("linkString");
        String token = intent.getStringExtra("token");
        String userID = intent.getStringExtra("userID");
        setIntentStrings(linkString, userID, token);
    }

    private void initDisplay() {
        setContentView(R.layout.activity_sensor_connecter);

        continueButton = (Button) findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canContinue()) {
                    // Pass BLE device info to next intent
                    requestConnectToResult(mScannedDeviceInfos.get(antPos));
                }
            }
        });

        checkBTSettings();
        connectedBTDevices = (ListView) findViewById(R.id.lv_ble);
        displayConnectedDevices();

        mScannedDeviceInfos = new ArrayList<AsyncScanController.AsyncScanResultDeviceInfo>();
        adapter_devNameList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        ListView listView_Devices = (ListView)findViewById(R.id.lv_ant);
        listView_Devices.setAdapter(adapter_devNameList);
        listView_Devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //Return the id of the selected already connected device
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                antDeviceFlag = true;
                antPos = pos;
                if(!bleDeviceFlag) {
                    Toast.makeText(Activity_SensorConnecter.this, "Please select a BLE biosensor.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void requestConnectToResult(final AsyncScanController.AsyncScanResultDeviceInfo asyncScanResultDeviceInfo) {
        //Inform the user we are connecting
        runOnUiThread(new Runnable() {
            public void run() {
                releaseHandle = hrScanCtrl.requestDeviceAccess(asyncScanResultDeviceInfo,
                        new AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc>() {
                            @Override
                            public void onResultReceived(AntPlusHeartRatePcc result, RequestAccessResult resultCode, DeviceState initialDeviceState) {
                                if(resultCode != RequestAccessResult.SEARCH_TIMEOUT) {
                                    //On a connection timeout the scan automatically resumes, so we inform the user, and go back to scanning
                                    base_IPluginAccessResultReceiver.onResultReceived(result, resultCode, initialDeviceState);
                                    hrScanCtrl = null;
                                }
                            }
                        }, base_IDeviceStateChangeReceiver);
            }
        });
    }

    /**
     * Requests the asynchronous scan controller
     */
    @Override
    protected void requestAccessToPcc() {
        initDisplay();
        hrScanCtrl = AntPlusHeartRatePcc.requestAsyncScanController(this, 0,
                new AsyncScanController.IAsyncScanResultReceiver() {
                    @Override
                    public void onSearchStopped(RequestAccessResult reasonStopped) {
                        //The triggers calling this function use the same codes and require the same actions as those received by the standard access result receiver
                        base_IPluginAccessResultReceiver.onResultReceived(null, reasonStopped, DeviceState.DEAD);
                    }

                    @Override
                    public void onSearchResult(final AsyncScanController.AsyncScanResultDeviceInfo deviceFound) {
                        for(AsyncScanController.AsyncScanResultDeviceInfo i: mScannedDeviceInfos) {
                            //The current implementation of the async scan will reset it's ignore list every 30s,
                            //So we have to handle checking for duplicates in our list if we run longer than that
                            if(i.getAntDeviceNumber() == deviceFound.getAntDeviceNumber()) {
                                //Found already connected device, ignore
                                return;
                            }
                        }

                        //We split up devices already connected to the plugin from un-connected devices to make this information more visible to the user,
                        //since the user most likely wants to be aware of which device they are already using in another app
                        if(!deviceFound.isAlreadyConnected()) {
                            mScannedDeviceInfos.add(deviceFound);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter_devNameList.add(deviceFound.getDeviceDisplayName());
                                    adapter_devNameList.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                });
    }


    /**
     * Ensures our controller is closed whenever we reset
     */
    @Override
    protected void handleReset() {
        if(hrScanCtrl != null)
        {
            hrScanCtrl.closeScanController();
            hrScanCtrl = null;
        }
        super.handleReset();
    }

    /**
     * Ensures our controller is closed whenever we exit
     */
    @Override
    protected void onDestroy() {
        if(hrScanCtrl != null)
        {
            hrScanCtrl.closeScanController();
            hrScanCtrl = null;
        }
        super.onDestroy();
    }


    private boolean canContinue() {
        if(antDeviceFlag && bleDeviceFlag) {
            // No toast, continue
            return true;
        } else if(!antDeviceFlag) {
            Toast.makeText(Activity_SensorConnecter.this, "Please select a ANT+ sensor.", Toast.LENGTH_SHORT).show();
        } else if(!bleDeviceFlag) {
            Toast.makeText(Activity_SensorConnecter.this, "Please select a BLE sensor.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(Activity_SensorConnecter.this, "Please select a sensor from each.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void displayConnectedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        mConnectedDeviceList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        connectedBTDevices.setAdapter(mConnectedDeviceList);

        for (BluetoothDevice pairedDevice : pairedDevices) {
            mConnectedDeviceList.add(pairedDevice.getName());
            mConnectedDeviceList.notifyDataSetChanged();
        }
        connectedBTDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice[] btdeviceslist = new BluetoothDevice[pairedDevices.size()];
                pairedDevices.toArray(btdeviceslist);
                //intent.putExtra(DEVICE, btdeviceslist[i]);

                bleDeviceFlag = true;
                blePos = i;
                setBluetoothDevice(btdeviceslist[i]);
                if(!antDeviceFlag) {
                    Toast.makeText(Activity_SensorConnecter.this, "Please select an ANT+ biosensor.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkBTSettings() {
        // Use this check to determine whether BLE is supported on the device.  Then you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }

    }



}
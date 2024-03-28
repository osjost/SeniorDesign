package com.example.cytocheck;

import static android.system.Os.close;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.security.Provider;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BluetoothLeService extends Service {
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    public final static UUID UUID_TEMPERATURE_MEASUREMENT = UUID.fromString(CoreGATTAttributes.TEMPERATURE_MEASUREMENT);
    public final static String ACTION_GATT_CONNECTED =
            "com.example.antemulator.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.antemulator.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.antemulator.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_TEMPERATURE_AVAILABLE =
            "com.example.antemulator.bluetooth.le.ACTION_TEMPERATURE_AVAILABLE";
    public final static String ACTION_BATTERY_LEVEL_AVAILABLE =
            "com.example.antemulator.bluetooth.le.ACTION_BATTERY_LEVEL_AVAILABLE";
    public final static String EXTRA_TEMPERATURE_VALUE =
            "com.example.antemulator.bluetooth.le.EXTRA_TEMPERATURE_VALUE";
    public final static String EXTRA_BATTERY_VALUE =
            "com.example.antemulator.bluetooth.le.EXTRA_BATTERY_VALUE";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.antemulator.bluetooth.le.ACTION_DATA_AVAILABLE";
    static final int IEEE11073_NaN = 0x007FFFFF;
    static final int IEEE11073_inf = 0x007FFFFE;
    static final int IEEE11073_minus_inf = 0x00800002;
    static final int IEEE11073_NRes = 0x00800000;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    private int connectionState;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }

        registerReceiver(this.mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        return true;
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            return false;
        }
        try {
            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            // connect to the GATT server on the device
            mBluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }


    public List<BluetoothGattService> getSupportedGattServices () {
            if (mBluetoothGatt == null) return null;
            return mBluetoothGatt.getServices();
    }
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    public void setCharacteristicNotification (BluetoothGattCharacteristic characteristic, boolean enabled){
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        // This is specific to Temperature Measurement.
        if (UUID_TEMPERATURE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CoreGATTAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                String intentAction;
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intentAction = ACTION_GATT_CONNECTED;
                    broadcastUpdate(intentAction);
                    // Attempts to discover services after successful connection.
                    mBluetoothGatt.discoverServices();

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    AppPreferences.setLastCbtValue(BluetoothLeService.this, 0);
                    broadcastUpdate(intentAction);
                }
            }
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive (Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF) {

                }
                // Bluetooth is disconnected, do handling here:
                AppPreferences.setLastCbtValue(BluetoothLeService.this, 0);
                broadcastUpdate(intent);
                String intentAction;
                intentAction = ACTION_GATT_DISCONNECTED;
                broadcastUpdate(intentAction);

            }

        }

    };

    private void broadcastUpdate(Intent intent) {
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        Intent intent;

        if (UUID_TEMPERATURE_MEASUREMENT.equals(characteristic.getUuid())) {
            intent = new Intent(ACTION_TEMPERATURE_AVAILABLE);
            double temperature = TemperatureReading.fromCharacteristic(characteristic);
            float fTemperature = (float) temperature;
            AppPreferences.setLastCbtValue(BluetoothLeService.this, fTemperature);
            intent.putExtra("TEMP_DATA", temperature);
        }  else {
            intent = new Intent(action);
        }

        sendBroadcast(intent);
    }
}
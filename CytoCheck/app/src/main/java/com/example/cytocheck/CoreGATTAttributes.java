package com.example.cytocheck;

import java.util.HashMap;

public class CoreGATTAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    // xxxxxxxx-0000-1000-8000-00805F9B34FB is the Bluetooth Base UUID --> add short form UUID16
    public static String TEMPERATURE_MEASUREMENT = "00002a1c-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";

    static {
        // Services
        attributes.put("00001809-0000-1000-8000-00805f9b34fb", "Health Thermometer Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Characteristics.
        attributes.put(TEMPERATURE_MEASUREMENT, "Temperature Measurement");
        attributes.put(BATTERY_LEVEL, "Battery Level");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}

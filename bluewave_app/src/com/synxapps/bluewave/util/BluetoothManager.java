package com.synxapps.bluewave.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.synxapps.bluewave.MainActivity;
import com.synxapps.bluewave.adapters.MeetListAdapter;
import com.synxapps.bluewave.entity.Contact;
import com.synxapps.bluewave.entity.Device;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BluetoothManager {
	
	private BluetoothAdapter btAdapter;
	private static BluetoothManager instance;
    private BroadcastReceiver mReceiver;
    private LocalDBHandler db;
    Contact owner;
	
	public BluetoothManager() {
		btAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	public static BluetoothManager getInstance() {
		if (instance == null) {
			instance = new BluetoothManager();
		}
		return instance;
	}
	
	public void startDiscovery(MainActivity ui, Contact o) {
		//Save the owner's reference
		this.owner = o;
		//Create the DB Handler
		db = new LocalDBHandler(ui);
		//Check if the bluetooth is already enabled
		if (!btAdapter.isEnabled()) {
			//Enable the bluetooth
			enable();
			//Start scanning for devices
			btAdapter.startDiscovery();
		} else {
			//Start scanning for devices
			btAdapter.startDiscovery();
		}
		Log.d("BT", "Init");
		// Create a BroadcastReceiver for ACTION_FOUND
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, final Intent intent) {
            	//This receiver is run on the main activity thread
            	//For avoid UI lagging, we run it on a background thread
            	new Thread(new Runnable() {
					@Override
					public void run() {
						String action = intent.getAction();
		                // When discovery finds a device
		                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		                    // Get the BluetoothDevice object from the Intent
		                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		                    // Add the name and address if it is not already listed
		                    if (!db.deviceExists(new Device(device))) {
		                    	//Create a new device based on the bluetoothdevice found
		                    	Device d = new Device();
		                    	d.setMac_addr(device.getAddress());
		                    	d.setName(device.getName());
		                    	d.setType(device.getBluetoothClass().getMajorDeviceClass());
		                    	d.setFound_time(Timestamp.valueOf((String) new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date().getTime())));
		                    	d.setStatus(Device.STATE_NOT_ANALYZED);
		                    	//Save the device on the DB
		                    	db.saveDevice(d, owner);
		                    	//Notify the mainactivity that a new device has been added
		                    	Log.d("BT", "Device saved: " + d.getMac_addr());
		                    }
		                }
		                //When discovery finishes scanning
		                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
		                	//Wait 5 seconds
		                	try {
		                		Log.d("BT", "Scan finished. Restarting in 5 seconds...");
								Thread.sleep(5000);
							} catch (InterruptedException e) {}
		                	if (btAdapter.isEnabled()) {
			                	//Restart the discovery
			                	btAdapter.startDiscovery();
			                	Log.d("BT", "Scan restarted");
		                	}
		                }
					}
				}).start();
            }
        };
        
        // Register the BroadcastReceiver for device found and discovery status
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter enabledFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        ui.registerReceiver(mReceiver, foundFilter);
        ui.registerReceiver(mReceiver, enabledFilter);
	}
	
	public void stopDiscovery(MainActivity ui) {
		if (btAdapter.isEnabled()) {
			btAdapter.disable();
		}
		//Unregister the bluetooth receivers
		ui.unregisterReceiver(mReceiver);
	}
	
	private void enable() {
		btAdapter.enable();
		//Wait for the bluetooth to be enabled
		while (btAdapter.getState() != BluetoothAdapter.STATE_ON) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}
	}
	
	public boolean isEnabled() {
		return btAdapter.isEnabled();
	}
	
	public String getBluetoothMAC() {
		return btAdapter.getAddress();
	}

}

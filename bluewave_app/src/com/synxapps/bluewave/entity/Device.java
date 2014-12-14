package com.synxapps.bluewave.entity;

import java.sql.Timestamp;

import android.bluetooth.BluetoothDevice;

public class Device {
	
	public static int STATE_NOT_ANALYZED = 0, STATE_NOT_USER = 1, STATE_ANALYZED = 2;
	private String mac_addr;
	private String name;
	private int type;
	private Timestamp found_time;
	private int status;
	
	public Device() {
		
	}
	
	public Device(BluetoothDevice device) {
		mac_addr = device.getAddress();
		name = device.getName();
		type = device.getBluetoothClass().getMajorDeviceClass();
	}
	
	public String getMac_addr() {
		return mac_addr;
	}
	public void setMac_addr(String mac_addr) {
		this.mac_addr = mac_addr;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Timestamp getFound_time() {
		return found_time;
	}
	public void setFound_time(Timestamp found_time) {
		this.found_time = found_time;
	}
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}

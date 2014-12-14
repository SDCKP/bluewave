package com.synxapps.bluewave.entity;

import java.sql.Timestamp;

import android.graphics.Bitmap;

public class Contact extends PreContact {
	
	private String email;
	private String lastmsg;
	private Timestamp lastmsg_time;
	
	
	public String getLastmsg() {
		return lastmsg;
	}
	public void setLastmsg(String lastmsg) {
		this.lastmsg = lastmsg;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public Timestamp getLastmsg_time() {
		return lastmsg_time;
	}
	public void setLastmsg_time(Timestamp lastmsg_time) {
		this.lastmsg_time = lastmsg_time;
	}
	
	public String toString() {
		return "ID: " + this.getId() + ", Alias: " + this.getAlias() + ", email: " + this.getEmail();
	}
}

package com.synxapps.bluewave.entity;

import java.sql.Timestamp;

import android.graphics.Bitmap;

public class PreContact {
	
	private int id;
	private String alias;
	private Bitmap avatar;
	private String matchingPercent;
	private Timestamp found_time;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}	
	public Bitmap getAvatar() {
		return avatar;
	}
	public void setAvatar(Bitmap avatar) {
		this.avatar = avatar;
	}
	public String getMatchingPercent() {
		return matchingPercent;
	}
	public void setMatchingPercent(String matchingPercent) {
		this.matchingPercent = matchingPercent;
	}
	public Timestamp getFound_time() {
		return found_time;
	}
	public void setFound_time(Timestamp found_time) {
		this.found_time = found_time;
	}
	
	public String toString() {
		return "ID: " + this.getId() + ", Alias: " + this.getAlias();
	}
}

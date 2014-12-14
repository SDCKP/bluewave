package com.synxapps.bluewave.entity;

import java.sql.Timestamp;

public class Message {

	private int id;
	private int id_from;
	private int id_to;
	private String alias_from;
	private String alias_to;
	private String content;
	private Timestamp creation_time;
	private boolean isRead;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getId_from() {
		return id_from;
	}
	public void setId_from(int id_from) {
		this.id_from = id_from;
	}
	public int getId_to() {
		return id_to;
	}
	public void setId_to(int id_to) {
		this.id_to = id_to;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Timestamp getCreation_time() {
		return creation_time;
	}
	public void setCreation_time(Timestamp creation_time) {
		this.creation_time = creation_time;
	}
	public String getAlias_from() {
		return alias_from;
	}
	public void setAlias_from(String alias_from) {
		this.alias_from = alias_from;
	}
	public String getAlias_to() {
		return alias_to;
	}
	public void setAlias_to(String alias_to) {
		this.alias_to = alias_to;
	}
	public boolean isRead() {
		return isRead;
	}
	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}
	
	public String toString() {
		return "mID: " + getId() + "; fromID: " + getId_from() + "; toID: " + getId_to() +
				";aliasFrom: " + getAlias_from() + "; aliasTo: " + getAlias_from() +
				";Content: " + getContent() + ";Date: " + getCreation_time() + ";Read: " + isRead();
				
	}
}

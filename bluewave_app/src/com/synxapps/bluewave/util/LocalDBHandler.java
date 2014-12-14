package com.synxapps.bluewave.util;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.synxapps.bluewave.entity.Contact;
import com.synxapps.bluewave.entity.Device;
import com.synxapps.bluewave.entity.Message;
import com.synxapps.bluewave.entity.PreContact;

import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

public class LocalDBHandler extends SQLiteOpenHelper {
	
	private static final String DBNAME = "bluewave_data.db";
	private static final String user_table = "bluewave_userinfo";
	private static final String messages_table = "bluewave_messages";
	private static final String contacts_table = "bluewave_contacts";
	private static final String devices_table = "bluewave_devices";
	private static final String precontacts_table = "bluewave_precontacts";
	private static final String contact_requests_table = "bluewave_contact_requests";
	
	public LocalDBHandler(Context context) {
		super(context, DBNAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_MESSAGES_TABLE = "CREATE TABLE " + messages_table + " (msgid INT PRIMARY KEY," +
				"id_from INT, id_to INT, send_time TIMESTAMP, content TEXT, read INT)";
		String CREATE_USERINFO_TABLE = "CREATE TABLE " + user_table + " (email TEXT PRIMARY KEY," +
				"password TEXT, userID int)";
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + contacts_table + " (id INT PRIMARY KEY," +
				"ownerID INT, alias TEXT, lastmsg_content TEXT, lastmsg_time TIMESTAMP, avatar TEXT)";
		String CREATE_DEVICES_TABLE = "CREATE TABLE " + devices_table + " (mac_addr TEXT PRIMARY KEY," +
				"name TEXT, ownerID INT, type INT, found_time TIMESTAMP, status INT)";
		String CREATE_PRECONTACTS_TABLE = "CREATE TABLE " + precontacts_table + " (id INT PRIMARY KEY," +
				"ownerID INT, alias TEXT, match TEXT, found_time TIMESTAMP, avatar TEXT)";
		String CREATE_CONTACT_REQUESTS_TABLE = "CREATE TABLE " + contact_requests_table + " (requesterID INT PRIMARY KEY," +
				"ownerID INT, alias TEXT, match TEXT, found_time TIMESTAMP, avatar TEXT, seen INT)";
		//Create the messages table
		db.execSQL(CREATE_MESSAGES_TABLE);
		//Create the user info table (for the owner, remember login data)
		db.execSQL(CREATE_USERINFO_TABLE);
		//Create the contact data table
		db.execSQL(CREATE_CONTACTS_TABLE);
		//Create the contact data table
		db.execSQL(CREATE_DEVICES_TABLE);
		//Create the contact data table
		db.execSQL(CREATE_PRECONTACTS_TABLE);
		//Create the contact request data table
		db.execSQL(CREATE_CONTACT_REQUESTS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//Drop the existing tables
		db.execSQL("DROP TABLE IF EXISTS " + messages_table);
		db.execSQL("DROP TABLE IF EXISTS " + user_table);
		db.execSQL("DROP TABLE IF EXISTS " + contacts_table);
		
		//Recreate the tables again
		onCreate(db);
	}
	
	//Save a message on the db
	public void saveMessage(Message m) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("msgid", m.getId());
		values.put("id_from", m.getId_from());
		values.put("id_to", m.getId_to());
		values.put("send_time", getSQLDate(m.getCreation_time().getTime()));
		values.put("content", m.getContent());
		values.put("read", m.isRead());
		db.insert(messages_table, null, values);
	}
	
	public void saveUserInfo(String email, String password, int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("email", email);
		values.put("password", password);
		values.put("userID", id);
		db.insert(user_table, null, values);
	}
	
	public String[] getUserLoginData() {
		String[] usrinfo = new String[3];
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] columns = {"email", "password", "userID"};
		
		//Execute the query
		Cursor rs = db.query(user_table, columns, null, null, null, null, null, null);
		//Check if there is any result
		if (rs.moveToFirst()) {
			usrinfo[0] = rs.getString(0);
			usrinfo[1] = rs.getString(1);
			usrinfo[2] = String.valueOf(rs.getInt(2));
			
		}

		//Close the cursor
		rs.close();

		return usrinfo;
	}
	
	//Remove the data from the user from the DB
	public void clearUserLogin() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM " + user_table);
		db.execSQL("DELETE FROM " + messages_table);
		db.execSQL("DELETE FROM " + contacts_table);
		db.execSQL("DELETE FROM " + devices_table);
		db.execSQL("DELETE FROM " + precontacts_table);
		db.execSQL("DELETE FROM " + contact_requests_table);
	}
	
	//Clear contact list
	public void clearContacts(Contact owner) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM " + contacts_table + " WHERE ownerID = " + owner.getId());
	}
	
	//Get the messages from the selected user
	public ArrayList<Message> getMessagesFromContact(Contact o, Contact c) {
		SQLiteDatabase db = this.getReadableDatabase();
		ArrayList<Message> saved_msgs = new ArrayList<Message>();
		
		String[] columns = {"msgid", "id_from", "id_to", "send_time", "content", "read"};
		String[] filter = {String.valueOf(c.getId()), String.valueOf(o.getId()), String.valueOf(o.getId()), String.valueOf(c.getId())};
		
		//Execute the query
		Cursor rs = db.query(messages_table, columns, "(id_from = ? AND id_to = ?) OR (id_from = ? AND id_to = ?)", filter, null, null, "send_time", "50");
		//Check if there is any result
		if (rs.moveToFirst()) {
			do {
				//Obtain the data from the db and create a new message with it
				Message m = new Message();
				m.setId(rs.getInt(0));
				m.setId_from(rs.getInt(1));
				m.setId_to(rs.getInt(2));
				m.setCreation_time(Timestamp.valueOf(rs.getString(3)));
				m.setContent(rs.getString(4));
				m.setRead(toBolean(rs.getInt(5)));
				//Add the message to the arraylist
				saved_msgs.add(m);
				//Set the message as read
				setMessageRead(m);
			} while (rs.moveToNext());
		}
		//Close the cursor
		rs.close();

		//Return the array of messages
		return saved_msgs;
	}
	
	//Get the messages from the selected user
	public ArrayList<Message> getUnreadMessagesFromContact(Contact o, Contact c) {
		SQLiteDatabase db = this.getReadableDatabase();
		ArrayList<Message> saved_msgs = new ArrayList<Message>();
		
		String[] columns = {"msgid", "id_from", "id_to", "send_time", "content", "read"};
		String[] filter = {String.valueOf(c.getId()), String.valueOf(o.getId()), String.valueOf(o.getId()), String.valueOf(c.getId())};
		
		//Execute the query
		Cursor rs = db.query(messages_table, columns, "((id_from = ? AND id_to = ?) OR (id_from = ? AND id_to = ?)) AND read = 0", filter, null, null, "send_time", "50");
		//Check if there is any result
		if (rs.moveToFirst()) {
			do {
				//Obtain the data from the db and create a new message with it
				Message m = new Message();
				m.setId(rs.getInt(0));
				m.setId_from(rs.getInt(1));
				m.setId_to(rs.getInt(2));
				m.setCreation_time(Timestamp.valueOf(rs.getString(3)));
				m.setContent(rs.getString(4));
				m.setRead(toBolean(rs.getInt(5)));
				//Add the message to the arraylist
				saved_msgs.add(m);
				//Set the message as read
				setMessageRead(m);
			} while (rs.moveToNext());
		}
		//Close the cursor
		rs.close();
		
		//Return the array of messages
		return saved_msgs;
	}
	
	//Get the last unread message
	public Message getLastUnreadMessage(Contact owner) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] columns = {"msgid", "id_from", "id_to", "send_time", "content", "read"};
		String[] filter = {String.valueOf(owner.getId())};
		
		//Execute the query
		Cursor rs = db.query(messages_table, columns, "id_to = ? AND read = 0", filter, null, null, "send_time", "1");
		Message m = null;
		//Check if there is any result
		if (rs.moveToFirst()) {
			m = new Message();
			m.setId(rs.getInt(0));
			m.setId_from(rs.getInt(1));
			m.setId_to(rs.getInt(2));
			m.setCreation_time(Timestamp.valueOf(rs.getString(3)));
			m.setContent(rs.getString(4));
			m.setRead(toBolean(rs.getInt(5)));
		}
		//Close the cursor
		rs.close();

		//Return the array of messages
		return m;
	}
	
	//Check if the message has been readed by the local user or not
	public boolean isMessageRead(Message m) {
		SQLiteDatabase db = this.getReadableDatabase();
		int msgID = m.getId();
		
		String[] columns = {"msgID", "read"};
		String[] filter = {String.valueOf(msgID), "1"};
		
		//Execute the query
		Cursor rs = db.query(messages_table, columns, "msgid = ? AND read = ?", filter, null, null, null, null);
		//Check if there is any result
		if (rs.moveToFirst()) {
			return true;
		}
		//Close the cursor
		rs.close();

		return false;
	}
	
	//Set a message as readed
	public void setMessageRead(Message m) {
		SQLiteDatabase db = this.getWritableDatabase();
		int msgID = m.getId();
		db.execSQL("UPDATE " + messages_table + " SET read = 1 WHERE msgid = " + msgID);
		m.setRead(true);
	}
	
	//Check if the message is already on the DB
	public boolean messageExists(Message m) {
		int msgID = m.getId();
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] columns = {"msgID"};
		String[] filter = {String.valueOf(msgID)};
		
		//Execute the query
		Cursor rs = db.query(messages_table, columns, "msgid = ?", filter, null, null, null, null);
		//Check if there is any result
		if (rs.moveToFirst()) {
			return true;
		}
		//Close the cursor
		rs.close();

		return false;
	}
	
	//Check if the message is already on the DB
	public int getUnreadMessageCount(Contact owner) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] columns = {"count(*)"};
		String[] filter = {String.valueOf(owner.getId())};
		
		//Execute the query
		Cursor rs = db.query(messages_table, columns, "id_to = ? AND read = 0", filter, null, null, null, null);
		//Check if there is any result
		if (rs.moveToFirst()) {
			return rs.getInt(0);
		}
		//Close the cursor
		rs.close();

		return 0;
	}
	
	//Save the contacts of the owner on the DB
	public void saveContacts(ArrayList<Contact> contacts, Contact owner) {
		SQLiteDatabase db = this.getWritableDatabase();
		for (Contact c : contacts) {
			ContentValues values = new ContentValues();
			values.put("id", String.valueOf(c.getId()));
			values.put("ownerID", owner.getId());
			values.put("alias", c.getAlias());
			if (c.getLastmsg() != null)
			values.put("lastmsg_content", c.getLastmsg());
			if (c.getLastmsg_time() != null)
			values.put("lastmsg_time", getSQLDate(c.getLastmsg_time().getTime()));
			if (c.getAvatar() != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				c.getAvatar().compress(Bitmap.CompressFormat.PNG, 100, stream);
				values.put("avatar", Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT));
			}
			db.insert(contacts_table, null, values);
			
		}
	}
	
	//Return the contacts from the specified owner
	public ArrayList<Contact> getContactsFrom(Contact owner) {
		SQLiteDatabase db = this.getReadableDatabase();

		ArrayList<Contact> contacts = new ArrayList<Contact>();
		
		String[] columns = {"id", "alias", "lastmsg_content", "lastmsg_time", "avatar"};
		String[] filter = {String.valueOf(owner.getId())};
		
		//Execute the query
		Cursor rs = db.query(contacts_table, columns, "ownerID = ?", filter, null, null, "lastmsg_time DESC", null);
		//Check if there is any result
		if (rs.moveToFirst()) {
			do {
				//Obtain the data from the db and create a new contact with it
				Contact c = new Contact();
				c.setId(rs.getInt(0));
				c.setAlias(rs.getString(1));
				if (rs.getString(2) != null)
				c.setLastmsg(rs.getString(2));
				if (rs.getString(3) != null)
				c.setLastmsg_time(Timestamp.valueOf(rs.getString(3)));
				if (rs.getString(4) != null) {
					byte[] avBytes = Base64.decode(rs.getString(4), Base64.DEFAULT);
					c.setAvatar(BitmapFactory.decodeByteArray(avBytes, 0, avBytes.length));
				}
				//Add the contact to the arraylist
				contacts.add(c);
			} while (rs.moveToNext());
		}
		//Close the cursor
		rs.close();
		
		return contacts;
	}
	
	public Contact getContactById(Contact contact) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		Contact c = new Contact();
		
		String[] columns = {"id", "alias", "lastmsg_content", "lastmsg_time", "avatar"};
		String[] filter = {String.valueOf(contact.getId())};
		
		//Execute the query
		Cursor rs = db.query(contacts_table, columns, "id = ?", filter, null, null, null, null);
		//Check if there is any result
		if (rs.moveToFirst()) {
			//Obtain the data from the db and fill the contact data with it
			c.setId(rs.getInt(0));
			c.setAlias(rs.getString(1));
			if (rs.getString(2) != null)
			c.setLastmsg(rs.getString(2));
			if (rs.getString(3) != null)
			c.setLastmsg_time(Timestamp.valueOf(rs.getString(3)));
			if (rs.getString(4) != null) {
				byte[] avBytes = Base64.decode(rs.getString(4), Base64.DEFAULT);
				c.setAvatar(BitmapFactory.decodeByteArray(avBytes, 0, avBytes.length));
			}
		}
		//Close the cursor
		rs.close();
		
		return c;
	}
	
	//Add a new single contact to the DB
	public void addContact(Contact contact, Contact owner) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("id", String.valueOf(contact.getId()));
		values.put("ownerID", owner.getId());
		values.put("alias", contact.getAlias());
		if (contact.getLastmsg() != null)
		values.put("lastmsg_content", contact.getLastmsg());
		if (contact.getLastmsg_time() != null)
		values.put("lastmsg_time", getSQLDate(contact.getLastmsg_time().getTime()));
		if (contact.getAvatar() != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			contact.getAvatar().compress(Bitmap.CompressFormat.PNG, 100, stream);
			values.put("avatar", Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT));
		}
		db.insert(contacts_table, null, values);
	}
	
	//Delete a contact from the DB
	public void deleteContact(Contact contact, Contact owner) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL("DELETE FROM " + contacts_table + " WHERE id = " + contact.getId() + " AND ownerID = " + owner.getId());
		//Delete the messages from the contact aswell
		deleteContactMessages(contact, owner);
	}
	
	//Delete the messages from an specific contact
	public void deleteContactMessages(Contact contact, Contact owner) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL("DELETE FROM " + messages_table + " WHERE ((id_from = " + contact.getId() + 
				" AND id_to = " + owner.getId() + ") OR (id_from = " + owner.getId() + 
				" AND id_to = " + contact.getId() + "))");
	}
	
	//Update the last message and the time of a contact
	public void updateContactLastMessage(Contact contact, Contact owner, Message msg) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL("UPDATE " + contacts_table + " SET lastmsg_content = '"  + msg.getContent() + "'," +
				" lastmsg_time = '" + getSQLDate(msg.getCreation_time().getTime()) + "'" +
				" WHERE id = " + contact.getId() + " AND ownerID = " + owner.getId());
	}
	
	//Change the password of the saved account on the local DB
	public void updateUserPass(String pass) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL("UPDATE " + user_table + " SET password = '" + pass + "'");
	}
	
	//Return if the user is logged in or not (user data on db)
	public boolean isUserLogged() {
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] columns = {"*"};
		
		//Execute the query
		Cursor rs = db.query(user_table, columns, null, null, null, null, null, null);
		//Check if there is any result
		if (rs.moveToFirst()) {
			rs.close();
			return true;
		} else {
			rs.close();
			return false;
		}
	}
	
	//Save a newly found bluetooth device on the DB
	public void saveDevice(Device device, Contact owner) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("mac_addr", String.valueOf(device.getMac_addr()));
		values.put("name", device.getName());
		values.put("ownerID", owner.getId());
		values.put("type", device.getType());
		values.put("found_time", getSQLDate(device.getFound_time().getTime()));
		values.put("status", device.getStatus());
		
		db.insert(devices_table, null, values);
	}
	
	//Get all the devices stored on the db
	public ArrayList<Device> getDevices(Contact owner) {
		SQLiteDatabase db = this.getReadableDatabase();

		ArrayList<Device> devices = new ArrayList<Device>();
		
		String[] columns = {"mac_addr", "name", "type", "found_time", "status"};
		String[] filter = {String.valueOf(owner.getId())};
		
		//Execute the query
		Cursor rs = db.query(devices_table, columns, "ownerID = ?", filter, null, null, "found_time ASC", null);
		//Check if there is any result
		if (rs.moveToFirst()) {
			do {
				//Obtain the data from the db and create a new device with it
				Device d = new Device();
				d.setMac_addr(rs.getString(0));
				d.setName(rs.getString(1));
				d.setType(rs.getInt(2));
				d.setFound_time(Timestamp.valueOf(rs.getString(3)));
				d.setStatus(rs.getInt(4));
				
				//Add the device to the arraylist
				devices.add(d);
			} while (rs.moveToNext());
		}
		//Close the cursor
		rs.close();
		
		return devices;
	}
	
	//Save the precontact on the DB
	public void savePreContact(PreContact pc, Contact owner) {
		
		if (!preContactExists(pc, owner)) {
			SQLiteDatabase db = this.getWritableDatabase();
	
			ContentValues values = new ContentValues();
			values.put("id", pc.getId());
			values.put("ownerID", owner.getId());
			values.put("alias", pc.getAlias());
			values.put("match", pc.getMatchingPercent());
			values.put("found_time", getSQLDate(pc.getFound_time().getTime()));
			if (pc.getAvatar() != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				pc.getAvatar().compress(Bitmap.CompressFormat.PNG, 100, stream);
				values.put("avatar", Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT));
			}
			
			db.insert(precontacts_table, null, values);
		}
	}
	
	//Get all the precontacts stored on the db
	public ArrayList<PreContact> getPreContacts(Contact owner) {
		SQLiteDatabase db = this.getReadableDatabase();

		ArrayList<PreContact> precontacts = new ArrayList<PreContact>();
		
		String[] columns = {"id", "alias", "match", "found_time", "avatar"};
		String[] filter = {String.valueOf(owner.getId())};
		
		//Execute the query
		Cursor rs = db.query(precontacts_table, columns, "ownerID = ?", filter, null, null, "found_time ASC", null);
		//Check if there is any result
		if (rs.moveToFirst()) {
			do {
				//Obtain the data from the db and create a new device with it
				PreContact pc = new PreContact();
				pc.setId(rs.getInt(0));
				pc.setAlias(rs.getString(1));
				pc.setMatchingPercent(rs.getString(2));
				pc.setFound_time(Timestamp.valueOf(rs.getString(3)));
				if (rs.getString(4) != null) {
					byte[] avBytes = Base64.decode(rs.getString(4), Base64.DEFAULT);
					pc.setAvatar(BitmapFactory.decodeByteArray(avBytes, 0, avBytes.length));
				}
				
				//Add the device to the arraylist
				precontacts.add(pc);
			} while (rs.moveToNext());
		}
		//Close the cursor
		rs.close();
		
		return precontacts;
	}
	
	//Return if the device exists on the DB
	public boolean deviceExists(Device device) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] columns = {"*"};
		String[] filter = {device.getMac_addr()};
		
		//Execute the query
		Cursor rs = db.query(devices_table, columns, "mac_addr = ?", filter, null, null, null, null);
		//Check if there is any result
		if (rs.moveToFirst()) {
			rs.close();
			return true;
		} else {
			rs.close();
			return false;
		}
	}
	
	public boolean preContactExists(PreContact pc, Contact owner) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] columns = {"*"};
		String[] filter = {String.valueOf(owner.getId()), String.valueOf(pc.getId())};
		
		//Execute the query
		Cursor rs = db.query(precontacts_table, columns, "ownerID = ? AND id = ?", filter, null, null, null, null);
		//Check if there is any result
		if (rs.moveToFirst()) {
			rs.close();
			return true;
		} else {
			rs.close();
			return false;
		}
	}
	
	//Delete a precontact
	public void deletePreContact(PreContact pc, Contact o) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL("DELETE FROM " + precontacts_table + " WHERE id = " + pc.getId() + " AND ownerID = " + o.getId());
	}
	
	//Clear the device and precontact data from the DB
	public void clearDiscoveryData(Contact owner) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		//Clear the devices data
		db.execSQL("DELETE FROM " + devices_table + " WHERE status != " + Device.STATE_NOT_ANALYZED + " AND ownerID = " + owner.getId());
		//Clear the precontacts data
		db.execSQL("DELETE FROM " + precontacts_table + " WHERE ownerID = " + owner.getId());
	}

	//Update a device
	public void updateDeviceStatus(Device dv) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		//Update the device with the corresponding MAC
		db.execSQL("UPDATE " + devices_table + " SET status = " + dv.getStatus() + " WHERE mac_addr = '" + dv.getMac_addr() + "'");
	}
	
	//Add a new contact request
	public void addContactRequest(PreContact pc, Contact owner) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		//Add the precontact		
		ContentValues values = new ContentValues();
		values.put("requesterID", pc.getId());
		values.put("ownerID", owner.getId());
		values.put("alias", pc.getAlias());
		values.put("match", pc.getMatchingPercent());
		values.put("found_time", getSQLDate(pc.getFound_time().getTime()));
		values.put("seen", 0);
		if (pc.getAvatar() != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			pc.getAvatar().compress(Bitmap.CompressFormat.PNG, 100, stream);
			values.put("avatar", Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT));
		}
		
		db.insert(contact_requests_table, null, values);
	}
	
	//Delete a contact request
	public void deleteContactRequest(PreContact pc, Contact owner) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL("DELETE FROM " + contact_requests_table + " WHERE requesterID = " + pc.getId() + " AND ownerID = " + owner.getId());
	}
	
	//Return the amount of unseen requests
	public int getUnseenRequestCount(Contact owner) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] columns = {"count(*)"};
		String[] filter = {String.valueOf(owner.getId())};
		
		//Execute the query
		Cursor rs = db.query(contact_requests_table, columns, "ownerID = ? AND seen = 0", filter, null, null, null, null);
		//Check if there is any result
		if (rs.moveToFirst()) {
			return rs.getInt(0);
		}
		//Close the cursor
		rs.close();

		return 0;
	}
	
	//Set a request as seen
	public void setRequestSeen(PreContact pc, Contact owner) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL("UPDATE " + contact_requests_table + " SET seen = 1 WHERE requesterID = " + pc.getId() + " AND ownerID = " + owner.getId());
	}
	
	//Get the last unseen contact request
	public PreContact getLastContactRequest(Contact owner) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] columns = {"requesterID", "alias", "match", "found_time", "avatar"};
		String[] filter = {String.valueOf(owner.getId())};
		
		//Execute the query
		Cursor rs = db.query(contact_requests_table, columns, "ownerID = ? AND seen = 0", filter, null, null, "found_time", "1");
		PreContact pc = null;
		//Check if there is any result
		if (rs.moveToFirst()) {
			pc = new PreContact();
			pc.setId(rs.getInt(0));
			pc.setAlias(rs.getString(1));
			pc.setMatchingPercent(rs.getString(2));
			pc.setFound_time(Timestamp.valueOf(rs.getString(3)));
			if (rs.getString(4) != null) {
				byte[] avBytes = Base64.decode(rs.getString(4), Base64.DEFAULT);
				pc.setAvatar(BitmapFactory.decodeByteArray(avBytes, 0, avBytes.length));
			}
		}
		//Close the cursor
		rs.close();

		//Return the array of messages
		return pc;
	}
	
	//Get the last unseen contact request
	public ArrayList<PreContact> getContactRequests(Contact owner) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		ArrayList<PreContact> pcs = new ArrayList<PreContact>();
		
		String[] columns = {"requesterID", "alias", "match", "found_time", "avatar"};
		String[] filter = {String.valueOf(owner.getId())};
		
		//Execute the query
		Cursor rs = db.query(contact_requests_table, columns, "ownerID = ?", filter, null, null, "found_time", null);
		PreContact pc = null;
		//Check if there is any result
		while (rs.moveToNext()) {
			pc = new PreContact();
			pc.setId(rs.getInt(0));
			pc.setAlias(rs.getString(1));
			pc.setMatchingPercent(rs.getString(2));
			pc.setFound_time(Timestamp.valueOf(rs.getString(3)));
			if (rs.getString(4) != null) {
				byte[] avBytes = Base64.decode(rs.getString(4), Base64.DEFAULT);
				pc.setAvatar(BitmapFactory.decodeByteArray(avBytes, 0, avBytes.length));
			}
			pcs.add(pc);
		}
		//Close the cursor
		rs.close();

		//Return the array of messages
		return pcs;
	}
	
	//Delete a request
	public void deleteRequest(PreContact pc, Contact owner) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL("DELETE FROM " + contact_requests_table + " WHERE requesterID = " + pc.getId() + " AND ownerID = " + owner.getId());
	}
	
	//Transform a long timestamp into a valid SQLite date string
	private String getSQLDate(long d) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date(d));
	}
	
	//Obvious...
	private boolean toBolean(int intValue) {
		return (intValue != 0);
	}

}

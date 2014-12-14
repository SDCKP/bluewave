package com.synxapps.bluewave.util;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.synxapps.bluewave.ChatActivity;
import com.synxapps.bluewave.MainActivity;
import com.synxapps.bluewave.ProfileViewActivity;
import com.synxapps.bluewave.RequestsActivity;
import com.synxapps.bluewave.entity.Contact;
import com.synxapps.bluewave.entity.ContactProfile;
import com.synxapps.bluewave.entity.Device;
import com.synxapps.bluewave.entity.Message;
import com.synxapps.bluewave.entity.PreContact;
import com.synxapps.bluewave.enums.Gender;
import com.synxapps.bluewave.enums.Interests;
import com.synxapps.bluewave.enums.LookingFor;
import com.synxapps.bluewave.enums.Nationality;
import com.synxapps.bluewave.R;

public class BackgroundNetwork {
	
	static private BackgroundNetwork instance;
	private Activity mainActivity;
	private int msgCount, requestCount;
	private LocalDBHandler db;
	private static boolean isRunning = false;
	
	public static BackgroundNetwork getInstance() {
		if (instance == null) {
			instance = new BackgroundNetwork();
		}
		return instance;
	}
	
	private BackgroundNetwork() {
		msgCount = 0;
		requestCount = 0;
	}
	
	//Initialize a background process that check for server updates 
	//and show notifications or do specific actions accordingly
	public void initServerListener(final Contact owner, final Activity mainActivity) {
		this.mainActivity = mainActivity;
		db = new LocalDBHandler(this.mainActivity);
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean notify = false;
				isRunning = true;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {}
				while(db.isUserLogged()) {
					/*Check for new chat messages*/
					//Create the parameters to be sent to the server
					BasicNameValuePair[] params = new BasicNameValuePair[3];
					params[0] = new BasicNameValuePair("type", "chatmanager");
					params[1] = new BasicNameValuePair("action", "getmsgs");
					params[2] = new BasicNameValuePair("id", String.valueOf(owner.getId()));
					//Create the network handler and send the request
					RESTHandler handler = RESTHandler.getInstance();
					String response = handler.performRequest(params);
					//Add the messages to the array of messages if there are new messages
					if (!response.equals("0") && !response.equals("timeout")) {
						try {
							//Parse the server reply with the new messages
							JSONArray jsa = new JSONArray(response);
							//Iterate on each message and save it on an array of messages
							for (int mc = 0; mc < jsa.length(); mc++) {
								Message m = new Message();
								m.setId(jsa.getJSONObject(mc).getInt("msgid"));
								m.setId_from(jsa.getJSONObject(mc).getInt("id_from"));
								m.setId_to(owner.getId());
								m.setCreation_time(Timestamp.valueOf(jsa.getJSONObject(mc).getString("send_time")));
								m.setContent(jsa.getJSONObject(mc).getString("content"));
								m.setRead(false);
								if (!db.messageExists(m)) {
									//Save the new messages on the DB
									db.saveMessage(m);
									//Update the last message from the contact
									Contact c = new Contact();
									c.setId(m.getId_from());
									db.updateContactLastMessage(c, owner, m);
								}
							}
							//Notify the user with notifications
							notify = true;
							
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					/*End of the check for the new messages*/
					
					/*Check for new contact requests*/
					//Create the parameters to be sent to the server
					BasicNameValuePair[] params2 = new BasicNameValuePair[3];
					params2[0] = new BasicNameValuePair("type", "contact_manager");
					params2[1] = new BasicNameValuePair("action", "getrequests");
					params2[2] = new BasicNameValuePair("id", String.valueOf(owner.getId()));
					//Create the network handler and send the request
					RESTHandler handler2 = RESTHandler.getInstance();
					String response2 = handler2.performRequest(params2);
					//Add the messages to the array of messages if there are new messages
					if (!response2.equals("0") && !response2.equals("timeout")) {
						try {
							//Parse the server reply with the new messages
							JSONArray jsa = new JSONArray(response2);
							//Iterate on each message and save it on an array of messages
							for (int rc = 0; rc < jsa.length(); rc++) {
								PreContact pc = new PreContact();
								pc.setId(jsa.getJSONObject(rc).getInt("requester_id"));
								pc.setAlias(jsa.getJSONObject(rc).getString("alias"));
								pc.setMatchingPercent("N/A");
								pc.setFound_time(Timestamp.valueOf(jsa.getJSONObject(rc).getString("found_time")));
								byte[] avBytes = Base64.decode(jsa.getJSONObject(rc).getString("avatar"), Base64.DEFAULT);
								pc.setAvatar(BitmapFactory.decodeByteArray(avBytes, 0, avBytes.length));
								//Save the request on the db
								db.addContactRequest(pc, owner);
							}
							//Notify the user with notifications
							notify = true;
							
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					/*End of the check for new contact requests*/
					/*Check for differences on contact list*/
					//Try to get them from the server
					ArrayList<Contact> contactsSrv = BackgroundNetwork.getInstance().obtainContactData(owner);
					//Obtain the contacts from the DB
					ArrayList<Contact> contactsDB = db.getContactsFrom(owner);
					//Check for differences on contact list
					if (contactsSrv.size() != contactsDB.size()) {
						//Update the local DB with the new contact list
						db.clearContacts(owner);
						db.saveContacts(contactsSrv, owner);
						//Update the UI
						if (!mainActivity.isFinishing()) {
							((MainActivity)mainActivity).updateUI();
						}
					}
					/*End of contact list comparision*/
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {}
					/*Parse the messages and settings and notify the user accordingly*/
					msgCount = db.getUnreadMessageCount(owner);
					requestCount = db.getUnseenRequestCount(owner);
					//Get the application settings
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
			        //Checks if the notifications are enabled on the preferences
					if (notify && prefs.getBoolean("pref_notification_enable", true)) {
						int vibration_ms = 250;
						//If the vibration is disabled from the settings, set to 0 the duration of the notifications vibration
						if (!prefs.getBoolean("pref_notification_vibrate", true)) {
							vibration_ms = 0;
						}
						//Check for new messages for show the notification
						if (msgCount > 0) {
							//Check that the notifications for new messages are enabled
							if (prefs.getStringSet("pref_notification_type_list", null).contains(mainActivity.getResources().getStringArray(R.array.txt_settings_notifications_cases_values)[0])) {
								if (!mainActivity.isFinishing()) {
									((MainActivity)mainActivity).reloadContacts();
								}
								if (msgCount > 1) {
									NotificationHandler.getInstance()
									.showNotification(mainActivity, MainActivity.class,
									android.R.drawable.ic_dialog_email, vibration_ms,
									mainActivity.getResources().getString(R.string.txt_notification_newmsgs), 
									msgCount + " " + mainActivity.getResources().getString(R.string.txt_notification_messages), null);
								} else {
									Message msg = db.getLastUnreadMessage(owner);
									Contact o = new Contact();
									o.setId(msg.getId_to());
									o.setAlias(getUserInfo(msg.getId_to()).getAlias());
									Contact c = new Contact();
									c.setId(msg.getId_from());
									c.setAlias(getUserInfo(msg.getId_from()).getAlias());
									//Create a bundle and put the contact info selected on it
									Bundle contactBundle = new Bundle();
									contactBundle.putInt("com.synxapps.bluewave.CONTACT_ID", c.getId());
									contactBundle.putString("com.synxapps.bluewave.CONTACT_NAME", c.getAlias());
									contactBundle.putInt("com.synxapps.bluewave.OWNER_ID", o.getId());
									contactBundle.putString("com.synxapps.bluewave.OWNER_NAME", o.getAlias());
									String mContent = msg.getContent();
									NotificationHandler.getInstance()
									.showNotification(mainActivity, ChatActivity.class,
									android.R.drawable.ic_dialog_email, vibration_ms,
									c.getAlias(), mContent, contactBundle);
								}
								notify = false;
							}
						}
						//Check for new messages for show the notification
						if (requestCount > 0) {
							//Check that the notifications for new messages are enabled
							if (prefs.getStringSet("pref_notification_type_list", null).contains(mainActivity.getResources().getStringArray(R.array.txt_settings_notifications_cases_values)[2])) {
								/*if (!mainActivity.isFinishing()) {
									((MainActivity)mainActivity).reloadContacts();
								}*/
								if (requestCount > 1) {
									Bundle reqBundle = new Bundle();
									reqBundle.putInt("com.synxapps.bluewave.ownerID", owner.getId());
									//Show the notification
									NotificationHandler.getInstance()
									.showNotification(mainActivity, RequestsActivity.class,
									android.R.drawable.ic_menu_add, vibration_ms,
									requestCount + " " + mainActivity.getResources().getString(R.string.txt_meet_add_multi_notification_request_title), 
									mainActivity.getResources().getString(R.string.txt_meet_add_multi_notification_request_desc), reqBundle);
								} else {
									PreContact pc = db.getLastContactRequest(owner);
									//Create a bundle and put the contact info selected on it
									Bundle profileBundle = new Bundle();
									profileBundle.putInt("com.synxapps.bluewave.contactID", pc.getId());
									profileBundle.putInt("com.synxapps.bluewave.ownerID", owner.getId());
									profileBundle.putString("com.synxapps.bluewave.isPreContact", "true");
									profileBundle.putString("com.synxapps.bluewave.viewtype", "request");
									NotificationHandler.getInstance()
									.showNotification(mainActivity, ProfileViewActivity.class,
									android.R.drawable.ic_menu_add, vibration_ms,
									mainActivity.getResources().getString(R.string.txt_meet_add_single_notification_request_title) + " " + pc.getAlias(), 
									mainActivity.getResources().getString(R.string.txt_meet_add_single_notification_request_desc), profileBundle);
								}
								notify = false;
							}
						}
					}
				}
			}
			
		}).start();
		isRunning = false;
	}
	
	public Contact getUserInfo(String mail) {
		//Setup the parameters to be sent
		BasicNameValuePair[] params = new BasicNameValuePair[2];
		params[0] = new BasicNameValuePair("type", "user_info");
		params[1] = new BasicNameValuePair("email", mail);
		RESTHandler handler = RESTHandler.getInstance();
		//Send the REST request to the server and gather the response
		String response = handler.performRequest(params);
		//Check for timeout
		if (response == "timeout") {
			return null;
		}
		if (!response.equalsIgnoreCase("-1") && !response.equalsIgnoreCase("-2")) {
	    	  try {
		  			//Obtain the JSON with the info of the user
	    		  	JSONObject jsc = new JSONObject(response);
		  			Contact c = new Contact();
		  			c.setId(jsc.getInt("id"));
		  			c.setAlias(jsc.getString("alias"));
		  			c.setEmail(mail);
		  			return c;
	  			} catch (JSONException ex) {
	  				Log.d("JSON Parser", "Invalid JSON format when reading the user info with email " + mail);
	  			}
	      }
		return null;
	}
	
	public Contact getUserInfo(int id) {
		//Setup the parameters to be sent
		BasicNameValuePair[] params = new BasicNameValuePair[2];
		params[0] = new BasicNameValuePair("type", "user_info");
		params[1] = new BasicNameValuePair("id", String.valueOf(id));
		RESTHandler handler = RESTHandler.getInstance();
		//Send the REST request to the server and gather the response
		String response = handler.performRequest(params);
		//Check for timeout
		if (response == "timeout") {
			return null;
		}
		if (!response.equalsIgnoreCase("-1") && !response.equalsIgnoreCase("-2")) {
	    	  try {
		  			//Obtain the JSON with the info of the user
	    		  	JSONObject jsc = new JSONObject(response);
		  			Contact c = new Contact();
		  			c.setId(id);
		  			c.setAlias(jsc.getString("alias"));
		  			return c;
	  			} catch (JSONException ex) {
	  				Log.d("JSON Parser", "Invalid JSON format when reading the user info");
	  			}
	      }
		return null;
	}
	
	//Sends a request to the server to remove a contact from the contact list
	public boolean deleteContact(Contact o, Contact c) {
		//Getting the REST handler instance
		RESTHandler handler = RESTHandler.getInstance();
		//Create the parameters for the login
		BasicNameValuePair[] params = new BasicNameValuePair[3];
		params[0] = new BasicNameValuePair("type", "delete_contact");
		params[1] = new BasicNameValuePair("ownerID", String.valueOf(o.getId()));
		params[2] = new BasicNameValuePair("contactID", String.valueOf(c.getId()));
		//Send the REST request to the server and gather the response
		String response = handler.performRequest(params);
		//Check for errors
		if (response.equalsIgnoreCase("1")) {
			return true;
		} else {
			return false;
		}
	}
	
	public ArrayList<Contact> obtainContactData(Contact contact) {
    	//Arraylist to store the contacts
    	ArrayList<Contact> data = new ArrayList<Contact>();
    	//Getting the REST handler instance
		RESTHandler handler = RESTHandler.getInstance();
		//Create the parameters for the login
		BasicNameValuePair[] params = new BasicNameValuePair[2];
		params[0] = new BasicNameValuePair("type", "contacts");
		params[1] = new BasicNameValuePair("ownerID", String.valueOf(contact.getId()));
		//Send the REST request to the server and gather the response
		String response = handler.performRequest(params);
		//Check for timeout
		if (response == "timeout") {
			return null;
		}
		//Check for errors
		if (!response.equalsIgnoreCase("-1") && !response.equalsIgnoreCase("-2") && !response.equalsIgnoreCase("-3")) {
			try {
			//Obtain the JSON with the contacts
			JSONArray jsc = new JSONArray(response);
			//Iterate through the JSON and add the new contacts on the data arraylist
			for (int i = 0; i < jsc.length(); i++) {
				Contact c = new Contact();
				c.setId(jsc.getJSONObject(i).getInt("id"));
				c.setAlias(jsc.getJSONObject(i).getString("alias"));
				c.setLastmsg(jsc.getJSONObject(i).getString("lastmsg"));
				String t = jsc.getJSONObject(i).getString("lastmsg_time");
				if (t.length() > 0) {
					c.setLastmsg_time(Timestamp.valueOf(t));
				}
				data.add(c);
				byte[] avBytes = Base64.decode(jsc.getJSONObject(i).getString("avatar"), Base64.DEFAULT);
				c.setAvatar(BitmapFactory.decodeByteArray(avBytes, 0, avBytes.length));
		      }
			} catch (JSONException ex) {
				Log.d("JSON Parser", "Invalid JSON format when reading the contact list: " + ex.getMessage());
			}
	      }
		return data;
    }
	
	public int sendMessage(int id_from, int id_to, String content) {
		//Setup the parameters to be sent
		BasicNameValuePair[] params = new BasicNameValuePair[5];
		params[0] = new BasicNameValuePair("type", "chatmanager");
		params[1] = new BasicNameValuePair("action", "sendmsg");
		params[2] = new BasicNameValuePair("from", String.valueOf(id_from));
		params[3] = new BasicNameValuePair("to", String.valueOf(id_to));
		params[4] = new BasicNameValuePair("content", content);
		RESTHandler handler = RESTHandler.getInstance();
		//Send the REST request to the server and gather the response
		String response = handler.performRequest(params);
		//Check for timeout
		if (response == "timeout") {
			return -5;
		}
		return Integer.parseInt(response);
	}
	
	public int login(String email, String pw) {
		//Getting the REST handler instance
		RESTHandler handler = RESTHandler.getInstance();
		//Create the parameters for the login
		BasicNameValuePair[] params = new BasicNameValuePair[3];
		params[0] = new BasicNameValuePair("type", "login");
		params[1] = new BasicNameValuePair("email", email);
		params[2] = new BasicNameValuePair("pass", pw);
		//Send the REST request to the server and gather the response
		String response = handler.performRequest(params);
		//Check for timeout
		if (response == "timeout") {
			return -5;
		}
		return Integer.parseInt(response);
	}

	public ContactProfile getUserProfile(ContactProfile contact, Contact owner) {
    	//Getting the REST handler instance
		RESTHandler handler = RESTHandler.getInstance();
		//Create the parameters for the profile request
		BasicNameValuePair[] params = new BasicNameValuePair[3];
		params[0] = new BasicNameValuePair("type", "profile");
		params[1] = new BasicNameValuePair("contactID", String.valueOf(contact.getId()));
		params[2] = new BasicNameValuePair("ownerID", String.valueOf(owner.getId()));
		//Send the REST request to the server and gather the response
		String response = handler.performRequest(params);
		//Check for timeout
		if (response == "timeout") {
			return null;
		}
		//Check for errors
		if (!response.equalsIgnoreCase("-1") && !response.equalsIgnoreCase("-2")) {
			try {
				//Obtain the JSON with the profile of the user
			  	JSONObject jsc = new JSONObject(response);
				contact.setAlias(jsc.getString("alias"));
				if (jsc.getString("gender") == "null") { 
					contact.setGender(Gender.NULL); 
				} else {
					contact.setGender(Gender.valueOf(jsc.getString("gender")));
				}
				if (jsc.getString("birthdate") == "null") {
					contact.setBirthdate(null);
				} else {
					contact.setBirthdate(Timestamp.valueOf(jsc.getString("birthdate")));
				}
				if (jsc.getString("nationality") == "null") { 
					contact.setNationality(Nationality.NULL);
				} else {
					contact.setNationality(Nationality.valueOf(jsc.getString("nationality")));
				}
				if (jsc.getString("lookingfor") == "null") { 
					contact.setLookingFor(LookingFor.NULL);
				} else {
					contact.setLookingFor(LookingFor.valueOf(jsc.getString("lookingfor")));
				}
				contact.setAbout(jsc.getString("about"));
				if (jsc.getString("height") == "null") {
					contact.setHeight(0);
				} else {
					contact.setHeight(jsc.getInt("height"));
				}
				if (jsc.getString("weight") == "null") {
					contact.setWeight(0);
				} else {
					contact.setWeight(jsc.getInt("weight"));
				}
				String[] intrst = jsc.getString("interests").split(",");
				ArrayList<Interests> interests = new ArrayList<Interests>();
				for (int i = 0; i < intrst.length; i++) {
					if (intrst[i] == "null") {
						interests.add(Interests.NULL);
					} else {
						interests.add(Interests.valueOf(intrst[i]));
					}
				}
				contact.setInterests(interests);
				byte[] avBytes = Base64.decode(jsc.getString("avatar"), Base64.DEFAULT);
				contact.setAvatar(BitmapFactory.decodeByteArray(avBytes, 0, avBytes.length));
			} catch (JSONException ex) {
				Log.d("JSON Parser", "Invalid JSON format when reading the user profile: " + ex.getMessage());
			}
	      }
		return contact;
	}
	
	//Sends a request to the server to remove a contact from the contact list
	public boolean changePass(Contact user, String oldpass, String newpass) {
		//Getting the REST handler instance
		RESTHandler handler = RESTHandler.getInstance();
		//Create the parameters for the password change
		BasicNameValuePair[] params = new BasicNameValuePair[4];
		params[0] = new BasicNameValuePair("type", "change_password");
		params[1] = new BasicNameValuePair("userID", String.valueOf(user.getId()));
		params[2] = new BasicNameValuePair("current_pass", oldpass);
		params[3] = new BasicNameValuePair("new_pass", newpass);
		//Send the REST request to the server and gather the response
		String response = handler.performRequest(params);
		//Check for errors
		if (response.equalsIgnoreCase("1")) {
			return true;
		} else {
			return false;
		}
	}
	
	//Sends a request to the server to remove the account
	public boolean deleteAccount(Contact user, String currentpass) {
		//Getting the REST handler instance
		RESTHandler handler = RESTHandler.getInstance();
		//Create the parameters for the account deletion
		BasicNameValuePair[] params = new BasicNameValuePair[3];
		params[0] = new BasicNameValuePair("type", "delete_account");
		params[1] = new BasicNameValuePair("userID", String.valueOf(user.getId()));
		params[2] = new BasicNameValuePair("current_pass", currentpass);
		//Send the REST request to the server and gather the response
		String response = handler.performRequest(params);
		//Check for errors
		if (response.equalsIgnoreCase("1")) {
			return true;
		} else {
			return false;
		}
	}
	
	public int updateProfile(ContactProfile owner) {
		//Compress the avatar if is set
		String encAvatar = "";
		if (owner.getAvatar() != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			owner.getAvatar().compress(Bitmap.CompressFormat.PNG, 100, stream);
			encAvatar = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
		}
		//Getting the REST handler instance
		RESTHandler handler = RESTHandler.getInstance();
		//Create the parameters for the profile edition
		BasicNameValuePair[] params = new BasicNameValuePair[12];
		params[0] = new BasicNameValuePair("type", "profile_edit");
		params[1] = new BasicNameValuePair("userID", String.valueOf(owner.getId()));
		params[2] = new BasicNameValuePair("alias", owner.getAlias());
		params[3] = new BasicNameValuePair("gender", owner.getGender().toString());
		params[4] = new BasicNameValuePair("birthdate", (owner.getBirthdate() != null)?owner.getBirthdate().toString():"");
		params[5] = new BasicNameValuePair("nationality", owner.getNationality().toString());
		params[6] = new BasicNameValuePair("lookingfor", owner.getLookingFor().toString());
		params[7] = new BasicNameValuePair("about", owner.getAbout());
		params[8] = new BasicNameValuePair("height", String.valueOf(owner.getHeight()));
		params[9] = new BasicNameValuePair("weight", String.valueOf(owner.getWeight()));
		params[10] = new BasicNameValuePair("interests", owner.getInterestsArray());
		params[11] = new BasicNameValuePair("avatar", encAvatar);
		//Send the REST request to the server and gather the response
		String response = handler.performRequest(params);
		//Return the response
		return Integer.parseInt(response);
	}

	public PreContact getDeviceInfo(Device dv) {
		//Getting the REST handler instance
		RESTHandler handler = RESTHandler.getInstance();
		//Create the parameters for the profile edition
		BasicNameValuePair[] params = new BasicNameValuePair[3];
		params[0] = new BasicNameValuePair("type", "device_info");
		params[1] = new BasicNameValuePair("ownerMAC", BluetoothManager.getInstance().getBluetoothMAC());
		params[2] = new BasicNameValuePair("targetMAC", dv.getMac_addr());
		//Send the REST request to the server and gather the response
		String response = handler.performRequest(params);
		if (response.equals("timeout")) {
			return null;
		}
		//Check for errors
		if (!response.equalsIgnoreCase("-1") && !response.equalsIgnoreCase("-2") && !response.equalsIgnoreCase("-3")) {
			try {
				//Obtain the JSON with the device info
				JSONObject jso = new JSONObject(response);
				//Create a new precontact with the contact data
				PreContact pc = new PreContact();
				pc.setId(jso.getInt("id"));
				pc.setAlias(jso.getString("alias"));
				pc.setMatchingPercent("N/A");
				pc.setFound_time(dv.getFound_time());
				byte[] avBytes = Base64.decode(jso.getString("avatar"), Base64.DEFAULT);
				pc.setAvatar(BitmapFactory.decodeByteArray(avBytes, 0, avBytes.length));
				return pc;
			} catch (JSONException ex) {
				Log.d("JSON Parser", "Invalid JSON format when parsing the precontact data\n" + ex.getMessage());
				return new PreContact();
			}
	      } else { //Error received
	    	  return new PreContact();
	      }
	}
	
	public int sendContactRequest(Contact owner, Contact target) {
		//Setup the parameters to be sent
		BasicNameValuePair[] params = new BasicNameValuePair[4];
		params[0] = new BasicNameValuePair("type", "contact_manager");
		params[1] = new BasicNameValuePair("action", "sendrequest");
		params[2] = new BasicNameValuePair("ownerID", String.valueOf(owner.getId()));
		params[3] = new BasicNameValuePair("targetID", String.valueOf(target.getId()));
		RESTHandler handler = RESTHandler.getInstance();
		//Send the REST request to the server and gather the response
		String response = handler.performRequest(params);
		//Check for timeout
		if (response == "timeout") {
			return -5;
		}
		return Integer.parseInt(response);
	}
	
	public int rejectContactRequest(Contact owner, Contact target) {
		//Setup the parameters to be sent
		BasicNameValuePair[] params = new BasicNameValuePair[4];
		params[0] = new BasicNameValuePair("type", "contact_manager");
		params[1] = new BasicNameValuePair("action", "rejectrequest");
		params[2] = new BasicNameValuePair("ownerID", String.valueOf(owner.getId()));
		params[3] = new BasicNameValuePair("targetID", String.valueOf(target.getId()));
		RESTHandler handler = RESTHandler.getInstance();
		//Send the REST request to the server and gather the response
		String response = handler.performRequest(params);
		//Check for timeout
		if (response == "timeout") {
			return -5;
		}
		return Integer.parseInt(response);
	}
	
	public boolean isRunning() {
		return isRunning;
	}
}

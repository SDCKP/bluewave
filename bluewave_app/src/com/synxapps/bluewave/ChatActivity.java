package com.synxapps.bluewave;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.synxapps.bluewave.adapters.ChatListView;
import com.synxapps.bluewave.adapters.ChatMessagesAdapter;
import com.synxapps.bluewave.entity.Contact;
import com.synxapps.bluewave.entity.Message;
import com.synxapps.bluewave.util.BackgroundNetwork;
import com.synxapps.bluewave.util.LocalDBHandler;
import com.synxapps.bluewave.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity {

	private ImageView sendBt;
	private TextView msg_field;
	private ListView msg_list;
	private Context ui;
	private Contact contact;
	private Contact owner;
	private LocalDBHandler db;
	private ChatMessagesAdapter chatMsgAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		ui = this;
		db = new LocalDBHandler(this);
		
		//Create the contact object with the data of the contact from the db
		contact = new Contact();
		contact.setId(getIntent().getExtras().getInt("com.synxapps.bluewave.CONTACT_ID"));
		contact = db.getContactById(contact);
		//Get the ID of the owner
		owner = new Contact();
		owner.setId(getIntent().getExtras().getInt("com.synxapps.bluewave.OWNER_ID"));
		owner.setAlias(getIntent().getExtras().getString("com.synxapps.bluewave.OWNER_NAME"));
		
		//Obtain the stored messages from the local db for this contact
		ArrayList<Message> saved_msgs = new ArrayList<Message>();
		saved_msgs = db.getMessagesFromContact(owner, contact);
		
		//Create the messages adapter with the data from the contact and sets the adapter on it
		chatMsgAdapter = new ChatMessagesAdapter(this, contact);
		//Set the stored messages on the adapter from the db
		chatMsgAdapter.add(saved_msgs);
		
		//Add the back button on the actionbar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//Set the title of the actionbar with the name of the contact
		getActionBar().setTitle(contact.getAlias());
		//Set the icon of the actionbar with the contact avatar
		getActionBar().setIcon(new BitmapDrawable(getResources(), contact.getAvatar()));
		
		//Get component's references
		sendBt = (ImageView)findViewById(R.id.chat_bt_send);
		msg_field = (TextView)findViewById(R.id.chat_field_msg);
		msg_list = (ChatListView)findViewById(R.id.chat_list_messages);
		msg_list.setDivider(null);
		//Set the adapter to the listview
		msg_list.setAdapter(chatMsgAdapter);

		//Register the send button click
		sendBt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (msg_field.getText().toString().length() > 0) {
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							//Create the message entity and store it on the array of messages
							Message m = new Message();
							m.setId_from(owner.getId());
							m.setId_to(contact.getId());
							m.setAlias_from(owner.getAlias());
							m.setAlias_to(contact.getAlias());
							m.setCreation_time(Timestamp.valueOf((String) new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date().getTime())));
							m.setContent(msg_field.getText().toString());
							m.setRead(true);
							//Send the message to the server
							sendMessage(m);
							//Add the message on the UI
							addMessageToUI(m);
							//Save the message on the local database
							db.saveMessage(m);
							//Update the last message from the contact
							db.updateContactLastMessage(contact, owner, m);
							//Clear the input field
							clearInput();
						}
					}).start();
				}
			}
		});
		
		//Check for new messages
		messageChecker();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;

			case R.id.chat_delete:
				//Ask the user if he want to delete the contact
		        new AlertDialog.Builder(this)
		        .setIcon(R.drawable.ic_dialog_warning)
		        .setTitle(R.string.txt_delete)
		        .setMessage(R.string.txt_delete_confirmation)
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface dialog, int which) {
		            	//User clicks on yes, sends the request of link deletion to the server
		            	if (BackgroundNetwork.getInstance().deleteContact(owner, contact)) {
		            		//No errors, delete the contact localy, notice the user and refresh the list
			        		db.deleteContact(contact, owner);
			        		showToast(getResources().getString(R.string.txt_delete_success));
		            		startActivity(new Intent(ui, MainActivity.class));
		            		finish();
		            	} else {
		            		//Error deleting the contact, do nothing but notify the user
		            		showToast(getResources().getString(R.string.txt_delete_error));
		            	}
		            }
		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();
				return true;
			case R.id.chat_profile:
				//Create the intent of the profile and send the ID of the contact to get the profile from
				Intent profileView = new Intent(this, ProfileViewActivity.class);
				profileView.putExtra("com.synxapps.bluewave.contactID", contact.getId());
				profileView.putExtra("com.synxapps.bluewave.ownerID", owner.getId());
				startActivity(profileView);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void clearInput() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				msg_field.setText("");
			}
		});
	}
	
	//Sends a message to the server msg queue and returns the message id, returns -1 if error
	public boolean sendMessage(Message m) {
		int response = BackgroundNetwork.getInstance().sendMessage(m.getId_from(), m.getId_to(), m.getContent());
		switch (response) {
	      case -1: //Missing parameters (never should happen)
	    	  showToast(getResources().getString(R.string.err_missing_params));
	    	  break;
	      case -2: //Invalid action
	    	  showToast(getResources().getString(R.string.err_msg_not_sent));
	    	  break;
	      case -3: //Contact is not on the contact list
	    	  showToast(getResources().getString(R.string.err_not_in_contacts));
	    	  break;
	      default: //The response wasnt an error
	    	  //Check if its a positive number (valid msg id)
	    	  if (response >= 0) {
	    		  //Save the id of the msg on the message object
	    		  m.setId(response);
	    		  return true;
	    	  }
	    	  //Is not a valid response (never should trigger unless server error)
	    	  showToast(getResources().getString(R.string.err_msg_not_sent));
	      }
		return false;
	}
	
	private void addMessageToUI(final Message m) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//Add the message on the adapter
				chatMsgAdapter.add(m);
				//Scroll the listview to the last msg
				msg_list.smoothScrollToPosition(chatMsgAdapter.getCount() - 1);
			}
		});
	}
	
	private void messageChecker() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				//This thread keeps running checking the local DB for new messages until the activity is closed
				while (!isFinishing()) {
					//Get an arraylist of unread messages from the DB
					ArrayList<Message> msgs = db.getUnreadMessagesFromContact(owner, contact);
					for (Message m : msgs) {
						//Add the message to the UI
						addMessageToUI(m);
						//Set the message as read
						db.setMessageRead(m);
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
				}
			}
		}).start();
	}
	
	public void showToast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(ui, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

}

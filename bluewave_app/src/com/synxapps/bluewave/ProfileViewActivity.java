package com.synxapps.bluewave;

import java.io.ByteArrayOutputStream;

import com.synxapps.bluewave.R;
import com.synxapps.bluewave.entity.Contact;
import com.synxapps.bluewave.entity.ContactProfile;
import com.synxapps.bluewave.enums.Gender;
import com.synxapps.bluewave.enums.LookingFor;
import com.synxapps.bluewave.enums.Nationality;
import com.synxapps.bluewave.util.BackgroundNetwork;
import com.synxapps.bluewave.util.LocalDBHandler;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class ProfileViewActivity extends Activity {
	private ContactProfile contact;
	private Contact owner;
	private ProgressDialog loadingDialog;
	private String type;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_view);
		
		//Set the default type (normal)
		type = "contact";
		//Check if its another kind of type (meet or request)
		if (getIntent().getExtras().getString("com.synxapps.bluewave.viewtype") != null) {
			type = getIntent().getExtras().getString("com.synxapps.bluewave.viewtype");
		}
		// Show the Up button in the action bar.
		setupActionBar();
		
		//Creates a loading dialog
		loadingDialog = ProgressDialog.show(ProfileViewActivity.this, "", 
                getResources().getString(R.string.txt_profile_fetching), true);
		
		//Obtain the ID of the user to get the data from the server
		contact = new ContactProfile();
		contact.setId(getIntent().getExtras().getInt("com.synxapps.bluewave.contactID"));
		//Obtain the ID from the owner
		owner = new Contact();
		owner.setId(getIntent().getExtras().getInt("com.synxapps.bluewave.ownerID"));
		
		new Thread(new Runnable() {
					
			@Override
			public void run() {
				//Send the request to the server to get the user information
				contact = BackgroundNetwork.getInstance().getUserProfile(contact, owner);
				
				//Check for timeout
				if (contact == null) {
					//Notify the timeout and close the activity
					showToast(getResources().getString(R.string.err_timeout));
					finish();
				} else {
					//Check that it got the data
					if (contact.getAlias() != null) {
						//Obtain the components references
						final TextView profile_alias = (TextView)findViewById(R.id.profileview_alias);
						final TextView profile_gender = (TextView)findViewById(R.id.profileview_gender_value);
						final TextView profile_age = (TextView)findViewById(R.id.profileview_age_value);
						final TextView profile_nationality = (TextView)findViewById(R.id.profileview_nationality_value);
						final TextView profile_lookingfor = (TextView)findViewById(R.id.profileview_lookingfor_value);
						final TextView profile_about = (TextView)findViewById(R.id.profileview_about_value);
						final TextView profile_height = (TextView)findViewById(R.id.profileview_height_value);
						final TextView profile_weight = (TextView)findViewById(R.id.profileview_weight_value);
						final TextView profile_interests = (TextView)findViewById(R.id.profileview_interests_list);
						final ImageView profile_avatar = (ImageView)findViewById(R.id.profileview_avatar);
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								//Set the data on the components acording to the values from the database
								profile_alias.setText(contact.getAlias());
								profile_gender.setText(
										stringify(
												contact
												.getGender()
												.toString()));
								if (contact.getAge() == -1) {
									profile_age.setText(getResources().getString(R.string.attr_NULL));
								} else {
									profile_age.setText(String.valueOf(contact.getAge()));
								}
								if (contact.getNationality() == null) { contact.setNationality(Nationality.NULL); }
								profile_nationality.setText(stringify(contact.getNationality().toString()));
								if (contact.getLookingFor() == null) { contact.setLookingFor(LookingFor.NULL); }
								profile_lookingfor.setText(stringify(contact.getLookingFor().toString()));
								if (contact.getAbout() != null && contact.getAbout() != "null") {
									profile_about.setText(contact.getAbout());
								} else {
									profile_about.setText(getResources().getString(R.string.attr_NULL));
								}
								if (contact.getHeight() == 0) { 
									profile_height.setText(stringify(Gender.NULL.toString()));
								} else {
									profile_height.setText(String.valueOf(contact.getHeight()));
								}
								if (contact.getWeight() == 0) { 
									profile_weight.setText(stringify(Gender.NULL.toString()));
								} else {
									profile_weight.setText(String.valueOf(contact.getWeight()));
								}
								String intersts = "";
								for (int i = 0; i < contact.getInterests().size(); i++) {
									intersts += stringify(contact.getInterests().get(i).toString());
									if (i < contact.getInterests().size()-1) {
										intersts += ", ";
									}
								}
								profile_interests.setText(intersts);
								if (contact.getAvatar() != null) {
									profile_avatar.setImageBitmap(contact.getAvatar());
									profile_avatar.setOnClickListener(new View.OnClickListener() {
										
										@Override
										public void onClick(View v) {
											Intent imageViewer = new Intent(ProfileViewActivity.this, ImageViewer.class);
											ByteArrayOutputStream stream = new ByteArrayOutputStream();
											contact.getAvatar().compress(Bitmap.CompressFormat.PNG, 100, stream);
											imageViewer.putExtra("com.synxapps.bluewave.AVATAR", stream.toByteArray());
											imageViewer.putExtra("com.synxapps.bluewave.ALIAS", contact.getAlias());
											startActivity(imageViewer);
										}
									});
								}
								
								//Hide the loading dialog
								loadingDialog.dismiss();
							}
						});
					} else { //Profile data wasnt retrieved. Notify the error and terminate the activity
						showToast(getResources().getString(R.string.err_not_in_contacts));
						finish();
					}
				}
			}
		}).start();
		
	}
	
	public String stringify(String attr) {
		return getResources().getString(getResources().getIdentifier("attr_" + attr, "string", ProfileViewActivity.this.getPackageName()));
	}

	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (type.equals("meet")) {
			getMenuInflater().inflate(R.menu.profile_precontact, menu);
		} else if (type.equals("request")) {
			getMenuInflater().inflate(R.menu.profile_request, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//Terminate the profile activity (brings the previous activity back)
			finish();
			return true;
		case R.id.profile_precontact_add:
			//Send the contact request to the user
			int response = BackgroundNetwork.getInstance().sendContactRequest(owner, contact);
			if (response == 1) {
				//Show a message confirming it
				showToast(getResources().getString(R.string.txt_meet_add_confirm));
				//Delete the contact from the meet list
				LocalDBHandler db = new LocalDBHandler(this);
				db.deletePreContact(contact, owner);
				db.deleteRequest(contact, owner);
				//Close the profile view
				finish();
			} else if (response == 2) {
				//Show a message confirming it
				showToast(getResources().getString(R.string.txt_meet_added));
				//Delete the contact from the db
				LocalDBHandler db = new LocalDBHandler(this);
				db.deletePreContact(contact, owner);
				db.deleteRequest(contact, owner);
				//Add the contact to the contact list
				db.addContact(contact, owner);
				//Close the profile view
				finish();
			} else {
				//Show the error accordingly
				String msg = "";
				switch(response) {
					case -1:
						msg = getResources().getString(R.string.err_missing_params);
						break;
					case -2:
						msg = getResources().getString(R.string.err_missing_params);
						break;
					case -3:
						msg = getResources().getString(R.string.err_already_contact);
						break;
					case -4:
						msg = getResources().getString(R.string.err_user_not_exists);
						break;
					case -5:
						msg = getResources().getString(R.string.err_timeout);
						break;
					default:
						msg = getResources().getString(R.string.err_missing_params);
				}
				showToast(msg);
			}
			return true;
		case R.id.profile_precontact_ignore:
			//Send the contact request to the user
			int response2 = BackgroundNetwork.getInstance().rejectContactRequest(owner, contact);
			if (response2 == 1) {
				//Show a message confirming it
				showToast(getResources().getString(R.string.txt_meet_ignored_ok));
				//Delete the request from the DB
				LocalDBHandler db = new LocalDBHandler(this);
				db.deleteRequest(contact, owner);
				//Close the profile view activity
				finish();
			} else {
				//Show the error accordingly
				String msg = "";
				switch(response2) {
					case -1:
						msg = getResources().getString(R.string.err_missing_params);
						break;
					case -2:
						msg = getResources().getString(R.string.err_missing_params);
						break;
					case -3:
						msg = getResources().getString(R.string.err_already_contact);
						break;
					case -4:
						msg = getResources().getString(R.string.err_user_not_exists);
						break;
					case -5:
						msg = getResources().getString(R.string.err_timeout);
						break;
					default:
						msg = getResources().getString(R.string.err_missing_params);
				}
				showToast(msg);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void showToast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
			}
		});
	}
}

package com.synxapps.bluewave;

import com.synxapps.bluewave.entity.Contact;
import com.synxapps.bluewave.util.BackgroundNetwork;
import com.synxapps.bluewave.util.LocalDBHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class SettingsActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		
		// Display the fragment as the main content.
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        
        SettingsFragment mPrefsFragment = new SettingsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();
	}

	private void setupActionBar() {
			getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static class SettingsFragment extends PreferenceFragment implements OnPreferenceClickListener {
		 
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            
            //Bind the preference clicks to this listener
            findPreference("pref_account_logout").setOnPreferenceClickListener(this);
            findPreference("pref_account_pass_change").setOnPreferenceClickListener(this);
            findPreference("pref_account_delete").setOnPreferenceClickListener(this);
            findPreference("pref_about_FAQ").setOnPreferenceClickListener(this);
        }

		@Override
		public boolean onPreferenceClick(Preference preference) {
			String key = preference.getKey();
			//Log out
			if (key.equals("pref_account_logout")) {
				logout();
				return true;
			}
			//Password change
			if (key.equals("pref_account_pass_change")) {
				changepass();
				return true;
			}
			//Account delete
			if (key.equals("pref_account_delete")) {
				deleteaccount();
				return true;
			}
			//Display FAQ dialog
			if (key.equals("pref_about_FAQ")) {
				showFAQ();
				return true;
			}
			return false;
		}

		private void logout() {
			new AlertDialog.Builder(getActivity())
	        .setIcon(R.drawable.ic_dialog_warning)
	        .setTitle(R.string.txt_settings_account_logout)
	        .setMessage(R.string.txt_settings_account_logout_warningmsg)
	        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	LocalDBHandler db = new LocalDBHandler(getActivity());
	            	//Delete the stored login data from the DB
	            	db.clearUserLogin();
	            	//Open the login activity
	            	Intent loginActivity = new Intent(getActivity(), LoginActivity.class);
	            	startActivity(loginActivity);
	            	//Close the settings activity
	            	getActivity().finish();
	            }
	        })
	        .setNegativeButton(android.R.string.no, null)
	        .show();
		}
		
		private void changepass() {
			//Input field for the dialogs
			final EditText oldpass_input = new EditText(getActivity());
			final EditText newpass_input = new EditText(getActivity());
			final EditText newpass2_input = new EditText(getActivity());
			//Create the input dialogs
			new AlertDialog.Builder(getActivity())
	        .setTitle(R.string.txt_settings_account_pass_change_step1_title)
	        .setMessage(R.string.txt_settings_account_pass_change_step1_desc)
			.setView(oldpass_input)
	        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	//Obtain the old password
	            	final String oldpass = oldpass_input.getText().toString();
	            	//Ask for the new password
	    			new AlertDialog.Builder(getActivity())
	    	        .setTitle(R.string.txt_settings_account_pass_change_step2_title)
	    	        .setMessage(R.string.txt_settings_account_pass_change_step2_desc)
	    			.setView(newpass_input)
	    	        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	    	            @Override
	    	            public void onClick(DialogInterface dialog, int which) {
	    	            	//Obtain the new password
	    	            	final String newpass = newpass_input.getText().toString();
	    	            	//Ask to repeat the new password
	    	    			new AlertDialog.Builder(getActivity())
	    	    	        .setTitle(R.string.txt_settings_account_pass_change_step3_title)
	    	    	        .setMessage(R.string.txt_settings_account_pass_change_step3_desc)
	    	    			.setView(newpass2_input)
	    	    	        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	    	    	            @Override
	    	    	            public void onClick(DialogInterface dialog, int which) {
	    	    	            	if (newpass.equals(newpass2_input.getText().toString())) {
	    	    	            		//Get the userID from the DB and create a contact object with it
	    	    	            		LocalDBHandler db = new LocalDBHandler(getActivity());
	    	    	            		Contact user = new Contact();
	    	    	            		user.setId(Integer.parseInt(db.getUserLoginData()[2]));
	    	    	            		//Send the password change request
	    	    	            		if (BackgroundNetwork.getInstance().changePass(user, oldpass, newpass)) {
	    	    	            			//Update the password on the local database
	    	    	            			db.updateUserPass(newpass);
	    	    	            			//Indicate the user the successful change of the password
	    	    	            			showToast(getResources().getString(R.string.txt_settings_account_pass_change_success));
	    	    	            		} else {
	    	    	            			//Pass not changed (wrong pass normally)
	    	    	            			showToast(getResources().getString(R.string.err_pass_wrong));
	    	    	            		}
	    	    	            	} else {
	    	    	            		//Passwords doesnt match
	    	    	            		showToast(getResources().getString(R.string.err_pass_unmatch));
	    	    	            	}
	    	    	            }
	    	    	        })
	    	    	        .setNegativeButton(android.R.string.no, null)
	    	    	        .show();
	    	            }
	    	        })
	    	        .setNegativeButton(android.R.string.no, null)
	    	        .show();
	            }
	        })
	        .setNegativeButton(android.R.string.no, null)
	        .show();
		}
		
		private void showFAQ() {
			new AlertDialog.Builder(getActivity())
	        .setIcon(R.drawable.ic_dialog_info)
	        .setTitle(R.string.txt_settings_about_FAQ)
	        .setMessage(Html.fromHtml(getString(R.string.txt_settings_about_FAQ_content)))
	        .setPositiveButton(android.R.string.yes, null)
	        .show();
		}
		
		private void deleteaccount() {
			final EditText current_pass = new EditText(getActivity());
			//Request the current password of the account
			new AlertDialog.Builder(getActivity())
	        .setTitle(R.string.txt_settings_account_delete)
	        .setMessage(R.string.txt_settings_account_pass_change_step1_desc)
			.setView(current_pass)
	        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					final LocalDBHandler db = new LocalDBHandler(getActivity());
					final String[] userdata = db.getUserLoginData();
					//Check if the password is valid
					if (BackgroundNetwork.getInstance().login(userdata[0], current_pass.getText().toString()) == Integer.parseInt(userdata[2])) {
						new AlertDialog.Builder(getActivity())
				        .setIcon(R.drawable.ic_dialog_warning)
				        .setTitle(R.string.txt_settings_account_delete)
				        .setMessage(R.string.txt_settings_account_delete_dialog)
				        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				            @Override
				            public void onClick(DialogInterface dialog, int which) {
				            	//Generate a contact with the data from the DB
								Contact user = new Contact();
								user.setId(Integer.parseInt(userdata[2]));
								user.setEmail(userdata[0]);
				            	//Send the delete account request to the server
				            	if (BackgroundNetwork.getInstance().deleteAccount(user, current_pass.getText().toString())) {
				            		//Delete the stored login data from the DB
					            	db.clearUserLogin();
					            	//Open the login activity
					            	Intent loginActivity = new Intent(getActivity(), LoginActivity.class);
					            	startActivity(loginActivity);
					            	//Close the settings activity
					            	getActivity().finish();
				            	} else {
				            		showToast(getResources().getString(R.string.err_account_delete));
				            	}
				            }
				        })
				        .setNegativeButton(android.R.string.no, null)
				        .show();
					} else {
						showToast(getResources().getString(R.string.err_pass_wrong));
					}
				}
			})
			.setNegativeButton(android.R.string.no, null)
			.show();
		}
		
		public void showToast(final String msg) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
}

package com.synxapps.bluewave;

import java.util.ArrayList;
import java.util.Locale;

import com.synxapps.bluewave.adapters.MeetListAdapter;
import com.synxapps.bluewave.adapters.TalkContactsAdapter;
import com.synxapps.bluewave.entity.Contact;
import com.synxapps.bluewave.entity.Device;
import com.synxapps.bluewave.entity.PreContact;
import com.synxapps.bluewave.util.BackgroundNetwork;
import com.synxapps.bluewave.util.BluetoothManager;
import com.synxapps.bluewave.util.LocalDBHandler;
import com.synxapps.bluewave.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	private static Contact owner;
	static Context ui;
	private ActionBar actionBar;
	private static LocalDBHandler db;
	private static boolean discovery;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Initialize the discovery to false
		discovery = false;
		
		//Get the UI reference
		ui = this;
		
		//Check the DB for previously-logged user
		db = new LocalDBHandler(this);
		String[] info = db.getUserLoginData();
		if (info[0] == null) {
			//Start the login page intent
			Intent loginActivity = new Intent(ui, LoginActivity.class);
			startActivity(loginActivity);
			finish();
		} else {
			setContentView(R.layout.activity_main);

			//Obtain the info of the owner from the DB
			db = new LocalDBHandler(this);
			String[] uTMP = db.getUserLoginData();
			owner = new Contact();
			owner.setId(Integer.parseInt(uTMP[2]));
			owner.setEmail(uTMP[0]);
			
			// Set up the action bar.
			actionBar = getActionBar();
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			
			// Create the adapter that will return a fragment for each of the three
			// primary sections of the app.
			mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
			
			// Set up the ViewPager with the sections adapter.
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mSectionsPagerAdapter);
	
			// When swiping between different sections, select the corresponding
			// tab. We can also use ActionBar.Tab#select() to do this if we have
			// a reference to the Tab.
			mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
						@Override
						public void onPageSelected(int position) {
							actionBar.setSelectedNavigationItem(position);
						}
					});
	
			// For each of the sections in the app, add a tab to the action bar.
			for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
				// Create a tab with text corresponding to the page title defined by
				// the adapter. Also specify this Activity object, which implements
				// the TabListener interface, as the callback (listener) for when
				// this tab is selected.
				actionBar.addTab(actionBar.newTab()
						.setText(mSectionsPagerAdapter.getPageTitle(i))
						.setTabListener(this));
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//Keep logged in
		tryLogin();
		//Reload the contact list every time the activity is brought to front
		reloadContacts();
	}
	
	//Reload the contact list
	public void reloadContacts() {
		final ArrayList<Contact> contacts = db.getContactsFrom(owner);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ListView clist = (ListView)findViewById(R.id.talk_listview);
				if (clist != null) {
					((TalkContactsAdapter)clist.getAdapter()).reloadContacts(contacts);
				}
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.main_requests:
			//Start the requests activity, sending the owner ID
			Intent requests = new Intent(this, RequestsActivity.class);
			requests.putExtra("com.synxapps.bluewave.ownerID", owner.getId());
			startActivity(requests);
			break;
		case R.id.main_profile:
			//Start the profile activity, sending the owner ID
			Intent profile_edit = new Intent(this, ProfileEditActivity.class);
			profile_edit.putExtra("com.synxapps.bluewave.ownerID", owner.getId());
			startActivity(profile_edit);
			break;
		case R.id.main_settings:
			//Start the settings activity
			Intent settingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
			startActivity(settingsActivity);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new SectionFragment();
			Bundle args = new Bundle();
			args.putInt(SectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.tab_talk).toUpperCase(l);
			case 1:
				return getString(R.string.tab_meet).toUpperCase(l);
			case 2:
				return getString(R.string.tab_discover).toUpperCase(l);
			}
			return null;
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	      super.onCreateContextMenu(menu, v, menuInfo);
	      if (v.getId()==R.id.talk_listview) {
	          getMenuInflater().inflate(R.menu.chat, menu);
	      }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	      final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	      switch(item.getItemId()) {
	         case R.id.chat_delete:
	        	//Ask the user if he want to delete the contact
		        new AlertDialog.Builder(this)
		        .setIcon(R.drawable.ic_dialog_warning)
		        .setTitle(R.string.txt_delete)
		        .setMessage(R.string.txt_delete_confirmation)
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface dialog, int which) {
		            	//Obtain the contact from the list
			        	Contact contact = (Contact)((ListView)findViewById(R.id.talk_listview)).getAdapter().getItem(info.position);
				        //User clicks on yes, sends the request of link deletion to the server
			        	if (BackgroundNetwork.getInstance().deleteContact(owner, contact)) {
			        		//No errors, delete the contact localy, notice the user and refresh the list
			        		db.deleteContact(contact, owner);
			        		showToast(getResources().getString(R.string.txt_delete_success));
			        		((TalkContactsAdapter)((ListView)findViewById(R.id.talk_listview)).getAdapter()).reloadContacts(db.getContactsFrom(owner));
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
	        	Contact contact = (Contact)((ListView)findViewById(R.id.talk_listview)).getAdapter().getItem(info.position);
	        	//Create the intent of the profile and send the ID of the contact to get the profile from
				Intent profileView = new Intent(this, ProfileViewActivity.class);
				profileView.putExtra("com.synxapps.bluewave.contactID", contact.getId());
				profileView.putExtra("com.synxapps.bluewave.ownerID", owner.getId());
				startActivity(profileView);
	            return true;
	         default:
                return super.onContextItemSelected(item);
	      }
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class SectionFragment extends Fragment {
		public static final String ARG_SECTION_NUMBER = "section_number";

		public SectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView;
			//Change the content of the tab depending on the tab number
			if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) { //Talk tab
				rootView = setupTalkFragment(inflater, container);
			} else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) { //Meet tab
				rootView = setupMeetFragment(inflater, container);
			} else {
				rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
				TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
				dummyTextView.setText("Coming soon...");
			}
			return rootView;
		}

		private View setupMeetFragment(LayoutInflater inflater, ViewGroup container) {
			//Inflate the talk fragment
			View rootView = inflater.inflate(R.layout.meet_fragment, container, false);
			//Load the contact list
			final ListView meet_list = (ListView)rootView.findViewById(R.id.meet_listview);
			final LinearLayout meet_control = (LinearLayout)rootView.findViewById(R.id.meet_controls);
			final TextView meet_status = (TextView)rootView.findViewById(R.id.meet_control_status_text);
			final MeetListAdapter meet_adapter = new MeetListAdapter(getActivity(), owner);
			meet_list.setAdapter(meet_adapter);
			
			//Create onclick listener for open the profile view on identified devices
			meet_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					//Check if the selected item is a precontact and not an undentified device
					if (meet_adapter.getItem(position) instanceof PreContact) {
						//Obtain the clicked contact from the listview adapter
						PreContact ctc = (PreContact)meet_adapter.getItem(position);
						//Create the intent of the profile and send the ID of the contact to get the profile from
						Intent profileView = new Intent(getActivity(), ProfileViewActivity.class);
						profileView.putExtra("com.synxapps.bluewave.contactID", ctc.getId());
						profileView.putExtra("com.synxapps.bluewave.ownerID", owner.getId());
						profileView.putExtra("com.synxapps.bluewave.viewtype", "meet");
						startActivity(profileView);
					}
				}
			});
			
			meet_control.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (discovery) { //Stop the discovery mode
						BluetoothManager.getInstance().stopDiscovery((MainActivity) ui);
						meet_status.setText(getResources().getString(R.string.txt_meet_discovery_disabled));
						meet_control.setBackgroundColor(getResources().getColor(R.color.bluewave_red));
						discovery = false;
					} else { //Start the discovery mode
						//Start the discovery
						BluetoothManager.getInstance().startDiscovery((MainActivity) ui, owner);
						//Change the status text and background
						meet_status.setText(getResources().getString(R.string.txt_meet_discovery_enabled));
						meet_control.setBackgroundColor(getResources().getColor(R.color.bluewave_blue));
						discovery = true;
						//Create a thread that checks for found devices and place them on the list
						new Thread(new Runnable() {
							@Override
							public void run() {
								//Clear the previous scan results//
								//Clear the adapter
								meet_adapter.clear();
								//Clear the DB
								db.clearDiscoveryData(owner);
								while (discovery) {
									//Get the state of the bluetooth
									if (!BluetoothManager.getInstance().isEnabled()) {
										meet_status.setText(getResources().getString(R.string.txt_meet_discovery_disabled));
										discovery = false;
									}
									//Get the stored devices from the DB
									ArrayList<Device> devices = db.getDevices(owner);
									//Get the stored precontacts from the DB
									ArrayList<PreContact> precontacts = db.getPreContacts(owner);
									
									for (Device dv : devices) {
										//Check only for unanalyzed devices
										if (dv.getStatus() == Device.STATE_NOT_ANALYZED) {
											//Check if the device is on bluewave's server
											PreContact pc = BackgroundNetwork.getInstance().getDeviceInfo(dv);
											if (pc != null) {
												if (pc.getAlias() == null) {
													//The device is not on the server (not an user)
													dv.setStatus(Device.STATE_NOT_USER);
													//Update the state on the db
													db.updateDeviceStatus(dv);
												} else {
													dv.setStatus(Device.STATE_ANALYZED);
													//Save the precontact on the db
													db.savePreContact(pc, owner);
													//Update the state on the db
													db.updateDeviceStatus(dv);
													//Add the precontact on the arraylist
													precontacts.add(pc);
												}
											} else {
												//Timeout
											}
										}
									}
									//Clear the previous adapter data
									meet_adapter.clear();
									//Add the devices and precontacts to the adapter
									meet_adapter.addDevices(devices);
									meet_adapter.addPreContacts(precontacts);
									//Update the UI
									meet_adapter.refreshUI();
									//Sleep for 3 seconds
									try {
										Thread.sleep(3000);
									} catch (InterruptedException e) {}
								}
							}
						}).start();
					}
				}
			});
			
			return rootView;			
		}

		private View setupTalkFragment(LayoutInflater inflater, ViewGroup container) {
			//Inflate the talk fragment
			View rootView = inflater.inflate(R.layout.talk_fragment, container, false);
			//Load the contact list
			final ListView contact_list = (ListView)rootView.findViewById(R.id.talk_listview);
			//Load the contacts from the database
			ArrayList<Contact> contacts = db.getContactsFrom(owner);
			//Try to get them from the server
			ArrayList<Contact> contactsSrv = BackgroundNetwork.getInstance().obtainContactData(owner);
			if (contactsSrv != null) {
				//If the contacts from the server and the device doesnt match
				if (contactsSrv.size() != contacts.size()) {
					//Delete the local contacts
					db.clearContacts(owner);
					//Add the contacts from the server
					db.saveContacts(contactsSrv, owner);
					contacts = db.getContactsFrom(owner);
				}
			}
			//Set the adapter of the list with the contacts
			contact_list.setAdapter(new TalkContactsAdapter(getActivity(), contacts));
			//Set the click listeners
			contact_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> av, View v,
						int pos, long arg3) {
					//Obtain the clicked contact from the listview adapter
					Contact ctc = (Contact)((TalkContactsAdapter)av.getAdapter()).getItem(pos);
					//Create a bundle and put the contact info selected on it
					Bundle contactBundle = new Bundle();
					contactBundle.putInt("com.synxapps.bluewave.CONTACT_ID", ctc.getId());
					contactBundle.putInt("com.synxapps.bluewave.OWNER_ID", owner.getId());
					//Create the intent for the chat activity and start it
					Intent chatIntent = new Intent(ui, ChatActivity.class);
					chatIntent.putExtras(contactBundle);
					ui.startActivity(chatIntent);
				}
			});
			registerForContextMenu(contact_list);
			
			return rootView;
		}
	}
	
	private void tryLogin() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String[] info = db.getUserLoginData();
				int loginResponse = BackgroundNetwork.getInstance().login(info[0], info[1]);
				if (loginResponse == owner.getId()) {
					if (!BackgroundNetwork.getInstance().isRunning()) {
						//Initialize a background thread that listen to server updates
						BackgroundNetwork.getInstance().initServerListener(owner, (Activity) ui);
					}
				} else if (loginResponse != -5) { //Login unsuccessful, notice the use, clear the login data from the DB and move to the login activity
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							new AlertDialog.Builder(ui)
					        .setIcon(R.drawable.ic_dialog_warning)
					        .setTitle(R.string.err_login_notice_title)
					        .setMessage(R.string.err_login_notice_desc)
					        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					            @Override
					            public void onClick(DialogInterface dialog, int which) {
					            	//Delete the stored login data from the DB
					            	db.clearUserLogin();
					            	//Open the login activity
					            	Intent loginActivity = new Intent(ui, LoginActivity.class);
					            	startActivity(loginActivity);
					            	//Close the main activity
					            	finish();
					            }
					        })
					        .show();
						}
					});
				} else { //Error -5, timeout
					//showToast(getResources().getString(R.string.err_timeout));
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
	
	public void updateUI() {
		ListView contactlist = (ListView)findViewById(R.id.talk_listview);
		((TalkContactsAdapter)contactlist.getAdapter()).reloadContacts(db.getContactsFrom(owner));
		//Notify the user
		showToast("Contact sync");
	}

}

package com.synxapps.bluewave;

import java.util.ArrayList;

import com.synxapps.bluewave.adapters.RequestsAdapter;
import com.synxapps.bluewave.entity.Contact;
import com.synxapps.bluewave.entity.PreContact;
import com.synxapps.bluewave.util.LocalDBHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class RequestsActivity extends Activity {
	
	private Contact owner;
	private ListView requestList;
	private ArrayList<PreContact> requests;
	private RequestsAdapter requestAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_requests);
		
		//Enable the back button on the action bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//Obtain the ID from the owner
		owner = new Contact();
		owner.setId(getIntent().getExtras().getInt("com.synxapps.bluewave.ownerID"));
		
		//Obtain the request list reference
		requestList = (ListView)findViewById(R.id.request_list);
		
		//Obtain the requests from the local DB
		LocalDBHandler db = new LocalDBHandler(this);
		requests = db.getContactRequests(owner);
		
		//Set the adapter
		requestAdapter = new RequestsAdapter(this, owner);
		requestAdapter.addPreContacts(requests);
		requestList.setAdapter(requestAdapter);
		
		//Set the click listener for launch the profileview of the selected user
		//Create onclick listener for open the profile view on identified devices
		requestList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//Check if the selected item is a precontact and not an undentified device
				if (requestAdapter.getItem(position) instanceof PreContact) {
					//Obtain the clicked contact from the listview adapter
					PreContact ctc = (PreContact)requestAdapter.getItem(position);
					//Create the intent of the profile and send the ID of the contact to get the profile from
					Intent profileView = new Intent(getApplicationContext(), ProfileViewActivity.class);
					profileView.putExtra("com.synxapps.bluewave.contactID", ctc.getId());
					profileView.putExtra("com.synxapps.bluewave.ownerID", owner.getId());
					profileView.putExtra("com.synxapps.bluewave.viewtype", "request");
					startActivity(profileView);
				}
			}
		});
		
	}
	
	@Override
	protected void onResume() {
		//Obtain the requests from the local DB
		LocalDBHandler db = new LocalDBHandler(this);
		requests = db.getContactRequests(owner);
		//Clear the adapter
		requestAdapter.clear();
		//Refresh the data on it
		requestAdapter.addPreContacts(requests);
		requestAdapter.refreshUI();
		super.onResume();
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
}

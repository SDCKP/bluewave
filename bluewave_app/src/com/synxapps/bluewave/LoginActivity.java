package com.synxapps.bluewave;

import com.synxapps.bluewave.util.BackgroundNetwork;
import com.synxapps.bluewave.util.LocalDBHandler;
import com.synxapps.bluewave.R;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class LoginActivity extends Activity {
	
	Button bt_login;
	EditText field_email, field_pass;
	TextView register_label;
	Context ui;
	LocalDBHandler db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Saving the UI's reference
		ui = this;
		
		//Initialize the DB handler
		db = new LocalDBHandler(this);
		
		setContentView(R.layout.activity_login);
		//Obtain the component's references
		bt_login = (Button)findViewById(R.id.login_button);
		field_email = (EditText)findViewById(R.id.login_field_email);
		field_pass = (EditText)findViewById(R.id.login_field_password);
		register_label = (TextView)findViewById(R.id.login_label_regdesc);
		
		//Check if the activity received a bundle to fill the fields
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.getString("com.synxapps.bluewave.EMAIL") != null && extras.getString("com.synxapps.bluewave.PASS") != null) {
				field_email.setText(getIntent().getExtras().getString("com.synxapps.bluewave.EMAIL"));
				field_pass.setText(getIntent().getExtras().getString("com.synxapps.bluewave.PASS"));
			}
		}
		//Register the actions when clicking on the components (short tap)
		bt_login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				performLogin();
			}
		});
		register_label.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Create the register intent
				Intent regIntent = new Intent(ui, RegisterActivity.class);
				//Build and display the register activity
				startActivity(regIntent);				
			}
		});
		
	}
	
	private void performLogin() {
		int response = BackgroundNetwork.getInstance().login(field_email.getText().toString(), field_pass.getText().toString());
		switch (response) {
	      case -1: //Missing parameters (never should happen)
	    	  showToast(getResources().getString(R.string.err_missing_params));
	    	  break;
	      case -2: //Wrong email/password combination
	    	  showToast(getResources().getString(R.string.err_wrong_login));
	    	  break;
	      default: //Anything else
	    	  if (response > 0) {
	    		  //Save the email, password and id (server response) on the local db for the next logins
	    		  db.saveUserInfo(field_email.getText().toString(), field_pass.getText().toString(), response);
		    	  //Start the main page intent
		    	  Intent mainActivity = new Intent(ui, MainActivity.class);
		    	  startActivity(mainActivity);
	    	  } else { //Unknown error
	    		  showToast(getResources().getString(R.string.err_missing_params));
	    	  }
	      }
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

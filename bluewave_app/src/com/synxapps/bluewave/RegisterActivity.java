package com.synxapps.bluewave;

import org.apache.http.message.BasicNameValuePair;

import com.synxapps.bluewave.util.BluetoothManager;
import com.synxapps.bluewave.util.RESTHandler;
import com.synxapps.bluewave.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class RegisterActivity extends Activity {
	Button regBt;
	EditText field_email, field_pass, field_alias;
	Context ui;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		// Show the Up button in the action bar.
		setupActionBar();
		
		//Saving the UI's reference
		ui = this;
		//Obtain the components' reference
		regBt = (Button)findViewById(R.id.register_regbt);
		field_email = (EditText)findViewById(R.id.register_field_email);
		field_pass = (EditText)findViewById(R.id.register_field_pass_1);
		field_alias = (EditText)findViewById(R.id.register_field_alias);
		
		//Register the click of the button
		regBt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				performRegister();
			}
		});
	}
	
	public void performRegister() {
		//Check that both passwords match
		String pass1 = ((EditText)findViewById(R.id.register_field_pass_1)).getText().toString();
		String pass2 = ((EditText)findViewById(R.id.register_field_pass_2)).getText().toString();
		String alias = ((EditText)findViewById(R.id.register_field_alias)).getText().toString();
		//Check that all the fields are filled
		if (!field_email.getText().toString().isEmpty() && !pass1.isEmpty() && !pass2.isEmpty()&& !alias.isEmpty()) {
			if (pass1.equalsIgnoreCase(pass2)) {
				//Passwords match, proceed with the register
				//Getting the REST handler instance
				RESTHandler handler = RESTHandler.getInstance();
				//Create the parameters for the register
				BasicNameValuePair[] params = new BasicNameValuePair[5];
				params[0] = new BasicNameValuePair("type", "register");
				params[1] = new BasicNameValuePair("email", field_email.getText().toString());
				params[2] = new BasicNameValuePair("pass", field_pass.getText().toString());
				params[3] = new BasicNameValuePair("alias", field_alias.getText().toString());
				params[4] = new BasicNameValuePair("btAddress", BluetoothManager.getInstance().getBluetoothMAC());
				//Send the REST request to the server and gather the response
				String response = handler.performRequest(params);
				switch (Integer.parseInt(response)) {
			      case -1: //Wrong petition (should never enter here)
			    	  showToast(getResources().getString(R.string.err_missing_params));
			    	  break;
			      case -2: //Invalid email (format)
			    	  showToast(getResources().getString(R.string.err_invalid_email));
			    	  break;
			      case -3: //Email in use (already registered)
			    	  showToast(getResources().getString(R.string.err_email_used));
			    	  break;
			      case -4: //Password is too simple
			    	  showToast(getResources().getString(R.string.err_pass_too_simple));
			    	  break;
			      case 1: //Registered successfully
			    	//Create the bundle with the email and pw for fill the login page
					Bundle loginbdl = new Bundle();
					loginbdl.putString("com.synxapps.bluewave.EMAIL", field_email.getText().toString());
					loginbdl.putString("com.synxapps.bluewave.PASS", field_pass.getText().toString());
					//Create the login intent to open after registration is successful
					Intent login_intent = new Intent(ui, LoginActivity.class);
					login_intent.putExtras(loginbdl);
					//Start the intent with the data filled
					startActivity(login_intent);
				}
			} else {
				//Passwords doesn't match, abort register
				showToast(getResources().getString(R.string.err_pass_unmatch));
			}
		} else {
			//The fields are not filled
			showToast(getResources().getString(R.string.err_fields_unfilled));
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

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}

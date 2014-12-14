package com.synxapps.bluewave;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.synxapps.bluewave.R;
import com.synxapps.bluewave.entity.ContactProfile;
import com.synxapps.bluewave.enums.Gender;
import com.synxapps.bluewave.enums.Interests;
import com.synxapps.bluewave.enums.LookingFor;
import com.synxapps.bluewave.enums.Nationality;
import com.synxapps.bluewave.util.BackgroundNetwork;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputFilter;

public class ProfileEditActivity extends Activity {
	private static final int IMAGE_PICK = 1, IMAGE_CROP = 2;
	private ContactProfile owner;
	private ProgressDialog loadingDialog;
	private Activity ui = this;
	TextView profile_alias;
	TextView profile_gender;
	TextView profile_age;
	TextView profile_nationality;
	TextView profile_lookingfor;
	TextView profile_about;
	TextView profile_height;
	TextView profile_weight;
	TextView profile_interests;
	ImageView profile_avatar;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_view);
		// Show the Up button in the action bar.
		setupActionBar();
		
		//Creates a loading dialog
		loadingDialog = ProgressDialog.show(ProfileEditActivity.this, "", 
                getResources().getString(R.string.txt_profile_fetching), true);
		
		//Obtain the ID from the owner
		owner = new ContactProfile();
		owner.setId(getIntent().getExtras().getInt("com.synxapps.bluewave.ownerID"));
		
		new Thread(new Runnable() {
					
			@Override
			public void run() {
				//Send the request to the server to get the profile data
				owner = BackgroundNetwork.getInstance().getUserProfile(owner, owner);
				
				//Check for timeout
				if (owner == null) {
					//Notify the timeout and close the activity
					showToast(getResources().getString(R.string.err_timeout));
					finish();
				} else {
					//Obtain the components references
					profile_alias = (TextView)findViewById(R.id.profileview_alias);
					profile_gender = (TextView)findViewById(R.id.profileview_gender_value);
					profile_age = (TextView)findViewById(R.id.profileview_age_value);
					profile_nationality = (TextView)findViewById(R.id.profileview_nationality_value);
					profile_lookingfor = (TextView)findViewById(R.id.profileview_lookingfor_value);
					profile_about = (TextView)findViewById(R.id.profileview_about_value);
					profile_height = (TextView)findViewById(R.id.profileview_height_value);
					profile_weight = (TextView)findViewById(R.id.profileview_weight_value);
					profile_interests = (TextView)findViewById(R.id.profileview_interests_list);
					profile_avatar = (ImageView)findViewById(R.id.profileview_avatar);
					
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							//Set the data on the components acording to the values from the server
							profile_alias.setText(owner.getAlias());
							profile_gender.setText(stringify(owner.getGender().toString()));
							if (owner.getAge() == -1) {
								profile_age.setText(getResources().getString(R.string.attr_NULL));
							} else {
								profile_age.setText(String.valueOf(owner.getAge()));
							}
							if (owner.getNationality() == null) { owner.setNationality(Nationality.NULL); }
							profile_nationality.setText(stringify(owner.getNationality().toString()));
							if (owner.getLookingFor() == null) { owner.setLookingFor(LookingFor.NULL); }
							profile_lookingfor.setText(stringify(owner.getLookingFor().toString()));
							if (owner.getAbout() != null && owner.getAbout() != "null") {
								profile_about.setText(owner.getAbout());
							} else {
								profile_about.setText(getResources().getString(R.string.attr_NULL));
							}
							if (owner.getHeight() == 0) { 
								profile_height.setText(stringify(Gender.NULL.toString()));
							} else {
								profile_height.setText(String.valueOf(owner.getHeight()));
							}
							if (owner.getWeight() == 0) { 
								profile_weight.setText(stringify(Gender.NULL.toString()));
							} else {
								profile_weight.setText(String.valueOf(owner.getWeight()));
							}
							String intersts = "";
							for (int i = 0; i < owner.getInterests().size(); i++) {
								intersts += stringify(owner.getInterests().get(i).toString());
								if (i < owner.getInterests().size()-1) {
									intersts += ", ";
								}
							}
							profile_interests.setText(intersts);
							if (owner.getAvatar() != null) {
								profile_avatar.setImageBitmap(owner.getAvatar());
							}
							//Hide the loading dialog
							loadingDialog.dismiss();
						}
					});
					//Register the clicks on the profile fields so they can be edited
					registerProfileComponentsClicks();
				}
			}
		}).start();
		
	}
	
	public String stringify(String attr) {
		return getResources().getString(getResources().getIdentifier("attr_" + attr, "string", ProfileEditActivity.this.getPackageName()));
	}

	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.profile_edit, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//Terminate the profile activity (brings the previous activity back)
			finish();
			return true;
		case R.id.profile_save:
			//Send the profile update request to the server and close the profile edit page
			int response = BackgroundNetwork.getInstance().updateProfile(owner);
			if (response == 1) {
				showToast(getResources().getString(R.string.txt_profile_edit_success));
				finish();
			} else {
				Log.d("ERF", ""+response);
				showToast(getResources().getString(R.string.txt_profile_edit_error));
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void registerProfileComponentsClicks() {
		profile_avatar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Create a new intent for obtain the avatar image from the local device
				Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, IMAGE_PICK);
			}
		});
		profile_alias.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText alias = new EditText(ui);
				//Set the current value on the input field
				alias.setText(owner.getAlias());
				alias.setSingleLine(true);
				//Maximum characters on the input
				int maxLength = 16; 
				InputFilter[] filter = new InputFilter[1];
				filter[0] = new InputFilter.LengthFilter(maxLength);
				alias.setFilters(filter);
				//Shows the inputdialog
				new AlertDialog.Builder(ui)
		        .setTitle(R.string.txt_profile_edit_alias_title)
		        .setMessage(R.string.txt_profile_edit_alias_desc)
				.setView(alias)
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (alias != null && alias.getText().toString().length() > 0) {
							//Update the data on both the local object and the interface
							owner.setAlias(alias.getText().toString());
							profile_alias.setText(alias.getText().toString());
						}
		            }
		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();
			}
		});
		profile_about.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText about = new EditText(ui);
				//Set the current value on the input field
				if (owner.getAbout() != "null")
				about.setText(owner.getAbout());
				//Maximum characters on the input
				int maxLength = 500; 
				InputFilter[] filter = new InputFilter[1];
				filter[0] = new InputFilter.LengthFilter(maxLength);
				about.setFilters(filter);
				//Shows the inputdialog
				new AlertDialog.Builder(ui)
		        .setTitle(R.string.txt_profile_edit_about_title)
		        .setMessage(R.string.txt_profile_edit_about_desc)
				.setView(about)
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (about != null && about.getText().toString().length() > 0) {
							//Update the data on both the local object and the interface
							owner.setAbout(about.getText().toString());
							profile_about.setText(about.getText().toString());
						}
		            }
		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();
			}
		});
		profile_height.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final NumberPicker height = new NumberPicker(ui);
				//Set the current value on the input field
				height.setMaxValue(220);
				height.setMinValue(140);
				height.setValue(owner.getHeight());
				//Shows the inputdialog
				new AlertDialog.Builder(ui)
		        .setTitle(R.string.txt_profile_edit_height_title)
		        .setMessage(R.string.txt_profile_edit_height_desc)
				.setView(height)
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (height != null) {
							//Update the data on both the local object and the interface
							owner.setHeight(height.getValue());
							if (height.getValue() == 0) {
								profile_height.setText(getResources().getString(R.string.attr_NULL));
							} else {
								profile_height.setText(String.valueOf(height.getValue()));
							}
						}
		            }
		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();
			}
		});
		profile_weight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final NumberPicker weight = new NumberPicker(ui);
				//Set the current value on the input field
				weight.setMaxValue(140);
				weight.setMinValue(45);
				weight.setValue(owner.getWeight());
				//Shows the inputdialog
				new AlertDialog.Builder(ui)
		        .setTitle(R.string.txt_profile_edit_weight_title)
		        .setMessage(R.string.txt_profile_edit_weight_desc)
				.setView(weight)
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (weight != null) {
							//Update the data on both the local object and the interface
							owner.setWeight(weight.getValue());
							if (weight.getValue() == 0) {
								profile_weight.setText(getResources().getString(R.string.attr_NULL));
							} else {
								profile_weight.setText(String.valueOf(weight.getValue()));
							}
						}
		            }
		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();
			}
		});
		profile_gender.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int curValIdx = -1;
				//Get the gender list from the enum
				String[] gds = new String[Gender.values().length];
				for (int g = 0; g < Gender.values().length; g++) {
					gds[g] = stringify(Gender.values()[g].toString());
					//Check the index of the current selected value
					if (Gender.values()[g] == owner.getGender()) {
						curValIdx = g;
					}
				}
				//Shows the selection dialog
				new AlertDialog.Builder(ui)
		        .setTitle(R.string.txt_profile_edit_gender_title)
				.setSingleChoiceItems(gds, curValIdx, null)
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Obtain the selected item on the list
						android.widget.ListView lw = ((AlertDialog)dialog).getListView();
						Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
						//Go through the gender list and check which value has been selected
						for (int g = 0; g < Gender.values().length; g++) {
							if (stringify(Gender.values()[g].toString()).equals(checkedItem.toString())) {
								//If the gender match with the listed one, set it to the owner
								owner.setGender(Gender.values()[g]);
								//Also change the value of the view
								profile_gender.setText(checkedItem.toString());
							}
						}
		            }
		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();
			}
		});
		profile_nationality.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int curValIdx = -1;
				//Get the nationality list from the enum
				String[] gds = new String[Nationality.values().length];
				for (int g = 0; g < Nationality.values().length; g++) {
					gds[g] = stringify(Nationality.values()[g].toString());
					//Check the index of the current selected value
					if (Nationality.values()[g] == owner.getNationality()) {
						curValIdx = g;
					}
				}
				//Shows the selection dialog
				new AlertDialog.Builder(ui)
		        .setTitle(R.string.txt_profile_edit_location_title)
				.setSingleChoiceItems(gds, curValIdx, null)
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Obtain the selected item on the list
						android.widget.ListView lw = ((AlertDialog)dialog).getListView();
						Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
						//Go through the nationality list and check which value has been selected
						for (int g = 0; g < Nationality.values().length; g++) {
							if (stringify(Nationality.values()[g].toString()).equals(checkedItem.toString())) {
								//If the nationality match with the listed one, set it to the owner
								owner.setNationality(Nationality.values()[g]);
								//Also change the value of the view
								profile_nationality.setText(checkedItem.toString());
							}
						}
		            }
		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();
			}
		});
		profile_lookingfor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int curValIdx = -1;
				//Get the lookingfor list from the enum
				String[] gds = new String[LookingFor.values().length];
				for (int g = 0; g < LookingFor.values().length; g++) {
					gds[g] = stringify(LookingFor.values()[g].toString());
					//Check the index of the current selected value
					if (LookingFor.values()[g] == owner.getLookingFor()) {
						curValIdx = g;
					}
				}
				//Shows the selection dialog
				new AlertDialog.Builder(ui)
		        .setTitle(R.string.txt_profile_edit_lookingfor_title)
				.setSingleChoiceItems(gds, curValIdx, null)
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Obtain the selected item on the list
						android.widget.ListView lw = ((AlertDialog)dialog).getListView();
						Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
						//Go through the lookingfor list and check which value has been selected
						for (int g = 0; g < LookingFor.values().length; g++) {
							if (stringify(LookingFor.values()[g].toString()).equals(checkedItem.toString())) {
								//If the lookingfor match with the listed one, set it to the owner
								owner.setLookingFor(LookingFor.values()[g]);
								//Also change the value of the view
								profile_lookingfor.setText(checkedItem.toString());
							}
						}
		            }
		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();
			}
		});
		profile_age.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Get the calendar for defining the date ranges
				final Calendar c = Calendar.getInstance();
				//Set the maximum and minimum age
				int maxAge = 90, minAge = 18;
				final int maxYear = c.get(Calendar.YEAR) - minAge;
				final int maxMonth = c.get(Calendar.MONTH);
				final int maxDay = c.get(Calendar.DAY_OF_MONTH);
				final int minYear = c.get(Calendar.YEAR) - maxAge;
				final int minMonth = c.get(Calendar.MONTH);
				final int minDay = c.get(Calendar.DAY_OF_MONTH);
				//Get the birth date of the user and set it to the calendar if defined
				if (owner.getBirthdate() != null) {
					c.setTime(new java.sql.Date(owner.getBirthdate().getTime()));
				} else {
					c.setTime(new java.sql.Date(System.currentTimeMillis()));
				}
				//Obtain the data of the birthdate
				int curYear = c.get(Calendar.YEAR);
				int curMonth = c.get(Calendar.MONTH);
				int curDay = c.get(Calendar.DAY_OF_MONTH);
				 
				DatePickerDialog dpd = new DatePickerDialog(ui,
				        new DatePickerDialog.OnDateSetListener() {
				            @Override
				            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				            	if (year > maxYear ||monthOfYear > maxMonth && year == maxYear||
				                        dayOfMonth > maxDay && year == maxYear && monthOfYear == maxMonth){
				            		//Too young
				            		showToast(getResources().getString(R.string.txt_profile_edit_age_invalid));
				               }
				               else if (year < minYear ||monthOfYear < minMonth && year == minYear||
				                        dayOfMonth < minDay && year == minYear && monthOfYear == minMonth) {
				            	   //Too old
				            	   showToast(getResources().getString(R.string.txt_profile_edit_age_invalid));
				               }
				               else {
				            	   //Valid date, set the calendar with the date, save it on the owner object and update the view
				            	   c.set(year, monthOfYear, dayOfMonth);
				            	   Timestamp bdate = new Timestamp(c.getTime().getTime());
				            	   owner.setBirthdate(bdate);
				            	   profile_age.setText(String.valueOf(owner.getAge()));
				               }
				            }
				        }, curYear, curMonth, curDay);
				dpd.setMessage(getResources().getString(R.string.txt_profile_edit_age_desc));
				dpd.setCancelable(false);
				dpd.show();
			}
		});
		profile_interests.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Get the interests list from the enum
				String[] gds = new String[Interests.values().length-1];
				final boolean[] curInterests = new boolean[Interests.values().length-1];
				for (int g = 0; g < Interests.values().length-1; g++) {
					gds[g] = stringify(Interests.values()[g].toString());
					//Check if the interest is on the owner list
					if (owner.hasInterest(Interests.values()[g])) {
						curInterests[g] = true;
					} else {
						curInterests[g] = false;
					}
				}
				//Shows the multiselection dialog
				new AlertDialog.Builder(ui)
		        .setTitle(R.string.txt_profile_edit_interests_title)
				.setMultiChoiceItems(gds, curInterests, new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						curInterests[which] = isChecked;
					}
				})
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Get the selected interests
						ArrayList<Interests> ints = new ArrayList<Interests>();
						for (int g = 0; g < curInterests.length; g++) {
							if (curInterests[g]) { 
								ints.add(Interests.values()[g]);
							}
						}
						//Check if there is at least an interest on the list, if not, add the null interest
						if (ints.size() == 0) {
							ints.add(Interests.NULL);
						}
						//Save the interests on the owner object
						owner.setInterests(ints);
						//Get the names of the interests and put them on the view
						String intersts = "";
						for (int i = 0; i < owner.getInterests().size(); i++) {
							intersts += stringify(owner.getInterests().get(i).toString());
							if (i < owner.getInterests().size()-1) {
								intersts += ", ";
							}
						}
						profile_interests.setText(intersts);
		            }
		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();
			}
		});
	}
	
	//Obtain the selected image and set it as the avatar
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == IMAGE_PICK) {
	            String selectedImageUri = data.getData().getPath();
	            if (selectedImageUri != null) {
	                //Call to the google camera crop activity
	                performCrop(data.getData());
	            } else {
	            	showToast(getResources().getString(R.string.txt_profile_edit_avatar_invalid));
	            }
			} else if (requestCode == IMAGE_CROP) { //Image after crop
				Bundle extras = data.getExtras();
				//Get the image bitmap from the google camera crop activity
				Bitmap av = (Bitmap) extras.getParcelable("data");
				
				//Save the bitmap on the owner object
	            owner.setAvatar(av);
	            
				//Change the view
	            profile_avatar.setImageBitmap(owner.getAvatar());
			}
        }
	}
	
	//Call the google camera app intent for crop the image
	//FROM: http://stackoverflow.com/questions/15115498/let-user-crop-image
	private void performCrop(Uri imageUri){
        try {
            Intent intent = new Intent("com.android.camera.action.CROP"); 
            intent.setType("image/*");

            List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );
            int size = list.size();

            if (size >= 0) {
                intent.setData(imageUri);        
                intent.putExtra("crop", "false");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", 1000);
                intent.putExtra("outputY", 800);
                intent.putExtra("scale", true);  
                intent.putExtra("return-data", true);

                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);
                i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                startActivityForResult(i, IMAGE_CROP);  
            } 

        }
        catch(ActivityNotFoundException anfe){
            showToast(getResources().getString(R.string.txt_profile_edit_avatar_cantcrop));
        }
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

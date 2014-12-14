package com.synxapps.bluewave;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

public class ImageViewer extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Create the imageview where the image will be displayed
		ImageView img = new ImageView(this);
		//Get the image from the bundle
		byte[] avBytes = getIntent().getExtras().getByteArray("com.synxapps.bluewave.AVATAR");
		//Put the image on the imageview
		img.setImageBitmap(BitmapFactory.decodeByteArray(avBytes, 0, avBytes.length));
		//Set the background color black
		img.setBackgroundColor(Color.BLACK);
		//Set the title of the actionbar to the alias of the user which the avatar belongs to
		getActionBar().setTitle(getIntent().getExtras().getString("com.synxapps.bluewave.ALIAS"));
		setContentView(img);
		
		//Enable the back button
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//Terminate the profile activity (brings the previous activity back)
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}

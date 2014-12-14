package com.synxapps.bluewave.adapters;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.synxapps.bluewave.entity.Contact;
import com.synxapps.bluewave.entity.Device;
import com.synxapps.bluewave.entity.PreContact;
import com.synxapps.bluewave.util.LocalDBHandler;
import com.synxapps.bluewave.R;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MeetListAdapter extends BaseAdapter {
	 
    private Activity activity;
    private ArrayList<Object> data;
    private LayoutInflater inflater=null;
    private boolean firstDevice, firstPreContact;
 
    public MeetListAdapter(Activity a, Contact owner) {
        activity = a;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        data = new ArrayList<Object>();
        firstDevice = true;
        firstPreContact = true;
    }
 
    public int getCount() {
        return data.size();
    }
 
    public Object getItem(int position) {
        return data.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.meet_contact, null);
        //Get the components references
        TextView alias = (TextView)vi.findViewById(R.id.meet_alias);
        TextView match = (TextView)vi.findViewById(R.id.meet_matchingPercent);
        TextView meet_time = (TextView)vi.findViewById(R.id.meet_time);
        ImageView avatar =(ImageView)vi.findViewById(R.id.meet_avatar);
        TextView separator = (TextView)vi.findViewById(R.id.contact_separator);
        
        if (data.get(position) instanceof PreContact) { //Its a precontact
        	PreContact pc = ((PreContact)data.get(position));
        	if (firstPreContact) {
        		separator.setText(activity.getResources().getString(R.string.txt_meet_waveful));
        		separator.setVisibility(View.VISIBLE);
        		firstPreContact = false;
        	}
        	alias.setText(pc.getAlias());
            match.setText(pc.getMatchingPercent());
            meet_time.setText(activity.getResources().getString(R.string.txt_meet_found) + " " +
            		new SimpleDateFormat("HH:mm", Locale.US).format(new Date(pc.getFound_time().getTime())));
            
            if (pc.getAvatar() != null) {
            	Bitmap av = (pc.getAvatar());
            	avatar.setImageBitmap(Bitmap.createScaledBitmap(av, 100, 100, false));
            }
        } else if (data.get(position) instanceof Device) { //Its a device
        	Device dv = ((Device)data.get(position));
        	//Set the separator and its text if neccessary
        	if (firstDevice && dv.getStatus() == Device.STATE_NOT_ANALYZED) {
        		separator.setText(activity.getResources().getString(R.string.txt_meet_unidentified));
        		separator.setVisibility(View.VISIBLE);
        		firstDevice = false;
        	} else if (firstDevice && dv.getStatus() == Device.STATE_NOT_USER) {
        		separator.setText(activity.getResources().getString(R.string.txt_meet_waveless));
        		separator.setVisibility(View.VISIBLE);
        		firstDevice = false;
        	}
        	if (dv.getName() != null && !dv.getName().isEmpty()) {
        		alias.setText(dv.getName());
        	} else {
        		alias.setText(activity.getResources().getString(R.string.err_not_available));
        	}
        	match.setText(dv.getMac_addr());
        	meet_time.setText(activity.getResources().getString(R.string.txt_meet_found) + " " +
            		new SimpleDateFormat("HH:mm", Locale.US).format(new Date(dv.getFound_time().getTime())));
        	avatar.setImageBitmap(getDeviceIconByType(dv.getType()));
        }
        return vi;
    }
    
    //Get the icon of the device depending on the device type
    private Bitmap getDeviceIconByType(int type) {
    	switch (type) {
    	case 256:
    		return BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_device_computer);
    	case 512:
    		return BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_device_phone);
    	case 1024:
    		return BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_device_audio);
    	case 1536:
    		return BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_device_tv);
    	case 768:
    		return BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_device_networking);
    	case 1792:
    		return BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_device_wearable);
		default:
			return BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_device_unk);
    	}
    }
    
    public void add(PreContact pc) {
    	//Add the precontact to the data arraylist
    	data.add(pc);
    }

    public void addPreContacts(ArrayList<PreContact> pcs) {
    	//Add the arraylist of precontacts to the data arraylist
    	for (PreContact pc : pcs) {
    		data.add(pc);
    	}
    }
    
    public void add(Device dv) {
    	//Add the device to the data arraylist
    	data.add(dv);
    }
    
    public void addDevices(ArrayList<Device> dvs) {
    	//Add the arraylist of devices to the data arraylist
    	for (Device dv : dvs) {
    		//Only show the devices that are not analyzed (which means, devices with no user or timeouts)
    		if (dv.getStatus() != Device.STATE_ANALYZED)
    		data.add(dv);
    	}
    }
    
    public void refreshUI() {
    	activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
		        //Notify the UI to update
		        notifyDataSetChanged();
			}
		});
    }
    
    public void clear() {
    	//Clear the data
    	data.clear();
    	firstDevice = true;
    	firstPreContact = true;
    }
    
    public void showToast(final String msg) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
			}
		});
	}

}

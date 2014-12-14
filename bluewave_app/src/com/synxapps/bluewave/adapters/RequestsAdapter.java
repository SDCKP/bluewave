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

public class RequestsAdapter extends BaseAdapter {
	 
    private Activity activity;
    private ArrayList<PreContact> data;
    private LayoutInflater inflater=null;
 
    public RequestsAdapter(Activity a, Contact owner) {
        activity = a;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        data = new ArrayList<PreContact>();
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
        
    	PreContact pc = ((PreContact)data.get(position));
    	alias.setText(pc.getAlias());
        match.setText(pc.getMatchingPercent());
        meet_time.setText(activity.getResources().getString(R.string.txt_meet_found) + " " +
        		new SimpleDateFormat("HH:mm", Locale.US).format(new Date(pc.getFound_time().getTime())));
        
        if (pc.getAvatar() != null) {
        	Bitmap av = (pc.getAvatar());
        	avatar.setImageBitmap(Bitmap.createScaledBitmap(av, 100, 100, false));
        }
        
        return vi;
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

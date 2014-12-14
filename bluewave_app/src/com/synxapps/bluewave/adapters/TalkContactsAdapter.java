package com.synxapps.bluewave.adapters;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.synxapps.bluewave.entity.Contact;
import com.synxapps.bluewave.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TalkContactsAdapter extends BaseAdapter {
	 
    private Activity activity;
    private ArrayList<Contact> data;
    private LayoutInflater inflater=null;
 
    public TalkContactsAdapter(Activity a, ArrayList<Contact> contacts) {
        activity = a;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //Fill the adapter
        data = contacts;
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
            vi = inflater.inflate(R.layout.talk_contact, null);
        
        TextView alias = (TextView)vi.findViewById(R.id.contact_alias);
        alias.setText(data.get(position).getAlias());
        TextView last_msg = (TextView)vi.findViewById(R.id.contact_lastmsg);
        last_msg.setText(data.get(position).getLastmsg());
        TextView time = (TextView)vi.findViewById(R.id.contact_lastmsg_time);
        if (data.get(position).getLastmsg_time() != null)
        time.setText(new SimpleDateFormat("HH:mm", Locale.US).format(new Date(data.get(position).getLastmsg_time().getTime())));
        ImageView avatar =(ImageView)vi.findViewById(R.id.contact_picture);
        if (data.get(position).getAvatar() != null) {
        	Bitmap av = data.get(position).getAvatar();
        	avatar.setImageBitmap(Bitmap.createScaledBitmap(av, 100, 100, false));
        }
 
        return vi;
    }
    
    public void reloadContacts(ArrayList<Contact> contacts) {
    	//Clear the previous data
    	data.clear();
    	//Obtain the data from the server and fill the adapter
        data = contacts;
        //Notify the UI to update
        activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
		        notifyDataSetChanged();
			}
        	
        });
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

package com.synxapps.bluewave.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.synxapps.bluewave.entity.Contact;
import com.synxapps.bluewave.entity.Message;
import com.synxapps.bluewave.R;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatMessagesAdapter extends BaseAdapter {
	private Activity activity;
    private ArrayList<Message> data;
    private Contact contact;
    private static LayoutInflater inflater=null;
    private LinearLayout wrapper;
 
    public ChatMessagesAdapter(Activity a, Contact contact) {
        activity = a;
        this.contact = contact;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        data = new ArrayList<Message>();
    }
    
    public void add(Message obj) {
    	data.add(obj);
    	//Update the UI
    	notifyDataSetChanged();
    }
    
    public void add(ArrayList<Message> msgs) {
    	for (Message m : msgs) {
    		add(m);
    	}
    	//Update the UI
    	notifyDataSetChanged();
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
            vi = inflater.inflate(R.layout.chat_message, null);
 
        //Obtain the elements references
        wrapper = (LinearLayout) vi.findViewById(R.id.chat_wrapper);
        LinearLayout aligner = (LinearLayout) vi.findViewById(R.id.chat_align);
        TextView msg_content = (TextView) vi.findViewById(R.id.chat_message);
        TextView msg_time = (TextView) vi.findViewById(R.id.chat_message_time);
        //Obtain the message based on the position
        Message m = (Message)getItem(position);
        
        //Set the text of the message
        msg_content.setText(m.getContent());
        //Set the hour of the message
        msg_time.setText(new SimpleDateFormat("HH:mm", Locale.US).format(m.getCreation_time().getTime()));
        
        //Change the color and the position of the chat bubble depending on the creator of the msg
        wrapper.setBackgroundResource((contact.getId() == m.getId_from()) ? R.drawable.bubble_left : R.drawable.bubble_right);
		aligner.setGravity((contact.getId() == m.getId_from()) ? Gravity.LEFT : Gravity.RIGHT);
 
        return vi;
    }
}

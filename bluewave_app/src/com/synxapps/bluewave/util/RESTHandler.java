package com.synxapps.bluewave.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class RESTHandler {
	
	static private RESTHandler instance;
	static private String parameters, response;
	static private boolean busy;
	static private int timeout = 5000;
	static private String host = "synx7.dyndns.org:1234/projects/bluewave";
	
	public static RESTHandler getInstance() {
		if (instance == null) {
			instance = new RESTHandler();
		}
		//Sleep until the handler is free to use
		while (busy) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
		}
		//Set the handler as busy for avoid other threads to mess with it
		busy = true;
		return instance;
	}
	
	private RESTHandler() {
		parameters = "";
		busy = false;
	}
	
	public String performRequest(BasicNameValuePair[] params) {
		parameters = "";
		try {
			for (int i = 0; i < params.length; i++) {
				parameters += params[i].getName() + "=" + URLEncoder.encode(params[i].getValue(), "UTF-8");
				if (i != params.length-1) {
					parameters += "&";
				}
			}
		} catch (UnsupportedEncodingException e) {}
		//Clear the last result
		response = null;

		//Perform the server request
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				URL url = null;
				try {
					//URL object to hold the login URL
					url = new URL("http://" + host + "/handler.php");
				} catch (MalformedURLException e1) {}
				//HTTP connection object for establish the connection to the server
			    HttpURLConnection urlConnection = null;
				try {
				  //Connects to the URL
				  urlConnection = (HttpURLConnection) url.openConnection();
				  //Use the POST method
				  urlConnection.setRequestMethod("POST");
				  //Disable cache
				  urlConnection.setUseCaches(false);
				  //Set the time until the connection timeout
				  urlConnection.setConnectTimeout(timeout);
				  //Open the output stream for the post params
				  DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
				  //Write the post params on the stream
			      wr.writeBytes (parameters);
			      //Clean and close the stream
			      wr.flush();
			      wr.close();
				} catch (IOException e1) {}
			    try {
		    	  //Read the server response
			      InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			      ByteArrayOutputStream baos = new ByteArrayOutputStream();
			      //Iterate through the stream
			      int b;
			      while ((b = in.read()) != -1) {
			    	  baos.write(b);
			      }
			      //Save the response
			      response = baos.toString();
			    } catch (Exception e) {
			    	response = "timeout";
			    } finally {
			      urlConnection.disconnect();
			    }
			}
		}).start();
		
		//Wait for the network thread to get the server response and then return it
		do {
		} while (response == null);
		
		//Free the handler
		busy = false;
		
		//Return the response of the server
		return response;
	}

}

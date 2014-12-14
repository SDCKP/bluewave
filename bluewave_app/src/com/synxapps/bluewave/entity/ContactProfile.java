package com.synxapps.bluewave.entity;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

import com.synxapps.bluewave.enums.Gender;
import com.synxapps.bluewave.enums.Interests;
import com.synxapps.bluewave.enums.LookingFor;
import com.synxapps.bluewave.enums.Nationality;

public class ContactProfile extends Contact {
	
	private Gender gender;
	private Timestamp birthdate;
	private Nationality nationality;
	private LookingFor lookingFor;
	private String about;
	private int height; //cm
	private int weight; //kg
	private ArrayList<Interests> interests;
	
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	public Timestamp getBirthdate() {
		return birthdate;
	}
	public void setBirthdate(Timestamp birthdate) {
		this.birthdate = birthdate;
	}
	public Nationality getNationality() {
		return nationality;
	}
	public void setNationality(Nationality nationality) {
		this.nationality = nationality;
	}
	public LookingFor getLookingFor() {
		return lookingFor;
	}
	public void setLookingFor(LookingFor lookingFor) {
		this.lookingFor = lookingFor;
	}
	public String getAbout() {
		return about;
	}
	public void setAbout(String about) {
		this.about = about;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public ArrayList<Interests> getInterests() {
		return interests;
	}
	public void setInterests(ArrayList<Interests> interests) {
		this.interests = interests;
	}
	
	public int getAge() {
		return calculateAge(birthdate);
	}
	
	public boolean hasInterest(Interests it) {
		for (Interests i : this.interests) {
			if (i.equals(it)) {
				return true;
			}
		}
		return false;
	}
	
	public String getInterestsArray() {
		String rval = "";
		for (int i = 0; i < interests.size(); i++) {
			rval += interests.get(i).toString();
			if (i < interests.size()-1) {
				rval += ",";
			}
		}
		return rval;
	}
	
	private int calculateAge(Timestamp bdate) {
		if (bdate == null) return -1;
		//Create a calendar with the birthday date
		Calendar dob = Calendar.getInstance();  
		dob.setTime(bdate);  
		//Create another calendar with the current date
		Calendar today = Calendar.getInstance();  
		
		//Calculate an approximate age with the year data
		int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
		
		//Check if the day of the month is the same for take a year or not from the approximated calculation
		if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
		  age--;  
		} else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
		    && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
		  age--;  
		}
		return age;
	}
}

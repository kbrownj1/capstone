package com.example.capstone;

public class GroupMember {
	
	private String name;
	private double location[];
	private boolean isEmergency;
	
	public GroupMember(String name, double lat, double lng, boolean isEmergency) {
		this.name = name;
		
		this.location = new double[2];
		this.location[0] = lat;
		this.location[1] = lng;
		this.isEmergency = isEmergency;
		
	}
	
	public void setLocation(double lat, double lng) {
		this.location[0] = lat;
		this.location[1] = lng;
	}
	
	public String getName() {
		return this.name;
	}
	
	public double getLat(){
		return this.location[0];
	}
	
	public double getLong() {
		return this.location[1];
	}
	
	public boolean isEmergency() {
		return this.isEmergency;
	}

	public void setEmergency(boolean isEmergency) {
		this.isEmergency = isEmergency;
	}
	
	@Override
	public String toString(){
		return this.name + " " + "lat:" + this.location[0] + " lon:" + this.location[1] + ", " + (this.isEmergency ? Constants.EMERGENCY : "");
	}	
	
}

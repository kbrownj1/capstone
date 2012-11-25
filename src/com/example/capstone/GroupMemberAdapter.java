package com.example.capstone;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class GroupMemberAdapter extends BaseAdapter{
	
	private String className = GroupMemberAdapter.class.getSimpleName();
	
	private Activity activity;
	private static LayoutInflater inflater = null;
	private ArrayList<GroupMember> members;
	
	public GroupMemberAdapter(Activity a, ArrayList<GroupMember> members){
		
		this.activity = a;
		this.members = members;
		Log.i("MMMC", this.className + " " + "There are " + members.size() + " members in the adapter array");
		GroupMemberAdapter.inflater = (LayoutInflater)this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
		for( GroupMember member : members){
			Log.d("MMMC", this.className + " " + member.getName() + " " + member.getLat() + " " + member.getLong());
		}
		
	}

	
	@Override
	public int getCount() {
		return this.members.size();
	}

	
	@Override
	public Object getItem(int position) {
		return position;
	}

	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public static class ViewHolder{
		public TextView name;
		public TextView longitude;
		public TextView latitude;
		public TextView emergency;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi=convertView;
	     ViewHolder holder;
	     if(convertView==null){
	         vi = inflater.inflate(R.layout.group_member, null);
	         holder = new ViewHolder();
	         holder.name      = (TextView)vi.findViewById(R.id.group_member_name);
	         holder.latitude  = (TextView)vi.findViewById(R.id.group_mamber_lat);
	         holder.longitude = (TextView)vi.findViewById(R.id.group_mamber_long);
	         holder.emergency = (TextView)vi.findViewById(R.id.text_emergency);
	         vi.setTag(holder);
	     } else {
	         holder=(ViewHolder)vi.getTag();
	     }
	        
	     Log.i("MMMC", "Adding a group member to the screen at position " + position);
	     GroupMember member = this.members.get(position);
	     Log.d("MMMC", GroupMemberAdapter.class.getSimpleName() + " " + member.toString());
	     	
	     holder.name.setText(member.getName());

	     holder.latitude.setText("Lat: " + member.getLat());
	     holder.longitude.setText("Long: " + member.getLong());
	     
	     if (member.isEmergency()) {
	    	 holder.emergency.setVisibility(View.VISIBLE);
	     } else {
	    	 holder.emergency.setVisibility(View.GONE);
	     }
	     
	     return vi;
		
		//Double latitude = new Double(members.get(position).getLat());		 
		//Double longitude = new Double(members.get(position).getLong());
		 
		//holder.longitude.setText(longitude.toString());
		//holder.latitude.setText(latitude.toString());	

	}

}

package com.example.capstone;

import static com.example.capstone.Constants.SAN_JOSE_LAT;
import static com.example.capstone.Constants.SAN_JOSE_LON;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

public class Group extends Activity{
	
	private static final String TAG = "MMMC";
	private static final String className = Chat.class.getSimpleName();
    
    private ArrayList<GroupMember> members;
    
    private ListView list;
	private GroupMemberAdapter adapter;
	
	private SQLiteDatabase database;
	
	private Context context;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.members = new ArrayList<GroupMember>();
		
		
		this.context = getApplicationContext();
		this.database = Database.getDatabase(this.context, null);
		
		// TODO: Remove this next line after test is done.
		//addMockUsers(this.database);
		
		
			
		setContentView(R.layout.group);
			
		this.list = (ListView)findViewById(R.id.groupList);
		this.adapter = new GroupMemberAdapter(this, this.members);
		this.list.setAdapter(this.adapter);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		refreshMembers();
		
		this.list = (ListView)findViewById(R.id.groupList);
		this.adapter = new GroupMemberAdapter(this, this.members);
		this.list.setAdapter(this.adapter);
		
		
	}
	
	/**
	 * Reads the database to update the member list.
	 * It will clear out the member list and rely on the current
	 * state of the database for member information.
	 */
	private void refreshMembers() {
		Cursor	resultsCursor = null;
		try {
			// Columns to query from the user_info table
			String columns[] = {"name", "lat", "long", "in_trouble"};
			
			//run the query
			resultsCursor = this.database.query("user_info", columns, null, null, null, null, null);
			
			// Parse the results and create a group member for each entry
			Log.i(TAG, className + ": number of members in the group " + resultsCursor.getCount());
			
			this.members.clear();
			
			if (resultsCursor.moveToFirst()){
				do {
					String name         = resultsCursor.getString(0);
					Double lat          = resultsCursor.getDouble(1);
					Double lng          = resultsCursor.getDouble(2);
					boolean isEmergency = resultsCursor.getInt(3) != 0;
	//				long emergencyTime  = resultsCursor.getInt(4);
					
					Log.i(TAG, className + " adding " + name + " to group at location " + lat + " " + lng + ", emergency=" + isEmergency);
					
					GroupMember member = new GroupMember(name, lat, lng, isEmergency);
					this.members.add(member);
					
				} while (resultsCursor.moveToNext());
			} else {
				Log.i(TAG, className + " there are no members present in the group");
				this.members.add(new GroupMember("No one is in your group", SAN_JOSE_LAT, SAN_JOSE_LON, false));
			}
		} finally {
			if (resultsCursor != null) {
				resultsCursor.close();
			}
		}
	
	}

	/**
	 * Add mock data to database table user_info.
	 * @param db	Reference to the database containing user_info table.
	 */
//	private static void addMockUsers(SQLiteDatabase db) {
//		
//		addRowToDb(db, "person A", 37.3041, -121.8727, null);
//		
//		addRowToDb(db, "person B", 37.3000, -121.8700, null);
//		addRowToDb(db, "person C", 37.3100, -121.8800, null);
//		
//		addRowToDb(db, "Sam Artin", 37.0850, -121.6092, Constants.EMERGENCY);
//		
//		
//	}
	
	/**
	 * Add single row to user_info table
	 * @param db		Open database
	 * @param name		Name of user
	 * @param lat		Latitude of user
	 * @param lon		Longitude of user
	 * @param snippet	Empty string unless EMERGENCY.
	 * @param snippet TODO
	 */
//	private static void addRowToDb(SQLiteDatabase db, String name, double lat, double lon, String snippet) {
//		ContentValues values = new ContentValues();
//		values.put("name", name);
//		values.put("lat", lat);
//		values.put("long", lon);
//		int inTroubleValue = Constants.EMERGENCY.equals(snippet) ? 1 : 0;
//		values.put("in_trouble", inTroubleValue);
//		
//		if(db.update("user_info", values, "name='" + name + "'", null) < 1 ) {
//			db.insert("user_info", "_id", values);
//			Log.d("CNL", "ERIK: DATABASE UPDATED, USER " + name + " " + (inTroubleValue != 0 ? Constants.EMERGENCY : "") + " ADDED");
//		}
//		else
//			Log.d("CNL", "ERIK: DATABASE UPDATED, USER " + name + " " + (inTroubleValue != 0 ? Constants.EMERGENCY : "") + " UPDATED");
//		
//	}
}
package com.example.capstone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class SplashScreen extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		Thread splashThread = new Thread(){
		
			@Override
			public void run() {
				try{
					int waited = 0;
					while (waited < 5000){
						sleep(100);
						waited += 100;
					}
				} catch (InterruptedException e){
					Log.e("MMMC", SplashScreen.class.getSimpleName() + " " + e.getMessage());
				} finally {
					finish();
					Intent i = new Intent();
					i.setClassName("com.example.capstone", "com.example.capstone.Interface");
					startActivity(i);
				}
			}
		};
		splashThread.start();
	}
}

package com.example.capstone;

import com.example.capstone.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class MessageView extends TextView {
	
	private Paint seperator;
	//private Paint linePaint;
	private int backgroundColor;
	//private float margin;

	public MessageView(Context context, AttributeSet ats, int ds) {
		super(context, ats, ds);
		init();
	}
	
	public MessageView(Context context, AttributeSet ats){
		super(context, ats);
		init();
	}
	
	public MessageView(Context context){
		super(context);
		init();
	}
	
	public void init(){
		Resources myResources = getResources();
		
		this.seperator = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		this.seperator.setColor(myResources.getColor(R.color.message_line));
		//linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//linePaint.setColor(myResources.getColor(R.color.))
		
		this.backgroundColor = myResources.getColor(R.color.message_me);
	}
	
	@Override
	public void onDraw(Canvas canvas){
		canvas.drawColor(this.backgroundColor);		
	}

}

package com.elephant.ediyou.activity;


import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.elephant.ediyou.R;
import com.elephant.ediyou.view.datetimepicker.NumericWheelAdapter;
import com.elephant.ediyou.view.datetimepicker.WheelView;

/**
 * 选择时长的dialog
 * @author syghh
 *
 */
public class MyTimePickerDialog extends Dialog implements android.view.View.OnClickListener {
	private final OnDateTimeSetListener mCallBack;
	private int  curr_hour, curr_minute;
	final WheelView  wv_hours, wv_mins;

	private Button btnDialogLeft;
	private Button btnDialogRight;
	
	public MyTimePickerDialog(Context context, OnDateTimeSetListener callBack) {
		super(context, R.style.dialog);
		int hour = 0;
		int minute = 0;
		mCallBack = callBack;
		
		// 找到dialog的布局文件
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.my_time_choice_layout, null);

		int textSize = 0;
		textSize = adjustFontSize(getWindow().getWindowManager());


		// 时
		wv_hours = (WheelView) view.findViewById(R.id.hour);
		wv_hours.setAdapter(new NumericWheelAdapter(0, 23));
		wv_hours.setCyclic(true);
		wv_hours.setLabel("小时");
		wv_hours.setCurrentItem(hour);

		// 分
		wv_mins = (WheelView) view.findViewById(R.id.mins);
		wv_mins.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
		wv_mins.setCyclic(true);
		wv_mins.setLabel("分钟");
		wv_mins.setCurrentItem(minute);		
		wv_hours.TEXT_SIZE = textSize;
		wv_mins.TEXT_SIZE = textSize;
		
		btnDialogLeft = (Button) view.findViewById(R.id.btnDialogLeft);
		btnDialogRight = (Button) view.findViewById(R.id.btnDialogRight);
		btnDialogLeft.setOnClickListener(this);
		btnDialogRight.setOnClickListener(this);

		setContentView(view);
		WindowManager windowManager = this.getWindow().getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
		lp.width = (int) (display.getWidth() - 20); // 设置宽度
		this.getWindow().setAttributes(lp);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnDialogLeft:
			
			dismiss();
			break;
		case R.id.btnDialogRight:
			curr_hour = wv_hours.getCurrentItem();
			curr_minute = wv_mins.getCurrentItem();
			if (mCallBack != null) {
				mCallBack.onDateTimeSet( curr_hour, curr_minute);
			}
			dismiss();
			break;
		default:
			break;
		}
		
	}
	
//	public void onClick(DialogInterface dialog, int which) {
//		curr_hour = wv_hours.getCurrentItem();
//		curr_minute = wv_mins.getCurrentItem();
//		if (mCallBack != null) {
//			mCallBack.onDateTimeSet( curr_hour, curr_minute);
//		}
//	}

	public void show() {
		super.show();
	}

	public interface OnDateTimeSetListener {
		void onDateTimeSet( int hour, int minute);
	}

	public static int adjustFontSize(WindowManager windowmanager) {

		int screenWidth = windowmanager.getDefaultDisplay().getWidth();
		int screenHeight = windowmanager.getDefaultDisplay().getHeight();
		/*
		 * DisplayMetrics dm = new DisplayMetrics(); dm =
		 * windowmanager.getApplicationContext
		 * ().getResources().getDisplayMetrics(); int widthPixels =
		 * dm.widthPixels; int heightPixels = dm.heightPixels; float density =
		 * dm.density; fullScreenWidth = (int)(widthPixels * density);
		 * fullScreenHeight = (int)(heightPixels * density);
		 */
		if (screenWidth <= 240) { // 240X320 屏幕
			return 10;
		} else if (screenWidth <= 320) { // 320X480 屏幕
			return 14;
		} else if (screenWidth <= 480) { // 480X800 或 480X854 屏幕
			return 24;
		} else if (screenWidth <= 540) { // 540X960 屏幕
			return 26;
		} else if (screenWidth <= 800) { // 800X1280 屏幕
			return 30;
		} else { // 大于 800X1280
			return 30;
		}
	}

}

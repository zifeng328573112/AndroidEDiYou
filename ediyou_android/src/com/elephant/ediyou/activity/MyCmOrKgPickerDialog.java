package com.elephant.ediyou.activity;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.elephant.ediyou.R;
import com.elephant.ediyou.view.datetimepicker.NumericWheelAdapter;
import com.elephant.ediyou.view.datetimepicker.WheelView;

/**
 * 选择体重或身高的dialog,身高：cm；体重：kg；
 * 
 * @author syghh
 * 
 */
public class MyCmOrKgPickerDialog extends Dialog implements android.view.View.OnClickListener {
	private final OnCmOrKgSetListener mCallBack;
	private int mType;
	private int curr_cm_or_kg;
	final WheelView wv_cm_or_kg;

	private Button btnDialogLeft;
	private Button btnDialogRight;

	/**
	 * 滚轮组件的构造函数
	 * 
	 * @param context
	 * @param type
	 *            0:cm; 1:kg
	 * @param callBack
	 */
	public MyCmOrKgPickerDialog(Context context, int type, OnCmOrKgSetListener callBack) {
		super(context, R.style.dialog);
		int cmOrKgInt = 0;
		mCallBack = callBack;
		mType = type;
		// 找到dialog的布局文件
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.my_cm_kg_choice_layout, null);
		TextView tvPickerDialogTitle = (TextView) view.findViewById(R.id.tvPickerDialogTitle);
		int textSize = 0;
		textSize = adjustFontSize(getWindow().getWindowManager());

		// 体重或身高
		wv_cm_or_kg = (WheelView) view.findViewById(R.id.cmOrKg);
		wv_cm_or_kg.setCyclic(true);
		if (type == 0) {// 身高
			tvPickerDialogTitle.setText("请选择您的身高");
			wv_cm_or_kg.setAdapter(new NumericWheelAdapter(150, 230));
			wv_cm_or_kg.setLabel("cm");
		} else {// 体重
			tvPickerDialogTitle.setText("请选择您的体重");
			wv_cm_or_kg.setAdapter(new NumericWheelAdapter(20, 150));
			wv_cm_or_kg.setLabel("kg");
		}
		wv_cm_or_kg.setCurrentItem(cmOrKgInt);

		wv_cm_or_kg.TEXT_SIZE = textSize;

		btnDialogLeft = (Button) view.findViewById(R.id.btnDialogLeft);
		btnDialogRight = (Button) view.findViewById(R.id.btnDialogRight);
		btnDialogLeft.setOnClickListener(this);
		btnDialogRight.setOnClickListener(this);

		setContentView(view);
		
		//仅一列数据时，下面的设定组件宽高就不添加了：
//		WindowManager windowManager = this.getWindow().getWindowManager();
//		Display display = windowManager.getDefaultDisplay();
//		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
//		lp.width = (int) (display.getWidth() - 150); // 设置宽度
//		this.getWindow().setAttributes(lp);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnDialogLeft:
			
			dismiss();
			break;
		case R.id.btnDialogRight:
			curr_cm_or_kg = Integer.parseInt(wv_cm_or_kg.getTextItem(wv_cm_or_kg.getCurrentItem()));
			if (mCallBack != null) {
				mCallBack.onCmOrKgSet(curr_cm_or_kg, mType);
			}
			dismiss();
			break;
		default:
			break;
		}

	}

	public void show() {
		super.show();
	}

	/**
	 * type为0:cm; type为1:kg;
	 * 
	 * @author syghh
	 * 
	 */
	public interface OnCmOrKgSetListener {
		void onCmOrKgSet(int cmOrKgCount, int type);
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

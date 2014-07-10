package com.elephant.ediyou.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.R;
import com.elephant.ediyou.bean.ProvincesBean;
import com.elephant.ediyou.db.DataBaseAdapter;
import com.elephant.ediyou.view.datetimepicker.ArrayWheelAdapter;
import com.elephant.ediyou.view.datetimepicker.OnWheelChangedListener;
import com.elephant.ediyou.view.datetimepicker.WheelView;

/**
 * 选择城市
 * 
 * @author syghh
 * 
 */
public class MyCityPickerDialog extends Dialog implements android.view.View.OnClickListener {
	private final OnCitySetListener mCallBack;
	private String curr_province, curr_city;
	final WheelView wv_provinces, wv_citys;
	private DataBaseAdapter dba;

	private Button btnDialogLeft;
	private Button btnDialogRight;

	public MyCityPickerDialog(Context context, OnCitySetListener callBack) {
		super(context,R.style.dialog);
		dba = ((CommonApplication) context.getApplicationContext()).getDbAdapter();
		mCallBack = callBack;
		// 找到dialog的布局文件
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.my_city_choice_layout, null);

		int textSize = 0;
		textSize = adjustFontSize(getWindow().getWindowManager());

		List<ProvincesBean> provincesList = new ArrayList<ProvincesBean>();
		provincesList = dba.findAllProvinces();

		List<String> provincesNameList = new ArrayList<String>();
		List<List<String>> cityNameListByPId = new ArrayList<List<String>>();
		for (int i = 0; i < provincesList.size(); i++) {
			String provincesName = provincesList.get(i).getName();
			provincesNameList.add(provincesName);

			List<String> citysNameList = new ArrayList<String>();
			citysNameList = dba.findCitysByProvinceId(i + 1);
			cityNameListByPId.add(citysNameList);
		}
//		cityNameListByPId.contains("");
		String[] provinces = (String[]) provincesNameList.toArray(new String[0]);
		final String[][] cities = new String[cityNameListByPId.size()][];
		for (int i = 0; i < cityNameListByPId.size(); i++) {
			cities[i] = (String[]) cityNameListByPId.get(i).toArray(new String[0]);
		}

		// 省
		wv_provinces = (WheelView) view.findViewById(R.id.provinces);
		wv_provinces.setAdapter(new ArrayWheelAdapter<String>(provinces));
		wv_provinces.setCurrentItem(0);

		// 城市
		wv_citys = (WheelView) view.findViewById(R.id.citys);
		wv_citys.setAdapter(new ArrayWheelAdapter<String>(cities[0]));
		wv_citys.setCurrentItem(0);
		wv_provinces.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				wv_citys.setAdapter(new ArrayWheelAdapter<String>(cities[newValue]));
				wv_citys.setCurrentItem(cities[newValue].length / 2);
			}
		});

		wv_provinces.TEXT_SIZE = textSize;
		wv_citys.TEXT_SIZE = textSize;

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
			curr_province = wv_provinces.getTextItem(wv_provinces.getCurrentItem());
			curr_city = wv_citys.getTextItem(wv_citys.getCurrentItem());
			if (mCallBack != null) {
				mCallBack.onDateTimeSet(curr_province, curr_city);
			}
			dismiss();
			break;
		default:
			break;
		}

	}

	// public void onClick(DialogInterface dialog, int which) {
	// curr_province = wv_provinces.getTextItem(wv_provinces.getCurrentItem());
	// curr_city = wv_citys.getTextItem(wv_citys.getCurrentItem());
	// if (mCallBack != null) {
	// mCallBack.onDateTimeSet(curr_province, curr_city);
	// }
	// }

	public void show() {
		super.show();
	}

	public interface OnCitySetListener {
		void onDateTimeSet(String province, String city);
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

package com.elephant.ediyou.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.R;
import com.elephant.ediyou.bean.ProvincesBean;
import com.elephant.ediyou.db.DataBaseAdapter;

public class ChoiceLocation {
	static String otherCityhas = null;
	static String provinceName = null;

	/**
	 * 
	 * @return the city name which had choised.
	 */
	public static String choiceOtherLocationDialog(final Context context, final TextView tvOtherLocation) {

		final DataBaseAdapter dba = ((CommonApplication) context.getApplicationContext()).getDbAdapter();
		final AlertDialog dialogChoiceLocation = new AlertDialog.Builder(context).create();
		dialogChoiceLocation.show();
		Window dialogWindow = dialogChoiceLocation.getWindow();
		dialogWindow.setContentView(R.layout.choice_other_location_dialog);

		ListView lvProvinces = (ListView) dialogWindow.findViewById(R.id.lvProvinces);
		final ListView lvCitys = (ListView) dialogWindow.findViewById(R.id.lvCitys);

		List<ProvincesBean> provincesList = new ArrayList<ProvincesBean>();
		provincesList = dba.findAllProvinces();
		List<String> provincesNameList = new ArrayList<String>();
		for (int i = 0; i < provincesList.size(); i++) {
			String provincesName = provincesList.get(i).getName();
			provincesNameList.add(provincesName);
		}
		ArrayAdapter<String> provincesAdapter = new ArrayAdapter<String>(context, R.layout.choice_other_location_dialog_item, provincesNameList);
		lvProvinces.setAdapter(provincesAdapter);
		lvProvinces.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent1, View view1, int position1, long id1) {
				provinceName = (String) parent1.getItemAtPosition(position1);
				List<String> citysNameList = new ArrayList<String>();
				citysNameList = dba.findCitysByProvinceId(position1 + 1);
				ArrayAdapter<String> citysAdapter = new ArrayAdapter<String>(context, R.layout.choice_other_location_dialog_item, citysNameList);
				lvCitys.setAdapter(citysAdapter);
				lvCitys.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent2, View view2, int position2, long id2) {
						String otherCityhasStr = (String) parent2.getItemAtPosition(position2);
						tvOtherLocation.setText(provinceName + " " + otherCityhasStr);
						if (provinceName.equals("北京") || provinceName.equals("天津") || provinceName.equals("上海") || provinceName.equals("重庆") || provinceName.equals("香港") || provinceName.equals("澳门")) {
							otherCityhas = provinceName;
						} else {
							otherCityhas = otherCityhasStr;
						}
						dialogChoiceLocation.dismiss();
					}
				});
			}
		});
		return otherCityhas;
	}
}

package com.elephant.ediyou.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.db.DataBaseAdapter;
import com.elephant.ediyou.util.ChoiceLocation;
import com.umeng.analytics.MobclickAgent;

public class RooSearchActivity extends Activity implements IBaseActivity, OnClickListener {
	
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle, tvOtherLocation;
	private ImageView ivSelectLocation;
	private RadioGroup rgFreeState, rgSex; 
	private Spinner spinnerAgeSection, spinnerGooGrade, spinnerServiceGrade;
	private String[] age_section, goo_grade, service_grade;
	
	private CommonApplication app;
	
	private String location;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roo_search);
		location = getIntent().getStringExtra("cityName");
		findView();
		fillData();
		app = (CommonApplication) getApplication();
		app.addActivity(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}


	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.ic_search_selector);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvOtherLocation = (TextView) this.findViewById(R.id.tvOtherLocation);
		tvOtherLocation.setText(location);
		ivSelectLocation = (ImageView) this.findViewById(R.id.ivSelectLocation);
		rgFreeState = (RadioGroup) this.findViewById(R.id.rgFreeState);
		rgSex = (RadioGroup) this.findViewById(R.id.rgSex);
		spinnerAgeSection = (Spinner) this.findViewById(R.id.spinnerAgeSection);
		spinnerGooGrade = (Spinner) this.findViewById(R.id.spinnerGooGrade);
//		spinnerServiceGrade = (Spinner) this.findViewById(R.id.spinnerServiceGrade);
		
		age_section = this.getResources().getStringArray(R.array.age_section); 
		goo_grade = this.getResources().getStringArray(R.array.goo_grade); 
		service_grade = this.getResources().getStringArray(R.array.service_grade); 
	}

	@Override
	public void fillData() {
		tvTitle.setText("搜索袋鼠");

		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		ivSelectLocation.setOnClickListener(this);
		tvOtherLocation.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnLeft: // 返回到上一层窗口
			finish();
			break;
		case R.id.btnRight: // 开始搜索
			Bundle bd = new Bundle();
			String tmpStr;
			String locationNow = tvOtherLocation.getText().toString();
			if(!TextUtils.isEmpty(locationNow)) {
				int locationId = app.getDbAdapter().findCityId(locationNow);
				bd.putInt(Constants.KEY_ROO_SEARCH_CITY, locationId);
			} else {
				bd.putInt(Constants.KEY_ROO_SEARCH_CITY, 0);
			}
			bd.putInt(Constants.KEY_ROO_SEARCH_FREE_STATE, 
					Integer.parseInt((String)((RadioButton)findViewById(rgFreeState.getCheckedRadioButtonId())).getTag()));
			tmpStr = (String)(((RadioButton)findViewById(rgSex.getCheckedRadioButtonId())).getTag());
			if(tmpStr.equals("a")) {
				bd.putString(Constants.KEY_ROO_SEARCH_SEX, null);
			} else {
				bd.putString(Constants.KEY_ROO_SEARCH_SEX, 
						(String)(((RadioButton)findViewById(rgSex.getCheckedRadioButtonId())).getTag()));
			}
			int s = spinnerAgeSection.getSelectedItemPosition();
			if(spinnerAgeSection.getSelectedItemId() == 0) {
				bd.putString(Constants.KEY_ROO_SEARCH_AGE_SECTION, null);
			} else {
				bd.putString(Constants.KEY_ROO_SEARCH_AGE_SECTION, 
						age_section[spinnerAgeSection.getSelectedItemPosition()]);
			}
			int ss = spinnerGooGrade.getSelectedItemPosition();
			if(spinnerGooGrade.getSelectedItemPosition() == 0) {
				bd.putInt(Constants.KEY_ROO_SEARCH_GOO_GRADE, 0);
			} else {
				bd.putInt(Constants.KEY_ROO_SEARCH_GOO_GRADE, 
						Integer.parseInt(goo_grade[spinnerGooGrade.getSelectedItemPosition()]));
			}
//			if(spinnerServiceGrade.getSelectedItemPosition() == 0) {
//				bd.putString(Constants.KEY_ROO_SEARCH_SERVICE_GRADE, null);
//			} else {
//				bd.putString(Constants.KEY_ROO_SEARCH_SERVICE_GRADE, 
//						service_grade[spinnerServiceGrade.getSelectedItemPosition()]);
//			}
			Intent intent = new Intent(this, RooSearchResultActivity.class);
			intent.putExtras(bd);
			startActivity(intent);
			break;
		case R.id.ivSelectLocation: // 选择城市
		case R.id.tvOtherLocation:
			new MyCityPickerDialog(this, new MyCityPickerDialog.OnCitySetListener() {
				@Override
				public void onDateTimeSet(String province, String city) {
					tvOtherLocation.setText(city);
				}
			}).show();
			break;
		}
	}
}

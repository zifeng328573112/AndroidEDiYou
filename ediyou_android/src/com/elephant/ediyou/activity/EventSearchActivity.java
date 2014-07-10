package com.elephant.ediyou.activity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.umeng.analytics.MobclickAgent;

public class EventSearchActivity extends Activity implements IBaseActivity, OnClickListener, OnCheckedChangeListener {

	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle, tvEventStartTime;
	private EditText etSearchInfo;
	private RadioGroup rgEventType, rgCostAverage1, rgCostAverage2, rgCostAverageCurr;
	private RelativeLayout rlEventStartTime;
	private int rbCostAverageLastId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_search);

		findView();
		fillData();

		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String tagString = null;
		switch (v.getId()) {
		case R.id.btnRight:
			Bundle bd = new Bundle();
			bd.putString(Constants.KEY_SEARCH_EVENT_TITLE, etSearchInfo.getText().toString());
			tagString = (String) (((RadioButton) findViewById(rgEventType.getCheckedRadioButtonId())).getTag());
			bd.putInt(Constants.KEY_SEARCH_EVENT_TYPE, Integer.parseInt(tagString));
			tagString = (String) (((RadioButton) findViewById(rgCostAverageCurr.getCheckedRadioButtonId())).getTag());
			String[] strSplit = tagString.split(",");
			bd.putInt(Constants.KEY_SEARCH_EVENT_COSTSTART, Integer.parseInt(strSplit[0]));
			bd.putInt(Constants.KEY_SEARCH_EVENT_COSTEND, Integer.parseInt(strSplit[1]));
			if (tvEventStartTime.getText().toString().equals("不限")) {
				bd.putString(Constants.KEY_SEARCH_EVENT_STARTTIME, "");
			} else {
				bd.putString(Constants.KEY_SEARCH_EVENT_STARTTIME, tvEventStartTime.getText().toString());
			}
			Intent intent = new Intent(this, EventSearchResultActivity.class);
			intent.putExtras(bd);
			startActivity(intent);
			break;
		case R.id.btnLeft:
			finish();
			break;
		case R.id.rlEventStartTime:
			MyDatePickerDialog myDatePickerDialog = new MyDatePickerDialog(EventSearchActivity.this, new MyDatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateTimeSet(int year, int monthOfYear, int dayOfMonth) {
					String monthOfYearStr = String.valueOf(monthOfYear);
					String dayOfMonthStr = String.valueOf(dayOfMonth);
					if (monthOfYearStr.length() == 1) {
						monthOfYearStr = "0" + monthOfYearStr;
					}
					if (dayOfMonthStr.length() == 1) {
						dayOfMonthStr = "0" + dayOfMonthStr;
					}
					String eventStr = year + "-" + monthOfYearStr + "-" + dayOfMonthStr;
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					Date eventDate = null;
					try {
						eventDate = df.parse(eventStr);

					} catch (ParseException e) {
						e.printStackTrace();
					}
					long eventStartL = eventDate.getTime();
					Calendar mCalendar = Calendar.getInstance();
					long currentTimeL = mCalendar.getTimeInMillis();
					if (eventStartL < currentTimeL) {
						Toast.makeText(EventSearchActivity.this, "开始时间不能为过去哦！", Toast.LENGTH_LONG).show();
					} else {
						tvEventStartTime.setText(eventStr);
					}
				}
			});
			myDatePickerDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					tvEventStartTime.setText("不限");
				}
			});
			myDatePickerDialog.show();
			break;
		}
	}

//	@Override
//	@Deprecated
//	protected Dialog onCreateDialog(int id) {
//		Calendar c = Calendar.getInstance();
//		DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
//			public void onDateSet(DatePicker dp, int year, int month, int dayOfMonth) {
//				tvEventStartTime.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
//			}
//		}, c.get(Calendar.YEAR), // 传入年份
//				c.get(Calendar.MONTH), // 传入月份
//				c.get(Calendar.DAY_OF_MONTH) // 传入天数
//		);
//		datePickerDialog.setButton2("取消", new DialogInterface.OnClickListener() {	
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				tvEventStartTime.setText("不限");
//				
//			}
//		});
//		return datePickerDialog;
//	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		if (0 != rbCostAverageLastId) {
			RadioButton rb = (RadioButton) findViewById(rbCostAverageLastId);
			rb.setChecked(false);
		}
		rbCostAverageLastId = checkedId;
		rgCostAverageCurr = group;
	}

	@Override
	public void findView() {
		// TODO Auto-generated method stub
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.ic_search_selector);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		etSearchInfo = (EditText) this.findViewById(R.id.etSearchInfo);
		tvEventStartTime = (TextView) this.findViewById(R.id.tvEventStartTime);
		rlEventStartTime = (RelativeLayout) this.findViewById(R.id.rlEventStartTime);
		rgEventType = (RadioGroup) this.findViewById(R.id.rgEventType);
		rgCostAverage1 = (RadioGroup) this.findViewById(R.id.rgCostAverage1);
		rgCostAverage2 = (RadioGroup) this.findViewById(R.id.rgCostAverage2);
		rgCostAverageCurr = rgCostAverage1;
		rbCostAverageLastId = R.id.rbCostAverage0to100;
	}

	@Override
	public void fillData() {
		// TODO Auto-generated method stub
		tvTitle.setText("搜索活动");
		tvTitle.setVisibility(View.VISIBLE);

		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		rlEventStartTime.setOnClickListener(this);
		rgCostAverage1.setOnCheckedChangeListener(this);
		rgCostAverage2.setOnCheckedChangeListener(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
	}

}

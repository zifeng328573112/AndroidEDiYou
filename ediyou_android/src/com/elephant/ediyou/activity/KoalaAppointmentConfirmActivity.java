package com.elephant.ediyou.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.AppointmentBean;
import com.elephant.ediyou.util.DateUtil;
import com.elephant.ediyou.util.SharedPrefUtil;

/**
 * 考拉预约确认页面
 * 
 * @author syghh
 * 
 */
public class KoalaAppointmentConfirmActivity extends Activity implements IBaseActivity, OnClickListener {

	// 标题；
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;

	private TextView tvTime;
	private View viewLocation;
	private TextView tvLocation;
	private EditText etService;
	private EditText etLocation;

	private long rooId;
	private ArrayList<String> hireList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.koala_appointment_confirm);
		findView();
		fillData();
		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) findViewById(R.id.btnLeft);
		btnRight = (Button) findViewById(R.id.btnRight);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

		tvTime = (TextView) findViewById(R.id.tvTime);
		viewLocation = findViewById(R.id.viewLocation);
		tvLocation = (TextView) findViewById(R.id.tvLocation);
		etLocation = (EditText) findViewById(R.id.etLocation);
		etService = (EditText) findViewById(R.id.etService);

		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		tvTime.setOnClickListener(this);
		viewLocation.setOnClickListener(this);
	}

	@Override
	public void fillData() {
		if (getIntent() != null) {
			hireList = (ArrayList<String>) getIntent().getSerializableExtra("hireTime");
			if (hireList.size() > 1) {
				tvTime.setText("从" + getStartDate(hireList) + "开始，共" + hireList.size() + "天");
			} else {
				tvTime.setText(hireList.get(0));
			}
			rooId = getIntent().getLongExtra("rooId", 0);
		}
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		tvTitle.setText(R.string.roo_appointment_confirm);
		btnRight.setBackgroundResource(R.drawable.ic_submit_selector);

	}

	private String getStartDate(List<String> dateList){
		Date startDate = DateUtil.stringToDate("yyyy-MM-dd", dateList.get(0));
		for (int i = 1; i < dateList.size(); i++) {
			Date date = DateUtil.stringToDate("yyyy-MM-dd", dateList.get(i));
			if(date.before(startDate)){
				startDate = date;
			}
		}
		return DateUtil.dateToString("yyyy-MM-dd", startDate);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnRight:
			String locationStr = etLocation.getText().toString().trim();
			String serviceStr = etService.getText().toString().trim();
			if (TextUtils.isEmpty(locationStr)) {
				Toast.makeText(this, "请填写预约地点", Toast.LENGTH_SHORT).show();
				return;
			}
			if (TextUtils.isEmpty(serviceStr)) {
				Toast.makeText(this, "请填写服务需求", Toast.LENGTH_SHORT).show();
				return;
			}
			AppointmentBean bean = new AppointmentBean();
			bean.setRooID(rooId);
			bean.setAddress(locationStr);
			bean.setServicerequire(serviceStr);
			bean.setUserID((long) SharedPrefUtil.getUserBean(this).getUserId());
			StringBuffer sb = new StringBuffer();
			int hireListSize = hireList.size();
			for (int i = 0; i < hireListSize; i++) {
				String dateStr = hireList.get(i);
				sb.append(dateStr);
				if (i != hireListSize - 1) {
					sb.append(",");
				}
			}
			bean.setOrderTime(sb.toString());

			// try {
			// JSONObject obj = new
			// BusinessHelper().profileCreateOrder(SharedPrefUtil.getUserBean(this).getUserId(),
			// 1, serviceStr, locationStr,
			// sb.toString(),SharedPrefUtil.getUserBean(this).getAccessToken());
			// System.out.println(obj.toString());
			// } catch (SystemException e) {
			// // TODO Auto-generated catch block
			// Log.i("RooAppointmentConfirmActivity", e.getMessage());
			// }

			Intent intent = new Intent(this, KoalaOrderDetailActivity.class);
			intent.putExtra("orderBean", bean);
			startActivity(intent);
			break;
		case R.id.tvTime:
			Intent timeIntent = new Intent(this, RooScheduleActivity.class);
			timeIntent.putExtra(Constants.ORDER_ROO, true);
			startActivity(timeIntent);
			break;
		case R.id.viewLocation:
			startActivityForResult(new Intent(this, KoalaAppointmentLocationActivity.class), Constants.REQUEST_LOCATION);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_LOCATION) {

		}
	}
}

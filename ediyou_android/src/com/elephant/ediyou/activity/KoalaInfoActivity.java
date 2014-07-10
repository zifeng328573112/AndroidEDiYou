package com.elephant.ediyou.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.ChoiceLocation;
import com.elephant.ediyou.util.DateUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

public class KoalaInfoActivity extends Activity implements IBaseActivity, OnClickListener {

	// 个人介绍
	private TextView 			tvPersonalIntroduce;
	private EditText 			etPersonalIntroduce;
	private Button 				btnPersonalIntroduceEdit;
	// 基本信息；
	private TextView 			tvHeight, tvWeight, tvArea, etArea, tvHobby, tvPhone;
	private EditText 			etHeight, etWeight, etHobby, etPhone;
	private Button 				btnBasicInfoEdit;
	// 详细信息；
	private TextView 			tvNationality, tvRace, tvBirthday, etBirthday, tvConstellation, tvZodiac, tvFaith;
	private EditText 			etNationality, etRace, etConstellation, etZodiac, etFaith;
	private Button 				btnDetailInfoEdit;

	private boolean 			isSelf = false;// 是否是查看自己页面；
	private boolean 			isEditPersonalIntroduce = false;
	private long 				koalaId;
	private boolean 			isEditBasicInfo = false;
	private boolean 			isEditDetailInfo = false;
	private InputMethodManager 	inputManager;
	private Calendar 			cal = Calendar.getInstance();
	private CommonApplication 	app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.koala_info);
		app 			= (CommonApplication) getApplication();
		app.addActivity(this);
		inputManager 	= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		koalaId 		= getIntent().getLongExtra("id", 0);
		if (koalaId == SharedPrefUtil.getUserBean(this).getUserId()) {
			isSelf = true;
		}
		findView();
		fillData();
	}

	@Override
	public void findView() {
		btnPersonalIntroduceEdit 	= (Button) findViewById(R.id.btnPersonalIntroduceEdit);
		tvPersonalIntroduce 		= (TextView) findViewById(R.id.tvPersonalIntroduce);
		etPersonalIntroduce 		= (EditText) findViewById(R.id.etPersonalIntroduce);
		// 基本信息；
		btnBasicInfoEdit 			= (Button) findViewById(R.id.btnBasicInfoEdit);
		tvHeight 					= (TextView) findViewById(R.id.tvHeight);
		etHeight 					= (EditText) findViewById(R.id.etHeight);
		tvWeight 					= (TextView) findViewById(R.id.tvWeight);
		etWeight 					= (EditText) findViewById(R.id.etWeight);
		tvArea 						= (TextView) findViewById(R.id.tvArea);
		etArea 						= (TextView) findViewById(R.id.etArea);
		tvHobby 					= (TextView) findViewById(R.id.tvHobby);
		etHobby 					= (EditText) findViewById(R.id.etHobby);
		tvPhone 					= (TextView) findViewById(R.id.tvPhone);
		etPhone 					= (EditText) findViewById(R.id.etPhone);
		// 详细信息；
		btnDetailInfoEdit 			= (Button) findViewById(R.id.btnDetailInfoEdit);
		tvNationality 				= (TextView) findViewById(R.id.tvNationality);
		etNationality 				= (EditText) findViewById(R.id.etNationality);
		tvRace 						= (TextView) findViewById(R.id.tvRace);
		etRace 						= (EditText) findViewById(R.id.etRace);
		tvBirthday 					= (TextView) findViewById(R.id.tvBirthday);
		etBirthday 					= (TextView) findViewById(R.id.etBirthday);
		tvConstellation 			= (TextView) findViewById(R.id.tvConstellation);
		etConstellation 			= (EditText) findViewById(R.id.etConstellation);
		tvZodiac 					= (TextView) findViewById(R.id.tvZodiac);
		etZodiac 					= (EditText) findViewById(R.id.etZodiac);
		tvFaith 					= (TextView) findViewById(R.id.tvFaith);
		etFaith 					= (EditText) findViewById(R.id.etFaith);

		btnPersonalIntroduceEdit.setOnClickListener(this);
		btnBasicInfoEdit.setOnClickListener(this);
		btnDetailInfoEdit.setOnClickListener(this);
	}

	@Override
	public void fillData() {
		if (!isSelf) {
			btnPersonalIntroduceEdit.setVisibility(View.GONE);
			btnBasicInfoEdit.setVisibility(View.GONE);
			btnDetailInfoEdit.setVisibility(View.GONE);
		}
		if (NetUtil.checkNet(this)) {
			new GetKoalaInfoTask(koalaId, this).execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.btnPersonalIntroduceEdit:
			if (!isEditPersonalIntroduce) {
				tvPersonalIntroduce.setVisibility(View.GONE);
				etPersonalIntroduce.setVisibility(View.VISIBLE);
				etPersonalIntroduce.setText(tvPersonalIntroduce.getText().toString().replace("<BR>", "\n").replace("<br>", "\n"));
				btnPersonalIntroduceEdit.setBackgroundResource(R.drawable.btn_edit_submit_selector);
				isEditPersonalIntroduce = true;

			} else {
				tvPersonalIntroduce.setVisibility(View.VISIBLE);
				etPersonalIntroduce.setVisibility(View.GONE);
				tvPersonalIntroduce.setText(etPersonalIntroduce.getText().toString().replace("<BR>", "\n").replace("<br>", "\n"));
				btnPersonalIntroduceEdit.setBackgroundResource(R.drawable.btn_edit_selector);
				isEditPersonalIntroduce = false;
				inputManager.hideSoftInputFromWindow(btnPersonalIntroduceEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);// 隐藏键盘
				if (NetUtil.checkNet(this)) {
					new UpdatePersonalInfoTask("personalIntroduce").execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.btnBasicInfoEdit:
			if (!isEditBasicInfo) {
				tvHeight.setVisibility(View.GONE);
				tvWeight.setVisibility(View.GONE);
				tvArea.setVisibility(View.GONE);
				tvHobby.setVisibility(View.GONE);
				tvPhone.setVisibility(View.GONE);

				etHeight.setVisibility(View.VISIBLE);
				etWeight.setVisibility(View.VISIBLE);
				etArea.setVisibility(View.VISIBLE);
				etHobby.setVisibility(View.VISIBLE);
				etPhone.setVisibility(View.VISIBLE);
				etArea.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						ChoiceLocation.choiceOtherLocationDialog(KoalaInfoActivity.this, etArea);
					}
				});

				etHeight.setText(tvHeight.getText().toString().replace("cm", ""));
				etWeight.setText(tvWeight.getText().toString().replace("kg", ""));
				etArea.setText(tvArea.getText().toString());
				etHobby.setText(tvHobby.getText().toString());
				etPhone.setText(tvPhone.getText().toString());
				btnBasicInfoEdit.setBackgroundResource(R.drawable.btn_edit_submit_selector);
				isEditBasicInfo = true;
			} else {
				tvHeight.setVisibility(View.VISIBLE);
				tvWeight.setVisibility(View.VISIBLE);
				tvArea.setVisibility(View.VISIBLE);
				tvHobby.setVisibility(View.VISIBLE);
				tvPhone.setVisibility(View.VISIBLE);

				etHeight.setVisibility(View.GONE);
				etWeight.setVisibility(View.GONE);
				etArea.setVisibility(View.GONE);
				etHobby.setVisibility(View.GONE);
				etPhone.setVisibility(View.GONE);
				if (etHeight.getText().toString().trim().equals("")) {
					tvHeight.setText("");
				} else {
					tvHeight.setText(etHeight.getText().toString() + "cm");
				}
				if (etWeight.getText().toString().trim().equals("")) {
					tvWeight.setText("");
				} else {
					tvWeight.setText(etWeight.getText().toString() + "kg");
				}
				tvArea.setText(etArea.getText().toString());
				tvHobby.setText(etHobby.getText().toString());
				tvPhone.setText(etPhone.getText().toString());
				btnBasicInfoEdit.setBackgroundResource(R.drawable.btn_edit_selector);
				isEditBasicInfo = false;
				inputManager.hideSoftInputFromWindow(btnBasicInfoEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);// 隐藏键盘
				if (NetUtil.checkNet(this)) {
					new UpdatePersonalInfoTask("basicInfo").execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.btnDetailInfoEdit:
			if (!isEditDetailInfo) {
				tvNationality.setVisibility(View.GONE);
				tvRace.setVisibility(View.GONE);
				tvBirthday.setVisibility(View.GONE);
				tvConstellation.setVisibility(View.GONE);
				tvZodiac.setVisibility(View.GONE);
				tvFaith.setVisibility(View.GONE);

				etNationality.setVisibility(View.VISIBLE);
				etRace.setVisibility(View.VISIBLE);
				etConstellation.setVisibility(View.VISIBLE);
				etZodiac.setVisibility(View.VISIBLE);
				etFaith.setVisibility(View.VISIBLE);
				etBirthday.setVisibility(View.VISIBLE);
				etBirthday.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						inputManager.hideSoftInputFromWindow(etBirthday.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);// 隐藏键盘
						showDateDialog();
					}
				});

				etNationality.setText(tvNationality.getText().toString());
				etBirthday.setText(tvBirthday.getText().toString());
				etRace.setText(tvRace.getText().toString());
				etConstellation.setText(tvConstellation.getText().toString());
				etZodiac.setText(tvZodiac.getText().toString());
				etFaith.setText(tvFaith.getText().toString());

				btnDetailInfoEdit.setBackgroundResource(R.drawable.btn_edit_submit_selector);
				isEditDetailInfo = true;
			} else {
				tvNationality.setVisibility(View.VISIBLE);
				tvRace.setVisibility(View.VISIBLE);
				tvBirthday.setVisibility(View.VISIBLE);
				tvConstellation.setVisibility(View.VISIBLE);
				tvZodiac.setVisibility(View.VISIBLE);
				tvFaith.setVisibility(View.VISIBLE);

				etNationality.setVisibility(View.GONE);
				etBirthday.setVisibility(View.GONE);
				etRace.setVisibility(View.GONE);
				etConstellation.setVisibility(View.GONE);
				etZodiac.setVisibility(View.GONE);
				etFaith.setVisibility(View.GONE);

				tvNationality.setText(etNationality.getText().toString());
				tvBirthday.setText(etBirthday.getText().toString());
				tvRace.setText(etRace.getText().toString());
				tvConstellation.setText(etConstellation.getText().toString());
				tvZodiac.setText(etZodiac.getText().toString());
				tvFaith.setText(etFaith.getText().toString());
				btnDetailInfoEdit.setBackgroundResource(R.drawable.btn_edit_selector);
				isEditDetailInfo = false;
				inputManager.hideSoftInputFromWindow(btnDetailInfoEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);// 隐藏键盘
				if (NetUtil.checkNet(this)) {
					new UpdatePersonalInfoTask("detailInfo").execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 显示日期选项dialog；
	 */
	private void showDateDialog() {
		String dateStr = etBirthday.getText().toString();
		if (dateStr.equals("")) {
			new DatePickerDialog(this, listener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
		} else {
			Date date = DateUtil.stringToDate("yyyy-MM-dd", dateStr);
			cal.setTime(date);
			new DatePickerDialog(this, listener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
		}
	}

	private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, monthOfYear);
			cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDate();
		}
	};

	private void updateDate() {
		Calendar nowCalendar = Calendar.getInstance();
		if (cal.getTimeInMillis() < nowCalendar.getTimeInMillis()) {
			etBirthday.setText(DateUtil.dateToString("yyyy-MM-dd", cal.getTime()));
		} else {
			Toast.makeText(KoalaInfoActivity.this, "出生日期不能超过当前日期哦", Toast.LENGTH_SHORT).show();
		}
	}

	private class GetKoalaInfoTask extends AsyncTask<Void, Void, JSONObject> {
		private long id;
		private Context context;

		public GetKoalaInfoTask(long id, Context context) {
			super();
			this.id = id;
			this.context = context;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject koalaInfo = null;
			try {
				koalaInfo = new BusinessHelper().getKoalaInfo(id);
			} catch (Exception e) {
				MobclickAgent.reportError(context, StringUtil.getExceptionInfo(e));
			}
			return koalaInfo;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					if (Constants.SUCCESS == result.getInt("status")) {
						JSONObject dataJson = result.getJSONObject("data");
						tvBirthday.setText(dataJson.getString("birthday"));
						tvPersonalIntroduce.setText(dataJson.getString("intro").replace("<BR>", "\n").replace("<br>", "\n"));
						if (dataJson.getString("height").trim().equals("") || dataJson.getInt("height") == 0) {
							tvHeight.setText("");
						} else {
							tvHeight.setText(dataJson.getString("height") + "cm");
						}
						if (dataJson.getString("weight").trim().equals("") || dataJson.getInt("weight") == 0) {
							tvWeight.setText("");
						} else {
							tvWeight.setText(dataJson.getString("weight") + "kg");
						}
						tvArea.setText(dataJson.getString("pName") + " " + dataJson.getString("cityName"));
						tvHobby.setText(dataJson.getString("hobby"));
						tvPhone.setText(dataJson.getString("telephone"));
						tvNationality.setText(dataJson.getString("nationality"));
						tvRace.setText(dataJson.getString("nation"));
						tvBirthday.setText(dataJson.getString("birthday"));
						tvConstellation.setText(dataJson.getString("constellation"));
						tvZodiac.setText(dataJson.getString("animalyear"));
						tvFaith.setText(dataJson.getString("faith"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(context, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 通过type判断更新的信息；personalIntroduce为个人介绍，basicInfo为基本信息， detailInfo为详细信息
	 * 
	 * @author Zhou
	 * 
	 */
	private class UpdatePersonalInfoTask extends AsyncTask<Void, Void, JSONObject> {
		private String type;

		public UpdatePersonalInfoTask(String type) {
			super();
			this.type = type;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject update = null;
			long userId = SharedPrefUtil.getUserBean(KoalaInfoActivity.this).getUserId();
			String accessToken = SharedPrefUtil.getUserBean(KoalaInfoActivity.this).getAccessToken();
			try {
				if (type.equals("personalIntroduce")) {
					update = new BusinessHelper().updateIntro(userId, tvPersonalIntroduce.getText().toString().replace("<BR>", "\n").replace("<br>", "\n"), accessToken);
				} else if (type.equals("basicInfo")) {
					int height = 0;
					try {
						height = Integer.parseInt(tvHeight.getText().toString().replace("cm", ""));
					} catch (Exception e) {
					}
					int weight = 0;
					try {
						weight = Integer.parseInt(tvWeight.getText().toString().replace("kg", ""));
					} catch (Exception e) {
					}
					String hobby 		= tvHobby.getText().toString();
					String telephone 	= tvPhone.getText().toString();
					String area 		= tvArea.getText().toString().trim();
					String provinceStr 	= null;
					String cityStr 		= null;
					if (!area.equals("")) {
						StringTokenizer token 	= new StringTokenizer(area, " ");
						provinceStr 			= token.nextToken();
						cityStr 				= token.nextToken();
					}
					int[] ids = new int[2];
					if (provinceStr != null && cityStr != null) {
						ids = app.getDbAdapter().findProvinceCityId(provinceStr, cityStr);
					}
					update = new BusinessHelper().updateBasicInfo(koalaId, height, weight, hobby, telephone, ids[1], ids[0], accessToken);
				} else if (type.equals("detailInfo")) {
					String nationality 		= tvNationality.getText().toString();
					String birthday 		= tvBirthday.getText().toString();
					String constellation 	= tvConstellation.getText().toString();
					String nation 			= tvRace.getText().toString();
					String animalyear 		= tvZodiac.getText().toString();
					String faith 			= tvFaith.getText().toString();
					update 					= new BusinessHelper().updateDetailInfo(userId, nationality, birthday, constellation, nation, animalyear, faith, accessToken);
				}
			} catch (Exception e) {
			}
			return update;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					if (result.getInt("status") == Constants.SUCCESS) {
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(KoalaInfoActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(KoalaInfoActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(KoalaInfoActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
				}
			} else {
				Toast.makeText(KoalaInfoActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
			}
		}
	}
}

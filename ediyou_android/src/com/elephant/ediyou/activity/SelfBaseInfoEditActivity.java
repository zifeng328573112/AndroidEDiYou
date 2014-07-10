package com.elephant.ediyou.activity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.db.DataBaseAdapter;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.DateUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 袋鼠(考拉)个人信息编辑(本人才能编辑)
 * 
 * @author SongYuan
 * 
 */
public class SelfBaseInfoEditActivity extends Activity implements IBaseActivity, OnClickListener {

	// title
	private Button btnLeft, btnRight;
	private TextView tvTitle;

	private TextView tvLevel;
	private ProgressBar pbExperience;
	private EditText etNickName;
	private TextView etArea;
	private TextView tvBirthday;
	private View viewPhone;
	private TextView tvHobby, tvHeight, tvWeight;
	private ImageView ivHobbyArrow;//爱好一栏的箭头；
	private EditText etPhone;
	private EditText etNationality, etRace, etConstellation, etZodiac, etFaith;

	private ProgressDialog pd;
	private InputMethodManager inputManager;
	private CommonApplication app;

	private boolean isEdit = false;
	private long userId = 0;

	// 当点击返回时，判断是否有修改：
	// 原有数据：
	private int heightIntOld;
	private int weightIntOld;
	private String hobbyStrOldId;
	private String hobbyStrOld;
	private String telephoneStrOld;
	private String provinceStrOld;
	private String cityStrOld;
	private String areaStrOld;
	private String nationalityStrOld;
	private String birthdayStrOld;
	private String constellationStrOld;
	private String nationStrOld;
	private String animalyearStrOld;
	private String faithStrOld;
	private String nicknameStrOld;

	// 在点击返回键时的数据：
	private int heightIntNew;
	private int weightIntNew;
	private String hobbyStrNew;
	private String telephoneStrNew;
	private String areaStrNew;
	private String nationalityStrNew;
	private String birthdayStrNew;
	private String constellationStrNew;
	private String nationStrNew;
	private String animalyearStrNew;
	private String faithStrNew;
	private String nicknameStrNew;

	boolean hasChange;// 是否有改变

	private String[] hobbyArr = new String[2];

	private DataBaseAdapter dba;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.self_info_edit);
		dba = ((CommonApplication) getApplicationContext()).getDbAdapter();
		userId = getIntent().getLongExtra("userId", 0);
		isEdit = getIntent().getBooleanExtra("isEdit", false);
		findView();
		fillData();
		app = (CommonApplication) getApplication();
		app.addActivity(this);
		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.bg_btn_selector);
		btnRight.setText("保存");
		btnRight.setGravity(Gravity.CENTER);
		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

		etNickName = (EditText) findViewById(R.id.etNickName);
		tvLevel = (TextView) findViewById(R.id.tvLevel);
		pbExperience = (ProgressBar) findViewById(R.id.pbExperience);

		tvHeight = (TextView) findViewById(R.id.tvHeight);
		tvWeight = (TextView) findViewById(R.id.tvWeight);
		etArea = (TextView) findViewById(R.id.etArea);

		tvHeight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new MyCmOrKgPickerDialog(SelfBaseInfoEditActivity.this, 0, new MyCmOrKgPickerDialog.OnCmOrKgSetListener() {
					@Override
					public void onCmOrKgSet(int cmOrKgCount, int type) {
						tvHeight.setText(cmOrKgCount + "cm");
					}
				}).show();
			}
		});

		tvWeight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new MyCmOrKgPickerDialog(SelfBaseInfoEditActivity.this, 1, new MyCmOrKgPickerDialog.OnCmOrKgSetListener() {
					@Override
					public void onCmOrKgSet(int cmOrKgCount, int type) {
						tvWeight.setText(cmOrKgCount + "kg");
					}
				}).show();
			}
		});
		
		etArea.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new MyCityPickerDialog(SelfBaseInfoEditActivity.this, new MyCityPickerDialog.OnCitySetListener() {
					@Override
					public void onDateTimeSet(String province, String city) {
						etArea.setText(province + " " + city);
					}
				}).show();
			}
		});
		tvHobby = (TextView) findViewById(R.id.tvHobby);
		tvHobby.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SelfBaseInfoEditActivity.this, ChoiseHobbyActivity.class));
			}
		});
		ivHobbyArrow = (ImageView) findViewById(R.id.ivHobbyArrow);
		viewPhone = findViewById(R.id.viewPhone);
		etPhone = (EditText) findViewById(R.id.etPhone);
		tvBirthday = (TextView) findViewById(R.id.tvBirthday);
		tvBirthday.setOnClickListener(this);
		// 详细信息；
		etNationality = (EditText) findViewById(R.id.etNationality);
		etRace = (EditText) findViewById(R.id.etRace);
		etConstellation = (EditText) findViewById(R.id.etConstellation);
		etZodiac = (EditText) findViewById(R.id.etZodiac);
		etFaith = (EditText) findViewById(R.id.etFaith);

		// 输入限制：
		StringUtil.limitEditTextLength(etNationality, 32, this);
		StringUtil.limitEditTextLength(etRace, 32, this);
		StringUtil.limitEditTextLength(etFaith, 32, this);
	}

	@Override
	public void fillData() {
		tvTitle.setText("个人信息");
		if (!isEdit) {
			btnRight.setVisibility(View.INVISIBLE);
			etNickName.setBackgroundDrawable(null);
			etNickName.setFocusable(false);
			viewPhone.setVisibility(View.GONE);
			tvHeight.setBackgroundDrawable(null);
			tvHeight.setClickable(false);
			tvWeight.setBackgroundDrawable(null);
			tvWeight.setClickable(false);
			etArea.setBackgroundDrawable(null);
			etArea.setClickable(false);
			tvHobby.setBackgroundDrawable(null);
			tvHobby.setClickable(false);
			etNationality.setBackgroundDrawable(null);
			etNationality.setFocusable(false);
			etRace.setBackgroundDrawable(null);
			etRace.setFocusable(false);
			tvBirthday.setBackgroundDrawable(null);
			tvBirthday.setClickable(false);
//			etConstellation.setBackgroundDrawable(null);
//			etConstellation.setFocusable(false);
//			etZodiac.setBackgroundDrawable(null);
//			etZodiac.setFocusable(false);
			etFaith.setBackgroundDrawable(null);
			etFaith.setFocusable(false);
			ivHobbyArrow.setVisibility(View.INVISIBLE);
		}
		etConstellation.setBackgroundDrawable(null);
		etConstellation.setFocusable(false);
		etZodiac.setBackgroundDrawable(null);
		etZodiac.setFocusable(false);
		if (NetUtil.checkNet(this)) {
			new InfoTask("get").execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			heightIntNew = 0;
			try {
				heightIntNew = Integer.parseInt(tvHeight.getText().toString().replace("cm", ""));
			} catch (Exception e) {
			}
			weightIntNew = 0;
			try {
				weightIntNew = Integer.parseInt(tvWeight.getText().toString().replace("kg", ""));
			} catch (Exception e) {
			}
			hobbyStrNew = tvHobby.getText().toString();
			telephoneStrNew = etPhone.getText().toString();
			areaStrNew = etArea.getText().toString();
			nationalityStrNew = etNationality.getText().toString();
			birthdayStrNew = tvBirthday.getText().toString();
			constellationStrNew = etConstellation.getText().toString();
			nationStrNew = etRace.getText().toString();
			animalyearStrNew = etZodiac.getText().toString();
			faithStrNew = etFaith.getText().toString();
			nicknameStrNew = etNickName.getText().toString();
			if (heightIntNew != heightIntOld || weightIntNew != weightIntOld || !hobbyStrNew.equals(hobbyStrOld)
					|| !telephoneStrNew.equals(telephoneStrOld) || !areaStrNew.equals(areaStrOld) || !nationalityStrNew.equals(nationalityStrOld)
					|| !birthdayStrNew.equals(birthdayStrOld) || !constellationStrNew.equals(constellationStrOld)
					|| !nationStrNew.equals(nationStrOld) || !animalyearStrNew.equals(animalyearStrOld) || !faithStrNew.equals(faithStrOld)
					|| !nicknameStrNew.equals(nicknameStrOld)) {
				UpdateOrNot();// 提交修改
			} else {
				this.finish();
			}
			break;
		case R.id.btnRight:
			if (NetUtil.checkNet(this)) {
				String nickname = etNickName.getText().toString();
				if (TextUtils.isEmpty(nickname)) {
					Toast.makeText(this, "昵称不能为空", Toast.LENGTH_LONG).show();
					return;
				}
				inputManager.hideSoftInputFromWindow(btnRight.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);// 隐藏键盘
				new InfoTask("save").execute();
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.tvBirthday:
			new MyDatePickerDialog(SelfBaseInfoEditActivity.this, new MyDatePickerDialog.OnDateSetListener() {
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
					String birthdayStr = year + "-" + monthOfYearStr + "-" + dayOfMonthStr;
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					Date birthdayDate = null;
					try {
						birthdayDate = df.parse(birthdayStr);

					} catch (ParseException e) {
						e.printStackTrace();
					}
					long birthdayL = birthdayDate.getTime();
					Calendar mCalendar = Calendar.getInstance();
					long currentTimeL = mCalendar.getTimeInMillis();
					if (birthdayL > currentTimeL) {
						Toast.makeText(SelfBaseInfoEditActivity.this, "生日不能为未来哦！", Toast.LENGTH_LONG).show();
					} else {
						tvBirthday.setText(birthdayStr);
						String zodiacStr = DateUtil.date2Zodica(birthdayDate);
						etZodiac.setText(zodiacStr);
						String constellationStr = DateUtil.date2Constellation(birthdayDate);
						etConstellation.setText(constellationStr);
					}
				}

			}).show();

			break;
		default:
			break;
		}
	}

	/**
	 * 返回时，若数据有修改，提示的Dialog,自定义。
	 */
	public void UpdateOrNot() {
		final AlertDialog dialogExit = new AlertDialog.Builder(SelfBaseInfoEditActivity.this).create();
		dialogExit.show();
		Window dialogWindow = dialogExit.getWindow();
		dialogWindow.setContentView(R.layout.dialog_common_layout);
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialogExit.getWindow().getAttributes();
		lp.width = (int) (display.getWidth() - 60); // 设置宽度
		dialogExit.getWindow().setAttributes(lp);
		TextView tvDialogMsg = (TextView) dialogWindow.findViewById(R.id.tvDialogMsg);
		tvDialogMsg.setText("您对部分的个人信息进行了修改，是否需要提交？");
		Button btnDialogLeft = (Button) dialogWindow.findViewById(R.id.btnDialogLeft);
		btnDialogLeft.setText("取消提交");
		btnDialogLeft.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();
				SelfBaseInfoEditActivity.this.finish();
				
			}
		});
		// 关闭对话框架
		Button btnDialogRight = (Button) dialogWindow.findViewById(R.id.btnDialogRight);
		btnDialogRight.setText("确认提交");
		btnDialogRight.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();
				btnRight.performClick();
			}
		});
	}

	/**
	 * 个人信息
	 * 
	 * @author Zhoujun type 为”get“表示获取个人信息，”save“表示修改保存个人信息；
	 */
	private class InfoTask extends AsyncTask<Void, Void, JSONObject> {

		private String type;

		public InfoTask(String type) {
			super();
			this.type = type;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(SelfBaseInfoEditActivity.this);
			}
			if (type.equals("get")) {
				pd.setMessage("正在获取...");
			} else if (type.equals("save")) {
				pd.setMessage("正在保存..");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject json = null;
			String accessToken = SharedPrefUtil.getUserBean(SelfBaseInfoEditActivity.this).getAccessToken();
			try {
				if (type.equals("get")) {
					json = new BusinessHelper().getInfo(userId);
				} else if (type.equals("save")) {
					heightIntOld = 0;
					try {
						heightIntOld = Integer.parseInt(tvHeight.getText().toString().replace("cm", ""));
					} catch (Exception e) {
					}
					weightIntOld = 0;
					try {
						weightIntOld = Integer.parseInt(tvWeight.getText().toString().replace("kg", ""));
					} catch (Exception e) {
					}
					hobbyStrOld = tvHobby.getText().toString();
					hobbyStrOldId = dba.hobbyIdStrByName(hobbyStrOld);
					telephoneStrOld = etPhone.getText().toString();
					areaStrOld = etArea.getText().toString().trim();
					provinceStrOld = null;
					cityStrOld = null;
					if (!areaStrOld.equals("")) {
						StringTokenizer token = new StringTokenizer(areaStrOld, " ");
						provinceStrOld = token.nextToken();
						cityStrOld = token.nextToken();
					}
					int[] ids = new int[2];
					if (provinceStrOld != null && cityStrOld != null) {
						ids = app.getDbAdapter().findProvinceCityId(provinceStrOld, cityStrOld);
					}
					nationalityStrOld = etNationality.getText().toString();
					birthdayStrOld = tvBirthday.getText().toString();
					constellationStrOld = etConstellation.getText().toString();
					nationStrOld = etRace.getText().toString();
					animalyearStrOld = etZodiac.getText().toString();
					faithStrOld = etFaith.getText().toString();
					nicknameStrOld = etNickName.getText().toString();
					json = new BusinessHelper().updateInfo(userId, accessToken, heightIntOld, weightIntOld, hobbyStrOldId, nationalityStrOld,
							constellationStrOld, animalyearStrOld, faithStrOld, ids[0], ids[1], birthdayStrOld, nationStrOld, nicknameStrOld,
							telephoneStrOld);
				}
			} catch (Exception e) {
			}
			return json;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (pd != null) {
				pd.dismiss();
			}
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						if (type.equals("get")) {
							JSONObject data = result.getJSONObject("data");
							heightIntOld = 0;
							try {
								heightIntOld = Integer.parseInt(data.getString("height"));
							} catch (Exception e) {
							}
							weightIntOld = 0;
							try {
								weightIntOld = Integer.parseInt(data.getString("weight"));
							} catch (Exception e) {
							}
							hobbyStrOldId = data.getString("hobby");
							hobbyStrOld = dba.hobbyNameStrById(hobbyStrOldId);

							telephoneStrOld = data.getString("telephone");
							areaStrOld = data.getString("provinceName") + " " + data.getString("cityName");
							nationalityStrOld = data.getString("nationality");
							birthdayStrOld = data.getString("birthday");
							constellationStrOld = data.getString("constellation");
							nationStrOld = data.getString("nation");
							animalyearStrOld = data.getString("animalyear");
							faithStrOld = data.getString("faith");
							nicknameStrOld = data.getString("nickname");
							//
							tvLevel.setText("LV" + data.getInt("level"));
							int progress = (int) data.getDouble("percentage");
							pbExperience.setProgress(progress);
							etNickName.setText(nicknameStrOld);
							etPhone.setText(telephoneStrOld);

							if (heightIntOld > 0) {
								tvHeight.setText(String.valueOf(heightIntOld) + "cm");
							}
							if (weightIntOld > 0) {
								tvWeight.setText(String.valueOf(weightIntOld) + "kg");
							}
							etRace.setText(nationStrOld);// 民族
							tvHobby.setText(hobbyStrOld);
							etArea.setText(areaStrOld);
							etNationality.setText(nationalityStrOld);// 国籍
							etConstellation.setText(constellationStrOld);
							etZodiac.setText(animalyearStrOld);
							etFaith.setText(faithStrOld);
							tvBirthday.setText(birthdayStrOld);

							boolean isKoala = data.getInt("isKangaroo") == Constants.KOALA ? true : false;
							if (isKoala && isEdit) {
								tvBirthday.setClickable(true);
								tvBirthday.setBackgroundResource(R.drawable.bg_edit);
							} else if (!isKoala && isEdit) {
								tvBirthday.setClickable(false);
								tvBirthday.setBackgroundDrawable(null);
							}
						} else if (type.equals("save")) {
							UserBean userBean = SharedPrefUtil.getUserBean(SelfBaseInfoEditActivity.this);
							userBean.setNickname(nicknameStrNew);
							String birthday = tvBirthday.getText().toString();
							userBean.setBirthday(birthday);
							SharedPrefUtil.setUserBean(SelfBaseInfoEditActivity.this, userBean);
							Toast.makeText(SelfBaseInfoEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
							SelfBaseInfoEditActivity.this.finish();
						}
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(SelfBaseInfoEditActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(SelfBaseInfoEditActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(SelfBaseInfoEditActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					Toast.makeText(SelfBaseInfoEditActivity.this, "数据错误", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			} else {
				Toast.makeText(SelfBaseInfoEditActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			btnLeft.performClick();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		hobbyArr = dba.findHobbysIdAndStr();
		tvHobby.setText(hobbyArr[1]);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}

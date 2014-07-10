package com.elephant.ediyou.activity;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.RegisterActivity.RegistTask;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.StringUtil;
import com.google.zxing.common.StringUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * 仅手机号码注册（第一步）
 * 
 * @author syghh
 * 
 */
public class RegisterWithPhoneNumActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;

	private TextView tvUserProtocol;

	private EditText edtPhoneNum;
	private Button btnGetAuthCode;
	private EditText edtPutAuthCode;
	private Button btnCheck;

	private boolean isCheckedProtocol = false;
	
	private TextView tvShowSecond;//展示倒计时
	private boolean isTime = false;
	private Timer mTimer;
	private int time = 60;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_with_phone_num);
		findView();
		fillData();
		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.bg_btn_selector);
		btnRight.setText("下一步");
		btnRight.setGravity(Gravity.CENTER);
		btnRight.setTextColor(Color.rgb(157, 208, 99));
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

		edtPhoneNum = (EditText) this.findViewById(R.id.edtPhoneNum);
		btnGetAuthCode = (Button) this.findViewById(R.id.btnGetAuthCode);
		edtPutAuthCode = (EditText) this.findViewById(R.id.edtPutAuthCode);
		btnCheck = (Button) this.findViewById(R.id.btnCheck);
		
		tvShowSecond = (TextView) this.findViewById(R.id.tvShowSecond);
		
		tvUserProtocol = (TextView) this.findViewById(R.id.tvUserProtocol);

		tvUserProtocol.setOnClickListener(this);
		btnGetAuthCode.setOnClickListener(this);
		
		btnCheck.setOnClickListener(this);
		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
	}

	@Override
	public void fillData() {
		tvTitle.setText("注册");
		isCheckedProtocol = true;
		btnCheck.setBackgroundResource(R.drawable.ic_check_sel);
		tvUserProtocol.setText(Html.fromHtml("我同意" + "<u><font color=\"#3366FF\">" + "《用户注册协议》" + "</u>"));
		StringUtil.limitEditTextLength(edtPhoneNum, 11, this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnRight:// 下一步，验证
			String phoneNum = edtPhoneNum.getText().toString().trim();
			String code = edtPutAuthCode.getText().toString().trim();
			if (NetUtil.checkNet(this)) {
				if (isCheckedProtocol == true) {
					if (!TextUtils.isEmpty(phoneNum)) {
						if (!TextUtils.isEmpty(code)) {
							if (StringUtil.isMobile(phoneNum)) {
								// 验证验证码
								new RegisterCheckCodeTask(phoneNum, code).execute();
							} else {
								Toast.makeText(this, "输入的手机号码不合法", Toast.LENGTH_SHORT).show();
							}
						} else {
							Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(this, "请输入手机号码", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(this, "请您查看并勾选《用户注册协议》", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btnGetAuthCode:// 获取验证码
			String phoneNumStr = edtPhoneNum.getText().toString().trim();
			if (NetUtil.checkNet(this)) {
				if (!TextUtils.isEmpty(phoneNumStr)) {
					if (StringUtil.isMobile(phoneNumStr)) {
						btnGetAuthCode.setTextColor(getResources().getColor(R.color.hint_gray));
						btnGetAuthCode.setClickable(false);
						new RegisterGetCodeTask(phoneNumStr).execute();
					} else {
						Toast.makeText(this, "输入的手机号码不合法", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(this, "请输入手机号码", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.tvUserProtocol:// 注册协议
			Intent intent = new Intent(this, RegisterProtocolActivity.class);
			intent.putExtra("isKangaroo", 0); // 0-考拉， 1-袋鼠
			startActivity(intent);
			break;
		case R.id.btnCheck:
			if (isCheckedProtocol == false) {
				isCheckedProtocol = true;
				btnCheck.setBackgroundResource(R.drawable.ic_check_sel);
			} else {
				isCheckedProtocol = false;
				btnCheck.setBackgroundResource(R.drawable.ic_check_nor);
			}
			break;
		}
	}
	
	/**
	 * 启动倒计时定时器
	 */
	private void setTimerTask() {
		tvShowSecond.setVisibility(View.VISIBLE);
		isTime = true;
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 1;
				doActionHandler.sendMessage(message);
			}
		}, 1000, 1000);
	}

	/**
	 * do some action
	 */
	private Handler doActionHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int msgId = msg.what;
			switch (msgId) {
			case 1:
				time--;
				tvShowSecond.setText(time + "秒后可重发");
				if (time == 0) {
					closeTimer();
					isTime = false ;
					btnGetAuthCode.setTextColor(getResources().getColor(R.color.text_green));
					btnGetAuthCode.setClickable(true);
					tvShowSecond.setVisibility(View.GONE);
					time = 60;
				}
				break;
			}
		}
	};

	/**
	 * 关闭
	 */
	private void closeTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	/**
	 * 获取验证码
	 * 
	 * @author syghh
	 * 
	 */
	class RegisterGetCodeTask extends AsyncTask<Void, Void, JSONObject> {

		private String phoneNum;

		public RegisterGetCodeTask(String phoneNum) {
			this.phoneNum = phoneNum;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().registerGetCode(phoneNum);
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						setTimerTask();
						Toast.makeText(RegisterWithPhoneNumActivity.this, "系统已向您的手机发送此次注册的验证码短信，请您及时查收", Toast.LENGTH_LONG).show();
					} else {
						btnGetAuthCode.setTextColor(getResources().getColor(R.color.text_green));
						btnGetAuthCode.setClickable(true);
						tvShowSecond.setVisibility(View.GONE);
						Toast.makeText(RegisterWithPhoneNumActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					btnGetAuthCode.setTextColor(getResources().getColor(R.color.text_green));
					btnGetAuthCode.setClickable(true);
					tvShowSecond.setVisibility(View.GONE);
					Toast.makeText(RegisterWithPhoneNumActivity.this, "获取验证码失败，请重新获取", Toast.LENGTH_SHORT).show();
				}
			} else {
				btnGetAuthCode.setTextColor(getResources().getColor(R.color.text_green));
				btnGetAuthCode.setClickable(true);
				tvShowSecond.setVisibility(View.GONE);
				Toast.makeText(RegisterWithPhoneNumActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 验证
	 * 
	 * @author syghh
	 * 
	 */
	class RegisterCheckCodeTask extends AsyncTask<Void, Void, JSONObject> {

		private String phoneNum;
		private String code;

		public RegisterCheckCodeTask(String phoneNum, String code) {
			this.phoneNum = phoneNum;
			this.code = code;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().registerCheckCode(phoneNum, code);
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						startActivity(new Intent(RegisterWithPhoneNumActivity.this, RegisterSetPasswordActivity.class).putExtra("phoneNum", phoneNum));
						Toast.makeText(RegisterWithPhoneNumActivity.this, "验证码已验证成功，请设定密码", Toast.LENGTH_SHORT).show();
					} else {

						Toast.makeText(RegisterWithPhoneNumActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(RegisterWithPhoneNumActivity.this, "验证失败，请重新验证", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(RegisterWithPhoneNumActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
		}
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
}

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
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.EventListHadCreateActivity.GetEventHadCreateListTask;
import com.elephant.ediyou.activity.RegisterWithPhoneNumActivity.RegisterCheckCodeTask;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 找回密码
 * 
 * @author syghh
 * 
 */
public class GetMyPasswordBackActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button btnLeft;
	private TextView tvTitle;
	private Button btnRight;

	private ProgressDialog pd;

	private Button btnByMoblie;// 通过手机号找回
	private LinearLayout llMobileType;
	private EditText edtMobile;
	private Button btnGetAuthCode;
	
	private EditText edtPhoneNum;
	private EditText edtPutAuthCode;
	private TextView tvShowSecond;
	private boolean isTime = false;
	private Timer mTimer;
	private int time = 60;

	private Button btnbyEmail;// 通过邮箱找回
	private LinearLayout llEmailType;
	private EditText edtEmail;

	private final static int BY_MOBLIE = 0;
	private final static int BY_EMAIL = 1;

	private int type = BY_MOBLIE;// 判断“通过手机号”（0）或“通过邮箱”（1）

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_my_password_back);
		findView();
		fillData();
		// 将Activity添加到容器
		((CommonApplication) getApplicationContext()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.bg_btn_selector);
		btnRight.setText("下一步");
		btnRight.setGravity(Gravity.CENTER);
		btnRight.setTextColor(Color.rgb(157, 208, 99));
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("找回密码");

		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);

		btnByMoblie = (Button) this.findViewById(R.id.btnByMoblie);
		llMobileType = (LinearLayout) this.findViewById(R.id.llMobileType);
		edtMobile = (EditText) this.findViewById(R.id.edtMobile);
		
		edtPhoneNum = (EditText) findViewById(R.id.edtPhoneNum);
		btnGetAuthCode = (Button) findViewById(R.id.btnGetAuthCode);
		btnGetAuthCode.setOnClickListener(this);
		edtPutAuthCode = (EditText) findViewById(R.id.edtPutAuthCode);
		tvShowSecond = (TextView) findViewById(R.id.tvShowSecond);

		btnbyEmail = (Button) this.findViewById(R.id.btnbyEmail);
		llEmailType = (LinearLayout) this.findViewById(R.id.llEmailType);
		edtEmail = (EditText) this.findViewById(R.id.edtEmail);

		btnByMoblie.setOnClickListener(this);
		btnbyEmail.setOnClickListener(this);

	}

	@Override
	public void fillData() {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnRight:
			String phoneNum = edtPhoneNum.getText().toString().trim();
			String code = edtPutAuthCode.getText().toString().trim();
			if(!StringUtil.isMobile(phoneNum)){
				Toast.makeText(this, "输入的手机号码不合法", Toast.LENGTH_SHORT).show();
				return;
			}
			if(TextUtils.isEmpty(code)){
				Toast.makeText(this, "输入的验证码为空", Toast.LENGTH_SHORT).show();
				return;
			}
			if (NetUtil.checkNet(this)) {
				new PasswordResetCheckCodeTask(phoneNum, code).execute();
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.btnGetAuthCode:
			String phoneStr = edtPhoneNum.getText().toString();
			if(!StringUtil.isMobile(phoneStr)){
				Toast.makeText(this, "输入的手机号码不合法", Toast.LENGTH_SHORT).show();
				return;
			}
			if(NetUtil.checkNet(this)){
				btnGetAuthCode.setTextColor(getResources().getColor(R.color.hint_gray));
				btnGetAuthCode.setClickable(false);
				new PasswordGetCodeTask(phoneStr).execute();
			}else{
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btnByMoblie:// 通过手机找回
			btnByMoblie.setBackgroundResource(R.drawable.order_tab_bg_sel);
			btnByMoblie.setTextColor(Color.rgb(157, 208, 99));
			btnbyEmail.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btnbyEmail.setTextColor(Color.rgb(201, 195, 179));
			llMobileType.setVisibility(View.VISIBLE);
			llEmailType.setVisibility(View.GONE);
			type = BY_MOBLIE;
			break;
		case R.id.btnbyEmail:// 通过邮箱找回
			btnByMoblie.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btnByMoblie.setTextColor(Color.rgb(201, 195, 179));
			btnbyEmail.setBackgroundResource(R.drawable.order_tab_bg_sel);
			btnbyEmail.setTextColor(Color.rgb(157, 208, 99));
			llMobileType.setVisibility(View.GONE);
			llEmailType.setVisibility(View.VISIBLE);			
			type = BY_EMAIL;
			break;
		}
	}

	/**
	 * 获取验证码
	 * 
	 * @author syghh
	 * 
	 */
	class PasswordGetCodeTask extends AsyncTask<Void, Void, JSONObject> {

		private String phoneNum;

		public PasswordGetCodeTask(String phoneNum) {
			this.phoneNum = phoneNum;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().passwordGetCode(phoneNum);
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
						Toast.makeText(GetMyPasswordBackActivity.this, "系统已向您的手机发送找回密码的验证码短信，请您及时查收", Toast.LENGTH_SHORT).show();
					} else {
						btnGetAuthCode.setTextColor(getResources().getColor(R.color.text_green));
						btnGetAuthCode.setClickable(true);
						tvShowSecond.setVisibility(View.GONE);
						Toast.makeText(GetMyPasswordBackActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					btnGetAuthCode.setTextColor(getResources().getColor(R.color.text_green));
					btnGetAuthCode.setClickable(true);
					tvShowSecond.setVisibility(View.GONE);
					Toast.makeText(GetMyPasswordBackActivity.this, "获取验证码失败，请重新获取", Toast.LENGTH_SHORT).show();
				}
			} else {
				btnGetAuthCode.setTextColor(getResources().getColor(R.color.text_green));
				btnGetAuthCode.setClickable(true);
				tvShowSecond.setVisibility(View.GONE);
				Toast.makeText(GetMyPasswordBackActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 * 验证
	 * 
	 * @author syghh
	 * 
	 */
	class PasswordResetCheckCodeTask extends AsyncTask<Void, Void, JSONObject> {

		private String phoneNum;
		private String code;

		public PasswordResetCheckCodeTask(String phoneNum, String code) {
			this.phoneNum = phoneNum;
			this.code = code;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().passwordCheckCode(phoneNum, code);
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
						Intent intent = new Intent(GetMyPasswordBackActivity.this, RegisterSetPasswordActivity.class);
						intent.putExtra("resetPassword", true);
						intent.putExtra("phoneNum", phoneNum);
						startActivity(intent);
						Toast.makeText(GetMyPasswordBackActivity.this, "验证码已验证成功，请重置密码", Toast.LENGTH_SHORT).show();
					} else {

						Toast.makeText(GetMyPasswordBackActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(GetMyPasswordBackActivity.this, "验证失败，请重新验证", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(GetMyPasswordBackActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
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
}

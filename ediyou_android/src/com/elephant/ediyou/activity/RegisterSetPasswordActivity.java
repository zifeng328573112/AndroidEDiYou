package com.elephant.ediyou.activity;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 仅手机号码注册（第二步）---设置用户密码
 * 
 * @author syghh
 * 
 */
public class RegisterSetPasswordActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;

	private EditText edtPassword;
	private EditText edtConfirmPassword;

	private String phoneNum;
	private boolean isResetPassword = false;

	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_set_password);
		phoneNum = getIntent().getStringExtra("phoneNum");
		isResetPassword = getIntent().getBooleanExtra("resetPassword", false);
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

		edtPassword = (EditText) this.findViewById(R.id.edtPassword);
		edtConfirmPassword = (EditText) this.findViewById(R.id.edtConfirmPassword);
		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
	}

	@Override
	public void fillData() {
		tvTitle.setText("设置密码");
		if (isResetPassword) {
			tvTitle.setText("重置密码");
			btnRight.setText("提交");
		}
		StringUtil.limitEditTextLength(edtPassword, 32, this);
		StringUtil.limitEditTextLength(edtConfirmPassword, 32, this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnRight:// 下一步
			String passWordStr = edtPassword.getText().toString().trim();
			String passWordSureStr = edtConfirmPassword.getText().toString().trim();

			if (StringUtil.isStringLengthInLimit(passWordStr, 6, 32) != 0) {
				Toast.makeText(this, "请输入6-32位的密码", Toast.LENGTH_SHORT).show();
				return;
			}
			if (passWordStr.equals(passWordSureStr)) {
				if (isResetPassword) {
					if (NetUtil.checkNet(this)) {
						new ResetPasswordTask(phoneNum, passWordStr).execute();
					} else {
						Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
					}
				} else {
					Intent intent = new Intent(RegisterSetPasswordActivity.this, RegisterNextActivity.class);
					intent.putExtra("email", phoneNum);
					intent.putExtra("password", passWordStr);
					startActivity(intent);
				}
				// if (NetUtil.checkNet(this)) {
				// new RegistTask(phoneNum, passWordStr).execute();
				// } else {
				// Toast.makeText(this, R.string.NoSignalException,
				// Toast.LENGTH_LONG).show();
				// }

			} else {
				Toast.makeText(this, "两次输入密码不一致", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	private class ResetPasswordTask extends AsyncTask<Void, Void, JSONObject> {
		private String phone;
		private String password;

		public ResetPasswordTask(String phone, String password) {
			super();
			this.phone = phone;
			this.password = password;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RegisterSetPasswordActivity.this);
			}
			pd.setMessage("重置密码中...");
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().resetPassword(phone, password);
			} catch (SystemException e) {
				return null;
			}
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
						Toast.makeText(RegisterSetPasswordActivity.this, "重置密码成功，请使用新密码登录！", Toast.LENGTH_SHORT).show();
						Intent loginIntent = new Intent(RegisterSetPasswordActivity.this, LoginActivity.class);
						startActivity(loginIntent);
						finish();
					} else {
						Toast.makeText(RegisterSetPasswordActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					Toast.makeText(RegisterSetPasswordActivity.this, "重置密码失败，请重试", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(RegisterSetPasswordActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
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

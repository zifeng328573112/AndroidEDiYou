package com.elephant.ediyou.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
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
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

public class FeedBackActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;

	private EditText etContent;
	private EditText etUserPhone;

	private UserBean userBean;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		userBean = SharedPrefUtil.getUserBean(this);
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
		btnRight.setBackgroundResource(R.drawable.bg_btn_selector);
		btnRight.setText("提交");
		btnRight.setGravity(Gravity.CENTER);
		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		etContent = (EditText) findViewById(R.id.etContent);
		etUserPhone = (EditText) findViewById(R.id.etUserPhone);
	}

	@Override
	public void fillData() {
		tvTitle.setText("用户反馈");
		StringUtil.limitEditTextLength(etContent, 200, this);
		StringUtil.limitEditTextLength(etUserPhone, 11, this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			overridePendingTransition(R.anim.push_right_in_noalp_back, R.anim.push_left_out_noalp_back);
			break;
		case R.id.btnRight:
			long userId = userBean.getUserId();
			String accessToken = userBean.getAccessToken();
			String content = etContent.getText().toString();
			String user_phone = etUserPhone.getText().toString();
			if (userBean == null) {
				Intent intent = new Intent(this, LoginActivity.class);
				intent.putExtra("back", "back");
				startActivity(intent);
			} else {
				if (StringUtil.isBlank(content)) {
					Toast.makeText(this, "请填写您的建议内容", Toast.LENGTH_LONG).show();
				} else if (!StringUtil.isMobile(user_phone)) {
					Toast.makeText(this, "输入的手机号码不合法", Toast.LENGTH_LONG).show();
				} else {
					// 提交
					if (NetUtil.checkNet(this)) {
						new UpLoadUsersFeedBack(userId, accessToken, content, user_phone).execute();
					} else {
						Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
					}
				}
			}

			break;
		}
	}

	/**
	 * 用户反馈
	 * 
	 * @author syghh
	 * 
	 */
	class UpLoadUsersFeedBack extends AsyncTask<String, Void, JSONObject> {
		private long userId;
		private String accessToken;
		private String content;
		private String user_phone;

		public UpLoadUsersFeedBack(Long userId, String accessToken, String content, String user_phone) {
			this.userId = userId;
			this.accessToken = accessToken;
			this.content = content;
			this.user_phone = user_phone;
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			try {
				return new BusinessHelper().upLoadUsersFeedBack(userId, content, accessToken, user_phone);
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
					if (result.getInt("status") == Constants.SUCCESS) {
						Toast.makeText(FeedBackActivity.this, "非常感谢您的反馈信息，我们将做得更好！", Toast.LENGTH_SHORT).show();
						FeedBackActivity.this.finish();
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(FeedBackActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(FeedBackActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(FeedBackActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(FeedBackActivity.this, "反馈失败", Toast.LENGTH_SHORT).show();
			}

		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
			overridePendingTransition(R.anim.push_right_in_noalp_back, R.anim.push_left_out_noalp_back);
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
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}

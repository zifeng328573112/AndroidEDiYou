package com.elephant.ediyou.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.LoginActivity.ProfileKangarooId;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.google.zxing.common.StringUtils;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.umeng.analytics.MobclickAgent;

/**
 * 注册页面一
 * 
 * @author syghh
 * 
 */
public class RegisterActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;

	private EditText edtEmail;
	private EditText edtPassWord;
	private EditText edtPassWordSure;
	private Button btnCheck;
	private TextView tvUserProtocol;

	private ImageView ivSina;
	private ImageView ivQQ;

	private boolean isCheckedProtocol = false;

	private String emailStr;
	private String passWordStr;
	private String passWordSureStr;

	private ProgressDialog pd;
	private OAuthV2 oAuth;// qq认证；
	private static final int SINA_LOGIN = 0;
	private static final int QQ_LOGIN = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_layout);
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
		edtEmail = (EditText) this.findViewById(R.id.edtEmail);
		edtPassWord = (EditText) this.findViewById(R.id.edtPassWord);
		edtPassWordSure = (EditText) this.findViewById(R.id.edtPassWordSure);
		btnCheck = (Button) this.findViewById(R.id.btnCheck);
		tvUserProtocol = (TextView) this.findViewById(R.id.tvUserProtocol);

		ivSina = (ImageView) this.findViewById(R.id.ivSina);
		ivQQ = (ImageView) this.findViewById(R.id.ivQQ);
		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		
		btnCheck.setOnClickListener(this);
		tvUserProtocol.setOnClickListener(this);
		ivSina.setOnClickListener(this);
		ivQQ.setOnClickListener(this);

	}

	@Override
	public void fillData() {
		tvTitle.setText("注册");
		isCheckedProtocol = true;
		btnCheck.setBackgroundResource(R.drawable.ic_check_sel);
		tvUserProtocol.setText(Html.fromHtml("我同意" + "<u><font color=\"#3366FF\">" + "《用户注册协议》" + "</u>"));
		StringUtil.limitEditTextLength(edtEmail, 32, this);
		StringUtil.limitEditTextLength(edtPassWord, 32, this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnRight:// 下一步
			emailStr = edtEmail.getText().toString().trim();
			passWordStr = edtPassWord.getText().toString().trim();
			passWordSureStr = edtPassWordSure.getText().toString().trim();
			if (StringUtil.isBlank(emailStr) || StringUtil.isBlank(passWordStr) || StringUtil.isBlank(passWordSureStr)) {
				Toast.makeText(this, "请输入完整的注册信息", Toast.LENGTH_SHORT).show();
				return;
			} else {
				if (!StringUtil.isEmail(emailStr) && emailStr.contains("@")) {
					Toast.makeText(this, "输入的邮箱不合法", Toast.LENGTH_SHORT).show();
					return;
				} else if (!StringUtil.isEmail(emailStr) && !emailStr.contains("@")) {
					if (!StringUtil.isMobile(emailStr)) {
						Toast.makeText(this, "输入的手机号码不合法", Toast.LENGTH_SHORT).show();
						return;
					}
				}

				if (StringUtil.isStringLengthInLimit(emailStr, 6, 32) != 0) {
					Toast.makeText(this, "请输入6-32位的邮箱", Toast.LENGTH_SHORT).show();
					return;
				}
				if (StringUtil.isStringLengthInLimit(passWordStr, 6, 32) != 0) {
					Toast.makeText(this, "请输入6-32位的密码", Toast.LENGTH_SHORT).show();
					return;
				}
				if (passWordStr.equals(passWordSureStr)) {
					if (NetUtil.checkNet(this)) {
						if (isCheckedProtocol == true) {
							// 注册（异步）
							new RegistTask(emailStr, passWordStr).execute();
						} else {
							Toast.makeText(this, "请您查看并勾选《用户注册协议》", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
					}

				} else {
					Toast.makeText(this, "两次输入密码不一致", Toast.LENGTH_SHORT).show();
				}
			}
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
		case R.id.tvUserProtocol:// 用户协议查看跳转
			Intent intent = new Intent(this, RegisterProtocolActivity.class);
			intent.putExtra("isKangaroo", 0); // 0-考拉， 1-袋鼠
			startActivity(intent);
			break;
		case R.id.ivSina:
			if (SharedPrefUtil.checkWeiboBind(this)) {
				String uid = SharedPrefUtil.getWeiboUid(this);
				new ThirdLoginTask(uid, SINA_LOGIN).execute();
			} else {
				if (NetUtil.checkNet(this)) {
					Intent authorizeIntent = new Intent(this, AuthorizeActivity.class);
					authorizeIntent.putExtra(Constants.EXTRA_BIND_FROM, Constants.BIND_WEIBO);
					startActivityForResult(authorizeIntent, Constants.REQUEST_CODE_BIND_WEIBO);
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
				}
			}
			break;
		case R.id.ivQQ:
			if (SharedPrefUtil.checkQQBind(this)) {
				String openId = SharedPrefUtil.getQQOpenid(this);
				new ThirdLoginTask(openId, QQ_LOGIN).execute();
			} else {
				if (NetUtil.checkNet(this)) {
					oAuth = new OAuthV2(Constants.TENCENT_REDIRECT_URL);
					oAuth.setClientId(Constants.TENCENT_APP_ID);
					oAuth.setClientSecret(Constants.TENCENT_APP_KEY);
					Intent qqIntent = new Intent(this, OAuthV2AuthorizeWebView.class);
					qqIntent.putExtra("oauth", oAuth);
					startActivityForResult(qqIntent, Constants.REQUEST_CODE_BIND_QQ);

				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
				}
			}
			break;
		}
	}

	/**
	 * 注册
	 * 
	 * @author syghh
	 * 
	 */
	class RegistTask extends AsyncTask<Void, Void, JSONObject> {

		private String username;
		private String password;

		public RegistTask(String username, String password) {
			this.username = username;
			this.password = password;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RegisterActivity.this);
				pd.setMessage("正在注册...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().register(username, password);
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (pd != null)
				pd.dismiss();
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						JSONObject userJson = result.getJSONObject("user");
						int userId = userJson.getInt("user_id");
						String accessToken = userJson.getString("access_token");
						Intent next = new Intent(RegisterActivity.this, RegisterNextActivity.class);
						next.putExtra("email", emailStr);
						next.putExtra("userId", userId);
						next.putExtra("access_token", accessToken);
						startActivity(next);
						Toast.makeText(RegisterActivity.this, "注册成功！您已经成为考拉用户", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(RegisterActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(RegisterActivity.this, "注册账号失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(RegisterActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_BIND_QQ) {
			if (resultCode == OAuthV2AuthorizeWebView.RESULT_CODE) {
				// 取得返回的OAuthV2类实例oAuth
				oAuth = (OAuthV2) data.getExtras().getSerializable("oauth");
				SharedPrefUtil.setQQInfo(this, oAuth.getAccessToken(), oAuth.getExpiresIn(), oAuth.getOpenid(), System.currentTimeMillis() + "");

				new ThirdLoginTask(oAuth.getOpenid(), QQ_LOGIN).execute();
			}
		}
		if (requestCode == Constants.REQUEST_CODE_BIND_WEIBO) {
			if (resultCode == RESULT_OK) {
				String uid = SharedPrefUtil.getWeiboUid(RegisterActivity.this);
				new ThirdLoginTask(uid, SINA_LOGIN).execute();
			}
		}
	}

	/**
	 * 第三方登陆；
	 * 
	 * @author Zhoujun
	 * 
	 */
	private class ThirdLoginTask extends AsyncTask<Void, Void, JSONObject> {

		private String tId;
		private int tType;

		public ThirdLoginTask(String tId, int tType) {
			super();
			this.tId = tId;
			this.tType = tType;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RegisterActivity.this);
				pd.setMessage("正在登陆...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject thirdLogin = null;
			try {
				thirdLogin = new BusinessHelper().thirdLogin(tType, tId);
			} catch (Exception e) {
			}
			return thirdLogin;
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
						UserBean userBean = new UserBean(result.getJSONObject("user"));
						SharedPrefUtil.setUserBean(RegisterActivity.this, userBean);
						long userId = userBean.getUserId();
						String access_token = userBean.getAccessToken();
						if (userBean.getIsKangaroo() == 1) {
							new ProfileKangarooId(userId, access_token).execute();
						} else {
							startActivity(new Intent(RegisterActivity.this, MainHomeActivityGroup.class));
							finish();
						}
						Toast.makeText(RegisterActivity.this, "登录成功", Toast.LENGTH_LONG).show();
					} else if (status == 2) {// 第三方首次登陆,补充个人信息
						Intent intent = new Intent(RegisterActivity.this, RegisterNextActivity.class);
						intent.putExtra("thirdLogin", true);
						startActivity(intent);
					}
				} catch (JSONException e) {
				}

			}
		}

	}

	/**
	 * 若为袋鼠，获取袋鼠id
	 * 
	 * @author syghh
	 * 
	 */
	class ProfileKangarooId extends AsyncTask<Void, Void, JSONObject> {

		private long userId;
		private String access_token;

		public ProfileKangarooId(long userId, String access_token) {
			this.userId = userId;
			this.access_token = access_token;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().profileKangarooId(userId, access_token);
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (pd != null)
				pd.dismiss();
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						long rooId = result.getLong("data");
						SharedPrefUtil.setRooId(RegisterActivity.this, rooId);
						startActivity(new Intent(RegisterActivity.this, MainHomeActivityGroup.class));
						finish();
						Toast.makeText(RegisterActivity.this, "获取成功", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(RegisterActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(RegisterActivity.this, "获取失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(RegisterActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}
}

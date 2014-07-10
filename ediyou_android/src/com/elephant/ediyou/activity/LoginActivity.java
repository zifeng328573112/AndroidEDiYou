package com.elephant.ediyou.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.AndroidUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.tencent.mm.sdk.platformtools.Log;
import com.tencent.tauth.TAuthView;
import com.tencent.tauth.TencentOpenAPI;
import com.tencent.tauth.bean.OpenId;
import com.tencent.tauth.http.Callback;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.umeng.analytics.MobclickAgent;

/**
 * 登录页面
 * (第三方登录部分已在布局文件中暂时 gone)
 * @author syghh
 * 
 */
public class LoginActivity extends Activity implements IBaseActivity, OnClickListener {
	private EditText edtUsername;
	private EditText edtPassword;
	private Button btnRegist;
	private Button btnLogin;
	private TextView tvAround;

	private String username;
	private String password;

	private TextView tvGetPasswordBack;

	private ImageView ivSina;
	private ImageView ivQQ;

	private ProgressDialog pd;
	private OAuthV2 oAuth;// qq认证；
	private static final int SINA_LOGIN = 0;
	private static final int QQ_LOGIN = 1;

	private AuthReceiver receiver;
	private String from = null;// 若from mainhome或则跳转并finish，若==null,则仅finish

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		username = getIntent().getStringExtra("username");
		from = getIntent().getStringExtra("back");
		if(from==null){
			from = "toMainHome";
		}
		findView();
		fillData();
		clearInfo();
		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		edtUsername = (EditText) this.findViewById(R.id.edtUsername);
		if (!StringUtil.isBlank(username)) {
			edtUsername.setText(username);
		}
		edtPassword = (EditText) this.findViewById(R.id.edtPassword);
		btnRegist = (Button) this.findViewById(R.id.btnRegist);
		btnLogin = (Button) this.findViewById(R.id.btnLogin);
		tvAround = (TextView) this.findViewById(R.id.tvAround);

		ivSina = (ImageView) this.findViewById(R.id.ivSina);
		ivQQ = (ImageView) this.findViewById(R.id.ivQQ);
		ivSina.setOnClickListener(this);
		ivQQ.setOnClickListener(this);

		btnRegist.setOnClickListener(this);
		btnLogin.setOnClickListener(this);
		tvAround.setOnClickListener(this);
		btnRegist.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					btnRegist.setTextColor(Color.BLACK);
				} else if (event.getAction() != MotionEvent.ACTION_DOWN) {
					btnRegist.setTextColor(Color.rgb(157, 208, 99));
				}
				return false;
			}

		});
		btnLogin.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					btnLogin.setTextColor(Color.BLACK);
				} else if (event.getAction() != MotionEvent.ACTION_DOWN) {
					btnLogin.setTextColor(Color.rgb(157, 208, 99));
				}
				return false;
			}

		});
		tvAround.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					tvAround.setTextColor(Color.BLACK);
				} else if (event.getAction() != MotionEvent.ACTION_DOWN) {
					tvAround.setTextColor(Color.rgb(157, 208, 99));
				}

				return false;
			}

		});

		tvGetPasswordBack = (TextView) this.findViewById(R.id.tvGetPasswordBack);
		tvGetPasswordBack.setOnClickListener(this);
	}

	@Override
	public void fillData() {

	}

	/**
	 * 在登陆界面清除用户信息，绑定的账号等；
	 */
	private void clearInfo() {
		SharedPrefUtil.clearUserBean(this);
		SharedPrefUtil.clearQQBind(this);
		SharedPrefUtil.clearWeiboBind(this);
		SharedPrefUtil.clearNotiLocation(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRegist:
			startActivity(new Intent(this, RegisterWithPhoneNumActivity.class));
//			startActivity(new Intent(this, RegisterActivity.class));
			break;
		case R.id.btnLogin:
			username = edtUsername.getText().toString().trim();
			password = edtPassword.getText().toString().trim();
			if(!StringUtil.isMobile(username)){
				Toast.makeText(this, "请您正确的手机号码登录", Toast.LENGTH_SHORT).show();
				return;
			}
			if(TextUtils.isEmpty(password)){
				Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
				return;
			}
			if (NetUtil.checkNet(this)) {
				new LoginTask(username, password).execute();
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.tvAround:

			startActivity(new Intent(this, MainHomeActivityGroup.class));
			finish();
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
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.ivQQ:
			if (SharedPrefUtil.checkQQBind(this)) {
				String openId = SharedPrefUtil.getQQOpenid(this);
				new ThirdLoginTask(openId, QQ_LOGIN).execute();
			} else {
				if (NetUtil.checkNet(this)) {
					// oAuth = new OAuthV2(Constants.TENCENT_REDIRECT_URL);
					// oAuth.setClientId(Constants.TENCENT_APP_ID);
					// oAuth.setClientSecret(Constants.TENCENT_APP_KEY);
					// Intent qqIntent = new Intent(this,
					// OAuthV2AuthorizeWebView.class);
					// qqIntent.putExtra("oauth", oAuth);
					// startActivityForResult(qqIntent,
					// Constants.REQUEST_CODE_BIND_QQ);
					registerIntentReceivers();
					Intent intent = new Intent(this, com.tencent.tauth.TAuthView.class);
					intent.putExtra(TAuthView.CLIENT_ID, Constants.QQ_APP_ID);
					intent.putExtra(TAuthView.SCOPE, Constants.QQ_APP_SCOPE);
					intent.putExtra(TAuthView.TARGET, Constants.QQ_APP_TARGET);
					startActivity(intent);
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.tvGetPasswordBack:
			startActivity(new Intent(this, GetMyPasswordBackActivity.class));
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 当progress存在时，点击返回取消progress
			if (pd != null) {
				pd.dismiss();
			}
			// 当退出时，弹出推出框
			//exitHome();
			startActivity(new Intent(LoginActivity.this, MainHomeActivityGroup.class));
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 退出提示的Dialog,自定义。
	 */
	public void exitHome() {
		final AlertDialog dialogExit = new AlertDialog.Builder(LoginActivity.this).create();
		dialogExit.show();
		Window dialogWindow = dialogExit.getWindow();
		dialogWindow.setContentView(R.layout.dialog_common_layout);
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialogExit.getWindow().getAttributes();
		lp.width = (int) (display.getWidth() - 60); // 设置宽度
		dialogExit.getWindow().setAttributes(lp);

		TextView tvDialogMsg = (TextView) dialogWindow.findViewById(R.id.tvDialogMsg);
		tvDialogMsg.setText("您确认退出吗？");
		Button btnDialogLeft = (Button) dialogWindow.findViewById(R.id.btnDialogLeft);
		btnDialogLeft.setText("再逛逛吧");
		btnDialogLeft.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();
				
			}
		});
		// 关闭对话框架
		Button btnDialogRight = (Button) dialogWindow.findViewById(R.id.btnDialogRight);
		btnDialogRight.setText("确认退出");
		btnDialogRight.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();
				AndroidUtil.exitApp(LoginActivity.this); // 退出
			}
		});
	}

	/**
	 * 登录
	 * 
	 * @author syghh
	 * 
	 */
	class LoginTask extends AsyncTask<Void, Void, JSONObject> {

		private String username;
		private String password;

		public LoginTask(String username, String password) {
			this.username = username;
			this.password = password;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(LoginActivity.this);
				pd.setMessage("正在登录...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().login(username, password);
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
						UserBean userBean = new UserBean(result.getJSONObject("user"));
						SharedPrefUtil.setUserBean(LoginActivity.this, userBean);
						long userId = userBean.getUserId();
						String access_token = userBean.getAccessToken();
						if (userBean.getIsKangaroo() == 1) {
							new ProfileKangarooId(userId, access_token).execute();
						} else {
							if (from.equals("back")) {
								finish();
							} else {
								startActivity(new Intent(LoginActivity.this, MainHomeActivityGroup.class));
								finish();
							}

						}
						Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(LoginActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(LoginActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
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
						SharedPrefUtil.setRooId(LoginActivity.this, rooId);
						if (from.equals("back")) {
							finish();
						} else {
							startActivity(new Intent(LoginActivity.this, RooSelfCenterActivity.class));
							finish();
						}

						Toast.makeText(LoginActivity.this, "获取成功", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(LoginActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(LoginActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(LoginActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void registerIntentReceivers() {
		receiver = new AuthReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(TAuthView.AUTH_BROADCAST);
		registerReceiver(receiver, filter);
	}

	private void unregisterIntentReceivers() {
		unregisterReceiver(receiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (pd != null) {
			pd.dismiss();
			pd = null;
		}
		if (receiver != null) {
			unregisterIntentReceivers();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// TODO
		if (requestCode == Constants.REQUEST_CODE_BIND_QQ) {
			if (resultCode == OAuthV2AuthorizeWebView.RESULT_CODE) {
				// 取得返回的OAuthV2类实例oAuth
				oAuth = (OAuthV2) data.getExtras().getSerializable("oauth");
				SharedPrefUtil.setQQInfo(this, oAuth.getAccessToken(), oAuth.getExpiresIn(), oAuth.getOpenid(), System.currentTimeMillis() + "");

				new ThirdLoginTask(oAuth.getOpenid(), QQ_LOGIN).execute();
			}
		}
		if (requestCode == Constants.REQUEST_CODE_BIND_WEIBO) {
			String uid = SharedPrefUtil.getWeiboUid(LoginActivity.this);
			new ThirdLoginTask(uid, SINA_LOGIN).execute();
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
				pd = new ProgressDialog(LoginActivity.this);
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
				e.printStackTrace();
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
						SharedPrefUtil.setUserBean(LoginActivity.this, userBean);
						long userId = userBean.getUserId();
						String access_token = userBean.getAccessToken();
						if (userBean.getIsKangaroo() == 1) {
							new ProfileKangarooId(userId, access_token).execute();
						} else {
							startActivity(new Intent(LoginActivity.this, MainHomeActivityGroup.class));
							finish();
						}
						Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
					} else if (status == 2) {// 第三方首次登陆,补充个人信息
						UserBean userBean = new UserBean(result.getJSONObject("user"));
						SharedPrefUtil.setUserBean(LoginActivity.this, userBean);
						Intent intent = new Intent(LoginActivity.this, RegisterNextActivity.class);
						intent.putExtra("thirdLogin", true);
						startActivity(intent);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}
	}

	public class AuthReceiver extends BroadcastReceiver {

		private static final String TAG = "AuthReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle exts = intent.getExtras();
			String raw = exts.getString("raw");
			final String access_token = exts.getString(TAuthView.ACCESS_TOKEN);
			final String expires_in = exts.getString(TAuthView.EXPIRES_IN);
			String error_ret = exts.getString(TAuthView.ERROR_RET);
			String error_des = exts.getString(TAuthView.ERROR_DES);
			Log.i(TAG, String.format("raw: %s, access_token:%s, expires_in:%s", raw, access_token, expires_in));
			if (access_token != null) {
				// mAccessToken = access_token;
				// ((TextView)findViewById(R.id.access_token)).setText(access_token);
				// // TDebug.msg("正在获取OpenID...", getApplicationContext());
				// if(!isFinishing())
				// {
				// showDialog(PROGRESS);
				// }
				// 用access token 来获取open id
				TencentOpenAPI.openid(access_token, new Callback() {
					@Override
					public void onSuccess(final Object obj) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// dismissDialog(PROGRESS);
								// setOpenIdText(((OpenId)obj).getOpenId());
								String openid = ((OpenId) obj).getOpenId();
								String access_curr_time = System.currentTimeMillis() + "";
								SharedPrefUtil.setQQConnectInfo(LoginActivity.this, access_token, expires_in, openid, access_curr_time);
								// Toast.makeText(LoginActivity.this, openid,
								// Toast.LENGTH_SHORT).show();
								new ThirdLoginTask(SharedPrefUtil.getQQConnectOpenid(LoginActivity.this), QQ_LOGIN).execute();
							}
						});
					}

					@Override
					public void onFail(int ret, final String msg) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// dismissDialog(PROGRESS);
								// TDebug.msg(msg, getApplicationContext());
								Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
							}
						});
					}
				});
			}
			if (error_ret != null) {
				Toast.makeText(LoginActivity.this, "获取access token失败" + "\n错误码: " + error_ret + "\n错误信息: " + error_des, Toast.LENGTH_SHORT).show();
			}
		}

	}
}

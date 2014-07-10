package com.elephant.ediyou.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.R;
import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.view.RenrenAuthListener;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.umeng.analytics.MobclickAgent;
import com.weibo.net.AccessToken;
import com.weibo.net.DialogError;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;

/**
 * 账号绑定
 * 
 * @author Aizhimin
 * 
 */
public class AccountBindActivity extends Activity implements OnClickListener {
	private static final String 	TAG = "AccountBindActivity";

	private Button 					btnLeft, btnRight;
	private ImageView 				ivTitle;
	private TextView 				tvTitle;

	private Button 					bind_sina_btn;
	private Button 					bind_qq_btn;
	private OAuthV2 				oAuth;
	private Button 					bind_renren_btn;
	private Renren 					renren;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_bind);
		findView();
		// 添加到容器中
		((CommonApplication) getApplicationContext()).addActivity(this);
	}

	private void findView() {
		btnLeft 		= (Button) findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight 		= (Button) findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		ivTitle 		= (ImageView) findViewById(R.id.ivTitle);
		ivTitle.setVisibility(View.GONE);
		tvTitle 		= (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("分享设置");
		btnLeft.setOnClickListener(this);

		bind_sina_btn 	= (Button) this.findViewById(R.id.bind_sina_btn);
		bind_sina_btn.setOnClickListener(this);

		bind_qq_btn 	= (Button) this.findViewById(R.id.bind_qq_btn);
		bind_qq_btn.setOnClickListener(this);

		bind_renren_btn = (Button) this.findViewById(R.id.bind_renren_btn);
		bind_renren_btn.setOnClickListener(this);
		renren 			= new Renren(Constants.RENREN_API_KEY, Constants.RENREN_SECRET_KEY, Constants.RENREN_APP_ID, this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		if (SharedPrefUtil.checkWeiboBind(AccountBindActivity.this)) {
			bind_sina_btn.setText(R.string.unbind);
			bind_sina_btn.setTextColor(Color.BLACK);
			bind_sina_btn.setBackgroundResource(R.drawable.bg_connect_btn_pressed);
		} else {
			bind_sina_btn.setText(R.string.account_bind);
			bind_sina_btn.setTextColor(getResources().getColor(R.color.text_green));
			bind_sina_btn.setBackgroundResource(R.drawable.bg_btn_nor);
		}
		if (SharedPrefUtil.checkQQBind(this)) {
			bind_qq_btn.setText(R.string.unbind);
			bind_qq_btn.setTextColor(Color.BLACK);
			bind_qq_btn.setBackgroundResource(R.drawable.bg_connect_btn_pressed);
		} else {
			bind_qq_btn.setText(R.string.account_bind);
			bind_qq_btn.setTextColor(getResources().getColor(R.color.text_green));
			bind_qq_btn.setBackgroundResource(R.drawable.bg_btn_nor);
		}
		if (renren.isAccessTokenValid() && renren.isSessionKeyValid()) {
			bind_renren_btn.setText(R.string.unbind);
			bind_renren_btn.setTextColor(Color.BLACK);
			bind_renren_btn.setBackgroundResource(R.drawable.bg_connect_btn_pressed);
		} else {
			bind_renren_btn.setText(R.string.account_bind);
			bind_renren_btn.setTextColor(getResources().getColor(R.color.text_green));
			bind_renren_btn.setBackgroundResource(R.drawable.bg_btn_nor);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			overridePendingTransition(R.anim.push_right_in_noalp_back, R.anim.push_left_out_noalp_back);
			break;
		case R.id.bind_sina_btn:// 绑定新浪微博
			if (SharedPrefUtil.getUserBean(this) != null) {
				if (SharedPrefUtil.checkWeiboBind(AccountBindActivity.this)) {
					AlertDialog.Builder ab = new AlertDialog.Builder(this);
					ab.setMessage(getString(R.string.unbind_promt));
					ab.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

						}
					});
					ab.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							SharedPrefUtil.clearWeiboBind(AccountBindActivity.this);
							bind_sina_btn.setText(R.string.account_bind);

							CookieSyncManager.createInstance(getApplicationContext());
							CookieManager.getInstance().removeAllCookie();
						}
					});
					ab.show();
				} else {
					if (NetUtil.checkNet(this)) {
						Intent authorizeIntent = new Intent(this, AuthorizeActivity.class);
						authorizeIntent.putExtra(Constants.EXTRA_BIND_FROM, Constants.BIND_WEIBO);
						startActivityForResult(authorizeIntent, Constants.REQUEST_CODE_BIND_WEIBO);
					} else {
						Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
					}
				}
			} else {
				Toast.makeText(this, R.string.no_login, Toast.LENGTH_LONG).show();
				startActivity(new Intent(this, LoginActivity.class).putExtra("back", "back"));
			}
			break;
		case R.id.bind_qq_btn:// qq绑定
			if (SharedPrefUtil.getUserBean(this) != null) {

				if (SharedPrefUtil.checkQQBind(this)) {
					AlertDialog.Builder ab = new AlertDialog.Builder(this);
					ab.setMessage(getString(R.string.unbind_promt));
					ab.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

						}
					});
					ab.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							SharedPrefUtil.clearQQBind(AccountBindActivity.this);
							bind_qq_btn.setText(R.string.account_bind);

							CookieSyncManager.createInstance(getApplicationContext());
							CookieManager.getInstance().removeAllCookie();

						}
					});
					ab.show();
				} else {
					if (NetUtil.checkNet(this)) {
						oAuth 			= new OAuthV2(Constants.TENCENT_REDIRECT_URL);
						oAuth.setClientId(Constants.TENCENT_APP_ID);
						oAuth.setClientSecret(Constants.TENCENT_APP_KEY);
						Intent intent 	= new Intent(this, OAuthV2AuthorizeWebView.class);
						intent.putExtra("oauth", oAuth);
						startActivityForResult(intent, TENCENT_OAUTH_REQUESTCODE);
					} else {
						Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
					}
				}
			} else {
				Toast.makeText(this, R.string.no_login, Toast.LENGTH_LONG).show();
				startActivity(new Intent(this, LoginActivity.class).putExtra("back", "back"));
			}
			break;
		case R.id.bind_renren_btn:// 人人
			if (SharedPrefUtil.getUserBean(this) != null) {

				if (renren == null)
					renren = new Renren(Constants.RENREN_API_KEY, Constants.RENREN_SECRET_KEY, Constants.RENREN_APP_ID, this);
				if (renren.isAccessTokenValid() && renren.isSessionKeyValid()) {
					AlertDialog.Builder ab = new AlertDialog.Builder(this);
					ab.setMessage(getString(R.string.unbind_promt));
					ab.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

						}
					});
					ab.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							renren.logout(AccountBindActivity.this);
							bind_renren_btn.setText(R.string.account_bind);
						}
					});
					ab.show();
				} else {
					if (NetUtil.checkNet(this)) {
						Intent authorizeIntent = new Intent(this, AuthorizeActivity.class);
						authorizeIntent.putExtra(Constants.EXTRA_BIND_FROM, Constants.BIND_RENREN);
						startActivityForResult(authorizeIntent, Constants.REQUEST_CODE_BIND_RENREN);
						// renren.authorize(this, null, listener,
						// RENREN_OAUTH_REQUESTCODE);
					} else {
						Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
					}
				}
			} else {
				Toast.makeText(this, R.string.no_login, Toast.LENGTH_LONG).show();
				startActivity(new Intent(this, LoginActivity.class).putExtra("back", "back"));
			}
			break;
		}
	}

	/**
	 * 新浪微博绑定回调
	 * 
	 * @author Aizhimin
	 * 
	 */
	public class AuthDialogListener implements WeiboDialogListener {
		@Override
		public void onComplete(Bundle values) {
			String sina_uid 		= values.getString("uid");
			String token 			= values.getString("access_token");
			String expires_in 		= values.getString("expires_in");
			String currTime 		= System.currentTimeMillis() + "";
			SharedPrefUtil.setWeiboInfo(AccountBindActivity.this, sina_uid, token, expires_in, currTime);
			AccessToken accessToken = new AccessToken(token, Constants.WEIBO_CONSUMER_SECRET);
			accessToken.setExpiresIn(expires_in);
			Weibo.getInstance().setAccessToken(accessToken);
			bind_sina_btn.setText(R.string.unbind);
			Toast.makeText(AccountBindActivity.this, R.string.account_bind_success, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onError(DialogError e) {
			Log.e(TAG, "Auth error : " + e.getMessage());
			Toast.makeText(AccountBindActivity.this, R.string.weibo_bind_faild, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCancel() {
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Log.e(TAG, "Auth exception : " + e.getMessage());
			Toast.makeText(AccountBindActivity.this, R.string.weibo_bind_faild, Toast.LENGTH_LONG).show();
		}
	}

	public static final int TENCENT_OAUTH_REQUESTCODE = 1;// 腾讯回调
	public static final int RENREN_OAUTH_REQUESTCODE = 2;// 人人回调

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TENCENT_OAUTH_REQUESTCODE) { // 对应之前设置的的myRequsetCode
			if (resultCode == OAuthV2AuthorizeWebView.RESULT_CODE) {
				// 取得返回的OAuthV2类实例oAuth
				oAuth 					= (OAuthV2) data.getExtras().getSerializable("oauth");
				String access_token 	= oAuth.getAccessToken();
				String expires_in		= oAuth.getExpiresIn();
				String openid 			= oAuth.getOpenid();
				String access_curr_time = System.currentTimeMillis() + "";
				SharedPrefUtil.setQQInfo(AccountBindActivity.this, access_token, expires_in, openid, access_curr_time);
				bind_qq_btn.setText(R.string.unbind);
				Toast.makeText(AccountBindActivity.this, R.string.account_bind_success, Toast.LENGTH_LONG).show();
			}
		}

		if (resultCode == RESULT_OK) {
			if (requestCode == Constants.REQUEST_CODE_BIND_RENREN) {
				if (renren != null) {
					renren.authorizeCallback(requestCode, resultCode, data);
				}
				bind_renren_btn.setText(R.string.unbind);
				Toast.makeText(AccountBindActivity.this, R.string.account_bind_success, Toast.LENGTH_LONG).show();
			} else if (requestCode == Constants.REQUEST_CODE_BIND_WEIBO) {
				bind_sina_btn.setText(R.string.unbind);
			}
		}
	}

	final RenrenAuthListener listener = new RenrenAuthListener() {

		@Override
		public void onComplete(Bundle values) {
			// Log.d("test",values.toString());
			bind_renren_btn.setText(R.string.unbind);
			Toast.makeText(AccountBindActivity.this, R.string.account_bind_success, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
			Toast.makeText(AccountBindActivity.this, R.string.weibo_bind_faild, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancelLogin() {
		}

		@Override
		public void onCancelAuth(Bundle values) {
		}

	};

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
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}

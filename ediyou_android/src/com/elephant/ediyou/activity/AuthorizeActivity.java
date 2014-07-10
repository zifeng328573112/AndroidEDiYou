package com.elephant.ediyou.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.R;
import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.Util;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.view.RenrenAuthListener;
import com.renren.api.connect.android.view.RenrenDialogListener;
import com.umeng.analytics.MobclickAgent;
import com.weibo.net.AccessToken;
import com.weibo.net.DialogError;
import com.weibo.net.Oauth2AccessTokenHeader;
import com.weibo.net.RequestToken;
import com.weibo.net.Token;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;
/**
 * 授权界面
 * @author Aizhimin
 *
 */
public class AuthorizeActivity extends Activity {

	
	private final static String 			TAG = "AuthorizeActivity";
	
	//-------------------------sina_weibo start-------------------------------------//
	private RequestToken 					mRequestToken = null;
	private Token 							mAccessToken = null;
	private String 							mRedirectUrl;
	private static String 					WEIBO_APP_KEY;
    private static String 					WEIBO_APP_SECRET;
    
    public static final String 				TOKEN = "access_token";
    public static final String 				EXPIRES = "expires_in";
    public static String 					URL_OAUTH2_ACCESS_AUTHORIZE = "https://api.weibo.com/oauth2/authorize";
    
    private WeiboDialogListener 			mAuthDialogListener;
    private WeiboDialogListener 			mListener;
    
  //-------------------------sina_weibo end-------------------------------------//
    
  //-------------------------renren_weibo start---------------------------------//
	RenrenDialogListener 					mRenrenListener;
	private boolean 						isPost = false;
  //-------------------------renren_weibo end-----------------------------------//  
    WebView 								mWebView;
    private String 							mUrl;//dialogUrl
    
    private View 							progress;
	
	private ProgressDialog 					mSpinner;
	
	String 									fromType;
    
    private static final int 				DEFAULT_AUTH_ACTIVITY_CODE = 32973;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authorize);
		
		findView();
		fillData();
		//添加到容器中
		((CommonApplication)getApplicationContext()).addActivity(this);
        
	}

	private void fillData() {
		fromType = getIntent().getStringExtra(Constants.EXTRA_BIND_FROM);
		if(Constants.BIND_WEIBO.equals(fromType)){
			authorizeWeibo();
		}
		if(Constants.BIND_RENREN.equals(fromType)){
			authorizeRenren();
		}
	}

	private void findView() {
		mWebView 	= (WebView) findViewById(R.id.weibo_webview);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        
        progress 	= this.findViewById(R.id.progress);
		progress.setVisibility(View.GONE);
		
		mSpinner 	= new ProgressDialog(this);
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("加载中...");
		
	}
	//--------------------------------------sina_weibo_start----------------------------//
	public void authorizeWeibo(){
		//init
		Utility.setRequestHeader("Accept-Encoding", "gzip");
        Utility.setTokenObject(this.mRequestToken);
        mRedirectUrl 		= Constants.WEIBO_REDIRECT_URL;
        WEIBO_APP_KEY 		= Constants.WEIBO_CONSUMER_KEY;
        WEIBO_APP_SECRET 	= Constants.WEIBO_CONSUMER_SECRET;
        authorize(AuthorizeActivity.this, new String[] {}, DEFAULT_AUTH_ACTIVITY_CODE, new AuthDialogListener());
        
	}
	
    private void authorize(Activity activity, String[] permissions, int activityCode, final WeiboDialogListener listener) {
        Utility.setAuthorization(new Oauth2AccessTokenHeader());

        boolean singleSignOnStarted = false;
        mAuthDialogListener = listener;

        // Prefer single sign-on, where available.
        if (activityCode >= 0) {
            singleSignOnStarted = startSingleSignOn(activity, WEIBO_APP_KEY, permissions, activityCode);
        }
        // Otherwise fall back to traditional dialog.
        if (!singleSignOnStarted) {
            startDialogAuth(activity, permissions);
        }

    }
	
    
    private boolean startSingleSignOn(Activity activity, String applicationId, String[] permissions, int activityCode) {
        return false;
    }
    
    private void startDialogAuth(Activity activity, String[] permissions) {
        WeiboParameters params = new WeiboParameters();
        if (permissions.length > 0) {
            params.add("scope", TextUtils.join(",", permissions));
        }
        CookieSyncManager.createInstance(activity);
        dialog(activity, params, new WeiboDialogListener() {

            public void onComplete(Bundle values) {
                // ensure any cookies set by the dialog are saved
                CookieSyncManager.getInstance().sync();
                if (null == mAccessToken) {
                    mAccessToken = new Token();
                }
                mAccessToken.setToken(values.getString(TOKEN));
                mAccessToken.setExpiresIn(values.getString(EXPIRES));
                if (isSessionValid()) {
                    Log.d("Weibo-authorize", "Login Success! access_token=" + mAccessToken.getToken() + " expires=" + mAccessToken.getExpiresIn());
                    mAuthDialogListener.onComplete(values);
                } else {
                    Log.d("Weibo-authorize", "Failed to receive access token");
                    mAuthDialogListener.onWeiboException(new WeiboException("Failed to receive access token."));
                }
            }

            public void onError(DialogError error) {
                Log.d("Weibo-authorize", "Login failed: " + error);
                mAuthDialogListener.onError(error);
            }

            public void onWeiboException(WeiboException error) {
                Log.d("Weibo-authorize", "Login failed: " + error);
                mAuthDialogListener.onWeiboException(error);
            }

            public void onCancel() {
                Log.d("Weibo-authorize", "Login canceled");
                mAuthDialogListener.onCancel();
            }
        });
    }
    
    public void dialog(Context context, WeiboParameters parameters, final WeiboDialogListener listener) {
        parameters.add("client_id", WEIBO_APP_KEY);
        parameters.add("response_type", "token");
        parameters.add("redirect_uri", mRedirectUrl);
        parameters.add("display", "mobile");

        if (isSessionValid()) {
            parameters.add(TOKEN, mAccessToken.getToken());
        }
        String url = URL_OAUTH2_ACCESS_AUTHORIZE + "?" + Utility.encodeUrl(parameters);
        if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Utility.showAlert(context, "Error", "Application requires permission to access the Internet");
        } else {
        	mUrl = url;
        	mListener = listener;
        	mWebView.setWebViewClient(new WeiboWebViewClient());
        	mWebView.loadUrl(mUrl);
        }
    }
    
    public boolean isSessionValid() {
        if (mAccessToken != null) {
            return (!TextUtils.isEmpty(mAccessToken.getToken()) && (mAccessToken.getExpiresIn() == 0 || (System.currentTimeMillis() < mAccessToken.getExpiresIn())));
        }
        return false;
    }
    
	/**
	 * 微博验证
	 * @author Aizhimin
	 *
	 */
	class AuthDialogListener implements WeiboDialogListener {
		@Override
		public void onComplete(Bundle values) {
			String sina_uid 		= values.getString("uid");
			String token 			= values.getString("access_token");
			String expires_in 		= values.getString("expires_in");
			String currTime 		= System.currentTimeMillis()+"";
			SharedPrefUtil.setWeiboInfo(AuthorizeActivity.this,sina_uid, token, expires_in, currTime);
			AccessToken accessToken = new AccessToken(token, Constants.WEIBO_CONSUMER_SECRET);
			accessToken.setExpiresIn(expires_in);
			Weibo.getInstance().setAccessToken(accessToken);
			setResult(RESULT_OK);
			AuthorizeActivity.this.finish();
			Toast.makeText(AuthorizeActivity.this, R.string.account_bind_success, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onError(DialogError e) {
			Log.e(TAG,  "Auth error : " + e.getMessage());
			Toast.makeText(AuthorizeActivity.this, R.string.weibo_bind_faild, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel() {
			AuthorizeActivity.this.finish();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Log.e(TAG, "Auth exception : " + e.getMessage());
		}

	}
	
	private class WeiboWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "Redirect URL: " + url);
            // 待后台增加对默认重定向地址的支持后修改下面的逻辑
            if (url.startsWith(mRedirectUrl)) {
                handleRedirectUrl(view, url);
                return true;
            }
            // launch non-dialog URLs in a full browser
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            mListener.onError(new DialogError(description, errorCode, failingUrl));
//            WeiboDialog.this.dismiss();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "onPageStarted URL: " + url);
            // google issue. shouldOverrideUrlLoading not executed
            if (url.startsWith(mRedirectUrl)) {
                handleRedirectUrl(view, url);
                view.stopLoading();
                return;
            }
            super.onPageStarted(view, url, favicon);
            mSpinner.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "onPageFinished URL: " + url);
            super.onPageFinished(view, url);
//            progress.setVisibility(View.GONE);
            if(mSpinner!=null && mSpinner.isShowing()){
            	mSpinner.dismiss();
            }

            // mBtnClose.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.VISIBLE);
        }

        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

    }
	
	private void handleRedirectUrl(WebView view, String url) {
        Bundle values = Utility.parseUrl(url);

        String error = values.getString("error");
        String error_code = values.getString("error_code");

        if (error == null && error_code == null) {
            mListener.onComplete(values);
        } else if (error.equals("access_denied")) {
            // 用户或授权服务器拒绝授予数据访问权限
            mListener.onCancel();
        } else {
            mListener.onWeiboException(new WeiboException(error, Integer.parseInt(error_code)));
        }
    }
	
	//--------------------------------------sina_weibo_end----------------------------//
	
	//--------------------------------------renren_weibo_start------------------------//
	
	public void authorizeRenren(){
		CustomRenren renren = new CustomRenren(Constants.RENREN_API_KEY, Constants.RENREN_SECRET_KEY, Constants.RENREN_APP_ID, this);
		String[] permissions = { "publish_feed", "create_album", "photo_upload", "read_user_album", "status_update" };
		renren.authorize(AuthorizeActivity.this, permissions, listener, Constants.REQUEST_CODE_BIND_RENREN);
//		renren.authorize(AuthorizeActivity.this, null, listener);
	}

	class CustomRenren extends Renren{
		
		public String apiKey;
		
		public final String[] CUSTOM_DEFAULT_PERMISSIONS = { "publish_feed", "create_album", "photo_upload", "read_user_album", "status_update" };

		public CustomRenren(Parcel in) {
			super(in);
		}
		
		public CustomRenren(String apiKey, String secret, String appId, Context context){
			super(apiKey, secret, appId, context);
			this.apiKey = apiKey;
			Log.i(TAG, "CusromRenren-->CustomRenren");
		}
		
		
		public void authorize(Activity activity, String[] permissions, final RenrenDialogListener listener, String redirectUrl, String responseType) {
			Log.i(TAG, "CusromRenren-->authorize");
			// 调用CookieManager.getInstance之前
			// 必须先调用CookieSyncManager.createInstance
			CookieSyncManager.createInstance(activity);

			Bundle params = new Bundle();
			params.putString("client_id", apiKey);
			params.putString("redirect_uri", redirectUrl);
			params.putString("response_type", responseType);
			params.putString("display", "touch");
			
			//若开发者提供的权限列表为空，则使用默认权限列表
			if(permissions == null) {
				permissions = CUSTOM_DEFAULT_PERMISSIONS;
			}
			
			if (permissions != null && permissions.length > 0) {
				String scope = TextUtils.join(" ", permissions);
				params.putString("scope", scope);
			}

			String url = AUTHORIZE_URL + "?" + Util.encodeUrl(params);
			if (activity.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
				Util.showAlert(activity, "没有权限", "应用需要访问互联网的权限");
			} else {
				mUrl = url;
				mRenrenListener = listener;
				mWebView.setWebViewClient(new RenrenWebViewClient());
				if (isPost) {
//					mWebView.postUrl(mUrl, EncodingUtils.getBytes(mPostData, "BASE64"));
				} else {
					mWebView.loadUrl(mUrl);
				}
			}
		}
		
		private class RenrenWebViewClient extends WebViewClient {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.d(TAG, "Redirect URL: " + url);
				int b = mRenrenListener.onPageBegin(url);
				switch (b) {
				case RenrenDialogListener.ACTION_PROCCESSED:
//					RenrenDialog.this.dismiss();
					return true;
				case RenrenDialogListener.ACTION_DIALOG_PROCCESS:
					return false;
				}
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				Log.i(TAG, "Webview loading URL: " + url);
				boolean b = mRenrenListener.onPageStart(url);
				if (b) {
					view.stopLoading();
//					RenrenDialog.this.dismiss();
					AuthorizeActivity.this.finish();
					return;
				}
				super.onPageStarted(view, url, favicon);
				mSpinner.show();
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				mRenrenListener.onReceivedError(errorCode, description, failingUrl);
				mSpinner.hide();
//				RenrenDialog.this.dismiss();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				mRenrenListener.onPageFinished(url);
//				if (showTitle) {
//					String t = view.getTitle();
//					if (t != null && t.length() > 0) {
//						title.setText(t);
//					}
//				}
				mSpinner.hide();
			}
		}
		
	}
	
	RenrenAuthListener listener = new RenrenAuthListener() {

		@Override
		public void onComplete(Bundle values) {
//			Log.d("test",values.toString());
			setResult(RESULT_OK);
			Toast.makeText(AuthorizeActivity.this, R.string.account_bind_success, Toast.LENGTH_SHORT).show();
			AuthorizeActivity.this.finish();
		}

		@Override
		public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
			Toast.makeText(AuthorizeActivity.this, R.string.weibo_bind_faild, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancelLogin() {
			AuthorizeActivity.this.finish();
		}

		@Override
		public void onCancelAuth(Bundle values) {
		}
		
	};

	
	//--------------------------------------renren_weibo_end--------------------------//
	
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
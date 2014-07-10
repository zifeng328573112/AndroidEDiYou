package com.elephant.ediyou.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.elephant.ediyou.R;
import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.common.ResponseBean;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.photos.PhotoUploadRequestParam;
import com.renren.api.connect.android.status.StatusSetRequestParam;
import com.renren.api.connect.android.view.RenrenAuthListener;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.tencent.weibo.api.TAPI;
import com.tencent.weibo.constants.OAuthConstants;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.umeng.analytics.MobclickAgent;
import com.weibo.net.AccessToken;
import com.weibo.net.DialogError;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

/**
 * sns分享
 * 
 * @author Aizhimin
 * 
 */
public class SnsShareActivity extends Activity implements IBaseActivity, OnClickListener {
	private static final String 		TAG = "SnsShareActivity";

	private Button 						btnLeft;
	private ImageView 					ivTitle;
	private TextView 					tvTitle;
	private Button 						btnRight;

	public int 							shareType;// 分享平台
	public int 							resType;// 分享内容的类型
	public String 						localFilePath;// 本地文件地址
	public String 						defaultContent;// 默认分享内容
	private String 						tempFileStr;// 临时文件地址；

	private ProgressDialog 				pd;
	private EditText 					weibo_post_et;// 微博内容

	private IWXAPI 						wxApi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sns_share);
		wxApi 			= WXAPIFactory.createWXAPI(this, Constants.WEIXIN_APP_ID);
		wxApi.registerApp(Constants.WEIXIN_APP_ID);
		shareType 		= getIntent().getIntExtra(Constants.EXTRA_SHARE_TYPE, -1);
		resType 		= getIntent().getIntExtra(Constants.EXTRA_RES_TYPE, -1);
		localFilePath 	= getIntent().getStringExtra(Constants.EXTRA_IMAGE_URL);
		defaultContent 	= getIntent().getStringExtra(Constants.EXTRA_SHARE_CONTENT);
		findView();
		fillData();
		// 将Activity添加到容器
		((CommonApplication) getApplicationContext()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft 		= (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setOnClickListener(this);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		tvTitle 		= (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		btnRight 		= (Button) this.findViewById(R.id.btnRight);
		btnRight.setOnClickListener(this);
		btnRight.setBackgroundResource(R.drawable.ic_submit_selector);

		weibo_post_et 	= (EditText) this.findViewById(R.id.weibo_post_et);

	}

	@Override
	public void fillData() {
		if (shareType == Constants.SHARE_TO_WEIBO) {
			tvTitle.setText("分享到新浪微博");
		} else if (shareType == Constants.SHARE_TO_QQ) {
			tvTitle.setText("分享到腾讯微博");
		} else if (shareType == Constants.SHARE_TO_RENREN) {
			tvTitle.setText("分享到人人网");
		} else if (shareType == Constants.SHARE_TO_WEIXIN) {
			tvTitle.setText("分享到微信");

		} else if (shareType == Constants.SHARE_TO_WEIXIN_FRS) {
			tvTitle.setText("分享到微信朋友圈");
		}

		if (!StringUtil.isBlank(defaultContent)) {
			weibo_post_et.setText(defaultContent);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:// 返回
			finish();
			break;
		case R.id.btnRight:// 提交
			if (!NetUtil.checkNet(this)) {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
				return;
			}
			tempFileStr = Environment.getExternalStorageDirectory() + "/" + Constants.APP_DIR_NAME + "/" + "temp.jpg";
			try {
				Bitmap src 			= ImageUtil.revitionImageSize2(localFilePath, this, 480);
				Bitmap waterMark 	= ImageUtil.drawableToBitmap(getResources().getDrawable(R.drawable.water_marker));
				src 				= ImageUtil.watermarkBitmap(src, waterMark);
				File tempFile 		= new File(tempFileStr);
				if (tempFile.exists()) {
					tempFile.delete();
				}
				FileOutputStream out = new FileOutputStream(tempFile, false);
				if (src.compress(CompressFormat.JPEG, 100, out)) {
					out.flush();
					out.close();
				}
			} catch (IOException e) {
			}
			if (shareType == Constants.SHARE_TO_WEIBO) {
				shareToWeibo();
			} else if (shareType == Constants.SHARE_TO_QQ) {
				shareToQQ();
			} else if (shareType == Constants.SHARE_TO_RENREN) {
				shareToRenren();
			} else if (shareType == Constants.SHARE_TO_WEIXIN) {
				shareToWeixin();
			} else if (shareType == Constants.SHARE_TO_WEIXIN_FRS) {
				shareToWeixinFrs();
			}
			break;
		}
	}

	private void shareToWeixin() {

		// SendAuth.Req req = new SendAuth.Req();
		// req.scope = "post_timeline";
		// req.state = "none";
		// wxApi.sendReq(req);

		String text 			= weibo_post_et.getText().toString();
		WXTextObject textObj 	= new WXTextObject();
		textObj.text 			= text;

		// 用WXTextObject对象初始化一个WXMediaMessage对象
		WXMediaMessage msg 		= new WXMediaMessage();
		msg.mediaObject 		= textObj;
		// 发送文本类型的消息时，title字段不起作用
		// msg.title = "Will be ignored";
		msg.description 		= text;

		// 构造一个Req
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction 		= buildTransaction("text"); // transaction字段用于唯一标识一个请求
		req.message 			= msg;
		// req.scene = isTimelineCb.isChecked() ?
		// SendMessageToWX.Req.WXSceneTimeline :
		// SendMessageToWX.Req.WXSceneSession;
		req.scene 				= SendMessageToWX.Req.WXSceneSession;
		// 调用api接口发送数据到微信
		boolean bool 			= wxApi.sendReq(req);
		System.out.println("-------------bool:" + bool);
		wxApi.openWXApp();
		finish();
	}

	private void shareToWeixinFrs() {

		// SendAuth.Req req = new SendAuth.Req();
		// req.scope = "post_timeline";
		// req.state = "none";
		// wxApi.sendReq(req);

		String text 			= weibo_post_et.getText().toString();
		WXTextObject textObj 	= new WXTextObject();
		textObj.text 			= text;

		// 用WXTextObject对象初始化一个WXMediaMessage对象
		WXMediaMessage msg 		= new WXMediaMessage();
		msg.mediaObject 		= textObj;
		// 发送文本类型的消息时，title字段不起作用
		// msg.title = "Will be ignored";
		msg.description 		= text;

		// 构造一个Req
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction 		= buildTransaction("text"); // transaction字段用于唯一标识一个请求
		req.message 			= msg;
		// req.scene = isTimelineCb.isChecked() ?
		// SendMessageToWX.Req.WXSceneTimeline :
		// SendMessageToWX.Req.WXSceneSession;
		req.scene 				= SendMessageToWX.Req.WXSceneTimeline;
		// 调用api接口发送数据到微信
		boolean bool 			= wxApi.sendReq(req);
		System.out.println("-------------bool:" + bool);
		finish();
	}

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

	/**
	 * 分享（文字微博）到新浪微博
	 * 
	 * @param context
	 * @param content
	 */
	public static void shareSinaContent(Context context, String content) {
		Intent intent = new Intent(context, SnsShareActivity.class);
		intent.putExtra(Constants.EXTRA_RES_TYPE, Constants.SHARE_RES_CONTENT);
		intent.putExtra(Constants.EXTRA_SHARE_CONTENT, content);
		intent.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_WEIBO);
		context.startActivity(intent);
	}

	/**
	 * 分享（图片微博）到新浪微博
	 * 
	 * @param context
	 * @param content
	 * @param imageUrl
	 */
	public static void shareSinaImage(Context context, String content, String imageUrl) {
		Intent intent = new Intent(context, SnsShareActivity.class);
		intent.putExtra(Constants.EXTRA_RES_TYPE, Constants.SHARE_RES_IMAGE);
		intent.putExtra(Constants.EXTRA_IMAGE_URL, imageUrl);
		intent.putExtra(Constants.EXTRA_SHARE_CONTENT, content);
		intent.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_WEIBO);
		context.startActivity(intent);
	}

	/**
	 * 分享（文字微博）到QQ
	 * 
	 * @param context
	 * @param content
	 */
	public static void shareQQContent(Context context, String content) {
		Intent intent = new Intent(context, SnsShareActivity.class);
		intent.putExtra(Constants.EXTRA_RES_TYPE, Constants.SHARE_RES_CONTENT);
		intent.putExtra(Constants.EXTRA_SHARE_CONTENT, content);
		intent.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_QQ);
		context.startActivity(intent);
	}

	/**
	 * 分享（图片微博）到QQ
	 * 
	 * @param context
	 * @param content
	 * @param imageUrl
	 */
	public static void shareQQImage(Context context, String content, String imageUrl) {
		Intent intent = new Intent(context, SnsShareActivity.class);
		intent.putExtra(Constants.EXTRA_RES_TYPE, Constants.SHARE_RES_IMAGE);
		intent.putExtra(Constants.EXTRA_IMAGE_URL, imageUrl);
		intent.putExtra(Constants.EXTRA_SHARE_CONTENT, content);
		intent.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_QQ);
		context.startActivity(intent);
	}

	/**
	 * 分享（文字微博）到人人
	 * 
	 * @param context
	 * @param content
	 */
	public static void shareRenrenContent(Context context, String content) {
		Intent intent = new Intent(context, SnsShareActivity.class);
		intent.putExtra(Constants.EXTRA_RES_TYPE, Constants.SHARE_RES_CONTENT);
		intent.putExtra(Constants.EXTRA_SHARE_CONTENT, content);
		intent.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_RENREN);
		context.startActivity(intent);
	}

	/**
	 * 分享（图片微博）到人人
	 * 
	 * @param context
	 * @param content
	 * @param imageUrl
	 */
	public static void shareRenrenImage(Context context, String content, String imageUrl) {
		Intent intent = new Intent(context, SnsShareActivity.class);
		intent.putExtra(Constants.EXTRA_RES_TYPE, Constants.SHARE_RES_IMAGE);
		intent.putExtra(Constants.EXTRA_IMAGE_URL, imageUrl);
		intent.putExtra(Constants.EXTRA_SHARE_CONTENT, content);
		intent.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_RENREN);
		context.startActivity(intent);
	}

	// --------------------------------------------新浪微博接口---------------------------------
	/**
	 * 分享到微博
	 */
	public void shareToWeibo() {
		if (SharedPrefUtil.checkWeiboBind(this)) {
			postTo(Constants.SHARE_TO_WEIBO);
		} else {
			Intent authorizeIntent = new Intent(this, AuthorizeActivity.class);
			authorizeIntent.putExtra(Constants.EXTRA_BIND_FROM, Constants.BIND_WEIBO);
			startActivityForResult(authorizeIntent, Constants.REQUEST_CODE_BIND_WEIBO);
			// Weibo weibo = Weibo.getInstance();
			// weibo.setupConsumerConfig(Constants.WEIBO_CONSUMER_KEY,
			// Constants.WEIBO_CONSUMER_SECRET);
			// weibo.setRedirectUrl(Constants.WEIBO_REDIRECT_URL);
			// weibo.authorize(SnsShareActivity.this,new AuthDialogListener());
		}
	}

	/**
	 * 发送微博
	 */
	private void postTo(int shareType) {
		String status = weibo_post_et.getText().toString().trim() + "http://www.ediyou.cn";
		if (StringUtil.countStringLength(status, 140)) {
			Toast.makeText(this, R.string.weibo_content_too_long, Toast.LENGTH_LONG).show();
			return;
		}
		if (NetUtil.checkNet(this)) {
			if (pd == null) {
				pd = new ProgressDialog(this);
				pd.setMessage(getString(R.string.publish));
			}
			pd.show();
			if (shareType == Constants.SHARE_TO_WEIBO) {
				new WeiboPostTask(status, tempFileStr).execute();
				// 统计分享
				MobclickAgent.onEvent(this, "share_to_sina");
			} else if (shareType == Constants.SHARE_TO_QQ) {
				new QQPostTask(status, tempFileStr).execute();
				// 统计分享
				MobclickAgent.onEvent(this, "share_to_qq");
			} else if (shareType == Constants.SHARE_TO_RENREN) {
				new RenrenPostTask(status, tempFileStr).execute();
				// 统计分享
				MobclickAgent.onEvent(this, "share_to_renren");
			}

		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 微博验证
	 * 
	 * @author Aizhimin
	 * 
	 */
	class AuthDialogListener implements WeiboDialogListener {
		@Override
		public void onComplete(Bundle values) {
			String sina_uid 		= values.getString("uid");
			String token 			= values.getString("access_token");
			String expires_in 		= values.getString("expires_in");
			String currTime 		= System.currentTimeMillis() + "";
			SharedPrefUtil.setWeiboInfo(SnsShareActivity.this, sina_uid, token, expires_in, currTime);
			AccessToken accessToken = new AccessToken(token, Constants.WEIBO_CONSUMER_SECRET);
			accessToken.setExpiresIn(expires_in);
			Weibo.getInstance().setAccessToken(accessToken);
			postTo(Constants.SHARE_TO_WEIBO);
		}

		@Override
		public void onError(DialogError e) {
			Log.e(TAG, "Auth error : " + e.getMessage());
			Toast.makeText(SnsShareActivity.this, R.string.weibo_bind_faild, Toast.LENGTH_LONG).show();
			// Toast.makeText(getApplicationContext(),"Auth error : " +
			// e.getMessage(), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCancel() {
			// Toast.makeText(getApplicationContext(),
			// "Auth cancel",Toast.LENGTH_LONG).show();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Log.e(TAG, "Auth exception : " + e.getMessage());
			Toast.makeText(SnsShareActivity.this, R.string.weibo_bind_faild, Toast.LENGTH_LONG).show();
			// Toast.makeText(getApplicationContext(),"Auth exception : " +
			// e.getMessage(), Toast.LENGTH_LONG).show();
		}

	}

	int exceptionCode = -1;

	/**
	 * 发布微博
	 * 
	 * @author Aizhimin
	 * 
	 */
	class WeiboPostTask extends AsyncTask<Void, Void, JSONObject> {
		public static final String status_update_url = "https://api.weibo.com/2/statuses/update.json";// 文字微博
		public static final String image_upload_url = "https://upload.api.weibo.com/2/statuses/upload.json";// 图片微博
		String status;
		String file;

		public WeiboPostTask(String status, String file) {
			this.status = status;
			this.file = file;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			Weibo weibo = Weibo.getInstance();
			weibo.setupConsumerConfig(Constants.WEIBO_CONSUMER_KEY, Constants.WEIBO_CONSUMER_SECRET);
			AccessToken accessToken = new AccessToken(SharedPrefUtil.getWeiboAccessToken(SnsShareActivity.this), Constants.WEIBO_CONSUMER_SECRET);
			accessToken.setExpiresIn(SharedPrefUtil.getWeiboExpiresIn(SnsShareActivity.this));
			weibo.setAccessToken(accessToken);

			WeiboParameters bundle = new WeiboParameters();
			bundle.add("source", Weibo.getAppKey());
			if (StringUtil.isBlank(status)) {
				status = getString(R.string.weibo_repost);
			}
			bundle.add("status", status);
			String url = null;
			if (resType == Constants.SHARE_RES_IMAGE) {
				bundle.add("pic", file);
				url = image_upload_url;
			} else {
				url = status_update_url;
			}
			try {
				String wangyuexin = weibo.request(SnsShareActivity.this, url, bundle, Utility.HTTPMETHOD_POST, weibo.getAccessToken());
				return new JSONObject(wangyuexin);
			} catch (WeiboException e) {
				MobclickAgent.reportError(SnsShareActivity.this, e.getMessage());
				if (e.getStatusCode() == 21301) {// 认证失败
					exceptionCode = 21301;
					return null;
				} else if (e.getStatusCode() == 20019 || e.getStatusCode() == 20017) {
					exceptionCode = e.getStatusCode();
					return null;
				}
			} catch (JSONException e) {
				MobclickAgent.reportError(SnsShareActivity.this, e.getMessage());
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
					if (!result.isNull("id")) {
						Toast.makeText(SnsShareActivity.this, R.string.publish_success, Toast.LENGTH_LONG).show();
						SnsShareActivity.this.finish();
					} else {
						Toast.makeText(SnsShareActivity.this, R.string.publish_fail, Toast.LENGTH_LONG).show();
					}
				} catch (NotFoundException e) {
					MobclickAgent.reportError(SnsShareActivity.this, e.getMessage());
					Toast.makeText(SnsShareActivity.this, R.string.publish_fail, Toast.LENGTH_LONG).show();
				}
			} else {
				if (exceptionCode == 21301) {
					Intent authorizeIntent = new Intent(SnsShareActivity.this, AuthorizeActivity.class);
					authorizeIntent.putExtra(Constants.EXTRA_BIND_FROM, Constants.BIND_WEIBO);
					startActivityForResult(authorizeIntent, Constants.REQUEST_CODE_BIND_WEIBO);

					// Weibo weibo = Weibo.getInstance();
					// weibo.setupConsumerConfig(Constants.WEIBO_CONSUMER_KEY,
					// Constants.WEIBO_CONSUMER_SECRET);
					// weibo.setRedirectUrl(Constants.WEIBO_REDIRECT_URL);
					// weibo.authorize(SnsShareActivity.this, new
					// AuthDialogListener());
					Toast.makeText(SnsShareActivity.this, R.string.weibo_auth_faild, Toast.LENGTH_LONG).show();
				} else if (exceptionCode == 20019 || exceptionCode == 20017) {
					Toast.makeText(SnsShareActivity.this, R.string.weibo_repeat_content, Toast.LENGTH_LONG).show();
				}
			}

			// Toast.makeText(SnsShareActivity.this, "exceptionCode:" +
			// exceptionCode, Toast.LENGTH_LONG).show();
		}
	}

	// --------------------------腾讯微博接口------------------------------------
	private OAuthV2 oAuth;

	private void shareToQQ() {
		if (SharedPrefUtil.checkQQBind(this)) {
			postTo(Constants.SHARE_TO_QQ);
		} else {
			oAuth = new OAuthV2(Constants.TENCENT_REDIRECT_URL);
			oAuth.setClientId(Constants.TENCENT_APP_ID);
			oAuth.setClientSecret(Constants.TENCENT_APP_KEY);
			Intent intent = new Intent(this, OAuthV2AuthorizeWebView.class);
			intent.putExtra("oauth", oAuth);
			startActivityForResult(intent, TENCENT_OAUTH_REQUESTCODE);
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
				String expires_in 		= oAuth.getExpiresIn();
				String openid 			= oAuth.getOpenid();
				String access_curr_time = System.currentTimeMillis() + "";
				SharedPrefUtil.setQQInfo(SnsShareActivity.this, access_token, expires_in, openid, access_curr_time);
				postTo(Constants.SHARE_TO_QQ);
			}
		}

		if (resultCode == RESULT_OK) {
			if (requestCode == Constants.REQUEST_CODE_BIND_WEIBO) {
				postTo(Constants.SHARE_TO_WEIBO);
			} else if (requestCode == Constants.REQUEST_CODE_BIND_RENREN) {
				if (renren != null) {
					renren.authorizeCallback(requestCode, resultCode, data);
				}
				postTo(Constants.SHARE_TO_RENREN);
			}
		}
	}

	/**
	 * 发布腾讯微博
	 * 
	 * @author Aizhimin
	 * 
	 */
	class QQPostTask extends AsyncTask<Void, Void, JSONObject> {
		String status;
		String file;

		public QQPostTask(String status, String file) {
			this.status = status;
			this.file = file;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			TAPI tAPI = new TAPI(OAuthConstants.OAUTH_VERSION_2_A);
			try {
				if (oAuth == null) {
					oAuth = new OAuthV2(Constants.TENCENT_REDIRECT_URL);
					oAuth.setClientId(Constants.TENCENT_APP_ID);
					oAuth.setClientSecret(Constants.TENCENT_APP_KEY);
					oAuth.setAccessToken(SharedPrefUtil.getQQAccessToken(SnsShareActivity.this));
					oAuth.setOpenid(SharedPrefUtil.getQQOpenid(SnsShareActivity.this));
				}
				String response = null;
				if (resType == Constants.SHARE_RES_IMAGE) {
					response = tAPI.addPic(oAuth, "json", status, NetUtil.getLocalIpAddress(), file);
				} else {
					response = tAPI.add(oAuth, "json", status, NetUtil.getLocalIpAddress());
				}
				return new JSONObject(response);
			} catch (Exception e) {
				MobclickAgent.reportError(SnsShareActivity.this, e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (pd != null)
				pd.dismiss();
			if (result != null) {
				// Log.e(TAG, "response:"+result);
				try {
					if (!result.isNull("ret") && result.getInt("ret") == 0) {
						Toast.makeText(SnsShareActivity.this, R.string.publish_success, Toast.LENGTH_LONG).show();
						SnsShareActivity.this.finish();
					} else if (result.getInt("ret") == 1) {
						Toast.makeText(SnsShareActivity.this, "参数错误", Toast.LENGTH_LONG).show();
					} else if (result.getInt("ret") == 2) {
						Toast.makeText(SnsShareActivity.this, "频率受限", Toast.LENGTH_LONG).show();
					} else if (result.getInt("ret") == 3) {
						Toast.makeText(SnsShareActivity.this, "鉴权失败", Toast.LENGTH_LONG).show();
					} else if (result.getInt("ret") == 4) {
						Toast.makeText(SnsShareActivity.this, "服务器内部错误", Toast.LENGTH_LONG).show();
					} else if (result.getInt("ret") == 7) {
						Toast.makeText(SnsShareActivity.this, "未实名认证", Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					MobclickAgent.reportError(SnsShareActivity.this, e.getMessage());
					Toast.makeText(SnsShareActivity.this, R.string.publish_fail, Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(SnsShareActivity.this, R.string.publish_fail, Toast.LENGTH_LONG).show();
			}
		}
	}

	// ----------------------人人网平台-------------------------------------------------------------
	private Renren renren;

	private void shareToRenren() {
		renren = new Renren(Constants.RENREN_API_KEY, Constants.RENREN_SECRET_KEY, Constants.RENREN_APP_ID, this);
		if (renren.isAccessTokenValid() && renren.isSessionKeyValid()) {
			postTo(Constants.SHARE_TO_RENREN);
		} else {
			Intent authorizeIntent = new Intent(this, AuthorizeActivity.class);
			authorizeIntent.putExtra(Constants.EXTRA_BIND_FROM, Constants.BIND_RENREN);
			startActivityForResult(authorizeIntent, Constants.REQUEST_CODE_BIND_RENREN);

			// renren.authorize(this, null, listener, RENREN_OAUTH_REQUESTCODE);
		}
	}

	final RenrenAuthListener listener = new RenrenAuthListener() {

		@Override
		public void onComplete(Bundle values) {
			// Log.d("test",values.toString());
			postTo(Constants.SHARE_TO_RENREN);
		}

		@Override
		public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
			Toast.makeText(SnsShareActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancelLogin() {
		}

		@Override
		public void onCancelAuth(Bundle values) {
		}

	};

	/**
	 * 发布人人状态
	 * 
	 * @author Aizhimin
	 * 
	 */
	class RenrenPostTask extends AsyncTask<Void, Void, ResponseBean> {
		String status;
		String file;

		public RenrenPostTask(String status, String file) {
			this.status = status;
			this.file = file;
		}

		@Override
		protected ResponseBean doInBackground(Void... params) {
			try {
				if (renren == null) {
					renren = new Renren(Constants.RENREN_API_KEY, Constants.RENREN_SECRET_KEY, Constants.RENREN_APP_ID, SnsShareActivity.this);
				}
				if (resType == Constants.SHARE_RES_IMAGE) {
					PhotoUploadRequestParam photoParam = new PhotoUploadRequestParam();
					photoParam.setCaption(status);
					photoParam.setFile(new File(file));
					return renren.publishPhoto(photoParam);
				} else {
					StatusSetRequestParam feed = new StatusSetRequestParam(status);
					return renren.publishStatus(feed);
				}
			} catch (Exception e) {
				MobclickAgent.reportError(SnsShareActivity.this, e.getMessage());
			} catch (Throwable e) {
				MobclickAgent.reportError(SnsShareActivity.this, e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(ResponseBean result) {
			super.onPostExecute(result);
			if (pd != null)
				pd.dismiss();
			if (result != null) {
				Toast.makeText(SnsShareActivity.this, R.string.publish_success, Toast.LENGTH_LONG).show();
				SnsShareActivity.this.finish();
			} else {
				Toast.makeText(SnsShareActivity.this, R.string.publish_fail, Toast.LENGTH_LONG).show();
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

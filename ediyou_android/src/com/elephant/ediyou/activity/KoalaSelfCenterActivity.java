package com.elephant.ediyou.activity;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.ImageCacheLoader;
import com.elephant.ediyou.ImageCacheLoader.ImageCallback;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.AndroidUtil;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.elephant.ediyou.view.MyHorizontalScrollView;
import com.elephant.ediyou.view.MyHorizontalScrollView.SizeCallback;
import com.umeng.analytics.MobclickAgent;

/**
 * 考拉个人中心（编辑）
 * 
 * @author SongYuan
 * 
 */
public class KoalaSelfCenterActivity extends Activity implements IBaseActivity,
		OnClickListener {
	private MyHorizontalScrollView scrollView;
	private static View settingView;// 设置菜单的view
	private static View mainView;// 个人中心界面的view
	private static View currentView;// 当前显示的view

	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;

	// 设置菜单
	private LinearLayout llSettingTitle;
	private ImageView ivSettingUserPhoto;
	private TextView tvSettingUserName;
	private RelativeLayout rlHome;// 主页；
	private RelativeLayout rlPushAndLocation;// 通知和定位
	private RelativeLayout rlShareSetting;// 分享设置
	private RelativeLayout rlHelpCenter;// 帮助中心
	private RelativeLayout rlFeedBack;// 用户反馈
	private RelativeLayout rlAbout;// 关于我们
	private RelativeLayout rlExit;
	private TextView tvExit;
	private Boolean isLogin = null;
	private View viewBottom1;

	private File PHOTO_DIR;// 头像照片目录
	private UserBean userBean;

	private static int screenWidth;// 屏幕宽度
	private static int sdkVersion;// SDK版本
	private final static int ANDROIDSDK9 = 9;// android2.3SDK

	// 各个功能入口
	private View btnOrder;// 我的订单
	private View btnMessageCenter;// 消息中心
	private TextView tvOrderNum;
	private TextView tvMsgCenterNum;

	private RelativeLayout rlKoalaSelfIntroduce;// 个人介绍
	private RelativeLayout rlKoalaSelfInfo;// 个人信息
	private RelativeLayout rlKoalaSelfPhoto;// 我的照片
	private RelativeLayout rlKoalaSelfEventHadto;// 参加过的活动
	private RelativeLayout rlKoalaApplyToRoo;// 认证中心

	private String whatActivityFrom;// 是从哪个（袋鼠列表首页 or 活动列表首页）跳转过来的
	private boolean fromFunction = false;// 标示是否是从首页下方功能按钮区跳转而来的

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
		sdkVersion = AndroidUtil.getAndroidSDKVersion();
		if (SharedPrefUtil.checkToken(this)) {
			userBean = SharedPrefUtil.getUserBean(this);
		}
		if (getIntent() != null) {
			whatActivityFrom = getIntent().getStringExtra(
					Constants.KEY_SEL_CITY);
			fromFunction = getIntent().getBooleanExtra("fromFunction", false);
		}
		if (StringUtil.isBlank(whatActivityFrom)) {
			whatActivityFrom = Constants.KEY_SEL_CITY_FROM_HOME;
		}

		findView();
		fillData();
		// 将Activity添加到容器
		((CommonApplication) getApplicationContext()).addActivity(this);
	}

	@Override
	public void findView() {
		LayoutInflater inflater = LayoutInflater.from(this);
		scrollView = (MyHorizontalScrollView) inflater.inflate(R.layout.home,
				null);
		setContentView(scrollView);

		// 设置界面
		settingView = inflater.inflate(R.layout.setting, null);
		mainView = inflater.inflate(R.layout.koala_self_center, null);
		btnLeft = (Button) mainView.findViewById(R.id.btnLeft);
		btnLeft.setOnClickListener(new ClickListenerForScrolling(scrollView,
				settingView));
		btnRight = (Button) mainView.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.bg_btn_selector);
		btnRight.setText("预览");
		btnRight.setGravity(Gravity.CENTER);
		btnRight.setTextColor(Color.rgb(157, 208, 99));
		btnRight.setOnClickListener(this);
		tvTitle = (TextView) mainView.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("个人中心");

		final View[] children = new View[] { settingView, mainView };

		// Scroll to app (view[1]) when layout finished.
		int scrollToViewIdx = 1;
		scrollView.initViews(children, scrollToViewIdx,
				new SizeCallbackForMenu(btnLeft));
		currentView = mainView;

		ScrollView scrollSetting = (ScrollView) settingView
				.findViewById(R.id.scrollSetting);
		if (sdkVersion >= ANDROIDSDK9) {
			scrollSetting.setOverScrollMode(View.OVER_SCROLL_NEVER);
		}
		// 设置菜单title
		llSettingTitle = (LinearLayout) settingView
				.findViewById(R.id.llSettingTitle);
		ivSettingUserPhoto = (ImageView) settingView
				.findViewById(R.id.ivSettingUserPhoto);
		tvSettingUserName = (TextView) settingView
				.findViewById(R.id.tvSettingUserName);
		// 设置菜单上的条目组件
		rlHome = (RelativeLayout) settingView.findViewById(R.id.rlHome);
		rlPushAndLocation = (RelativeLayout) settingView
				.findViewById(R.id.rlPushAndLocation);
		rlShareSetting = (RelativeLayout) settingView
				.findViewById(R.id.rlShareSetting);
		rlHelpCenter = (RelativeLayout) settingView
				.findViewById(R.id.rlHelpCenter);
		rlFeedBack = (RelativeLayout) settingView.findViewById(R.id.rlFeedBack);
		rlAbout = (RelativeLayout) settingView.findViewById(R.id.rlAbout);
		rlExit = (RelativeLayout) settingView.findViewById(R.id.rlExit);
		tvExit = (TextView) settingView.findViewById(R.id.tvExit);
		viewBottom1 = (View) settingView.findViewById(R.id.viewBottom1);
		if (!SharedPrefUtil.checkToken(KoalaSelfCenterActivity.this)) {
			isLogin = false;
			rlExit.setVisibility(View.GONE);
			tvSettingUserName.setText("登录注册");
		} else {
			tvExit.setText("注销账号");
			isLogin = true;

		}

		llSettingTitle.setOnClickListener(this);
		rlHome.setOnClickListener(this);
		rlPushAndLocation.setOnClickListener(this);
		rlShareSetting.setOnClickListener(this);
		rlHelpCenter.setOnClickListener(this);
		rlFeedBack.setOnClickListener(this);
		rlAbout.setOnClickListener(this);
		rlExit.setOnClickListener(this);

		if (userBean != null && userBean.getIsKangaroo() == 0) {
			ImageView ivLineAboveOrderToMy = (ImageView) this
					.findViewById(R.id.ivLineAboveOrderToMy);
			ivLineAboveOrderToMy.setVisibility(View.GONE);
		} else if (userBean != null && userBean.getIsKangaroo() == 1) {
		}

		btnOrder = (View) mainView.findViewById(R.id.btnOrder);
		btnMessageCenter = (View) mainView.findViewById(R.id.btnMessageCenter);
		tvOrderNum = (TextView) mainView.findViewById(R.id.tvOrderNum);
		tvMsgCenterNum = (TextView) mainView.findViewById(R.id.tvMsgCenterNum);

		rlKoalaSelfIntroduce = (RelativeLayout) mainView
				.findViewById(R.id.rlKoalaSelfIntroduce);
		rlKoalaSelfInfo = (RelativeLayout) mainView
				.findViewById(R.id.rlKoalaSelfInfo);
		rlKoalaSelfPhoto = (RelativeLayout) mainView
				.findViewById(R.id.rlKoalaSelfPhoto);
		rlKoalaSelfEventHadto = (RelativeLayout) mainView
				.findViewById(R.id.rlKoalaSelfEventHadto);
		rlKoalaApplyToRoo = (RelativeLayout) mainView
				.findViewById(R.id.rlKoalaApplyToRoo);

		btnOrder.setOnClickListener(this);
		btnMessageCenter.setOnClickListener(this);

		rlKoalaSelfIntroduce.setOnClickListener(this);
		rlKoalaSelfInfo.setOnClickListener(this);
		rlKoalaSelfPhoto.setOnClickListener(this);
		rlKoalaSelfEventHadto.setOnClickListener(this);
		rlKoalaApplyToRoo.setOnClickListener(this);

	}

	@Override
	public void fillData() {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRight:
			startActivity(new Intent(this, KoalaShowActivity.class).putExtra(
					"uid", (long) userBean.getUserId()));
			break;
		case R.id.btnOrder:
			startActivity(new Intent(this, MyOrderActivity.class).putExtra(
					"from", "koalaSelf"));
			finish();
			break;
		case R.id.btnMessageCenter:
			Intent msgCenter = new Intent(this, MessageCenterActivity.class);
			startActivity(msgCenter);
			break;
		case R.id.rlKoalaSelfIntroduce:
			startActivity(new Intent(this, SelfIntroductionActivity.class));
			break;
		case R.id.rlKoalaSelfInfo:
			Intent profileIntent = new Intent(this,
					SelfBaseInfoEditActivity.class);
			long userId = SharedPrefUtil.getUserBean(this).getUserId();
			profileIntent.putExtra("userId", userId);
			profileIntent.putExtra("isEdit", true);
			startActivity(profileIntent);
			break;
		case R.id.rlKoalaSelfPhoto:
			startActivity(new Intent(this, SelfPhotosAndAvatarActivity.class)
					.putExtra("userId", (long) userBean.getUserId()));
			break;
		case R.id.rlKoalaSelfEventHadto:// 袋鼠参加的活动
			startActivity(new Intent(this, EventListHadJoinActivity.class)
					.putExtra(Constants.EXTRA_USER_ID,
							(long) userBean.getUserId()).putExtra(
							Constants.EXTRA_NAME, userBean.getNickname()));
			break;
		case R.id.rlKoalaApplyToRoo:// 申请为袋鼠
			startActivity(new Intent(this, RooApplyActivity.class));
			break;
		// 以下为设置菜单上的监听
		case R.id.llSettingTitle:// 个人主页
			if (isLogin) {
				if (SharedPrefUtil.checkToken(this)) {
					btnLeft.performClick();
				}
			} else {
				startActivity(new Intent(this, LoginActivity.class));
				finish();
				overridePendingTransition(R.anim.push_right_in_noalp,
						R.anim.push_left_out_noalp);
			}

			break;
		case R.id.rlHome:// 返回首页
			if (fromFunction) {
				finish();
				overridePendingTransition(R.anim.push_right_in_noalp_back,
						R.anim.push_left_out_noalp_back);
			} else {
				Intent intent = null;
				if (!StringUtil.isBlank(whatActivityFrom)
						&& whatActivityFrom
								.equals(Constants.KEY_SEL_CITY_FROM_HOME)) {
					intent = new Intent(this, MainHomeActivityGroup.class);
				} else if (!StringUtil.isBlank(whatActivityFrom)
						&& whatActivityFrom
								.equals(Constants.KEY_SEL_CITY_FROM_EVENT)) {
					intent = new Intent(this, MainHomeActivityGroup.class);
					intent.putExtra("viewPageNow", "eventList");
				}
				startActivity(intent);
				finish();
				overridePendingTransition(R.anim.push_right_in_noalp,
						R.anim.push_left_out_noalp);
			}
			break;
		case R.id.rlPushAndLocation:// 通知和定位设置
			startActivity(new Intent(this,
					NotificationAndLocationSettingActivity.class));
			overridePendingTransition(R.anim.push_right_in_noalp,
					R.anim.push_left_out_noalp);
			break;
		case R.id.rlShareSetting:// 分享设置
			startActivity(new Intent(this, AccountBindActivity.class));
			overridePendingTransition(R.anim.push_right_in_noalp,
					R.anim.push_left_out_noalp);
			break;
		case R.id.rlHelpCenter:// 帮助中心
			startActivity(new Intent(KoalaSelfCenterActivity.this,
					HelpCenterActivity.class));
			overridePendingTransition(R.anim.push_right_in_noalp,
					R.anim.push_left_out_noalp);
			break;
		case R.id.rlFeedBack:// 用户反馈
			startActivity(new Intent(KoalaSelfCenterActivity.this,
					FeedBackActivity.class));
			overridePendingTransition(R.anim.push_right_in_noalp,
					R.anim.push_left_out_noalp);
			break;
		case R.id.rlAbout:// 关于我们
			startActivity(new Intent(KoalaSelfCenterActivity.this,
					AboutUsActivity.class));
			overridePendingTransition(R.anim.push_right_in_noalp,
					R.anim.push_left_out_noalp);
			break;
		case R.id.rlExit: {
			if (!isLogin) {
				Intent intent1 = new Intent(KoalaSelfCenterActivity.this,
						LoginActivity.class);
				intent1.putExtra("back", "back");
				overridePendingTransition(R.anim.push_right_in_noalp,
						R.anim.push_left_out_noalp);
				startActivity(intent1);
			} else if (isLogin) {
				exitAccount();
			}

		}
			break;
		default:
			break;
		}
	}

	public void exitAccount() {
		final AlertDialog dialogExit = new AlertDialog.Builder(
				KoalaSelfCenterActivity.this).create();
		dialogExit.show();
		Window dialogWindow = dialogExit.getWindow();
		dialogWindow.setContentView(R.layout.dialog_common_layout);
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialogExit.getWindow().getAttributes();
		lp.width = (int) (display.getWidth() - 60); // 设置宽度
		dialogExit.getWindow().setAttributes(lp);

		TextView tvDialogMsg = (TextView) dialogWindow
				.findViewById(R.id.tvDialogMsg);
		tvDialogMsg.setText("您确定注销吗？");
		Button btnDialogLeft = (Button) dialogWindow
				.findViewById(R.id.btnDialogLeft);
		btnDialogLeft.setText("取消");
		btnDialogLeft.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();

			}
		});
		// 关闭对话框架
		Button btnDialogRight = (Button) dialogWindow
				.findViewById(R.id.btnDialogRight);
		btnDialogRight.setText("确定");
		btnDialogRight.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();
				SharedPrefUtil.clearUserBean(KoalaSelfCenterActivity.this);
				isLogin = false;
				Toast.makeText(KoalaSelfCenterActivity.this, "退出登录",
						Toast.LENGTH_SHORT).show();// 退出
				fillUserData();
			}
		});
	}

	/**
	 * 加载用户头像、昵称等数据
	 */
	private void fillUserData() {
		// 设置菜单的title的用户头像、昵称
		if (SharedPrefUtil.getUserBean(this).getAccessToken() != null) {
			isLogin = true;
			llSettingTitle.setClickable(true);
			UserBean userBeanOnSet = userBean;
			// 设置用户名字
			if (userBeanOnSet.getNickname() == null
					|| "".equals(userBeanOnSet.getNickname())) {
				tvSettingUserName.setText("");
			} else {
				tvSettingUserName.setText(userBeanOnSet.getNickname());
				ivSettingUserPhoto
				.setImageResource(R.drawable.bg_photo_defualt);
			}
			PHOTO_DIR = new File(Environment.getExternalStorageDirectory()
					+ "/" + Constants.APP_DIR_NAME + "/");
			// 设置用户头像
			if (!StringUtil.isBlank(userBeanOnSet.getAvatarUrl())) {
				File avatarFile = new File(PHOTO_DIR,
						ImageUtil.createAvatarFileName(String
								.valueOf(userBeanOnSet.getUserId())));
				if ((!NetUtil.checkNet(this)) && avatarFile != null
						&& avatarFile.exists()) {
					Drawable cacheDrawable = ImageCacheLoader.getInstance()
							.fetchLocal(avatarFile.getPath());
					ivSettingUserPhoto.setImageDrawable(cacheDrawable);
				} else {
					Drawable cacheDrawable = ImageCacheLoader.getInstance()
							.loadDrawable(userBeanOnSet.getAvatarUrl(),
									new ImageCallback() {
										@Override
										public void imageLoaded(
												Drawable imageDrawable,
												String imageUrl) {
											if (imageDrawable != null) {
												ivSettingUserPhoto
														.setImageDrawable(imageDrawable);
											} else {
												if (userBean.getIsKangaroo() == 1) {

													ivSettingUserPhoto
															.setImageResource(R.drawable.bg_kangoo_photo_defualt);
												}
												ivSettingUserPhoto
														.setImageResource(R.drawable.bg_photo_defualt);
											}
										}
									});
					if (cacheDrawable != null) {
						ivSettingUserPhoto.setImageDrawable(cacheDrawable);
					} else {
						if (userBean.getIsKangaroo() == 1) {
							ivSettingUserPhoto
									.setImageResource(R.drawable.bg_kangoo_photo_defualt);
						} else
							ivSettingUserPhoto
									.setImageResource(R.drawable.bg_photo_defualt);

					}
				}
			}
		} else {
			ivSettingUserPhoto.setImageResource(R.drawable.bg_photo_defualt);
			tvSettingUserName.setText("登录注册");
			rlExit.setVisibility(View.GONE);
			viewBottom1.setVisibility(View.GONE);
			isLogin = false;
		}
	}

	static int left = 0;
	static boolean leftMenuOut = false;

	static class ClickListenerForScrolling implements OnClickListener {
		HorizontalScrollView scrollView;
		View view;

		/**
		 * Menu must NOT be out/shown to start with.
		 */
		public ClickListenerForScrolling(HorizontalScrollView scrollView,
				View view) {
			super();
			this.scrollView = scrollView;
			this.view = view;
		}

		@Override
		public void onClick(View v) {
			// Log.i(TAG, "menu---onClick");
			if (v.getId() != R.id.btnLeft && left > 0) {
				return;
			}
			int viewWidth = view.getMeasuredWidth();
			// Ensure menu is visible
			view.setVisibility(View.VISIBLE);
			if (v.getId() == R.id.btnLeft) {
				if (!leftMenuOut) {
					// Log.i(TAG, "leftMenuOut:"+leftMenuOut+"|left:0");
					// Scroll to 0 to reveal menu
					left = 0;
					// 各种分辨率偏移不一样；
					int offset = 0;
					if (screenWidth <= 320) {
						offset = 20;
					} else if (screenWidth <= 480) {
						offset = 30;
					} else {
						offset = 40;
					}
					scrollView.smoothScrollTo(left + offset, 0);
					currentView = settingView;
				} else {
					// Log.i(TAG,
					// "leftMenuOut:"+leftMenuOut+"|left:"+viewWidth);
					// Scroll to menuWidth so menu isn't on screen.
					left = viewWidth;
					scrollView.smoothScrollTo(left, 0);
					currentView = mainView;
				}
				leftMenuOut = !leftMenuOut;
			}
		}
	}

	/**
	 * Helper that remembers the width of the 'slide' button, so that the
	 * 'slide' button remains in view, even when the menu is showing.
	 */
	static class SizeCallbackForMenu implements SizeCallback {
		int btnWidth;
		View btnSlide;

		public SizeCallbackForMenu(View btnSlide) {
			super();
			this.btnSlide = btnSlide;
		}

		@Override
		public void onGlobalLayout() {
			btnWidth = btnSlide.getMeasuredWidth();
			System.out.println("btnWidth=" + btnWidth);
		}

		@Override
		public void getViewSize(int idx, int w, int h, int[] dims) {
			dims[0] = w;
			dims[1] = h;
			// final int menuIdx = 0;
			// if (idx == menuIdx) {
			// dims[0] = w - btnWidth;
			// }
			if (idx != 1) {
				// 当视图不是中间的视图
				dims[0] = w - btnWidth;
			}
		}
	}

	/**
	 * 重新获取用户信息
	 * 
	 * @author syghh
	 * 
	 */
	private class GetUsersInfoTask extends AsyncTask<Void, Void, JSONObject> {

		private long userId;
		private String access_token;

		public GetUsersInfoTask(long userId, String access_token) {
			super();
			this.userId = userId;
			this.access_token = access_token;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().getUsersInfo(userId, access_token);
			} catch (SystemException e) {
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
						userBean = new UserBean(result.getJSONObject("user"));
						fillUserData();
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(KoalaSelfCenterActivity.this,
								R.string.time_out, Toast.LENGTH_LONG).show();
						ivSettingUserPhoto
								.setImageResource(R.drawable.bg_photo_defualt);
						tvSettingUserName.setText("登录注册");
						rlExit.setVisibility(View.GONE);
						viewBottom1.setVisibility(View.GONE);
						isLogin = false;
					}
				} catch (JSONException e) {
					Toast.makeText(KoalaSelfCenterActivity.this, "获取用户信息失败",
							Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(KoalaSelfCenterActivity.this, "服务器请求失败",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 重新获取用户信息，主要用于更新用户的头像昵称等
		if (NetUtil.checkNet(this) && userBean != null) {
			new GetUsersInfoTask(SharedPrefUtil.getUserBean(this).getUserId(),
					SharedPrefUtil.getUserBean(this).getAccessToken())
					.execute();
			new GetMsgNumTask().execute();
		} else if (userBean == null) {
			ivSettingUserPhoto.setImageResource(R.drawable.bg_photo_defualt);
			tvSettingUserName.setText("登录注册");
			rlExit.setVisibility(View.GONE);
			viewBottom1.setVisibility(View.GONE);
			isLogin = false;
		}
		MobclickAgent.onResume(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (currentView == settingView) {
				btnLeft.performClick();
				return true;
			}
			if (fromFunction) {
				finish();
			} else {
				startActivity(new Intent(this, MainHomeActivityGroup.class));
				finish();
			}
			overridePendingTransition(R.anim.push_right_in_noalp_back,
					R.anim.push_left_out_noalp_back);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// 以下代码添加的情况下，点击功能菜单item后，将自动设定当前的view为主view（目前业务需求不需要添加）
		// if (currentView == settingView) {
		// btnLeft.performClick();
		// }
	}

	/**
	 * 获取我的订单和消息中心数目；
	 * 
	 * @author Zhoujun
	 * 
	 */
	private class GetMsgNumTask extends AsyncTask<Void, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(Void... params) {
			String access_token = SharedPrefUtil.getUserBean(
					KoalaSelfCenterActivity.this).getAccessToken();
			long userId = SharedPrefUtil.getUserBean(
					KoalaSelfCenterActivity.this).getUserId();
			try {
				return new BusinessHelper().getMsgNum(access_token, userId);
			} catch (SystemException e) {
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
						int sysCount = result.getInt("sysCount");
						int orderCount = result.getInt("senderOrderCount");
						if (sysCount > 0) {
							tvMsgCenterNum.setVisibility(View.VISIBLE);
							tvMsgCenterNum.setText(sysCount + "");
						} else {
							tvMsgCenterNum.setVisibility(View.GONE);
						}
						if (orderCount > 0) {
							tvOrderNum.setVisibility(View.VISIBLE);
							tvOrderNum.setText(orderCount + "");
						} else {
							tvOrderNum.setVisibility(View.GONE);
						}
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(KoalaSelfCenterActivity.this,
								R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(KoalaSelfCenterActivity.this,
								LoginActivity.class).putExtra("back", "back"));
					}
				} catch (JSONException e) {
				}
			} else {

			}
		}
	}
}

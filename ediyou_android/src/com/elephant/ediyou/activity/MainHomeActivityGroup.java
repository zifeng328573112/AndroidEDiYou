package com.elephant.ediyou.activity;

import java.io.File;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKGeocoderAddressComponent;
import com.baidu.mapapi.MKLocationManager;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.ImageCacheLoader;
import com.elephant.ediyou.ImageCacheLoader.ImageCallback;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.bean.VersionBean;
import com.elephant.ediyou.db.DataBaseAdapter;
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
 * 主页（袋鼠主页和活动主页的TabHost）
 * 
 * @author syghh
 * 
 */
public class MainHomeActivityGroup extends ActivityGroup implements OnClickListener, IBaseActivity, LocationListener {
	private MyHorizontalScrollView scrollView;
	private static View settingView;
	private static View mainView;
	private static View currentView;// 当前显示的view

	// Title
	private Button btnLeft;
	private Button btnHome;
	private Button btnEvent;
	private LinearLayout container;
	private final static int TAB_ROO_HOME = 0;
	private final static int TAB_EVENT_HOME = 1;
	private int tab_type = TAB_ROO_HOME;

	// 新需求修改或添加的title组件：
	private Button btnRight;

	private LinearLayout llHomeCenterTitle;
	private TextView tvTitleLocation;
	private ImageView ivChoiceLocation;
	private String locationStr;// title需要的定位城市

	// gps
	private BMapManager mMapManager = null;
	private MKLocationManager mLocationManager = null;
	private MKSearch mMKSearch = null;
	private String city;// gps获取的城市名

	// 底部功能按钮
	private ImageView ivCenterBottom;
	private ImageView ivFunctionOrders;
	private ImageView ivFunctionMyhome;
	private ImageView ivFunctionEvent;
	private ImageView ivFunctionMsg;
	// 动画效果
	Animation ordersInAnim;
	Animation ordersOutAnim;
	Animation myhomeInAnim;
	Animation myhomeOutAnim;
	Animation eventInAnim;
	Animation eventOutAnim;
	Animation msgInAnim;
	Animation msgOutAnim;

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
	/**
	 * 数据库操作对象
	 */
	private DataBaseAdapter dba;

	private int cityId = 650;// 丽江
	private int pId = -1;
	private int cityIdToSerach;

	private MyTask animationTimerTask;// 动画的定时切换
	private Timer timer;
	private AnimationDrawable animationDrawable;
	private VersionBean versionBean;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		dba = ((CommonApplication) getApplicationContext()).getDbAdapter();
		screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
		sdkVersion = AndroidUtil.getAndroidSDKVersion();
		if (SharedPrefUtil.checkToken(this)) {
			userBean = SharedPrefUtil.getUserBean(this);
		}

		String eventList = getIntent().getStringExtra("viewPageNow");
		if (!StringUtil.isBlank(eventList) && eventList.equals("eventList")) {
			tab_type = TAB_EVENT_HOME;
		}
		locationStr = getIntent().getStringExtra("location");
		if (!StringUtil.isBlank(locationStr) && !locationStr.equals("城市")) {
			SharedPrefUtil.setChoicedLocation(this, locationStr);
			if (locationStr.equals("北京") || locationStr.equals("上海") || locationStr.equals("天津") || locationStr.equals("重庆") || locationStr.equals("香港") || locationStr.equals("澳门")) {
				pId = dba.findProvinceId(locationStr);
				// 仅用于传递到袋鼠搜索界面
				cityIdToSerach = dba.findCityId(locationStr);
			} else {
				cityId = dba.findCityId(locationStr);
			}

		} else {
			if (!StringUtil.isBlank(SharedPrefUtil.getChoicedLocation(this))) {
				locationStr = SharedPrefUtil.getChoicedLocation(this);
				if (!locationStr.equals("城市")) {
					if (locationStr.equals("北京") || locationStr.equals("上海") || locationStr.equals("天津") || locationStr.equals("重庆") || locationStr.equals("香港") || locationStr.equals("澳门")) {
						pId = dba.findProvinceId(locationStr);

						// 仅用于传递到袋鼠搜索界面
						cityIdToSerach = dba.findCityId(locationStr);

					} else {
						cityId = dba.findCityId(locationStr);
					}
				}
			}
		}
		// 首页的定位，新业务只要求丽江，故不使用
		// if (NetUtil.checkNet(this)) {
		// if (SharedPrefUtil.getLocationSetting(this)) {
		// initMap();
		// }
		// } else {
		// Toast.makeText(this, R.string.NoSignalException,
		// Toast.LENGTH_SHORT).show();
		// }

		findView();
		fillData();
		((CommonApplication) getApplication()).addActivity(this);
		if (NetUtil.checkNet(this)) {
			checkVersion();
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void findView() {
		LayoutInflater inflater = LayoutInflater.from(this);
		scrollView = (MyHorizontalScrollView) inflater.inflate(R.layout.home, null);
		setContentView(scrollView);

		// 设置界面
		settingView = inflater.inflate(R.layout.setting, null);
		mainView = inflater.inflate(R.layout.main_home_activity_group, null);
		btnLeft = (Button) mainView.findViewById(R.id.btnLeft);
		btnLeft.setOnClickListener(new ClickListenerForScrolling(scrollView, settingView));

		final View[] children = new View[] { settingView, mainView };

		// Scroll to app (view[1]) when layout finished.
		int scrollToViewIdx = 1;
		scrollView.initViews(children, scrollToViewIdx, new SizeCallbackForMenu(btnLeft));
		currentView = mainView;

		ScrollView scrollSetting = (ScrollView) settingView.findViewById(R.id.scrollSetting);
		if (sdkVersion >= ANDROIDSDK9) {
			scrollSetting.setOverScrollMode(View.OVER_SCROLL_NEVER);
		}

		// 新需求修改或添加的title组件：
		btnRight = (Button) mainView.findViewById(R.id.btnRight);
		btnRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainHomeActivityGroup.this, MyLocationLBSActivity.class));
			}
		});

		// 设置菜单title
		llSettingTitle = (LinearLayout) settingView.findViewById(R.id.llSettingTitle);
		ivSettingUserPhoto = (ImageView) settingView.findViewById(R.id.ivSettingUserPhoto);
		tvSettingUserName = (TextView) settingView.findViewById(R.id.tvSettingUserName);
		// 设置菜单上的条目组件
		rlHome = (RelativeLayout) settingView.findViewById(R.id.rlHome);
		rlPushAndLocation = (RelativeLayout) settingView.findViewById(R.id.rlPushAndLocation);
		rlShareSetting = (RelativeLayout) settingView.findViewById(R.id.rlShareSetting);
		rlHelpCenter = (RelativeLayout) settingView.findViewById(R.id.rlHelpCenter);
		rlFeedBack = (RelativeLayout) settingView.findViewById(R.id.rlFeedBack);
		rlAbout = (RelativeLayout) settingView.findViewById(R.id.rlAbout);
		rlExit = (RelativeLayout) settingView.findViewById(R.id.rlExit);
		tvExit = (TextView) settingView.findViewById(R.id.tvExit);
		viewBottom1 = (View) settingView.findViewById(R.id.viewBottom1);
		if (!SharedPrefUtil.checkToken(MainHomeActivityGroup.this)) {
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
			ImageView ivLineAboveOrderToMy = (ImageView) this.findViewById(R.id.ivLineAboveOrderToMy);
			ivLineAboveOrderToMy.setVisibility(View.GONE);
		} else if (userBean != null && userBean.getIsKangaroo() == 1) {
		}

		// title的各个组件
		llHomeCenterTitle = (LinearLayout) mainView.findViewById(R.id.llHomeCenterTitle);
		tvTitleLocation = (TextView) mainView.findViewById(R.id.tvTitleLocation);
		ivChoiceLocation = (ImageView) mainView.findViewById(R.id.ivChoiceLocation);
		llHomeCenterTitle.setOnClickListener(this);

		btnHome = (Button) mainView.findViewById(R.id.btnHome);
		btnEvent = (Button) mainView.findViewById(R.id.btnEvent);
		container = (LinearLayout) mainView.findViewById(R.id.container);
		btnHome.setOnClickListener(this);
		btnEvent.setOnClickListener(this);
		switchActivity(tab_type);
		if (tab_type == TAB_ROO_HOME) {
			btnHome.setBackgroundResource(R.drawable.ic_title_right_home_sel);
			btnHome.setTextColor(Color.WHITE);
			btnEvent.setBackgroundResource(R.drawable.ic_title_right_event_nor);
			btnEvent.setTextColor(Color.rgb(157, 208, 99));
		} else if (tab_type == TAB_EVENT_HOME) {
			btnHome.setBackgroundResource(R.drawable.ic_title_right_home_nor);
			btnHome.setTextColor(Color.rgb(157, 208, 99));
			btnEvent.setBackgroundResource(R.drawable.ic_title_right_event_sel);
			btnEvent.setTextColor(Color.WHITE);
		}

		// 底部功能按钮
		ivCenterBottom = (ImageView) mainView.findViewById(R.id.ivCenterBottom);
		LayoutParams lp = ivCenterBottom.getLayoutParams();
		if (screenWidth < 500) {
			lp.width = 200;
			lp.height = 100;
		} else {
			lp.width = 240;
			lp.height = 120;
		}
		ivCenterBottom.setLayoutParams(lp);
		ivCenterBottom.setVisibility(View.VISIBLE);
		ivCenterBottom.setClickable(true);
		ivFunctionOrders = (ImageView) mainView.findViewById(R.id.ivFunctionOrders);
		ivFunctionMyhome = (ImageView) mainView.findViewById(R.id.ivFunctionMyhome);
		ivFunctionEvent = (ImageView) mainView.findViewById(R.id.ivFunctionEvent);
		ivFunctionMsg = (ImageView) mainView.findViewById(R.id.ivFunctionMsg);

		ordersInAnim = AnimationUtils.loadAnimation(this, R.anim.push_order_top_in);
		ordersOutAnim = AnimationUtils.loadAnimation(this, R.anim.push_order_bottom_out);
		myhomeInAnim = AnimationUtils.loadAnimation(this, R.anim.push_myhome_top_in);
		myhomeOutAnim = AnimationUtils.loadAnimation(this, R.anim.push_myhome_bottom_out);
		eventInAnim = AnimationUtils.loadAnimation(this, R.anim.push_event_top_in);
		eventOutAnim = AnimationUtils.loadAnimation(this, R.anim.push_event_bottom_out);
		msgInAnim = AnimationUtils.loadAnimation(this, R.anim.push_msg_top_in);
		msgOutAnim = AnimationUtils.loadAnimation(this, R.anim.push_msg_bottom_out);

		ivCenterBottom.setOnClickListener(this);
		ivFunctionOrders.setOnClickListener(this);
		ivFunctionMyhome.setOnClickListener(this);
		ivFunctionEvent.setOnClickListener(this);
		ivFunctionMsg.setOnClickListener(this);

	}

	@Override
	public void fillData() {
		// 因业务需求为仅丽江，顾先不定位或设置
		// setTitleLocation();
		// 故直接设置城市为 丽江
		locationStr = "丽江";
		tvTitleLocation.setText(locationStr);

		// 开始动画的定时器
		startAnimationTask();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llHomeCenterTitle: {
			Intent intent = new Intent(this, ChoiceHotLocationActivity.class);
			if (tab_type == TAB_ROO_HOME) {
				intent.putExtra(Constants.KEY_SEL_CITY, Constants.KEY_SEL_CITY_FROM_HOME);
			} else if (tab_type == TAB_EVENT_HOME) {
				intent.putExtra(Constants.KEY_SEL_CITY, Constants.KEY_SEL_CITY_FROM_EVENT);
			}
			intent.putExtra("location", tvTitleLocation.getText().toString().trim());
			startActivity(intent);
			finish();
		}
			break;
		case R.id.btnHome:
			if (tab_type != TAB_ROO_HOME) {
				tab_type = TAB_ROO_HOME;
				btnHome.setBackgroundResource(R.drawable.ic_title_right_home_sel);
				btnHome.setTextColor(Color.WHITE);
				btnEvent.setBackgroundResource(R.drawable.ic_title_right_event_nor);
				btnEvent.setTextColor(Color.rgb(157, 208, 99));
				switchActivity(TAB_ROO_HOME);
			}
			break;
		case R.id.btnEvent:
			if (tab_type != TAB_EVENT_HOME) {
				tab_type = TAB_EVENT_HOME;
				btnHome.setBackgroundResource(R.drawable.ic_title_right_home_nor);
				btnHome.setTextColor(Color.rgb(157, 208, 99));
				btnEvent.setBackgroundResource(R.drawable.ic_title_right_event_sel);
				btnEvent.setTextColor(Color.WHITE);
				switchActivity(TAB_EVENT_HOME);
			}
			break;
		case R.id.ivCenterBottom:// 底部功能按钮（考拉头像的）
			if (ivFunctionOrders.getVisibility() == View.VISIBLE) {
				ivCenterBottom.setImageResource(R.anim.koala_bow_head_animation);
				animationDrawable = (AnimationDrawable) ivCenterBottom.getDrawable();
				animationDrawable.setOneShot(true);
				animationDrawable.start();

				startAnimationTask();

				ivFunctionOrders.startAnimation(ordersOutAnim);
				ivFunctionMyhome.startAnimation(myhomeOutAnim);
				ivFunctionEvent.startAnimation(eventOutAnim);
				ivFunctionMsg.startAnimation(msgOutAnim);

				ivFunctionOrders.setVisibility(View.GONE);
				ivFunctionMyhome.setVisibility(View.GONE);
				ivFunctionEvent.setVisibility(View.GONE);
				ivFunctionMsg.setVisibility(View.GONE);

			} else {
				closeTimer();
				ivCenterBottom.setImageResource(R.anim.koala_raisehead_animation);
				animationDrawable = (AnimationDrawable) ivCenterBottom.getDrawable();
				animationDrawable.setOneShot(true);
				animationDrawable.start();

				ivFunctionOrders.startAnimation(ordersInAnim);
				ivFunctionMyhome.startAnimation(myhomeInAnim);
				ivFunctionEvent.startAnimation(eventInAnim);
				ivFunctionMsg.startAnimation(msgInAnim);

				ivFunctionOrders.setVisibility(View.VISIBLE);
				ivFunctionMyhome.setVisibility(View.VISIBLE);
				ivFunctionEvent.setVisibility(View.VISIBLE);
				ivFunctionMsg.setVisibility(View.VISIBLE);

			}
			break;
		case R.id.ivFunctionOrders:// 跳转到我的订单列表
			if (isLogin) {
				if (SharedPrefUtil.checkToken(this)) {
					if (SharedPrefUtil.getUserBean(this).getIsKangaroo() == 0) {
						startActivity(new Intent(this, MyOrderActivity.class).putExtra("from", "home"));
					} else if ((SharedPrefUtil.getUserBean(this).getIsKangaroo() == 1)) {
						startActivity(new Intent(this, MyReceivedOrderActivity.class).putExtra("from", "home"));
					}
				}
			} else {
				startActivity(new Intent(this, LoginActivity.class));
				finish();
			}

			ivCenterBottom.performClick();
			break;
		case R.id.ivFunctionMyhome:// 个人的主页（下方功能按钮部分）
			if (isLogin) {
				if (SharedPrefUtil.checkToken(this)) {
					if (SharedPrefUtil.getUserBean(this).getIsKangaroo() == 0) {
						int userId = SharedPrefUtil.getUserBean(this).getUserId();
						Intent intentToMyhome = new Intent(this, KoalaSelfCenterActivity.class);
						intentToMyhome.putExtra("koalaId", (long) userId);
						if (tab_type == TAB_ROO_HOME) {
							intentToMyhome.putExtra(Constants.KEY_SEL_CITY, Constants.KEY_SEL_CITY_FROM_HOME);
						} else if (tab_type == TAB_EVENT_HOME) {
							intentToMyhome.putExtra(Constants.KEY_SEL_CITY, Constants.KEY_SEL_CITY_FROM_EVENT);
						}
						intentToMyhome.putExtra("fromFunction", true);// 传fromFunction为true，并且不finish当前首页
						startActivity(intentToMyhome);
					} else if (SharedPrefUtil.getUserBean(this).getIsKangaroo() == 1) {
						int userId = SharedPrefUtil.getUserBean(this).getUserId();
						Intent intentToMyhome = new Intent(this, RooSelfCenterActivity.class);
						intentToMyhome.putExtra("uid", (long) userId);
						intentToMyhome.putExtra("rooId", SharedPrefUtil.getRooId(this));// 袋鼠id
						if (tab_type == TAB_ROO_HOME) {
							intentToMyhome.putExtra(Constants.KEY_SEL_CITY, Constants.KEY_SEL_CITY_FROM_HOME);
						} else if (tab_type == TAB_EVENT_HOME) {
							intentToMyhome.putExtra(Constants.KEY_SEL_CITY, Constants.KEY_SEL_CITY_FROM_EVENT);
						}
						intentToMyhome.putExtra("fromFunction", true);// 传fromFunction为true，并且不finish当前首页
						startActivity(intentToMyhome);
					}
				}
			} else {
				startActivity(new Intent(this, LoginActivity.class));
				finish();
			}

			ivCenterBottom.performClick();
			break;
		case R.id.ivFunctionEvent:// 搜索按钮
			Intent intentSearch = new Intent();
			if (tab_type == TAB_ROO_HOME) {
				intentSearch.setClass(this, RooSearchActivity.class);
				if (locationStr != null) {
					intentSearch.putExtra("cityName", locationStr);
				}
			} else if (tab_type == TAB_EVENT_HOME) {
				intentSearch.setClass(this, EventSearchActivity.class);
			}

			startActivity(intentSearch);
			ivCenterBottom.performClick();
			break;
		case R.id.ivFunctionMsg:// 跳转到我的私信
			if (isLogin) {
				Intent msgCenter1 = new Intent(MainHomeActivityGroup.this, MessageCenterActivity.class);
				startActivity(msgCenter1);
			} else {
				startActivity(new Intent(this, LoginActivity.class));
				finish();
			}

			ivCenterBottom.performClick();
			break;
		case R.id.llSettingTitle:// 个人主页title入口
			if (isLogin) {
				if (SharedPrefUtil.checkToken(this)) {
					if (SharedPrefUtil.getUserBean(this).getIsKangaroo() == 0) {
						int userId = SharedPrefUtil.getUserBean(this).getUserId();
						Intent intentToMyhome = new Intent(this, KoalaSelfCenterActivity.class);
						intentToMyhome.putExtra("koalaId", (long) userId);
						if (tab_type == TAB_ROO_HOME) {
							intentToMyhome.putExtra(Constants.KEY_SEL_CITY, Constants.KEY_SEL_CITY_FROM_HOME);
						} else if (tab_type == TAB_EVENT_HOME) {
							intentToMyhome.putExtra(Constants.KEY_SEL_CITY, Constants.KEY_SEL_CITY_FROM_EVENT);
						}
						startActivity(intentToMyhome);
						finish();
						overridePendingTransition(R.anim.push_right_in_noalp, R.anim.push_left_out_noalp);
					} else if (SharedPrefUtil.getUserBean(this).getIsKangaroo() == 1) {
						int userId = SharedPrefUtil.getUserBean(this).getUserId();
						Intent intentToMyhome = new Intent(this, RooSelfCenterActivity.class);
						intentToMyhome.putExtra("uid", (long) userId);
						intentToMyhome.putExtra("rooId", SharedPrefUtil.getRooId(this));// 袋鼠id
						if (tab_type == TAB_ROO_HOME) {
							intentToMyhome.putExtra(Constants.KEY_SEL_CITY, Constants.KEY_SEL_CITY_FROM_HOME);
						} else if (tab_type == TAB_EVENT_HOME) {
							intentToMyhome.putExtra(Constants.KEY_SEL_CITY, Constants.KEY_SEL_CITY_FROM_EVENT);
						}
						startActivity(intentToMyhome);
						finish();
						overridePendingTransition(R.anim.push_right_in_noalp, R.anim.push_left_out_noalp);
					}
				}
			} else {
				startActivity(new Intent(this, LoginActivity.class));
				overridePendingTransition(R.anim.push_right_in_noalp, R.anim.push_left_out_noalp);
				finish();
			}
			break;
		case R.id.rlHome:
			left = mainView.getMeasuredWidth();
			scrollView.smoothScrollTo(left, 0);
			currentView = mainView;
			break;
		case R.id.rlPushAndLocation:// 通知和定位设置
			startActivity(new Intent(this, NotificationAndLocationSettingActivity.class));
			overridePendingTransition(R.anim.push_right_in_noalp, R.anim.push_left_out_noalp);
			break;
		case R.id.rlShareSetting:// 分享设置
			startActivity(new Intent(this, AccountBindActivity.class));
			overridePendingTransition(R.anim.push_right_in_noalp, R.anim.push_left_out_noalp);
			break;
		case R.id.rlHelpCenter:// 帮助中心
			startActivity(new Intent(MainHomeActivityGroup.this, HelpCenterActivity.class));
			overridePendingTransition(R.anim.push_right_in_noalp, R.anim.push_left_out_noalp);
			break;
		case R.id.rlFeedBack:// 用户反馈
			startActivity(new Intent(MainHomeActivityGroup.this, FeedBackActivity.class));
			overridePendingTransition(R.anim.push_right_in_noalp, R.anim.push_left_out_noalp);
			break;
		case R.id.rlAbout:// 关于我们
			startActivity(new Intent(MainHomeActivityGroup.this, AboutUsActivity.class));
			overridePendingTransition(R.anim.push_right_in_noalp, R.anim.push_left_out_noalp);
			break;
		case R.id.rlExit: {
			if (!isLogin) {
				Intent intent = new Intent(MainHomeActivityGroup.this, LoginActivity.class);
				startActivity(intent);
				finish();
			} else if (isLogin) {
				exitAccount();
			}
		}
			break;
		}
	}

	/**
	 * 启动viewPager的自动切换
	 */
	private void startAnimationTask() {
		timer = new Timer();
		animationTimerTask = new MyTask(this);
		int timeDurationType = new Random().nextInt(3);
		if (timeDurationType == 0) {
			timer.schedule(animationTimerTask, 0, 5000);
		} else if (timeDurationType == 1) {
			timer.schedule(animationTimerTask, 0, 10000);
		} else if (timeDurationType == 2) {
			timer.schedule(animationTimerTask, 0, 15000);
		}
	}

	private void closeTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (animationTimerTask != null) {
			animationTimerTask = null;
		}
	}

	// TimerTask
	class MyTask extends TimerTask {
		private Activity context;

		MyTask(Activity context) {
			this.context = context;
		}

		@Override
		public void run() {
			// 更新UI方法
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			context.runOnUiThread(updateThread);
		}
	}

	// 用于操作UI线程
	Runnable updateThread = new Runnable() {
		@Override
		public void run() {
			// 更新UI
			int nowanimation = new Random().nextInt(3);
			if (nowanimation == 0) {
				ivCenterBottom.setImageResource(R.anim.koala_blink_animation);
			} else if (nowanimation == 1) {
				ivCenterBottom.setImageResource(R.anim.koala_smile_animation);
			} else if (nowanimation == 2) {
				ivCenterBottom.setImageResource(R.anim.koala_sleep_animation);
			}

			animationDrawable = (AnimationDrawable) ivCenterBottom.getDrawable();
			animationDrawable.setOneShot(false);
			animationDrawable.start();
		}
	};

	/**
	 * 提示注销与否
	 */
	public void exitAccount() {
		final AlertDialog dialogExit = new AlertDialog.Builder(MainHomeActivityGroup.this).create();
		dialogExit.show();
		Window dialogWindow = dialogExit.getWindow();
		dialogWindow.setContentView(R.layout.dialog_common_layout);
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialogExit.getWindow().getAttributes();
		lp.width = (int) (display.getWidth() - 60); // 设置宽度
		dialogExit.getWindow().setAttributes(lp);

		TextView tvDialogMsg = (TextView) dialogWindow.findViewById(R.id.tvDialogMsg);
		tvDialogMsg.setText("您确定注销吗？");
		Button btnDialogLeft = (Button) dialogWindow.findViewById(R.id.btnDialogLeft);
		btnDialogLeft.setText("取消");
		btnDialogLeft.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();
			}
		});
		// 关闭对话框架
		Button btnDialogRight = (Button) dialogWindow.findViewById(R.id.btnDialogRight);
		btnDialogRight.setText("确定");
		btnDialogRight.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();
				SharedPrefUtil.clearUserBean(MainHomeActivityGroup.this);
				isLogin = false;
				Toast.makeText(MainHomeActivityGroup.this, "退出登录", Toast.LENGTH_SHORT).show();// 退出
				fillUserData();
			}
		});
	}

	/**
	 * 根据activity的id来确定ActivityGroup显示的当前页
	 * 
	 * @param id
	 */
	private void switchActivity(int id) {
		container.removeAllViews();
		Intent intent = null;
		switch (id) {
		case 0:
			intent = new Intent(this, MainHomeActivity.class);
			break;
		case 1:
			intent = new Intent(this, EventHomeListActivity.class);
			break;
		}
		if (cityId != -1) {
			intent.putExtra("cityId", cityId);
		}
		if (pId != -1) {
			intent.putExtra("pId", pId);
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Window subActivity = getLocalActivityManager().startActivity("subActivity", intent);
		container.addView(subActivity.getDecorView(), ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
	}

	/**
	 * 设置当前城市
	 */
	public void setTitleLocation() {
		if (!StringUtil.isBlank(city) && !city.equals(tvTitleLocation.getText().toString())) {
			final AlertDialog dialogExit = new AlertDialog.Builder(MainHomeActivityGroup.this).create();
			dialogExit.show();
			Window dialogWindow = dialogExit.getWindow();
			dialogWindow.setContentView(R.layout.dialog_common_layout);
			WindowManager windowManager = getWindowManager();
			Display display = windowManager.getDefaultDisplay();
			WindowManager.LayoutParams lp = dialogExit.getWindow().getAttributes();
			lp.width = (int) (display.getWidth() - 60); // 设置宽度
			dialogExit.getWindow().setAttributes(lp);

			TextView tvDialogMsg = (TextView) dialogWindow.findViewById(R.id.tvDialogMsg);
			tvDialogMsg.setText("您确认选择" + city + "为目标旅游城市吗？");
			Button btnDialogLeft = (Button) dialogWindow.findViewById(R.id.btnDialogLeft);
			btnDialogLeft.setText("取消");
			btnDialogLeft.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					dialogExit.dismiss();
				}
			});
			// 关闭对话框架
			Button btnDialogRight = (Button) dialogWindow.findViewById(R.id.btnDialogRight);
			btnDialogRight.setText("确认");
			btnDialogRight.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					int locationLe = city.length();
					String cityName = city.substring(0, locationLe - 1);
					tvTitleLocation.setText(cityName);
					// 重新加载袋鼠或活动数据
					switchActivity(tab_type);
					dialogExit.dismiss();
				}
			});
		} else {
			if (!StringUtil.isBlank(locationStr)) {
				tvTitleLocation.setText(locationStr);
				SharedPrefUtil.setChoicedLocation(this, locationStr);
			} else if (StringUtil.isBlank(locationStr) && !StringUtil.isBlank(SharedPrefUtil.getChoicedLocation(this))) {
				tvTitleLocation.setText(SharedPrefUtil.getChoicedLocation(this));
			}
		}

		// 当第一次获取到了当前城市名后，关闭，不再运行获取
		if (mMapManager != null) {
			mMapManager.stop();
		}
	}

	/**
	 * 加载用户头像、昵称等数据
	 */
	private void fillUserData() {
		// 设置菜单的title的用户头像、昵称
		if (SharedPrefUtil.getUserBean(this).getAccessToken() != null) {
			isLogin = true;
			llSettingTitle.setClickable(true);
			final UserBean userBeanOnSet = userBean;
			// 设置用户名字
			if (userBeanOnSet.getNickname() == null || "".equals(userBeanOnSet.getNickname())) {
				modifyNickNameDialog();
			} else {
				tvSettingUserName.setText(userBeanOnSet.getNickname());
				ivSettingUserPhoto.setImageResource(R.drawable.bg_photo_defualt);
			}
			PHOTO_DIR = new File(Environment.getExternalStorageDirectory() + "/" + Constants.APP_DIR_NAME + "/");
			// 设置用户头像
			if (!StringUtil.isBlank(userBeanOnSet.getAvatarUrl())) {
				File avatarFile = new File(PHOTO_DIR, ImageUtil.createAvatarFileName(String.valueOf(userBeanOnSet.getUserId())));
				if ((!NetUtil.checkNet(this)) && avatarFile != null && avatarFile.exists()) {
					Drawable cacheDrawable = ImageCacheLoader.getInstance().fetchLocal(avatarFile.getPath());
					ivSettingUserPhoto.setImageDrawable(cacheDrawable);
				} else {
					Drawable cacheDrawable = ImageCacheLoader.getInstance().loadDrawable(userBeanOnSet.getAvatarUrl(), new ImageCallback() {
						@Override
						public void imageLoaded(Drawable imageDrawable, String imageUrl) {
							if (imageDrawable != null) {
								ivSettingUserPhoto.setImageDrawable(imageDrawable);
							} else {
								if (userBeanOnSet.getIsKangaroo() == 1) {
									ivSettingUserPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
								} else
									ivSettingUserPhoto.setImageResource(R.drawable.bg_photo_defualt);
							}
						}
					});
					if (cacheDrawable != null) {
						ivSettingUserPhoto.setImageDrawable(cacheDrawable);
					} else {
						if (userBeanOnSet.getIsKangaroo() == 1) {
							ivSettingUserPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
						} else
							ivSettingUserPhoto.setImageResource(R.drawable.bg_photo_defualt);
					}
				}
			}
		} else {
			ivSettingUserPhoto.setImageResource(R.drawable.bg_photo_defualt);
			tvSettingUserName.setText("登录注册");
			rlExit.setVisibility(View.GONE);
			viewBottom1.setVisibility(View.GONE);
			isLogin = false;
			// llSettingTitle.setClickable(false);
		}
	}

	/**
	 * 修改昵称的Dialog,自定义。
	 */
	public void modifyNickNameDialog() {
		final AlertDialog dialogExit = new AlertDialog.Builder(MainHomeActivityGroup.this).create();
		dialogExit.show();
		Window dialogWindow = dialogExit.getWindow();
		dialogWindow.setContentView(R.layout.dialog_common_layout);
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialogExit.getWindow().getAttributes();
		lp.width = (int) (display.getWidth() - 60); // 设置宽度
		dialogExit.getWindow().setAttributes(lp);

		TextView tvDialogMsg = (TextView) dialogWindow.findViewById(R.id.tvDialogMsg);
		tvDialogMsg.setText("你还没有昵称，为了让更多的人能认识你，赶紧去修改昵称吧");
		tvDialogMsg.setGravity(Gravity.LEFT);
		Button btnDialogLeft = (Button) dialogWindow.findViewById(R.id.btnDialogLeft);
		btnDialogLeft.setText("下次再说");
		btnDialogLeft.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();

			}
		});
		// 关闭对话框架
		Button btnDialogRight = (Button) dialogWindow.findViewById(R.id.btnDialogRight);
		btnDialogRight.setText("修改昵称");
		btnDialogRight.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int userId = SharedPrefUtil.getUserBean(MainHomeActivityGroup.this).getUserId();
				Intent intentToMyhome = new Intent(MainHomeActivityGroup.this, SelfBaseInfoEditActivity.class);
				intentToMyhome.putExtra("userId", (long) userId);
				intentToMyhome.putExtra("isEdit", true);
				startActivity(intentToMyhome);
				dialogExit.dismiss();
			}
		});
	}

	/**
	 * 初始化地图（GPS）
	 */
	private void initMap() {
		mMapManager = new BMapManager(this);
		mMapManager.init(getString(R.string.map_key), null);
		// super.initMapActivity(mMapManager);

		mMKSearch = new MKSearch();
		mMKSearch.init(mMapManager, new MySearchListener());
		mLocationManager = mMapManager.getLocationManager();
		mLocationManager.requestLocationUpdates(this);
		// 使用GPS定位
		mLocationManager.enableProvider((int) MKLocationManager.MK_GPS_PROVIDER);
	}

	/**
	 * 当位置发生变化时触发此方法
	 * 
	 * @param location
	 *            当前位置
	 */
	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			// 将当前位置转换成地理坐标点
			mMKSearch.reverseGeocode(new GeoPoint((int) (location.getLatitude() * 1e6), (int) (location.getLongitude() * 1e6)));
		}
	}

	/**
	 * 获取当前城市名
	 * 
	 * @author syghh
	 * 
	 */
	public class MySearchListener implements MKSearchListener {
		@Override
		public void onGetAddrResult(MKAddrInfo result, int iError) {
			if (result != null) {
				MKGeocoderAddressComponent kk = result.addressComponents;
				city = kk.city;
				setTitleLocation();
			}
		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult result, int iError) {
		}

		@Override
		public void onGetPoiResult(MKPoiResult result, int type, int iError) {

		}

		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult result, int iError) {
		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {

		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {

		}

		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {

		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (currentView == settingView) {
				btnLeft.performClick();
				return true;
			}
			// 当退出时，弹出推出框
			exitHome();
			return true;

		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 退出提示的Dialog,自定义。
	 */
	public void exitHome() {
		final AlertDialog dialogExit = new AlertDialog.Builder(MainHomeActivityGroup.this).create();
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
				AndroidUtil.exitApp(MainHomeActivityGroup.this); // 退出
			}
		});
	}

	/**
	 * 当跳转时的动画
	 */
	private void animaGoTo(final Context context, final Class<?> cls, View view) {
		startActivity(new Intent(context, cls));
		overridePendingTransition(R.anim.push_right_in_noalp, R.anim.push_left_out_noalp);

	}

	@Override
	protected void onResume() {
		super.onResume();
		// if (mMapManager != null) {
		// mMapManager.start();
		// }
		// if (mLocationManager != null) {
		// mLocationManager.requestLocationUpdates(this);
		// mMKSearch = new MKSearch();
		// mMKSearch.init(mMapManager, new MySearchListener());
		// }
		// 重新获取用户信息，主要用于更新用户的头像昵称等
		if (NetUtil.checkNet(this) && userBean != null) {
			new GetUsersInfoTask(SharedPrefUtil.getUserBean(this).getUserId(), SharedPrefUtil.getUserBean(this).getAccessToken()).execute();
		} else if (userBean == null) {
			ivSettingUserPhoto.setImageResource(R.drawable.bg_photo_defualt);
			tvSettingUserName.setText("登录注册");
			rlExit.setVisibility(View.GONE);
			viewBottom1.setVisibility(View.GONE);
			isLogin = false;
		}
		MobclickAgent.onResume(this);

	}

	static int left = 0;
	static boolean leftMenuOut = false;

	static class ClickListenerForScrolling implements OnClickListener {
		HorizontalScrollView scrollView;
		View view;

		/**
		 * Menu must NOT be out/shown to start with.
		 */
		public ClickListenerForScrolling(HorizontalScrollView scrollView, View view) {
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

	@Override
	protected void onPause() {
		super.onPause();
		if (mMapManager != null) {
			mMapManager.stop();
		}
		closeTimer();
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

	@Override
	protected void onRestart() {
		super.onRestart();
		startAnimationTask();
	}

	/**
	 * 获取我的订单和消息中心数目；
	 * 
	 * @author Zhoujun
	 * 
	 */
	private class GetMsgNumTask extends AsyncTask<Void, Void, JSONObject> {

		private long userId;
		private String access_token;

		public GetMsgNumTask(long userId, String access_token) {
			super();
			this.userId = userId;
			this.access_token = access_token;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
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
						int orderCount = result.getInt("orderCount");
						if (sysCount > 0) {
						} else {
						}
						if (orderCount > 0) {
						} else {
						}
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(MainHomeActivityGroup.this, R.string.time_out, Toast.LENGTH_SHORT).show();
						startActivity(new Intent(MainHomeActivityGroup.this, LoginActivity.class));
						finish();
					}
				} catch (JSONException e) {
				}
			} else {

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
						SharedPrefUtil.setUserBean(MainHomeActivityGroup.this, userBean);
						fillUserData();
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(MainHomeActivityGroup.this, R.string.time_out, Toast.LENGTH_SHORT).show();
						ivSettingUserPhoto.setImageResource(R.drawable.bg_photo_defualt);
						tvSettingUserName.setText("登录注册");
						rlExit.setVisibility(View.GONE);
						viewBottom1.setVisibility(View.GONE);
						isLogin = false;
					}
				} catch (JSONException e) {
					Toast.makeText(MainHomeActivityGroup.this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(MainHomeActivityGroup.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 检查版本更新
	 */
	private void checkVersion() {
		new AsyncTask<Void, Void, VersionBean>() {
			@Override
			protected VersionBean doInBackground(Void... params) {
				try {
					int versCode = AndroidUtil.getAppVersionCode(MainHomeActivityGroup.this);
					String versionName = AndroidUtil.getAppVersionName(MainHomeActivityGroup.this);
					JSONObject obj = new BusinessHelper().checkVersion(Constants.CHANNEL_TYPE, Constants.CLIENT_TYPE + "", versCode, versionName);
					if (obj != null) {
						versionBean = new VersionBean(obj);
					}
				} catch (JSONException e) {
					MobclickAgent.reportError(MainHomeActivityGroup.this, StringUtil.getExceptionInfo(e));
				} catch (SystemException e) {
					MobclickAgent.reportError(MainHomeActivityGroup.this, StringUtil.getExceptionInfo(e));
				}
				return versionBean;
			}

			@Override
			protected void onPostExecute(VersionBean result) {
				if (result != null) {
					String status = result.getStatus();
					if ("1".equals(status)) {// 成功
						createVersionDialog();
					}
				}
			}

		}.execute();
	}

	protected void createVersionDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("版本更新");
		String verText = versionBean.getContent();
		if (StringUtil.isBlank(verText)) {
			verText = "";
		}
		builder.setMessage(verText);
		builder.setNeutralButton("更新", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Uri uri = Uri.parse(versionBean.getUrl());
				// Uri uri =
				// Uri.parse("http://192.168.1.115:8080/ediyou/download.jsp");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});

		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				boolean isComp = versionBean.isComp();
				if (isComp) {
					AndroidUtil.exitApp(MainHomeActivityGroup.this);
				}
				dialog.dismiss();
			}
		});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				boolean isComp = versionBean.isComp();
				if (isComp) {
					AndroidUtil.exitApp(MainHomeActivityGroup.this);
				}
				dialog.dismiss();
			}
		});
		builder.create();
		builder.show();
	}
}

package com.elephant.ediyou.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.elephant.ediyou.bean.HomeRooListBean;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.bean.VersionBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.AndroidUtil;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.elephant.ediyou.view.GridViewInScrollView;
import com.elephant.ediyou.view.MyHorizontalScrollView;
import com.elephant.ediyou.view.MyHorizontalScrollView.SizeCallback;
import com.elephant.ediyou.view.ScrollViewIncludeViewPager;
import com.umeng.analytics.MobclickAgent;

/**
 * 袋鼠首页(暂未使用)
 * 
 * @author Aizhimin 说明：
 *         1、组件在xml中id的命名规范：组件缩写+组件作用描述。例：用户名输入框的命名：etUsername(et代码EditText
 *         ,username代表输入框意义) 2、组件在activity类文件中的命名和1保持一致。例： private EditText
 *         etUsername; 3、每个activity必须实现IBaseActivity接口 4、网络访问请求使用AsyncTask异步任务进行
 *         5、遵循java的代码命名规范。
 */
public class HomeActivity extends Activity implements IBaseActivity, OnClickListener, OnItemClickListener, LocationListener {
	private MyHorizontalScrollView scrollView;
	private static View settingView;
	private static View mainView;
	private static View currentView;// 当前显示的view

	// Title
	private Button btnLeft;
	private Button btnHome;
	private Button btnEvent;
	private LinearLayout llHomeCenterTitle;
	private TextView tvTitleLocation;
	private ImageView ivChoiceLocation;
	private View progress;

	private String locationStr;// title需要的定位城市

	private ScrollViewIncludeViewPager svivHome;// 主页的整体ScrollView

	// 首页活动ViewPager
	private ViewPager viewPagerHome;
	private FrameLayout flEvnetsItem;// ViewPager的item
	private ArrayList<View> views;// ViewPager的item数据
	private MyPagerAdapter myPagerAdapter;
	private RadioGroup groupPoint;// 圆点指引
	private int currentPhotoPosition;// 当前选择图片的索引
	private static final int FIRSTINDEX = 0;// ViewPager初始位置
	private static int screenWidth;// 屏幕宽度
	private static int sdkVersion;// SDK版本
	private final static int ANDROIDSDK9 = 9;// android2.3SDK
	// event的图片和文字
	private ImageView ivEventImage;
	private TextView tvEventName;
	// 自动切换时间任务
	private TimerTask timerTask;
	private Timer timer;

	// Top袋鼠
	private TopRooShowAdapter topRooShowAdapter;
	private static GridViewInScrollView gvHomeTopRooShow;

	// 袋鼠展示列表
	private RooShowAdapter rooShowAdapter;
	private static GridViewInScrollView gvHomeRooShow;
	private int pageNo = 1;// 起始页
	private int pageSize = 18;// 每页个数
	private int totalPage = -1;// 总页数
	private boolean LIST_RECORD_TASK_RUNING = false;// 加载记录的任务是否执行中，如果执行中（true），就不再创建新的任务，
	private RooListTask rooListTask;

	private ProgressDialog pd;

	private List<HomeRooListBean> rooListBeans;
	private final static int TOPROOCOUNT = 3;

	// 底部功能按钮
	private ImageView ivFunction;
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

	// gps
	private BMapManager mMapManager = null;
	private MKLocationManager mLocationManager = null;
	private MKSearch mMKSearch = null;
	private String city;// gps获取的城市名

	// 设置菜单
	private LinearLayout llSettingTitle;
	private ImageView ivSettingUserPhoto;
	private TextView tvSettingUserName;
	private RelativeLayout rlMyOrder;// 我的订单
	private RelativeLayout rlOrderToMy;// 我收到的订单
	private TextView tvMyReceivedOrderNum;// 我收到的订单数目；
	private RelativeLayout rlMsgCenter;// 消息中心
	private TextView tvMsgCenterNum;// 消息中心数目;
	private RelativeLayout rlMyAccount;// 我的账户
	private RelativeLayout rlApplyToRoo;// 申请成为袋鼠
	private RelativeLayout rlPushAndLocation;// 通知和定位
	private RelativeLayout rlShareSetting;// 分享设置
	private RelativeLayout rlHelpCenter;// 帮助中心
	private RelativeLayout rlFeedBack;// 用户反馈
	private RelativeLayout rlAbout;// 关于我们
	private RelativeLayout rlExit;

	private File PHOTO_DIR;// 头像照片目录
	private UserBean userBean;
	private VersionBean versionBean;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
		sdkVersion = AndroidUtil.getAndroidSDKVersion();
		if (SharedPrefUtil.checkToken(this)) {
			userBean = SharedPrefUtil.getUserBean(this);
		}

		locationStr = getIntent().getStringExtra("location");
		if (NetUtil.checkNet(this)) {
			checkVersion();
			if (SharedPrefUtil.getLocationSetting(this)) {
				initMap();
			}
			if (!LIST_RECORD_TASK_RUNING) {
				rooListTask = new RooListTask();
				rooListTask.execute();
			}
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
		}

		findView();
		fillData();
		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		LayoutInflater inflater = LayoutInflater.from(this);
		scrollView = (MyHorizontalScrollView) inflater.inflate(R.layout.home, null);
		setContentView(scrollView);

		// 设置界面
		settingView = inflater.inflate(R.layout.setting, null);
		mainView = inflater.inflate(R.layout.main, null);
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
		// 设置菜单title
		llSettingTitle = (LinearLayout) settingView.findViewById(R.id.llSettingTitle);
		ivSettingUserPhoto = (ImageView) settingView.findViewById(R.id.ivSettingUserPhoto);
		tvSettingUserName = (TextView) settingView.findViewById(R.id.tvSettingUserName);
		// 设置菜单上的条目组件
		// rlMyOrder = (RelativeLayout)
		// settingView.findViewById(R.id.rlMyOrder);
		// rlOrderToMy = (RelativeLayout)
		// settingView.findViewById(R.id.rlOrderToMy);
		// tvMyReceivedOrderNum = (TextView)
		// settingView.findViewById(R.id.tvMyReceivedOrderNum);
		// rlMsgCenter = (RelativeLayout)
		// settingView.findViewById(R.id.rlMsgCenter);
		// tvMsgCenterNum = (TextView)
		// settingView.findViewById(R.id.tvMsgCenterNum);
		// rlMyAccount = (RelativeLayout)
		// settingView.findViewById(R.id.rlMyAccount);
		// rlApplyToRoo = (RelativeLayout)
		// settingView.findViewById(R.id.rlApplyToRoo);
		rlPushAndLocation = (RelativeLayout) settingView.findViewById(R.id.rlPushAndLocation);
		rlShareSetting = (RelativeLayout) settingView.findViewById(R.id.rlShareSetting);
		rlHelpCenter = (RelativeLayout) settingView.findViewById(R.id.rlHelpCenter);
		rlFeedBack = (RelativeLayout) settingView.findViewById(R.id.rlFeedBack);
		rlAbout = (RelativeLayout) settingView.findViewById(R.id.rlAbout);
		rlExit = (RelativeLayout) settingView.findViewById(R.id.rlExit);

		llSettingTitle.setOnClickListener(this);
		rlMyOrder.setOnClickListener(this);
		rlOrderToMy.setOnClickListener(this);
		rlMsgCenter.setOnClickListener(this);
		rlMyAccount.setOnClickListener(this);
		rlApplyToRoo.setOnClickListener(this);
		rlPushAndLocation.setOnClickListener(this);
		rlShareSetting.setOnClickListener(this);
		rlHelpCenter.setOnClickListener(this);
		rlFeedBack.setOnClickListener(this);
		rlAbout.setOnClickListener(this);
		rlExit.setOnClickListener(this);

		if (userBean != null && userBean.getIsKangaroo() == 0) {
			ImageView ivLineAboveOrderToMy = (ImageView) this.findViewById(R.id.ivLineAboveOrderToMy);
			rlOrderToMy.setVisibility(View.GONE);
			ivLineAboveOrderToMy.setVisibility(View.GONE);
		} else if (userBean != null && userBean.getIsKangaroo() == 1) {
			rlApplyToRoo.setVisibility(View.GONE);
		}

		// title的各个组件
		btnEvent = (Button) mainView.findViewById(R.id.btnEvent);
		llHomeCenterTitle = (LinearLayout) mainView.findViewById(R.id.llHomeCenterTitle);
		llHomeCenterTitle.setVisibility(View.VISIBLE);
		tvTitleLocation = (TextView) mainView.findViewById(R.id.tvTitleLocation);
		ivChoiceLocation = (ImageView) mainView.findViewById(R.id.ivChoiceLocation);
		progress = mainView.findViewById(R.id.progress);
		progress.setVisibility(View.GONE);
		btnEvent.setOnClickListener(this);
		llHomeCenterTitle.setOnClickListener(this);

		// 主页的ScrollView
		svivHome = (ScrollViewIncludeViewPager) mainView.findViewById(R.id.svivHome);
		svivHome.getView();
		svivHome.setOnScrollListener(new ScrollViewIncludeViewPager.OnScrollListener() {
			@Override
			public void onTop() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onBottom() {
				if (NetUtil.checkNet(HomeActivity.this)) {
					if (!LIST_RECORD_TASK_RUNING && pageNo < totalPage) {
						rooListTask = new RooListTask();
						rooListTask.execute();
					}
				} else {
					Toast.makeText(HomeActivity.this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onAutoScroll(int l, int t, int oldl, int oldt) {
				// TODO Auto-generated method stub

			}
		});

		// 活动广告
		viewPagerHome = (ViewPager) mainView.findViewById(R.id.viewPagerHomeEvent);
		groupPoint = (RadioGroup) mainView.findViewById(R.id.groupPoint);
		// Top袋鼠展示列表
		gvHomeTopRooShow = (GridViewInScrollView) mainView.findViewById(R.id.gvHomeTopRooShow);
		gvHomeTopRooShow.setOnItemClickListener(this);

		// 袋鼠展示列表
		gvHomeRooShow = (GridViewInScrollView) mainView.findViewById(R.id.gvHomeRooShow);
		gvHomeRooShow.setOnItemClickListener(this);
		// gvHomeRooShow.setOnScrollListener(loadNewPageListener);

		// 底部功能按钮
		ivFunction = (ImageView) mainView.findViewById(R.id.ivFunction);
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

		ivFunction.setOnClickListener(this);
		ivFunctionOrders.setOnClickListener(this);
		ivFunctionMyhome.setOnClickListener(this);
		ivFunctionEvent.setOnClickListener(this);
		ivFunctionMsg.setOnClickListener(this);

	}

	@Override
	public void fillData() {
		setTitleLocation();
		fillPagerData();
		fillUserData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnEvent:
			// Toast.makeText(this, "正在努力开发中哦...", Toast.LENGTH_LONG).show();
			startActivity(new Intent(this, EventHomeListActivity.class));
			break;
		case R.id.llHomeCenterTitle: {
			Intent intent = new Intent(this, ChoiceHotLocationActivity.class);
			intent.putExtra(Constants.KEY_SEL_CITY, Constants.KEY_SEL_CITY_FROM_HOME);
			startActivity(intent);
			finish();
		}
			break;
		case R.id.ivFunction:// 底部功能按钮（考拉头像的）
			if (ivFunctionOrders.getVisibility() == View.VISIBLE) {
				ivFunctionOrders.startAnimation(ordersOutAnim);
				ivFunctionMyhome.startAnimation(myhomeOutAnim);
				ivFunctionEvent.startAnimation(eventOutAnim);
				ivFunctionMsg.startAnimation(msgOutAnim);
				// ivFunctionOrders.clearAnimation();
				// ivFunctionMyhome.clearAnimation();
				// ivFunctionEvent.clearAnimation();
				// ivFunctionMsg.clearAnimation();

				ivFunctionOrders.setVisibility(View.GONE);
				ivFunctionMyhome.setVisibility(View.GONE);
				ivFunctionEvent.setVisibility(View.GONE);
				ivFunctionMsg.setVisibility(View.GONE);

			} else {
				ivFunctionOrders.startAnimation(ordersInAnim);
				ivFunctionMyhome.startAnimation(myhomeInAnim);
				ivFunctionEvent.startAnimation(eventInAnim);
				ivFunctionMsg.startAnimation(msgInAnim);

				// ivFunctionOrders.clearAnimation();
				// ivFunctionMyhome.clearAnimation();
				// ivFunctionEvent.clearAnimation();
				// ivFunctionMsg.clearAnimation();

				ivFunctionOrders.setVisibility(View.VISIBLE);
				ivFunctionMyhome.setVisibility(View.VISIBLE);
				ivFunctionEvent.setVisibility(View.VISIBLE);
				ivFunctionMsg.setVisibility(View.VISIBLE);

			}
			break;
		case R.id.ivFunctionOrders:// 跳转到我的订单列表
			Toast.makeText(this, "正在努力开发中哦...", Toast.LENGTH_LONG).show();
			break;
		case R.id.ivFunctionMyhome:// 跳转到个人的主页
			if (SharedPrefUtil.checkToken(this)) {
				if (SharedPrefUtil.getUserBean(this).getIsKangaroo() == 0) {
					int userId = SharedPrefUtil.getUserBean(this).getUserId();
					Intent intentToMyhome = new Intent(this, KoalaProfileActivity.class);
					intentToMyhome.putExtra("koalaId", (long) userId);
					startActivity(intentToMyhome);
				} else if (SharedPrefUtil.getUserBean(this).getIsKangaroo() == 1) {
					int userId = SharedPrefUtil.getUserBean(this).getUserId();
					Intent intentToMyhome = new Intent(this, RooProfileActivity.class);
					intentToMyhome.putExtra("uid", (long) userId);
					intentToMyhome.putExtra("rooId", SharedPrefUtil.getRooId(this));// 袋鼠id
					startActivity(intentToMyhome);
				}

			} else {
				startActivity(new Intent(this, LoginActivity.class));
				finish();
			}
			break;
		case R.id.ivFunctionEvent:// 搜索按钮
			// Toast.makeText(this, "正在努力开发中哦...", Toast.LENGTH_LONG).show();
			startActivity(new Intent(this, RooSearchActivity.class));
			break;
		case R.id.ivFunctionMsg:// 跳转到我的私信
			Intent msgCenter1 = new Intent(HomeActivity.this, MessageCenterActivity.class);
			startActivity(msgCenter1);
			break;
		case R.id.llSettingTitle:// 个人主页
			if (SharedPrefUtil.checkToken(this)) {
				if (SharedPrefUtil.getUserBean(this).getIsKangaroo() == 0) {
					int userId = SharedPrefUtil.getUserBean(this).getUserId();
					Intent intentToMyhome = new Intent(this, KoalaProfileActivity.class);
					intentToMyhome.putExtra("koalaId", (long) userId);
					startActivity(intentToMyhome);
				} else if (SharedPrefUtil.getUserBean(this).getIsKangaroo() == 1) {
					int userId = SharedPrefUtil.getUserBean(this).getUserId();
					Intent intentToMyhome = new Intent(this, RooProfileActivity.class);
					intentToMyhome.putExtra("uid", (long) userId);
					intentToMyhome.putExtra("rooId", SharedPrefUtil.getRooId(this));// 袋鼠id
					startActivity(intentToMyhome);
				}

			} else {
				startActivity(new Intent(this, LoginActivity.class));
				finish();
			}
			break;
		// case R.id.rlMyOrder:// 我的订单
		// if (SharedPrefUtil.checkToken(this)) {
		// startActivity(new Intent(this, MyOrderActivity.class));
		// } else {
		// startActivity(new Intent(this, LoginActivity.class));
		// finish();
		// }

		// break;
		// case R.id.rlOrderToMy:// 我 收到 的订单
		// if (SharedPrefUtil.checkToken(this)) {
		// startActivity(new Intent(this, MyReceivedOrderActivity.class));
		// } else {
		// startActivity(new Intent(this, LoginActivity.class));
		// finish();
		// }
		// break;
		// case R.id.rlMsgCenter:// 信息中心
		// Intent msgCenter = new Intent(HomeActivity.this,
		// MessageCenterActivity.class);
		// startActivity(msgCenter);
		// break;
		// case R.id.rlMyAccount:// 我的账户
		// Intent myAccount = new Intent(HomeActivity.this,
		// MyAccountActivity.class);
		// startActivity(myAccount);
		// break;
		// case R.id.rlApplyToRoo:// 申请成为袋鼠
		// Intent applyRoo = new Intent(HomeActivity.this,
		// RooApplyActivity.class);
		// startActivity(applyRoo);
		// break;
		case R.id.rlPushAndLocation:// 通知和定位设置
			startActivity(new Intent(this, NotificationAndLocationSettingActivity.class));
			break;
		case R.id.rlShareSetting:// 分享设置
			startActivity(new Intent(this, AccountBindActivity.class));
			break;
		case R.id.rlHelpCenter:// 帮助中心

			break;
		case R.id.rlFeedBack:// 用户反馈

			break;
		case R.id.rlAbout:// 关于我们

			break;
		case R.id.rlExit: {
			SharedPrefUtil.clearUserBean(HomeActivity.this);
			Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
			startActivity(intent);
		}
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.gvHomeTopRooShow:
			HomeRooListBean topRooBean = (HomeRooListBean) topRooShowAdapter.getItem(position);
			Intent topIntent = new Intent(HomeActivity.this, RooProfileActivity.class);
			topIntent.putExtra("uid", (long) topRooBean.getUid());// userId
			topIntent.putExtra("rooId", topRooBean.getKangarooId());// 袋鼠id
			startActivity(topIntent);
			break;
		case R.id.gvHomeRooShow:
			HomeRooListBean rooBean = (HomeRooListBean) rooShowAdapter.getItem(position);
			Intent intent = new Intent(HomeActivity.this, RooProfileActivity.class);
			intent.putExtra("uid", (long) rooBean.getUid());// userId
			intent.putExtra("rooId", rooBean.getKangarooId());// 袋鼠id
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	/**
	 * 设置当前城市
	 */
	public void setTitleLocation() {
		if (!StringUtil.isBlank(locationStr)) {
			tvTitleLocation.setText(locationStr);
			SharedPrefUtil.setChoicedLocation(this, locationStr);
		} else if (StringUtil.isBlank(locationStr) && !StringUtil.isBlank(SharedPrefUtil.getChoicedLocation(this))) {
			tvTitleLocation.setText(SharedPrefUtil.getChoicedLocation(this));
		} else {
			tvTitleLocation.setText(city);
		}
	}

	/**
	 * 加载用户头像、昵称等数据
	 */
	private void fillUserData() {
		// 设置菜单的title的用户头像、昵称
		if (userBean != null) {
			// 设置用户名字
			tvSettingUserName.setText(userBean.getNickname());
			PHOTO_DIR = new File(Environment.getExternalStorageDirectory() + "/" + Constants.APP_DIR_NAME + "/");
			// 设置用户头像
			if (!StringUtil.isBlank(userBean.getAvatarUrl())) {
				File avatarFile = new File(PHOTO_DIR, ImageUtil.createAvatarFileName(String.valueOf(userBean.getUserId())));
				if ((!NetUtil.checkNet(this)) && avatarFile != null && avatarFile.exists()) {
					Drawable cacheDrawable = ImageCacheLoader.getInstance().fetchLocal(avatarFile.getPath());
					ivSettingUserPhoto.setImageDrawable(cacheDrawable);
				} else {
					Drawable cacheDrawable = ImageCacheLoader.getInstance().loadDrawable(userBean.getAvatarUrl(), new ImageCallback() {
						@Override
						public void imageLoaded(Drawable imageDrawable, String imageUrl) {
							if (imageDrawable != null) {
								ivSettingUserPhoto.setImageDrawable(imageDrawable);
							} else {
								if (userBean.getIsKangaroo() == 1) {

									ivSettingUserPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
								} else
									ivSettingUserPhoto.setImageResource(R.drawable.bg_photo_defualt);
							}
						}
					});
					if (cacheDrawable != null) {
						ivSettingUserPhoto.setImageDrawable(cacheDrawable);
					} else {
						if (userBean.getIsKangaroo() == 1) {

							ivSettingUserPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
						} else
							ivSettingUserPhoto.setImageResource(R.drawable.bg_photo_defualt);
					}
				}
			}
		}
	}

	/**
	 * ViewPager加载数据到组件
	 * 
	 * @param proms
	 */
	private void fillPagerData(/* final ArrayList<EventsBean> eventsBeans */) {
		views = new ArrayList<View>();
		for (int i = 0; i < 3; i++) {
			flEvnetsItem = (FrameLayout) getLayoutInflater().inflate(R.layout.events_item, null);
			ivEventImage = (ImageView) flEvnetsItem.findViewById(R.id.ivEventImage);
			tvEventName = (TextView) flEvnetsItem.findViewById(R.id.tvEventName);

			LayoutParams lp = ivEventImage.getLayoutParams();
			lp.width = screenWidth - 10;
			ivEventImage.setLayoutParams(lp);
			ivEventImage.setImageResource(R.drawable.view);
			tvEventName.setText("游丽江古城，品瑶寨风情");
			// --------------------------------------------------begin
			/*
			 * String imgUrl = eventsBeans.get(i).getImg();
			 * ivEventImage.setTag(imgUrl); Drawable cacheDrawable =
			 * ImageCacheLoader.getInstance() .loadDrawable(imgUrl, new
			 * ImageCallback() { public void imageLoaded(Drawable imageDrawable,
			 * String imageUrl) { ImageView ivImageView = (ImageView)
			 * viewPagerHome .findViewWithTag(imageUrl); if (ivImageView !=
			 * null) { if (imageDrawable != null) { int oldwidth = imageDrawable
			 * .getIntrinsicWidth(); int oldheight = imageDrawable
			 * .getIntrinsicHeight(); LayoutParams lp = ivImageView
			 * .getLayoutParams(); lp.width = screenWidth; lp.height =
			 * (oldheight * screenWidth) / oldwidth;
			 * ivImageView.setLayoutParams(lp);
			 * ivImageView.setImageDrawable(imageDrawable); } else {
			 * ivEventImage .setImageResource(R.drawable.view); } } } }); if
			 * (cacheDrawable != null) { int oldwidth =
			 * cacheDrawable.getIntrinsicWidth(); int oldheight =
			 * cacheDrawable.getIntrinsicHeight(); LayoutParams lp =
			 * ivEventImage.getLayoutParams(); lp.width = screenWidth; lp.height
			 * = (oldheight * screenWidth) / oldwidth;
			 * ivEventImage.setLayoutParams(lp);
			 * ivEventImage.setImageDrawable(cacheDrawable); } else {
			 * ivEventImage.setImageResource(R.drawable.view); }
			 */

			// --------------------------------------------------------end
			views.add(flEvnetsItem);

			flEvnetsItem.setClickable(true);
			flEvnetsItem.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i("HomeActivity", "currentPhotoPosition % views.size():--" + currentPhotoPosition % views.size());
					switch (currentPhotoPosition % views.size()) {
					case 0:
						// Intent intent = new Intent(HomeActivity.this,
						// SpecialPriceActivity.class);
						// intent.putExtra("event",
						// eventsBeans.get(currentPhotoPosition %
						// views.size()));
						// startActivity(intent);
						break;
					case 1:
						// intent = new Intent(HomeActivity.this,
						// GroupBuyActivity.class);
						// intent.putExtra("event",
						// eventsBeans.get(currentPhotoPosition %
						// views.size()));
						// startActivity(intent);
						break;
					case 2:
						// intent = new Intent(HomeActivity.this,
						// ShopRecommendActivity.class);
						// intent.putExtra("event",
						// eventsBeans.get(currentPhotoPosition %
						// views.size()));
						// startActivity(intent);
						break;
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
					case 8:
					case 9:
						// intent = new Intent(HomeActivity.this,
						// SpecialPriceActivity.class);
						// intent.putExtra("event",
						// eventsBeans.get(currentPhotoPosition %
						// views.size()));
						// startActivity(intent);
						break;
					}
				}
			});
		}
		// 添加指引圆点
		addPointView(views.size());

		if (myPagerAdapter == null) {
			myPagerAdapter = new MyPagerAdapter(this, views);
		} else {
			myPagerAdapter.clear();
			myPagerAdapter.setData(views);
		}
		viewPagerHome.setAdapter(myPagerAdapter);
		viewPagerHome.setCurrentItem(FIRSTINDEX);
		myPagerAdapter.notifyDataSetChanged();
		viewPagerHome.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int index) {
				currentPhotoPosition = index;
				View child = groupPoint.getChildAt(index % views.size());
				if (child instanceof RadioButton) {
					RadioButton radBtn = (RadioButton) child;
					radBtn.setChecked(true);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		// 启动活动ViewPager的自动切换
		startTask();
	}

	private void addPointView(int count) {
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				RadioButton radBtn = new RadioButton(this);
				radBtn.setClickable(false);
				RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(25, RadioGroup.LayoutParams.WRAP_CONTENT);
				radBtn.setButtonDrawable(R.drawable.ic_slide_point_selector);
				groupPoint.addView(radBtn, params);
			}
		}
		View v = groupPoint.getChildAt(0);
		RadioButton radioBtn = (RadioButton) v;
		radioBtn.setChecked(true);
	}

	/**
	 * 启动viewPager的自动切换
	 */
	private void startTask() {
		if (timerTask == null) {
			timerTask = new TimerTask() {
				@Override
				public void run() {
					viewHandler.sendEmptyMessage(currentPhotoPosition);
					whatOption();
				}
			};
			timer = new Timer();
			timer.schedule(timerTask, 0, 5000);
		}
	}

	private void closeTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (timerTask != null) {
			timerTask = null;
		}
	}

	/**
	 * 操作圆点轮换变背景
	 */
	private void whatOption() {
		currentPhotoPosition++;

	}

	/**
	 * 处理定时切换广告栏图片的句柄
	 */
	private final Handler viewHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			viewPagerHome.setCurrentItem(msg.what);
			super.handleMessage(msg);
		}

	};

	/**
	 * ViewPager的适配器
	 * 
	 * @author syghh
	 * 
	 */
	private class MyPagerAdapter extends PagerAdapter {
		private Context mContext;
		private ArrayList<View> views;

		public MyPagerAdapter(Context context, ArrayList<View> views) {
			this.mContext = context;
			this.views = views;
		}

		@Override
		public int getCount() {
			return Integer.MAX_VALUE;
		}

		public void setData(ArrayList<View> views) {
			this.views = views;
		}

		private void clear() {
			if (views != null)
				views.clear();
			this.notifyDataSetChanged();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			// ((ViewPager) container).removeView(listViews.get(position));
		}

		@Override
		public Object instantiateItem(View container, int position) {
			try {
				((ViewPager) container).addView(views.get(position % views.size()), 0);
			} catch (Exception e) {

			}
			return views.get(position % views.size());
		}

	}

	/**
	 * 首页Top袋鼠
	 * 
	 * @author syghh
	 * 
	 */
	private static class TopRooShowAdapter extends BaseAdapter {
		private Context mContext;
		private List<HomeRooListBean> rooBeans;
		private boolean isNull = false;

		public TopRooShowAdapter(Context context) {
			this.mContext = context;
		}

		public void setData(List<HomeRooListBean> rooBeans) {
			if (rooBeans.isEmpty()) {
				isNull = true;
			} else {
				isNull = false;
			}
			this.rooBeans = rooBeans;
		}

		private void clear() {
			if (rooBeans != null)
				rooBeans.clear();
			isNull = true;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return TOPROOCOUNT;
		}

		@Override
		public Object getItem(int position) {
			return rooBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.home_top_roo_show_item, null);
				viewHolder = new ViewHolder();
				viewHolder.ivRoo = (ImageView) convertView.findViewById(R.id.ivRoo);
				viewHolder.ivGender = (ImageView) convertView.findViewById(R.id.ivGender);
				viewHolder.ivTopLv = (ImageView) convertView.findViewById(R.id.ivTopLv);
				viewHolder.tvAge = (TextView) convertView.findViewById(R.id.tvAge);
				viewHolder.tvLevel = (TextView) convertView.findViewById(R.id.tvLevel);
				viewHolder.tvBadge = (TextView) convertView.findViewById(R.id.tvBadge);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			if (position == 0) {
				viewHolder.ivTopLv.setImageResource(R.drawable.ic_top1);
			} else if (position == 1) {
				viewHolder.ivTopLv.setImageResource(R.drawable.ic_top2);
			} else if (position == 2) {
				viewHolder.ivTopLv.setImageResource(R.drawable.ic_top3);
			}

			if (isNull) {
				viewHolder.ivRoo.setImageResource(R.drawable.bg_kangoo_photo_defualt);
			}
			HomeRooListBean rooBean = rooBeans.get(position);
			if (rooBean.getGender() == "m") {
				viewHolder.ivGender.setImageResource(R.drawable.ic_male);
			} else if (rooBean.getGender() == "f") {
				viewHolder.ivGender.setImageResource(R.drawable.ic_fale);
			}
			viewHolder.tvAge.setText(rooBean.getAge());
			viewHolder.tvLevel.setText("lv" + rooBean.getKangarooLevel());
			viewHolder.tvBadge.setText(rooBean.getkTitle());
			String catImageUrl = rooBean.getAvatarUrl();
			viewHolder.ivRoo.setTag(catImageUrl);
			Drawable cacheDrawable = ImageCacheLoader.getInstance().loadDrawable(rooBean.getAvatarUrl(), new ImageCallback() {
				@Override
				public void imageLoaded(Drawable imageDrawable, String imageUrl) {
					ImageView ivPhoto = (ImageView) gvHomeTopRooShow.findViewWithTag(imageUrl);
					if (ivPhoto != null) {
						if (imageDrawable != null) {

							ivPhoto.setImageDrawable(imageDrawable);
							TopRooShowAdapter.this.notifyDataSetChanged();
						} else {
							ivPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
						}
					}
				}
			});
			if (cacheDrawable != null) {
				viewHolder.ivRoo.setImageDrawable(cacheDrawable);
			} else {
				viewHolder.ivRoo.setImageResource(R.drawable.bg_kangoo_photo_defualt);
			}
			return convertView;
		}

		static class ViewHolder {
			ImageView ivRoo;
			ImageView ivGender;
			ImageView ivTopLv;
			TextView tvAge;
			TextView tvLevel;
			TextView tvBadge;
		}
	}

	/**
	 * 首页Top袋鼠下方的其他袋鼠
	 * 
	 * @author syghh
	 * 
	 */
	private static class RooShowAdapter extends BaseAdapter {
		private Context mContext;
		private List<HomeRooListBean> rooBeans;
		private boolean isNull = false;

		public RooShowAdapter(Context context) {
			this.mContext = context;
		}

		public void setData(List<HomeRooListBean> rooBeans) {
			if (rooBeans.isEmpty()) {
				isNull = true;
			} else {
				isNull = false;
			}
			this.rooBeans = rooBeans;
		}

		public void add(List<HomeRooListBean> rooBeans) {
			this.rooBeans.addAll(rooBeans);
			this.notifyDataSetChanged();
		}

		private void clear() {
			if (rooBeans != null)
				rooBeans.clear();
			isNull = true;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return rooBeans.size();
		}

		@Override
		public Object getItem(int position) {
			return rooBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.home_roo_show_item, null);
				viewHolder = new ViewHolder();
				viewHolder.ivRoo = (ImageView) convertView.findViewById(R.id.ivRoo);
				viewHolder.ivGender = (ImageView) convertView.findViewById(R.id.ivGender);
				viewHolder.tvAge = (TextView) convertView.findViewById(R.id.tvAge);
				viewHolder.tvLevel = (TextView) convertView.findViewById(R.id.tvLevel);
				viewHolder.tvBadge = (TextView) convertView.findViewById(R.id.tvBadge);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			if (isNull) {
				viewHolder.ivRoo.setImageResource(R.drawable.bg_kangoo_photo_defualt);
			}
			HomeRooListBean rooBean = rooBeans.get(position);
			if (rooBean.getGender() == "m") {
				viewHolder.ivGender.setImageResource(R.drawable.ic_male);
			} else if (rooBean.getGender() == "f") {
				viewHolder.ivGender.setImageResource(R.drawable.ic_fale);
			}
			viewHolder.tvAge.setText(rooBean.getAge());
			viewHolder.tvLevel.setText("lv" + rooBean.getKangarooLevel());
			viewHolder.tvBadge.setText(rooBean.getkTitle());
			String catImageUrl = rooBean.getAvatarUrl();
			viewHolder.ivRoo.setTag(catImageUrl);
			Drawable cacheDrawable = ImageCacheLoader.getInstance().loadDrawable(rooBean.getAvatarUrl(), new ImageCallback() {
				@Override
				public void imageLoaded(Drawable imageDrawable, String imageUrl) {
					ImageView ivPhoto = (ImageView) gvHomeRooShow.findViewWithTag(imageUrl);
					if (ivPhoto != null) {
						if (imageDrawable != null) {

							ivPhoto.setImageDrawable(imageDrawable);
							RooShowAdapter.this.notifyDataSetChanged();
						} else {
							ivPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
						}
					}
				}
			});
			if (cacheDrawable != null) {
				viewHolder.ivRoo.setImageDrawable(cacheDrawable);
			} else {
				viewHolder.ivRoo.setImageResource(R.drawable.bg_kangoo_photo_defualt);
			}
			return convertView;
		}

		static class ViewHolder {
			ImageView ivRoo;
			ImageView ivGender;
			TextView tvAge;
			TextView tvLevel;
			TextView tvBadge;
		}
	}

	/**
	 * 袋鼠列表
	 * 
	 * @author syghh
	 * 
	 */
	class RooListTask extends AsyncTask<Void, Void, JSONObject> {
		public RooListTask() {
			LIST_RECORD_TASK_RUNING = true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(HomeActivity.this);
				pd.setMessage("努力加载袋鼠列表中...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			// try {
			// return new BusinessHelper().kangarooList(pageNo, pageSize);
			// } catch (SystemException e) {
			// e.printStackTrace();
			// }
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
						totalPage = result.getInt("total");
						JSONArray rooListJson = result.getJSONArray("data");
						rooListBeans = HomeRooListBean.constractList(rooListJson);
						// Top袋鼠数据
						if (pageNo == 1) {
							topRooShowAdapter = new TopRooShowAdapter(HomeActivity.this);
							topRooShowAdapter.setData(rooListBeans);
							gvHomeTopRooShow.setAdapter(topRooShowAdapter);
						}
						// 袋鼠列表数据
						List<HomeRooListBean> rooListBeansNor = new ArrayList<HomeRooListBean>();
						for (int i = TOPROOCOUNT; i < rooListBeans.size(); i++) {
							rooListBeansNor.add(rooListBeans.get(i));
						}
						if (rooShowAdapter == null || rooShowAdapter.getCount() == 0) {
							rooShowAdapter = new RooShowAdapter(HomeActivity.this);
							rooShowAdapter.setData(rooListBeansNor);
							gvHomeRooShow.setAdapter(rooShowAdapter);
							pageNo = 1;
						} else {
							rooShowAdapter.add(rooListBeansNor);
						}
						pageNo = pageNo + 1;

						Toast.makeText(HomeActivity.this, "加载成功", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(HomeActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(HomeActivity.this, "加载失败", Toast.LENGTH_LONG).show();
				} catch (SystemException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(HomeActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
			// 加载记录的任务结束
			LIST_RECORD_TASK_RUNING = false;
		}
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
			MKGeocoderAddressComponent kk = result.addressComponents;
			city = kk.city;
			setTitleLocation();
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
			// 当progress存在时，点击返回取消progress
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
		final AlertDialog dialogExit = new AlertDialog.Builder(HomeActivity.this).create();
		dialogExit.show();
		Window dialogWindow = dialogExit.getWindow();
		dialogWindow.setContentView(R.layout.exit_dialog);
		Button ok = (Button) dialogWindow.findViewById(R.id.btn_ok);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();
				AndroidUtil.exitApp(HomeActivity.this); // 退出
			}
		});
		// 关闭对话框架
		Button cancel = (Button) dialogWindow.findViewById(R.id.btn_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();
			}
		});
	}

	@Override
	protected void onResume() {
		if (mMapManager != null) {
			mMapManager.start();
		}
		if (mLocationManager != null) {
			mLocationManager.requestLocationUpdates(this);
			mMKSearch = new MKSearch();
			mMKSearch.init(mMapManager, new MySearchListener());
		}
		startTask();
		if (NetUtil.checkNet(this)) {
			new GetMsgNumTask(userBean.getUserId(), userBean.getAccessToken()).execute();
		}
		super.onResume();
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
					scrollView.smoothScrollTo(left, 0);
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
							tvMsgCenterNum.setVisibility(View.VISIBLE);
							tvMsgCenterNum.setText(sysCount + "");
						} else {
							tvMsgCenterNum.setVisibility(View.GONE);
						}
						if (orderCount > 0) {
							tvMyReceivedOrderNum.setVisibility(View.VISIBLE);
							tvMyReceivedOrderNum.setText(orderCount + "");
						} else {
							tvMyReceivedOrderNum.setVisibility(View.GONE);
						}
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(HomeActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(HomeActivity.this, LoginActivity.class));
					}
				} catch (JSONException e) {
				}
			} else {

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
					int versCode = AndroidUtil.getAppVersionCode(HomeActivity.this);
					String versionName = AndroidUtil.getAppVersionName(HomeActivity.this);
					JSONObject obj = new BusinessHelper().checkVersion(Constants.CHANNEL_TYPE, Constants.CLIENT_TYPE + "", versCode, versionName);
					if (obj != null) {
						versionBean = new VersionBean(obj);
					}
				} catch (JSONException e) {
					MobclickAgent.reportError(HomeActivity.this, StringUtil.getExceptionInfo(e));
				} catch (SystemException e) {
					MobclickAgent.reportError(HomeActivity.this, StringUtil.getExceptionInfo(e));
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
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});

		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				boolean isComp = versionBean.isComp();
				if (isComp) {
					AndroidUtil.exitApp(HomeActivity.this);
				}
				dialog.dismiss();
			}
		});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				boolean isComp = versionBean.isComp();
				if (isComp) {
					AndroidUtil.exitApp(HomeActivity.this);
				}
				dialog.dismiss();
			}
		});
		builder.create();
		builder.show();
	}
}

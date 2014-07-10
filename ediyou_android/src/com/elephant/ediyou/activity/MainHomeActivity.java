package com.elephant.ediyou.activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.ImageCacheLoader;
import com.elephant.ediyou.ImageCacheLoader.ImageCallback;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.BannerListBean;
import com.elephant.ediyou.bean.HomeRooListBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.AndroidUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.view.GridViewInScrollView;
import com.elephant.ediyou.view.ScrollViewIncludeViewPager;
import com.umeng.analytics.MobclickAgent;

/**
 * 袋鼠首页
 * 
 * @author Aizhimin 说明：
 *         1、组件在xml中id的命名规范：组件缩写+组件作用描述。例：用户名输入框的命名：etUsername(et代码EditText
 *         ,username代表输入框意义) 2、组件在activity类文件中的命名和1保持一致。例： private EditText
 *         etUsername; 3、每个activity必须实现IBaseActivity接口 4、网络访问请求使用AsyncTask异步任务进行
 *         5、遵循java的代码命名规范。
 */
public class MainHomeActivity extends FragmentActivity implements IBaseActivity, OnItemClickListener{

	private View progress;

	private ScrollViewIncludeViewPager svivHome;// 主页的整体ScrollView

	// 首页活动ViewPager
	private ViewPager viewPagerHome;
	private FrameLayout flEvnetsItem;// ViewPager的item
	private BannerAdapter bannerAdapter;
	private RadioGroup groupPoint;// 圆点指引
	private int currentPhotoPosition;// 当前选择图片的索引
	private static final int FIRSTINDEX = 0;// ViewPager初始位置
	private static int screenWidth;// 屏幕宽度
	private static int sdkVersion;// SDK版本
	private final static int ANDROIDSDK9 = 9;// android2.3SDK

	private ArrayList<BannerListBean> bannerListBeans = new ArrayList<BannerListBean>();
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

	int cityId = -1;// 城市Id
	int pId = -1;// 省份Id
	
//	private ImageFetcher mImageFetcher;

	//当当地无袋鼠时的提示
	private TextView tvShowNo;
	
	private int bannerWidth;
	private int bannerHeight;
	
	private static int itemWith;//袋鼠item的宽度
	private com.elephant.ediyou.imagecache2.ImageFetcher mBigImageFetcher;
	
	public com.elephant.ediyou.imagecache2.ImageFetcher getImageFetcher(){
		return  mBigImageFetcher;
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_home);
		screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
		
		com.elephant.ediyou.imagecache2.ImageCache.ImageCacheParams cacheParams =
	                new com.elephant.ediyou.imagecache2.ImageCache.ImageCacheParams(this, Constants.APP_DIR_NAME);
	        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
	        cacheParams.memoryCacheEnabled = false;
	        cacheParams.compressQuality = 80;
	        bannerWidth = screenWidth - 10;
	        bannerHeight = (screenWidth-120)/2;
	        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
	        mBigImageFetcher = new com.elephant.ediyou.imagecache2.ImageFetcher(this, bannerWidth);
	        mBigImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
	        mBigImageFetcher.setImageFadeIn(false);
		
		cityId = getIntent().getIntExtra("cityId", -1);
		pId = getIntent().getIntExtra("pId", -1);
		
		sdkVersion = AndroidUtil.getAndroidSDKVersion();
		itemWith = (screenWidth-20)/3;

		findView();
		if (NetUtil.checkNet(this)) {
			// 加载首页Bannner数据
			new LoadBannerListTask(cityId, pId).execute();
			if (!LIST_RECORD_TASK_RUNING) {
				rooListTask = new RooListTask();
				rooListTask.execute();
			}

		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
		}
		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		// 主页的ScrollView
		svivHome = (ScrollViewIncludeViewPager) this.findViewById(R.id.svivHome);
		svivHome.getView();
		svivHome.setOnScrollListener(new ScrollViewIncludeViewPager.OnScrollListener() {
			@Override
			public void onTop() {

			}

			@Override
			public void onScroll() {

			}

			@Override
			public void onBottom() {
				if (NetUtil.checkNet(MainHomeActivity.this)) {
					if (!LIST_RECORD_TASK_RUNING && pageNo <= totalPage) {
						rooListTask = new RooListTask();
						rooListTask.execute();
					}
				} else {
					Toast.makeText(MainHomeActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onAutoScroll(int l, int t, int oldl, int oldt) {

			}
		});

		// 活动广告
		viewPagerHome = (ViewPager) this.findViewById(R.id.viewPagerHomeEvent);
		viewPagerHome.setLayoutParams(new FrameLayout.LayoutParams(bannerWidth, bannerHeight));
		groupPoint = (RadioGroup) this.findViewById(R.id.groupPoint);
		// Top袋鼠展示列表
		gvHomeTopRooShow = (GridViewInScrollView) this.findViewById(R.id.gvHomeTopRooShow);
		gvHomeTopRooShow.setOnItemClickListener(this);

		// 袋鼠展示列表
		gvHomeRooShow = (GridViewInScrollView) this.findViewById(R.id.gvHomeRooShow);
		gvHomeRooShow.setOnItemClickListener(this);

		tvShowNo = (TextView) this.findViewById(R.id.tvShowNo);
		
	}

	@Override
	public void fillData() {
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.gvHomeTopRooShow:
			HomeRooListBean topRooBean = (HomeRooListBean) topRooShowAdapter.getItem(position);
			// Intent topIntent = new Intent(MainHomeActivity.this,
			// RooProfileActivity.class);
			Intent topIntent = new Intent(MainHomeActivity.this, RooShowActivity.class);
			topIntent.putExtra("uid", (long) topRooBean.getUid());// userId
			topIntent.putExtra("rooId", topRooBean.getKangarooId());// 袋鼠id
			startActivity(topIntent);
			break;
		case R.id.gvHomeRooShow:
			HomeRooListBean rooBean = (HomeRooListBean) rooShowAdapter.getItem(position);
			Intent intent = new Intent(MainHomeActivity.this, RooShowActivity.class);
			intent.putExtra("uid", (long) rooBean.getUid());// userId
			intent.putExtra("rooId", rooBean.getKangarooId());// 袋鼠id
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	/**
	 * ViewPager加载数据到组件
	 * 
	 * @param proms
	 */
	private void fillPagerData(final ArrayList<BannerListBean> bannerListBeans) {
		bannerAdapter = new BannerAdapter(getSupportFragmentManager(), bannerListBeans);
		// 添加指引圆点
		addPointView(bannerListBeans.size());
		viewPagerHome.setAdapter(bannerAdapter);
		viewPagerHome.setCurrentItem(FIRSTINDEX);

		viewPagerHome.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int index) {
				currentPhotoPosition = index;
				View child = groupPoint.getChildAt(index % bannerListBeans.size());
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

	private boolean isEnd = false;
	/**
	 * 操作圆点轮换变背景
	 */
	private void whatOption() {
		if(currentPhotoPosition == bannerAdapter.getCount() - 1){
			isEnd = true;
		}
		if(currentPhotoPosition == 0){
			isEnd = false;
		}
		if(isEnd){
			currentPhotoPosition--;
		}else{
			currentPhotoPosition++;
		}
		
	}

	/**
	 * 推荐首页Banner图片
	 * @author ISP
	 *
	 */
	private class BannerAdapter extends FragmentPagerAdapter{

		private List <BannerListBean> bannerListBeans;
		private FragmentManager fm;
		public BannerAdapter(FragmentManager fm) {
			super(fm);
		}

		public BannerAdapter(FragmentManager fm,
				List<BannerListBean> bannerListBeans) {
			super(fm);
			this.fm = fm;
			this.bannerListBeans = bannerListBeans;
		}

		@Override
		public int getCount() {
			return bannerListBeans.size();
		}


		@Override
		public Fragment getItem(int arg0) {
			final int position = arg0;
			String title = bannerListBeans.get(arg0).getTitleName();
			MainHomeBannerFragment fragment = MainHomeBannerFragment.newInstance(bannerListBeans.get(arg0).getPicUrlApp(),title);
			fragment.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int bannerType = bannerListBeans.get(position).getType();
					if (bannerType == Constants.BANNER_TYPE_EVENT) {
						// 跳活动详情
						Intent intent = new Intent(MainHomeActivity.this, EventDetailActivity.class);
						intent.putExtra("activityId", bannerListBeans.get(position).getActivityId());
						startActivity(intent);
					} else if (bannerType == Constants.BANNER_TYPE_WEB) {
						// web浏览器
						Intent intent = new Intent(MainHomeActivity.this, BannerItemWebViewActivity.class);
						intent.putExtra("url", bannerListBeans.get(position).getUrl());
						intent.putExtra("name", bannerListBeans.get(position).getTitleName());
						startActivity(intent);
					}
					
				}
			});
			return fragment;
		}

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
		private ArrayList<View> views;

		@Override
		public int getCount() {
			return Integer.MAX_VALUE;
		}

		public void setData(ArrayList<View> views) {
			this.views = views;
			notifyDataSetChanged();
		}

		private void clear() {
			if (views != null)
				views.clear();
			// this.notifyDataSetChanged();
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
	private class TopRooShowAdapter extends BaseAdapter {
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
			if(rooBeans!=null){
				int size = rooBeans.size();
				if(size>TOPROOCOUNT){
					size = TOPROOCOUNT;
				}
				return size;
			}else{
				return 0;
			}
			
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
			int rooSize = rooBeans.size();
			if(position<rooSize){
				HomeRooListBean rooBean = rooBeans.get(position);
				if (rooBean.getGender().equals("m")) {
					viewHolder.ivGender.setImageResource(R.drawable.ic_male);
				} else if (rooBean.getGender().equals("f")) {
					viewHolder.ivGender.setImageResource(R.drawable.ic_fale);
				}
				viewHolder.tvAge.setText(rooBean.getAge());
				viewHolder.tvLevel.setText("lv" + rooBean.getKangarooLevel());
				viewHolder.tvBadge.setText(rooBean.getkTitle());
				String catImageUrl = rooBean.getAvatarUrl();
				
				LayoutParams lp = viewHolder.ivRoo.getLayoutParams();
				lp.width = itemWith;
				lp.height = itemWith;
				viewHolder.ivRoo.setLayoutParams(lp);
				
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
			}
			return convertView;
		}

		class ViewHolder {
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
	private class RooShowAdapter extends BaseAdapter {
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
			if ("m".equals(rooBean.getGender())) {
				viewHolder.ivGender.setImageResource(R.drawable.ic_male);
			} else if ("f".equals(rooBean.getGender())) {
				viewHolder.ivGender.setImageResource(R.drawable.ic_fale);
			}
			viewHolder.tvAge.setText(rooBean.getAge());
			viewHolder.tvLevel.setText("lv" + rooBean.getKangarooLevel());
			viewHolder.tvBadge.setText(rooBean.getkTitle());
			LayoutParams lp = viewHolder.ivRoo.getLayoutParams();
			lp.width = itemWith;
			lp.height = itemWith;
			viewHolder.ivRoo.setLayoutParams(lp);
			String catImageUrl = rooBean.getAvatarUrl();
//			if (mImageFetcher != null) {
//				mImageFetcher.loadImage(catImageUrl, viewHolder.ivRoo);
//			} else{
//				viewHolder.ivRoo.setImageResource(R.drawable.bg_kangoo_photo_defualt);
//			}
			if(catImageUrl!=null){
				viewHolder.ivRoo.setTag(catImageUrl);
				Drawable cacheDrawable = ImageCacheLoader.getInstance().loadDrawableAppointType(rooBean.getAvatarUrl(), new ImageCallback() {
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
				},ImageCacheLoader.IMAGE_TYPE_OF_JPEG);
				if (cacheDrawable != null) {
					viewHolder.ivRoo.setImageDrawable(cacheDrawable);
				} else {
					viewHolder.ivRoo.setImageResource(R.drawable.bg_kangoo_photo_defualt);
				}
			}else{
				viewHolder.ivRoo.setImageResource(R.drawable.bg_kangoo_photo_defualt);
			}
			
			return convertView;
		}

		class ViewHolder {
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
				pd = new ProgressDialog(MainHomeActivity.this);
				pd.setMessage("努力加载袋鼠列表中...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				if (cityId == -1 && pId != -1) {
					return new BusinessHelper().kangarooList(pageNo, pageSize, null, pId);
				} else if (cityId != -1 && pId == -1) {
					return new BusinessHelper().kangarooList(pageNo, pageSize, cityId, null);
				} else if (cityId == -1 && pId == -1) {
					return new BusinessHelper().kangarooList(pageNo, pageSize, null, null);
				}
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
						totalPage = result.getInt("total");
						JSONArray rooListJson = result.getJSONArray("data");
						rooListBeans = HomeRooListBean.constractList(rooListJson);
						// Top袋鼠数据
						if (pageNo == 1) {
							topRooShowAdapter = new TopRooShowAdapter(MainHomeActivity.this);
							topRooShowAdapter.setData(rooListBeans);
							gvHomeTopRooShow.setAdapter(topRooShowAdapter);
						}
						// 袋鼠列表数据
						List<HomeRooListBean> rooListBeansNor = new ArrayList<HomeRooListBean>();
						if (pageNo == 1) {
							for (int i = TOPROOCOUNT; i < rooListBeans.size(); i++) {
								rooListBeansNor.add(rooListBeans.get(i));
							}
						} else {
							for (int i = 0; i < rooListBeans.size(); i++) {
								rooListBeansNor.add(rooListBeans.get(i));
							}
						}

						if (rooShowAdapter == null || rooShowAdapter.getCount() == 0) {
							rooShowAdapter = new RooShowAdapter(MainHomeActivity.this);
							rooShowAdapter.setData(rooListBeansNor);
							gvHomeRooShow.setAdapter(rooShowAdapter);
							pageNo = 1;
						} else {
							rooShowAdapter.add(rooListBeansNor);
						}
						pageNo = pageNo + 1;

					} else {
						Toast.makeText(MainHomeActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
						tvShowNo.setVisibility(View.VISIBLE);
						tvShowNo.setText(result.getString("error"));
						gvHomeTopRooShow.setVisibility(View.GONE);
						gvHomeRooShow.setVisibility(View.GONE);
					}
				} catch (JSONException e) {
					Toast.makeText(MainHomeActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
				} catch (SystemException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(MainHomeActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
			// 加载记录的任务结束
			LIST_RECORD_TASK_RUNING = false;
		}
	}

	/**
	 * 加载Banner数据
	 * 
	 * @author Yuan
	 * 
	 */
	class LoadBannerListTask extends AsyncTask<Void, Void, JSONObject> {
		private int cityId;
		private int pId;

		public LoadBannerListTask(int cityId, int pId) {
			this.cityId = cityId;
			this.pId = pId;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				if (cityId == -1 && pId != -1) {
					return new BusinessHelper().getBannerList(null, pId);
				} else if (cityId != -1 && pId == -1) {
					return new BusinessHelper().getBannerList(cityId, null);
				} else if (cityId == -1 && pId == -1) {
					return new BusinessHelper().getBannerList(null, null);
				}
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
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						JSONArray jArray = result.getJSONArray("data");
						bannerListBeans = BannerListBean.constractList(jArray);
						// 填入数据
						fillPagerData(bannerListBeans);
					} else {
//						Toast.makeText(MainHomeActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(MainHomeActivity.this, "加载banner失败", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(MainHomeActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
//		mImageFetcher.setExitTasksEarly(false);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		startTask();
	}

	@Override
	protected void onPause() {
		super.onPause();
		closeTimer();
		MobclickAgent.onPause(this);
//		mImageFetcher.setExitTasksEarly(true);
//		mImageFetcher.flushCache();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
//		ImageCache mImageCache = mImageFetcher.getImageCache();
//		if (mImageCache != null) {
//			mImageCache.clearCache();
//			mImageCache.close();
//			mImageCache = null;
//		}
//		mImageFetcher.closeCache();
//		mImageFetcher.clearCache();
//		mImageFetcher = null;
//		int count = gvHomeRooShow.getChildCount();
//		for (int i = 0; i < count; i++) {
//			LinearLayout layout = (LinearLayout) gvHomeRooShow.getChildAt(i);
//			layout.removeAllViews();
//		}
		System.gc();
	}
	
	public void resetBannerSize(final ViewPager imageView, int width, int height) {
		int viewSize = screenWidth - 20;
		float endHeight = 0;
		if (viewSize != 0) {
			if (width > 0 && height > 0) {
				if (width < viewSize) {
					float bl = (float) div(viewSize, width);
					endHeight = bl * height;
				} else {
					float bl = (float) div(width, viewSize);
					endHeight = height / bl;
				}
			}
			FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) imageView.getLayoutParams();
			params.height = (int) endHeight;
			imageView.setLayoutParams(params);
		}
	}

	public double div(int v1, int v2) {
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.divide(b2, 3, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	
}

package com.elephant.ediyou.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.AsyncImageLoader;
import com.elephant.ediyou.AsyncImageLoader.ImageCallback;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.EventListBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.imagecache.ImageCache;
import com.elephant.ediyou.imagecache.ImageFetcher;
import com.elephant.ediyou.imagecache.ImageCache.ImageCacheParams;
import com.elephant.ediyou.util.AndroidUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.StringUtil;
import com.elephant.ediyou.view.GridViewInScrollView;
import com.elephant.ediyou.view.ScrollViewIncludeViewPager;
import com.umeng.analytics.MobclickAgent;

/**
 * 活动首页
 * 
 * @author Administrator
 * 
 */
public class EventHomeListActivity extends Activity implements IBaseActivity, OnClickListener, OnItemClickListener {
	private static int screenWidth;// 屏幕宽度
	private ScrollViewIncludeViewPager svivEventHome;// 主页的整体ScrollView
	// 活动展示列表
	private EventShowAdapter eventHomeShowAdapter;
	private static GridViewInScrollView gvEventShow;
	private int pageNo = 1;// 起始页
	private int pageSize = 18;// 每页个数
	private int totalPage = -1;// 总页数
	private boolean LIST_RECORD_TASK_RUNING = false;// 加载记录的任务是否执行中，如果执行中（true），就不再创建新的任务，
	private EventHomeListTask eventHomeListTask;
	private ProgressDialog pd;
	private List<EventListBean> eventHomeListBeans;
	private List<EventListBean> eventHomeListBeansAll = new ArrayList<EventListBean>();
	private final static int TOPEVENTCOUNT = 1;
	int cityId = -1;// 城市Id
	int pId = -1;// 省份Id
	// 活动推荐位
	FrameLayout flTopEvent;
	LinearLayout llEventPhoto;
	ImageView ivEventPhoto;
	TextView tvEventLikeCount;
	TextView tvEventForwardCount;
	TextView tvEventCurrCount;
	TextView tvEventLimitCount;
	TextView tvEventTitle;
	TextView tvEventType;
	TextView tvEventEndTime;
	TextView tvEventStartTime;
	TextView tvEventCost;
	// modify by arvin
	View vEvent;// 顶部活动,如果没有活动隐藏
	//当当地无袋鼠时的提示
	private TextView tvShowNo;
	private LinearLayout llEventHome;
	
	private static final String TAG = "EventHomeListActivity";
	
//	private ImageFetcher mImageFetcher;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_home);
//		ImageCacheParams cacheParams = new ImageCacheParams(this, Constants.APP_DIR_NAME);
//		cacheParams.setMemCacheSizePercent(0.25f);
		// Set memory cache to 25% of mem class
		// cacheParams.setMemCacheSizePercent(this, 0.10f);
//		cacheParams.memoryCacheEnabled = false;
		// cacheParams.initDiskCacheOnCreate = true;
//		cacheParams.compressQuality = 60;
//		mImageFetcher = new ImageFetcher(this, (int) ((screenWidth/2)));
//		mImageFetcher.setLoadingImage(R.drawable.view);
//		mImageFetcher.addImageCache(cacheParams);
//		mImageFetcher.setImageFadeIn(false);
		Log.i(TAG, "onCreate");
		if (getIntent() != null) {
			cityId = getIntent().getIntExtra("cityId", -1);
			pId = getIntent().getIntExtra("pId", -1);
		}
		screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
		findView();
		fillData();
		updateList();

		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		svivEventHome = (ScrollViewIncludeViewPager) this.findViewById(R.id.svivEventHome);
		// 推荐位，第一个活动
		flTopEvent = (FrameLayout) this.findViewById(R.id.flTopEvent);
		llEventPhoto = (LinearLayout) this.findViewById(R.id.llEventPhoto);
		ivEventPhoto = (ImageView) this.findViewById(R.id.ivEventPhoto);
		int ivEventPhotoWidth = screenWidth * 7 / 12;
		LinearLayout.LayoutParams LayoutParams = new LinearLayout.LayoutParams(ivEventPhotoWidth, ViewGroup.LayoutParams.FILL_PARENT);
		llEventPhoto.setLayoutParams(LayoutParams);
		tvEventLikeCount = (TextView) this.findViewById(R.id.tvEventLikeCount);
		tvEventForwardCount = (TextView) this.findViewById(R.id.tvEventForwardCount);
		tvEventCurrCount = (TextView) this.findViewById(R.id.tvEventCurrCount);
		tvEventLimitCount = (TextView) this.findViewById(R.id.tvEventLimitCount);
		tvEventTitle = (TextView) this.findViewById(R.id.tvEventTitle);
		tvEventType = (TextView) this.findViewById(R.id.tvEventType);
		tvEventEndTime = (TextView) this.findViewById(R.id.tvEndTime);
		tvEventStartTime = (TextView) this.findViewById(R.id.tvStartTime);
		tvEventCost = (TextView) this.findViewById(R.id.tvEventCost);
		vEvent = this.findViewById(R.id.viewEvent);

		flTopEvent.setOnClickListener(this);
		// 活动展示列表
		gvEventShow = (GridViewInScrollView) this.findViewById(R.id.gvEventShow);
		
		tvShowNo = (TextView) this.findViewById(R.id.tvShowNo);
		llEventHome = (LinearLayout)this.findViewById(R.id.llEventHome);
	}

	@Override
	public void fillData() {
		gvEventShow.setOnItemClickListener(this);
		svivEventHome.getView();
		svivEventHome.setOnScrollListener(new ScrollViewIncludeViewPager.OnScrollListener() {
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
				if (NetUtil.checkNet(EventHomeListActivity.this)) {
					if (pageNo <= totalPage) {
						updateList();
					} else {
						Toast.makeText(EventHomeListActivity.this, "已全部加载完成", Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(EventHomeListActivity.this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onAutoScroll(int l, int t, int oldl, int oldt) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		default: {
			if (eventHomeListBeansAll != null) {
				int listSize = eventHomeListBeansAll.size();
				if (listSize > 0) {
					Intent intent = new Intent(this, EventDetailActivity.class);
					intent.putExtra("activityId", eventHomeListBeansAll.get(0).getId());
					startActivity(intent);
				}
			}
		}
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, EventDetailActivity.class);
		intent.putExtra("activityId", eventHomeListBeansAll.get(position + 1).getId());
		intent.putExtra("promoterId", (long) eventHomeListBeansAll.get(position + 1).getPromoter());
		startActivity(intent);
	}

	/**
	 * 启动异步任务
	 */
	protected void updateList() {
		if (NetUtil.checkNet(this)) {
			if (!LIST_RECORD_TASK_RUNING) {
				eventHomeListTask = new EventHomeListTask(pageNo, pageSize, cityId, pId);
				eventHomeListTask.execute();
			}
		} else {
			vEvent.setVisibility(View.INVISIBLE);
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 填充推荐位活动（第一条）
	 */
	private void fillTitleEvent() {
		EventListBean eventBean = eventHomeListBeans.get(0);
		setImageByUrl(ivEventPhoto, eventBean.getCoverUrl(),true);
		tvEventType.setText(eventBean.getType());
		tvEventCost.setText(eventBean.getCost()+ "元/人");
		tvEventCurrCount.setText(String.format("%d", eventBean.getCurrentCount()));
		tvEventLikeCount.setText(String.format("%d", eventBean.getLikeCount()));
		tvEventLimitCount.setText(String.format("%d", eventBean.getLimitCount()));
		String startTimeStr = eventBean.getStartTime().substring(0, 11);
		eventBean.setStartTime(startTimeStr);
		String endTimeStr = eventBean.getEndTime().substring(0, 11);
		eventBean.setEndTime(endTimeStr);
		tvEventStartTime.setText(startTimeStr);
		tvEventEndTime.setText(endTimeStr);
		tvEventTitle.setText(eventBean.getTitle());
	}

	/**
	 * 首页活动列表
	 * 
	 * @author syghh
	 * 
	 */
	private class EventShowAdapter extends BaseAdapter {
		private Context mContext;
		private List<EventListBean> eventBeans;

		public EventShowAdapter(Context context) {
			this.mContext = context;
		}

		public void setData(List<EventListBean> eventBeans) {
			this.eventBeans = eventBeans;
		}

		public void add(List<EventListBean> eventBeans) {
			this.eventBeans.addAll(eventBeans);
			this.notifyDataSetChanged();
		}

		private void clear() {
			if (eventBeans != null)
				eventBeans.clear();
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return eventBeans.size();
		}

		@Override
		public Object getItem(int position) {
			return eventBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.event_list_item, null);
				viewHolder = createViewHolderByConvertView(convertView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			fillData(position, viewHolder);
			gvEventShow.setFocusable(false);
			gvEventShow.setFocusableInTouchMode(false);
			return convertView;
		}

		private ViewHolder createViewHolderByConvertView(View convertView) {
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.ivEventPhoto = (ImageView) convertView.findViewById(R.id.ivEventPhoto);
			viewHolder.tvEventLikeCount = (TextView) convertView.findViewById(R.id.tvEventLikeCount);
			viewHolder.tvEventForwardCount = (TextView) convertView.findViewById(R.id.tvEventForwardCount);
			viewHolder.tvEventCurrCount = (TextView) convertView.findViewById(R.id.tvEventCurrCount);
			viewHolder.tvEventLimitCount = (TextView) convertView.findViewById(R.id.tvEventLimitCount);
			viewHolder.tvEventTitle = (TextView) convertView.findViewById(R.id.tvEventTitle);
			viewHolder.tvEventType = (TextView) convertView.findViewById(R.id.tvEventType);
			viewHolder.tvEndTime = (TextView) convertView.findViewById(R.id.tvEndTime);
			viewHolder.tvStartTime = (TextView) convertView.findViewById(R.id.tvStartTime);
			viewHolder.tvEventCost = (TextView) convertView.findViewById(R.id.tvEventCost);
			return viewHolder;
		}

		private void fillData(int position, ViewHolder viewHolder) {
			EventListBean eventBean = eventBeans.get(position);
			setImageByUrl(viewHolder.ivEventPhoto, eventBean.getCoverUrl(),false);
			viewHolder.tvEventType.setText(eventBean.getType());
			viewHolder.tvEventCost.setText(StringUtil.DoubleToAmountString(eventBean.getCost())+"元/人/次");
			viewHolder.tvEventCurrCount.setText(String.format("%d", eventBean.getCurrentCount()));
			viewHolder.tvEventLikeCount.setText(String.format("%d", eventBean.getLikeCount()));
			viewHolder.tvEventLimitCount.setText(String.format("%d", eventBean.getLimitCount()));
			viewHolder.tvEventForwardCount.setText(String.format("%d", eventBean.getForwardCount()));
			String startTimeStr = eventBean.getStartTime().substring(0, 11);
			eventBean.setStartTime(startTimeStr);
			String endTimeStr = eventBean.getEndTime().substring(0, 11);
			eventBean.setEndTime(endTimeStr);
			viewHolder.tvStartTime.setText(startTimeStr);
			viewHolder.tvEndTime.setText(endTimeStr);
			viewHolder.tvEventTitle.setText(eventBean.getTitle());
		}

		private class ViewHolder {
			ImageView ivEventPhoto;
			TextView tvEventLikeCount;
			TextView tvEventForwardCount;
			TextView tvEventCurrCount;
			TextView tvEventLimitCount;
			TextView tvEventTitle;
			TextView tvEventType;
//			TextView tvEventLag;
			TextView tvStartTime;
			TextView tvEndTime;
			TextView tvEventCost;
		}
	}

	public void setImageByUrl(ImageView imageView, String url,final boolean isTop) {
		if (null == url) {
			return;
		}
//		if (mImageFetcher != null) {
//			mImageFetcher.loadImage(url, imageView);
//		} else{
//			imageView.setImageResource(R.drawable.view);
//		}
		imageView.setTag(url);
		final Drawable cacheDrawable = AsyncImageLoader.getInstance().loadDrawableAppointType(url, new ImageCallback() {
			@Override
			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				ImageView image = null;
				if(isTop){
					image = (ImageView) llEventPhoto.findViewWithTag(imageUrl);
				}else{
					image = (ImageView) gvEventShow.findViewWithTag(imageUrl);
				}
				if (image != null) {
					if (imageDrawable != null) {
						image.setImageDrawable(imageDrawable);
					} else {
						image.setImageResource(R.drawable.view);
					}
				}
			}
		}, AsyncImageLoader.IMAGE_TYPE_OF_JPEG);
		if (cacheDrawable != null) {
			imageView.setImageDrawable(cacheDrawable);
		} else {
			imageView.setImageResource(R.drawable.view);
		}
	}

	/**
	 * 活动列表
	 * 
	 * @author syghh
	 * 
	 */
	class EventHomeListTask extends AsyncTask<Void, Void, JSONObject> {
		private int pageNum;
		private int pageSize;
		private Integer city;
		private Integer pId;

		public EventHomeListTask(int pageNum, int pageSize, Integer city, Integer pId) {
			LIST_RECORD_TASK_RUNING = true;
			this.pageNum = pageNum;
			this.pageSize = pageSize;
			this.city = city;
			this.pId = pId;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(EventHomeListActivity.this);
				pd.setMessage("努力加载活动列表中...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				if (cityId == -1 && pId != -1) {
					return new BusinessHelper().eventList(pageNum, pageSize, null, pId);
				} else if (cityId != -1 && pId == -1) {
					return new BusinessHelper().eventList(pageNum, pageSize, city, null);
				} else if (cityId == -1 && pId == -1) {
					return new BusinessHelper().eventList(pageNum, pageSize, null, null);
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
						if (result.has("total")) {
							totalPage = result.getInt("total");
						}
						if (result.has("data")) {
							String jsonData = result.getString("data");
							if (jsonData.contains("id") || jsonData.contains("title")) {
								JSONArray eventListJson = new JSONArray(jsonData);
								eventHomeListBeans = EventListBean.constractList(eventListJson);
								// set推荐位的活动数据
								if (pageNo == 1) {
									if (eventHomeListBeans != null) {
										int listsize = eventHomeListBeans.size();
										if (listsize <= 0) {
											vEvent.setVisibility(View.INVISIBLE);
										} else {
											vEvent.setVisibility(View.VISIBLE);
											fillTitleEvent();
										}
									}
								}
								// 活动列表数据
								List<EventListBean> eventListBeansNor = new ArrayList<EventListBean>();
								if (pageNo == 1) {
									for (int i = TOPEVENTCOUNT; i < eventHomeListBeans.size(); i++) {
										eventListBeansNor.add(eventHomeListBeans.get(i));
									}
								} else {
									for (int i = 0; i < eventHomeListBeans.size(); i++) {
										eventListBeansNor.add(eventHomeListBeans.get(i));
									}
								}
								// set活动列表的adapter
								if (eventHomeShowAdapter == null || eventHomeShowAdapter.getCount() == 0) {
									eventHomeShowAdapter = new EventShowAdapter(EventHomeListActivity.this);
									eventHomeShowAdapter.setData(eventListBeansNor);
									gvEventShow.setAdapter(eventHomeShowAdapter);
									pageNo = 1;
								} else {
									eventHomeShowAdapter.add(eventListBeansNor);
								}
								eventHomeListBeansAll.addAll(eventHomeListBeans);
								pageNo = pageNo + 1;
							} else {
								vEvent.setVisibility(View.INVISIBLE);
							}
						} else {
							String error = "加载出错";
							if (result.has("error")) {
								error = result.getString("error");
							}
							Toast.makeText(EventHomeListActivity.this, error, Toast.LENGTH_LONG).show();
						}
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(EventHomeListActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(EventHomeListActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(EventHomeListActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
						tvShowNo.setVisibility(View.VISIBLE);
						tvShowNo.setText(result.getString("error"));
						llEventHome.setVisibility(View.GONE);
					}
				} catch (JSONException e) {
					Toast.makeText(EventHomeListActivity.this, "加载失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(EventHomeListActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
			// 加载记录的任务结束
			LIST_RECORD_TASK_RUNING = false;
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
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
//		mImageFetcher.setExitTasksEarly(true);
//		mImageFetcher.flushCache();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
//		ImageCache mImageCache = mImageFetcher.getImageCache();
//		if (mImageCache != null) {
//			mImageCache.clearCache();
//			mImageCache.close();
//			mImageCache = null;
//		}
//		mImageFetcher.closeCache();
//		mImageFetcher.clearCache();
//		mImageFetcher = null;
//		int count = gvEventShow.getChildCount();
//		for (int i = 0; i < count; i++) {
//			LinearLayout layout = (LinearLayout) gvEventShow.getChildAt(i);
//			layout.removeAllViews();
//		}
		System.gc();
	}
}
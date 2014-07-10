package com.elephant.ediyou.activity;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.AsyncImageLoader;
import com.elephant.ediyou.AsyncImageLoader.ImageCallback;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.EventPhotoBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.AndroidUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.elephant.ediyou.view.LazyScrollView;
import com.elephant.ediyou.view.LazyScrollView.OnScrollListener;
import com.umeng.analytics.MobclickAgent;

/**
 * 活动详情的图片瀑布流
 * 
 * @author syghh
 * 
 */
public class EventDetailPhotoFallActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	private ProgressDialog pd;

	private List<EventPhotoBean> eventPhotoBeans;
	private List<EventPhotoBean> eventPhotoBeanAlls;
	LazyScrollView scrollView;
	LinearLayout vCollection;

	float density;

	int scroll_height;
	int itemWidth;// 每列宽度
	int pageNo = 1;// 当前页数
	int totalPage = -1;// 总页数
	int pageSize = 20;// 每页数量

	private final static int columnCount = 2;// 一共有多少列
	private final static int columnSpace = 3;// 列宽的间隙大小
	private final static String TAG = "EventDetailPhotoFallActivity";

	private final static int UPDATE_TYPE_NEW = 11;
	private final static int UPDATE_TYPE_ADD = 12;

	private boolean isSelf;

	public static Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
	public static int maxSize = 15;

	private long activityId;
	private long userId;
	private long promoterId;// 创建人Id

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_fall);
		activityId = getIntent().getLongExtra("activityId", 0);
		promoterId = getIntent().getLongExtra("promoterId", 0);
		userId = (long) SharedPrefUtil.getUserBean(this).getUserId();
		if (userId == promoterId) {
			isSelf = true;
		} else {
			isSelf = false;
		}
//		isSelf = true;// 测试
//		 activityId = 22;// 测试

		density = AndroidUtil.getDensity(EventDetailPhotoFallActivity.this);
		Display display = this.getWindowManager().getDefaultDisplay();
		itemWidth = (display.getWidth() - columnSpace * (columnCount + 1)) / columnCount;// 根据屏幕大小计算每列大小

		findView();
		initCollection();

		if (imageCache == null)
			imageCache = new HashMap<String, SoftReference<Drawable>>();

		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnLeft.setOnClickListener(this);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		if (isSelf == false) {
			btnRight.setVisibility(View.INVISIBLE);
		}
		btnRight.setBackgroundResource(R.drawable.bg_btn_selector);
		btnRight.setText("编辑");
		btnRight.setGravity(Gravity.CENTER);
		btnRight.setTextColor(Color.rgb(157, 208, 99));
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("活动图片");
		btnRight.setOnClickListener(this);

		scrollView = (LazyScrollView) this.findViewById(R.id.scroll_photo);
		vCollection = (LinearLayout) this.findViewById(R.id.llEventDetailPhotoList);

		eventPhotoBeanAlls = new ArrayList<EventPhotoBean>();
		if (NetUtil.checkNet(this)) {
			new ActivityPhotoTask(activityId, pageNo, pageSize).execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();

		}

		scrollView.getView();
		scrollView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onTop() {
			}

			@Override
			public void onScroll() {
			}

			@Override
			public void onBottom() {
				if (NetUtil.checkNet(EventDetailPhotoFallActivity.this)) {
					if (pageNo > totalPage && totalPage != -1) {
						Toast.makeText(EventDetailPhotoFallActivity.this, "内容全部加载完成", Toast.LENGTH_SHORT).show();
					} else {
						if (!isRuning) {
							new ActivityPhotoTask(activityId, pageNo, pageSize).execute();
						}
					}
				} else {
					Toast.makeText(EventDetailPhotoFallActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onAutoScroll(int l, int t, int oldl, int oldt) {
			}
		});

	}

	@Override
	public void fillData() {

	}

	private void initCollection() {
		for (int i = 0; i < columnCount; i++) {
			LinearLayout itemLayout = new LinearLayout(this);
			LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(itemWidth, LayoutParams.WRAP_CONTENT);

			if (i == columnCount - 1) {
				itemLayout.setPadding(columnSpace, 0, columnSpace, columnSpace);
			} else {
				itemLayout.setPadding(columnSpace, 0, 0, columnSpace);
			}
			itemLayout.setOrientation(LinearLayout.VERTICAL);
			itemLayout.setLayoutParams(itemParam);
			vCollection.addView(itemLayout);
		}
	}

	private void showImageByBeans(List<EventPhotoBean> eventPhotoBeans, int updateType) {
		if (updateType == UPDATE_TYPE_NEW) {
			int childCount = vCollection.getChildCount();
			for (int i = 0; i < childCount; i++) {
				LinearLayout layout = (LinearLayout) vCollection.getChildAt(i);
				layout.removeAllViews();
			}
		}
		int childCount = vCollection.getChildCount();
		int allChildCount = 0;
		for (int i = 0; i < childCount; i++) {
			LinearLayout layout = (LinearLayout) vCollection.getChildAt(i);
			int itemCount = layout.getChildCount();
			allChildCount += itemCount;
		}

		int leftSize = allChildCount % columnCount;
		int nextStartPosition = leftSize;// 下一个开始的集合

		if (eventPhotoBeans != null) {
			int size = eventPhotoBeans.size();
			for (int i = 0; i < size; i++) {
				EventPhotoBean eventPhotoBean = eventPhotoBeans.get(i);
				String headImgUrl = "";
				int photoWidth = eventPhotoBean.getWidth();
				int photoHeight = eventPhotoBean.getHeight();
				int realPhotoHeight = (photoHeight * itemWidth) / photoWidth;
				LinearLayout.LayoutParams paramItem = new LinearLayout.LayoutParams((int) (itemWidth - 10), (int) (realPhotoHeight));
				paramItem.topMargin = columnSpace;
				LinearLayout layout = (LinearLayout) vCollection.getChildAt(nextStartPosition);

				ImageView ivItem = new ImageView(this);
				ivItem.setBackgroundResource(R.drawable.bg_photo);
				ivItem.setLayoutParams(paramItem);
				ivItem.setScaleType(ScaleType.FIT_XY);
				headImgUrl = eventPhotoBean.getThumbUrl();
				ivItem.setTag(headImgUrl);
				ivItem.setTag(R.id.tag_evnetphotobean, eventPhotoBean);
				ivItem.setOnClickListener(clickListener);
				if (!StringUtil.isBlank(headImgUrl)) {
					if (!AsyncImageLoader.getInstance().containsUrl(headImgUrl)) {
						Drawable cacheDrawable = AsyncImageLoader.getInstance().loadSoftDrawable(imageCache, size, headImgUrl, new ImageCallback() {
							public void imageLoaded(Drawable imageDrawable, String imageUrl) {
								int collSize = vCollection.getChildCount();
								for (int j = 0; j < collSize; j++) {
									LinearLayout item = (LinearLayout) vCollection.getChildAt(j);
									ImageView ivImageView = (ImageView) item.findViewWithTag(imageUrl);
									if (ivImageView != null && imageDrawable != null) {
										ivImageView.setImageDrawable(imageDrawable);
										break;
									}
								}
							}
						});
						if (cacheDrawable != null) {
							ivItem.setImageDrawable(cacheDrawable);
						} else {
							ivItem.setImageDrawable(null);
						}
					} else {
						ivItem.setImageDrawable(null);
					}
				} else {
					ivItem.setImageDrawable(null);
				}
				layout.addView(ivItem);
				if (nextStartPosition == columnCount - 1) {
					nextStartPosition = 0;
				} else {
					nextStartPosition++;
				}
			}
		}
	}

	// 每张图片的点击事件
	OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			EventPhotoBean eventPhotoBean = (EventPhotoBean) v.getTag(R.id.tag_evnetphotobean);
			((CommonApplication) EventDetailPhotoFallActivity.this.getApplicationContext()).setPhotos(eventPhotoBeanAlls);
			Intent intent = new Intent(EventDetailPhotoFallActivity.this, EventPhotoActivity.class);
			intent.putExtra("photoId", eventPhotoBean.getPhotoId());
			startActivity(intent);

		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			setResult(2);
			this.finish();
			break;
		case R.id.btnRight:
			Intent intent = new Intent(EventDetailPhotoFallActivity.this, EventPhotoEditActivity.class);
			intent.putExtra("activityId", activityId);
			intent.putExtra("from", "had");
			startActivityForResult(intent, 1);
			break;
		}

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case 1:
			setResult(0);
			this.finish();
			break;

		default:
			break;
		}
	}

	private boolean isRuning = false;

	/**
	 * 活动图片
	 * 
	 * @author syghh
	 * 
	 */
	class ActivityPhotoTask extends AsyncTask<Void, Void, JSONObject> {

		private long activityId;
		private int pageIndex;
		private int pageSize;

		public ActivityPhotoTask(long activityId, int pageIndex, int pageSize) {
			this.activityId = activityId;
			this.pageIndex = pageIndex;
			this.pageSize = pageSize;
		}

		@Override
		protected void onPreExecute() {
			isRuning = true;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().activityPhoto(activityId, pageIndex, pageSize);
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
						eventPhotoBeans = EventPhotoBean.constantListBean(result.getJSONArray("data"));
						totalPage = result.getInt("total");
						eventPhotoBeanAlls.addAll(eventPhotoBeans);
						showImageByBeans(eventPhotoBeans, UPDATE_TYPE_ADD);

						pageNo++;
					} else {
						Toast.makeText(EventDetailPhotoFallActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(EventDetailPhotoFallActivity.this, "读图错误", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(EventDetailPhotoFallActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
			isRuning = false;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			setResult(2);
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		activityId = getIntent().getLongExtra("activityId", 0);
		userId = SharedPrefUtil.getUserBean(this).getUserId();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (imageCache != null) {
			imageCache.clear();
			imageCache = null;
		}
		int count = vCollection.getChildCount();
		for (int i = 0; i < count; i++) {
			LinearLayout layout = (LinearLayout) vCollection.getChildAt(i);
			layout.removeAllViews();
		}
		System.gc();
	}

}

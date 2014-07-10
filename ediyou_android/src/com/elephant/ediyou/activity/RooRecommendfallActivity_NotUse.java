package com.elephant.ediyou.activity;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.elephant.ediyou.AsyncImageLoader;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.AsyncImageLoader.ImageCallback;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.RooRecommentBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.AndroidUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.StringUtil;
import com.elephant.ediyou.view.LazyScrollView;
import com.elephant.ediyou.view.LazyScrollView.OnScrollListener;
import com.umeng.analytics.MobclickAgent;

/**
 * 袋鼠推荐（袋鼠个人详情页）---->瀑布流，未使用。
 * 
 * @author syghh
 * 
 */
public class RooRecommendfallActivity_NotUse extends Activity implements IBaseActivity {

	private ProgressDialog pd;

	private List<RooRecommentBean> rooRecommentList;
	LazyScrollView scrollView;
	LinearLayout vCollection;

	float density;

	int scroll_height;
	int itemWidth;// 每列宽度
	int pageNo = 1;// 当前页数
	int totalPage = -1;// 总页数
	int pageSize = 5;// 每页数量

	private final static int columnCount = 2;// 一共有多少列
	private int columnSpace = 10;// 列宽的间隙大小
	private final static String TAG = "RooRecommendActivity";

	private final static int UPDATE_TYPE_NEW = 11;
	private final static int UPDATE_TYPE_ADD = 12;

	Dialog dialogVip;

	public static Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
	public static int maxSize = 8;

	private long rooId;// 当前页面所属的 袋鼠id
	private boolean isSelf = false;

	private RelativeLayout rlCommon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_fall);
		rlCommon = (RelativeLayout) this.findViewById(R.id.rlCommon);
		rlCommon.setVisibility(View.GONE);
		if (getIntent() != null) {
			// rooId = getIntent().getLongExtra("rooId", 0);
			rooId = 1;
			isSelf = getIntent().getBooleanExtra("isSelf", false);
		}
		density = AndroidUtil.getDensity(RooRecommendfallActivity_NotUse.this);
		Display display = this.getWindowManager().getDefaultDisplay();
		itemWidth = (display.getWidth() - columnCount * (columnCount + 1)) / columnCount;// 根据屏幕大小计算每列大小

		findView();
		initCollection();

		if (imageCache == null)
			imageCache = new HashMap<String, SoftReference<Drawable>>();

		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		scrollView = (LazyScrollView) findViewById(R.id.scroll_photo);
		vCollection = (LinearLayout) findViewById(R.id.llEventDetailPhotoList);

		if (NetUtil.checkNet(this)) {
			new LoadKangarooRecommendTask(rooId, pageNo, pageSize).execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();

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
				if (NetUtil.checkNet(RooRecommendfallActivity_NotUse.this)) {
					if (pageNo > totalPage && totalPage != -1) {
						Toast.makeText(RooRecommendfallActivity_NotUse.this, "内容全部加载完成",
								Toast.LENGTH_SHORT).show();
					} else {
						if (!isRuning) {
							new LoadKangarooRecommendTask(rooId, pageNo, pageSize)
									.execute();
						}
					}
				} else {
					Toast.makeText(RooRecommendfallActivity_NotUse.this, R.string.NoSignalException,
							Toast.LENGTH_SHORT).show();
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
			LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(
					itemWidth, LayoutParams.WRAP_CONTENT);

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

	private void showImageByBeans(List<RooRecommentBean> eventPhotoBeans, int updateType) {
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
		if (pageNo == 1) {
			LinearLayout layout = (LinearLayout) vCollection
					.getChildAt(nextStartPosition);
			ImageView addNew = new ImageView(this);
			addNew.setImageResource(R.drawable.ic_add_new);
			addNew.setBackgroundResource(R.drawable.bg_photo);
			addNew.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, layout
					.getWidth()));
			layout.addView(addNew, 0);
			addNew.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(RooRecommendfallActivity_NotUse.this,
							RooRecommentNewActivity.class));

				}
			});
		}
		if (eventPhotoBeans != null) {
			int size = eventPhotoBeans.size();
			for (int i = 0; i < size; i++) {
				RooRecommentBean RooRecommentBean = eventPhotoBeans.get(i);
				String coverUrl = "";
				// int photoWidth = RooRecommentBean.getWidth();
				// int photoHeight = RooRecommentBean.getHeight();
				int photoWidth = 200;
				int photoHeight = 100;
				// int realPhotoHeight = photoWidth/itemWidth*photoHeight;
				int realPhotoHeight = (photoHeight * itemWidth) / photoWidth;
				FrameLayout frameLayout = (FrameLayout) getLayoutInflater().inflate(
						R.layout.roo_recommend_photo_fall_item, null);
				LinearLayout.LayoutParams paramItem = new LinearLayout.LayoutParams(
						(int) (itemWidth), (int) (realPhotoHeight));
				paramItem.topMargin = columnSpace;
				// frameLayout.setLayoutParams(paramItem);
				LinearLayout layout = (LinearLayout) vCollection
						.getChildAt(nextStartPosition);

				TextView title = (TextView) frameLayout
						.findViewById(R.id.tvRecommentTitleName);
				TextView content = (TextView) frameLayout
						.findViewById(R.id.tvRecommentContent);
				final ProgressBar progressLoad = (ProgressBar) frameLayout
						.findViewById(R.id.progressLoad);

				title.setText(RooRecommentBean.getTitle());
				content.setText(RooRecommentBean.getContent());
				ImageView ivItem = (ImageView) frameLayout
						.findViewById(R.id.ivRecommentCoverPhoto);

				coverUrl = RooRecommentBean.getCoverUrl();
				progressLoad.setTag(coverUrl);
				ivItem.setTag(coverUrl);
				ivItem.setTag(R.id.tag_evnetphotobean, RooRecommentBean);
				ivItem.setOnClickListener(clickListener);
				if (!StringUtil.isBlank(coverUrl)) {
					if (!AsyncImageLoader.getInstance().containsUrl(coverUrl)) {
						Drawable cacheDrawable = AsyncImageLoader.getInstance()
								.loadSoftDrawable(imageCache, size, coverUrl,
										new ImageCallback() {
											public void imageLoaded(
													Drawable imageDrawable,
													String imageUrl) {
												int collSize = vCollection
														.getChildCount();
												for (int j = 0; j < collSize; j++) {
													LinearLayout item = (LinearLayout) vCollection
															.getChildAt(j);
													ImageView ivImageView = (ImageView) item
															.findViewWithTag(imageUrl);
													if (ivImageView != null
															&& imageDrawable != null) {
														ivImageView
																.setImageDrawable(imageDrawable);
														progressLoad
																.setVisibility(View.GONE);
														break;
													}
												}
											}
										});
						if (cacheDrawable != null) {
							progressLoad.setVisibility(View.GONE);
							ivItem.setImageDrawable(cacheDrawable);
						} else {
							ivItem.setImageResource(R.drawable.bg_image_default);
						}
					} else {
						ivItem.setImageResource(R.drawable.bg_image_default);
					}
				} else {
					ivItem.setImageResource(R.drawable.bg_image_default);
				}
				layout.addView(frameLayout);
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
			RooRecommentBean rooRecommentBean = (RooRecommentBean) v
					.getTag(R.id.tag_evnetphotobean);
			Intent intent = new Intent(RooRecommendfallActivity_NotUse.this,
					RooRecommendDetailActivity.class);
			intent.putExtra("recomId", rooRecommentBean.getRecommentId());
			startActivity(intent);
		}
	};

	private boolean isRuning = false;

	/**
	 * 袋鼠推荐列表
	 * 
	 * @author syghh
	 * 
	 */
	class LoadKangarooRecommendTask extends AsyncTask<Void, Void, JSONObject> {

		private long rooId;
		private int pageIndex;
		private int pageSize;

		public LoadKangarooRecommendTask(long rooId, int pageIndex, int pageSize) {
			this.rooId = rooId;
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
				return new BusinessHelper().kangarooRecommendList(rooId, pageIndex,
						pageSize);
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
						rooRecommentList = RooRecommentBean.constantListBean(result
								.getJSONArray("data"));
						totalPage = result.getInt("total");
						showImageByBeans(rooRecommentList, UPDATE_TYPE_ADD);
						pageNo++;
					} else {
						Toast.makeText(RooRecommendfallActivity_NotUse.this,
								result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(RooRecommendfallActivity_NotUse.this, "读图错误", Toast.LENGTH_LONG)
							.show();
				}
			} else {
				Toast.makeText(RooRecommendfallActivity_NotUse.this, "服务器请求失败", Toast.LENGTH_LONG)
						.show();
			}
			isRuning = false;
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

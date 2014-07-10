package com.elephant.ediyou.activity;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.AsyncImageLoader;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.AsyncImageLoader.ImageCallback;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.PersonalPhotoBean;
import com.elephant.ediyou.bean.RooRecommentBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.view.GridViewInScrollView;
import com.umeng.analytics.MobclickAgent;

/**
 * 袋鼠推荐（袋鼠个人详情页）--->GridView显示（使用中）
 * 
 * @author syghh
 * 
 */
public class RooRecommendActivity extends Activity implements IBaseActivity {
	private GridViewInScrollView gvPhoto;
	private RooRecommendAdapter rooRecommendAdapter;
	private List<RooRecommentBean> rooRecommentBeanList;

	private int pageNo = 1;// 页码
	private int pageSize = 8;// 每页数量
	private int totalPage = -1;// 一共多少页
	public Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
	public static int maxSize = 8;

	private long rooId;// 袋鼠id
	private boolean isSelf;// 是否为自己
	private int itemWidth;// item宽度
	private int columnSpac = 10;// 间距
	private int columnCount = 2;// 列数
	// 加载更多
	private ProgressBar pbFooter;
	private TextView tvFooterMore;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.koala_roo_photo);
		if (getIntent() != null) {
			rooId = getIntent().getLongExtra("rooId", 0);

			isSelf = getIntent().getBooleanExtra("isSelf", false);
		}
		rooId = 1;// 测试
		isSelf = true;
		Display display = this.getWindowManager().getDefaultDisplay();
		itemWidth = (display.getWidth() - columnSpac * (columnCount + 1)) / columnCount;// 根据屏幕大小计算每列大小
		if (NetUtil.checkNet(this)) {
			new LoadKangarooRecommendTask(rooId, pageNo, pageSize).execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();

		}
		findView();
		if (imageCache == null)
			imageCache = new HashMap<String, SoftReference<Drawable>>();
		((CommonApplication) getApplication()).addActivity(this);

	}

	@Override
	public void findView() {
		gvPhoto = (GridViewInScrollView) this.findViewById(R.id.gvPhoto);
		gvPhoto.setNumColumns(columnCount);
		pbFooter = (ProgressBar) findViewById(R.id.progressBar);
		tvFooterMore = (TextView) findViewById(R.id.tvMore);
	}

	@Override
	public void fillData() {
		if (isSelf) {
			rooRecommentBeanList.add(0, new RooRecommentBean());
		}
		rooRecommendAdapter = new RooRecommendAdapter(this, rooRecommentBeanList, imageCache);
		gvPhoto.setAdapter(rooRecommendAdapter);
	}

	/**
	 * 推荐数据列表适配器
	 * 
	 * @author syghh
	 * 
	 */
	class RooRecommendAdapter extends BaseAdapter {
		private Context mContext;
		private List<RooRecommentBean> recommentBeanList;
		private Map<String, SoftReference<Drawable>> imageCache;

		public RooRecommendAdapter(Context context, List<RooRecommentBean> recommentBeanList, Map<String, SoftReference<Drawable>> imageCache) {
			this.mContext = context;
			this.recommentBeanList = recommentBeanList;
			this.imageCache = imageCache;
		}

		@Override
		public int getCount() {
			return recommentBeanList.size();
		}

		@Override
		public Object getItem(int position) {
			return recommentBeanList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		/**
		 * 当删除当前条目信息后，刷新数据，使删除的Item立即消失
		 * 
		 * @param position
		 */
		public void remove(int position) {
			recommentBeanList.remove(position);
			this.notifyDataSetChanged();
		}

		public void setList(List<RooRecommentBean> recommentBeanList) {
			this.recommentBeanList = recommentBeanList;
			this.notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.roo_recommend_photo_fall_item, null);
				viewHolder = new ViewHolder();
				viewHolder.flRecommendLayout = (FrameLayout) convertView.findViewById(R.id.flRecommendLayout);
				viewHolder.llRecommendShow = (LinearLayout) convertView.findViewById(R.id.llRecommendShow);
				viewHolder.ivRecommentCoverPhoto = (ImageView) convertView.findViewById(R.id.ivRecommentCoverPhoto);
				viewHolder.tvRecommentTitleName = (TextView) convertView.findViewById(R.id.tvRecommentTitleName);
				viewHolder.tvRecommentContent = (TextView) convertView.findViewById(R.id.tvRecommentContent);
				viewHolder.llAddNewShow = (LinearLayout) convertView.findViewById(R.id.llAddNewShow);
				viewHolder.ivDeleteMyRecommend = (ImageView) convertView.findViewById(R.id.ivDeleteMyRecommend);
				viewHolder.progressLoad = (ProgressBar) convertView.findViewById(R.id.progressLoad);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			android.view.ViewGroup.LayoutParams llparam = viewHolder.llAddNewShow.getLayoutParams();
			llparam.width = itemWidth;
			llparam.height = itemWidth + 20;

			android.view.ViewGroup.LayoutParams flparam = viewHolder.llRecommendShow.getLayoutParams();
			flparam.width = itemWidth;
			flparam.height = itemWidth + 20;

			viewHolder.llRecommendShow.setLayoutParams(flparam);

			android.view.ViewGroup.LayoutParams ivparam = viewHolder.ivRecommentCoverPhoto.getLayoutParams();
			ivparam.width = itemWidth;
			ivparam.height = itemWidth;
			final RooRecommentBean rooRecommentBean = recommentBeanList.get(position);
			if (isSelf && position == 0) {
				viewHolder.llRecommendShow.setVisibility(View.GONE);
				viewHolder.llAddNewShow.setVisibility(View.VISIBLE);
				viewHolder.progressLoad.setVisibility(View.GONE);
				viewHolder.llAddNewShow.setLayoutParams(llparam);
				viewHolder.llAddNewShow.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(RooRecommendActivity.this, RooRecommentNewActivity.class));
					}
				});
			} else {
				if (rooRecommentBean.isDelState()) {
					viewHolder.ivDeleteMyRecommend.setVisibility(View.VISIBLE);
				} else {
					viewHolder.ivDeleteMyRecommend.setVisibility(View.GONE);
				}
				viewHolder.ivDeleteMyRecommend.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

					}
				});

				// viewHolder.ivRecommentCoverPhoto.setLayoutParams(ivparam);
				viewHolder.tvRecommentTitleName.setText(rooRecommentBean.getTitle());
				viewHolder.tvRecommentContent.setText(rooRecommentBean.getContent());
				viewHolder.ivRecommentCoverPhoto.setTag(rooRecommentBean.getCoverUrl());
				viewHolder.progressLoad.setTag(rooRecommentBean.getCoverUrl() + "-progress");
				Drawable cacheDrawable = AsyncImageLoader.getInstance().loadSoftDrawable(imageCache, maxSize, rooRecommentBean.getCoverUrl(),
						new ImageCallback() {
							@Override
							public void imageLoaded(Drawable imageDrawable, String imageUrl) {
								ImageView ivVideo = (ImageView) gvPhoto.findViewWithTag(imageUrl);
								ProgressBar progress = (ProgressBar) gvPhoto.findViewWithTag(imageUrl + "-progress");
								if (ivVideo != null) {
									if (imageDrawable != null) {
										progress.setVisibility(View.GONE);
										ivVideo.setImageDrawable(imageDrawable);
										RooRecommendAdapter.this.notifyDataSetChanged();
									} else {
										ivVideo.setImageDrawable(null);
									}
								}
							}
						});
				if (cacheDrawable != null) {
					viewHolder.progressLoad.setVisibility(View.GONE);
					viewHolder.ivRecommentCoverPhoto.setImageDrawable(cacheDrawable);
				} else {
					viewHolder.progressLoad.setVisibility(View.GONE);
					viewHolder.ivRecommentCoverPhoto.setImageDrawable(null);
				}

				viewHolder.ivRecommentCoverPhoto.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (rooRecommentBean.isDelState()) {
							rooRecommentBean.setDelState(false);
							notifyDataSetChanged();
							return;
						} else {
							Intent intent = new Intent(RooRecommendActivity.this, RooRecommendDetailActivity.class);
							intent.putExtra("recomId", rooRecommentBean.getRecommentId());
							intent.putExtra("isSelf", isSelf);
							startActivity(intent);
						}
					}
				});
				if (isSelf) {
					viewHolder.ivRecommentCoverPhoto.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							if (!rooRecommentBean.isDelState()) {
								rooRecommentBean.setDelState(true);
								notifyDataSetChanged();
							}
							return true;
						}
					});
				}

			}
			return convertView;
		}

		class ViewHolder {
			FrameLayout flRecommendLayout;
			LinearLayout llRecommendShow;
			ImageView ivRecommentCoverPhoto;
			TextView tvRecommentTitleName;
			TextView tvRecommentContent;
			LinearLayout llAddNewShow;
			ImageView ivDeleteMyRecommend;
			ProgressBar progressLoad;
		}

	}

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
				return new BusinessHelper().kangarooRecommendList(rooId, pageIndex, pageSize);
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
						rooRecommentBeanList = RooRecommentBean.constantListBean(result.getJSONArray("data"));
						totalPage = result.getInt("total");
						fillData();
						pageNo++;
					} else {
						Toast.makeText(RooRecommendActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(RooRecommendActivity.this, "读图错误", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(RooRecommendActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
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
		System.gc();
	}

}

package com.elephant.ediyou.activity;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
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
import com.elephant.ediyou.bean.RooSearchBean;
import com.elephant.ediyou.bean.RooSearchBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 袋鼠搜索结果页
 * 
 * @author
 * 
 */
public class RooSearchResultActivity extends Activity implements IBaseActivity,
		OnClickListener, OnItemClickListener {

	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle, tvSearchResultNum;
	private static GridView gvSearchResultList;
	private LinearLayout llSearchResultNum, llSearchResultNothing;

	private RooSearchAdapter rooSearchAdapter;
	private int totalPage = -1;// 总页数
	private boolean LIST_RECORD_TASK_RUNING = false;// 加载记录的任务是否执行中，如果执行中（true），就不再创建新的任务，
	private RooSearchTask rooSearchTask;
	private List<RooSearchBean> rooSearchBeans;

	private ProgressDialog pd;
	private Bundle bundle;

	private int pageNo = 1;// 起始页
	private int pageSize = 18;// 每页个数
	private int city, count, level;
	private String title, birthday, gender;
	private static int itemWith;// 袋鼠item的宽度

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roo_search_result);
		findView();
		fillData();
		updateList();
		itemWith = (this.getWindowManager().getDefaultDisplay().getWidth() - 20) / 3;
		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("搜索结果");
		gvSearchResultList = (GridView) this
				.findViewById(R.id.gvSearchResultList);
		llSearchResultNum = (LinearLayout) this
				.findViewById(R.id.llSearchResultNum);
		llSearchResultNothing = (LinearLayout) this
				.findViewById(R.id.llSearchResultNothing);
		tvSearchResultNum = (TextView) this
				.findViewById(R.id.tvSearchResultNum);

		bundle = this.getIntent().getExtras();
	}

	@Override
	public void fillData() {
		city = bundle.getInt(Constants.KEY_ROO_SEARCH_CITY);
		title = bundle.getString(Constants.KEY_ROO_SEARCH_SERVICE_GRADE);
		count = bundle.getInt(Constants.KEY_ROO_SEARCH_FREE_STATE);
		level = bundle.getInt(Constants.KEY_ROO_SEARCH_GOO_GRADE);
		birthday = bundle.getString(Constants.KEY_ROO_SEARCH_AGE_SECTION);
		gender = bundle.getString(Constants.KEY_ROO_SEARCH_SEX);

		tvTitle.setText("搜索结果");
		llSearchResultNum.setVisibility(View.GONE);
		llSearchResultNothing.setVisibility(View.GONE);
		btnLeft.setOnClickListener(this);
		gvSearchResultList.setOnItemClickListener(this);
		gvSearchResultList.setOnScrollListener(loadNewPageListener);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(this, RooShowActivity.class);
		intent.putExtra("uid", (long) rooSearchAdapter.getBeans().get(position).getUserId());// userId
		intent.putExtra("rooId", (long) rooSearchAdapter.getBeans().get(position)
				.getKangarooId());
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		}
	}

	/**
	 * 首页活动列表
	 * 
	 * @author syghh
	 * 
	 */
	private static class RooSearchAdapter extends BaseAdapter {
		private Context mContext;
		private List<RooSearchBean> eventBeans;

		public RooSearchAdapter(Context context) {
			this.mContext = context;
		}

		public void setData(List<RooSearchBean> eventBeans) {
			this.eventBeans = eventBeans;
		}

		public List<RooSearchBean> getBeans(){
			return this.eventBeans;
		}
		
		public void add(List<RooSearchBean> eventBeans) {
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
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.home_roo_show_item, null);
				viewHolder = createViewHolderByConvertView(convertView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			fillData(position, viewHolder);

			return convertView;
		}

		private ViewHolder createViewHolderByConvertView(View convertView) {
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.ivRoo = (ImageView) convertView.findViewById(R.id.ivRoo);
			viewHolder.ivGender = (ImageView) convertView
					.findViewById(R.id.ivGender);
			viewHolder.tvAge = (TextView) convertView.findViewById(R.id.tvAge);
			viewHolder.tvLevel = (TextView) convertView
					.findViewById(R.id.tvLevel);
			viewHolder.tvBadge = (TextView) convertView
					.findViewById(R.id.tvBadge);
			return viewHolder;
		}

		private void fillData(int position, ViewHolder viewHolder) {
			String tmpStr = null;
			RooSearchBean eventBean = eventBeans.get(position);
			LayoutParams lp = viewHolder.ivRoo.getLayoutParams();
			lp.width = itemWith;
			lp.height = itemWith;
			viewHolder.ivRoo.setLayoutParams(lp);
			setImageByUrl(viewHolder.ivRoo, eventBean.getAvatarUrl());
			tmpStr = eventBean.getGender();
			if (!StringUtil.isBlank(tmpStr)) {
				if (tmpStr.equals("m")) {
					viewHolder.ivGender.setImageResource(R.drawable.ic_male);
				} else {
					viewHolder.ivGender.setImageResource(R.drawable.ic_fale);
				}
			}
			viewHolder.tvAge.setText(eventBean.getAge());
			viewHolder.tvLevel
					.setText(String.format("%d", eventBean.getLevel()));
			viewHolder.tvBadge.setText(eventBean.getTitle());
		}

		private class ViewHolder {
			ImageView ivRoo;
			ImageView ivGender;
			TextView tvAge;
			TextView tvLevel;
			TextView tvBadge;
		}

		private void setImageByUrl(ImageView imageView, String url) {
			if (null == url) {
				return;
			}
			String img = url.replace("\\", "");
			imageView.setTag(img);
			final Drawable cacheDrawable = AsyncImageLoader.getInstance()
					.loadDrawable(img, new ImageCallback() {
						@Override
						public void imageLoaded(Drawable imageDrawable,
								String imageUrl) {
							ImageView image = null;
							image = (ImageView) gvSearchResultList
									.findViewWithTag(imageUrl);

							if (image != null) {
								if (imageDrawable != null) {
									image.setImageDrawable(imageDrawable);
								} else {
									image.setImageResource(R.drawable.bg_kangoo_photo_defualt);
								}
							}
						}
					});
			if (cacheDrawable != null) {
				imageView.setImageDrawable(cacheDrawable);
			} else {
				imageView.setImageResource(R.drawable.bg_kangoo_photo_defualt);
			}
		}
	}

	/**
	 * 袋鼠列表
	 * 
	 * @author syghh
	 * 
	 */
	class RooSearchTask extends AsyncTask<Void, Void, JSONObject> {
		public RooSearchTask() {
			LIST_RECORD_TASK_RUNING = true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RooSearchResultActivity.this);
				pd.setMessage("搜索中...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().rooSearch(pageNo, pageSize, city,
						title, count, level, birthday, gender);
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
						int count = result.getInt("count");
						llSearchResultNothing.setVisibility(View.GONE);
						llSearchResultNum.setVisibility(View.VISIBLE);
						tvSearchResultNum.setText(String.valueOf(count));
						JSONArray eventListJson = result.getJSONArray("data");
						rooSearchBeans = RooSearchBean
								.constractList(eventListJson);
						if (rooSearchAdapter == null
								|| rooSearchAdapter.getCount() == 0) {
							rooSearchAdapter = new RooSearchAdapter(
									RooSearchResultActivity.this);
							rooSearchAdapter.setData(rooSearchBeans);
							gvSearchResultList.setAdapter(rooSearchAdapter);
							pageNo = 1;
						} else {
							rooSearchAdapter.add(rooSearchBeans);
						}
						pageNo = pageNo + 1;

						Toast.makeText(RooSearchResultActivity.this, "加载成功",
								Toast.LENGTH_LONG).show();
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(RooSearchResultActivity.this,
								R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(RooSearchResultActivity.this,
								LoginActivity.class).putExtra("back", "back"));
					} else {
						if (pageNo == 1) {
							llSearchResultNothing.setVisibility(View.VISIBLE);
							llSearchResultNum.setVisibility(View.GONE);
							Toast.makeText(RooSearchResultActivity.this,
									result.getString("error"),
									Toast.LENGTH_LONG).show();
						}

					}
				} catch (JSONException e) {
					Toast.makeText(RooSearchResultActivity.this, "加载失败",
							Toast.LENGTH_LONG).show();
				}/*
				 * catch (SystemException e) { e.printStackTrace(); }
				 */
			} else {
				Toast.makeText(RooSearchResultActivity.this, "服务器请求失败",
						Toast.LENGTH_LONG).show();
			}
			// 加载记录的任务结束
			LIST_RECORD_TASK_RUNING = false;
		}
	}

	/**
	 * 滚动监听器
	 */
	OnScrollListener loadNewPageListener = new OnScrollListener() {
		private int lastItem;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastItem = firstVisibleItem + visibleItemCount - 1;//
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// 滚动到最后，默认加载下一页
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
					&& lastItem == rooSearchAdapter.getCount() - 1) {
				updateList();
			}
		}
	};

	protected void updateList() {
		if (NetUtil.checkNet(this)) {
			if (!LIST_RECORD_TASK_RUNING) {
				rooSearchTask = new RooSearchTask();
				rooSearchTask.execute();
			}
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG)
					.show();
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

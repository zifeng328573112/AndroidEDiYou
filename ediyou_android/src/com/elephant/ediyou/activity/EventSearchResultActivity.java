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
import com.elephant.ediyou.bean.EventListBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

public class EventSearchResultActivity extends Activity implements IBaseActivity, OnClickListener, OnItemClickListener {

	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle, tvSearchResultNum;
	private static GridView gvSearchResultList;
	private LinearLayout llSearchResultNum, llSearchResultNothing;

	private EventShowAdapter eventShowAdapter;
	private int totalPage = -1;// 总页数
	private boolean LIST_RECORD_TASK_RUNING = false;// 加载记录的任务是否执行中，如果执行中（true），就不再创建新的任务，
	private EventHomeListTask eventHomeListTask;
	private List<EventListBean> eventHomeListBeans;

	private ProgressDialog pd;
	private Bundle bundle;

	private int pageNo = 1;// 起始页
	private int pageSize = 18;// 每页个数
	private long eventSearchType = 0;
	private String eventSearchTitle = null;
	private String eventSearchStartTime = null;
	private int eventSearchCostStart = 0, eventSearchCostEnd = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_search_result);
		findView();
		fillData();
		updateList();

		((CommonApplication) getApplication()).addActivity(this);
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
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("搜索结果");
		gvSearchResultList = (GridView) this.findViewById(R.id.gvSearchResultList);
		llSearchResultNum = (LinearLayout) this.findViewById(R.id.llSearchResultNum);
		llSearchResultNothing = (LinearLayout) this.findViewById(R.id.llSearchResultNothing);
		tvSearchResultNum = (TextView) this.findViewById(R.id.tvSearchResultNum);

		bundle = this.getIntent().getExtras();
	}

	@Override
	public void fillData() {
		eventSearchType = bundle.getInt(Constants.KEY_SEARCH_EVENT_TYPE);
		eventSearchTitle = bundle.getString(Constants.KEY_SEARCH_EVENT_TITLE);
		eventSearchStartTime = bundle.getString(Constants.KEY_SEARCH_EVENT_STARTTIME);
		eventSearchCostStart = bundle.getInt(Constants.KEY_SEARCH_EVENT_COSTSTART);
		eventSearchCostEnd = bundle.getInt(Constants.KEY_SEARCH_EVENT_COSTEND);

		llSearchResultNum.setVisibility(View.GONE);
		llSearchResultNothing.setVisibility(View.GONE);
		btnLeft.setOnClickListener(this);
		gvSearchResultList.setOnItemClickListener(this);
		gvSearchResultList.setOnScrollListener(loadNewPageListener);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, EventDetailActivity.class);
		intent.putExtra("activityId", eventHomeListBeans.get(position).getId());
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
	private static class EventShowAdapter extends BaseAdapter {
		private Context mContext;
		private List<EventListBean> eventBeans;
		private boolean isNull = false;

		public EventShowAdapter(Context context) {
			this.mContext = context;
		}

		public void setData(List<EventListBean> eventBeans) {
			if (eventBeans.isEmpty()) {
				isNull = true;
			} else {
				isNull = false;
			}
			this.eventBeans = eventBeans;
		}

		public void add(List<EventListBean> eventBeans) {
			this.eventBeans.addAll(eventBeans);
			this.notifyDataSetChanged();
		}

		private void clear() {
			if (eventBeans != null)
				eventBeans.clear();
			isNull = true;
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
			setImageByUrl(viewHolder.ivEventPhoto, eventBean.getCoverUrl());
			viewHolder.tvEventType.setText(eventBean.getType());
			viewHolder.tvEventCost.setText(StringUtil.DoubleToAmountString(eventBean.getCost()));
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
			TextView tvStartTime;
			TextView tvEndTime;
			TextView tvEventCost;
		}

		private void setImageByUrl(ImageView imageView, String url) {
			imageView.setTag(url);
			final Drawable cacheDrawable = AsyncImageLoader.getInstance().loadDrawable(url, new ImageCallback() {
				@Override
				public void imageLoaded(Drawable imageDrawable, String imageUrl) {
					ImageView image = null;
					image = (ImageView) gvSearchResultList.findViewWithTag(imageUrl);

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
	 * 活动列表
	 * 
	 * @author syghh
	 * 
	 */
	class EventHomeListTask extends AsyncTask<Void, Void, JSONObject> {
		public EventHomeListTask() {
			LIST_RECORD_TASK_RUNING = true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(EventSearchResultActivity.this);
				pd.setMessage("搜索中...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().eventSearch(pageNo, pageSize, eventSearchType, eventSearchTitle, eventSearchStartTime,
						eventSearchCostStart, eventSearchCostEnd);
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
						eventHomeListBeans = EventListBean.constractList(eventListJson);
						if (eventShowAdapter == null || eventShowAdapter.getCount() == 0) {
							eventShowAdapter = new EventShowAdapter(EventSearchResultActivity.this);
							eventShowAdapter.setData(eventHomeListBeans);
							gvSearchResultList.setAdapter(eventShowAdapter);
							pageNo = 1;
						} else {
							eventShowAdapter.add(eventHomeListBeans);
						}
						pageNo = pageNo + 1;

						Toast.makeText(EventSearchResultActivity.this, "加载成功", Toast.LENGTH_LONG).show();
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(EventSearchResultActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(EventSearchResultActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						if (pageNo == 1) {
							llSearchResultNothing.setVisibility(View.VISIBLE);
							llSearchResultNum.setVisibility(View.GONE);
							Toast.makeText(EventSearchResultActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
						}
					}
				} catch (JSONException e) {
					Toast.makeText(EventSearchResultActivity.this, "加载失败", Toast.LENGTH_LONG).show();
				}/*
				 * catch (SystemException e) { e.printStackTrace(); }
				 */
			} else {
				Toast.makeText(EventSearchResultActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
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
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			lastItem = firstVisibleItem + visibleItemCount - 1;//
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastItem == eventShowAdapter.getCount() - 1) {
				if (pageNo < totalPage) {
					updateList();
				}
			}
		}
	};

	protected void updateList() {
		if (NetUtil.checkNet(this)) {
			if (!LIST_RECORD_TASK_RUNING) {
				eventHomeListTask = new EventHomeListTask();
				eventHomeListTask.execute();
			}
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
		}
	}

}

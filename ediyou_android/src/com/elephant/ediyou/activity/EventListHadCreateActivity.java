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
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 袋鼠发起过的活动
 * 
 * @author SongYuan
 * 
 */
public class EventListHadCreateActivity extends Activity implements IBaseActivity, OnClickListener, OnItemClickListener {
	private Button btnLeft;
	private TextView tvTitle;
	private Button btnRight;
	private EventAdapter eventAdapter;
	private GridView gvEventShow;
	private int pageNo = 1;// 起始页
	private int pageSize = 18;// 每页个数
	private int totalPage = -1;// 总页数
	private boolean LIST_RECORD_TASK_RUNING = false;// 加载记录的任务是否执行中，如果执行中（true），就不再创建新的任务，
	private ArrayList<EventListBean> eventHadJoinListBeans = new ArrayList<EventListBean>();

	private TextView tvShowNo;// 当没有数据时显示
	
	private ProgressDialog pd;
	private long userId;
	private CommonApplication app;
	private boolean isSelf = false;
	private String name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_list_had_layout);
		userId = getIntent().getLongExtra(Constants.EXTRA_USER_ID, 0);
		name = getIntent().getStringExtra(Constants.EXTRA_NAME);
		if (!StringUtil.isBlank(SharedPrefUtil.getUserBean(this).getAccessToken()) && userId == (long) SharedPrefUtil.getUserBean(this).getUserId()) {
			isSelf = true;
		}
		findView();
		fillData();
		if (NetUtil.checkNet(this)) {
			new GetEventHadCreateListTask().execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
		}
		app = (CommonApplication) getApplication();
		app.addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) findViewById(R.id.btnLeft);
		btnRight = (Button) findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		if (isSelf) {
			tvTitle.setText("活动管理");
		} else {
			tvTitle.setText(/*"袋鼠" + */"'" + name + "'" + "创建的活动");
		}

		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnLeft.setOnClickListener(this);

		tvShowNo = (TextView)this.findViewById(R.id.tvShowNo);
		
		gvEventShow = (GridView) this.findViewById(R.id.gvEventShow);
	}

	@Override
	public void fillData() {
		gvEventShow.setOnItemClickListener(this);
		gvEventShow.setOnScrollListener(loadNewPageListener);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, EventDetailActivity.class);
		intent.putExtra("activityId", eventHadJoinListBeans.get(position).getId());
		intent.putExtra("promoterId", (long) eventHadJoinListBeans.get(position).getPromoter());
		startActivity(intent);
	}

	/**
	 * 滚动监听器,加载下一页
	 */
	OnScrollListener loadNewPageListener = new OnScrollListener() {
		private int lastItem;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			lastItem = firstVisibleItem + visibleItemCount - 1;//
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// 滚动到最后，默认加载下一页
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastItem == eventAdapter.getCount() - 1) {
				if (NetUtil.checkNet(EventListHadCreateActivity.this)) {
					if (!LIST_RECORD_TASK_RUNING) {
						if (pageNo < totalPage) {
							new GetEventHadCreateListTask().execute();
						} else {
							Toast.makeText(EventListHadCreateActivity.this, R.string.load_all, Toast.LENGTH_SHORT).show();
						}
					}
				} else {
					Toast.makeText(EventListHadCreateActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
			}

		}
	};

	/**
	 * 活动列表的适配器
	 * 
	 * @author syghh
	 * 
	 */
	private class EventAdapter extends BaseAdapter {
		private Context mContext;
		private List<EventListBean> eventBeans;

		public EventAdapter(Context context) {
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
				viewHolder = new ViewHolder();
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
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			EventListBean eventBean = eventBeans.get(position);
			viewHolder.tvEventType.setText(eventBean.getType());
			viewHolder.tvEventCost.setText(String.valueOf(eventBean.getCost()) + "元/人/次");
			viewHolder.tvEventCurrCount.setText(String.valueOf(eventBean.getCurrentCount()));
			viewHolder.tvEventLikeCount.setText(String.valueOf(eventBean.getLikeCount()));
			viewHolder.tvEventLimitCount.setText(String.valueOf(eventBean.getLimitCount()));
			viewHolder.tvEventForwardCount.setText(String.valueOf(eventBean.getForwardCount()));
			String startTimeStr = eventBean.getStartTime().substring(0, 11);
			eventBean.setStartTime(startTimeStr);
			String endTimeStr = eventBean.getEndTime().substring(0, 11);
			eventBean.setEndTime(endTimeStr);
			viewHolder.tvStartTime.setText(startTimeStr);
			viewHolder.tvEndTime.setText(endTimeStr);
			viewHolder.tvEventTitle.setText(eventBean.getTitle());
			String url = eventBean.getCoverUrl();
			viewHolder.ivEventPhoto.setTag(url);
			Drawable cacheDrawable = AsyncImageLoader.getInstance().loadDrawable(url, new ImageCallback() {
				@Override
				public void imageLoaded(Drawable imageDrawable, String imageUrl) {
					ImageView image = null;
					image = (ImageView) gvEventShow.findViewWithTag(imageUrl);
					if (image != null) {
						if (imageDrawable != null) {
							image.setImageDrawable(imageDrawable);
						} else {
							image.setImageResource(R.drawable.view);
						}
					}
				}
			});
			if (cacheDrawable != null) {
				viewHolder.ivEventPhoto.setImageDrawable(cacheDrawable);
			} else {
				viewHolder.ivEventPhoto.setImageResource(R.drawable.view);
			}

			return convertView;
		}

		class ViewHolder {
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
	}

	/**
	 * 获取当前用户参加过的活动的异步任务
	 * 
	 * @author syghh
	 * 
	 */
	class GetEventHadCreateListTask extends AsyncTask<Void, Void, JSONObject> {
		public GetEventHadCreateListTask() {
			LIST_RECORD_TASK_RUNING = true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(EventListHadCreateActivity.this);
				pd.setMessage("努力加载活动列表中...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().rooCurrEvent(pageNo, pageSize, userId);
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
						JSONArray eventListJson = result.getJSONArray("data");
						tvShowNo.setVisibility(View.GONE);
						if (eventListJson.length() > 0) {
							eventHadJoinListBeans = EventListBean.constractList(eventListJson, eventHadJoinListBeans);
							if (eventAdapter == null || eventAdapter.getCount() == 0) {
								eventAdapter = new EventAdapter(EventListHadCreateActivity.this);
								eventAdapter.setData(eventHadJoinListBeans);
								gvEventShow.setAdapter(eventAdapter);
								pageNo = 1;
							} else {
								eventAdapter.add(eventHadJoinListBeans);
							}
							pageNo = pageNo + 1;
						} else {
							Toast.makeText(EventListHadCreateActivity.this, "全部加载完毕", Toast.LENGTH_SHORT).show();
						}
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(EventListHadCreateActivity.this, R.string.time_out, Toast.LENGTH_SHORT).show();
						startActivity(new Intent(EventListHadCreateActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						tvShowNo.setVisibility(View.VISIBLE);
						Toast.makeText(EventListHadCreateActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(EventListHadCreateActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(EventListHadCreateActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
			// 加载记录的任务结束
			LIST_RECORD_TASK_RUNING = false;
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

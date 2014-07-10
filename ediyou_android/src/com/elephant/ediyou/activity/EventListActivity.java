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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
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
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.DateUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.elephant.ediyou.view.GridViewInScrollView;
import com.umeng.analytics.MobclickAgent;

/**
 * 袋鼠和考拉参与过的活动（个人详情页）—暂未使用
 * 
 * @author syghh
 * 
 */
public class EventListActivity extends Activity implements IBaseActivity, OnClickListener, OnItemClickListener {

	
	private EventShowAdapter eventHomeShowAdapter;
	private static GridViewInScrollView gvEventShow;
	private int pageNo = 1;// 起始页
	private int pageSize = 18;// 每页个数
	private int totalPage = -1;// 总页数
	private boolean LIST_RECORD_TASK_RUNING = false;// 加载记录的任务是否执行中，如果执行中（true），就不再创建新的任务，
	private EventHomeListTask eventHomeListTask;
	private ArrayList<EventListBean> eventHomeListBeans = new ArrayList<EventListBean>();

	private ProgressDialog pd;
	private long uid;// 用户ID
	private UserBean userBean;
	private CommonApplication app;
	private USER_TYPE userType;

	enum USER_TYPE {
		ROO, KOALA
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_list);
		app = (CommonApplication) getApplication();
		app.addActivity(this);

		findView();
		fillData();
		updateList();
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, EventDetailActivity.class);
		intent.putExtra("activityId", eventHomeListBeans.get(position).getId());
		intent.putExtra("promoterId", (long) eventHomeListBeans.get(position).getPromoter());
		startActivity(intent);
	}

	@Override
	public void findView() {
		gvEventShow = (GridViewInScrollView) this.findViewById(R.id.gvEventShow);
	}

	@Override
	public void fillData() {
		uid = getIntent().getLongExtra("id", 0);
		if (getIntent().getStringExtra("type").equals("Roo")) {
			userType = USER_TYPE.ROO;
		} else {
			userType = USER_TYPE.KOALA;
		}
		gvEventShow.setOnItemClickListener(this);
		gvEventShow.setOnScrollListener(loadNewPageListener);

		userBean = SharedPrefUtil.getUserBean(this);
		
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

	/**
	 * 首页活动列表
	 * 
	 * @author syghh
	 * 
	 */
	private class EventShowAdapter extends BaseAdapter {
		private Context mContext;
		private List<EventListBean> eventBeans;
		private boolean isNull = false;
		private USER_TYPE userType;

		public EventShowAdapter(Context context, USER_TYPE type) {
			this.mContext = context;
			this.userType = type;
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
				viewHolder = findView(position, convertView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			fillData(position, viewHolder);

			return convertView;
		}

		private ViewHolder findView(int position, View convertView) {
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.llEventItem = (LinearLayout) convertView.findViewById(R.id.llEventItem);
			viewHolder.llAddNewShow = (LinearLayout) convertView.findViewById(R.id.llAddNewShow);
			viewHolder.ivEventPhoto = (ImageView) convertView.findViewById(R.id.ivEventPhoto);
			viewHolder.tvEventLikeCount = (TextView) convertView.findViewById(R.id.tvEventLikeCount);
			viewHolder.tvEventForwardCount = (TextView) convertView.findViewById(R.id.tvEventForwardCount);
			viewHolder.tvEventCurrCount = (TextView) convertView.findViewById(R.id.tvEventCurrCount);
			viewHolder.tvEventLimitCount = (TextView) convertView.findViewById(R.id.tvEventLimitCount);
			viewHolder.tvEventTitle = (TextView) convertView.findViewById(R.id.tvEventTitle);
			viewHolder.tvEventType = (TextView) convertView.findViewById(R.id.tvEventType);
			viewHolder.tvEventStartTime = (TextView) convertView.findViewById(R.id.tvEventStartTime);
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
				viewHolder.tvEventStartTime.setText(DateUtil.getNearTime(eventBean.getStartTime()));
				viewHolder.tvEventTitle.setText(eventBean.getTitle());
		}

		private class ViewHolder {
			LinearLayout llEventItem;
			LinearLayout llAddNewShow;

			ImageView ivEventPhoto;
			TextView tvEventLikeCount;
			TextView tvEventForwardCount;
			TextView tvEventCurrCount;
			TextView tvEventLimitCount;
			TextView tvEventTitle;
			TextView tvEventType;
			TextView tvEventStartTime;
			TextView tvEventCost;
		}

		private void setImageByUrl(ImageView imageView, String url) {
			if (null == url) {
				return;
			}
			String img = url.replace("\\", "");
			imageView.setTag(img);
			final Drawable cacheDrawable = AsyncImageLoader.getInstance().loadDrawable(img, new ImageCallback() {
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
				imageView.setImageDrawable(cacheDrawable);
			} else {
				imageView.setImageResource(R.drawable.view);
			}
		}
	}

	/**
	 * 袋鼠列表
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
				pd = new ProgressDialog(EventListActivity.this);
				pd.setMessage("努力加载活动列表中...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				if (userType == USER_TYPE.ROO) {
					return new BusinessHelper().rooCurrEvent(pageNo, pageSize, uid);
				} else {
					return new BusinessHelper().currHadJoinEvent(pageNo, pageSize, uid);
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
						JSONArray eventListJson = result.getJSONArray("data");
						if (eventListJson.length() > 0) {
							eventHomeListBeans = EventListBean.constractList(eventListJson, eventHomeListBeans);
							if (eventHomeShowAdapter == null || eventHomeShowAdapter.getCount() == 0) {
								eventHomeShowAdapter = new EventShowAdapter(EventListActivity.this, userType);
								eventHomeShowAdapter.setData(eventHomeListBeans);
								gvEventShow.setAdapter(eventHomeShowAdapter);
								pageNo = 1;
							} else {
								eventHomeShowAdapter.add(eventHomeListBeans);
							}
							pageNo = pageNo + 1;

							Toast.makeText(EventListActivity.this, "加载成功", Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(EventListActivity.this, "记录为空", Toast.LENGTH_LONG).show();
						}
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(EventListActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(EventListActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(EventListActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(EventListActivity.this, "加载失败", Toast.LENGTH_LONG).show();
				}/*
				 * catch (SystemException e) { e.printStackTrace(); }
				 */
			} else {
				Toast.makeText(EventListActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
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
			// 滚动到最后，默认加载下一页
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastItem == eventHomeShowAdapter.getCount() - 1) {
				updateList();
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

	public static void addTabTo(Context context, long id, String type, TabHost tabHost, String tabTag) {
		Intent eventIntent = new Intent(context, EventListActivity.class);
		eventIntent.putExtra("id", id);
		eventIntent.putExtra("type", type);
		tabHost.addTab(tabHost.newTabSpec(tabTag).setIndicator("活动").setContent(eventIntent));
	}
}

package com.elephant.ediyou.activity;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.CommentListBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 评论展示页（袋鼠和考拉均可用）
 * 
 * @author SongYuan
 * 
 */
public class CommentShowActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button 					btnLeft;
	private TextView 				tvTitle;
	private Button 					btnRight;
	private ProgressDialog 			pd;
	private CommonApplication 		app;

	private int 					pageNo = 1;// 起始页
	private int 					pageSize = 10;// 每页个数
	private int 					totalPage = -1;// 总页数
	private boolean 				LIST_RECORD_TASK_RUNING = false;// 加载记录的任务是否执行中，如果执行中（true），就不再创建新的任务，

	private Button 					btnFromOrder;// 切换到“来自预约”（订单）的评论列表
	private Button 					btnFromEvent;// 切换到“来自活动”的评论列表
	private ListView 				lvCommentList;

	private TextView 				tvShowNo;// 当没有数据时显示

	private List<CommentListBean> 	commentListBeans;
	private CommentListAdapter 		commentListAdapter;

	private long 					userId;

	private final static int 		FROM_ORDER = 0;
	private final static int 		FROM_EVENT = 1;

	private int 					type = -1;// 判断“来自预约”（订单）的评论（0）或“来自活动”的评论（1）

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comment_show_layout);
		userId 	= getIntent().getLongExtra("userId", 0);
		app 	= (CommonApplication) getApplication();
		findView();
		fillData();
		app.addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft 		= (Button) findViewById(R.id.btnLeft);
		btnRight 		= (Button) findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle 		= (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("评论");
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnLeft.setOnClickListener(this);

		btnFromOrder 	= (Button) this.findViewById(R.id.btnFromOrder);
		btnFromEvent 	= (Button) this.findViewById(R.id.btnFromEvent);
		btnFromOrder.setOnClickListener(this);
		btnFromEvent.setOnClickListener(this);

		tvShowNo 		= (TextView) findViewById(R.id.tvShowNo);

		lvCommentList 	= (ListView) this.findViewById(R.id.lvCommentList);
		lvCommentList.setOnScrollListener(loadNewPageListener);
	}

	@Override
	public void fillData() {
		type = FROM_ORDER;
		if (NetUtil.checkNet(this)) {
			if (!LIST_RECORD_TASK_RUNING) {
				pageNo = 1;
				new GetCommentListTask(userId, pageNo, pageSize, type).execute();
			}
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnFromOrder:// 来自预约（订单）的评论
			tvShowNo.setVisibility(View.GONE);
			btnFromOrder.setBackgroundResource(R.drawable.order_tab_bg_sel);
			btnFromOrder.setTextColor(Color.rgb(157, 208, 99));
			btnFromEvent.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btnFromEvent.setTextColor(Color.rgb(201, 195, 179));
			type = FROM_ORDER;
			if (commentListBeans != null) {
				commentListBeans.clear();
			}
			if (commentListAdapter != null) {
				commentListAdapter.notifyDataSetChanged();
			}
			if (NetUtil.checkNet(this)) {
				if (!LIST_RECORD_TASK_RUNING) {
					pageNo = 1;
					new GetCommentListTask(userId, pageNo, pageSize, type).execute();
				}
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.btnFromEvent:// 来自活动
			tvShowNo.setVisibility(View.GONE);
			btnFromOrder.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btnFromOrder.setTextColor(Color.rgb(201, 195, 179));
			btnFromEvent.setBackgroundResource(R.drawable.order_tab_bg_sel);
			btnFromEvent.setTextColor(Color.rgb(157, 208, 99));
			if (commentListBeans != null) {
				commentListBeans.clear();
			}
			if (commentListAdapter != null) {
				commentListAdapter.notifyDataSetChanged();
			}
			type = FROM_EVENT;
			if (NetUtil.checkNet(this)) {
				if (!LIST_RECORD_TASK_RUNING) {
					pageNo = 1;
					new GetCommentListTask(userId, pageNo, pageSize, type).execute();
				}
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
			break;
		}

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
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastItem == commentListAdapter.getCount() - 1) {
				if (NetUtil.checkNet(CommentShowActivity.this)) {
					if (!LIST_RECORD_TASK_RUNING) {
						if (pageNo < totalPage) {
							new GetCommentListTask(userId, pageNo, pageSize, type);
						} else {
							Toast.makeText(CommentShowActivity.this, R.string.load_all, Toast.LENGTH_SHORT).show();
						}
					}
				} else {
					Toast.makeText(CommentShowActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
			}

		}
	};

	/**
	 * 评论数据适配器
	 * 
	 * @author SongYuan
	 * 
	 */
	class CommentListAdapter extends BaseAdapter {
		private Context mContext;
		private List<CommentListBean> commentListBeans;

		public CommentListAdapter(Context context) {
			this.mContext = context;
		}

		public void setData(List<CommentListBean> commentListBeans) {
			this.commentListBeans = commentListBeans;
		}

		public void add(List<CommentListBean> commentListBeans) {
			this.commentListBeans.addAll(commentListBeans);
			this.notifyDataSetChanged();
		}

		private void clear() {
			if (commentListBeans != null)
				commentListBeans.clear();
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return commentListBeans.size();
		}

		@Override
		public Object getItem(int position) {
			return commentListBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView 				= LayoutInflater.from(mContext).inflate(R.layout.comment_show_item, null);
				viewHolder 					= new ViewHolder();
				viewHolder.ratingbarShow 	= (RatingBar) convertView.findViewById(R.id.ratingbarShow);
				viewHolder.tvContent 		= (TextView) convertView.findViewById(R.id.tvContent);
				viewHolder.tvNickname 		= (TextView) convertView.findViewById(R.id.tvNickname);
				viewHolder.tvCreateTime 	= (TextView) convertView.findViewById(R.id.tvCreateTime);
				convertView.setTag(viewHolder);
			} else {
				viewHolder 					= (ViewHolder) convertView.getTag();
			}

			CommentListBean bean 			= commentListBeans.get(position);
			float ratingL = 0;
			try {
				ratingL = Float.valueOf(bean.getLevel());
			} catch (Exception e) {
			}
			viewHolder.ratingbarShow.setRating(ratingL);
			viewHolder.tvContent.setText(bean.getContent());
			viewHolder.tvNickname.setText(bean.getNickname());
			String creatTimeStr = bean.getCreatedTime().substring(0, bean.getCreatedTime().length() - 2);
			viewHolder.tvCreateTime.setText(creatTimeStr);
			return convertView;
		}

		class ViewHolder {
			RatingBar 		ratingbarShow;
			TextView 		tvContent;
			TextView 		tvNickname;
			TextView 		tvCreateTime;
		}
	}

	/**
	 * 获取当前用户的评论列表
	 * 
	 * @author syghh
	 * 
	 */
	class GetCommentListTask extends AsyncTask<Void, Void, JSONObject> {
		private long 		userId;
		private int 		pageNo;
		private int 		pageSize;
		private int 		type;

		public GetCommentListTask(long userId, int pageNo, int pageSize, int type) {
			LIST_RECORD_TASK_RUNING 	= true;
			this.userId 				= userId;
			this.pageNo 				= pageNo;
			this.pageSize 				= pageSize;
			this.type 					= type;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(CommentShowActivity.this);
			}
			pd.setMessage("获取评论列表中...");
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().getCommentList(userId, pageNo, pageSize, type);
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
						JSONArray jsonArray = result.getJSONArray("data");
						commentListBeans = CommentListBean.constantsLetterListBean(jsonArray);
						if (commentListAdapter == null || commentListAdapter.getCount() == 0) {
							commentListAdapter = new CommentListAdapter(CommentShowActivity.this);
							commentListAdapter.setData(commentListBeans);
							lvCommentList.setAdapter(commentListAdapter);
							pageNo = 1;
						} else {
							commentListAdapter.add(commentListBeans);
						}
						pageNo = pageNo + 1;
						Toast.makeText(CommentShowActivity.this, "加载成功", Toast.LENGTH_SHORT).show();
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(CommentShowActivity.this, R.string.time_out, Toast.LENGTH_SHORT).show();
						startActivity(new Intent(CommentShowActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						tvShowNo.setVisibility(View.VISIBLE);
//						Toast.makeText(CommentShowActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(CommentShowActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
				}/*
				 * catch (SystemException e) { e.printStackTrace(); }
				 */
			} else {
				Toast.makeText(CommentShowActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
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

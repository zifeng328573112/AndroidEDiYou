/**
 * 
 */
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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.KeyEvent;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.OrderListBean;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.DateUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 我收到的订单（袋鼠）
 * 
 * @author syghh
 * 
 */
public class MyReceivedOrderActivity extends Activity implements IBaseActivity, OnClickListener, OnItemClickListener {
	private CommonApplication app;
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	private ProgressDialog pd;

	private Button btn_ordering;// 未确认
	private Button btn_underWay;// 进行中
	private Button btn_completed;// 已完成

	private TextView tvShowNo;// 当没有数据时显示

	private ListView lvOrder;
	private ArrayList<OrderListBean> orderListBeans;
	private OrderListAdapter orderListAdapter;
	private int pageNo = 1;// 起始页
	private int pageSize = 20;// 每页个数
	private int totalPage = -1;// 总页数
	private boolean LIST_RECORD_TASK_RUNING = false;// 加载记录的任务是否执行中，如果执行中（true），就不再创建新的任务，
	private ProfileReordersTask profileMyOrdersTask;
	private UserBean userBean;
	private long RooId;
	private String accessToken;
	private Integer state = null;// 当前列表需要查询的订单状态的订单数据（必传）
	private Integer state2 = null;// 当前列表需要查询的订单状态2的订单数据（非必传）
	private Integer state3 = null;// 当前列表需要查询的订单状态3的订单数据（非必传）
	private Integer state4 = null;// 当前列表需要查询的订单状态2的订单数据（非必传）
	private Integer state5 = null;// 当前列表需要查询的订单状态3的订单数据（非必传）

	// 支付方式；
	private static final int ONLINE_PAY = 0;
	private String from;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_order);
		app = (CommonApplication) getApplication();
		userBean = SharedPrefUtil.getUserBean(this);
		RooId = SharedPrefUtil.getRooId(this);
		accessToken = userBean.getAccessToken();
		if (getIntent() != null) {
			from = getIntent().getStringExtra("from");
		}
		if(TextUtils.isEmpty(from)){
			from = "";
		}
		findView();
		fillData();
		state = Constants.ROO_NOT_CONFIRM;
		state2 = Constants.KOALA_NOT_PAY;
		state3 = null;
		state4 = null;
		state5 = null;
		app.addActivity(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (NetUtil.checkNet(this)) {
			if (!LIST_RECORD_TASK_RUNING) {
				profileMyOrdersTask = new ProfileReordersTask(RooId, pageNo, pageSize, accessToken, state, state2, state3, state4, state5);
				profileMyOrdersTask.execute();
			}
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
		}
		MobclickAgent.onResume(this);

	}

	@Override
	public void findView() {
		btnLeft = (Button) findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		btn_ordering = (Button) findViewById(R.id.btn_ordering);
		btn_underWay = (Button) findViewById(R.id.btn_underWay);
		btn_completed = (Button) findViewById(R.id.btn_completed);

		btnLeft.setOnClickListener(this);
		btn_ordering.setOnClickListener(this);
		btn_underWay.setOnClickListener(this);
		btn_completed.setOnClickListener(this);

		tvShowNo = (TextView) findViewById(R.id.tvShowNo);

		lvOrder = (ListView) findViewById(R.id.lv_order_list);
		lvOrder.setOnScrollListener(loadNewPageListener);
		lvOrder.setOnItemClickListener(this);
	}

	@Override
	public void fillData() {
		tvTitle.setText("我收到的订单");
		orderListBeans = new ArrayList<OrderListBean>();
		orderListAdapter = new OrderListAdapter(this);
		lvOrder.setAdapter(orderListAdapter);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			if (from.equals("")) {
				startActivity(new Intent(this, RooSelfCenterActivity.class));
				finish();
			} else if (from.equals("home")) {
				finish();
			} else if (from.equals("rooSelf")) {
				startActivity(new Intent(this, RooSelfCenterActivity.class));
				finish();
			}
			break;
		case R.id.btn_ordering:// 预约中
			tvShowNo.setVisibility(View.GONE);
			btn_ordering.setBackgroundResource(R.drawable.order_tab_bg_sel);
			btn_ordering.setTextColor(Color.rgb(157, 208, 99));
			btn_underWay.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btn_underWay.setTextColor(Color.rgb(201, 195, 179));
			btn_completed.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btn_completed.setTextColor(Color.rgb(201, 195, 179));
			state = Constants.ROO_NOT_CONFIRM;
			state2 = Constants.KOALA_NOT_PAY;
			state3 = null;
			state4 = null;
			state5 = null;
			orderListBeans.clear();
			if (orderListAdapter != null) {
				orderListAdapter.notifyDataSetChanged();
			}
			pageNo = 1;
			if (NetUtil.checkNet(this)) {
				if (!LIST_RECORD_TASK_RUNING) {
					profileMyOrdersTask = new ProfileReordersTask(RooId, pageNo, pageSize, accessToken, state, state2, state3, state4, state5);
					profileMyOrdersTask.execute();
				}
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.btn_underWay:// 进行中
			tvShowNo.setVisibility(View.GONE);
			btn_ordering.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btn_ordering.setTextColor(Color.rgb(201, 195, 179));
			btn_underWay.setBackgroundResource(R.drawable.order_tab_bg_sel);
			btn_underWay.setTextColor(Color.rgb(157, 208, 99));
			btn_completed.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btn_completed.setTextColor(Color.rgb(201, 195, 179));
			state = Constants.ONLINE_PAY;
			state2 = Constants.OFFLINE_PAY;
			state3 = Constants.KOALA_NOT_CONFIRM;
			state4 = null;
			state5 = null;
			orderListBeans.clear();
			if (orderListAdapter != null) {
				orderListAdapter.notifyDataSetChanged();
			}
			pageNo = 1;
			if (NetUtil.checkNet(this)) {
				if (!LIST_RECORD_TASK_RUNING) {
					profileMyOrdersTask = new ProfileReordersTask(RooId, pageNo, pageSize, accessToken, state, state2, state3, state4, state5);
					profileMyOrdersTask.execute();
				}
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.btn_completed:// 已完成
			tvShowNo.setVisibility(View.GONE);
			btn_ordering.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btn_ordering.setTextColor(Color.rgb(201, 195, 179));
			btn_underWay.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btn_underWay.setTextColor(Color.rgb(201, 195, 179));
			btn_completed.setBackgroundResource(R.drawable.order_tab_bg_sel);
			btn_completed.setTextColor(Color.rgb(157, 208, 99));
			state = Constants.KOALA_NOT_COMMENT;
			state2 = Constants.ROO_NOT_COMMENT;
			state3 = Constants.ORDER_DONE;
			state4 = Constants.ROO_REFUSE;
			state5 = Constants.ORDER_INVALID;
			orderListBeans.clear();
			if (orderListAdapter != null) {
				orderListAdapter.notifyDataSetChanged();
			}
			pageNo = 1;
			if (NetUtil.checkNet(this)) {
				if (!LIST_RECORD_TASK_RUNING) {
					profileMyOrdersTask = new ProfileReordersTask(RooId, pageNo, pageSize, accessToken, state, state2, state3, state4, state5);
					profileMyOrdersTask.execute();
				}
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
			}
			break;
		default:
			break;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		long userId = orderListBeans.get(position).getUserId();
		String orderNumber = orderListBeans.get(position).getOrderNumber();
		Intent intent = new Intent(this, RooOrderDetailActivity.class);
		intent.putExtra("userId", userId);
		intent.putExtra("orderNumber", orderNumber);
		startActivity(intent);
		finish();
	}

	/**
	 * 我的订单适配器
	 * 
	 * @author syghh
	 * 
	 */
	class OrderListAdapter extends BaseAdapter {
		private Context context;

		public OrderListAdapter(Context context) {
			this.context = context;
		}

		public void add(List<OrderListBean> orderListBeans) {
			orderListBeans.addAll(orderListBeans);
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return orderListBeans.size();
		}

		@Override
		public Object getItem(int position) {
			return orderListBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.my_order_item, null);
				viewHolder = new ViewHolder();
				viewHolder.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
				viewHolder.tvCreateTime = (TextView) convertView.findViewById(R.id.tvCreateTime);
				viewHolder.tvOppositeNickName = (TextView) convertView.findViewById(R.id.tvOppositeNickName);
				viewHolder.tvAppointMentTime = (TextView) convertView.findViewById(R.id.tvAppointMentTime);
				viewHolder.tvState = (TextView) convertView.findViewById(R.id.tvState);
				viewHolder.ivIsRead = (ImageView) convertView.findViewById(R.id.ivIsRead);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			if (position % 2 == 0) {
				convertView.setBackgroundResource(R.drawable.bg_repeat);
			} else {
				convertView.setBackgroundResource(R.drawable.repeat_blod_slant);
			}
			TextPaint paint = viewHolder.tvLocation.getPaint();
			if(orderListBeans.get(position).isReaded()){
				paint.setFakeBoldText(false);
				viewHolder.ivIsRead.setVisibility(View.GONE);
			}else{
				paint.setFakeBoldText(true);
				viewHolder.ivIsRead.setVisibility(View.VISIBLE);
			}
			viewHolder.tvLocation.setText(orderListBeans.get(position).getAddress());
			String creatTime = DateUtil.getConversationTime(orderListBeans.get(position).getCreatedTime());
			viewHolder.tvCreateTime.setText(creatTime);
			viewHolder.tvOppositeNickName.setText(orderListBeans.get(position).getLoginName());
			viewHolder.tvAppointMentTime.setText(orderListBeans.get(position).getReservationTime());

			// 0-袋鼠拒绝1-袋鼠未确认2-未支付3-进行中、线上支付4进行中、线下支付5考拉未确认(已见面)6考拉未评价7袋鼠未评价8完成9订单过期
			switch (orderListBeans.get(position).getState()) {
			case Constants.ROO_REFUSE:
				viewHolder.tvState.setText("已拒绝");
				break;
			case Constants.ROO_NOT_CONFIRM:
				viewHolder.tvState.setText("请接受");
				break;
			case Constants.KOALA_NOT_PAY:
				viewHolder.tvState.setText("等待支付");
				break;
			case Constants.ONLINE_PAY:
				viewHolder.tvState.setText("已支付");
				break;
			case Constants.OFFLINE_PAY:
				viewHolder.tvState.setText("线下支付");
				break;
			case Constants.KOALA_NOT_CONFIRM:
				viewHolder.tvState.setText("等待确认");
				break;
			case Constants.KOALA_NOT_COMMENT:
				viewHolder.tvState.setText("考拉未评价");
				break;
			case Constants.ROO_NOT_COMMENT:
				viewHolder.tvState.setText("请评价");
				break;
			case Constants.ORDER_DONE:
				viewHolder.tvState.setText("双方已互评");
				break;
			case Constants.ORDER_INVALID:
				viewHolder.tvState.setText("订单过期");
			default:
				break;
			}

			return convertView;
		}

		private class ViewHolder {
			TextView tvLocation;
			TextView tvCreateTime;
			TextView tvOppositeNickName;
			TextView tvAppointMentTime;
			TextView tvState;
			ImageView ivIsRead;
		}

	}

	/**
	 * 获取我的订单列表
	 * 
	 * @author syghh
	 * 
	 */
	class ProfileReordersTask extends AsyncTask<Void, Void, JSONObject> {

		private long rooId;
		private int pageNum;
		private int pageSize;
		private String access_token;
		private Integer state;
		private Integer state2;
		private Integer state3;
		private Integer state4;
		private Integer state5;

		public ProfileReordersTask(long rooId, int pageNum, int pageSize, String access_token, Integer state, Integer state2, Integer state3,
				Integer state4, Integer state5) {
			this.rooId = rooId;
			this.pageNum = pageNum;
			this.pageSize = pageSize;
			this.access_token = access_token;
			this.state = state;
			this.state2 = state2;
			this.state3 = state3;
			this.state4 = state4;
			this.state5 = state5;
			LIST_RECORD_TASK_RUNING = true;

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(MyReceivedOrderActivity.this);
				pd.setMessage("正在获取...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().profileReorders(rooId, pageNum, pageSize, access_token, state, state2, state3, state4, state5);
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
						JSONArray orderListArray = result.getJSONArray("data");
						orderListBeans = OrderListBean.constractList(orderListArray);
						totalPage = result.getInt("total");
						if (result.getInt("current") > pageNo) {
							orderListAdapter.add(orderListBeans);
						} else {
							orderListAdapter = new OrderListAdapter(MyReceivedOrderActivity.this);
							lvOrder.setAdapter(orderListAdapter);
						}
						pageNo = pageNo + 1;
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(MyReceivedOrderActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(MyReceivedOrderActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						tvShowNo.setVisibility(View.VISIBLE);
//						Toast.makeText(MyReceivedOrderActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(MyReceivedOrderActivity.this, "获取失败", Toast.LENGTH_LONG).show();
				} catch (SystemException e) {
					e.printStackTrace();
					Toast.makeText(MyReceivedOrderActivity.this, "获取失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(MyReceivedOrderActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
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
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastItem == orderListAdapter.getCount() - 1) {
				if (NetUtil.checkNet(MyReceivedOrderActivity.this)) {
					if (!LIST_RECORD_TASK_RUNING && pageNo < totalPage) {
						profileMyOrdersTask = new ProfileReordersTask(RooId, pageNo, pageSize, accessToken, state, state2, state3, state4, state5);
						profileMyOrdersTask.execute();
					}
				} else {
					Toast.makeText(MyReceivedOrderActivity.this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
				}
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (from == null) {
				startActivity(new Intent(this, RooSelfCenterActivity.class));
				finish();
			} else if (from.equals("home")) {
				finish();
			} else if (from.equals("rooSelf")) {
				startActivity(new Intent(this, RooSelfCenterActivity.class));
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}

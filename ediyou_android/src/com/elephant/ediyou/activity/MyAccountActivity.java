package com.elephant.ediyou.activity;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.AsyncImageLoader;
import com.elephant.ediyou.AsyncImageLoader.ImageCallback;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.AccountListBean;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 我的账户(其中注释部分为暂未调节的接口)
 * 
 * @author syghh
 * 
 */
public class MyAccountActivity extends Activity implements IBaseActivity, OnClickListener {

	private CommonApplication app;
	private UserBean userBean;
	private long uId;

	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	private TextView tvAccountBalance;
	private Button btnAccountPay;
	private RadioGroup rgAccountList;
	private RadioButton rbAccountIncome, rbAccountExpend;
	private static ListView lvAccountList;

	private AccountListAdapter accountListAdapter;
	private int pageNo = 1;// 起始页
	private int pageSize = 18;// 每页个数
	private int totalPage = -1;// 总页数
	private boolean LIST_RECORD_TASK_RUNING = false;// 加载记录的任务是否执行中，如果执行中（true），就不再创建新的任务，
	private List<AccountListBean> accountListBeans;
	private int accountState = 0; // 0-收入1-支出
//	private int isKangaroo = 0; // 0-考拉， 1-袋鼠

	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_account);
		app = (CommonApplication) getApplication();
		userBean = SharedPrefUtil.getUserBean(this);
		uId = userBean.getUserId();
		findView();
		updateList();
		fillData();

		app.addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvAccountBalance = (TextView) this.findViewById(R.id.tvAccountBalance);
		btnAccountPay = (Button) this.findViewById(R.id.btnAccountPay);
		rgAccountList = (RadioGroup) this.findViewById(R.id.rgAccountList);
		rbAccountIncome = (RadioButton) this.findViewById(R.id.rbAccountIncome);
		rbAccountExpend = (RadioButton) this.findViewById(R.id.rbAccountExpend);
		lvAccountList = (ListView) this.findViewById(R.id.lvAccountList);
	}

	@Override
	public void fillData() {
		tvTitle.setText("我的帐户");

		btnLeft.setOnClickListener(this);
		btnAccountPay.setOnClickListener(this);
		rbAccountIncome.setOnClickListener(this);
		rbAccountExpend.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnAccountPay:
			String moneyStr = tvAccountBalance.getText().toString();
			if (TextUtils.isEmpty(moneyStr)) {
				Toast.makeText(this, "没有可用余额提现", Toast.LENGTH_SHORT).show();
				return;
			}
			double totalMoney = 0;
			try {
				totalMoney = Double.parseDouble(moneyStr);
			} catch (Exception e) {
			}
			if (totalMoney <= 0) {
				Toast.makeText(this, "没有可用余额提现", Toast.LENGTH_SHORT).show();
				return;
			}
			showWithdrawDialog();
			break;
		case R.id.rbAccountIncome:
			rbAccountIncome.setTextColor(Color.rgb(157, 208, 99));
			rbAccountExpend.setTextColor(Color.rgb(201, 195, 179));
			if (null != accountListAdapter) {
				accountListAdapter.clear();
			}
			accountState = 0;
			pageNo = 1;
			updateList();
			break;
		case R.id.rbAccountExpend:
			rbAccountIncome.setTextColor(Color.rgb(201, 195, 179));
			rbAccountExpend.setTextColor(Color.rgb(157, 208, 99));
			if (null != accountListAdapter) {
				accountListAdapter.clear();
			}
			accountState = 1;
			pageNo = 1;
			updateList();
			break;
		}
	}

	/**
	 * 首页活动列表
	 * 
	 * @author syghh
	 * 
	 */
	private static class AccountListAdapter extends BaseAdapter {
		private Context mContext;
		private List<AccountListBean> beansList = null;
		private Boolean isNull;

		public AccountListAdapter(Context context) {
			this.mContext = context;
		}

		public void setData(List<AccountListBean> eventBeans) {
			if (eventBeans.isEmpty()) {
				isNull = true;
			} else {
				isNull = false;
			}
			this.beansList = eventBeans;
		}

		public void add(List<AccountListBean> eventBeans) {
			this.beansList.addAll(eventBeans);
			this.notifyDataSetChanged();
		}

		private void clear() {
			if (beansList != null)
				beansList.clear();
			isNull = true;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return beansList.size();
		}

		@Override
		public Object getItem(int position) {
			return beansList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;

			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.my_account_item, null);
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
			viewHolder.tvAccountTouchTime = (TextView) convertView.findViewById(R.id.tvAccountTouchTime);
			viewHolder.tvAccountTouchMoney = (TextView) convertView.findViewById(R.id.tvAccountTouchMoney);
			viewHolder.tvAccountTouchTName = (TextView) convertView.findViewById(R.id.tvAccountTouchTName);
			return viewHolder;
		}

		private void fillData(int position, ViewHolder viewHolder) {
			AccountListBean bean = beansList.get(position);
			String time = bean.getTouchTime().substring(0, bean.getTouchTime().length() -2);
			viewHolder.tvAccountTouchTime.setText(time);
			viewHolder.tvAccountTouchMoney.setText(bean.getTouchMoney());
			viewHolder.tvAccountTouchTName.setText(bean.getTname());
		}

		private class ViewHolder {
			TextView tvAccountTouchTime;
			TextView tvAccountTouchMoney;
			TextView tvAccountTouchTName;
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
					image = (ImageView) lvAccountList.findViewWithTag(imageUrl);
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

	private void showWithdrawDialog() {
		LayoutInflater inflater = this.getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_modify_service_price, null);
		TextView tvDialogMsg = (TextView) view.findViewById(R.id.tvDialogMsg);
		TextView tvDialogTitle = (TextView) view.findViewById(R.id.tvDialogTitle);
		final EditText etPrice = (EditText) view.findViewById(R.id.etPrice);
		Button btnDialogLeft = (Button) view.findViewById(R.id.btnDialogLeft);
		Button btnDialogRight = (Button) view.findViewById(R.id.btnDialogRight);

		tvDialogTitle.setText("账户提现");
		tvDialogMsg.setText("请输入您提现的金额！");
		final Dialog dialog = new Dialog(this, R.style.dialog);
		dialog.setContentView(view);
		dialog.show();
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
		lp.width = (int) (display.getWidth() - 60); // 设置宽度
		dialog.getWindow().setAttributes(lp);

		btnDialogLeft.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final String priceStr = etPrice.getText().toString();
				if (TextUtils.isEmpty(priceStr)) {
					Toast.makeText(MyAccountActivity.this, "请输入提现金额", Toast.LENGTH_SHORT).show();
					return;
				}
				if (!StringUtil.checkNum(priceStr)) {
					Toast.makeText(MyAccountActivity.this, "请输入有效数字", Toast.LENGTH_SHORT).show();
					return;
				}
				double totalMoney = Double.parseDouble(tvAccountBalance.getText().toString());
				double withdrawMoney = Double.parseDouble(priceStr);
				if (withdrawMoney > totalMoney) {
					Toast.makeText(MyAccountActivity.this, "提现的金额不能超过您的账户余额！", Toast.LENGTH_SHORT).show();
					return;
				}
				if(NetUtil.checkNet(MyAccountActivity.this)){
					new WithdrawTask(withdrawMoney).execute();
				}else{
					Toast.makeText(MyAccountActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
					return;
				}
				dialog.dismiss();
			}
		});
		btnDialogRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();

			}
		});
	}

	/**
	 * 袋鼠列表
	 * 
	 * @author syghh
	 * 
	 */
	class AccountListTask extends AsyncTask<Void, Void, JSONObject> {
		public AccountListTask() {
			LIST_RECORD_TASK_RUNING = true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(MyAccountActivity.this);
			}
			pd.setMessage("努力加载收单信息，请稍后...");
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().accountInfo(pageNo, pageSize, uId, accountState, userBean.getAccessToken());
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
						System.out.println("### "+result);
						tvAccountBalance.setText(String.format("%d", result.getInt("balance")));
						JSONArray accountListJson = result.getJSONArray("data");
						accountListBeans = AccountListBean.constractList(accountListJson);
						if (accountListAdapter == null || accountListAdapter.getCount() == 0) {
							accountListAdapter = new AccountListAdapter(MyAccountActivity.this);
							accountListAdapter.setData(accountListBeans);
							lvAccountList.setAdapter(accountListAdapter);
							pageNo = 1;
						} else {
							accountListAdapter.add(accountListBeans);
						}
						pageNo = pageNo + 1;

					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(MyAccountActivity.this, R.string.time_out, Toast.LENGTH_SHORT).show();
						startActivity(new Intent(MyAccountActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(MyAccountActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(MyAccountActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
				}/*
				 * catch (SystemException e) { e.printStackTrace(); }
				 */
			} else {
				Toast.makeText(MyAccountActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
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
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastItem == accountListAdapter.getCount() - 1) {
				updateList();
			}
		}
	};

	protected void updateList() {
		if (NetUtil.checkNet(this)) {
			if (!LIST_RECORD_TASK_RUNING) {
				 new AccountListTask().execute();
			}
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
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

	/**
	 * 账户提现任务类
	 * 
	 * @author Zhoujun
	 * 
	 */
	private class WithdrawTask extends AsyncTask<Void, Void, JSONObject> {
		private double touchMoney;

		public WithdrawTask(double touchMoney) {
			super();
			this.touchMoney = touchMoney;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(MyAccountActivity.this);
			}
			pd.setMessage("正在提交账户提现申请...");
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().withdraw(uId, touchMoney);
			} catch (SystemException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if(pd != null){
				pd.dismiss();
			}
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						Toast.makeText(MyAccountActivity.this, "提现申请已经提交成功，等待后台处理！", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(MyAccountActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
				}
			} else {
				Toast.makeText(MyAccountActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
		}

	}
}

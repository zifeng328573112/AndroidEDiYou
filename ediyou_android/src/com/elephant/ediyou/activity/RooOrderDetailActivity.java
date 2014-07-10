package com.elephant.ediyou.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.ImageCacheLoader;
import com.elephant.ediyou.ImageCacheLoader.ImageCallback;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.OrderBean;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.sec.code.CaptureActivity;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 订单详情（袋鼠）
 * 
 * @author syghh
 * 
 */
public class RooOrderDetailActivity extends Activity implements IBaseActivity,
		OnClickListener {

	private CommonApplication app;
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	private ProgressDialog pd;
	// 订单信息
	private View viewTwoDimensionalCode;// 二维码布局；
	private TextView tvConfirmQrCodePrompt;
	private ImageView ivTwoDimCode;
	private TextView tvOrderNumHere;
	private TextView tvGeneralDate;
	private TextView tvOrderState;
	private TextView tvServerPrice;
	private TextView tvServerTime;
	private TextView tvServerTotalPrice;
	// 对方资料
	private LinearLayout llContast;
	private ImageView ivHeadImg;
	private TextView tvName;
	private ImageView ivGender;
	private TextView tvAge;
	private TextView tvLevel;
	private TextView tvBadge;
	private TextView tvArea;
	// 时间地点要求
	private TextView tvOrderTime;
	private TextView tvOrderPlace;
	private TextView tvServerInfo;

	// 订单信息
	private OrderBean orderBean;
	private int orderNumber;// 订单号
	private String createdTime;// 创建时间
	private int state;// 订单状态:0-未支付1-已支付2-袋鼠已确认3-袋鼠未确认4-袋鼠拒绝订单
	private int unitPrice;// 单价
	private int serviceDays;// 服务天数
	private int totalPrice;// 总价

	// 底部按钮
	private Button btn_report_order;
	private Button btn_con_roo;
	private Button btn_order_state;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roo_order_detail);

		findView();
		fillData();
		if (getIntent() != null) {
			long userId = SharedPrefUtil.getUserBean(this).getUserId();
			int orderNumber = Integer.parseInt(getIntent().getStringExtra(
					"orderNumber"));
			String access_token = SharedPrefUtil.getUserBean(this)
					.getAccessToken();
			if (NetUtil.checkNet(this)) {
				new ProfileOrderDetailTask(orderNumber, userId, access_token)
						.execute();
				
				new UpdateOrderTagTask(userId, orderNumber + "").execute();
			} else {
				Toast.makeText(this, R.string.NoSignalException,
						Toast.LENGTH_LONG).show();
			}
		}
		((CommonApplication) getApplication()).addActivity(this);

	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setText(R.string.order_detail);
		tvTitle.setVisibility(View.VISIBLE);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnLeft.setOnClickListener(this);
		btnRight.setBackgroundResource(R.drawable.ic_camera_selector);
		btnRight.setOnClickListener(this);
		btnRight.setVisibility(View.INVISIBLE);

		viewTwoDimensionalCode = this.findViewById(R.id.viewTwoDimensionalCode);
		tvConfirmQrCodePrompt = (TextView) findViewById(R.id.tvConfirmQrCodePrompt);
		ivTwoDimCode = (ImageView) this.findViewById(R.id.ivTwoDimCode);
		tvOrderNumHere = (TextView) this.findViewById(R.id.tvOrderNumHere);
		tvGeneralDate = (TextView) this.findViewById(R.id.tvGenerateDate);
		tvOrderState = (TextView) this.findViewById(R.id.tvOrderState);
		tvServerPrice = (TextView) this.findViewById(R.id.tvServerPrice);
		tvServerTime = (TextView) this.findViewById(R.id.tvServerTime);
		tvServerTotalPrice = (TextView) this
				.findViewById(R.id.tvServerTotalPrice);

		llContast = (LinearLayout) this.findViewById(R.id.llContast);
		ivHeadImg = (ImageView) this.findViewById(R.id.ivHeadImg);
		tvName = (TextView) this.findViewById(R.id.tvName);
		ivGender = (ImageView) this.findViewById(R.id.ivGender);
		tvAge = (TextView) this.findViewById(R.id.tvAge);
		tvLevel = (TextView) this.findViewById(R.id.tvLevel);
		tvBadge = (TextView) this.findViewById(R.id.tvBadge);
		tvArea = (TextView) this.findViewById(R.id.tvArea);

		tvOrderTime = (TextView) this.findViewById(R.id.tvOrderTime);
		tvOrderPlace = (TextView) this.findViewById(R.id.tvOrderPlace);
		tvServerInfo = (TextView) this.findViewById(R.id.tvServerInfo);

		btn_report_order = (Button) this.findViewById(R.id.btn_report_order);
		btn_report_order.setOnClickListener(this);
		btn_con_roo = (Button) this.findViewById(R.id.btn_con_roo);
		btn_con_roo.setOnClickListener(this);
		btn_con_roo.setText("联系考拉");
		btn_order_state = (Button) this.findViewById(R.id.btn_order_state);
	}

	@Override
	public void fillData() {
		tvConfirmQrCodePrompt.setText("(请在见面后让考拉扫描你的二维码,确认对方已经赴约)");
	}

	private boolean isHandle = false;
	/**
	 * 填充生成订单后返回的信息
	 */
	public void fillOrder() {
		String twoDimensionalCodeUrl = orderBean.getQrcode();
		if (!TextUtils.isEmpty(twoDimensionalCodeUrl)) {
			viewTwoDimensionalCode.setVisibility(View.VISIBLE);
			ivTwoDimCode.setTag(twoDimensionalCodeUrl);
			Drawable cacheDrawable = ImageCacheLoader.getInstance()
					.loadDrawable(twoDimensionalCodeUrl, new ImageCallback() {
						@Override
						public void imageLoaded(Drawable imageDrawable,
								String imageUrl) {
							ImageView ivImage = (ImageView) ivTwoDimCode
									.findViewWithTag(imageUrl);
							if (ivImage != null) {
								if (imageDrawable != null) {
									ivImage.setImageDrawable(imageDrawable);
								}
							}
						}
					});
			if (cacheDrawable != null) {
				ivTwoDimCode.setImageDrawable(cacheDrawable);
			} else {
				ivTwoDimCode.setImageDrawable(null);
			}
		}
		createdTime = orderBean.getCreatedTime();
		orderNumber = orderBean.getOrderNumber();
		serviceDays = orderBean.getServiceDays();
		state = orderBean.getState();
		totalPrice = orderBean.getTotalPrice();
		unitPrice = orderBean.getUnitPrice();

		tvOrderNumHere.setText(String.valueOf(orderNumber));
		tvGeneralDate.setText(createdTime);
		// 0-袋鼠拒绝1-袋鼠未确认2-未支付3-进行中、线上支付4进行中、线下支付5考拉未确认(已见面)6考拉未评价7袋鼠未评价8完成9订单过期
		switch (state) {
		case Constants.ROO_REFUSE:
			tvOrderState.setText("我拒绝的订单");
			btn_con_roo.setVisibility(View.GONE);
			btn_order_state.setText("已拒绝");
			break;
		case Constants.ROO_NOT_CONFIRM:
			tvOrderState.setText("我未接受");
			btn_order_state.setText("接受预约");
			btn_order_state.setBackgroundResource(R.drawable.item_selector);
			btn_order_state.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (NetUtil.checkNet(RooOrderDetailActivity.this)) {
						new UpdateOrderStateTask(orderNumber,
								Constants.KOALA_NOT_PAY, SharedPrefUtil
										.getUserBean(
												RooOrderDetailActivity.this)
										.getAccessToken()).execute();
					} else {
						Toast.makeText(RooOrderDetailActivity.this,
								R.string.NoSignalException, Toast.LENGTH_LONG)
								.show();
						return;
					}
				}
			});
			btn_report_order.setText("拒绝预约");
			btn_report_order.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					AlertDialog dialog = new AlertDialog.Builder(
							RooOrderDetailActivity.this)
							.setMessage("您确定拒绝考拉的预约吗？")
							.setTitle("拒绝预约")
							.setNegativeButton("取消",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									})
							.setNeutralButton("确定",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											if (NetUtil
													.checkNet(RooOrderDetailActivity.this)) {
												new UpdateOrderStateTask(
														orderNumber,
														Constants.ROO_REFUSE,
														SharedPrefUtil
																.getUserBean(
																		RooOrderDetailActivity.this)
																.getAccessToken())
														.execute();
											} else {
												Toast.makeText(
														RooOrderDetailActivity.this,
														R.string.NoSignalException,
														Toast.LENGTH_LONG)
														.show();
												return;
											}
										}
									}).create();
					dialog.show();
				}
			});
			break;

		case Constants.KOALA_NOT_PAY:
			tvOrderState.setText("等待支付");
			btn_order_state.setText("等待支付");
			break;
		case Constants.ONLINE_PAY:
			tvOrderState.setText("已支付");
			btn_order_state.setText("等待确认");
			break;
		case Constants.OFFLINE_PAY:
			tvOrderState.setText("线下支付");
			btn_order_state.setText("等待确认");
			break;
		case Constants.KOALA_NOT_CONFIRM:
			tvOrderState.setText("考拉未确认");
			btn_order_state.setText("等待确认");
			break;
		case Constants.KOALA_NOT_COMMENT:
			tvOrderState.setText("考拉未评价");
			btn_order_state.setText("等待评价");
			break;
		case Constants.ROO_NOT_COMMENT:
			tvOrderState.setText("未评价");
			btn_order_state.setText("请评价");
			btn_order_state.setBackgroundResource(R.drawable.item_selector);
			btn_order_state.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(RooOrderDetailActivity.this,
							OrderCommentActivity.class);
					intent.putExtra("koala", false);
					intent.putExtra("orderBean", orderBean);
					startActivity(intent);
				}
			});
			break;
		case Constants.ORDER_DONE:
			tvOrderState.setText("双方已互评");
			btn_order_state.setText("双方已互评");
			break;
		case Constants.ORDER_INVALID:
			tvOrderState.setText("订单失效");
			btn_order_state.setText("订单失效");
			break;
		default:
			break;
		}

		tvServerPrice.setText(String.valueOf(unitPrice) + " 元/天");
		tvServerTime.setText(String.valueOf(serviceDays) + " 天");
		tvServerTotalPrice.setText(String.valueOf(totalPrice) + " 元");
		String headImgUrl = orderBean.getHeadUrl();
		ivHeadImg.setTag(headImgUrl);
		Drawable cacheDrawable = ImageCacheLoader.getInstance().loadDrawable(
				headImgUrl, new ImageCallback() {
					@Override
					public void imageLoaded(Drawable imageDrawable,
							String imageUrl) {
						ImageView ivPhoto = (ImageView) llContast
								.findViewWithTag(imageUrl);
						if (ivPhoto != null) {
							if (imageDrawable != null) {

								ivPhoto.setImageDrawable(imageDrawable);

							} else {
								if (orderBean.getWasIsKangaroo() == 1) {

									ivPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
								} else
									ivPhoto.setImageResource(R.drawable.bg_photo_defualt);
							}
						}
					}
				});
		if (cacheDrawable != null) {
			ivHeadImg.setImageDrawable(cacheDrawable);
		} else {
			if (orderBean.getWasIsKangaroo() == 1) {
				ivHeadImg.setImageResource(R.drawable.bg_kangoo_photo_defualt);
			} else
				ivHeadImg.setImageResource(R.drawable.bg_photo_defualt);
		}

		tvName.setText(orderBean.getName());
		if (orderBean.getGender().equals("f")) {
			ivGender.setImageResource(R.drawable.ic_fale);
		} else if (orderBean.getGender().equals("m")) {
			ivGender.setImageResource(R.drawable.ic_fale);
		}
		tvAge.setText(orderBean.getAge() + "岁");
		tvBadge.setText(orderBean.getBadge() + "");
		tvLevel.setText("Lv" + orderBean.getLevel());
		tvArea.setText(orderBean.getCity());

		tvOrderTime.setText(orderBean.getOrderTime());
		tvOrderPlace.setText(orderBean.getOrderPlace());
		tvServerInfo.setText(orderBean.getOrderServerInfo());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			startActivity(new Intent(this, MyReceivedOrderActivity.class));
			finish();
			break;
		case R.id.btnRight:
			Intent capture = new Intent(RooOrderDetailActivity.this,
					CaptureActivity.class);
			startActivity(capture);
			break;
		case R.id.btn_report_order:
			final AlertDialog dialogExit = new AlertDialog.Builder(
					RooOrderDetailActivity.this).create();
			dialogExit.show();
			Window dialogWindow = dialogExit.getWindow();
			dialogWindow.setContentView(R.layout.dialog_common_layout);
			WindowManager windowManager = getWindowManager();
			Display display = windowManager.getDefaultDisplay();
			WindowManager.LayoutParams lp = dialogExit.getWindow()
					.getAttributes();
			lp.width = (int) (display.getWidth() - 60); // 设置宽度
			dialogExit.getWindow().setAttributes(lp);

			TextView tvDialogMsg = (TextView) dialogWindow
					.findViewById(R.id.tvDialogMsg);
			tvDialogMsg.setText("您确定投诉吗？");
			Button btnDialogLeft = (Button) dialogWindow
					.findViewById(R.id.btnDialogLeft);
			btnDialogLeft.setText("取消");
			btnDialogLeft.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					dialogExit.dismiss();
				}
			});
			// 关闭对话框架
			Button btnDialogRight = (Button) dialogWindow
					.findViewById(R.id.btnDialogRight);
			btnDialogRight.setText("确定");
			btnDialogRight.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					startActivity(new Intent(RooOrderDetailActivity.this,
							ReportOrderActivity.class).putExtra("orderBean",
							orderBean));
					dialogExit.dismiss();
				}
			});
			break;
		case R.id.btn_con_roo:
			Intent letterIntent = new Intent(RooOrderDetailActivity.this,
					PersonalLetterActivity.class);
			if (orderBean != null) {
				letterIntent.putExtra(Constants.EXTRA_USER_ID,
						orderBean.getWasId());
				letterIntent
						.putExtra(Constants.EXTRA_NAME, orderBean.getName());
				letterIntent.putExtra(Constants.EXTRA_AVATAR,
						orderBean.getHeadUrl());
				letterIntent.putExtra("isRoo", orderBean.getIsKangaroo());
				startActivity(letterIntent);
			}
		default:
			break;
		}
	}

	/**
	 * 获取服务器返回的订单
	 * 
	 * @author syghh
	 * 
	 */
	class ProfileOrderDetailTask extends AsyncTask<Void, Void, JSONObject> {

		private long userId;
		private int orderNumber;
		private String access_token;

		public ProfileOrderDetailTask(int orderNumber, long userId,
				String access_token) {
			this.orderNumber = orderNumber;
			this.userId = userId;
			this.access_token = access_token;

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RooOrderDetailActivity.this);
				pd.setMessage("正在获取...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().profileOrderDetail(orderNumber,
						userId, access_token);
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
						JSONObject orderJson = result.getJSONObject("data");
						orderBean = new OrderBean(orderJson);
						fillOrder();
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(RooOrderDetailActivity.this,
								R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(RooOrderDetailActivity.this,
								LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(RooOrderDetailActivity.this,
								result.getString("error"), Toast.LENGTH_SHORT)
								.show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(RooOrderDetailActivity.this, "订单获取失败",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(RooOrderDetailActivity.this, "服务器请求失败",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 修改订单状态；
	 * 
	 * @author syghh
	 * 
	 */
	class UpdateOrderStateTask extends AsyncTask<Void, Void, JSONObject> {

		private int state;
		private int orderNumber;
		private String access_token;

		public UpdateOrderStateTask(int orderNumber, int state,
				String access_token) {
			this.orderNumber = orderNumber;
			this.state = state;
			this.access_token = access_token;

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RooOrderDetailActivity.this);
				pd.setMessage("正在获取...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				if (state == Constants.KOALA_NOT_PAY) {// 袋鼠确认订单；
					return new BusinessHelper().kangarooConfirmOrder(
							orderNumber, access_token);
				} else {
					return new BusinessHelper().updateOrderState(orderNumber
							+ "", state, access_token);
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
						isHandle = true;
						if (state == Constants.ROO_REFUSE) {
							btn_order_state
									.setBackgroundResource(android.R.color.white);
							btn_order_state.setText("已拒绝");
							tvOrderState.setText("我拒绝的订单");
						} else if (state == Constants.KOALA_NOT_PAY) {
							btn_order_state
									.setBackgroundResource(android.R.color.white);
							btn_order_state.setText("等待支付");
							tvOrderState.setText("我已确认");
						}
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(RooOrderDetailActivity.this,
								R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(RooOrderDetailActivity.this,
								LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(RooOrderDetailActivity.this,
								result.getString("error"), Toast.LENGTH_LONG)
								.show();
					}
				} catch (JSONException e) {
				}
			} else {
				Toast.makeText(RooOrderDetailActivity.this, "服务器请求失败",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 修改订单为已读状态；
	 * 
	 * @author Zhoujun
	 * 
	 */
	private class UpdateOrderTagTask extends AsyncTask<Void, Void, JSONObject> {
		private long userId;
		private String orderNumber;

		public UpdateOrderTagTask(long userId, String orderNumber) {
			super();
			this.userId = userId;
			this.orderNumber = orderNumber;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().updateOrderTag(userId, orderNumber,
						Constants.RECEIVE_ORDER);
			} catch (SystemException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			startActivity(new Intent(this, MyReceivedOrderActivity.class));
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
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

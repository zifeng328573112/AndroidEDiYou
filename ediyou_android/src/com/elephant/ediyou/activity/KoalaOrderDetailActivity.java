package com.elephant.ediyou.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import com.elephant.ediyou.activity.PayWithAlipayActivity.UpdateOrderStateTask;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.AppointmentBean;
import com.elephant.ediyou.bean.OrderBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.sec.code.CaptureActivity;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 订单详情（考拉）
 * 
 * @author syghh
 * 
 */
public class KoalaOrderDetailActivity extends Activity implements
		IBaseActivity, OnClickListener {
	private CommonApplication app;
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	private ProgressDialog pd;

	// 订单信息
	private View viewTwoDimensionalCode;// 二维码布局；
	private ImageView ivTwoDimCode;
	private TextView tvOrderNumHere;
	private TextView tvGeneralDate;
	private TextView tvOrderState;
	private TextView tvServerPrice;
	private TextView tvServerTime;
	private TextView tvServerTotalPrice;
	// 对方资料
	private TextView tvService;// 考拉应该显示预约对象；
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
	// 保险单信息；
	private TextView tvInsuranceTitle;
	private View viewInsurance;
	private TextView tvInsuranceNum;// 保险订单号
	private TextView tvInsuranceType;// 保险种类；
	private TextView tvInsuranceCompany;// 保险公司;
	private TextView tvInsuranceTime;// 投保时间；

	// 预约信息
	private AppointmentBean appointmentBean;
	private long userId;

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
		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {

			case Constants.REQUEST_PAY_SUCCESS:// 付款成功；
				if (NetUtil.checkNet(this)) {
					new ProfileOrderDetailTask(orderNumber, userId,
							SharedPrefUtil.getUserBean(this).getAccessToken())
							.execute();
					new UpdateOrderTagTask(userId, orderNumber + "").execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException,
							Toast.LENGTH_LONG).show();
				}
				break;
			case Constants.REQUEST_SCAN_QRCODE:// 扫描二维码；
				btnRight.setVisibility(View.INVISIBLE);
				if (orderBean.getIsOnline() == 0) {// 线上支付，跳转到支付确认界面；
					tvOrderState.setText("已见面");
					btn_order_state.setText("请确认订单");
					btn_order_state
							.setBackgroundResource(R.drawable.item_selector);
					btn_order_state
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									// Intent intent = new
									// Intent(KoalaOrderDetailActivity.this,
									// PayWithAlipayActivity.class);
									// intent.putExtra("orderNumber",
									// orderBean.getOrderNumber());
									// intent.putExtra("orderBean", orderBean);
									// startActivity(intent);
									showConfirmDialog();
								}
							});
				} else {// 线下支付，跳转到评价界面；
					tvOrderState.setText("已见面");
					btn_order_state.setText("请评价");
					btn_order_state
							.setBackgroundResource(R.drawable.item_selector);
					btn_order_state
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									Intent intent = new Intent(
											KoalaOrderDetailActivity.this,
											OrderCommentActivity.class);
									intent.putExtra("koala", true);
									intent.putExtra("orderBean", orderBean);
									startActivity(intent);
								}
							});
				}
				break;
			default:
				break;
			}
		}
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
		ivTwoDimCode = (ImageView) this.findViewById(R.id.ivTwoDimCode);
		tvOrderNumHere = (TextView) this.findViewById(R.id.tvOrderNumHere);
		tvGeneralDate = (TextView) this.findViewById(R.id.tvGenerateDate);
		tvOrderState = (TextView) this.findViewById(R.id.tvOrderState);
		tvServerPrice = (TextView) this.findViewById(R.id.tvServerPrice);
		tvServerTime = (TextView) this.findViewById(R.id.tvServerTime);
		tvServerTotalPrice = (TextView) this
				.findViewById(R.id.tvServerTotalPrice);

		tvService = (TextView) this.findViewById(R.id.tvService);
		tvService.setText("预约对象");
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

		tvInsuranceTitle = (TextView) findViewById(R.id.tvInsuranceTitle);
		viewInsurance = findViewById(R.id.viewInsurance);
		tvInsuranceNum = (TextView) findViewById(R.id.tvInsuranceNum);
		tvInsuranceType = (TextView) findViewById(R.id.tvInsuranceType);
		tvInsuranceCompany = (TextView) findViewById(R.id.tvInsuranceCompany);
		tvInsuranceTime = (TextView) findViewById(R.id.tvInsuranceTime);

		btn_report_order = (Button) this.findViewById(R.id.btn_report_order);
		btn_report_order.setOnClickListener(this);
		btn_con_roo = (Button) this.findViewById(R.id.btn_con_roo);
		btn_con_roo.setOnClickListener(this);
		btn_order_state = (Button) this.findViewById(R.id.btn_order_state);
	}

	@Override
	public void fillData() {
		userId = SharedPrefUtil.getUserBean(this).getUserId();
		String access_token = SharedPrefUtil.getUserBean(this).getAccessToken();
		appointmentBean = (AppointmentBean) getIntent().getSerializableExtra(
				"orderBean");
		if (appointmentBean != null) {// 生成订单的详情
			String serviceRequire = appointmentBean.getServicerequire();
			String address = appointmentBean.getAddress();
			String reservationTime = appointmentBean.getOrderTime();
			if (NetUtil.checkNet(this)) {
				new ProfileCreateOrderTask(userId, appointmentBean.getRooID(),
						serviceRequire, address, reservationTime, access_token)
						.execute();
			} else {
				Toast.makeText(this, R.string.NoSignalException,
						Toast.LENGTH_LONG).show();
			}
			tvOrderTime.setText(reservationTime);
			tvOrderPlace.setText(address);
			tvServerInfo.setText(serviceRequire);
		} else {
			if (getIntent() != null) {
				long userId = getIntent().getLongExtra("userId", 0);
				int orderNumber = Integer.parseInt(getIntent().getStringExtra(
						"orderNumber"));
				if (NetUtil.checkNet(this)) {
					new ProfileOrderDetailTask(orderNumber, userId,
							SharedPrefUtil.getUserBean(this).getAccessToken())
							.execute();
					new UpdateOrderTagTask(userId, orderNumber + "").execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException,
							Toast.LENGTH_LONG).show();
				}
			}
		}

	}

	/**
	 * 填充生成订单后返回的信息
	 */
	public void fillOrder() {
		// String twoDimensionalCodeUrl = orderBean.getQrcode();
		// if (!TextUtils.isEmpty(twoDimensionalCodeUrl)) {
		// viewTwoDimensionalCode.setVisibility(View.VISIBLE);
		// ivTwoDimCode.setTag(twoDimensionalCodeUrl);
		// Drawable cacheDrawable =
		// ImageCacheLoader.getInstance().loadDrawable(twoDimensionalCodeUrl,
		// new ImageCallback() {
		// @Override
		// public void imageLoaded(Drawable imageDrawable, String imageUrl) {
		// ImageView ivImage = (ImageView)
		// ivTwoDimCode.findViewWithTag(imageUrl);
		// if (ivImage != null) {
		// if (imageDrawable != null) {
		// ivImage.setImageDrawable(imageDrawable);
		// }
		// }
		// }
		// });
		// if (cacheDrawable != null) {
		// ivTwoDimCode.setImageDrawable(cacheDrawable);
		// } else {
		// ivTwoDimCode.setImageDrawable(null);
		// }
		// }
		if (orderBean.getState() == Constants.KOALA_NOT_CONFIRM
				|| orderBean.getState() == Constants.OFFLINE_PAY) {
			btnRight.setVisibility(View.VISIBLE);
		}
		createdTime = orderBean.getCreatedTime();
		orderNumber = orderBean.getOrderNumber();
		serviceDays = orderBean.getServiceDays();
		state = orderBean.getState();
		totalPrice = orderBean.getTotalPrice();
		unitPrice = orderBean.getUnitPrice();

		tvOrderNumHere.setText(String.valueOf(orderNumber));
		tvGeneralDate.setText(createdTime);
		// 0-袋鼠拒绝1-袋鼠未确认2-未支付3-进行中、线上支付4进行中、线下支付5考拉未确认6考拉未评价7袋鼠未评价8完成9订单过期
		switch (state) {
		case Constants.ROO_REFUSE:
			tvOrderState.setText("袋鼠拒绝订单");
			btn_order_state.setVisibility(View.GONE);
			break;
		case Constants.ROO_NOT_CONFIRM:
			tvOrderState.setText("袋鼠未确认");
			btn_order_state.setText("等待确认");
			btn_order_state.setBackgroundResource(android.R.color.white);
			break;
		case Constants.KOALA_NOT_PAY:
			tvOrderState.setText("袋鼠已确认");
			btn_order_state.setText("请付款");
			btn_order_state.setBackgroundResource(R.drawable.item_selector);
			btn_order_state.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(KoalaOrderDetailActivity.this,
							KoalaOrderChoosePayActivity.class);
					intent.putExtra("orderBean", orderBean);
					startActivityForResult(intent,
							Constants.REQUEST_PAY_SUCCESS);
				}
			});
			break;
		case Constants.ONLINE_PAY:
			tvOrderState.setText("已支付");
			btn_order_state.setText("确认订单");
			btn_order_state.setBackgroundResource(R.drawable.item_selector);
			btn_order_state.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Intent intent = new Intent(KoalaOrderDetailActivity.this,
					// PayWithAlipayActivity.class);
					// intent.putExtra("orderNumber",
					// orderBean.getOrderNumber());
					// intent.putExtra("orderBean", orderBean);
					// startActivity(intent);
					showConfirmDialog();
				}
			});
			break;
		case Constants.OFFLINE_PAY:
			tvOrderState.setText("线下支付");
			btn_order_state.setText("请评价");
			btn_order_state.setBackgroundResource(R.drawable.item_selector);
			btn_order_state.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(KoalaOrderDetailActivity.this,
							OrderCommentActivity.class);
					intent.putExtra("koala", true);
					intent.putExtra("orderBean", orderBean);
					startActivity(intent);
				}
			});
			break;
		case Constants.KOALA_NOT_CONFIRM:
			tvOrderState.setText("已支付");
			if (orderBean.getIsOnline() == 0) {// 线上支付，跳转到支付确认界面；
				btn_order_state.setText("请确认订单");
				btn_order_state.setBackgroundResource(R.drawable.item_selector);
				btn_order_state.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// Intent intent = new
						// Intent(KoalaOrderDetailActivity.this,
						// PayWithAlipayActivity.class);
						// intent.putExtra("orderNumber",
						// orderBean.getOrderNumber());
						// intent.putExtra("orderBean", orderBean);
						// startActivity(intent);
						showConfirmDialog();
					}
				});
			} else {// 线下支付，跳转到评价界面；
				btn_order_state.setText("请评价");
				btn_order_state.setBackgroundResource(R.drawable.item_selector);
				btn_order_state.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(
								KoalaOrderDetailActivity.this,
								OrderCommentActivity.class);
						intent.putExtra("koala", true);
						intent.putExtra("orderBean", orderBean);
						startActivity(intent);
					}
				});
			}
			break;
		case Constants.KOALA_NOT_COMMENT:
			tvOrderState.setText("请及时评价");
			btn_order_state.setText("请评价");
			btn_order_state.setBackgroundResource(R.drawable.item_selector);
			btn_order_state.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(KoalaOrderDetailActivity.this,
							OrderCommentActivity.class);
					intent.putExtra("koala", true);
					intent.putExtra("orderBean", orderBean);
					startActivity(intent);
				}
			});
			break;
		case Constants.ROO_NOT_COMMENT:
			tvOrderState.setText("等待袋鼠评价");
			btn_order_state.setText("等待袋鼠评价");
			break;

		case Constants.ORDER_DONE:
			tvOrderState.setText("双方已互评");
			btn_order_state.setVisibility(View.GONE);
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
								if (orderBean.getIsKangaroo() == 1) {
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
			if (orderBean.getIsKangaroo() == 1) {
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

		if (!TextUtils.isEmpty(orderBean.getInsuranceNo())) {
			tvInsuranceTitle.setVisibility(View.VISIBLE);
			viewInsurance.setVisibility(View.VISIBLE);

			tvInsuranceNum.setText(orderBean.getInsuranceNo());
			tvInsuranceType.setText(orderBean.getInsuranceType());
			tvInsuranceCompany.setText(orderBean.getInsuranceCompany());
			tvInsuranceTime.setText(orderBean.getInsuranceTime());
		}
	}

	private void showConfirmDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_common_layout, null);
		TextView tvDialogMsg = (TextView) view.findViewById(R.id.tvDialogMsg);
		Button btnDialogLeft = (Button) view.findViewById(R.id.btnDialogLeft);
		btnDialogLeft.setText("确认");
		Button btnDialogRight = (Button) view.findViewById(R.id.btnDialogRight);
		btnDialogRight.setText("取消");
		tvDialogMsg.setText("请确认已经进行二维码扫描并且本次服务已经结束，否则可能会人财两空!");
		// tvDialogMsg.setText("请在本次服务完成后再确认订单，否则可能会人财两空!");
		final Dialog dialog = new Dialog(this, R.style.dialog);
		dialog.setContentView(view);
		dialog.show();
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
		lp.width = (int) (display.getWidth() - 60); // 设置宽度
		dialog.getWindow().setAttributes(lp);
		final String access_token = SharedPrefUtil.getUserBean(this).getAccessToken();
		btnDialogLeft.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (NetUtil.checkNet(KoalaOrderDetailActivity.this)) {

					new koalaPayTask(userId, orderNumber, access_token)
							.execute();
					new UpdateOrderStateTask(orderNumber + "",
							Constants.KOALA_NOT_COMMENT).execute();
				} else {
					Toast.makeText(KoalaOrderDetailActivity.this,
							R.string.NoSignalException, Toast.LENGTH_SHORT)
							.show();
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			if (SharedPrefUtil.getUserBean(this).getIsKangaroo() == 0) {
				startActivity(new Intent(this, MyOrderActivity.class).putExtra(
						"from", "koalaSelf"));
				finish();
			} else {
				startActivity(new Intent(this, MyOrderActivity.class).putExtra(
						"from", "rooSelf"));
				finish();
			}

			break;
		case R.id.btnRight:
			Intent capture = new Intent(KoalaOrderDetailActivity.this,
					CaptureActivity.class);
			capture.putExtra("orderNumber", orderBean.getOrderNumber());
			startActivityForResult(capture, Constants.REQUEST_SCAN_QRCODE);
			break;
		case R.id.btn_report_order:
			final AlertDialog dialogExit = new AlertDialog.Builder(
					KoalaOrderDetailActivity.this).create();
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
					dialogExit.dismiss();
					startActivity(new Intent(KoalaOrderDetailActivity.this,
							ReportOrderActivity.class).putExtra("orderBean",
							orderBean));
				}
			});
			break;
		case R.id.btn_con_roo:
			Intent letterIntent = new Intent(KoalaOrderDetailActivity.this,
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
			break;
		case R.id.btn_order_state:
			Intent payIntent = new Intent(this,
					KoalaOrderChoosePayActivity.class);
			payIntent.putExtra("orderNumber", orderBean.getOrderNumber());
			startActivity(payIntent);
			break;
		default:
			break;
		}
	}

	/**
	 * 生成订单任务
	 * 
	 * @author syghh
	 * 
	 */
	class ProfileCreateOrderTask extends AsyncTask<Void, Void, JSONObject> {

		private long userId;
		private long kangarooId;
		private String serviceRequire;
		private String address;
		private String reservationTime;
		private String access_token;

		public ProfileCreateOrderTask(long userId, long kangarooId,
				String serviceRequire, String address, String reservationTime,
				String access_token) {
			this.userId = userId;
			this.kangarooId = kangarooId;
			this.serviceRequire = serviceRequire;
			this.address = address;
			this.reservationTime = reservationTime;
			this.access_token = access_token;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(KoalaOrderDetailActivity.this);
				pd.setMessage("正在生成...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().profileCreateOrder(userId,
						kangarooId, serviceRequire, address, reservationTime,
						access_token);
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
						Toast.makeText(KoalaOrderDetailActivity.this, "订单生成成功",
								Toast.LENGTH_LONG).show();
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(KoalaOrderDetailActivity.this,
								R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(KoalaOrderDetailActivity.this,
								LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(KoalaOrderDetailActivity.this,
								result.getString("error"), Toast.LENGTH_LONG)
								.show();
					}
				} catch (JSONException e) {
					Toast.makeText(KoalaOrderDetailActivity.this, "订单生成失败",
							Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(KoalaOrderDetailActivity.this, "服务器请求失败",
						Toast.LENGTH_LONG).show();
			}
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
				pd = new ProgressDialog(KoalaOrderDetailActivity.this);
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
						Toast.makeText(KoalaOrderDetailActivity.this,
								R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(KoalaOrderDetailActivity.this,
								LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(KoalaOrderDetailActivity.this,
								result.getString("error"), Toast.LENGTH_SHORT)
								.show();
					}
				} catch (JSONException e) {
					Toast.makeText(KoalaOrderDetailActivity.this, "订单获取失败",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				// Toast.makeText(KoalaOrderDetailActivity.this, "服务器请求失败",
				// Toast.LENGTH_SHORT).show();
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
						Constants.MY_ORDER);
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
			if (SharedPrefUtil.getUserBean(this).getIsKangaroo() == 0) {
				startActivity(new Intent(this, MyOrderActivity.class).putExtra(
						"from", "koalaSelf"));
				finish();
			} else {
				startActivity(new Intent(this, MyOrderActivity.class).putExtra(
						"from", "rooSelf"));
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 确认订单
	 * 
	 * @author hubujun
	 * 
	 */
	private class koalaPayTask extends AsyncTask<Void, Void, JSONObject> {
		private long userId;
		private int orderNumber;
		private String access_token;

		public koalaPayTask(long userId, int orderNumber, String access_token) {
			super();
			this.userId = userId;
			this.orderNumber = orderNumber;
			this.access_token = access_token;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().payUseAccount(userId,
						orderNumber, access_token);
			} catch (SystemException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
		}

	}

	/**
	 * 考拉确认订单
	 * 
	 * @author syghh
	 * 
	 */
	class UpdateOrderStateTask extends AsyncTask<Void, Void, JSONObject> {

		private String orderNumber;
		private int state;

		public UpdateOrderStateTask(String orderNumber, int state) {
			this.orderNumber = orderNumber;
			this.state = state;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			String access_token = SharedPrefUtil.getUserBean(
					KoalaOrderDetailActivity.this).getAccessToken();
			try {
				JSONObject result = new BusinessHelper().updateOrderState(
						orderNumber, state, access_token);
				return result;
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
						Toast.makeText(KoalaOrderDetailActivity.this,
								"您已经确认订单，请及时给袋鼠评价打分", Toast.LENGTH_LONG).show();
						tvOrderState.setText("请及时评价");
						btn_order_state.setText("请评价");
						btn_order_state
								.setBackgroundResource(R.drawable.item_selector);
						btn_order_state
								.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View v) {
										Intent intent = new Intent(
												KoalaOrderDetailActivity.this,
												OrderCommentActivity.class);
										intent.putExtra("koala", true);
										intent.putExtra("orderBean", orderBean);
										startActivity(intent);
									}
								});
					} else {
						Toast.makeText(KoalaOrderDetailActivity.this,
								result.getString("error"), Toast.LENGTH_LONG)
								.show();
					}
				} catch (JSONException e) {
					// Toast.makeText(PayWithAlipayActivity.this, "提交失败",
					// Toast.LENGTH_LONG).show();
				}
			} else {
				// Toast.makeText(PayWithAlipayActivity.this, "服务器请求失败",
				// Toast.LENGTH_LONG).show();
			}
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

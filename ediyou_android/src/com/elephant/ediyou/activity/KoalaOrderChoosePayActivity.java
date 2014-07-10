package com.elephant.ediyou.activity;

import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.OrderBean;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.elephant.ediyou.util.alipay.AlixId;
import com.elephant.ediyou.util.alipay.BaseHelper;
import com.elephant.ediyou.util.alipay.MobileSecurePayHelper;
import com.elephant.ediyou.util.alipay.MobileSecurePayer;
import com.elephant.ediyou.util.alipay.PartnerConfig;
import com.elephant.ediyou.util.alipay.ResultChecker;
import com.elephant.ediyou.util.alipay.Rsa;

/**
 * 订单详情（考拉，选择支付方式）
 * 
 * @author syghh
 * 
 */
public class KoalaOrderChoosePayActivity extends Activity implements IBaseActivity, OnClickListener {

	private CommonApplication app;
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	private ProgressDialog pd;

	private TextView tvOrderNum;
	private OrderBean orderBean;

	private ImageView ivChoicedOnlinePay;
	private ImageView ivChoicedOfflinePay;
	private ImageView ivCoupon;// 免费券 （不使用了）
	private ImageView ivInsurance;// 保险；
	private TextView tvInsuranceIntroduce;// 显示保险说明
	private boolean isGetInsurance = true;// 是否获取保险；

	private EditText etPhoneNum, etName, etIdCard, etEmail;
	//private RadioGroup rgGender;
	private String genderStr = "m";

	private View viewInsuranceTitle;// 保险的标题；
	private View viewInsurance;// 获取保险填写界面；

	private boolean isChoiceOnlinePay = true;// 在线支付与否
	UserBean userBean;
	private static int NO_COUPON = 2;// 没有免费券；
	private static int INSUFFICIENT_BALANCE = 3;// 余额不足；

	private ProgressDialog mProgress = null;
	private static final String TAG = "KoalaOrderChoosePayActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.koala_order_choose_pay);
		userBean = SharedPrefUtil.getUserBean(KoalaOrderChoosePayActivity.this);
		if (getIntent() != null) {
			orderBean = (OrderBean) getIntent().getSerializableExtra("orderBean");
		}
		app = (CommonApplication) getApplication();
		findView();
		fillData();
		app.addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) findViewById(R.id.btnLeft);
		btnRight = (Button) findViewById(R.id.btnRight);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(R.string.order_detail);
		tvTitle.setVisibility(View.VISIBLE);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnLeft.setOnClickListener(this);
		btnRight.setBackgroundResource(R.drawable.ic_submit_selector);
		btnRight.setOnClickListener(this);

		tvOrderNum = (TextView) findViewById(R.id.tvOrderNum);
		ivChoicedOnlinePay = (ImageView) findViewById(R.id.ivChoicedOnlinePay);
		ivChoicedOfflinePay = (ImageView) findViewById(R.id.ivChoicedOfflinePay);
		ivCoupon = (ImageView) findViewById(R.id.ivCoupon);
		ivInsurance = (ImageView) findViewById(R.id.ivInsurance);
		tvInsuranceIntroduce = (TextView) findViewById(R.id.tvInsuranceIntroduce);

		viewInsurance = findViewById(R.id.viewInsurance);
		viewInsuranceTitle = findViewById(R.id.viewInsuranceTitle);

		etPhoneNum = (EditText) findViewById(R.id.etPhoneNum);
		etName = (EditText) findViewById(R.id.etName);
		etIdCard = (EditText) findViewById(R.id.etIdCard);
		etEmail = (EditText) findViewById(R.id.etEmail);
		//rgGender = (RadioGroup) findViewById(R.id.rgGender);

		ivChoicedOnlinePay.setOnClickListener(this);
		ivChoicedOfflinePay.setOnClickListener(this);
		ivCoupon.setOnClickListener(this);
		ivInsurance.setOnClickListener(this);

	}

	@Override
	public void fillData() {
		tvTitle.setText("订单详情");
		tvOrderNum.setText(orderBean.getOrderNumber() + "");
		/*rgGender.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == R.id.rbMale){
					genderStr = "m";
				}else{
					genderStr = "f";
				}
			}
		});*/
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			this.finish();
			break;
		case R.id.btnRight:
			if (isChoiceOnlinePay == false) {
				if (NetUtil.checkNet(this)) {
//					new paySuccessTask(orderBean.getOrderNumber() + "", userBean.getAccessToken()).execute();
					new ProfileUpdateOrderOnline(orderBean.getOrderNumber() + "", userBean.getAccessToken()).execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
			} else {
				// 当选择线上支付时，更改状态码为已支付到第三方
				// if (NetUtil.checkNet(this)) {
				// if (isUseCoupon) {//代金券 (现在已经不用了)
				// new PayOrderTask("coupon", orderNum,
				// userBean.getAccessToken()).execute();
				// } else {
				// new PayOrderTask("account", orderNum,
				// userBean.getAccessToken()).execute();
				// }
				// } else {
				// Toast.makeText(this, R.string.NoSignalException,
				// Toast.LENGTH_SHORT).show();
				// }
				String phoneStr = etPhoneNum.getText().toString();
				String nameStr = etName.getText().toString();
				String idCardStr = etIdCard.getText().toString();
				String emailStr = etEmail.getText().toString();
				 if(!StringUtil.isMobile(phoneStr)){
				 Toast.makeText(this, "请输入正确的手机号码！",
				 Toast.LENGTH_SHORT).show();
				 return;
				 }
				 if(TextUtils.isEmpty(nameStr)){
				 Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show();
				 return;
				 }
				 if(TextUtils.isEmpty(idCardStr)){
				 Toast.makeText(this, "身份证不能为空", Toast.LENGTH_SHORT).show();
				 return;
				 }
				 if(!StringUtil.isEmail(emailStr)){
				 Toast.makeText(this, "请填写正确的邮箱", Toast.LENGTH_SHORT).show();
				 return;
				 }
				if (NetUtil.checkNet(this)) {
					
					new InsuranceTask(orderBean.getOrderNumber() + "", nameStr, idCardStr, emailStr, phoneStr,genderStr).execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
			}

			break;
		case R.id.ivChoicedOnlinePay:
			if (isChoiceOnlinePay == false) {
				isChoiceOnlinePay = true;
				ivChoicedOnlinePay.setImageResource(R.drawable.btn_round_checked);
				ivChoicedOfflinePay.setImageResource(R.drawable.btn_round_uncheck);
				viewInsuranceTitle.setVisibility(View.VISIBLE);
				tvInsuranceIntroduce.setVisibility(View.VISIBLE);
				viewInsurance.setVisibility(View.VISIBLE);
			} else {
				ivChoicedOnlinePay.setImageResource(R.drawable.btn_round_checked);
				ivChoicedOfflinePay.setImageResource(R.drawable.btn_round_uncheck);
				viewInsuranceTitle.setVisibility(View.GONE);
				tvInsuranceIntroduce.setVisibility(View.GONE);
				viewInsurance.setVisibility(View.GONE);
			}
			break;
		case R.id.ivChoicedOfflinePay:
			if (isChoiceOnlinePay == true) {
				isChoiceOnlinePay = false;
				ivChoicedOfflinePay.setImageResource(R.drawable.btn_round_checked);
				ivChoicedOnlinePay.setImageResource(R.drawable.btn_round_uncheck);
				viewInsurance.setVisibility(View.GONE);
				ivInsurance.setImageResource(R.drawable.btn_square_uncheck);
				viewInsuranceTitle.setVisibility(View.GONE);
				tvInsuranceIntroduce.setVisibility(View.GONE);
				viewInsurance.setVisibility(View.GONE);
			} else {
				viewInsurance.setVisibility(View.GONE);
				ivInsurance.setImageResource(R.drawable.btn_square_uncheck);
				ivChoicedOfflinePay.setImageResource(R.drawable.btn_round_checked);
				ivChoicedOnlinePay.setImageResource(R.drawable.btn_round_uncheck);
				viewInsuranceTitle.setVisibility(View.VISIBLE);
				tvInsuranceIntroduce.setVisibility(View.VISIBLE);
				viewInsurance.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.ivCoupon:
			break;
		case R.id.ivInsurance:
			break;
		default:
			break;
		}
	}

	//
	// the handler use to receive the pay result.
	// 这里接收支付结果，支付宝手机端同步通知
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				String strRet = (String) msg.obj;
                                     //resultStatus={9000};memo={};result={partner="2088901291567284"&seller="account2@ediyou.cn"&out_trade_no="1368708710"&subject="E地游预约订单"&body="暂无描述"&total_fee="1"&notify_url="http://192.168.1.115:8080/ediyou/api/v1/profile/payForSuccess.json"&success="true"&sign_type="RSA"&sign="QlcjSYSuUtkGqixyMsMDwCQyP5vssQmkWFOnpa8kcRpjNtNqdh+Yd10vLGBBU8pYH1Su8opde9hhJkSnfiqQ8eQ0kGo8Z30XmG8OupP9luGRwjjlBJTqAsXvDrgzM9LGapoSI5We0hQ0+vJDgRmWR3B9Dx0sfd4qI1TWeazzMXc="}
				Log.e(TAG, strRet); // strRet范例：resultStatus={9000};memo={};result={partner="2088201564809153"&seller="2088201564809153"&out_trade_no="050917083121576"&subject="123456"&body="2010新款NIKE 耐克902第三代板鞋 耐克男女鞋 386201 白红"&total_fee="0.01"&notify_url="http://notify.java.jpxx.org/index.jsp"&success="true"&sign_type="RSA"&sign="d9pdkfy75G997NiPS1yZoYNCmtRbdOP0usZIMmKCCMVqbSG1P44ohvqMYRztrB6ErgEecIiPj9UldV5nSy9CrBVjV54rBGoT6VSUF/ufjJeCSuL510JwaRpHtRPeURS1LXnSrbwtdkDOktXubQKnIMg2W0PreT1mRXDSaeEECzc="}
				switch (msg.what) {
				case AlixId.RQF_PAY: {
					//
					closeProgress();

					BaseHelper.log(TAG, strRet);

					// 处理交易结果
					try {
						// 获取交易状态码，具体状态代码请参看文档
						String tradeStatus = "resultStatus={";
						int imemoStart = strRet.indexOf("resultStatus=");
						imemoStart += tradeStatus.length();
						int imemoEnd = strRet.indexOf("};memo=");
						tradeStatus = strRet.substring(imemoStart, imemoEnd);

						// 先验签通知
						ResultChecker resultChecker = new ResultChecker(strRet);
						int retVal = resultChecker.checkSign();
						// 验签失败
						if (retVal == ResultChecker.RESULT_CHECK_SIGN_FAILED) {
							BaseHelper.showDialog(KoalaOrderChoosePayActivity.this, "提示", getResources().getString(R.string.check_sign_failed),
									android.R.drawable.ic_dialog_alert);
						} else {// 验签成功。验签成功后再判断交易状态码
							if (tradeStatus.equals("9000"))// 判断交易状态码，只有9000表示交易成功
							{
					/*			new Thread(){
									public void run() {
										
										try {
											new BusinessHelper().getPayForSuccess(""+orderBean.getOrderNumber());
										} catch (SystemException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									};
								}.start();
								*/
								
								new paySuccessTask(orderBean.getOrderNumber() + "", userBean.getAccessToken()).execute();
								
								setResult(RESULT_OK);
								finish();
							} else
								BaseHelper.showDialog(KoalaOrderChoosePayActivity.this, "提示", "支付失败。交易状态码:" + tradeStatus, R.drawable.infoicon);
						}

					} catch (Exception e) {
						e.printStackTrace();
						BaseHelper.showDialog(KoalaOrderChoosePayActivity.this, "提示", strRet, R.drawable.infoicon);
					}
				}
					break;
				}

				super.handleMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 支付成功
	 * 
	 * @author hubujun
	 * 
	 */
	class paySuccessTask extends AsyncTask<Void, Void, JSONObject> {
		
		private String orderNumber;
		private 	String accessToken;
		public paySuccessTask(String orderNumber, String accessToken) {
			this.orderNumber = orderNumber;
			this.accessToken = accessToken;
		}
		
		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().getPayForSuccess(orderNumber,accessToken);
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
						System.out.println("----------------------------"+result);
					} else {
						Toast.makeText(KoalaOrderChoosePayActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(KoalaOrderChoosePayActivity.this, "提交失败", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(KoalaOrderChoosePayActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
		}
	}
	/**
	 * 提交（线下支付的选择）
	 * 
	 * @author syghh
	 * 
	 */
	class ProfileUpdateOrderOnline extends AsyncTask<Void, Void, JSONObject> {

		private String orderNumber;
		private String accessToken;

		public ProfileUpdateOrderOnline(String orderNumber, String accessToken) {
			this.orderNumber = orderNumber;
			this.accessToken = accessToken;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().profileUpdateOrderOnline(orderNumber, accessToken);
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
						Intent intent = getIntent();
						intent.putExtra("url", result.getString("data"));
						intent.putExtra("isOnline", 1);
						setResult(RESULT_OK, intent);
						finish();
					} else {
						Toast.makeText(KoalaOrderChoosePayActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(KoalaOrderChoosePayActivity.this, "提交失败", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(KoalaOrderChoosePayActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 提交保单信息后再支付；
	 * @author Zhoujun
	 *
	 */
	private class InsuranceTask extends AsyncTask<Void, Void, JSONObject>{

		private String orderNumber;
		private String realName;
		private String card;
		private String email;
		private String phone;
		private String gender;
		
		
		public InsuranceTask(String orderNumber, String realName, String card, String email, String phone,String gender) {
			super();
			this.orderNumber = orderNumber;
			this.realName = realName;
			this.card = card;
			this.email = email;
			this.phone = phone;
			this.gender = gender;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(pd == null){
				pd = new ProgressDialog(KoalaOrderChoosePayActivity.this);
			}
			pd.setMessage("正在提交保险信息...");
			pd.show();
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().uploadInsuranceInfo(orderNumber, realName, card, email, phone,gender);
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
			if(result != null){
				try {
					int status = result.getInt("status");
					if(status == Constants.SUCCESS){
						MobileSecurePayHelper mspHelper = new MobileSecurePayHelper(KoalaOrderChoosePayActivity.this);
						boolean isMobile_spExist = mspHelper.detectMobile_sp();
						if (!isMobile_spExist)
							return;

						try {
							// prepare the order info.
							// 准备订单信息
							String orderInfo = getOrderInfo();
							// 这里根据签名方式对订单信息进行签名
							String signType = getSignType();
							String strsign = sign(signType, orderInfo);
							Log.v("sign:", strsign);
							// 对签名进行编码
							strsign = URLEncoder.encode(strsign);
							// 组装好参数
							String info = orderInfo + "&sign=" + "\"" + strsign + "\"" + "&" + getSignType();
							Log.v("orderInfo:", info);
							// start the pay.
							// 调用pay方法进行支付
							MobileSecurePayer msp = new MobileSecurePayer();
							boolean bRet = msp.pay(info, mHandler, AlixId.RQF_PAY, KoalaOrderChoosePayActivity.this);

							if (bRet) {
								// show the progress bar to indicate that we have
								// started
								// paying.
								// 显示“正在支付”进度条
								closeProgress();
								mProgress = BaseHelper.showProgress(KoalaOrderChoosePayActivity.this, null, "正在支付", false, true);
							} else
								;
						} catch (Exception ex) {
							Toast.makeText(KoalaOrderChoosePayActivity.this, R.string.remote_call_failed, Toast.LENGTH_SHORT).show();
						}
					}else{
						Toast.makeText(KoalaOrderChoosePayActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
				}
			}else{
				Toast.makeText(KoalaOrderChoosePayActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show();
			}
		}
	}

	String getOrderInfo() {
		String strOrderInfo = "partner=" + "\"" + PartnerConfig.PARTNER + "\"";
		strOrderInfo += "&";
		strOrderInfo += "seller=" + "\"" + PartnerConfig.SELLER + "\"";
		strOrderInfo += "&";
		strOrderInfo += "out_trade_no=" + "\"" + orderBean.getOrderNumber() + "\"";
		strOrderInfo += "&";
		strOrderInfo += "subject=" + "\"" + "E地游预约订单" + "\"";
		strOrderInfo += "&";
		strOrderInfo += "body=" + "\""+"暂无描述" + "\"";
		strOrderInfo += "&";
		strOrderInfo += "total_fee=" + "\"" + orderBean.getTotalPrice() + "\"";
		strOrderInfo += "&";
		// 回调页需要重新填写
		strOrderInfo += "notify_url=" + "\"" + new BusinessHelper().getNotifyUrl() + "\"";

		return strOrderInfo;
	}

	/**
	 * get the sign type we use. 获取签名方式
	 * 
	 * @return
	 */
	String getSignType() {
		String getSignType = "sign_type=" + "\"" + "RSA" + "\"";
		return getSignType;
	}

	/**
	 * sign the order info. 对订单信息进行签名
	 * 
	 * @param signType
	 *            签名方式
	 * @param content
	 *            待签名订单信息
	 * @return
	 */
	String sign(String signType, String content) {
		return Rsa.sign(content, PartnerConfig.RSA_PRIVATE);
	}

	//
	// close the progress bar
	// 关闭进度框
	void closeProgress() {
		try {
			if (mProgress != null) {
				mProgress.dismiss();
				mProgress = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

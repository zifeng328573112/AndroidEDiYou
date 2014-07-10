package com.elephant.ediyou.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.OrderBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 支付宝支付
 * 
 * @author syghh
 * 
 */
public class PayWithAlipayActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	private ProgressDialog pd;

	private TextView tvOrderNumberInPay;

	private EditText edtAplipayPassword;
	private String aplipayPasswordStr;

	private OrderBean orderBean;
	private int orderNumber;
	private String access_token;
	// dialog
	private TextView tvDialogMsg;
	private Button btnDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay_with_aplipay);
		if (getIntent() != null) {
			orderBean = (OrderBean) getIntent().getSerializableExtra("orderBean");
			orderNumber = getIntent().getIntExtra("orderNumber", 0);
		}
		access_token = SharedPrefUtil.getUserBean(this).getAccessToken();
		findView();
		fillData();
		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.ic_submit_selector);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

		tvOrderNumberInPay = (TextView) this.findViewById(R.id.tvOrderNumberInPay);

		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);

		edtAplipayPassword = (EditText) this.findViewById(R.id.edtAplipayPassword);
	}

	@Override
	public void fillData() {
		tvTitle.setText("确认订单");
		tvOrderNumberInPay.setText("订单编号:" + orderNumber);
		// 弹出提示框，提醒用户；
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_alert, null);
		tvDialogMsg = (TextView) view.findViewById(R.id.tvDialogMsg);
		btnDialog = (Button) view.findViewById(R.id.btnDialog);
//		tvDialogMsg.setText("请在本次服务完成后再确认订单，否则可能会人财两空!");
		tvDialogMsg.setText("请确认已经进行二维码扫描并且本次服务已经结束，否则可能会人财两空!");
		final Dialog dialog = new Dialog(this, R.style.dialog);
		dialog.setContentView(view);
		dialog.show();
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
		lp.width = (int) (display.getWidth() - 60); // 设置宽度
		dialog.getWindow().setAttributes(lp);

		btnDialog.setOnClickListener(new View.OnClickListener() {

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
			finish();
			break;
		case R.id.btnRight:// 提交
			// aplipayPasswordStr =
			// edtAplipayPassword.getText().toString().trim();
			// if(aplipayPasswordStr == null){
			// Toast.makeText(this, "请填写您的支付宝密码", Toast.LENGTH_SHORT).show();
			// } else {
			// 异步任务（或进入支付宝界面）
			if (NetUtil.checkNet(this)) {
				new UpdateOrderStateTask(orderNumber + "", Constants.KOALA_NOT_COMMENT, access_token).execute();
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
			}

			// }
			break;
		}
	}

	/**
	 * 提交付款状态（已支付到袋鼠）
	 * 
	 * @author syghh
	 * 
	 */
	class UpdateOrderStateTask extends AsyncTask<Void, Void, JSONObject> {

		private String orderNumber;
		private int state;
		private String access_token;

		public UpdateOrderStateTask(String orderNumber, int state, String access_token) {
			this.orderNumber = orderNumber;
			this.state = state;
			this.access_token = access_token;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().updateOrderState(orderNumber, state, access_token);
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
						startActivity(new Intent(PayWithAlipayActivity.this, OrderCommentActivity.class).putExtra("orderBean", orderBean));// 测试
						PayWithAlipayActivity.this.finish();
						// Toast.makeText(KoalaOrderChoosePayActivity.this,
						// "获取成功", Toast.LENGTH_LONG)
						// .show();
					} else {
						Toast.makeText(PayWithAlipayActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
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

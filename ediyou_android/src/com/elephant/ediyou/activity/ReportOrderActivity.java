package com.elephant.ediyou.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.CommentShowActivity.GetCommentListTask;
import com.elephant.ediyou.activity.RegisterNextActivity.profileInfoAddTask;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.OrderBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;

/**
 * 举报订单
 * 
 * @author SongYuan
 * 
 */
public class ReportOrderActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;

	private TextView tvOrderNum;
	private TextView tvWasNickName;
	private EditText edtReportReason;

	private OrderBean orderBean;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_order);
		orderBean = (OrderBean) getIntent().getSerializableExtra("orderBean");
		findView();
		fillData();
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.ic_submit_selector);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("举报订单");

		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);

		tvOrderNum = (TextView) this.findViewById(R.id.tvOrderNum);
		tvWasNickName = (TextView) this.findViewById(R.id.tvWasNickName);
		edtReportReason = (EditText) this.findViewById(R.id.edtReportReason);

	}

	@Override
	public void fillData() {
		tvOrderNum.setText(String.valueOf(orderBean.getOrderNumber()));
		tvWasNickName.setText(orderBean.getName());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnRight:// 提交
			long userId = orderBean.getUserId();
			String contents = edtReportReason.getText().toString().trim();
			long informerId = orderBean.getWasId();
			String access_token = SharedPrefUtil.getUserBean(this).getAccessToken();
			String orderNum = String.valueOf(orderBean.getOrderNumber());
			if (access_token != null && SharedPrefUtil.checkToken(this)) {
				if (!StringUtil.isBlank(contents)) {
					if (NetUtil.checkNet(this)) {
						new ReportOrderTask(userId, contents, informerId, access_token, orderNum).execute();
					} else {
						Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(this, "请您输入举报原因", Toast.LENGTH_LONG).show();
				}
			}
			break;
		}
	}

	/**
	 * 举报订单
	 * 
	 * @author syghh
	 * 
	 */
	class ReportOrderTask extends AsyncTask<Void, Void, JSONObject> {
		private long userId;
		private String contents;
		private long informerId;
		private String access_token;
		private String orderNum;

		public ReportOrderTask(long userId, String contents, long informerId, String access_token, String orderNum) {
			this.userId = userId;
			this.contents = contents;
			this.informerId = informerId;
			this.access_token = access_token;
			this.orderNum = orderNum;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().reportOrder(userId, contents, informerId, access_token, orderNum);
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						Toast.makeText(ReportOrderActivity.this, "您的举报提交成功，客服人员将在24小时内与您联系", Toast.LENGTH_LONG).show();
						finish();
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(ReportOrderActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(ReportOrderActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(ReportOrderActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(ReportOrderActivity.this, "提交失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(ReportOrderActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

}

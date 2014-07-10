package com.elephant.ediyou.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
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
import com.umeng.analytics.MobclickAgent;

/**
 * 评价界面，订单的双方互评
 * 
 * @author SongYuan
 * 
 */
public class OrderCommentActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	private ProgressDialog pd;

	private TextView tvCommentPoint;
	private RatingBar ratingbar;
	private EditText edtComment;

	private long userId;
	private String access_token;

	private OrderBean orderBean = null;

	private boolean firstComment = false;// 评价，第一步
	private UserBean userBean;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_comment);
		userBean = SharedPrefUtil.getUserBean(this);
		userId = userBean.getUserId();
		access_token = SharedPrefUtil.getUserBean(this).getAccessToken();
		if (getIntent() != null) {
			orderBean = (OrderBean) getIntent().getSerializableExtra("orderBean");
		}
		firstComment = getIntent().getBooleanExtra("koala", false);
		findView();
		fillData();
		// 将Activity添加到容器
		((CommonApplication) getApplicationContext()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.ic_submit_selector);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		tvCommentPoint = (TextView) this.findViewById(R.id.tvCommentPoint);
		ratingbar = (RatingBar) this.findViewById(R.id.ratingbar);
		edtComment = (EditText) this.findViewById(R.id.edtComment);
	}

	@Override
	public void fillData() {
		//输入字数限制（字符）
		StringUtil.limitEditTextLength(edtComment, 200, this);
		if (firstComment == true) {
			tvCommentPoint.setText("请您为袋鼠打分。如在7日内未打分，系统将自动给予对方3星评价");
		} else {
			tvCommentPoint.setText("考拉已为您的服务打分咯，请为您的服务对象打分。如在7日内未打分，系统将自动给予对方3星评价");
		}
		tvTitle.setText("订单评价");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnRight:// 提交
			int level = (int) ratingbar.getRating();
			String commentStr = edtComment.getText().toString();
			long kangarooId = orderBean.getKangarooId();
			long promoter = orderBean.getPromoter();
			
			int orderNumber = orderBean.getOrderNumber();
			int isKangaroo = SharedPrefUtil.getUserBean(this).getIsKangaroo();
			long userID = SharedPrefUtil.getUserBean(this).getUserId();
			boolean isOrderPromoter = false;
			if(userID==promoter){
				isOrderPromoter = true;
			}else{
				isOrderPromoter = false;
			}
			if (level == 0) {
				level = 3;
			}
			if (TextUtils.isEmpty(commentStr)) {
				Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
				return;
			}
			if (NetUtil.checkNet(this)) {
				new OrderCommentInsertTask(kangarooId, promoter, level, orderNumber, commentStr, access_token, isOrderPromoter).execute();
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
			}

			break;
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

	/**
	 * 提交评论
	 * 
	 * @author syghh
	 * 
	 */
	class OrderCommentInsertTask extends AsyncTask<Void, Void, JSONObject> {
		long kangarooId;
		long promoter;
		int level;
		int orderNumber;
		String content;
		String access_token;
		boolean firstComment;

		public OrderCommentInsertTask(long kangarooId, long promoter, int level, int orderNumber, String content, String access_token,
				boolean firstComment) {
			this.kangarooId = kangarooId;
			this.promoter = promoter;
			this.level = level;
			this.orderNumber = orderNumber;
			this.content = content;
			this.access_token = access_token;
			this.firstComment = firstComment;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(OrderCommentActivity.this);
				pd.setMessage("提交中...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().orderCommentInsert(kangarooId, promoter, level, orderNumber, content, access_token, firstComment);
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (pd != null) {
				pd.dismiss();
			}
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						if (userBean.getIsKangaroo() == 0) {
							startActivity(new Intent(OrderCommentActivity.this, KoalaSelfCenterActivity.class));
							finish();
						} else if (userBean.getIsKangaroo() == 1) {
							startActivity(new Intent(OrderCommentActivity.this, RooSelfCenterActivity.class));
							finish();
						}
						Toast.makeText(OrderCommentActivity.this, "提交成功", Toast.LENGTH_LONG).show();
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(OrderCommentActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(OrderCommentActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(OrderCommentActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(OrderCommentActivity.this, "提交失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(OrderCommentActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
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

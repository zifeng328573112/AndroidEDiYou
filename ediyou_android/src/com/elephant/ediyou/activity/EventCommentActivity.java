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
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * @author Zhoujun
 * @version 创建时间：2013-1-14 上午11:34:25 类说明
 */
public class EventCommentActivity extends Activity implements IBaseActivity, OnClickListener {

	private Button 				btnLeft;
	private Button 				btnRight;
	private TextView 			tvTitle;
	private ProgressDialog 		pd;

	private TextView 			tvCommentPoint;
	private RatingBar 			ratingbar;
	private EditText 			edtComment;

	private int 				type;
	private long 				userId;
	private long 				wasId;// 活动通知对方的id
	private long 				activityId;// 活动的id
	private String 				content;// 通知内容
	private long 				notiId;// 活动通知对方的id

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_comment);
		if (getIntent() != null) {
			content 		= getIntent().getStringExtra("content");
			type 			= getIntent().getIntExtra("type", 0);
			userId 			= getIntent().getLongExtra("userId", 0);
			wasId 			= getIntent().getLongExtra("wasId", 0);
			activityId 		= getIntent().getLongExtra("activityId", 0);
			notiId 			= getIntent().getLongExtra("notiId", 0);
		}
		findView();
		fillData();
		// 将Activity添加到容器
		((CommonApplication) getApplicationContext()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft 		= (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight 		= (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.ic_submit_selector);
		tvTitle 		= (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		tvCommentPoint 	= (TextView) this.findViewById(R.id.tvCommentPoint);
		ratingbar 		= (RatingBar) this.findViewById(R.id.ratingbar);
		edtComment 		= (EditText) this.findViewById(R.id.edtComment);
	}

	@Override
	public void fillData() {
		// 输入字数限制（字符）
		StringUtil.limitEditTextLength(edtComment, 200, this);
		if (type == Constants.NOTI_ACTIVITY_SEND) {
			tvCommentPoint.setText(content + "(如在7日内未打分，系统将自动给予对方3星评价)");
		} else {
			tvCommentPoint.setText(content);
		}
		tvTitle.setText("活动评价");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			startActivity(new Intent(this, MainHomeActivityGroup.class));
			finish();
			break;
		case R.id.btnRight:// 提交
			int level = (int) ratingbar.getRating();
			String commentStr = edtComment.getText().toString();
			if (level == 0) {
				level = 3;
			}
			if (TextUtils.isEmpty(commentStr)) {
				Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
				return;
			}
			if (NetUtil.checkNet(this)) {
				new EventCommentInsertTask(commentStr, level).execute();
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
			}

			break;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			startActivity(new Intent(EventCommentActivity.this, MainHomeActivityGroup.class));
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
	class EventCommentInsertTask extends AsyncTask<Void, Void, JSONObject> {
		private String content;
		private int level;

		public EventCommentInsertTask(String content, int level) {
			super();
			this.content = content;
			this.level = level;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(EventCommentActivity.this);
				pd.setMessage("提交中...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject obj = null;
			try {
				if (type == Constants.NOTI_ACTIVITY_SEND) {
					obj = new BusinessHelper().koalaCommentInEvent(userId, activityId, content, level, wasId, notiId);
				} else {
					obj = new BusinessHelper().rooCommentInEvent(wasId, activityId, content, level, userId, notiId);
				}
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return obj;
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
						Toast.makeText(EventCommentActivity.this, "评价成功", Toast.LENGTH_LONG).show();
						startActivity(new Intent(EventCommentActivity.this, MainHomeActivityGroup.class));
						finish();
					} else {
						Toast.makeText(EventCommentActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(EventCommentActivity.this, "提交失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(EventCommentActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
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

package com.elephant.ediyou.activity;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.NotifyBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 系统消息详情页
 * 
 * @author syghh
 * 
 */
public class MessageSysDetailActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button btnLeft;
	private TextView tvTitle;
	private Button btnRight;

	private TextView tvNoti;
	private Button btnSysComment;
	
	private NotifyBean notifyBean;
	private int type;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_sys_detail);
		if(getIntent() != null){
			notifyBean = (NotifyBean) getIntent().getSerializableExtra("SysNotifyBean");
		}
		if(notifyBean != null){
			type = Integer.parseInt(notifyBean.getType());
		}
		
		findView();
		fillData();
		// 将Activity添加到容器
		((CommonApplication) getApplicationContext()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) findViewById(R.id.btnLeft);
		btnRight = (Button) findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("系统消息");
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnLeft.setOnClickListener(this);
		
		tvNoti = (TextView) this.findViewById(R.id.tvNoti);
		btnSysComment = (Button) this.findViewById(R.id.btnSysComment);
		if(type != 7 && type != 8){
			btnSysComment.setVisibility(View.GONE);
		}else if (type == 7 || type == 8){
			btnSysComment.setOnClickListener(this);
		}
	}

	@Override
	public void fillData() {
		if(notifyBean != null){
			tvNoti.setText(notifyBean.getContent());
			if(NetUtil.checkNet(this) && notifyBean.getStatus() == 0){
				new UpdateNotificationStatusTask().execute();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnSysComment://跳转到相对应的评论界面
			String content = notifyBean.getContent();
			long userId = notifyBean.getUserId();
			long wasId = notifyBean.getWasId();
			long activityId = notifyBean.getActivityId();
			long notiId = notifyBean.getId();
			Intent intent = new Intent(this, EventCommentActivity.class);
			intent.putExtra("content", content);
			intent.putExtra("type", type);
			intent.putExtra("userId", userId);
			intent.putExtra("wasId", wasId);
			intent.putExtra("activityId", activityId);
			intent.putExtra("notiId", notiId);
			startActivity(intent);
			break;
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
	 * 更新系统通知已读
	 * @author Zhoujun
	 *
	 */
	private class UpdateNotificationStatusTask extends AsyncTask<Void, Void, JSONObject>{

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().updateNotificationStatus(notifyBean.getId());
			} catch (SystemException e) {
				return null;
			}
		}
		
	}
}

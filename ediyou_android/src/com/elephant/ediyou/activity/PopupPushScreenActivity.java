package com.elephant.ediyou.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.util.SharedPrefUtil;

/**
 * 桌面弹窗提示通知的 DialogActivity
 * 
 * @author syghh
 * 
 */
public class PopupPushScreenActivity extends Activity implements IBaseActivity, OnClickListener {

	private TextView tvDialogTitle;
	private TextView tvDialogMsg;
	private Button btnDialogLeft;
	private Button btnDialogRight;

	private String msgStr = null;
	private int type = -1;
	private String msgTitle = null;
	private int isKangaroo = -1;
	
	private long userId;
	private long wasId;//活动通知对方的id
	private long activityId;//活动的id 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_common_layout);
		isKangaroo = SharedPrefUtil.getUserBean(this).getIsKangaroo();
		if (getIntent() != null) {
			msgStr = getIntent().getStringExtra("content");
			type = getIntent().getIntExtra("type", -1);
			msgTitle = getIntent().getStringExtra("msgTitle");
			
			userId = getIntent().getLongExtra("userId", 0);
			wasId = getIntent().getLongExtra("wasId", 0);
			activityId = getIntent().getLongExtra("activityId", 0);
		}
		// msgStr = "您有新消息哦！";//测试
		// type = "2";//测试

		findView();
		fillData();
		// 将Activity添加到容器
		((CommonApplication) getApplicationContext()).addActivity(this);
	}

	@Override
	public void findView() {
		tvDialogTitle = (TextView) this.findViewById(R.id.tvDialogTitle);
		tvDialogTitle.setText("通知");
		tvDialogMsg = (TextView) this.findViewById(R.id.tvDialogMsg);
		btnDialogLeft = (Button) this.findViewById(R.id.btnDialogLeft);
		btnDialogRight = (Button) this.findViewById(R.id.btnDialogRight);
		btnDialogLeft.setOnClickListener(this);
		btnDialogRight.setOnClickListener(this);

	}

	@Override
	public void fillData() {
		tvDialogMsg.setText(msgStr);
		btnDialogLeft.setText("进入查看");
		btnDialogRight.setText("忽略此条");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnDialogLeft:
			if (type == Constants.NOTI_LETTER) {
				startActivity(new Intent(PopupPushScreenActivity.this, MessageCenterActivity.class));
			} else if (type == Constants.NOTI_ORDER) {//订单那状态改变的通知
				if(isKangaroo == 0){
					startActivity(new Intent(PopupPushScreenActivity.this, MyOrderActivity.class));
				} else if(isKangaroo == 1) {
					startActivity(new Intent(PopupPushScreenActivity.this, MyReceivedOrderActivity.class));
				}
			} else if (type == Constants.NOTI_RESERVATION) {//预约通知
				startActivity(new Intent(PopupPushScreenActivity.this, MyReceivedOrderActivity.class));
			} else if (type == Constants.NOTI_SYSTEM) {
				startActivity(new Intent(PopupPushScreenActivity.this, MessageCenterActivity.class));
			}else if(Constants.NOTI_ACTIVITY == type){//跳转到活动评价界面
//				startActivity(new Intent(PopupPushScreenActivity.this, EventCommentActivity.class));
			}else if(Constants.NOTI_ACTIVITY_SEND == type){
				Intent sendIntent = new Intent(this, EventCommentActivity.class);
				sendIntent.putExtra("content", msgStr);
				sendIntent.putExtra("type", type);
				sendIntent.putExtra("userId", userId);
				sendIntent.putExtra("wasId", wasId);
				sendIntent.putExtra("activityId", activityId);
				startActivity(sendIntent);
			}else if(Constants.NOTI_ACTIVITY_RECEIVER == type){
				Intent receiverIntent = new Intent(this, EventCommentActivity.class);
				receiverIntent.putExtra("content", msgStr);
				receiverIntent.putExtra("type", type);
				receiverIntent.putExtra("userId", userId);
				receiverIntent.putExtra("wasId", wasId);
				receiverIntent.putExtra("activityId", activityId);
				startActivity(receiverIntent);
			}
			finish();
			break;
		case R.id.btnDialogRight:
			finish();
			break;
		default:
			break;
		}

	}

}

package com.elephant.ediyou.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.service.PullService;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 通知和定位设置；
 * 
 * @author Zhoujun
 * 
 */
public class NotificationAndLocationSettingActivity extends Activity implements IBaseActivity, OnClickListener {

	// 标题；
	private Button btnLeft, btnRight;
	private ImageView ivTitle;
	private TextView tvTitle;
	//dialog
	private TextView tvDialogMsg;
	private Button btnDialog;

	private ToggleButton tgNotificationSetting, tgLocationSetting;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notification_and_location_setting);
		findView();
		fillData();
		((CommonApplication) getApplicationContext()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		ivTitle = (ImageView) findViewById(R.id.ivTitle);
		ivTitle.setVisibility(View.GONE);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("通知和定位服务");
		btnLeft.setOnClickListener(this);

		tgNotificationSetting = (ToggleButton) findViewById(R.id.tgNotificationSetting);
		tgLocationSetting = (ToggleButton) findViewById(R.id.tgLocationSetting);
	}

	@Override
	public void fillData() {
		boolean notificationSetting = SharedPrefUtil.getNotificationSetting(this);
		tgNotificationSetting.setChecked(notificationSetting);
		tgNotificationSetting.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(!isChecked){
					//弹出提示框，提醒用户；
					LayoutInflater inflater = getLayoutInflater();
					View view = inflater.inflate(R.layout.dialog_alert, null);
					tvDialogMsg = (TextView) view.findViewById(R.id.tvDialogMsg);
					btnDialog = (Button) view.findViewById(R.id.btnDialog);
					tvDialogMsg.setText("一旦关闭消息通知，您将无法及时收到订单消息和消息中心通知!");
					final Dialog dialog = new Dialog(NotificationAndLocationSettingActivity.this, R.style.dialog);
					dialog.setContentView(view);
					dialog.show();
					WindowManager windowManager = getWindowManager();
					Display display = windowManager.getDefaultDisplay();
					WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
					lp.width = (int)(display.getWidth()-60); //设置宽度
					dialog.getWindow().setAttributes(lp);
					
					btnDialog.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
				}
				tgNotificationSetting.setChecked(isChecked);
				SharedPrefUtil.setNotificationSetting(NotificationAndLocationSettingActivity.this, isChecked);
				if(isChecked){
					Intent serviceIntent = new Intent (NotificationAndLocationSettingActivity.this, PullService.class);
					startService(serviceIntent);
				}
			}
		});

		boolean locationSetting = SharedPrefUtil.getLocationSetting(this);
		tgLocationSetting.setChecked(locationSetting);
		tgLocationSetting.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				tgLocationSetting.setChecked(isChecked);
				SharedPrefUtil.setLocationSetting(NotificationAndLocationSettingActivity.this, isChecked);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			this.finish();
			overridePendingTransition(R.anim.push_right_in_noalp_back, R.anim.push_left_out_noalp_back);
			break;

		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
			overridePendingTransition(R.anim.push_right_in_noalp_back, R.anim.push_left_out_noalp_back);
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

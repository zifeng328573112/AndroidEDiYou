package com.elephant.ediyou.activity;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutUsActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button 			btnLeft;
	private TextView 		tvTitle;
	private Button 			btnRight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aboutus);
		findView();
		fillData();
		// 将Activity添加到容器
		((CommonApplication) getApplicationContext()).addActivity(this);

	}

	@Override
	public void findView() {
		btnLeft 	= (Button) findViewById(R.id.btnLeft);
		btnRight 	= (Button) findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle 	= (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

	}

	@Override
	public void fillData() {
		tvTitle.setText("关于我们");
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnLeft.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			overridePendingTransition(R.anim.push_right_in_noalp_back, R.anim.push_left_out_noalp_back);
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

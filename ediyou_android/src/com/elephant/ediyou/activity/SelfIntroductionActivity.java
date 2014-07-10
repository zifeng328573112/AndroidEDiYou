package com.elephant.ediyou.activity;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 个人介绍（袋鼠考拉通用）
 * 
 * @author ISP
 * 
 */
public class SelfIntroductionActivity extends Activity implements IBaseActivity, OnClickListener {

	// title
	private Button btnLeft, btnRight;
	private TextView tvTitle;

	private EditText etSelfIntroduction;
	private ProgressDialog pd;
	private InputMethodManager inputManager;

	private String introStrOld;
	private String introStrNew;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.self_introduction);
		findView();
		fillData();
		// 将Activity添加到容器
		((CommonApplication) getApplicationContext()).addActivity(this);
		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			introStrNew = etSelfIntroduction.getText().toString();
			if (!introStrNew.equals(introStrOld)) {
				UpdateOrNot();
			} else {
				this.finish();
			}
			break;
		case R.id.btnRight:
			if (NetUtil.checkNet(this)) {
				inputManager.hideSoftInputFromWindow(btnRight.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);// 隐藏键盘
				new SelfIntroductionTask("save").execute();
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.bg_btn_selector);
		btnRight.setText("保存");
		btnRight.setGravity(Gravity.CENTER);
		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

		etSelfIntroduction = (EditText) findViewById(R.id.etSelfIntroduction);
	}

	@Override
	public void fillData() {
		tvTitle.setText("个人介绍");
		if (NetUtil.checkNet(this)) {
			new SelfIntroductionTask("get").execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
		}
		//限制字数
		StringUtil.limitEditTextLength(etSelfIntroduction, 300, this);
	}

	/**
	 * 返回时，若数据有修改，提示的Dialog,自定义。
	 */
	public void UpdateOrNot() {
		final AlertDialog dialogExit = new AlertDialog.Builder(SelfIntroductionActivity.this).create();
		dialogExit.show();
		Window dialogWindow = dialogExit.getWindow();
		dialogWindow.setContentView(R.layout.dialog_common_layout);
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialogExit.getWindow().getAttributes();
		lp.width = (int) (display.getWidth() - 60); // 设置宽度
		dialogExit.getWindow().setAttributes(lp);
		TextView tvDialogMsg = (TextView) dialogWindow.findViewById(R.id.tvDialogMsg);
		tvDialogMsg.setText("您对部分的个人介绍进行了修改，是否需要提交？");
		Button btnDialogLeft = (Button) dialogWindow.findViewById(R.id.btnDialogLeft);
		btnDialogLeft.setText("取消提交");
		btnDialogLeft.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();
				SelfIntroductionActivity.this.finish();
			}
		});
		// 关闭对话框架
		Button btnDialogRight = (Button) dialogWindow.findViewById(R.id.btnDialogRight);
		btnDialogRight.setText("确认提交");
		btnDialogRight.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();
				btnRight.performClick();
				
			}
		});
	}

	/**
	 * 
	 * @author Zhoujun type 为”get“表示获取个人介绍，”save“表示修改保存个人介绍；
	 */
	private class SelfIntroductionTask extends AsyncTask<Void, Void, JSONObject> {

		private String type;

		public SelfIntroductionTask(String type) {
			super();
			this.type = type;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(SelfIntroductionActivity.this);
			}
			if (type.equals("get")) {
				pd.setMessage("正在获取...");
			} else if (type.equals("save")) {
				pd.setMessage("正在保存..");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject json = null;
			long userId = SharedPrefUtil.getUserBean(SelfIntroductionActivity.this).getUserId();
			String accessToken = SharedPrefUtil.getUserBean(SelfIntroductionActivity.this).getAccessToken();
			try {
				if (type.equals("get")) {
					json = new BusinessHelper().getIntroduction(userId, accessToken);
				} else if (type.equals("save")) {
					introStrOld = etSelfIntroduction.getText().toString();
					json = new BusinessHelper().updateIntro(userId, introStrOld, accessToken);
				}
			} catch (Exception e) {
			}
			return json;
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
						if (type.equals("get")) {
							introStrOld = result.getString("intro");
							etSelfIntroduction.setText(introStrOld);
							etSelfIntroduction.setSelection(introStrOld.length());
						} else if (type.equals("save")) {
							SelfIntroductionActivity.this.finish();
							Toast.makeText(SelfIntroductionActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
						}
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(SelfIntroductionActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(SelfIntroductionActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(SelfIntroductionActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					Toast.makeText(SelfIntroductionActivity.this, "数据错误", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			} else {
				Toast.makeText(SelfIntroductionActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			btnLeft.performClick();
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

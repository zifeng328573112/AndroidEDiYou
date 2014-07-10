package com.elephant.ediyou.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.LoginActivity.LoginTask;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 注册协议页面（考拉）
 * 
 * @author syghh
 * 
 */
public class RegisterProtocolActivity extends Activity implements IBaseActivity,
		OnClickListener {
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	private ProgressDialog pd;
	private int isKangaroo = 0; // 0-考拉， 1-袋鼠

	private TextView tvRegisterProtocol;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_protocol);
		findView();
		fillData();
		if(NetUtil.checkNet(this)){
			new koalaProtocolTask().execute();	
		}else{
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
		}
		((CommonApplication) getApplication()).addActivity(this);

	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

		tvRegisterProtocol = (TextView) this.findViewById(R.id.tvRegisterProtocol);
		btnLeft.setOnClickListener(this);
	}

	@Override
	public void fillData() {
		isKangaroo = getIntent().getIntExtra("isKangaroo", 0);
		if(0 == isKangaroo) {
			tvTitle.setText("考拉注册协议");
		} else {
			tvTitle.setText("E地游袋鼠协议");
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		}
	}

	/**
	 * 考拉注册协议获取
	 * @author syghh
	 *
	 */
	class koalaProtocolTask extends AsyncTask<Void, Void, JSONObject> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RegisterProtocolActivity.this);
				pd.setMessage("正在获取...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				if(0 == isKangaroo) {
					return new BusinessHelper().koalaProtocol();
				} else {
					return new BusinessHelper().rooProtocol();
				}
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
						tvRegisterProtocol.setText(Html.fromHtml(result.getString("data")));
					} else {
						Toast.makeText(RegisterProtocolActivity.this,
								result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(RegisterProtocolActivity.this, "获取失败",
							Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(RegisterProtocolActivity.this, "服务器请求失败",
						Toast.LENGTH_LONG).show();
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

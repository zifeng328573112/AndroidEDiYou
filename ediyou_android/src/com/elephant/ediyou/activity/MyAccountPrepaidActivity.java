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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.umeng.analytics.MobclickAgent;

public class MyAccountPrepaidActivity extends Activity implements IBaseActivity, OnClickListener,
	android.widget.CompoundButton.OnCheckedChangeListener, 
	android.widget.RadioGroup.OnCheckedChangeListener {

	private CommonApplication app;
	private UserBean userBean;
	private long uId;
	
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	
	private TextView tvRooAccountBalance;
	private RadioGroup rgPrepaidSel, rgBankSelLine1, rgBankSelLine2, rgBankSelLine3;
	private RadioButton rbPrepaidUseBank, rbPrepaidUseVoutch;
	private RadioButton rbBankSelZhiFuBao;
	private EditText etAmountConfirm, etInputVoutchCode;
	private Button btnPrepaidConfirm;
	private LinearLayout llPrepaidUseBank;
	
	private int rbBankSelLastId = 0;
	private RadioGroup rgBankSelCurr;
	private int isKangaroo = 0; // 0-考拉， 1-袋鼠
	
	private boolean LIST_RECORD_TASK_RUNING = false;// 加载记录的任务是否执行中，如果执行中（true），就不再创建新的任务，
	private VoutchPrepaidTask voutchPrepaidTask;
	private ProgressDialog pd;
	
	private long voutchCode = 0;
	private String prepaidResult;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_account_prepaid);
		app = (CommonApplication) getApplication();
		userBean = SharedPrefUtil.getUserBean(this);
		uId = userBean.getUserId();
		if (getIntent() != null) {
			isKangaroo = getIntent().getIntExtra("isKangaroo", 0);
		}
		findView();
		fillData();
		
		app.addActivity(this);
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
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnPrepaidConfirm:
			if(rgPrepaidSel.getCheckedRadioButtonId() == R.id.rbPrepaidUseBank) {
				payByAlipay(Integer.parseInt(etAmountConfirm.getText().toString()));
			} else if(rgPrepaidSel.getCheckedRadioButtonId() == R.id.rbPrepaidUseVoutch) {
				new VoutchPrepaidTask().execute();
			}
			break;
		case R.id.btnLeft:
			finish();
			break;
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(true == isChecked) {
			switch(buttonView.getId()) {
			case R.id.rbPrepaidUseBank:
				etAmountConfirm.setEnabled(true);
				etInputVoutchCode.setEnabled(false);
				break;
			case R.id.rbPrepaidUseVoutch:
				etAmountConfirm.setEnabled(false);
				etInputVoutchCode.setEnabled(true);
				break;
			}
		}
	}
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch(group.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.rgPrepaidSel:
			onRgPrepaidSelCahngeChecked(checkedId);
			break;
		case R.id.rgBankSelLine1:
		case R.id.rgBankSelLine2:
		case R.id.rgBankSelLine3:
			if(0 != rbBankSelLastId) {
				RadioButton rb = (RadioButton)findViewById(rbBankSelLastId);
				rb.setChecked(false);
			}
			rbBankSelLastId = checkedId;
			rgBankSelCurr = group;
			break;
		}
	}
	
	private void onRgPrepaidSelCahngeChecked(int checkedId) {
		
	}
	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		
		tvRooAccountBalance = (TextView)this.findViewById(R.id.tvAccountBalance);
		rgPrepaidSel = (RadioGroup)this.findViewById(R.id.rgPrepaidSel);
		rgBankSelLine1 = (RadioGroup)this.findViewById(R.id.rgBankSelLine1);
		rgBankSelLine2 = (RadioGroup)this.findViewById(R.id.rgBankSelLine2);
		rgBankSelLine3 = (RadioGroup)this.findViewById(R.id.rgBankSelLine3);
		rbPrepaidUseBank = (RadioButton)this.findViewById(R.id.rbPrepaidUseBank);
		rbPrepaidUseVoutch = (RadioButton)this.findViewById(R.id.rbPrepaidUseVoutch);
		rbBankSelZhiFuBao = (RadioButton)this.findViewById(R.id.rbBankSelZhiFuBao);
		etAmountConfirm = (EditText)this.findViewById(R.id.etAmountConfirm);
		etInputVoutchCode = (EditText)this.findViewById(R.id.etInputVoutchCode);
		btnPrepaidConfirm = (Button)this.findViewById(R.id.btnPrepaidConfirm);
		llPrepaidUseBank = (LinearLayout)this.findViewById(R.id.llPrepaidUseBank);
		
		if(0 == isKangaroo) {
			rbPrepaidUseBank.setVisibility(View.GONE);
			llPrepaidUseBank.setVisibility(View.GONE);
			rbPrepaidUseVoutch.setChecked(true);
		}
	}
	
	@Override
	public void fillData() {
		tvTitle.setText("扣款帐号充值");
		
		rbBankSelLastId = R.id.rbBankSelZhiFuBao;
		btnLeft.setOnClickListener(this);
		btnPrepaidConfirm.setOnClickListener(this);
		rgBankSelLine1.setOnCheckedChangeListener(this);
		rgBankSelLine2.setOnCheckedChangeListener(this);
		rgBankSelLine3.setOnCheckedChangeListener(this);
		rbPrepaidUseBank.setOnCheckedChangeListener(this);
		rbPrepaidUseVoutch.setOnCheckedChangeListener(this);
		
		onRgPrepaidSelCahngeChecked(rgPrepaidSel.getCheckedRadioButtonId());
	}
	
	/**
	 * 代金券充值
	 * 
	 * @author syghh
	 * 
	 */
	class VoutchPrepaidTask extends AsyncTask<Void, Void, JSONObject> {
		public VoutchPrepaidTask() {
			LIST_RECORD_TASK_RUNING = true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(MyAccountPrepaidActivity.this);
				pd.setMessage("正在处理中，请稍后...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				voutchCode = Integer.parseInt(etInputVoutchCode.getText().toString());
				return new BusinessHelper().vouchersPrepaid(userBean.getAccessToken(), 1/*uId*/, voutchCode);
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
						prepaidResult = result.getString("data");
						showDialog(0);
					} else if(status == Constants.TOKEN_FAILED) {
						Toast.makeText(MyAccountPrepaidActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(MyAccountPrepaidActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(MyAccountPrepaidActivity.this, result.getString("error"),
								Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(MyAccountPrepaidActivity.this, "加载失败", Toast.LENGTH_LONG).show();
				}/* catch (SystemException e) {
					e.printStackTrace();
				}*/
			} else {
				Toast.makeText(MyAccountPrepaidActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
			// 加载记录的任务结束
			LIST_RECORD_TASK_RUNING = false;
			
			cancel(true);
		}

	}
	
	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = new AlertDialog.Builder(MyAccountPrepaidActivity.this).setTitle("充值结果").setMessage(prepaidResult) // 设置内容 
							.setNegativeButton("取消",new DialogInterface.OnClickListener() 
							{ 
								public void onClick(DialogInterface dialog, int whichButton) 
								{ 
									// 点击"取消"按钮之后退出程序 
									//finish();
								} 
							}).create();// 创建 
		return dialog; 
	}
	
	private void payByAlipay(int amount) {
		
	}
	
	/**
	 * the OnCancelListener for lephone platform. lephone系统使用到的取消dialog监听
	 */
	public static class AlixOnCancelListener implements
			DialogInterface.OnCancelListener {
		Activity mcontext;

		public AlixOnCancelListener(Activity context) {
			mcontext = context;
		}

		public void onCancel(DialogInterface dialog) {
			mcontext.onKeyDown(KeyEvent.KEYCODE_BACK, null);
		}
	}
}

package com.elephant.ediyou.activity;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 袋鼠服务信息（编辑）
 * 
 * @author SongYuan
 * 
 */
public class RooServeInfoActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	private ProgressDialog pd;
	private TextView tvPrice;
	private Button btnChangePrice;
	private EditText edtServiceContent;
	private EditText edtLanguage;

	private double commission = -1;// 当前等级的最高佣金
	private String level;// 袋鼠当前等级
	private String accessToken;
	private long userId;
	private long rooId;

	private String serviceStrOld;
	private String languageStrOld;
	private double priceDOld;

	private String serviceStrNew;
	private String languageStrNew;
	private double priceDNew;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roo_serve_info_edit);
		accessToken = SharedPrefUtil.getUserBean(RooServeInfoActivity.this).getAccessToken();
		userId = SharedPrefUtil.getUserBean(RooServeInfoActivity.this).getUserId();
		rooId = SharedPrefUtil.getRooId(RooServeInfoActivity.this);
		findView();
		if (NetUtil.checkNet(this)) {
			new GetRooServiceInfoNewTask(userId, accessToken).execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
		}
		fillData();
		// 将Activity添加到容器
		((CommonApplication) getApplicationContext()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.bg_btn_selector);
		btnRight.setText("保存");
		btnRight.setGravity(Gravity.CENTER);
		btnRight.setTextColor(Color.rgb(157, 208, 99));
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("服务信息");

		tvPrice = (TextView) this.findViewById(R.id.tvPrice);
		btnChangePrice = (Button) this.findViewById(R.id.btnChangePrice);
		edtServiceContent = (EditText) this.findViewById(R.id.edtServiceContent);
		edtLanguage = (EditText) this.findViewById(R.id.edtLanguage);
		btnLeft.setOnClickListener(this);
		btnChangePrice.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		//输入字数限制（字符）
		StringUtil.limitEditTextLength(edtServiceContent, 32, this);
		StringUtil.limitEditTextLength(edtLanguage, 32, this);
	}

	@Override
	public void fillData() {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			serviceStrNew = edtServiceContent.getText().toString();
			languageStrNew = edtLanguage.getText().toString();
			priceDNew = Double.parseDouble(tvPrice.getText().toString().replace("元/每天", ""));
			if (priceDNew != priceDOld || !serviceStrNew.equals(serviceStrOld) || !languageStrNew.equals(languageStrOld)) {
				UpdateOrNot();
			} else {
				finish();
			}
			break;
		case R.id.btnRight:// 提交
			serviceStrOld = edtServiceContent.getText().toString();
			languageStrOld = edtLanguage.getText().toString();
			priceDOld = Double.parseDouble(tvPrice.getText().toString().replace("元/每天", ""));
			if (NetUtil.checkNet(this)) {
				new UpdateServeInfoTask(serviceStrOld, languageStrOld, priceDOld).execute();
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btnChangePrice:// 更改服务价格
			if (commission == 0) {
				Toast.makeText(RooServeInfoActivity.this, "联网异常，无法获取最大佣金", Toast.LENGTH_LONG).show();
			} else {
				LayoutInflater inflater = RooServeInfoActivity.this.getLayoutInflater();
				View view = inflater.inflate(R.layout.dialog_modify_service_price, null);
				TextView tvDialogMsg = (TextView) view.findViewById(R.id.tvDialogMsg);
				final EditText etPrice = (EditText) view.findViewById(R.id.etPrice);
				Button btnDialogLeft = (Button) view.findViewById(R.id.btnDialogLeft);
				Button btnDialogRight = (Button) view.findViewById(R.id.btnDialogRight);

				CharSequence msg = Html.fromHtml("您当前的地陪等级是" + "<a><font color=\"#E70E1F\">" + level + "</a>" + "级" + "<br>" + "你可以设置的每日最大价格是"
						+ "<a><font color=\"#E70E1F\">" + commission + "</a>" + "元" + "<br>" + "请输入您要设置的价格");
				tvDialogMsg.setText(msg);

				final Dialog dialog = new Dialog(RooServeInfoActivity.this, R.style.dialog);
				dialog.setContentView(view);
				dialog.show();
				WindowManager windowManager = getWindowManager();
				Display display = windowManager.getDefaultDisplay();
				WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
				lp.width = (int) (display.getWidth() - 60); // 设置宽度
				dialog.getWindow().setAttributes(lp);

				btnDialogLeft.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						final String price = etPrice.getText().toString();
						if (TextUtils.isEmpty(price)) {
							Toast.makeText(RooServeInfoActivity.this, "请输入变更的价格", Toast.LENGTH_SHORT).show();
							return;
						} else if (!StringUtil.checkNum(price)) {
							Toast.makeText(RooServeInfoActivity.this, "请输入有效数字", Toast.LENGTH_SHORT).show();
							return;
						} else if (Double.parseDouble(price) > commission) {
							Toast.makeText(RooServeInfoActivity.this, "输入的价格不要超过每日最大价格", Toast.LENGTH_SHORT).show();
							return;
						}
						tvPrice.setText(price + "元/每天");
						dialog.dismiss();
					}
				});
				btnDialogRight.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();

					}
				});
			}
			break;
		}
	}

	/**
	 * 返回时，若数据有修改，提示的Dialog,自定义。
	 */
	public void UpdateOrNot() {
		final AlertDialog dialogExit = new AlertDialog.Builder(RooServeInfoActivity.this).create();
		dialogExit.show();
		Window dialogWindow = dialogExit.getWindow();
		dialogWindow.setContentView(R.layout.dialog_common_layout);
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialogExit.getWindow().getAttributes();
		lp.width = (int) (display.getWidth() - 60); // 设置宽度
		dialogExit.getWindow().setAttributes(lp);
		TextView tvDialogMsg = (TextView) dialogWindow.findViewById(R.id.tvDialogMsg);
		tvDialogMsg.setText("您对部分的服务信息进行了修改，是否需要提交？");
		Button btnDialogLeft = (Button) dialogWindow.findViewById(R.id.btnDialogLeft);
		btnDialogLeft.setText("取消提交");
		btnDialogLeft.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogExit.dismiss();
				RooServeInfoActivity.this.finish();
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
	 * 填充服务信息数据
	 * 
	 * @param price
	 * @param service
	 * @param language
	 */
	public void fillServiceData(double price, String service, String language) {
		tvPrice.setText(String.valueOf(price) + "元/每天");
		edtServiceContent.setText(service);
		edtLanguage.setText(language);
	}

	/**
	 * 获取当前用户的服务信息（新的接口）
	 * 
	 * @author syghh
	 * 
	 */
	class GetRooServiceInfoNewTask extends AsyncTask<Void, Void, JSONObject> {
		private long userId;
		private String access_token;

		public GetRooServiceInfoNewTask(long userId, String access_token) {
			super();
			this.userId = userId;
			this.access_token = access_token;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject getJSON = null;
			try {
				getJSON = new BusinessHelper().getRooServiceInfoNew(userId, access_token);
			} catch (Exception e) {
			}

			return getJSON;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					if (result.getInt("status") == Constants.SUCCESS) {
						JSONObject data = result.getJSONObject("data");
						int levelInt = data.getInt("level");
						priceDOld = data.getDouble("price");
						serviceStrOld = data.getString("service");
						languageStrOld = data.getString("language");
						fillServiceData(priceDOld, serviceStrOld, languageStrOld);
						level = String.valueOf(levelInt);
						if (NetUtil.checkNet(RooServeInfoActivity.this)) {
							new GetMaxCommissionTask(level, accessToken).execute();
						} else {
							Toast.makeText(RooServeInfoActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
						}
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(RooServeInfoActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(RooServeInfoActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(RooServeInfoActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			} else {
				Toast.makeText(RooServeInfoActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 根据等级获取最高佣金
	 * 
	 * @author Zhoujun
	 * 
	 */
	class GetMaxCommissionTask extends AsyncTask<Void, Void, JSONObject> {
		private String level;
		private String token;

		public GetMaxCommissionTask(String level, String token) {
			super();
			this.level = level;
			this.token = token;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject commission = null;
			try {
				commission = new BusinessHelper().getMaxCommission(level, token);
			} catch (SystemException e) {
			}
			return commission;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					if (result.getInt("status") == Constants.SUCCESS) {
						commission = result.getDouble("data");
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(RooServeInfoActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(RooServeInfoActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(RooServeInfoActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
				}

			} else {
				commission = -1;
				Toast.makeText(RooServeInfoActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 修改服务信息Task
	 * 
	 * @author syghh
	 * 
	 */
	class UpdateServeInfoTask extends AsyncTask<Void, Void, JSONObject> {
		private String serviceStr;
		private String languageStr;
		private double priceD;

		public UpdateServeInfoTask(String serviceStr, String languageStr, double priceD) {
			super();
			this.serviceStr = serviceStr;
			this.languageStr = languageStr;
			this.priceD = priceD;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject update = null;
			try {
				update = new BusinessHelper().updateRooService(userId, accessToken, serviceStr, languageStr, priceD);
			} catch (Exception e) {
			}

			return update;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RooServeInfoActivity.this);
				pd.setMessage("提交中...");
			}
			pd.show();
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (pd != null)
				pd.dismiss();
			if (result != null) {
				try {
					if (result.getInt("status") == Constants.SUCCESS) {
						RooServeInfoActivity.this.finish();
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(RooServeInfoActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(RooServeInfoActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(RooServeInfoActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					Toast.makeText(RooServeInfoActivity.this, "数据错误", Toast.LENGTH_SHORT).show();

				}
			} else {
				Toast.makeText(RooServeInfoActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
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

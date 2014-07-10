package com.elephant.ediyou.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.CitysBean;
import com.elephant.ediyou.bean.HobbyBean;
import com.elephant.ediyou.bean.ProvincesBean;
import com.elephant.ediyou.db.DataBaseAdapter;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.service.PullService;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 启动页
 * 
 * @author syghh
 * 
 */
public class LogoActivity extends Activity implements IBaseActivity {
	private int screenWidth;
	private static final int widthXOne = 720;// 720宽的屏幕
	private static final int widthXTwo = 800;
	/**
	 * 数据库操作对象
	 */
	private DataBaseAdapter dba;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo);
		dba = ((CommonApplication) getApplicationContext()).getDbAdapter();
		screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();// 获取图片宽度
		RelativeLayout rlLogo = (RelativeLayout) this.findViewById(R.id.rlLogo);
		// 分辨率为1280x800 & 1280x720手机的logo图片资源设置
		if (screenWidth == widthXOne) {
			rlLogo.setBackgroundResource(R.drawable.logo);
		} else if (screenWidth == widthXTwo) {
			rlLogo.setBackgroundResource(R.drawable.logo_x);
		}
		// 将热门城市，省、市数据插入数据库
		if (NetUtil.checkNet(this)) {
			new AddressShowTask().execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
		}

		// 获取所有爱好标签
		if (NetUtil.checkNet(LogoActivity.this)) {
			new GetAllHobbyListTask().execute();
		} else {
			Toast.makeText(LogoActivity.this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
		}

		AlphaAnimation aa = new AlphaAnimation(1.0f, 1.0f);
		aa.setDuration(3000);
		rlLogo.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation arg0) {
				//判断是否是第一次进入应用
				if (SharedPrefUtil.isFistLogin(LogoActivity.this)) {
					Intent intent = new Intent();
					intent.setClass(LogoActivity.this, FirstStartActivity.class);
					LogoActivity.this.startActivity(intent);
					// 显示帮助画面，当点击按钮进入menu
					SharedPrefUtil.setFistLogined(LogoActivity.this);
				} else{
					if (SharedPrefUtil.checkToken(LogoActivity.this) && (SharedPrefUtil.getUserBean(LogoActivity.this).getIsKangaroo() == 1)) {
						startActivity(new Intent(LogoActivity.this, RooSelfCenterActivity.class));
						LogoActivity.this.finish();
					} else {
						startActivity(new Intent(LogoActivity.this, MainHomeActivityGroup.class));
						overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
						LogoActivity.this.finish();
					}
				}
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}
		});
		// 启动push服务
		if (SharedPrefUtil.getNotificationSetting(this)) {
			Intent it = new Intent(this, PullService.class);
			startService(it);
		}
		// 将Activity添加到容器
		((CommonApplication) getApplicationContext()).addActivity(this);
	}

	@Override
	public void findView() {
		// TODO Auto-generated method stub
	}

	@Override
	public void fillData() {
		// TODO Auto-generated method stub
	}
	
	/**
	 * 获取热门城市，其他省、市
	 * 
	 * @author syghh
	 * 
	 */
	class AddressShowTask extends AsyncTask<Void, Void, JSONObject> {
		
		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().addressShow();
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						JSONObject rooListJson = result.getJSONObject("data");

						List<CitysBean> hotCitysList = new ArrayList<CitysBean>();
						hotCitysList = CitysBean.constractList(rooListJson.getJSONArray("hotcitys"));
						/**
						 * 向数据库批量插入热门城市信息
						 */
						dba.bantchHotCitys(hotCitysList);

						List<ProvincesBean> provincesList = new ArrayList<ProvincesBean>();
						provincesList = ProvincesBean.constractList(rooListJson.getJSONArray("provinces"));
						/**
						 * 向数据库批量插入省信息
						 */
						dba.bantchProvinces(provincesList);

						List<CitysBean> citysList = new ArrayList<CitysBean>();
						citysList = CitysBean.constractList(rooListJson.getJSONArray("citys"));
						/**
						 * 向数据库批量插入城市信息
						 */
						dba.bantchCitys(citysList);

						// Toast.makeText(LogoActivity.this, "数据库加载成功",
						// Toast.LENGTH_LONG)
						// .show();
					} else {
						Toast.makeText(LogoActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(LogoActivity.this, "数据库加载失败", Toast.LENGTH_LONG).show();
				} catch (SystemException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(LogoActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 获取所有的爱好标签
	 * 
	 * @author syghh
	 * 
	 */
	class GetAllHobbyListTask extends AsyncTask<Void, Void, JSONObject> {
		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject obj = null;
			try {
				obj = new BusinessHelper().getAllHobbyList();
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return obj;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						JSONObject data = result.getJSONObject("data");
						List<HobbyBean> allHobbyBeansList = new ArrayList<HobbyBean>();
						allHobbyBeansList = HobbyBean.constractList(data.getJSONArray("hobbyList"));
						if (allHobbyBeansList != null && allHobbyBeansList.size() > 0) {
							// 将所有爱好数据插入库
							dba.bantchHobbys(allHobbyBeansList);
						}
					} else {
						Toast.makeText(LogoActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(LogoActivity.this, "爱好数据错误", Toast.LENGTH_LONG).show();
				} catch (SystemException e) {
					Toast.makeText(LogoActivity.this, "爱好数据错误", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(LogoActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
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
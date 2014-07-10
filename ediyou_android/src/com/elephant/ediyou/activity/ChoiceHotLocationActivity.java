package com.elephant.ediyou.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKGeocoderAddressComponent;
import com.baidu.mapapi.MKLocationManager;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.CitysBean;
import com.elephant.ediyou.bean.ProvincesBean;
import com.elephant.ediyou.db.DataBaseAdapter;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.ChoiceLocation;
import com.elephant.ediyou.util.DateUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.view.ListViewInScrollView;
import com.elephant.ediyou.view.MyRadioButtonInListView;
import com.umeng.analytics.MobclickAgent;
/**
 * 选择城市的页面
 * @author syghh
 *
 */
public class ChoiceHotLocationActivity extends MapActivity implements IBaseActivity, OnClickListener, LocationListener {
	private Button 					btnLeft;
	private Button 					btnRight;
	private TextView 				tvTitle;

	private TextView 				tvGPSLocation;
	private ImageView 				ivSelectGPSLoction;
	private ListViewInScrollView 	lvHotLocation;

	private RelativeLayout 			rlSelectGPSLoction;// 选择gps定位的地址

	private RelativeLayout 			rlChoiceOtherLocation;
	private TextView 				tvOtherLocation;

	private boolean 				isGPSLocation = false;// 是否有选择gps定位
	private String 					hotCityhas;// 有热门城市已选择的城市名；无则为空
	private String 					otherCityhas;// 有其他城市已选择的城市名；无则为空

	private String 					provinceName;
	private String 					location;

	private ProgressDialog 			pd;
	/**
	 * 数据库操作对象
	 */
	private DataBaseAdapter 		dba;

	// gps当前城市
	private BMapManager	 			mMapManager = null;
	private MKLocationManager 		mLocationManager = null;
	private MKSearch 				mMKSearch = null;
	private String 					city;// gps获取的城市名

	private String 					whatActivityFrom;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choice_hot_location);
		dba = ((CommonApplication) getApplicationContext()).getDbAdapter();
		location = getIntent().getStringExtra("location");
		findView();
		if (dba.findAllHotCitys() != null) {
			List<CitysBean> hotCitysList = new ArrayList<CitysBean>();
			hotCitysList = dba.findAllHotCitys();
			HotlocationAdapter hotlocationAdapter = new HotlocationAdapter(ChoiceHotLocationActivity.this, hotCitysList);
			lvHotLocation.setAdapter(hotlocationAdapter);
		}

		fillData();
		initMap();
		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft 				= (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight 				= (Button) this.findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle 				= (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

		tvGPSLocation 			= (TextView) this.findViewById(R.id.tvGPSLocation);
		ivSelectGPSLoction 		= (ImageView) this.findViewById(R.id.ivSelectGPSLoction);
		lvHotLocation 			= (ListViewInScrollView) this.findViewById(R.id.lvHotLocation);
		rlChoiceOtherLocation 	= (RelativeLayout) this.findViewById(R.id.rlChoiceOtherLocation);
		tvOtherLocation 		= (TextView) this.findViewById(R.id.tvOtherLocation);
		btnLeft.setOnClickListener(this);
		ivSelectGPSLoction.setOnClickListener(this);
		rlChoiceOtherLocation.setOnClickListener(this);

		rlSelectGPSLoction 		= (RelativeLayout) this.findViewById(R.id.rlSelectGPSLoction);
		rlSelectGPSLoction.setOnClickListener(this);
	}

	@Override
	public void fillData() {
		tvTitle.setText("选择热门城市");
		whatActivityFrom = getIntent().getStringExtra(Constants.KEY_SEL_CITY);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			/* if (isGPSLocation == true && otherCityhas == null && hotCityhas == null) {
				 location = tvGPSLocation.getText().toString();
			 } else if (isGPSLocation == false && otherCityhas == null && hotCityhas != null) {
				 location = hotCityhas;
			 } else if (isGPSLocation == false && otherCityhas != null && hotCityhas == null) {
				 location = otherCityhas;
			 }*/
			intentBackHome();
			break;
		/* case R.id.ivSelectGPSLoction:
		 if (isGPSLocation) {
			 isGPSLocation = false;
			 ivSelectGPSLoction.setImageResource(R.drawable.ic_check_nor);
		 } else {
			 isGPSLocation = true;
			 ivSelectGPSLoction.setImageResource(R.drawable.ic_check_sel);
		 // 清空已选的热门城市数据
		 if (temp != null) {
			 temp.cancleImage();
		 }
			 hotCityhas = null;
			 // 清空其他城市已选择的数据
			 otherCityhas = null;
			 tvOtherLocation.setText("其他城市选择");
		 }
		 break;*/

		case R.id.rlSelectGPSLoction:// 新的，点击直接获取gps城市返回首页
			isGPSLocation = true;
			// 清空已选的热门城市数据
			if (temp != null) {
				temp.cancleImage();
			}
			hotCityhas = null;
			// 清空其他城市已选择的数据
			otherCityhas = null;
			tvOtherLocation.setText("其他城市选择");
			location = tvGPSLocation.getText().toString();
			intentBackHome();
			break;
		case R.id.rlChoiceOtherLocation:
			// 弹出Dialog,省市级联选择
			// choiceOtherLocationDialog();
			new MyCityPickerDialog(this, new MyCityPickerDialog.OnCitySetListener() {
				@Override
				public void onDateTimeSet(String province, String city) {
					tvOtherLocation.setText(province + " " + city);
					if (province.equals("北京") || province.equals("天津") || province.equals("上海") || province.equals("重庆") || province.equals("香港")
							|| province.equals("澳门")) {
						otherCityhas = province;
					} else {
						otherCityhas = city;
					}
					location = otherCityhas;
					// 清空已选的热门城市数据
					if (temp != null) {
						temp.cancleImage();
					}
					hotCityhas = null;
					// 将gps定位数据清空
					isGPSLocation = false;
					intentBackHome();
				}
			}).show();
			
			
			break;
		}
	}

	/**
	 * 返回首页
	 */
	private void intentBackHome() {
		Intent intent = null;
		if (whatActivityFrom.equals(Constants.KEY_SEL_CITY_FROM_HOME)) {
			intent = new Intent(this, MainHomeActivityGroup.class);
		} else if (whatActivityFrom.equals(Constants.KEY_SEL_CITY_FROM_EVENT)) {
			intent = new Intent(this, MainHomeActivityGroup.class);
			intent.putExtra("viewPageNow", "eventList");
		}
		if (location != null) {
			intent.putExtra("location", location);
		}
		startActivity(intent);
		finish();
	}

	MyRadioButtonInListView temp;

	/**
	 * 热门城市
	 * 
	 * @author syghh
	 * 
	 */
	class HotlocationAdapter extends BaseAdapter {
		private Context 			context;
		private LayoutInflater 		inflater;
		private List<CitysBean> 	citysBeans;
		private boolean 			isNull = false;

		public HotlocationAdapter(Context context, List<CitysBean> citysBeans) {
			super();
			this.context = context;
			this.citysBeans = citysBeans;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return citysBeans.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final MyRadioButtonInListView radioButton;
			radioButton = new MyRadioButtonInListView(context);
			radioButton.setText(citysBeans.get(position).getName());
			radioButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 模版不为空，则chage.
					if (temp != null) {
						// temp.chageImage();
					}
					temp = radioButton;
					// radioButton.chageImage();
					hotCityhas = radioButton.getText().toString();
					// Log.i("ChoiceHotLocationActivity", "hotCityhas:" +
					// hotCityhas);
					location = hotCityhas;
					// 清空其他城市已选择的数据
					otherCityhas = null;
					tvOtherLocation.setText("其他城市选择");
					// 将gps定位数据清空
					isGPSLocation = false;
					intentBackHome();
				}
			});
			return radioButton;
		}
	}

	 /**
	 * 选择其他城市，跳出关联省市双列表
	 */
	/* private void choiceOtherLocationDialog() {
		 final AlertDialog dialogChoiceLocation = new
		 AlertDialog.Builder(ChoiceHotLocationActivity.this).create();
		 dialogChoiceLocation.show();
		 Window dialogWindow = dialogChoiceLocation.getWindow();
		 dialogWindow.setContentView(R.layout.choice_other_location_dialog);
	
		 ListView lvProvinces = (ListView)
		 dialogWindow.findViewById(R.id.lvProvinces);
		 final ListView lvCitys = (ListView)
		 dialogWindow.findViewById(R.id.lvCitys);
	
		 List<ProvincesBean> provincesList = new ArrayList<ProvincesBean>();
		 provincesList = dba.findAllProvinces();
		 List<String> provincesNameList = new ArrayList<String>();
		 for (int i = 0; i < provincesList.size(); i++) {
			 String provincesName = provincesList.get(i).getName();
			 provincesNameList.add(provincesName);
		 }
		 ArrayAdapter<String> provincesAdapter = new
		 ArrayAdapter<String>(ChoiceHotLocationActivity.this,  R.layout.choice_other_location_dialog_item, provincesNameList);
		 lvProvinces.setAdapter(provincesAdapter);
		 lvProvinces.setOnItemClickListener(new OnItemClickListener() {
			 @Override
			 public void onItemClick(AdapterView<?> parent1, View view1, int position1, long id1) {
				 provinceName = (String) parent1.getItemAtPosition(position1);
	
				 List<String> citysNameList = new ArrayList<String>();
				 citysNameList = dba.findCitysByProvinceId(position1 + 1);
				 ArrayAdapter<String> citysAdapter = new ArrayAdapter<String>(ChoiceHotLocationActivity.this, R.layout.choice_other_location_dialog_item, citysNameList);
				 lvCitys.setAdapter(citysAdapter);
				 lvCitys.setOnItemClickListener(new OnItemClickListener() {
					 @Override
					 public void onItemClick(AdapterView<?> parent2, View view2, int position2, long id2) {
						 String otherCityhasStr = (String) parent2.getItemAtPosition(position2);
						 tvOtherLocation.setText(provinceName + " " + otherCityhasStr);
						 if (provinceName.equals("北京") || provinceName.equals("天津") ||
								 provinceName.equals("上海") || provinceName.equals("重庆")
								 || provinceName.equals("香港") || provinceName.equals("澳门")) {
								 otherCityhas = provinceName;
						 } else {
							 otherCityhas = otherCityhasStr;
						 }
						 location = otherCityhas;
						 intentBackHome();
						 dialogChoiceLocation.dismiss();
	
						 }
					 });
					 }
				 });
	 }*/

	/**
	 * 初始化地图（GPS）
	 */
	private void initMap() {
		mMapManager = new BMapManager(this);
		mMapManager.init(getString(R.string.map_key), null);
		// super.initMapActivity(mMapManager);

		mMKSearch = new MKSearch();
		mMKSearch.init(mMapManager, new MySearchListener());
		mLocationManager = mMapManager.getLocationManager();
		mLocationManager.requestLocationUpdates(this);
		// 使用GPS定位
		mLocationManager.enableProvider((int) MKLocationManager.MK_GPS_PROVIDER);
	}

	/**
	 * 当位置发生变化时触发此方法
	 * 
	 * @param location
	 *            当前位置
	 */
	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			// 将当前位置转换成地理坐标点
			mMKSearch.reverseGeocode(new GeoPoint((int) (location.getLatitude() * 1e6), (int) (location.getLongitude() * 1e6)));
		}
	}

	/**
	 * 获取当前城市名
	 * 
	 * @author syghh
	 * 
	 */
	public class MySearchListener implements MKSearchListener {
		@Override
		public void onGetAddrResult(MKAddrInfo result, int iError) {
			MKGeocoderAddressComponent kk 	= result.addressComponents;
			city 							= kk.city;
			int cityLe 						= city.length();
			String cityName 				= city.substring(0, cityLe - 1);
			tvGPSLocation.setText(cityName);
		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult result, int iError) {
		}

		@Override
		public void onGetPoiResult(MKPoiResult result, int type, int iError) {

		}

		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult result, int iError) {
		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {

		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {

		}

		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {

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
		if (mMapManager != null) {
			mMapManager.start();
		}
		if (mLocationManager != null) {
			mLocationManager.requestLocationUpdates(this);
			mMKSearch = new MKSearch();
			mMKSearch.init(mMapManager, new MySearchListener());
		}
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mMapManager != null) {
			mMapManager.stop();
		}
		MobclickAgent.onPause(this);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}

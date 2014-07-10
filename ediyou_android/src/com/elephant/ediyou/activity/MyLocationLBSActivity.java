package com.elephant.ediyou.activity;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKGeocoderAddressComponent;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.umeng.analytics.MobclickAgent;

/**
 * 用户查看当前的位置
 * 
 * @author syghh
 * 
 */
public class MyLocationLBSActivity extends MapActivity implements IBaseActivity, OnClickListener {

	private Button btnLeft;
	private TextView tvTitle;
	private Button btnRight;

	private MapView mMapView = null;
	private LocationListener mLocationListener = null;// onResume时注册此listener，onPause时需要Remove
	private MyLocationOverlay mLocationOverlay = null; // 定位图层
	private CommonApplication app;
	private MKSearch mMKSearch = null;
	private MapController mMapController = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_location_lbs);
		app = (CommonApplication) this.getApplication();
		findView();
		fillData();
		initMap();
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
	}

	@Override
	public void fillData() {
		tvTitle.setText("当前位置");
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnLeft.setOnClickListener(this);
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
	 * 初始化
	 */
	public void initMap() {
		if (app.mBMapMan == null) {
			app.mBMapMan = new BMapManager(getApplication());
			app.mBMapMan.init(app.mStrKey, new CommonApplication.MyGeneralListener());
		}
		app.mBMapMan.start();
		// 如果使用地图SDK，请初始化地图Activity
		super.initMapActivity(app.mBMapMan);

		mMapView = (MapView) this.findViewById(R.id.bmapsView);
//		mMapView.setBuiltInZoomControls(true);
//		// 设置在缩放动画过程中也显示overlay,默认为不绘制
//		mMapView.setDrawOverlayWhenZooming(true);
//		
		mMapView.setBuiltInZoomControls(false); // 设置启用内置的缩放控件

		// 得到mMapView的控制权,可以用它控制和驱动平移和缩放
		mMapController = mMapView.getController();

		GeoPoint point = new GeoPoint((int) (39.915 * 1E6),
				(int) (116.404 * 1E6)); // 用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
		mMapController.setCenter(point); // 设置地图中心点
		mMapController.setZoom(16);

		// 添加定位图层
		mLocationOverlay = new MyLocationOverlay(this, mMapView);
		mMapView.getOverlays().add(mLocationOverlay);

		

		// 注册定位事件
		mLocationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {
					mMapView.invalidate(); // 刷新地图
					GeoPoint pt = new GeoPoint((int) (location.getLatitude() * 1e6), (int) (location.getLongitude() * 1e6));
					mMapView.getController().animateTo(pt);
					mMapController.setCenter(pt); // 设置地图中心点
					mMapController.setZoom(16);
					
					mMKSearch = new MKSearch();
					mMKSearch.init(app.mBMapMan, new MySearchListener());
					
					mMKSearch.reverseGeocode(pt);
				}
			}
		};
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public class MySearchListener implements MKSearchListener {
		@Override
		public void onGetAddrResult(MKAddrInfo result, int iError) {
			MKGeocoderAddressComponent kk = result.addressComponents;
			String city = kk.city;
			int cityLe = city.length();
			String cityName = city.substring(0, cityLe - 1);
			tvTitle.setText(cityName);
		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult result, int iError) {
		}

		@Override
		public void onGetPoiResult(MKPoiResult result, int type, int iError) {

		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {

		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {

		}

		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult arg0, int arg1) {

		}

		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {

		}
	}

	@Override
	protected void onResume() {
		// 注册定位事件，定位后将地图移动到定位点
		app.mBMapMan.getLocationManager().requestLocationUpdates(mLocationListener);
		mLocationOverlay.enableMyLocation();
		mLocationOverlay.enableCompass(); // 打开指南针
		app.mBMapMan.start();

		mMKSearch = new MKSearch();
		mMKSearch.init(app.mBMapMan, new MySearchListener());

		MobclickAgent.onResume(this);
		super.onResume();

	}

	@Override
	protected void onPause() {
		app.mBMapMan.getLocationManager().removeUpdates(mLocationListener);
		mLocationOverlay.disableMyLocation();
		mLocationOverlay.disableCompass(); // 关闭指南针
		app.mBMapMan.stop();
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (app.mBMapMan != null) {
			app.mBMapMan.destroy();
			app.mBMapMan = null;
		}

		super.onDestroy();
	}
}

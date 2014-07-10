package com.elephant.ediyou.activity;

import java.util.List;

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
import com.baidu.mapapi.MKLocationManager;
import com.baidu.mapapi.MKPoiInfo;
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
import com.baidu.mapapi.PoiOverlay;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.umeng.analytics.MobclickAgent;

/**
 * 预约地图页面
 * 
 * @author syghh
 * 
 */
public class KoalaAppointmentLocationActivity extends MapActivity implements IBaseActivity, OnClickListener,
		LocationListener {

	// 标题；
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	// MapView；
	private BMapManager mMapManager = null;
	private MapView mMapView = null;
	private MKLocationManager mLocationManager = null;
	private MapController mMapController = null;
	private MKSearch mMKSearch = null;
	private GeoPoint pt;// 当前位置的坐标
	private boolean isFirstTime = true;

	private CommonApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.koala_appointment_location);
		app = (CommonApplication) getApplication();
		app.addActivity(this);
		initMap();
		findView();
		fillData();
	}

	@Override
	public void findView() {
		btnLeft = (Button) findViewById(R.id.btnLeft);
		btnRight = (Button) findViewById(R.id.btnRight);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		btnRight.setVisibility(View.INVISIBLE);

		btnLeft.setOnClickListener(this);
	}

	@Override
	public void fillData() {
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		tvTitle.setText(R.string.roo_appointment_location);

	}

	/**
	 * 初始化
	 */
	private void initMap() {
		mMapManager = new BMapManager(this);
		mMapManager.init(getString(R.string.map_key), null);
		super.initMapActivity(mMapManager);

		mMKSearch = new MKSearch();
		mMKSearch.init(mMapManager, new MySearchListener());

		mMapView = (MapView) findViewById(R.id.bmapsView);
		mMapView.setBuiltInZoomControls(false); // 设置启用内置的缩放控件

		// 得到mMapView的控制权,可以用它控制和驱动平移和缩放
		mMapController = mMapView.getController();

		GeoPoint point = new GeoPoint((int) (39.915 * 1E6), (int) (116.404 * 1E6)); // 用给定的经纬度构造一个GeoPoint，单位是微度
																					// (度
																					// *
																					// 1E6)
		mMapController.setCenter(point); // 设置地图中心点
		mMapController.setZoom(16);

		mLocationManager = mMapManager.getLocationManager();
		mLocationManager.requestLocationUpdates(this);
		// 使用GPS定位
		mLocationManager.enableProvider((int) MKLocationManager.MK_GPS_PROVIDER);

		MyLocationOverlay myLocation = new MyLocationOverlay(this, mMapView);
		myLocation.enableMyLocation(); // 启用定位
		// myLocation.enableCompass(); // 启用指南针
		mMapView.getOverlays().add(myLocation);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onPause() {
		if (mMapManager != null) {
			mMapManager.stop();
		}
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		if (mMapManager != null) {
			mMapManager.start();
		}
		if (mLocationManager != null) {
			mLocationManager.requestLocationUpdates(this);
			mMKSearch = new MKSearch();
			mMKSearch.init(mMapManager, new MySearchListener());
		}
		super.onResume();
		MobclickAgent.onResume(this);
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
			pt = new GeoPoint((int) (location.getLatitude() * 1000000), (int) (location.getLongitude() * 1000000));
			// 将当前位置设置为地图的中心
			mMapController.setCenter(pt);
			app.mBMapMan.getLocationManager().removeUpdates(this);
			app.mBMapMan.getLocationManager().disableProvider(MKLocationManager.MK_GPS_PROVIDER);
			if (isFirstTime) {
				isFirstTime = false;
				mMKSearch.poiSearchNearBy("商场", pt, 1000);
				mMKSearch.poiSearchNearBy("电影院", pt, 1000);
				mMKSearch.poiSearchNearBy("银行", pt, 1000);
				mMKSearch.poiSearchNearBy("超市", pt, 1000);
			}
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public class MySearchListener implements MKSearchListener {
		@Override
		public void onGetAddrResult(MKAddrInfo result, int iError) {
		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult result, int iError) {
		}

		@Override
		public void onGetPoiResult(MKPoiResult result, int type, int iError) {
			if (result == null) {
				return;
			}
			mMapView.getOverlays().clear();
			mMapView.invalidate(); // 刷新地图
			PoiOverlay poioverlay = new PoiOverlay(KoalaAppointmentLocationActivity.this, mMapView);
			poioverlay.setData(result.getAllPoi());
			mMapView.getOverlays().add(poioverlay);

			List<MKPoiInfo> mkPoiInfos = result.getAllPoi();
			if (mkPoiInfos != null) {
				// if(!isFirstTime){
				// listAddr.clear();
				// }
				// listAddr.addAll(mkPoiInfos);
				// adapterAddress.notifyDataSetChanged();
			}

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
}

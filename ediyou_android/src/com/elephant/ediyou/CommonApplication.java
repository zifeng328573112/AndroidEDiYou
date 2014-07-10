package com.elephant.ediyou;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;
import com.elephant.ediyou.bean.EventPhotoBean;
import com.elephant.ediyou.db.DataBaseAdapter;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
/**
 * 应用全局变量
 * @author Aizhimin
 * 说明：	1、可以缓存一些应用全局变量。比如数据库操作对象
 */
public class CommonApplication extends Application {
	/**
	 * Singleton pattern
	 */
	private static CommonApplication instance;
	static CommonApplication mDemoApp;
	public BMapManager mBMapMan = null;//地图管理类；
	public String mStrKey = "82069EB19267A400E80733137A0A8A4C2B5D6F9A";
	boolean m_bKeyRight = true;	
	private IWXAPI wxApi;
	private String eventStartTime = "";//创建活动的开始时间；
	
	public static class MyGeneralListener implements MKGeneralListener {
		@Override
		public void onGetNetworkState(int iError) {
			Log.d("MyGeneralListener", "onGetNetworkState error is "+ iError);
		}

		@Override
		public void onGetPermissionState(int iError) {
			Log.d("MyGeneralListener", "onGetPermissionState error is "+ iError);
			if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
				CommonApplication.mDemoApp.m_bKeyRight = false;
			}
		}
	}
	/**
	 * 数据库操作类
	 * @return
	 */
	private DataBaseAdapter dataBaseAdapter;
	
	public static CommonApplication getInstance() {
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		mBMapMan = new BMapManager(this);
		mBMapMan.init(this.mStrKey, new MyGeneralListener());
		mBMapMan.getLocationManager().setNotifyInternal(10, 5);
		dataBaseAdapter = new DataBaseAdapter(this);
		dataBaseAdapter.open();
		wxApi = WXAPIFactory.createWXAPI(this, Constants.WEIXIN_APP_ID, true);
		wxApi.registerApp(Constants.WEIXIN_APP_ID);
	}
	
	/**
	 * 获得数据库操作对象
	 * @return
	 */
	public DataBaseAdapter getDbAdapter(){
		return this.dataBaseAdapter;
	}
	
	/**
	 * 缓存activity对象索引
	 */
	public List<Activity> activities = new ArrayList<Activity>();;
	public List<Activity> getActivities(){
		return activities;
	}
	public void addActivity(Activity mActivity) {
		activities.add(mActivity);
	}
	
	@Override
	public void onTerminate() {
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}
		super.onTerminate();
	}

	/**
	 * 某相册下的图片集合
	 */
	public List<EventPhotoBean> eventPhotoBeans;
	public List<EventPhotoBean> getPhotos() {
		return eventPhotoBeans;
	}
	public void setPhotos(List<EventPhotoBean> eventPhotoBeans) {
		this.eventPhotoBeans = eventPhotoBeans;
	}
	
	public List<EventPhotoBean> favEventPhotoBeans;

	public List<EventPhotoBean> getFavPhotos() {
		return favEventPhotoBeans;
	}

	public void setFavPhotos(List<EventPhotoBean> favEventPhotoBeans) {
		this.favEventPhotoBeans = favEventPhotoBeans;
	}

	public String getEventStartTime() {
		return eventStartTime;
	}

	public void setEventStartTime(String eventStartTime) {
		this.eventStartTime = eventStartTime;
	}
	
}

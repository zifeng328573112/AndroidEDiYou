package com.elephant.ediyou.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.EventCommentActivity;
import com.elephant.ediyou.activity.MainHomeActivityGroup;
import com.elephant.ediyou.activity.MessageCenterActivity;
import com.elephant.ediyou.activity.MyOrderActivity;
import com.elephant.ediyou.activity.MyReceivedOrderActivity;
import com.elephant.ediyou.activity.PopupPushScreenActivity;
import com.elephant.ediyou.activity.RooSelfCenterActivity;
import com.elephant.ediyou.bean.NotifyBean;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.AndroidUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;

/**
 * 信息推送服务
 * 
 * @author Aizhimin
 * 
 */
public class PullService extends Service {

	public static boolean isCurrActivity = false;// 私信页面是否是当前页面
	public BusinessHelper businessHelper;
	private NotificationManager mNM;
	public static final int PUSH_MESSAGE = 100;

	private Handler iNotifyHandler;
	private TimerTask notifyTimerTask;
	private Timer notifyTimer;

	private Handler iMessageHandler;
	private TimerTask messageTimerTask;
	private Timer messageTimer;

	private final static String TAG = "PullService";

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		businessHelper = new BusinessHelper();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		initNotifyHandler();
		startNotifyTask();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mNM.cancelAll();
		isrun = false;
		Log.i(TAG, "onDestroy");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (!SharedPrefUtil.getNotificationSetting(PullService.this)) {
			stopNotifyTimer();
			stopSelf();
		}
	}

	public boolean isrun = true;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new Binder() {
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}
	};

	private void initNotifyHandler() {
		iNotifyHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				super.handleMessage(msg);
			}
		};
	}

	private void startNotifyTask() {
		if (notifyTimerTask == null) {
			notifyTimerTask = new TimerTask() {
				@Override
				public void run() {
					try {
						if (NetUtil.checkNet(PullService.this)) {
							UserBean userBean = SharedPrefUtil.getUserBean(PullService.this);
							if (userBean != null) {
								int userID = userBean.getUserId();
								String accessToken = userBean.getAccessToken();
								int isKangaroo = userBean.getIsKangaroo();
								if (userID != 0 && userID != -1 && !StringUtil.isBlank(accessToken) && SharedPrefUtil.checkToken(PullService.this)) {
									JSONObject obj = businessHelper.unreadlistNotify(userID, accessToken);
									if (obj != null) {
										if (obj.has("status")) {
											int status = obj.getInt("status");
											if (status == 1) {
												if (obj.has("notis")) {
													JSONArray arr = obj.getJSONArray("notis");
													if (arr != null) {
														List<NotifyBean> notifyBeans = NotifyBean.constractList(arr);
														if (notifyBeans != null) {
															int size = notifyBeans.size();
															if (size > 0) {
																NotifyBean notifyBean = notifyBeans.get(0);
																int type = Integer.parseInt(notifyBean.getType());
																String title = notifyBean.getTitle();
																String content = notifyBean.getContent();
																if (Constants.NOTI_LETTER == type && !isCurrActivity) {
																	notifyOrIntentTo(type, title, content, MessageCenterActivity.class);
																} else if (Constants.NOTI_SYSTEM == type && !isCurrActivity) {
																	notifyOrIntentTo(type, title, content, MessageCenterActivity.class);
																} else if (Constants.NOTI_RESERVATION == type) {
																	notifyOrIntentTo(type, title, content, MyReceivedOrderActivity.class);
																} else if (Constants.NOTI_ORDER == type) {
																	if (isKangaroo == 0) {
																		notifyOrIntentTo(type, title, content, MyOrderActivity.class);
																	} else if (isKangaroo == 1) {
																		notifyOrIntentTo(type, title, content, MyReceivedOrderActivity.class);
																	}
																} else if (Constants.NOTI_ACTIVITY == type) {
																	// 直接跳转到袋鼠的个人中心
																	showNotification(type, title, content, RooSelfCenterActivity.class, 0, 0, 0,0);
																} else if (Constants.NOTI_ACTIVITY_SEND == type) {
																	notifyOrIntentTo(type, title, content, EventCommentActivity.class,
																			notifyBean.getUserId(), notifyBean.getWasId(), notifyBean.getActivityId(),notifyBean.getId());
																} else if (Constants.NOTI_ACTIVITY_RECEIVER == type) {
																	notifyOrIntentTo(type, title, content, EventCommentActivity.class,
																			notifyBean.getUserId(), notifyBean.getWasId(), notifyBean.getActivityId(),notifyBean.getId());
																}
															}
														}
													}
												}
											}
										} else {

										}
									}
								}
							}
						}

					} catch (Exception e) {
						// TODO: handle exception
						// Log.i(TAG, e.getMessage());
					}

					// Message msg = new Message();
					// msg.what = HANDLE_TYPE_MOVE;
					// iHandler.sendMessage(msg);
				}
			};
			notifyTimer = new Timer();
			notifyTimer.schedule(notifyTimerTask, 0, 30 * 1000);
		}
	}

	private void stopNotifyTimer() {
		if (notifyTimer != null) {
			notifyTimer.cancel();
			notifyTimer = null;
		}
		if (notifyTimerTask != null) {
			notifyTimerTask = null;
		}
	}

	/**
	 * 显示通知
	 * 
	 * @param eventBean
	 */
	private void showNotification(int type, String title, String content, Class<?> cls, long userId, long wasId, long activityId, long notiId) {
		// The details of our fake message
		// CharSequence title = createTitle(eventtype);
		// CharSequence content = createContent(eventtype,count);

		// The PendingIntent to launch our activity if the user selects this
		// notification
		Intent intent = new Intent(this, cls);
		intent.putExtra("type", type);// 消息类型
		intent.putExtra("title", title);// 消息内容
		if (userId != 0) {// 活动评论
			intent.putExtra("content", content);
			intent.putExtra("type", type);
			intent.putExtra("userId", userId);
			intent.putExtra("wasId", wasId);
			intent.putExtra("activityId", activityId);
			intent.putExtra("notiId", notiId);
		}
		/**
		 * requestCode 这个属性需要不一样，否则的话多个通知会指向相同的intent
		 */
		PendingIntent contentIntent = PendingIntent.getActivity(this, type, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// The ticker text, this uses a formatted string so our message could be
		// localized

		// construct the Notification object.
		Notification notif = new Notification(R.drawable.ic_launcher, null, System.currentTimeMillis());
		// 点击通知后自动从通知栏消失
		notif.flags = Notification.FLAG_AUTO_CANCEL;
		// Set the info for the views that show in the notification panel.
		notif.setLatestEventInfo(this, title, content, contentIntent);
		// AndroidUtil.exitApp(PullService.this); // 退出
		// after a 100ms delay, vibrate for 250ms, pause for 100 ms and
		// then vibrate for 500ms.
		// notif.vibrate = new long[] { 100, 250, 100, 500 };
		notif.defaults = Notification.DEFAULT_SOUND;
		mNM.notify(type, notif);
	}

	/**
	 * 判断app是否在前台运行
	 */
	private boolean isTopActivity() {
		String packageName = "com.elephant.ediyou";
		ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo.size() > 0) {
			Log.d(TAG, " 当前正在运行的程序包名：" + tasksInfo.get(0).topActivity.getPackageName());
			// 应用程序位于堆栈的顶层
			ComponentName topActivity = tasksInfo.get(0).topActivity;
			if (packageName.equals(topActivity.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 显示通知栏通知或者跳转
	 * 
	 * @param notifyId
	 * @param title
	 * @param content
	 * @param cla
	 */
	private void notifyOrIntentTo(int type, String title, String content, Class<?> cla) {
		if (isTopActivity()) {
			showNotification(type, title, content, cla, 0, 0, 0, 0);

		} else {
			Intent intent = new Intent();
			intent.setClass(PullService.this, PopupPushScreenActivity.class);
			intent.putExtra("title", title);
			intent.putExtra("content", content);
			intent.putExtra("type", type);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			AndroidUtil.exitApp(PullService.this); // 退出
		}
	}

	/**
	 * 显示通知栏通知或者跳转
	 * 
	 * @param notifyId
	 * @param title
	 * @param content
	 * @param cla
	 */
	private void notifyOrIntentTo(int type, String title, String content, Class<?> cla, long userId, long wasId, long activityId, long notiId) {
		if (isTopActivity()) {
			showNotification(type, title, content, cla, userId, wasId, activityId, notiId);
		} else {
			Intent intent = new Intent();
			intent.setClass(PullService.this, PopupPushScreenActivity.class);
			intent.putExtra("title", title);
			intent.putExtra("content", content);
			intent.putExtra("type", type);
			intent.putExtra("userId", userId);
			intent.putExtra("wasId", wasId);
			intent.putExtra("activityId", activityId);
			intent.putExtra("notiId", notiId);

			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			AndroidUtil.exitApp(PullService.this); // 退出
			startActivity(intent);
		}
	}
}

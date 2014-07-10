package com.elephant.ediyou.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.service.PullService;
import com.elephant.ediyou.util.SharedPrefUtil;

public class PhoneStartReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		UserBean bean = SharedPrefUtil.getUserBean(context);
		boolean notiSetting = SharedPrefUtil.getNotificationSetting(context);
		if (action.equals("android.intent.action.BOOT_COMPLETED") && bean != null && notiSetting) {
			Intent serviceIntent = new Intent(context,
					PullService.class);
			context.startService(serviceIntent);
		}
	}
}
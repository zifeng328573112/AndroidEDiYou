package com.elephant.ediyou.util;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

public class WeixingHelper {

	// APP_ID 替换为你的应用从官方网站申请到的合法appId
	public static final String APP_ID = "wx86c3082e40c74a20";
	public static IWXAPI api;
	private Context context;
	private boolean result = false;

	public WeixingHelper(Context context) {

		this.context = context;
		if (null == api) {
			api = WXAPIFactory.createWXAPI(context, APP_ID);
			api.registerApp(APP_ID);
		}

	}

	public static IWXAPI getApi() {
		return api;
	}

	public void shareText() {

		String text = "段不";
		if (text == null || text.length() == 0) {
			return;
		}

		// 初始化一个WXTextObject对象
		WXTextObject textObj = new WXTextObject();
		textObj.text = text;

		// 用WXTextObject对象初始化一个WXMediaMessage对象
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObj;
		msg.title = "类型的";
		// 发送文本类型的消息时，title字段不起作用
		// msg.title = "Will be ignored";
		msg.description = " 发送文本类型的消息时";

		// 构造一个Req
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneTimeline;
		// req.scene = SendMessageToWX.Req.WXSceneSession;

		// 调用api接口发送数据到微信

		result = api.sendReq(req);

	}

	public void sharePicture(String path) {

		File file = new File(path);

		WXImageObject imgObj = new WXImageObject();
		imgObj.setImagePath(path);

		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;

		Bitmap bmp = BitmapFactory.decodeFile(path);
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 80, 80, true);
		bmp.recycle();
		msg.thumbData = bmpToByteArray(thumbBmp, true);
		msg.title = "i am image title";
		msg.description = "i am image description";

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("img");
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneTimeline;
		// req.scene = SendMessageToWX.Req.WXSceneSession;
		boolean result = false;
		result = api.sendReq(req);

	}

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis())
				: type + System.currentTimeMillis();
	}

	private byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}

		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public void shareWeb(String type, String text, String path) {

		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = "http://www.ediyou.cn";
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = "来自E地游的本地Party,快来看看吧!";
		msg.description = "立即点击链接：http://www.ediyou.cn";
		Bitmap bmp = BitmapFactory.decodeFile(path);

		msg.thumbData = getBitmapBytes(bmp, false);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;
		if (type.equals("Fre")) {
			req.scene = SendMessageToWX.Req.WXSceneSession;
		} else
			req.scene = SendMessageToWX.Req.WXSceneTimeline;
		boolean result = false;
		result = api.sendReq(req);

		Log.e("edi", "result i s -------" + result);

	}// 需要对图片进行处理，否则微信会在log中输出thumbData检查错误

	private static byte[] getBitmapBytes(Bitmap bitmap, boolean paramBoolean) {
		Bitmap localBitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.RGB_565);
		Canvas localCanvas = new Canvas(localBitmap);
		int i;
		int j;
		if (bitmap.getHeight() > bitmap.getWidth()) {
			i = bitmap.getWidth();
			j = bitmap.getWidth();
		} else {
			i = bitmap.getHeight();
			j = bitmap.getHeight();
		}
		while (true) {
			localCanvas.drawBitmap(bitmap, new Rect(0, 0, i, j), new Rect(0, 0,
					80, 80), null);
			if (paramBoolean)
				bitmap.recycle();
			ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
			localBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
					localByteArrayOutputStream);
			localBitmap.recycle();
			byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
			try {
				localByteArrayOutputStream.close();
				return arrayOfByte;
			} catch (Exception e) {

			}
			i = bitmap.getHeight();
			j = bitmap.getHeight();
		}
	}

}

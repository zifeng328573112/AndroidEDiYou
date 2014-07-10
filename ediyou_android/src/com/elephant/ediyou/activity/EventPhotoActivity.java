package com.elephant.ediyou.activity;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.AsyncImageLoader;
import com.elephant.ediyou.AsyncImageLoader.ImageCallback;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.EventPhotoBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.FileUtil;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.umeng.analytics.MobclickAgent;

/**
 * 活动详情中图片的大图页
 * 
 * @author syghh
 * 
 */
public class EventPhotoActivity extends Activity implements IBaseActivity, OnClickListener {

	private Button btnLeft;
	private ImageView ivTitle;
	private TextView tvTitle;
	private Button btnRight;

	private ViewPager mViewPager;
	private PhotoPagerAdapter photoPagerAdapter;

	private List<EventPhotoBean> eventPhotoBeans;
	private int currentPhotoPosition;// 当前选择图片索引
	private EventPhotoBean EventPhotoBean;// 当前位置的对象
	private Button btnShare;// 分享到微博
	private Button btnCollect;// 收藏、赞
	private Button btnDown;// 下载
	private int screenWidth;// 屏幕宽度

	private File localFile;

	private ProgressDialog pd;

	View dialogV;
	Button btnShareToWeibo;
	Button btnShareToQQ;
	Button btnShareToWeixinFre;
	Button btnShareToWeixinFres;
	Button btnCancel;

	Dialog shareDialog;
	private long currPhotoId;

	public static Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
	public static int maxSize = 3;
	private long userId;

	
	private IWXAPI wxApi;
	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
	private static final int THUMB_SIZE = 150;
	private String path;
	private boolean versionCanShareWeiXin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_detail);
		MobclickAgent.onError(this);

		wxApi = WXAPIFactory.createWXAPI(this, Constants.WEIXIN_APP_ID);
		int wxSdkVersion = wxApi.getWXAppSupportAPI();
		if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION) {
			versionCanShareWeiXin = true;
		} else {
			versionCanShareWeiXin = false;
		}
		
		eventPhotoBeans = ((CommonApplication) getApplicationContext()).getPhotos();
		userId = SharedPrefUtil.getUserBean(this).getUserId();
		if (eventPhotoBeans == null) {
			eventPhotoBeans = new ArrayList<EventPhotoBean>();
		}
		currPhotoId = getIntent().getLongExtra("photoId", 0);
		currentPhotoPosition = getCurrentPhotoPosition();
		findView();
		fillData();
		screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
		// 添加到容器中
		((CommonApplication) getApplicationContext()).addActivity(this);
		if (imageCache == null)
			imageCache = new HashMap<String, SoftReference<Drawable>>();
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnLeft.setOnClickListener(this);
		ivTitle = (ImageView) this.findViewById(R.id.ivTitle);
		ivTitle.setVisibility(View.GONE);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		int size = 0;
		if (eventPhotoBeans != null) {
			size = eventPhotoBeans.size();
		}
		tvTitle.setText((currentPhotoPosition + 1) + "/" + size);
		tvTitle.setVisibility(View.VISIBLE);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);

		btnShare = (Button) this.findViewById(R.id.btnShare);
		btnShare.setOnClickListener(this);

		btnCollect = (Button) this.findViewById(R.id.btnCollect);
		btnCollect.setOnClickListener(this);

		btnDown = (Button) this.findViewById(R.id.btnDown);
		btnDown.setOnClickListener(this);
		EventPhotoBean = eventPhotoBeans.get(currentPhotoPosition);
		if (EventPhotoBean != null) {
			int collectCount = EventPhotoBean.getLikeCount();
			int shareCount = EventPhotoBean.getForwardCount();
			btnCollect.setText("" + collectCount);
			btnShare.setText("" + shareCount);
		}
		mViewPager = (ViewPager) this.findViewById(R.id.photo_detail_pager);
		photoPagerAdapter = new PhotoPagerAdapter(this, eventPhotoBeans);
		mViewPager.setAdapter(photoPagerAdapter);
		mViewPager.setCurrentItem(currentPhotoPosition);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int index) {
				currentPhotoPosition = index;
				tvTitle.setText((index + 1) + "/" + eventPhotoBeans.size());
				if (index + 1 == eventPhotoBeans.size()) {
					Toast.makeText(EventPhotoActivity.this, "已是最后一张", 3).show();
				}
				EventPhotoBean = eventPhotoBeans.get(currentPhotoPosition);
				currPhotoId = EventPhotoBean.getPhotoId();
				if (EventPhotoBean != null) {
					int collectCount = EventPhotoBean.getLikeCount();
					int shareCount = EventPhotoBean.getForwardCount();
					btnCollect.setText("" + collectCount);
					btnShare.setText("" + shareCount);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		// dialog
		dialogV = getLayoutInflater().inflate(R.layout.share_dialog, null);
		btnShareToWeibo = (Button) dialogV.findViewById(R.id.btnShareToWeibo);
		btnShareToQQ = (Button) dialogV.findViewById(R.id.btnShareToQQ);
		btnShareToWeixinFre = (Button) dialogV.findViewById(R.id.btnShareToWeixinFre);
		btnShareToWeixinFres = (Button) dialogV.findViewById(R.id.btnShareToWeixinFres);
		btnCancel = (Button) dialogV.findViewById(R.id.dialog_cancel);

		btnShareToWeibo.setOnClickListener(this);
		btnShareToQQ.setOnClickListener(this);
		btnShareToWeixinFre.setOnClickListener(this);
		btnShareToWeixinFres.setOnClickListener(this);
		btnCancel.setOnClickListener(this);

		shareDialog = showShareDialog();

	}

	@Override
	public void fillData() {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnDown:// 下载
			downLoadImage();
			break;
		case R.id.btnCollect:// 收藏(赞)
			if (SharedPrefUtil.getUserBean(this) != null) {
				if (NetUtil.checkNet(this)) {
					new UpActivityPhotoLikeCountTask(currPhotoId, userId).execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(this, "请先登陆账号", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.btnShare:// 分享
			EventPhotoBean eventPhotoBean = eventPhotoBeans.get(currentPhotoPosition);
			path = AsyncImageLoader.PHOTO_DIR + "/" + StringUtil.createImageName(eventPhotoBean.getPhotoUrl());
			localFile = new File(path);
			if (localFile.exists()) {
				shareDialog.show();

			} else {
				// Toast.makeText(this, R.string.photo_loading,
				// Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.btnShareToWeibo:
			shareDialog.dismiss();
			Intent intent = new Intent(this, SnsShareActivity.class);
			intent.putExtra(Constants.EXTRA_IMAGE_URL, localFile.getPath());
			intent.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_WEIBO);
			intent.putExtra(Constants.EXTRA_RES_TYPE, Constants.SHARE_RES_IMAGE);
			EventPhotoBean = eventPhotoBeans.get(currentPhotoPosition);
			intent.putExtra(Constants.EXTRA_SHARE_CONTENT, "");
			startActivity(intent);
			// 统计分享次数
			if (NetUtil.checkNet(this)) {
				new UpdateAcPhotoForwordCountTask(currPhotoId).execute();
			}
			break;
		case R.id.btnShareToQQ:
			shareDialog.dismiss();
			intent = new Intent(this, SnsShareActivity.class);
			intent.putExtra(Constants.EXTRA_IMAGE_URL, localFile.getPath());
			intent.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_QQ);
			intent.putExtra(Constants.EXTRA_RES_TYPE, Constants.SHARE_RES_IMAGE);
			EventPhotoBean = eventPhotoBeans.get(currentPhotoPosition);
			intent.putExtra(Constants.EXTRA_SHARE_CONTENT, "");
			startActivity(intent);
			// 统计分享次数
			if (NetUtil.checkNet(this)) {
				new UpdateAcPhotoForwordCountTask(currPhotoId).execute();
			}
			break;
		case R.id.btnShareToWeixinFre:
			shareDialog.dismiss();
			
			// 统计分享次数
			if (NetUtil.checkNet(this)) {
				new UpdateAcPhotoForwordCountTask(currPhotoId).execute();
			}
			if(versionCanShareWeiXin){
				//分享到好友
				shareToWeixin("Fre",path,localFile);
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.btnShareToWeixinFres:
			shareDialog.dismiss();
			
			// 统计分享次数
			if (NetUtil.checkNet(this)) {
				new UpdateAcPhotoForwordCountTask(currPhotoId).execute();
			}if(versionCanShareWeiXin){
				//分享到朋友圈
				shareToWeixin("Fres",path,localFile);
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.dialog_cancel:
			shareDialog.dismiss();
			break;
		}
	}
	
	
	/**
	 * 分享到微信，直接调取微信客户端
	 */
	private void shareToWeixin(String type,String path,File localFile) {
		if (!localFile.exists()) {
			Toast.makeText(EventPhotoActivity.this, "图片文件不存在", Toast.LENGTH_LONG).show();
		}

		WXImageObject imgObj = new WXImageObject();
		imgObj.setImagePath(path);

		WXMediaMessage msg = new WXMediaMessage();
		msg.title = "来自E地游的本地Party，快来看看吧：";
		msg.description = "立即点击链接：http://www.ediyou.cn";
		msg.mediaObject = imgObj;
		
		try {
			Bitmap bmp = ImageUtil.revitionImageSize(path, 200, 200, EventPhotoActivity.this);
			
			Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 200, 100, true);
			bmp.recycle();
			msg.thumbData = ImageUtil.bmpToByteArray(thumbBmp, true,100);

			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("img");
			req.message = msg;
			if(type.equals("Fre")){
				req.scene = SendMessageToWX.Req.WXSceneSession;
			} else if (type.equals("Fres")){
				req.scene = SendMessageToWX.Req.WXSceneTimeline;
			}
			wxApi.sendReq(req);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}


	private int getCurrentPhotoPosition() {
		if (eventPhotoBeans != null) {
			int photoSize = eventPhotoBeans.size();
			for (int i = 0; i < photoSize; i++) {
				EventPhotoBean EventPhotoBean = eventPhotoBeans.get(i);
				Long id = EventPhotoBean.getPhotoId();
				if (id != null) {
					if (id == currPhotoId) {
						return i;
					}
				}
			}
		}
		return 0;
	}

	public void downLoadImage() {
		EventPhotoBean EventPhotoBean = eventPhotoBeans.get(currentPhotoPosition);
		String imageName = StringUtil.createImageName(EventPhotoBean.getPhotoUrl());
		// 创建存储下载图片的目录
		File ImageDownloadfiles = new File(AsyncImageLoader.PHOTO_DIR + "/" + "imageDownload" + "/");
		if (!ImageDownloadfiles.exists()) {
			ImageDownloadfiles.mkdirs();
		}
		File ImageDownloadfile = new File(AsyncImageLoader.PHOTO_DIR + "/" + "imageDownload" + "/" + imageName);
		File localFile = new File(AsyncImageLoader.PHOTO_DIR, "/" + imageName);
		if (localFile != null && localFile.exists()) {
			FileUtil.copyFile(this, localFile, ImageDownloadfile);
			Toast.makeText(this, "已下载至:" + AsyncImageLoader.PHOTO_DIR + "/" + "imageDownload" + "/", Toast.LENGTH_LONG).show();
		} else {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					downLoadImage();
				}
			}, 1000);
		}
	}

	/**
	 * 图片Pager容器
	 * 
	 * @author syghh
	 * 
	 */
	private class PhotoPagerAdapter extends PagerAdapter {
		private Context mContext;
		private List<EventPhotoBean> eventPhotoBeans;

		public PhotoPagerAdapter(Context mContext, List<EventPhotoBean> eventPhotoBeans) {
			this.mContext = mContext;
			this.eventPhotoBeans = eventPhotoBeans;
		}

		@Override
		public void destroyItem(ViewGroup container, int arg1, Object arg2) {
			container.removeView((View) arg2);
		}

		@Override
		public Object instantiateItem(View container, int position) {
			EventPhotoBean eventPhotoBean = eventPhotoBeans.get(position);
			View view = LayoutInflater.from(mContext).inflate(R.layout.photo_detail_item, null);
			final ImageView ivPhoto = (ImageView) view.findViewById(R.id.ivPhoto);
			final ImageView ivPhotoDefault = (ImageView) view.findViewById(R.id.ivPhotoDefault);
			String hightImgUrl = eventPhotoBean.getPhotoUrl();
			final View progress = view.findViewById(R.id.progress);
			Drawable cacheDrawable = AsyncImageLoader.getInstance().loadSoftDrawable(imageCache, maxSize, hightImgUrl, new ImageCallback() {
				public void imageLoaded(Drawable imageDrawable, String imageUrl) {
					if (imageDrawable != null) {
						ivPhoto.setVisibility(View.VISIBLE);
						int oldwidth = imageDrawable.getIntrinsicWidth();
						int oldheight = imageDrawable.getIntrinsicHeight();
						LayoutParams lp = ivPhoto.getLayoutParams();
						lp.width = screenWidth;
						lp.height = (oldheight * screenWidth) / oldwidth;
						ivPhoto.setLayoutParams(lp);
						ivPhoto.setImageDrawable(imageDrawable);
						progress.setVisibility(View.GONE);
						ivPhotoDefault.setVisibility(View.GONE);
					} else {
						ivPhoto.setVisibility(View.GONE);
						progress.setVisibility(View.VISIBLE);
						ivPhotoDefault.setVisibility(View.VISIBLE);
					}
				}

			});
			if (cacheDrawable != null) {
				ivPhoto.setVisibility(View.VISIBLE);
				int oldwidth = cacheDrawable.getIntrinsicWidth();
				int oldheight = cacheDrawable.getIntrinsicHeight();
				LayoutParams lp = ivPhoto.getLayoutParams();
				lp.width = screenWidth;
				lp.height = (oldheight * screenWidth) / oldwidth;
				ivPhoto.setLayoutParams(lp);
				ivPhoto.setImageDrawable(cacheDrawable);
				ivPhotoDefault.setVisibility(View.GONE);
				progress.setVisibility(View.GONE);
			} else {
				ivPhoto.setVisibility(View.GONE);
				progress.setVisibility(View.VISIBLE);
				ivPhotoDefault.setVisibility(View.VISIBLE);
			}
			((ViewPager) container).addView(view, 0);
			return view;
		}

		@Override
		public int getCount() {
			return eventPhotoBeans.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

	}

	/**
	 * 分享对话框
	 */
	private Dialog showShareDialog() {
		Dialog dialog = new Dialog(this, R.style.dialog);
		dialog.setContentView(dialogV);
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
		lp.width = (int) (display.getWidth() - 40); // 设置宽度
		lp.gravity = Gravity.BOTTOM;
		dialog.getWindow().setAttributes(lp);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
			}
		});
		return dialog;
	}

	/**
	 * 更新活动图片赞的数量
	 * 
	 * @author syghh
	 * 
	 */
	class UpActivityPhotoLikeCountTask extends AsyncTask<Void, Void, JSONObject> {
		private long photoId;
		private long userId;

		public UpActivityPhotoLikeCountTask(long photoId, long userId) {
			this.photoId = photoId;
			this.userId = userId;

		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().upActivityPhotoLikeCount(photoId, userId);
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
						btnCollect.setText(String.valueOf((Integer.parseInt(btnCollect.getText().toString().trim()) + 1)));
						EventPhotoBean.setLikeCount((Integer.parseInt(btnCollect.getText().toString().trim())));
					} else {
						Toast.makeText(EventPhotoActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
				}
			} else {
				Toast.makeText(EventPhotoActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 更新活动图片转发（分享到微博）的数量
	 * 
	 * @author syghh
	 * 
	 */
	class UpdateAcPhotoForwordCountTask extends AsyncTask<Void, Void, JSONObject> {
		private long photoId;

		public UpdateAcPhotoForwordCountTask(long photoId) {
			this.photoId = photoId;

		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().updateAcPhotoForwordCount(photoId);
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
						btnShare.setText(String.valueOf((Integer.parseInt(btnShare.getText().toString().trim()) + 1)));
					} else {
						Toast.makeText(EventPhotoActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
				}
			} else {
				Toast.makeText(EventPhotoActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		imageCache.clear();
		imageCache = null;
		System.gc();
	}

}

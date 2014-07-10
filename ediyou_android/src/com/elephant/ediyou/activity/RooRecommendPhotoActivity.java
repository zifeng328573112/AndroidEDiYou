package com.elephant.ediyou.activity;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.AsyncImageLoader;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.R;
import com.elephant.ediyou.AsyncImageLoader.ImageCallback;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.RooRecommentBean;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 活动详情中图片的高清图显示
 * 
 * @author syghh
 * 
 */
public class RooRecommendPhotoActivity extends Activity implements IBaseActivity,
		OnClickListener {

	private Button btnLeft;
	private ImageView ivTitle;
	private TextView tvTitle;
	private Button btnRight;

	private ViewPager mViewPager;
	private PhotoPagerAdapter photoPagerAdapter;

	private ArrayList<RooRecommentBean> rooRecommentBeans;
	private int currentPhotoPosition;// 当前选择图片索引

	private RelativeLayout rlPhotoShare;
//	private Button btnShare;// 分享到微博
//	private Button btnCollect;// 留言评论
//	private Button btnDown;// 收藏
//	private File localFile;	
//
//	View dialogV;
//	Button btnShareToWeibo;
//	Button btnShareToQQ;
//	Button btnShareToRenren;
//	Button btnCancel;
//
//	Dialog shareDialog;
	
	private int screenWidth;// 屏幕宽度
	private ProgressDialog pd;

	private long currPhotoId;
	private boolean isSelf;
	
	public static Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
	public static int maxSize = 3;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_detail);
		MobclickAgent.onError(this);
		rooRecommentBeans = (ArrayList<RooRecommentBean>) getIntent().getSerializableExtra("rooRecommentBeans"); 
		if (rooRecommentBeans == null) {
			rooRecommentBeans = new ArrayList<RooRecommentBean>();
		}
		currPhotoId = getIntent().getLongExtra("photoId", 0);
		isSelf = getIntent().getBooleanExtra("isSelf", false);

		isSelf = true;//测试
		
		if(isSelf){
			rooRecommentBeans.remove(0);
		}
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
		if (rooRecommentBeans != null) {
			size = rooRecommentBeans.size();
		}
		tvTitle.setText((currentPhotoPosition + 1) + "/" + size);
		tvTitle.setVisibility(View.VISIBLE);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);

		rlPhotoShare = (RelativeLayout)this.findViewById(R.id.rlPhotoShare);
		rlPhotoShare.setVisibility(View.GONE);
		
//		btnShare = (Button) this.findViewById(R.id.btnShare);
//		btnShare.setOnClickListener(this);
//
//		btnCollect = (Button) this.findViewById(R.id.btnCollect);
//		btnCollect.setOnClickListener(this);
//
//		btnDown = (Button) this.findViewById(R.id.btnDown);
//		btnDown.setOnClickListener(this);

		mViewPager = (ViewPager) this.findViewById(R.id.photo_detail_pager);
		photoPagerAdapter = new PhotoPagerAdapter(this, rooRecommentBeans);
		mViewPager.setAdapter(photoPagerAdapter);
		mViewPager.setCurrentItem(currentPhotoPosition);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int index) {
				currentPhotoPosition = index;
				tvTitle.setText((index + 1) + "/" + rooRecommentBeans.size());
				if (index + 1 == rooRecommentBeans.size()) {
					Toast.makeText(RooRecommendPhotoActivity.this, "已是最后一张", 3).show();
				}
//				RooRecommentBean rooRecommentBean = rooRecommentBeans.get(currentPhotoPosition);
//				if (RooRecommentBean.getIsCollection() != null
//						&& RooRecommentBean.getIsCollection().equals(RooRecommentBean.COLLECTED)) {
//					Drawable drawable = getResources().getDrawable(R.drawable.ic_fav_sel);
//					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
//							drawable.getMinimumHeight());
//					btnPrivate.setCompoundDrawables(drawable, null, null, null);
//				} else {
//					Drawable drawable = getResources().getDrawable(
//							R.drawable.ic_fav_selector);
//					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
//							drawable.getMinimumHeight());
//					btnPrivate.setCompoundDrawables(drawable, null, null, null);
//				}
//				if (rooRecommentBean != null) {
//					int collectCount = rooRecommentBean.getLikeCount();
//					int shareCount = RooRecommentBean.getForwardCount();
//					btnCollect.setText("" + collectCount);
//					btnShare.setText("" + shareCount);
//				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		// dialog
//		dialogV = getLayoutInflater().inflate(R.layout.share_dialog, null);
//		btnShareToWeibo = (Button) dialogV.findViewById(R.id.btnShareToWeibo);
//		btnShareToQQ = (Button) dialogV.findViewById(R.id.btnShareToQQ);
//		btnShareToRenren = (Button) dialogV.findViewById(R.id.btnShareToRenren);
//		btnCancel = (Button) dialogV.findViewById(R.id.dialog_cancel);
//
//		btnShareToWeibo.setOnClickListener(this);
//		btnShareToQQ.setOnClickListener(this);
//		btnShareToRenren.setOnClickListener(this);
//		btnCancel.setOnClickListener(this);
//
//		shareDialog = showShareDialog();

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
//		case R.id.btnRight:// 下载
//			downLoadImage();
//			break;
		case R.id.btnCollect:// 收藏
//			UserBean userBean = SharedPrefUtil.getUserBean(this);
//			if (userBean == null) {
//				startActivity(new Intent(this, LoginActivity.class).putExtra(
//						Constants.EXTRA_LOGIN_FROM, Constants.LOGIN_FROM_PHOTO_DETAIL));
//				return;
//			}
//
//			if (NetUtil.checkNet(this)) {
//
//			} else {
//				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG)
//						.show();
//			}
			break;
		case R.id.btnShare:// 分享
//			RooRecommentBean RooRecommentBean = rooRecommentBeans.get(currentPhotoPosition);
//			localFile = new File(AsyncImageLoader.PHOTO_DIR,
//					StringUtil.createImageName(RooRecommentBean.getPhotoUrl()));
//			if (localFile.exists()) {
//				shareDialog.show();
//
//			} else {
//				// Toast.makeText(this, R.string.photo_loading,
//				// Toast.LENGTH_LONG).show();
//			}
//			break;
		case R.id.btnShareToWeibo:
//			shareDialog.dismiss();
//			Intent intent = new Intent(this, GalleryPhotoShareActivity.class);
//			intent.putExtra(Constants.EXTRA_PHOTO_URL, localFile.getPath());
//			intent.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_WEIBO);
//			RooRecommentBean = rooRecommentBeans.get(currentPhotoPosition);
//			intent.putExtra(Constants.EXTRA_RES_ID, RooRecommentBean.getId());
//			startActivity(intent);
			break;
		case R.id.btnShareToQQ:
//			shareDialog.dismiss();
//			intent = new Intent(this, GalleryPhotoShareActivity.class);
//			intent.putExtra(Constants.EXTRA_PHOTO_URL, localFile.getPath());
//			intent.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_QQ);
//			RooRecommentBean = rooRecommentBeans.get(currentPhotoPosition);
//			intent.putExtra(Constants.EXTRA_RES_ID, RooRecommentBean.getId());
//			startActivity(intent);
			break;
		case R.id.btnShareToWeixinFre:
//			shareDialog.dismiss();
//			intent = new Intent(this, GalleryPhotoShareActivity.class);
//			intent.putExtra(Constants.EXTRA_PHOTO_URL, localFile.getPath());
//			intent.putExtra(Constants.EXTRA_SHARE_TYPE, Constants.SHARE_TO_RENREN);
//			RooRecommentBean = rooRecommentBeans.get(currentPhotoPosition);
//			intent.putExtra(Constants.EXTRA_RES_ID, RooRecommentBean.getId());
//			startActivity(intent);
			break;
		case R.id.dialog_cancel:
//			shareDialog.dismiss();
			break;
		}
	}

	private int getCurrentPhotoPosition() {
		if (rooRecommentBeans != null) {
			int photoSize = rooRecommentBeans.size();
			for (int i = 0; i < photoSize; i++) {
				RooRecommentBean RooRecommentBean = rooRecommentBeans.get(i);
				Long id = RooRecommentBean.getPhotoId();
				if (id != null) {
					if (id == currPhotoId) {
						return i;
					}
				}
			}
		}
		return 0;
	}

//	public void downLoadImage() {
//		RooRecommentBean RooRecommentBean = rooRecommentBeans.get(currentPhotoPosition);
//		File localFile = new File(AsyncImageLoader.PHOTO_DIR,
//				StringUtil.createImageName(RooRecommentBean.getPhotoUrl()));
//		if (localFile.exists()) {
//			ImageUtil.addImageToGallery(this, localFile.getPath());
//			Toast.makeText(this, "下载成功", Toast.LENGTH_LONG).show();
//		} else {
//			new Handler().postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					downLoadImage();
//				}
//			}, 1000);
//		}
//	}

	/**
	 * 图片Pager容器
	 * 
	 * @author syghh
	 * 
	 */
	private class PhotoPagerAdapter extends PagerAdapter {
		private Context mContext;
		private List<RooRecommentBean> rooRecommentBeans;

		public PhotoPagerAdapter(Context mContext, List<RooRecommentBean> rooRecommentBeans) {
			this.mContext = mContext;
			this.rooRecommentBeans = rooRecommentBeans;
		}

		@Override
		public void destroyItem(ViewGroup container, int arg1, Object arg2) {
			container.removeView((View) arg2);
		}

		@Override
		public Object instantiateItem(View container, int position) {
			RooRecommentBean RooRecommentBean = rooRecommentBeans.get(position);
			View view = LayoutInflater.from(mContext).inflate(R.layout.photo_detail_item,
					null);
			final ImageView ivPhoto = (ImageView) view.findViewById(R.id.ivPhoto);
			final ImageView ivPhotoDefault = (ImageView) view
					.findViewById(R.id.ivPhotoDefault);
			String hightImgUrl = RooRecommentBean.getPhotoUrl();
			final View progress = view.findViewById(R.id.progress);
			Drawable cacheDrawable = AsyncImageLoader.getInstance().loadSoftDrawable(
					imageCache, maxSize, hightImgUrl, new ImageCallback() {
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
			return rooRecommentBeans.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

	}

	/**
	 * 分享对话框
	 */
//	private Dialog showShareDialog() {
//		Dialog dialog = new Dialog(this, R.style.dialog);
//		dialog.setContentView(dialogV);
//		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//
//			@Override
//			public void onCancel(DialogInterface dialog) {
//				dialog.dismiss();
//			}
//		});
//		return dialog;
//	}

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

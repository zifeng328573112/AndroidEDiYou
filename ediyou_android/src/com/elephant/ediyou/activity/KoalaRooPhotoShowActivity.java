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
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.AsyncImageLoader;
import com.elephant.ediyou.AsyncImageLoader.ImageCallback;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.R;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.PersonalPhotoBean;
import com.umeng.analytics.MobclickAgent;

public class KoalaRooPhotoShowActivity extends Activity implements IBaseActivity,OnClickListener {

	//标题；
	private Button btnLeft;
	private TextView tvTitle;
	private Button btnRight;

	private ViewPager mViewPager;
	private PhotoPagerAdapter photoPagerAdapter;
	
	private int screenWidth;// 屏幕宽度
	private File localFile;
	private ProgressDialog pd;

	private List<PersonalPhotoBean> photoBeans;
	private int currentPhotoPosition;// 当前选择图片索引
	
	private long currPhotoId;
	private boolean isSelf;

	public static Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
	public static int maxSize = 3;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.koala_roo_photo_show);
		photoBeans = (List<PersonalPhotoBean>) getIntent().getSerializableExtra("photoBeans");
		if (photoBeans == null) {
			photoBeans = new ArrayList<PersonalPhotoBean>();
		}
		isSelf = getIntent().getBooleanExtra("isSelf", false);
		currPhotoId = getIntent().getLongExtra("photoId", 0);
//		if(isSelf){
//			photoBeans.remove(0);
//		}
		currentPhotoPosition = getCurrentPhotoPosition();
		findView();
		fillData();
		screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
		// 添加到容器中
		((CommonApplication) getApplicationContext()).addActivity(this);
		if (imageCache == null){
			imageCache = new HashMap<String, SoftReference<Drawable>>();
		}
		
	}
	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnLeft.setOnClickListener(this);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		int size = 0;
		if (photoBeans != null) {
			size = photoBeans.size();
		}
		tvTitle.setText((currentPhotoPosition + 1) + "/" + size);
		tvTitle.setVisibility(View.VISIBLE);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		
		mViewPager = (ViewPager) this.findViewById(R.id.photo_detail_pager);
		photoPagerAdapter = new PhotoPagerAdapter(this, photoBeans);
		mViewPager.setAdapter(photoPagerAdapter);
		mViewPager.setCurrentItem(currentPhotoPosition);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int index) {
				currentPhotoPosition = index;
				tvTitle.setText((index + 1) + "/" + photoBeans.size());
				if (index + 1 == photoBeans.size()) {
					Toast.makeText(KoalaRooPhotoShowActivity.this, "已是最后一张", 3).show();
				}
				PersonalPhotoBean photoBean = photoBeans.get(currentPhotoPosition);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

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
		default:
			break;
		}
	}
	
	private int getCurrentPhotoPosition() {
		if (photoBeans != null) {
			int photoSize = photoBeans.size();
			for (int i = 0; i < photoSize; i++) {
				PersonalPhotoBean photoBean = photoBeans.get(i);
				Long id = photoBean.getPhotoId();
				if (id != null) {
					if (id == currPhotoId) {
						return i;
					}
				}
			}
		}
		return 0;
	}
	
	/**
	 * 图片Pager容器
	 * 
	 * @author syghh
	 * 
	 */
	private class PhotoPagerAdapter extends PagerAdapter {
		private Context mContext;
		private List<PersonalPhotoBean> photoBeans;

		public PhotoPagerAdapter(Context mContext, List<PersonalPhotoBean> photoBeans) {
			this.mContext = mContext;
			this.photoBeans = photoBeans;
		}

		@Override
		public void destroyItem(ViewGroup container, int arg1, Object arg2) {
			container.removeView((View) arg2);
		}

		@Override
		public Object instantiateItem(View container, int position) {
			PersonalPhotoBean photoBean = photoBeans.get(position);
			View view = LayoutInflater.from(mContext).inflate(R.layout.photo_detail_item,
					null);
			final ImageView ivPhoto = (ImageView) view.findViewById(R.id.ivPhoto);
			final ImageView ivPhotoDefault = (ImageView) view
					.findViewById(R.id.ivPhotoDefault);
			String hightImgUrl = photoBean.getPhotoUrl();
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
								PhotoPagerAdapter.this.notifyDataSetChanged();
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
			return photoBeans.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
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

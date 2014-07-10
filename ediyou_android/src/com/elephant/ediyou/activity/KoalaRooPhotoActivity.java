package com.elephant.ediyou.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.AsyncImageLoader;
import com.elephant.ediyou.AsyncImageLoader.ImageCallback;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.RooRecommendDetailActivity.KangarooUpRecommendTask;
import com.elephant.ediyou.activity.RooRecommendDetailActivity.LoadRecommendDetailTask;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.PersonalPhotoBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.elephant.ediyou.view.GridViewInScrollView;
import com.umeng.analytics.MobclickAgent;

/**
 * 袋鼠和考拉个人照片展示编辑--Old
 * 
 * @author Zhoujun
 * 
 */
public class KoalaRooPhotoActivity extends Activity implements IBaseActivity, OnClickListener {

	public static Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
	public static int maxSize = 15;
	ArrayList<PersonalPhotoBean> photoBeans = new ArrayList<PersonalPhotoBean>();
	// 加载更多
	private ProgressBar pbFooter;
	private TextView tvFooterMore;
	private int pageNo = 1;
	private int pageSize = 15;
	private int totalPage = -1;

	private ProgressDialog pd;
	private long uid;
	private boolean isSelf = false;
	private CommonApplication app;
	Display display;

	/* 拍照的照片存储位置 */
	private File PHOTO_DIR;
	private File mCurrentPhotoFile;// 照相机拍照得到的图片，临时文件
	private File photoFile;// 照片文件
	/* 用来标识请求照相功能的activity */
	public static final int CAMERA_WITH_DATA = 3023;
	/* 用来标识请求gallery的activity */
	public static final int PHOTO_PICKED_WITH_DATA = 3021;

	private GridViewInScrollView gvPersonalPhoto;
	private PersonalPhotoAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.koala_roo_photo);
		app = (CommonApplication) getApplication();
		app.addActivity(this);
		uid = getIntent().getLongExtra("id", 0);
		if (uid == SharedPrefUtil.getUserBean(this).getUserId()) {
			isSelf = true;
		}

		display = this.getWindowManager().getDefaultDisplay();

		findView();
		fillData();
		createPhotoDir();
	}

	/**
	 * 创建照片存储目录
	 */
	private void createPhotoDir() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			PHOTO_DIR = new File(Environment.getExternalStorageDirectory() + "/" + Constants.APP_DIR_NAME + "/");
			if (!PHOTO_DIR.exists()) {
				// 创建照片的存储目录
				PHOTO_DIR.mkdirs();
			}
		} else {
			Toast.makeText(this, "请检查SD卡是否正常", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void findView() {
		gvPersonalPhoto = (GridViewInScrollView) findViewById(R.id.gvPhoto);
		pbFooter = (ProgressBar) findViewById(R.id.progressBar);
		tvFooterMore = (TextView) findViewById(R.id.tvMore);

	}

	@Override
	public void fillData() {
		if (isSelf) {
			photoBeans.add(0, new PersonalPhotoBean());
		}
		adapter = new PersonalPhotoAdapter();
		gvPersonalPhoto.setAdapter(adapter);
		if (NetUtil.checkNet(KoalaRooPhotoActivity.this)) {
			new PersonalPhotoTask(uid, pageNo, pageSize).execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
		}

		if (photoBeans.size() < pageSize) {
			tvFooterMore.setVisibility(View.GONE);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvMore:// 点击加载更多
			if (!isRuning && pageNo > 1 && pageNo <= totalPage) {
				pbFooter = new ProgressBar(this);
				if (NetUtil.checkNet(this)) {
					new PersonalPhotoTask(uid, pageNo, pageSize).execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
				}
			}
			break;

		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PHOTO_PICKED_WITH_DATA:
				Bitmap cameraBitmap = data.getParcelableExtra("data");
				if (cameraBitmap == null) {
					Uri dataUri = data.getData();
					Intent intent = getCropImageIntent(dataUri);
					startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
					return;
				}
				try {
					// 保存缩略图
					FileOutputStream out = null;
					photoFile = new File(PHOTO_DIR, "photo" + uid + ".jpg");
					if (photoFile != null && photoFile.exists()) {
						photoFile.delete();
					}
					out = new FileOutputStream(photoFile, false);

					if (cameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
						out.flush();
						out.close();
					}
					if (mCurrentPhotoFile.exists())
						mCurrentPhotoFile.delete();
				} catch (Exception e) {
					MobclickAgent.reportError(KoalaRooPhotoActivity.this, StringUtil.getExceptionInfo(e));
				}
				// Uri dataUri = data.getData();
				if (NetUtil.checkNet(this)) {
					new UpdatePhotoTask("upload", photoFile).execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
				}
				break;
			case CAMERA_WITH_DATA:// 拍照
				doCropPhoto(mCurrentPhotoFile);
				break;

			default:
				break;
			}

		}
	};

	/**
	 * 调用图片剪辑程序去剪裁图片
	 * 
	 * @param f
	 */
	protected void doCropPhoto(File f) {
		try {
			// 启动gallery去剪辑这个照片
			final Intent intent = getCropImageIntent(Uri.fromFile(f));
			startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
		} catch (Exception e) {
			MobclickAgent.reportError(this, StringUtil.getExceptionInfo(e));
			Toast.makeText(this, "照片裁剪出错", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 调用图片剪辑程序
	 */
	public static Intent getCropImageIntent(Uri photoUri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 400);
		intent.putExtra("outputY", 300);

		intent.putExtra("return-data", true);
		return intent;
	}

	private boolean isRuning = false;// 标示此任务是否在运行中

	/**
	 * 活动图片
	 * 
	 * @author syghh
	 * 
	 */
	class PersonalPhotoTask extends AsyncTask<Void, Void, JSONObject> {

		private long userId;
		private int pageIndex;
		private int pageSize;

		public PersonalPhotoTask(long userId, int pageIndex, int pageSize) {
			this.userId = userId;
			this.pageIndex = pageIndex;
			this.pageSize = pageSize;
		}

		@Override
		protected void onPreExecute() {
			if (pd == null) {
				pd = new ProgressDialog(KoalaRooPhotoActivity.this);
			}
			pd.setMessage("正在获取照片...");
			isRuning = true;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().getPersonalPhoto(userId, pageIndex, pageSize);
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (pd != null) {
				pd.dismiss();
			}
			if (result != null) {

				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						photoBeans = PersonalPhotoBean.constractListBean(result.getJSONArray("data"), photoBeans);
						if (photoBeans.size() > 0) {
							adapter.notifyDataSetChanged();
						}
						if (photoBeans.size() >= pageSize) {
							tvFooterMore.setVisibility(View.VISIBLE);
						} else if (photoBeans.size() < pageSize) {
							tvFooterMore.setVisibility(View.GONE);
						}
						totalPage = result.getInt("total");
						if (result.getInt("current") == result.getInt("total")) {
							tvFooterMore.setText("全部加载完毕");
						}
					} else {
						Toast.makeText(KoalaRooPhotoActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
						tvFooterMore.setVisibility(View.GONE);
					}
				} catch (JSONException e) {
					Toast.makeText(KoalaRooPhotoActivity.this, "读图错误", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(KoalaRooPhotoActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
			isRuning = false;
		}
	}

	private class PersonalPhotoAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return photoBeans.size();
		}

		@Override
		public Object getItem(int position) {
			return photoBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void remove(int position) {
			photoBeans.remove(position);
			this.notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final PersonalPhotoBean photoBean = photoBeans.get(position);
			final int delPosition = position;
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = getLayoutInflater().inflate(R.layout.koala_roo_photo_item, null);
				holder.ivPhoto = (ImageView) convertView.findViewById(R.id.ivPhoto);
				holder.ivDel = (ImageView) convertView.findViewById(R.id.ivDelPhoto);
				holder.viewAddPhoto = convertView.findViewById(R.id.viewAddPhoto);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			int itemWidth = (display.getWidth() - 4 * 10) / 3;
			android.view.ViewGroup.LayoutParams param = holder.ivPhoto.getLayoutParams();
			param.width = itemWidth;
			param.height = itemWidth;

			if (isSelf && position == 0) {
				holder.viewAddPhoto.setVisibility(View.VISIBLE);
				holder.viewAddPhoto.setLayoutParams(param);
				holder.ivPhoto.setVisibility(View.GONE);
				holder.viewAddPhoto.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						createUploadIconDialog();
					}
				});
			} else {
				if (photoBean.isDel()) {
					holder.ivDel.setVisibility(View.VISIBLE);
				} else {
					holder.ivDel.setVisibility(View.GONE);
				}
				holder.ivDel.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (NetUtil.checkNet(KoalaRooPhotoActivity.this)) {
							new UpdatePhotoTask("del", photoBean.getPhotoId(), delPosition).execute();
						} else {
							Toast.makeText(KoalaRooPhotoActivity.this, R.string.NoSignalException, Toast.LENGTH_LONG);
						}
					}
				});
				holder.viewAddPhoto.setVisibility(View.GONE);
				holder.ivPhoto.setVisibility(View.VISIBLE);
				holder.ivPhoto.setLayoutParams(param);
				String thumbUrl = photoBean.getThumbUrl();
				holder.ivPhoto.setTag(R.id.tag_personalphotobean, photoBean);
				holder.ivPhoto.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (photoBean.isDel()) {
							photoBean.setDel(false);
							notifyDataSetChanged();
							return;
						} else {
							Intent intent = new Intent(KoalaRooPhotoActivity.this, KoalaRooPhotoShowActivity.class);
							intent.putExtra("photoId", photoBean.getPhotoId());
							intent.putExtra("photoBeans", photoBeans);
							intent.putExtra("isSelf", isSelf);
							startActivity(intent);
						}
					}
				});
				holder.ivPhoto.setTag(thumbUrl);
				Drawable cacheDrawable = AsyncImageLoader.getInstance().loadDrawable(thumbUrl, new ImageCallback() {
					@Override
					public void imageLoaded(Drawable imageDrawable, String imageUrl) {
						ImageView ivImage = (ImageView) gvPersonalPhoto.findViewWithTag(imageUrl);
						if (ivImage != null) {
							if (imageDrawable != null) {
								ivImage.setImageDrawable(imageDrawable);
							}
						}
					}
				});
				if (cacheDrawable != null) {
					holder.ivPhoto.setImageDrawable(cacheDrawable);
				}
				if (isSelf) {
					holder.ivPhoto.setOnLongClickListener(new View.OnLongClickListener() {

						@Override
						public boolean onLongClick(View v) {
							if (!photoBean.isDel()) {
								photoBean.setDel(true);
								notifyDataSetChanged();
							}
							return true;
						}
					});
				}
			}

			return convertView;
		}

	}

	class ViewHolder {
		private ImageView ivPhoto;
		private ImageView ivDel;
		private View viewAddPhoto;
	}

	/**
	 * 上传头像
	 * 
	 * @param v
	 */
	private void createUploadIconDialog() {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("选择照片");
		ab.setItems(new String[] { "相机拍摄", "手机相册", "取消" }, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch (which) {
				case 0:// 相机拍摄
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
						doTakePhoto();// 用户点击了从照相机获取
					} else {
						Toast.makeText(KoalaRooPhotoActivity.this, "请检查SD卡是否正常", Toast.LENGTH_SHORT).show();
					}
					break;
				case 1:// 手机相册
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("image/*");
					intent.putExtra("return-data", true);
					startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
					break;
				case 2:

					break;
				}
			}
		});
		ab.show();
	}

	/**
	 * 拍照获取图片
	 * 
	 */
	protected void doTakePhoto() {
		try {
			mCurrentPhotoFile = new File(PHOTO_DIR, ImageUtil.getPhotoFileName());// 给新照的照片文件命名
			final Intent intent = getTakePickIntent(mCurrentPhotoFile);
			startActivityForResult(intent, CAMERA_WITH_DATA);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "拍照出错", Toast.LENGTH_LONG).show();
		}
	}

	public static Intent getTakePickIntent(File f) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		return intent;
	}

	/**
	 * 处理照片接口；type为“upload”为上传照片；type为“del”为删除照片；
	 * 
	 * @author Zhoujun
	 * 
	 */
	private class UpdatePhotoTask extends AsyncTask<Void, Void, JSONObject> {

		private String type;
		private long photoId;
		private File file;
		private int position;

		public UpdatePhotoTask(String type, long photoId, int position) {
			super();
			this.type = type;
			this.photoId = photoId;
			this.position = position;
		}

		public UpdatePhotoTask(String type, File file) {
			super();
			this.type = type;
			this.file = file;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (type.equals("upload")) {
				if (pd == null) {
					pd = new ProgressDialog(KoalaRooPhotoActivity.this);
				}
				pd.setMessage("上传照片中...");
				pd.show();
			}
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			String token = SharedPrefUtil.getUserBean(KoalaRooPhotoActivity.this).getAccessToken();
			JSONObject obj = null;
			try {
				if (type.equals("upload")) {
					long userId = SharedPrefUtil.getUserBean(KoalaRooPhotoActivity.this).getUserId();
					obj = new BusinessHelper().uploadPersonalPhoto(userId, file, token);
				} else if (type.equals("del")) {
					obj = new BusinessHelper().delPersonalPhoto(photoId, token);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			return obj;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (pd != null) {
				pd.dismiss();
			}
			String failText = null;
			String successText = null;
			if (type.equals("upload")) {
				failText = "上传照片失败";
				successText = "上传照片成功";
			} else if (type.equals("del")) {
				failText = "删除照片失败";
				successText = "删除照片成功";
			}
			if (result != null) {
				try {
					if (result.getInt("status") == Constants.SUCCESS) {
						Toast.makeText(KoalaRooPhotoActivity.this, successText, Toast.LENGTH_SHORT).show();
						if (type.equals("upload")) {
							PersonalPhotoBean bean = new PersonalPhotoBean(result.getJSONArray("data").getJSONObject(0));
							photoBeans.add(1, bean);
							adapter.notifyDataSetChanged();
							if (result.getInt("current") == result.getInt("total")) {
								tvFooterMore.setText("全部加载完毕");
							}
						} else if (type.equals("del")) {
							adapter.remove(position);
						}
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(KoalaRooPhotoActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(KoalaRooPhotoActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(KoalaRooPhotoActivity.this, failText, Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			} else {
				Toast.makeText(KoalaRooPhotoActivity.this, failText, Toast.LENGTH_LONG).show();
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

}

package com.elephant.ediyou.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
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
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.PersonalPhotoBean;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

public class SelfPhotosAndAvatarActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button 					btnLeft;
	private TextView 				tvTitle;
	private Button 					btnRight;
	private CommonApplication 		app;

	private TextView 				tvShowNo;

	public static Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
	public static int 				maxSize = 15;
	ArrayList<PersonalPhotoBean> 	photoBeans = new ArrayList<PersonalPhotoBean>();
	// 加载更多
	private ProgressBar 			pbFooter;
	private TextView 				tvFooterMore;
	private int 					pageNo = 1;
	private int 					pageSize = 15;
	private int 					totalPage = -1;

	private ProgressDialog 			pd;
	private long 					userId;
	private String 					accessToken;
	private boolean 				isSelf = false;
	Display 						display;

	/* 拍照的照片存储位置 */
	private File 					PHOTO_DIR;
	private File 					mCurrentPhotoFile;// 照相机拍照得到的图片，临时文件
	private File 					photoFile;// 照片文件
	/* 用来标识请求照相功能的activity */
	public static final int 		CAMERA_WITH_DATA = 3023;
	/* 用来标识请求gallery的activity */
	public static final int 		PHOTO_PICKED_WITH_DATA = 3021;
	private static final int 		SETTING_AVATAR = 1002;

	private GridView 				gvPersonalPhoto;

	private PersonalPhotoAdapter 	adapter;

	private String 					name;
	private String 					titleType;// 袋鼠或考拉

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.self_photos_and_avatar_layout);
		app 			= (CommonApplication) getApplication();
		app.addActivity(this);

		userId 			= getIntent().getLongExtra(Constants.EXTRA_USER_ID, 0);
		name 			= getIntent().getStringExtra(Constants.EXTRA_NAME);
		titleType 		= getIntent().getStringExtra("rooOrKoala");
		if (userId == SharedPrefUtil.getUserBean(this).getUserId()) {
			isSelf = true;
		}
		accessToken 	= SharedPrefUtil.getUserBean(this).getAccessToken();
		display 		= this.getWindowManager().getDefaultDisplay();
		createPhotoDir();
		findView();
		fillData();
	}

	@Override
	public void findView() {
		btnLeft 		= (Button) findViewById(R.id.btnLeft);
		btnRight 		= (Button) findViewById(R.id.btnRight);
		tvTitle 		= (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		if (isSelf) {
			btnRight.setBackgroundResource(R.drawable.bg_btn_selector);
			btnRight.setText("上传");
			btnRight.setGravity(Gravity.CENTER);
			btnRight.setTextColor(Color.rgb(157, 208, 99));
			tvTitle.setText("我的照片");
		} else {
			btnRight.setVisibility(View.INVISIBLE);
			if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(titleType)) {
				if (titleType.equals("roo")) {
					tvTitle.setText(/* "袋鼠" + */"'" + name + "'" + "的照片");
				} else if (titleType.equals("koala")) {
					tvTitle.setText(/* "考拉" + */"'" + name + "'" + "的照片");
				}
			}
		}

		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight.setOnClickListener(this);
		btnLeft.setOnClickListener(this);

		gvPersonalPhoto 	= (GridView) findViewById(R.id.gvPhoto);
		pbFooter 			= (ProgressBar) findViewById(R.id.progressBar);
		tvFooterMore 		= (TextView) findViewById(R.id.tvMore);
		tvFooterMore.setOnClickListener(this);

		tvShowNo 			= (TextView) findViewById(R.id.tvShowNo);
	}

	@Override
	public void fillData() {
		// if (isSelf) {
			// photoBeans.add(0, new PersonalPhotoBean());
		// }
		adapter = new PersonalPhotoAdapter();
		gvPersonalPhoto.setAdapter(adapter);
		if (NetUtil.checkNet(SelfPhotosAndAvatarActivity.this)) {
			new PersonalPhotoTask(userId, pageNo, pageSize).execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
		}

		if (photoBeans.size() < pageSize) {
			tvFooterMore.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnRight:// 上传照片
			createUploadIconDialog();
			break;
		case R.id.tvMore:// 点击加载更多
			if (!isRuning && pageNo > 1 && pageNo <= totalPage) {
				pbFooter = new ProgressBar(this);
				if (NetUtil.checkNet(this)) {
					new PersonalPhotoTask(userId, pageNo, pageSize).execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
			}
			break;
		}
	}

	/****************************** 关于上传照片的方法 ********************************/
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CAMERA_WITH_DATA:// 拍照
			case PHOTO_PICKED_WITH_DATA:
			/*	 Bitmap cameraBitmap = data.getParcelableExtra("data");
				 if (cameraBitmap == null) {
					 Uri dataUri = data.getData();
					 Intent intent = getCropImageIntent(dataUri);
					 startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
					 return;
				 }*/
				Bitmap cameraBitmap = null;
				String img_path = null;
				if (requestCode == CAMERA_WITH_DATA) {
					Uri uri = Uri.fromFile(mCurrentPhotoFile);
					img_path = mCurrentPhotoFile.getAbsolutePath();
				} else {
					Uri uri 						= data.getData();
					String[] proj 					= { MediaStore.Images.Media.DATA };
					Cursor actualimagecursor 		= managedQuery(uri, proj, null, null, null);
					int actual_image_column_index 	= actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					actualimagecursor.moveToFirst();
					img_path 						= actualimagecursor.getString(actual_image_column_index);
				}
				photoFile = new File(img_path);
				try {
					cameraBitmap = ImageUtil.revitionImageSize2(img_path, SelfPhotosAndAvatarActivity.this, 480);
				} catch (IOException e) {
					MobclickAgent.reportError(SelfPhotosAndAvatarActivity.this, StringUtil.getExceptionInfo(e));
				}
				try {
					// 保存缩略图
					FileOutputStream out = null;
					// photoFile = new File(PHOTO_DIR, "photo" + userId +
					// ".jpg");
					// if (photoFile != null && photoFile.exists()) {
					// photoFile.delete();
					// }
					out = new FileOutputStream(photoFile, false);
					if (cameraBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)) {
						out.flush();
						out.close();
					}
					// if (mCurrentPhotoFile.exists())
					// mCurrentPhotoFile.delete();
				} catch (Exception e) {
					MobclickAgent.reportError(SelfPhotosAndAvatarActivity.this, StringUtil.getExceptionInfo(e));
				}
				// Uri dataUri = data.getData();
				if (NetUtil.checkNet(this)) {
					new UpdatePhotoTask("upload", photoFile).execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
				break;
			// case CAMERA_WITH_DATA:// 拍照
			// doCropPhoto(mCurrentPhotoFile);
			// break;
			case SETTING_AVATAR:// 设置头像；
				Bitmap avatarBitmap = data.getParcelableExtra("data");
				photoFile = new File(PHOTO_DIR, StringUtil.createSign(userId+"") + ".jpg");
				if (photoFile != null && photoFile.exists()) {
					photoFile.delete();
				}
				try {
					FileOutputStream out = null;
					out = new FileOutputStream(photoFile, false);
					if (avatarBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)) {
						out.flush();
						out.close();
					}
				} catch (Exception e) {
					MobclickAgent.reportError(SelfPhotosAndAvatarActivity.this, StringUtil.getExceptionInfo(e));
				}
				if (NetUtil.checkNet(this)) {
					new SetAvatarPhotoTask(photoFile).execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
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
			startActivityForResult(intent, SETTING_AVATAR);
		} catch (Exception e) {
			MobclickAgent.reportError(this, StringUtil.getExceptionInfo(e));
			Toast.makeText(this, "照片裁剪出错", Toast.LENGTH_SHORT).show();
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
		intent.putExtra("outputX", 300);
		intent.putExtra("outputY", 300);

		intent.putExtra("return-data", true);
		return intent;
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
						Toast.makeText(SelfPhotosAndAvatarActivity.this, "请检查SD卡是否正常", Toast.LENGTH_SHORT).show();
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
			Toast.makeText(this, "拍照出错", Toast.LENGTH_SHORT).show();
		}
	}

	public static Intent getTakePickIntent(File f) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		return intent;
	}

	/****************************** 关于上传照片的方法结束 ********************************/

	private boolean isRuning = false;// 标示此任务是否在运行中

	/**
	 * 个人照片显示的是适配器
	 * 
	 * @author syghh
	 * 
	 */
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
				convertView = getLayoutInflater().inflate(R.layout.self_photos_and_avatar_item, null);
				holder.ivPhoto = (ImageView) convertView.findViewById(R.id.ivPhoto);
				holder.ivIsAvatar = (ImageView) convertView.findViewById(R.id.ivIsAvatar);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final ViewHolder holderUse = holder;
			int itemWidth = (display.getWidth() - 4 * 10) / 3;
			android.view.ViewGroup.LayoutParams param = holder.ivPhoto.getLayoutParams();
			param.width = itemWidth;
			param.height = itemWidth;
			holder.ivPhoto.setLayoutParams(param);
			String thumbUrl = photoBean.getThumbUrl();
			holder.ivPhoto.setImageResource(R.drawable.bg_kangoo_photo_defualt);
			holder.ivPhoto.setTag(R.id.tag_personalphotobean, photoBean);
			holder.ivPhoto.setTag(thumbUrl);
			Drawable cacheDrawable = AsyncImageLoader.getInstance().loadDrawable(thumbUrl, new ImageCallback() {
				@Override
				public void imageLoaded(Drawable imageDrawable, String imageUrl) {
					ImageView ivImage = (ImageView) gvPersonalPhoto.findViewWithTag(imageUrl);
					if (ivImage != null) {
						if (imageDrawable != null) {
							ivImage.setTag(imageDrawable);
							PersonalPhotoAdapter.this.notifyDataSetChanged();
						}
					}
				}
			});
			if (cacheDrawable != null) {
				holder.ivPhoto.setImageDrawable(cacheDrawable);
			}

			if (isSelf) {
				// 比对user的头像与照片的url，一致则显示头像的标识
				// if (photoBean.getPhotoUrl().equals(
				// SharedPrefUtil.getUserBean(SelfPhotosAndAvatarActivity.this).getAvatarUrl()))
				// {
				// holder.ivIsAvatar.setVisibility(View.VISIBLE);
				// } else {
				// holder.ivIsAvatar.setVisibility(View.GONE);
				// }
				holder.ivIsAvatar.setVisibility(View.GONE);
				// 长按弹出对话框，选择设为头像或者删除
				holder.ivPhoto.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						LayoutInflater inflater = SelfPhotosAndAvatarActivity.this.getLayoutInflater();
						View view = inflater.inflate(R.layout.dialog_common_layout, null);
						TextView tvDialogMsg = (TextView) view.findViewById(R.id.tvDialogMsg);
						Button btnDialogLeft = (Button) view.findViewById(R.id.btnDialogLeft);
						Button btnDialogRight = (Button) view.findViewById(R.id.btnDialogRight);
						btnDialogLeft.setText("设为头像");
						btnDialogRight.setText("删除照片");
						tvDialogMsg.setText("请您选择下一步的操作");
						final Dialog dialog = new Dialog(SelfPhotosAndAvatarActivity.this, R.style.dialog);
						dialog.setContentView(view);
						dialog.show();
						// 设为我的头像
						btnDialogLeft.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								String url1 = photoBean.getPhotoUrl();
								File file = new File(PHOTO_DIR, StringUtil.createImageName(url1));
								doCropPhoto(file);
								// if
								// (NetUtil.checkNet(SelfPhotosAndAvatarActivity.this))
								// {
								//
								// new SetAvatarPhotoTask(userId, accessToken,
								// url1).execute();
								//
								// } else {
								// Toast.makeText(SelfPhotosAndAvatarActivity.this,
								// R.string.NoSignalException,
								// Toast.LENGTH_SHORT);
								// }
								dialog.dismiss();
							}
						});
						// 删除这个照片
						btnDialogRight.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (NetUtil.checkNet(SelfPhotosAndAvatarActivity.this)) {
									new UpdatePhotoTask("del", photoBean.getPhotoId(), delPosition).execute();
								} else {
									Toast.makeText(SelfPhotosAndAvatarActivity.this, R.string.NoSignalException,
											Toast.LENGTH_SHORT);
								}
								dialog.dismiss();
							}
						});
						return true;
					}
				});
			}
			// 点击进入大图
			holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(SelfPhotosAndAvatarActivity.this, KoalaRooPhotoShowActivity.class);
					intent.putExtra("photoId", photoBean.getPhotoId());
					intent.putExtra("photoBeans", photoBeans);
					intent.putExtra("isSelf", isSelf);
					startActivity(intent);
				}
			});
			return convertView;
		}

	}

	class ViewHolder {
		private ImageView ivPhoto;
		private ImageView ivIsAvatar;
	}

	/**
	 * 获取个人照片
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
				pd = new ProgressDialog(SelfPhotosAndAvatarActivity.this);
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
						tvShowNo.setVisibility(View.GONE);
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
						pageNo = pageNo + 1;
					} else {
						// Toast.makeText(SelfPhotosAndAvatarActivity.this,
						// result.getString("error"),
						// Toast.LENGTH_SHORT).show();
						tvShowNo.setVisibility(View.VISIBLE);
						tvFooterMore.setVisibility(View.GONE);
					}
				} catch (JSONException e) {
					Toast.makeText(SelfPhotosAndAvatarActivity.this, "读图错误", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(SelfPhotosAndAvatarActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
			isRuning = false;
		}
	}

	/**
	 * 处理照片接口（上传或删除图片）； type为“upload”为上传照片；type为“del”为删除照片；
	 * 
	 * @author Zhoujun
	 * 
	 */
	class UpdatePhotoTask extends AsyncTask<Void, Void, JSONObject> {

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
					pd = new ProgressDialog(SelfPhotosAndAvatarActivity.this);
				}
				pd.setMessage("上传照片中...");
				pd.show();
			}
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			String token = SharedPrefUtil.getUserBean(SelfPhotosAndAvatarActivity.this).getAccessToken();
			JSONObject obj = null;
			try {
				if (type.equals("upload")) {
					long userId = SharedPrefUtil.getUserBean(SelfPhotosAndAvatarActivity.this).getUserId();
					obj = new BusinessHelper().uploadPersonalPhoto(userId, file, token);
				} else if (type.equals("del")) {
					obj = new BusinessHelper().delPersonalPhoto(photoId, token);
				}
			} catch (Exception e) {
				e.printStackTrace();
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
						Toast.makeText(SelfPhotosAndAvatarActivity.this, successText, Toast.LENGTH_SHORT).show();
						if (type.equals("upload")) {
							PersonalPhotoBean bean = new PersonalPhotoBean(result.getJSONArray("data").getJSONObject(0));
							photoBeans.add(0, bean);
							adapter.notifyDataSetChanged();
							if (result.getInt("current") == result.getInt("total")) {
								tvFooterMore.setText("全部加载完毕");
							}
						} else if (type.equals("del")) {
							adapter.remove(position);
						}
						// 及时更改 表示是否有照片数据 的组件的显示状态
						if (adapter.getCount() < 1) {
							tvShowNo.setVisibility(View.VISIBLE);
						} else {
							tvShowNo.setVisibility(View.GONE);
						}

					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(SelfPhotosAndAvatarActivity.this, R.string.time_out, Toast.LENGTH_SHORT).show();
						startActivity(new Intent(SelfPhotosAndAvatarActivity.this, LoginActivity.class).putExtra(
								"back", "back"));
					} else {
						Toast.makeText(SelfPhotosAndAvatarActivity.this, failText, Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			} else {
				Toast.makeText(SelfPhotosAndAvatarActivity.this, failText, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 将某照片设置为头像的Task
	 * 
	 * @author SongYuan
	 * 
	 */
	class SetAvatarPhotoTask extends AsyncTask<Void, Void, JSONObject> {

		private File avatarFile;

		public SetAvatarPhotoTask(File avatarFile) {
			super();
			this.avatarFile = avatarFile;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(SelfPhotosAndAvatarActivity.this);
			}
			pd.setMessage("正在设置头像...");
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			UserBean user = SharedPrefUtil.getUserBean(SelfPhotosAndAvatarActivity.this);
			String nickname = user.getNickname();
			String birthday = user.getBirthday();
			String gender = user.getGender();
			try {
				return new BusinessHelper().profileUpload(nickname, birthday, gender, avatarFile, userId, accessToken);
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if(pd != null){
				pd.dismiss();
			}
			if (result != null) {
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						// 设置头像成功后，将新的url set到userbean中并保存，并刷新适配器
						// UserBean userBean =
						// SharedPrefUtil.getUserBean(SelfPhotosAndAvatarActivity.this);
						// userBean.setAvatarUrl(photoUrl);
						// SharedPrefUtil.setUserBean(SelfPhotosAndAvatarActivity.this,
						// userBean);
						Toast.makeText(SelfPhotosAndAvatarActivity.this, "设置头像成功", Toast.LENGTH_SHORT)
						.show();
					}else if(status == Constants.TOKEN_FAILED){
						Toast.makeText(SelfPhotosAndAvatarActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
						startActivity(new Intent(SelfPhotosAndAvatarActivity.this, LoginActivity.class).putExtra("back", "back"));
					}else {
						Toast.makeText(SelfPhotosAndAvatarActivity.this, result.getString("error"), Toast.LENGTH_SHORT)
								.show();
					}
				} catch (JSONException e) {
					Toast.makeText(SelfPhotosAndAvatarActivity.this, "设置失败", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(SelfPhotosAndAvatarActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
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

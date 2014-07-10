package com.elephant.ediyou.activity;

import java.io.File;
import java.io.FileOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.AsyncImageLoader;
import com.elephant.ediyou.AsyncImageLoader.ImageCallback;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 资料页面（考拉）
 * 
 * @author syghh
 * 
 */
public class KoalaProfileActivity extends TabActivity implements IBaseActivity, OnClickListener,
		OnCheckedChangeListener {
	// 标题；
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	private ProgressDialog pd;
	// 头像附近的信息；
	private ImageView ivBg;// 头像后面背景图片；
	private ImageView ivPortrait;
	private TextView tvEmail;
	private TextView tvName;
	private EditText etName;
	private ImageView ivGender;
	private TextView tvAge;
	private TextView tvLevel;
	private TextView tvBadge;
	private ImageView ivBlackList;
	private Button btnNameEdit;
	// tab标题；
	private final static String TAB_INFO_TAG = "信息";
	private final static String TAB_EVENT_TAG = "活动";
	private final static String TAB_PHOTO_TAG = "照片";
	private RadioButton btnInfo;
	private RadioButton btnEvent;
	private RadioButton btnPhoto;

	private boolean isSelf = false;// 是否是查看自己页面；
	private boolean isEditName = false;
	private long koalaId;
	private InputMethodManager inputManager;

	/* 拍照的照片存储位置 */
	private File PHOTO_DIR;
	private File mCurrentPhotoFile;// 照相机拍照得到的图片，临时文件
	private File avatarFile;// 头像文件
	private File bgFile;// 背景文件
	/* 用来标识请求照相功能的activity */
	public static final int CAMERA_WITH_DATA = 3023;
	/* 用来标识请求gallery的activity */
	public static final int PHOTO_PICKED_WITH_DATA = 3021;
	private static String type = "portrait";// 换头像还是背景，portrait为头像，bg为背景

	private ScrollView myScrollView;
	private TabHost tabHost;
	private RadioGroup radiogp_main;
	private CommonApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.koala_profile);
		koalaId = getIntent().getLongExtra("koalaId", 0);
		if (koalaId == SharedPrefUtil.getUserBean(this).getUserId()) {
			isSelf = true;
		}
		app = (CommonApplication) getApplication();
		app.addActivity(this);
		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		findView();
		fillData();
		createPhotoDir();
	}

	@Override
	public void findView() {
		btnLeft = (Button) findViewById(R.id.btnLeft);
		btnRight = (Button) findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);

		ivBg = (ImageView) findViewById(R.id.ivBg);
		ivPortrait = (ImageView) findViewById(R.id.ivPortrait);
		tvEmail = (TextView) findViewById(R.id.tvEmail);
		tvName = (TextView) findViewById(R.id.tvName);
		etName = (EditText) findViewById(R.id.etName);
		ivGender = (ImageView) findViewById(R.id.ivGender);
		tvAge = (TextView) findViewById(R.id.tvAge);
		tvLevel = (TextView) findViewById(R.id.tvLevel);
		tvBadge = (TextView) findViewById(R.id.tvBadge);
		btnNameEdit = (Button) findViewById(R.id.btnNameEdit);
		btnNameEdit.setOnClickListener(this);

		ivBlackList = (ImageView) findViewById(R.id.ivBlackList);
		ivBlackList.setOnClickListener(this);

		btnLeft.setOnClickListener(this);

		myScrollView = (ScrollView) findViewById(R.id.myScrollView);
		tabHost = this.getTabHost();
		radiogp_main = (RadioGroup) findViewById(R.id.radiogp_main);
		radiogp_main.setOnCheckedChangeListener(this);
		btnInfo = (RadioButton) findViewById(R.id.radio_info);
		btnEvent = (RadioButton) findViewById(R.id.radio_event);
		btnPhoto = (RadioButton) findViewById(R.id.radio_photo);
		btnInfo.setChecked(true);
	}

	@Override
	public void fillData() {
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		tvTitle.setText(R.string.koala_profile);
		if (!isSelf) {
			ivBlackList.setVisibility(View.VISIBLE);
			btnNameEdit.setVisibility(View.GONE);
		} else {
			ivPortrait.setOnClickListener(this);
			ivBg.setOnClickListener(this);
		}

		Intent infoIntent = new Intent(this, KoalaInfoActivity.class);
		infoIntent.putExtra("id", koalaId);
		tabHost.addTab(tabHost.newTabSpec(TAB_INFO_TAG).setIndicator("信息").setContent((infoIntent)));

		EventListActivity.addTabTo(this, koalaId, "Koala", tabHost, TAB_EVENT_TAG);
		Intent photoIntent = new Intent(this, KoalaRooPhotoActivity.class);
		photoIntent.putExtra("id", koalaId);
		tabHost.addTab(tabHost.newTabSpec(TAB_PHOTO_TAG).setIndicator("照片").setContent(photoIntent));
		if (NetUtil.checkNet(this)) {
			new GetKoalaInfoTask(koalaId, KoalaProfileActivity.this).execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.ivPortrait:
			type = "portrait";
			createUploadIconDialog();
			break;
		case R.id.ivBg:
			type = "bg";
			createUploadIconDialog();
			break;
		case R.id.ivBlackList:
			showBlackDialog();
			break;
		case R.id.btnNameEdit:
			if (!isEditName) {
				tvName.setVisibility(View.GONE);
				etName.setVisibility(View.VISIBLE);
				etName.setText(tvName.getText().toString());
				isEditName = true;
				btnNameEdit.setBackgroundResource(R.drawable.btn_edit_submit_selector);
			} else {
				tvName.setVisibility(View.VISIBLE);
				etName.setVisibility(View.GONE);
				
				isEditName = false;
				btnNameEdit.setBackgroundResource(R.drawable.btn_edit_selector);
				inputManager.hideSoftInputFromWindow(btnNameEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);// 隐藏键盘
				if(TextUtils.isEmpty(etName.getText().toString())){
					Toast.makeText(this, "请填写昵称", Toast.LENGTH_SHORT).show();
					return;
				} else if (StringUtil.countStringLength(etName.getText().toString(), 10)){
					Toast.makeText(this, "昵称长度不能超过10个字哦", Toast.LENGTH_SHORT).show();
					return;
				}
				tvName.setText(etName.getText().toString());
				if (NetUtil.checkNet(this)) {
					new UpdatePersonalInfoTask("name").execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 创建头像存储目录
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

	/**
	 * 上传头像
	 * 
	 * @param v
	 */
	private void createUploadIconDialog() {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		if (type.equals("portrait")) {
			ab.setTitle("选择头像");
		} else if (type.equals("bg")) {
			ab.setTitle("选择背景");
		}
		ab.setItems(new String[] { "相机拍摄", "手机相册", "取消" }, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch (which) {
				case 0:// 相机拍摄
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
						doTakePhoto();// 用户点击了从照相机获取
					} else {
						Toast.makeText(KoalaProfileActivity.this, "请检查SD卡是否正常", Toast.LENGTH_SHORT).show();
					}
					break;
				case 1:// 手机相册
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("image/*");
					intent.putExtra("crop", "true");
					if (type.equals("portrait")) {
						intent.putExtra("aspectX", 1);
						intent.putExtra("aspectY", 1);
						intent.putExtra("outputX", 200);
						intent.putExtra("outputY", 200);
					} else if (type.equals("bg")) {
						intent.putExtra("aspectX", 5);
						intent.putExtra("aspectY", 3);
						intent.putExtra("outputX", 600);
						intent.putExtra("outputY", 200);
					}
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PHOTO_PICKED_WITH_DATA:// 相册
				Bitmap cameraBitmap = data.getParcelableExtra("data");
				if (cameraBitmap == null) {
					Uri dataUri = data.getData();
					Intent intent = getCropImageIntent(dataUri);
					startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
				}
				if (type.equals("portrait")) {
					ivPortrait.setImageBitmap(cameraBitmap);
					try {
						// 保存缩略图
						FileOutputStream out = null;
						File file = new File(PHOTO_DIR, ImageUtil.createAvatarFileName(String.valueOf(koalaId)));
						if (file != null && file.exists()) {
							file.delete();
						}
						avatarFile = new File(PHOTO_DIR, ImageUtil.createAvatarFileName(String.valueOf(koalaId)));
						out = new FileOutputStream(avatarFile, false);

						if (cameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
							out.flush();
							out.close();
						}
						if (mCurrentPhotoFile.exists())
							mCurrentPhotoFile.delete();
					} catch (Exception e) {
						MobclickAgent.reportError(KoalaProfileActivity.this, StringUtil.getExceptionInfo(e));
					}
					String birthday = SharedPrefUtil.getUserBean(this).getBirthday();
					String gender = SharedPrefUtil.getUserBean(this).getGender();
					String accessToken = SharedPrefUtil.getUserBean(this).getAccessToken();
					if (NetUtil.checkNet(KoalaProfileActivity.this)) {
						new profileInfoAddTask(tvName.getText().toString(), birthday, gender, avatarFile, koalaId,
								accessToken).execute();
					} else {
						Toast.makeText(KoalaProfileActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT)
								.show();
					}

				} else if (type.equals("bg")) {
					ivBg.setImageBitmap(cameraBitmap);
					try {
						// 保存缩略图
						FileOutputStream out = null;
						File file = new File(PHOTO_DIR, "bg" + ImageUtil.createAvatarFileName(String.valueOf(koalaId)));
						if (file != null && file.exists()) {
							file.delete();
						}
						bgFile = new File(PHOTO_DIR, "bg" + ImageUtil.createAvatarFileName(String.valueOf(koalaId)));
						out = new FileOutputStream(bgFile, false);

						if (cameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
							out.flush();
							out.close();
						}
						if (mCurrentPhotoFile.exists())
							mCurrentPhotoFile.delete();
					} catch (Exception e) {
						MobclickAgent.reportError(KoalaProfileActivity.this, StringUtil.getExceptionInfo(e));
					}
					long userId = SharedPrefUtil.getUserBean(this).getUserId();
					if (bgFile != null && bgFile.exists()) {
						new profileInfoAddTask(userId, bgFile).execute();
					}
				}

				break;
			case CAMERA_WITH_DATA:// 拍照
				doCropPhoto(mCurrentPhotoFile);
				break;
			}
		}
	}

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
			MobclickAgent.reportError(KoalaProfileActivity.this, StringUtil.getExceptionInfo(e));
			Toast.makeText(this, "照片裁剪出错", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 调用图片剪辑程序
	 */
	public static Intent getCropImageIntent(Uri photoUri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		if (type.equals("portrait")) {
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 200);
			intent.putExtra("outputY", 200);
		} else if (type.equals("bg")) {
			intent.putExtra("aspectX", 5);
			intent.putExtra("aspectY", 3);
			intent.putExtra("outputX", 600);
			intent.putExtra("outputY", 200);
		}
		intent.putExtra("return-data", true);
		return intent;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.radio_info:
			myScrollView.smoothScrollTo(0, 20);

			btnInfo.setTextColor(getResources().getColor(R.color.text_green));
			btnEvent.setTextColor(getResources().getColor(R.color.text_grey));
			btnPhoto.setTextColor(getResources().getColor(R.color.text_grey));
			tabHost.setCurrentTabByTag(TAB_INFO_TAG);
			break;
		case R.id.radio_event:
			myScrollView.smoothScrollTo(0, 20);

			btnInfo.setTextColor(getResources().getColor(R.color.text_grey));
			btnEvent.setTextColor(getResources().getColor(R.color.text_green));
			btnEvent.setTextColor(getResources().getColor(R.color.text_green));
			tabHost.setCurrentTabByTag(TAB_EVENT_TAG);
			break;
		case R.id.radio_photo:
			myScrollView.smoothScrollTo(0, 20);

			btnInfo.setTextColor(getResources().getColor(R.color.text_grey));
			btnEvent.setTextColor(getResources().getColor(R.color.text_grey));
			btnPhoto.setTextColor(getResources().getColor(R.color.text_green));
			tabHost.setCurrentTabByTag(TAB_PHOTO_TAG);
			break;
		default:
			break;
		}
	}

	private class GetKoalaInfoTask extends AsyncTask<Void, Void, JSONObject> {
		private long id;
		private Context context;

		public GetKoalaInfoTask(long id, Context context) {
			super();
			this.id = id;
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(context);
				pd.setMessage("正在获取信息...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject koalaInfo = null;
			try {
				koalaInfo = new BusinessHelper().getKoalaInfo(id);
			} catch (Exception e) {
				MobclickAgent.reportError(context, StringUtil.getExceptionInfo(e));
			}
			return koalaInfo;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (pd != null)
				pd.dismiss();
			if (result != null) {
				try {
					if (Constants.SUCCESS == result.getInt("status")) {
						JSONObject dataJson = result.getJSONObject("data");
						String avatarUrl = dataJson.getString("avatarUrl");
						ivPortrait.setTag(avatarUrl);
						Drawable cacheDrawable = AsyncImageLoader.getInstance().loadDrawable(avatarUrl,
								new ImageCallback() {
									@Override
									public void imageLoaded(Drawable imageDrawable, String imageUrl) {
										ImageView ivImage = (ImageView) ivPortrait.findViewWithTag(imageUrl);
										if (ivImage != null) {
											if (imageDrawable != null) {
												ivImage.setImageDrawable(imageDrawable);
											}
										}
									}
								});
						if (cacheDrawable != null) {
							ivPortrait.setImageDrawable(cacheDrawable);
						}
						String bgUrl = dataJson.getString("bgUrl");
						if (bgUrl != null && !bgUrl.equals("")) {
							ivBg.setTag(bgUrl);
							Drawable bgDrawable = AsyncImageLoader.getInstance().loadDrawable(bgUrl,
									new ImageCallback() {
										@Override
										public void imageLoaded(Drawable imageDrawable, String imageUrl) {
											ImageView ivImage = (ImageView) ivBg.findViewWithTag(imageUrl);
											if (ivImage != null) {
												if (imageDrawable != null) {
													ivImage.setImageDrawable(imageDrawable);
												}
											}
										}
									});
							if (bgDrawable != null) {
								ivBg.setImageDrawable(bgDrawable);
							}
						}
						tvEmail.setText(dataJson.getString("loginName"));
						tvName.setText(dataJson.getString("nickName"));
						if ("f".equals(dataJson.getString("gender"))) {
							ivGender.setImageResource(R.drawable.ic_fale);
						} else {
							ivGender.setImageResource(R.drawable.ic_male);
						}
						tvAge.setText(dataJson.getString("age") + "岁");
						tvLevel.setText("Lv" + dataJson.getString("cowraLevel"));
						tvBadge.setText(dataJson.getString("kTitle"));
						btnInfo.setChecked(true);
						myScrollView.smoothScrollTo(0, 20);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(context, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 更新个人信息；
	 * @author Zhoujun
	 *
	 */
	private class UpdatePersonalInfoTask extends AsyncTask<Void, Void, JSONObject> {
		private String type;

		public UpdatePersonalInfoTask(String type) {
			super();
			this.type = type;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject update = null;
			long userId = SharedPrefUtil.getUserBean(KoalaProfileActivity.this).getUserId();
			String accessToken = SharedPrefUtil.getUserBean(KoalaProfileActivity.this).getAccessToken();
			try {
				if (type.equals("name")) {
					String name = tvName.getText().toString();
					String birthday = SharedPrefUtil.getUserBean(KoalaProfileActivity.this).getBirthday();
					String gender = SharedPrefUtil.getUserBean(KoalaProfileActivity.this).getGender();
					update = new BusinessHelper().profileUpdate(name, birthday, gender, userId, accessToken);
				}
			} catch (Exception e) {
			}

			return update;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					if (result.getInt("status") == Constants.SUCCESS) {

					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(KoalaProfileActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(KoalaProfileActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(KoalaProfileActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
				}
			} else {
				Toast.makeText(KoalaProfileActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
			}
		}  
	}

	/**
	 * 上传头像；
	 * 
	 * @author Zhoujun
	 * 
	 */
	class profileInfoAddTask extends AsyncTask<Void, Void, JSONObject> {

		private String nickname;
		private String birthday;
		private String gender;
		private File avatarFile;
		private long userId;
		private String accessToken;
		private File file;

		public profileInfoAddTask(long userId, File file) {
			super();
			this.userId = userId;
			this.file = file;
		}

		public profileInfoAddTask(String nickname, String birthday, String gender, File avatarFile, long userId,
				String accessToken) {
			this.nickname = nickname;
			this.birthday = birthday;
			this.gender = gender;
			this.avatarFile = avatarFile;
			this.userId = userId;
			this.accessToken = accessToken;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				if (type.equals("portrait")) {
					if (avatarFile != null && avatarFile.exists()) {
						return new BusinessHelper().profileUpload(nickname, birthday, gender, avatarFile, userId,
								accessToken);
					} else {
						return new BusinessHelper().profileUpdate(nickname, birthday, gender, userId,accessToken);
					}
				} else if (type.equals("bg")) {
					return new BusinessHelper().updateBg(userId, file);
				}
			} catch (SystemException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (pd != null)
				pd.dismiss();
			if (result != null) {
				if (type.equals("portrait")) {
					try {
						if (result.getInt("status") == Constants.SUCCESS) {

						} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
							Toast.makeText(KoalaProfileActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
							startActivity(new Intent(KoalaProfileActivity.this, LoginActivity.class).putExtra("back", "back"));
						} else {
							Toast.makeText(KoalaProfileActivity.this, result.getString("error"), Toast.LENGTH_LONG)
									.show();
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			} else {
				Toast.makeText(KoalaProfileActivity.this, "上传头像失败失败", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 拉黑Dialog；
	 */
	private void showBlackDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("您确认要拉黑吗?");
		builder.setTitle("温馨提示");
		builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});

		builder.setNegativeButton("确认", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				new BlackTask().execute();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	/**
	 * 拉黑
	 * 
	 * @author Zhoujun
	 * 
	 */
	private class BlackTask extends AsyncTask<Void, Void, JSONObject> {
		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject update = null;
			long userId = SharedPrefUtil.getUserBean(KoalaProfileActivity.this).getUserId();
			String accessToken = SharedPrefUtil.getUserBean(KoalaProfileActivity.this).getAccessToken();
			try {
				update = new BusinessHelper().black(accessToken, userId, koalaId);
			} catch (Exception e) {
			}

			return update;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					if (result.getInt("status") == Constants.SUCCESS) {
						Toast.makeText(KoalaProfileActivity.this, result.getString("data"), Toast.LENGTH_SHORT).show();
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(KoalaProfileActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
						startActivity(new Intent(KoalaProfileActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(KoalaProfileActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
				}
			} else {
				Toast.makeText(KoalaProfileActivity.this, "拉黑失败", Toast.LENGTH_SHORT).show();
			}
		}
	}
}

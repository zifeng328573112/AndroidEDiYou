package com.elephant.ediyou.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.StringUtil;
import com.elephant.ediyou.view.GridViewInScrollView;
import com.umeng.analytics.MobclickAgent;

public class RooRecommentNewActivity extends Activity implements IBaseActivity, OnClickListener {
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	private ProgressDialog pd;

	private LinearLayout llWarnAddNewPhoto;
	private ImageView ivNewPhoto;
	private EditText edtTitleName;
	private EditText edtContent;
	private Button btnNewRecommend;
	private GridViewInScrollView gvReCommendPhoto;// 添加推荐图片的显示

	private File coverFile;// 封面图片的文件
	private File albumFile;// 封面图片的文件
	/* 拍照的照片存储位置 */
	private File PHOTO_DIR;
	private File mCurrentPhotoFile;// 照相机拍照得到的图片，临时文件
	/* 用来标识请求照相功能的activity */
	public static final int CAMERA_WITH_DATA = 3023;
	/* 用来标识请求gallery的activity */
	public static final int PHOTO_PICKED_WITH_DATA = 3021;
	private static int photoType;// 设定是封面图取图或相册取图
	private final static int COVER_PHOTO = 1;// 封面图取图
	private final static int ALBUM_PHOTO = 2;// 相册取图

	private AddRecommendPhotoAdapter addRecommendPhotoAdapter;
	private ArrayList<Bitmap> newPhotoList = new ArrayList<Bitmap>();

	private long rooId;// 袋鼠id
	private long recomId;// 新建的推荐的id
	private Display display;

	private boolean isUpdatePhoto = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roo_recommend_new);
		display = this.getWindowManager().getDefaultDisplay();
		if (getIntent() != null) {
			rooId = getIntent().getLongExtra("rooId", 0);

		}
		rooId = 1;
		createPhotoDir();
		findView();
		fillData();
		//将Activity添加到容器
		((CommonApplication) getApplicationContext()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setBackgroundResource(R.drawable.ic_submit_selector);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("新建推荐");

		llWarnAddNewPhoto = (LinearLayout) this.findViewById(R.id.llWarnAddNewPhoto);
		ivNewPhoto = (ImageView) this.findViewById(R.id.ivNewPhoto);
		edtTitleName = (EditText) this.findViewById(R.id.edtTitleName);
		edtContent = (EditText) this.findViewById(R.id.edtContent);
		btnNewRecommend = (Button) this.findViewById(R.id.btnNewRecommend);
		llWarnAddNewPhoto.setOnClickListener(this);
		btnNewRecommend.setOnClickListener(this);

		gvReCommendPhoto = (GridViewInScrollView) this.findViewById(R.id.gvReCommendPhoto);

	}

	@Override
	public void fillData() {
		Bitmap bitmap = ImageUtil.getBitMapByRes(this, R.drawable.ic_add_photo);
		newPhotoList.add(0, bitmap);
		addRecommendPhotoAdapter = new AddRecommendPhotoAdapter();
		gvReCommendPhoto.setAdapter(addRecommendPhotoAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnRight:// 提交
			
			break;
		case R.id.llWarnAddNewPhoto:// 添加封面图
			photoType = COVER_PHOTO;
			createUploadIconDialog();
			break;
		case R.id.btnNewRecommend:// 提交推荐的基本信息
			String titleName = edtTitleName.getText().toString().trim();
			String content = edtContent.getText().toString().trim();
			if (coverFile == null || !coverFile.exists()) {
				Toast.makeText(this, "请您添加推荐主题图", Toast.LENGTH_LONG).show();
			} else {
				if (StringUtil.isBlank(titleName) || StringUtil.isBlank(content)) {
					Toast.makeText(this, "请您添加完整的创建信息", Toast.LENGTH_LONG).show();
				} else {
					if (NetUtil.checkNet(this)) {
						new KangarooInsertRecomTask(rooId, titleName, content, coverFile).execute();
					} else {
						Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
					}
				}
			}
			break;

		}
	}

	/**
	 * 创建图片目录
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
		ab.setTitle("选择头像");
		ab.setItems(new String[] { "相机拍摄", "手机相册", "取消" }, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch (which) {
				case 0:// 相机拍摄
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
						doTakePhoto();// 用户点击了从照相机获取
					} else {
						Toast.makeText(RooRecommentNewActivity.this, "请检查SD卡是否正常", Toast.LENGTH_SHORT).show();
					}
					break;
				case 1:// 手机相册
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("image/*");
					if (photoType == COVER_PHOTO) {
						intent.putExtra("crop", "true");
						intent.putExtra("aspectX", 3);
						intent.putExtra("aspectY", 1);
						intent.putExtra("outputX", 600);
						intent.putExtra("outputY", 200);
						intent.putExtra("return-data", true);
					} else if (photoType == ALBUM_PHOTO) {
						intent.putExtra("crop", "true");
						intent.putExtra("aspectX", 1);
						intent.putExtra("aspectY", 1);
						intent.putExtra("outputX", 400);
						intent.putExtra("outputY", 300);
						intent.putExtra("return-data", true);
					}
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
			// Launch camera to take photo for selected contact
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

				if (photoType == COVER_PHOTO) {
					ivNewPhoto.setVisibility(View.VISIBLE);
					ivNewPhoto.setImageBitmap(cameraBitmap);
					llWarnAddNewPhoto.setVisibility(View.GONE);
				} else if (photoType == ALBUM_PHOTO) {
					if (NetUtil.checkNet(this)) {
						new KangarooUpLoadRecomPicTask(recomId, albumFile).execute();
					} else {
						Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
					}
					newPhotoList.add(cameraBitmap);
					if (newPhotoList.size() > 0) {
						addRecommendPhotoAdapter.notifyDataSetChanged();
					}
				}

				try {
					// 保存缩略图
					FileOutputStream out = null;
					if (photoType == COVER_PHOTO) {
						File coverPhotoFile = new File(PHOTO_DIR, "RecommendCover" + ImageUtil.getPhotoFileName());
						if (coverPhotoFile != null && coverPhotoFile.exists()) {
							coverPhotoFile.delete();
						}
						coverFile = new File(PHOTO_DIR, "RecommendCover" + ImageUtil.getPhotoFileName());
						out = new FileOutputStream(coverFile, false);
					} else if (photoType == ALBUM_PHOTO) {
						File albumPhotoFile = new File(PHOTO_DIR, "RecommendAlbum" + ImageUtil.getPhotoFileName());
						if (albumPhotoFile != null && albumPhotoFile.exists()) {
							albumPhotoFile.delete();
						}
						albumFile = new File(PHOTO_DIR, "RecommendAlbum" + ImageUtil.getPhotoFileName());
						out = new FileOutputStream(albumFile, false);
					}

					if (cameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
						out.flush();
						out.close();
					}
					if (mCurrentPhotoFile.exists())
						mCurrentPhotoFile.delete();
				} catch (Exception e) {
					MobclickAgent.reportError(RooRecommentNewActivity.this, StringUtil.getExceptionInfo(e));
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
			MobclickAgent.reportError(RooRecommentNewActivity.this, StringUtil.getExceptionInfo(e));
			Toast.makeText(this, "照片裁剪出错", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 调用图片剪辑程序
	 */
	public static Intent getCropImageIntent(Uri photoUri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		if (photoType == COVER_PHOTO) {
			intent.putExtra("crop", "true");
			intent.putExtra("aspectX", 3);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 600);
			intent.putExtra("outputY", 200);
			intent.putExtra("return-data", true);
		} else if (photoType == ALBUM_PHOTO) {
			intent.putExtra("crop", "true");
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 400);
			intent.putExtra("outputY", 300);
			intent.putExtra("return-data", true);
		}

		return intent;

	}

	/**
	 * 添加的推荐图片的adapter
	 * 
	 * @author syghh
	 * 
	 */
	private class AddRecommendPhotoAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return newPhotoList.size();
		}

		@Override
		public Object getItem(int position) {
			return newPhotoList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void remove(int position) {
			newPhotoList.remove(position);
			this.notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Bitmap photoBitmap = newPhotoList.get(position);
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = getLayoutInflater().inflate(R.layout.new_recommend_photo_item, null);
				holder.ivPhoto = (ImageView) convertView.findViewById(R.id.ivPhoto);
				holder.viewAddPhoto = convertView.findViewById(R.id.viewAddPhoto);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			int itemWidth = (display.getWidth() - 5 * 5) / 4;
			android.view.ViewGroup.LayoutParams param = holder.ivPhoto.getLayoutParams();
			param.width = itemWidth;
			param.height = itemWidth;

			if (position == 0) {
				holder.viewAddPhoto.setVisibility(View.VISIBLE);
				holder.viewAddPhoto.setLayoutParams(param);
				holder.ivPhoto.setVisibility(View.GONE);
				holder.viewAddPhoto.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (isUpdatePhoto == true) {
							photoType = ALBUM_PHOTO;
							createUploadIconDialog();
						} else {
							Toast.makeText(RooRecommentNewActivity.this, "请先“创建”新的推荐基本信息", Toast.LENGTH_LONG).show();
						}
					}
				});
			} else {
				holder.viewAddPhoto.setVisibility(View.GONE);
				holder.ivPhoto.setVisibility(View.VISIBLE);
				holder.ivPhoto.setLayoutParams(param);
				holder.ivPhoto.setImageBitmap(photoBitmap);
			}

			return convertView;
		}

	}

	class ViewHolder {
		private ImageView ivPhoto;
		private View viewAddPhoto;
	}

	/**
	 * 新建推荐（封面图、名字、描述）
	 * 
	 * @author syghh
	 * 
	 */
	class KangarooInsertRecomTask extends AsyncTask<Void, Void, JSONObject> {

		private long kangarooId;
		private String title;
		private String content;
		private File coverFile;

		public KangarooInsertRecomTask(long kangarooId, String title, String content, File coverFile) {
			this.kangarooId = kangarooId;
			this.title = title;
			this.content = content;
			this.coverFile = coverFile;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RooRecommentNewActivity.this);
				pd.setMessage("新建推荐基本信息，操作完成后请添加推荐相册...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().kangarooInsertRecom(kangarooId, title, content, coverFile);
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
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						edtTitleName.setClickable(false);
						edtTitleName.setClickable(false);
						recomId = result.getLong("data");
						isUpdatePhoto = true;
						Toast.makeText(RooRecommentNewActivity.this, "创建成功", Toast.LENGTH_LONG).show();
					} else if (status == Constants.FAILED) {
						Toast.makeText(RooRecommentNewActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(RooRecommentNewActivity.this, "创建失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(RooRecommentNewActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 新建推荐上传图片
	 * 
	 * @author syghh
	 * 
	 */
	class KangarooUpLoadRecomPicTask extends AsyncTask<Void, Void, JSONObject> {

		private long recomId;
		private File coverFile;

		public KangarooUpLoadRecomPicTask(long recomId, File coverFile) {
			this.recomId = recomId;
			this.coverFile = coverFile;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RooRecommentNewActivity.this);
				pd.setMessage("图片上传中...");
			}
			pd.setMessage("图片上传中...");
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().kangarooUpLoadRecomPic(recomId, coverFile);
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
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {

						Toast.makeText(RooRecommentNewActivity.this, "上传成功", Toast.LENGTH_LONG).show();
					} else if (status == Constants.FAILED) {
						Toast.makeText(RooRecommentNewActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(RooRecommentNewActivity.this, "上传失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(RooRecommentNewActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

}

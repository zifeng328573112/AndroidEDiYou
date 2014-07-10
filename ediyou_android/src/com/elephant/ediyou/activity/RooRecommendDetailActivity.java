package com.elephant.ediyou.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Date;
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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.elephant.ediyou.bean.RooRecommentBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.AndroidUtil;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.StringUtil;
import com.elephant.ediyou.view.GridViewInScrollView;
import com.umeng.analytics.MobclickAgent;

/**
 * 袋鼠推荐详情
 * 
 * @author syghh
 * 
 */
public class RooRecommendDetailActivity extends Activity implements IBaseActivity, OnClickListener {
	// title
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	// 加载数据的progress
	private ProgressDialog pd;

	private ArrayList<RooRecommentBean> recommentBeans = new ArrayList<RooRecommentBean>();
	private ArrayList<RooRecommentBean> recommentBeanAlls;

	float density;

	int scroll_height;
	int itemWidth;// 每列宽度
	int pageNo = 1;// 当前页数
	int totalPage = -1;// 总页数
	int pageSize = 10;// 每页数量

	private final static int columnCount = 2;// 一共有多少列
	private int columnSpace = 10;// 列宽的间隙大小

	public static Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
	public static int maxSize = 10;

	private RooRecommentBean rooRecommentBean;// 获取的推荐详情封面、名称、描述等的对象
	private LinearLayout llTitle;
	private ImageView ivRooRecommentDCover;// 详情封面
	private TextView tvRooRecommentDTitleName;// 推荐名称
	private TextView tvRooRecommentDContent;// 推荐描述
	private EditText edtDTitleName;// 编辑：推荐名称
	private EditText edtDContent;// 编辑：推荐描述
	private Button btnEditDetail;// 编辑
	private boolean isEdit = false;// 是否为编辑状态

	private String updateData;// 修改推荐的数据（title和content // CoverPhoto）
	private static final String PHOTO = "data_photo"; // CoverPhoto
	private static final String STR = "data_str"; // title和content

	private long recomId;// 推荐的id
	private int screenWidth;// 屏幕宽度

	private ProgressBar pbFooter;
	private TextView tvFooterMore;

	private GridViewInScrollView gvPhoto;
	private boolean isSelf ;
	private RecommendPhotoAdapter recommendPhotoAdapter;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roo_recommend_detail);
		if (getIntent() != null) {
			recomId = getIntent().getLongExtra("recomId", 0);
			isSelf = getIntent().getBooleanExtra("isSelf", false);
		}
		recomId = 30;// 测试
		isSelf = true;//测试
		
		density = AndroidUtil.getDensity(RooRecommendDetailActivity.this);
		Display display = this.getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();
		itemWidth = (display.getWidth() - columnSpace * (columnCount + 1)) / columnCount;// 根据屏幕大小计算每列大小

		findView();
		createPhotoDir();
		((CommonApplication) getApplication()).addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("推荐详情");
		btnRight = (Button) this.findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		btnLeft.setOnClickListener(this);
		llTitle = (LinearLayout) this.findViewById(R.id.llTitle);
		ivRooRecommentDCover = (ImageView) this.findViewById(R.id.ivRooRecommentDCover);
		tvRooRecommentDTitleName = (TextView) this.findViewById(R.id.tvRooRecommentDTitleName);
		tvRooRecommentDContent = (TextView) this.findViewById(R.id.tvRooRecommentDContent);
		// 对推荐详情基本信息的编辑
		edtDTitleName = (EditText) this.findViewById(R.id.edtDTitleName);
		edtDContent = (EditText) this.findViewById(R.id.edtDContent);
		btnEditDetail = (Button) this.findViewById(R.id.btnEditDetail);
		btnEditDetail.setOnClickListener(this);
		ivRooRecommentDCover.setOnClickListener(this);

		gvPhoto = (GridViewInScrollView) this.findViewById(R.id.gvPhoto);
		gvPhoto.setNumColumns(columnCount);
		pbFooter = (ProgressBar) findViewById(R.id.progressBar);
		tvFooterMore = (TextView) findViewById(R.id.tvMore);
		tvFooterMore.setOnClickListener(this);
		if (isSelf) {
			recommentBeans.add(0, new RooRecommentBean());
		}
		recommendPhotoAdapter = new RecommendPhotoAdapter();
		gvPhoto.setAdapter(recommendPhotoAdapter);

		recommentBeanAlls = new ArrayList<RooRecommentBean>();
		if (NetUtil.checkNet(this)) {
			new LoadRecommendDetailTask(recomId, pageNo, pageSize).execute();
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();

		}

	}

	@Override
	public void fillData() {
		tvRooRecommentDTitleName.setText(rooRecommentBean.getTitle());
		tvRooRecommentDContent.setText(rooRecommentBean.getContent());

		String coverUrl = rooRecommentBean.getCoverUrl();
		ivRooRecommentDCover.setTag(coverUrl);
		Drawable cacheDrawable = AsyncImageLoader.getInstance().loadDrawable(coverUrl, new ImageCallback() {
			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				ImageView ivImageView = (ImageView) llTitle.findViewWithTag(imageUrl);
				if (ivImageView != null) {
					if (imageDrawable != null) {
						int oldwidth = imageDrawable.getIntrinsicWidth();
						int oldheight = imageDrawable.getIntrinsicHeight();
						LayoutParams lp = ivImageView.getLayoutParams();
						lp.width = screenWidth;
						lp.height = (oldheight * screenWidth) / oldwidth;
						ivImageView.setLayoutParams(lp);
						ivImageView.setImageDrawable(imageDrawable);
					} else {
						ivRooRecommentDCover.setImageResource(R.drawable.view);
					}
				}
			}
		});
		if (cacheDrawable != null) {
			int oldwidth = cacheDrawable.getIntrinsicWidth();
			int oldheight = cacheDrawable.getIntrinsicHeight();
			LayoutParams lp = ivRooRecommentDCover.getLayoutParams();
			lp.width = screenWidth;
			lp.height = (oldheight * screenWidth) / oldwidth;
			ivRooRecommentDCover.setLayoutParams(lp);
			ivRooRecommentDCover.setImageDrawable(cacheDrawable);
		} else {
			ivRooRecommentDCover.setImageResource(R.drawable.view);
		}

	}

	// 每张图片的点击事件
	OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			RooRecommentBean RooRecommentBean = (RooRecommentBean) v.getTag(R.id.tag_recomendphotobean);
			Intent intent = new Intent(RooRecommendDetailActivity.this, RooRecommendPhotoActivity.class);
			intent.putExtra("rooRecommentBeans", (Serializable) recommentBeanAlls);
			intent.putExtra("photoId", RooRecommentBean.getPhotoId());
			startActivity(intent);
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			finish();
			break;
		case R.id.btnEditDetail:// 点击编辑推荐详情基本信息
			if (!isEdit) {
				btnEditDetail.setBackgroundResource(R.drawable.btn_edit_submit_selector);
				tvRooRecommentDTitleName.setVisibility(View.GONE);
				edtDTitleName.setVisibility(View.VISIBLE);
				edtDTitleName.setText(tvRooRecommentDTitleName.getText().toString());
				tvRooRecommentDContent.setVisibility(View.GONE);
				edtDContent.setVisibility(View.VISIBLE);			
				edtDContent.setText(tvRooRecommentDContent.getText().toString());
				isEdit = true;
			} else {
				btnEditDetail.setBackgroundResource(R.drawable.btn_edit_selector);
				tvRooRecommentDTitleName.setVisibility(View.VISIBLE);
				edtDTitleName.setVisibility(View.GONE);
				tvRooRecommentDContent.setVisibility(View.VISIBLE);
				edtDContent.setVisibility(View.GONE);
				String title = edtDTitleName.getText().toString().trim();
				String content = edtDContent.getText().toString().trim();
				updateData = STR;
				// 上传修改的信息（title、content）
				if (NetUtil.checkNet(this)) {
					new KangarooUpRecommendTask(recomId, title, content, null).execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
				}
				isEdit = false;
			}
			break;
		case R.id.ivRooRecommentDCover:// 点击更换推荐封面图
			updateData = PHOTO;
			photoType = COVER_PHOTO;
			createUploadIconDialog();
			break;
		case R.id.tvMore:// 点击加载更多	
			if(!isRuning && pageNo > 1 && pageNo <= totalPage){
				pbFooter = new ProgressBar(this);
				if (NetUtil.checkNet(this)) {
					new LoadRecommendDetailTask(recomId, pageNo, pageSize).execute();
				} else {
					Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
				}
			}
			break;
			
		}
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
						Toast.makeText(RooRecommendDetailActivity.this, "请检查SD卡是否正常", Toast.LENGTH_SHORT).show();
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
					MobclickAgent.reportError(RooRecommendDetailActivity.this, StringUtil.getExceptionInfo(e));
				}

				// 执行上传图片任务
				if (photoType == COVER_PHOTO) {
					ivRooRecommentDCover.setImageBitmap(cameraBitmap);
					// 上传修改的信息（Photo）
					String title = tvRooRecommentDTitleName.getText().toString().trim();
					String content = tvRooRecommentDContent.getText().toString().trim();
					if (NetUtil.checkNet(this)) {
						new KangarooUpRecommendTask(recomId, coverFile).execute();
					} else {
						Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
					}
				} else if (photoType == ALBUM_PHOTO) {
					if (NetUtil.checkNet(this)) {
						new UpdateRecommendPhotoTask("upload", albumFile).execute();
					} else {
						Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_LONG).show();
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
			MobclickAgent.reportError(RooRecommendDetailActivity.this, StringUtil.getExceptionInfo(e));
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

	private class RecommendPhotoAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return recommentBeans.size();
		}

		@Override
		public Object getItem(int position) {
			return recommentBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void remove(int position) {
			recommentBeans.remove(position);
			this.notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final RooRecommentBean recommentBean = recommentBeans.get(position);
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
						photoType = ALBUM_PHOTO;
						createUploadIconDialog();
					}
				});
			} else {
				if (recommentBean.isDelState()) {
					holder.ivDel.setVisibility(View.VISIBLE);
				} else {
					holder.ivDel.setVisibility(View.GONE);
				}
				holder.ivDel.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// long dd = recommentBean.getPhotoId();
						if (NetUtil.checkNet(RooRecommendDetailActivity.this)) {
							new UpdateRecommendPhotoTask("del", recommentBean.getPhotoId(), delPosition).execute();
						} else {
							Toast.makeText(RooRecommendDetailActivity.this, R.string.NoSignalException, Toast.LENGTH_LONG);
						}
					}
				});
				holder.viewAddPhoto.setVisibility(View.GONE);
				holder.ivPhoto.setVisibility(View.VISIBLE);
				holder.ivPhoto.setLayoutParams(param);
				String thumbUrl = recommentBean.getThumbUrl();
				holder.ivPhoto.setTag(R.id.tag_recomendphotobean, recommentBean);
				holder.ivPhoto.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (recommentBean.isDelState()) {
							recommentBean.setDelState(false);
							notifyDataSetChanged();
							return;
						} else {
							Intent intent = new Intent(RooRecommendDetailActivity.this, RooRecommendPhotoActivity.class);
							intent.putExtra("photoId", recommentBean.getPhotoId());
							intent.putExtra("rooRecommentBeans", recommentBeanAlls);
							startActivity(intent);
						}
					}
				});
				holder.ivPhoto.setTag(thumbUrl);
				Drawable cacheDrawable = AsyncImageLoader.getInstance().loadDrawable(thumbUrl, new ImageCallback() {
					@Override
					public void imageLoaded(Drawable imageDrawable, String imageUrl) {
						ImageView ivImage = (ImageView) gvPhoto.findViewWithTag(imageUrl);
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
							if (!recommentBean.isDelState()) {
								recommentBean.setDelState(true);
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

	private boolean isRuning = false;// 标示此任务是否在运行中

	/**
	 * 推荐详情
	 * 
	 * @author syghh
	 * 
	 */
	class LoadRecommendDetailTask extends AsyncTask<Void, Void, JSONObject> {
		private long recomId;
		private int pageIndex;
		private int pageSize;

		public LoadRecommendDetailTask(long recomId, int pageIndex, int pageSize) {
			this.recomId = recomId;
			this.pageIndex = pageIndex;
			this.pageSize = pageSize;
		}

		@Override
		protected void onPreExecute() {
			isRuning = true;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().kangarooRecommendDetail(recomId, pageIndex, pageSize);
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
						recommentBeans = RooRecommentBean.constantAddListBean(result.getJSONObject("data").getJSONArray("recomPhotos"),
								recommentBeans);
						rooRecommentBean = new RooRecommentBean(result.getJSONObject("data"));
						totalPage = result.getJSONObject("data").getJSONArray("recomPhotos").length() / pageSize + 1;
						recommentBeanAlls.addAll(recommentBeans);
						
						if (recommentBeans.size() > 0) {
							recommendPhotoAdapter.notifyDataSetChanged();
						}
						if (recommentBeans.size() >= pageSize) {
							tvFooterMore.setVisibility(View.VISIBLE);
						} else if (recommentBeans.size() < pageSize) {
							tvFooterMore.setVisibility(View.GONE);
						}
						if (pageNo == totalPage) {
							tvFooterMore.setText("全部加载完毕");
							tvFooterMore.setClickable(false);
						}
						if(pbFooter != null){
							pbFooter.setVisibility(View.GONE);
						}
						pageNo++;
						fillData();
					} else {
						Toast.makeText(RooRecommendDetailActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(RooRecommendDetailActivity.this, "读图错误", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(RooRecommendDetailActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
			isRuning = false;
		}
	}

	/**
	 * 修改推荐（封面图、名字、描述）
	 * 
	 * @author syghh
	 * 
	 */
	class KangarooUpRecommendTask extends AsyncTask<Void, Void, JSONObject> {

		private long recomId;
		private String title;
		private String content;
		private File coverFile;

		public KangarooUpRecommendTask(long recomId, String title, String content, File coverFile) {
			this.recomId = recomId;
			this.title = title;
			this.content = content;
			this.coverFile = coverFile;
		}
		
		public KangarooUpRecommendTask(long recomId, File coverFile) {
			this.recomId = recomId;
			this.coverFile = coverFile;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(RooRecommendDetailActivity.this);
				if (updateData.equals(STR)) {
					pd.setMessage("修改推荐基本信息，请稍后...");
				} else if (updateData.equals(PHOTO)) {
					pd.setMessage("照片上传中...");
				}

			} else {
				if (updateData.equals(STR)) {
					pd.setMessage("修改推荐基本信息，请稍后...");
				} else if (updateData.equals(PHOTO)) {
					pd.setMessage("照片上传中...");
				}
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				if (updateData.equals(STR)) {
					return new BusinessHelper().kangarooUpRecommend(recomId, title, content);
				} else if (updateData.equals(PHOTO)) {
					return new BusinessHelper().kangarooUpRecommendPhoto(recomId, coverFile);
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
				try {
					int status = result.getInt("status");
					if (status == Constants.SUCCESS) {
						if(updateData.equals(STR)){
							tvRooRecommentDTitleName.setText(edtDTitleName.getText().toString().trim());
							tvRooRecommentDContent.setText(edtDContent.getText().toString().trim());
						}
						Toast.makeText(RooRecommendDetailActivity.this, "修改成功", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(RooRecommendDetailActivity.this, result.getString("error"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(RooRecommendDetailActivity.this, "修改失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(RooRecommendDetailActivity.this, "服务器请求失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 处理照片接口；type为“upload”为上传照片；type为“del”为删除照片；
	 * 
	 * @author Zhoujun
	 * 
	 */
	private class UpdateRecommendPhotoTask extends AsyncTask<Void, Void, JSONObject> {

		private String type;
		private long photoId;
		private File file;
		private int position;

		public UpdateRecommendPhotoTask(String type, long photoId, int position) {
			super();
			this.type = type;
			this.photoId = photoId;
			this.position = position;
		}

		public UpdateRecommendPhotoTask(String type, File file) {
			super();
			this.type = type;
			this.file = file;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (type.equals("upload")) {
				if (pd == null) {
					pd = new ProgressDialog(RooRecommendDetailActivity.this);
				}
				pd.setMessage("上传照片中...");
				pd.show();
			}
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject obj = null;
			try {
				if (type.equals("upload")) {
					obj = new BusinessHelper().kangarooUpLoadRecomPic(recomId, file);
				} else if (type.equals("del")) {
					obj = new BusinessHelper().delKangarooRecommendPhoto(photoId);
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
						Toast.makeText(RooRecommendDetailActivity.this, successText, Toast.LENGTH_SHORT).show();
						if (type.equals("upload")) {
							RooRecommentBean bean = new RooRecommentBean(result.getJSONObject("data").getJSONArray("recomPhotos").getJSONObject(0));
							recommentBeans.add(1, bean);
							recommendPhotoAdapter.notifyDataSetChanged();
							if (recommentBeans.size() >= pageSize) {
								tvFooterMore.setVisibility(View.VISIBLE);
							} else if (recommentBeans.size() < pageSize) {
								tvFooterMore.setVisibility(View.GONE);
							}
							if (result.getInt("current") == result.getInt("total")) {
								tvFooterMore.setText("全部加载完毕");
							}
						} else if (type.equals("del")) {
							recommendPhotoAdapter.remove(position);
						}
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(RooRecommendDetailActivity.this, R.string.time_out, Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(RooRecommendDetailActivity.this, failText, Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			} else {
				Toast.makeText(RooRecommendDetailActivity.this, failText, Toast.LENGTH_LONG).show();
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
		if (imageCache != null) {
			imageCache.clear();
			imageCache = null;
		}
		System.gc();
	}

}

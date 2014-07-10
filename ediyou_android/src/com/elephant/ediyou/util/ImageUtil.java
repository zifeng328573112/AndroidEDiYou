package com.elephant.ediyou.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

/**
 * 图片处理工具类
 * 
 * @author Aizhimin
 * 
 */
public class ImageUtil {

	/**
	 * drawable 转换成 bitmap
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		int width 				= drawable.getIntrinsicWidth(); // 取 drawable 的长宽
		int height 				= drawable.getIntrinsicHeight();
		Bitmap.Config config 	= drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565; // 取
																																// 的颜色格式
		Bitmap bitmap 			= Bitmap.createBitmap(width, height, config); // 建立对应
																	// bitmap
		Canvas canvas 			= new Canvas(bitmap); // 建立对应 bitmap 的画布
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas); // 把 drawable 内容画到画布中
		return bitmap;
	}

	/**
	 * 图片加上圆角效果
	 * 
	 * @param drawable  需要处理的图片
	 * @param percent  圆角比例大小
	 * @return
	 */
	public static Bitmap getRoundCornerBitmapWithPic(Drawable drawable, float percent) {
		Bitmap bitmap = drawableToBitmap(drawable);
		return getRoundedCornerBitmapWithPic(bitmap, percent);
	}

	/**
	 * 压缩图片
	 * 
	 * @param path
	 * @param size
	 * @param context
	 * @return
	 * @throws IOException
	 */
	public static Bitmap revitionImageSize(String path, int sizeWidth, int sizeHeight, Context context) throws IOException {
		/** 取得图片*/
		InputStream temp 				= new FileInputStream(path);
		BitmapFactory.Options options 	= new BitmapFactory.Options();
		// 这个参数代表，不为bitmap分配内存空间，只记录一些该图片的信息（例如图片大小），说白了就是为了内存优化
		options.inJustDecodeBounds 		= true;
		// 通过创建图片的方式，取得options的内容（这里就是利用了java的地址传递来赋值）
		BitmapFactory.decodeStream(temp, null, options);
		// 关闭流
		temp.close();

		// 生成压缩的图片
		int i = 0;
		Bitmap bitmap = null;
		while (true) {
			// 这一步是根据要设置的大小，使宽和高都能满足
			if ((options.outWidth >> i <= sizeWidth) && (options.outHeight >> i <= sizeHeight)) {
				// 重新取得流，注意：这里一定要再次加载，不能二次使用之前的流！
				temp = new FileInputStream(path);

				// 这个参数表示 新生成的图片为原始图片的几分之一。
				options.inSampleSize = (int) Math.pow(4.0D, i);

				// 这里之前设置为了true，所以要改为false，否则就创建不出图片
				options.inJustDecodeBounds = false;

				bitmap = BitmapFactory.decodeStream(temp, null, options);
				break;
			}
			i += 1;
		}
		return bitmap;
	}

	/**
	 * 压缩图片
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static Bitmap revitionImageSize(String path, Context context) throws IOException {
		// 取得图片
		BitmapFactory.Options options 	= new BitmapFactory.Options();
		// 这个参数代表，不为bitmap分配内存空间，只记录一些该图片的信息（例如图片大小），说白了就是为了内存优化
		options.inJustDecodeBounds 		= true;
		// 通过创建图片的方式，取得options的内容（这里就是利用了java的地址传递来赋值）
		Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/test.jpg", options);
		options.inJustDecodeBounds 		= false;

		// 关闭流
		int be = (int) (options.outHeight / (float) 200);
		if (be <= 0)
			be = 1;
		// 生成压缩的图片
		options.inSampleSize = be;
		bitmap = BitmapFactory.decodeFile(path, options);

		return bitmap;
	}

	public static Bitmap revitionImageSize2(String path, Context context, int wrapperWidth) throws IOException {
		BitmapFactory.Options opt 	= new BitmapFactory.Options();
		opt.inJustDecodeBounds 		= true;
		BitmapFactory.decodeFile(path, opt);
		opt.inJustDecodeBounds 		= false;
		opt.inPreferredConfig 		= Bitmap.Config.RGB_565;
		opt.inPurgeable 			= true;
		opt.inInputShareable 		= true;
		int be = (int) ((opt.outWidth + wrapperWidth) / (float) (wrapperWidth));
		if (be <= 0)
			be = 1;
		opt.inSampleSize = be;
		Bitmap bitmap 				= BitmapFactory.decodeFile(path, opt);
		return bitmap;
	}

	/**
	 * 图片加上圆角效果
	 * 
	 * @param bitmap  要处理的位图
	 * @param roundPx 圆角大小
	 * @return 返回处理后的位图
	 */
	public static Bitmap getRoundedCornerBitmapWithPic(Bitmap bitmap, float percent) {
		Bitmap output 		= Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas 		= new Canvas(output);

		final int color 	= 0xff424242;
		final Paint paint 	= new Paint();
		final Rect rect 	= new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF 	= new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, bitmap.getWidth() * percent, bitmap.getHeight() * percent, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	/**
	 * 获得某个图片资源的BitMap对象
	 * 
	 * @param context
	 * @param drawableId
	 * @return
	 */
	public static Bitmap getBitMapByRes(Context context, int drawableId) {
		BitmapFactory.Options opts 	= new BitmapFactory.Options();
		opts.inJustDecodeBounds 	= true;
		BitmapFactory.decodeResource(context.getResources(), drawableId, opts);
		opts.inJustDecodeBounds 	= false;
		Bitmap bitmap 				= BitmapFactory.decodeResource(context.getResources(), drawableId, opts);
		return bitmap;
	}

	/**
	 * 获得某个图片资源的宽高
	 * 
	 * @param context
	 * @param drawableId
	 * @return
	 */
	public static int[] getImageWidthHeight(Context context, int drawableId) {
		BitmapFactory.Options opts 		= new BitmapFactory.Options();
		opts.inJustDecodeBounds 		= true;
		BitmapFactory.decodeResource(context.getResources(), drawableId, opts);
		opts.inJustDecodeBounds	 		= false;
		Bitmap bitmap 					= BitmapFactory.decodeResource(context.getResources(), drawableId, opts);
		return new int[] { bitmap.getWidth(), bitmap.getHeight() };
	}

	/**
	 * 设置iamgeview 显示宽高
	 * 
	 * @param iv
	 * @param photoWidth
	 * @param oldwidth
	 * @param oldheight
	 */
	public static void setImageViewParams(ImageView iv, int photoWidth, int oldwidth, int oldheight) {
		LayoutParams lp 		= iv.getLayoutParams();
		lp.width 				= photoWidth;
		lp.height 				= (oldheight * photoWidth) / oldwidth;
		iv.setLayoutParams(lp);
	}

	/**
	 * 开启图片裁减工具
	 * 
	 * @param uri
	 *            图片的uri
	 * @return uri不是图片格式就返回空
	 */
	public static Intent startCrop(Uri uri) {
		Intent intent = null;
		intent = new Intent("com.android.camera.action.CROP");// 打开图片裁减工具
		if (uri != null) {
			intent.setDataAndType(uri, "image/*");
			intent.putExtra("crop", "true");
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 80);
			intent.putExtra("outputY", 80);
			intent.putExtra("return-data", true);
		}
		return intent;
	}

	/**
	 * 开启图片裁剪工具
	 * 
	 * @param data  传送data数据
	 * @return
	 */
	public static Intent startCrop(Bitmap data) {
		Intent intent 	= null;
		intent 			= new Intent("com.android.camera.action.CROP");
		if (data != null) {
			intent.setType("image/*");
			intent.putExtra("data", data);
			intent.putExtra("crop", "true");
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 80);
			intent.putExtra("outputY", 80);
			intent.putExtra("return-data", true);
		}
		return intent;
	}

	/**
	 * 用当前时间给取得的图片命名
	 * 
	 */
	public static String getPhotoFileName() {
		Date date 					= new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmsss");
		return dateFormat.format(date) + ".jpg";
	}

	/**
	 * 头像文件名
	 * 
	 * @param sid
	 * @return
	 */
	public static String createAvatarFileName(String sid) {
		return "avatar_" + sid + ".jpg";
	}

	/**
	 * 根据远程服务器id生成图片文件名
	 * 
	 * @param remoteId
	 * @return
	 */
	public static String createPhotoFileName(String remoteId) {
		return "photo_" + remoteId + ".jpg";
	}

	/**
	 * 根据图片宽高设置ImagView的图片资源
	 */
	public static void setImageFitSize(Drawable imageDrawable, ImageView ivImageView, int newWidth) {
		int oldwidth 		= imageDrawable.getIntrinsicWidth();
		int oldheight 		= imageDrawable.getIntrinsicHeight();
		LayoutParams lp 	= ivImageView.getLayoutParams();
		lp.width 			= newWidth;
		lp.height 			= (oldheight * newWidth) / oldwidth;
		ivImageView.setLayoutParams(lp);
		ivImageView.setImageDrawable(imageDrawable);
	}

	/**
	 * 添加照片到相册
	 * 
	 * @param context
	 * @param filePath
	 */
	public static void addImageToGallery(Context context, String filePath) {
		Intent intent 		= new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri uri 			= Uri.fromFile(new File(filePath));
		intent.setData(uri);
		context.sendBroadcast(intent);
	}

	/**
	 * 把文本中的表情文字转换成表情图片（图片来自资源文件）
	 * 
	 * @param emotionMap  表情图片资源文件的Map〈String, Integer〉
	 * @param wbTxt  整个文本
	 * @param context
	 * @return
	 */
	public static SpannableString changeTextToEmotions(Map<String, Integer> emotionMap, String wbTxt, Context context) {
		SpannableString spann 	= new SpannableString(wbTxt);
		if (wbTxt != null && !"".equals(wbTxt)) {
			for (Entry<String, Integer> entry : emotionMap.entrySet()) {
				int res 		= entry.getValue();
				String key 		= entry.getKey();
				int begin 		= 0;
				int starts 		= 0;
				int end 		= 0;
				while (wbTxt.indexOf(key, begin) != -1) {
					Bitmap bitmap 		= ImageUtil.getBitMapByRes(context, res);
					Drawable drawable 	= new BitmapDrawable(bitmap);
					if (drawable != null) {
						drawable.setBounds(5, 5, 48, 48);
						ImageSpan span 	= new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);

						starts 			= wbTxt.indexOf(key, begin);
						end 			= starts + key.length();
						spann.setSpan(span, starts, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
						begin 			= starts + 1;
					}
				}
			}

		}
		return spann;
	}

	/******************************** 微信调用 *******************************************/
	/**
	 * 将图片转转为字节流
	 * 
	 * @param bmp
	 * @param needRecycle
	 * @param picAccuracy 精度 0-100
	 * @return
	 */
	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle, int picAccuracy) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.JPEG, picAccuracy, output);
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

	private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;

	/**
	 * 压缩图片
	 * 
	 * @param path
	 * @param height
	 * @param width
	 * @param crop
	 * @return
	 */
	public static Bitmap extractThumbNail(final String path, final int height, final int width, final boolean crop) {
		Assert.assertTrue(path != null && !path.equals("") && height > 0 && width > 0);

		BitmapFactory.Options options = new BitmapFactory.Options();

		try {
			options.inJustDecodeBounds = true;
			Bitmap tmp = BitmapFactory.decodeFile(path, options);
			if (tmp != null) {
				tmp.recycle();
				tmp = null;
			}

			final double beY = options.outHeight * 1.0 / height;
			final double beX = options.outWidth * 1.0 / width;
			options.inSampleSize = (int) (crop ? (beY > beX ? beX : beY) : (beY < beX ? beX : beY));
			if (options.inSampleSize <= 1) {
				options.inSampleSize = 1;
			}

			// NOTE: out of memory error
			while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
				options.inSampleSize++;
			}

			int newHeight = height;
			int newWidth = width;
			if (crop) {
				if (beY > beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			} else {
				if (beY < beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			}

			options.inJustDecodeBounds = false;

			Bitmap bm = BitmapFactory.decodeFile(path, options);
			if (bm == null) {
				return null;
			}

			final Bitmap scale = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
			if (scale != null) {
				bm.recycle();
				bm = scale;
			}

			if (crop) {
				final Bitmap cropped = Bitmap.createBitmap(bm, (bm.getWidth() - width) >> 1, (bm.getHeight() - height) >> 1, width, height);
				if (cropped == null) {
					return bm;
				}
				bm.recycle();
				bm = cropped;
			}
			return bm;
		} catch (final OutOfMemoryError e) {
			options = null;
		}
		return null;
	}

	/**
	 * 加水印 也可以加文字
	 * 
	 * @param src
	 * @param watermark
	 * @return
	 */
	public static Bitmap watermarkBitmap(Bitmap src, Bitmap watermark) {
		if (src == null) {
			return null;
		}
		int w 			= src.getWidth();
		int h 			= src.getHeight();
		// 需要处理图片太大造成的内存超过的问题,这里我的图片很小所以不写相应代码了
		Bitmap newb 	= Bitmap.createBitmap(w, h, Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
		Canvas cv 		= new Canvas(newb);
		cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src
		Paint paint 	= new Paint();
		// 加入图片
		if (watermark != null) {
			int ww 		= watermark.getWidth();
			int wh 		= watermark.getHeight();
//			paint.setAlpha(50);
			 cv.drawBitmap(watermark, w - ww, h - wh - 5, paint);//
			// 在src的右下角画入水印
//			cv.drawBitmap(watermark, 0, 0, paint);// 在src的左上角画入水印
		} else {
			Log.i("i", "water mark failed");
		}
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		cv.restore();// 存储
		return newb;
	}
}

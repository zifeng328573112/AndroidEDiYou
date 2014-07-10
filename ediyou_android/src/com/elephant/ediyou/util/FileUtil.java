package com.elephant.ediyou.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import com.elephant.ediyou.Constants;

import android.content.Context;
import android.os.Environment;

/**
 * 文件处理工具类
 * @author Aizhimin
 * 
 */
public class FileUtil {
	
	
	public static final int COPY_FILE_RESULT_TYPE_SDCARD_NOT_EXIST = 101;//sd卡不存在
	public static final int COPY_FILE_RESULT_TYPE_COPY_SUCCESS = 102;//拷贝成功
	public static final int COPY_FILE_RESULT_TYPE_COPY_FAILD = 103;//拷贝失败
	public static final int COPY_FILE_RESULT_TYPE_DIR_ERROR = 104;//目录错误
	public static final int COPY_FILE_RESULT_TYPE_SOURCE_FILE_NOT_EXIST = 105;//源文件不存在
	
	/**
	 * 读取assets下的文本数据
	 * @param fileName
	 * @return
	 */
	public static String getStringFromAssets(Context context,String fileName){ 
        try { 
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName) ); 
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String Result="";
            while((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return null;
	}
	
	/**
	 * 读取文本流文件
	 * @param is
	 * @return
	 */
	public static String readStream(InputStream is) {
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			int i = is.read();
			while (i != -1) {
				bo.write(i);
				i = is.read();
			}
			return bo.toString();
		} catch (IOException e) {
			return "";
		}
	}
	
	/**
	 * 获得文件大小 
	 * @param filePath
	 * @return
	 * @throws IOException 
	 */
	public static long getFileSize(File f) throws IOException{
		long s=0;
        if (f.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(f);
            s= fis.available();
        } 
        return s;
	}
	/**
	 * 格式化文件大小
	 * @param fileS
	 * @return
	 * @throws IOException 
	 */
	public static String formatFileSize(File f) {//转换文件大小
		String fileSizeString = "";
		try{
			long fileS = getFileSize(f);
	        DecimalFormat df = new DecimalFormat("#.00");
	        if (fileS < 1024) {
	            fileSizeString = df.format((double) fileS) + "b";
	        } else if (fileS < 1048576) {
	            fileSizeString = df.format((double) fileS / 1024) + "kb";
	        } else if (fileS < 1073741824) {
	            fileSizeString = df.format((double) fileS / 1048576) + "mb";
	        } else {
	            fileSizeString = df.format((double) fileS / 1073741824) + "gb";
	        }
		}catch(Exception e){
		}
        return fileSizeString;
    }
	
	/**
	 * 拷贝资源文件到sd卡
	 * @param context
	 * @param resId
	 * @param databaseFilename  如数据库文件拷贝到sd卡中
	 */
	public static void copyResToSdcard(Context context, int resId,
			String databaseFilename) {// name为sd卡下制定的路径
		try {
			// 不存在得到数据库输入流对象
			InputStream is = context.getResources().openRawResource(resId);
			// 创建输出流
			FileOutputStream fos = new FileOutputStream(databaseFilename);
			// 将数据输出
			byte[] buffer = new byte[8192];
			int count = 0;
			while ((count = is.read(buffer)) > 0) {
				fos.write(buffer, 0, count);
			}
			// 关闭资源
			fos.close();
			is.close();
		} catch (Exception e) {
		}
	}
	
	/**
	 * 拷贝文件(默认不删除源文件)
	 * @param context
	 * @param fromPath 源文件完整路径
	 * @param toPath 目标文件完整路径
	 * @return 返回拷贝状态(详见该类静态变量)
	 */
	public static int copyFile(Context context, String fromPath, String toPath){
		return copyFile(context, fromPath, toPath, false);
	}
	
	/**
	 * 拷贝文件(默认不删除源文件)
	 * @param context
	 * @param fromFile 源文件
	 * @param toFile 目标文件
	 * @return 返回拷贝状态(详见该类静态变量)
	 */
	public static int copyFile(Context context, File fromFile, File toFile){
		return copyFile(context, fromFile, toFile, false);
	}
	
	/**
	 * 拷贝文件(可以选择是否删除源文件)
	 * @param context
	 * @param fromPath 源文件路径
	 * @param toPath 目标文件路径
	 * @param isDelRes 是否删除源文件
	 * @return 返回拷贝状态(详见该类静态变量)
	 */
	public static int copyFile(Context context, String fromPath, String toPath, boolean isDelRes){
		if(!StringUtil.isBlank(fromPath) || !StringUtil.isBlank(toPath)){
			File fromFile = new File(fromPath);
			File toFile = new File(toPath);
			return copyFile(context, fromFile, toFile, isDelRes);
		}else{
			return COPY_FILE_RESULT_TYPE_DIR_ERROR;
		}
	}
	
	/**
	 * 拷贝文件(可以选择是否删除源文件)
	 * @param context
	 * @param fromFile 源文件
	 * @param toFile 目标文件
	 * @param isDelRes 是否删除源文件
	 * @return 返回拷贝状态 (详见该类静态变量)
	 */

	public static int copyFile(Context context, File fromFile, File toFile, boolean isDelRes){
		if(fromFile==null || toFile==null){
			return COPY_FILE_RESULT_TYPE_DIR_ERROR;
		}
		if(AndroidUtil.existSdcard()){
			try {
				if(fromFile.exists()){
					String toPath = toFile.getAbsolutePath();
					String toDir = toPath.substring(0, toPath.lastIndexOf("/"));
					File toDirFile = new File(toDir);
					if(!toDirFile.exists()){
						toDirFile.mkdirs();
					}
					if(!toFile.exists()){
						toFile.createNewFile();
					}
					FileInputStream in = new FileInputStream(fromFile);
					FileOutputStream out = new FileOutputStream(toFile);
					byte[] buffer = new byte[1024];
					int count = 0;
					while ((count = in.read(buffer)) > 0) {
						out.write(buffer, 0, count);
					}
					// 关闭资源
					out.close();
					in.close();
				}else{
					return COPY_FILE_RESULT_TYPE_SOURCE_FILE_NOT_EXIST;
				}
				if(isDelRes){//判断是否删除源文件
					if(fromFile.exists()){
						fromFile.delete();
					}
				}
				if(toFile.exists()){
					return COPY_FILE_RESULT_TYPE_COPY_SUCCESS;
				}else{
					return COPY_FILE_RESULT_TYPE_COPY_FAILD;
				}
			} catch (Exception e) {
				return COPY_FILE_RESULT_TYPE_COPY_FAILD;
			}
		}else{
			return COPY_FILE_RESULT_TYPE_SDCARD_NOT_EXIST;
		}
	}
	
	/**
	 * 获取相机拍照存放目录(最后没有带斜杠)
	 */
	public static String getCameraDir(){
		String path = "";
		if(AndroidUtil.existSdcard()){
			File sdcardFile = Environment.getExternalStorageDirectory();
			path = sdcardFile.getAbsolutePath()+"/"+Constants.APP_DIR_NAME+"/"+Constants.CAMERA_DIR_NAME;
		}
		return path;
	}
	
}

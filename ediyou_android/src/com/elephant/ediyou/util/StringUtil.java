package com.elephant.ediyou.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.elephant.ediyou.R;
import com.elephant.ediyou.util.alipay.Constant;

/**
 * 字符串工具类
 * 
 * @author Aizhimin 说明：处理一下字符串的常用操作，字符串校验等
 */
public class StringUtil {
	/**
	 * 判断字符串是否为空或者空字符串 如果字符串是空或空字符串则返回true，否则返回false
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isBlank(String str) {
		if (str == null || "".equals(str)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 验证邮箱输入是否合法
	 * 
	 * @param strEmail
	 * @return
	 */
	public static boolean isEmail(String strEmail) {
		// String strPattern =
		// "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		// String strPattern =
		// "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
		String strPattern = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(strEmail);
		return m.matches();
	}

	/**
	 * 验证手机号码
	 * 
	 * @param strMobile
	 * @return 是：true；不是：false
	 */
	public static boolean isMobile(String strMobile) {
		if (!StringUtil.isBlank(strMobile) && strMobile.length() == 11) {
			try {
				Long mobile = Long.parseLong(strMobile);
				String estr = String.valueOf(mobile).substring(0, 2);
				if (estr.equals("13") || estr.equals("14") || estr.equals("15") || estr.equals("18")) {
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * MD5加密
	 * 
	 * @param secret_key
	 * @return
	 */
	public static String createSign(String secret_key) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(secret_key.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			System.exit(-1);
		} catch (UnsupportedEncodingException e) {
		}

		byte[] byteArray = messageDigest.digest();

		StringBuffer md5StrBuff = new StringBuffer();

		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		return md5StrBuff.toString();
	}

	/**
	 * 字符全角化
	 * 
	 * @param input
	 * @return
	 */
	public static String ToDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)
				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}

	/**
	 * 判断是否是中文
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串是否超过指定字符数（注：此方法好像有问题）
	 * 
	 * @param content
	 * @param stringNum
	 *            指定字符数 如：140
	 * @return 
	 * 
	 */
	public static boolean countStringLength(String content, int stringNum) {
		int result = 0;
		if (content != null && !"".equals(content)) {
			char[] contentArr = content.toCharArray();
			if (contentArr != null) {
				for (int i = 0; i < contentArr.length; i++) {
					char c = contentArr[i];
					if (isChinese(c)) {
						result += 3;
					} else {
						result += 1;
					}
				}
			}
		}
		if (result > stringNum *3) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 将网络图片路径md5加密作为文件名
	 * 
	 * @param imageUrl
	 * @return
	 */
	public static String createImageName(String imageUrl) {
		return createSign(imageUrl) + ".jpg";
	}

	/**
	 * 将网络图片路径md5加密作为文件名,可以设置图片类型
	 * 
	 * @param imageUrl
	 * @param imgSuffix
	 * @return
	 */
	public static String createImageName(String imageUrl, String imgSuffix) {
		return createSign(imageUrl) + imgSuffix;
	}

	/**
	 * 将null转换为""
	 * 
	 * @param str
	 * @return
	 */
	public static String trimNull(String str) {
		if (str == null || "null".equalsIgnoreCase(str))
			return "";
		else
			return str;
	}

	/**
	 * 把异常信息打印出来
	 * 
	 * @param e
	 * @return
	 */
	public static String getExceptionInfo(Exception e) {
		String result = "";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		result = e.getMessage() + "/r/n" + sw.toString();
		pw.close();
		try {
			sw.close();
		} catch (IOException e1) {

		}
		return result;
	}

	/**
	 * 判断乱码为"�"这个特殊字符的情况
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isSpecialCharacter(String str) {
		// 是"�"这个特殊字符的乱码情况
		if (str.contains("ï¿½")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串是否在Unicode中
	 * 
	 * @param chineseStr
	 * @return
	 */
	public static boolean isChineseCharacter(String chineseStr) {
		char[] charArray = chineseStr.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			// 是否是Unicode编码,除了"�"这个字符.这个字符要另外早isSpecialCharacter()处理
			if ((charArray[i] >= '\u0000' && charArray[i] < '\uFFFD') || ((charArray[i] > '\uFFFD' && charArray[i] < '\uFFFF'))) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}

	public static boolean checkNum(String num) {
		String check = "^[0-9]*$";
		Pattern regex = Pattern.compile(check);
		Matcher matcher = regex.matcher(num);
		boolean isMatched = matcher.matches();
		return isMatched;
	}

	public static String DoubleToAmountString(Double num) {
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(num);
	}

	/**
	 * 获得指定字符串的字符数
	 * @param value 指定字符串
	 * @return 字符长度
	 * @author syghh
	 */
	public static int StringLength(String value) {
		int valueLength = 0;
		String chinese = "[\u4e00-\u9fa5]";
		for (int i = 0; i < value.length(); i++) {
			String temp = value.substring(i, i + 1);
			if (temp.matches(chinese)) {
				valueLength += 2;
			} else {
				valueLength += 1;
			}
		}
		return valueLength;
	}

	/**
	 * 判断指定字符串的字符数是否小于限定字符长度
	 * @param value 指定字符串
	 * @param limitLength 限定字符长度
	 * @return 未超出 true; 超出 false
	 * @author syghh
	 */
	public static boolean isStringLengthOut(String value, int limitLength) {
		int valueLength = 0;
		String chinese = "[\u4e00-\u9fa5]";
		for (int i = 0; i < value.length(); i++) {
			String temp = value.substring(i, i + 1);
			if (temp.matches(chinese)) {
				valueLength += 2;
			} else {
				valueLength += 1;
			}
		}
		if(valueLength > limitLength){
			return false;
		}else {
			return true;
		}
	}
	
	/**
	 * 判断指定字符串是否在指定字符数范围（minLength-maxLength）内，即：minLength <= x <=maxLength 
	 * @param value 指定字符串
	 * @param minLength 指定最小字符长度
	 * @param maxLength 指定最大字符长度
	 * @return -1:小于minLength;    0:在范围内;    1:大于maxLength
	 * @author syghh
	 */
	public static int isStringLengthInLimit(String value, int minLength, int maxLength) {
		int valueLength = 0;
		String chinese = "[\u4e00-\u9fa5]";
		for (int i = 0; i < value.length(); i++) {
			String temp = value.substring(i, i + 1);
			if (temp.matches(chinese)) {
				valueLength += 2;
			} else {
				valueLength += 1;
			}
		}
		if(valueLength >= minLength && valueLength <= maxLength){
			return 0;
		}else if(valueLength > maxLength){
			return 1;
		}else {
			return -1;
		}
	}
	
	
	/**
	 * 限制EditText的长度
	 * 
	 * @param editText
	 * @param limitLength
	 *            字符数
	 * @param context
	 */
	public static void limitEditTextLength(final EditText editText, final int limitLength, final Context context) {
		// 输入框限制输入字数
		editText.addTextChangedListener(new TextWatcher() {
			private CharSequence temp;
			private int selectionStart;
			private int selectionEnd;

			@Override
			public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
				temp = s;
			}

			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				selectionStart = editText.getSelectionStart();
				selectionEnd = editText.getSelectionEnd();
				Log.i("gongbiao1", "" + selectionStart);
				boolean isStringLengthOut = isStringLengthOut(String.valueOf(temp), limitLength);
				if (!isStringLengthOut) {
					Toast.makeText(context, "长度已超出" + limitLength + "个字", Toast.LENGTH_SHORT).show();
					s.delete(selectionStart - 1, selectionEnd);
					int tempSelection = selectionEnd;
					editText.setText(s);
					editText.setSelection(tempSelection);// 设置光标在最后
				}
			}
		});
	}

}

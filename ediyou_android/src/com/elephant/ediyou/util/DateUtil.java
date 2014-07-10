package com.elephant.ediyou.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * 日期处理工具类
 * 
 * @author Aizhimin 说明：对日期格式的格式化与转换操作等一系列操作
 */
public class DateUtil {

	/**
	 * 字符串转换成日期 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param strFormat
	 *            格式定义 如：yyyy-MM-dd HH:mm:ss
	 * @param dateValue
	 *            日期对象
	 * @return
	 */
	public static Date stringToDate(String strFormat, String dateValue) {
		if (dateValue == null)
			return null;
		if (strFormat == null)
			strFormat = "yyyy-MM-dd HH:mm:ss";

		SimpleDateFormat dateFormat = new SimpleDateFormat(strFormat);
		Date newDate = null;
		try {
			newDate = dateFormat.parse(dateValue);
		} catch (ParseException pe) {
			newDate = null;
		}
		return newDate;
	}

	/**
	 * 判断两个时间的时间差是否小于等于0，即某事件是否过期。固定str1-str2。
	 * 
	 * @param str1
	 *            ,不能为null，必须为String："2012-12-12 12:12:12"
	 * @param str2
	 *            ,若为null，这指的是“现在”
	 * @return 小于等于0，返回true；否则，false。
	 */
	public static boolean lagLessThanZero(String str1, String str2) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date one;
		Date two;
		if (str2 == null) {
			two = new Date();
		} else {
			try {
				two = df.parse(str2);
			} catch (ParseException pe) {
				two = new Date();
			}
		}
		try {
			one = df.parse(str1);
		} catch (ParseException pe) {
			one = new Date();
		}
		long oneL = one.getTime();
		long twoL = two.getTime();
		if ((oneL - twoL) <= 0) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 日期转成字符串
	 * 
	 * @param strFormat
	 *            格式定义 如：yyyy-MM-dd HH:mm:ss
	 * @param date
	 *            日期字符串
	 * @return
	 */
	public static String dateToString(String strFormat, Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(strFormat);
		return dateFormat.format(date);
	}

	/**
	 * 计算两个日期间隔天数
	 * 
	 * @param begin
	 * @param end
	 * @return
	 */
	public static int countDays(Date begin, Date end) {
		int days = 0;
		Calendar c_b = Calendar.getInstance();
		Calendar c_e = Calendar.getInstance();
		c_b.setTime(begin);
		c_e.setTime(end);
		while (c_b.before(c_e)) {
			days++;
			c_b.add(Calendar.DAY_OF_YEAR, 1);
		}
		return days;
	}

	/**
	 * 获取最近的时间
	 * 
	 * @param time
	 * @return
	 */
	public static String getNearTime(String str1) {
		String result = "";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date one;
		Date two;
		long day = 0;
		long hour = 0;
		long min = 0;
		long sec = 0;
		try {
			one = df.parse(str1);
			two = new Date();
			long time1 = one.getTime();
			long time2 = two.getTime();
			long diff;
			if (time1 < time2) {
				diff = time2 - time1;
			} else {
				diff = time1 - time2;
			}
			day = diff / (24 * 60 * 60 * 1000);
			hour = (diff / (60 * 60 * 1000) - day * 24);
			min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
			sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
			System.out.println(day + "天" + hour + "小时" + min + "分钟" + sec + "秒");
			long m = day * 24 * 60 + hour * 60 + min;

			if (m <= 1) {
				result = "一分钟前";
			} else if (m > 1 && m <= 5) {
				result = "五分钟前";
			} else if (m > 5 && m <= 30) {
				result = "半小时前";
			} else if (m > 30 && m <= 60) {
				result = "一小时前";
			} else if (m > 60 && m <= 60 * 2) {
				result = "两小时前";
			} else if (m > 60 * 2 && m <= 60 * 24) {
				result = "一天前";
			} else if (m > 60 * 24 && m <= 60 * 24 * 2) {
				result = "两天前";
			} else if (m > 60 * 24 * 2 && m <= 60 * 24 * 7) {
				result = "一星期前";
			} else if (m > 60 * 24 * 7 && m <= 60 * 24 * 30) {
				result = "一个月前";
			} else if (m > 60 * 24 * 30 && m <= 60 * 24 * 30 * 6) {
				result = "六个月前";
			} else if (m > 60 * 24 * 30 * 6) {
				result = "很久以前";
			}
			day = diff / (24 * 60 * 60 * 1000);
			hour = (diff / (60 * 60 * 1000) - day * 24);
			min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
			sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		} catch (ParseException e) {
		}
		return result;
	}

	/**
	 * 两个时间之间相差距离多少天
	 * 
	 * @param one
	 *            时间参数 1：,若为null，则表示现在
	 * @param two
	 *            时间参数 2：,若为null，则表示现在
	 * @return 相差天数
	 */
	public static long getDistanceDays(String str1, String str2) throws Exception {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date one;
		Date two;
		long days = 0;
		try {

			if (str1 == null) {
				one = new Date();
			} else {
				one = df.parse(str1);
			}

			if (str2 == null) {
				two = new Date();
			} else {
				two = df.parse(str2);
			}

			long time1 = one.getTime();
			long time2 = two.getTime();
			long diff;
			if (time1 < time2) {
				diff = time2 - time1;
			} else {
				diff = time1 - time2;
			}
			days = diff / (1000 * 60 * 60 * 24);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return days;
	}

	/**
	 * 两个时间相差距离多少天多少小时多少分多少秒
	 * 
	 * @param str1
	 *            时间参数 1 格式：1990-01-01 12:00:00,若为null，则表示现在
	 * @param str2
	 *            时间参数 2 格式：2009-01-01 12:00:00,若为null，则表示现在
	 * @return long[] 返回值为：{天, 时, 分, 秒}
	 */
	public static long[] getDistanceTimes(String str1, String str2) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date one;
		Date two;
		long day = 0;
		long hour = 0;
		long min = 0;
		long sec = 0;
		try {

			if (str1 == null) {
				one = new Date();
			} else {
				one = df.parse(str1);
			}

			if (str2 == null) {
				two = new Date();
			} else {
				two = df.parse(str2);
			}

			long time1 = one.getTime();
			long time2 = two.getTime();
			long diff;
			if (time1 < time2) {
				diff = time2 - time1;
			} else {
				diff = time1 - time2;
			}
			day = diff / (24 * 60 * 60 * 1000);
			hour = (diff / (60 * 60 * 1000) - day * 24);
			min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
			sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		long[] times = { day, hour, min, sec };
		return times;
	}

	/**
	 * 两个时间相差距离多少天多少小时多少分多少秒
	 * 
	 * @param str1
	 *            时间参数 1 格式：1990-01-01 12:00:00,若为null，则表示现在
	 * @param str2
	 *            时间参数 2 格式：2009-01-01 12:00:00,若为null，则表示现在
	 * @return String 返回值为：xx天xx小时xx分xx秒
	 */
	public static String getDistanceTime(String str1, String str2) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date one;
		Date two;
		long day = 0;
		long hour = 0;
		long min = 0;
		long sec = 0;

		try {

			if (str1 == null) {
				one = new Date();
			} else {
				one = df.parse(str1);
			}

			if (str2 == null) {
				two = new Date();
			} else {
				two = df.parse(str2);
			}

			long time1 = one.getTime();
			long time2 = two.getTime();
			long diff;
			if (time1 < time2) {
				diff = time2 - time1;
			} else {
				diff = time1 - time2;
			}
			day = diff / (24 * 60 * 60 * 1000);
			hour = (diff / (60 * 60 * 1000) - day * 24);
			min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
			sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return day + "天" + hour + "小时" + min + "分" + sec + "秒";
	}

	/**
	 * 两个时间相差距离多少天多少小时多少分
	 * 
	 * @param str1
	 *            时间参数 1 格式：1990-01-01 12:00:00,若为null，则表示现在
	 * @param str2
	 *            时间参数 2 格式：2009-01-01 12:00:00,若为null，则表示现在
	 * @return String 返回值为：xx天xx小时xx分xx秒
	 */
	public static String getDistanceTimeMinute(String str1, String str2) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date one;
		Date two;
		long day = 0;
		long hour = 0;
		long min = 0;
		long sec = 0;

		try {

			if (str1 == null) {
				one = new Date();
			} else {
				one = df.parse(str1);
			}

			if (str2 == null) {
				two = new Date();
			} else {
				two = df.parse(str2);
			}

			long time1 = one.getTime();
			long time2 = two.getTime();
			long diff;
			if (time1 < time2) {
				diff = time2 - time1;
			} else {
				diff = time1 - time2;
			}
			day = diff / (24 * 60 * 60 * 1000);
			hour = (diff / (60 * 60 * 1000) - day * 24);
			min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
			// sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min *
			// 60);
			if (day >= 1) {
				return day + "天";
			} else if (hour >= 1) {
				return hour + "小时";
			} else {
				return min + "分";
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return day + "天" + hour + "小时" + min + "分";
	}

	/**
	 * 如果是昨天就显示年月日，今天的只显示时间；
	 * 
	 * @param time
	 * @return
	 */
	public static String getConversationTime(String str1) {
		String result = "";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date one;
		Date two;
		try {
			one = df.parse(str1);
			two = new Date();

			if (one.getYear() == two.getYear() && one.getMonth() == two.getMonth() && one.getDay() == two.getDay()) {
				result = "今天  " + dateToString("HH:mm", one);
				;
			} else {
				result = dateToString("yyyy-MM-dd", one);
			}
		} catch (ParseException e) {
		}
		return result;
	}

	public static String millSecondToDateExt(long millSecond, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String time = sdf.format(new Date(millSecond));
		return time;
	}

	/**
	 * 判断是否是下一个月
	 * 
	 * @param clickDate
	 * @param currentYear
	 * @param currentMonth
	 * @return 1 为点击的是下个月，-1点击的是上个月,0为本月
	 */
	public static int isNextMonth(Date clickDate, int currentYear, int currentMonth) {
		int clickYear = clickDate.getYear() + 1900;
		int clickMonth = clickDate.getMonth();
		if (clickYear > currentYear) {
			return 1;
		} else if (clickYear == currentYear) {
			if (clickMonth > currentMonth) {
				return 1;
			} else if (clickMonth < currentMonth) {
				return -1;
			} else {
				return 0;
			}
		} else {
			return -1;
		}
	}

	/**
	 * 判断日期是否是开始日期之后
	 * 
	 * @param startTime
	 * @param endTime
	 * @return true 表示和开始日期相同或者开始日期之后， false表示开始日期之前；
	 */
	@SuppressWarnings("deprecation")
	public static boolean isAfterStartTime(String endTime, String startTime) {
		Date startDate = stringToDate("yyyy-MM-dd", startTime);
		Date endDate = stringToDate("yyyy-MM-dd", endTime);
		if (endDate.getYear() < startDate.getYear()) {
			return false;
		} else if (endDate.getYear() == startDate.getYear()) {
			if (endDate.getMonth() < startDate.getMonth()) {
				return false;
			} else if (endDate.getMonth() == startDate.getMonth()) {
				if (endDate.getDate() < startDate.getDate()) {
					return false;
				} else {
					return true;
				}
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	/**
	 * 起始时间是否超过30天
	 * 
	 * @param startTime
	 * @param endTime
	 * @return true 表示超过了，false表示没超过（30天也不算超过）
	 */
	public static boolean isMoreThanThirtyDays(String startTime, String endTime) {
		Date startDate = stringToDate("yyyy-MM-dd", startTime);
		Date endDate = stringToDate("yyyy-MM-dd", endTime);
		long start = startDate.getTime();
		long end = endDate.getTime();
		int day = (int) ((start - end) / (24 * 60 * 60 * 1000));
		if (day + 1 > 30) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 开始和结束时间是否包含忙碌和雇佣；
	 * 
	 * @param startTime
	 * @param endTime
	 * @param timeMap
	 * @return true 表示包含， false 包含不包含
	 */
	public static boolean isContainBusyTime(String startTime, String endTime, Map<String, Integer> timeMap) {
		boolean temp = false;
		Date startDate = stringToDate("yyyy-MM-dd", startTime);
		Date endDate = stringToDate("yyyy-MM-dd", endTime);
		long start = startDate.getTime();
		long end = endDate.getTime();
		int day = (int) ((end - start) / (24 * 60 * 60 * 1000));
		Calendar cal = Calendar.getInstance();
		for (int i = 0; i < day; i++) {
			cal.setTime(startDate);
			cal.add(Calendar.DATE, i);
			Date date = cal.getTime();
			String dateStr = dateToString("yyyy-MM-dd", date);
			if (timeMap.containsKey(dateStr)) {
				temp = true;
				break;
			}
		}
		return temp;
	}

	/**
	 * 生日获取属相；
	 * 
	 * @param birthday
	 * @return
	 */
	public static String date2Zodica(Date birthday) {
		final String[] zodiacArr = { "猴", "鸡", "狗", "猪", "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊" };
		Calendar cal = Calendar.getInstance();
		cal.setTime(birthday);
		return zodiacArr[cal.get(Calendar.YEAR) % 12];
	}

	/**
	 * 根据日期获取星座
	 * 
	 * @param time
	 * @return
	 */
	public static String date2Constellation(Date birthday) {
		final String[] constellationArr = { "水瓶座", "双鱼座", "牡羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "魔羯座" };
		final int[] constellationEdgeDay = { 20, 19, 21, 21, 21, 22, 23, 23, 23, 23, 22, 22 };
		Calendar cal = Calendar.getInstance();
		cal.setTime(birthday);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		if (day < constellationEdgeDay[month]) {
			month = month - 1;
		}
		if (month >= 0) {
			return constellationArr[month];
		}
		// default to return 魔羯
		return constellationArr[11];
	}
}

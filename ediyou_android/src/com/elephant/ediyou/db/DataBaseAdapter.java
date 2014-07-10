/**
 * 
 */
package com.elephant.ediyou.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.elephant.ediyou.bean.CitysBean;
import com.elephant.ediyou.bean.HobbyBean;
import com.elephant.ediyou.bean.ProvincesBean;

/**
 * 数据库操作类
 * 
 * @author Arvin 说明： 1、数据库操作类 2、定义好数据表名，数据列，数据表创建语句 3、操作表的方法紧随其后
 */
public class DataBaseAdapter {
	/**
	 * 数据库版本
	 */
	private static final int DATABASE_VERSION = 1;
	/**
	 * 数据库名称
	 */
	private static final String DATABASE_NAME = "ele4android.db";
	/**
	 * 数据库表id
	 */
	public static final String RECORD_ID = "_id";

	private SQLiteDatabase db;
	private ReaderDbOpenHelper dbOpenHelper;

	public DataBaseAdapter(Context context) {
		this.dbOpenHelper = new ReaderDbOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void open() {
		this.db = dbOpenHelper.getWritableDatabase();
	}

	public void close() {
		if (db != null) {
			db.close();
		}
		if (dbOpenHelper != null) {
			dbOpenHelper.close();
		}
	}

	private class ReaderDbOpenHelper extends SQLiteOpenHelper {

		public ReaderDbOpenHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
			// 创建表
			_db.execSQL(CREATE_SQL_PROVINCES);
			_db.execSQL(CREATE_SQL_CITYS);
			_db.execSQL(CREATE_SQL_HOTCITYS);
			_db.execSQL(CREATE_SQL_HOOBBYS);
			_db.execSQL(CREATE_SQL_USER_HOBBYS);

		}

		/**
		 * 升级应用时，有数据库改动在此方法中修改。
		 */
		@Override
		public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {

		}
	}

	/************************************************** 省市级联 ********************************************************/

	/****************************** 省表 ********************************/
	/**
	 * 省表
	 */
	public static final String TABLE_NAME_PROVINCES = "t_provinces";

	/**
	 * 省表中的列定义
	 * 
	 * @author Aizhimin
	 */
	public interface ProvincesColumns {
		public static final String PROVINCEID = "provinceId";
		public static final String NAME = "name";
	}

	/**
	 * 省表查询列
	 */
	public static final String[] PROJECTION_PROVINCES = new String[] { RECORD_ID, ProvincesColumns.PROVINCEID, ProvincesColumns.NAME };
	/**
	 * 省表的创建语句
	 */
	public static final String CREATE_SQL_PROVINCES = "create table " + TABLE_NAME_PROVINCES + " (" + RECORD_ID
			+ " integer primary key autoincrement," + ProvincesColumns.PROVINCEID + " integer, " + ProvincesColumns.NAME + " text " + ");";

	/**
	 * 批量插入省信息
	 * 
	 * @param scbList
	 */
	public synchronized void bantchProvinces(List<ProvincesBean> provincesList) {
		SQLiteDatabase localDb = db;
		try {
			localDb.beginTransaction();
			localDb.delete(TABLE_NAME_PROVINCES, null, null);
			for (ProvincesBean provincesBean : provincesList) {
				String sql = "insert into " + TABLE_NAME_PROVINCES + " (" + ProvincesColumns.PROVINCEID + "," + ProvincesColumns.NAME
						+ ") values(?,?)";
				localDb.execSQL(sql, new Object[] { provincesBean.getId(), provincesBean.getName() });
			}
			localDb.setTransactionSuccessful();
		} finally {
			localDb.endTransaction();
		}
	}

	/**
	 * 获取省数据
	 * 
	 * @return
	 */
	public List<ProvincesBean> findAllProvinces() {
		List<ProvincesBean> provincesList = new ArrayList<ProvincesBean>();
		Cursor c = db.query(TABLE_NAME_PROVINCES, PROJECTION_PROVINCES, null, null, null, null, ProvincesColumns.PROVINCEID);
		while (c.moveToNext()) {
			ProvincesBean province = new ProvincesBean();
			province.setId(c.getInt(1));
			province.setName(c.getString(2));
			provincesList.add(province);
		}
		c.close();
		return provincesList;
	}

	/****************************** 城市表 ********************************/

	/**
	 * 城市表
	 */
	public static final String TABLE_NAME_CITYS = "t_citys";

	/**
	 * 城市表中的列定义
	 * 
	 * @author Aizhimin
	 */
	public interface CitysColumns {
		public static final String CITYID = "cityId";
		public static final String NAME = "name";
		public static final String PROVINCEID = "provinceId";
		public static final String STATE = "state";
	}

	/**
	 * 城市表查询列
	 */
	public static final String[] PROJECTION_CITYS = new String[] { RECORD_ID, CitysColumns.CITYID, CitysColumns.NAME, CitysColumns.PROVINCEID,
			CitysColumns.STATE };
	/**
	 * 城市表的创建语句
	 */
	public static final String CREATE_SQL_CITYS = "create table " + TABLE_NAME_CITYS + " (" + RECORD_ID + " integer primary key autoincrement,"
			+ CitysColumns.CITYID + " integer, " + CitysColumns.NAME + " text, " + CitysColumns.PROVINCEID + " integer, " + CitysColumns.STATE
			+ " integer " + ");";

	/**
	 * 批量插入城市信息
	 * 
	 * @param scbList
	 */
	public synchronized void bantchCitys(List<CitysBean> citysList) {
		SQLiteDatabase localDb = db;
		try {
			localDb.beginTransaction();
			localDb.delete(TABLE_NAME_CITYS, null, null);
			for (CitysBean citysBean : citysList) {
				String sql = "insert into " + TABLE_NAME_CITYS + " (" + CitysColumns.CITYID + "," + CitysColumns.NAME + "," + CitysColumns.PROVINCEID
						+ "," + CitysColumns.STATE + ") values(?,?,?,?)";
				localDb.execSQL(sql, new Object[] { citysBean.getId(), citysBean.getName(), citysBean.getProvinceId(), citysBean.getState() });
			}
			localDb.setTransactionSuccessful();
		} finally {
			localDb.endTransaction();
		}
	}

	/************************ 省、城市表联合查询,查寻对应省份的城市 **************************/

	/**
	 * 省、城市表联合查询,查寻对应省份的城市数据
	 * 
	 * @return
	 */
	public List<String> findCitysByProvinceId(Integer provinceId) {
		List<String> cityNameList = new ArrayList<String>();
		Cursor c = db.rawQuery(
				"select t_citys.name from t_citys,t_provinces where t_citys.provinceId = t_provinces.provinceId and t_citys.provinceId = ?",
				new String[] { provinceId + "" });
		while (c.moveToNext()) {
			int columniIndex = c.getColumnIndex("name");
			cityNameList.add(c.getString(columniIndex));
		}
		c.close();
		return cityNameList;
	}

	/************************ 热门城市 **************************/

	/**
	 * 热门城市表
	 */
	public static final String TABLE_NAME_HOTCITYS = "t_hotcitys";

	/**
	 * 热门城市表中的列定义
	 * 
	 * @author Aizhimin
	 */
	public interface HotCitysColumns {
		public static final String CITYID = "cityId";
		public static final String NAME = "name";
		public static final String PROVINCEID = "provinceId";
		public static final String STATE = "state";
	}

	/**
	 * 热门城市表查询列
	 */
	public static final String[] PROJECTION_HOTCITYS = new String[] { RECORD_ID, CitysColumns.CITYID, CitysColumns.NAME, CitysColumns.PROVINCEID,
			CitysColumns.STATE };
	/**
	 * 热门城市表的创建语句
	 */
	public static final String CREATE_SQL_HOTCITYS = "create table " + TABLE_NAME_HOTCITYS + " (" + RECORD_ID + " integer primary key autoincrement,"
			+ CitysColumns.CITYID + " integer, " + CitysColumns.NAME + " text, " + CitysColumns.PROVINCEID + " integer, " + CitysColumns.STATE
			+ " integer " + ");";

	/**
	 * 批量插入热门城市信息
	 * 
	 * @param scbList
	 */
	public synchronized void bantchHotCitys(List<CitysBean> citysList) {
		SQLiteDatabase localDb = db;
		try {
			localDb.beginTransaction();
			localDb.delete(TABLE_NAME_HOTCITYS, null, null);
			for (CitysBean citysBean : citysList) {
				String sql = "insert into " + TABLE_NAME_HOTCITYS + " (" + CitysColumns.CITYID + "," + CitysColumns.NAME + ","
						+ CitysColumns.PROVINCEID + "," + CitysColumns.STATE + ") values(?,?,?,?)";
				localDb.execSQL(sql, new Object[] { citysBean.getId(), citysBean.getName(), citysBean.getProvinceId(), citysBean.getState() });
			}
			localDb.setTransactionSuccessful();
		} finally {
			localDb.endTransaction();
		}
	}

	/**
	 * 获取热门城市数据
	 * 
	 * @return
	 */
	public List<CitysBean> findAllHotCitys() {
		List<CitysBean> hotCitysList = new ArrayList<CitysBean>();
		Cursor c = db.query(TABLE_NAME_HOTCITYS, PROJECTION_HOTCITYS, null, null, null, null, HotCitysColumns.CITYID);
		while (c.moveToNext()) {
			CitysBean city = new CitysBean();
			city.setId(c.getInt(1));
			city.setName(c.getString(2));
			city.setProvinceId(c.getInt(3));
			city.setState(c.getInt(4));
			hotCitysList.add(city);
		}
		c.close();
		return hotCitysList;
	}

	/**
	 * 查找省份id和城市id；
	 * 
	 * @param province
	 * @param city
	 * @return 数组第一个是省ID，第二个是城市ID
	 */
	public int[] findProvinceCityId(String province, String city) {
		int[] ids = new int[2];
		String sql = "select t_provinces.provinceId , t_citys.cityId from t_provinces,t_citys where t_provinces.name = ? and t_citys.name = ? ";
		Cursor cur = db.rawQuery(sql, new String[] { province, city });
		while (cur.moveToNext()) {
			int provincIndex = cur.getColumnIndex("provinceId");
			int provinceId = cur.getInt(provincIndex);

			int cityIndex = cur.getColumnIndex("cityId");
			int cityId = cur.getInt(cityIndex);
			ids[0] = provinceId;
			ids[1] = cityId;
		}
		cur.close();
		return ids;
	}

	/**
	 * 通过GPS、数据库获取的城市名来获取城市id
	 * 
	 * @param cityName
	 * @return
	 */
	public int findCityId(String cityName) {
		int cityId = 0;
		// int cityNameLe = cityName.length();
		// String cityNameSh = cityName.substring(0, cityNameLe - 1);
		Cursor cursor = db.query(TABLE_NAME_CITYS, null, "name like ?", new String[] { "%" + cityName + "%" }, null, null, null);
		while (cursor.moveToNext()) {
			int cityIndex = cursor.getColumnIndex("cityId");
			cityId = cursor.getInt(cityIndex);
		}
		cursor.close();
		return cityId;
	}

	/**
	 * 通过GPS、数据库获取的城市名来获取省份（直辖市）id
	 * 
	 * @param cityName
	 * @return
	 */
	public int findProvinceId(String pName) {
		int pId = 0;
		// int cityNameLe = cityName.length();
		// String cityNameSh = cityName.substring(0, cityNameLe - 1);
		Cursor cursor = db.query(TABLE_NAME_PROVINCES, null, "name like ?", new String[] { "%" + pName + "%" }, null, null, null);
		while (cursor.moveToNext()) {
			int pIndex = cursor.getColumnIndex("provinceId");
			pId = cursor.getInt(pIndex);
		}
		cursor.close();
		return pId;
	}

	/************************ 所有爱好标签数据库 **************************/

	/**
	 * 爱好表
	 */
	public static final String TABLE_NAME_HOBBYS = "t_hobbys";

	/**
	 * 爱好表中的列定义
	 * 
	 * @author Aizhimin
	 */
	public interface HobbysColumns {
		public static final String HOBBY_ID = "hobbyId";
		public static final String HOBBY_NAME = "hobby_name";
	}

	/**
	 * 爱好表查询列
	 */
	public static final String[] PROJECTION_HOBBYS = new String[] { RECORD_ID, HobbysColumns.HOBBY_ID, HobbysColumns.HOBBY_NAME };
	/**
	 * 爱好表的创建语句
	 */
	public static final String CREATE_SQL_HOOBBYS = "create table " + TABLE_NAME_HOBBYS + " (" + RECORD_ID + " integer primary key autoincrement,"
			+ HobbysColumns.HOBBY_ID + " integer, " + HobbysColumns.HOBBY_NAME + " text " + ");";

	/**
	 * 批量插入爱好信息
	 * 
	 * @param scbList
	 */
	public synchronized void bantchHobbys(List<HobbyBean> hobbyBeansList) {
		SQLiteDatabase localDb = db;
		try {
			localDb.beginTransaction();
			localDb.delete(TABLE_NAME_HOBBYS, null, null);
			for (HobbyBean hobbyBean : hobbyBeansList) {
				String sql = "insert into " + TABLE_NAME_HOBBYS + " (" + HobbysColumns.HOBBY_ID + "," + HobbysColumns.HOBBY_NAME + ") values(?,?)";
				localDb.execSQL(sql, new Object[] { hobbyBean.getId(), hobbyBean.getName() });
			}
			localDb.setTransactionSuccessful();
		} finally {
			localDb.endTransaction();
		}
	}

	/**
	 * 获取所有爱好标签数据
	 * 
	 * @return
	 */
	public List<HobbyBean> findAllHobbys() {
		List<HobbyBean> hobbyBeansList = new ArrayList<HobbyBean>();
		Cursor c = db.query(TABLE_NAME_HOBBYS, PROJECTION_HOBBYS, null, null, null, null, HobbysColumns.HOBBY_ID);
		while (c.moveToNext()) {
			HobbyBean hobby = new HobbyBean();
			hobby.setId(c.getInt(1));
			hobby.setName(c.getString(2));
			hobbyBeansList.add(hobby);
		}
		c.close();
		return hobbyBeansList;
	}

	/************************ 当前手机用户已选爱好标签数据库 **************************/

	/**
	 * 已选爱好表
	 */
	public static final String TABLE_NAME_USER_HOBBYS = "t_user_hobbys";

	/**
	 * 已选爱好表中的列定义
	 * 
	 * @author Aizhimin
	 */
	public interface UserHobbysColumns {
		public static final String HOBBY_ID = "hobbyId";
		public static final String HOBBY_NAME = "hobby_name";
	}

	/**
	 * 已选爱好表查询列
	 */
	public static final String[] PROJECTION_USER_HOBBYS = new String[] { RECORD_ID, UserHobbysColumns.HOBBY_ID, UserHobbysColumns.HOBBY_NAME };
	/**
	 * 已选爱好表的创建语句
	 */
	public static final String CREATE_SQL_USER_HOBBYS = "create table " + TABLE_NAME_USER_HOBBYS + " (" + RECORD_ID
			+ " integer primary key autoincrement," + UserHobbysColumns.HOBBY_ID + " integer, " + UserHobbysColumns.HOBBY_NAME + " text " + ");";

	/**
	 * 批量插入已选爱好信息
	 * 
	 * @param scbList
	 */
	public synchronized void bantchUserHobbys(List<HobbyBean> hobbyBeansList) {
		SQLiteDatabase localDb = db;
		try {
			localDb.beginTransaction();
			localDb.delete(TABLE_NAME_USER_HOBBYS, null, null);
			for (HobbyBean hobbyBean : hobbyBeansList) {
				String sql = "insert into " + TABLE_NAME_USER_HOBBYS + " (" + UserHobbysColumns.HOBBY_ID + "," + UserHobbysColumns.HOBBY_NAME
						+ ") values(?,?)";
				localDb.execSQL(sql, new Object[] { hobbyBean.getId(), hobbyBean.getName() });
			}
			localDb.setTransactionSuccessful();
		} finally {
			localDb.endTransaction();
		}
	}

	/**
	 * 获取所有已选爱好标签数据
	 * 
	 * @return
	 */
	public List<HobbyBean> findAllUserHobbys() {
		List<HobbyBean> hobbyBeansList = new ArrayList<HobbyBean>();
		Cursor c = db.query(TABLE_NAME_USER_HOBBYS, PROJECTION_USER_HOBBYS, null, null, null, null, UserHobbysColumns.HOBBY_ID);
		while (c.moveToNext()) {
			HobbyBean hobby = new HobbyBean();
			hobby.setId(c.getInt(1));
			hobby.setName(c.getString(2));
			hobbyBeansList.add(hobby);
		}
		c.close();
		return hobbyBeansList;
	}

	/**
	 * 返回一个爱好Id的字符串（含“,”号）和一个爱好名称的字符串（含“,”号）
	 * 
	 * @return String{id,name}。String[0] = id;String[1] = name;
	 */
	public String[] findHobbysIdAndStr() {
		List<HobbyBean> hobbyBeansList = new ArrayList<HobbyBean>();
		hobbyBeansList = findAllUserHobbys();
		String hobbyStrs = "";
		String hobbyIds = "";
		if (hobbyBeansList != null && hobbyBeansList.size() > 0) {
			for (int i = 0; i < hobbyBeansList.size(); i++) {
				HobbyBean hobbyBean = hobbyBeansList.get(i);
				hobbyStrs = hobbyStrs + hobbyBean.getName() + ",";
				hobbyIds = hobbyIds + hobbyBean.getId() + ",";
			}
			if (!TextUtils.isEmpty(hobbyStrs)) {
				hobbyStrs = hobbyStrs.substring(0, hobbyStrs.length() - 1);
			}
			if (!TextUtils.isEmpty(hobbyIds)) {
				hobbyIds = hobbyIds.substring(0, hobbyIds.length() - 1);
			}
		}
		String[] userHobbyArr = new String[2];
		userHobbyArr[0] = hobbyIds;
		userHobbyArr[1] = hobbyStrs;
		return userHobbyArr;
	}

	/**
	 * 根据id的字符串（含“,”号）返回一个爱好名称的字符串（含“,”号）
	 * 
	 * @param hobbyId
	 * @return
	 */
	public String hobbyNameStrById(String hobbyIds) {
		if (!TextUtils.isEmpty(hobbyIds)) {
			String[] hobbyStrIds = hobbyIds.split(",");
			List<HobbyBean> hobbyBeansList = new ArrayList<HobbyBean>();
			hobbyBeansList = findAllHobbys();
			String hobbyName = "";
			for (int i = 0; i < hobbyStrIds.length; i++) {
				int hobbyId = Integer.parseInt(hobbyStrIds[i]);
				for (int j = 0; j < hobbyBeansList.size(); j++) {
					if (hobbyId == hobbyBeansList.get(j).getId()) {
						hobbyName = hobbyName + hobbyBeansList.get(j).getName() + ",";
					}
				}
			}
			return hobbyName.substring(0, hobbyName.length() - 1);
		} else {
			return "";
		}

	}

	/**
	 * 根据id的字符串（含“,”号）返回一个爱好名称的字符串（含“,”号）
	 * 
	 * @param hobbyId
	 * @return
	 */
	public String hobbyIdStrByName(String hobbyNames) {
		if (!TextUtils.isEmpty(hobbyNames)) {
			String[] hobbyStrNames = hobbyNames.split(",");
			List<HobbyBean> hobbyBeansList = new ArrayList<HobbyBean>();
			hobbyBeansList = findAllHobbys();
			String hobbyId = "";
			for (int i = 0; i < hobbyStrNames.length; i++) {
				String hobbyName = hobbyStrNames[i];
				for (int j = 0; j < hobbyBeansList.size(); j++) {
					if (hobbyName.equals(hobbyBeansList.get(j).getName())) {
						hobbyId = hobbyId + hobbyBeansList.get(j).getId() + ",";
					}
				}
			}
			return hobbyId.substring(0, hobbyId.length() - 1);
		} else {
			return "";
		}

	}

}

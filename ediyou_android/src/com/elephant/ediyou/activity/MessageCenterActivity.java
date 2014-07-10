/**
 * 
 */
package com.elephant.ediyou.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.elephant.ediyou.AsyncImageLoader;
import com.elephant.ediyou.AsyncImageLoader.ImageCallback;
import com.elephant.ediyou.CommonApplication;
import com.elephant.ediyou.Constants;
import com.elephant.ediyou.R;
import com.elephant.ediyou.SystemException;
import com.elephant.ediyou.activity.impl.IBaseActivity;
import com.elephant.ediyou.bean.BlackListBean;
import com.elephant.ediyou.bean.LetterListBean;
import com.elephant.ediyou.bean.NotifyBean;
import com.elephant.ediyou.bean.UserBean;
import com.elephant.ediyou.helper.BusinessHelper;
import com.elephant.ediyou.util.DateUtil;
import com.elephant.ediyou.util.ImageUtil;
import com.elephant.ediyou.util.NetUtil;
import com.elephant.ediyou.util.SharedPrefUtil;
import com.elephant.ediyou.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 我的订单（考拉）
 * 
 * @author Arvin
 * 
 */
public class MessageCenterActivity extends Activity implements IBaseActivity, OnClickListener {
	private CommonApplication app;
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;
	private ProgressDialog pd;

	private TextView tvShowNo;// 当没有数据时显示

	private Button btn_msg;// 消息
	private Button btn_sys_msg;// 系统消息
	private Button btn_black_list;// 黑名单

	private static ListView lvOrder;
	private int pageNo = 1;// 起始页
	private int pageSize = 10;// 每页个数
	private int totalPage = -1;// 总页数
	private boolean LIST_RECORD_TASK_RUNING = false;// 加载记录的任务是否执行中，如果执行中（true），就不再创建新的任务，
	private UserBean userBean;
	private long userId;
	private String accessToken;
	private int g_listType = 0;

	// 私信
	private List<LetterListBean> letterListBeans;
	private LetterListAdapter letterAdapter;
	// 黑名单
	private List<BlackListBean> blackListBean;
	private BlackListAdapter blackListAdapter;
	// 系统消息
	private List<NotifyBean> notifyListBean;
	private SystemMsgAdapter sysMsgAdapter;

	private Map<String, Integer> faceMap = new HashMap<String, Integer>();
	private int[] faceRes = new int[] { R.drawable.ic_face_001, R.drawable.ic_face_002, R.drawable.ic_face_003, R.drawable.ic_face_004,
			R.drawable.ic_face_005, R.drawable.ic_face_006, R.drawable.ic_face_007, R.drawable.ic_face_008, R.drawable.ic_face_009,
			R.drawable.ic_face_010, R.drawable.ic_face_011, R.drawable.ic_face_012, R.drawable.ic_face_013, R.drawable.ic_face_014,
			R.drawable.ic_face_015, R.drawable.ic_face_016 };
	int haveFacePic = -1;// 一段对话中是否含有表情图片；是，0；否，-1。

	private String title = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_center);

		for (int i = 0; i < faceRes.length; i++) {
			String j;
			int k = i + 1;
			if (k < 10) {
				j = "00" + k;
			} else if (k < 100) {
				j = "0" + k;
			} else {
				j = "" + k;
			}
			String key = "[edu" + j + "]";
			faceMap.put(key, faceRes[i]);
		}

		if (getIntent() != null) {
			title = getIntent().getStringExtra("title");
		}

		app = (CommonApplication) getApplication();
		userBean = SharedPrefUtil.getUserBean(this);
		userId = userBean.getUserId();
		accessToken = userBean.getAccessToken();
		findView();
		fillData();
		if (NetUtil.checkNet(this)) {
			if (!LIST_RECORD_TASK_RUNING) {
				// profileMyOrdersTask = new ProfileMyOrdersTask(userId, pageNo,
				// pageSize,
				// accessToken, state, state2, state3);
				// profileMyOrdersTask.execute();
			}
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
		}
		app.addActivity(this);
	}

	@Override
	public void findView() {
		btnLeft = (Button) findViewById(R.id.btnLeft);
		btnLeft.setBackgroundResource(R.drawable.ic_back__selector);
		btnRight = (Button) findViewById(R.id.btnRight);
		btnRight.setVisibility(View.INVISIBLE);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setVisibility(View.VISIBLE);
		btn_msg = (Button) findViewById(R.id.btn_msg);
		btn_sys_msg = (Button) findViewById(R.id.btn_sys_msg);
		btn_black_list = (Button) findViewById(R.id.btn_black_list);

		tvShowNo = (TextView) findViewById(R.id.tvShowNo);

		btnLeft.setOnClickListener(this);
		btn_msg.setOnClickListener(this);
		btn_sys_msg.setOnClickListener(this);
		btn_black_list.setOnClickListener(this);

		lvOrder = (ListView) findViewById(R.id.lv_order_list);
		lvOrder.setOnItemClickListener(itemClickListener);
		registerForContextMenu(lvOrder);
		lvOrder.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				LetterListBean bean = letterListBeans.get(position);
				if (bean != null) {
					createDialog(bean.getNickName(), position);
				}
				return true;
			}
		});
	}

	/**
	 * dialog
	 * 
	 * @param v
	 */
	private void createDialog(String title, final int clickPosition) {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(title);
		ab.setItems(new String[] { "加入黑名单", "删除会话", "取消" }, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch (which) {
				case 0:// 加入黑名单
					if (NetUtil.checkNet(MessageCenterActivity.this)) {
						int fId = letterListBeans.get(clickPosition).getFriendId();
						new BlackTask(fId).execute();
					} else {
						Toast.makeText(MessageCenterActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
					}
					break;
				case 1:// 删除会话

					break;
				case 2:

					break;
				}
			}
		});
		ab.show();
	}

	@Override
	public void fillData() {
		tvTitle.setText("消息中心");
		/*
		 * letterListBeans = new ArrayList<LetterListBean>(); letterAdapter =
		 * new LetterListAdapter(this); lvOrder.setAdapter(letterAdapter); state
		 * = Constants.ROO_REFUSE; state2 = Constants.KOALA_NOT_PAY; state3 =
		 * Constants.ONLINE_PAY;
		 */
		if (NetUtil.checkNet(this)) {
			if (!LIST_RECORD_TASK_RUNING) {
				pageNo = 1;
				new LetterListTask().execute();
			}
		} else {
			Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLeft:
			if (title == null) {
				finish();
			} else {
				if (userBean.getIsKangaroo() == 0) {
					startActivity(new Intent(this, KoalaSelfCenterActivity.class));
				} else if (userBean.getIsKangaroo() == 1) {
					startActivity(new Intent(this, RooSelfCenterActivity.class));
				}

			}

			break;
		case R.id.btn_msg:// 消息
			tvShowNo.setVisibility(View.GONE);
			btn_msg.setBackgroundResource(R.drawable.order_tab_bg_sel);
			btn_msg.setTextColor(Color.rgb(157, 208, 99));
			btn_sys_msg.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btn_sys_msg.setTextColor(Color.rgb(201, 195, 179));
			btn_black_list.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btn_black_list.setTextColor(Color.rgb(201, 195, 179));
			if (null != letterListBeans) {
				letterListBeans.clear();
			}
			if (null != letterAdapter) {
				letterAdapter.notifyDataSetChanged();
			}
			if (null != notifyListBean) {
				notifyListBean.clear();
			}
			if (null != sysMsgAdapter) {
				sysMsgAdapter.notifyDataSetChanged();
			}
			if (null != blackListBean) {
				blackListBean.clear();
			}
			if (null != blackListAdapter) {
				blackListAdapter.notifyDataSetChanged();
			}

			lvOrder.setOnItemClickListener(itemClickListener);

			if (NetUtil.checkNet(this)) {
				if (!LIST_RECORD_TASK_RUNING) {
					pageNo = 1;
					new LetterListTask().execute();
				}
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_sys_msg:// 系统消息
			tvShowNo.setVisibility(View.GONE);
			btn_msg.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btn_msg.setTextColor(Color.rgb(201, 195, 179));
			btn_sys_msg.setBackgroundResource(R.drawable.order_tab_bg_sel);
			btn_sys_msg.setTextColor(Color.rgb(157, 208, 99));
			btn_black_list.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btn_black_list.setTextColor(Color.rgb(201, 195, 179));
			if (null != letterListBeans) {
				letterListBeans.clear();
			}
			if (null != letterAdapter) {
				letterAdapter.notifyDataSetChanged();
			}
			if (null != notifyListBean) {
				notifyListBean.clear();
			}
			if (null != sysMsgAdapter) {
				sysMsgAdapter.notifyDataSetChanged();
			}
			if (null != blackListBean) {
				blackListBean.clear();
			}
			if (null != blackListAdapter) {
				blackListAdapter.notifyDataSetChanged();
			}
			lvOrder.setOnItemClickListener(null);

			if (NetUtil.checkNet(this)) {
				if (!LIST_RECORD_TASK_RUNING) {
					pageNo = 1;
					new SystemMsgTask().execute();
				}
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_black_list:// 黑名单
			tvShowNo.setVisibility(View.GONE);
			btn_msg.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btn_msg.setTextColor(Color.rgb(201, 195, 179));
			btn_sys_msg.setBackgroundResource(R.drawable.order_tab_bg_nor);
			btn_sys_msg.setTextColor(Color.rgb(201, 195, 179));
			btn_black_list.setBackgroundResource(R.drawable.order_tab_bg_sel);
			btn_black_list.setTextColor(Color.rgb(157, 208, 99));
			if (null != letterListBeans) {
				letterListBeans.clear();
			}
			if (null != letterAdapter) {
				letterAdapter.notifyDataSetChanged();
			}
			if (null != notifyListBean) {
				notifyListBean.clear();
			}
			if (null != sysMsgAdapter) {
				sysMsgAdapter.notifyDataSetChanged();
			}
			if (null != blackListBean) {
				blackListBean.clear();
			}
			if (null != blackListAdapter) {
				blackListAdapter.notifyDataSetChanged();
			}
			lvOrder.setOnItemClickListener(null);

			if (NetUtil.checkNet(this)) {
				if (!LIST_RECORD_TASK_RUNING) {
					pageNo = 1;
					new BlackListTask().execute();
				}
			} else {
				Toast.makeText(this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}

	}

	OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			LetterListBean bean = letterListBeans.get(position);
			if (bean != null) {
				long friendId = bean.getFriendId();
				String name = bean.getNickName();
				letterListBeans.get(position).setStatus(1);
				letterAdapter.notifyDataSetChanged();
				Intent letter = new Intent(MessageCenterActivity.this, PersonalLetterActivity.class);
				letter.putExtra(Constants.EXTRA_USER_ID, friendId);
				letter.putExtra(Constants.EXTRA_NAME, name);
				letter.putExtra(Constants.EXTRA_AVATAR, bean.getAvaterurl());
				letter.putExtra("isRoo", bean.getIsRoo());
				startActivity(letter);
			}
		}
	};

	/**
	 * 黑名单列表接口
	 * 
	 * @author syghh
	 * 
	 */
	private class BlackListAdapter extends BaseAdapter {
		private Context mContext;
		private List<BlackListBean> eventBeans;
		private boolean isNull = false;
		private Handler handle;

		public BlackListAdapter(Context context, Handler hdl) {
			this.mContext = context;
			handle = hdl;
		}

		public void setData(List<BlackListBean> eventBeans) {
			if (eventBeans.isEmpty()) {
				isNull = true;
			} else {
				isNull = false;
			}
			this.eventBeans = eventBeans;
		}

		public void add(List<BlackListBean> eventBeans) {
			this.eventBeans.addAll(eventBeans);
			this.notifyDataSetChanged();
		}

		private void clear() {
			if (eventBeans != null)
				eventBeans.clear();
			isNull = true;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return eventBeans.size();
		}

		@Override
		public Object getItem(int position) {
			return eventBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;

			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.message_blacklist_item, null);
				viewHolder = createViewHolderByConvertView(convertView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			fillData(position, viewHolder, convertView);

			return convertView;
		}

		private ViewHolder createViewHolderByConvertView(View convertView) {
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.ivBlackListPhoto = (ImageView) convertView.findViewById(R.id.ivBlackListPhoto);
			viewHolder.ivBackListSex = (ImageView) convertView.findViewById(R.id.ivBackListSex);
			viewHolder.tvBlackListLoginName = (TextView) convertView.findViewById(R.id.tvBlackListLoginName);
			viewHolder.tvBlackListAge = (TextView) convertView.findViewById(R.id.tvBlackListAge);
			viewHolder.tvBlackListKLevel = (TextView) convertView.findViewById(R.id.tvBlackListKLevel);
			viewHolder.tvBlackListCityName = (TextView) convertView.findViewById(R.id.tvBlackListCityName);
			viewHolder.ivBlackListDelete = (ImageView) convertView.findViewById(R.id.ivBlackListDelete);
			return viewHolder;
		}

		private void fillData(int position, ViewHolder viewHolder, View convertView) {
			BlackListBean eventBean = eventBeans.get(position);
			setImageByUrl(viewHolder.ivBlackListPhoto, eventBean.getAvatarUrl());

			if (position % 2 == 0) {
				convertView.setBackgroundResource(R.drawable.bg_repeat);
			} else {
				convertView.setBackgroundResource(R.drawable.repeat_blod_slant);
			}
			if (eventBean.getGender().equals("m")) {
				viewHolder.ivBackListSex.setBackgroundResource(R.drawable.ic_male);
			} else {
				viewHolder.ivBackListSex.setBackgroundResource(R.drawable.ic_fale);
			}
			viewHolder.tvBlackListLoginName.setText(eventBean.getLoginName());
			viewHolder.tvBlackListLoginName.setText(eventBean.getLoginName());
			viewHolder.tvBlackListAge.setText(eventBean.getAge());
			viewHolder.tvBlackListKLevel.setText(eventBean.getKLevel());
			viewHolder.tvBlackListCityName.setText(eventBean.getCityName());
			viewHolder.ivBlackListDelete.setTag(String.format("%d", position));
			viewHolder.ivBlackListDelete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Bundle bd = new Bundle();
					bd.putInt("location", Integer.parseInt((String) v.getTag()));
					Message msg = new Message();
					msg.setData(bd);
					handle.sendMessage(msg);
				}
			});
		}

		private class ViewHolder {
			ImageView ivBlackListPhoto;
			ImageView ivBackListSex;
			TextView tvBlackListLoginName;
			TextView tvBlackListAge;
			TextView tvBlackListKLevel;
			TextView tvBlackListCityName;
			ImageView ivBlackListDelete;
		}

		private void setImageByUrl(ImageView imageView, String url) {
			if (null == url) {
				return;
			}
			String img = url.replace("\\", "");
			imageView.setTag(img);
			final Drawable cacheDrawable = AsyncImageLoader.getInstance().loadDrawable(img, new ImageCallback() {
				@Override
				public void imageLoaded(Drawable imageDrawable, String imageUrl) {
					ImageView image = null;
					image = (ImageView) lvOrder.findViewWithTag(imageUrl);

					if (image != null) {
						if (imageDrawable != null) {
							image.setImageDrawable(imageDrawable);
						} else {
							image.setImageResource(R.drawable.bg_kangoo_photo_defualt);
						}
					}
				}
			});
			if (cacheDrawable != null) {
				imageView.setImageDrawable(cacheDrawable);
			} else {
				imageView.setImageResource(R.drawable.bg_kangoo_photo_defualt);
			}
		}
	}

	/**
	 * 黑名单列表
	 * 
	 * @author syghh
	 * 
	 */
	class BlackListTask extends AsyncTask<Void, Void, JSONObject> {

		public BlackListTask() {
			LIST_RECORD_TASK_RUNING = true;
			g_listType = Constants.MSG_CENTER_TYPE_BLACK;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(MessageCenterActivity.this);
			}
			pd.setMessage("更新黑名单列表中...");
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().blackList(userId, pageNo, pageSize, accessToken);
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
						totalPage = result.getInt("total");
						JSONArray blackListJson = result.getJSONArray("data");
						blackListBean = BlackListBean.constractList(blackListJson);
						if (blackListAdapter == null || blackListAdapter.getCount() == 0) {
							blackListAdapter = new BlackListAdapter(MessageCenterActivity.this, delBlankHdl);
							blackListAdapter.setData(blackListBean);
							lvOrder.setAdapter(blackListAdapter);
							pageNo = 1;
						} else {
							blackListAdapter.add(blackListBean);
						}
						pageNo = pageNo + 1;
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(MessageCenterActivity.this, R.string.time_out, Toast.LENGTH_SHORT).show();
						startActivity(new Intent(MessageCenterActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						tvShowNo.setVisibility(View.VISIBLE);
//						Toast.makeText(MessageCenterActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(MessageCenterActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
				}/*
				 * catch (SystemException e) { e.printStackTrace(); }
				 */
			} else {
				Toast.makeText(MessageCenterActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
			// 加载记录的任务结束
			cancel(true);
			LIST_RECORD_TASK_RUNING = false;
		}
	}

	/**
	 * 私信会话适配器
	 * 
	 * @author syghh
	 * 
	 */
	private class LetterListAdapter extends BaseAdapter {
		private Context mContext;
		private List<LetterListBean> eventBeans;
		private boolean isNull = false;

		public LetterListAdapter(Context context) {
			this.mContext = context;
		}

		public void setData(List<LetterListBean> eventBeans) {
			if (eventBeans.isEmpty()) {
				isNull = true;
			} else {
				isNull = false;
			}
			this.eventBeans = eventBeans;
		}

		public void add(List<LetterListBean> eventBeans) {
			this.eventBeans.addAll(eventBeans);
			this.notifyDataSetChanged();
		}

		private void clear() {
			if (eventBeans != null)
				eventBeans.clear();
			isNull = true;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return eventBeans.size();
		}

		@Override
		public Object getItem(int position) {
			return eventBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;

			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.message_center_item, null);
				viewHolder = createViewHolderByConvertView(convertView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			fillData(convertView, position, viewHolder);

			return convertView;
		}

		private ViewHolder createViewHolderByConvertView(View convertView) {
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.ivUserPhoto = (ImageView) convertView.findViewById(R.id.ivUserPhoto);
			viewHolder.ivIsRead = (ImageView) convertView.findViewById(R.id.ivIsRead);
			viewHolder.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
			viewHolder.tvCreateTime = (TextView) convertView.findViewById(R.id.tvCreateTime);
			viewHolder.tvOppositeNickName = (TextView) convertView.findViewById(R.id.tvOppositeNickName);
			return viewHolder;
		}

		private void fillData(View convertView, int position, ViewHolder viewHolder) {
			LetterListBean bean = eventBeans.get(position);
			setImageByUrl(viewHolder.ivUserPhoto, bean.getAvaterurl(),bean.getIsRoo());

			if (position % 2 == 0) {
				convertView.setBackgroundResource(R.drawable.bg_repeat);
			} else {
				convertView.setBackgroundResource(R.drawable.repeat_blod_slant);
			}

			String contentStr = bean.getLastLetter();
			SpannableString spannableString = null;
			if (!StringUtil.isBlank(contentStr)) {
				boolean isHaveFacePic = contentStr.contains("[edu");
				if (isHaveFacePic == true) {
					if (faceMap != null) {
						spannableString = ImageUtil.changeTextToEmotions(faceMap, contentStr, MessageCenterActivity.this);
					}
				}
			}

			if (spannableString != null) {
				viewHolder.tvLocation.setText(spannableString);
			} else {
				viewHolder.tvLocation.setText(contentStr);
			}

			// viewHolder.tvLocation.setText(bean.getLastLetter());

			String sendTime = DateUtil.getConversationTime(bean.getSendTime());
			viewHolder.tvCreateTime.setText(sendTime);
			viewHolder.tvOppositeNickName.setText(bean.getNickName());
			if (bean.getStatus() == 0) {
				viewHolder.ivIsRead.setImageResource(R.drawable.msg_unread);
			} else {
				viewHolder.ivIsRead.setImageResource(R.drawable.msg_read);
			}
		}

		private class ViewHolder {
			ImageView ivUserPhoto;
			ImageView ivIsRead;
			TextView tvLocation;
			TextView tvCreateTime;
			TextView tvOppositeNickName;
		}

		private void setImageByUrl(ImageView imageView, String url,final int isRoo) {
			if (null == url) {
				return;
			}
			String img = url.replace("\\", "");
			imageView.setTag(img);
			final Drawable cacheDrawable = AsyncImageLoader.getInstance().loadDrawable(img, new ImageCallback() {
				@Override
				public void imageLoaded(Drawable imageDrawable, String imageUrl) {
					ImageView image = null;
					image = (ImageView) lvOrder.findViewWithTag(imageUrl);

					if (image != null) {
						if (imageDrawable != null) {
							image.setImageDrawable(imageDrawable);
						} else {
							if(isRoo==1){
								image.setImageResource(R.drawable.bg_kangoo_photo_defualt);
							}else
							image.setImageResource(R.drawable.bg_photo_defualt);
						}
					}
				}
			});
			if (cacheDrawable != null) {
				imageView.setImageDrawable(cacheDrawable);
			} else {
				if(isRoo==1){
					imageView.setImageResource(R.drawable.bg_kangoo_photo_defualt);
				}else
				imageView.setImageResource(R.drawable.bg_photo_defualt);
			}
		}
	}

	/**
	 * 私信会话列表
	 * 
	 * @author syghh
	 * 
	 */
	class LetterListTask extends AsyncTask<Void, Void, JSONObject> {

		public LetterListTask() {
			LIST_RECORD_TASK_RUNING = true;
			g_listType = Constants.MSG_CENTER_TYPE_LETTER;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(MessageCenterActivity.this);
			}
			pd.setMessage("更新私信会话列表中...");
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().listConv(userId, pageNo, pageSize, accessToken);
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
						JSONArray notifyListJson = result.getJSONArray("data");
						letterListBeans = LetterListBean.constantsLetterListBean(notifyListJson);
						if (letterAdapter == null || letterAdapter.getCount() == 0) {
							letterAdapter = new LetterListAdapter(MessageCenterActivity.this);
							letterAdapter.setData(letterListBeans);
							lvOrder.setAdapter(letterAdapter);
							pageNo = 1;
						} else {
							letterAdapter.add(letterListBeans);
						}
						pageNo = pageNo + 1;
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(MessageCenterActivity.this, R.string.time_out, Toast.LENGTH_SHORT).show();
						startActivity(new Intent(MessageCenterActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						tvShowNo.setVisibility(View.VISIBLE);
//						Toast.makeText(MessageCenterActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(MessageCenterActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
				}/*
				 * catch (SystemException e) { e.printStackTrace(); }
				 */
			} else {
				Toast.makeText(MessageCenterActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
			// 加载记录的任务结束
			cancel(true);
			LIST_RECORD_TASK_RUNING = false;
		}
	}

	/**
	 * 系统消息列表接口
	 * 
	 * @author syghh
	 * 
	 */
	private class SystemMsgAdapter extends BaseAdapter {
		private Context mContext;
		private List<NotifyBean> eventBeans;
		private boolean isNull = false;

		public SystemMsgAdapter(Context context) {
			this.mContext = context;
		}

		public void setData(List<NotifyBean> eventBeans) {
			if (eventBeans.isEmpty()) {
				isNull = true;
			} else {
				isNull = false;
			}
			this.eventBeans = eventBeans;
		}

		public void add(List<NotifyBean> eventBeans) {
			this.eventBeans.addAll(eventBeans);
			this.notifyDataSetChanged();
		}

		private void clear() {
			if (eventBeans != null)
				eventBeans.clear();
			isNull = true;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return eventBeans.size();
		}

		@Override
		public Object getItem(int position) {
			return eventBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;

			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.message_sys_item, null);
				viewHolder = createViewHolderByConvertView(convertView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			fillData(position, viewHolder, convertView);
			
			return convertView;
		}

		private ViewHolder createViewHolderByConvertView(View convertView) {
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.ivUserPhoto = (ImageView) convertView.findViewById(R.id.ivUserPhoto);
			viewHolder.ivIsRead = (ImageView) convertView.findViewById(R.id.ivIsRead);
			viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tvContent);
			return viewHolder;
		}

		private void fillData(int position, ViewHolder viewHolder, View convertView) {
			final NotifyBean notifyBean = eventBeans.get(position);
			setImageByUrl(viewHolder.ivUserPhoto, notifyBean.getUri());
			if (position % 2 == 0) {
				convertView.setBackgroundResource(R.drawable.bg_repeat);
			} else {
				convertView.setBackgroundResource(R.drawable.repeat_blod_slant);
			}
			if (notifyBean.getStatus() == 0) {
				viewHolder.ivIsRead.setImageResource(R.drawable.msg_unread);
			} else {
				viewHolder.ivIsRead.setImageResource(R.drawable.msg_read);
			}
			viewHolder.tvTitle.setText(notifyBean.getTitle());
			viewHolder.tvContent.setText(notifyBean.getContent());
			//点击进入系统消息详情
			convertView.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MessageCenterActivity.this, MessageSysDetailActivity.class);
					intent.putExtra("SysNotifyBean", notifyBean);
					startActivity(intent);
				}
			});
		}

		private class ViewHolder {
			ImageView ivUserPhoto;
			ImageView ivIsRead;
			TextView tvTitle;
			TextView tvContent;
		}

		private void setImageByUrl(ImageView imageView, String url) {
			if (null == url) {
				return;
			}
			String img = url.replace("\\", "");
			imageView.setTag(img);
			final Drawable cacheDrawable = AsyncImageLoader.getInstance().loadDrawable(img, new ImageCallback() {
				@Override
				public void imageLoaded(Drawable imageDrawable, String imageUrl) {
					ImageView image = null;
					image = (ImageView) lvOrder.findViewWithTag(imageUrl);

					if (image != null) {
						if (imageDrawable != null) {
							image.setImageDrawable(imageDrawable);
						} else {
							image.setImageResource(R.drawable.bg_kangoo_photo_defualt);
						}
					}
				}
			});
			if (cacheDrawable != null) {
				imageView.setImageDrawable(cacheDrawable);
			} else {
				imageView.setImageResource(R.drawable.bg_kangoo_photo_defualt);
			}
		}
	}

	/**
	 * 系统消息列表
	 * 
	 * @author syghh
	 * 
	 */
	class SystemMsgTask extends AsyncTask<Void, Void, JSONObject> {

		public SystemMsgTask() {
			LIST_RECORD_TASK_RUNING = true;
			g_listType = Constants.MSG_CENTER_TYPE_SYS;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(MessageCenterActivity.this);
			}
			pd.setMessage("更新系统消息列表中...");
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().listNoti(userId, pageNo, pageSize, accessToken);
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
						totalPage = result.getInt("total");
						JSONArray notifyListJson = result.getJSONArray("data");
						notifyListBean = NotifyBean.constractList(notifyListJson);
						if (sysMsgAdapter == null || sysMsgAdapter.getCount() == 0) {
							sysMsgAdapter = new SystemMsgAdapter(MessageCenterActivity.this);
							sysMsgAdapter.setData(notifyListBean);
							lvOrder.setAdapter(sysMsgAdapter);
							pageNo = 1;
						} else {
							sysMsgAdapter.add(notifyListBean);
						}
						pageNo = pageNo + 1;
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(MessageCenterActivity.this, R.string.time_out, Toast.LENGTH_SHORT).show();
						startActivity(new Intent(MessageCenterActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						tvShowNo.setVisibility(View.VISIBLE);
//						Toast.makeText(MessageCenterActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(MessageCenterActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
				}/*
				 * catch (SystemException e) { e.printStackTrace(); }
				 */
			} else {
				Toast.makeText(MessageCenterActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
			// 加载记录的任务结束
			cancel(true);
			LIST_RECORD_TASK_RUNING = false;
		}
	}

	/**
	 * 黑名单删除
	 * 
	 * @author syghh
	 * 
	 */
	class BlackListDelTask extends AsyncTask<Void, Void, JSONObject> {

		private int delLocation;

		public BlackListDelTask(int location) {
			LIST_RECORD_TASK_RUNING = true;
			delLocation = location;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (pd == null) {
				pd = new ProgressDialog(MessageCenterActivity.this);
				pd.setMessage("正在移除黑名单，请稍后...");
			}
			pd.show();
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				return new BusinessHelper().blackListDel(blackListBean.get(delLocation).getId(), accessToken);
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
						Toast.makeText(MessageCenterActivity.this, "成功移除黑名单", Toast.LENGTH_SHORT).show();
						blackListBean.remove(delLocation);
						blackListAdapter.notifyDataSetChanged();
					} else if (status == Constants.TOKEN_FAILED) {
						Toast.makeText(MessageCenterActivity.this, R.string.time_out, Toast.LENGTH_SHORT).show();
						startActivity(new Intent(MessageCenterActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
						Toast.makeText(MessageCenterActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Toast.makeText(MessageCenterActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
				}/*
				 * catch (SystemException e) { e.printStackTrace(); }
				 */
			} else {
				Toast.makeText(MessageCenterActivity.this, "服务器请求失败", Toast.LENGTH_SHORT).show();
			}
			// 加载记录的任务结束
			cancel(true);
			LIST_RECORD_TASK_RUNING = false;
		}
	}

	Handler delBlankHdl = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (false == LIST_RECORD_TASK_RUNING) {
				new BlackListDelTask(msg.getData().getInt("location")).execute();
			}
		}
	};

	/**
	 * 滚动监听器
	 */
	OnScrollListener loadNewPageListener = new OnScrollListener() {
		private int lastItem;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			lastItem = firstVisibleItem + visibleItemCount - 1;//
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// 滚动到最后，默认加载下一页
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastItem == blackListAdapter.getCount() - 1) {
				if (letterListBeans.size() > 0) {
					updateLetterList();
				} else if (notifyListBean.size() > 0) {
					updateSystemNotifyList();
				} else if (blackListBean.size() > 0) {
					updateBlackList();
				}

			}
		}

		/**
		 * 私信列表加载下一页
		 */
		private void updateLetterList() {
			if (NetUtil.checkNet(MessageCenterActivity.this)) {
				if (!LIST_RECORD_TASK_RUNING) {
					new LetterListTask().execute();
				}
			} else {
				Toast.makeText(MessageCenterActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
		}

		/**
		 * 系统列表加载下一页
		 */
		private void updateSystemNotifyList() {
			if (NetUtil.checkNet(MessageCenterActivity.this)) {
				if (!LIST_RECORD_TASK_RUNING) {
					new SystemMsgTask().execute();
				}
			} else {
				Toast.makeText(MessageCenterActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
		}

		/**
		 * 黑名单列表加载下一页
		 */
		private void updateBlackList() {
			if (NetUtil.checkNet(MessageCenterActivity.this)) {
				if (!LIST_RECORD_TASK_RUNING) {
					new BlackListTask().execute();
				}
			} else {
				Toast.makeText(MessageCenterActivity.this, R.string.NoSignalException, Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * 拉黑
	 * 
	 * @author Zhoujun
	 * 
	 */
	private class BlackTask extends AsyncTask<Void, Void, JSONObject> {
		private long uid;

		public BlackTask(long uid) {
			super();
			this.uid = uid;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject update = null;
			long userId = SharedPrefUtil.getUserBean(MessageCenterActivity.this).getUserId();
			String accessToken = SharedPrefUtil.getUserBean(MessageCenterActivity.this).getAccessToken();
			try {
				update = new BusinessHelper().black(accessToken, userId, uid);
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
						Toast.makeText(MessageCenterActivity.this, result.getString("data"), Toast.LENGTH_SHORT).show();
					} else if (result.getInt("status") == Constants.TOKEN_FAILED) {
						Toast.makeText(MessageCenterActivity.this, R.string.time_out, Toast.LENGTH_SHORT).show();
						startActivity(new Intent(MessageCenterActivity.this, LoginActivity.class).putExtra("back", "back"));
					} else {
//						Toast.makeText(MessageCenterActivity.this, result.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
				}
			} else {
				Toast.makeText(MessageCenterActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show();
			}
		}
	}
}

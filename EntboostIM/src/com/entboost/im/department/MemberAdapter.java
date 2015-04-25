package com.entboost.im.department;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.yunim.service.EntboostCache;
import net.yunim.service.constants.EB_USER_LINE_STATE;
import net.yunim.service.entity.MemberInfo;
import net.yunim.utils.ResourceUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.chat.ChatActivity;
import com.entboost.ui.utils.AbImageUtil;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;

public class MemberAdapter extends BaseAdapter {
	private Context mContext;
	private List<MemberInfo> memberInfos = new ArrayList<MemberInfo>();
	private BitmapUtils bitmapUtils;
	private boolean selecteduser;

	public Context getmContext() {
		return mContext;
	}

	public MemberAdapter(Context context) {
		mContext = context;
		bitmapUtils = new BitmapUtils(mContext);
	}

	public void setSelecteduser(boolean selecteduser) {
		this.selecteduser = selecteduser;
	}

	public BitmapUtils getBitmapUtils() {
		return bitmapUtils;
	}

	public void setInput(List<MemberInfo> memberInfos) {
		if (memberInfos != null) {
			List<MemberInfo> online = new ArrayList<MemberInfo>();
			List<MemberInfo> offline = new ArrayList<MemberInfo>();
			for (int i = 0; i < memberInfos.size(); i++) {
				MemberInfo member = memberInfos.get(i);
				if (member.getState() == EB_USER_LINE_STATE.EB_LINE_STATE_ONLINE
						.getValue()) {
					online.add(member);
				} else {
					offline.add(member);
				}
			}
			Collections.sort(online);
			Collections.sort(offline);
			this.memberInfos.clear();
			this.memberInfos.addAll(online);
			this.memberInfos.addAll(offline);
		}
	}

	private class MemberInfoViewHolder {
		ImageView userImg;
		TextView itemsText;
		TextView description;
		ImageView item_user_chat;
		ImageButton user_select;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public int getCount() {
		return memberInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return memberInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final MemberInfo memberInfo = (MemberInfo) getItem(position);
		final MemberInfoViewHolder holder1;
		if (convertView == null) {
			// 使用自定义的list_items作为Layout
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_user, parent, false);
			// 减少findView的次数
			holder1 = new MemberInfoViewHolder();
			// 初始化布局中的元素
			holder1.userImg = ((ImageView) convertView
					.findViewById(R.id.user_head));
			holder1.itemsText = ((TextView) convertView
					.findViewById(R.id.user_name));
			holder1.description = ((TextView) convertView
					.findViewById(R.id.user_description));
			holder1.item_user_chat = ((ImageView) convertView
					.findViewById(R.id.item_user_chat));
			holder1.user_select = ((ImageButton) convertView
					.findViewById(R.id.user_select));
			convertView.setTag(holder1);
		} else {
			holder1 = (MemberInfoViewHolder) convertView.getTag();
		}
		holder1.item_user_chat.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, ChatActivity.class);
				intent.putExtra(ChatActivity.INTENT_TITLE,
						memberInfo.getUsername());
				intent.putExtra(ChatActivity.INTENT_UID,
						memberInfo.getEmp_uid());
				mContext.startActivity(intent);
			}
		});
		Bitmap img = ResourceUtils.getHeadBitmap(memberInfo.getH_r_id());
		if (img != null) {
			if (memberInfo.getState() == EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE
					.getValue()) {
				holder1.userImg.setImageBitmap(AbImageUtil.grey(img));
			} else {
				holder1.userImg.setImageBitmap(img);
			}
		} else {
			bitmapUtils.display(holder1.userImg, memberInfo.getHeadUrl(),
					new BitmapLoadCallBack<ImageView>() {

						@Override
						public void onLoadCompleted(ImageView arg0,
								String arg1, Bitmap arg2,
								BitmapDisplayConfig arg3, BitmapLoadFrom arg4) {
							if (memberInfo.getState() == EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE
									.getValue()) {
								setBitmap(arg0, AbImageUtil.grey(arg2));
							} else {
								setBitmap(arg0, arg2);
							}

						}

						@Override
						public void onLoadFailed(ImageView arg0, String arg1,
								Drawable arg2) {
							if (memberInfo.getState() == EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE
									.getValue()) {
								holder1.userImg.setImageBitmap(AbImageUtil
										.grey(BitmapFactory.decodeResource(
												mContext.getResources(),
												R.drawable.head1)));
							} else {
								holder1.userImg
										.setImageResource(R.drawable.head1);
							}

						}

					});
		}
		if (selecteduser) {
			holder1.user_select.setVisibility(View.VISIBLE);
		}
		if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
			holder1.itemsText.setText(memberInfo.getUsername() + "[自己]");
			holder1.item_user_chat.setVisibility(View.GONE);
		} else {
			holder1.itemsText.setText(memberInfo.getUsername());
		}
		holder1.description.setText(memberInfo.getDescription());
		return convertView;
	}

}

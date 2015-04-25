package com.entboost.im.persongroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.yunim.service.EntboostCache;
import net.yunim.service.constants.EB_USER_LINE_STATE;
import net.yunim.service.entity.DepartmentInfo;
import net.yunim.service.entity.GroupInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.entity.PersonGroupInfo;
import net.yunim.utils.ResourceUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.department.DepartmentInfoActivity;
import com.entboost.ui.utils.AbImageUtil;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.util.LogUtils;

public class GroupAdapter<T> extends BaseExpandableListAdapter {
	private Context mContext;
	private Map<Long, List<Object>> groupMemberInfos = new HashMap<Long, List<Object>>();
	private List<T> groups = new ArrayList<T>();
	private BitmapUtils bitmapUtils;
	private boolean selectMember;

	public Context getmContext() {
		return mContext;
	}

	public void setSelectMember(boolean selectMember) {
		this.selectMember = selectMember;
	}

	public GroupAdapter(Context context) {
		mContext = context;
		bitmapUtils = new BitmapUtils(mContext);
	}

	public void setInput(List<T> groups) {
		this.groups.clear();
		this.groups.addAll(groups);
	}

	public void setMembers(Long groupid, boolean hasNextGroup) {
		this.groupMemberInfos.remove(groupid);
		ArrayList<Object> temp = new ArrayList<Object>();
		if (hasNextGroup) {
			List<DepartmentInfo> ndis = EntboostCache.getNextDepartmentInfos(groupid);
			if(ndis!=null){
				temp.addAll(ndis);
			}
		}
		List<MemberInfo> gis = EntboostCache.getGroupMemberInfos(groupid);
		if(gis!=null){
			temp.addAll(gis);
		}
		// LogUtils.e("加载群组成员：" + groupid + "|数量：" + temp.size());
		this.groupMemberInfos.put(groupid, temp);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		List<Object> members = groupMemberInfos.get(getGroupId(groupPosition));
		if (members != null && members.size() > 0) {
			return members.get(childPosition);
		}
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		Object obj = getChild(groupPosition, childPosition);
		if (obj instanceof MemberInfo) {
			MemberInfoViewHolder holder1;
			if (convertView == null
					|| !(convertView.getTag() instanceof MemberInfoViewHolder)) {
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
			final MemberInfo memberInfo = (MemberInfo) obj;
			holder1.item_user_chat
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(mContext,
									ChatActivity.class);
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
									BitmapDisplayConfig arg3,
									BitmapLoadFrom arg4) {
								if (memberInfo.getState() == EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE
										.getValue()) {
									setBitmap(arg0, AbImageUtil.grey(arg2));
								} else {
									setBitmap(arg0, arg2);
								}
							}

							@Override
							public void onLoadFailed(ImageView arg0,
									String arg1, Drawable arg2) {
								if (memberInfo.getState() == EB_USER_LINE_STATE.EB_LINE_STATE_OFFLINE
										.getValue()) {
									arg0.setImageBitmap(AbImageUtil
											.grey(BitmapFactory.decodeResource(
													mContext.getResources(),
													R.drawable.head1)));
								} else {
									arg0.setImageResource(R.drawable.head1);
								}
							}

						});
			}
			if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
				holder1.itemsText.setText(memberInfo.getUsername() + "[自己]");
				holder1.item_user_chat.setVisibility(View.GONE);
			} else {
				holder1.itemsText.setText(memberInfo.getUsername());
				holder1.item_user_chat.setVisibility(View.VISIBLE);
			}
			holder1.description.setText(memberInfo.getDescription());
			if (selectMember) {
				holder1.user_select.setVisibility(View.VISIBLE);
				if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
					holder1.user_select.setVisibility(View.INVISIBLE);
				}
			}
		} else {
			GroupViewHolder holder2;
			if (convertView == null
					|| !(convertView.getTag() instanceof GroupViewHolder)) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.item_group, parent, false);
				// 减少findView的次数
				holder2 = new GroupViewHolder();
				// 初始化布局中的元素
				holder2.itemsHead = ((ImageView) convertView
						.findViewById(R.id.item_group_head));
				holder2.itemsProgress = ((ProgressBar) convertView
						.findViewById(R.id.item_group_head_progress));
				holder2.itemsText = ((TextView) convertView
						.findViewById(R.id.item_group_name));
				holder2.itemsChat = ((ImageView) convertView
						.findViewById(R.id.item_group_chat));
				holder2.itemsInfo = ((ImageView) convertView
						.findViewById(R.id.item_group_info));
				convertView.setTag(holder2);
			} else {
				holder2 = (GroupViewHolder) convertView.getTag();
			}
			convertView.setBackgroundResource(R.drawable.mm_listitem3);
			final DepartmentInfo group = (DepartmentInfo) obj;
			holder2.itemsHead.setVisibility(View.VISIBLE);
			holder2.itemsProgress.setVisibility(View.GONE);
			holder2.itemsHead.setImageResource(R.drawable.ui65new);
			holder2.itemsText.setText(group.getDep_name() + "("
					+ group.getEmp_count() + ")");
			holder2.itemsChat.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, ChatActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra(ChatActivity.INTENT_TITLE,
							group.getDep_name());
					intent.putExtra(ChatActivity.INTENT_UID,
							group.getDep_code());
					intent.putExtra(ChatActivity.INTENT_CHATTYPE,
							ChatActivity.CHATTYPE_GROUP);
					mContext.startActivity(intent);
				}
			});
			holder2.itemsInfo.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							DepartmentInfoActivity.class);
					intent.putExtra("depid", group.getDep_code());
					mContext.startActivity(intent);
				}
			});
			if (selectMember) {
				holder2.itemsChat.setVisibility(View.GONE);
				holder2.itemsInfo.setVisibility(View.GONE);
			}
			if (group.getMy_emp_id() == null || group.getMy_emp_id() <= 0) {
				holder2.itemsChat.setVisibility(View.INVISIBLE);
			}else{
				holder2.itemsChat.setVisibility(View.VISIBLE);
			}
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		List<Object> members = groupMemberInfos.get(getGroupId(groupPosition));
		if (members != null) {
			return members.size();
		}
		return 0;
	}

	private int getMemberChildrenCount(int groupPosition) {
		List<Object> members = groupMemberInfos.get(getGroupId(groupPosition));
		int num = 0;
		if (members != null) {
			for (Object obj : members) {
				if (obj instanceof MemberInfo) {
					++num;
				}
			}
		}
		return num;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		GroupInfo group = (GroupInfo) groups.get(groupPosition);
		if (group == null) {
			return -1;
		} else {
			return group.getDep_code();
		}
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		final GroupInfo group = (GroupInfo) getGroup(groupPosition);
		GroupViewHolder holder1;
		if (convertView == null) {
			// 使用自定义的list_items作为Layout
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_group, parent, false);
			// 减少findView的次数
			holder1 = new GroupViewHolder();
			// 初始化布局中的元素
			holder1.itemsHead = ((ImageView) convertView
					.findViewById(R.id.item_group_head));
			holder1.itemsProgress = ((ProgressBar) convertView
					.findViewById(R.id.item_group_head_progress));
			holder1.itemsText = ((TextView) convertView
					.findViewById(R.id.item_group_name));
			holder1.itemsChat = ((ImageView) convertView
					.findViewById(R.id.item_group_chat));
			holder1.itemsInfo = ((ImageView) convertView
					.findViewById(R.id.item_group_info));
			convertView.setTag(holder1);
		} else {
			holder1 = (GroupViewHolder) convertView.getTag();
		}
		if (isExpanded
				&& group.getEmp_count() != getMemberChildrenCount(groupPosition)) {
			holder1.itemsHead.setVisibility(View.GONE);
			holder1.itemsProgress.setVisibility(View.VISIBLE);
		} else {
			holder1.itemsHead.setVisibility(View.VISIBLE);
			holder1.itemsProgress.setVisibility(View.GONE);
		}
		if (isExpanded) {
			holder1.itemsHead.setImageResource(R.drawable.ui67new);
		} else {
			holder1.itemsHead.setImageResource(R.drawable.ui66new);
		}
		holder1.itemsText.setText(group.getDep_name() + "("
				+ group.getEmp_count() + ")");
		holder1.itemsChat.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, ChatActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(ChatActivity.INTENT_TITLE, group.getDep_name());
				intent.putExtra(ChatActivity.INTENT_UID, group.getDep_code());
				intent.putExtra(ChatActivity.INTENT_CHATTYPE,
						ChatActivity.CHATTYPE_GROUP);
				mContext.startActivity(intent);
			}
		});
		holder1.itemsInfo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (group instanceof PersonGroupInfo) {
					Intent intent = new Intent(mContext,
							PersonGroupInfoActivity.class);
					intent.putExtra("depid", group.getDep_code());
					mContext.startActivity(intent);
				} else if (group instanceof DepartmentInfo) {
					Intent intent = new Intent(mContext,
							DepartmentInfoActivity.class);
					intent.putExtra("depid", group.getDep_code());
					mContext.startActivity(intent);
				}
			}
		});
		if (selectMember) {
			holder1.itemsChat.setVisibility(View.GONE);
			holder1.itemsInfo.setVisibility(View.GONE);
		}
		if (group.getMy_emp_id() == null || group.getMy_emp_id() <= 0) {
			holder1.itemsChat.setVisibility(View.INVISIBLE);
		}else{
			holder1.itemsChat.setVisibility(View.VISIBLE);
		}
		// holder1.description.setText(mContext.getResources().getStringArray(
		// R.array.group_type)[EB_GROUP_TYPE.getIndex(group.getType())]);
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

}

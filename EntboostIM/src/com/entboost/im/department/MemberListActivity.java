package com.entboost.im.department;

import java.util.List;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.listener.LoadMemberListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.global.MyApplication;
import com.entboost.im.persongroup.MemberSelectActivity;
import com.entboost.ui.base.view.pupmenu.PopMenuConfig;
import com.entboost.ui.base.view.pupmenu.PopMenuItem;
import com.entboost.ui.base.view.pupmenu.PopMenuItemOnClickListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;

public class MemberListActivity extends EbActivity {

	private ListView memberlistView;
	private MemberAdapter memberAdapter;
	private OnItemClickListener memberListener;
	private long depid;
	private boolean selecteduser;

	@Override
	protected void onResume() {
		super.onResume();
		memberAdapter.setInput(EntboostCache.getGroupMemberInfos(depid));
		memberAdapter.notifyDataSetChanged();
	}

	@Override
	public void onUserStateChange(Long uid) {
		memberAdapter.setInput(EntboostCache.getGroupMemberInfos(depid));
		memberAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_member_list);
		ViewUtils.inject(this);
		depid = getIntent().getLongExtra("depid", -1);
		selecteduser = getIntent().getBooleanExtra("selecteduser", false);
		memberlistView = (ListView) findViewById(R.id.memberlist);
		memberAdapter = new MemberAdapter(this);
		memberAdapter.setSelecteduser(selecteduser);
		if (!selecteduser) {
			memberListener = new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Object obj = memberAdapter.getItem(position);
					MemberInfo memberInfo = (MemberInfo) obj;
					Intent intent = new Intent(view.getContext(),
							MemberInfoActivity.class);
					if (memberInfo != null) {
						intent.putExtra("memberCode", memberInfo.getEmp_code());
						if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
							intent.putExtra("selfFlag", true);
						}
						startActivity(intent);
					}
				}
			};
		} else {
			memberListener = new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Object obj = memberAdapter.getItem(position);
					MemberInfo memberInfo = (MemberInfo) obj;
					ImageView selectImg = (ImageView) view
							.findViewById(R.id.user_select);
					if (selectImg.getVisibility() == View.GONE) {
						return;
					}
					List<Object> selectedMap = MyApplication.getInstance()
							.getSelectedUserList();
					Drawable srcImg = selectImg.getDrawable();
					if (srcImg == null) {
						selectImg.setImageResource(R.drawable.uitb_57);
						selectedMap.add(memberInfo);
					} else {
						selectImg.setImageDrawable(null);
						selectedMap.remove(memberInfo);
					}
				}
			};
		}
		memberAdapter.setInput(EntboostCache.getGroupMemberInfos(depid));
		memberlistView.setAdapter(memberAdapter);
		memberlistView.setOnItemClickListener(memberListener);
		memberlistView.setOnScrollListener(new PauseOnScrollListener(
				memberAdapter.getBitmapUtils(), false, true));
		pageInfo.showProgress("正在加载群组成员信息");
		EntboostUM.loadMembers(depid, new LoadMemberListener() {

			@Override
			public void onFailure(String errMsg) {
				pageInfo.showError("无法加载群组成员信息");
			}

			@Override
			public void onLoadMemberSuccess() {
				pageInfo.hide();
				memberAdapter.setInput(EntboostCache.getGroupMemberInfos(depid));
				memberAdapter.notifyDataSetChanged();
			}
		});
		initMenu();
	}

	public void initMenu() {
		PopMenuConfig config = new PopMenuConfig();
		config.setBackground_resId(R.drawable.popmenu);
		config.setTextColor(Color.WHITE);
		this.getTitleBar().addRightImageButton(R.drawable.ic_action_refresh,
				config, new PopMenuItem(new PopMenuItemOnClickListener() {

					@Override
					public void onItemClick() {
						pageInfo.showProgress("正在加载群组成员信息");
						EntboostUM.loadServiceMembers(depid,
								new LoadMemberListener() {

									@Override
									public void onFailure(String errMsg) {
										pageInfo.showError("无法加载群组成员信息");
									}

									@Override
									public void onLoadMemberSuccess() {
										pageInfo.hide();
										memberAdapter.setInput(EntboostCache
												.getGroupMemberInfos(depid));
										memberAdapter.notifyDataSetChanged();
									}
								});
					}

				}));
//		if (!selecteduser) {
//			this.getTitleBar().addRightImageButton(
//					R.drawable.ic_action_add_person, config,
//					new PopMenuItem(new PopMenuItemOnClickListener() {
//
//						@Override
//						public void onItemClick() {
//							Intent intent = new Intent(MemberListActivity.this,
//									MemberSelectActivity.class);
//							intent.putExtra("groupid", depid);
//							startActivity(intent);
//						}
//
//					}));
//		}
	}

}

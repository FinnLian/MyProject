package com.entboost.im.persongroup;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.GroupInfo;
import net.yunim.service.listener.DelGroupListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.department.MemberListActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class PersonGroupInfoActivity extends EbActivity {

	private GroupInfo groupInfo;
	private Long depid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_department_info);
		ViewUtils.inject(this);
		depid = getIntent().getLongExtra("depid", -1);
		groupInfo = EntboostCache.getGroup(depid);
		init();
	}

	@OnClick(R.id.department_member_layout)
	public void showMemberlist(View view) {
		Intent intent = new Intent(this, MemberListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("group", groupInfo);
		this.startActivity(intent);
	}

	private void init() {
		TextView name = (TextView) findViewById(R.id.department_username);
		TextView description = (TextView) findViewById(R.id.department_description);
		TextView creator = (TextView) findViewById(R.id.department_account);
		TextView memberNum = (TextView) findViewById(R.id.department_member);
		Button umbtn = (Button) findViewById(R.id.department_usermanager);
		if (groupInfo != null) {
			memberNum.setText(groupInfo.getEmp_count() + "");
			creator.setText(groupInfo.getCreator());
			name.setText(groupInfo.getDep_name() + "["
					+ groupInfo.getDep_code() + "]");
			description.setText(groupInfo.getDescription());
			umbtn.setVisibility(View.VISIBLE);
		}
	}

	@OnClick(R.id.department_usermanager)
	public void usermanager(View view) {
		Intent intent = new Intent(this, MemberSelectActivity.class);
		intent.putExtra("groupid", depid);
		startActivity(intent);
	}

	@OnClick(R.id.department_send_btn)
	public void sendMsg(View view) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(ChatActivity.INTENT_TITLE, groupInfo.getDep_name());
		intent.putExtra(ChatActivity.INTENT_UID, groupInfo.getDep_code());
		intent.putExtra(ChatActivity.INTENT_CHATTYPE,
				ChatActivity.CHATTYPE_GROUP);
		this.startActivity(intent);
	}

	@OnClick(R.id.department_del_btn)
	public void del(View view) {
		showDialog("提示", "确定要解散群组吗？", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showProgressDialog("正在解散群组");
				if (groupInfo != null) {
					EntboostUM.delPersonGroup(groupInfo.getDep_code(),
							new DelGroupListener() {

								@Override
								public void onFailure(String errMsg) {
									removeProgressDialog();
									pageInfo.showError(errMsg);
								}

								@Override
								public void onDelGroupSuccess() {
									removeProgressDialog();
									finish();
								}
							});
				} else {
					removeProgressDialog();
					showToast("解散群组失败！");
				}
			}
		});

	}

}

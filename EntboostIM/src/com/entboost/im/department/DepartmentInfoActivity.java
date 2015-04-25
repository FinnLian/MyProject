package com.entboost.im.department;

import net.yunim.service.EntboostCache;
import net.yunim.service.constants.EB_GROUP_TYPE;
import net.yunim.service.entity.GroupInfo;
import net.yunim.utils.UIUtils;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.chat.ChatActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class DepartmentInfoActivity extends EbActivity {

	private GroupInfo departmentInfo;
	private Long depid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_department_info);
		ViewUtils.inject(this);
		depid = getIntent().getLongExtra("depid", -1);
		departmentInfo = EntboostCache.getGroup(depid);
		init();
	}

	private void init() {
		TextView name = (TextView) findViewById(R.id.department_username);
		TextView description = (TextView) findViewById(R.id.department_description);
		TextView creator = (TextView) findViewById(R.id.department_account);
		TextView memberNum = (TextView) findViewById(R.id.department_member);
		View send_btn = findViewById(R.id.department_send_btn);
		findViewById(R.id.department_del_btn).setVisibility(View.GONE);
		if (departmentInfo != null) {
			memberNum.setText(departmentInfo.getEmp_count() + "");
			creator.setText(departmentInfo.getCreator());
			name.setText(departmentInfo.getDep_name() + "["
					+ departmentInfo.getDep_code() + "]");
			description.setText(departmentInfo.getDescription());
			if (departmentInfo.getMy_emp_id() == null
					|| departmentInfo.getMy_emp_id() <= 0) {
				send_btn.setVisibility(View.GONE);
			}
		}
	}

	@OnClick(R.id.department_member_layout)
	public void showMemberlist(View view) {
		Intent intent = new Intent(this, MemberListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("depid", departmentInfo.getDep_code());
		this.startActivity(intent);
	}

	@OnClick(R.id.department_send_btn)
	public void sendMsg(View view) {
		if ((departmentInfo.getType() == EB_GROUP_TYPE.EB_GROUP_TYPE_DEPARTMENT
				.getValue() || departmentInfo.getType() == EB_GROUP_TYPE.EB_GROUP_TYPE_PROJECT
				.getValue())
				&& departmentInfo.getMy_emp_id() == null) {
			UIUtils.showToast(this, "不是所属企业群组，无法进行群组会话！");
			return;
		}
		Intent intent = new Intent(this, ChatActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(ChatActivity.INTENT_TITLE, departmentInfo.getDep_name());
		intent.putExtra(ChatActivity.INTENT_UID, departmentInfo.getDep_code());
		intent.putExtra(ChatActivity.INTENT_CHATTYPE,
				ChatActivity.CHATTYPE_GROUP);
		this.startActivity(intent);
	}

}

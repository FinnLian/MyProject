package com.entboost.im.persongroup;

import net.yunim.service.entity.MemberInfo;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.chat.ChatActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class PersonInfoActivity extends EbActivity {

	private MemberInfo memberInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_member_info);
		ViewUtils.inject(this);
		memberInfo = (MemberInfo) getIntent().getSerializableExtra(
				"memberInfo");
		init();
	}

	public void init() {
		TextView username = (TextView) findViewById(R.id.member_username);
		TextView name = (TextView) findViewById(R.id.member_name);
		TextView account = (TextView) findViewById(R.id.member_account);
		TextView description = (TextView) findViewById(R.id.member_description);
		if (memberInfo != null) {
			account.setText(memberInfo.getEmp_account());
			username.setText(memberInfo.getUsername());
			name.setText(memberInfo.getUsername());
			description.setText(memberInfo.getDescription());
		}
	}

	
	@OnClick(R.id.member_send_btn)
	public void sendMsg(View view){
		Intent intent = new Intent(this, ChatActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(ChatActivity.INTENT_TITLE, memberInfo.getUsername());
		intent.putExtra(ChatActivity.INTENT_UID, memberInfo.getEmp_uid());
		this.startActivity(intent);
	}


}

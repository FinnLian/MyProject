package com.entboost.im.user;

import net.yunim.service.EntboostCache;
import net.yunim.service.entity.AccountInfo;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class UserInfoActivity extends EbActivity {

	@ViewInject(R.id.info_account)
	private TextView info_account;
	@ViewInject(R.id.info_username)
	private TextView info_username;
	@ViewInject(R.id.info_description)
	private TextView info_description;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_user_info);
		ViewUtils.inject(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		AccountInfo user = EntboostCache.getUser();
		info_account.setText(user.getAccount());
		info_username.setText(user.getUsername());
		info_description.setText(user.getDescription());
	}

	@OnClick(R.id.info_name_layout)
	public void openInfoNameEdit(View view) {
		Intent intent = new Intent(this, InfoNameEditActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@OnClick(R.id.info_description_layout)
	public void openInfoDescriptionEdit(View view) {
		Intent intent = new Intent(this, InfoDescriptionEditActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

}

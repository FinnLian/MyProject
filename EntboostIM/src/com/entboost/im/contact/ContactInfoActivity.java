package com.entboost.im.contact;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.ContactInfo;
import net.yunim.service.listener.DelContactListener;
import net.yunim.utils.UIUtils;

import org.apache.commons.lang3.StringUtils;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.chat.ChatActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class ContactInfoActivity extends EbActivity {

	private Long con_id;
	private ContactInfo contactInfo;

	@Override
	protected void onResume() {
		super.onResume();
		init();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_contact_info);
		ViewUtils.inject(this);
		con_id = getIntent().getLongExtra("con_id", -1);
		findViewById(R.id.contact_username).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ContactInfoActivity.this,
								ContactNameEditActivity.class);
						intent.putExtra("contact", contactInfo.getContact());
						startActivity(intent);
					}
				});
		findViewById(R.id.contact_description_layout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ContactInfoActivity.this,
								ContactDescriptionActivity.class);
						intent.putExtra("contact", contactInfo.getContact());
						startActivity(intent);
					}
				});
//		findViewById(R.id.contact_group_layout).setOnClickListener(
//				new OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						Intent intent = new Intent(ContactInfoActivity.this,
//								ContactGroupActivity.class);
//						intent.putExtra("contact", contactInfo.getContact());
//						startActivity(intent);
//					}
//				});
	}

	private void init() {
		TextView username = (TextView) findViewById(R.id.contact_username);
		TextView account = (TextView) findViewById(R.id.contact_account);
		TextView description = (TextView) findViewById(R.id.contact_description);
		TextView group = (TextView) findViewById(R.id.contact_group);
		TextView contact_tel = (TextView) findViewById(R.id.contact_tel);
		TextView contact_phone = (TextView) findViewById(R.id.contact_phone);
		TextView contact_email = (TextView) findViewById(R.id.contact_email);
		TextView contact_job_title = (TextView) findViewById(R.id.contact_job_title);
		TextView contact_fax = (TextView) findViewById(R.id.contact_fax);
		TextView contact_company = (TextView) findViewById(R.id.contact_company);
		TextView contact_url = (TextView) findViewById(R.id.contact_url);
		TextView contact_address = (TextView) findViewById(R.id.contact_address);
		contactInfo = EntboostCache.getContactInfoById(con_id);
		if (contactInfo != null) {
			if (contactInfo.getCon_uid() == null) {
				account.setText(contactInfo.getContact());
			}else{
				account.setText(contactInfo.getContact()+"("+contactInfo.getCon_uid()+")");
			}
			username.setText(contactInfo.getName());
			description.setText(contactInfo.getDescription());
			group.setText(contactInfo.getGroupName());
			contact_tel.setText(contactInfo.getTel());
			if(contactInfo.getPhone()==null){
				contact_phone.setText("");
			}else{
				contact_phone.setText(contactInfo.getPhone()+"");
			}
			contact_email.setText(contactInfo.getEmail());
			contact_job_title.setText(contactInfo.getJob_title());
			contact_fax.setText(contactInfo.getFax());
			contact_company.setText(contactInfo.getCompany());
			contact_url.setText(contactInfo.getUrl());
			contact_address.setText(contactInfo.getAddress());
			if (contactInfo.getCon_uid() == null) {
				View send_btn = findViewById(R.id.contact_send_btn);
				send_btn.setVisibility(View.GONE);
			}
		}
	}

	@OnClick(R.id.contact_group_layout)
	public void contact_groupmanager(View view) {
		if (contactInfo != null) {
			Intent intent = new Intent(ContactInfoActivity.this,
					SelectContactGroupActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("con_id", contactInfo.getCon_id());
			startActivity(intent);
		}
	}

	@OnClick(R.id.contact_send_btn)
	public void sendMsg(View view) {
		if (contactInfo.getCon_uid() == null) {
			UIUtils.showToast(this, "该联系人不是系统用户，无法进行会话！");
		} else {
			Intent intent = new Intent(ContactInfoActivity.this,
					ChatActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			String name = null;
			if (StringUtils.isNotBlank(contactInfo.getName())) {
				name = contactInfo.getName();
			} else {
				name = contactInfo.getContact();
			}
			intent.putExtra(ChatActivity.INTENT_TITLE, name);
			intent.putExtra(ChatActivity.INTENT_UID, contactInfo.getCon_uid());
			startActivity(intent);
		}
	}

	@OnClick(R.id.contact_del_btn)
	public void delContact(View view) {
		showDialog("提示", "确认要删除联系人吗？", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				showProgressDialog("删除联系人");
				EntboostUM.delContact(contactInfo.getCon_id(),
						new DelContactListener() {

							@Override
							public void onFailure(String errMsg) {
								pageInfo.showError(errMsg);
								removeProgressDialog();
							}

							@Override
							public void onDelContactSuccess() {
								removeProgressDialog();
								finish();
							}
						});
			}

		});

	}
}

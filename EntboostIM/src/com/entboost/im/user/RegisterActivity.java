package com.entboost.im.user;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.AppAccountInfo;
import net.yunim.service.listener.RegisterListener;
import net.yunim.utils.UIUtils;

import org.apache.commons.lang3.StringUtils;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class RegisterActivity extends EbActivity {

	@ViewInject(R.id.register_username)
	private EditText userName;
	@ViewInject(R.id.register_passwd)
	private EditText pwd;
	@ViewInject(R.id.register_confirm_passwd)
	private EditText confirmpwd;
	@ViewInject(R.id.register_ent)
	private EditText ent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_register);
		ViewUtils.inject(this);
		AppAccountInfo appInfo = EntboostCache.getAppInfo();
		if (appInfo != null && appInfo.getOpen_register() == 1) {
			findViewById(R.id.register_ent_layout).setVisibility(View.GONE);
		}
	}

	@OnClick(R.id.register_register)
	public void regiter(View view) {
		final String name = userName.getText().toString();
		final String pwdstr = pwd.getText().toString();
		final String confirmpwdstr = confirmpwd.getText().toString();
		final String entname = ent.getText().toString();
		if (!StringUtils.equals(pwdstr, confirmpwdstr)) {
			pageInfo.showError("两次输入密码不一致！");
			return;
		}
		if (StringUtils.isBlank(name)) {
			pageInfo.showError("帐号不能为空！");
			return;
		}
		if (StringUtils.isBlank(pwdstr)) {
			pageInfo.showError("密码不能为空！");
			return;
		}
		showProgressDialog("正在注册中...");
		EntboostUM.emailRegister(name, pwdstr, entname,
				new RegisterListener() {

					@Override
					public void onFailure(String errMsg) {
						pageInfo.showError(errMsg);
						removeProgressDialog();
					}

					@Override
					public void onRegisterSuccess() {
						removeProgressDialog();
						if (EntboostCache.getAppInfo().getSend_reg_mail() == 1) {
							UIUtils.showToast(RegisterActivity.this,
									"请到注册邮箱激活帐号！");
						}
						finish();
					}
				});
	}

}

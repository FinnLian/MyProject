package com.entboost.im.user;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostLC;
import net.yunim.service.entity.AccountInfo;
import net.yunim.service.entity.AppAccountInfo;
import net.yunim.service.listener.FindPWDListener;
import net.yunim.service.listener.LogonAccountListener;
import net.yunim.utils.UIUtils;

import org.apache.commons.lang3.StringUtils;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.entboost.bitmap.AbImageDownloader;
import com.entboost.im.MainActivity;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.global.AppUtils;
import com.entboost.im.setting.SetLogonServiceAddrActivity;
import com.entboost.ui.base.view.pupmenu.PopMenu;
import com.entboost.ui.base.view.pupmenu.PopMenuConfig;
import com.entboost.ui.base.view.pupmenu.PopMenuItemOnClickListener;
import com.entboost.ui.base.view.titlebar.AbTitleBar;
import com.entboost.ui.utils.AbImageUtil;
import com.entboost.utils.AbFileUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class LoginActivity extends EbActivity {
	@ViewInject(R.id.login_username)
	private EditText loginName;
	@ViewInject(R.id.login_passwd)
	private EditText loginPWD;
	@ViewInject(R.id.login_register)
	private Button registerBtn;
	@ViewInject(R.id.login_vistor_login_btn)
	private Button vistor_loginBtn;
	@ViewInject(R.id.ent_logo)
	private ImageView entlogo;
	@ViewInject(R.id.login_username_downImg)
	private ImageButton login_username_downImg;
	@ViewInject(R.id.login_username_layout)
	private RelativeLayout login_username_layout;
	@ViewInject(R.id.app_version)
	private TextView app_version;
	@ViewInject(R.id.login_username_clear)
	private ImageButton login_username_clear;
	@ViewInject(R.id.login_passwd_clear)
	private ImageButton login_passwd_clear;

	private String[] accountHistory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_login);
		AbTitleBar titleBar = this.getTitleBar();
		titleBar.setVisibility(View.GONE);
		ViewUtils.inject(this);
		app_version.setText(AppUtils.getVersion(this));
		AppAccountInfo appInfo = EntboostCache.getAppInfo();
		// Log.e("appInfo.getOpen_register()", appInfo.getOpen_register()+"");
		// Log.e("appInfo.getOpen_visitor()", appInfo.getOpen_visitor()+"");
		// Log.e("appInfo.getEnt_logo_url()", appInfo.getEnt_logo_url()+"");
		if (appInfo != null) {
			if (appInfo.getOpen_register() == 0) {
				registerBtn.setVisibility(View.GONE);
			}
			if (appInfo.getOpen_visitor() == 0) {
				vistor_loginBtn.setVisibility(View.GONE);
			}
			if (appInfo.getEnt_logo_url() != null) {
				entlogo.setImageBitmap(AbFileUtil.getBitmapFromSDCache(
						appInfo.getEnt_logo_url(), AbImageUtil.SCALEIMG, 86, 86));
			} else {
				entlogo.setImageResource(R.drawable.ic_launcher);
			}
		}
		accountHistory = EntboostCache.getAccountHistorys();
		if (accountHistory == null || accountHistory.length <= 1) {
			login_username_downImg.setVisibility(View.GONE);
		}
		String lastAccount = EntboostCache.getLastLoginAccount();
		if (StringUtils.isNotBlank(lastAccount)) {
			loginName.setText(lastAccount);
		}
		loginName.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() != 0) {
					login_username_clear.setVisibility(View.VISIBLE);
				} else {
					login_username_clear.setVisibility(View.GONE);
				}
			}
		});
		login_username_clear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				loginName.setText("");
			}
		});
		loginPWD.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() != 0) {
					login_passwd_clear.setVisibility(View.VISIBLE);
				} else {
					login_passwd_clear.setVisibility(View.GONE);
				}
			}
		});
		login_passwd_clear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				loginPWD.setText("");
			}
		});
	}

	@OnClick(R.id.login_username_downImg)
	public void selectAccount(View view) {
		PopMenuConfig config = new PopMenuConfig();
		config.setWidth(login_username_layout.getWidth());
		final PopMenu popMenu = new PopMenu(view.getContext(), config);
		for (final String account : accountHistory) {
			popMenu.addItem(account, new PopMenuItemOnClickListener() {

				@Override
				public void onItemClick() {
					loginName.setText(account);
				}

			});
		}
		popMenu.showAsDropDown(login_username_layout);
	}

	@OnClick(R.id.login_setService)
	public void setServiceAddr(View view) {
		Intent intent = new Intent(LoginActivity.this,
				SetLogonServiceAddrActivity.class);
		startActivity(intent);
	}

	@OnClick(R.id.login_login_btn)
	public void login(View view) {
		String name = loginName.getText().toString();
		String pwd = loginPWD.getText().toString();
		// 空值校验
		if (StringUtils.isBlank(name)) {
			showToast("帐号不能为空！");
			return;
		}
		if (StringUtils.isBlank(pwd)) {
			showToast("密码不能为空！");
			return;
		}
		showProgressDialog("努力登录中...");
		EntboostLC.logon(name, pwd, new LogonAccountListener() {

			@Override
			public void onFailure(String errMsg) {
				pageInfo.showError(errMsg);
				removeProgressDialog();
			}

			@Override
			public void onLogonSuccess(AccountInfo pAccountInfo) {
				removeProgressDialog();
				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});
	}

	@OnClick(R.id.login_forget_passwd)
	public void forgetPwd(View view) {
		// TODO 校验
		String name = loginName.getText().toString();
		if (StringUtils.isBlank(name)) {
			showToast("帐号不能为空！");
			return;
		}
		showProgressDialog("重置密码正在发往注册邮箱中，请稍候");
		EntboostLC.findPwd(name, new FindPWDListener() {

			@Override
			public void onFailure(String errMsg) {
				pageInfo.showError(errMsg);
				removeProgressDialog();
			}

			@Override
			public void onFindPWDSuccess() {
				removeProgressDialog();
				UIUtils.showToast(LoginActivity.this, "成功找回密码，请到注册邮箱中查看！");
			}
		});
	}

	@OnClick(R.id.login_register)
	public void register(View view) {
		Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@OnClick(R.id.login_vistor_login_btn)
	public void vistorLogin(View view) {
		showProgressDialog("努力登录中...");
		EntboostLC.logonVisitor(new LogonAccountListener() {

			@Override
			public void onFailure(String errMsg) {
				pageInfo.showError(errMsg);
				removeProgressDialog();
			}

			@Override
			public void onLogonSuccess(AccountInfo pAccountInfo) {
				removeProgressDialog();
				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});
	}

}

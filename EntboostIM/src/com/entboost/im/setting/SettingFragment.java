package com.entboost.im.setting;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostLC;
import net.yunim.service.entity.AccountInfo;
import net.yunim.utils.ResourceUtils;
import net.yunim.utils.UIUtils;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.global.AbConstant;
import com.entboost.im.R;
import com.entboost.im.base.EbFragment;
import com.entboost.im.user.LoginActivity;
import com.entboost.im.user.UserInfoActivity;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;

public class SettingFragment extends EbFragment {
	private BitmapUtils bitmapUtils;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = onCreateEbView(R.layout.fragment_setting, inflater,
				container);
		// 显示自己的默认名片头像
		bitmapUtils = new BitmapUtils(this.getActivity());
		final ImageView userHead = (ImageView) view
				.findViewById(R.id.user_head);
		AccountInfo user = EntboostCache.getUser();
		userHead.setFocusable(false);
		Bitmap img = ResourceUtils.getHeadBitmap(user.getHead_rid());
		if (img != null) {
			userHead.setImageBitmap(img);
		} else {
			bitmapUtils.display(userHead, user.getHeadUrl(),
					new BitmapLoadCallBack<ImageView>() {

						@Override
						public void onLoadCompleted(ImageView arg0,
								String arg1, Bitmap arg2,
								BitmapDisplayConfig arg3, BitmapLoadFrom arg4) {
							setBitmap(arg0, arg2);
						}

						@Override
						public void onLoadFailed(ImageView arg0, String arg1,
								Drawable arg2) {
							userHead.setImageResource(R.drawable.head1);
						}

					});
		}
		TextView userText = (TextView) view.findViewById(R.id.user_name);
		userText.setText(user.getUsername());
		view.findViewById(R.id.user_layout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(activity,
								UserInfoActivity.class);
						startActivity(intent);
					}
				});
		view.findViewById(R.id.message_layout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						UIUtils.showToast(activity, "消息提醒通知正在建设中...");
					}
				});
		view.findViewById(R.id.s_layout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						UIUtils.showToast(activity, "隐私与安全正在建设中...");
					}
				});
		view.findViewById(R.id.chat_layout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						UIUtils.showToast(activity, "聊天对话正在建设中...");
					}
				});
		view.findViewById(R.id.logout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						View view = mInflater.inflate(R.layout.dialog_exit,
								null);
						activity.showDialog(AbConstant.DIALOGBOTTOM, view);
						view.findViewById(R.id.logout_layout)
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										EntboostLC.logout();
										activity.getBottomDialog().cancel();
										activity.finish();
										Intent intent = new Intent(activity,
												LoginActivity.class);
										startActivity(intent);
									}
								});
						view.findViewById(R.id.exit_layout).setOnClickListener(
								new OnClickListener() {

									@Override
									public void onClick(View v) {
										EntboostLC.exit();
										activity.getBottomDialog().cancel();
										activity.finish();
									}
								});
					}
				});
		return view;
	}
}

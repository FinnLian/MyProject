package com.entboost.im.chat;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.yunim.eb.constants.EBConstants;
import net.yunim.service.EntboostCM;
import net.yunim.service.EntboostCache;
import net.yunim.service.api.UserCenter;
import net.yunim.service.cache.FileCacheUtils;
import net.yunim.service.entity.AccountInfo;
import net.yunim.service.entity.ChatRoomRichMsg;
import net.yunim.service.entity.FileCache;
import net.yunim.service.entity.MemberInfo;
import net.yunim.utils.ResourceUtils;
import net.yunim.utils.UIUtils;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.contact.DefaultUserInfoActivity;
import com.entboost.im.department.MemberInfoActivity;
import com.entboost.im.user.UserInfoActivity;
import com.entboost.utils.AbDateUtil;
import com.entboost.voice.ExtAudioRecorder;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;

public class ChatMsgViewAdapter extends BaseAdapter {
	private List<ChatRoomRichMsg> mChatMsgList = new ArrayList<ChatRoomRichMsg>();
	private LayoutInflater mInflater;
	private Context context;
	private BitmapUtils bitmapUtils;
	private Map<Long, ProgressBar> progressBarIngs = new ConcurrentHashMap<Long, ProgressBar>();
	private Map<Long, TextView> progressBarIngDescriptions = new ConcurrentHashMap<Long, TextView>();
	private DecimalFormat decimalFormat = new DecimalFormat(".0");// 构造方法的字符格式这里如果小数不足1位,会以0补足.

	public void refreshFileProgressBar(FileCache fileCache) {
		ProgressBar progressBar = progressBarIngs.get(fileCache.getMsgId());
		TextView description = progressBarIngDescriptions.get(fileCache
				.getMsgId());
		if (progressBar != null && description != null) {
			progressBar.setMax((int)fileCache.getTotalSize());
			progressBar.setProgress((int)fileCache.getCurSize());
			description.setVisibility(View.VISIBLE);
			description.setText("下载进度" + fileCache.getPercent() + "  速度："
					+ fileCache.getCurSpeed());
		}
	}

	public void refreshFileProgressBar(ChatRoomRichMsg msg) {
		ProgressBar progressBar = progressBarIngs.get(msg.getMsgid());
		TextView description = progressBarIngDescriptions.get(msg.getMsgid());
		if (progressBar != null && description != null) {
			progressBar.setMax(100);
			progressBar.setProgress(msg.getPercent().intValue());
			description.setVisibility(View.VISIBLE);
			description.setText("进度：" + decimalFormat.format(msg.getPercent())
					+ "%");
		}
	}

	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}

	public ChatMsgViewAdapter() {
	}

	public void initChat(List<ChatRoomRichMsg> list) {
		this.mChatMsgList.clear();
		this.mChatMsgList.addAll(list);
	}

	public ChatMsgViewAdapter(Context context, List<ChatRoomRichMsg> list) {
		this.context = context;
		initChat(list);
		this.mInflater = LayoutInflater.from(context);
		bitmapUtils = new BitmapUtils(context);
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	@Override
	public int getCount() {
		return mChatMsgList.size();
	}

	@Override
	public Object getItem(int position) {
		return mChatMsgList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		ChatRoomRichMsg mChatMsg = mChatMsgList.get(position);
		if (mChatMsg.getSender() - UserCenter.getInstance().getUserid() == 0) {
			return IMsgViewType.IMVT_COM_MSG;
		} else {
			return IMsgViewType.IMVT_TO_MSG;
		}

	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ChatRoomRichMsg mChatMsg = mChatMsgList.get(position);
		final boolean isComMsg = getItemViewType(position) == IMsgViewType.IMVT_COM_MSG;
		final ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			if (isComMsg) {
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_right, null);
				viewHolder.mProgressBar = (ProgressBar) convertView
						.findViewById(R.id.sendingProgress);
				viewHolder.errorImg = (ImageView) convertView
						.findViewById(R.id.errorImg);
				viewHolder.file_progressBar = (ProgressBar) convertView
						.findViewById(R.id.file_progressBar);
				viewHolder.fileBtnLayout = (LinearLayout) convertView
						.findViewById(R.id.fileBtnLayout);
				viewHolder.file_refuse_btn = (Button) convertView
						.findViewById(R.id.file_refuse_btn);
			} else {
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_left, null);
				viewHolder.sendName = (TextView) convertView
						.findViewById(R.id.sendName);
				viewHolder.fileBtnLayout = (LinearLayout) convertView
						.findViewById(R.id.fileBtnLayout);
				viewHolder.file_progressBar = (ProgressBar) convertView
						.findViewById(R.id.file_progressBar);
				viewHolder.file_receive_btn = (Button) convertView
						.findViewById(R.id.file_receive_btn);
				viewHolder.file_refuse_btn = (Button) convertView
						.findViewById(R.id.file_refuse_btn);
				viewHolder.file_path_btn = (Button) convertView
						.findViewById(R.id.file_path_btn);
			}
			viewHolder.chatDiscription = (TextView) convertView
					.findViewById(R.id.chatDiscription);
			viewHolder.sendTime = (TextView) convertView
					.findViewById(R.id.sendTime);
			viewHolder.chatLayout = (LinearLayout) convertView
					.findViewById(R.id.chatLayout);
			viewHolder.chatContent = (TextView) convertView
					.findViewById(R.id.chatContent);
			viewHolder.userHead = (ImageView) convertView
					.findViewById(R.id.userHead);
			if (isComMsg) {
				viewHolder.userHead
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								AccountInfo user = EntboostCache.getUser();
								MemberInfo member = EntboostCache.getMember(
										user.getUid(), mChatMsg.getDepCode());
								if (member != null) {
									Intent intent = new Intent(context,
											MemberInfoActivity.class);
									intent.putExtra("memberCode",
											member.getEmp_code());
									intent.putExtra("selfFlag", true);
									context.startActivity(intent);
								} else {
									Intent intent = new Intent(context,
											UserInfoActivity.class);
									context.startActivity(intent);
								}
							}
						});
			} else {
				viewHolder.userHead
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								MemberInfo member = EntboostCache.getMember(
										mChatMsg.getSender(),
										mChatMsg.getDepCode());
								if (member != null) {
									Intent intent = new Intent(context,
											MemberInfoActivity.class);
									intent.putExtra("memberCode",
											member.getEmp_code());
									context.startActivity(intent);
								} else {
									Intent intent = new Intent(context,
											DefaultUserInfoActivity.class);
									intent.putExtra("uid", mChatMsg.getSender());
									context.startActivity(intent);
								}
							}
						});
			}
			viewHolder.chatAttach = (ImageView) convertView
					.findViewById(R.id.chatAttach);
			viewHolder.voicetip = (ImageView) convertView
					.findViewById(R.id.voicetip);
			viewHolder.isComMsg = isComMsg;
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.file_progressBar.setVisibility(View.GONE);
		viewHolder.file_progressBar.setProgress(0);
		viewHolder.fileBtnLayout.setVisibility(View.GONE);
		viewHolder.chatLayout.setVisibility(View.VISIBLE);
		viewHolder.voicetip.setVisibility(View.GONE);
		viewHolder.chatAttach.setVisibility(View.GONE);
		viewHolder.chatDiscription.setVisibility(View.GONE);
		if (viewHolder.file_path_btn != null) {
			viewHolder.file_path_btn.setVisibility(View.GONE);
		}
		viewHolder.chatAttach.setOnClickListener(null);
		viewHolder.chatAttach.setFocusable(false);
		if (mChatMsgList.size() > 0 && position > 1) {
			ChatRoomRichMsg lastChatMsg = mChatMsgList.get(position - 1);
			if (lastChatMsg == null
					|| AbDateUtil.getOffectMinutes(mChatMsg.getMsgTime(),
							lastChatMsg.getMsgTime()) >= 1) {
				viewHolder.sendTime.setText(AbDateUtil.formatDateStr2Desc(
						AbDateUtil.getStringByFormat(mChatMsg.getMsgTime(),
								AbDateUtil.dateFormatYMDHMS),
						AbDateUtil.dateFormatYMDHMS));
			} else {
				viewHolder.sendTime.setVisibility(View.GONE);
			}
		} else {
			viewHolder.sendTime.setText(AbDateUtil.formatDateStr2Desc(
					AbDateUtil.getStringByFormat(mChatMsg.getMsgTime(),
							AbDateUtil.dateFormatYMDHMS),
					AbDateUtil.dateFormatYMDHMS));
		}
		if (mChatMsg.getType() == ChatRoomRichMsg.CHATROOMRICHMSG_TYPE_PIC) {
			viewHolder.chatAttach.setVisibility(View.VISIBLE);
			viewHolder.chatLayout.setVisibility(View.GONE);
			viewHolder.chatAttach.setImageBitmap(mChatMsg.getPicBitmap());
			viewHolder.chatAttach
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(new File(
									mChatMsg.getFilePath())), "image/*");
							context.startActivity(intent);
						}
					});
		} else if (mChatMsg.getType() == ChatRoomRichMsg.CHATROOMRICHMSG_TYPE_RICH) {
			viewHolder.chatLayout.setVisibility(View.VISIBLE);
			viewHolder.chatAttach.setVisibility(View.GONE);
			viewHolder.chatContent.setText(UIUtils.getTipCharSequence(mChatMsg
					.getTipHtml()));
			viewHolder.chatContent.setMovementMethod(LinkMovementMethod
					.getInstance());
		} else if (mChatMsg.getType() == ChatRoomRichMsg.CHATROOMRICHMSG_TYPE_VOICE) {
			viewHolder.voicetip.setVisibility(View.VISIBLE);
			viewHolder.chatLayout.setVisibility(View.VISIBLE);
			viewHolder.chatAttach.setVisibility(View.GONE);
			viewHolder.chatContent.setText(UIUtils.getTipCharSequence(mChatMsg
					.getTipHtml()));
			viewHolder.chatContent
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							ExtAudioRecorder.play(mChatMsg.getFilePath());
						}
					});
		} else if (mChatMsg.getType() == ChatRoomRichMsg.CHATROOMRICHMSG_TYPE_FILE) {
			if (!isComMsg) {
				if (mChatMsg.getStatus() == EBConstants.RECEIVEFILE_STATUS_DEFAULT) {
					viewHolder.fileBtnLayout.setVisibility(View.VISIBLE);
					viewHolder.file_progressBar.setVisibility(View.GONE);
					viewHolder.chatDiscription.setVisibility(View.GONE);
					progressBarIngs.remove(mChatMsg.getMsgid());
					progressBarIngDescriptions.remove(mChatMsg.getMsgid());
				} else if (mChatMsg.getStatus() == EBConstants.RECEIVEFILE_STATUS_ING) {
					viewHolder.fileBtnLayout.setVisibility(View.GONE);
					viewHolder.file_progressBar.setVisibility(View.VISIBLE);
					progressBarIngs.put(mChatMsg.getMsgid(),
							viewHolder.file_progressBar);
					progressBarIngDescriptions.put(mChatMsg.getMsgid(),
							viewHolder.chatDiscription);
				} else if (mChatMsg.getStatus() == EBConstants.RECEIVEFILE_STATUS_OK) {
					viewHolder.fileBtnLayout.setVisibility(View.GONE);
					viewHolder.file_progressBar.setVisibility(View.GONE);
					viewHolder.chatDiscription.setVisibility(View.VISIBLE);
					viewHolder.chatDiscription.setText("接收完成");
					viewHolder.file_path_btn.setVisibility(View.VISIBLE);
					progressBarIngs.remove(mChatMsg.getMsgid());
					progressBarIngDescriptions.remove(mChatMsg.getMsgid());
					viewHolder.file_path_btn
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									Intent intent = new Intent();
									intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
									File file = new File(FileCacheUtils
											.getFilePath());
									intent.setDataAndType(Uri.fromFile(file),
											"*/*");
									context.startActivity(intent);
								}

							});
				} else if (mChatMsg.getStatus() == EBConstants.RECEIVEFILE_STATUS_ERROR) {
					viewHolder.fileBtnLayout.setVisibility(View.GONE);
					viewHolder.file_progressBar.setVisibility(View.GONE);
					viewHolder.chatDiscription.setVisibility(View.VISIBLE);
					viewHolder.chatDiscription.setText("接收失败");
					progressBarIngs.remove(mChatMsg.getMsgid());
					progressBarIngDescriptions.remove(mChatMsg.getMsgid());
				} else if (mChatMsg.getStatus() == EBConstants.RECEIVEFILE_STATUS_CANCEL) {
					viewHolder.fileBtnLayout.setVisibility(View.GONE);
					viewHolder.file_progressBar.setVisibility(View.GONE);
					viewHolder.chatDiscription.setVisibility(View.VISIBLE);
					viewHolder.chatDiscription.setText("取消接收文件");
					progressBarIngs.remove(mChatMsg.getMsgid());
					progressBarIngDescriptions.remove(mChatMsg.getMsgid());
				}
				viewHolder.chatContent.setText(mChatMsg.getText());
				viewHolder.file_receive_btn
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								EntboostCM.acceptFile(mChatMsg);
								viewHolder.fileBtnLayout
										.setVisibility(View.GONE);
								viewHolder.file_progressBar
										.setVisibility(View.VISIBLE);
								progressBarIngs.put(mChatMsg.getMsgid(),
										viewHolder.file_progressBar);
								progressBarIngDescriptions.put(
										mChatMsg.getMsgid(),
										viewHolder.chatDiscription);
							}
						});
				viewHolder.file_refuse_btn
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								EntboostCM.rejectFile(mChatMsg);
								viewHolder.fileBtnLayout
										.setVisibility(View.GONE);
								viewHolder.chatDiscription
										.setVisibility(View.VISIBLE);
								viewHolder.chatDiscription.setText("拒绝接收文件");
							}
						});
			} else {
				viewHolder.fileBtnLayout.setVisibility(View.VISIBLE);
				if (mChatMsg.getStatus() == EBConstants.SENDFILE_STATUS_DEFAULT) {
					viewHolder.file_progressBar.setVisibility(View.VISIBLE);
					viewHolder.chatDiscription.setText("等待对方接收文件");
					viewHolder.file_refuse_btn.setVisibility(View.VISIBLE);
					if (mChatMsg.getMsgid() != null) {
						progressBarIngs.put(mChatMsg.getMsgid(),
								viewHolder.file_progressBar);
						progressBarIngDescriptions.put(mChatMsg.getMsgid(),
								viewHolder.chatDiscription);
					}
				} else if (mChatMsg.getStatus() == EBConstants.SENDFILE_STATUS_ING) {
					viewHolder.file_progressBar.setVisibility(View.VISIBLE);
					viewHolder.file_refuse_btn.setVisibility(View.VISIBLE);
					if (mChatMsg.getMsgid() != null) {
						progressBarIngs.put(mChatMsg.getMsgid(),
								viewHolder.file_progressBar);
						progressBarIngDescriptions.put(mChatMsg.getMsgid(),
								viewHolder.chatDiscription);
					}
				} else if (mChatMsg.getStatus() == EBConstants.SENDFILE_STATUS_OK) {
					viewHolder.file_progressBar.setVisibility(View.GONE);
					viewHolder.chatDiscription.setText("文件发送完成");
					viewHolder.file_refuse_btn.setVisibility(View.GONE);
					if (mChatMsg.getMsgid() != null) {
						progressBarIngs.remove(mChatMsg.getMsgid());
						progressBarIngDescriptions.remove(mChatMsg.getMsgid());
					}
				} else if (mChatMsg.getStatus() == EBConstants.SENDFILE_STATUS_ERROR) {
					viewHolder.file_progressBar.setVisibility(View.GONE);
					viewHolder.chatDiscription.setText("文件发送失败");
					viewHolder.file_refuse_btn.setVisibility(View.GONE);
					if (mChatMsg.getMsgid() != null) {
						progressBarIngs.remove(mChatMsg.getMsgid());
						progressBarIngDescriptions.remove(mChatMsg.getMsgid());
					}
				} else if (mChatMsg.getStatus() == EBConstants.SENDFILE_STATUS_CANCEL) {
					viewHolder.file_progressBar.setVisibility(View.GONE);
					viewHolder.chatDiscription.setVisibility(View.VISIBLE);
					viewHolder.chatDiscription.setText("文件发送被取消");
					viewHolder.file_refuse_btn.setVisibility(View.GONE);
					if (mChatMsg.getMsgid() != null) {
						progressBarIngs.remove(mChatMsg.getMsgid());
						progressBarIngDescriptions.remove(mChatMsg.getMsgid());
					}
				}
				viewHolder.chatContent.setText(mChatMsg.getText());
				viewHolder.file_refuse_btn
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								EntboostCM.cancelSendFile(mChatMsg);
								viewHolder.fileBtnLayout
										.setVisibility(View.VISIBLE);
								viewHolder.file_progressBar
										.setVisibility(View.GONE);
								viewHolder.chatDiscription.setText("取消文件发送");
								viewHolder.file_refuse_btn
										.setVisibility(View.GONE);
								if (mChatMsg.getMsgid() != null) {
									progressBarIngs.remove(mChatMsg.getMsgid());
									progressBarIngDescriptions.remove(mChatMsg
											.getMsgid());
								}
							}
						});
			}
		}
		if (isComMsg) {
			// 显示自己的默认名片头像
			AccountInfo user = EntboostCache.getUser();
			Bitmap img = ResourceUtils.getHeadBitmap(user.getHead_rid());
			if (img != null) {
				viewHolder.userHead.setImageBitmap(img);
			} else {
				bitmapUtils.display(viewHolder.userHead, user.getHeadUrl(),
						new BitmapLoadCallBack<ImageView>() {

							@Override
							public void onLoadCompleted(ImageView arg0,
									String arg1, Bitmap arg2,
									BitmapDisplayConfig arg3,
									BitmapLoadFrom arg4) {
								setBitmap(arg0, arg2);
							}

							@Override
							public void onLoadFailed(ImageView arg0,
									String arg1, Drawable arg2) {
								viewHolder.userHead
										.setImageResource(R.drawable.head1);
							}

						});
			}
			if (mChatMsg.getStatus() == EBConstants.SEND_STATUS_ING) {
				viewHolder.mProgressBar.setVisibility(View.VISIBLE);
				viewHolder.errorImg.setVisibility(View.GONE);
			} else if (mChatMsg.getStatus() == EBConstants.SEND_STATUS_OK) {
				viewHolder.mProgressBar.setVisibility(View.GONE);
				viewHolder.errorImg.setVisibility(View.GONE);
			} else if (mChatMsg.getStatus() == EBConstants.SEND_STATUS_ERROR) {
				viewHolder.mProgressBar.setVisibility(View.GONE);
				viewHolder.errorImg.setVisibility(View.VISIBLE);
				viewHolder.errorImg
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								((ChatActivity) context).showDialog("提示",
										"确认要重发消息吗？",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												EntboostCM.reSendMsg(mChatMsg);
											}

										});
							}
						});
			}
		} else {
			// 显示会话对方的默认名片头像
			if (mChatMsg.getChatType() == ChatRoomRichMsg.CHATTYPE_GROUP) {
				viewHolder.sendName.setVisibility(View.VISIBLE);
				viewHolder.sendName.setText(mChatMsg.getSendName());
			}
			Bitmap img = ResourceUtils.getHeadBitmap(mChatMsg.getHid());
			if (img != null) {
				viewHolder.userHead.setImageBitmap(img);
			} else {
				bitmapUtils.display(viewHolder.userHead, mChatMsg.getHeadUrl(),
						new BitmapLoadCallBack<ImageView>() {

							@Override
							public void onLoadCompleted(ImageView arg0,
									String arg1, Bitmap arg2,
									BitmapDisplayConfig arg3,
									BitmapLoadFrom arg4) {
								setBitmap(arg0, arg2);
							}

							@Override
							public void onLoadFailed(ImageView arg0,
									String arg1, Drawable arg2) {
								viewHolder.userHead
										.setImageResource(R.drawable.head1);
							}

						});
			}

		}

		return convertView;
	}

	static class ViewHolder {
		public LinearLayout chatLayout;
		public TextView sendTime;
		public TextView sendName;
		public TextView chatContent;
		public ImageView userHead;
		public ImageView voicetip;
		public ImageView chatAttach;
		public boolean isComMsg = true;
		public ProgressBar mProgressBar;
		public ImageView errorImg;
		public LinearLayout fileBtnLayout;
		public ProgressBar file_progressBar;
		public Button file_receive_btn;
		public Button file_refuse_btn;
		public TextView chatDiscription;
		public Button file_path_btn;
	}

}

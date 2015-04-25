package com.entboost.im.chat;

import java.io.File;

import net.yunim.service.EntboostCM;
import net.yunim.service.EntboostCache;
import net.yunim.service.cache.FileCacheUtils;
import net.yunim.service.entity.AppAccountInfo;
import net.yunim.service.entity.ChatRoomRichMsg;
import net.yunim.service.entity.FileCache;
import net.yunim.service.entity.Resource;
import net.yunim.service.listener.CallUserListener;
import net.yunim.utils.UIUtils;

import org.apache.commons.lang3.StringUtils;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.contact.DefaultUserInfoActivity;
import com.entboost.ui.base.view.pupmenu.PopMenuItem;
import com.entboost.ui.base.view.pupmenu.PopMenuItemOnClickListener;
import com.entboost.ui.base.view.titlebar.AbTitleBar;
import com.entboost.utils.NetworkUtils;
import com.entboost.voice.ExtAudioRecorder;

public class ChatActivity extends EbActivity {

	public static final String INTENT_TITLE = "intent_title";
	public static final String INTENT_UID = "intent_uid";
	public static final String INTENT_CHATTYPE = "intent_chattype";

	public static final int CHATTYPE_PERSON = 0;
	public static final int CHATTYPE_GROUP = 1;

	private int chattype = CHATTYPE_PERSON;
	private Long uid;
	private EditText mContentEdit;
	private ChatMsgViewAdapter mChatMsgViewAdapter;
	private ListView mMsgListView;
	private LinearLayout emotionsAppPanel;
	private GridView expressionGriView;
	private EmotionsImageAdapter emotionsImageAdapter;
	private Button picBtn;
	private Button sendBtn;
	private ImageView voiceImg;
	private String title;
	private LinearLayout morePanel;
	private ImageButton moreBtn;
	private Button fileBtn;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onSendFileIng(ChatRoomRichMsg msg) {
		if (mChatMsgViewAdapter != null) {
			mChatMsgViewAdapter.refreshFileProgressBar(msg);
		}
	}

	@Override
	public void onReceiveFileIncoming(FileCache fileCache) {
		if (mChatMsgViewAdapter != null) {
			mChatMsgViewAdapter.refreshFileProgressBar(fileCache);
		}
	}

	@Override
	public void onReceiveUserMessage(ChatRoomRichMsg msg) {
		// 启动会话界面，接收消息自动设置已读
		if (msg.getChatType() == ChatRoomRichMsg.CHATTYPE_GROUP) {
			if (msg.getDepCode() - uid == 0) {
				EntboostCache.readMsg(msg.getDepCode());
			} else {
				pageInfo.showInfo(msg.getSendName() + "[" + msg.getDepName()
						+ "]:" + UIUtils.getTipCharSequence(msg.getTipHtml()),
						5);
			}
		} else {
			if (msg.getSender() - uid == 0
					|| msg.getSender() - EntboostCache.getUid() == 0) {
				EntboostCache.readMsg(msg.getSender());
			} else {
				pageInfo.showInfo(
						msg.getSendName() + ":"
								+ UIUtils.getTipCharSequence(msg.getTipHtml()),
						5);
			}
		}
		refreshPage();
	}

	/**
	 * 刷新会话页面
	 */
	private void refreshPage() {
		if (uid != null) {
			mChatMsgViewAdapter.initChat(EntboostCache.getChatMsgs(uid));
		}
		mChatMsgViewAdapter.notifyDataSetChanged();
		mMsgListView.setSelection(mMsgListView.getBottom());
	}

	/**
	 * 发送消息的状态发生改变
	 */
	@Override
	public void onSendStatusChanged(ChatRoomRichMsg msg) {
		refreshPage();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_chat);
		AbTitleBar titleBar = this.getTitleBar();
		title = this.getIntent().getStringExtra(INTENT_TITLE);
		titleBar.setTitleText(title);
		uid = this.getIntent().getLongExtra(INTENT_UID, -1);
		if (uid > 0) {
			EntboostCache.readMsg(uid);// 设置消息已读
		}
		chattype = this.getIntent().getIntExtra(INTENT_CHATTYPE,
				CHATTYPE_PERSON);
		if (chattype == CHATTYPE_PERSON) {
			EntboostCache.loadPersonChatMsg(uid);// 加载已有缓存会话信息
		} else {
			EntboostCache.loadGroupChatMsg(uid);
		}
		if (chattype == CHATTYPE_PERSON) {
			// 增加右上角会话对方的名片信息按钮
			this.getTitleBar().addRightImageButton(R.drawable.uitb_20, null,
					new PopMenuItem(new PopMenuItemOnClickListener() {

						@Override
						public void onItemClick() {
							Intent intent = new Intent(ChatActivity.this,
									DefaultUserInfoActivity.class);
							intent.putExtra("uid", uid);
							startActivity(intent);
						}

					}));
			this.getTitleBar().addRightImageButton(
					R.drawable.ic_action_add_group, null,
					new PopMenuItem(new PopMenuItemOnClickListener() {

						@Override
						public void onItemClick() {
							showDialog("提示", "是否需要转为临时讨论组",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											EntboostCM.call2Group(uid,
													new CallUserListener() {

														@Override
														public void onFailure(
																String errMsg) {
															removeProgressDialog();
															pageInfo.showError(errMsg);
														}

														@Override
														public void onCalling() {
															showProgressDialog("正在转为临时讨论组");
														}

														@Override
														public void onCallAlerting(
																Long uid,
																Long callid) {
															removeProgressDialog();
															showToast("转为临时讨论组成功！");
														}
													});
										}
									});
						}

					}));
		}
		// 增加右上角漫游消息的按钮
		// 1、判断是否有漫游消息的功能
		final AppAccountInfo appInfo = EntboostCache.getAppInfo();
		if (AppAccountInfo.SAVE_CONVERSATIONS_ONLINE == appInfo
				.getSave_conversations()
				|| AppAccountInfo.SAVE_CONVERSATIONS_ALL == appInfo
						.getSave_conversations()) {
			this.getTitleBar().addRightImageButton(
					R.drawable.menu_add_tempgroup, null,
					new PopMenuItem(new PopMenuItemOnClickListener() {

						@Override
						public void onItemClick() {
							Intent intent = new Intent(ChatActivity.this,
									OnlieChatMsgActivity.class);
							if (chattype == CHATTYPE_PERSON) {
								intent.putExtra("onlineChatUrl", appInfo
										.getPersonConversationsAllUrl(uid));
							} else {
								intent.putExtra("onlineChatUrl", appInfo
										.getGroupConversationsAllUrl(uid));
							}
							startActivity(intent);
						}

					}));
		}
		mContentEdit = (EditText) findViewById(R.id.content);
		sendBtn = (Button) findViewById(R.id.sendBtn);
		voiceImg = (ImageView) this.findViewById(R.id.imageViewvoice);
		final Button voiceSendBtn = (Button) this
				.findViewById(R.id.voicesendBtn);
		final ImageButton voiceBtn = (ImageButton) this
				.findViewById(R.id.voiceBtn);
		final ImageButton keyBtn = (ImageButton) this.findViewById(R.id.keyBtn);
		voiceSendBtn.setOnTouchListener(new View.OnTouchListener() {

			private ExtAudioRecorder recorder;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					if (!NetworkUtils.isNetworkConnected(ChatActivity.this)) {
						pageInfo.showError(ChatActivity.this
								.getString(R.string.msg_error_localNoNetwork));
						return false;
					}
					// 按住事件发生后执行代码的区域
					voiceImg.setVisibility(View.VISIBLE);
					recorder = EntboostCM.startRecording();
					voiceSendBtn.setText("松开发送");
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					// 移动事件发生后执行代码的区域
					break;
				}
				case MotionEvent.ACTION_UP: {
					// 松开事件发生后执行代码的区域
					voiceSendBtn.setText("按住说话");
					voiceImg.setVisibility(View.GONE);
					if (recorder != null) {
						recorder.stopRecord();
						if (uid < 0) {
							pageInfo.showError(ChatActivity.this
									.getString(R.string.msg_send_uiderror));
							return false;
						}
						if (uid >= 0) {
							if (chattype == CHATTYPE_PERSON) {
								EntboostCM.sendVoice(uid,
										recorder.getFilePath());
							} else {
								EntboostCM.sendGroupVoice(uid,
										recorder.getFilePath());
							}

						}
						refreshPage();
					}
					break;
				}
				default:
					break;
				}
				return false;
			}
		});
		voiceBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				View btnLayout = findViewById(R.id.voiceBtnLayout);
				btnLayout.setVisibility(View.VISIBLE);
				View contentLayout = findViewById(R.id.contentLayout);
				contentLayout.setVisibility(View.GONE);
				voiceBtn.setVisibility(View.GONE);
				keyBtn.setVisibility(View.VISIBLE);
			}
		});
		keyBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				View btnLayout = findViewById(R.id.voiceBtnLayout);
				btnLayout.setVisibility(View.GONE);
				View contentLayout = findViewById(R.id.contentLayout);
				contentLayout.setVisibility(View.VISIBLE);
				voiceBtn.setVisibility(View.VISIBLE);
				keyBtn.setVisibility(View.GONE);
			}
		});
		mContentEdit.addTextChangedListener(new TextWatcher() {

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
				if (s.length() > 0) {
					sendBtn.setVisibility(View.VISIBLE);
					moreBtn.setVisibility(View.GONE);
				} else {
					sendBtn.setVisibility(View.GONE);
					moreBtn.setVisibility(View.VISIBLE);
				}
			}
		});

		sendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String text = mContentEdit.getText().toString().trim();
				if (StringUtils.isBlank(text)) {
					UIUtils.showToast(ChatActivity.this, "不能发送空消息！");
					return;
				}
				if (!NetworkUtils.isNetworkConnected(ChatActivity.this)) {
					pageInfo.showError(ChatActivity.this
							.getString(R.string.msg_error_localNoNetwork));
					return;
				}
				if (uid < 0) {
					pageInfo.showError(ChatActivity.this
							.getString(R.string.msg_send_uiderror));
					return;
				}
				if (uid >= 0) {
					if (chattype == CHATTYPE_PERSON) {
						EntboostCM.sendText(uid, text);
					} else {
						EntboostCM.sendGroupText(uid, text);
					}
				}
				refreshPage();
				// 清空文本框
				mContentEdit.setText("");
				mMsgListView.setSelection(mMsgListView.getBottom());
				// View view = getWindow().peekDecorView();
				// if (view != null) {
				// InputMethodManager inputmanger = (InputMethodManager)
				// getSystemService(Context.INPUT_METHOD_SERVICE);
				// inputmanger.hideSoftInputFromWindow(view.getWindowToken(),
				// 0);
				// }
			}

		});

		mMsgListView = (ListView) this.findViewById(R.id.mListView);
		mChatMsgViewAdapter = new ChatMsgViewAdapter(ChatActivity.this,
				EntboostCache.getChatMsgs(uid));
		mMsgListView.setAdapter(mChatMsgViewAdapter);
		mMsgListView.setSelection(mChatMsgViewAdapter.getCount() - 1);

		expressionGriView = (GridView) this
				.findViewById(R.id.expressionGridView);
		emotionsImageAdapter = new EmotionsImageAdapter(this);
		expressionGriView.setAdapter(emotionsImageAdapter);
		expressionGriView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final Resource emotions = (Resource) emotionsImageAdapter
						.getItem(position);
				UIUtils.addEmotions(mContentEdit, emotions);
				emotionsAppPanel.setVisibility(View.GONE);
			}
		});

		emotionsAppPanel = (LinearLayout) this
				.findViewById(R.id.expressionAppPanel);
		picBtn = (Button) this.findViewById(R.id.picBtn);
		picBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!NetworkUtils.isNetworkConnected(ChatActivity.this)) {
					pageInfo.showError(ChatActivity.this
							.getString(R.string.msg_error_localNoNetwork));
					return;
				}
				getPicFromContent();
			}
		});
		ImageButton emotionBtn = (ImageButton) this
				.findViewById(R.id.emotionBtn);
		emotionBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (emotionsAppPanel.getVisibility() == View.GONE) {
					emotionsAppPanel.setVisibility(View.VISIBLE);
				} else {
					emotionsAppPanel.setVisibility(View.GONE);
				}
				((BaseAdapter) expressionGriView.getAdapter())
						.notifyDataSetChanged();
			}
		});

		morePanel = (LinearLayout) this.findViewById(R.id.morePanel);
		moreBtn = (ImageButton) this.findViewById(R.id.moreBtn);
		moreBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (morePanel.getVisibility() == View.GONE) {
					morePanel.setVisibility(View.VISIBLE);
				} else {
					morePanel.setVisibility(View.GONE);
				}
			}
		});
		fileBtn = (Button) this.findViewById(R.id.fileBtn);
		fileBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!NetworkUtils.isNetworkConnected(ChatActivity.this)) {
					pageInfo.showError(ChatActivity.this
							.getString(R.string.msg_error_localNoNetwork));
					return;
				}
				getFileFromContent();
			}
		});
	}

	private void getFileFromContent() {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
		File file = new File(FileCacheUtils.getFilePath());
		intent.setDataAndType(Uri.fromFile(file), "*/*");
		startActivityForResult(intent, 3);
	}

	private void getPicFromContent() {
		Intent intent = new Intent();
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
		} else {
			intent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		startActivityForResult(intent, 2);
	}

	private String getPathFromUri(Uri fileUrl) {
		String fileName = null;
		Uri filePathUri = fileUrl;
		if (fileUrl != null) {
			if (fileUrl.getScheme().toString().compareTo("content") == 0) {
				// content://开头的uri
				Cursor cursor = getContentResolver().query(fileUrl, null, null,
						null, null);
				if (cursor != null && cursor.moveToFirst()) {
					int column_index = cursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					fileName = cursor.getString(column_index); // 取出文件路径

					// Android 4.1 更改了SD的目录，sdcard映射到/storage/sdcard0
					if (!fileName.startsWith("/storage")
							&& !fileName.startsWith("/mnt")) {
						// 检查是否有”/mnt“前缀
						fileName = "/mnt" + fileName;
					}
					cursor.close();
				}
			} else if (fileUrl.getScheme().compareTo("file") == 0) // file:///开头的uri
			{
				fileName = Uri.decode(filePathUri.toString()).replace("file://", "");
				int index = fileName.indexOf("/sdcard");
				fileName = index == -1 ? fileName : fileName.substring(index);

//				if (!fileName.startsWith("/mnt")) {
//					// 加上"/mnt"头
//					fileName += "/mnt";
//				}
			}
		}
		return fileName;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		String filePath = getPathFromUri(data.getData());
		if (requestCode == 2) {// 获取手机中的图片
			if (filePath.endsWith("jpg") || filePath.endsWith("png")) {
				sendPic(filePath);
			}
		} else if (requestCode == 3) {// 获取手机中的文件
			sendFile(filePath);
		}
	}

	public void sendPic(String picUri) {
		if (uid < 0) {
			pageInfo.showError(ChatActivity.this
					.getString(R.string.msg_send_uiderror));
			return;
		}
		if (uid >= 0) {
			if (chattype == CHATTYPE_PERSON) {
				EntboostCM.sendPic(uid, picUri);
			} else {
				EntboostCM.sendGroupPic(uid, picUri);
			}
		}
		refreshPage();
	}

	public void sendFile(String filePath) {
		if (uid < 0) {
			pageInfo.showError(ChatActivity.this
					.getString(R.string.msg_send_uiderror));
			return;
		}
		if (uid >= 0) {
			if (chattype == CHATTYPE_PERSON) {
				EntboostCM.sendFile(uid, filePath);
			} else {
				
			}
		}
		morePanel.setVisibility(View.GONE);
		refreshPage();
	}
}

package com.entboost.im.contact;

import java.util.ArrayList;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.ContactInfo;
import net.yunim.service.entity.GroupInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.entity.SearchResultInfo;
import net.yunim.service.listener.SearchMemberListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.department.DepartmentInfoActivity;
import com.entboost.im.department.MemberInfoActivity;
import com.lidroid.xutils.ViewUtils;

public class SearchContactActivity extends EbActivity {

	private ListView mListView;
	private SearchContactAdapter adapter;
	private int type;
	private long starttime;
	private long endTime;
	private long waitTime = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_search_contact);
		ViewUtils.inject(this);
		mListView = (ListView) findViewById(R.id.mListView);
		adapter = new SearchContactAdapter(this, mInflater,
				new ArrayList<SearchResultInfo>());
		mListView.setAdapter(adapter);
		type = getIntent().getIntExtra("type", -1);
		EditText search = (EditText) findViewById(R.id.search);
		if (type == SearchResultInfo.TYPE_GROUPCHAT) {
			search.setHint("搜索:个人群组、企业部门");
			search.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
					EntboostCache.searchGroup(adapter.getList(), s.toString());
					adapter.notifyDataSetChanged();
				}
			});
		} else if (type == SearchResultInfo.TYPE_CONTACTCHAT) {
			search.setHint("搜索:联系人");
			search.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
					EntboostCache.searchContact(adapter.getList(), s.toString());
					adapter.notifyDataSetChanged();
				}
			});
		} else if (type == SearchResultInfo.TYPE_USERCHAT) {
			search.setHint("搜索:群组成员");
			starttime = System.currentTimeMillis();
			search.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
					endTime = System.currentTimeMillis();
					if (endTime - starttime - waitTime >= 0) {
						starttime = System.currentTimeMillis();
						pageInfo.showProgress("正在搜索群组成员");
						adapter.getList().clear();
						EntboostUM.searchMember(s.toString(),
								new SearchMemberListener() {

									@Override
									public void onFailure(String errMsg) {
										starttime = System.currentTimeMillis();
										pageInfo.showError(errMsg);
									}

									@Override
									public void onSearchMemberSuccess(SearchResultInfo sri) {
										starttime = System.currentTimeMillis();
										pageInfo.hide();
										if(sri!=null){
											adapter.addSearchResultInfo(sri);
											adapter.notifyDataSetChanged();
										}
									}

								});
					}
				}
			});

		}
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 获取该行的数据
				SearchResultInfo info = (SearchResultInfo) adapter
						.getItem(position);
				if (info != null) {
					if (info.getType() == SearchResultInfo.TYPE_GROUPCHAT) {
						GroupInfo departmentInfo = (GroupInfo) info.getObj();
						Intent intent = new Intent(SearchContactActivity.this,
								DepartmentInfoActivity.class);
						intent.putExtra("depid", departmentInfo.getDep_code());
						startActivity(intent);
					} else if (info.getType() == SearchResultInfo.TYPE_CONTACTCHAT) {
						ContactInfo contactInfo = (ContactInfo) info.getObj();
						Intent intent = new Intent(SearchContactActivity.this,
								ContactInfoActivity.class);
						intent.putExtra("contact", contactInfo.getContact());
						startActivity(intent);
					} else if (info.getType() == SearchResultInfo.TYPE_USERCHAT) {
						MemberInfo memberInfo = (MemberInfo) info.getObj();
						Intent intent = new Intent(SearchContactActivity.this,
								MemberInfoActivity.class);
						Bundle bundle = new Bundle();
						bundle.putSerializable("memberInfo", memberInfo);
						intent.putExtras(bundle);
						intent.putExtra("memberCode", memberInfo.getEmp_code());
						if (memberInfo.getEmp_uid() - EntboostCache.getUid() == 0) {
							intent.putExtra("selfFlag", true);
						}
						startActivity(intent);
					}
				}
			}
		});

	}

}

package com.entboost.im.department;

import java.util.ArrayList;
import java.util.List;

import net.yunim.service.entity.DepartmentInfo;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.chat.ChatActivity;
import com.entboost.im.common.tree.Node;

public class DepartmentTreeListAdapter extends BaseAdapter {
	private Context context = null;
	private List<Node> nodeList = new ArrayList<Node>();// 所有的节点
	private List<Node> nodeListToShow = new ArrayList<Node>();// 要展现的节点
	private LayoutInflater inflater = null;
	private Node root = null;
	private boolean selectuser;

	public DepartmentTreeListAdapter(Context con) {
		this.context = con;
		this.inflater = (LayoutInflater) con
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void selectUser() {
		this.selectuser = true;
	}

	public void setInput(Node Root) {
		if (Root != null) {
			establishNodeList(Root);
			this.root = Root;
			setNodeListToShow();
		}
	}

	public void establishNodeList(Node node) {
		nodeList.add(node);
		if (node.isLeafOrNot())
			return;
		List<Node> children = node.getChildren();
		if (children == null) {
			return;
		}
		for (int i = 0; i < children.size(); i++) {
			establishNodeList(children.get(i));
		}
	}

	public void setNodeListToShow() {
		this.nodeListToShow.clear();
		establishNodeListToShow(this.root);
	}

	// 构造要展示在listview的nodeListToShow
	public void establishNodeListToShow(Node node) {
		this.nodeListToShow.add(node);
		if (node.getExpanded() && !node.isLeafOrNot()
				&& node.getChildren() != null) {
			List<Node> children = node.getChildren();
			for (int i = 0; i < children.size(); i++) {
				establishNodeListToShow(children.get(i));
			}
		}
	}

	// 根据oid得到某一个Node,并更改其状态
	public void changeNodeExpandOrFold(int position) {
		String oid = this.nodeListToShow.get(position).getOid();
		for (int i = 0; i < this.nodeList.size(); i++) {
			if (nodeList.get(i).getOid().equals(oid)) {
				boolean flag = nodeList.get(i).getExpanded();
				nodeList.get(i).setExpanded(!flag);
			}

		}
	}

	// listItem被点击的响应事件
	public Node OnListItemClick(int position) {
		Node node = this.nodeListToShow.get(position);
		if (node.isLeafOrNot()) {
			// 处理snmp代码
			// Toast.makeText(this.context, "该节点为子节点",
			// Toast.LENGTH_SHORT).show();
			return node;
		} else {
			this.changeNodeExpandOrFold(position);
			this.setNodeListToShow();
			this.notifyDataSetChanged();
			return null;
		}
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return nodeListToShow.size();
	}

	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return nodeListToShow.get(arg0);
	}

	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	public View getView(int position, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		Holder holder = null;
		final Node node = this.nodeListToShow.get(position);
		DepartmentInfo group = (DepartmentInfo) node.getValue();
		if (view != null) {
			holder = (Holder) view.getTag();
		} else {
			holder = new Holder();
			view = this.inflater.inflate(R.layout.item_tree_node, null);
			holder.description = (TextView) view
					.findViewById(R.id.item_tree_node_name);
			holder.nodeIcon = (ImageView) view
					.findViewById(R.id.item_tree_node_ico);
			holder.expandOrFoldIcon = (ImageView) view
					.findViewById(R.id.item_tree_node_expandOrFoldIcon);
			holder.talkBtn = (ImageButton) view
					.findViewById(R.id.item_tree_node_talk);
			holder.infoBtn = (ImageButton) view
					.findViewById(R.id.item_tree_node_info);
			view.setTag(holder);
		}
		if(selectuser){
			holder.infoBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, MemberListActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("depid", Long.valueOf(node.getOid()));
					intent.putExtra("selecteduser", true);
					context.startActivity(intent);
				}
			});
		}else{
			holder.infoBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context,
							DepartmentInfoActivity.class);
					intent.putExtra("depid", Long.valueOf(node.getOid()));
					context.startActivity(intent);
				}
			});
		}
		// 设置文字
		holder.description.setText(node.getDescription() + "(" + node.getDesc()
				+ ")");
		if (group != null) {
			if (group.getMy_emp_id() != null && group.getMy_emp_id() > 0) {
				holder.talkBtn.setVisibility(View.VISIBLE);
				holder.talkBtn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(context, ChatActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_SINGLE_TOP
								| Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra(ChatActivity.INTENT_TITLE,
								node.getDescription());
						intent.putExtra(ChatActivity.INTENT_UID,
								Long.valueOf(node.getOid()));
						intent.putExtra(ChatActivity.INTENT_CHATTYPE,
								ChatActivity.CHATTYPE_GROUP);
						context.startActivity(intent);
					}
				});
			} else {
				holder.talkBtn.setVisibility(View.GONE);
			}
			holder.infoBtn.setVisibility(View.VISIBLE);
		} else {
			holder.talkBtn.setVisibility(View.GONE);
			holder.infoBtn.setVisibility(View.GONE);
		}

		// 设置图标
		int icon = node.getIcon();
		if (icon != -1) {
			holder.nodeIcon.setImageResource(icon);
			holder.nodeIcon.setVisibility(View.VISIBLE);
		} else
			holder.nodeIcon.setVisibility(View.GONE);

		// 设置展开折叠图标
		if (!node.isLeafOrNot()) {
			int expandIcon = node.getExpandOrFoldIcon();
			if (expandIcon == -1)
				holder.expandOrFoldIcon.setVisibility(View.INVISIBLE);
			else {
				holder.expandOrFoldIcon.setImageResource(expandIcon);
				holder.expandOrFoldIcon.setVisibility(View.VISIBLE);
			}

		} else {
			holder.expandOrFoldIcon.setVisibility(View.INVISIBLE);
		}
		view.setPadding(node.getLevel() * 35, 10, 10, 10);
		if(selectuser){
			holder.talkBtn.setVisibility(View.GONE);
		}
		return view;
	}

	public class Holder {
		TextView description;
		ImageView nodeIcon;
		ImageView expandOrFoldIcon;
		ImageButton talkBtn;
		ImageButton infoBtn;
	}
}

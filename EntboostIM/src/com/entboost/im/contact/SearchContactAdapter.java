package com.entboost.im.contact;

import java.util.List;

import net.yunim.service.EntboostCache;
import net.yunim.service.entity.DepartmentInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.entity.SearchResultInfo;
import net.yunim.utils.ResourceUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.im.R;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;

public class SearchContactAdapter extends BaseAdapter {
	private List<SearchResultInfo> list;
	private Context mContext;
	private LayoutInflater mInflater;
	private BitmapUtils bitmapUtils;

	public SearchContactAdapter(Context context, LayoutInflater mInflater,
			List<SearchResultInfo> list) {
		this.mContext = context;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = list;
		bitmapUtils = new BitmapUtils(mContext);
	}

	public List<SearchResultInfo> getList() {
		return list;
	}

	public void addSearchResultInfo(SearchResultInfo searchResultInfo) {
		if (list != null) {
			list.add(searchResultInfo);
		}
	}

	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return this.list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			// 使用自定义的list_items作为Layout
			convertView = mInflater.inflate(R.layout.item_msg_history, parent,
					false);
			// 减少findView的次数
			holder = new ViewHolder();
			// 初始化布局中的元素
			holder.itemsIcon = ((ImageView) convertView
					.findViewById(R.id.msg_head));
			holder.itemsTitle = ((TextView) convertView
					.findViewById(R.id.msg_name));
			holder.itemsText = ((TextView) convertView
					.findViewById(R.id.msg_message));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 获取该行的数据
		SearchResultInfo obj = (SearchResultInfo) getItem(position);
		if (obj != null) {
			holder.itemsTitle.setText(obj.getName());
			holder.itemsText.setText(obj.getSrc());
			if (obj.getType() == SearchResultInfo.TYPE_USERCHAT) {
				MemberInfo memberInfo = (MemberInfo) obj.getObj();
				Bitmap img = ResourceUtils
						.getHeadBitmap(memberInfo.getH_r_id());
				if (img != null) {
					holder.itemsIcon.setImageBitmap(img);
				} else {
					bitmapUtils.display(holder.itemsIcon,
							memberInfo.getHeadUrl(),
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
									holder.itemsIcon
											.setImageResource(R.drawable.head1);
								}

							});
				}
			}
		}

		// 图片的下载
		// mAbImageDownloader.display(holder.itemsIcon, null);

		return convertView;
	}

	/**
	 * View元素
	 */
	static class ViewHolder {
		ImageView itemsIcon;
		TextView itemsTitle;
		TextView itemsText;
	}
}

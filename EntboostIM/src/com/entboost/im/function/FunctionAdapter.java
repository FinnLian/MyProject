package com.entboost.im.function;

import java.util.Vector;

import net.yunim.service.entity.FuncInfo;
import net.yunim.utils.ResourceUtils;

import org.apache.commons.lang3.StringUtils;

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

public class FunctionAdapter extends BaseAdapter {
	private Context mContext;
	// xml转View对象
	private LayoutInflater mInflater;
	private Vector<FuncInfo> list=new Vector<FuncInfo>();
	private BitmapUtils bitmapUtils;

	public FunctionAdapter(Context context, LayoutInflater mInflater,
			Vector<FuncInfo> list) {
		this.mContext = context;
		// 用于将xml转为View
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list.addAll(list);
		bitmapUtils = new BitmapUtils(mContext);
	}

	public void setList(Vector<FuncInfo> list) {
		this.list.clear();
		this.list.addAll(list);
	}

	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public Object getItem(int position) {
		return this.list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			// 使用自定义的list_items作为Layout
			convertView = mInflater.inflate(R.layout.item_funcinfo, parent,
					false);
			// 减少findView的次数
			holder = new ViewHolder();
			// 初始化布局中的元素
			holder.itemsIcon = ((ImageView) convertView
					.findViewById(R.id.msg_head));
			holder.itemsTitle = ((TextView) convertView
					.findViewById(R.id.msg_name));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 获取该行的数据
		FuncInfo obj = (FuncInfo) getItem(position);
		holder.itemsTitle.setText(obj.getFunc_name());
		if (StringUtils.isNotBlank(obj.getIcon_res_id())) {
			Bitmap img = ResourceUtils.getHeadBitmap(Long.valueOf(obj
					.getIcon_res_id()));
			if (img != null) {
				holder.itemsIcon.setImageBitmap(img);
			} else {
				bitmapUtils.display(holder.itemsIcon, obj.getIconUrl(),
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
		return convertView;
	}

	/**
	 * View元素
	 */
	static class ViewHolder {
		ImageView itemsIcon;
		TextView itemsTitle;
	}
}

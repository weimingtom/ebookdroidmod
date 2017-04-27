package org.mupdfdemo2.adapter;

import java.util.LinkedList;

import org.mupdfdemo2.model.ChoosePDFItem;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iteye.weimingtom.ebdmod.R;

public class ChoosePDFAdapter extends BaseAdapter {
	private final LinkedList<ChoosePDFItem> mItems;
	private final LayoutInflater mInflater;

	public ChoosePDFAdapter(LayoutInflater inflater) {
		mInflater = inflater;
		mItems = new LinkedList<ChoosePDFItem>();
	}

	public void clear() {
		mItems.clear();
	}

	public void add(ChoosePDFItem item) {
		mItems.add(item);
		notifyDataSetChanged();
	}

	public int getCount() {
		return mItems.size();
	}

	public Object getItem(int i) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	private int iconForType(int type) {
		switch (type) {
		case ChoosePDFItem.PARENT: 
			return R.drawable.ebookdroid_ic_arrow_up;
		
		case ChoosePDFItem.DIR: 
			return R.drawable.ebookdroid_ic_dir;
		
		case ChoosePDFItem.DOC: 
			return R.drawable.ebookdroid_ic_doc;
		
		default: 
			return 0;
		}
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = mInflater.inflate(R.layout.ebookdroid_picker_entry, null);
		} else {
			v = convertView;
		}
		ChoosePDFItem item = mItems.get(position);
		((TextView)v.findViewById(R.id.name)).setText(item.name);
		((ImageView)v.findViewById(R.id.icon)).setImageResource(iconForType(item.type));
		((ImageView)v.findViewById(R.id.icon)).setColorFilter(Color.argb(255, 0, 0, 0));
		return v;
	}

}

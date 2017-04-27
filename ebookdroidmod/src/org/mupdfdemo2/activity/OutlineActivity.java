package org.mupdfdemo2.activity;

import org.mupdfdemo2.adapter.OutlineAdapter;
import org.mupdfdemo2.model.OutlineActivityData;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.artifex.mupdfdemo.OutlineItem;
import com.iteye.weimingtom.ebdmod.BaseActivity;
import com.iteye.weimingtom.ebdmod.R;

/**
 * FIXME:大纲，需要重新整理为继承Activity类
 * @author Administrator
 *
 */
public class OutlineActivity extends BaseActivity {
	private final static boolean D = false;
	private final static String TAG = "OutlineActivity";
	
	private ListView listViewOutline;
	private OutlineItem mItems[];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ebookdroid_mupdf_activity_outline);
		setupActionBar();
		listViewOutline = (ListView) this.findViewById(R.id.listViewOutline);
		
		mItems = OutlineActivityData.get().items;
		listViewOutline.setAdapter(new OutlineAdapter(getLayoutInflater(),mItems));
		listViewOutline.setSelection(OutlineActivityData.get().position);
		listViewOutline.setDividerHeight(0);
		listViewOutline.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				OutlineActivity.this.onListItemClick(l, v, position, id);
			}
		});
		setResult(-1);
	}

	protected void onListItemClick(AdapterView<?> l, View v, int position, long id) {
		OutlineActivityData.get().position = 
				listViewOutline.getFirstVisiblePosition();
		if (mItems[position].type == OutlineItem.TYPE_FZ_LINK_GOTO) {
			setResult(mItems[position].page);
			finish();
		} else {
			//FIXME:"未实现URL跳转"
			if (D) {
				Log.e(TAG, "not implemented URL jump");
			}
			finish();
		}
	}
	
	private void setupActionBar() {
		actionBar.setIcon(R.drawable.icon_detail);
		actionBar.setTitle(MuPDFActivity.TITLE_PDF_OUTLINE);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
    @Override
    public boolean onOptionsItemSelected(
    	com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	finish();
        	return true;
        }
        return false;
    }
}

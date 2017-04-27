package org.ebookdroid2.dialog;

import java.util.List;

import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.activity.ViewerActivityController;
import org.ebookdroid2.adapter.OutlineAdapter;
import org.ebookdroid2.codec.OutlineLink;
import org.ebookdroid2.manager.BookSettings;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class OutlineDialog extends Dialog implements OnItemClickListener {
    final IActivityController base;
    final List<OutlineLink> outline;

    public OutlineDialog(final IActivityController base, final List<OutlineLink> outline) {
        super(base.getContext());
        this.base = base;
        this.outline = outline;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        maximizeWindow(getWindow());        
        setTitle("大纲");
        final ListView listView = new ListView(getContext());
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setContentView(listView);
        final BookSettings bs = base.getBookSettings();
        OutlineLink current = null;
        if (bs != null) {
            final int currentIndex = bs.currentPage.docIndex;
            for (final OutlineLink item : outline) {
                if (currentIndex <= item.targetPage - 1) {
                    current = item;
                    break;
                }
            }
        }
        final OutlineAdapter adapter = new OutlineAdapter(getContext(), base, outline, current);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        if (current != null) {
            int pos = adapter.getItemPosition(current);
            if (pos != -1) {
                listView.setSelection(pos);
            }
        }
    }
    
	public static void maximizeWindow(final Window window) {
		window.setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
	}

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        this.dismiss();
        //FIXME:这里转换有问题
        ViewerActivityController ctrl = (ViewerActivityController)(this.base);
        ctrl.gotoOutlineItem(parent, view, position, id);
    }
}

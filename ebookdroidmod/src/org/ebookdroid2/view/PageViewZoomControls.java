package org.ebookdroid2.view;

import org.ebookdroid2.model.ZoomModel;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

//缩放控件层
public class PageViewZoomControls extends LinearLayout {
    public PageViewZoomControls(final Context context, final ZoomModel zoomModel) {
        super(context);
        setVisibility(View.GONE);
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.BOTTOM);
        addView(fillInParent(this, new ZoomRoll(context, zoomModel)));
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return false;
    }
    
    public static View fillInParent(final View parent, final View view) {
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        return view;
    }
}

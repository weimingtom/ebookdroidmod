package org.ebookdroid2.view;

import org.ebookdroid2.dialog.GoToPageDialog;
import org.ebookdroid2.touch.DefaultGestureDetector;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * 这个类res/layout需要用到，请勿删除
 *
 */
public class BookmarkView extends TextView {
	private final static boolean D = false;
	private final static String TAG = "BookmarkView";
	
    protected GoToPageDialog actions;

    protected DefaultGestureDetector detector;

    public BookmarkView(final Context context) {
        super(context);
        init(context);
    }

    public BookmarkView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BookmarkView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    protected void init(final Context context) {
        detector = new DefaultGestureDetector(context, new GestureListener());
    }

    public GoToPageDialog getActions() {
        return actions;
    }

    public void setActions(final GoToPageDialog actions) {
        this.actions = actions;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (D) {
        	Log.e(TAG, "onTouch(" + event + ")");
        }
        super.onTouchEvent(event);
        return detector.onTouchEvent(event);
    }

    protected class GestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(final MotionEvent e) {
            if (D) {
            	Log.e(TAG, "onSingleTapConfirmed(" + e + ")");
            }
            if (actions != null) {
                actions.updateControls(BookmarkView.this);
            }
            //FIXME:processTap(TouchManager.Touch.SingleTap, e);
            return true; 
        }

        @Override
        public boolean onDoubleTap(final MotionEvent e) {
            if (D) {
            	Log.e(TAG, "onDoubleTap(" + e + ")");
            }
            if (actions != null) {
            	actions.showAddBookmarkDlg(BookmarkView.this);
            }
            //FIXME: processTap(TouchManager.Touch.DoubleTap, e);
            return true; 
        }

        @Override
        public void onLongPress(final MotionEvent e) {
            if (D) {
            	Log.e(TAG, "onLongPress(" + e + ")");
            }
            final MotionEvent cancel = MotionEvent.obtain(e);
            cancel.setAction(MotionEvent.ACTION_CANCEL);
            detector.onTouchEvent(cancel);
        }

        @Override
        public boolean onDown(final MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(final MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float vX, final float vY) {
            return true;
        }

        @Override
        public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
            return true;
        }
    }

}

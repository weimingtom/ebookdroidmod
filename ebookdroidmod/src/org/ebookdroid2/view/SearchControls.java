package org.ebookdroid2.view;

import org.ebookdroid2.activity.ViewerActivityHelper;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iteye.weimingtom.ebdmod.R;

//搜索控件
public class SearchControls extends LinearLayout {
    private EditText m_edit;
    private ImageButton m_prevButton;
    private ImageButton m_nextButton;

    public SearchControls(final Activity parent, final ViewerActivityHelper helper) {
        super(parent);
        setVisibility(View.GONE);
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(parent).inflate(R.layout.ebookdroid_seach_controls, this, true);
        m_prevButton = (ImageButton) findViewById(R.id.search_controls_prev);
        m_nextButton = (ImageButton) findViewById(R.id.search_controls_next);
        m_edit = (EditText) findViewById(R.id.search_controls_edit);
        //向后搜索
        m_prevButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//backwardSearch
				helper.getController(parent).doSearch(m_edit.getEditableText(), null, "false");
			}
        });
        //向前搜索
        m_nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//forwardSearch
				helper.getController(parent).doSearch(m_edit.getEditableText(), null, "true");
			}
        });
        //回车向前搜索
        m_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView textView, final int actionId, final KeyEvent keyEvent) {
                if ((actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE)) {
                    if ((keyEvent == null || keyEvent.getAction() == KeyEvent.ACTION_UP)) {
                    	helper.getController(parent).doSearch(m_edit.getEditableText(), textView.getText(), "true");
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            m_edit.requestFocus();
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return false;
    }

    public int getActualHeight() {
        return m_edit.getHeight();
    }
}

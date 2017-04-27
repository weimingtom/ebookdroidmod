package org.mupdfdemo2.view;

import android.content.Context;
import android.widget.ImageView;

public class OpaqueImageView extends ImageView {
	public OpaqueImageView(Context context) {
		super(context);
	}

	@Override
	public boolean isOpaque() {
		return true;
	}
}

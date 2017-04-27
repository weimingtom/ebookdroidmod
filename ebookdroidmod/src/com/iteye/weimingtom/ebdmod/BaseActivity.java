package com.iteye.weimingtom.ebdmod;

import android.os.Bundle;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

public class BaseActivity extends SherlockActivity {
	protected ActionBar actionBar;
	protected ActionBarSherlock abs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		abs = this.getSherlock();
		actionBar = this.getSupportActionBar();
	}
	
	protected boolean getIsUploadOffLine() {
		//TODO:return true if stop asking upload
		return true;
	}
	
	protected void onUploadProcess(String pdffilepath) {
		//TODO:begin upload after asking upload
		//do nothing
	}
}

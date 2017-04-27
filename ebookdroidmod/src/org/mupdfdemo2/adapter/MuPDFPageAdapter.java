package org.mupdfdemo2.adapter;

import org.mupdfdemo2.task.AsyncTask;
import org.mupdfdemo2.view.MuPDFPageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.artifex.mupdfdemo.MuPDFCore;

public class MuPDFPageAdapter extends BaseAdapter {
	private final static boolean D = false;
	private final static String TAG = "MuPDFPageAdapter";
	
	private final Context mContext;
	private final MuPDFCore mCore;
	private final SparseArray<PointF> mPageSizes = new SparseArray<PointF>();
	private Bitmap mSharedHqBm;
	//新增，单页模式
	private boolean singlePageMode = false;
	private int singlePagePosition = 0;
	
	//新增，启动单页模式，用于批注
	public void enableSinglePage(boolean singlePageMode, 
			int singlePagePosition) {
		this.singlePageMode = singlePageMode;
		this.singlePagePosition = singlePagePosition;
		this.mPageSizes.clear();
		int page = mCore.countPages();
		if (this.singlePageMode) {
			//超出页面
			if (this.singlePagePosition < 0 && 
				this.singlePagePosition >= page) {
				this.singlePagePosition = -1;
			}
		}
		this.notifyDataSetChanged();
	}
	
	public MuPDFPageAdapter(Context c, MuPDFCore core) {
		mContext = c;
		mCore = core;
	}

	//MuPDFActivity.MY_MOD
	public int getCount() {
		if (singlePageMode) {
			return mCore.countPages() > 0 ? 1 : 0;
		} else {
			return mCore.countPages();
		}
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public void releaseBitmaps() {
		if (mSharedHqBm != null) {
			mSharedHqBm.recycle();
		}
		mSharedHqBm = null;
	}

	@Override
	public View getView(int position_, View convertView, ViewGroup parent) {
		if (D) {
			Log.e(TAG, "getView 001");
		}
		final MuPDFPageView pageView;
		if (convertView == null) {
			if (mSharedHqBm == null || mSharedHqBm.getWidth() != parent.getWidth() || mSharedHqBm.getHeight() != parent.getHeight()) {
				mSharedHqBm = Bitmap.createBitmap(parent.getWidth(), parent.getHeight(), Bitmap.Config.ARGB_8888);
			}
			pageView = new MuPDFPageView(mContext, mCore, new Point(parent.getWidth(), parent.getHeight()), mSharedHqBm);
		} else {
			pageView = (MuPDFPageView) convertView;
		}
		if (singlePageMode) {
			position_ = singlePagePosition;
		}
		final int position = position_;
		if (D) {
			Log.e(TAG, "getView 002 position = " + position);
		}
		if (singlePageMode && position < 0) {
			//异常情况，页数超出范围
			pageView.blank();
		} else {
			PointF pageSize = mPageSizes.get(position);
			if (pageSize != null) {
				if (D) {
					Log.e(TAG, "getView 003 position = " + position);
				}
				pageView.setPage(position, pageSize);
			} else {
				if (D) {
					Log.e(TAG, "getView 004 position = " + position);
				}
				pageView.blank(position);
				//FIXME:这里是耗时操作
				AsyncTask<Void,Void,PointF> sizingTask = new AsyncTask<Void,Void,PointF>() {
					@Override
					protected PointF doInBackground(Void... arg0) {
						if (D) {
							Log.e(TAG, "getView 005 position = " + position);
						}
						return mCore.getPageSize(position);
					}
	
					@Override
					protected void onPostExecute(PointF result) {
						super.onPostExecute(result);
						if (D) {
							Log.e(TAG, "getView 006 position = " + position);
						}
						mPageSizes.put(position, result);
						if (pageView.getPage() == position) {
							if (D) {
								Log.e(TAG, "getView 007 position = " + position);
							}
							pageView.setPage(position, result);
						}
					}
				};
				sizingTask.execute((Void)null);
			}
		}
		if (D) {
			Log.e(TAG, "getView 002-2 position = " + position);
		}
		return pageView;
	}
}

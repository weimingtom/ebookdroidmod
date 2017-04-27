package org.mupdfdemo2.view;

import java.util.ArrayList;
import java.util.Iterator;

import org.mupdfdemo2.model.Hit;
import org.mupdfdemo2.model.TextProcessor;
import org.mupdfdemo2.model.TextSelector;
import org.mupdfdemo2.task.AsyncTask;
import org.mupdfdemo2.task.CancellableAsyncTask;
import org.mupdfdemo2.task.CancellableTaskDefinition;
import org.mupdfdemo2.task.MuPDFCancellableTaskDefinition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.artifex.mupdfdemo.Annotation;
import com.artifex.mupdfdemo.LinkInfo;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.TextWord;
import com.iteye.weimingtom.ebdmod.R;

public class MuPDFPageView extends ViewGroup implements MuPDFView {
	private final static boolean D = false;
	private final static String TAG = "MuPDFPageView";

	private static final int HIGHLIGHT_COLOR = 0x802572AC;
	private static final int LINK_COLOR = 0x80AC7225; //褐色
	private static final int BOX_COLOR = 0xFF4444FF;
	private static final int INK_COLOR = 0xFFFF0000;
	private static final float INK_THICKNESS = 10.0f;
	public static final int BACKGROUND_COLOR = 0xFFFFFFFF;
	private static final int PROGRESS_DIALOG_DELAY = 200;
	
	protected final Context mContext;
	protected int mPageNumber;
	private Point mParentSize;
	protected Point mSize;
	protected float mSourceScale;
	private ImageView mEntire; 
	private Bitmap mEntireBm;
	private Matrix mEntireMat;
	private AsyncTask<Void,Void,TextWord[][]> mGetText;
	private AsyncTask<Void,Void,LinkInfo[]> mGetLinkInfo;
	private CancellableAsyncTask<Void, Void> mDrawEntire;
	private Point mPatchViewSize; 
	private Rect mPatchArea;
	private ImageView mPatch;
	private Bitmap mPatchBm;
	private CancellableAsyncTask<Void,Void> mDrawPatch;
	private RectF mSearchBoxes[];
	protected LinkInfo mLinks[];
	private RectF mSelectBox;
	private TextWord mText[][];
	private RectF mItemSelectBox;
	//批注的点
	protected ArrayList<ArrayList<PointF>> mDrawing;
	//FIXME:高亮染色图层，同时也是批注层
	private View mSearchView; 
	private boolean mIsBlank;
	//FIXME:是否高亮显示链接，下级开关，默认应该是false
	private boolean mHighlightLinks = false; 
	private ProgressBar mBusyIndicator;
	private final Handler mHandler = new Handler();
	private final MuPDFCore mCore;
	private Annotation mAnnotations[];
	private int mSelectedAnnotationIndex = -1;
	private AsyncTask<Void, Void, Annotation[]> mLoadAnnotations;
	private AsyncTask<PointF[], Void, Void> mAddStrikeOut;
	private AsyncTask<PointF[][], Void, Void> mAddInk;
	private AsyncTask<Integer, Void, Void> mDeleteAnnotation;

	public MuPDFPageView(Context c,
			MuPDFCore core,
			Point parentSize, Bitmap sharedHqBm) {
		super(c);
		mContext  = c;
		mParentSize = parentSize;
		setBackgroundColor(BACKGROUND_COLOR);
		mEntireBm = Bitmap.createBitmap(parentSize.x, parentSize.y, Config.ARGB_8888);
		mPatchBm = sharedHqBm;
		mEntireMat = new Matrix();
		mCore = core;
	}

	@Override
	public LinkInfo hitLink(float x, float y) {
		float scale = mSourceScale * (float) getWidth() / (float) mSize.x;
		float docRelX = (x - getLeft()) / scale;
		float docRelY = (y - getTop()) / scale;
		for (LinkInfo l : mLinks) {
			if (l.rect.contains(docRelX, docRelY)) {
				return l;
			}
		}
		return null;
	}

	@Override
	public Hit passClickEvent(float x, float y) {
		float scale = mSourceScale * (float) getWidth() / (float) mSize.x;
		final float docRelX = (x - getLeft()) / scale;
		final float docRelY = (y - getTop()) / scale;
		boolean hit = false;
		int i;
		if (mAnnotations != null) {
			for (i = 0; i < mAnnotations.length; i++) {
				if (mAnnotations[i].contains(docRelX, docRelY)) {
					hit = true;
					break;
				}
			}
			if (hit) {
				switch (mAnnotations[i].type) {
				case Annotation.HIGHLIGHT:
				case Annotation.UNDERLINE:
				case Annotation.SQUIGGLY:
				case Annotation.STRIKEOUT:
				case Annotation.INK:
					mSelectedAnnotationIndex = i;
					setItemSelectBox(mAnnotations[i]);
					return Hit.Annotation;
				}
			}
		}
		mSelectedAnnotationIndex = -1;
		setItemSelectBox(null);
		return Hit.Nothing;
	}

	@Override
	public boolean copySelection() {
		final StringBuilder text = new StringBuilder();
		processSelectedText(new TextProcessor() {
			private StringBuilder line;

			@Override
			public void onStartLine() {
				line = new StringBuilder();
			}

			@Override
			public void onWord(TextWord word) {
				if (line.length() > 0)
					line.append(' ');
				line.append(word.w);
			}

			@Override
			public void onEndLine() {
				if (text.length() > 0)
					text.append('\n');
				text.append(line);
			}
		});
		if (text.length() == 0) {
			return false;
		}
		//剪贴板开始
		android.text.ClipboardManager cm = 
			(android.text.ClipboardManager) mContext
				.getSystemService(Context.CLIPBOARD_SERVICE);
		cm.setText(text);
		//剪贴板结束
		deselectText();
		return true;
	}

	@Override
	public boolean markupSelection(final int type) {
		final ArrayList<PointF> quadPoints = new ArrayList<PointF>();
		processSelectedText(new TextProcessor() {
			private RectF rect;

			@Override
			public void onStartLine() {
				rect = new RectF();
			}

			@Override
			public void onWord(TextWord word) {
				rect.union(word);
			}

			@Override
			public void onEndLine() {
				if (!rect.isEmpty()) {
					quadPoints.add(new PointF(rect.left, rect.bottom));
					quadPoints.add(new PointF(rect.right, rect.bottom));
					quadPoints.add(new PointF(rect.right, rect.top));
					quadPoints.add(new PointF(rect.left, rect.top));
				}
			}
		});
		if (quadPoints.size() == 0) {
			return false;
		}
		mAddStrikeOut = new AsyncTask<PointF[], Void, Void>() {
			@Override
			protected Void doInBackground(PointF[]... params) {
				addMarkup(params[0], type);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				loadAnnotations();
				update();
			}
		};
		mAddStrikeOut.execute(
			quadPoints.toArray(new PointF[quadPoints.size()]));
		deselectText();
		return true;
	}

	@Override
	public void deleteSelectedAnnotation() {
		if (mSelectedAnnotationIndex != -1) {
			if (mDeleteAnnotation != null) {
				mDeleteAnnotation.cancel(true);
			}
			mDeleteAnnotation = new AsyncTask<Integer, Void, Void>() {
				@Override
				protected Void doInBackground(Integer... params) {
					mCore.deleteAnnotation(mPageNumber, params[0]);
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					loadAnnotations();
					update();
				}
			};
			mDeleteAnnotation.execute(mSelectedAnnotationIndex);
			mSelectedAnnotationIndex = -1;
			setItemSelectBox(null);
		}
	}

	@Override
	public void deselectAnnotation() {
		mSelectedAnnotationIndex = -1;
		setItemSelectBox(null);
	}

	//FIXME:这个异步处理导致可能退出时还没保存，
	//而使得core.hasChange()方法返回false
	@Override
	public boolean saveDraw() {
		PointF[][] path = getDraw();
		if (path == null) {
			return false;
		}
		//path非空才可以设置为已修改
		mCore.isModified = true;
		if (mAddInk != null) {
			mAddInk.cancel(true);
			mAddInk = null;
		}
		mAddInk = new AsyncTask<PointF[][], Void, Void>() {
			@Override
			protected Void doInBackground(PointF[][]... params) {
				mCore.addInkAnnotation(mPageNumber, params[0]);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				loadAnnotations();
				update();
			}
		};
		mAddInk.execute(getDraw());
		cancelDraw();
		return true;
	}

	protected CancellableTaskDefinition<Void, Void> getDrawPageTask(
			final Bitmap bm, final int sizeX, final int sizeY,
			final int patchX, final int patchY, final int patchWidth,
			final int patchHeight) {
		return new MuPDFCancellableTaskDefinition<Void, Void>(mCore) {
			@Override
			public Void doInBackground(MuPDFCore.Cookie cookie, Void... params) {
				//FIXME:这里移除了bm.eraseColor(0);
				//FIXME:这个是为了兼容安卓3，但目前未出现过问题
				mCore.drawPage(bm, mPageNumber, sizeX, sizeY, patchX, patchY, patchWidth, patchHeight, cookie);
				return null;
			}
		};
	}

	protected CancellableTaskDefinition<Void, Void> getUpdatePageTask(
			final Bitmap bm, final int sizeX, final int sizeY,
			final int patchX, final int patchY, final int patchWidth,
			final int patchHeight) {
		return new MuPDFCancellableTaskDefinition<Void, Void>(mCore) {
			@Override
			public Void doInBackground(MuPDFCore.Cookie cookie, Void... params) {
				//FIXME:这里移除了bm.eraseColor(0);
				//FIXME:这个是为了兼容安卓3，但目前未出现过问题
				mCore.updatePage(bm, mPageNumber, sizeX, sizeY, patchX, patchY, patchWidth, patchHeight, cookie);
				return null;
			}
		};
	}

	protected LinkInfo[] getLinkInfo() {
		//FIXME:在C代码里面做缩放
		return mCore.getPageLinks(mPageNumber, true); 
	}

	protected TextWord[][] getText() {
		return mCore.textLines(mPageNumber);
	}

	protected void addMarkup(PointF[] quadPoints, int type) {
		mCore.addMarkupAnnotation(mPageNumber, quadPoints, type);
	}

	private void loadAnnotations() {
		mAnnotations = null;
		if (mLoadAnnotations != null) {
			mLoadAnnotations.cancel(true);
		}
		mLoadAnnotations = new AsyncTask<Void, Void, Annotation[]>() {
			@Override
			protected Annotation[] doInBackground(Void... params) {
				return mCore.getAnnoations(mPageNumber);
			}

			@Override
			protected void onPostExecute(Annotation[] result) {
				mAnnotations = result;
			}
		};
		mLoadAnnotations.execute();
	}

	@Override
	public void setPage(final int page, PointF size) {
		loadAnnotations();
		setPageOld(page, size);
	}

	@Override
	public void setScale(float scale) {
		//FIXME:do nothing通过onlayout自动缩放，不需要写代码
	}

	@Override
	public void releaseResources() {
		if (mLoadAnnotations != null) {
			mLoadAnnotations.cancel(true);
			mLoadAnnotations = null;
		}
		if (mAddStrikeOut != null) {
			mAddStrikeOut.cancel(true);
			mAddStrikeOut = null;
		}
		if (mDeleteAnnotation != null) {
			mDeleteAnnotation.cancel(true);
			mDeleteAnnotation = null;
		}
		this.releaseResourcesOld();
	}

	private void reinit() {
		if (mDrawEntire != null) {
			mDrawEntire.cancelAndWait();
			mDrawEntire = null;
		}
		if (mDrawPatch != null) {
			mDrawPatch.cancelAndWait();
			mDrawPatch = null;
		}
		if (mGetLinkInfo != null) {
			mGetLinkInfo.cancel(true);
			mGetLinkInfo = null;
		}
		if (mGetText != null) {
			mGetText.cancel(true);
			mGetText = null;
		}
		mIsBlank = true;
		mPageNumber = 0;
		if (mSize == null) {
			mSize = mParentSize;
		}
		if (mEntire != null) {
			mEntire.setImageBitmap(null);
			mEntire.invalidate();
		}
		if (mPatch != null) {
			mPatch.setImageBitmap(null);
			mPatch.invalidate();
		}
		mPatchViewSize = null;
		mPatchArea = null;
		mSearchBoxes = null;
		mLinks = null;
		mSelectBox = null;
		mText = null;
		mItemSelectBox = null;
	}

	public void releaseResourcesOld() {
		reinit();
		if (mBusyIndicator != null) {
			removeView(mBusyIndicator);
			mBusyIndicator = null;
		}
	}

	@Override
	public void releaseBitmaps() {
		reinit();
		if (mEntireBm != null) {
			mEntireBm.recycle();
		}
		mEntireBm = null;
		if (mPatchBm != null) {
			mPatchBm.recycle();
		}
		mPatchBm = null;
	}

	@Override
	public void blank(int page) {
		reinit();
		mPageNumber = page;
		//FIXME:这个是个在后面转动的loading动画，
		//用于AsyncTask，不是在最上面图层，而是在最下面
		if (mBusyIndicator == null) {
			mBusyIndicator = new ProgressBar(mContext);
			mBusyIndicator.setIndeterminate(true);
			mBusyIndicator.setBackgroundResource(R.drawable.ebookdroid_busy);
			addView(mBusyIndicator);
		}
		setBackgroundColor(BACKGROUND_COLOR);
	}

	//新增，用于异常情况
	//FIXME:这个方法未测试
	public void blank() {
		reinit();
		mPageNumber = 0;
		setBackgroundColor(BACKGROUND_COLOR);
	}
	
	public void setPageOld(int page, PointF size) {
		if (mDrawEntire != null) {
			mDrawEntire.cancelAndWait();
			mDrawEntire = null;
		}
		mIsBlank = false;
		if (mSearchView != null) {
			mSearchView.invalidate();
		}
		mPageNumber = page;
		if (mEntire == null) {
			mEntire = new OpaqueImageView(mContext);
			mEntire.setScaleType(ImageView.ScaleType.MATRIX);
			addView(mEntire);
		}
		mSourceScale = Math.min(mParentSize.x / size.x, 
				mParentSize.y / size.y);
		Point newSize = new Point(
				(int)(size.x * mSourceScale), 
				(int)(size.y * mSourceScale));
		mSize = newSize;
		mEntire.setImageBitmap(null);
		mEntire.invalidate();
		mGetLinkInfo = new AsyncTask<Void,Void,LinkInfo[]>() {
			protected LinkInfo[] doInBackground(Void... v) {
				return getLinkInfo();
			}
			protected void onPostExecute(LinkInfo[] v) {
				mLinks = v;
				if (D) {
					Log.e(TAG, "=================> mLinks : " + mLinks + ", len = " + (mLinks != null ? mLinks.length : 0));
				}
				if (mSearchView != null) {
					mSearchView.invalidate();
				}
			}
		};
		mGetLinkInfo.execute();
		mDrawEntire = new CancellableAsyncTask<Void, Void>(
				getDrawPageTask(mEntireBm, mSize.x, mSize.y, 
						0, 0, mSize.x, mSize.y)) {
			@Override
			public void onPreExecute() {
				setBackgroundColor(BACKGROUND_COLOR);
				mEntire.setImageBitmap(null);
				mEntire.invalidate();
				if (mBusyIndicator == null) {
					mBusyIndicator = new ProgressBar(mContext);
					mBusyIndicator.setIndeterminate(true);
					mBusyIndicator.setBackgroundResource(R.drawable.ebookdroid_busy);
					addView(mBusyIndicator);
					mBusyIndicator.setVisibility(INVISIBLE);
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (mBusyIndicator != null)
								mBusyIndicator.setVisibility(VISIBLE);
						}
					}, PROGRESS_DIALOG_DELAY);
				}
			}

			@Override
			public void onPostExecute(Void result) {
				removeView(mBusyIndicator);
				mBusyIndicator = null;
				mEntire.setImageBitmap(mEntireBm);
				mEntire.invalidate();
				setBackgroundColor(Color.TRANSPARENT);
			}
		};
		mDrawEntire.execute();
		if (mSearchView == null) {
			mSearchView = new View(mContext) {
				@Override
				protected void onDraw(final Canvas canvas) {
					super.onDraw(canvas);
					final float scale = mSourceScale*(float)getWidth()/(float)mSize.x;
					final Paint paint = new Paint();
					if (!mIsBlank && mSearchBoxes != null) {
						paint.setColor(HIGHLIGHT_COLOR);
						for (RectF rect : mSearchBoxes)
							canvas.drawRect(rect.left*scale, rect.top*scale,
									        rect.right*scale, rect.bottom*scale,
									        paint);
					}
					if (!mIsBlank && mLinks != null && mHighlightLinks) { //FIXME:这里用于染色为褐色
						paint.setColor(LINK_COLOR);
						for (LinkInfo link : mLinks) {
							canvas.drawRect(link.rect.left*scale, link.rect.top*scale,
									        link.rect.right*scale, link.rect.bottom*scale,
									        paint);
						}
					}
					if (mSelectBox != null && mText != null) {
						paint.setColor(HIGHLIGHT_COLOR);
						processSelectedText(new TextProcessor() {
							RectF rect;

							@Override
							public void onStartLine() {
								rect = new RectF();
							}

							@Override
							public void onWord(TextWord word) {
								rect.union(word);
							}

							@Override
							public void onEndLine() {
								if (!rect.isEmpty())
									canvas.drawRect(rect.left*scale, rect.top*scale, rect.right*scale, rect.bottom*scale, paint);
							}
						});
					}
					if (mItemSelectBox != null) {
						paint.setStyle(Paint.Style.STROKE);
						paint.setColor(BOX_COLOR);
						canvas.drawRect(mItemSelectBox.left*scale, mItemSelectBox.top*scale, mItemSelectBox.right*scale, mItemSelectBox.bottom*scale, paint);
					}
					if (mDrawing != null) {
						Path path = new Path();
						PointF p;
						paint.setAntiAlias(true);
						paint.setDither(true);
						paint.setStrokeJoin(Paint.Join.ROUND);
						paint.setStrokeCap(Paint.Cap.ROUND);
						paint.setStyle(Paint.Style.FILL);
						paint.setStrokeWidth(INK_THICKNESS * scale);
						paint.setColor(INK_COLOR);
						Iterator<ArrayList<PointF>> it = mDrawing.iterator();
						while (it.hasNext()) {
							ArrayList<PointF> arc = it.next();
							if (arc.size() >= 2) {
								Iterator<PointF> iit = arc.iterator();
								p = iit.next();
								float mX = p.x * scale;
								float mY = p.y * scale;
								path.moveTo(mX, mY);
								while (iit.hasNext()) {
									p = iit.next();
									float x = p.x * scale;
									float y = p.y * scale;
									path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
									mX = x;
									mY = y;
								}
								path.lineTo(mX, mY);
							} else {
								p = arc.get(0);
								canvas.drawCircle(p.x * scale, p.y * scale, INK_THICKNESS * scale / 2, paint);
							}
						}
						paint.setStyle(Paint.Style.STROKE);
						canvas.drawPath(path, paint);
					}
				}
			};
			addView(mSearchView);
		}
		requestLayout();
	}

	@Override
	public void setSearchBoxes(RectF searchBoxes[]) {
		mSearchBoxes = searchBoxes;
		if (mSearchView != null)
			mSearchView.invalidate();
	}

	@Override
	public void setLinkHighlighting(boolean f) {
		mHighlightLinks = f;
		if (mSearchView != null) {
			mSearchView.invalidate();
		}
	}

	@Override
	public void deselectText() {
		mSelectBox = null;
		mSearchView.invalidate();
	}

	@Override
	public void selectText(float x0, float y0, float x1, float y1) {
		float scale = mSourceScale * (float)getWidth() / (float)mSize.x;
		float docRelX0 = (x0 - getLeft())/scale;
		float docRelY0 = (y0 - getTop())/scale;
		float docRelX1 = (x1 - getLeft())/scale;
		float docRelY1 = (y1 - getTop())/scale;
		if (docRelY0 <= docRelY1) {
			mSelectBox = new RectF(docRelX0, docRelY0, docRelX1, docRelY1);
		} else {
			mSelectBox = new RectF(docRelX1, docRelY1, docRelX0, docRelY0);
		}
		mSearchView.invalidate();
		if (mGetText == null) {
			mGetText = new AsyncTask<Void,Void,TextWord[][]>() {
				@Override
				protected TextWord[][] doInBackground(Void... params) {
					return getText();
				}
				
				@Override
				protected void onPostExecute(TextWord[][] result) {
					mText = result;
					mSearchView.invalidate();
				}
			};
			mGetText.execute();
		}
	}

	//批注模式开始
	@Override
	public void startDraw(float x, float y) {
		float scale = mSourceScale * (float)getWidth() / (float)mSize.x;
		float docRelX = (x - getLeft()) / scale;
		float docRelY = (y - getTop()) / scale;
		if (mDrawing == null) {
			mDrawing = new ArrayList<ArrayList<PointF>>();
		}
		ArrayList<PointF> arc = new ArrayList<PointF>();
		arc.add(new PointF(docRelX, docRelY));
		mDrawing.add(arc);
		mSearchView.invalidate();
	}

	@Override
	public void continueDraw(float x, float y) {
		float scale = mSourceScale * (float)getWidth() / (float)mSize.x;
		float docRelX = (x - getLeft()) / scale;
		float docRelY = (y - getTop()) / scale;
		if (mDrawing != null && mDrawing.size() > 0) {
			ArrayList<PointF> arc = mDrawing.get(mDrawing.size() - 1);
			arc.add(new PointF(docRelX, docRelY));
			mSearchView.invalidate();
		}
	}

	@Override
	public void cancelDraw() {
		mDrawing = null;
		mSearchView.invalidate();
	}

	protected PointF[][] getDraw() {
		if (mDrawing == null) {
			return null;
		}
		PointF[][] path = new PointF[mDrawing.size()][];
		for (int i = 0; i < mDrawing.size(); i++) {
			ArrayList<PointF> arc = mDrawing.get(i);
			path[i] = arc.toArray(new PointF[arc.size()]);
		}
		return path;
	}

	protected void processSelectedText(TextProcessor tp) {
		(new TextSelector(mText, mSelectBox)).select(tp);
	}

	public void setItemSelectBox(RectF rect) {
		mItemSelectBox = rect;
		if (mSearchView != null) {
			mSearchView.invalidate();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int x, y;
		switch(View.MeasureSpec.getMode(widthMeasureSpec)) {
		case View.MeasureSpec.UNSPECIFIED:
			x = mSize.x;
			break;
			
		default:
			x = View.MeasureSpec.getSize(widthMeasureSpec);
			break;
		}
		switch(View.MeasureSpec.getMode(heightMeasureSpec)) {
		case View.MeasureSpec.UNSPECIFIED:
			y = mSize.y;
			break;
			
		default:
			y = View.MeasureSpec.getSize(heightMeasureSpec);
			break;
		}
		setMeasuredDimension(x, y);
		if (mBusyIndicator != null) {
			int limit = Math.min(mParentSize.x, mParentSize.y)/2;
			mBusyIndicator.measure(View.MeasureSpec.AT_MOST | limit, View.MeasureSpec.AT_MOST | limit);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int w = right-left;
		int h = bottom-top;
		if (mEntire != null) {
			if (mEntire.getWidth() != w || mEntire.getHeight() != h) {
				mEntireMat.setScale(w/(float)mSize.x, h/(float)mSize.y);
				mEntire.setImageMatrix(mEntireMat);
				mEntire.invalidate();
			}
			mEntire.layout(0, 0, w, h);
		}
		if (mSearchView != null) {
			mSearchView.layout(0, 0, w, h);
		}
		if (mPatchViewSize != null) {
			if (mPatchViewSize.x != w || mPatchViewSize.y != h) {
				mPatchViewSize = null;
				mPatchArea = null;
				if (mPatch != null) {
					mPatch.setImageBitmap(null);
					mPatch.invalidate();
				}
			} else {
				mPatch.layout(mPatchArea.left, mPatchArea.top, mPatchArea.right, mPatchArea.bottom);
			}
		}
		if (mBusyIndicator != null) {
			int bw = mBusyIndicator.getMeasuredWidth();
			int bh = mBusyIndicator.getMeasuredHeight();
			mBusyIndicator.layout(
				(w - bw) / 2, 
				(h - bh) / 2, 
				(w + bw) / 2, 
				(h + bh) / 2);
		}
	}

	@Override
	public void updateHq(boolean update) {
		Rect viewArea = new Rect(getLeft(),getTop(),getRight(),getBottom());
		if (viewArea.width() == mSize.x || viewArea.height() == mSize.y) {
			if (mPatch != null) {
				mPatch.setImageBitmap(null);
				mPatch.invalidate();
			}
		} else {
			final Point patchViewSize = new Point(viewArea.width(), viewArea.height());
			final Rect patchArea = new Rect(0, 0, mParentSize.x, mParentSize.y);
			if (!patchArea.intersect(viewArea)) {
				return;
			}
			patchArea.offset(-viewArea.left, -viewArea.top);
			boolean area_unchanged = patchArea.equals(mPatchArea) && patchViewSize.equals(mPatchViewSize);
			if (area_unchanged && !update) {
				return;
			}
			boolean completeRedraw = !(area_unchanged && update);
			if (mDrawPatch != null) {
				mDrawPatch.cancelAndWait();
				mDrawPatch = null;
			}
			if (mPatch == null) {
				mPatch = new OpaqueImageView(mContext);
				mPatch.setScaleType(ImageView.ScaleType.MATRIX);
				addView(mPatch);
				mSearchView.bringToFront();
			}
			CancellableTaskDefinition<Void, Void> task;
			if (completeRedraw) {
				task = getDrawPageTask(mPatchBm, patchViewSize.x, patchViewSize.y,
								patchArea.left, patchArea.top,
								patchArea.width(), patchArea.height());
			} else {
				task = getUpdatePageTask(mPatchBm, patchViewSize.x, patchViewSize.y,
						patchArea.left, patchArea.top,
						patchArea.width(), patchArea.height());
			}
			mDrawPatch = new CancellableAsyncTask<Void,Void>(task) {
				public void onPostExecute(Void result) {
					mPatchViewSize = patchViewSize;
					mPatchArea = patchArea;
					mPatch.setImageBitmap(mPatchBm);
					mPatch.invalidate();
					//FIXME：这里移除requestLayout();
					mPatch.layout(mPatchArea.left, mPatchArea.top, mPatchArea.right, mPatchArea.bottom);
				}
			};
			mDrawPatch.execute();
		}
	}

	@Override
	public void update() {
		if (mDrawEntire != null) {
			mDrawEntire.cancelAndWait();
			mDrawEntire = null;
		}
		if (mDrawPatch != null) {
			mDrawPatch.cancelAndWait();
			mDrawPatch = null;
		}
		mDrawEntire = new CancellableAsyncTask<Void, Void>(
				getUpdatePageTask(mEntireBm, mSize.x, mSize.y, 
						0, 0, mSize.x, mSize.y)) {
			public void onPostExecute(Void result) {
				mEntire.setImageBitmap(mEntireBm);
				mEntire.invalidate();
			}
		};
		mDrawEntire.execute();
		updateHq(true);
	}

	@Override
	public void removeHq() {
		if (mDrawPatch != null) {
			mDrawPatch.cancelAndWait();
			mDrawPatch = null;
		}
		mPatchViewSize = null;
		mPatchArea = null;
		if (mPatch != null) {
			mPatch.setImageBitmap(null);
			mPatch.invalidate();
		}
	}

	@Override
	public int getPage() {
		return mPageNumber;
	}

	@Override
	public boolean isOpaque() {
		return true;
	}
}

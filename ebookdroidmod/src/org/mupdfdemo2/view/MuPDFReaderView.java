package org.mupdfdemo2.view;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.mupdfdemo2.activity.MuPDFActivity;
import org.mupdfdemo2.adapter.MuPDFPageAdapter;
import org.mupdfdemo2.model.Hit;
import org.mupdfdemo2.task.SearchTaskResult;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Scroller;

import com.artifex.mupdfdemo.LinkInfo;

/**
 * FIXME:里面有很多代码是强制转换((MuPDFView) v)
 */
public class MuPDFReaderView extends AdapterView<Adapter> implements GestureDetector.OnGestureListener, 
	ScaleGestureDetector.OnScaleGestureListener, Runnable {
	private final static boolean D = false;
	private final static String TAG = "MuPDFReaderView";
	
	private static final int MOVING_DIAGONALLY = 0;
	private static final int MOVING_LEFT = 1;
	private static final int MOVING_RIGHT = 2;
	private static final int MOVING_UP = 3;
	private static final int MOVING_DOWN = 4;
	private static final int FLING_MARGIN = 100;
	private static final int GAP = 20;
	private static final float MIN_SCALE = 1.0f;
	private static final float MAX_SCALE = 5.0f;
	private static final float REFLOW_SCALE_FACTOR = 0.5f;
	private static final boolean HORIZONTAL_SCROLLING = true;
	private static final float TOUCH_TOLERANCE = 2;

	private float mX; //批注模式下坐标 
	private float mY; //批注模式下坐标
	private AlertDialog.Builder mAlertBuilder;
	private Adapter mAdapter;
	private int mCurrent; 
	private boolean mResetLayout;
	private final SparseArray<View> mChildViews = new SparseArray<View>(3);
	private final LinkedList<View> mViewCache = new LinkedList<View>();
	private boolean mUserInteracting; 
	private boolean mScaling; 
	//FIXME:查看模式下的缩放率，
	//注意只是个相对值，参考measureView()
	private float mScale = 1.0f; 
	private int mXScroll; //查看模式下手指移动页面时的偏移
	private int mYScroll; //查看模式下手指移动页面时的偏移
	private boolean mReflow = false;
	private boolean mReflowChanged = false;
	private final GestureDetector mGestureDetector;
	private final ScaleGestureDetector mScaleGestureDetector;
	private final Scroller mScroller;
	private final Stepper mStepper;
	private int mScrollerLastX;
	private int mScrollerLastY;
	private float mLastScaleFocusX;
	private float mLastScaleFocusY;
	private boolean memAlert = false;

	public static abstract class ViewMapper {
		public abstract void applyToView(View view);
	}

	public enum Mode {
		Viewing,  //正常
		Selecting, //选择文本
		Drawing //画批注
	}
	
	private final Context mContext;
	//FIXME:是否高亮显示链接，上级开关，默认应该是false
	private boolean mLinksEnabled = true; 
	private Mode mMode = Mode.Viewing;
	//FIXME:
	private boolean tapDisabled = false; 
	private int tapPageMargin;

	protected void onTapMainDocArea() {
		
	}
	
	protected void onDocMotion() {
		
	}
	
	protected void onHit(Hit item) {
		
	}

	//高亮模式，但现在不需要这样
	public void setLinksEnabled(boolean b) {
		mLinksEnabled = b;
		resetupChildren();
	}

	public void setMode(Mode m) {
		mMode = m;
	}

	public static final int BACKGROUND_COLOR = 0xFFCCCCCC;
	private void setup() {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) mContext
			.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
		tapPageMargin = (int)dm.xdpi;
		if (tapPageMargin < 100) {
			tapPageMargin = 100;
		}
		if (tapPageMargin > dm.widthPixels / 5) {
			tapPageMargin = dm.widthPixels / 5;
		}
		setBackgroundColor(BACKGROUND_COLOR);
	}

	public MuPDFReaderView(Context context) {
		super(context);
		mGestureDetector = new GestureDetector(this);
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		mScroller = new Scroller(context);
		mStepper = new Stepper(this, this);
		mAlertBuilder = new AlertDialog.Builder(this.getContext());
		mContext = context;
		setup();
	}

	public MuPDFReaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (isInEditMode()) {
			//防止IDE显示时出问题
			mGestureDetector = null;
			mScaleGestureDetector = null;
			mScroller = null;
			mStepper = null;
		} else {
			mGestureDetector = new GestureDetector(this);
			mScaleGestureDetector = new ScaleGestureDetector(context, this);
			mScroller = new Scroller(context);
			mStepper = new Stepper(this, this);
		}
		mAlertBuilder = new AlertDialog.Builder(this.getContext());
		mContext = context;
		setup();
	}

	public MuPDFReaderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mGestureDetector = new GestureDetector(this);
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		mScroller = new Scroller(context);
		mStepper = new Stepper(this, this);
		mAlertBuilder = new AlertDialog.Builder(this.getContext());
		mContext = context;
		setup();
	}

	public boolean onSingleTapUp(MotionEvent e) {
		LinkInfo link = null;
		if (D) {
			Log.e(TAG, "====================>onSingleTapUp 001");
		}
		if (mMode == Mode.Viewing && !tapDisabled) {
			MuPDFView pageView = (MuPDFView) getDisplayedView();
			Hit item = pageView.passClickEvent(e.getX(), e.getY());
			onHit(item);
			if (D) {
				Log.e(TAG, "====================>onSingleTapUp 002");
			}
			if (item == Hit.Nothing) {
				if (D) {
					Log.e(TAG, "====================>onSingleTapUp 003");
				}
				if (mLinksEnabled && pageView != null && (link = pageView.hitLink(e.getX(), e.getY())) != null) {
					if (link.type == LinkInfo.TYPE_INTERNAL) {
						setDisplayedViewIndex(link.pageNumber);								
					} else if (link.type == LinkInfo.TYPE_EXTERNAL) {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link.url));
						mContext.startActivity(intent);								
					} else if (link.type == LinkInfo.TYPE_REMOTE) {
						//do nothing
					}
				} else if (e.getX() < tapPageMargin) {
					this.smartMoveBackwards();
				} else if (e.getX() > super.getWidth() - tapPageMargin) {
					this.smartMoveForwards();
				} else if (e.getY() < tapPageMargin) {
					this.smartMoveBackwards();
				} else if (e.getY() > super.getHeight() - tapPageMargin) {
					this.smartMoveForwards();
				} else {
					onTapMainDocArea();
				}
			}
		}
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		mScroller.forceFinished(true);
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		MuPDFView pageView = (MuPDFView)getDisplayedView();
		switch (mMode) {
		case Viewing:
			if (!tapDisabled) {
				onDocMotion();
			}
			if (!mScaling) {
				mXScroll -= distanceX;
				mYScroll -= distanceY;
				requestLayout();
			}
			return true;
			
		case Selecting:
			if (pageView != null) {
				pageView.selectText(e1.getX(), e1.getY(), 
					e2.getX(), e2.getY());
			}
			return true;
		
		default:
			return true;
		}
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		switch (mMode) {
		case Viewing:
			if (mScaling) {
				return true;
			}
			View v = mChildViews.get(mCurrent);
			if (v != null) {
				Rect bounds = getScrollBounds(v);
				switch (directionOfTravel(velocityX, velocityY)) {
				case MOVING_LEFT:
					if (HORIZONTAL_SCROLLING && bounds.left >= 0) {
						View vl = mChildViews.get(mCurrent + 1);
						if (vl != null) {
							slideViewOntoScreen(vl);
							return true;
						}
					}
					break;
					
				case MOVING_UP:
					if (!HORIZONTAL_SCROLLING && bounds.top >= 0) {
						View vl = mChildViews.get(mCurrent + 1);
						if (vl != null) {
							slideViewOntoScreen(vl);
							return true;
						}
					}
					break;
					
				case MOVING_RIGHT:
					if (HORIZONTAL_SCROLLING && bounds.right <= 0) {
						View vr = mChildViews.get(mCurrent - 1);
						if (vr != null) {
							slideViewOntoScreen(vr);
							return true;
						}
					}
					break;
					
				case MOVING_DOWN:
					if (!HORIZONTAL_SCROLLING && bounds.bottom <= 0) {
						View vr = mChildViews.get(mCurrent - 1);
						if (vr != null) {
							slideViewOntoScreen(vr);
							return true;
						}
					}
					break;
				}
				mScrollerLastX = mScrollerLastY = 0;
				Rect expandedBounds = new Rect(bounds);
				expandedBounds.inset(-FLING_MARGIN, -FLING_MARGIN);
				if (withinBoundsInDirectionOfTravel(bounds, velocityX, velocityY)
						&& expandedBounds.contains(0, 0)) {
					mScroller.fling(0, 0, (int) velocityX, (int) velocityY,
							bounds.left, bounds.right, bounds.top, bounds.bottom);
					mStepper.prod();
				}
			}
			return true;
			
		default:
			return true;
		}
	}

	public boolean onScaleBegin(ScaleGestureDetector d) {
		tapDisabled = true;
		mScaling = true;
		mXScroll = mYScroll = 0;
		mLastScaleFocusX = mLastScaleFocusY = -1;
		return true;
	}

	public boolean onTouchEvent(MotionEvent event) {
		//FIXME:批注模式
		if (mMode == Mode.Drawing) {
			float x = event.getX();
			float y = event.getY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				touch_start(x, y);
				break;
				
			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				break;
				
			case MotionEvent.ACTION_UP:
				touch_up();
				break;
			}
		}
		if ((event.getAction() & event.getActionMasked()) == MotionEvent.ACTION_DOWN) {
			tapDisabled = false;
		}
		mScaleGestureDetector.onTouchEvent(event);
		mGestureDetector.onTouchEvent(event);
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
			mUserInteracting = true;
		}
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
			mUserInteracting = false;
			View v = mChildViews.get(mCurrent);
			if (v != null) {
				if (mScroller.isFinished()) {
					slideViewOntoScreen(v);
				}
				if (mScroller.isFinished()) {
					postSettle(v);
				}
			}
		}
		requestLayout();
		return true;
	}

	private void touch_start(float x, float y) {
		MuPDFView pageView = (MuPDFView)getDisplayedView();
		if (pageView != null) {
			pageView.startDraw(x, y);
		}
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			MuPDFView pageView = (MuPDFView)getDisplayedView();
			if (pageView != null) {
				pageView.continueDraw(x, y);
			}
			mX = x;
			mY = y;
		}
	}

	private void touch_up() {
		// NOOP
	}

	protected void onChildSetup(int i, View v) {
		if (SearchTaskResult.get() != null && SearchTaskResult.get().pageNumber == i) {
			((MuPDFView) v).setSearchBoxes(SearchTaskResult.get().searchBoxes);
		} else {
			((MuPDFView) v).setSearchBoxes(null);
		}
		((MuPDFView) v).setLinkHighlighting(mLinksEnabled);
	}

	protected void onMoveToChild(int i) {
		if (SearchTaskResult.get() != null && 
			SearchTaskResult.get().pageNumber != i) {
			SearchTaskResult.set(null);
			resetupChildren();
		}
	}

	protected void onMoveOffChild(int i) {
		View v = getView(i);
		if (v != null) {
			((MuPDFView)v).deselectAnnotation();
		}
	}

	protected void onSettle(View v) {
		((MuPDFView) v).updateHq(false);
	}

	protected void onUnsettle(View v) {
		((MuPDFView) v).removeHq();
	}

	protected void onNotInUse(View v) {
		((MuPDFView) v).releaseResources();
	}

	protected void onScaleChild(View v, Float scale) {
		((MuPDFView) v).setScale(scale);
	}

	public int getDisplayedViewIndex() {
		return mCurrent;
	}

	public void setDisplayedViewIndex(int i) {
		if (0 <= i && i < mAdapter.getCount()) {
			onMoveOffChild(mCurrent);
			mCurrent = i;
			onMoveToChild(i);
			mResetLayout = true;
			requestLayout();
		}
	}

	public void moveToNext() {
		View v = mChildViews.get(mCurrent + 1);
		if (v != null) {
			slideViewOntoScreen(v);
		}
	}

	public void moveToPrevious() {
		View v = mChildViews.get(mCurrent - 1);
		if (v != null) {
			slideViewOntoScreen(v);
		}
	}

	private int smartAdvanceAmount(int screenHeight, int max) {
		int advance = (int) (screenHeight * 0.9 + 0.5);
		int leftOver = max % advance;
		int steps = max / advance;
		if (leftOver == 0) {
			// do nothing
		} else if ((float) leftOver / steps <= screenHeight * 0.05) {
			advance += (int) ((float) leftOver / steps + 0.5);
		} else {
			int overshoot = advance - leftOver;
			if ((float) overshoot / steps <= screenHeight * 0.1) {
				advance -= (int) ((float) overshoot / steps + 0.5);
			}
		}
		if (advance > max) {
			advance = max;
		}
		return advance;
	}

	public void smartMoveForwards() {
		View v = mChildViews.get(mCurrent);
		if (v == null) {
			return;
		}
		int screenWidth = getWidth();
		int screenHeight = getHeight();
		int remainingX = mScroller.getFinalX() - mScroller.getCurrX();
		int remainingY = mScroller.getFinalY() - mScroller.getCurrY();
		int top = -(v.getTop() + mYScroll + remainingY);
		int right = screenWidth - (v.getLeft() + mXScroll + remainingX);
		int bottom = screenHeight + top;
		int docWidth = v.getMeasuredWidth();
		int docHeight = v.getMeasuredHeight();
		int xOffset, yOffset;
		if (bottom >= docHeight) {
			if (right + screenWidth > docWidth) {
				View nv = mChildViews.get(mCurrent + 1);
				if (nv == null) {
					return;
				}
				int nextTop = -(nv.getTop() + mYScroll + remainingY);
				int nextLeft = -(nv.getLeft() + mXScroll + remainingX);
				int nextDocWidth = nv.getMeasuredWidth();
				int nextDocHeight = nv.getMeasuredHeight();
				yOffset = (nextDocHeight < screenHeight ? ((nextDocHeight - screenHeight) >> 1)
						: 0);
				if (nextDocWidth < screenWidth) {
					xOffset = (nextDocWidth - screenWidth) >> 1;
				} else {
					xOffset = right % screenWidth;
					if (xOffset + screenWidth > nextDocWidth) {
						xOffset = nextDocWidth - screenWidth;
					}
				}
				xOffset -= nextLeft;
				yOffset -= nextTop;
			} else {
				xOffset = screenWidth;
				yOffset = screenHeight - bottom;
			}
		} else {
			xOffset = 0;
			yOffset = smartAdvanceAmount(screenHeight, docHeight - bottom);
		}
		mScrollerLastX = mScrollerLastY = 0;
		mScroller.startScroll(0, 0, remainingX - xOffset, remainingY - yOffset,
				400);
		mStepper.prod();
	}

	public void smartMoveBackwards() {
		View v = mChildViews.get(mCurrent);
		if (v == null) {
			return;
		}
		int screenWidth = getWidth();
		int screenHeight = getHeight();
		int remainingX = mScroller.getFinalX() - mScroller.getCurrX();
		int remainingY = mScroller.getFinalY() - mScroller.getCurrY();
		int left = -(v.getLeft() + mXScroll + remainingX);
		int top = -(v.getTop() + mYScroll + remainingY);
		int docHeight = v.getMeasuredHeight();
		int xOffset, yOffset;
		if (top <= 0) {
			if (left < screenWidth) {
				View pv = mChildViews.get(mCurrent - 1);
				if (pv == null) {
					return;
				}
				int prevDocWidth = pv.getMeasuredWidth();
				int prevDocHeight = pv.getMeasuredHeight();
				yOffset = (prevDocHeight < screenHeight ? ((prevDocHeight - screenHeight) >> 1)
						: 0);
				int prevLeft = -(pv.getLeft() + mXScroll);
				int prevTop = -(pv.getTop() + mYScroll);
				if (prevDocWidth < screenWidth) {
					xOffset = (prevDocWidth - screenWidth) >> 1;
				} else {
					xOffset = (left > 0 ? left % screenWidth : 0);
					if (xOffset + screenWidth > prevDocWidth)
						xOffset = prevDocWidth - screenWidth;
					while (xOffset + screenWidth * 2 < prevDocWidth)
						xOffset += screenWidth;
				}
				xOffset -= prevLeft;
				yOffset -= prevTop - prevDocHeight + screenHeight;
			} else {
				xOffset = -screenWidth;
				yOffset = docHeight - screenHeight + top;
			}
		} else {
			xOffset = 0;
			yOffset = -smartAdvanceAmount(screenHeight, top);
		}
		mScrollerLastX = mScrollerLastY = 0;
		mScroller.startScroll(0, 0, remainingX - xOffset, remainingY - yOffset,
				400);
		mStepper.prod();
	}

	public void resetupChildren() {
		for (int i = 0; i < mChildViews.size(); i++) {
			onChildSetup(mChildViews.keyAt(i), mChildViews.valueAt(i));
		}
	}

	public void applyToChildren(ViewMapper mapper) {
		for (int i = 0; i < mChildViews.size(); i++) {
			mapper.applyToView(mChildViews.valueAt(i));
		}
	}

	public void refresh(boolean reflow) {
		mReflow = reflow;
		mReflowChanged = true;
		mResetLayout = true;
		mScale = 1.0f;
		mXScroll = mYScroll = 0;
		requestLayout();
	}

	public View getView(int i) {
		return mChildViews.get(i);
	}

	public View getDisplayedView() {
		return mChildViews.get(mCurrent);
	}

	public void run() {
		if (!mScroller.isFinished()) {
			mScroller.computeScrollOffset();
			int x = mScroller.getCurrX();
			int y = mScroller.getCurrY();
			mXScroll += x - mScrollerLastX;
			mYScroll += y - mScrollerLastY;
			mScrollerLastX = x;
			mScrollerLastY = y;
			requestLayout();
			mStepper.prod();
		} else if (!mUserInteracting) {
			View v = mChildViews.get(mCurrent);
			if (v != null) {
				postSettle(v);
			}
		}
	}

	public void onLongPress(MotionEvent e) {
		
	}

	public void onShowPress(MotionEvent e) {
	}

	public boolean onScale(ScaleGestureDetector detector) {
		float previousScale = mScale;
		float scale_factor = mReflow ? REFLOW_SCALE_FACTOR : 1.0f;
		float min_scale = MIN_SCALE * scale_factor;
		float max_scale = MAX_SCALE * scale_factor;
		mScale = Math.min(
				Math.max(mScale * detector.getScaleFactor(), min_scale),
				max_scale);
		if (D) {
			Log.e(TAG, "=====================mScale" + mScale);
		}
		if (mReflow) {
			View v = mChildViews.get(mCurrent);
			if (v != null) {
				onScaleChild(v, mScale);
			}
		} else {
			float factor = mScale / previousScale;
			View v = mChildViews.get(mCurrent);
			if (v != null) {
				float currentFocusX = detector.getFocusX();
				float currentFocusY = detector.getFocusY();
				int viewFocusX = (int) currentFocusX - (v.getLeft() + mXScroll);
				int viewFocusY = (int) currentFocusY - (v.getTop() + mYScroll);
				mXScroll += viewFocusX - viewFocusX * factor;
				mYScroll += viewFocusY - viewFocusY * factor;
				if (mLastScaleFocusX >= 0) {
					mXScroll += currentFocusX - mLastScaleFocusX;
				}
				if (mLastScaleFocusY >= 0) {
					mYScroll += currentFocusY - mLastScaleFocusY;
				}
				mLastScaleFocusX = currentFocusX;
				mLastScaleFocusY = currentFocusY;
				requestLayout();
			}
		}
		return true;
	}

	public void onScaleEnd(ScaleGestureDetector detector) {
		if (mReflow) {
			applyToChildren(new ViewMapper() {
				@Override
				public void applyToView(View view) {
					onScaleChild(view, mScale);
				}
			});
		}
		mScaling = false;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int n = getChildCount();
		for (int i = 0; i < n; i++) {
			measureView(getChildAt(i));
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		try {
			onLayout2(changed, left, top, right, bottom);
		} catch (java.lang.OutOfMemoryError e) {
			e.printStackTrace();
			if (D) {
				Log.e(TAG, "Out of memory during layout");
			}
			if (!memAlert) {
				memAlert = true;
				AlertDialog alertDialog = mAlertBuilder.create(); //MuPDFActivity.getAlertBuilder().create();
				alertDialog.setMessage("Out of memory during layout");
				alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						memAlert = false;
					}
				});
				alertDialog.show();
			}
		}
	}

	private void onLayout2(boolean changed, int left, int top, int right,
			int bottom) {
		if (isInEditMode()) {
			return;
		}
		View cv = mChildViews.get(mCurrent);
		Point cvOffset;
		if (!mResetLayout) {
			if (cv != null) {
				boolean move;
				cvOffset = subScreenSizeOffset(cv);
				if (HORIZONTAL_SCROLLING) {
					move = cv.getLeft() + cv.getMeasuredWidth() + cvOffset.x
							+ GAP / 2 + mXScroll < getWidth() / 2;
				} else {
					move = cv.getTop() + cv.getMeasuredHeight() + cvOffset.y
							+ GAP / 2 + mYScroll < getHeight() / 2;
				}
				if (move && mCurrent + 1 < mAdapter.getCount()) {
					postUnsettle(cv);
					mStepper.prod();
					onMoveOffChild(mCurrent);
					mCurrent++;
					onMoveToChild(mCurrent);
				}
				if (HORIZONTAL_SCROLLING) {
					move = cv.getLeft() - cvOffset.x - GAP / 2 + mXScroll >= getWidth() / 2;
				} else {
					move = cv.getTop() - cvOffset.y - GAP / 2 + mYScroll >= getHeight() / 2;
				}
				if (move && mCurrent > 0) {
					postUnsettle(cv);
					mStepper.prod();
					onMoveOffChild(mCurrent);
					mCurrent--;
					onMoveToChild(mCurrent);
				}
			}
			int numChildren = mChildViews.size();
			int childIndices[] = new int[numChildren];
			for (int i = 0; i < numChildren; i++) {
				childIndices[i] = mChildViews.keyAt(i);
			}
			for (int i = 0; i < numChildren; i++) {
				int ai = childIndices[i];
				if (ai < mCurrent - 1 || ai > mCurrent + 1) {
					View v = mChildViews.get(ai);
					onNotInUse(v);
					mViewCache.add(v);
					removeViewInLayout(v);
					mChildViews.remove(ai);
				}
			}
		} else {
			mResetLayout = false;
			mXScroll = mYScroll = 0;
			int numChildren = mChildViews.size();
			for (int i = 0; i < numChildren; i++) {
				View v = mChildViews.valueAt(i);
				onNotInUse(v);
				mViewCache.add(v);
				removeViewInLayout(v);
			}
			mChildViews.clear();
			if (mReflowChanged) {
				mReflowChanged = false;
				mViewCache.clear();
			}
			mStepper.prod();
		}
		int cvLeft, cvRight, cvTop, cvBottom;
		boolean notPresent = (mChildViews.get(mCurrent) == null);
		cv = getOrCreateChild(mCurrent);
		cvOffset = subScreenSizeOffset(cv);
		if (notPresent) {
			cvLeft = cvOffset.x;
			cvTop = cvOffset.y;
		} else {
			cvLeft = cv.getLeft() + mXScroll;
			cvTop = cv.getTop() + mYScroll;
		}
		mXScroll = mYScroll = 0;
		cvRight = cvLeft + cv.getMeasuredWidth();
		cvBottom = cvTop + cv.getMeasuredHeight();
		if (!mUserInteracting && mScroller.isFinished()) {
			Point corr = getCorrection(
				getScrollBounds(cvLeft, cvTop, cvRight, cvBottom));
			cvRight += corr.x;
			cvLeft += corr.x;
			cvTop += corr.y;
			cvBottom += corr.y;
		} else if (HORIZONTAL_SCROLLING && 
				cv.getMeasuredHeight() <= getHeight()) {
			Point corr = getCorrection(
				getScrollBounds(cvLeft, cvTop, cvRight, cvBottom));
			cvTop += corr.y;
			cvBottom += corr.y;
		} else if (!HORIZONTAL_SCROLLING && 
				cv.getMeasuredWidth() <= getWidth()) {
			Point corr = getCorrection(
				getScrollBounds(cvLeft, cvTop, cvRight, cvBottom));
			cvRight += corr.x;
			cvLeft += corr.x;
		}
		if (D) {
			Log.e(TAG, 
				"onlayout2 001" +
				",left:" + cvLeft + 
				",top:" + cvTop + 
				",right:" + cvRight + 
				",bottom:" + cvBottom 
			);
		}
		if (MuPDFActivity.MY_MOD) {
			if (false) {
				//FIXME:暂时还不能限制移动范围，因为有bug
				if (cvLeft > 0) {
					cvLeft = 0;
				}
				if (cvTop > 0) {
					cvTop = 0;
				}
				float scale = (float) getWidth() / (float) cv.getMeasuredWidth() * mScale;
				if (cvRight < this.getWidth()) {
					cvRight = this.getWidth();
				}
				if (cvBottom < (int)(cv.getMeasuredHeight() * scale)) {
					cvBottom = (int)(cv.getMeasuredHeight() * scale);
				}
			}
		}
		cv.layout(cvLeft, cvTop, cvRight, cvBottom);
		if (mCurrent > 0) {
			View lv = getOrCreateChild(mCurrent - 1);
			Point leftOffset = subScreenSizeOffset(lv);
			if (HORIZONTAL_SCROLLING) {
				int gap = leftOffset.x + GAP + cvOffset.x;
				int leftLayout = (cvLeft - lv.getMeasuredWidth() - gap);
				int topLayout = ((cvBottom + cvTop - lv.getMeasuredHeight()) / 2);
				int rightLayout = (cvLeft - gap);
				int bottomLayout = ((cvBottom + cvTop + lv.getMeasuredHeight()) / 2);
				lv.layout(leftLayout, 
						topLayout, 
						rightLayout,
						bottomLayout);
				if (D) {
					Log.e(TAG, 
						"onlayout2 002" +
						",left:" + leftLayout + 
						",top:" + topLayout + 
						",right:" + rightLayout + 
						",bottom:" + bottomLayout 
					);
				}
			} else {
				int gap = leftOffset.y + GAP + cvOffset.y;
				lv.layout((cvLeft + cvRight - lv.getMeasuredWidth()) / 2, 
						cvTop - lv.getMeasuredHeight() - gap,
						(cvLeft + cvRight + lv.getMeasuredWidth()) / 2, 
						cvTop - gap);
			}
		}
		if (mCurrent + 1 < mAdapter.getCount()) {
			View rv = getOrCreateChild(mCurrent + 1);
			Point rightOffset = subScreenSizeOffset(rv);
			if (HORIZONTAL_SCROLLING) {
				int gap = cvOffset.x + GAP + rightOffset.x;
				int leftLayout = (cvRight + gap);
				int topLayout = ((cvBottom + cvTop - rv.getMeasuredHeight()) / 2);
				int rightLayout = (cvRight + rv.getMeasuredWidth() + gap);
				int bottomLayout = ((cvBottom + cvTop + rv.getMeasuredHeight()) / 2);
				rv.layout(leftLayout,
						topLayout,
						rightLayout, 
						bottomLayout);
				if (D) {
					Log.e(TAG, 
						"onlayout2 003" +
						",left:" + leftLayout + 
						",top:" + topLayout + 
						",right:" + rightLayout + 
						",bottom:" + bottomLayout  
					);
				}
			} else {
				int gap = cvOffset.y + GAP + rightOffset.y;
				rv.layout((cvLeft + cvRight - rv.getMeasuredWidth()) / 2,
						cvBottom + gap,
						(cvLeft + cvRight + rv.getMeasuredWidth()) / 2,
						cvBottom + gap + rv.getMeasuredHeight());
			}
		}
		invalidate();
	}

	@Override
	public Adapter getAdapter() {
		return mAdapter;
	}

	@Override
	public View getSelectedView() {
		return null;
	}

	@Override
	public void setAdapter(Adapter adapter) {
		if (null != mAdapter && adapter != mAdapter) {
			if (adapter instanceof MuPDFPageAdapter) {
				((MuPDFPageAdapter) adapter).releaseBitmaps();
			}
		}
		mAdapter = adapter;
		requestLayout();
	}

	//FIXME:这个异常有什么用？？？
	@Override
	public void setSelection(int arg0) {
		throw new UnsupportedOperationException("当前不支持此格式");
	}

	private View getCached() {
		if (mViewCache.size() == 0) {
			return null;
		} else {
			return mViewCache.removeFirst();
		}
	}

	private View getOrCreateChild(int i) {
		View v = mChildViews.get(i);
		if (v == null) {
			v = mAdapter.getView(i, getCached(), this);
			addAndMeasureChild(i, v);
			onChildSetup(i, v);
			onScaleChild(v, mScale);
		}
		return v;
	}

	private void addAndMeasureChild(int i, View v) {
		LayoutParams params = v.getLayoutParams();
		if (params == null) {
			params = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
		}
		addViewInLayout(v, 0, params, true);
		mChildViews.append(i, v); // Record the view against it's adapter index
		measureView(v);
	}

	private void measureView(View v) {
		v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		if (!mReflow) {
			//当mScale==1时，即最小缩放比时对应的视图实际缩放比
			float scale = Math.min(
					(float) getWidth() / (float) v.getMeasuredWidth(),
					(float) getHeight() / (float) v.getMeasuredHeight());
			if (MuPDFActivity.MY_MOD) {
				if (false) {
					float ratioOutside = (float) getWidth() / (float) getHeight();
					float ratioInside = (float) v.getMeasuredWidth() / (float) v.getMeasuredHeight();
					if (ratioOutside > ratioInside) {
						//适应宽度
						scale = (float) getWidth() / (float) v.getMeasuredWidth();
					} else {
						scale = (float) getHeight() / (float) v.getMeasuredHeight();
					}
				} else {
					scale = (float) getWidth() / (float) v.getMeasuredWidth();
				}
			}
			v.measure(
				View.MeasureSpec.EXACTLY | 
				(int) (v.getMeasuredWidth() * scale * mScale),
				View.MeasureSpec.EXACTLY | 
				(int) (v.getMeasuredHeight() * scale * mScale));
		} else {
			v.measure(
				View.MeasureSpec.EXACTLY | (int) (v.getMeasuredWidth()),
				View.MeasureSpec.EXACTLY | (int) (v.getMeasuredHeight()));
		}
	}

	private Rect getScrollBounds(int left, int top, int right, int bottom) {
		int xmin = getWidth() - right;
		int xmax = -left;
		int ymin = getHeight() - bottom;
		int ymax = -top;
		if (xmin > xmax) {
			xmin = xmax = (xmin + xmax) / 2;
		}
		if (ymin > ymax) {
			ymin = ymax = (ymin + ymax) / 2;
		}
		return new Rect(xmin, ymin, xmax, ymax);
	}

	private Rect getScrollBounds(View v) {
		return getScrollBounds(
				v.getLeft() + mXScroll, 
				v.getTop() + mYScroll,
				v.getLeft() + v.getMeasuredWidth() + mXScroll,
				v.getTop() + v.getMeasuredHeight() + mYScroll);
	}

	private Point getCorrection(Rect bounds) {
		return new Point(Math.min(Math.max(0, bounds.left), bounds.right),
				Math.min(Math.max(0, bounds.top), bounds.bottom));
	}

	private void postSettle(final View v) {
		post(new Runnable() {
			public void run() {
				onSettle(v);
			}
		});
	}

	private void postUnsettle(final View v) {
		post(new Runnable() {
			public void run() {
				onUnsettle(v);
			}
		});
	}

	private void slideViewOntoScreen(View v) {
		Point corr = getCorrection(getScrollBounds(v));
		if (corr.x != 0 || corr.y != 0) {
			mScrollerLastX = mScrollerLastY = 0;
			mScroller.startScroll(0, 0, corr.x, corr.y, 400);
			mStepper.prod();
		}
	}

	private Point subScreenSizeOffset(View v) {
		return new Point(Math.max((getWidth() - v.getMeasuredWidth()) / 2, 0),
			Math.max((getHeight() - v.getMeasuredHeight()) / 2, 0));
	}

	private static int directionOfTravel(float vx, float vy) {
		if (Math.abs(vx) > 2 * Math.abs(vy)) {
			return (vx > 0) ? MOVING_RIGHT : MOVING_LEFT;
		} else if (Math.abs(vy) > 2 * Math.abs(vx)) {
			return (vy > 0) ? MOVING_DOWN : MOVING_UP;
		} else {
			return MOVING_DIAGONALLY;
		}
	}

	private static boolean withinBoundsInDirectionOfTravel(Rect bounds,
			float vx, float vy) {
		switch (directionOfTravel(vx, vy)) {
		case MOVING_DIAGONALLY:
			return bounds.contains(0, 0);
			
		case MOVING_LEFT:
			return bounds.left <= 0;
		
		case MOVING_RIGHT:
			return bounds.right >= 0;
		
		case MOVING_UP:
			return bounds.top <= 0;
		
		case MOVING_DOWN:
			return bounds.bottom >= 0;
		
		default:
			throw new NoSuchElementException();
		}
	}
	
	private final static class Stepper {
		protected final View mPoster;
		protected final Runnable mTask;
		protected boolean mPending;

		public Stepper(View v, Runnable r) {
			mPoster = v;
			mTask = r;
			mPending = false;
		}

		public void prod() {
			if (!mPending) {
				mPending = true;
				mPoster.post(new Runnable() {
					@Override
					public void run() {
						mPending = false;
						mTask.run();
					}
				});
			}
		}
	}
}

package com.suse.android.sidemenu.utils;


import java.util.ArrayList;
import java.util.Comparator;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.KeyEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class ViewBehind extends ViewGroup {
        private static final String TAG = "CustomViewPager";
        private static final boolean DEBUG = false;

        private static final boolean USE_CACHE = false;

        private static final int MAX_SETTLE_DURATION = 600; // ms
        private static final int MIN_DISTANCE_FOR_FLING = 25; // dips

        private static final int[] LAYOUT_ATTRS = new int[] {
                android.R.attr.layout_gravity
        };

        static class ItemInfo {
                Object object;
                int position;
                boolean scrolling;
        }

        private static final Comparator<ItemInfo> COMPARATOR = new Comparator<ItemInfo>(){

                public int compare(ItemInfo lhs, ItemInfo rhs) {
                        return lhs.position - rhs.position;
                }};

                private static final Interpolator sInterpolator = new Interpolator() {
                        public float getInterpolation(float t) {
                                t -= 1.0f;
                                return t * t * t * t * t + 1.0f;
                        }
                };

                private ItemInfo mContent;

                private PagerAdapter mAdapter;
                private int mCurItem;   // Index of currently displayed page.
                private int mRestoredCurItem = -1;
                private Parcelable mRestoredAdapterState = null;
                private ClassLoader mRestoredClassLoader = null;
                private Scroller mScroller;
                private PagerObserver mObserver;

                private int mPageMargin;
                private Drawable mMarginDrawable;
                private int mTopPageBounds;
                private int mBottomPageBounds;

                private int mChildWidthMeasureSpec;
                private int mChildHeightMeasureSpec;
                private boolean mInLayout;

                private boolean mScrollingCacheEnabled;

                private boolean mPopulatePending;
                private boolean mScrolling;
                private float mScrollScale = 0.5f;
                private boolean mEnabled = true;

               
                private boolean mFirstLayout = true;
                private boolean mCalledSuper;
                private int mDecorChildCount;

                private OnPageChangeListener mOnPageChangeListener;
                private OnPageChangeListener mInternalPageChangeListener;
                private OnAdapterChangeListener mAdapterChangeListener;

               
                public static final int SCROLL_STATE_IDLE = 0;

                
                public static final int SCROLL_STATE_DRAGGING = 1;

                
                public static final int SCROLL_STATE_SETTLING = 2;

                private int mScrollState = SCROLL_STATE_IDLE;

                
                public interface OnPageChangeListener {

                       
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

                       
                        public void onPageSelected(int position);

                       
                        public void onPageScrollStateChanged(int state);
                }

               
                public static class SimpleOnPageChangeListener implements OnPageChangeListener {

                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                // free Space
                        }


                        public void onPageSelected(int position) {
                        		// free Space
                        }


                        public void onPageScrollStateChanged(int state) {
                        		// free Space
                        }
                }

               
                interface OnAdapterChangeListener {
                        public void onAdapterChanged(PagerAdapter oldAdapter, PagerAdapter newAdapter);
                }

             
                interface Decor {}

                public ViewBehind(Context context) {
                        super(context);
                        initViewBehind();
                }

                public ViewBehind(Context context, AttributeSet attrs) {
                        super(context, attrs);
                        initViewBehind();
                }

                void initViewBehind() {
                        setWillNotDraw(false);
                        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                        setFocusable(true);
                        final Context context = getContext();
                        mScroller = new Scroller(context, sInterpolator);
                        setAdapter(new PagerAdapter());
                }

                private void setScrollState(int newState) {
                        if (mScrollState == newState) {
                                return;
                        }

                        mScrollState = newState;
                        if (mOnPageChangeListener != null) {
                                mOnPageChangeListener.onPageScrollStateChanged(newState);
                        }
                }

                private void setAdapter(PagerAdapter adapter) {
                        if (mAdapter != null) {
                                mAdapter.unregisterDataSetObserver(mObserver);
                                mAdapter.startUpdate(this);
                                mAdapter.destroyItem(this, mContent.position, mContent.object);
                                mAdapter.finishUpdate(this);
                                mContent = null;
                                removeNonDecorViews();
                                mCurItem = 0;
                                scrollTo(0, 0);
                        }

                        final PagerAdapter oldAdapter = mAdapter;
                        mAdapter = adapter;

                        if (mAdapter != null) {
                                if (mObserver == null) {
                                        mObserver = new PagerObserver();
                                }
                                mAdapter.registerDataSetObserver(mObserver);
                                mPopulatePending = false;
                                if (mRestoredCurItem >= 0) {
                                        mAdapter.restoreState(mRestoredAdapterState, mRestoredClassLoader);
                                        setCurrentItemInternal(mRestoredCurItem, false, true);
                                        mRestoredCurItem = -1;
                                        mRestoredAdapterState = null;
                                        mRestoredClassLoader = null;
                                } else {
                                        populate();
                                }
                        }

                        if (mAdapterChangeListener != null && oldAdapter != adapter) {
                                mAdapterChangeListener.onAdapterChanged(oldAdapter, adapter);
                        }
                }

                private void removeNonDecorViews() {
                        for (int i = 0; i < getChildCount(); i++) {
                                final View child = getChildAt(i);
                                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                                if (!lp.isDecor) {
                                        removeViewAt(i);
                                        i--;
                                }
                        }
                }


                public PagerAdapter getAdapter() {
                        return mAdapter;
                }

                void setOnAdapterChangeListener(OnAdapterChangeListener listener) {
                        mAdapterChangeListener = listener;
                }

                public void setCurrentItem(int item) {
                        mPopulatePending = false;
                        setCurrentItemInternal(item, !mFirstLayout, false);
                }

                public void setCurrentItem(int item, boolean smoothScroll) {
                        mPopulatePending = false;
                        setCurrentItemInternal(item, smoothScroll, false);
                }

                public int getCurrentItem() {
                        return mCurItem;
                }

                void setCurrentItemInternal(int item, boolean smoothScroll, boolean always) {
                        setCurrentItemInternal(item, smoothScroll, always, 0);
                }

                void setCurrentItemInternal(int item, boolean smoothScroll, boolean always, int velocity) {
                        if (mAdapter == null || mAdapter.getCount() <= 0) {
                                setScrollingCacheEnabled(false);
                                return;
                        }
                        if (!always && mCurItem == item && mContent != null) {
                                setScrollingCacheEnabled(false);
                                return;
                        }
                        if (item < 0) {
                                item = 0;
                        } else if (item >= mAdapter.getCount()) {
                                item = mAdapter.getCount() - 1;
                        }
                        if (item > 0 && item < getItems().size()) {

                                mContent.scrolling = true;
                        }
                        final boolean dispatchSelected = mCurItem != item;
                        mCurItem = item;
                        populate();

                        final int destX = getChildLeft(mCurItem);
                        if (smoothScroll) {
                                smoothScrollTo(destX, 0, velocity);
                                if (dispatchSelected && mOnPageChangeListener != null) {
                                        mOnPageChangeListener.onPageSelected(item);
                                }
                                if (dispatchSelected && mInternalPageChangeListener != null) {
                                        mInternalPageChangeListener.onPageSelected(item);
                                }
                        } else {
                                if (dispatchSelected && mOnPageChangeListener != null) {
                                        mOnPageChangeListener.onPageSelected(item);
                                }
                                if (dispatchSelected && mInternalPageChangeListener != null) {
                                        mInternalPageChangeListener.onPageSelected(item);
                                }
                                completeScroll();
                                scrollTo(destX, 0);
                        }
                }

 
                public void setOnPageChangeListener(OnPageChangeListener listener) {
                        mOnPageChangeListener = listener;
                }


                OnPageChangeListener setInternalPageChangeListener(OnPageChangeListener listener) {
                        OnPageChangeListener oldListener = mInternalPageChangeListener;
                        mInternalPageChangeListener = listener;
                        return oldListener;
                }


                public void setPageMargin(int marginPixels) {
                        final int oldMargin = mPageMargin;
                        mPageMargin = marginPixels;

                        final int width = getWidth();
                        recomputeScrollPosition(width, width, marginPixels, oldMargin);

                        requestLayout();
                }

                public int getPageMargin() {
                        return mPageMargin;
                }


                public void setPageMarginDrawable(Drawable d) {
                        mMarginDrawable = d;
                        if (d != null) refreshDrawableState();
                        setWillNotDraw(d == null);
                        invalidate();
                }

                public void setPageMarginDrawable(int resId) {
                        setPageMarginDrawable(getContext().getResources().getDrawable(resId));
                }


                protected boolean verifyDrawable(Drawable who) {
                        return super.verifyDrawable(who) || who == mMarginDrawable;
                }


                protected void drawableStateChanged() {
                        super.drawableStateChanged();
                        final Drawable d = mMarginDrawable;
                        if (d != null && d.isStateful()) {
                                d.setState(getDrawableState());
                        }
                }


                float distanceInfluenceForSnapDuration(float f) {
                        f -= 0.5f;
                        f *= 0.3f * Math.PI / 2.0f;
                        return android.util.FloatMath.sin(f);
                }
                
                public void setScrollScale(float f) {
                        if (f >= 0 && f <= 2) {
                                mScrollScale = f;
                        }
                }

                public int getDestScrollX() {
                        if (isMenuOpen()) {
                                return getBehindWidth();
                        } else {
                                return 0;
                        }
                }

                public int getChildLeft(int i) {
                        return 0;
                }

                public int getChildRight(int i) {
                        return getChildLeft(i) + getChildWidth(i);
                }

                public boolean isMenuOpen() {
                        return getScrollX() == 0;
                }

                public int getCustomWidth() {
                        int i = isMenuOpen()? 0 : 1;
                        return getChildWidth(i);
                }

                public int getChildWidth(int i) {
                        if (i <= 0) {
                                return getBehindWidth();
                        } else {
                                return getChildAt(i).getMeasuredWidth();
                        }
                }

                public int getBehindWidth() {
                        ViewGroup.LayoutParams params = getLayoutParams();
                        return params.width;

                }


                void smoothScrollTo(int x, int y) {
                        smoothScrollTo(x, y, 0);
                }

                void smoothScrollTo(int x, int y, int velocity) {
                        if (getChildCount() == 0) {
                                // Nothing to do.
                                setScrollingCacheEnabled(false);
                                return;
                        }
                        int sx = getScrollX();
                        int sy = getScrollY();
                        int dx = x - sx;
                        int dy = y - sy;
                        if (dx == 0 && dy == 0) {
                                completeScroll();
                                setScrollState(SCROLL_STATE_IDLE);
                                return;
                        }

                        setScrollingCacheEnabled(true);
                        mScrolling = true;
                        setScrollState(SCROLL_STATE_SETTLING);

                        final int width = getCustomWidth();
                        final int halfWidth = width / 2;
                        final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / width);
                        final float distance = halfWidth + halfWidth *
                                        distanceInfluenceForSnapDuration(distanceRatio);

                        int duration = 0;
                        velocity = Math.abs(velocity);
                        if (velocity > 0) {
                                duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
                        } else {
                                final float pageDelta = (float) Math.abs(dx) / (width + mPageMargin);
                                duration = (int) ((pageDelta + 1) * 100);
                                // TODO set custom duration!
                                duration = MAX_SETTLE_DURATION;
                        }
                        duration = Math.min(duration, MAX_SETTLE_DURATION);

                        mScroller.startScroll(sx, sy, dx, dy, duration);
                        invalidate();
                }               

                public void scrollTo(float percentage, int y) {
                        scrollTo((int)(percentage*getMeasuredWidth()*mScrollScale), y);
                }

                private ArrayList<ItemInfo> getItems() {
                        ArrayList<ItemInfo> mItems = new ArrayList<ItemInfo>();
                        if (mContent != null) {
                                mItems.add(mContent);
                        }
                        return mItems;
                }

                public void dataSetChanged() {
                        boolean needPopulate = getItems().size() < mAdapter.getCount();
                        int newCurrItem = -1;

                        boolean isUpdating = false;
                        ArrayList<ItemInfo> items = getItems();
                        for (int i = 0; i < items.size(); i++) {
                                final ItemInfo ii = items.get(i);
                                final int newPos = mAdapter.getItemPosition(ii.object);

                                if (newPos == PagerAdapter.POSITION_UNCHANGED) {
                                        continue;
                                }

                                if (newPos == PagerAdapter.POSITION_NONE) {
                                        items.remove(i);
                                        i--;

                                        if (!isUpdating) {
                                                mAdapter.startUpdate(this);
                                                isUpdating = true;
                                        }

                                        mAdapter.destroyItem(this, ii.position, ii.object);
                                        needPopulate = true;

                                        if (mCurItem == ii.position) {
                                                newCurrItem = Math.max(0, Math.min(mCurItem, mAdapter.getCount() - 1));
                                        }
                                        continue;
                                }

                                if (ii.position != newPos) {
                                        if (ii.position == mCurItem) {
                                                newCurrItem = newPos;
                                        }

                                        ii.position = newPos;
                                        needPopulate = true;
                                }
                        }

                        if (isUpdating) {
                                mAdapter.finishUpdate(this);
                        }

                        if (newCurrItem >= 0) {
                                setCurrentItemInternal(newCurrItem, false, true);
                                needPopulate = true;
                        }
                        if (needPopulate) {
                                populate();
                                requestLayout();
                        }
                }

                void populate() {
                        if (mAdapter == null) {
                                return;
                        }

                        if (mPopulatePending) {
                                if (DEBUG) Log.i(TAG, "populate is pending, skipping for now...");
                                return;
                        }

                        if (getWindowToken() == null) {
                                return;
                        }

                        mAdapter.startUpdate(this);

                        if (DEBUG) {
                                Log.i(TAG, "Current page list:");
                                for (int i=0; i<getItems().size(); i++) {
                                        Log.i(TAG, "#" + i + ": page " + getItems().get(i).position);
                                }
                        }

                        ItemInfo curItem = null;
                        if (mContent != null && mContent.position == mCurItem) {
                                curItem = mContent;
                        }

                        mAdapter.setPrimaryItem(this, mCurItem, curItem != null ? curItem.object : null);

                        mAdapter.finishUpdate(this);

                        if (hasFocus()) {
                                View currentFocused = findFocus();
                                ItemInfo ii = currentFocused != null ? infoForAnyChild(currentFocused) : null;
                                if (ii == null || ii.position != mCurItem) {
                                        for (int i=0; i<getChildCount(); i++) {
                                                View child = getChildAt(i);
                                                ii = infoForChild(child);
                                                if (ii != null && ii.position == mCurItem) {
                                                        if (child.requestFocus(FOCUS_FORWARD)) {
                                                                break;
                                                        }
                                                }
                                        }
                                }
                        }
                }


                public static class SavedState extends BaseSavedState {
                        int position;
                        Parcelable adapterState;
                        ClassLoader loader;

                        public SavedState(Parcelable superState) {
                                super(superState);
                        }


                        public void writeToParcel(Parcel out, int flags) {
                                super.writeToParcel(out, flags);
                                out.writeInt(position);
                                out.writeParcelable(adapterState, flags);
                        }


                        public String toString() {
                                return "FragmentPager.SavedState{"
                                                + Integer.toHexString(System.identityHashCode(this))
                                                + " position=" + position + "}";
                        }

                        public static final Parcelable.Creator<SavedState> CREATOR
                        = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {

                                public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                                        return new SavedState(in, loader);
                                }

                                public SavedState[] newArray(int size) {
                                        return new SavedState[size];
                                }
                        });

                        SavedState(Parcel in, ClassLoader loader) {
                                super(in);
                                if (loader == null) {
                                        loader = getClass().getClassLoader();
                                }
                                position = in.readInt();
                                adapterState = in.readParcelable(loader);
                                this.loader = loader;
                        }
                }


                public Parcelable onSaveInstanceState() {
                        Parcelable superState = super.onSaveInstanceState();
                        SavedState ss = new SavedState(superState);
                        ss.position = mCurItem;
                        if (mAdapter != null) {
                                ss.adapterState = mAdapter.saveState();
                        }
                        return ss;
                }


                public void onRestoreInstanceState(Parcelable state) {
                        if (!(state instanceof SavedState)) {
                                super.onRestoreInstanceState(state);
                                return;
                        }

                        SavedState ss = (SavedState)state;
                        super.onRestoreInstanceState(ss.getSuperState());

                        if (mAdapter != null) {
                                mAdapter.restoreState(ss.adapterState, ss.loader);
                                setCurrentItemInternal(ss.position, false, true);
                        } else {
                                mRestoredCurItem = ss.position;
                                mRestoredAdapterState = ss.adapterState;
                                mRestoredClassLoader = ss.loader;
                        }
                }

                public void setContent(View v) {
                        mAdapter.setBehind(v);
                        setBackgroundDrawable(v.getBackground());

                        ItemInfo ii = new ItemInfo();
                        ii.position = 0;
                        ii.object = mAdapter.instantiateItem(this, 0);
                        mContent = ii;

                        mAdapter.notifyDataSetChanged();
                }

                public void addView(View child, int index, ViewGroup.LayoutParams params) {
                        if (!checkLayoutParams(params)) {
                                params = generateLayoutParams(params);
                        }
                        final LayoutParams lp = (LayoutParams) params;
                        lp.isDecor |= child instanceof Decor;
                        if (mInLayout) {
                                if (lp != null && lp.isDecor) {
                                        throw new IllegalStateException("Cannot add pager decor view during layout");
                                }
                                addViewInLayout(child, index, params);
                                child.measure(mChildWidthMeasureSpec, mChildHeightMeasureSpec);
                        } else {
                                super.addView(child, index, params);
                        }

                        if (USE_CACHE) {
                                if (child.getVisibility() != GONE) {
                                        child.setDrawingCacheEnabled(mScrollingCacheEnabled);
                                } else {
                                        child.setDrawingCacheEnabled(false);
                                }
                        }
                }

                ItemInfo infoForChild(View child) {
                        if (mAdapter.isViewFromObject(child, mContent.object)) {
                                return mContent;
                        }
                        return null;
                }

                ItemInfo infoForAnyChild(View child) {
                        ViewParent parent;
                        while ((parent=child.getParent()) != this) {
                                if (parent == null || !(parent instanceof View)) {
                                        return null;
                                }
                                child = (View)parent;
                        }
                        return infoForChild(child);
                }


                protected void onAttachedToWindow() {
                        super.onAttachedToWindow();
                        mFirstLayout = true;
                }


                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                        int width = getDefaultSize(0, widthMeasureSpec);
                        int height = getDefaultSize(0, heightMeasureSpec);
                        setMeasuredDimension(width, height);

                        int childWidthSize = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
                        int childHeightSize = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

                        int size = getChildCount();
                        for (int i = 0; i < size; ++i) {
                                final View child = getChildAt(i);
                                if (child.getVisibility() != GONE) {
                                        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                                        if (lp != null && lp.isDecor) {
                                                final int hgrav = lp.gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                                                final int vgrav = lp.gravity & Gravity.VERTICAL_GRAVITY_MASK;
                                                Log.d(TAG, "gravity: " + lp.gravity + " hgrav: " + hgrav + " vgrav: " + vgrav);
                                                int widthMode = MeasureSpec.AT_MOST;
                                                int heightMode = MeasureSpec.AT_MOST;
                                                boolean consumeVertical = vgrav == Gravity.TOP || vgrav == Gravity.BOTTOM;
                                                boolean consumeHorizontal = hgrav == Gravity.LEFT || hgrav == Gravity.RIGHT;

                                                if (consumeVertical) {
                                                        widthMode = MeasureSpec.EXACTLY;
                                                } else if (consumeHorizontal) {
                                                        heightMode = MeasureSpec.EXACTLY;
                                                }

                                                final int widthSpec = MeasureSpec.makeMeasureSpec(childWidthSize, widthMode);
                                                final int heightSpec = MeasureSpec.makeMeasureSpec(childHeightSize, heightMode);
                                                child.measure(widthSpec, heightSpec);

                                                if (consumeVertical) {
                                                        childHeightSize -= child.getMeasuredHeight();
                                                } else if (consumeHorizontal) {
                                                        childWidthSize -= child.getMeasuredWidth();
                                                }
                                        }
                                }
                        }

                        mChildWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
                        mChildHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize, MeasureSpec.EXACTLY);

                        mInLayout = true;
                        populate();
                        mInLayout = false;

                        size = getChildCount();
                        for (int i = 0; i < size; ++i) {
                                final View child = getChildAt(i);
                                if (child.getVisibility() != GONE) {
                                        if (DEBUG) Log.v(TAG, "Measuring #" + i + " " + child
                                                        + ": " + mChildWidthMeasureSpec);

                                        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                                        if (lp == null || !lp.isDecor) {
                                                child.measure(mChildWidthMeasureSpec, mChildHeightMeasureSpec);
                                        }
                                }
                        }
                }

                protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                        super.onSizeChanged(w, h, oldw, oldh);
                        
                        if (w != oldw) {
                                recomputeScrollPosition(w, oldw, mPageMargin, mPageMargin);
                        }
                }

                private void recomputeScrollPosition(int width, int oldWidth, int margin, int oldMargin) {
                        final int widthWithMargin = width + margin;
                        if (oldWidth > 0) {
                                final int oldScrollPos = getDestScrollX();
                                final int oldwwm = oldWidth + oldMargin;
                                final int oldScrollItem = oldScrollPos / oldwwm;
                                final float scrollOffset = (float) (oldScrollPos % oldwwm) / oldwwm;
                                final int scrollPos = (int) ((oldScrollItem + scrollOffset) * widthWithMargin);
                                scrollTo(scrollPos, getScrollY());
                                if (!mScroller.isFinished()) {
                                        final int newDuration = mScroller.getDuration() - mScroller.timePassed();
                                        mScroller.startScroll(scrollPos, 0, getChildLeft(mCurItem), 0, newDuration);
                                }
                        } else {
                                int scrollPos = getChildLeft(mCurItem);
                                if (scrollPos != getScrollX()) {
                                        completeScroll();
                                        scrollTo(scrollPos, getScrollY());
                                }
                        }
                }


                protected void onLayout(boolean changed, int l, int t, int r, int b) {
                        mInLayout = true;
                        populate();
                        mInLayout = false;

                        final int count = getChildCount();
                        int height = b - t;
                        int paddingTop = getPaddingTop();
                        int paddingBottom = getPaddingBottom();

                        int decorCount = 0;

                        for (int i = 0; i < count; i++) {
                                final View child = getChildAt(i);
                                if (child.getVisibility() != GONE) {
                                        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                                        ItemInfo ii;
                                        int childLeft = 0;
                                        int childTop = 0;
                                        if (lp.isDecor) {
                                                decorCount++;
                                                childLeft = getChildLeft(i);
                                                int childWidth = child.getMeasuredWidth();
                                                child.layout(childLeft, childTop,
                                                                childLeft + child.getMeasuredWidth(),
                                                                childTop + child.getMeasuredHeight());
                                        } else if ((ii = infoForChild(child)) != null) {
                                                childTop = paddingTop;
                                                if (DEBUG) Log.v(TAG, "Positioning #" + i + " " + child + " f=" + ii.object
                                                                + ":" + childLeft + "," + childTop + " " + child.getMeasuredWidth()
                                                                + "x" + child.getMeasuredHeight());
                                                childLeft = getChildLeft(i);
                                                child.layout(childLeft, childTop,
                                                                childLeft + child.getMeasuredWidth(),
                                                                childTop + child.getMeasuredHeight());
                                        }
                                }
                        }
                        mTopPageBounds = paddingTop;
                        mBottomPageBounds = height - paddingBottom;
                        mDecorChildCount = decorCount;
                        mFirstLayout = false;
                }


                public void computeScroll() {
                        if (DEBUG) Log.i(TAG, "computeScroll: finished=" + mScroller.isFinished());
                        if (!mScroller.isFinished()) {
                                if (mScroller.computeScrollOffset()) {
                                        if (DEBUG) Log.i(TAG, "computeScroll: still scrolling");
                                        int oldX = getScrollX();
                                        int oldY = getScrollY();
                                        int x = mScroller.getCurrX();
                                        int y = mScroller.getCurrY();

                                        if (oldX != x || oldY != y) {
                                                scrollTo(x, y);
                                                pageScrolled(x);
                                        }

                                        invalidate();
                                        return;
                                }
                        }

                        completeScroll();
                }

                private void pageScrolled(int xpos) {
                        // TODO
                        final int widthWithMargin = getChildWidth(mCurItem) + mPageMargin;
                        final int position = xpos / widthWithMargin;
                        final int offsetPixels = xpos % widthWithMargin;
                        final float offset = (float) offsetPixels / widthWithMargin;

                        mCalledSuper = false;
                        onPageScrolled(position, offset, offsetPixels);
                        if (!mCalledSuper) {
                                throw new IllegalStateException(
                                                "onPageScrolled did not call superclass implementation");
                        }
                }

                protected void onPageScrolled(int position, float offset, int offsetPixels) {
                        if (mDecorChildCount > 0) {
                                final int scrollX = getScrollX();
                                int paddingLeft = getPaddingLeft();
                                int paddingRight = getPaddingRight();
                                final int width = getWidth();
                                final int childCount = getChildCount();
                                for (int i = 0; i < childCount; i++) {
                                        final View child = getChildAt(i);
                                        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                                        if (!lp.isDecor) continue;

                                        final int hgrav = lp.gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                                        int childLeft = 0;
                                        switch (hgrav) {
                                        default:
                                                childLeft = paddingLeft;
                                                break;
                                        case Gravity.LEFT:
                                                childLeft = paddingLeft;
                                                paddingLeft += child.getWidth();
                                                break;
                                        case Gravity.CENTER_HORIZONTAL:
                                                childLeft = Math.max((width - child.getMeasuredWidth()) / 2,
                                                                paddingLeft);
                                                break;
                                        case Gravity.RIGHT:
                                                childLeft = width - paddingRight - child.getMeasuredWidth();
                                                paddingRight += child.getMeasuredWidth();
                                                break;
                                        }
                                        childLeft += scrollX;

                                        final int childOffset = childLeft - child.getLeft();
                                        if (childOffset != 0) {
                                                child.offsetLeftAndRight(childOffset);
                                        }
                                }
                        }

                        if (mOnPageChangeListener != null) {
                                mOnPageChangeListener.onPageScrolled(position, offset, offsetPixels);
                        }
                        if (mInternalPageChangeListener != null) {
                                mInternalPageChangeListener.onPageScrolled(position, offset, offsetPixels);
                        }
                        mCalledSuper = true;
                }

                private void completeScroll() {
                        boolean needPopulate = mScrolling;
                        if (needPopulate) {
                                // Done with scroll, no longer want to cache view drawing.
                                setScrollingCacheEnabled(false);
                                mScroller.abortAnimation();
                                int oldX = getScrollX();
                                int oldY = getScrollY();
                                int x = mScroller.getCurrX();
                                int y = mScroller.getCurrY();
                                if (oldX != x || oldY != y) {
                                        scrollTo(x, y);
                                }
                                setScrollState(SCROLL_STATE_IDLE);
                        }
                        mPopulatePending = false;
                        mScrolling = false;
                        if (mContent != null && mContent.scrolling) {
                                needPopulate = true;
                                mContent.scrolling = false;
                        }
                        if (needPopulate) {
                                populate();
                        }
                }

                public boolean onInterceptTouchEvent(MotionEvent ev) {
                        return false;
                }

                public boolean onTouchEvent(MotionEvent ev) {
                        return false;
                }

                protected void onDraw(Canvas canvas) {
                        super.onDraw(canvas);
                        
                        if (mPageMargin > 0 && mMarginDrawable != null) {
                                final int scrollX = getDestScrollX();
                                final int width = getChildWidth(mCurItem);
                                final int offset = scrollX % (width + mPageMargin);
                                if (offset != 0) {
                                        // Pages fit completely when settled; we only need to draw when in between
                                        final int left = scrollX - offset + width;
                                        mMarginDrawable.setBounds(left, mTopPageBounds, left + mPageMargin,
                                                        mBottomPageBounds);
                                        mMarginDrawable.draw(canvas);
                                }
                        }
                }

                private void setScrollingCacheEnabled(boolean enabled) {
                        if (mScrollingCacheEnabled != enabled) {
                                mScrollingCacheEnabled = enabled;
                                if (USE_CACHE) {
                                        final int size = getChildCount();
                                        for (int i = 0; i < size; ++i) {
                                                final View child = getChildAt(i);
                                                if (child.getVisibility() != GONE) {
                                                        child.setDrawingCacheEnabled(enabled);
                                                }
                                        }
                                }
                        }
                }

                protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
                        if (v instanceof ViewGroup) {
                                final ViewGroup group = (ViewGroup) v;
                                final int scrollX = v.getScrollX();
                                final int scrollY = v.getScrollY();
                                final int count = group.getChildCount();
                                for (int i = count - 1; i >= 0; i--) {
                                        final View child = group.getChildAt(i);
                                        if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() &&
                                                        y + scrollY >= child.getTop() && y + scrollY < child.getBottom() &&
                                                        canScroll(child, true, dx, x + scrollX - child.getLeft(),
                                                                        y + scrollY - child.getTop())) {
                                               
                                                return true;
                                        }
                                }
                        }

                        return checkV && ViewCompat.canScrollHorizontally(v, -dx);
                }


                public boolean dispatchKeyEvent(KeyEvent event) {

                        return super.dispatchKeyEvent(event) || executeKeyEvent(event);
                }


                public boolean executeKeyEvent(KeyEvent event) {
                        boolean handled = false;
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                switch (event.getKeyCode()) {
                                case KeyEvent.KEYCODE_DPAD_LEFT:
                                        handled = arrowScroll(FOCUS_LEFT);
                                        break;
                                case KeyEvent.KEYCODE_DPAD_RIGHT:
                                        handled = arrowScroll(FOCUS_RIGHT);
                                        break;
                                case KeyEvent.KEYCODE_TAB:
                                        if (Build.VERSION.SDK_INT >= 11) {
                                                if (KeyEventCompat.hasNoModifiers(event)) {
                                                        handled = arrowScroll(FOCUS_FORWARD);
                                                } else if (KeyEventCompat.hasModifiers(event, KeyEvent.META_SHIFT_ON)) {
                                                        handled = arrowScroll(FOCUS_BACKWARD);
                                                }
                                        }
                                        break;
                                }
                        }
                        return handled;
                }

                public boolean arrowScroll(int direction) {
                        View currentFocused = findFocus();
                        if (currentFocused == this) currentFocused = null;

                        boolean handled = false;

                        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused,
                                        direction);
                        if (nextFocused != null && nextFocused != currentFocused) {
                                if (direction == View.FOCUS_LEFT) {

                                        if (currentFocused != null && nextFocused.getLeft() >= currentFocused.getLeft()) {
                                                handled = pageLeft();
                                        } else {
                                                handled = nextFocused.requestFocus();
                                        }
                                } else if (direction == View.FOCUS_RIGHT) {

                                        if (currentFocused != null && nextFocused.getLeft() <= currentFocused.getLeft()) {
                                                handled = pageRight();
                                        } else {
                                                handled = nextFocused.requestFocus();
                                        }
                                }
                        } else if (direction == FOCUS_LEFT || direction == FOCUS_BACKWARD) {
                                handled = pageLeft();
                        } else if (direction == FOCUS_RIGHT || direction == FOCUS_FORWARD) {

                                handled = pageRight();
                        }
                        if (handled) {
                                playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
                        }
                        return handled;
                }

                boolean pageLeft() {
                        if (mCurItem > 0) {
                                setCurrentItem(mCurItem-1, true);
                                return true;
                        }
                        return false;
                }

                boolean pageRight() {
                        if (mAdapter != null && mCurItem < (mAdapter.getCount()-1)) {
                                setCurrentItem(mCurItem+1, true);
                                return true;
                        }
                        return false;
                }


                public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
                        final int focusableCount = views.size();

                        final int descendantFocusability = getDescendantFocusability();

                        if (descendantFocusability != FOCUS_BLOCK_DESCENDANTS) {
                                for (int i = 0; i < getChildCount(); i++) {
                                        final View child = getChildAt(i);
                                        if (child.getVisibility() == VISIBLE) {
                                                ItemInfo ii = infoForChild(child);
                                                if (ii != null && ii.position == mCurItem) {
                                                        child.addFocusables(views, direction, focusableMode);
                                                }
                                        }
                                }
                        }

                        if (
                                        descendantFocusability != FOCUS_AFTER_DESCENDANTS ||
                                        (focusableCount == views.size())) {
                                if (!isFocusable()) {
                                        return;
                                }
                                if ((focusableMode & FOCUSABLES_TOUCH_MODE) == FOCUSABLES_TOUCH_MODE &&
                                                isInTouchMode() && !isFocusableInTouchMode()) {
                                        return;
                                }
                                if (views != null) {
                                        views.add(this);
                                }
                        }
                }

                /**
                 * We only want the current page that is being shown to be touchable.
                 */

                public void addTouchables(ArrayList<View> views) {
                        for (int i = 0; i < getChildCount(); i++) {
                                final View child = getChildAt(i);
                                if (child.getVisibility() == VISIBLE) {
                                        ItemInfo ii = infoForChild(child);
                                        if (ii != null && ii.position == mCurItem) {
                                                child.addTouchables(views);
                                        }
                                }
                        }
                }


                protected boolean onRequestFocusInDescendants(int direction,
                                Rect previouslyFocusedRect) {
                        int index;
                        int increment;
                        int end;
                        int count = getChildCount();
                        if ((direction & FOCUS_FORWARD) != 0) {
                                index = 0;
                                increment = 1;
                                end = count;
                        } else {
                                index = count - 1;
                                increment = -1;
                                end = -1;
                        }
                        for (int i = index; i != end; i += increment) {
                                View child = getChildAt(i);
                                if (child.getVisibility() == VISIBLE) {
                                        ItemInfo ii = infoForChild(child);
                                        if (ii != null && ii.position == mCurItem) {
                                                if (child.requestFocus(direction, previouslyFocusedRect)) {
                                                        return true;
                                                }
                                        }
                                }
                        }
                        return false;
                }


                public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {

                        final int childCount = getChildCount();
                        for (int i = 0; i < childCount; i++) {
                                final View child = getChildAt(i);
                                if (child.getVisibility() == VISIBLE) {
                                        final ItemInfo ii = infoForChild(child);
                                        if (ii != null && ii.position == mCurItem &&
                                                        child.dispatchPopulateAccessibilityEvent(event)) {
                                                return true;
                                        }
                                }
                        }

                        return false;
                }


                protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
                        return new LayoutParams();
                }


                protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
                        return generateDefaultLayoutParams();
                }


                protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
                        return p instanceof LayoutParams && super.checkLayoutParams(p);
                }


                public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
                        return new LayoutParams(getContext(), attrs);
                }

                private class PagerObserver extends DataSetObserver {

                        public void onChanged() {
                                dataSetChanged();
                        }

                        public void onInvalidated() {
                                dataSetChanged();
                        }
                }

                public static class LayoutParams extends ViewGroup.LayoutParams {

                        public boolean isDecor;

                        public int gravity;

                        public LayoutParams() {
                                this(0);
                        }

                        public LayoutParams(int customWidth) {
                                super(FILL_PARENT, FILL_PARENT);
                                if (width != 0) {
                                        width = customWidth;
                                }
                        }

                        public LayoutParams(Context context, AttributeSet attrs) {
                                super(context, attrs);

                                final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
                                gravity = a.getInteger(0, Gravity.NO_GRAVITY);
                                a.recycle();
                        }
                }
}
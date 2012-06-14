package com.suse.android.sidemenu.utils;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;


public class PagerAdapter {
        private DataSetObservable mObservable = new DataSetObservable();

        public static final int POSITION_UNCHANGED = -1;
        public static final int POSITION_NONE = -2;

        private View mBehind;
        private View mContent;  
        private LayoutParams mContentParams;

        public void setBehind(View v) {
                mBehind = v;
        }

        public void setContent(View v, LayoutParams params) {
                mContent = v;
                mContentParams = params;
        }


        public int getCount() {
                int count = 0;
                count += (mBehind != null)? 1 : 0;
                count += (mContent != null)? 1 : 0;
                return count;
        }


        public void startUpdate(ViewGroup container) {
                startUpdate((View) container);
        }

       
        public Object instantiateItem(ViewGroup container, int position) {
                View view = null;
                LayoutParams params = null;
                switch (position) {
                case 0:
                        view = mBehind;
                        break;
                case 1:
                        view = mContent;
                        params = mContentParams;
                        break;
                }
                if (container instanceof ViewAbove) {
                        ViewAbove pager = (ViewAbove) container;
                        pager.addView(view, position, params);
                } else if (container instanceof ViewBehind) {
                        ViewBehind pager = (ViewBehind) container;
                        pager.addView(view, position, params);
                }
                return view;    
        }

        
        public void destroyItem(ViewGroup container, int position, Object object) {
                ((ViewAbove) container).removeView((View)object);
        }

       
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
                setPrimaryItem((View) container, position, object);
        }

        
        public void finishUpdate(ViewGroup container) {
                finishUpdate((View) container);
        }

        
        public void startUpdate(View container) {
        }

        
        public Object instantiateItem(View container, int position) {
                throw new UnsupportedOperationException(
                                "Required method instantiateItem was not overridden");
        }

       
        public void destroyItem(View container, int position, Object object) {
                throw new UnsupportedOperationException("Required method destroyItem was not overridden");
        }

        
        public void setPrimaryItem(View container, int position, Object object) {
        }

       
        public void finishUpdate(View container) {
        }

       
        public boolean isViewFromObject(View view, Object object) {
                return view == object;
        }

        
        public Parcelable saveState() {
                return null;
        }

        
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

       
        public int getItemPosition(Object object) {
                return POSITION_UNCHANGED;
        }

       
        public void notifyDataSetChanged() {
                mObservable.notifyChanged();
        }

        public void registerDataSetObserver(DataSetObserver observer) {
                mObservable.registerObserver(observer);
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
                mObservable.unregisterObserver(observer);
        }

        
        public CharSequence getPageTitle(int position) {
                return null;
        }
}

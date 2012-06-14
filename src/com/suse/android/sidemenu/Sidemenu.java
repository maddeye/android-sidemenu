package com.suse.android.sidemenu;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.suse.android.sidemenu.utils.*;

public class Sidemenu extends RelativeLayout {
        
        private ViewAbove mViewAbove;
        private ViewBehind mViewBehind;
        
        public Sidemenu(Context context) {
                this(context, null);
        }
        
        public Sidemenu(Context context, AttributeSet attrs) {
                this(context, attrs, 0);
        }

        public Sidemenu(Context context, AttributeSet attrs, int defStyle) {
                super(context, attrs, defStyle);
                
                // get the Above and Behind views
                mViewAbove = (ViewAbove) findViewById(R.id.sidemenuabove);
                mViewBehind = (ViewBehind) findViewById(R.id.sidemenubehind);
                
        }

        public void registerViews(ViewAbove va, ViewBehind vb) {
                mViewAbove = va;
                mViewBehind = vb;
                mViewAbove.setViewBehind(mViewBehind);
        }
        
        public void onMeasure(int widthSpec, int heightSpec) {
                super.onMeasure(widthSpec, heightSpec);
        }
        
        public void setAboveContent(View v, ViewGroup.LayoutParams p) {
                mViewAbove.setContent(v, p);
                mViewAbove.invalidate();
                mViewAbove.dataSetChanged();
        }
        
        public void setBehindContent(View v) {
                mViewBehind.setContent(v);
                mViewBehind.invalidate();
                mViewBehind.dataSetChanged();
        }
        
        public void setStatic(boolean b) {
                if (b) {
                        mViewAbove.setPagingEnabled(false);
                        mViewAbove.setCurrentItem(1);
                        mViewBehind.setCurrentItem(0);  
                }
        }

        public void showMenu() {
                mViewAbove.setCurrentItem(0);
        }

        public void showContent() {
                mViewAbove.setCurrentItem(1);
        }

        public boolean isMenuOpen() {
                return mViewAbove.getCurrentItem() == 0;
        }
        
        public void setBehindOffset(int i) {
                ((RelativeLayout.LayoutParams)mViewBehind.getLayoutParams()).setMargins(0, 0, i, 0);
        }

        public void setScrollScale(float f) {
                mViewBehind.setScrollScale(f);
        }

}
package com.suse.android.sidemenu;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;


import com.suse.android.sidemenu.utils.*;
import com.suse.android.sidemenu.utils.ViewAbove.LayoutParams;

public class SidemenuActivity extends Activity {

        private Sidemenu msidemenu;
        private View mLayout;
        private boolean mContentViewCalled = false;
        private boolean mBehindContentViewCalled = false;
        private int behindOffset;
        private float scrollScale;
        
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                                
                super.setContentView(R.layout.sidemenumain);
                
                msidemenu = (Sidemenu) super.findViewById(R.id.sidemenulayout);
                msidemenu.registerViews((ViewAbove) super.findViewById(R.id.sidemenuabove),
                                (ViewBehind) super.findViewById(R.id.sidemenubehind));
                mLayout = super.findViewById(R.id.sidemenulayout);
        }

        public void onPostCreate(Bundle savedInstanceState) {
                super.onPostCreate(savedInstanceState);
                if (!mContentViewCalled || !mBehindContentViewCalled) {
                        throw new IllegalStateException("Both setContentView and" +
                                        "setBehindContentView must be called in onCreate.");
                }
                msidemenu.setStatic(isStatic());
        }

        @Override
        public void setContentView(int id) {
                setContentView(getLayoutInflater().inflate(id, null));
        }

        public void setContentView(View v) {
                setContentView(v, null);
        }

        public void setContentView(View v, LayoutParams params) {
                if (!mContentViewCalled) {
                        mContentViewCalled = !mContentViewCalled;
                }
                msidemenu.setAboveContent(v, params);
        }
        
        public void setBehindContentView(int id) {
                setBehindContentView(getLayoutInflater().inflate(id, null));
        }

        public void setBehindContentView(View v) {
                setBehindContentView(v, null);
        }
        
        public void setBehindContentView(View v, LayoutParams params) {
                if (!mBehindContentViewCalled) {
                        mBehindContentViewCalled = !mBehindContentViewCalled;
                }
                msidemenu.setBehindContent(v);
        }
        
        private boolean isStatic() {
                return mLayout instanceof LinearLayout;
        }
        
        public boolean isOpened() {
       	 return msidemenu.isMenuOpen();
        }
                
        public int getBehindOffset() {

            return behindOffset;
	    }
	    
	    public void setBehindOffset(int i) {
	            msidemenu.setBehindOffset(i);
	            behindOffset = i;
	    }
	    
	    public float getBehindScrollScale() {
	            return scrollScale;
	    }
	    
	    public void setBehindScrollScale(float f) {
	            msidemenu.setScrollScale(f);
	            scrollScale = f;
	    }
        @Override
        public View findViewById(int id) {
                return msidemenu.findViewById(id);
        }

        
        public void toggle() {
                if (msidemenu.isMenuOpen()) {
                        showContent();
                } else {
                        showMenu();
                }
        }

        public void showMenu() {
                msidemenu.showMenu();
        }

        public void showContent() {
                msidemenu.showContent();
        }




}
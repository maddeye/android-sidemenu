package com.suse.android.sidemenu;

import com.suse.android.sidemenu.utils.ViewAbove;
import com.suse.android.sidemenu.utils.ViewBehind;
import com.suse.android.sidemenu.utils.ViewAbove.LayoutParams;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.TabActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

public class SidemenuTabActivity extends ActivityGroup
{
	
	 private Sidemenu msidemenu;
     private View mLayout;
     private boolean mContentViewCalled = false;
     private boolean mBehindContentViewCalled = false;
     

     
	 private TabHost mTabHost;
	 private String mDefaultTab = null;
	 private int mDefaultTabIndex = -1;
     
     protected void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
                          
             requestWindowFeature(Window.FEATURE_NO_TITLE);
             
             super.setContentView(R.layout.sidemenumain);
             
             msidemenu = (Sidemenu)super.findViewById(R.id.sidemenulayout);
             msidemenu.registerViews((ViewAbove) findViewById(R.id.sidemenuabove),
                             (ViewBehind) findViewById(R.id.sidemenubehind));
             mLayout = super.findViewById(R.id.sidemenulayout);
     }

     @Override
     public void onPostCreate(Bundle savedInstanceState) {
             super.onPostCreate(savedInstanceState);


 	         if (mTabHost.getCurrentTab() == -1) {
 	             mTabHost.setCurrentTab(0);
 	         }
             
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
             MenuContentChanged();
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
             MenuContentChanged();
     }
     
     private boolean isStatic() {
             return mLayout instanceof LinearLayout;
     }
             
     public int getBehindOffset() {

             return 0;
     }
     
     public void setBehindOffset(int i) {
             msidemenu.setBehindOffset(i);
     }
     
     public float getBehindScrollScale() {
             return 0;
     }
     
     public void setBehindScrollScale(float f) {
             msidemenu.setScrollScale(f);
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


     public void setDefaultTab(String tag) {
         mDefaultTab = tag;
         mDefaultTabIndex = -1;
     }


     public void setDefaultTab(int index) {
         mDefaultTab = null;
         mDefaultTabIndex = index;
     }

     @Override
     protected void onRestoreInstanceState(Bundle state) {
         super.onRestoreInstanceState(state);
         String cur = state.getString("currentTab");
         if (cur != null) {
             mTabHost.setCurrentTabByTag(cur);
         }
         if (mTabHost.getCurrentTab() < 0) {
             if (mDefaultTab != null) {
                 mTabHost.setCurrentTabByTag(mDefaultTab);
             } else if (mDefaultTabIndex >= 0) {
                 mTabHost.setCurrentTab(mDefaultTabIndex);
             }
         }
     }

     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         String currentTabTag = mTabHost.getCurrentTabTag();
         if (currentTabTag != null) {
             outState.putString("currentTab", currentTabTag);
         }
     }

     public void MenuContentChanged() {
         super.onContentChanged();
         mTabHost = (TabHost) findViewById(android.R.id.tabhost);

         if (mTabHost == null) {
             throw new RuntimeException(
                     "Your content must have a TabHost whose id attribute is " +
                     "'android.R.id.tabhost'");
         }
         mTabHost.setup(getLocalActivityManager());
     }


     @Override
     protected void
     onChildTitleChanged(Activity childActivity, CharSequence title) {
         if (getLocalActivityManager().getCurrentActivity() == childActivity) {
             View tabView = mTabHost.getCurrentTabView();
             if (tabView != null && tabView instanceof TextView) {
                 ((TextView) tabView).setText(title);
             }
         }
     }

     public TabHost getTabHost() {
         return mTabHost;
     }

     public TabWidget getTabWidget() {
         return mTabHost.getTabWidget();
     }
}


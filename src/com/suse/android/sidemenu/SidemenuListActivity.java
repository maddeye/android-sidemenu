package com.suse.android.sidemenu;

import com.suse.android.sidemenu.utils.ViewAbove;
import com.suse.android.sidemenu.utils.ViewBehind;
import com.suse.android.sidemenu.utils.ViewAbove.LayoutParams;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SidemenuListActivity extends Activity
{
	
	 private Sidemenu msidemenu;
     private View mLayout;
     private boolean mContentViewCalled = false;
     private boolean mBehindContentViewCalled = false;
     
     
     protected void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
                  
             requestWindowFeature(Window.FEATURE_NO_TITLE);
             
             super.setContentView(R.layout.sidemenumain);
             
             msidemenu = (Sidemenu)super.findViewById(R.id.sidemenulayout);
             msidemenu.registerViews((ViewAbove) findViewById(R.id.sidemenuabove),
                             (ViewBehind) findViewById(R.id.sidemenubehind));
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

     
     
     protected ListAdapter mAdapter;

     protected ListView mList;

     private Handler mHandler = new Handler();
     private boolean mFinishedStart = false;

     private Runnable mRequestFocus = new Runnable() {
         public void run() {
             mList.focusableViewAvailable(mList);
         }
     };
     

     protected void onListItemClick(ListView l, View v, int position, long id) {
     }
     
    
     @Override
     protected void onRestoreInstanceState(Bundle state) {
         super.onRestoreInstanceState(state);
     }


     public void MenuContentChanged() {
         super.onContentChanged();
         mList = (ListView)findViewById(android.R.id.list);
         if (mList == null) {
             throw new RuntimeException(
                     "Your content must have a ListView whose id attribute is " +
                     "'android.R.id.list'");
         }
         mList.setOnItemClickListener(mOnClickListener);
         if (mFinishedStart) {
             setListAdapter(mAdapter);
         }
         mHandler.post(mRequestFocus);
         mFinishedStart = true;
     }

     public void setListAdapter(ListAdapter adapter) {
         synchronized (this) {
             mAdapter = adapter;
             mList.setAdapter(adapter);
         }
     }

     public void setSelection(int position) {
         mList.setSelection(position);
     }

     public int getSelectedItemPosition() {
         return mList.getSelectedItemPosition();
     }

     public long getSelectedItemId() {
         return mList.getSelectedItemId();
     }

     public ListView getListView() {
         return mList;
     }

     public ListAdapter getListAdapter() {
         return mAdapter;
     }


     private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
         public void onItemClick(AdapterView parent, View v, int position, long id)
         {
             onListItemClick((ListView)parent, v, position, id);
         }
     };

}

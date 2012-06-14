package com.suse.android.sidemenu;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.suse.android.sidemenu.utils.*;
import com.suse.android.sidemenu.utils.ViewAbove.LayoutParams;

public class SidemenuActivity extends Activity {

        private Sidemenu msidemenu;
        private View mLayout;
        private boolean mContentViewCalled = false;
        private boolean mBehindContentViewCalled = false;
        private sidemenuList mMenuList;

        public void onCreate(Bundle savedInstanceState) {
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
        
        public int getBehindOffset() {
                // TODO
                return 0;
        }
        
        public void setBehindOffset(int i) {
                msidemenu.setBehindOffset(i);
        }
        
        public float getBehindScrollScale() {
                // TODO
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
//              if (isStatic()) return;
                if (msidemenu.isMenuOpen()) {
                        showContent();
                } else {
                        showMenu();
                }
        }

        public void showMenu() {
//              if (isStatic()) return;
                msidemenu.showMenu();
        }

        public void showContent() {
//              if (isStatic()) return;
                msidemenu.showContent();
        }

        public void addMenuListItem(MenuListItem mli) {
                mMenuList.add(mli);
        }

        public static class sidemenuList extends ListView {
                public sidemenuList(final Context context) {
                        super(context);
                        setAdapter(new sidemenuListAdapter(context));
                        setOnItemClickListener(new OnItemClickListener() {
                                public void onItemClick(AdapterView<?> parent, View view, int position,
                                                long id) {
                                        OnClickListener listener = ((sidemenuListAdapter)getAdapter()).getItem(position).mListener;
                                        if (listener != null) listener.onClick(view);
                                }                               
                        });
                }
                public void add(MenuListItem mli) {
                        ((sidemenuListAdapter)getAdapter()).add(mli);
                }
        }

        public static class sidemenuListAdapter extends ArrayAdapter<MenuListItem> {

                public sidemenuListAdapter(Context context) {
                        super(context, 0);
                }
                public View getView(int position, View convertView, ViewGroup parent) {
                        View v;
                        if (convertView != null) {
                                v = convertView;
                        } else {
                                LayoutInflater inflater = 
                                                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                v = inflater.inflate(R.layout.sidemenurow, null);
                        }
                        MenuListItem item = getItem(position);
                        ImageView icon = (ImageView) v.findViewById(R.id.sidemenurowicon);
                        icon.setImageDrawable(item.mIcon);
                        TextView title = (TextView) v.findViewById(R.id.sidemenurowtitle);
                        title.setText(item.mTitle);
                        return v;
                }
        }

        public class MenuListItem {
                private Drawable mIcon;
                private String mTitle;
                private OnClickListener mListener;
                public MenuListItem(String title) {
                        mTitle = title;
                }
                public void setTitle(String title) {
                        mTitle = title;
                }
                public void setOnClickListener(OnClickListener listener) {
                        mListener = listener;
                }
                public View toListViewRow() {
                        View v = SidemenuActivity.this.getLayoutInflater().inflate(R.layout.sidemenurow, null);
                        ((TextView)v.findViewById(R.id.sidemenurowtitle)).setText(mTitle);
                        ((ImageView)v.findViewById(R.id.sidemenurowicon)).setImageDrawable(mIcon);
                        return v;
                }
        }



}
Android Sidemenu (Yet another Fly-in-Menu)
==========================================

-----------------


What is it
----------

Android Sidemenu is a Facebook like fly-in-menu.

Screenshots

![Overlay](https://github.com/maddeye/android-sidemenu/raw/master/resources/AboveScreen.jpg?raw=true) 

![Toggle Button](https://github.com/maddeye/android-sidemenu/raw/master/resources/Toggle.jpg?raw=true)
 
![Drag](https://github.com/maddeye/android-sidemenu/raw/master/resources/Drag.jpg?raw=true)

Version
-------

1.0.0(Release)


Android Version
---------------

Android 2.2 API Level 8 or higher


How to use it
-------------

Right click on your project and choose *Properties* (You also can press Alt+Enter), then click on *Android* in the list on the left. At the bottom you find the used libraries. There you add the Sidemenu project.


Activity where you want the menu

```Java
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.suse.android.sidemenu.SidemenuActivity;

public class SidemenuSampleActivity extends Sidemenu[List|Tab]Activity {
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setBehindContentView(R.layout.main2);
        
        ((Button)this.findViewById(R.id.btn_toggle)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                        toggle();
                }
        });
        
        setBehindOffset(60);
        setBehindScrollScale(0.5f);
    }
}
```


with **setContentView** you set the main screen layout (the screen with the white background) and with **setBehindContentView** you set the menu layout (grey background).

**toggle()** open/close the menu. You also can open the menu when you drag the left border.


Documentation
=============

-------------


General
-------

* setContentView(int) : <font color="#daa520">void</font>
	- Set the main content layout.

* setContentView(View) : <font color="#daa520">void</font>
	- Set the main content layout.

* setContentView(View, LayoutParams) : <font color="#daa520">void</font>
	- Set the main content layout.

* setBehindContentView(int) : <font color="#daa520">void</font>
	- Set the sidemenu layout.

* setBehindContentView(View) : <font color="#daa520">void</font>
	- Set the sidemenu layout.

* setBehindContentView(View, LayoutParams) : <font color="#daa520">void</font>
	- Set the sidemenu layout.

* isOpened() : <font color="#daa520">boolean</font>
	- (true) Sidemenu is opened.
	- (false) Sidemenu is closed.

* setBehindOffset(int) : <font color="#daa520">void</font>
	- Set the distance to the margin on the right side.

* setBehindScroolScale(int) : <font color="#daa520">void</font>
	- Set the speed of the scroll animation.

* toggle() : <font color="#daa520">void</font>
	- Open/Close the sidemenu.

* showMenu() : <font color="#daa520">void</font>
	- Get the sidemenu to the foreground.

* showContent() : <font color="#daa520">void</font>
	- Get the content to the foreground.


<font color="#ff0000">Every Activity own the standard methods of the superclass.</font>

=============

Authors
-------

* Daniel Igel <digel@suse.de>


License
-------

Copyright © 2011 SUSE LINUX Products GmbH.

Android-Sidemenu is licensed under the MIT license. See MIT-LICENSE for details.

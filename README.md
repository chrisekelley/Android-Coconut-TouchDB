Android-Coconut-TouchDB
=======================

Android-Coconut-TouchDB uses [TouchDB-Android](https://github.com/couchbaselabs/TouchDB-Android) 
to provide [Couchapps](http://couchapp.org/page/index) on Android devices.

An Android [APK](https://github.com/vetula/Android-Coconut-TouchDB/raw/master/Android-Coconut-TouchDB.apk) is available 
if you'd like to test this out.

Configuration
-------------

Clone [TouchDB-Android](https://github.com/vetula/TouchDB-Android), which is my fork of [TouchDB-Android](https://github.com/couchbaselabs/TouchDB-Android).
My fork implements [do_POST_DesignDocument ](https://github.com/couchbaselabs/TouchDB-Android/pull/9) which is used to view incident details. 

Using your own Couchapp
------------------------

If you use your own Couchapp, edit res/raw/coconut.properties and change coconut-sample to the name of your couchapp. 
Note that you can also change the port in this file.

    app_db=coconut-sample
    couchAppInstanceUrl=coconut-sample/_design/coconut/index.html

You must provide .zips of the touchdb and touchdb attachments directory.

An easy (but slow) way to do this is to deploy Android-Coconut-TouchDB to the 
[Android Emulator](http://developer.android.com/guide/developing/devices/emulator.html) and then pushing your new Couchapp
to it. 

Redirect the emulator to a local port. In this example, TouchDB, which runs on port 8888, is redirected to port 8880:

    telnet localhost 5554 
    redir add tcp:8880:8888

Ensure that you have added 

     , "tou": {"db": "http://127.0.0.1:8880/coconut-sample"}

to your .couchapprc file.

Push the new project to the emulator:

	couchapp push tou

Pushing to the emulator is very slow. Using couchapp -v, watching logcat, or having some coffee can help. 
You also may be able to push to a device instead of the emulator.

Once it is on the emulator you can download the SQLlite db to the app:

	cd Android-Coconut-TouchDB/assets
	adb pull /data/data/org.rti.rcd.ict.touchdb.testapp/files
	cd files

	Zip coconut-sample.touchdb to create coconut-sample.touchdb.zip
	Zip coconut-sample to create coconut-sample.zip

Remove the old zips and un-zipped items.
	
Sample Couchapp
---------------

More information about the sample couchapp is on the [Cococnut-sample](https://github.com/vetula/coconut-sample) project page.
	
TODO:
------
* Replication
* Add all of the C2DM code from [Android-Coconut-MobileFuton](https://github.com/vetula/Android-Coconut-MobileFuton) 
* Push MobileFuton into this build.
	
What else?
-----------

Take a look at [Android-Coconut-MobileFuton](https://github.com/vetula/Android-Coconut-MobileFuton), which is an Android runtime 
for Couchapps using Mobile-Couchbase and may provide a little more background on how this works. 


Kudos:
-------

* Marty Schoch: [TouchDB-Android](https://github.com/couchbaselabs/TouchDB-Android)
* Dale Harvey: [Mobile Futon](https://github.com/daleharvey/Android-MobileFuton)


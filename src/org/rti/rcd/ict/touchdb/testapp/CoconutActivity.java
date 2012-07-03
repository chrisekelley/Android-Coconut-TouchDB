package org.rti.rcd.ict.touchdb.testapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.rti.rcd.ict.lgug.utils.CoconutUtils;
import org.rti.rcd.ict.lgug.utils.UnZip;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.javascript.TDJavaScriptViewCompiler;
import com.couchbase.touchdb.listener.TDListener;


public class CoconutActivity extends Activity {

    public static final String TAG = "CoconutActivity";
    private TDListener listener;
	private WebView webView;
    private static CoconutActivity coconutRef;
    private ProgressDialog progressDialog;
    private Handler uiHandler;
    private ProgressDialog installProgress;
    static Handler myHandler;
    private String couchAppUrl;
    // setup clock
    Calendar cal = null;
    Date starttime = null;
    long long_starttime = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //String couchAppUrl = "/";
        //stopwatch_start();
        String filesDir = getFilesDir().getAbsolutePath();
		
	    Properties properties = new Properties();

    	try {
    		InputStream rawResource = getResources().openRawResource(R.raw.coconut);
    		properties.load(rawResource);
    		System.out.println("The properties are now loaded");
    		System.out.println("properties: " + properties);
    	} catch (Resources.NotFoundException e) {
    		System.err.println("Did not find raw resource: " + e);
    	} catch (IOException e) {
    		System.err.println("Failed to open microlog property file");
    	}
    	
        String ipAddress = "0.0.0.0";
        Log.d(TAG, ipAddress);
		String host = ipAddress;
		int port = 8888;
		String url = "http://" + host + ":" + Integer.toString(port) + "/";
		this.setCouchAppUrl(url + properties.getProperty("couchAppInstanceUrl"));
		
        TDServer server;
        try {
            server = new TDServer(filesDir);
            
            //String ipAddress = IPUtils.getLocalIpAddress();
            //listener = new TDListener(server, 8888, ipAddress);
            listener = new TDListener(server, 8888);
            listener.start();
    		//String host = "localhost";

    		//couchAppUrl = url + "coconut-emas/_design/coconut/index.html";
    		//couchAppUrl = url + "coconut-emas/doc1/";
    		
    		/*TDDatabase db = server.getExistingDatabaseNamed("coconut-emas");
    		if(db == null) {
                String couchAppDoc = createTestDatabase(server);
                couchAppUrl = url + couchAppDoc;
    		}*/
            
            TDView.setCompiler(new TDJavaScriptViewCompiler());

        } catch (IOException e) {
            Log.e(TAG, "Unable to create TDServer", e);
        }
        
        final Activity activity = this;
        webView = new WebView(CoconutActivity.this);
        uiHandler = new Handler();
		coconutRef = this;
        
        progressDialog = new ProgressDialog(CoconutActivity.this);
		//progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setTitle("TouchDB");
		progressDialog.setMessage("Loading. Please wait...");
		progressDialog.setCancelable(false);
	    progressDialog.setOwnerActivity(coconutRef);
	    progressDialog.setIndeterminate(true);
	    progressDialog.setProgress(0);
	    progressDialog.show();
	    
		//webView.setWebChromeClient(new WebChromeClient());
		webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress)
            {
            	// Activities and WebViews measure progress with different scales.
				// The progress meter will automatically disappear when we reach 100%
				//activity.setProgress(progress * 1000);
				progressDialog.show();
				activity.setProgress(progress * 1000);
				coconutRef.setProgress(progress * 1000);
				progressDialog.setProgress(progress * 1000);
				progressDialog.incrementProgressBy(progress);
				Log.d(TAG, "Progress: " + progress);

				if(progress == 100 && progressDialog.isShowing()) {
					Log.d(TAG, "Progress: DONE! " + progress);
					 // Stop clock and calculate time elapsed
			        //stopwatchFinish();
					progressDialog.dismiss();
				}
            }

			public void stopwatchFinish() {
				Calendar cal2 = new GregorianCalendar();
				Date endtime = cal2.getTime();
				long long_endtime = endtime.getTime();
				long difference = (long_endtime - long_starttime);
				float diffSecs = difference / 1000;
				Log.v(TAG,"********  Time to open app: " + difference + " or " + diffSecs + " seconds ******");
			}
        });
		webView.setWebViewClient(new CustomWebViewClient());		
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.getSettings().setDomStorageEnabled(true);

		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		webView.requestFocus(View.FOCUS_DOWN);
	    webView.setOnTouchListener(new View.OnTouchListener() {
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	            switch (event.getAction()) {
	                case MotionEvent.ACTION_DOWN:
	                case MotionEvent.ACTION_UP:
	                    if (!v.hasFocus()) {
	                        v.requestFocus();
	                    }
	                    break;
	            }
	            return false;
	        }
	    });
        setContentView(R.layout.main);
		setContentView(webView);
    	String appDb = properties.getProperty("app_db");
	    File destination = new File(filesDir + File.separator + appDb + ".touchdb");
	    Log.d(TAG, "Checking for touchdb at " + filesDir + File.separator + appDb + ".touchdb");
	    if (!destination.exists()) {
	    	Log.d(TAG, "Touchdb does not exist. Unzipping files.");
	    	// must be in the assets directory
	    	//installProgress =  ProgressDialog.show(CoconutActivity.this, "", "Installing database. Please wait...", true);
	    	try {
	    		// This is the touchdb
	    		//CoconutUtils.unZipFromAssets(this.getApplicationContext(), appDb + ".touchdb.zip", filesDir);
	        	String destinationFilename = CoconutUtils.extractFromAssets(this.getApplicationContext(), appDb + ".touchdb.zip", filesDir);	
	        	File destFile = new File(destinationFilename);
	    		unzipFile(destFile);
	    		// These are the attachments
	    		//CoconutUtils.unZipFromAssets(this.getApplicationContext(), appDb + ".zip", filesDir);
	    		destinationFilename = CoconutUtils.extractFromAssets(this.getApplicationContext(), appDb + ".zip", filesDir);	
	        	destFile = new File(destinationFilename);
	    		unzipFile(destFile);
                loadWebview();
			} catch (Exception e) {
				e.printStackTrace();
				String errorMessage = "There was an error extracting the database.";
				displayLargeMessage(errorMessage, "big");
				Log.d(TAG, errorMessage);
				//installProgress.setMessage("There was an error - unable to find database zip.");
				progressDialog.setMessage(errorMessage);
				this.setCouchAppUrl("/");
			    //installProgress.dismiss();
			}
	    } else {
	    	Log.d(TAG, "Touchdb exists. Loading WebView.");
	    	loadWebview();
	    }
    }

	public void stopwatch_start() {
		cal = new GregorianCalendar();
		starttime = cal.getTime();
        long_starttime = starttime.getTime();
    	Log.v(TAG, "Start page view" + starttime.toString());
	}

	private void loadWebview() {
		String status = listener.getStatus();
		Log.d(TAG, "Server status:" + status);
		Log.d(TAG, "webView.loadUrl: " + couchAppUrl);
		webView.loadUrl(this.getCouchAppUrl());

	}

	private class CustomWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith("tel:")) {
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
				startActivity(intent);
			} else if (url.startsWith("http:") || url.startsWith("https:")) {
				view.loadUrl(url);
			}
			return true;
		}
	}
	
	public void displayMessage( String message ) {
		//uiHandler.post( new AddMessageTask( message ) );
		uiHandler.post( new ToastMessage( this, message ) );
	}
	
	public void displayLargeMessage( String message, String size ) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = null;
		if (size.equals("big")) {
			layout = inflater.inflate(R.layout.toast_layout_large,(ViewGroup) findViewById(R.id.toast_layout_large));
		} else {
			layout = inflater.inflate(R.layout.toast_layout_medium,(ViewGroup) findViewById(R.id.toast_layout_large));
		}
		
		ImageView image = (ImageView) layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.android);
		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(message);
		//uiHandler.post( new ToastMessage( this, message ) );
		/*Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);*/
		uiHandler.post( new ToastMessageBig( this, message, layout ) );
		//toast.show();
	}
	
	public void unzipFile(File zipfile) {
		//installProgress = ProgressDialog.show(CoconutActivity.this, "Extract Zip","Extracting Files...", false, false);
		File zipFile = zipfile;
		displayLargeMessage("Extracting: " + zipfile, "medium");
		String directory = null;
		directory = zipFile.getParent();
		directory = directory + "/";
		myHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// process incoming messages here
				switch (msg.what) {
				case 0:
					// update progress bar
					//installProgress.setMessage("" + (String) msg.obj);
					Log.d(TAG,  (String) msg.obj);
					break;
				case 1:
					//installProgress.cancel();
					//Toast toast = Toast.makeText(getApplicationContext(), "Zip extracted successfully", Toast.LENGTH_SHORT);
					displayLargeMessage(msg.obj + ": Complete.", "medium");
					//toast.show();
					//provider.refresh();
					Log.d(TAG, msg.obj + ":Zip extracted successfully");
					break;
				case 2:
					//installProgress.cancel();
					break;
				}
				super.handleMessage(msg);
			}

		};
		/*Thread workthread = new Thread(new UnZip(myHandler, zipFile, directory));
	    workthread.start();*/
		UnZip unzip = new UnZip(myHandler, zipFile, directory);
		unzip.run();
		Log.d(TAG, "Completed extraction.");
	}

	public String getCouchAppUrl() {
		return couchAppUrl;
	}

	public void setCouchAppUrl(String couchAppUrl) {
		this.couchAppUrl = couchAppUrl;
	}
	
/*	   public static Context getAppContext() {
	        return CoconutActivity.getAppContext();
	    }*/

}

class ToastMessage implements Runnable {
    public ToastMessage( Context ctx, String msg ) {
        this.ctx = ctx;
        this.msg = msg;
    }

    public void run() {
        Toast.makeText( ctx, msg, Toast.LENGTH_SHORT).show();
    }

    Context ctx;
    String msg;
}

class ToastMessageBig implements Runnable {
	View layout;
	Context ctx;
	String msg;
	
	public ToastMessageBig( Context ctx, String msg, View layout ) {
		this.ctx = ctx;
		this.msg = msg;
		this.layout = layout;
	}
	
	public void run() {
		//Toast.makeText( ctx, msg, Toast.LENGTH_SHORT).show();
		Toast toast = new Toast(ctx);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}
}






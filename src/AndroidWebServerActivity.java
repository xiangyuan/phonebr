package com.wondershare;

import com.wondershare.http.core.WebServer;
import com.wondershare.util.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AndroidWebServerActivity extends Activity {
	/** Called when the activity is first created. */

	private Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			String message = (String) msg.obj;
			tv.setText(message);
		}
	};
	TextView tv = null;
	WebServer ws = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Button ok = (Button) findViewById(R.id.start);
		tv = (TextView) findViewById(R.id.msg);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ws = new WebServer(getApplicationContext());
				ws.start();
				Message msg = mHandler.obtainMessage();
				msg.obj = "http://" + Utils.getLocalIpAddress() + ":"
						+ WebServer.NET_REQUEST_PORT;
				mHandler.sendMessage(msg);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (ws != null) {
			ws.closeServer();
		}
	}
}
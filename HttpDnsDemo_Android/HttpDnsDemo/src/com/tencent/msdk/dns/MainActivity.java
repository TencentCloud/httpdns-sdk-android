package com.tencent.msdk.dns;

import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tencent.beacon.event.UserAction;

public class MainActivity extends Activity {
	private EditText mEdit;
	private Button mButton;
	private TextView mText;
	private String httpDns;
	private Vector<String> vector = null;
	private static final int UPDATE_UI = 0;
	private static String TAG = "WGGetHostByName";
	private String mDomain = "www.qq.com.";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mEdit = (EditText) findViewById(R.id.edit);
		mButton = (Button) findViewById(R.id.button);
		mText = (TextView) findViewById(R.id.text);

		// 初始化HttpDns
		MSDKDnsResolver.getInstance().init(MainActivity.this.getApplicationContext());
		// 初始化灯塔（注意：如果已接入MSDK或者灯塔，检查是否已初始化，以免重复操作）
		UserAction.initUserAction(MainActivity.this.getApplicationContext());

		mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						// mDomain只能传入域名请不要传入ip
						mDomain = mEdit.getText().toString();
						Log.i(TAG, "mDomain is " + mDomain);
						if (mDomain != null) {
							vector = WGGetHostByName(mDomain);
							if (vector != null) {
								httpDns = vector.toString().replace("[", "").replace("]", "");
								Log.i(TAG, "httpDns is " + httpDns);

							}
						}

						Message msg = new Message();
						msg.what = UPDATE_UI;
						msg.obj = httpDns;
						mHandler.sendMessage(msg);

					}
				}).start();

			}
		});

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Vector<String> WGGetHostByName(String domain) {
		// 调用httpdns解析接口
		String ips = MSDKDnsResolver.getInstance().getAddrByName(domain);
		Vector mVector = new Vector();
		if (ips != null) {
			if (ips.contains(";")) {
				String[] temp = ips.split(";");
				for (int i = 0; i < temp.length; i++) {
					mVector.add(temp[i]);
				}
			} else {
				mVector.add(ips);
			}
		}
		return mVector;
	}

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE_UI:
				String data = (String) msg.obj;
				mText.setText(data);
				break;
			default:
				break;
			}
		}

	};

}

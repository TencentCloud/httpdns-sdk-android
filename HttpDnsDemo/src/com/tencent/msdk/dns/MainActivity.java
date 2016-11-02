package com.tencent.msdk.dns;

import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tencent.beacon.event.UserAction;

public class MainActivity extends Activity {
	private EditText mEdit;
	private Button mButton, h5Button;
	private TextView mText;
	private String httpDns;
	private Vector<String> vector = null;
	private static final int UPDATE_UI = 0;
	private String mDomain = "www.qq.com.";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mEdit = (EditText) findViewById(R.id.edit);
		mText = (TextView) findViewById(R.id.text);
		mButton = (Button) findViewById(R.id.button);
		h5Button = (Button) findViewById(R.id.h5button);

		// 初始化HttpDns
		MSDKDnsResolver.getInstance().init(MainActivity.this.getApplicationContext());
		// 初始化灯塔（注意：如果已接入MSDK或者灯塔，检查是否已初始化，以免重复操作）
		UserAction.initUserAction(MainActivity.this.getApplicationContext());
		// 普通HttpDns解析
		mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						// mDomain只能传入域名请不要传入IP
						mDomain = mEdit.getText().toString();
						if (mDomain != null) {
							// 调用HttpDns接口
							vector = WGGetHostByName(mDomain);
							if (vector != null) {
								httpDns = vector.toString().replace("[", "").replace("]", "");
								Logger.i("httpDns is " + httpDns);

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

		// HTML5页面解析Demo，如果游戏或者应用没有涉及H5页面解析可不用关注
		h5Button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, WebViewDemo.class);
				startActivity(intent);
			}
		});

	}

	// 将调用HttpDns返回的IP集合封装成Vector，业务如果不需要则直接使用返回的IPS即可
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Vector<String> WGGetHostByName(String domain) {
		// 调用HttpDns解析接口
		String ips = MSDKDnsResolver.getInstance().getAddrByName(domain);
		Logger.i("Final to user ips are:" + ips);
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

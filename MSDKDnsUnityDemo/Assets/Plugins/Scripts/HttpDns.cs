using UnityEngine;
using System.Runtime.InteropServices;

namespace com.tencent.httpdns {
    public class HttpDns {
#if UNITY_ANDROID
	private static AndroidJavaObject m_dnsJo;
	private static AndroidJavaClass sGSDKPlatformClass;
	public static void Init() {
		AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
		if (jc == null)
			return;
			
		AndroidJavaObject joactivety = jc.GetStatic<AndroidJavaObject>("currentActivity");
		if (joactivety == null)
			return;
		AndroidJavaObject context = joactivety.Call<AndroidJavaObject>("getApplicationContext");
		// 初始化HttpDns
		AndroidJavaObject joDnsClass = new AndroidJavaObject("com.tencent.msdk.dns.MSDKDnsResolver");
		Debug.Log(" WGGetHostByName ===========" + joDnsClass);
		if (joDnsClass == null)
			return;
		m_dnsJo = joDnsClass.CallStatic<AndroidJavaObject>("getInstance");
			Debug.Log(" WGGetHostByName ===========" + m_dnsJo);
		if (m_dnsJo == null)
			return;
		m_dnsJo.Call("init", context);
		// 初始化灯塔
		AndroidJavaObject joBeaconClass = new AndroidJavaObject("com.tencent.beacon.event.UserAction");
		if (joBeaconClass == null)
			return;
		m_dnsJo.Call("initUserAction", context);
	}
		
	public static string GetHttpDnsIP( string strUrl ) {
		string strIp = string.Empty;
		if (AndroidJNI.AttachCurrentThread() != 0)
        {
            return null;
        }
		// 解析得到IP配置集合
		strIp = m_dnsJo.Call<string>("getAddrByName", strUrl);
		Debug.Log( strIp );
		if( strIp != null )
		{
		    // 取第一个
			string[] strIps = strIp.Split(';');
			strIp = strIps[0];
		}
		return strIp;
	}
#endif

#if UNITY_IOS
        [DllImport("__Internal")]
		private static extern string WGGetHostByName(string domain);
#endif

        // 解析域名
		public static string GetHostByName(string domain) {
#if UNITY_EDITOR
			return "1.2.3.4;0";
#endif

#if UNITY_IOS
			return WGGetHostByName (domain);
#endif
				
#if UNITY_ANDROID
			return GetHttpDnsIP(domain);
#endif
		}


	}
}
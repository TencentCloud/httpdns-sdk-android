using UnityEngine;
using System.Collections;
using UnityEngine.UI;
using com.tencent.httpdns;

public class ClickEvent : MonoBehaviour {

	public InputField domain;
	public Text result;
#if UNITY_ANDROID
	public void Awake() {
		HttpDns.Init();
	}
#endif
    public void StartClick() {
		print ("WGGetHostByName StartClick");
		string domainStr = domain.text;
		print (domainStr);
		if (domainStr == null || domainStr.Equals("")) {
			domainStr = "www.qq.com";
			print("输入为空，使用默认域名：www.qq.com");
			result.text = "输入为空，使用默认域名：www.qq.com";
		}
		string ips = HttpDns.GetHostByName(domainStr);
		print ("WGGetHostByName ips " + ips);
#if UNITY_ANDROID
		result.text = ips;
#endif

#if UNITY_IOS
		string[] sArray=ips.Split(new char[] {';'}); 
		if (sArray != null && sArray.Length > 1) {
			if (!sArray[1].Equals("0")) {
				//使用建议：当ipv6地址存在时，优先使用ipv6地址
				//TODO 使用ipv6地址进行连接，注意格式，ipv6需加方框号[ ]进行处理，例如：http://[64:ff9b::b6fe:7475]/
				result.text = "ipv6地址存在：" + sArray[1] + "，建议优先使用ipv6地址，注意格式，需加方框号加[]处理！";
			} else {
				//使用ipv4地址进行连接
				result.text = "ipv6地址不存在，使用ipv4地址进行连接：" + sArray[0];
			}
		}
#endif
    }
}

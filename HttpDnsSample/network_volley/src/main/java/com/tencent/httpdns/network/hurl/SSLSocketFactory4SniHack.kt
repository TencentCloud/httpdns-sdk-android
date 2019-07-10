package com.tencent.httpdns.network

import android.os.Build
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.InetAddress
import java.net.Socket
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.net.ssl.*

internal class SSLSocketFactory4SniHack(private val hostname: String) : SSLSocketFactory() {

    override fun getDefaultCipherSuites(): Array<out String> =
        realSSLSocketFactory.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<out String> =
        realSSLSocketFactory.supportedCipherSuites

    override fun createSocket(host: String?, port: Int): Socket =
        realSSLSocketFactory.createSocket(host, port)

    override fun createSocket(host: String?, port: Int, localHost: InetAddress?, localPort: Int): Socket =
        realSSLSocketFactory.createSocket(host, port, localHost, localPort)

    override fun createSocket(host: InetAddress?, port: Int): Socket =
        realSSLSocketFactory.createSocket(host, port)

    override fun createSocket(address: InetAddress?, port: Int, localAddress: InetAddress?, localPort: Int): Socket =
        realSSLSocketFactory.createSocket(address, port, localAddress, localPort)

    override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket {
        val sslSocket = realSSLSocketFactory.createSocket(s, host, port, autoClose) as SSLSocket
        // NOTE: 如果无法成功setHostname, 则SNI扩展无法成功设置
        // 这里直接消化异常, 由网络库决定如何处理网络访问失败情况
        try {
            setHostnameMethod(sslSocket)
                ?.invoke(sslSocket, hostname)
        } catch (ignored: IllegalAccessException) {
        } catch (ignored: InvocationTargetException) {
        }
        if (!HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, sslSocket.session)) {
            throw SSLPeerUnverifiedException("Cannot verify hostname:$hostname")
        }
        return sslSocket
    }

    companion object {

        private const val SET_HOSTNAME_METHOD_NAME = "setHostname"

        private val realSSLSocketFactory by lazy {
            newSslSocketFactory(platformTrustManager())
        }

        private var _setHostnameMethod: Method? = null

        private fun setHostnameMethod(sslSocket: SSLSocket): Method? {
            if (null == _setHostnameMethod) {
                // NOTE: 如果无法成功反射setHostname, 则SNI扩展无法成功设置
                // 这里直接消化异常, 由网络库决定如何处理网络访问失败情况
                try {
                    _setHostnameMethod = sslSocket::class.java.getMethod(
                        SET_HOSTNAME_METHOD_NAME, String::class.java)
                } catch (e: NoSuchMethodException) {
                }
            }
            return _setHostnameMethod
        }

        // copy from OkHttp

        private fun platformTrustManager() =
            try {
                val trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(null as KeyStore?)
                val trustManagers = trustManagerFactory.trustManagers
                if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
                    throw IllegalStateException("Unexpected default trust managers:${Arrays.toString(trustManagers)}")
                }
                trustManagers[0] as X509TrustManager
            } catch (e: GeneralSecurityException) {
                throw RuntimeException("No System TLS", e) // The system has no TLS. Just give up.
            }

        private fun newSslSocketFactory(trustManager: X509TrustManager) =
            try {
                val sslContext = getSSLContext()
                sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
                sslContext.socketFactory
            } catch (e: GeneralSecurityException) {
                throw RuntimeException("No System TLS", e) // The system has no TLS. Just give up.
            }

        private fun getSSLContext(): SSLContext {
            if (Build.VERSION.SDK_INT in 16..21) {
                try {
                    return SSLContext.getInstance("TLSv1.2")
                } catch (ignored: NoSuchAlgorithmException) {
                    // fallback to TLS
                }
            }

            try {
                return SSLContext.getInstance("TLS")
            } catch (e: NoSuchAlgorithmException) {
                throw IllegalStateException("No TLS provider", e)
            }
        }
    }
}

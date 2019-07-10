package com.tencent.httpdns.sample

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.tencent.httpdns.base.log.DnsLog
import com.tencent.httpdns.network.base.DEFAULT_ENCODING
import com.tencent.httpdns.network.base.HTML_MIME_TYPE
import com.tencent.httpdns.sample.webview.WebViewHelper
import kotlinx.android.synthetic.main.fragment_website.*

class WebsiteFragment : Fragment() {

    private val websiteViewModel by lazy {
        // NOTE: Let it crash if fragment has not attached to activity
        ViewModelProviders.of(activity!!).get(WebsiteViewModel::class.java)
    }

    private val websiteIndex by lazy {
        arguments!!.getInt(WEBSITE_INDEX)
    }

    private val websiteContent by lazy {
        websiteViewModel.websites[websiteIndex]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DnsLog.d("$this onAttach()")
        websiteContent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DnsLog.d("$this onCreate()")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        DnsLog.d("${this@WebsiteFragment} onCreateView()")
        return inflater.inflate(R.layout.fragment_website, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        DnsLog.d("$this onActivityCreated()")
    }

    override fun onStart() {
        super.onStart()

        DnsLog.d("$this onStart()")

        WebViewHelper.initWebView(webView)
        websiteContent
            .observe(this@WebsiteFragment, Observer { website ->
                DnsLog.d("Loaded data from ${website.url}")
                webView.loadDataWithBaseURL(
                    website.url,
                    website.content,
                    HTML_MIME_TYPE,
                    DEFAULT_ENCODING,
                    null
                )
            })
    }

    override fun onResume() {
        super.onResume()

        DnsLog.d("$this onResume()")
    }

    override fun onPause() {
        super.onPause()

        DnsLog.d("$this onPause()")
    }

    override fun onStop() {
        super.onStop()

        DnsLog.d("$this onStop()")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        DnsLog.d("$this onDestroyView()")
    }

    override fun onDestroy() {
        super.onDestroy()

        DnsLog.d("$this onDestroy()")
    }

    override fun onDetach() {
        super.onDetach()

        DnsLog.d("$this onDetach()")
    }

    companion object {

        private const val WEBSITE_INDEX = "websiteIndex"

        @JvmStatic
        fun newInstance(websiteIndex: Int): WebsiteFragment {
            DnsLog.d("WebsiteFragment.newInstance($websiteIndex) called")
            return WebsiteFragment().apply {
                arguments = Bundle().apply {
                    putInt(WEBSITE_INDEX, websiteIndex)
                }
            }
        }
    }
}

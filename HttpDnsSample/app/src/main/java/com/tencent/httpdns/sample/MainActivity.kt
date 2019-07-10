package com.tencent.httpdns.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tencent.httpdns.base.log.DnsLog

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val websiteViewModel by lazy {
        ViewModelProviders.of(this).get(WebsiteViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DnsLog.d("$this onCreate()")

        setContentView(R.layout.activity_main)

        websiteContainerViewPager.adapter =
            object : FragmentStateAdapter(this) {

                private val websiteFragments =
                    arrayOfNulls<WebsiteFragment>(websiteViewModel.websites.size)

                override fun getItemCount() = websiteFragments.size

                override fun getItem(position: Int) =
                    websiteFragments[position] ?: WebsiteFragment.newInstance(position).apply {
                        websiteFragments[position] = this
                    }
            }
        websiteContainerViewPager.offscreenPageLimit = 1
    }

    override fun onDestroy() {
        super.onDestroy()

        DnsLog.d("$this onDestroy()")
    }
}

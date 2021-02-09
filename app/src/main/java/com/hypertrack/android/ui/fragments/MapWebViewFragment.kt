package com.hypertrack.android.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_webview.*

class MapWebViewFragment : Fragment(R.layout.fragment_webview) {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)
        webView.settings.javaScriptEnabled = true

        val historyUrl = requireArguments().getString(WEBVIEW_URL)!!
        webView.loadUrl(historyUrl)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (srlHistory.isRefreshing) {
                    srlHistory.isRefreshing = false
                }
            }
        }
        srlHistory.setOnRefreshListener { webView.reload() }
    }

    companion object {
        const val WEBVIEW_URL = "webviewUrl"

        fun newInstance(deviceHistoryUrl: String) = MapWebViewFragment().apply {
            arguments = Bundle().apply {
                putString(WEBVIEW_URL, deviceHistoryUrl)
            }
        }
    }
}
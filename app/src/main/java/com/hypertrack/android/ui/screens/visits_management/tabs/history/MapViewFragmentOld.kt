package com.hypertrack.android.ui.screens.visits_management.tabs.history

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hypertrack.logistics.android.github.R

class MapViewFragmentOld(private val url: String) : Fragment() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_tab_map_webview_obsolete, container, false) as SwipeRefreshLayout
        val view = rootView.findViewById<WebView>(R.id.webView)
        if (view is WebView) {
            view.settings.javaScriptEnabled = true
            view.loadUrl(url)
            view.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (rootView.isRefreshing) rootView.isRefreshing = false
                }
            }
        }
        rootView.setOnRefreshListener { view.reload() }
        return rootView
    }
}

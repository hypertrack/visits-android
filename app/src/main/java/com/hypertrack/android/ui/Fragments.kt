package com.hypertrack.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.view_models.VisitsManagementViewModel
import com.hypertrack.logistics.android.github.R


class PageFragment(
    var page: Page,
    private val visitListsAdapter: RecyclerView.Adapter<*>,
    private val visitListViewManager: RecyclerView.LayoutManager,
    private val visitsViewModel: VisitsManagementViewModel
) : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate with page $page")
        if (arguments != null) {
            page = Page.values()[arguments!!.getInt(ARG_PAGE)]
        }
        Log.d(TAG, "Restored page $page")
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = when (page) {
        Page.VIEW -> {

            val view = inflater.inflate(R.layout.webview_fragment, container, false)
            if (view is WebView) {
                view.settings.javaScriptEnabled = true
                view.loadUrl(visitsViewModel.deviceHistoryWebViewUrl)
            }
            view
        }
        Page.LIST -> {
            val view = inflater.inflate(R.layout.visits_list_fragment, container, false)
            if (view is RecyclerView)
                view.apply {
                    layoutManager = visitListViewManager
                    adapter = visitListsAdapter
                }
            view
        }
    }

    companion object FACTORY {
        const val TAG = "PageFragment"
        const val ARG_PAGE = "ARG_PAGE"
        fun newInstance(
            page: Page,
            visitListsAdapter: RecyclerView.Adapter<*>,
            visitListViewManager: RecyclerView.LayoutManager,
            visitsViewModel: VisitsManagementViewModel
        ): PageFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page.ordinal)
            val fragment = PageFragment(page, visitListsAdapter, visitListViewManager, visitsViewModel)
            fragment.arguments = args
            return fragment
        }
    }
}

enum class Page {LIST, VIEW}

class SimpleFragmentPagerAdapter(
    fm: FragmentManager,
    context: Context,
    private val visitListsAdapter: RecyclerView.Adapter<*>,
    private val visitListViewManager: RecyclerView.LayoutManager,
    private val visitsViewModel: VisitsManagementViewModel
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val tabTitles = context.resources.getStringArray(R.array.tab_names)

    override fun getCount(): Int = tabTitles.size

    override fun getItem(position: Int): Fragment = PageFragment.newInstance(
        Page.values()[position], visitListsAdapter, visitListViewManager, visitsViewModel
    )

    override fun getPageTitle(position: Int): CharSequence? = tabTitles[position]

}
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
import com.hypertrack.logistics.android.github.R


class PageFragment : Fragment() {

    private var page:Page? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate with page $page")
        arguments?.let {
            page = Page.values()[it.getInt(ARG_PAGE)]
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
                val historyUrl = arguments?.getString(WEBVIEW_URL)
                view.loadUrl(historyUrl?:"")
            }
            view
        }
        else -> {
            val view = inflater.inflate(R.layout.visits_list_fragment, container, false)
            if (view is RecyclerView) {
                val activity = activity as VisitsManagementActivity
                view.apply {
                    layoutManager = activity.viewManager
                    adapter = activity.viewAdapter
                }
            }
            view
        }
    }

    companion object FACTORY {
        const val TAG = "PageFragment"
        const val ARG_PAGE = "ARG_PAGE"
        const val WEBVIEW_URL = "ARG_URL"
        fun newInstance(page: Page, deviceHistoryUrl: String): PageFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page.ordinal)
            args.putString(WEBVIEW_URL, deviceHistoryUrl)
            val fragment = PageFragment()
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
    private val deviceHistoryUrl: String
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val tabTitles = context.resources.getStringArray(R.array.tab_names)

    override fun getCount(): Int = tabTitles.size

    override fun getItem(position: Int): Fragment = PageFragment.newInstance(
        Page.values()[position], deviceHistoryUrl
    )

    override fun getPageTitle(position: Int): CharSequence? = tabTitles[position]

}
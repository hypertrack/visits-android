package com.hypertrack.android.ui.common

import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.RecyclerView

open class EndlessScrollListener(
    private val loadMoreListener: OnLoadMoreListener
) : RecyclerView.OnScrollListener() {
    open val visibleThreshold = 3
    private var currentPage = 0
    private var currentTotalItems = 0
    private val firstItemPageIndex = 0
    private var loading = false
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val totalItemCount = recyclerView.adapter!!.itemCount
        val visibleItemCount = recyclerView.layoutManager!!.childCount
        val firstVisibleItem =
            (recyclerView.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        if (totalItemCount < currentTotalItems) {
            currentPage = firstItemPageIndex
            currentTotalItems = totalItemCount
            if (totalItemCount == 0) {
                loading = true
            }
        }
        if (loading && totalItemCount > currentTotalItems) {
            loading = false
            currentTotalItems = totalItemCount
            currentPage++
        }
        if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {
            loadMoreListener.onLoadMore(currentPage + 1, totalItemCount)
            loading = true
        }
    }

    interface OnLoadMoreListener {
        fun onLoadMore(page: Int, totalItemsCount: Int)
    }

}
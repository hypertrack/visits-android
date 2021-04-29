package com.hypertrack.android.ui.base

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hypertrack.android.utils.MyApplication

abstract class BaseFragment<T : Activity>(layoutId: Int) : Fragment(layoutId) {

    fun mainActivity(): T = activity as T

    open val delegates: MutableList<FragmentDelegate<T>> by lazy {
        mutableListOf<FragmentDelegate<T>>(
//            HideKeyboardDelegate(this)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        delegates.forEach { it.onViewCreated(view) }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        delegates.forEach { it.onDestroyView() }
    }

    override fun onResume() {
        super.onResume()
        delegates.forEach { it.onResume() }
    }

    override fun onPause() {
        super.onPause()
        delegates.forEach { it.onPause() }
    }

    override fun onStop() {
        super.onStop()
        delegates.forEach { it.onStop() }
    }

    override fun onDestroy() {
        super.onDestroy()
        delegates.forEach { it.onDestroy() }
    }

    open fun onLeave() {
        delegates.forEach { it.onLeave() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        delegates.forEach { it.onSaveState(outState) }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            delegates.forEach { it.onLoadState(savedInstanceState) }
        }
    }

    abstract class FragmentDelegate<M : Activity>(protected val fragment: BaseFragment<M>) {
        open fun onViewCreated(view: View) {}
        open fun onResume() {}
        open fun onDestroyView() {}
        open fun onPause() {}
        open fun onStop() {}
        open fun onDestroy() {}
        open fun onLeave() {}
        open fun onSaveState(bundle: Bundle) {}
        open fun onLoadState(bundle: Bundle) {}
        open fun onBackPressed(): Boolean {
            return false
        }
    }

    open fun onBackPressed(): Boolean {
        delegates.forEach {
            if (it.onBackPressed()) {
                return true
            }
        }
        return false
    }

}
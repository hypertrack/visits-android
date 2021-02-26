package com.hypertrack.android.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import org.jetbrains.annotations.TestOnly

abstract class NavActivity : AppCompatActivity() {

    protected val allowNotBaseFragments = false

    abstract val layoutRes: Int

    abstract val navHostId: Int

    protected lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)

        navController = findNavController(navHostId)

        navController.addOnDestinationChangedListener { _, destination, bundle ->
            try {
                getCurrentBaseFragment()?.onLeave()
                onDestinationChanged(destination)
            } catch (e: IndexOutOfBoundsException) {
            }
        }

    }

    protected open fun onDestinationChanged(destination: NavDestination) {

    }

    protected fun getCurrentBaseFragment(): BaseFragment<*>? {
        val navHostFragment = supportFragmentManager.findFragmentById(navHostId)
        val res = navHostFragment!!.childFragmentManager.fragments[0]
        if (res is BaseFragment<*>) {
            return res
        } else {
            if (allowNotBaseFragments) {
                return null
            } else {
                throw IllegalStateException("allowNotBaseFragments = false")
            }
        }
    }

    protected fun getCurrentFragment(): Fragment {
        val navHostFragment = supportFragmentManager.findFragmentById(navHostId)
        return navHostFragment!!.childFragmentManager.fragments[0]
    }

    override fun onBackPressed() {
        if (getCurrentBaseFragment()?.onBackPressed() == false) {
            super.onBackPressed()
        }
    }

    @TestOnly
    fun getNavigationController(): NavController {
        return navController
    }
}
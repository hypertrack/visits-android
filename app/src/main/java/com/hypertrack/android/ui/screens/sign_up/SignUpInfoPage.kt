package com.hypertrack.android.ui.screens.sign_up

import android.app.Activity
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.hypertrack.android.interactors.LoginInteractor
import com.hypertrack.android.interactors.LoginInteractorImpl
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import java.util.*

//todo tests
object SignUpInfoPage {
    private const val TAG = "SignupInfoPage"

    fun getSignUpInfoPageView(
        activity: Activity,
        parent: ViewGroup?,
        cognitoUserAttributes: MutableMap<String, String>,
        incorrect: TextView
    ): View {
        val view: View =
            LayoutInflater.from(activity).inflate(R.layout.view_pager_signup_info, parent, false)
        val useCaseSelector = view.findViewById<Spinner>(R.id.spUseCase)
        val stateSelector = view.findViewById<Spinner>(R.id.state)

        val itemLayout = R.layout.item_spinner

        val notSelected = R.string.sign_up_not_selected.stringFromResource()
        val useCases = mapOf(
            R.string.sign_up_visits.stringFromResource() to LoginInteractor.UserAttrs.USE_CASE_VISITS,
            R.string.sign_up_deliveries.stringFromResource() to LoginInteractor.UserAttrs.USE_CASE_DELIVERIES,
            R.string.sign_up_rides.stringFromResource() to LoginInteractor.UserAttrs.USE_CASE_RIDES
        )

        val states = mapOf(
            R.string.sign_up_my_fleet.stringFromResource() to LoginInteractor.UserAttrs.STATE_MY_FLEET,
            R.string.sign_up_customer_fleet.stringFromResource() to LoginInteractor.UserAttrs.STATE_MY_CUSTOMERS_FLEET,
        )

        val displayUseCases = listOf(notSelected) + useCases.keys
        useCaseSelector.adapter =
            ArrayAdapter<String>(activity, itemLayout, displayUseCases)
        useCaseSelector.setSelection(
            displayUseCases.indexOf(
                cognitoUserAttributes[LoginInteractor.UserAttrs.USE_CASE_KEY]
            )
        )
        useCaseSelector.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                i: Int,
                l: Long
            ) {
                incorrect.visibility = View.INVISIBLE
                if (i == 0) {
                    cognitoUserAttributes.remove(LoginInteractor.UserAttrs.USE_CASE_KEY)
                } else {
                    cognitoUserAttributes[LoginInteractor.UserAttrs.USE_CASE_KEY] =
                        useCases[useCaseSelector.selectedItem]!!
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        val displayStates = listOf(notSelected) + states.keys
        stateSelector.adapter =
            ArrayAdapter<String>(activity, itemLayout, displayStates)
        stateSelector.setSelection(
            displayStates.indexOf(
                cognitoUserAttributes[LoginInteractor.UserAttrs.STATE_KEY]
            )
        )
        stateSelector.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                i: Int,
                l: Long
            ) {
                incorrect.visibility = View.INVISIBLE
                if (i == 0) {
                    cognitoUserAttributes.remove(LoginInteractor.UserAttrs.STATE_KEY)
                } else {
                    cognitoUserAttributes[LoginInteractor.UserAttrs.STATE_KEY] =
                        states[stateSelector.selectedItem]!!
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        return view
    }

    fun Int.stringFromResource(): String {
        return MyApplication.context.getString(this)
    }
}
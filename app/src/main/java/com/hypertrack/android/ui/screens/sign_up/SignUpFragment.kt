package com.hypertrack.android.ui.screens.sign_up

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.hypertrack.android.interactors.LoginInteractor
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.setGoneState
import com.hypertrack.android.ui.common.stringFromResource
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_signup.*
import java.util.*

class SignUpFragment : ProgressDialogFragment(R.layout.fragment_signup) {

    private val vm: SignUpViewModel by viewModels {
        MyApplication.injector.provideViewModelFactory(
            MyApplication.context
        )
    }

    private var company: String? = null
    private var email: String? = null
    private var password: String? = null

    private val cognitoUserAttributes = mutableMapOf<String, String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.title = R.string.sign_up.stringFromResource()

        view_pager.setAdapter(MyPagerAdapter())
        view_pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                when (view_pager.getCurrentItem()) {
                    PAGE_USER -> {
                        accept.setVisibility(View.INVISIBLE)
                        agree_to_terms_of_service.setVisibility(View.INVISIBLE)
                        next.setVisibility(View.VISIBLE)
                    }
                    PAGE_INFO -> {
                        accept.setVisibility(View.VISIBLE)
                        agree_to_terms_of_service.setVisibility(View.VISIBLE)
                        next.setVisibility(View.INVISIBLE)
                        view_pager.setVisibility(View.VISIBLE)
                    }
                }
            }
        })

        next.setOnClickListener {
            nextPage()
        }

        accept.setOnClickListener(View.OnClickListener {
            if (cognitoUserAttributes.keys.containsAll(
                    Arrays.asList(
                        LoginInteractor.UserAttrs.USE_CASE_KEY,
                        LoginInteractor.UserAttrs.STATE_KEY
                    )
                )
            ) {
                showProgress()
                vm.onSignUpClicked(email!!, password!!, cognitoUserAttributes)
            } else {
                showError(getString(R.string.all_fields_required))
                return@OnClickListener
            }
        })

        vm.errorText.observe(viewLifecycleOwner, {
            dismissProgress()
            incorrect.setGoneState(it == null)
            incorrect.text = it
        })

        vm.page.observe(viewLifecycleOwner, {
            view_pager.currentItem = it
        })

        vm.destination.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        sign_in.setOnClickListener {
            vm.onSignInClicked()
        }
    }

    private fun nextPage() {
        when (view_pager.getCurrentItem()) {
            PAGE_USER -> {
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(
                        company
                    )
                ) {
                    showError(getString(R.string.email_password_fields_required))
                    return
                }
                company?.let {
                    cognitoUserAttributes.put(LoginInteractor.UserAttrs.COMPANY_KEY, it)
                }
            }
            PAGE_INFO -> {
            }
        }
        val view = requireActivity().currentFocus
        if (view != null) {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        view_pager.setCurrentItem(view_pager.getCurrentItem() + 1, true)
    }

    private fun showError(msg: String) {
        incorrect.text = msg
        incorrect.visibility = View.VISIBLE
    }

    companion object {
        val PAGES_COUNT = 2
        val PAGE_USER = 0
        val PAGE_INFO = 1
    }

    inner class MyPagerAdapter : PagerAdapter() {
        override fun instantiateItem(collection: ViewGroup, position: Int): Any {
            val inflater = LayoutInflater.from(collection.context)
            var view: View? = null
            when (position) {
                PAGE_USER -> {
                    view = inflater.inflate(R.layout.view_pager_signup_user, collection, false)
                    val companyNameEditText = view.findViewById<EditText>(R.id.company_name)
                    val emailAddressEditText = view.findViewById<EditText>(R.id.email_address)
                    val passwordEditText = view.findViewById<EditText>(R.id.password)
                    val passwordClear = view.findViewById<View>(R.id.password_clear)
                    companyNameEditText.setText(company)
                    companyNameEditText.addTextChangedListener(object : HTTextWatcher() {
                        override fun afterTextChanged(editable: Editable) {
                            company = editable.toString()
                        }
                    })
                    emailAddressEditText.setText(email)
                    emailAddressEditText.addTextChangedListener(object : HTTextWatcher() {
                        override fun afterTextChanged(editable: Editable) {
                            email = editable.toString().toLowerCase()
                        }
                    })
                    passwordEditText.setText(password)
                    passwordEditText.addTextChangedListener(object : HTTextWatcher() {
                        override fun afterTextChanged(editable: Editable) {
                            passwordClear.visibility =
                                if (TextUtils.isEmpty(editable)) View.INVISIBLE else View.VISIBLE
                            password = editable.toString()
                        }
                    })
                    passwordEditText.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            nextPage()
                            v.clearFocus()
                            return@OnEditorActionListener true
                        }
                        false
                    })
                    passwordClear.setOnClickListener { passwordEditText.setText("") }
                }
                PAGE_INFO -> view = SignUpInfoPage.getSignUpInfoPageView(
                    mainActivity(),
                    collection,
                    cognitoUserAttributes,
                    incorrect
                )
            }
            collection.addView(view)
            return view!!
        }

        override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
            collection.removeView(view as View)
        }

        override fun getCount(): Int {
            return PAGES_COUNT
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }
    }
}


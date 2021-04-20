package com.hypertrack.android.ui.screens.visits_management.tabs.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.hypertrack.android.ui.MainActivity
import com.hypertrack.android.ui.base.BaseFragment
import com.hypertrack.android.ui.common.KeyValueAdapter
import com.hypertrack.android.ui.common.setLinearLayoutManager
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : BaseFragment<MainActivity>(R.layout.fragment_profile) {

    private val adapter = KeyValueAdapter(showCopyButton = true)

    private val vm: ProfileViewModel by viewModels {
        MyApplication.injector.provideUserScopeViewModelFactory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvProfile.setLinearLayoutManager(requireContext())
        rvProfile.adapter = adapter
        adapter.onCopyClickListener = {
            vm.onCopyItemClick(it)
        }

        vm.profile.observe(viewLifecycleOwner, {
            adapter.updateItems(it)
        })
    }

}
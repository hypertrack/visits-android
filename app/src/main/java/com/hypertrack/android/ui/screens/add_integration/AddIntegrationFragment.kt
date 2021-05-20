package com.hypertrack.android.ui.screens.add_integration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hypertrack.android.models.Integration
import com.hypertrack.android.ui.MainActivity
import com.hypertrack.android.ui.base.BaseAdapter
import com.hypertrack.android.ui.base.BaseFragment
import com.hypertrack.android.ui.common.*
import com.hypertrack.android.ui.screens.add_place_info.AddPlaceInfoFragment
import com.hypertrack.android.utils.Injector
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_add_integration.*
import kotlinx.android.synthetic.main.fragment_add_place_info.toolbar
import kotlinx.android.synthetic.main.item_integration.view.*
import kotlinx.android.synthetic.main.progress_bar.*

class AddIntegrationFragment : BaseFragment<MainActivity>(R.layout.fragment_add_integration) {

    private val adapter = object : BaseAdapter<Integration, BaseAdapter.BaseVh<Integration>>() {
        override val itemLayoutResource = R.layout.item_integration

        override fun createViewHolder(
            view: View,
            baseClickListener: (Int) -> Unit
        ): BaseVh<Integration> {
            return object : BaseContainerVh<Integration>(view, baseClickListener) {
                override fun bind(item: Integration) {
                    item.name?.toView(containerView.tvName)
                    item.type.toView(containerView.tvDescription)
                }
            }
        }
    }.apply {
        onItemClickListener = {
            vm.onIntegrationClicked(it)
        }
    }

    private val vm: AddIntegrationViewModel by viewModels {
        Injector.provideUserScopeViewModelFactory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.title = ""
        mainActivity().setSupportActionBar(toolbar)
        mainActivity().supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mainActivity().supportActionBar!!.setHomeButtonEnabled(true)

        rvIntegrations.setLinearLayoutManager(requireContext())
        rvIntegrations.adapter = adapter

        vm.integrations.observe(viewLifecycleOwner, {
            adapter.updateItems(it)
            lIntegrationsPlaceholder.setGoneState(it.isNotEmpty())
        })

        vm.loadingStateBase.observe(viewLifecycleOwner, {
            progress.setGoneState(!it)
            rvIntegrations.setGoneState(it)
            if (it) lIntegrationsPlaceholder.hide()
            if (it) {
                loader.playAnimation()
            } else {
                loader.cancelAnimation()
            }
        })

        vm.error.observe(viewLifecycleOwner, {
            it.consume { e ->
                SnackbarUtil.showErrorSnackbar(view, e.message)
            }
        })

        vm.integrationSelectedEvent.observe(viewLifecycleOwner, {
            it.consume { integration ->
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    AddPlaceInfoFragment.KEY_INTEGRATION,
                    integration
                )
                findNavController().popBackStack()
                Utils.hideKeyboard(requireActivity())
            }
        })

        etSearch.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterChanged(text: String) {
                vm.onQueryChanged(text)
            }
        })

        Utils.showKeyboard(requireActivity(), etSearch)
    }
}
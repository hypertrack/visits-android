package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hypertrack.backend.AbstractBackendProvider
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.views.HyperTrackViews
import com.hypertrack.sdk.views.dao.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShareTripFragment private constructor(
    private val mBackendProvider: AbstractBackendProvider,
    private val deviceId: String,
    private val realTimeUpdatesProvider: HyperTrackViews
) :
    Fragment(), ShareTripPresenter.View, OnBackPressedListener {
    private var presenter: ShareTripPresenter? = null
    private var loader: LoaderDecorator? = null
    private lateinit var share: View
    private lateinit var tripId: String
    private lateinit var shareUrl: String
    private val liveMapViewModel: LiveMapViewModel by viewModels({requireParentFragment()})


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tripId = arguments?.getString(TRIP_ID_KEY)?:""
        shareUrl = requireArguments().getString(SHARE_URL_KEY) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_share_trip, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader = LoaderDecorator(context)
        view.findViewById<View>(R.id.back)
            .setOnClickListener { requireActivity().onBackPressed() }
        share = view.findViewById(R.id.share)
    }

    override fun onResume() {
        super.onResume()
        presenter = ShareTripPresenter(
            requireContext(),
            this,
            shareUrl,
            mBackendProvider,
            deviceId,
            realTimeUpdatesProvider
        )
        share.setOnClickListener { presenter!!.shareTrackMessage() }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val map = liveMapViewModel.getMap()
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) { presenter?.subscribeTripUpdates(map, tripId) }
        }
    }

    override fun onPause() {
        super.onPause()
        presenter!!.pause()
    }

    override fun showProgressBar() {
        if (activity != null) {
            loader!!.start()
        }
    }

    override fun hideProgressBar() {
        if (activity != null) {
            loader!!.stop()
        }
    }

    override fun onTripUpdate(trip: Trip) {
        share.visibility = View.VISIBLE
    }

    override fun onBackPressed(): Boolean {
        presenter!!.endTrip()
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter!!.destroy()
    }

    companion object {
        const val TRIP_ID_KEY = "trip_id"
        const val SHARE_URL_KEY = "share_url"
        fun newInstance(
            tripId: String?,
            shareUrl: String?,
            backendProvider: AbstractBackendProvider,
            deviceId: String,
            realTimeUpdatesProvider: HyperTrackViews
        ): Fragment {
            val fragment = ShareTripFragment(backendProvider, deviceId, realTimeUpdatesProvider)
            val bundle = Bundle()
            bundle.putString(TRIP_ID_KEY, tripId)
            bundle.putString(SHARE_URL_KEY, shareUrl)
            fragment.arguments = bundle
            return fragment
        }
    }
}
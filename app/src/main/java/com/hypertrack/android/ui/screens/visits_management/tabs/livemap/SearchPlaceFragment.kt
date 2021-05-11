package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.models.AbstractBackendProvider
import com.hypertrack.android.ui.screens.sign_up.HTTextWatcher
import com.hypertrack.android.utils.Injector
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.views.HyperTrackViews
import javax.inject.Inject

class SearchPlaceFragment(
    @Inject private val backendProvider: AbstractBackendProvider,
    @Inject private val deviceId: String
) : Fragment(R.layout.fragment_search_place), SearchPlacePresenter.View {
    private lateinit var config: Config
    private lateinit var presenter: SearchPlacePresenter
    private lateinit var search: EditText
    private lateinit var destinationOnMap: View
    private var offlineView: View? = null
    private lateinit var home: View
    private lateinit var setHome: View
    private lateinit var homeInfo: View
    private lateinit var setOnMap: View
    private lateinit var confirm: View
    private var placesAdapter = PlacesAdapter()
    private lateinit var loader: LoaderDecorator
    private val liveMapViewModel: LiveMapViewModel by viewModels({requireParentFragment()})
    private lateinit var noDestination: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = arguments?.getParcelable("config") ?: Config.SEARCH_PLACE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = SearchPlacePresenter(
            requireContext(),
            this,
            backendProvider,
            deviceId,
            viewLifecycleOwner,
            SearchPlaceState(requireContext(), backendProvider),
            Injector.getVisitsRepo(requireActivity())
        )
        search = view.findViewById(R.id.search)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        setHasOptionsMenu(true)
        search.setOnClickListener {
            presenter.setMapDestinationModeEnable(
                false
            )
        }
        search.addTextChangedListener(object : HTTextWatcher() {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                presenter.search(charSequence.toString())
            }
        })
        search.requestFocus()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT)
        noDestination = view.findViewById(R.id.no_destination)
        requireActivity().setTitle(R.string.where_are_you_going)
        search.setHint(R.string.i_m_going_to)
        noDestination.setOnClickListener {
            presenter.setMapDestinationModeEnable(false)
            presenter.providePlace(null)
        }
        destinationOnMap = view.findViewById(R.id.destination_on_map)
        offlineView = view.findViewById(R.id.offline)
        home = view.findViewById(R.id.home)
        home.visibility = View.GONE
        setHome = view.findViewById(R.id.set_home)
        homeInfo = view.findViewById(R.id.home_info)
        val onHomeAddressClickListener = View.OnClickListener {
            Log.d(TAG, "On Home address clicked")
        }
        setHome.setOnClickListener(onHomeAddressClickListener)
        homeInfo.findViewById<View>(R.id.home_edit).setOnClickListener(onHomeAddressClickListener)
        homeInfo.setOnClickListener { presenter.selectHome() }
        setOnMap = view.findViewById(R.id.set_on_map)
        setOnMap.setOnClickListener { presenter.setMapDestinationModeEnable(true) }
        confirm = view.findViewById(R.id.confirm)
        confirm.setOnClickListener { presenter.confirm() }
        val layoutManager = LinearLayoutManager(activity)
        val locationsRecyclerView: RecyclerView = view.findViewById(R.id.locations)
        locationsRecyclerView.setHasFixedSize(true)
        val dividerItemDecoration = DividerItemDecoration(
            view.context,
            layoutManager.orientation
        )
        locationsRecyclerView.addItemDecoration(dividerItemDecoration)
        locationsRecyclerView.layoutManager = layoutManager

        placesAdapter.setOnItemClickListener(object : PlacesAdapter.OnItemClickListener {
            override fun onItemClick(
                adapter: RecyclerView.Adapter<*>?,
                view: View?,
                position: Int
            ) { placesAdapter.getItem(position)?.let { presenter.selectItem(it) }
            }

        })

        locationsRecyclerView.adapter = placesAdapter
        val hideSoftInputOnTouchListener = OnTouchListener { _, _ ->
            hideSoftInput()
            false
        }
        view.setOnTouchListener(hideSoftInputOnTouchListener)
        locationsRecyclerView.setOnTouchListener(hideSoftInputOnTouchListener)
        loader = LoaderDecorator(requireContext())
        presenter.search(null)
        view.visibility = View.INVISIBLE

        liveMapViewModel.state.observe(viewLifecycleOwner) { viewState ->
            when(viewState) {
                is OnTrip -> {
                    view.visibility = View.INVISIBLE
                    presenter.initMap(viewState.map)
                }
                is SearchPlace -> {
                    view.visibility = View.VISIBLE
                }
                else -> {
                    view.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun hideSoftInput() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(search.windowToken, 0)
        search.clearFocus()
    }

    override fun updateConnectionStatus(offline: Boolean) {
        if (offline) {
            offlineView!!.visibility = View.VISIBLE
            confirm.isEnabled = false
        } else {
            offlineView!!.visibility = View.GONE
            confirm.isEnabled = true
        }
    }

    override fun updateAddress(address: String?) {
        search.setText(address)
    }

    override fun updateHomeAddress(home: PlaceModel?) {
        if (home == null) {
            this.home.visibility = View.GONE
        } else {
            this.home.visibility = View.VISIBLE
            setHome.visibility = View.GONE
            homeInfo.visibility = View.VISIBLE
            (homeInfo.findViewById<View>(R.id.home_text) as TextView).text = home.address
        }
    }

    override fun updateList(list: List<PlaceModel>) {
        placesAdapter.clear()
        placesAdapter.addAll(list)
        placesAdapter.notifyDataSetChanged()
    }

    override fun showHomeAddress() {
        home.visibility = View.VISIBLE
    }

    override fun hideHomeAddress() {
        home.visibility = View.GONE
    }

    override fun showSetOnMap() {
        hideSoftInput()
        setOnMap.visibility = View.GONE
        destinationOnMap.visibility = View.VISIBLE
        confirm.visibility = View.VISIBLE
    }

    override fun hideSetOnMap() {
        setOnMap.visibility = View.VISIBLE
        destinationOnMap.visibility = View.GONE
        confirm.visibility = View.GONE
    }

    override fun showProgressBar() { activity?.let { loader.start() } }

    override fun hideProgressBar() { activity?.let { loader.stop() } }

    override fun finish() {
        liveMapViewModel.onPlaceSelected()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideSoftInput()
        presenter.destroy()
    }

    class Config : Parcelable {
        val key: String?
        var titleResId = 0
        var hintResId = 0
        var isSkipEnabled = false
        var isNotDecidedEnabled = false

        constructor(key: String) {
            this.key = key
        }

        fun setTitle(titleResId: Int): Config {
            this.titleResId = titleResId
            return this
        }

        fun setHint(hintResId: Int): Config {
            this.hintResId = hintResId
            return this
        }

        fun setSkipEnabled(skipEnabled: Boolean): Config {
            isSkipEnabled = skipEnabled
            return this
        }

        fun setNotDecidedEnabled(notDecidedEnabled: Boolean): Config {
            isNotDecidedEnabled = notDecidedEnabled
            return this
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(key)
            dest.writeInt(titleResId)
            dest.writeInt(hintResId)
            dest.writeByte(if (isSkipEnabled) 1.toByte() else 0.toByte())
            dest.writeByte(if (isNotDecidedEnabled) 1.toByte() else 0.toByte())
        }

        protected constructor(`in`: Parcel) {
            key = `in`.readString()
            titleResId = `in`.readInt()
            hintResId = `in`.readInt()
            isSkipEnabled = `in`.readByte().toInt() != 0
            isNotDecidedEnabled = `in`.readByte().toInt() != 0
        }

        companion object {
            val HOME_ADDRESS = Config("home")
                .setTitle(R.string.add_home_address)
                .setHint(R.string.search_address)
                .setSkipEnabled(true)
            val SEARCH_PLACE = Config("search")
                .setTitle(R.string.where_are_you_going)
                .setHint(R.string.i_m_going_to)
                .setNotDecidedEnabled(true)

            @JvmField
            val CREATOR: Parcelable.Creator<Config> = object : Parcelable.Creator<Config> {
                override fun createFromParcel(source: Parcel): Config {
                    return Config(source)
                }

                override fun newArray(size: Int): Array<Config?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        const val TAG = "SearchPlaceFragment"
        fun newInstance(
            config: Config?,
            backendProvider: AbstractBackendProvider,
            deviceId: String,
            realTimeUpdatesProvider: HyperTrackViews
        ): SearchPlaceFragment {
            val fragment = SearchPlaceFragment(backendProvider, deviceId)
            val bundle = Bundle()
            bundle.putParcelable("config", config)
            fragment.arguments = bundle
            return fragment
        }
    }
}
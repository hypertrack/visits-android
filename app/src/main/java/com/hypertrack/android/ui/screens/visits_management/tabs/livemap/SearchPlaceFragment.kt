package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.*
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.hypertrack.android.ui.screens.sign_up.HTTextWatcher
import com.hypertrack.backend.AbstractBackendProvider
import com.hypertrack.logistics.android.github.R

class SearchPlaceFragment private constructor(
    private val mBackendProvider: AbstractBackendProvider
    ) : Fragment(), OnMapReadyCallback, SearchPlacePresenter.View {
    private lateinit var config: Config
    private lateinit var presenter: SearchPlacePresenter
    private lateinit var search: EditText
    private lateinit var destinationOnMap: View
    private var offlineView: View? = null
    private var home: View? = null
    private lateinit var setHome: View
    private lateinit var homeInfo: View
    private lateinit var setOnMap: View
    private lateinit var confirm: View
    private var placesAdapter = PlacesAdapter()

    private var loader: LoaderDecorator? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = arguments?.getParcelable("config") ?: Config("")
        presenter = SearchPlacePresenter(requireContext(), config.key, this, mBackendProvider)    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_place, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search = view.findViewById(R.id.search)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)
        requireActivity().setTitle(config.titleResId)
        toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        setHasOptionsMenu(true)
        search.setHint(config.hintResId)
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
        val noDestination = view.findViewById<View>(R.id.no_destination)
        noDestination.visibility =
            if (config.isNotDecidedEnabled) View.VISIBLE else View.INVISIBLE
        noDestination.setOnClickListener {
            presenter.setMapDestinationModeEnable(false)
            presenter.providePlace(null)
        }
        destinationOnMap = view.findViewById(R.id.destination_on_map)
        offlineView = view.findViewById(R.id.offline)
        home = view.findViewById(R.id.home)
        setHome = view.findViewById(R.id.set_home)
        homeInfo = view.findViewById(R.id.home_info)
        val onHomeAddressClickListener = View.OnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_frame, newInstance(Config.HOME_ADDRESS, mBackendProvider), SearchPlaceFragment::class.java.simpleName)
                .addToBackStack(null)
                .commitAllowingStateLoss()
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
        loader = LoaderDecorator(context)
        presenter.search(null)
        (parentFragment as LiveMapFragment?)!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        presenter.initMap(googleMap)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search_place, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.skip).isVisible = config.isSkipEnabled
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.skip) {
            presenter.skip()
        }
        return false
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
            setHome.visibility = View.VISIBLE
            homeInfo.visibility = View.GONE
        } else {
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
        home?.visibility = View.VISIBLE
    }

    override fun hideHomeAddress() {
        home?.visibility = View.GONE
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

    override fun addShareTripFragment(tripId: String?, shareUrl: String?) {
        requireParentFragment()
            .childFragmentManager
            .beginTransaction().replace(
                R.id.fragment_frame,
                ShareTripFragment.newInstance(tripId, shareUrl, mBackendProvider),
                ShareTripFragment::class.java.simpleName
            )
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

    override fun finish() { activity?.onBackPressed() }

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
        fun newInstance(
            config: Config?,
            backendProvider: AbstractBackendProvider
        ): SearchPlaceFragment {
            val fragment = SearchPlaceFragment(backendProvider)
            val bundle = Bundle()
            bundle.putParcelable("config", config)
            fragment.arguments = bundle
            return fragment
        }
    }
}
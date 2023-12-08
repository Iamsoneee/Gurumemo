package com.sw.gurumemo.views

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sw.gurumemo.Constants
import com.sw.gurumemo.LocationProvider
import com.sw.gurumemo.MainActivity
import com.sw.gurumemo.R
import com.sw.gurumemo.adapters.SearchShopListAdapter
import com.sw.gurumemo.databinding.FragmentSearchBinding
import com.sw.gurumemo.retrofit.HotPepperService
import com.sw.gurumemo.retrofit.RetrofitConnection
import com.sw.gurumemo.retrofit.Shop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment(), View.OnClickListener {

    //    Passing latitude, longitude data from MainActivity to SearchFragment
    companion object {
        private val TAG = "SearchFragment"
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"

        fun newInstance(latitude: Double, longitude: Double): SearchFragment {
            val fragment = SearchFragment()
            val args = Bundle()
            args.putDouble(ARG_LATITUDE, latitude)
            args.putDouble(ARG_LONGITUDE, longitude)
            fragment.arguments = args
            return fragment
        }
    }

    private var binding: FragmentSearchBinding? = null

    private lateinit var locationProvider: LocationProvider
    private lateinit var adapter: SearchShopListAdapter
    private lateinit var retrofitAPI: HotPepperService


    //    private var isLoading = false
    private var query: String? = null
    private var currentPage = 1
    private var range = 3 // 検索範囲 (初期値: 1000m)
    private var order = 4 // 1:店名かな順 / 2:ジャンルコード順 / 3:小エリアコード順 / 4:おススメ順
    private val PAGE_SIZE = 30

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // to initialize parameter values
        range = 3
        order = 4

        val arguments = arguments
        if (arguments != null) {
            currentLatitude = arguments.getDouble(SearchFragment.ARG_LATITUDE, 0.0)
            currentLongitude = arguments.getDouble(SearchFragment.ARG_LONGITUDE, 0.0)
        }

        adapter = SearchShopListAdapter(this.requireContext())
        val layoutManager = LinearLayoutManager(requireContext())
        binding?.rvShopList?.layoutManager = layoutManager
        binding?.rvShopList?.adapter = adapter

        binding?.etSearchBar?.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    performSearch(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length!! > 0) {
                        binding?.ivEraseButton?.visibility = View.VISIBLE
                    } else {
                        binding?.ivEraseButton?.visibility = View.INVISIBLE
                    }
                    query = s.toString()
                }
            })

        binding?.ivBackButton?.setOnClickListener(this)
        binding?.ivEraseButton?.setOnClickListener(this)
        binding?.llCurrentLocation?.setOnClickListener(this)
        binding?.root?.setOnClickListener(this)
        binding?.etSearchBar?.setOnClickListener(this)

        binding?.tvWithin300m?.setOnClickListener(this)
        binding?.tvWithin500m?.setOnClickListener(this)
        binding?.tvWithin2km?.setOnClickListener(this)
        binding?.tvWithin3km?.setOnClickListener(this)

        binding?.tvFilter1?.setOnClickListener(this)
        binding?.tvFilter2?.setOnClickListener(this)
        binding?.tvFilter3?.setOnClickListener(this)
        binding?.tvFilter4?.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        val query: String = binding?.etSearchBar?.text.toString()
        when (v?.id) {
            R.id.iv_back_button -> {
                (requireActivity() as MainActivity).binding.bottomNavigationView.selectedItemId =
                    R.id.fragment_home
            }

            R.id.iv_erase_button -> {
                binding?.etSearchBar?.text = null
                binding?.llNoResult?.visibility = View.GONE
            }

            // current location button
            R.id.ll_current_location -> {
                locationProvider = LocationProvider(requireContext())
                lifecycleScope.launch {
                    try {
                        val address = withContext(Dispatchers.IO) {
                            locationProvider.getCurrentAddress(currentLatitude, currentLongitude)
                        }
                        Log.e(TAG, "Current address before country code check: $address")
                        address?.let {
                            if (address.countryCode.equals("JPN") || address.countryCode.equals("JP")) {
                                currentLatitude = address.latitude
                                currentLongitude = address.longitude
                                Log.e(
                                    TAG,
                                    "Current location in Japan: $currentLatitude $currentLongitude"
                                )
                            } else {
//                    setting default value in case user doesn't reside in Japan.
                                currentLatitude = Constants.DEFAULT_LATITUDE_JP
                                currentLongitude = Constants.DEFAULT_LONGITUDE_JP
                            }
                        } ?: run {
                            currentLatitude = Constants.DEFAULT_LATITUDE_JP
                            currentLongitude = Constants.DEFAULT_LONGITUDE_JP
                            Log.e(
                                TAG,
                                "Couldn't get latitude, longitude from current location. Default values are set."
                            )
                        }
                        val response = performSearch(query)
//                val response = performSearch(DEFAULT_QUERY)
                        Log.e(
                            TAG,
                            "Location after the location icon is clicked: $currentLatitude $currentLongitude"
                        )
                        Log.e(TAG, "Response after the location icon is clicked: $response")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error: ${e.message}")
                    }
                }
                binding?.rvShopList?.smoothScrollToPosition(0)
            }

            R.id.fragment_search -> {
                binding?.etSearchBar?.isCursorVisible = false
            }

            R.id.et_search_bar -> {
                binding?.etSearchBar?.isCursorVisible = true
            }

            // range buttons
            R.id.tv_within_300m -> {
                range = 1
                performSearch(query)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }

            R.id.tv_within_500m -> {
                range = 2
                performSearch(query)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }

            R.id.tv_within_2km -> {
                range = 4
                performSearch(query)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }

            R.id.tv_within_3km -> {
                range = 5
                performSearch(query)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }

            // filter buttons
            // おすすめ順
            R.id.tv_filter_1 -> {
                order = 1
                performSearch(query)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }

            // 小エリア順
            R.id.tv_filter_2 -> {
                order = 2
                performSearch(query)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }

            // ジャンル順
            R.id.tv_filter_3 -> {
                order = 3
                performSearch(query)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }
            // 店名かな順
            R.id.tv_filter_4 -> {
                order = 4
                performSearch(query)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            // 초기화
            currentPage = 1

            locationProvider = LocationProvider(requireContext())
            val location = locationProvider.getCurrentAddress(currentLatitude, currentLongitude)
            Log.e(TAG, "Location before searching: $currentLatitude $currentLongitude $location")
            Log.e(TAG, "Query before searching: $query")
            Log.e(TAG, "Range before searching: $range")
            Log.e(TAG, "Order before searching: $order")

            searchWithQuery(query, currentPage, range, order)
            Log.e(TAG, "Search with query")
        } else {
            Log.e(TAG, "Search without query")
            searchWithQuery(query, currentPage, range, order)
        }
    }

    private fun searchWithQuery(query: String, page: Int, range: Int, order: Int) {
        retrofitAPI = RetrofitConnection.getInstance().create(HotPepperService::class.java)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = retrofitAPI.getGourmetData(
                    apiKey = Constants.HOTPEPPER_API_KEY,
                    lat = currentLatitude.toString(),
                    lng = currentLongitude.toString(),
//                    name = query, // 가게 이름 검색
//                    nameKana = query,
                    nameAny = query, // 가게 이름 일부
                    keyword = query, // 키워드 검색
                    order = order,
                    range = range, // 검색 범위 설정
                    start = (page - 1) * PAGE_SIZE + 1,
                    count = PAGE_SIZE
                )
                Log.d(TAG, "API request for search response: $response")

                withContext(Dispatchers.Main) {
                    if (response.results.shop.isNotEmpty()) {
                        if (page == 1) {
                            updateUI(response.results.shop, isReplaced = true)
                        } else {
                            updateUI(response.results.shop, isReplaced = false)
                        }
                        binding?.llNoResult?.visibility = View.GONE
                    } else {
                        adapter.clearData()
                        binding?.llNoResult?.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "API request error: ${e.message}", e)
                }
            }
        }
    }

    private fun updateUI(shops: List<Shop>, isReplaced: Boolean) {
        if (isReplaced) {
            adapter.setData(shops)
        } else {
            adapter.addData(shops)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
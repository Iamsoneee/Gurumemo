package com.sw.gurumemo.views

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sw.gurumemo.Constants
import com.sw.gurumemo.LocationProvider
import com.sw.gurumemo.MainActivity
import com.sw.gurumemo.R
import com.sw.gurumemo.adapters.CustomSpinnerAdapter
import com.sw.gurumemo.adapters.SearchShopListAdapter
import com.sw.gurumemo.databinding.FragmentSearchBinding
import com.sw.gurumemo.retrofit.HotPepperService
import com.sw.gurumemo.retrofit.RetrofitConnection
import com.sw.gurumemo.retrofit.Shop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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


    private var isLoading = false
    private var query: String? = ""
    private var currentPage = 1
    private var range = 1 // 検索範囲 (初期値: 300m)
    private var order = 1 // 1:店名かな順 / 2:ジャンル順 / 3:小エリ順 / 4:おススメ順
    private var genre = ""
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

        val arguments = arguments
        if (arguments != null) {
            currentLatitude = arguments.getDouble(SearchFragment.ARG_LATITUDE, 0.0)
            currentLongitude = arguments.getDouble(SearchFragment.ARG_LONGITUDE, 0.0)
        }

        adapter = SearchShopListAdapter(this.requireContext())
        val layoutManager = LinearLayoutManager(requireContext())
        binding?.rvShopList?.layoutManager = layoutManager
        binding?.rvShopList?.adapter = adapter


        binding?.rvShopList?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                val itemTotalCount = recyclerView.adapter!!.itemCount - 1

                if (!recyclerView.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) {
                    currentPage++
                    isLoading = true
                    performSearch(query.orEmpty())
                } else {
                    isLoading = false
                }
            }
        })

        binding?.etSearchBar?.addTextChangedListener(
            object : TextWatcher {
                private var searchJob: Job? = null
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    searchJob?.cancel()
                    searchJob = lifecycleScope.launch {
//                        delay(500) // 예: 500ms 딜레이를 두고 검색 수행
                        performSearch(s.toString())
                    }
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

        binding?.tvWithin500m?.setOnClickListener(this)
        binding?.tvWithin1km?.setOnClickListener(this)
        binding?.tvWithin2km?.setOnClickListener(this)
        binding?.tvWithin3km?.setOnClickListener(this)

        binding?.tvFilter1?.setOnClickListener(this)
        binding?.tvFilter2?.setOnClickListener(this)
        binding?.tvFilter3?.setOnClickListener(this)
        binding?.tvFilter4?.setOnClickListener(this)

        val spinner = binding?.spinner
        val searchFiltersArray = resources.getStringArray(R.array.search_filters)
        val adapter =
            CustomSpinnerAdapter(requireContext(), searchFiltersArray)

        spinner?.adapter = adapter
        spinner?.setSelection(0)

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> order = 1
                    1 -> order = 2
                    2 -> order = 3
                    3 -> order = 4
                }
                lifecycleScope.launch { query?.let { performSearch(it) } }
                binding?.rvShopList?.scrollToPosition(0)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
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
                binding?.rvShopList?.scrollToPosition(0)
            }

            R.id.fragment_search -> {
                binding?.etSearchBar?.isCursorVisible = false
            }

            R.id.et_search_bar -> {
                binding?.etSearchBar?.isCursorVisible = true
            }

            // range buttons
            R.id.tv_within_500m -> {
                range = 2
                currentPage = 1
                performSearch(query)
                binding?.rvShopList?.scrollToPosition(0)
            }

            R.id.tv_within_1km -> {
                range = 3
                currentPage = 1
                performSearch(query)
                binding?.rvShopList?.scrollToPosition(0)
            }

            R.id.tv_within_2km -> {
                range = 4
                currentPage = 1
                Log.e(TAG, "current page: $currentPage")
                performSearch(query)
                binding?.rvShopList?.scrollToPosition(0)
            }

            R.id.tv_within_3km -> {
                range = 5
                currentPage = 1
                performSearch(query)
                binding?.rvShopList?.scrollToPosition(0)
            }

            // filter buttons
            // 居酒屋
            R.id.tv_filter_1 -> {
                genre = "G001"
                currentPage = 1
                performSearch(query)
                binding?.rvShopList?.scrollToPosition(0)
            }

            // 韓国料理
            R.id.tv_filter_2 -> {
                genre = "G017"
                currentPage = 1
                Log.e(TAG, "current page: $currentPage")
                performSearch(query)
                binding?.rvShopList?.scrollToPosition(0)
            }

            // イタリアン・フレンチ
            R.id.tv_filter_3 -> {
                genre = "G006"
                currentPage = 1
                performSearch(query)
                binding?.rvShopList?.scrollToPosition(0)
            }
            // 中華
            R.id.tv_filter_4 -> {
                genre = "G007"
                currentPage = 1
                performSearch(query)
                binding?.rvShopList?.scrollToPosition(0)
            }
        }
    }

    private fun performSearch(query: String) {
//        if (query.isNotEmpty()) {
        if (currentPage != 1) {
            currentPage++
            locationProvider = LocationProvider(requireContext())
            val location = locationProvider.getCurrentAddress(currentLatitude, currentLongitude)
            Log.e(
                TAG,
                "Location before searching: $currentLatitude $currentLongitude $location"
            )
            Log.e(TAG, "Query before searching: $query")
            Log.e(TAG, "Range before searching: $range")
            Log.e(TAG, "Order before searching: $order")

            searchWithQuery(query, currentPage, range, order, genre)
//                Log.e(TAG, "Search with query")
        } else {
            // 초기화
            currentPage = 1
            searchWithQuery(query, currentPage, range, order, genre)
        }
//        } else {
//            Log.e(TAG, "Search without query")
//            Log.e(TAG, "currentPage: $currentPage")
//            searchWithQuery(query, currentPage, range, order, genre)
//        }
    }


    private fun searchWithQuery(query: String, page: Int, range: Int, order: Int, genre: String) {
        retrofitAPI = RetrofitConnection.getInstance().create(HotPepperService::class.java)
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitAPI.getGourmetData(
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
                        genre = genre,
                        count = PAGE_SIZE
                    )
                }
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
                        if (!isLoading) {
                            adapter.clearData()
                            binding?.llNoResult?.visibility = View.VISIBLE
                        } else {

                        }

                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "API request error: ${e.message}", e)
                }
            }
        }
    }

    private suspend fun updateUI(shops: List<Shop>, isReplaced: Boolean) {
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
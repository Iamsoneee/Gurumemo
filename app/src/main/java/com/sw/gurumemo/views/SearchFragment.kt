package com.sw.gurumemo.views

import android.nfc.tech.MifareUltralight.PAGE_SIZE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import com.sw.gurumemo.R
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sw.gurumemo.Constants
import com.sw.gurumemo.LocationProvider
import com.sw.gurumemo.MainActivity
import com.sw.gurumemo.adapters.HomeShopListAdapter
import com.sw.gurumemo.adapters.SearchShopListAdapter
import com.sw.gurumemo.databinding.FragmentSearchBinding
import com.sw.gurumemo.retrofit.HotPepperService
import com.sw.gurumemo.retrofit.RetrofitConnection
import com.sw.gurumemo.retrofit.Shop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

class SearchFragment : Fragment(), View.OnClickListener {

    //    Passing latitude, longitude data from MainActivity to SearchFragment
    companion object {
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
    private var inputString = ""
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
        binding?.rvShopList?.layoutManager = LinearLayoutManager(this.requireContext())
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
                    if (s.toString().isNotEmpty()) {
                        performSearch(s.toString())
                    } else {
                        // Handle empty query case if needed
                    }

                }

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length!! > 0) {
                        binding?.ivEraseButton?.visibility = View.VISIBLE
                    } else {
                        binding?.ivEraseButton?.visibility = View.INVISIBLE
                    }

                    inputString = s.toString()

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
        when (v?.id) {
            R.id.iv_back_button -> {
                (requireActivity() as MainActivity).binding.bottomNavigationView.selectedItemId =
                    R.id.fragment_home
            }

            R.id.iv_erase_button -> {
                binding?.etSearchBar?.text = null
                binding?.llNoResult?.visibility = View.GONE
            }

            R.id.ll_current_location -> {
                locationProvider = LocationProvider(requireContext())
                currentLatitude = locationProvider.getLocationLatitude()
                currentLongitude = locationProvider.getLocationLongitude()
            }

            R.id.fragment_search -> {
                binding?.etSearchBar?.isCursorVisible = false
            }

            R.id.et_search_bar -> {
                binding?.etSearchBar?.isCursorVisible = true
            }
        }

        val buttonId = v?.id
        val query: String = binding?.etSearchBar?.text.toString()
        val currentPage = 1 // to initialize the page

        when (buttonId) {
            // range buttons
            R.id.tv_within_300m -> {
                range = 1
                searchWithQuery(query, currentPage, range, order)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }

            R.id.tv_within_500m -> {
                range = 2
                searchWithQuery(query, currentPage, range, order)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }

            R.id.tv_within_2km -> {
                range = 4
                searchWithQuery(query, currentPage, range, order)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }

            R.id.tv_within_3km -> {
                range = 5
                searchWithQuery(query, currentPage, range, order)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }

            // filter buttons
                // おすすめ順
            R.id.tv_filter_1 ->{
                order = 1
                searchWithQuery(query, currentPage, range, order)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }

                // 小エリア順
            R.id.tv_filter_2 ->{
                order = 2
                searchWithQuery(query, currentPage, range, order)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }

                // ジャンル順
            R.id.tv_filter_3 ->{
                order = 3
                searchWithQuery(query, currentPage, range, order)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }
                // 店名かな順
            R.id.tv_filter_4 ->{
                order = 4
                searchWithQuery(query, currentPage, range, order)
                binding?.rvShopList?.smoothScrollToPosition(0)
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            // 초기화
            currentPage = 1
            searchWithQuery(query, currentPage, range, order)
        } else {
            // Handle empty query case if needed
        }
    }

    private fun searchWithQuery(query: String, page: Int, range: Int, order: Int) {
        retrofitAPI = RetrofitConnection.getInstance().create(HotPepperService::class.java)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                isLoading = true

                val response = retrofitAPI.getGourmetData(
                    apiKey = Constants.HOTPEPPER_API_KEY,
                    lat = "34.702485",
                    lng = "135.495951",
//                    lat = currentLatitude.toString(),
//                    lng = currentLongitude.toString(),
                    name = query, // 가게 이름 검색
                    nameAny = query, // 가게 이름 일부
                    keyword = query, // 키워드 검색
                    order = order,
                    range = range, // 검색 범위 설정
                    start = (page - 1) * PAGE_SIZE + 1,
                    count = PAGE_SIZE
                )

                withContext(Dispatchers.Main) {
                    Log.d("API_RESPONSE", "Response: $response")

                    if (page == 1) {
                        updateUI(response.results.shop, isReplace = true)
                    } else {
                        updateUI(response.results.shop, isReplace = false)
                    }

                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("API_ERROR", "Error: ${e.message}", e)
                    binding?.llNoResult?.visibility = View.VISIBLE
                }
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadNextPage() {
        currentPage++
        binding?.etSearchBar?.text?.let { query ->
            searchWithQuery(query.toString(), currentPage, range, order)
        }
    }

    private fun updateUI(shops: List<Shop>, isReplace: Boolean) {

        if (shops.isNotEmpty()) {
            if (isReplace) {
                adapter.setData(shops)
            } else {
                adapter.addData(shops)
            }
            binding?.llNoResult?.visibility = View.GONE
        } else {
            adapter.clearData()
            binding?.llNoResult?.visibility = View.VISIBLE
        }

    }

    override fun onPause() {
        super.onPause()
        adapter.clearData()
    }

    override fun onResume() {
        super.onResume()
        if (inputString.isNotEmpty()){
            searchWithQuery(inputString,currentPage,range,order)
        }else{
            adapter.clearData()
        }
    }



    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
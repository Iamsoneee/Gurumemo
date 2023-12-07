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

    private var isLoading = false
    private var currentPage = 1
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
                }

            })
        binding?.ivBackButton?.setOnClickListener(this)
        binding?.ivEraseButton?.setOnClickListener(this)
        binding?.llCurrentLocation?.setOnClickListener(this)
        binding?.root?.setOnClickListener(this)
        binding?.etSearchBar?.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_back_button -> {
                (requireActivity() as MainActivity).binding.bottomNavigationView.selectedItemId =
                    R.id.fragment_home
            }

            R.id.iv_erase_button -> {
                binding?.etSearchBar?.setText(null)
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
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            // 초기화
            currentPage = 1
            searchWithQuery(query, currentPage)
        } else {
            // Handle empty query case if needed
        }
    }

    private fun searchWithQuery(query: String, page: Int) {
        val retrofitAPI = RetrofitConnection.getInstance().create(HotPepperService::class.java)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                isLoading = true

                val response = retrofitAPI.getGourmetData(
                    apiKey = Constants.HOTPEPPER_API_KEY,
//                    lat = currentLatitude.toString(),
//                    lng = currentLongitude.toString()
                    name = query, // 가게 이름 검색
                    keyword = query, // 키워드 검색
                    start = (page - 1) * PAGE_SIZE + 1,
                    count = PAGE_SIZE
                )

                val shopData = response.results.shop
                Log.e("SearchFragment", "$response")
                Log.e("SearchFragment", "${shopData[0].logo_image}")
                Log.e("SearchFragment", "$currentLatitude")
                Log.e("SearchFragment", "$currentLongitude")

                withContext(Dispatchers.Main) {
                    // 로그: 응답 결과 및 추가 정보 출력
                    Log.d("API_RESPONSE", "Response: $response")

                    if (page == 1) {
                        updateUI(response.results.shop, isReplace = true)
                    } else {
                        updateUI(response.results.shop, isReplace = false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // 로그: 에러 메시지 및 스택 트레이스 출력
                    Log.e("API_ERROR", "Error: ${e.message}", e)
                }
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadNextPage() {
        currentPage++
        binding?.etSearchBar?.text?.let { query ->
            searchWithQuery(query.toString(), currentPage)
        }
    }

    private fun updateUI(shops: List<Shop>, isReplace: Boolean) {
//        if (isReplace) {
//            adapter.setData(shops)
//        } else {
//            adapter.addData(shops)
//        }
        if (shops.isNotEmpty()) {
            if (isReplace) {
                adapter.setData(shops)
            } else {
                adapter.addData(shops)
            }
        } else {
            // Handle empty shop list case if needed
        }

    }


    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
package com.sw.gurumemo.views


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.sw.gurumemo.Constants
import com.sw.gurumemo.LocationProvider
import com.sw.gurumemo.MainActivity
import com.sw.gurumemo.R
import com.sw.gurumemo.adapters.HomeShopListAdapter
import com.sw.gurumemo.databinding.FragmentHomeBinding
import com.sw.gurumemo.retrofit.HotPepperService
import com.sw.gurumemo.retrofit.RetrofitConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    //    Passing latitude, longitude data from MainActivity to SearchFragment
    companion object {
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"

        fun newInstance(latitude: Double, longitude: Double): HomeFragment {
            Log.d("HomeFragment", "newInstance called with $latitude, $longitude")
            val fragment = HomeFragment()
            val args = Bundle().apply {
                putDouble(ARG_LATITUDE, latitude)
                putDouble(ARG_LONGITUDE, longitude)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private var binding: FragmentHomeBinding? = null

    private lateinit var adapter: HomeShopListAdapter

    private lateinit var locationProvider: LocationProvider

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    private var autoScrollJob: Job? = null
    private var autoScrollEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("HotPepper", "API Key: ${Constants.HOTPEPPER_API_KEY}")

        binding?.btnSearch?.setOnClickListener {
            (requireActivity() as MainActivity).binding.bottomNavigationView.selectedItemId =
                R.id.fragment_search
        }

        val arguments = arguments
        locationProvider = LocationProvider(requireContext())
        if (arguments != null) {
            currentLatitude = arguments.getDouble(ARG_LATITUDE, 0.0)
            currentLongitude = arguments.getDouble(ARG_LONGITUDE, 0.0)
            val currentAddress =
                locationProvider.getCurrentAddress(currentLatitude, currentLongitude)
            binding?.tvGeocoderThoroughfare?.text = currentAddress?.thoroughfare ?: "Welcome!"
            binding?.tvGeocoderCountryName?.text = currentAddress?.countryName ?: "周辺のおすすめスポット"
            binding?.tvGeocoderAdminArea?.text = currentAddress?.adminArea ?: ""
        }

        getShopImagesByCurrentLocation()
        setViewPagerWithAutoScroll()
    }

    private fun getShopImagesByCurrentLocation() {
        val retrofitAPI = RetrofitConnection.getInstance().create(HotPepperService::class.java)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = retrofitAPI.getGourmetData(
                    apiKey = Constants.HOTPEPPER_API_KEY,
//                    lat = currentLatitude.toString(),
//                    lng = currentLongitude.toString()
                )
                val shopData = response.results.shop
                Log.e("HotPepper", "$response")
                Log.e("HotPepper", "${shopData[0].logo_image}")
                Log.e("HotPepper", "$currentLatitude")
                Log.e("HotPepper", "$currentLongitude")


                withContext(Dispatchers.Main) {
                    adapter.setData(response.results.shop)
                }

//                withContext(Dispatchers.Main) {
//                    // 로그: 응답 결과 및 추가 정보 출력
//                    Log.d("API_RESPONSE", "Response: $response")
//
//                    if (page == 1) {
//                        updateUI(response.results.shop, isReplace = true)
//                    } else {
//                        updateUI(response.results.shop, isReplace = false)
//                    }
//                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("API_ERROR", "Error: ${e.message}", e)
                }
            }
        }
    }

    private suspend fun autoScroll() {
        while (autoScrollEnabled) {
            val itemCount = adapter.itemCount
            if (itemCount > 0) {
                Log.e("HomeFragment", "$itemCount")
                val currentItem = binding?.vpImageSlider?.currentItem ?: 0
                val nextItem = (currentItem + 1) % itemCount
                binding?.vpImageSlider?.setCurrentItem(nextItem, true)
            }
            delay(2000)  // Move to next every 2 seconds.
        }
    }

    private fun setViewPagerWithAutoScroll() {
        val springDotsIndicator = binding?.springDotsIndicator
        val viewPager = binding?.vpImageSlider
        adapter = HomeShopListAdapter(this.requireContext())
        viewPager?.adapter = adapter
        viewPager?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        springDotsIndicator?.attachTo(viewPager!!)

        // auto scroll function
        autoScrollJob = lifecycleScope.launch {
            autoScroll()
        }
//        val handler = Handler(Looper.getMainLooper())
//        val runnable = object : Runnable {
//            override fun run() {
//                val itemCount = adapter.itemCount
//                if (itemCount > 0) {
//                    Log.e("HomeFragment","$itemCount")
//                    val currentItem = binding?.vpImageSlider?.currentItem ?: 0
//                    val nextItem = (currentItem + 1) % itemCount
//                    binding?.vpImageSlider?.setCurrentItem(nextItem, true)
//                }
//                handler.postDelayed(this, 2000)  // Move to next every 2 seconds.
//            }
//        }
//        handler.postDelayed(runnable, 2000)  // 2 seconds delayed in the beginning

//        // click event
//        adapter.setItemClickListener(object: ShopListAdapter.OnItemClickListener{
//            override fun onClick(v: View, position: Int) {
//                adapter.
//            }
//
//        })
    }

    override fun onDestroyView() {
        autoScrollJob?.cancel()
        binding = null
        super.onDestroyView()
    }
}

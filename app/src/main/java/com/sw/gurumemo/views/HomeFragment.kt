package com.sw.gurumemo.views

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.viewpager2.widget.ViewPager2
import com.sw.gurumemo.LocationProvider
import com.sw.gurumemo.MainActivity
import com.sw.gurumemo.R
import com.sw.gurumemo.adapters.ViewPagerAdapter
import com.sw.gurumemo.databinding.FragmentHomeBinding
import java.io.IOException
import java.util.Locale

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
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var locationProvider: LocationProvider


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

        binding?.btnSearch?.setOnClickListener {
            (requireActivity() as MainActivity).binding.bottomNavigationView.selectedItemId =
                R.id.fragment_search
        }

        val arguments = arguments
        locationProvider = LocationProvider(requireContext())
        if (arguments != null) {
            val latitude = arguments.getDouble(ARG_LATITUDE, 0.0)
            val longitude = arguments.getDouble(ARG_LONGITUDE, 0.0)
            val currentAddress = locationProvider.getCurrentAddress(latitude, longitude)
            binding?.tvGeocoderThoroughfare?.text = currentAddress?.thoroughfare ?: "Welcome!"
            binding?.tvGeocoderCountryName?.text = currentAddress?.countryName ?: "周辺のおすすめスポット"
            binding?.tvGeocoderAdminArea?.text = currentAddress?.adminArea
        }
        setViewPagerWithAutoScroll()
    }

    private fun getShopList(): ArrayList<Int> {
        return arrayListOf<Int>(R.drawable.ic_home, R.drawable.ic_search, R.drawable.ic_bookmark)
    }

    private fun setViewPagerWithAutoScroll() {
        binding?.vpImageSlider?.adapter = ViewPagerAdapter(getShopList())
        binding?.vpImageSlider?.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val currentItem = binding?.vpImageSlider?.currentItem ?: 0
                val nextItem = (currentItem + 1) % getShopList().size
                binding?.vpImageSlider?.setCurrentItem(nextItem, true)
                handler.postDelayed(this, 2000)  // Move to next every 2 seconds.
            }
        }
        handler.postDelayed(runnable, 2000)  // 2 seconds delayed in the beginning
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}

package com.sw.gurumemo.views

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.sw.gurumemo.LocationProvider
import com.sw.gurumemo.MainActivity
import com.sw.gurumemo.R
import com.sw.gurumemo.databinding.FragmentSearchBinding

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

    lateinit var locationProvider: LocationProvider

    var currentLatitude: Double = 0.0
    var currentLongitude: Double = 0.0

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
            val latitude = arguments.getDouble(SearchFragment.ARG_LATITUDE, 0.0)
            val longitude = arguments.getDouble(SearchFragment.ARG_LONGITUDE, 0.0)
        }

        binding?.ivBackButton?.setOnClickListener(this)
        binding?.ivEraseButton?.setOnClickListener(this)
        binding?.llCurrentLocation?.setOnClickListener(this)

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
                binding?.tvLatLng?.text = "${currentLatitude}  ${currentLongitude}"
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
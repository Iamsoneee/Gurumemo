package com.sw.gurumemo

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sw.gurumemo.databinding.ActivityMainBinding
import com.sw.gurumemo.views.SearchFragment
import com.sw.gurumemo.views.HomeFragment
import com.sw.gurumemo.views.BookmarkFragment
import com.sw.gurumemo.LocationProvider

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private val PERMISSIONS_REQUEST_CODE = 100

    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>
    lateinit var locationProvider: LocationProvider

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAllPermissions()

        updateUI()

        setupBottomNavigationView()
        if (savedInstanceState == null) {
            binding.bottomNavigationView.selectedItemId = R.id.fragment_home
        }

    }

//    Bottom Navigation Settings

    private fun setupBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_home -> {
                    showBottomNavigation()
                    val homeFragment = HomeFragment.newInstance(latitude,longitude)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, homeFragment)
                        .commit()
                    true
                }

                R.id.fragment_search -> {
                    hideBottomNavigation()
                    val searchFragment = SearchFragment.newInstance(latitude, longitude)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, searchFragment)
                        .commit()
                    true
                }

                R.id.fragment_bookmark -> {
                    showBottomNavigation()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, BookmarkFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }
    }

    private fun hideBottomNavigation() {
        binding.bottomNavigationView.visibility = View.GONE
    }

    private fun showBottomNavigation() {
        binding.bottomNavigationView.visibility = View.VISIBLE
    }

//    Getting current location information

    private fun updateUI() {
        locationProvider = LocationProvider(this@MainActivity)

        latitude = locationProvider.getLocationLatitude()
        longitude = locationProvider.getLocationLongitude()

        if (latitude != 0.0 || longitude != 0.0) {

            val address = locationProvider.getCurrentAddress(latitude, longitude)
            address?.let {
                Log.d(
                    "Location",
                    "${address.countryName + address.adminArea + address.thoroughfare}"
                )
                Log.d("Location", "위도: ${latitude}")
                Log.d("Location", "경도: ${longitude}")

            }
        } else {
            Log.e("Location", "Couldn't get latitude, longitude from current location.")
        }
    }


//    Getting GPS permission from user

    private fun checkAllPermissions() {
        if (!isLocationServiceAvailable()) {
            showDialogForLocationServiceSetting()
        } else {
            isRunTimePermissionsGranted()
        }
    }

    private fun isLocationServiceAvailable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        return (locationManager.isProviderEnabled(
            LocationManager.GPS_PROVIDER
        ) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        ))
    }

    private fun isRunTimePermissionsGranted() {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                REQUIRED_PERMISSIONS,
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var checkResult = true
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                checkResult = false
                break
            }
        }
        if (checkResult) {
            updateUI()
        } else {
            Toast.makeText(
                this@MainActivity,
                "権現が拒否されました。アプリを再起動して権限を許可してください。",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun showDialogForLocationServiceSetting() {
        getGPSPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    if (isLocationServiceAvailable()) {
                        isRunTimePermissionsGranted()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "位置情報サービスを利用できません。",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        finish()
                    }
                }
            }
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("位置情報の使用を許可しますか？")
        builder.setMessage("位置サービスがオフになっています。アプリの利用には設定が必要です。")
        builder.setCancelable(true)
        builder.setPositiveButton("설정", DialogInterface.OnClickListener { dialog, id ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            getGPSPermissionLauncher.launch(callGPSSettingIntent)
        })
        builder.setNegativeButton("취소",
            DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
                Toast.makeText(
                    this@MainActivity,
                    "デバイスで位置サービス（GPS）を設定してからご利用ください。",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            })
        builder.create().show()
    }

}
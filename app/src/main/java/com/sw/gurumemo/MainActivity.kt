package com.sw.gurumemo

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sw.gurumemo.databinding.ActivityMainBinding
import com.sw.gurumemo.views.SearchFragment
import com.sw.gurumemo.views.HomeFragment
import com.sw.gurumemo.views.BookmarkFragment

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    lateinit var binding: ActivityMainBinding
    private lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var locationProvider: LocationProvider

    //　ランタイム権限要請コード
    private val PERMISSIONS_REQUEST_CODE = 100

    //　要請する権限リスト
    private var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )


    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, callback)

        checkAllPermissions()

        setupUI()

        //　Bottom Navigation Setting (初期値 - HomeFragment)
        setupBottomNavigationView()
        if (savedInstanceState == null) {
            binding.bottomNavigationView.selectedItemId = R.id.fragment_home
        }

    }

    //　戻るボタンのイベント設定
    private var backPressedTime = 0L

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - backPressedTime >= 1500) {
                backPressedTime = System.currentTimeMillis()
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.back_button_message),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (System.currentTimeMillis() - backPressedTime < 1500) {
                finish()
            }
        }
    }

    //　ユーザーが EditText の外側をタッチした際にキーボードを非表示にするため
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val imm: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return super.dispatchTouchEvent(ev)
    }

    //　Bottom Navigation 設定

    private fun setupBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_home -> {
                    showBottomNavigation()
                    val homeFragment = HomeFragment.newInstance(latitude, longitude)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, homeFragment)
                        .commit()
                    true
                }

                R.id.fragment_search -> {
                    showBottomNavigation()
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

    private fun showBottomNavigation() {
        binding.bottomNavigationView.visibility = View.VISIBLE
    }

    //　現在の位置情報を取得
    private fun setupUI() {
        locationProvider = LocationProvider(this@MainActivity)

        latitude = locationProvider.getLocationLatitude()
        longitude = locationProvider.getLocationLongitude()

        Log.d(TAG, "Current location before country code check: $latitude $longitude")

        if (latitude != 0.0 || longitude != 0.0) {

            val address =
                locationProvider.getCurrentAddress(latitude, longitude)

            address?.let {

                // countryCode に 'JPN' または 'JP' が含まれている場合、ユーザーが日本に居住しているものとみなす
                if (address.countryCode.equals("JPN") || address.countryCode.equals("JP")) {
                    latitude = locationProvider.getLocationLatitude()
                    longitude = locationProvider.getLocationLongitude()
                    Log.d(
                        TAG,
                        "Current location in Japan: $latitude $longitude ${address.countryCode}"
                    )
                } else {
                    // ユーザーが日本に居住していない場合のデフォルト値の設定
                    latitude = Constants.DEFAULT_LATITUDE_JP
                    longitude = Constants.DEFAULT_LONGITUDE_JP
                }
                Log.d(
                    TAG,
                    address.countryName + address.adminArea + address.thoroughfare + address.countryCode
                )
                Log.d(
                    TAG,
                    "Current location after country code check: $latitude $longitude"
                )

            }
        } else {
            Log.e(TAG, "Couldn't get latitude, longitude from current location.")
            latitude = Constants.DEFAULT_LATITUDE_JP
            longitude = Constants.DEFAULT_LONGITUDE_JP
        }
        val finalLocation =
            locationProvider.getCurrentAddress(latitude, longitude)?.countryCode
        Log.d(TAG, "Final location: $latitude $longitude $finalLocation")
    }


    // ユーザーにGPS権限をリクエスト
    private fun checkAllPermissions() {
        //　位置情報（GPS）の設定を確認
        if (!isLocationServiceAvailable()) {
            showDialogForLocationServiceSetting()
        }
        //　ランタイム権限を確認
        else {
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

        // 権限が一つでもない場合は、パーミッションのリクエストを行う
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

        //　リクエストコードが PERMISSIONS_REQUEST_CODE であり、リクエストされたパーミッションの数だけ受信された場合
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size) {
            var checkResult = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }
            if (checkResult) {
                setupUI()
                setupBottomNavigationView()
                binding.bottomNavigationView.selectedItemId = R.id.fragment_home
            } else {
                // パーミッションが拒否された場合はアプリを終了
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.permission_denial_message),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

    }

    private fun showDialogForLocationServiceSetting() {
        getGPSPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    if (isLocationServiceAvailable()) {
                        isRunTimePermissionsGranted()
                    } else {
                        //　位置情報サービス（GPS）が許可されていない場合は、アクティビティを終了
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.gps_unavailable_messsage),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        finish()
                    }
                }
            }

        //
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle(getString(R.string.gps_request_question))
        builder.setMessage(getString(R.string.gps_request_guide_message))
        builder.setCancelable(true)
        builder.setPositiveButton(getString(R.string.gps_permission_dialog_positive_button)) { _, _ ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            getGPSPermissionLauncher.launch(callGPSSettingIntent)
        }
        builder.setNegativeButton(
            getString(R.string.gps_permission_dialog_negative_button)
        ) { dialog, _ ->
            dialog.cancel()
            Toast.makeText(
                this@MainActivity,
                getString(R.string.gps_permission_dialog_negative_button_message),
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        builder.create().show()
    }

}
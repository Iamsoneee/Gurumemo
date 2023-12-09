package com.sw.gurumemo

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import com.sw.gurumemo.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.sw.gurumemo.databinding.ActivityShopDetailBinding
import com.sw.gurumemo.databinding.ActivitySplashBinding
import com.sw.gurumemo.retrofit.Shop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShopDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "ShopDetailActivity"

    private lateinit var binding: ActivityShopDetailBinding

    private var backPressedTime = 0L
    private var mMap: GoogleMap? = null

    private var shopLatitude: Double = 0.0
    private var shopLongitude: Double = 0.0
    private var shopName: String = ""
    private var shopLocation = LatLng(shopLatitude, shopLongitude)
    private var isBookmarked = false

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis()
                Toast.makeText(
                    this@ShopDetailActivity,
                    "뒤로 버튼을 한번 더 누르면 앱을 종료합니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (System.currentTimeMillis() - backPressedTime < 2000) {
                finish()
            }
        }
    }

    private fun addOnBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로 가기 버튼이 눌렸을 때 처리 동작
            }
        }

        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarShopDetailActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        binding.toolbarShopDetailActivity.setNavigationOnClickListener {
            finish()
        }

        val intent = intent
        val shop = intent.getSerializableExtra("shopData") as Shop

        shopName = shop.name
        shopLatitude = shop.lat!!
        shopLongitude = shop.lng!!

        Glide.with(this).load(shop.photo.pc.l).into(binding.ivMainImage)
        binding.tvShopName.text = shop.name

        if (shop.genre.catch.isBlank()) {
            binding.tvCatchPhrase.text = shop.catch
        } else {
            binding.tvCatchPhrase.text = shop.genre.catch
        }
        binding.tvBudget.text = shop.budget.name
        binding.tvOpeningHours.text = shop.open
        binding.tvAddressDetail.text = shop.address

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.btnBookmarkIcon.setOnClickListener {
            toggleFavoriteState(binding.btnBookmarkIcon)
        }

        binding.ivShareIcon.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shop.urls.pc)
            }
            startActivity(Intent.createChooser(sendIntent,R.string.shareMessage.toString()))
        }
    }

    private fun toggleFavoriteState(button: Button) {
        button.isSelected = !button.isSelected
        if (button.isSelected) {
            // 즐겨찾기 추가할 때 수행할 동작
            Log.e(TAG, "BOOKMARK ADDED")
            Toast.makeText(applicationContext, "Bookmark added!", Toast.LENGTH_SHORT).show()
        } else {
            // 즐겨찾기 제거할 때 수행할 동작
            Log.e(TAG, "BOOKMARK REMOVED")
            Toast.makeText(applicationContext, "Bookmark removed!", Toast.LENGTH_SHORT).show()
        }
    }


    // setting google maps fragment

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        CoroutineScope(Dispatchers.Main).launch {
            shopLocation =
                LatLng(shopLatitude, shopLongitude)

            mMap?.let {
                it.setMaxZoomPreference(20.0f)
                it.setMinZoomPreference(12.0f)
                it.moveCamera(CameraUpdateFactory.newLatLngZoom(shopLocation, 16f))

            }

            setMarker()
        }
    }

    private fun setMarker() {
        mMap?.let {
            it.clear()

            val markerOptions = MarkerOptions()
            markerOptions.position(shopLocation)
            markerOptions.title(shopName)
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant_location_accent))

            val marker = it.addMarker(markerOptions)

            it.setOnCameraIdleListener {
                marker?.let { marker -> marker.position = shopLocation }
            }
        }
    }

}
package com.sw.gurumemo

import android.content.Intent
import com.sw.gurumemo.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sw.gurumemo.databinding.ActivityShopDetailBinding
import com.sw.gurumemo.db.AppDatabase
import com.sw.gurumemo.db.BookmarkDao
import com.sw.gurumemo.db.BookmarkEntity
import com.sw.gurumemo.retrofit.Shop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShopDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "ShopDetailActivity"

    private lateinit var binding: ActivityShopDetailBinding

    private lateinit var db: AppDatabase
    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var shopId: String

    private var mMap: GoogleMap? = null

    private var shopLatitude: Double = 0.0
    private var shopLongitude: Double = 0.0
    private var shopName: String = ""
    private var shopLocation = LatLng(shopLatitude, shopLongitude)
    private var isBookmarked = false

//    val callback = object : OnBackPressedCallback(true) {
//        override fun handleOnBackPressed() {
//            if (System.currentTimeMillis() - backPressedTime >= 2000) {
//                backPressedTime = System.currentTimeMillis()
//                Toast.makeText(
//                    this@ShopDetailActivity,
//                    "뒤로 버튼을 한번 더 누르면 앱을 종료합니다.",
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else if (System.currentTimeMillis() - backPressedTime < 2000) {
//                finish()
//            }
//        }
//    }

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

        shopId = shop.id
        shopName = shop.name
        shopLatitude = shop.lat!!
        shopLongitude = shop.lng!!

        Glide.with(this).load(shop.photo.pc.l).into(binding.ivMainImage)
        binding.tvShopName.text = shop.name

        if (shop.genre.catch.isBlank()) {
            binding.tvCatchPhrase.text = shop.catch ?: shop.genre.catch
        }

        binding.tvBudget.text = shop.budget.name
        binding.tvOpeningHours.text = shop.open
        binding.tvAddressDetail.text = shop.address

        val logoUrl = shop.logo_image
        val catchPhrase = shop.catch ?: shop.genre.catch
        val access = shop.access

        db = AppDatabase.getInstance(this)!!
        bookmarkDao = db.getBookmarkDao()

        lifecycleScope.launch { isBookmarked = withContext(Dispatchers.IO){
            bookmarkDao.isShopBookmarked(shopId)
        } }


        binding.btnBookmarkIcon.setOnClickListener {
            if (binding.btnBookmarkIcon.isSelected) {
                toggleFavoriteState(binding.btnBookmarkIcon)
                deleteBookmark(shopId)
            } else {
                toggleFavoriteState(binding.btnBookmarkIcon)
                insertBookmark(shopId, logoUrl, shopName, catchPhrase, access, isBookmarked)
            }
        }


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.ivShareIcon.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shop.urls.pc)
            }
            startActivity(Intent.createChooser(sendIntent, R.string.shareMessage.toString()))
        }
    }

    // Bookmark Function
    private fun toggleFavoriteState(button: Button) {
        button.isSelected = !button.isSelected
        if (button.isSelected) {
            // 즐겨찾기 추가할 때 수행할 동작
            Log.e(TAG, "BOOKMARK ADDED")
            this.isBookmarked = true
            Toast.makeText(applicationContext, "Bookmark added!", Toast.LENGTH_SHORT).show()
        } else {
            // 즐겨찾기 제거할 때 수행할 동작
            Log.e(TAG, "BOOKMARK REMOVED")
            this.isBookmarked = false
            Toast.makeText(applicationContext, "Bookmark removed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertBookmark(
        shopId: String,
        logoUrl: String,
        shopName: String,
        catchPhrase: String,
        access: String,
        isBookmarked: Boolean
    ) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                bookmarkDao.insertBookmark(
                    BookmarkEntity(
                        shopId, logoUrl, shopName, catchPhrase, access, isBookmarked, null
                    )
                )
            }
        }
        Toast.makeText(applicationContext, "Bookmark has saved in database!", Toast.LENGTH_SHORT).show()
    }

    private fun deleteBookmark(shopId: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                bookmarkDao.deleteBookmark(shopId)
            }
        }
        Toast.makeText(applicationContext, "Bookmark has deleted from database!", Toast.LENGTH_SHORT).show()
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

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            val isShopBookmarked = withContext(Dispatchers.IO) {
                bookmarkDao.isShopBookmarked(shopId)
            }

            binding.btnBookmarkIcon.isSelected = isShopBookmarked
            isBookmarked = isShopBookmarked
        }
    }

}
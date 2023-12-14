package com.sw.gurumemo

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
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
    private lateinit var shop: Shop
    private lateinit var shopId: String

    private var mMap: GoogleMap? = null

    private var shopLatitude: Double = 0.0
    private var shopLongitude: Double = 0.0
    private var shopName: String = ""
    private var shopLocation = LatLng(shopLatitude, shopLongitude)
    private var isBookmarked = false

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

        //　homeFragment(viewPager slider), searchFragment(list item), bookmarkFragment(list item)
        // から Shop オブジェクトの情報を取得
        val intent = intent
        shop = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("shopData", Shop::class.java)!!
        } else {
            intent.getSerializableExtra("shopData") as Shop
        }

        shopId = shop.id
        shopName = shop.name
        shopLatitude = shop.lat ?: 0.0
        shopLongitude = shop.lng ?: 0.0

        Glide.with(this).load(shop.photo.pc.l).into(binding.ivMainImage)
        binding.tvShopName.text = shop.name

        val logoUrl = shop.logo_image
        val access = shop.access
        val catchPhrase = shop.catch
        val genreCatchPhrase = shop.genre.catch

        if (shop.genre.catch.isBlank()) {
            binding.tvCatchPhrase.text = catchPhrase
        } else {
           binding.tvCatchPhrase.text = genreCatchPhrase
        }

        binding.tvBudget.text = shop.budget.name
        binding.tvOpeningHours.text = shop.open
        binding.tvAddressDetail.text = shop.address


        db = AppDatabase.getInstance(this)
        bookmarkDao = db.getBookmarkDao()

        //　ブックマークアイコンのトグル状態の設定のための Room DB Query 要求
        // toggle OFF => DELETE
        // toggle ON  => INSERT
        lifecycleScope.launch {
            isBookmarked = withContext(Dispatchers.IO) {
                bookmarkDao.isShopBookmarked(shopId)
            }
        }

        binding.btnBookmarkIcon.setOnClickListener {
            if (binding.btnBookmarkIcon.isSelected) {
                toggleFavoriteState(binding.btnBookmarkIcon)
                deleteBookmark(shopId)
            } else {
                toggleFavoriteState(binding.btnBookmarkIcon)
                insertBookmark(shopId, logoUrl, shopName, catchPhrase, genreCatchPhrase, access, isBookmarked)
            }
        }


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.let {
            it.getMapAsync(this)
        }

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
            Log.d(TAG, "Bookmark added")
            this.isBookmarked = true
            Toast.makeText(applicationContext,
                getString(R.string.bookmark_saved_message), Toast.LENGTH_SHORT).show()
        } else {
            Log.d(TAG, "Bookmark removed")
            this.isBookmarked = false
            Toast.makeText(applicationContext,
                getString(R.string.bookmark_deleted_message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertBookmark(
        shopId: String,
        logoUrl: String,
        shopName: String,
        catchPhrase: String,
        genreCatchPhrase: String,
        access: String,
        isBookmarked: Boolean
    ) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                bookmarkDao.insertBookmark(
                    BookmarkEntity(
                        shopId, logoUrl, shopName, catchPhrase, genreCatchPhrase, access, isBookmarked, null
                    )
                )
            }
        }
    }

    private fun deleteBookmark(shopId: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                bookmarkDao.deleteBookmark(shopId)
            }
        }
        Log.d(TAG, "Bookmark has deleted from database")
    }


    // setting google maps fragment

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        CoroutineScope(Dispatchers.Main).launch {
            shopLocation = LatLng(shopLatitude, shopLongitude)

            mMap?.let {
                it.setMaxZoomPreference(20.0f)
                it.setMinZoomPreference(12.0f)
                it.moveCamera(CameraUpdateFactory.newLatLngZoom(shopLocation, 16f))
            }
            setMarker()
        }
    }

    // Shop オブジェクトの緯度、経度情報を使用してマップにマーカーを表示
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

        // ブックマークアイコンを UI に反映
        lifecycleScope.launch {
            val isCurrentShopBookmarked = withContext(Dispatchers.IO) {
                bookmarkDao.isShopBookmarked(shopId)
            }

            withContext(Dispatchers.Main) {
                isBookmarked = isCurrentShopBookmarked
                binding.btnBookmarkIcon.isSelected = isCurrentShopBookmarked
            }
        }
    }

}
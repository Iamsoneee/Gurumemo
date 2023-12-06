package com.sw.gurumemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import com.sw.gurumemo.databinding.ActivityShopDetailBinding
import com.sw.gurumemo.databinding.ActivitySplashBinding

class ShopDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopDetailBinding

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            Log.e("ShopDetailActivity", "뒤로가기 클릭")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarShopDetailActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //set back button
        supportActionBar?.title = null

        binding.toolbarShopDetailActivity?.setNavigationOnClickListener {
            this.onBackPressedDispatcher.addCallback(this, callback)
        }
    }
}
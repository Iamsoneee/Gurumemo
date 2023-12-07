package com.sw.gurumemo

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.bumptech.glide.Glide
import com.sw.gurumemo.databinding.ActivityShopDetailBinding
import com.sw.gurumemo.databinding.ActivitySplashBinding
import com.sw.gurumemo.retrofit.Shop

class ShopDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopDetailBinding

    private var backPressedTime = 0L

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
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //set back button
        supportActionBar?.title = null

        binding.toolbarShopDetailActivity.setNavigationOnClickListener {
            finish()
        }

        val intent = intent
        val shop = intent.getSerializableExtra("sliderShopData") as Shop

        Glide.with(this).load(shop.photo.pc.l).into(binding.ivMainImage)
        binding.tvShopName.text = shop.name
        binding.tvCatchPhrase.text = shop.catch
        binding.tvBudget.text = shop.budget.name
        binding.tvOpeningHours.text = shop.open
        binding.tvAddressDetail.text = shop.address

    }

}
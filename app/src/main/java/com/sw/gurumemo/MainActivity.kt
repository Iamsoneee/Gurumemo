package com.sw.gurumemo

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.sw.gurumemo.databinding.ActivityMainBinding
import com.sw.gurumemo.views.SearchFragment
import com.sw.gurumemo.views.HomeFragment
import com.sw.gurumemo.views.BookmarkFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private var mProgressDialog: Dialog? = null

    private var latitude: Double? = 35.68111
    private var longitude: Double? = 139.76667

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigationView()
        if (savedInstanceState == null) {
            binding.bottomNavigationView.selectedItemId = R.id.fragment_home
        }
    }

    private fun setupBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_home -> {
                    showBottomNavigation()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, HomeFragment())
                        .commit()
                    true
                }

                R.id.fragment_search -> {
                    hideBottomNavigation()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, SearchFragment())
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
}
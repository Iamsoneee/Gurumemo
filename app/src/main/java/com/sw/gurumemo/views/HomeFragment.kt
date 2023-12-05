package com.sw.gurumemo.views

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.sw.gurumemo.MainActivity
import com.sw.gurumemo.R
import com.sw.gurumemo.adapters.ViewPagerAdapter
import com.sw.gurumemo.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null
    lateinit var adapter: ViewPagerAdapter

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
                handler.postDelayed(this, 2000)  // 3초마다 전환하도록 설정
            }
        }
        handler.postDelayed(runnable, 2000)  // 처음에도 3초 후에 실행하도록 설정
    }
    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
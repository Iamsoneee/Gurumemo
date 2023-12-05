package com.sw.gurumemo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sw.gurumemo.databinding.ItemImageSliderBinding

class ViewPagerAdapter(private val shopList: ArrayList<Int>) : RecyclerView.Adapter<ViewPagerAdapter.ViewPagerHolder>() {

    inner class ViewPagerHolder(binding: ItemImageSliderBinding):RecyclerView.ViewHolder(binding.root) {
        val iv_shop = binding.ivShop
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewPagerHolder{
        val binding: ItemImageSliderBinding = ItemImageSliderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewPagerHolder(binding)
    }

    override fun getItemCount(): Int {
        return shopList.size
    }

    override fun onBindViewHolder(holder: ViewPagerHolder, position: Int) {
        holder.iv_shop.setImageResource(shopList[position])
    }

}